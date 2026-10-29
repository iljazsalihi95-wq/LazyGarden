package com.lazygarden.game

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.util.Locale
import java.util.concurrent.Executors

/** AI/recorded voice first, Android TTS only as an offline fallback. */
class GardenVoice(private val context:Context,private val enabled:()->Boolean={true}) {
 companion object {
  private const val VOICE_BACKEND="https://script.google.com/macros/s/AKfycby5NE2pnupggwtbbSn07lBSIUKns1rhO6kFkvKI4RzzzzDN0sE7XKD1TFTQy2UM68lv/exec"
 }
 private var player:MediaPlayer?=null
 private var released=false
 private var lastId=""
 private var generation=0
 private val main=Handler(Looper.getMainLooper())
 private val network=Executors.newSingleThreadExecutor()
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
  if(isTalking||player!=null||currentLine!=null){queued=cue;return true}
  return play(cue)
 }
 private fun play(cue:Line):Boolean{
  currentLine=cue
  cue.asset?.let{return playAsset(cue,it)}
  // Ask the Sheet/Apps Script backend for the exact scene/line/language voice already generated/cached.
  fetchAiAudio(cue)
  return true
 }
 private fun playAsset(cue:Line,asset:String):Boolean = try {
  val media=MediaPlayer();player=media;lastId="${cue.sceneId}/${cue.lineId}/${cue.characterId}/${cue.language}"
  media.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
  context.assets.openFd(asset).use{media.setDataSource(it.fileDescriptor,it.startOffset,it.length)}
  prepare(media,cue);true
 }catch(_:Exception){player?.release();player=null;fallback(cue)}
 private fun fetchAiAudio(cue:Line){
  val scene=URLEncoder.encode(cue.sceneId,"UTF-8");val line=URLEncoder.encode(cue.lineId,"UTF-8");val lang=URLEncoder.encode(cue.language,"UTF-8")
  network.execute{
   var connection:HttpURLConnection?=null
   try{
    val endpoint="$VOICE_BACKEND?action=line&lang=$lang&scene_id=$scene&line_id=$line"
    connection=(URL(endpoint).openConnection() as HttpURLConnection).apply{instanceFollowRedirects=true;connectTimeout=4500;readTimeout=6000;requestMethod="GET"}
    val json=connection.inputStream.bufferedReader().use{it.readText()}
    val root=JSONObject(json);val row=root.optJSONObject("line")
    val audio=row?.optString("audio_url","").orEmpty()
    main.post{if(!released&&currentLine===cue){if(audio.isNotBlank())playRemote(cue,audio) else fallback(cue)}}
   }catch(_:Exception){main.post{if(!released&&currentLine===cue)fallback(cue)}}finally{connection?.disconnect()}
  }
 }
 private fun playRemote(cue:Line,url:String){
  try{
   val media=MediaPlayer();player=media;lastId="${cue.sceneId}/${cue.lineId}/${cue.characterId}/${cue.language}"
   media.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
   media.setDataSource(url);prepare(media,cue)
  }catch(_:Exception){player?.release();player=null;fallback(cue)}
 }
 private fun prepare(media:MediaPlayer,cue:Line){
  media.setOnPreparedListener{if(player===it&&!released&&enabled()){it.start();talking(true)}else if(player===it)stop()}
  media.setOnCompletionListener{if(player===it){it.release();player=null;talking(false);currentLine=null;playQueued()}}
  media.setOnErrorListener{it,_,_->if(player===it){it.release();player=null;talking(false);fallback(cue)};true}
  media.prepareAsync()
 }
 private fun playQueued(){val next=queued?:return;queued=null;if(!released&&enabled())play(next)}
 private fun fallback(line:Line):Boolean {
  if(line.subtitle.isBlank()){currentLine=null;playQueued();return false}
  if(ttsReady)return playFallback(line)
  pending=line;return true
 }
 private fun playFallback(line:Line):Boolean {
  if(released||!enabled())return false
  val locale=Locale.forLanguageTag(when(line.language){"de"->"de-DE";"it"->"it-IT";"mk"->"mk-MK";"tr"->"tr-TR";else->"sq-AL"})
  if(tts.isLanguageAvailable(locale)<TextToSpeech.LANG_AVAILABLE){currentLine=null;playQueued();return false}
  tts.language=locale
  val localVoices=tts.voices?.filter{it.locale.language==locale.language&&!it.isNetworkConnectionRequired}?.sortedWith(compareByDescending<android.speech.tts.Voice>{it.quality}.thenBy{it.latency}).orEmpty()
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
 fun release(){stop();released=true;tts.shutdown();network.shutdownNow();main.removeCallbacksAndMessages(null)}
}

/** Exact packaged scene/line/character/language mapping; used before network audio. */
class VoiceManifest(context:Context) {
 data class Clip(val scene:String,val line:String,val character:String,val language:String,val asset:String,val license:String,val durationMillis:Long)
 private val clips:List<Clip> = try {
  val json=JSONObject(context.assets.open("voices/manifest.json").bufferedReader().use{it.readText()})
  val entries=json.getJSONArray("clips")
  (0 until entries.length()).mapNotNull{i->
   val item=entries.getJSONObject(i);val path=item.getString("asset");val license=item.getString("license")
   if(!path.startsWith("voices/")||path.split('/').any{it==".."}||license.isBlank())null
   else Clip(item.getString("scene"),item.getString("line"),item.getString("character"),item.getString("language"),path,license,item.optLong("durationMillis",0))
  }
 }catch(_:Exception){emptyList()}
 fun find(scene:String,line:String,character:String,language:String):Clip? = clips.singleOrNull{it.scene==scene&&it.line==line&&it.character==character&&it.language==language}
}
