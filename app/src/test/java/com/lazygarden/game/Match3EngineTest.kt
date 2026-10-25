package com.lazygarden.game
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random
class Match3EngineTest {
 @Test fun campaignBoardsArePlayableAndRoundTrip() {
  for(level in 1..250){val e=Match3Engine(level,Random(level));assertTrue(e.hasMove());assertEquals(e.spec.ice,e.ice.sumOf{it.sum()});val copy=Match3Engine(level);assertTrue(copy.restore(e.encode()));assertEquals(e.encode(),copy.encode())}
 }
 @Test fun invalidSwapNeverConsumesAMoveOrChangesBoard(){val e=Match3Engine(1,Random(4));val before=e.encode();assertTrue(e.swap(0,63).isEmpty());assertEquals(before,e.encode())}
 @Test fun movesAndCascadesRemainConsistentAcrossCampaign(){
  for(level in listOf(1,25,75,150,250)){
   val e=Match3Engine(level,Random(level))
   repeat(e.spec.moves){if(!e.won && !e.failed){val pair=(0..63).flatMap{a->listOf(a to a+1,a to a+8)}.first{e.legal(it.first,it.second)};val old=e.moves;assertTrue(e.swap(pair.first,pair.second).isNotEmpty());assertEquals(old-1,e.moves);assertTrue(e.board.all{row->row.all{it in 0..23}})}}
   assertTrue(e.won || e.failed)
  }
 }
 @Test fun corruptSaveIsRejected(){val e=Match3Engine(1);assertFalse(e.restore("bad"));assertFalse(e.restore(Match3Engine(2).encode()))}
}
