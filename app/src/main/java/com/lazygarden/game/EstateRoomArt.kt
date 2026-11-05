package com.lazygarden.game

import android.content.Context
import android.graphics.*
import kotlin.math.sin

/** Original HD room atlas, restored in place by the real construction timeline. */
object EstateRoomArt {
 fun bounds(d:RectF)=WorldRestorationArt.rect(d,.5f,.74f,.86f,.49f)
 fun draw(context:Context,c:Canvas,p:Paint,r:RectF,task:Int,repair:Float,finish:Float,time:Long){
  require(task in 7..19)
  val atlas=GameSkin.bitmap(context,R.drawable.estate_room_atlas)
  val index=task-7;val cw=atlas.width/4;val ch=atlas.height/4
  val src=Rect(index%4*cw,index/4*ch,(index%4+1)*cw,(index/4+1)*ch)
  val fixed=repair.coerceIn(0f,1f);val done=finish.coerceIn(0f,1f)
  p.style=Paint.Style.FILL;p.shader=null;p.alpha=255;p.colorFilter=null
  p.color=0x77372717;c.drawRoundRect(RectF(r.left+6,r.top+8,r.right+8,r.bottom+12),12f,12f,p)
  // The damaged room is the same place, with weathering, cracks and debris. The repaired
  // area advances at the current work step; the final cleanup removes the remaining wear.
  val gray=ColorMatrix().apply{setSaturation(.18f)}
  gray.postConcat(ColorMatrix(floatArrayOf(.65f,0f,0f,0f,8f,0f,.62f,0f,0f,5f,0f,0f,.58f,0f,0f,0f,0f,0f,1f,0f)))
  p.colorFilter=ColorMatrixColorFilter(gray);p.alpha=255;c.drawBitmap(atlas,src,r,p);p.colorFilter=null
  val restored=maxOf(done,fixed*.8f)
  if(restored>0){val layer=c.save();c.clipRect(r.left,r.top,r.left+r.width()*restored,r.bottom);c.drawBitmap(atlas,src,r,p);c.restoreToCount(layer)}
  if(done>0){p.alpha=(done*255).toInt();c.drawBitmap(atlas,src,r,p);p.alpha=255}
  val worn=1-maxOf(done,fixed)
  if(worn>0){
   p.style=Paint.Style.STROKE;p.strokeWidth=r.width()*.004f;p.color=Color.argb((worn*220).toInt(),45,34,25)
   for(i in 0..4){val x=r.left+r.width()*(.12f+i*.18f);val y=r.top+r.height()*.12f
    c.drawPath(Path().apply{moveTo(x,y);lineTo(x+r.width()*.02f,y+r.height()*.11f);lineTo(x-r.width()*.006f,y+r.height()*.17f);lineTo(x+r.width()*.03f,y+r.height()*.28f)},p)}
   p.style=Paint.Style.FILL
  }
  if(done<1){
   val props=GameSkin.bitmap(context,R.drawable.garden_work_props)
   for(i in 0..3){val x=r.left+r.width()*(.13f+i*.23f);val y=r.bottom-r.height()*.04f
    SpriteArt.draw(c,p,props,4,3,4,RectF(x-r.width()*.045f,y-r.height()*.12f,x+r.width()*.045f,y),(190*(1-done)).toInt())}
  }
  if(fixed>0&&done<1){
   val props=GameSkin.bitmap(context,R.drawable.garden_work_props)
   SpriteArt.draw(c,p,props,4,3,9,RectF(r.left+r.width()*.02f,r.bottom-r.height()*.21f,r.left+r.width()*.18f,r.bottom))
  }
  if(done>0){p.color=Color.argb((done*150).toInt(),255,238,169);c.drawCircle(r.right-r.width()*.08f,r.top+r.height()*.09f,r.width()*(.003f+.001f*sin(time/900f)),p)}
  p.shader=null;p.colorFilter=null;p.alpha=255;p.style=Paint.Style.FILL
 }
}
