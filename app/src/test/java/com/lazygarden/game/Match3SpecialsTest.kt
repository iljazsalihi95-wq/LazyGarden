package com.lazygarden.game

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class Match3SpecialsTest {
 private fun board():Match3Engine=Match3Engine(30,Random(813)).apply{
  for(r in 0..7)for(c in 0..7)board[r][c]=(r*2+c)%6
  terrain.forEach{it.fill(0)};ice.forEach{it.fill(0)}
 }
 private fun createRun(length:Int):Match3Engine.Wave {
  val e=board()
  for(c in 1..length)e.board[3][c]=0
  e.board[3][3]=1;e.board[2][3]=0
  return e.swap(19,27).first()
 }
 @Test fun fourCreatesRocketAndFiveCreatesRainbow(){
  assertTrue(createRun(4).after.any{r->r.any{it/6==1}})
  assertTrue(createRun(5).after.any{r->r.any{it/6==4}})
 }
 @Test fun tAndLIntersectionsCreateBombs(){
  for(start in listOf(1,2)){
   val e=board()
   for(c in start..start+2)e.board[3][c]=0
   e.board[3][3]=1;e.board[2][3]=0;e.board[4][3]=0;e.board[5][3]=0
   val wave=e.swap(19,27).first()
   assertTrue("Intersection should create a bomb",wave.after.any{r->r.any{it/6==3}})
  }
 }
 @Test fun rainbowSwapClearsTheOtherColorAndRoundTrips(){
  val e=board();e.board[2][2]=24;e.board[2][3]=2
  val target=(0..63).filter{e.board[it/8][it%8]%6==2}.toSet()
  val moves=e.moves;val first=e.swap(18,19).first()
  assertTrue(first.cells.containsAll(target-19));assertEquals(moves-1,e.moves)
  val copy=Match3Engine(30);assertTrue(copy.restore(e.encode()));assertEquals(e.encode(),copy.encode())
  e.board[0][0]=29;assertTrue(copy.restore(e.encode()));assertEquals(29,copy.board[0][0])
 }
 @Test fun doubleRainbowClearsWholeBoard(){val e=board();e.board[2][2]=24;e.board[2][3]=25;assertEquals((0..63).toSet(),e.swap(18,19).first().cells.toSet())}
 @Test fun combinationsHaveDistinctFootprints(){
  fun hits(a:Int,b:Int):Set<Int>{val e=board();e.board[3][3]=a;e.board[3][4]=b;return e.swap(27,28).first().cells.toSet()}
  val rockets=hits(6,12);assertTrue((0..7).all{3*8+it in rockets&&it*8+4 in rockets})
  val bombs=hits(18,19);assertTrue((1..5).all{r->(2..6).all{c->r*8+c in bombs}})
  val mixed=hits(18,6);assertTrue((0..63).filter{it/8 in 2..4||it%8 in 3..5}.all{it in mixed})
  assertNotEquals(rockets,bombs);assertNotEquals(bombs,mixed)
 }
 @Test fun existingBombSavesKeepTheirMeaning(){val e=board();e.board[2][2]=18;val copy=Match3Engine(30);assertTrue(copy.restore(e.encode()));assertEquals(18,copy.board[2][2])}
}
