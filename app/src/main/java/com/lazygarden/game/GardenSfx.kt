package com.lazygarden.game

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File
import kotlin.math.*

/** Original synthesized PCM Foley/cues. */
class GardenSfx(context:Context) {
 private val pool=SoundPool.Builder().setMaxStreams(3).setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build()
 private val ids=mutableMapOf<String,Int>();private val ready=mutableSetOf<Int>();private var released=false
 init {pool.setOnLoadCompleteListener{_,id,status->if(status==0)ready.add(id)}
  listOf("water","plant","shovel","stone","hammer","brush","bloom","reward","coin","star","booster","bomb","rocket","win","fail").forEachIndexed{i,key->val f=File(context.cacheDir,"lg-sfx-v1-$key.wav");if(!f.exists())f.writeBytes(wav(key,i));ids[key]=pool.load(f.absolutePath,1)}
 }
 fun play(key:String){if(released)return;val id=ids[key]?:return;if(id in ready)pool.play(id,.28f,.28f,1,0,1f)}
 fun pause(){if(!released)pool.autoPause()}
 fun release(){if(!released){released=true;pool.release()}}
 private fun wav(key:String,index:Int):ByteArray {
  val rate=22050;val length=if(key in listOf("water","reward","win")) .65 else .26;val n=(rate*length).toInt();val b=java.nio.ByteBuffer.allocate(44+n*2).order(java.nio.ByteOrder.LITTLE_ENDIAN)
  b.put("RIFF".toByteArray()).putInt(36+n*2).put("WAVEfmt ".toByteArray()).putInt(16).putShort(1).putShort(1).putInt(rate).putInt(rate*2).putShort(2).putShort(16).put("data".toByteArray()).putInt(n*2)
  val rng=java.util.Random(index.toLong());for(i in 0 until n){val t=i.toDouble()/rate;val e=sin(PI*i/n).pow(2)*(1-i.toDouble()/n);val noise=rng.nextDouble()*2-1;val f=when(key){"bomb"->65.0;"hammer","stone"->170.0;"water"->1300.0;"rocket"->400+2000*t;"fail"->350-220*t;else->520.0+index*35};val tone=sin(2*PI*f*t)+.35*sin(2*PI*f*1.5*t);val v=if(key in listOf("brush","shovel","plant","water","bomb"))noise*.65+tone*.2 else tone*.65;b.putShort((v*e*16000).toInt().coerceIn(-32768,32767).toShort())};return b.array()
 }
}
