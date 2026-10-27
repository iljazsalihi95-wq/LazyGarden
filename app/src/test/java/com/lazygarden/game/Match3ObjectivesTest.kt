package com.lazygarden.game

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class Match3ObjectivesTest {
 @Test fun campaignIntroducesAllObjectivesAndSavesAll250Boards(){
  val kinds=mutableSetOf<Int>();for(level in 1..250){val e=Match3Engine(level,Random(level));kinds.add(e.spec.terrainKind);assertEquals(e.spec.terrainCount,e.terrainRemaining);assertEquals(e.rescueTarget,e.cargo.sumOf{row->row.count{it>0}});assertTrue(e.hasMove());val copy=Match3Engine(level);assertTrue(copy.restore(e.encode()));assertEquals(e.encode(),copy.encode())};assertEquals(setOf(0,1,3,4,5),kinds)
 }
 @Test fun hammerDamagesStoneTwiceAndWatersFlower(){
  val e=Match3Engine(80,Random(2));e.terrain.forEach{it.fill(0)};e.terrain[3][3]=3;e.terrain[0][0]=5
  e.boost(Match3Engine.Booster.HAMMER,27);assertTrue(e.terrain[3][3] in 0..2)
  if(e.terrain[3][3]>0)e.boost(Match3Engine.Booster.HAMMER,27);assertEquals(0,e.terrain[3][3])
  e.boost(Match3Engine.Booster.HAMMER,0);assertEquals(0,e.terrain[0][0])
 }
 @Test fun rocketClearsSoilAndCratesWithoutUsingMoves(){val e=Match3Engine(51,Random(4));e.terrain.forEach{it.fill(0)};for(c in 0..7)e.terrain[4][c]=if(c%2==0)1 else 4;val moves=e.moves;val waves=e.boost(Match3Engine.Booster.ROW,35);assertTrue(waves.isNotEmpty());assertTrue(e.terrain[4].all{it==0});assertEquals(moves,e.moves);assertTrue(waves.first().terrainBefore[4].all{it>0})}
 @Test fun cargoFallsAndRescueIsCountedOnce(){val e=Match3Engine(126,Random(9));e.cargo.forEach{it.fill(0)};e.cargo[6][3]=2;val wave=e.boost(Match3Engine.Booster.ROW,59);assertTrue(wave.isNotEmpty());assertEquals(1,e.rescued);assertEquals(0,e.cargo.sumOf{it.sum()});e.boost(Match3Engine.Booster.ROW,59);assertEquals(1,e.rescued);val copy=Match3Engine(126);assertTrue(copy.restore(e.encode()));assertEquals(1,copy.rescued)}
 @Test fun legacySaveDoesNotAcquireImpossibleNewObjectives(){val e=Match3Engine(126);val legacy=e.encode().split(',').take(138).joinToString(",");assertTrue(e.restore(legacy));assertEquals(0,e.terrainRemaining);assertEquals(0,e.rescueTarget);assertEquals(0,e.cargo.sumOf{it.sum()})}
 @Test fun extraMovesAreBoundedAndPersisted(){val e=Match3Engine(1);assertFalse(e.addMoves(-1));assertFalse(e.addMoves(6));assertTrue(e.addMoves(5));val copy=Match3Engine(1);assertTrue(copy.restore(e.encode()));assertEquals(e.moves,copy.moves)}
 @Test fun malformedExtendedSaveIsRejectedAtomically(){val e=Match3Engine(101);val before=e.encode();val values=before.split(',').toMutableList();values[140]="99";assertFalse(e.restore(values.joinToString(",")));assertEquals(before,e.encode())}
}
