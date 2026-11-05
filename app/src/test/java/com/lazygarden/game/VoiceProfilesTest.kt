package com.lazygarden.game

import org.junit.Assert.*
import org.junit.Test

class VoiceProfilesTest {
    @Test fun allCharactersHaveStableDistinctDeliveryWithoutExtremePitch(){
        val profiles=SceneCast.actors.map{VoiceProfiles.forActor(it.id)}
        assertEquals(18,profiles.toSet().size)
        assertTrue(profiles.all{it.pitch in .80f..1.25f&&it.rate in .80f..1.05f})
        assertEquals(VoiceProfiles.forActor("nermiri"),VoiceProfiles.forActor("nermir"))
        assertEquals(VoiceProfiles.forActor("mevludini"),VoiceProfiles.forActor("mevludin"))
    }
    @Test fun voiceSelectionIsBoundedForEveryInstalledVoiceCount(){
        for(actor in SceneCast.actors)for(count in 1..8)
            assertTrue(VoiceProfiles.localIndex(actor.id,count) in 0 until count)
        assertEquals(0,VoiceProfiles.localIndex("meli",1))
        assertEquals(1,VoiceProfiles.localIndex("meli",2))
        assertEquals(0,VoiceProfiles.localIndex("rexha",2))
    }
}
