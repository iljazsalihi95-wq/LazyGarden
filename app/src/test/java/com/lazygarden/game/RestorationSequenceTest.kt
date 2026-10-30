package com.lazygarden.game

import org.junit.Assert.*
import org.junit.Test

class RestorationSequenceTest {
 @Test fun allLiveRestorationPlansHaveOrderedWorkAndBoundedProgress(){
  val plans=(2 until RestorationInventory.TASK_COUNT).map{RestorationSequence.plan(it)}
  assertEquals(RestorationInventory.TASK_COUNT-2,plans.size)
  assertEquals((2 until RestorationInventory.TASK_COUNT).toList(),plans.map{it.task})
  plans.forEach{p->
   assertEquals("ARRIVAL",p.steps.first().key)
   assertEquals("CELEBRATE",p.steps.last().key)
   assertTrue("task ${p.task} needs a substantial live sequence",p.duration>18000)
   assertTrue("task ${p.task} needs real work between arrival/reveal",p.steps.any{it.key !in setOf("ARRIVAL","INSPECT","REVEAL","CELEBRATE")})
   assertTrue("task ${p.task} needs voiced scene dialogue",p.steps.count{it.line.isNotBlank()}>=3)
   var start=0L
   p.steps.forEachIndexed{i,s->
    assertEquals(i,p.index(start));assertEquals(0f,p.progress(start,s.key),0f)
    assertEquals(1f,p.progress(start+s.duration,s.key),0f);assertTrue(s.duration>0);start+=s.duration
   }
   assertEquals(p.steps.lastIndex,p.index(p.duration+9999));assertEquals(1f,p.fraction(p.duration+9999),0f)
  }
 }
 @Test fun originalSceneDialogueKeepsFiveRealTranslations(){for(task in 2..6)for(step in RestorationSequence.plan(task).steps)if(step.line.isNotBlank()){val translations=listOf("sq","de","it","mk","tr").map{SceneText.line(step.line,it)};assertEquals(5,translations.toSet().size);assertTrue(translations.all{it.length>8})}}
 @Test fun flowerChoiceAndCraftsmanPathBoundariesRemainStable(){
  assertEquals(3,FlowerSequence.choices.map{it.id}.toSet().size)
  assertEquals(FlowerSequence.Stage.CALL_GEZIMI,FlowerSequence.stage(8000))
  assertEquals(FlowerSequence.Stage.CHOOSE,FlowerSequence.stage(14500))
  assertEquals(FlowerSequence.Stage.GEZIMI_ARRIVAL,FlowerSequence.stage(11500))
  assertEquals(1f,FlowerSequence.bloom(FlowerSequence.DURATION),0f)
  assertEquals(CharacterAnimation.State.WALK,FlowerSequence.actorState(11500,"gezimi"))
  assertEquals(CharacterAnimation.State.WORK,FlowerSequence.actorState(20500,"gezimi"))
  assertEquals(CharacterAnimation.State.HAPPY,FlowerSequence.actorState(FlowerSequence.DURATION,"rona"))
  assertEquals(RestorationSequence.plan(2).duration,PathSequence.DURATION)
 }
}
