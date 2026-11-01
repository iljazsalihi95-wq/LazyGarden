package com.lazygarden.game

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File
import kotlin.math.*

/** Original synthesized PCM Foley/cues with lightweight premium layering. */
class GardenSfx(context:Context) {
 private val pool=SoundPool.Builder().setMaxStreams(6).setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build()
 private val ids=mutableMapOf<String,Int>();private val ready=mutableSetOf<Int>();private var released=false
 init {pool.setOnLoadCompleteListener{_,id,status->if(status==0)ready.add(id)}
  listOf("water","plant","shovel","stone","hammer","brush","bloom","reward","coin","star","booster","bomb","rocket","win","fail","row","column","phone").forEachIndexed{i,key->val f=File(context.cacheDir,"lg-sfx-v2-$key.wav");if(!f.exists())f.writeBytes(wav(key,i));ids[key]=pool.load(f.absolutePath,1)}
 }
 fun play(key:String){
  if(released)return
  val id=ids[key]?:return
  if(id !in ready)return
  fun layer(volume:Float,rate:Float)=pool.play(id,volume,volume,1,0,rate.coerceIn(.5f,2f))
  when(key){
   "reward","win"->{layer(.30f,1f);layer(.16f,1.25f);layer(.10f,1.5f)}
   "star","bloom"->{layer(.27f,1f);layer(.12f,1.33f)}
   "coin"->{layer(.25f,1.12f);layer(.10f,1.48f)}
   "booster"->{layer(.28f,.92f);layer(.14f,1.32f)}
   "bomb"->{layer(.34f,.72f);layer(.16f,1.08f)}
   "rocket","row","column"->{layer(.28f,1f);layer(.11f,1.18f)}
   "fail"->layer(.25f,.88f)
   else->layer(.28f,1f)
  }
 }
 fun pause(){if(!released)pool.autoPause()}
 fun resume(){if(!released)pool.autoResume()}
 fun release(){if(!released){released=true;pool.release()}}
 private fun wav(key:String,index:Int):ByteArray {
  val rate=22050;val length=if(key in listOf("water","reward","win","phone")) .65 else .26;val n=(rate*length).toInt();val b=java.nio.ByteBuffer.allocate(44+n*2).order(java.nio.ByteOrder.LITTLE_ENDIAN)
  b.put("RIFF".toByteArray()).putInt(36+n*2).put("WAVEfmt ".toByteArray()).putInt(16).putShort(1).putShort(1).putInt(rate).putInt(rate*2).putShort(2).putShort(16).put("data".toByteArray()).putInt(n*2)
  val rng=java.util.Random(index.toLong());for(i in 0 until n){val t=i.toDouble()/rate;val e=sin(PI*i/n).pow(2)*(1-i.toDouble()/n);val noise=rng.nextDouble()*2-1;val f=when(key){"phone"->if((t*8).toInt()%2==0)660.0 else 880.0;"bomb"->65.0;"hammer","stone"->170.0;"water"->1300.0;"rocket","row"->400+2000*t;"column"->1800-1200*t;"fail"->350-220*t;else->520.0+index*35};val tone=sin(2*PI*f*t)+.35*sin(2*PI*f*1.5*t);val v=if(key in listOf("brush","shovel","plant","water","bomb"))noise*.65+tone*.2 else tone*.65;b.putShort((v*e*16000).toInt().coerceIn(-32768,32767).toShort())};return b.array()
 }
}
