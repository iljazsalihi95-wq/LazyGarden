package com.lazygarden.game

/** Deterministic choreography; milliseconds advance only while the scene is visible. */
object FountainSequence {
 enum class Stage { IDLE, WALK, LOOK, TALK, DISCOVER, WORK, REPAIR, WATER_START, CELEBRATE, COMPLETED }
 const val DURATION = 26000L
 fun stage(ms:Long):Stage = when {
  ms<800 -> Stage.IDLE
  ms<4800 -> Stage.WALK
  ms<6800 -> Stage.LOOK
  ms<9600 -> Stage.TALK
  ms<12400 -> Stage.DISCOVER
  ms<17400 -> Stage.WORK
  ms<19400 -> Stage.REPAIR
  ms<22200 -> Stage.WATER_START
  ms<DURATION -> Stage.CELEBRATE
  else -> Stage.COMPLETED
 }
 private fun smooth(value:Float):Float { val t=value.coerceIn(0f,1f);return t*t*(3-2*t) }
 fun camera(ms:Long):Float = smooth((ms-4000)/2500f)*(1-smooth((ms-24000)/2000f))
 fun repair(ms:Long):Float = ((ms-12400)/7000f).coerceIn(0f,1f)
 fun water(ms:Long) = ((ms-19400)/2800f).coerceIn(0f,1f)
 fun approach(ms:Long,actor:Int):Float = ((ms-800-actor*180)/ (3400f-actor*180)).coerceIn(0f,1f)
 fun speaker(ms:Long):Int = when(stage(ms)) {
  Stage.TALK -> 2
  Stage.DISCOVER -> 0
  Stage.WATER_START -> 1
  Stage.CELEBRATE -> 3
  else -> -1
 }
 fun dialogue(ms:Long):String = when(stage(ms)) {
  Stage.TALK -> "Pse nuk punon shatërvani?"
  Stage.DISCOVER -> "Pompa duket e bllokuar."
  Stage.WATER_START -> "Shikoni! Po vjen uji!"
  Stage.CELEBRATE -> "Ura! Erdhi uji!"
  else -> ""
 }
}
