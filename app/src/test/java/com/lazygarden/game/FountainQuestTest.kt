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
}
