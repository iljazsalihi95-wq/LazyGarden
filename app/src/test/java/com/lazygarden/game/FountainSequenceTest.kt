package com.lazygarden.game
import org.junit.Assert.*
import org.junit.Test
class FountainSequenceTest {
 @Test fun everyBeatIsOrderedAndWaterCannotStartBeforeRepair(){
  val stages=(0L..FountainSequence.DURATION step 100).map{FountainSequence.stage(it)}.distinct()
  assertEquals(FountainSequence.Stage.entries.toList(),stages)
  assertEquals(0f,FountainSequence.water(19399),0f)
  assertEquals(1f,FountainSequence.water(22200),0f)
 }
 @Test fun everyoneArrivesBeforeInspectionAndSpeaksOnlyInScene(){
  for(actor in 0..3){assertEquals(0f,FountainSequence.approach(0,actor),0f);assertEquals(1f,FountainSequence.approach(4800,actor),0f)}
  for(ms in listOf(7000L,10000L,20000L,23000L)){assertTrue(FountainSequence.speaker(ms) in 0..3);assertTrue(FountainSequence.dialogue(ms).isNotEmpty())}
  assertEquals(-1,FountainSequence.speaker(15000))
 }
}
