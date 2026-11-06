package com.lazygarden.game
import org.junit.Assert.*
import org.junit.Test
class WorkApproachTest {
 @Test fun serviceRouteDoesNotPassThroughTheObject(){
  for(side in listOf(false,true)){for(i in 0..65){val p=WorkApproach.position(.8f,.7f,i/100f,side);assertEquals(.96f,p.y,0.0001f)}
   for(i in 66..100){val p=WorkApproach.position(.8f,.7f,i/100f,side);assertEquals(.8f,p.x,0.0001f)}
  }
 }
 @Test fun workerStopsAndStaysAtTheSameWorkStation(){
  val end=CharacterAnimation.Position(.8f,.7f)
  assertEquals(end,WorkApproach.position(.8f,.7f,1f,true))
  assertEquals(end,WorkApproach.position(.8f,.7f,2f,true))
  assertEquals(1f,WorkApproach.progress(9000),0f)
 }
}
