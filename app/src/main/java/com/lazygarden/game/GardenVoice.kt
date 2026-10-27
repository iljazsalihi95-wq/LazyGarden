package com.lazygarden.game

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** Installed Android TTS voices, not recorded actors. Never substitutes another language silently. */
class GardenVoice(context:Context,private val enabled:()->Boolean={true}):TextToSpeech.OnInitListener {
 enum class Role { MAN,WOMAN,BOY,GIRL }
 private data class Line(val text:String,val language:String,val role:Role,val id:String)
 private var ready=false;private var released=false;private var pending:Line?=null;private var lastId=""
 private val tts=TextToSpeech(context.applicationContext,this)
 override fun onInit(status:Int){if(released)return;ready=status==TextToSpeech.SUCCESS;pending?.let{pending=null;play(it)}}
 fun speak(text:String,language:String,role:Role,utterance:String){if(released||!enabled()||text.isBlank()||utterance==lastId)return;lastId=utterance;val line=Line(text,language,role,utterance);if(ready)play(line) else pending=line}
 private fun play(line:Line){if(released||!ready||!enabled())return;val locale=Locale.forLanguageTag(when(line.language){"de"->"de-DE";"it"->"it-IT";"mk"->"mk-MK";"tr"->"tr-TR";else->"sq-AL"});if(tts.isLanguageAvailable(locale)<TextToSpeech.LANG_AVAILABLE)return;tts.language=locale
  val (pitch,rate)=when(line.role){Role.MAN->.88f to .92f;Role.WOMAN->1.06f to .96f;Role.BOY->1.20f to 1.02f;Role.GIRL->1.28f to 1.04f};tts.setPitch(pitch);tts.setSpeechRate(rate);tts.speak(line.text,TextToSpeech.QUEUE_FLUSH,null,line.id)
 }
 fun stop(){pending=null;if(ready)tts.stop()}
 fun release(){if(released)return;stop();released=true;tts.shutdown();ready=false}
}
