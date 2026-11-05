package com.lazygarden.game

import org.junit.Assert.*
import org.junit.Test

class SceneCastTest {
    @Test fun allStorySpeakersAndSpecialistsResolveToStableIdentities(){
        assertEquals(18,SceneCast.actors.size)
        assertEquals(18,SceneCast.actors.map{it.id}.toSet().size)
        GardenStory.chapters.flatMap{it.lines}.forEach{assertEquals(it.character.name.lowercase(),SceneCast.actor(it.character.name).id)}
        RestorationJobs.crew.forEach{assertEquals(SceneCast.canonical(it.id),SceneCast.actor(it.id).id)}
        assertEquals("nermiri",SceneCast.canonical("NERMIR"));assertEquals("mevludini",SceneCast.canonical("mevludin"))
    }
    @Test fun storyWaitsForWalkAndInspectionBeforeSpeechAndNeverPretendsToRepair(){
        assertEquals(CharacterAnimation.State.WALK,StoryBlocking.state(2199,true,true))
        assertEquals(CharacterAnimation.State.THINK,StoryBlocking.state(2200,true,true))
        assertEquals(CharacterAnimation.State.THINK,StoryBlocking.state(2899,true,true))
        assertEquals(CharacterAnimation.State.TALK,StoryBlocking.state(2900,true,true))
        assertEquals(CharacterAnimation.State.IDLE,StoryBlocking.state(2900,false,true))
        assertEquals(CharacterAnimation.State.IDLE,StoryBlocking.state(2900,true,false))
        for(ms in 0L..10000L step 100)assertNotEquals(CharacterAnimation.State.WORK,StoryBlocking.state(ms,true,true))
    }
    @Test fun translatedWordsCannotAccidentallySummonAWorker(){
        assertEquals(StoryBlocking.Beat.TALK,StoryBlocking.beat(0,1))
        assertEquals(StoryBlocking.Beat.TALK,StoryBlocking.beat(7,1))
        assertEquals(listOf("lazi","meli"),StoryBlocking.cast("lazi","meli","rexha"))
        assertEquals(listOf("rexha","lazi","meli"),StoryBlocking.cast("rexha","lazi","rexha"))
    }
    @Test fun laterJobsRetainTheCorrectCrewAndArrivalDoesNotRestartOnResume(){
        assertEquals("luli",RestorationJobs.worker(10,"ASSEMBLE").id)
        assertEquals("luli",RestorationJobs.worker(13,"REPAIR").id)
        assertEquals("luli",RestorationJobs.worker(15,"ASSEMBLE").id)
        assertEquals("rexha",RestorationJobs.worker(12,"REPAIR").id)
        val plan=RestorationSequence.plan(6)
        val roof=plan.steps.indexOfFirst{it.key=="ROOF"}
        val start=plan.steps.take(roof).sumOf{it.duration}
        assertEquals(start,RestorationJobs.arrivalAt(plan,roof))
        val clean=plan.steps.indexOfFirst{it.key=="CLEAN"}
        assertEquals(0L,RestorationJobs.arrivalAt(plan,clean))
    }
}
