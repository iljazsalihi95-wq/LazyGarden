package com.lazygarden.game

/** Atlas poses are shared, while every resident owns a different phase of the cycle. */
object ResidentAnimation {
 enum class State { IDLE, WALK, LOOK, TALK, WORK, HAPPY, CELEBRATE }
 fun frame(state:State,tick:Int):Int {
  val cycle=when(state){
   State.IDLE -> intArrayOf(0)
   State.WALK -> intArrayOf(1,0,2,0)
   State.LOOK -> intArrayOf(3)
   State.TALK -> intArrayOf(4,0,4,0)
   State.WORK -> intArrayOf(5,6)
   State.HAPPY -> intArrayOf(4,7,4,0)
   State.CELEBRATE -> intArrayOf(7,7,0)
  }
  return cycle[Math.floorMod(tick,cycle.size)]
 }
 fun flowerVisit(life:Float):Float = when {
  life<.25f -> 0f
  life<.5f -> (life-.25f)*4
  life<.75f -> 1f
  else -> (1-life)*4
 }
}
