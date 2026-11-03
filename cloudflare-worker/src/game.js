// LazyGarden gameplay API. D1 binding: DB. No real-money or WET wallet credits.
const json=(v,s=200)=>new Response(JSON.stringify(v),{status:s,headers:{"content-type":"application/json","cache-control":"no-store","access-control-allow-origin":"*","access-control-allow-headers":"authorization,content-type","access-control-allow-methods":"GET,POST,OPTIONS"}});
const err=(code,status=400)=>json({ok:false,error:code},status);
const now=()=>Math.floor(Date.now()/1000);
const uuid=()=>crypto.randomUUID();
const sha=async s=>Array.from(new Uint8Array(await crypto.subtle.digest("SHA-256",new TextEncoder().encode(s)))).map(x=>x.toString(16).padStart(2,"0")).join("");
const bearer=r=>/^Bearer ([a-zA-Z0-9_-]{40,})$/.exec(r.headers.get("authorization")||"")?.[1];
const randomToken=()=>Array.from(crypto.getRandomValues(new Uint8Array(32))).map(x=>x.toString(16).padStart(2,"0")).join("");
const body=async r=>{try{return await r.json()}catch{return null}};
async function player(r,db){
 const token=bearer(r);if(!token)return null;
 const hash=await sha(token);
 return db.prepare("SELECT p.id FROM sessions s JOIN players p ON p.id=s.player_id WHERE s.token_hash=? AND s.expires_at>?").bind(hash,now()).first();
}
async function state(db,id){
 const p=await db.prepare("SELECT level,stars,coins,lives,lives_updated,chapter,revision FROM progress WHERE player_id=?").bind(id).first();
 const inv=await db.prepare("SELECT item,quantity FROM inventory WHERE player_id=?").bind(id).all();
 return {ok:true,progress:p,inventory:inv.results};
}
async function handler(r,env){
 if(r.method==="OPTIONS")return json({ok:true});
 const path=new URL(r.url).pathname,db=env.DB;
 if(path==="/api/game/health")return json({ok:true,service:"lazygarden-game",database_configured:!!db});
 if(!db)return err("DATABASE_NOT_CONFIGURED",503);
 if(path==="/api/game/guest"&&r.method==="POST"){
  const id=uuid(),token=randomToken(),t=now();
  await db.batch([
   db.prepare("INSERT INTO players(id,created_at,last_seen) VALUES(?,?,?)").bind(id,t,t),
   db.prepare("INSERT INTO progress(player_id,lives_updated) VALUES(?,?)").bind(id,t),
   db.prepare("INSERT INTO sessions(token_hash,player_id,expires_at) VALUES(?,?,?)").bind(await sha(token),id,t+2592000)
  ]);
  return json({ok:true,player_id:id,session_token:token,expires_at:t+2592000});
 }
 const p=await player(r,db);if(!p)return err("UNAUTHORIZED",401);
 if(path==="/api/game/state"&&r.method==="GET")return json(await state(db,p.id));
 if(path==="/api/game/attempt/start"&&r.method==="POST"){
  const data=await body(r),level=Number(data?.level);
  if(!Number.isSafeInteger(level)||level<1)return err("INVALID_LEVEL");
  const s=await db.prepare("SELECT level,lives FROM progress WHERE player_id=?").bind(p.id).first();
  if(level!==s.level)return err("LEVEL_LOCKED",409);
  if(s.lives<1)return err("NO_LIVES",409);
  const id=uuid(),seed=crypto.getRandomValues(new Uint32Array(1))[0],moves=Math.max(12,25-Math.floor(level/10)),target=8+Math.min(30,level);
  const t=now();
  await db.batch([
   db.prepare("UPDATE progress SET lives=lives-1,revision=revision+1 WHERE player_id=? AND lives>0").bind(p.id),
   db.prepare("INSERT INTO level_attempts(id,player_id,level,seed,moves,target,expires_at) VALUES(?,?,?,?,?,?,?)").bind(id,p.id,level,seed,moves,target,t+3600)
  ]);
  return json({ok:true,attempt_id:id,level,seed,moves,target,expires_at:t+3600});
 }
 if(path==="/api/game/attempt/submit"&&r.method==="POST"){
  const d=await body(r);if(typeof d?.attempt_id!=="string"||!Array.isArray(d.moves)||d.moves.length>100)return err("INVALID_ATTEMPT");
  const a=await db.prepare("SELECT * FROM level_attempts WHERE id=? AND player_id=?").bind(d.attempt_id,p.id).first();
  if(!a||a.consumed||a.expires_at<now())return err("ATTEMPT_EXPIRED",409);
  // Until a deterministic server-side Match-3 replay is implemented, do not trust a client win.
  // Store the move transcript for verification; do not award coins, stars, or WET points.
  const eid=uuid();
  const res=await db.prepare("UPDATE level_attempts SET consumed=1 WHERE id=? AND player_id=? AND consumed=0").bind(a.id,p.id).run();
  if(res.meta.changes!==1)return err("ALREADY_SUBMITTED",409);
  await db.prepare("INSERT INTO game_events(id,player_id,kind,level,payload,created_at) VALUES(?,?,?,?,?,?)").bind(eid,p.id,"attempt_pending",a.level,JSON.stringify({attempt_id:a.id,moves:d.moves}),now()).run();
  return json({ok:true,status:"pending_verification",rewarded:false,event_id:eid},202);
 }
 return err("NOT_FOUND",404);
}
export {handler as gameHandler};
