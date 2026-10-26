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
 private val badge=GameSkin.bitmap(context,R.drawable.hd_level_badge)
 private val lock=GameSkin.bitmap(context,R.drawable.hd_lock)
 private val gold=ColorMatrixColorFilter(ColorMatrix(floatArrayOf(.2f,.7f,.7f,0f,0f,.2f,.6f,.2f,0f,0f,.1f,.15f,.02f,0f,0f,0f,0f,0f,1f,0f)))
 private val gray=ColorMatrixColorFilter(ColorMatrix().apply{setSaturation(0f)})
 private val nodes=mutableListOf<Pair<Int,PointF>>()
 private val xs=floatArrayOf(.50f,.49f,.55f,.66f,.70f,.65f,.54f,.46f,.47f,.53f,.57f,.55f,.48f,.46f,.52f,.59f,.50f,.44f,.55f,.59f,.47f,.36f,.43f,.58f,.56f)
 val currentY get()=(height*(.93f-((save.level-1)%25)*.034f)).toInt()
 override fun onMeasure(w:Int,h:Int){val width=MeasureSpec.getSize(w);setMeasuredDimension(width,(width*art.height.toFloat()/art.width).toInt())}
 override fun onDraw(c:Canvas){
  p.alpha=255;p.shader=null;c.drawBitmap(art,null,Rect(0,0,width,height),p);nodes.clear()
  for(i in 0..24){
   val level=chapter*25+i+1
   val pt=PointF(width*xs[i],height*(.93f-i*.034f));nodes.add(level to pt)
   val size=if(level==save.level)62*d else 50*d
   val bh=size*badge.height/badge.width
   p.alpha=if(level>save.level)210 else 255;p.colorFilter=if(level==save.level)gold else if(level>save.level)gray else null
   c.drawBitmap(badge,null,RectF(pt.x-size/2,pt.y-bh/2,pt.x+size/2,pt.y+bh/2),p);p.colorFilter=null;p.alpha=255
   if(level>save.level)c.drawBitmap(lock,null,RectF(pt.x-10*d,pt.y-15*d,pt.x+10*d,pt.y+7*d),p)
   else{
    p.typeface=Typeface.create("sans-serif-condensed",Typeface.BOLD);p.textSize=if(level>99)14*d else 18*d;p.textAlign=Paint.Align.CENTER
    p.style=Paint.Style.STROKE;p.strokeWidth=2*d;p.color=0xff284b57.toInt();c.drawText("$level",pt.x,pt.y+2*d,p)
    p.style=Paint.Style.FILL;p.color=Color.WHITE;c.drawText("$level",pt.x,pt.y+2*d,p)
   }
   val rating=save.rating(level)
   for(st in 0 until rating){val x=pt.x+(st-(rating-1)/2f)*12*d;c.drawBitmap(star,null,RectF(x-8*d,pt.y+bh/2-12*d,x+8*d,pt.y+bh/2+4*d),p)}
   if(level==save.level)c.drawBitmap(star,null,RectF(pt.x-8*d,pt.y-bh/2-10*d,pt.x+8*d,pt.y-bh/2+6*d),p)

  }
 }
 override fun onTouchEvent(e:MotionEvent):Boolean{
  if(e.action==MotionEvent.ACTION_DOWN)return true
  if(e.action==MotionEvent.ACTION_UP){performClick();nodes.minByOrNull{(_,p)->hypot(e.x-p.x,e.y-p.y)}?.let{(level,p)->if(hypot(e.x-p.x,e.y-p.y)<26*d && level<=save.level)play(level)};return true}
  return true
 }
 override fun performClick():Boolean{super.performClick();return true}
}
