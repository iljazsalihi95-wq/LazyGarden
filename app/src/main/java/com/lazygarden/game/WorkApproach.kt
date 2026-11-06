package com.lazygarden.game

/** A crew member enters on the lower service path, turns at the object's edge, then stops. */
object WorkApproach {
 const val ARRIVAL_MILLIS=2200L
 fun progress(elapsed:Long)= (elapsed/ARRIVAL_MILLIS.toFloat()).coerceIn(0f,1f)
 fun position(targetX:Float,targetY:Float,progress:Float,fromRight:Boolean):CharacterAnimation.Position {
  val t=progress.coerceIn(0f,1f)
  if(t>=1f)return CharacterAnimation.Position(targetX,targetY)
  val entry=if(fromRight)1.12f else -.12f
  return if(t<.65f)CharacterAnimation.move(CharacterAnimation.Position(entry,.96f),CharacterAnimation.Position(targetX,.96f),t/.65f)
   else CharacterAnimation.move(CharacterAnimation.Position(targetX,.96f),CharacterAnimation.Position(targetX,targetY),(t-.65f)/.35f)
 }
}
