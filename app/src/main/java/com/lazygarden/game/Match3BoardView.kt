package com.lazygarden.game

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.min

class Match3BoardView(c:Context,val engine:Match3Engine,private val changed:()->Unit,private val done:(Boolean)->Unit):View(c){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG)
 private val colors=intArrayOf(0xffff5b6e.toInt(),0xffffd34e.toInt(),0xff62d16f.toInt(),0xff56b9ff.toInt(),0xffb76cff.toInt(),0xffff8b3d.toInt())
 private var selected=-1
 private var down=-1
 private var busy=false
 private var wave:Match3Engine.Wave?=null
 private var effect=0f
 private var visual=Array(8){engine.board[it].clone()}
 private var cell=0f;private var left=0f;private var top=0f
 private val runner=object:Runnable{override fun run(){effect+=.08f;if(effect<1f){invalidate();postDelayed(this,16)}}}
 override fun onDetachedFromWindow(){removeCallbacks(runner);super.onDetachedFromWindow()}
 override fun onDraw(c:Canvas){
  super.onDraw(c);c.drawColor(0xff173f48.toInt())
  val density=resources.displayMetrics.density
  cell=min(width/8.5f,(height-190*density)/8f).coerceAtLeast(1f);left=(width-cell*8)/2;top=130*density
  p.color=Color.WHITE;p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.DEFAULT_BOLD;p.textSize=25*density
  c.drawText("LAZY GARDEN · ${engine.level}",width/2f,40*density,p)
  p.textSize=15*density;p.typeface=Typeface.DEFAULT
  c.drawText("Moves ${engine.moves}   •   Score ${engine.score}/${engine.spec.target}",width/2f,70*density,p)
  c.drawText("Frost ${engine.clearedIce}/${engine.spec.ice}   •   ${arrayOf("Rose","Sun","Leaf","Water","Iris","Orange")[engine.spec.collectColor]} ${engine.collected[engine.spec.collectColor]}/${engine.spec.collectCount}",width/2f,95*density,p)
  for(r in 0..7)for(col in 0..7){
   val x=left+col*cell;val y=top+r*cell;val cx=x+cell/2;val cy=y+cell/2
   p.color=if(selected==r*8+col)0xfff5d987.toInt() else if(engine.ice[r][col]>0)0xff9de1ed.toInt() else 0xff27616a.toInt()
   c.drawRoundRect(x+2,y+2,x+cell-2,y+cell-2,cell*.15f,cell*.15f,p)
   val value=visual[r][col];val hit=wave?.cells?.contains(r*8+col)==true && effect<1
   val radius=cell*.32f*(if(hit)1-effect else 1f)
   p.color=0x44000000;c.drawCircle(cx+2,cy+4,radius,p)
   p.color=colors[value%6];c.drawCircle(cx,cy,radius,p)
   p.color=0x88ffffff.toInt();c.drawCircle(cx-cell*.1f,cy-cell*.12f,radius*.25f,p)
   if(value>=6){p.color=Color.WHITE;p.textSize=cell*.4f;c.drawText(if(value/6==1)"↔" else if(value/6==2)"↕" else "✦",cx,cy+cell*.14f,p)}
   if(hit){p.color=colors[value%6];for(i in 0..5){val a=i*Math.PI/3; c.drawCircle(cx+(kotlin.math.cos(a)*cell*effect).toFloat(),cy+(kotlin.math.sin(a)*cell*effect).toFloat(),cell*.05f*(1-effect),p)}}
  }
  p.color=0xffffd475.toInt();p.textSize=18*density
  if(busy && engine.combo>1)c.drawText("CASCADE × ${wave?.combo?:1}",width/2f,top+cell*8+35*density,p)
 }
 private fun index(x:Float,y:Float):Int {if(x<left||x>=left+8*cell||y<top||y>=top+8*cell)return -1;return ((y-top)/cell).toInt()*8+((x-left)/cell).toInt()}
 override fun onTouchEvent(e:MotionEvent):Boolean {
  if(busy)return true
  if(e.action==MotionEvent.ACTION_DOWN){down=index(e.x,e.y);return true}
  if(e.action==MotionEvent.ACTION_UP){
   performClick();val end=index(e.x,e.y);if(end<0)return true
   val start=if(down>=0 && down!=end)down else selected
   if(start>=0 && engine.legal(start,end)){
    val waves=engine.swap(start,end);selected=-1;changed();busy=true;animate(waves,0)
   }else{selected=end;invalidate()}
   return true
  };return true
 }
 override fun performClick():Boolean {super.performClick();return true}
 private fun animate(waves:List<Match3Engine.Wave>,i:Int){
  if(i>=waves.size){visual=Array(8){engine.board[it].clone()};wave=null;busy=false;invalidate();if(engine.won||engine.failed)done(engine.won);return}
  wave=waves[i];visual=waves[i].before;effect=0f;post(runner)
  postDelayed({if(isAttachedToWindow){visual=waves[i].after;invalidate();postDelayed({if(isAttachedToWindow)animate(waves,i+1)},140)}},260)
 }
}
