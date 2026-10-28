package com.lazygarden.game
import android.content.Context
import android.graphics.*

/** Physical inventory uses the same original hardware and gardening assets as the world. */
class MaterialArt(private val context:Context){
 private fun hardware(c:Canvas,p:Paint,source:Rect,r:RectF){
  val b=GameSkin.bitmap(context,R.drawable.fountain_worksite)
  val scale=minOf(r.width()/source.width(),r.height()/source.height())
  val w=source.width()*scale;val h=source.height()*scale
  p.shader=null;p.colorFilter=null;p.alpha=255;p.style=Paint.Style.FILL
  c.drawBitmap(b,source,RectF(r.centerX()-w/2,r.centerY()-h/2,r.centerX()+w/2,r.centerY()+h/2),p)
 }
 fun draw(c:Canvas,p:Paint,m:Material,r:RectF){
  when(m){
   Material.TOOL->SpriteArt.draw(c,p,GameSkin.bitmap(context,R.drawable.fountain_worksite),2,2,2,r)
   Material.PIPE->hardware(c,p,Rect(790,250,1055,497),r)
   Material.VALVE->hardware(c,p,Rect(1290,172,1439,246),r)
   Material.PUMP_PART->hardware(c,p,Rect(1042,39,1156,180),r)
   else->{val i=when(m){Material.FLOWER,Material.SEEDLING->0;Material.SOIL,Material.SAND,Material.FERTILIZER->3;Material.STONE,Material.TILE->5;Material.BOARD,Material.SCREW,Material.PAINT,Material.HINGE,Material.CABLE->9;else->10};SpriteArt.draw(c,p,GameSkin.bitmap(context,R.drawable.garden_work_props),4,3,i,r)}
  }
 }
}
