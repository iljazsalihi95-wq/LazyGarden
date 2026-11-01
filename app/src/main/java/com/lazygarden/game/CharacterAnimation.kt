package com.lazygarden.game

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt

/** Reusable pose controller shared by native scene actors; the actor root stays on the ground plane. */
object CharacterAnimation {
 enum class State { IDLE,WALK,TALK,THINK,WORK,COLLECT,CARRY,HAPPY }
 data class Position(val x:Float,val y:Float)
 data class Gait(val position:Position,val bob:Float,val lean:Float,val step:Float)
 data class PoseMotion(val scaleX:Float=1f,val scaleY:Float=1f,val rotation:Float=0f,val lift:Float=0f,val pulse:Float=0f)
 fun ease(progress:Float):Float {val t=progress.coerceIn(0f,1f);return t*t*(3-2*t)}
 fun move(from:Position,to:Position,progress:Float):Position {val t=ease(progress);return Position(from.x+(to.x-from.x)*t,from.y+(to.y-from.y)*t)}
 /** Foot-planted stride: root Y is never bounced; step drives limbs/pose only. */
 fun gait(from:Position,to:Position,progress:Float):Gait {
  val u=progress.coerceIn(0f,1f);val dx=to.x-from.x;val dy=to.y-from.y;val distance=sqrt(dx*dx+dy*dy)
  val moving=u>0f&&u<1f&&distance>.001f
  if(!moving)return Gait(move(from,to,u),0f,0f,0f)
  val strideCount=(2.5f+distance*3.2f).coerceIn(3f,7f)
  val phase=(u*strideCount*2f*PI).toFloat();val step=sin(phase)
  val speedEnvelope=(6f*u*(1f-u)).coerceIn(0f,1f)
  val direction=if(abs(dx)<.001f)0f else if(dx>0f)1f else -1f
  val lean=(direction*speedEnvelope*.006f+step*.002f).coerceIn(-.009f,.009f)
  return Gait(move(from,to,u),0f,lean,step)
 }
 /** Subtle state-specific secondary motion. Values are deliberately small so feet remain planted. */
 fun poseMotion(state:State,elapsed:Long):PoseMotion {
  val seconds=elapsed.coerceAtLeast(0L)/1000f
  fun wave(hz:Float)=sin((seconds*hz*2f*PI).toFloat())
  // Fade secondary motion in after a state change so dialogue/work poses do not visually snap.
  val settle=ease((elapsed.coerceAtLeast(0L)/260f).coerceIn(0f,1f))
  fun scaled(value:Float)=value*settle
  return when(state){
   State.IDLE->{
    val breath=wave(.42f)
    PoseMotion(scaleX=1f+scaled(breath*.006f),scaleY=1f-scaled(breath*.004f),pulse=(breath+1f)*.5f*settle)
   }
   State.WALK->{
    val stride=wave(2.7f);val plant=abs(stride)
    PoseMotion(scaleX=1f+scaled(plant*.012f),scaleY=1f-scaled(plant*.010f),rotation=scaled(stride*.7f),pulse=plant*settle)
   }
   State.TALK->{
    val gesture=wave(1.8f);val emphasis=abs(wave(.9f))
    PoseMotion(rotation=scaled(gesture*.9f),scaleX=1f+scaled(emphasis*.006f),scaleY=1f-scaled(emphasis*.003f),lift=scaled(-emphasis*.0015f),pulse=(gesture+1f)*.5f*settle)
   }
   State.THINK->{
    val ponder=wave(.55f)
    PoseMotion(rotation=scaled(-1.2f+ponder*.35f),scaleX=1f-scaled(.003f),scaleY=1f-scaled(.005f),pulse=(ponder+1f)*.25f*settle)
   }
   State.WORK-> {
    val stroke=wave(2.2f);val effort=abs(wave(1.1f));val impact=abs(stroke)
    PoseMotion(rotation=scaled(stroke*1.8f),scaleX=1f+scaled(effort*.010f),scaleY=1f-scaled(effort*.006f),lift=scaled(-effort*.002f),pulse=impact*settle)
   }
   State.COLLECT-> {
    val scoop=abs(wave(1.9f));val reach=wave(.95f)
    PoseMotion(scaleX=1f+scaled(scoop*.018f),scaleY=1f-scaled(scoop*.012f),lift=scaled(-scoop*.004f),rotation=scaled(reach*.55f),pulse=scoop*settle)
   }
   State.CARRY->{
    val weight=wave(1.15f);val brace=abs(weight)
    PoseMotion(rotation=scaled(weight*.45f),scaleX=1f+scaled(brace*.005f),scaleY=1f-scaled(.008f+brace*.002f),lift=scaled(-.0015f),pulse=brace*.35f*settle)
   }
   State.HAPPY-> {
    val cheer=abs(wave(2.1f));val sway=wave(1.05f)
    PoseMotion(scaleX=1f+scaled(cheer*.025f),scaleY=1f+scaled(cheer*.025f),rotation=scaled(sway*.8f),lift=scaled(-cheer*.006f),pulse=(wave(2.1f)+1f)*.5f*settle)
   }
  }
 }
 fun facesRight(x:Float,targetX:Float)=x<=targetX
 fun routeFacing(from:Position,to:Position,progress:Float,targetX:Float):Boolean = if(progress>=0f&&progress<1f&&abs(to.x-from.x)>.001f)to.x>from.x else facesRight(move(from,to,progress).x,targetX)
 data class RouteSample(val position:Position,val facingRight:Boolean,val moving:Boolean,val step:Float=0f,val lean:Float=0f)
 fun sampleRoute(from:Position,to:Position,progress:Float,targetX:Float):RouteSample {val gait=gait(from,to,progress);return RouteSample(gait.position,routeFacing(from,to,progress,targetX),progress>0f&&progress<1f&&(abs(to.x-from.x)+abs(to.y-from.y)>.001f),gait.step,gait.lean)}
 class Player {
  var state=State.IDLE;private set
  private var started=0L;private var initialized=false
  fun update(next:State,now:Long){if(!initialized||state!=next){state=next;started=now;initialized=true}}
  fun elapsed(now:Long)=(now-started).coerceAtLeast(0L)
  fun frame(now:Long,arrivalAtlas:Boolean)=CharacterAnimation.frame(state,elapsed(now),arrivalAtlas)
  fun motion(now:Long)=poseMotion(state,elapsed(now))
 }
 private fun frameDuration(state:State)=when(state){
  State.WALK->165L
  State.WORK,State.COLLECT->180L
  State.HAPPY->170L
  State.TALK->205L
  State.IDLE,State.THINK,State.CARRY->240L
 }
 fun frame(state:State,elapsed:Long,arrivalAtlas:Boolean):Int {
  val tick=(elapsed.coerceAtLeast(0)/frameDuration(state)).toInt()
  val poses=if(arrivalAtlas)when(state){
   State.IDLE->intArrayOf(0)
   State.WALK->intArrayOf(1,0,2,0)
   State.TALK->intArrayOf(3,0)
   State.THINK->intArrayOf(4)
   State.WORK->intArrayOf(5,0,5)
   State.COLLECT->intArrayOf(5,6,5)
   State.CARRY->intArrayOf(6)
   State.HAPPY->intArrayOf(7,0,7)
  }else when(state){
   State.IDLE->intArrayOf(0)
   State.WALK->intArrayOf(1,0,2,0)
   State.TALK->intArrayOf(4,0)
   State.THINK->intArrayOf(3)
   State.WORK->intArrayOf(5,6,5)
   State.COLLECT->intArrayOf(5,6)
   State.CARRY->intArrayOf(6)
   State.HAPPY->intArrayOf(7,0,7)
  }
  return poses[tick%poses.size]
 }
}
