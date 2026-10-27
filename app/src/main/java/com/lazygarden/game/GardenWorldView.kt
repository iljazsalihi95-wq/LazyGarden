package com.lazygarden.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.abs
import kotlin.math.sin

/** Living estate: residents walk, talk and work inside the garden instead of story-only screens. */
class GardenWorldView(context:Context,private val tasks:Int,private val constructing:Boolean=false,private val decorations:List<DecorItem> = emptyList(),private val focus:DecorItem?=null,private val onRestorationComplete:()->Unit={},initialElapsed:Long=0L,private val checkpoint:(Long)->Unit={}):View(context){
 private val decorationArt=mutableMapOf<Int,Bitmap>()
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 private val after=BitmapFactory.decodeResource(resources,R.drawable.garden_estate)
 private val before=BitmapFactory.decodeResource(resources,R.drawable.garden_before)
 private val mask=Bitmap.createBitmap(256,384,Bitmap.Config.ARGB_8888)
 private val leafArt by lazy { GameSkin.bitmap(context,R.drawable.tile_leaf) }
 private val names=listOf("Meti","Bejdi","Meli","Lina")
 private var progress=0f; private var life=0f
 private var sceneMs=initialElapsed; private var completed=false
 private val sprites by lazy { BitmapFactory.decodeResource(resources,R.drawable.resident_atlas) }
 private var lifeAnimator:ValueAnimator?=null
 private val regions=arrayOf(RectF(.32f,.425f,.80f,.635f),RectF(.12f,.63f,.91f,1.04f),RectF(.75f,.31f,1.04f,.99f),RectF(-.04f,.46f,.32f,.65f),RectF(-.04f,.28f,.36f,.48f),RectF(.34f,.36f,.79f,.46f),RectF(.37f,.14f,1.04f,.415f))
 override fun onAttachedToWindow(){super.onAttachedToWindow();startClock()}
 private fun startClock(){
  if(windowVisibility!=VISIBLE || !isAttachedToWindow || lifeAnimator!=null)return
  var previous=android.os.SystemClock.uptimeMillis()
  lifeAnimator=ValueAnimator.ofFloat(0f,1f).apply{duration=12000;repeatCount=ValueAnimator.INFINITE;interpolator=LinearInterpolator();addUpdateListener{
   val now=android.os.SystemClock.uptimeMillis();val dt=(now-previous).coerceIn(0L,1000L);previous=now
   life=(life+dt/12000f)%1f
   if(constructing && !completed){sceneMs+=dt;progress=(sceneMs/FountainSequence.DURATION.toFloat()).coerceIn(0f,1f)
    if(sceneMs>=FountainSequence.DURATION){completed=true;post{if(isAttachedToWindow)onRestorationComplete()}}
   };invalidate()
  };start()}
 }
 override fun onWindowVisibilityChanged(visibility:Int){super.onWindowVisibilityChanged(visibility);if(visibility==VISIBLE)startClock() else stopClock()}
 private fun stopClock(){if(constructing)checkpoint(sceneMs);lifeAnimator?.removeAllUpdateListeners();lifeAnimator?.cancel();lifeAnimator=null}
 override fun onDetachedFromWindow(){stopClock();super.onDetachedFromWindow()}
 private fun bubble(c:Canvas,x:Float,y:Float,name:String,text:String){
  val w=(width*.48f).coerceAtMost(dp(210).toFloat());val h=dp(58).toFloat();val left=(x-w/2).coerceIn(dp(8).toFloat(),width-w-dp(8));val top=(y-h-dp(12)).coerceAtLeast(dp(72).toFloat())
  p.color=0xf5fff8e7.toInt();p.style=Paint.Style.FILL;c.drawRoundRect(RectF(left,top,left+w,top+h),dp(14).toFloat(),dp(14).toFloat(),p)
  val path=Path().apply{moveTo(x-dp(8),top+h);lineTo(x,top+h+dp(10));lineTo(x+dp(8),top+h);close()};c.drawPath(path,p)
  p.color=0xff2f7d45.toInt();p.textAlign=Paint.Align.LEFT;p.typeface=Typeface.DEFAULT_BOLD;p.textSize=dp(12).toFloat();c.drawText(name,left+dp(12),top+dp(20),p)
  p.color=0xff4c3927.toInt();p.typeface=Typeface.DEFAULT;p.textSize=dp(11).toFloat();c.drawText(text.take(34),left+dp(12),top+dp(42),p)
 }
 override fun onDraw(c:Canvas){super.onDraw(c);val scale=maxOf(width.toFloat()/after.width,height.toFloat()/after.height);val dw=after.width*scale;val dh=after.height*scale;val dst=RectF((width-dw)/2,(height-dh)/2,(width+dw)/2,(height+dh)/2);focus?.let{dst.offset(0f,height*.39f-(dst.top+it.y*dh))};p.alpha=255;p.xfermode=null;p.shader=null;c.drawBitmap(before,null,dst,p)
  if(tasks>=7)c.drawBitmap(after,null,dst,p) else if(tasks>0||constructing){mask.eraseColor(Color.TRANSPARENT);val mc=Canvas(mask);val mp=Paint(Paint.ANTI_ALIAS_FLAG).apply{color=Color.WHITE;maskFilter=BlurMaskFilter(3f,BlurMaskFilter.Blur.NORMAL)};for(i in regions.indices)if(i<tasks||(i==tasks&&constructing)){mp.alpha=if(i<tasks)255 else (255*if(tasks==0)FountainSequence.water(sceneMs) else progress).toInt();val r=regions[i];mc.drawRoundRect(RectF(r.left*256,r.top*384,r.right*256,r.bottom*384),5f,5f,mp)};val layer=c.saveLayer(dst,null);c.drawBitmap(after,null,dst,p);p.xfermode=PorterDuffXfermode(PorterDuff.Mode.DST_IN);c.drawBitmap(mask,null,dst,p);p.xfermode=null;c.restoreToCount(layer)}
  for(item in decorations){val bitmap=decorationArt.getOrPut(item.art){BitmapFactory.decodeResource(resources,item.art)};val w=dw*item.width;val h=w*bitmap.height/bitmap.width;val x=dst.left+item.x*dw;val y=dst.top+item.y*dh;p.alpha=255;p.shader=null;c.drawBitmap(bitmap,null,RectF(x-w/2,y-h,x+w/2,y),p)}
  drawResidentsAndWater(c,dst,dw,dh)
  p.shader=LinearGradient(0f,0f,0f,height.toFloat(),intArrayOf(0x99102b25.toInt(),0x00000000,0x990c261f.toInt()),floatArrayOf(0f,.45f,1f),Shader.TileMode.CLAMP);c.drawRect(0f,0f,width.toFloat(),height.toFloat(),p);p.shader=null

 }

