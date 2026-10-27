package com.lazygarden.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.abs
import kotlin.math.sin

/** Living estate scene: progressive restoration plus several independently moving residents. */
class GardenWorldView(context:Context,private val tasks:Int,private val constructing:Boolean=false,private val decorations:List<DecorItem> = emptyList(),private val focus:DecorItem?=null):View(context){
 private val decorationArt=mutableMapOf<Int,Bitmap>()
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 private val after=BitmapFactory.decodeResource(resources,R.drawable.garden_estate)
 private val before=BitmapFactory.decodeResource(resources,R.drawable.garden_before)
 private val mask=Bitmap.createBitmap(256,384,Bitmap.Config.ARGB_8888)
 private val cast=listOf(
  BitmapFactory.decodeResource(resources,R.drawable.character_meti),
  BitmapFactory.decodeResource(resources,R.drawable.character_bejdi),
  BitmapFactory.decodeResource(resources,R.drawable.character_meli),
  BitmapFactory.decodeResource(resources,R.drawable.character_lina)
 )
 private var progress=0f
 private var life=0f
 private var animator:ValueAnimator?=null
 private var lifeAnimator:ValueAnimator?=null
 private val regions=arrayOf(
  RectF(.32f,.425f,.80f,.635f),RectF(.12f,.63f,.91f,1.04f),RectF(.75f,.31f,1.04f,.99f),
  RectF(-.04f,.46f,.32f,.65f),RectF(-.04f,.28f,.36f,.48f),RectF(.34f,.36f,.79f,.46f),RectF(.37f,.14f,1.04f,.415f)
 )
 private data class Walker(val art:Int,val start:Float,val end:Float,val ground:Float,val size:Float,val speed:Float,val phase:Float)
 private val walkers=listOf(
  Walker(0,.12f,.80f,.84f,.095f,.72f,.00f),
  Walker(1,.82f,.24f,.77f,.082f,.55f,.19f),
  Walker(2,.25f,.68f,.69f,.076f,.43f,.41f),
  Walker(3,.72f,.39f,.61f,.072f,.38f,.67f)
 )
 override fun onAttachedToWindow(){
  super.onAttachedToWindow()
  lifeAnimator=ValueAnimator.ofFloat(0f,1f).apply{duration=12000;repeatCount=ValueAnimator.INFINITE;interpolator=LinearInterpolator();addUpdateListener{life=it.animatedValue as Float;invalidate()};start()}
  if(constructing){animator=ValueAnimator.ofFloat(0f,1f).apply{duration=2200;addUpdateListener{progress=it.animatedValue as Float;invalidate()};start()}}
 }
 override fun onDetachedFromWindow(){animator?.cancel();lifeAnimator?.cancel();super.onDetachedFromWindow()}
 private fun triangle(t:Float):Float{val x=((t%1f)+1f)%1f;return 1f-abs(x*2f-1f)}
 private fun character(c:Canvas,b:Bitmap,cx:Float,ground:Float,size:Float,bob:Float,working:Boolean=false){
  val h=size*b.height/b.width;val y=ground-h+bob
  p.alpha=255;p.xfermode=null;p.shader=null
  if(working){c.save();c.rotate(sin(life*40f)*2.2f,cx,ground);c.drawBitmap(b,null,RectF(cx-size/2,y,cx+size/2,ground+bob),p);c.restore()} else c.drawBitmap(b,null,RectF(cx-size/2,y,cx+size/2,ground+bob),p)
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

  // Several residents now live independently in the estate instead of one sprite sliding across the screen.
  if(tasks>0||constructing){
   val active=regions[tasks.coerceAtMost(6)]
   val workX=dst.left+dw*active.centerX().coerceIn(.10f,.90f)
   val workY=dst.top+dh*active.bottom.coerceIn(.45f,.88f)
   walkers.forEachIndexed{index,w->
    val travel=triangle(life*w.speed+w.phase)
    var x=dst.left+dw*(w.start+(w.end-w.start)*travel)
    var y=dst.top+dh*w.ground
    val assigned=constructing && index<3
    if(assigned){
     val settle=(progress*2.2f).coerceAtMost(1f)
     val slot=(index-1)*dw*.075f
     x=x+(workX+slot-x)*settle;y=y+(workY-y)*settle
    }
    val step=sin((life*w.speed+w.phase)*Math.PI.toFloat()*22f)*dp(2)
    character(c,cast[w.art],x,y,dw*w.size,step,assigned&&progress>.42f)
   }
   // Birds/butterflies keep the restored world visibly active.
   p.color=0xffffffff.toInt();p.style=Paint.Style.STROKE;p.strokeWidth=dp(2).toFloat();p.alpha=210
   for(i in 0..2){val t=(life+i*.27f)%1f;val bx=dst.left+dw*(.15f+.72f*t);val by=dst.top+dh*(.28f+.035f*sin(t*18f+i));c.drawArc(RectF(bx-dp(8),by-dp(4),bx,by+dp(4)),190f,150f,false,p);c.drawArc(RectF(bx,by-dp(4),bx+dp(8),by+dp(4)),200f,150f,false,p)}
   p.style=Paint.Style.FILL;p.alpha=255
   if(constructing){p.color=0xffffd45c.toInt();p.style=Paint.Style.STROKE;p.strokeWidth=dp(3).toFloat();p.alpha=(120+100*progress).toInt();val pulse=dp(16)+(dp(16)*progress);c.drawCircle(workX,workY-dp(18),pulse,p);p.style=Paint.Style.FILL;p.alpha=255}
  }
  p.shader=LinearGradient(0f,0f,0f,height.toFloat(),intArrayOf(0x99102b25.toInt(),0x00000000,0x990c261f.toInt()),floatArrayOf(0f,.45f,1f),Shader.TileMode.CLAMP);c.drawRect(0f,0f,width.toFloat(),height.toFloat(),p);p.shader=null
  if(constructing){p.color=0xffffe6a3.toInt();p.textSize=18*resources.displayMetrics.density;p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.DEFAULT_BOLD;c.drawText("Ekipi po punon në kopsht…",width/2f,height*.55f,p)}
 }
 private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
}
