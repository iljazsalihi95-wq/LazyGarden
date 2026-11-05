package com.lazygarden.game

import java.io.File
import java.security.MessageDigest

/** Bounded, text-addressed voice clips; subtitles changing never reuse an old recording. */
class SpeechCache(private val directory:File,private val maxBytes:Long=20L*1024*1024){
 companion object {const val MAX_CLIP_BYTES=4*1024*1024}
 private fun key(character:String,language:String,text:String)=MessageDigest.getInstance("SHA-256").digest("$character\n$language\n$text".toByteArray()).joinToString(""){"%02x".format(it)}
 @Synchronized fun find(character:String,language:String,text:String):File? = File(directory,key(character,language,text)+".mp3").takeIf{it.isFile&&it.length() in 1..MAX_CLIP_BYTES.toLong()}?.also{it.setLastModified(System.currentTimeMillis())}
 @Synchronized fun store(character:String,language:String,text:String,bytes:ByteArray):File {
  require(bytes.size in 1..MAX_CLIP_BYTES&&bytes.size<=maxBytes)
  directory.mkdirs();val file=File(directory,key(character,language,text)+".mp3")
  val pending=File.createTempFile("pending-",".tmp",directory)
  try{pending.outputStream().use{it.write(bytes);it.fd.sync()};check(pending.renameTo(file))}finally{pending.delete()}
  var size=directory.listFiles().orEmpty().sumOf{it.length()}
  for(old in directory.listFiles().orEmpty().filter{it!=file}.sortedBy{it.lastModified()}){if(size<=maxBytes)break;val length=old.length();if(old.delete())size-=length}
  return file
 }
}
