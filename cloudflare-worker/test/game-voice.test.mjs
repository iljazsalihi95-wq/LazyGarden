import {test} from 'node:test';
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import {webcrypto} from 'node:crypto';
if(!globalThis.crypto)globalThis.crypto=webcrypto;
const source=await readFile(new URL('../src/index.js',import.meta.url),'utf8');
const {default:worker}=await import('data:text/javascript;base64,'+Buffer.from(source).toString('base64'));
const audio=()=>new Response(new Uint8Array(512).fill(42),{headers:{'Content-Type':'audio/mpeg'}});
const request=(language='sq',text='Nuk del ujë. Lazi, na ndihmon?')=>new Request('https://garden.test/api/game/voice',{
 method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({scene_id:'fountain_quest',line_id:'call_lazi',character_id:'meli',language,text})});
// Transport simulations only. No external request, secret or provider charge.
test('a produced Studio clip plays without contacting the speech provider',async()=>{
 const original=globalThis.fetch;let calls=0;
 try{
  globalThis.fetch=async(url,opts)=>{calls++;assert.match(url,/studio.*\/api\/game\/voice$/);assert.equal(JSON.parse(opts.body).scene_id,'fountain_quest');return audio();};
  const result=await worker.fetch(request(),{});
  assert.equal(result.status,200);assert.equal(result.headers.get('X-Garden-Voice-Source'),'studio-r2');
  assert.equal((await result.arrayBuffer()).byteLength,512);assert.equal(calls,1);
 }finally{globalThis.fetch=original;}
});
test('missing interactive dialogue reaches real speech with the exact language and text',async()=>{
 const original=globalThis.fetch;
 try{
  for(const [lang,text,language]of[['sq','Nuk del ujë. Lazi, na ndihmon?','Albanian'],['it','Non esce acqua. Lazi, ci aiuti?','Italian']]){
   let calls=0;
   globalThis.fetch=async(url,opts)=>{
    calls++;
    if(url.includes('studio'))return Response.json({error:'UNKNOWN_GAME_DIALOGUE'},{status:404});
    assert.equal(url,'https://api.openai.com/v1/audio/speech');
    const p=JSON.parse(opts.body);assert.equal(p.input,text);assert.match(p.instructions,new RegExp(language));assert.equal(p.voice,'nova');return audio();
   };
   const result=await worker.fetch(request(lang,text),{OPENAI_API_KEY:'sk-test-123'});
   assert.equal(result.status,200);assert.equal(result.headers.get('X-Garden-Voice-Source'),'speech');assert.equal(calls,2);
  }
 }finally{globalThis.fetch=original;}
});
test('cached playback does not generate or look up the same clip again',async()=>{
 const original=globalThis.fetch,oldCaches=globalThis.caches;const saved=new Map();let calls=0;
 try{
  globalThis.caches={default:{match:async key=>saved.get(key.url)?.clone(),put:async(key,value)=>{saved.set(key.url,value);}}};
  globalThis.fetch=async()=>{calls++;return audio();};
  const first=await worker.fetch(request(),{});assert.equal(first.status,200);
  const second=await worker.fetch(request(),{});assert.equal(second.headers.get('X-Garden-Voice-Source'),'cache');assert.equal(calls,1);
  await worker.fetch(request('it','Non esce acqua.'),{});assert.equal(calls,2);
 }finally{globalThis.fetch=original;globalThis.caches=oldCaches;}
});
test('concurrent requests share production and configuration errors stay JSON',async()=>{
 const original=globalThis.fetch;let calls=0;
 try{
  globalThis.fetch=async()=>{calls++;await new Promise(resolve=>setTimeout(resolve,15));return audio();};
  const results=await Promise.all([worker.fetch(request(),{}),worker.fetch(request(),{})]);
  assert.equal(calls,1);for(const r of results)assert.equal((await r.arrayBuffer()).byteLength,512);
  globalThis.fetch=async()=>Response.json({error:'VOICE_NOT_PRODUCED'},{status:404});
  const bad=await worker.fetch(request(),{OPENAI_API_KEY:'not-a-key'});
  assert.equal(bad.status,503);assert.equal((await bad.json()).error,'INVALID_OPENAI_KEY_FORMAT');
 }finally{globalThis.fetch=original;}
});