 private fun resident(c:Canvas,id:Int,x:Float,y:Float,w:Float,frame:Int,faceRight:Boolean){
  val sheet=sprites;val cw=sheet.width/8;val ch=sheet.height/4
  val src=Rect(frame*cw,id*ch,(frame+1)*cw,(id+1)*ch)
  val h=w*ch/cw;val saved=c.save();c.scale(if(faceRight)1f else -1f,1f,x,y)
  p.shader=null;p.alpha=255;p.colorFilter=null
  c.drawBitmap(sheet,src,RectF(x-w/2,y-h,x+w/2,y),p);c.restoreToCount(saved)
 }
 private fun drawResidentsAndWater(c:Canvas,dst:RectF,dw:Float,dh:Float){
  val stage=FountainSequence.stage(sceneMs)
  val fountain=tasks==0 && constructing
  val fx=dst.left+dw*.55f;val fy=dst.top+dh*.515f
  val amount=if(tasks>0)1f else if(constructing)FountainSequence.water(sceneMs) else 0f
  if(amount>0f)drawWater(c,fx,fy,dw,amount)
  if(tasks==0 && amount<1f){
   // Debris marks the blocked intake; it clears progressively during the repair.
   val debris=if(constructing)((19400-sceneMs)/2000f).coerceIn(0f,1f) else 1f
   p.alpha=(255*debris).toInt()
   for(i in 0..3){val x=fx+dw*(.095f+i*.012f);val y=fy+dh*(.035f+(i%2)*.006f)
    c.drawBitmap(leafArt,null,RectF(x-dw*.018f,y-dw*.018f,x+dw*.018f,y+dw*.018f),p)
   };p.alpha=255
  }
  val xs=floatArrayOf(.40f,.68f,.30f,.77f);val ys=floatArrayOf(.59f,.59f,.64f,.64f)
  for(id in 0..3){
   val t=if(fountain)FountainSequence.approach(sceneMs,id) else if(tasks>0)1f else 0f
   val startX=if(id%2==0).15f else .88f;val startY=.81f+id*.025f
   var x=dst.left+dw*(startX+(xs[id]-startX)*t);var y=dst.top+dh*(startY+(ys[id]-startY)*t)
   val visit=if(tasks>0 && !constructing && id>=2)ResidentAnimation.flowerVisit(life) else 0f
   if(visit>0){val targetX=if(id==2).21f else .86f;val targetY=if(id==2).50f else .48f
    x+=(dst.left+dw*targetX-x)*visit;y+=(dst.top+dh*targetY-y)*visit
   }
   val walking=(fountain && stage==FountainSequence.Stage.WALK && t<1f) || visit>0f && visit<1f
   val working=fountain && stage in listOf(FountainSequence.Stage.WORK,FountainSequence.Stage.REPAIR) && id<2
   val speaking=fountain && FountainSequence.speaker(sceneMs)==id
   val celebrating=fountain && stage==FountainSequence.Stage.CELEBRATE
   val tick=(life*96).toInt()
   val motion=when {
    walking -> ResidentAnimation.State.WALK
    working -> ResidentAnimation.State.WORK
    celebrating -> ResidentAnimation.State.CELEBRATE
    speaking -> ResidentAnimation.State.TALK
    fountain && stage==FountainSequence.Stage.WATER_START -> ResidentAnimation.State.HAPPY
    fountain && stage in listOf(FountainSequence.Stage.LOOK,FountainSequence.Stage.DISCOVER) || visit==1f -> ResidentAnimation.State.LOOK
    else -> ResidentAnimation.State.IDLE
   }
   val frame=ResidentAnimation.frame(motion,tick+id)
   val bob=if(celebrating)-abs(sin(life*100+id))*dh*.009f else if(walking)-abs(sin(life*100+id))*dh*.002f else 0f
   val saved=c.save();val breath=1f+sin(life*18+id)*.006f;c.scale(1f,breath,x,y)
   val faceRight=if(visit>0)if(life<.75f)id==3 else id==2 else x<fx
   resident(c,id,x,y+bob,dw*.16f*(1-visit*.12f),frame,faceRight);c.restoreToCount(saved)
   if(speaking)bubble(c,x,y-dw*.23f,names[id],FountainSequence.dialogue(sceneMs))
  }
  if(fountain && stage in listOf(FountainSequence.Stage.WORK,FountainSequence.Stage.REPAIR)){
   // Barrier ribbons, cones, pump access and tool effects remain anchored to the job.
   val by=fy+dh*.09f
   p.style=Paint.Style.FILL;p.color=0xffe88928.toInt()
   for(side in listOf(-1,1)){val x=fx+side*dw*.19f;val path=Path().apply{moveTo(x,by-dh*.035f);lineTo(x-dw*.018f,by);lineTo(x+dw*.018f,by);close()};c.drawPath(path,p)}
   p.strokeWidth=dw*.009f;p.color=0xffffd553.toInt();c.drawLine(fx-dw*.19f,by-dh*.02f,fx+dw*.19f,by-dh*.02f,p)
   p.color=0xff4b665d.toInt();c.drawRoundRect(RectF(fx+dw*.09f,by-dh*.07f,fx+dw*.15f,by-dh*.025f),4f,4f,p)
   for(i in 0..5){val t=(life*8+i*.17f)%1f;p.color=0xffffd87a.toInt();p.alpha=((1-t)*230).toInt();c.drawCircle(fx+dw*.12f+sin(i*3f)*dw*.04f*t,by-dh*.05f-t*dh*.035f,dw*.003f,p)};p.alpha=255
  }
 }
 private fun drawWater(c:Canvas,x:Float,y:Float,w:Float,amount:Float){
  val saved=c.save();p.shader=null;p.style=Paint.Style.STROKE;p.strokeCap=Paint.Cap.ROUND
  // Continuous parabolic jets with flowing highlights and basin ripples.
  for(i in -3..3){val spread=i*w*.018f;val height=w*(.065f+.006f*sin(life*45+i))*amount
   val jet=Path().apply{moveTo(x,y-w*.055f);quadTo(x+spread*.4f,y-height-w*.055f,x+spread,y+w*.035f)}
   p.color=0xaa53cde9.toInt();p.strokeWidth=w*.005f*amount;c.drawPath(jet,p)
   p.color=0xdde6ffff.toInt();p.strokeWidth=w*.0018f*amount;c.drawPath(jet,p)
   val t=(life*10+i*.13f+1)%1f;val dx=x+spread*t;val dy=(1-t)*(1-t)*(y-w*.055f)+2*(1-t)*t*(y-height-w*.055f)+t*t*(y+w*.035f)
   p.style=Paint.Style.FILL;c.drawCircle(dx,dy,w*.0025f*amount,p);p.style=Paint.Style.STROKE
  }
  for(i in 0..4){val t=(life*4+i*.2f)%1f;p.color=0xffb5f7ff.toInt();p.alpha=((1-t)*170*amount).toInt();p.strokeWidth=w*.0018f;c.drawOval(RectF(x-w*(.025f+.095f*t),y+w*.025f-w*.018f*t,x+w*(.025f+.095f*t),y+w*.042f+w*.018f*t),p)}
  p.alpha=255;p.style=Paint.Style.FILL;p.strokeCap=Paint.Cap.BUTT;c.restoreToCount(saved)
 }

 private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
}
