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
   repeat(e.spec.moves){if(!e.won && !e.failed){val pair=(0..63).flatMap{a->listOf(a to a+1,a to a+8)}.first{e.legal(it.first,it.second)};val old=e.moves;assertTrue(e.swap(pair.first,pair.second).isNotEmpty());assertEquals(old-1,e.moves);assertTrue(e.board.all{row->row.all{it in 0..29}})}}
   assertTrue(e.won || e.failed)
  }
 }
 @Test fun corruptSaveIsRejected(){val e=Match3Engine(1);assertFalse(e.restore("bad"));assertFalse(e.restore(Match3Engine(2).encode()))}
 @Test fun allBoostersHaveRealEffectsAndPreserveMoves(){
  for(kind in Match3Engine.Booster.entries){
   val e=Match3Engine(30,Random(401));val before=e.encode();val moves=e.moves
   val waves=e.boost(kind,27)
   assertTrue("$kind must resolve",waves.isNotEmpty());assertEquals(moves,e.moves)
   assertNotEquals(before,e.encode());assertTrue(e.board.all{row->row.all{it in 0..29}})
   if(kind!=Match3Engine.Booster.SHUFFLE)assertTrue(e.score>0)
   val copy=Match3Engine(30);assertTrue(copy.restore(e.encode()));assertEquals(e.encode(),copy.encode())
  }
 }
 @Test fun premiumBoostersStayFunctionalAcrossEarlyMidAndLateCampaign(){
  val levels=listOf(1,50,125,200,250)
  for(level in levels)for(kind in Match3Engine.Booster.entries){
   val e=Match3Engine(level,Random(level*97+kind.ordinal));val moves=e.moves;val before=e.encode()
   val target=(0 until 64).firstOrNull{index->e.boost(kind,index).isNotEmpty()}
   if(target!=null){
    assertEquals("$kind must never spend a normal move",moves,e.moves)
    assertNotEquals("$kind must visibly change gameplay at level $level",before,e.encode())
    assertTrue("$kind must leave valid tiles",e.board.all{row->row.all{it in 0..29}})
    assertTrue("$kind must leave a serializable board",Match3Engine(level).restore(e.encode()))
   }
  }
 }
 @Test fun boosterPricesArePositiveAndDistinctEnoughForInventoryEconomy(){
  val costs=Match3Engine.Booster.entries.map{it.cost}
  assertTrue(costs.all{it>0})
  assertTrue("premium booster shop needs multiple price tiers",costs.toSet().size>=3)
 }
 @Test fun invalidBoosterTargetDoesNotMutate(){val e=Match3Engine(1);val state=e.encode();assertTrue(e.boost(Match3Engine.Booster.HAMMER,-1).isEmpty());assertEquals(state,e.encode())}
 @Test fun malformedTokenCannotBeSilentlyDropped(){val e=Match3Engine(1);assertFalse(e.restore(e.encode()+",bad"))}
 @Test fun terminalBoardsRejectSwapsAndBoosters(){
  val e=Match3Engine(1,Random(33))
  while(!e.won && !e.failed){val pair=(0..63).flatMap{a->listOf(a to a+1,a to a+8)}.first{e.legal(it.first,it.second)};e.swap(pair.first,pair.second)}
  val state=e.encode();assertFalse(e.legal(0,1));assertTrue(e.boost(Match3Engine.Booster.SHUFFLE,0).isEmpty());assertEquals(state,e.encode())
  assertEquals(e.won,e.earnedStars in 1..3)
 }
 @Test fun gravityTracksPreserveSurvivorsAndFallOnlyDownward(){
  for(level in listOf(1,20,100)){
   val e=Match3Engine(level,Random(level*39))
   repeat(10){if(!e.won && !e.failed){
    val move=(0..63).flatMap{a->listOf(a to a+1,a to a+8)}.first{e.legal(it.first,it.second)}
    for(wave in e.swap(move.first,move.second))if(!wave.shuffled){
     for(c in 0..7)for(r in 0..7){val origin=wave.origins[r][c]
      assertTrue(origin<=r)
      if(r>0)assertTrue(wave.origins[r-1][c]<origin)
      if(origin>=0 && origin*8+c !in wave.cells)assertEquals(wave.before[origin][c],wave.after[r][c])
     }
     assertTrue(wave.iceAfter.sumOf{it.sum()}<=wave.iceBefore.sumOf{it.sum()})
    }
   }}
  }
 }
}
