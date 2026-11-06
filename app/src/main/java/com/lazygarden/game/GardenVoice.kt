package com.lazygarden.game

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.util.Locale
import java.util.concurrent.Executors

/** AI/recorded audio only. Offline playback uses previously cached clips. */
class GardenVoice(private val context:Context,private val enabled:()->Boolean={true}) {
 companion object {
  @Volatile private var workerRetryAt=0L
  @Volatile private var scriptRetryAt=0L
  private const val VOICE_BACKEND="https://script.google.com/macros/s/AKfycby5NE2pnupggwtbbSn07lBSIUKns1rhO6kFkvKI4RzzzzDN0sE7XKD1TFTQy2UM68lv/exec"
  private const val SPEECH_BACKEND="https://lazygarden-backend.lazisalihi.workers.dev/api/voice/speech"
 }
 private val speechCache=SpeechCache(java.io.File(context.filesDir,"voice_cache"))
 private fun online():Boolean=runCatching{val cm=context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager;cm.getNetworkCapabilities(cm.activeNetwork)?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)==true}.getOrDefault(false)
 private var player:MediaPlayer?=null
 private var cachedSpeechFile:java.io.File?=null
 private var released=false
 private var lastId=""
 private val main=Handler(Looper.getMainLooper())
 private val network=Executors.newSingleThreadExecutor()
 data class Line(val sceneId:String,val lineId:String,val characterId:String,val language:String,val asset:String?,val subtitle:String,val durationMillis:Long)
 var currentLine:Line?=null;private set
 private var queued:Line?=null
 var isTalking=false;private set
 var onTalkingChanged:(Boolean)->Unit={}
 private val manifest by lazy { VoiceManifest(context) }
 fun speakLine(scene:String,line:String,character:String,language:String,subtitle:String="",durationMillis:Long=0L):Boolean {
  if(released||!enabled())return false
  val speaker=when(character){"nermir"->"nermiri";"mevludin"->"mevludini";else->character}
  val id="$scene/$line/$speaker/$language"
  if(id==lastId)return true
  val clip=manifest.find(scene,line,speaker,language)
  if(clip==null&&subtitle.isBlank()&&GardenContent.asset(context,id)==null)return false
  val cue=Line(scene,line,speaker,language,clip?.asset,subtitle,clip?.durationMillis?:durationMillis)
  if(isTalking||player!=null||currentLine!=null){queued=cue;return true}
  return play(cue)
 }
 private fun play(cue:Line):Boolean{
  currentLine=cue
  GardenContent.asset(context,"${cue.sceneId}/${cue.lineId}/${cue.characterId}/${cue.language}")?.let{playRemote(cue,it.absolutePath);return true}
  cue.asset?.let{return playAsset(cue,it)}
  speechCache.find(cue.characterId,cue.language,cue.subtitle)?.let{playRemote(cue,it.absolutePath);return true}
  if(!online())return fallback(cue)
  // Use Cloudflare AI speech directly; the API key stays in Worker secrets.
  fetchWorkerSpeech(cue)
  return true
 }
 private fun playAsset(cue:Line,asset:String):Boolean = try {
  val media=MediaPlayer();player=media;lastId="${cue.sceneId}/${cue.lineId}/${cue.characterId}/${cue.language}"
  media.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
  context.assets.openFd(asset).use{media.setDataSource(it.fileDescriptor,it.startOffset,it.length)}
  prepare(media,cue);true
 }catch(_:Exception){player?.release();player=null;fallback(cue)}
 private fun fetchWorkerSpeech(cue:Line){
  if(cue.subtitle.isBlank()||android.os.SystemClock.elapsedRealtime()<workerRetryAt){fetchAiAudio(cue);return}
  network.execute{
   var connection:HttpURLConnection?=null
   try{
    connection=(URL(SPEECH_BACKEND).openConnection() as HttpURLConnection).apply{
     requestMethod="POST";connectTimeout=5000;readTimeout=18000
     doOutput=true;setRequestProperty("Content-Type","application/json")
     setRequestProperty("User-Agent","LazyGarden/"+BuildConfig.VERSION_NAME)
    }
    val payload=JSONObject().put("character_id",cue.characterId.lowercase(Locale.ROOT))
     .put("language",cue.language).put("text",cue.subtitle).toString()
    connection.outputStream.use{it.write(payload.toByteArray(Charsets.UTF_8))}
    if(connection.responseCode !in 200..299||!connection.contentType.orEmpty().startsWith("audio/"))throw IllegalStateException("Speech backend unavailable")
    require(connection.contentLengthLong<=SpeechCache.MAX_CLIP_BYTES)
    val file=java.io.File.createTempFile("garden_voice_",".mp3",context.cacheDir)
    try{
     val bytes=connection.inputStream.use{input->
      val output=java.io.ByteArrayOutputStream();val buffer=ByteArray(8192)
      while(true){val count=input.read(buffer);if(count<0)break;require(output.size()+count<=SpeechCache.MAX_CLIP_BYTES);output.write(buffer,0,count)}
      output.toByteArray()
     }
     require(bytes.isNotEmpty());file.writeBytes(bytes)
     runCatching{speechCache.store(cue.characterId,cue.language,cue.subtitle,bytes)}
     main.post{
      if(!released&&currentLine===cue){cachedSpeechFile?.delete();cachedSpeechFile=file;playRemote(cue,file.absolutePath)}
      else file.delete()
     }
    }catch(e:Exception){file.delete();throw e}
   }catch(_:Exception){workerRetryAt=android.os.SystemClock.elapsedRealtime()+60_000;main.post{if(!released&&currentLine===cue)fetchAiAudio(cue)}}
   finally{connection?.disconnect()}
  }
 }
 private fun fetchAiAudio(cue:Line){
  if(!online()||android.os.SystemClock.elapsedRealtime()<scriptRetryAt){fallback(cue);return}
  val scene=URLEncoder.encode(cue.sceneId,"UTF-8");val line=URLEncoder.encode(cue.lineId,"UTF-8");val lang=URLEncoder.encode(cue.language,"UTF-8");val speaker=URLEncoder.encode(cue.characterId,"UTF-8")
  network.execute{
   var connection:HttpURLConnection?=null
   try{
    val endpoint="$VOICE_BACKEND?action=line&lang=$lang&scene_id=$scene&line_id=$line&character_id=$speaker"
    connection=(URL(endpoint).openConnection() as HttpURLConnection).apply{instanceFollowRedirects=true;connectTimeout=4500;readTimeout=6000;requestMethod="GET"}
    val json=connection.inputStream.bufferedReader().use{it.readText()}
    val root=JSONObject(json);val row=root.optJSONObject("line")
    val sameLanguage=row?.optString("lang",row.optString("language"))==cue.language
    val sameActor=row?.optString("character_id")==cue.characterId
    val sameText=row?.optString("text")?.trim()==cue.subtitle.trim()
    val audio=if(sameLanguage&&sameActor&&sameText)row?.optString("audio_url","").orEmpty().takeIf{it.startsWith("https://")}.orEmpty() else ""
    if(audio.isBlank())scriptRetryAt=android.os.SystemClock.elapsedRealtime()+60_000
    main.post{if(!released&&currentLine===cue){if(audio.isNotBlank())playRemote(cue,audio) else fallback(cue)}}
   }catch(_:Exception){scriptRetryAt=android.os.SystemClock.elapsedRealtime()+60_000;main.post{if(!released&&currentLine===cue)fallback(cue)}}finally{connection?.disconnect()}
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
  media.setOnCompletionListener{if(player===it){it.release();player=null;cachedSpeechFile?.delete();cachedSpeechFile=null;talking(false);currentLine=null;playQueued()}}
  media.setOnErrorListener{it,_,_->if(player===it){it.release();player=null;cachedSpeechFile?.delete();cachedSpeechFile=null;talking(false);fallback(cue)};true}
  media.prepareAsync()
 }
 private fun playQueued(){val next=queued?:return;queued=null;if(!released&&enabled())play(next)}
 /** No device TTS: a missing AI clip keeps the localized subtitle without a foreign-accent voice. */
 private fun fallback(line:Line):Boolean {
  if(currentLine===line)currentLine=null
  talking(false);playQueued();return false
 }
 private fun talking(value:Boolean){if(isTalking!=value){isTalking=value;onTalkingChanged(value)}}
 fun stop(){queued=null;lastId="";player?.release();player=null;cachedSpeechFile?.delete();cachedSpeechFile=null;currentLine=null;talking(false)}
 fun release(){stop();released=true;network.shutdownNow();main.removeCallbacksAndMessages(null)}
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
