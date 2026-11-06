package com.lazygarden.game
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random
class BoardBlueprintTest {
 @Test fun layoutsHaveSixDifferentStrategicShapes(){
  val shapes=(1..6).map{BoardBlueprint.cells(it,12)}
  assertEquals(6,shapes.toSet().size)
  shapes.forEach{assertEquals(12,it.size);assertEquals(12,it.toSet().size);assertTrue(it.all{cell->cell in 0..63})}
 }
 @Test fun everyChapterKeepsItsGoalCountAndCoveredDeliveryCargo(){
  for(level in 1..250){val e=Match3Engine(level,Random(level));assertEquals(e.spec.terrainCount,e.terrainRemaining)
   assertTrue(e.hasMove());assertEquals(e.rescueTarget,e.cargo.sumOf{row->row.count{it>0}})
   val copy=Match3Engine(level,Random(9));assertTrue(copy.restore(e.encode()));assertEquals(e.encode(),copy.encode())
  }
 }
 @Test fun constructionObstaclesStartAfterTheFirstDeliveryTutorial(){
  assertEquals(0,Campaign.bundledLevel(1).terrainKind)
  for(level in 2..7){assertEquals(1,Campaign.bundledLevel(level).terrainKind);assertTrue(Match3Engine(level).terrainRemaining>=2)}
 }
 @Test fun matureSitesMixCratesWithTheChapterObstacle(){
  for(level in listOf(26,51,76,126,201)){
   val e=Match3Engine(level,Random(level));val kinds=e.terrain.flatMap{it.toList()}.filter{it>0}.toSet()
   assertTrue(kinds.contains(1));assertTrue(kinds.contains(e.spec.terrainKind))
  }
 }
}
