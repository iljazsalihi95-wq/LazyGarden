package com.lazygarden.game

import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files

class SpeechCacheTest {
 @Test fun speakerLanguageAndTextSelectDistinctAudio(){
  val dir=Files.createTempDirectory("garden-speech").toFile()
  try{
   val cache=SpeechCache(dir);cache.store("meli","sq","Mirë",byteArrayOf(1,2,3))
   assertArrayEquals(byteArrayOf(1,2,3),cache.find("meli","sq","Mirë")!!.readBytes())
   assertNull(cache.find("lazi","sq","Mirë"));assertNull(cache.find("meli","de","Mirë"));assertNull(cache.find("meli","sq","Tekst tjetër"))
  }finally{dir.deleteRecursively()}
 }
 @Test fun boundedCacheEvictsOldAudioAndRejectsEmptyOrOversizedFiles(){
  val dir=Files.createTempDirectory("garden-speech").toFile()
  try{
   val cache=SpeechCache(dir,6);val old=cache.store("meli","sq","one",byteArrayOf(1,2,3,4));old.setLastModified(1)
   cache.store("meli","sq","two",byteArrayOf(5,6,7,8))
   assertNull(cache.find("meli","sq","one"));assertNotNull(cache.find("meli","sq","two"))
   assertTrue(runCatching{cache.store("meli","sq","empty",byteArrayOf())}.isFailure)
   assertTrue(runCatching{cache.store("meli","sq","large",ByteArray(7))}.isFailure)
   assertTrue(dir.listFiles()!!.sumOf{it.length()}<=6)
  }finally{dir.deleteRecursively()}
 }
}
