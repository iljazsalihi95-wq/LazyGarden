package com.lazygarden.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.sin

/** Original estate artwork with progressive restoration and a living work scene. */
class GardenWorldView(context:Context,private val tasks:Int,private val constructing:Boolean=false,private val decorations:List<DecorItem> = emptyList(),private val focus:DecorItem?=null):View(context){
 private val decorationArt=mutableMapOf<Int,Bitmap>()
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 private val after=BitmapFactory.decodeResource(resources,R.drawable.garden_estate)
 private val before=BitmapFactory.decodeResource(resources,R.drawable.garden_before)
 private val worker=BitmapFactory.decodeResource(resources,R.drawable.character_meti)
 private val helper=BitmapFactory.decodeResource(resources,R.drawable.character_bejdi)
 private val mask=Bitmap.createBitmap(256,384,Bitmap.Config.ARGB_8888)
 private var progress=0f
 private var life=0f
 private var animator:ValueAnimator?=null
 private var lifeAnimator:ValueAnimator?=null
 private val regions=arrayOf(
  RectF(.32f,.425f,.80f,.635f),RectF(.12f,.63f,.91f,1.04f),RectF(.75f,.31f,1.04f,.99f),
  RectF(-.04f,.46f,.32f,.65f),RectF(-.04f,.28f,.36f,.48f),RectF(.34f,.36f,.79f,.46f),RectF(.37f,.14f,1.04f,.415f)
 )
 override fun onAttachedToWindow(){
  super.onAttachedToWindow()
  lifeAnimator=ValueAnimator.ofFloat(0f,1f).apply{duration=4200;repeatCount=ValueAnimator.INFINITE;repeatMode=ValueAnimator.REVERSE;interpolator=AccelerateDecelerateInterpolator();addUpdateListener{life=it.animatedValue as Float;invalidate()};start()}
  if(constructing){animator=ValueAnimator.ofFloat(0f,1f).apply{duration=2200;interpolator=AccelerateDecelerateInterpolator();addUpdateListener{progress=it.animatedValue as Float;invalidate()};start()}}
 }
 override fun onDetachedFromWindow(){animator?.cancel();lifeAnimator?.cancel();super.onDetachedFromWindow()}
 private fun character(c:Canvas,b:Bitmap,cx:Float,ground:Float,size:Float,bob:Float){
  val h=size*b.height/b.width;val y=ground-h+bob
  p.alpha=255;p.xfermode=null;p.shader=null
  c.drawBitmap(b,null,RectF(cx-size/2,y,cx+size/2,ground+bob),p)
 }
 override fun onDraw(c:Canvas){
  super.onDraw(c)
  val scale=maxOf(width.toFloat()/after.width,height.toFloat()/after.height)
  val dw=after.width*scale;val dh=after.height*scale
  val dst=RectF((width-dw)/2,(height-dh)/2,(width+dw)/2,(height+dh)/2)
  focus?.let{dst.offset(0f,height*.39f-(dst.top+it.y*dh))}
  p.alpha=255;p.xfermode=null;p.shader=null;c.drawBitmap(before,null,dst,p)
  if(tasks>=7)c.drawBitmap(after,null,dst,p) else if(tasks>0||constructing){
   mask.eraseColor(Color.TRANSPARENT);val mc=Canvas(mask);val mp=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=Color.WHITE;maskFilter=BlurMaskFilter(3f,BlurMaskFilter.Blur.NORMAL)}
   for(i in regions.indices)if(i<tasks||(i==tasks&&constructing)){mp.alpha=if(i<tasks)255 else (255*progress).toInt();val r=regions[i];mc.drawRoundRect(RectF(r.left*256,r.top*384,r.right*256,r.bottom*384),5f,5f,mp)}
   val layer=c.saveLayer(dst,null);c.drawBitmap(after,null,dst,p);p.xfermode=PorterDuffXfermode(PorterDuff.Mode.DST_IN);c.drawBitmap(mask,null,dst,p);p.xfermode=null;c.restoreToCount(layer)
  }
  for(item in decorations){val bitmap=decorationArt.getOrPut(item.art){BitmapFactory.decodeResource(resources,item.art)};val w=dw*item.width;val h=w*bitmap.height/bitmap.width;val x=dst.left+item.x*dw;val y=dst.top+item.y*dh;p.alpha=255;p.shader=null;c.drawBitmap(bitmap,null,RectF(x-w/2,y-h,x+w/2,y),p)}

  // The estate is alive: characters cross the path; during restoration they converge on the active work zone.
  if(tasks>0||constructing){
   val bob=sin(life*Math.PI*2).toFloat()*dp(2).toFloat()
   val workX=if(constructing) dst.left+dw*(regions[tasks.coerceAtMost(6)].centerX()) else dst.left+dw*(.18f+.55f*life)
   val workY=if(constructing) dst.top+dh*(regions[tasks.coerceAtMost(6)].bottom.coerceAtMost(.88f)) else dst.top+dh*.82f
   character(c,worker,workX,workY,dw*.105f,bob)
   if(tasks>=2){val x2=if(constructing)workX-dw*.10f else dst.left+dw*(.82f-.42f*life);character(c,helper,x2,dst.top+dh*.76f,dw*.085f,-bob)}
   if(constructing){
    p.color=0xffffd45c.toInt();p.style=Paint.Style.STROKE;p.strokeWidth=dp(3).toFloat();p.alpha=(120+100*progress).toInt()
    val pulse=dp(16)+(dp(16)*progress);c.drawCircle(workX,workY-dp(18),pulse,p);p.style=Paint.Style.FILL;p.alpha=255
   }
  }
  p.shader=LinearGradient(0f,0f,0f,height.toFloat(),intArrayOf(0x99102b25.toInt(),0x00000000,0x990c261f.toInt()),floatArrayOf(0f,.45f,1f),Shader.TileMode.CLAMP);c.drawRect(0f,0f,width.toFloat(),height.toFloat(),p);p.shader=null
  if(constructing){p.color=0xffffe6a3.toInt();p.textSize=18*resources.displayMetrics.density;p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.DEFAULT_BOLD;c.drawText("Po punohet në kopsht…",width/2f,height*.55f,p)}
 }
 private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
}
