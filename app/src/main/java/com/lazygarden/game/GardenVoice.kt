package com.lazygarden.game

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import org.json.JSONObject

/** Only licensed recorded assets may speak. Missing recordings leave subtitles visible and audio silent. */
class GardenVoice(private val context:Context,private val enabled:()->Boolean={true}) {
 private var player:MediaPlayer?=null
 private var released=false
 private var lastId=""
 var isTalking=false;private set
 var onTalkingChanged:(Boolean)->Unit={}
 private val manifest by lazy { VoiceManifest(context) }
 fun speakLine(scene:String,line:String,character:String,language:String):Boolean {
  if(released||!enabled())return false
  val id="$scene/$line/$character/$language"
  if(id==lastId&&player!=null)return true
  stop()
  val clip=manifest.find(scene,line,character,language)?:return false
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
  }catch(_:Exception){stop();false}
 }
 private fun talking(value:Boolean){if(isTalking!=value){isTalking=value;onTalkingChanged(value)}}
 fun stop(){player?.release();player=null;lastId="";talking(false)}
 fun release(){stop();released=true}
}

/** Exact scene/line/character/language mapping; never falls back to another voice or language. */
class VoiceManifest(context:Context) {
 data class Clip(val scene:String,val line:String,val character:String,val language:String,val asset:String,val license:String)
 private val clips:List<Clip> = try {
  val json=JSONObject(context.assets.open("voices/manifest.json").bufferedReader().use{it.readText()})
  val entries=json.getJSONArray("clips")
  (0 until entries.length()).mapNotNull{i->
   val item=entries.getJSONObject(i)
   val path=item.getString("asset")
   val license=item.getString("license")
   if(!path.startsWith("voices/")||path.split('/').any{it==".."}||license.isBlank())null
   else Clip(item.getString("scene"),item.getString("line"),item.getString("character"),item.getString("language"),path,license)
  }
 }catch(_:Exception){emptyList()}
 fun find(scene:String,line:String,character:String,language:String):Clip? = clips.singleOrNull{
  it.scene==scene&&it.line==line&&it.character==character&&it.language==language
 }
}
