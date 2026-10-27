package com.lazygarden.game

/** Scene 3: live path restoration choreography. */
object PathSequence {
 const val DURATION=35_500L
 enum class Stage { ARRIVE, INSPECT, CLEAR, REMOVE_BROKEN, LEVEL_GROUND, LAY_STONES, BRUSH, REVEAL, CELEBRATE }
 fun stage(ms:Long)=Stage.entries[RestorationSequence.plan(2).index(ms)]
 fun progress(ms:Long,start:Long,end:Long)=((ms-start).toFloat()/(end-start).toFloat()).coerceIn(0f,1f)
 fun dialogue(ms:Long)=RestorationSequence.plan(2).let{it.steps[it.index(ms)].line}
}
