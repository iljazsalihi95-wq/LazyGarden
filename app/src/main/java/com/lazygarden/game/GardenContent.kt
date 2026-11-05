package com.lazygarden.game

import android.content.Context
import android.util.AtomicFile
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/** Data only. A release is activated at the next cold start, never halfway through a board. */
object GardenContent {
 const val ENDPOINT="https://raw.githubusercontent.com/iljazsalihi95-wq/LazyGarden/main/content/manifest.json"
 const val MAX_BYTES=512*1024
 data class Asset(val id:String,val url:String,val sha256:String,val kind:String)
 data class Release(val version:Int,val levels:Map<Int,LevelSpec>,val lines:Map<String,String>,
  val titles:Map<String,String>,val assets:List<Asset>,val events:List<Event>)
 data class Event(val id:String,val starts:Long,val ends:Long,val titles:Map<String,String>)
 @Volatile var active:Release?=null;private set
 private var initialized=false
 @Synchronized fun initialize(context:Context){
  if(initialized)return
  initialized=true;load(context)
  val app=context.applicationContext
  Thread({runCatching{refresh(app)}},"garden-content").start()
 }
 private val languages=setOf("sq","de","it","mk","tr")
 private val characters=GardenCharacter.entries.map{it.name.lowercase()}.toSet()
 private val artIds=setOf("garden_before","garden_estate","garden_map","hd_logo")
 fun validSpec(s:LevelSpec)=s.moves in 10..60&&s.target in 100..20000&&s.ice in 0..48&&s.collectColor in 0..5&&s.collectCount in 0..150&&s.terrainKind in setOf(0,1,3,4,5)&&s.terrainCount in 0..14&&s.rescue in 0..2&&(s.terrainKind!=0||s.terrainCount<=s.rescue)
 private fun integer(o:JSONObject,key:String):Int {val v=o.get(key);require(v is Number&&v.toDouble()==v.toInt().toDouble());return v.toInt()}
 private fun localized(o:JSONObject,max:Int):Map<String,String> = o.keys().asSequence().associateWith{lang->
  require(lang in languages);val s=o.getString(lang);require(s.isNotBlank()&&s.length<=max);s
 }.also{require(it.isNotEmpty())}
 fun parse(raw:String):Release {
  require(raw.toByteArray().size<=MAX_BYTES)
  val o=JSONObject(raw);require(integer(o,"schemaVersion")==1&&integer(o,"minAppContentApi")==1)
  val version=integer(o,"version");require(version>0)
  val levels=mutableMapOf<Int,LevelSpec>();val rows=o.getJSONArray("levels");require(rows.length()<=Campaign.COUNT)
  for(i in 0 until rows.length()){
   val r=rows.getJSONObject(i);val id=integer(r,"id");require(id in 1..Campaign.COUNT&&id !in levels)
   val s=LevelSpec(integer(r,"moves"),integer(r,"target"),integer(r,"ice"),integer(r,"collectColor"),integer(r,"collectCount"),integer(r,"terrainKind"),integer(r,"terrainCount"),integer(r,"rescue"))
   require(validSpec(s));levels[id]=s
  }
  val lines=mutableMapOf<String,String>();val titles=mutableMapOf<String,String>();val chapters=o.getJSONArray("chapters");require(chapters.length()<=RestorationInventory.TASK_COUNT)
  for(i in 0 until chapters.length()){
   val c=chapters.getJSONObject(i);val id=integer(c,"id");require(id in 1..RestorationInventory.TASK_COUNT)
   localized(c.getJSONObject("titles"),120).forEach{(lang,s)->require(titles.put("$id/$lang",s)==null)}
   val entries=c.getJSONArray("lines");require(entries.length()==GardenStory.LINES_PER_CHAPTER)
   for(j in 0 until entries.length()){
    val line=entries.getJSONObject(j);val speaker=line.getString("character");require(speaker in characters)
    // Content can translate authored lines, never silently switch their established speaker.
    require(speaker==GardenStory.chapters[id-1].lines[j].character.name.lowercase())
    localized(line.getJSONObject("text"),1200).forEach{(lang,s)->require(lines.put("$id/${j+1}/$lang",s)==null)}
   }
  }
  val assets=mutableListOf<Asset>();val items=o.getJSONArray("assets");require(items.length()<=100)
  for(i in 0 until items.length()){
   val a=items.getJSONObject(i);val id=a.getString("id");val kind=a.getString("kind");val url=URL(a.getString("url"));val sha=a.getString("sha256")
   require(assets.none{it.id==id}&&sha.matches(Regex("[a-f0-9]{64}")))
   require(url.protocol=="https"&&url.userInfo==null&&url.host in setOf("raw.githubusercontent.com","lazygarden-backend.lazisalihi.workers.dev"))
   require((kind=="image"&&id in artIds)||(kind=="audio"&&id.matches(Regex("story_[1-9][0-9]?/line_[1-3]/[a-z]+/(sq|de|it|mk|tr)"))))
   assets+=Asset(id,url.toString(),sha,kind)
  }
  val events=mutableListOf<Event>();val er=o.getJSONArray("events");require(er.length()<=30)
  for(i in 0 until er.length()){
   val e=er.getJSONObject(i);val id=e.getString("id");val start=e.getLong("starts");val end=e.getLong("ends")
   require(id.matches(Regex("[a-z0-9_-]{1,40}"))&&events.none{it.id==id}&&start>=0&&end>start)
   events+=Event(id,start,end,localized(e.getJSONObject("titles"),120))
  }
  return Release(version,levels,lines,titles,assets,events)
 }
 private fun root(context:Context)=File(context.filesDir,"live_content").apply{mkdirs()}
 private fun assetFile(context:Context,a:Asset)=File(root(context),a.sha256)
 private fun digest(bytes:ByteArray)=MessageDigest.getInstance("SHA-256").digest(bytes).joinToString(""){"%02x".format(it)}
 fun asset(context:Context,id:String):File?=active?.assets?.firstOrNull{it.id==id}?.let{assetFile(context,it).takeIf{f->f.isFile}}
 fun line(chapter:Int,line:Int,language:String,fallback:String)=active?.lines?.get("${chapter+1}/${line+1}/$language")?:fallback
 fun title(chapter:Int,language:String,fallback:String)=active?.titles?.get("${chapter+1}/$language")?:fallback
 fun event(language:String,now:Long=System.currentTimeMillis()/1000)=active?.events?.firstOrNull{now in it.starts until it.ends}?.titles?.let{it[language]}
 @Synchronized fun load(context:Context){
  active=runCatching{
   val file=AtomicFile(File(root(context),"manifest.json"));val release=parse(file.openRead().bufferedReader().use{it.readText()})
   require(release.assets.all{a->val f=assetFile(context,a);f.isFile&&f.length()<=8*1024*1024&&digest(f.readBytes())==a.sha256})
   release
  }.getOrNull()
 }
 private fun download(url:String,max:Int):ByteArray{
  val c=URL(url).openConnection() as HttpURLConnection
  try{
   c.connectTimeout=5000;c.readTimeout=10000;c.instanceFollowRedirects=false
   require(c.responseCode==200&&c.contentLengthLong<=max)
   return c.inputStream.use{input->val out=java.io.ByteArrayOutputStream();val buffer=ByteArray(8192);while(true){val n=input.read(buffer);if(n<0)break;require(out.size()+n<=max);out.write(buffer,0,n)};out.toByteArray()}
  }finally{c.disconnect()}
 }
 /** Validate all referenced assets before atomically replacing the last working manifest. */
 @Synchronized fun stage(context:Context,raw:String,fetch:(String,Int)->ByteArray={url,max->download(url,max)}):Boolean{
  val release=parse(raw);val disk=runCatching{parse(AtomicFile(File(root(context),"manifest.json")).openRead().bufferedReader().use{it.readText()}).version}.getOrDefault(0)
  if(release.version<=maxOf(active?.version?:0,disk))return false
  for(a in release.assets){val f=assetFile(context,a);if(f.isFile&&digest(f.readBytes())==a.sha256)continue
   val bytes=fetch(a.url,8*1024*1024);require(bytes.size<=8*1024*1024&&digest(bytes)==a.sha256)
   if(a.kind=="image"){
    val options=android.graphics.BitmapFactory.Options().apply{inJustDecodeBounds=true};android.graphics.BitmapFactory.decodeByteArray(bytes,0,bytes.size,options)
    require(options.outWidth in 1..4096&&options.outHeight in 1..4096&&options.outWidth.toLong()*options.outHeight<=8_000_000)
   }
   val file=AtomicFile(f);val stream=file.startWrite();try{stream.write(bytes);file.finishWrite(stream)}catch(t:Throwable){file.failWrite(stream);throw t}
  }
  val file=AtomicFile(File(root(context),"manifest.json"));val stream=file.startWrite()
  try{stream.write(raw.toByteArray());file.finishWrite(stream)}catch(t:Throwable){file.failWrite(stream);throw t}
  return true
 }
 fun refresh(context:Context):Boolean=stage(context,download(ENDPOINT,MAX_BYTES).toString(Charsets.UTF_8))
}
