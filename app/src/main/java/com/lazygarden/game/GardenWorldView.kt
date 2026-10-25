package com.lazygarden.game

import android.content.Context
import android.graphics.*
import android.view.View

class GardenWorldView(c:Context,private val restored:Boolean):View(c){
 private val p=Paint(3)
 override fun onDraw(c:Canvas){super.onDraw(c); val w=width.toFloat(); val h=height.toFloat()
  p.shader=LinearGradient(0f,0f,0f,h,Color.rgb(77,151,181),Color.rgb(44,102,63),Shader.TileMode.CLAMP);c.drawRect(0f,0f,w,h,p);p.shader=null
  // distant estate
  p.color=Color.rgb(232,207,164);c.drawRoundRect(w*.17f,h*.15f,w*.83f,h*.47f,24f,24f,p)
  p.color=Color.rgb(83,47,48);val roof=Path();roof.moveTo(w*.12f,h*.18f);roof.lineTo(w*.5f,h*.06f);roof.lineTo(w*.88f,h*.18f);roof.close();c.drawPath(roof,p)
  p.color=Color.rgb(50,89,110); for(i in 0..3)c.drawRoundRect(w*(.25f+i*.15f),h*.23f,w*(.32f+i*.15f),h*.34f,8f,8f,p)
  p.color=Color.rgb(83,52,35);c.drawRoundRect(w*.45f,h*.32f,w*.55f,h*.47f,8f,8f,p)
  // garden beds and winding path
  p.color=Color.rgb(204,185,137);val path=Path();path.moveTo(w*.38f,h);path.cubicTo(w*.25f,h*.76f,w*.7f,h*.69f,w*.5f,h*.45f);path.lineTo(w*.62f,h*.45f);path.cubicTo(w*.83f,h*.7f,w*.49f,h*.78f,w*.63f,h);path.close();c.drawPath(path,p)
  p.color=Color.rgb(35,92,44);for(i in 0..5){c.drawCircle(w*(.08f+i*.18f),h*(.55f+(i%2)*.08f),w*.075f,p)}
  // fountain state
  p.color=if(restored) Color.rgb(225,221,207) else Color.rgb(112,105,92);c.drawOval(w*.31f,h*.52f,w*.69f,h*.66f,p);c.drawRect(w*.47f,h*.43f,w*.53f,h*.57f,p);c.drawCircle(w*.5f,h*.43f,w*.07f,p)
  if(restored){p.color=Color.rgb(83,196,236);p.strokeWidth=8f;c.drawLine(w*.5f,h*.43f,w*.5f,h*.54f,p);c.drawArc(w*.39f,h*.43f,w*.61f,h*.59f,200f,140f,false,p)}
  else {p.color=Color.rgb(72,65,58);p.strokeWidth=7f;c.drawLine(w*.44f,h*.51f,w*.56f,h*.45f,p)}
  // foreground flowers
  val colors=intArrayOf(0xffffcc3d.toInt(),0xffff6c83.toInt(),0xffb46cff.toInt(),0xffffffff.toInt());for(i in 0..17){p.color=colors[i%colors.size];c.drawCircle(w*(.05f+(i%9)*.112f),h*(.72f+(i/9)*.12f),10f+(i%3)*2,p)}
 }
}
