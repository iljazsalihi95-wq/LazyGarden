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
  p.shader=null;p.colorFilter=null;p.alpha=255;p.style=Paint.Style.FILL
  when(m){
   Material.PAINT,Material.PLASTER->{
    val body=RectF(r.left+r.width()*.16f,r.top+r.height()*.24f,r.right-r.width()*.16f,r.bottom-r.height()*.10f)
    p.shader=LinearGradient(body.left,0f,body.right,0f,intArrayOf(0xff718e96.toInt(),0xffecf5ee.toInt(),0xff6c9099.toInt()),null,Shader.TileMode.CLAMP);c.drawRoundRect(body,r.width()*.06f,r.width()*.06f,p);p.shader=null
    p.color=if(m==Material.PAINT)0xffe9aa4e.toInt() else 0xffd4c2a3.toInt();c.drawRect(body.left,body.top+body.height()*.28f,body.right,body.bottom-body.height()*.20f,p)
    p.style=Paint.Style.STROKE;p.strokeWidth=r.width()*.04f;p.color=0xff66747b.toInt();c.drawArc(RectF(body.left,body.top-r.height()*.14f,body.right,body.top+r.height()*.30f),180f,180f,false,p);p.style=Paint.Style.FILL
   }
   Material.CABLE->{
    p.style=Paint.Style.STROKE;p.strokeWidth=r.width()*.065f;p.color=0xff3b545b.toInt()
    for(i in 0..2){val inset=r.width()*(.14f+i*.09f);c.drawOval(RectF(r.left+inset,r.top+inset,r.right-inset,r.bottom-inset),p)}
    p.color=0xffdc9957.toInt();c.drawLine(r.centerX(),r.bottom-r.height()*.18f,r.right-r.width()*.05f,r.bottom-r.height()*.03f,p);p.style=Paint.Style.FILL
   }
   Material.HINGE->{
    p.shader=LinearGradient(r.left,r.top,r.right,r.bottom,intArrayOf(0xfff5d57d.toInt(),0xff9c6d35.toInt()),null,Shader.TileMode.CLAMP)
    c.drawRoundRect(RectF(r.left+r.width()*.10f,r.top+r.height()*.18f,r.right-r.width()*.10f,r.bottom-r.height()*.18f),r.width()*.05f,r.width()*.05f,p);p.shader=null
    p.color=0xff6f533c.toInt();p.strokeWidth=r.width()*.05f;c.drawLine(r.centerX(),r.top+r.height()*.16f,r.centerX(),r.bottom-r.height()*.16f,p)
    for(x in listOf(.25f,.75f))for(y in listOf(.33f,.67f))c.drawCircle(r.left+r.width()*x,r.top+r.height()*y,r.width()*.045f,p)
   }
   Material.GLASS->{
    p.color=0xff577989.toInt();c.drawRoundRect(r,r.width()*.05f,r.width()*.05f,p)
    val glass=RectF(r);glass.inset(r.width()*.09f,r.height()*.09f)
    p.shader=LinearGradient(glass.left,glass.top,glass.right,glass.bottom,intArrayOf(0xffd9faff.toInt(),0xff6fb1ca.toInt()),null,Shader.TileMode.CLAMP);c.drawRect(glass,p);p.shader=null
    p.color=0xffefffff.toInt();p.strokeWidth=r.width()*.04f;c.drawLine(glass.left+r.width()*.1f,glass.bottom-r.height()*.12f,glass.right-r.width()*.08f,glass.top+r.height()*.12f,p)
   }
   Material.LAMP->{
    p.color=0xffffd67c.toInt();c.drawOval(RectF(r.left+r.width()*.22f,r.top+r.height()*.08f,r.right-r.width()*.22f,r.top+r.height()*.68f),p)
    p.color=0xff667b80.toInt();c.drawRoundRect(RectF(r.left+r.width()*.32f,r.top+r.height()*.64f,r.right-r.width()*.32f,r.bottom-r.height()*.10f),r.width()*.04f,r.width()*.04f,p)
   }
   Material.TILE->{
    p.shader=LinearGradient(r.left,r.top,r.right,r.bottom,intArrayOf(0xfff1a27c.toInt(),0xff9d4b36.toInt()),null,Shader.TileMode.CLAMP)
    val path=Path().apply{moveTo(r.left+r.width()*.12f,r.bottom-r.height()*.12f);lineTo(r.left+r.width()*.32f,r.top+r.height()*.12f);lineTo(r.right-r.width()*.08f,r.top+r.height()*.12f);lineTo(r.right-r.width()*.25f,r.bottom-r.height()*.12f);close()};c.drawPath(path,p);p.shader=null
    p.color=0xfff5be8e.toInt();p.strokeWidth=r.width()*.04f;c.drawLine(r.left+r.width()*.45f,r.bottom-r.height()*.14f,r.left+r.width()*.61f,r.top+r.height()*.14f,p)
   }
   Material.SCREW->{
    val layer=c.save();c.rotate(-32f,r.centerX(),r.centerY())
    val shaft=RectF(r.centerX()-r.width()*.09f,r.top+r.height()*.23f,r.centerX()+r.width()*.09f,r.bottom-r.height()*.08f)
    p.shader=LinearGradient(shaft.left,0f,shaft.right,0f,intArrayOf(0xff64717c.toInt(),0xffedf7ff.toInt(),0xff667580.toInt()),null,Shader.TileMode.CLAMP)
    c.drawRoundRect(shaft,r.width()*.04f,r.width()*.04f,p);p.shader=null
    p.color=0xff82939e.toInt();p.strokeWidth=r.width()*.035f
    for(i in 0..5){val y=shaft.top+shaft.height()*(i+1)/7;c.drawLine(shaft.left,y+r.height()*.025f,shaft.right,y-r.height()*.025f,p)}
    p.color=0xffd4e1e7.toInt();c.drawOval(RectF(r.centerX()-r.width()*.25f,r.top+r.height()*.08f,r.centerX()+r.width()*.25f,r.top+r.height()*.30f),p)
    p.color=0xff55636c.toInt();c.drawLine(r.centerX()-r.width()*.15f,r.top+r.height()*.19f,r.centerX()+r.width()*.15f,r.top+r.height()*.19f,p)
    c.restoreToCount(layer)
   }
   Material.TOOL->SpriteArt.draw(c,p,GameSkin.bitmap(context,R.drawable.fountain_worksite),2,2,2,r)
   Material.PIPE->hardware(c,p,Rect(790,250,1055,497),r)
   Material.VALVE->hardware(c,p,Rect(1290,172,1439,246),r)
   Material.PUMP_PART->hardware(c,p,Rect(1042,39,1156,180),r)
   else->{val i=when(m){Material.FLOWER,Material.SEEDLING->0;Material.SOIL,Material.SAND,Material.FERTILIZER->3;Material.STONE,Material.TILE->5;Material.BOARD,Material.SCREW,Material.PAINT,Material.HINGE,Material.CABLE->9;else->10};SpriteArt.draw(c,p,GameSkin.bitmap(context,R.drawable.garden_work_props),4,3,i,r)}
  }
 }
}
