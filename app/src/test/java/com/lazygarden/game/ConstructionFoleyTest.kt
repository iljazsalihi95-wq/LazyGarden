package com.lazygarden.game
import org.junit.Assert.*
import org.junit.Test
class ConstructionFoleyTest {
 @Test fun woodStoneSoilAndWaterAreDifferentSignals(){
  val keys=listOf("hammer","stone","shovel","brush","plant","water")
  val signals=keys.map{key->(0..500).map{i->ConstructionFoley.sample(key,i/22050.0,if(i%2==0).4 else -.4)}}
  assertEquals(keys.size,signals.toSet().size)
  signals.flatten().forEach{assertTrue(it!!.isFinite())}
  assertNull(ConstructionFoley.sample("coin",.1,.3))
 }
}
