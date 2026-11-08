import {test} from 'node:test';
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
const source=await readFile(new URL('../src/index.js',import.meta.url),'utf8');
const {default:worker}=await import('data:text/javascript;base64,'+Buffer.from(source).toString('base64'));
const request=(language='sq',character_id='lazi')=>new Request('https://garden.test/api/voice/speech',{
  method:'POST',headers:{'content-type':'application/json'},
  body:JSON.stringify({language,character_id,text:'Mirë se erdhët në kopsht.'})
});
// Synthetic credentials only; no live API calls.
test('speech unwraps pasted credentials, preserves language and streams audio',async()=>{
  const original=globalThis.fetch;
  try{
    for(const value of ['sk-test-123',' "sk-test-123" ',"'sk-test-123'",'Bearer sk-test-123','"Bearer sk-test-123"','Bearer "sk-test-123"']){
      let calls=0;
      globalThis.fetch=async(url,options)=>{
        calls++;
        assert.equal(url,'https://api.openai.com/v1/audio/speech');
        assert.equal(options.headers.authorization,'Bearer sk-test-123');
        const payload=JSON.parse(options.body);
        assert.equal(payload.input,'Mirë se erdhët në kopsht.');
        assert.match(payload.instructions,/Albanian/);
        assert.equal(payload.voice,'onyx');
        return new Response(new Uint8Array([73,68,51,1]),{headers:{'content-type':'audio/mpeg'}});
      };
      const response=await worker.fetch(request(),{OPENAI_API_KEY:value});
      assert.equal(response.status,200);
      assert.equal(response.headers.get('content-type'),'audio/mpeg');
      assert.equal((await response.arrayBuffer()).byteLength,4);
      assert.equal(calls,1);
    }
  }finally{globalThis.fetch=original;}
});
test('invalid configuration and unsupported languages never contact provider or leak values',async()=>{
  const original=globalThis.fetch;
  try{
    globalThis.fetch=async()=>{throw new Error('unexpected provider request');};
    for(const value of ['', 'not-a-key', 'sk-test bad']){
      const response=await worker.fetch(request(),{OPENAI_API_KEY:value});
      assert.equal(response.status,503);
      assert.deepEqual(await response.json(),{success:false,error:value?'INVALID_OPENAI_KEY_FORMAT':'OPENAI_NOT_CONFIGURED'});
    }
    const response=await worker.fetch(request('xx'),{OPENAI_API_KEY:'sk-test-123'});
    assert.equal(response.status,400);
  }finally{globalThis.fetch=original;}
});
test('provider failures never become fake audio or expose upstream bodies',async()=>{
  const original=globalThis.fetch;
  try{
    globalThis.fetch=async()=>new Response('private upstream details',{status:401});
    const response=await worker.fetch(request(),{OPENAI_API_KEY:'sk-test-123'});
    assert.equal(response.status,502);
    assert.deepEqual(await response.json(),{success:false,error:'SPEECH_PROVIDER_ERROR',status:401});
    globalThis.fetch=async()=>new Response('{}',{headers:{'content-type':'application/json'}});
    const invalid=await worker.fetch(request(),{OPENAI_API_KEY:'sk-test-123'});
    assert.equal((await invalid.json()).error,'INVALID_SPEECH_PROVIDER_AUDIO');
  }finally{globalThis.fetch=original;}
});
