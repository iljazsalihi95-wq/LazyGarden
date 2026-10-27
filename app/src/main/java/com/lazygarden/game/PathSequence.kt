package com.lazygarden.game

/** Scene 3: live path restoration choreography. */
object PathSequence {
 const val DURATION=34_000L
 enum class Stage { ARRIVE, INSPECT, CLEAR, REMOVE_BROKEN, LEVEL_GROUND, LAY_STONES, BRUSH, REVEAL, CELEBRATE }
 fun stage(ms:Long)=when(ms.coerceIn(0,DURATION)){
  in 0..3499->Stage.ARRIVE
  in 3500..6499->Stage.INSPECT
  in 6500..10499->Stage.CLEAR
  in 10500..14499->Stage.REMOVE_BROKEN
  in 14500..18499->Stage.LEVEL_GROUND
  in 18500..26499->Stage.LAY_STONES
  in 26500..29499->Stage.BRUSH
  in 29500..31999->Stage.REVEAL
  else->Stage.CELEBRATE
 }
 fun progress(ms:Long,start:Long,end:Long)=((ms-start).toFloat()/(end-start).toFloat()).coerceIn(0f,1f)
 fun dialogue(ms:Long)=when(stage(ms)){
  Stage.INSPECT->"Ky shteg është dëmtuar. Ta bëjmë përsëri të bukur!"
  Stage.LAY_STONES->"Gurët e rinj po vendosen. Edhe pak!"
  Stage.CELEBRATE->"Shtegu u rregullua! Tani kopshti duket shumë më bukur."
  else->""
 }
}
