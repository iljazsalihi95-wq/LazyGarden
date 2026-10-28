package com.lazygarden.game
import org.junit.Assert.*
import org.junit.Test
class FountainQuestTest {
 @Test fun actionsCannotSkipInspectionOrMaterialCollection(){
  for(stage in FountainQuest.Stage.entries){
   if(stage.duration==0L)assertNull(FountainQuest.timedNext(stage))
   assertNull(FountainQuest.actionNext(stage,FountainQuest.Action.RESTORE))
  }
  assertEquals(FountainQuest.Stage.PHONE_CALL,FountainQuest.actionNext(FountainQuest.Stage.CALL_REXHA,FountainQuest.Action.CALL_REXHA))
  assertNull(FountainQuest.actionNext(FountainQuest.Stage.TEST_WATER,FountainQuest.Action.CALL_REXHA))
  assertEquals(FountainQuest.Stage.CLEAN,FountainQuest.timedNext(FountainQuest.Stage.REPAIR))
 }
 @Test fun everyTimedStageHasAnExitAndBoundedMovement(){
  FountainQuest.Stage.entries.filter{it.duration>0}.forEach{
   assertNotNull(FountainQuest.timedNext(it));assertEquals(0f,FountainQuest.fraction(it,-100),0f);assertEquals(1f,FountainQuest.fraction(it,it.duration+1),0f)
  }
 }
 @Test fun inspectionActionsStartVisibleWorkBeforeOpeningTheNextObjective(){
  assertEquals(FountainQuest.Stage.TRY_WATER,FountainQuest.actionNext(FountainQuest.Stage.TEST_WATER,FountainQuest.Action.TEST_WATER))
  assertEquals(FountainQuest.Stage.LAZI_INSPECT,FountainQuest.actionNext(FountainQuest.Stage.INSPECT,FountainQuest.Action.INSPECT))
  assertEquals(FountainQuest.Stage.PUMP_DIAGNOSIS,FountainQuest.actionNext(FountainQuest.Stage.REXHA_INSPECT,FountainQuest.Action.INSPECT_PUMP))
  assertEquals(FountainQuest.Stage.COLLECT,FountainQuest.timedNext(FountainQuest.Stage.PUMP_DIAGNOSIS))
  assertEquals(FountainQuest.Stage.CHILDREN_WATER_APPROACH,FountainQuest.timedNext(FountainQuest.Stage.WATER_FLOW))
 }
 @Test fun dialogueCanFinishWithoutGivingAudioAnUnlimitedGameplayLock(){
  val stage=FountainQuest.Stage.PHONE_REPLY
  assertFalse(FountainQuest.canAdvance(stage,stage.duration-1,false))
  assertFalse(FountainQuest.canAdvance(stage,stage.duration,true))
  assertTrue(FountainQuest.canAdvance(stage,stage.duration,false))
  assertTrue(FountainQuest.canAdvance(stage,stage.duration+10000,true))
  assertFalse(FountainQuest.canAdvance(FountainQuest.Stage.COLLECT,99999,false))
 }
 @Test fun everySpokenCueAndNewObjectiveHasFiveTranslations(){
  val languages=listOf("sq","de","it","mk","tr")
  for(stage in FountainQuest.Stage.entries){
   val objective=languages.map{FountainText.objective(it,stage)}
   assertEquals("Objective $stage",5,objective.toSet().size)
   FountainText.cue(stage)?.let{cue->assertEquals("Subtitle $stage",5,languages.map{FountainText.get(it,cue.textKey)}.toSet().size)}
  }
 }

}
