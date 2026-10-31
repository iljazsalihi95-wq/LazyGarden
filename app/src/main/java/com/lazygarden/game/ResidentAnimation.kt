package com.lazygarden.game

/**
 * Lightweight atlas choreography for the family/resident layer.
 *
 * The atlas poses are shared, but the cycles deliberately revisit neutral poses
 * between gestures so residents read as people moving in the scene instead of
 * looping two-frame stickers. `phase` lets renderers offset family members and
 * avoid synchronized "robot" motion without changing the saved game state.
 */
object ResidentAnimation {
 enum class State { IDLE, WALK, LOOK, TALK, WORK, HAPPY, CELEBRATE }

 fun frame(state:State,tick:Int,phase:Int=0):Int {
  val cycle=when(state){
   State.IDLE -> intArrayOf(0,0,3,0,0,0)
   State.WALK -> intArrayOf(1,0,2,0)
   State.LOOK -> intArrayOf(3,0,3,0)
   State.TALK -> intArrayOf(4,0,4,3,0)
   State.WORK -> intArrayOf(5,6,5,0,6,5,0)
   State.HAPPY -> intArrayOf(4,7,4,7,0)
   State.CELEBRATE -> intArrayOf(7,4,7,0,7,4,0)
  }
  return cycle[Math.floorMod(tick+phase,cycle.size)]
 }

 /** Stable phase for a resident id, keeping simultaneous actors out of lockstep. */
 fun phase(actorId:String):Int = Math.floorMod(actorId.hashCode(),7)

 /**
  * Small secondary-motion envelope used by scene renderers for premium motion.
  * Values are intentionally subtle so feet stay visually planted.
  */
 fun motion(state:State,tick:Int,phase:Int=0):Motion {
  val t=(Math.floorMod(tick+phase,12)/12f)*(Math.PI*2).toFloat()
  val wave=kotlin.math.sin(t)
  return when(state){
   State.IDLE -> Motion(scale=1f+wave*.008f,lift=0f,sway=wave*.006f)
   State.WALK -> Motion(scale=1f,lift=kotlin.math.abs(wave)*.012f,sway=wave*.012f)
   State.LOOK -> Motion(scale=1f,lift=0f,sway=wave*.010f)
   State.TALK -> Motion(scale=1f+wave*.010f,lift=0f,sway=wave*.014f)
   State.WORK -> Motion(scale=1f+wave*.012f,lift=kotlin.math.abs(wave)*.006f,sway=wave*.018f)
   State.HAPPY -> Motion(scale=1f+wave*.018f,lift=kotlin.math.abs(wave)*.018f,sway=wave*.014f)
   State.CELEBRATE -> Motion(scale=1f+wave*.022f,lift=kotlin.math.abs(wave)*.025f,sway=wave*.020f)
  }
 }

 data class Motion(val scale:Float=1f,val lift:Float=0f,val sway:Float=0f)

 fun flowerVisit(life:Float):Float = when {
  life<.25f -> 0f
  life<.5f -> (life-.25f)*4
  life<.75f -> 1f
  else -> (1-life)*4
 }
}
