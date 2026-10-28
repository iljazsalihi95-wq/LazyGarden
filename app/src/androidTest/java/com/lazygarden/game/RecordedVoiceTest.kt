package com.lazygarden.game

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecordedVoiceTest {
 @Test fun missingClipsKeepTheTemporaryFallbackAndPlaybackLifecycle(){
  val context=ApplicationProvider.getApplicationContext<Context>()
  val json=JSONObject(context.assets.open("voices/manifest.json").bufferedReader().use{it.readText()})
  assertEquals(18,json.getJSONArray("characters").length())
  assertEquals(5,json.getJSONArray("languages").length())
  // This check intentionally changes when real licensed recordings are delivered.
  assertEquals(0,json.getJSONArray("clips").length())
  InstrumentationRegistry.getInstrumentation().runOnMainSync{
   val voice=GardenVoice(context)
   var talkingEvents=0
   voice.onTalkingChanged={talkingEvents++}
   for(language in listOf("sq","de","it","mk","tr")){
    assertFalse(voice.speakLine("fountain","talk","meli",language))
    assertFalse(voice.isTalking)
   }
   assertEquals(0,talkingEvents)
   assertTrue(voice.speakLine("fountain","talk","meli","de","Warum funktioniert der Brunnen nicht?",3500))
   assertEquals("meli",voice.currentLine?.characterId)
   assertEquals(3500L,voice.currentLine?.durationMillis)
   voice.release();assertFalse(voice.speakLine("fountain","talk","meli","sq"))
  }
 }
}
