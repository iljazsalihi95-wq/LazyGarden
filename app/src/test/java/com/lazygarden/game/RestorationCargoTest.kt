package com.lazygarden.game
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random
class RestorationCargoTest {
 @Test fun everyNewLevelHasCoveredRestorationCargo(){
  for(level in 1..250){
   val engine=Match3Engine(level,Random(level))
   assertTrue(engine.rescueTarget>0)
   for(r in 0..7)for(c in 0..7)if(engine.cargo[r][c]>0)assertTrue(engine.terrain[r][c]>0)
   assertEquals(engine.spec.terrainCount,engine.terrainRemaining)
  }
 }
 @Test fun coveredPartStaysPutUntilItsCoverBreaks(){
  val engine=Match3Engine(1,Random(81))
  engine.terrain.forEach{it.fill(0)};engine.cargo.forEach{it.fill(0)}
  engine.cargo[3][1]=1;engine.terrain[3][1]=3
  engine.boost(Match3Engine.Booster.ROW,57)
  assertEquals(1,engine.cargo[3][1]);assertEquals(0,engine.rescued)
  engine.boost(Match3Engine.Booster.HAMMER,25)
  engine.boost(Match3Engine.Booster.HAMMER,25)
  repeat(8){if(!engine.won&&!engine.failed)engine.boost(Match3Engine.Booster.COLUMN,57)}
  assertEquals(1,engine.rescued)
 }
}
