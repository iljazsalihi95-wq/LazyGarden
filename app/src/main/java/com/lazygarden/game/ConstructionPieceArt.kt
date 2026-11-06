package com.lazygarden.game

import android.content.Context
import android.graphics.*
import kotlin.math.*

/** Six readable construction tokens. The inventory object is the main artwork, never a fruit badge. */
class ConstructionPieceArt(context:Context){
 private val materials=MaterialArt(context)
 private val colors=intArrayOf(0xffe7ad4f.toInt(),0xff5fc7b0.toInt(),0xffaa88e3.toInt(),0xff60bfea.toInt(),0xffee8c82.toInt(),0xffa5cb61.toInt())
 fun draw(c:Canvas,p:Paint,index:Int,material:Material,r:RectF,water:Boolean=false){
  val i=index.coerceIn(0,5);val radius=r.width()*.13f
  p.shader=null;p.colorFilter=null;p.alpha=255;p.style=Paint.Style.FILL
  p.color=0x55332312;c.drawRoundRect(RectF(r.left,r.top+r.height()*.06f,r.right,r.bottom),radius,radius,p)
  val face=RectF(r.left,r.top,r.right,r.bottom-r.height()*.06f)
  p.alpha=255
  p.shader=LinearGradient(face.left,face.top,face.right,face.bottom,intArrayOf(Color.WHITE,colors[i],colors[i]),floatArrayOf(0f,.28f,1f),Shader.TileMode.CLAMP)
  val shape=Path()
  if(i%3==0)shape.addRoundRect(face,radius*1.8f,radius*1.8f,Path.Direction.CW)
  else if(i%3==1){for(k in 0..5){val a=(k*60-30)*PI/180;val x=face.centerX()+cos(a).toFloat()*face.width()*.52f;val y=face.centerY()+sin(a).toFloat()*face.height()*.52f;if(k==0)shape.moveTo(x,y)else shape.lineTo(x,y)};shape.close()}
  else{shape.moveTo(face.centerX(),face.top);shape.lineTo(face.right,face.centerY());shape.lineTo(face.centerX(),face.bottom);shape.lineTo(face.left,face.centerY());shape.close()}
  c.drawPath(shape,p);p.shader=null
  p.style=Paint.Style.STROKE;p.color=0xccfff5d1.toInt();p.strokeWidth=r.width()*.025f;c.drawPath(shape,p);p.style=Paint.Style.FILL
  val art=RectF(face);art.inset(r.width()*.16f,r.height()*.13f)
  materials.draw(c,p,material,art)
  if(water){
   val x=r.right-r.width()*.17f;val y=r.bottom-r.height()*.20f;val size=r.width()*.15f
   val drop=Path().apply{moveTo(x,y-size);cubicTo(x-size,y,x-size,y+size,x,y+size);cubicTo(x+size,y+size,x+size,y,x,y-size);close()}
   p.color=0xff287fc4.toInt();c.drawPath(drop,p);p.color=Color.WHITE;c.drawCircle(x-size*.25f,y+size*.2f,size*.2f,p)
  }
  p.alpha=255;p.shader=null
 }
}
