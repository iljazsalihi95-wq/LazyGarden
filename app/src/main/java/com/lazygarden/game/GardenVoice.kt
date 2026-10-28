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

/** Recorded clips first, with Android TTS as an offline fallback. Dialogue never cuts an active speaker mid-line. */
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
 private var queued:Line?=null
 private val femaleIds=setOf("meli","rona","medina","hafsa")
 private val childIds=setOf("rona","medina","hafsa","bejdi","meti","cufi","rejani","unejsi")
 private val tts=TextToSpeech(context.applicationContext){status->main.post{
  if(!released){ttsReady=status==TextToSpeech.SUCCESS;pending?.let{pending=null;playFallback(it)}}
 }}
 init {tts.setOnUtteranceProgressListener(object:UtteranceProgressListener(){
  override fun onStart(id:String?){main.post{if(id==lastId&&!released)talking(true)}}
  override fun onDone(id:String?){main.post{if(id==lastId){talking(false);currentLine=null;playQueued()}}}
  @Deprecated("Android callback") override fun onError(id:String?){main.post{if(id==lastId){talking(false);currentLine=null;playQueued()}}}
 })}
 var isTalking=false;private set
 var onTalkingChanged:(Boolean)->Unit={}
 private val manifest by lazy { VoiceManifest(context) }
 fun speakLine(scene:String,line:String,character:String,language:String,subtitle:String="",durationMillis:Long=0L):Boolean {
  if(released||!enabled())return false
  val id="$scene/$line/$character/$language"
  if(id==lastId)return true
  val clip=manifest.find(scene,line,character,language)
  val cue=Line(scene,line,character,language,clip?.asset,subtitle,clip?.durationMillis?:durationMillis)
  // Scene timers may advance before speech ends or while a recorded clip prepares.
  if(isTalking||player!=null){queued=cue;return true}
  return play(cue)
 }
 private fun play(cue:Line):Boolean{
  val clip=cue.asset
  currentLine=cue
  if(clip==null)return fallback(cue)
  return try {
   val media=MediaPlayer();player=media;lastId="${cue.sceneId}/${cue.lineId}/${cue.characterId}/${cue.language}"
   media.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
   context.assets.openFd(clip).use{media.setDataSource(it.fileDescriptor,it.startOffset,it.length)}
   media.setOnPreparedListener{if(player===it&&!released&&enabled()){it.start();talking(true)}else if(player===it)stop()}
   media.setOnCompletionListener{if(player===it){it.release();player=null;talking(false);currentLine=null;playQueued()}}
   media.setOnErrorListener{it,_,_->if(player===it){it.release();player=null;talking(false);currentLine=cue;fallback(cue)};true}
   media.prepareAsync();true
  }catch(_:Exception){player?.release();player=null;currentLine=cue;fallback(cue)}
 }
 private fun playQueued(){val next=queued?:return;queued=null;if(!released&&enabled())play(next)}
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
  val localVoices=tts.voices?.filter{it.locale.language==locale.language&&!it.isNetworkConnectionRequired}?.sortedWith(compareByDescending<android.speech.tts.Voice>{it.quality}.thenBy{it.latency}).orEmpty()
  // Android has no reliable gender metadata. Keep separate stable local voices when available; recorded/AI clips can replace them later.
  if(localVoices.isNotEmpty()){
   val index=if(line.characterId in femaleIds&&localVoices.size>1)1 else 0
   tts.voice=localVoices[index.coerceAtMost(localVoices.lastIndex)]
  }
  val pitch=when{line.characterId in femaleIds->1.08f;line.characterId in childIds->1.04f;else->.94f}
  val rate=if(line.characterId in childIds).91f else .88f
  tts.setPitch(pitch);tts.setSpeechRate(rate)
  currentLine=line;lastId="${line.sceneId}/${line.lineId}/${++generation}"
  return tts.speak(line.subtitle,TextToSpeech.QUEUE_FLUSH,null,lastId)==TextToSpeech.SUCCESS
 }
 private fun talking(value:Boolean){if(isTalking!=value){isTalking=value;onTalkingChanged(value)}}
 fun stop(){pending=null;queued=null;lastId="";if(ttsReady)tts.stop();player?.release();player=null;currentLine=null;talking(false)}
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
 fun find(scene:String,line:String,character:String,language:String):Clip? = clips.singleOrNull{it.scene==scene&&it.line==line&&it.character==character&&it.language==language}
}
