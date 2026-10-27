package com.lazygarden.game

import android.content.Context
import android.graphics.*

class PlantArt(context:Context){
 private val b=GameSkin.bitmap(context,R.drawable.garden_work_props)
 fun draw(c:Canvas,p:Paint,id:String,x:Float,y:Float,w:Float){val i=when(id){"lavender"->1;"daisy"->2;else->0};SpriteArt.draw(c,p,b,4,3,i,RectF(x-w/2,y-w,x+w/2,y))}
}
object SpriteArt {
 fun draw(c:Canvas,p:Paint,b:Bitmap,cols:Int,rows:Int,index:Int,r:RectF,alpha:Int=255){if(alpha<=0)return;p.shader=null;p.colorFilter=null;p.style=Paint.Style.FILL;p.alpha=alpha.coerceIn(0,255);val w=b.width/cols;val h=b.height/rows;c.drawBitmap(b,Rect(index%cols*w,index/cols*h,(index%cols+1)*w,(index/cols+1)*h),r,p);p.alpha=255}
}
/** Same durable artwork used by the cinematic and estate view. */
object WorldRestorationArt {
 fun draw(ctx:Context,c:Canvas,d:RectF,tasks:Int,life:Float){
  val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG);val save=GardenSave(ctx);val art=GameSkin.bitmap(ctx,R.drawable.restoration_objects)
  if(tasks>=2){val flowers=PlantArt(ctx);for(i in 0..9){val x=d.left+d.width()*(if(i<5).19f+i*.012f else .78f+(i-5)*.012f);val y=d.top+d.height()*(.67f+(i%5)*.034f);flowers.draw(c,p,save.flowerChoice?:"tulip",x,y,d.width()*.075f)}}
  // The estate after-layer already contains the finished paving, bench, trees and facade.
  // Do not stack duplicate buildings or miniature paving decals over that illustration.
  if(tasks>=5)RestorationObjectArt.draw(c,p,art,5,objectRect(d,4))
  if(tasks>=6)RestorationObjectArt.draw(c,p,art,8,objectRect(d,5))
 }
 fun objectRect(d:RectF,task:Int)=when(task){3->rect(d,.21f,.64f,.28f,.17f);4->rect(d,.19f,.75f,.27f,.16f);5->rect(d,.50f,.82f,.60f,.30f);6->rect(d,.69f,.29f,.58f,.31f);else->rect(d,.50f,.86f,.40f,.20f)}
 fun water(c:Canvas,p:Paint,d:RectF,ms:Float){val x=d.left+d.width()*.55f;val y=d.top+d.height()*.515f;val w=d.width();p.style=Paint.Style.STROKE;p.strokeWidth=w*.0025f;p.strokeCap=Paint.Cap.ROUND
  for(i in -2..2){val spread=w*i*.021f;val h=w*(.07f+.004f*kotlin.math.sin(ms/220+i));val path=Path().apply{moveTo(x,y-w*.055f);quadTo(x+spread*.4f,y-h,x+spread,y+w*.035f)};p.color=0xbbbcf5ff.toInt();c.drawPath(path,p);val t=(ms/1100+i*.17f+10)%1f;val xx=x+spread*t;val yy=(1-t)*(1-t)*(y-w*.055f)+2*(1-t)*t*(y-h)+t*t*(y+w*.035f);p.style=Paint.Style.FILL;c.drawCircle(xx,yy,w*.002f,p);p.style=Paint.Style.STROKE};p.style=Paint.Style.FILL;p.strokeCap=Paint.Cap.BUTT
 }
 fun rect(d:RectF,x:Float,y:Float,w:Float,h:Float)=RectF(d.left+(x-w/2)*d.width(),d.top+(y-h)*d.height(),d.left+(x+w/2)*d.width(),d.top+y*d.height())
 fun path(ctx:Context,c:Canvas,p:Paint,d:RectF,removed:Float,laid:Float){
  val b=GameSkin.bitmap(ctx,R.drawable.garden_work_props);val after=GameSkin.bitmap(ctx,R.drawable.garden_estate)
  val top=d.top+d.height()*.63f;val bottom=d.top+d.height()*1.04f
  val outline=Path().apply{moveTo(d.left+d.width()*.36f,top);lineTo(d.left+d.width()*.65f,top);lineTo(d.left+d.width()*.97f,bottom);lineTo(d.left+d.width()*.03f,bottom);close()}
  val clip=c.save();c.clipPath(outline)
  if(removed>0){val soil=c.save();c.clipRect(d.left,top,d.right,top+(bottom-top)*removed);SpriteArt.draw(c,p,b,4,3,3,RectF(d.left,top,d.right,bottom));c.restoreToCount(soil)}
  if(laid>0){val clean=c.save();c.clipRect(d.left,top,d.right,top+(bottom-top)*laid);c.drawBitmap(after,null,d,p);c.restoreToCount(clean)}
  c.restoreToCount(clip)
  // Only the currently landing stone is overlaid. Settled stones blend into the restored paving illustration.
  if(laid>0&&laid<1){val i=(laid*24).toInt().coerceIn(0,23);val t=(i/3)/7f;val w=d.width()*(.06f+.095f*t);val x=d.centerX()+(i%3-1)*w;val y=top+(bottom-top)*t;val land=(laid*24-i).coerceIn(0f,1f);val save=c.save();c.translate(0f,-d.height()*.025f*(1-land));SpriteArt.draw(c,p,b,4,3,5,RectF(x-w/2,y-w*.27f,x+w/2,y+w*.27f),(255*land).toInt());c.restoreToCount(save)}

 }
}

object RestorationObjectArt {
 // The generated atlas has deliberately uneven vertical gutters; explicit cuts keep roofs/leaves out of adjacent objects.
 fun draw(c:Canvas,p:Paint,b:Bitmap,index:Int,r:RectF,alpha:Int=255){if(alpha<=0)return;val edges=floatArrayOf(0f,.245f,.470f,.715f,1f);val row=index/3;val col=index%3;p.shader=null;p.colorFilter=null;p.style=Paint.Style.FILL;p.alpha=alpha.coerceIn(0,255);c.drawBitmap(b,Rect((col*b.width/3f).toInt(),(edges[row]*b.height).toInt(),((col+1)*b.width/3f).toInt(),(edges[row+1]*b.height).toInt()),r,p);p.alpha=255}
}
