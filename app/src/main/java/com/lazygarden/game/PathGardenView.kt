package com.lazygarden.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Scene 3 keeps the shared restoration mechanics but adds path-specific premium work feedback. */
class PathGardenView(
 context:Context,
 initialElapsed:Long=0L,
 checkpoint:(Long)->Unit={},
 onComplete:()->Unit={}
):RestorationGardenView(context,2,initialElapsed,checkpoint,onComplete) {
 private val fx=Paint(Paint.ANTI_ALIAS_FLAG)
 private val characterPaint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

 override fun onDraw(canvas:Canvas) {
  super.onDraw(canvas)
  val ms=sceneElapsedMillis
  drawCufi(canvas,ms)
  // CLEAR/REMOVE/LEVEL/PLACE: stone dust follows the active path work instead of a static overlay.
  if(ms in 7_000L..27_000L) {
   val phase=(ms%1_050L)/1_050f
   for(i in 0 until 14) {
    val t=(phase+i/14f)%1f
    val angle=(i*0.91f+phase*2f*PI.toFloat())
    val spread=width*(.035f+.12f*t)
    val x=width*.52f+cos(angle)*spread
    val y=height*.66f+sin(angle)*spread*.28f-t*height*.055f
    fx.color=if(i%3==0)0xffead39a.toInt() else 0xffb99b69.toInt()
    fx.alpha=((1f-t)*150).toInt()
    canvas.drawCircle(x,y,width*(.0035f+.003f*(1f-t)),fx)
   }
  }
  // During stone placement each completed beat gets a short warm pulse at the work surface.
  if(ms in 19_000L..27_000L) {
   val beat=((ms-19_000L)%1_000L)/1_000f
   fx.style=Paint.Style.STROKE
   fx.strokeWidth=resources.displayMetrics.density*2f
   fx.color=0xffffdf83.toInt()
   fx.alpha=((1f-beat)*190).toInt()
   canvas.drawCircle(width*.52f,height*.665f,width*(.025f+.055f*beat),fx)
   fx.style=Paint.Style.FILL
  }
  // Reveal/celebration gets a restrained shimmer so the repaired path reads as a reward moment.
  if(ms>=30_000L) {
   val pulse=.5f+.5f*sin(ms/220f)
   for(i in 0 until 7) {
    val x=width*(.30f+i*.067f)
    val y=height*(.60f+sin(i*1.7f)*.025f)
    fx.color=0xffffe9a6.toInt()
    fx.alpha=(70+80*pulse).toInt()
    canvas.drawCircle(x,y,width*(.003f+.002f*pulse),fx)
   }
  }
  fx.alpha=255
 }

 /** Cufi now enters the actual path scene instead of existing only in story data. */
 private fun drawCufi(canvas:Canvas,ms:Long) {
  val arrival=(ms/3_500f).coerceIn(0f,1f)
  val eased=CharacterAnimation.ease(arrival)
  val startX=-width*.12f
  val restX=width*.18f
  val x=startX+(restX-startX)*eased
  val ground=height*.76f
  val w=width*.18f
  val aspect=CharacterSprites.aspect(context,"cufi")
  val walking=arrival<1f
  val frame=if(walking)((ms/150L)%4L).toInt() else if(ms>=30_000L) 6 else 0
  characterPaint.color=0x44302010
  canvas.drawOval(RectF(x-w*.30f,ground-w*.035f,x+w*.30f,ground+w*.035f),characterPaint)
  CharacterSprites.draw(
   context,canvas,characterPaint,"cufi",frame,
   RectF(x-w/2f,ground-w*aspect,x+w/2f,ground),
   facingRight=true
  )
 }
}