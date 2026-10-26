package com.lazygarden.game

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.sin

/** A scrollable chapter-sized path; only unlocked nodes can launch a level. */
class LevelMapView(context: Context, private val save: GardenSave, private val play: (Int) -> Unit) : View(context) {
 private val p = Paint(Paint.ANTI_ALIAS_FLAG)
 private val d = resources.displayMetrics.density
 private val nodes = mutableListOf<Pair<Int, PointF>>()
 private val first = ((save.level - 1) / 25) * 25 + 1
 override fun onMeasure(w: Int, h: Int) { setMeasuredDimension(MeasureSpec.getSize(w), (2400*d).toInt()) }
 override fun onDraw(c: Canvas) {
  c.drawColor(Color.rgb(29, 90, 61)); nodes.clear()
  val path = Path()
  for (i in 0..24) {
   val point = PointF(width * (.5f + .27f * sin(i * .9f)), (2250 - i * 86)*d)
   nodes.add(first+i to point)
   if(i==0)path.moveTo(point.x,point.y) else path.lineTo(point.x,point.y)
  }
  p.style=Paint.Style.STROKE;p.strokeWidth=50*d;p.color=0xffb98b52.toInt();c.drawPath(path,p)
  p.strokeWidth=36*d;p.color=0xffead2a0.toInt();c.drawPath(path,p);p.style=Paint.Style.FILL
  for((level,pt) in nodes) {
   p.color=if(level==save.level)0xffffd45e.toInt() else if(level<save.level)0xff7cce73.toInt() else 0xff677b72.toInt()
   c.drawCircle(pt.x,pt.y,30*d,p)
   p.color=Color.WHITE;p.textSize=20*d;p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.DEFAULT_BOLD
   c.drawText(if(level<=save.level)"$level" else "🔒",pt.x,pt.y+7*d,p)
   if(save.rating(level)>0){p.color=0xffffdf70.toInt();p.textSize=15*d;c.drawText("★".repeat(save.rating(level)),pt.x,pt.y+44*d,p)}
  }
  p.color=Color.WHITE;p.textSize=23*d
  val zones=arrayOf("Hyrja", "Kopshti", "Shatërvani", "Serra", "Liqeni", "Prona")
  c.drawText("${zones[((first-1)/Campaign.COUNT.toFloat()*6).toInt().coerceAtMost(5)]} · $first–${first+24}",width/2f,70*d,p)
 }
 override fun onTouchEvent(e: MotionEvent): Boolean {
  if(e.action==MotionEvent.ACTION_DOWN)return true
  if(e.action==MotionEvent.ACTION_UP){performClick();nodes.firstOrNull { (_,p) -> kotlin.math.hypot(e.x-p.x,e.y-p.y)<36*d }?.let{if(it.first<=save.level)play(it.first)};return true}
  return true
 }
 override fun performClick(): Boolean {super.performClick();return true}
}
