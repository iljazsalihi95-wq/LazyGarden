package com.lazygarden.game

import android.content.Context
import android.graphics.*
import android.view.View
import kotlin.math.sin

class GardenWorldView(c:Context, private val restored:Boolean):View(c){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG)
 private val stroke=Paint(Paint.ANTI_ALIAS_FLAG).apply{style=Paint.Style.STROKE;strokeCap=Paint.Cap.ROUND}
 private var t=0f
 private fun oval(c:Canvas,l:Float,t:Float,r:Float,b:Float,color:Int){p.color=color;p.shader=null;c.drawOval(l,t,r,b,p)}
 private fun rect(c:Canvas,l:Float,t:Float,r:Float,b:Float,color:Int,rad:Float=0f){p.color=color;p.shader=null;c.drawRoundRect(l,t,r,b,rad,rad,p)}
 override fun onAttachedToWindow(){super.onAttachedToWindow();post(anim)}
 override fun onDetachedFromWindow(){removeCallbacks(anim);super.onDetachedFromWindow()}
 private val anim=object:Runnable{override fun run(){t+=.045f;invalidate();postDelayed(this,32)}}
 override fun onDraw(c:Canvas){
  super.onDraw(c);val w=width.toFloat();val h=height.toFloat();if(w<=0||h<=0)return
  // cinematic sky and distant hills
  p.shader=LinearGradient(0f,0f,0f,h*.72f,intArrayOf(0xff65c9ef.toInt(),0xffa8e0d1.toInt(),0xff5eaa63.toInt()),null,Shader.TileMode.CLAMP);c.drawRect(0f,0f,w,h,p);p.shader=null
  p.color=0x33ffffff;c.drawCircle(w*.18f,h*.10f,w*.11f,p)
  p.color=0xff4e9855.toInt();val hill=Path();hill.moveTo(0f,h*.38f);hill.cubicTo(w*.18f,h*.22f,w*.30f,h*.39f,w*.47f,h*.29f);hill.cubicTo(w*.66f,h*.17f,w*.78f,h*.36f,w,h*.24f);hill.lineTo(w,h*.55f);hill.lineTo(0f,h*.55f);hill.close();c.drawPath(hill,p)
  // layered manor shadow + body
  rect(c,w*.145f,h*.135f,w*.855f,h*.47f,0x33000000,28f)
  rect(c,w*.17f,h*.12f,w*.83f,h*.455f,0xffefd7aa.toInt(),22f)
  // manor wings and center tower
  rect(c,w*.20f,h*.19f,w*.38f,h*.455f,0xffe5c48e.toInt(),12f);rect(c,w*.62f,h*.19f,w*.80f,h*.455f,0xffe5c48e.toInt(),12f)
  rect(c,w*.39f,h*.10f,w*.61f,h*.455f,0xfff4ddb0.toInt(),10f)
  p.color=0xff743d42.toInt();val roof=Path();roof.moveTo(w*.12f,h*.19f);roof.lineTo(w*.31f,h*.095f);roof.lineTo(w*.43f,h*.19f);roof.close();c.drawPath(roof,p);val roof2=Path();roof2.moveTo(w*.57f,h*.19f);roof2.lineTo(w*.69f,h*.095f);roof2.lineTo(w*.88f,h*.19f);roof2.close();c.drawPath(roof2,p);val tower=Path();tower.moveTo(w*.36f,h*.115f);tower.lineTo(w*.50f,h*.025f);tower.lineTo(w*.64f,h*.115f);tower.close();c.drawPath(tower,p)
  // windows with highlights
  for(row in 0..1)for(col in 0..4){val x=w*(.235f+col*.132f);val y=h*(.225f+row*.105f);rect(c,x,y,x+w*.065f,y+h*.072f,0xff285c78.toInt(),7f);rect(c,x+w*.009f,y+h*.008f,x+w*.026f,y+h*.030f,0x99dff7ff.toInt(),3f)}
  // grand door + stairs
  rect(c,w*.455f,h*.315f,w*.545f,h*.455f,0xff6b4330.toInt(),10f);rect(c,w*.474f,h*.335f,w*.526f,h*.455f,0xff3d6d73.toInt(),6f)
  for(i in 0..2)rect(c,w*(.425f-i*.018f),h*(.455f+i*.014f),w*(.575f+i*.018f),h*(.47f+i*.014f),0xffc8b080.toInt(),3f)
  // lawn stripes and winding stone path
  rect(c,0f,h*.45f,w,h,0xff3f9a4e.toInt())
  for(i in 0..8){p.color=if(i%2==0)0x1040ff40 else 0x10206020;c.drawRect(i*w/9f,h*.45f,(i+1)*w/9f,h,p)}
  p.color=0xffd9c28e.toInt();val path=Path();path.moveTo(w*.34f,h);path.cubicTo(w*.28f,h*.80f,w*.72f,h*.73f,w*.47f,h*.47f);path.lineTo(w*.57f,h*.47f);path.cubicTo(w*.82f,h*.74f,w*.54f,h*.82f,w*.66f,h);path.close();c.drawPath(path,p)
  // trees with trunks, shadows and layered crowns
  for(i in 0..5){val x=w*(.06f+i*.18f);val y=h*(.50f+(i%2)*.055f);oval(c,x-w*.07f,y+h*.08f,x+w*.07f,y+h*.12f,0x33000000);rect(c,x-w*.012f,y,x+w*.012f,y+h*.105f,0xff76502f.toInt(),5f);oval(c,x-w*.072f,y-h*.06f,x+w*.072f,y+h*.055f,0xff216f39.toInt());oval(c,x-w*.050f,y-h*.085f,x+w*.045f,y+h*.02f,0xff49a84e.toInt());oval(c,x-w*.028f,y-h*.07f,x+w*.015f,y-h*.025f,0x66c9f38b)}
  // fountain plaza shadow
  oval(c,w*.27f,h*.555f,w*.73f,h*.69f,0x33000000)
  oval(c,w*.29f,h*.525f,w*.71f,h*.665f,if(restored)0xffeee8d9.toInt() else 0xff81796d.toInt())
  oval(c,w*.325f,h*.548f,w*.675f,h*.638f,if(restored)0xff51b9dc.toInt() else 0xff514d47.toInt())
  rect(c,w*.475f,h*.425f,w*.525f,h*.57f,if(restored)0xffe6dfcd.toInt() else 0xff746d63.toInt(),8f)
  oval(c,w*.445f,h*.405f,w*.555f,h*.475f,if(restored)0xfff5eee0.toInt() else 0xff81796d.toInt())
  if(restored){
   val wave=(sin(t.toDouble())*7).toFloat();stroke.color=0xffc8f4ff.toInt();stroke.strokeWidth=w*.009f;c.drawLine(w*.50f,h*.42f,w*.50f,h*(.525f)+wave,stroke);c.drawArc(w*.40f,h*.43f,w*.60f,h*.58f,195f,150f,false,stroke)
   p.color=0x66ffffff;c.drawCircle(w*.43f,h*.58f,w*.012f,p);c.drawCircle(w*.59f,h*.60f,w*.009f,p)
  }else{
   stroke.color=0xff453f39.toInt();stroke.strokeWidth=w*.009f;c.drawLine(w*.445f,h*.54f,w*.555f,h*.46f,stroke);c.drawLine(w*.50f,h*.43f,w*.535f,h*.47f,stroke)
   // weeds around broken fountain
   stroke.color=0xff255f2f.toInt();stroke.strokeWidth=w*.006f;for(i in 0..6){val x=w*(.31f+i*.063f);c.drawLine(x,h*.64f,x+w*.018f,h*.60f,stroke)}
  }
  // premium flower beds
  val flowers=intArrayOf(0xffffcf45.toInt(),0xffff6684.toInt(),0xffc071ff.toInt(),0xfffff1d0.toInt(),0xff5ed5ff.toInt())
  for(side in 0..1)for(i in 0..13){val x=if(side==0)w*(.045f+(i%7)*.038f) else w*(.955f-(i%7)*.038f);val y=h*(.70f+(i/7)*.105f+(i%3)*.012f);p.color=0xff236d35.toInt();p.strokeWidth=4f;c.drawLine(x,y,x,y+h*.035f,p);p.color=flowers[(i+side)%flowers.size];c.drawCircle(x,y,w*.013f,p);p.color=0xffffe38a.toInt();c.drawCircle(x,y,w*.004f,p)}
  // task marker belongs to the world, not a plain prototype button
  if(!restored){p.color=0xffffc928.toInt();c.drawCircle(w*.67f,h*.52f,w*.035f,p);p.color=Color.WHITE;p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.DEFAULT_BOLD;p.textSize=w*.045f;c.drawText("!",w*.67f,h*.537f,p)}
 }
}
