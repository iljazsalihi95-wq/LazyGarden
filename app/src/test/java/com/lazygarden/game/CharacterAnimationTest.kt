package com.lazygarden.game
import org.junit.Assert.*
import org.junit.Test
class CharacterAnimationTest {
 @Test fun movementClampsToEndpointsAndFacingFollowsTarget(){val a=CharacterAnimation.Position(1f,2f);val b=CharacterAnimation.Position(9f,8f);assertEquals(a,CharacterAnimation.move(a,b,-1f));assertEquals(b,CharacterAnimation.move(a,b,2f));assertEquals(CharacterAnimation.Position(5f,5f),CharacterAnimation.move(a,b,.5f));assertTrue(CharacterAnimation.facesRight(1f,9f));assertFalse(CharacterAnimation.facesRight(9f,1f))}
 @Test fun allEightStatesSampleValidFramesOnBothSheets(){for(sheet in listOf(true,false))for(state in CharacterAnimation.State.entries)for(t in 0L..3000L step 90)assertTrue(CharacterAnimation.frame(state,t,sheet) in 0..7)}
 @Test fun walkingKeepsFacingTheDestinationEvenAfterCrossingTheFountain(){
  val a=CharacterAnimation.Position(90f,0f);val b=CharacterAnimation.Position(30f,0f)
  assertFalse(CharacterAnimation.routeFacing(a,b,.1f,50f));assertFalse(CharacterAnimation.routeFacing(a,b,.9f,50f))
  assertTrue(CharacterAnimation.routeFacing(a,b,1f,50f))
  assertTrue(CharacterAnimation.sampleRoute(a,b,.9f,50f).moving)
  assertFalse(CharacterAnimation.sampleRoute(a,b,1f,50f).moving)
 }
 @Test fun stateChangesRestartOnlyThatActorsPoseClock(){
  val actor=CharacterAnimation.Player();actor.update(CharacterAnimation.State.WALK,1000)
  actor.update(CharacterAnimation.State.WALK,1400);assertEquals(400L,actor.elapsed(1400))
  actor.update(CharacterAnimation.State.TALK,1500);assertEquals(0L,actor.elapsed(1500));assertEquals(3,actor.frame(1500,true))
  val other=CharacterAnimation.Player();other.update(CharacterAnimation.State.WORK,1800)
  assertEquals(300L,actor.elapsed(1800));assertEquals(0L,other.elapsed(1800))
 }
}
