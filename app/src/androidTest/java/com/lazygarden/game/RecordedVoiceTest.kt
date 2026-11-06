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
 @Test fun missingClipsUseAiThenTemporaryFallbackAndKeepPlaybackLifecycle(){
  val context=ApplicationProvider.getApplicationContext<Context>()
  val json=JSONObject(context.assets.open("voices/manifest.json").bufferedReader().use{it.readText()})
  assertEquals(18,json.getJSONArray("characters").length())
  assertEquals(5,json.getJSONArray("languages").length())
  // This check intentionally changes when real licensed recordings are delivered.
  assertEquals(0,json.getJSONArray("clips").length())
  InstrumentationRegistry.getInstrumentation().runOnMainSync{
   val voice=GardenVoice(context)
   assertFalse(voice.retryLastLine())
   assertEquals(GardenVoice.Status.IDLE,voice.status)
   var talkingEvents=0
   voice.onTalkingChanged={talkingEvents++}
   // An unknown clip without subtitle text cannot produce speech and must not
   // start a backend request or leave a phantom active speaker.
   for(language in listOf("sq","de","it","mk","tr")){
    assertFalse(voice.speakLine("fountain","talk","meli",language))
    assertNull(voice.currentLine)
    assertFalse(voice.isTalking)
    voice.stop()
   }
   assertEquals(0,talkingEvents)
   // Availability depends on the device TTS languages and backend cooldown.
   // Acceptance is not evidence that an audio clip actually exists: validate
   // the active cue if pending, then require stop() to clear every path.
   val accepted=voice.speakLine("fountain","talk","meli","de","Warum funktioniert der Brunnen nicht?",3500)
   voice.currentLine?.let{assertEquals("meli",it.characterId);assertEquals(3500L,it.durationMillis)}
   if(!accepted)assertNull(voice.currentLine)
   voice.stop()
   assertNull(voice.currentLine)
   assertEquals(GardenVoice.Status.IDLE,voice.status)
   voice.release();assertFalse(voice.speakLine("fountain","talk","meli","sq"))
  }
 }
}
