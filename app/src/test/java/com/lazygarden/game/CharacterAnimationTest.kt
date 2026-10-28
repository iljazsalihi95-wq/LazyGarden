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
 @Test fun gaitIsGroundedAndSettlesExactlyAtDestination(){
  val a=CharacterAnimation.Position(0f,0f);val b=CharacterAnimation.Position(10f,0f)
  assertEquals(a,CharacterAnimation.gait(a,b,0f).position);assertEquals(b,CharacterAnimation.gait(a,b,1f).position)
  assertEquals(0f,CharacterAnimation.gait(a,b,0f).bob,0f);assertEquals(0f,CharacterAnimation.gait(a,b,1f).bob,0f)
  for(i in 1..9){val g=CharacterAnimation.gait(a,b,i/10f);assertTrue(g.bob<=0f);assertTrue(kotlin.math.abs(g.bob)<=.0046f);assertTrue(kotlin.math.abs(g.lean)<=.0141f)}
 }
 @Test fun groundedStrideDoesNotFloatOrOvershootOnShortAndLongRoutes(){
  for(distance in listOf(.15f,.5f,1f,3f,10f)){
   val a=CharacterAnimation.Position(0f,.72f);val b=CharacterAnimation.Position(distance,.72f)
   var last=-1f
   for(i in 0..100){val g=CharacterAnimation.gait(a,b,i/100f);assertTrue(g.position.x>=last);last=g.position.x;assertTrue(g.position.y==.72f);assertTrue(g.bob in -.0046f..0f)}
   assertEquals(b,CharacterAnimation.gait(a,b,1f).position)
  }
 }
 @Test fun stateChangesRestartOnlyThatActorsPoseClock(){
  val actor=CharacterAnimation.Player();actor.update(CharacterAnimation.State.WALK,1000)
  actor.update(CharacterAnimation.State.WALK,1400);assertEquals(400L,actor.elapsed(1400))
  actor.update(CharacterAnimation.State.TALK,1500);assertEquals(0L,actor.elapsed(1500));assertEquals(3,actor.frame(1500,true))
  val other=CharacterAnimation.Player();other.update(CharacterAnimation.State.WORK,1800)
  assertEquals(300L,actor.elapsed(1800));assertEquals(0L,other.elapsed(1800))
 }
}
