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
 @Test fun scaffoldAscentIsContinuousAndNewSpecialistsEnterBeforeClimbing(){
  val base=CharacterAnimation.Position(.4f,.88f);val top=CharacterAnimation.Position(.34f,.56f)
  assertEquals(base,WorkApproach.platformStation(base,top,0f));assertEquals(top,WorkApproach.platformStation(base,top,1f))
  var previous=base
  for(i in 1..100){val next=WorkApproach.platformStation(base,top,i/100f)
   assertTrue(kotlin.math.abs(next.y-previous.y)<.02f);previous=next
  }
  val entering=WorkApproach.position(top.x,top.y,0f,true)
  assertEquals(.96f,entering.y,0f);assertTrue(entering.x>1f)
 }

}
