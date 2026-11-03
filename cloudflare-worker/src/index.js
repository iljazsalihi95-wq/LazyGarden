const H={"content-type":"application/json; charset=utf-8","cache-control":"no-store","access-control-allow-origin":"*","access-control-allow-headers":"content-type","access-control-allow-methods":"GET, POST, OPTIONS"};
const reply=(data,status=200)=>new Response(JSON.stringify(data),{status,headers:H});
const clean=(v,max=6000)=>String(v??"").replace(/\s+/g," ").trim().slice(0,max);

async function gardenAI(request,env){
  if(!env.OPENAI_API_KEY)return reply({success:false,error:"OPENAI_NOT_CONFIGURED"},503);
  let p;
  try{p=await request.json();}catch{return reply({success:false,error:"INVALID_JSON"},400);}
  const input=clean(p.input||p.prompt);
  if(!input)return reply({success:false,error:"INPUT_REQUIRED"},400);
  const instructions=clean(p.system||"You are the AI story and hint assistant for LazyGarden, a family-friendly garden Match-3 game. Keep replies concise, helpful and consistent with story context supplied by the game.",2000);
  const r=await fetch("https://api.openai.com/v1/responses",{method:"POST",headers:{authorization:`Bearer ${env.OPENAI_API_KEY}`,"content-type":"application/json"},body:JSON.stringify({model:env.OPENAI_TEXT_MODEL||"gpt-6-luna",instructions,input})});
  let data;
  try{data=await r.json();}catch{return reply({success:false,error:"UPSTREAM_RESPONSE_ERROR"},502);}
  if(!r.ok)return reply({success:false,error:"OPENAI_ERROR",status:r.status},502);
  const text=(data.output||[]).flatMap(x=>x.content||[]).filter(x=>x.type==="output_text").map(x=>x.text).join("\n").trim();
  return reply({success:true,text,response_id:data.id});
}


const speakerVoices={lazi:"onyx",meli:"nova",bejdi:"echo",meti:"fable",cufi:"alloy",rona:"shimmer",rejani:"echo",medina:"nova",hafsa:"shimmer",unejsi:"fable",greku:"onyx",rexha:"echo",nermiri:"onyx",beka:"alloy",gezimi:"fable",besi:"echo",mevludini:"onyx",luli:"alloy"};
async function gardenSpeech(request,env){
  if(!env.OPENAI_API_KEY)return reply({success:false,error:"OPENAI_NOT_CONFIGURED"},503);
  let data;try{data=await request.json()}catch{return reply({success:false,error:"INVALID_JSON"},400)}
  const character=clean(data.character_id,32).toLowerCase(),text=clean(data.text,1200);
  if(!Object.hasOwn(speakerVoices,character)||!text)return reply({success:false,error:"INVALID_SPEECH_REQUEST"},400);
  const language=clean(data.language,8);
  const instruction="Speak naturally as the fictional LazyGarden character "+character+". Language: "+language+". Keep a warm, clear storytelling delivery.";
  const upstream=await fetch("https://api.openai.com/v1/audio/speech",{method:"POST",headers:{"authorization":"Bearer "+env.OPENAI_API_KEY,"content-type":"application/json"},body:JSON.stringify({model:env.OPENAI_TTS_MODEL||"gpt-4o-mini-tts",voice:speakerVoices[character],input:text,instructions:instruction,response_format:"mp3"})});
  if(!upstream.ok)return reply({success:false,error:"SPEECH_PROVIDER_ERROR",status:upstream.status},502);
  return new Response(upstream.body,{status:200,headers:{"content-type":"audio/mpeg","cache-control":"no-store","access-control-allow-origin":"*"}});
}

export default{async fetch(request,env){
  const u=new URL(request.url);
  if(request.method==="OPTIONS")return new Response(null,{status:204,headers:H});
  if(request.method==="GET"&&(u.pathname==="/"||u.pathname==="/health"))return reply({success:true,service:"lazygarden-backend",openai_configured:Boolean(env.OPENAI_API_KEY)});
  if(request.method==="POST"&&u.pathname==="/api/voice/speech")return gardenSpeech(request,env);
  if(request.method==="POST"&&(u.pathname==="/api/ai/respond"||u.pathname==="/api/garden/ai"))return gardenAI(request,env);
  return reply({success:false,error:"NOT_FOUND"},404);
}};
