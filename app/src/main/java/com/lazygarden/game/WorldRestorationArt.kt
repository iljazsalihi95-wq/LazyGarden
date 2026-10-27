package com.lazygarden.game

import android.content.Context
import android.graphics.*

class PlantArt(context:Context){
 private val b=GameSkin.bitmap(context,R.drawable.garden_work_props)
 fun draw(c:Canvas,p:Paint,id:String,x:Float,y:Float,w:Float){val i=when(id){"lavender"->1;"daisy"->2;else->0};SpriteArt.draw(c,p,b,4,3,i,RectF(x-w/2,y-w*.66f,x+w/2,y))}
}
object SpriteArt {
 fun draw(c:Canvas,p:Paint,b:Bitmap,cols:Int,rows:Int,index:Int,r:RectF,alpha:Int=255){if(alpha<=0)return;p.shader=null;p.colorFilter=null;p.style=Paint.Style.FILL;p.alpha=alpha.coerceIn(0,255);val w=b.width/cols;val h=b.height/rows;c.drawBitmap(b,Rect(index%cols*w,index/cols*h,(index%cols+1)*w,(index/cols+1)*h),r,p);p.alpha=255}
}
/** Same durable artwork used by the cinematic and estate view. */
object WorldRestorationArt {
 fun draw(ctx:Context,c:Canvas,d:RectF,tasks:Int,life:Float){
  val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG);val save=GardenSave(ctx);val art=GameSkin.bitmap(ctx,R.drawable.restoration_objects)
  if(tasks>=2){val flowers=PlantArt(ctx);for(i in 0..9){val x=d.left+d.width()*(if(i<5).19f+i*.012f else .78f+(i-5)*.012f);val y=d.top+d.height()*(.67f+(i%5)*.034f);flowers.draw(c,p,save.flowerChoice?:"tulip",x,y,d.width()*.075f)}}
  if(tasks>=3)path(ctx,c,p,d,1f,1f)
  if(tasks>=4)SpriteArt.draw(c,p,art,3,4,2,rect(d,.21f,.64f,.28f,.17f))
  if(tasks>=5)for(i in 0..2)SpriteArt.draw(c,p,art,3,4,5,rect(d,.13f+i*.34f,.47f+i*.025f,.23f,.19f))
  if(tasks>=6)SpriteArt.draw(c,p,art,3,4,8,rect(d,.48f,.40f,.32f,.25f))
  if(tasks>=7)SpriteArt.draw(c,p,art,3,4,11,rect(d,.69f,.29f,.58f,.31f))
 }
 fun rect(d:RectF,x:Float,y:Float,w:Float,h:Float)=RectF(d.left+(x-w/2)*d.width(),d.top+(y-h)*d.height(),d.left+(x+w/2)*d.width(),d.top+y*d.height())
 fun path(ctx:Context,c:Canvas,p:Paint,d:RectF,removed:Float,laid:Float){val b=GameSkin.bitmap(ctx,R.drawable.garden_work_props)
  for(i in 0..17){val row=i/3;val t=row/5f;val w=d.width()*(.052f+.032f*t);val x=d.centerX()+(i%3-1)*w*1.05f;val y=d.top+d.height()*(.67f+row*.038f);val r=RectF(x-w/2,y-w*.4f,x+w/2,y+w*.4f)
   val revealed=(laid*18-i).coerceIn(0f,1f);if(revealed>0){val saved=c.save();c.translate(0f,-d.height()*.025f*(1-revealed));SpriteArt.draw(c,p,b,4,3,5,r,(255*revealed).toInt());c.restoreToCount(saved)}else SpriteArt.draw(c,p,b,4,3,4,r,(255*(1-removed)).toInt())
  }
 }
}
