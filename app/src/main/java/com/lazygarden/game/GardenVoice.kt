package com.lazygarden.game

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** Runtime dialogue voice director. Uses installed Android voices; role pitch/rate keep adults and children distinct. */
class GardenVoice(context:Context,private val enabled:()->Boolean={true}):TextToSpeech.OnInitListener {
 enum class Role { MAN, WOMAN, BOY, GIRL }
 private var ready=false
 private val tts=TextToSpeech(context.applicationContext,this)
 override fun onInit(status:Int){ready=status==TextToSpeech.SUCCESS}
 fun speak(text:String,language:String,role:Role,utterance:String){
  if(!ready||!enabled()||text.isBlank())return
  tts.language=when(language){"de"->Locale.GERMAN;"it"->Locale.ITALIAN;"mk"->Locale("mk","MK");"tr"->Locale("tr","TR");else->Locale("sq","AL")}
  val (pitch,rate)=when(role){Role.MAN->.88f to .92f;Role.WOMAN->1.06f to .96f;Role.BOY->1.20f to 1.02f;Role.GIRL->1.28f to 1.04f}
  tts.setPitch(pitch);tts.setSpeechRate(rate);tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,utterance)
 }
 fun stop(){if(ready)tts.stop()}
 fun release(){tts.stop();tts.shutdown();ready=false}
}
