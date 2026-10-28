package com.lazygarden.game
import android.graphics.*
import kotlin.math.*

/** Reactions complement the atlas poses without moving actors off their ground anchor. */
object ActorGesture {
 enum class Mood { SURPRISE,JOY,SAD,THINK,ANALYSE,WORK }
 fun lean(mood:Mood,elapsed:Long):Float=when(mood){
  Mood.WORK->sin(elapsed/150f)*3.5f
  Mood.SAD->-5f
  Mood.ANALYSE->sin(elapsed/650f)*2f
  Mood.SURPRISE->sin((elapsed/500f).coerceAtMost(1f)*PI.toFloat())*-7f
  else->0f
 }
 fun draw(c:Canvas,p:Paint,mood:Mood,x:Float,y:Float,size:Float,elapsed:Long){
  p.shader=null;p.colorFilter=null;p.style=Paint.Style.FILL;p.alpha=255
  val beat=sin(elapsed/230f);val r=size*.13f
  when(mood){
   Mood.SURPRISE->{p.color=0xffffe39a.toInt();c.drawRoundRect(RectF(x-r*.22f,y-r*1.8f,x+r*.22f,y-r*.3f),r*.2f,r*.2f,p);c.drawCircle(x,y+r*.3f,r*.24f,p)}
   Mood.THINK,Mood.ANALYSE->{for(i in 0..2){p.color=0xffecf9fa.toInt();p.alpha=(140+100*sin(elapsed/260f-i)).toInt().coerceIn(30,255);c.drawCircle(x+(i-1)*r*.9f,y,r*(.19f+.05f*sin(elapsed/260f-i)),p)};p.alpha=255
    if(mood==Mood.ANALYSE){p.style=Paint.Style.STROKE;p.strokeWidth=size*.015f;p.color=0xffe5c26d.toInt();c.drawCircle(x,y-r*1.7f,r*.7f,p);c.drawLine(x+r*.5f,y-r*1.15f,x+r*1.1f,y-r*.55f,p);p.style=Paint.Style.FILL}}
   Mood.JOY->{for(i in 0..4){val a=(i*72+elapsed/16f)*PI.toFloat()/180;val rr=r*(1.5f+.2f*beat);p.color=if(i%2==0)0xffffdf78.toInt() else 0xffa4f4d2.toInt();c.drawCircle(x+cos(a)*rr,y+sin(a)*rr,r*.17f,p)}}
   Mood.SAD->{p.style=Paint.Style.STROKE;p.strokeWidth=size*.025f;p.color=0xffb5d6e4.toInt();c.drawArc(RectF(x-r,y-r*.1f,x+r,y+r),200f,140f,false,p);p.style=Paint.Style.FILL}
   Mood.WORK->{p.color=0xffffdf99.toInt();p.alpha=((beat+1)*100).toInt();for(i in 0..2)c.drawCircle(x+(i-1)*r,y-r*i*.4f,r*.12f,p)}
  }
  p.alpha=255;p.style=Paint.Style.FILL
 }
}
