package com.lazygarden.game

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File
import kotlin.math.*

/** Original synthesized PCM Foley/cues with lightweight premium layering. */
class GardenSfx(context:Context) {
 private val pool=SoundPool.Builder().setMaxStreams(10).setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build()
 private val ids=mutableMapOf<String,Int>();private val ready=mutableSetOf<Int>();private var released=false
 init {pool.setOnLoadCompleteListener{_,id,status->if(status==0)ready.add(id)}
  listOf("water","plant","shovel","stone","hammer","brush","bloom","reward","coin","star","booster","bomb","rocket","win","fail","row","column","phone","match3","match4","match5","cascade").forEachIndexed{i,key->val f=File(context.cacheDir,"lg-sfx-v6-$key.wav");if(!f.exists())f.writeBytes(wav(key,i));ids[key]=pool.load(f.absolutePath,1)}
 }
 fun play(key:String){
  if(released)return
  val id=ids[key]?:return
  if(id !in ready)return
  fun layer(volume:Float,rate:Float){pool.play(id,volume,volume,1,0,rate.coerceIn(.5f,2f))}
  when(key){
   "reward","win"->{layer(.46f,1f);layer(.24f,1.25f);layer(.16f,1.5f)}
   // BoardView uses star for cascades. Keep it bright and add an unmistakable combo accent.
   "star","bloom"->{layer(.50f,1f);layer(.28f,1.33f);if(key=="star"){playOverlay("match4",.42f,1.12f);playOverlay("cascade",.32f,1.24f)}}
   // A normal resolved swap reaches this cue, so the match-3 attack must be audible on a phone speaker.
   "coin"->{layer(.46f,1.12f);layer(.24f,1.48f);playOverlay("match3",.52f,1f)}
   "booster"->{layer(.44f,.92f);layer(.24f,1.32f)}
   "bomb"->{layer(.56f,.72f);layer(.28f,1.08f)}
   "rocket","row","column"->{layer(.48f,1f);layer(.22f,1.18f)}
   "match3"->{layer(.54f,1f);layer(.24f,1.32f)}
   "match4"->{layer(.58f,1.04f);layer(.28f,1.42f)}
   "match5"->{layer(.62f,1.08f);layer(.30f,1.48f);layer(.18f,1.72f)}
   "cascade"->{layer(.54f,1.12f);layer(.26f,1.52f)}
   "fail"->layer(.36f,.88f)
   else->layer(.38f,1f)
  }
 }
 private fun playOverlay(key:String,volume:Float,rate:Float){val id=ids[key]?:return;if(!released&&id in ready)pool.play(id,volume,volume,2,0,rate.coerceIn(.5f,2f))}
 /** Match-3 cue that grows with match size and cascade depth instead of repeating one flat click. */
 fun playMatch(matchSize:Int,cascadeDepth:Int=0){if(released)return;val key=when{matchSize>=5->"match5";matchSize==4->"match4";else->"match3"};play(key);if(cascadeDepth>0){val id=ids["cascade"]?:return;if(id in ready){val rate=(1.0f+cascadeDepth.coerceAtMost(5)*.09f).coerceAtMost(1.55f);pool.play(id,.42f,.42f,2,0,rate)}}}
 fun pause(){if(!released)pool.autoPause()};fun resume(){if(!released)pool.autoResume()};fun release(){if(!released){released=true;pool.release()}}
 private fun wav(key:String,index:Int):ByteArray {val rate=22050;val length=if(key in listOf("water","reward","win","phone")) .65 else if(key in listOf("match5","cascade")) .34 else .26;val n=(rate*length).toInt();val b=java.nio.ByteBuffer.allocate(44+n*2).order(java.nio.ByteOrder.LITTLE_ENDIAN);b.put("RIFF".toByteArray()).putInt(36+n*2).put("WAVEfmt ".toByteArray()).putInt(16).putShort(1).putShort(1).putInt(rate).putInt(rate*2).putShort(2).putShort(16).put("data".toByteArray()).putInt(n*2);val rng=java.util.Random(index.toLong());for(i in 0 until n){val t=i.toDouble()/rate;val e=sin(PI*i/n).pow(2)*(1-i.toDouble()/n);val noise=rng.nextDouble()*2-1;val f=when(key){"phone"->if((t*8).toInt()%2==0)660.0 else 880.0;"bomb"->65.0;"hammer","stone"->170.0;"water"->1300.0;"rocket","row"->400+2000*t;"column"->1800-1200*t;"fail"->350-220*t;"match3"->610+240*t;"match4"->690+420*t;"match5"->760+700*t;"cascade"->850+900*t;else->520.0+index*35};val tone=sin(2*PI*f*t)+.35*sin(2*PI*f*1.5*t);val sparkle=if(key in listOf("match4","match5","cascade")) .18*sin(2*PI*(f*2.05)*t) else 0.0;val v=if(key in listOf("brush","shovel","plant","water","bomb"))noise*.65+tone*.2 else tone*.62+sparkle;val impact=ConstructionFoley.sample(key,t,noise);val sample=if(impact==null)v*e else impact*ConstructionFoley.envelope(t);b.putShort((sample*16000).toInt().coerceIn(-32768,32767).toShort())};return b.array()}
}
