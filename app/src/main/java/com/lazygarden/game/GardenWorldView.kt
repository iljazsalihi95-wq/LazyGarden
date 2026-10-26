package com.lazygarden.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.View

/** Two aligned original paintings blended through saved restoration regions. */
class GardenWorldView(context:Context,private val tasks:Int,private val constructing:Boolean=false):View(context){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 private val after=BitmapFactory.decodeResource(resources,R.drawable.garden_estate)
 private val before=BitmapFactory.decodeResource(resources,R.drawable.garden_before)
 private val mask=Bitmap.createBitmap(256,384,Bitmap.Config.ARGB_8888)
 private var progress=0f
 private var animator:ValueAnimator?=null
 private val regions=arrayOf(
  RectF(.32f,.425f,.80f,.635f), // fountain
  RectF(.12f,.63f,.91f,1.04f), // path
  RectF(.75f,.31f,1.04f,.99f), // flower beds
  RectF(-.04f,.46f,.32f,.65f), // bench
  RectF(-.04f,.28f,.36f,.48f), // planting
  RectF(.34f,.36f,.79f,.46f), // entrance
  RectF(.37f,.14f,1.04f,.415f) // manor
 )
 override fun onAttachedToWindow(){super.onAttachedToWindow();if(constructing){animator=ValueAnimator.ofFloat(0f,1f).apply{duration=2000;addUpdateListener{progress=it.animatedValue as Float;invalidate()};start()}}}
 override fun onDetachedFromWindow(){animator?.cancel();super.onDetachedFromWindow()}
 override fun onDraw(c:Canvas){
  super.onDraw(c)
  val scale=maxOf(width.toFloat()/after.width,height.toFloat()/after.height)
  val dw=after.width*scale;val dh=after.height*scale
  val dst=RectF((width-dw)/2,(height-dh)/2,(width+dw)/2,(height+dh)/2)
  p.alpha=255;p.xfermode=null;p.shader=null;c.drawBitmap(before,null,dst,p)
  if(tasks>=7){c.drawBitmap(after,null,dst,p)}else if(tasks>0||constructing){
   mask.eraseColor(Color.TRANSPARENT)
   val mc=Canvas(mask);val mp=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=Color.WHITE;maskFilter=BlurMaskFilter(3f,BlurMaskFilter.Blur.NORMAL)}
   for(i in regions.indices){
    if(i<tasks || (i==tasks && constructing)){
     mp.alpha=if(i<tasks)255 else (255*progress).toInt()
     val r=regions[i];mc.drawRoundRect(RectF(r.left*256,r.top*384,r.right*256,r.bottom*384),5f,5f,mp)
    }
   }
   val layer=c.saveLayer(dst,null)
   c.drawBitmap(after,null,dst,p)
   p.xfermode=PorterDuffXfermode(PorterDuff.Mode.DST_IN);c.drawBitmap(mask,null,dst,p);p.xfermode=null
   c.restoreToCount(layer)
  }
  p.shader=LinearGradient(0f,0f,0f,height.toFloat(),intArrayOf(0x99102b25.toInt(),0x00000000,0x990c261f.toInt()),floatArrayOf(0f,.45f,1f),Shader.TileMode.CLAMP)
  c.drawRect(0f,0f,width.toFloat(),height.toFloat(),p);p.shader=null
  if(constructing){p.color=0xffffe6a3.toInt();p.textSize=18*resources.displayMetrics.density;p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.DEFAULT_BOLD;c.drawText("Po i kthejmë jetën kopshtit…",width/2f,height*.55f,p)}
 }
}
