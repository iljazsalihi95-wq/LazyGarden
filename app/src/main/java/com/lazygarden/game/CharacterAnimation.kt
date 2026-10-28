package com.lazygarden.game

/** Reusable pose controller shared by native scene actors; movement always stops at its destination. */
object CharacterAnimation {
 enum class State { IDLE,WALK,TALK,THINK,WORK,COLLECT,CARRY,HAPPY }
 data class Position(val x:Float,val y:Float)
 fun ease(progress:Float):Float {val t=progress.coerceIn(0f,1f);return t*t*(3-2*t)}
 fun move(from:Position,to:Position,progress:Float):Position {val t=ease(progress);return Position(from.x+(to.x-from.x)*t,from.y+(to.y-from.y)*t)}
 fun facesRight(x:Float,targetX:Float)=x<=targetX
 fun routeFacing(from:Position,to:Position,progress:Float,targetX:Float):Boolean =
  if(progress>=0f&&progress<1f&&kotlin.math.abs(to.x-from.x)>.001f)to.x>from.x
  else facesRight(move(from,to,progress).x,targetX)
 data class RouteSample(val position:Position,val facingRight:Boolean,val moving:Boolean)
 fun sampleRoute(from:Position,to:Position,progress:Float,targetX:Float):RouteSample = RouteSample(
  move(from,to,progress),routeFacing(from,to,progress,targetX),
  progress>=0f&&progress<1f&&(kotlin.math.abs(to.x-from.x)+kotlin.math.abs(to.y-from.y)>.001f)
 )
 /** Each actor owns its state clock; entering TALK/WORK starts that pose cycle at frame zero. */
 class Player {
  var state=State.IDLE;private set
  private var started=0L
  private var initialized=false
  fun update(next:State,now:Long){if(!initialized||state!=next){state=next;started=now;initialized=true}}
  fun elapsed(now:Long)=(now-started).coerceAtLeast(0L)
  fun frame(now:Long,arrivalAtlas:Boolean)=CharacterAnimation.frame(state,elapsed(now),arrivalAtlas)
 }
 fun frame(state:State,elapsed:Long,arrivalAtlas:Boolean):Int {
  val tick=(elapsed.coerceAtLeast(0)/180).toInt()
  val poses=if(arrivalAtlas)when(state){
   State.IDLE->intArrayOf(0);State.WALK->intArrayOf(1,0,2,0);State.TALK->intArrayOf(3,0)
   State.THINK->intArrayOf(4);State.WORK,State.COLLECT->intArrayOf(5)
   State.CARRY->intArrayOf(6);State.HAPPY->intArrayOf(7,0,7)
  }else when(state){
   State.IDLE->intArrayOf(0);State.WALK->intArrayOf(1,0,2,0);State.TALK->intArrayOf(4,0)
   State.THINK->intArrayOf(3);State.WORK,State.COLLECT->intArrayOf(5,6)
   State.CARRY->intArrayOf(6);State.HAPPY->intArrayOf(7,0,7)
  }
  return poses[tick%poses.size]
 }
}
