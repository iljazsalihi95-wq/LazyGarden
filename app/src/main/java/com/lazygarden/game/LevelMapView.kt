package com.lazygarden.game

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot

/** Native hit targets follow the painted estate path; chapter selection never unlocks levels. */
class LevelMapView(context:Context,private val save:GardenSave,private val chapter:Int=(save.level-1)/25,private val play:(Int)->Unit):View(context){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 private val d=resources.displayMetrics.density
 private val art=BitmapFactory.decodeResource(resources,R.drawable.garden_map)
 private val star=BitmapFactory.decodeResource(resources,R.drawable.reward_star)
 private val nodes=mutableListOf<Pair<Int,PointF>>()
 private val xs=floatArrayOf(.50f,.49f,.55f,.66f,.70f,.65f,.54f,.46f,.47f,.53f,.57f,.55f,.48f,.46f,.52f,.59f,.50f,.44f,.55f,.59f,.47f,.36f,.43f,.58f,.56f)
 val currentY get()=(height*(.93f-((save.level-1)%25)*.034f)).toInt()
 override fun onMeasure(w:Int,h:Int){val width=MeasureSpec.getSize(w);setMeasuredDimension(width,(width*art.height.toFloat()/art.width).toInt())}
 override fun onDraw(c:Canvas){
  p.alpha=255;p.shader=null;c.drawBitmap(art,null,Rect(0,0,width,height),p);nodes.clear()
  for(i in 0..24){
   val level=chapter*25+i+1
   val pt=PointF(width*xs[i],height*(.93f-i*.034f));nodes.add(level to pt)
   val radius=if(level==save.level)22*d else 17*d
   p.shader=RadialGradient(pt.x-radius*.3f,pt.y-radius*.4f,radius*1.8f,if(level==save.level)intArrayOf(0xfffff1a1.toInt(),0xffdfa02c.toInt()) else if(level<save.level)intArrayOf(0xff79dce6.toInt(),0xff176487.toInt()) else intArrayOf(0xffb6b9af.toInt(),0xff555e5c.toInt()),null,Shader.TileMode.CLAMP)
   c.drawCircle(pt.x,pt.y,radius,p);p.shader=null
   p.style=Paint.Style.STROKE;p.strokeWidth=2*d;p.color=0xffffe4a3.toInt();c.drawCircle(pt.x,pt.y,radius,p);p.style=Paint.Style.FILL
   p.typeface=Typeface.DEFAULT_BOLD;p.textSize=if(level>99)12*d else 15*d;p.textAlign=Paint.Align.CENTER;p.color=Color.WHITE
   c.drawText(if(level<=save.level)"$level" else "🔒",pt.x,pt.y+5*d,p)
   val rating=save.rating(level)
   for(s in 0 until rating){val x=pt.x+(s-(rating-1)/2f)*11*d;c.drawBitmap(star,null,RectF(x-7*d,pt.y+radius-2*d,x+7*d,pt.y+radius+12*d),p)}
  }
 }
 override fun onTouchEvent(e:MotionEvent):Boolean{
  if(e.action==MotionEvent.ACTION_DOWN)return true
  if(e.action==MotionEvent.ACTION_UP){performClick();nodes.minByOrNull{(_,p)->hypot(e.x-p.x,e.y-p.y)}?.let{(level,p)->if(hypot(e.x-p.x,e.y-p.y)<26*d && level<=save.level)play(level)};return true}
  return true
 }
 override fun performClick():Boolean{super.performClick();return true}
}
