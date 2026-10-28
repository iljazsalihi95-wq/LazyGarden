package com.lazygarden.game

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import org.json.JSONObject
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.os.Handler
import android.os.Looper
import java.util.Locale

/** Recorded clips first, with the existing Android TTS as a temporary fallback. */
class GardenVoice(private val context:Context,private val enabled:()->Boolean={true}) {
 private var player:MediaPlayer?=null
 private var released=false
 private var lastId=""
 private var generation=0
 private val main=Handler(Looper.getMainLooper())
 data class Line(val sceneId:String,val lineId:String,val characterId:String,val language:String,val asset:String?,val subtitle:String,val durationMillis:Long)
 var currentLine:Line?=null;private set
 private var ttsReady=false
 private var pending:Line?=null
 private val tts=TextToSpeech(context.applicationContext){status->main.post{
  if(!released){ttsReady=status==TextToSpeech.SUCCESS;pending?.let{pending=null;playFallback(it)}}
 }}
 init {tts.setOnUtteranceProgressListener(object:UtteranceProgressListener(){
  override fun onStart(id:String?){main.post{if(id==lastId&&!released)talking(true)}}
  override fun onDone(id:String?){main.post{if(id==lastId){talking(false);currentLine=null}}}
  @Deprecated("Android callback") override fun onError(id:String?){main.post{if(id==lastId){talking(false);currentLine=null}}}
 })}
 var isTalking=false;private set
 var onTalkingChanged:(Boolean)->Unit={}
 private val manifest by lazy { VoiceManifest(context) }
 fun speakLine(scene:String,line:String,character:String,language:String,subtitle:String="",durationMillis:Long=0L):Boolean {
  if(released||!enabled())return false
  val id="$scene/$line/$character/$language"
  if(id==lastId&&player!=null)return true
  stop()
  val clip=manifest.find(scene,line,character,language)
  val cue=Line(scene,line,character,language,clip?.asset,subtitle,clip?.durationMillis?:durationMillis)
  currentLine=cue
  if(clip==null)return fallback(cue)
  return try {
   val media=MediaPlayer()
   player=media;lastId=id
   media.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
   context.assets.openFd(clip.asset).use{media.setDataSource(it.fileDescriptor,it.startOffset,it.length)}
   media.setOnPreparedListener{
    if(player===it&&!released&&enabled()){it.start();talking(true)}else if(player===it)stop()
   }
   media.setOnCompletionListener{if(player===it)stop()}
   media.setOnErrorListener{it,_,_->if(player===it)stop();true}
   media.prepareAsync()
   true
  }catch(_:Exception){stop();currentLine=cue;fallback(cue)}
 }
 private fun fallback(line:Line):Boolean {
  if(line.subtitle.isBlank())return false
  if(ttsReady)return playFallback(line)
  pending=line;return true
 }
 private fun playFallback(line:Line):Boolean {
  if(released||!enabled())return false
  val locale=Locale.forLanguageTag(when(line.language){"de"->"de-DE";"it"->"it-IT";"mk"->"mk-MK";"tr"->"tr-TR";else->"sq-AL"})
  if(tts.isLanguageAvailable(locale)<TextToSpeech.LANG_AVAILABLE)return false
  tts.language=locale
  val pitch=when(line.characterId){"meli"->1.06f;"rona","medina","hafsa"->1.28f;"bejdi","meti","cufi","rejani","unejsi"->1.20f;else->.88f}
  tts.setPitch(pitch);tts.setSpeechRate(if(pitch<1f).92f else 1.02f)
  currentLine=line;lastId="${line.sceneId}/${line.lineId}/${++generation}"
  return tts.speak(line.subtitle,TextToSpeech.QUEUE_FLUSH,null,lastId)==TextToSpeech.SUCCESS
 }
 private fun talking(value:Boolean){if(isTalking!=value){isTalking=value;onTalkingChanged(value)}}
 fun stop(){pending=null;lastId="";if(ttsReady)tts.stop();player?.release();player=null;currentLine=null;talking(false)}
 fun release(){stop();released=true;tts.shutdown();main.removeCallbacksAndMessages(null)}
}

/** Exact scene/line/character/language mapping; never falls back to another voice or language. */
class VoiceManifest(context:Context) {
 data class Clip(val scene:String,val line:String,val character:String,val language:String,val asset:String,val license:String,val durationMillis:Long)
 private val clips:List<Clip> = try {
  val json=JSONObject(context.assets.open("voices/manifest.json").bufferedReader().use{it.readText()})
  val entries=json.getJSONArray("clips")
  (0 until entries.length()).mapNotNull{i->
   val item=entries.getJSONObject(i)
   val path=item.getString("asset")
   val license=item.getString("license")
   if(!path.startsWith("voices/")||path.split('/').any{it==".."}||license.isBlank())null
   else Clip(item.getString("scene"),item.getString("line"),item.getString("character"),item.getString("language"),path,license,item.optLong("durationMillis",0))
  }
 }catch(_:Exception){emptyList()}
 fun find(scene:String,line:String,character:String,language:String):Clip? = clips.singleOrNull{
  it.scene==scene&&it.line==line&&it.character==character&&it.language==language
 }
}
