package com.lazygarden.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

/** Raster game pieces on a native, responsive board. Input is locked during resolution. */
class Match3BoardView(c:Context,val engine:Match3Engine,private val changed:()->Unit,private val done:(Boolean)->Unit):View(c){
 init { contentDescription="Tabela Match-3" }
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 private val d=resources.displayMetrics.density
 private val tiles= intArrayOf(R.drawable.tile_apple,R.drawable.tile_flower,R.drawable.tile_leaf,R.drawable.tile_water,R.drawable.tile_berry,R.drawable.tile_orange).map{BitmapFactory.decodeResource(resources,it)}
 private val background=BitmapFactory.decodeResource(resources,R.drawable.garden_estate)
 private val specialArt=intArrayOf(R.drawable.boost_row,R.drawable.boost_column,R.drawable.boost_bomb).map{BitmapFactory.decodeResource(resources,it)}
 private var selected=-1;private var down=-1;private var busy=false
 private var wave:Match3Engine.Wave?=null;private var effect=1f
 private var visual=Array(8){engine.board[it].clone()}
 private var cell=0f;private var left=0f;private var top=0f
 private var swapA=-1;private var swapB=-1;private var swapProgress=0f
 private var animator:ValueAnimator?=null
 var targetBooster:Match3Engine.Booster?=null
 var onBoosterTarget:((Match3Engine.Booster,Int)->List<Match3Engine.Wave>)?=null
 val isBusy get()=busy
 override fun onDetachedFromWindow(){animator?.cancel();removeCallbacks(advance);super.onDetachedFromWindow()}
 private var advance=Runnable{}
 private fun text(c:Canvas,s:String,x:Float,y:Float,size:Float,color:Int=0xff54371f.toInt()){
  p.shader=null;p.alpha=255;p.color=color;p.textSize=size;p.typeface=Typeface.DEFAULT_BOLD;p.textAlign=Paint.Align.CENTER;c.drawText(s,x,y,p)
 }
 private fun panel(c:Canvas,r:RectF,colors:IntArray,radius:Float=18*d){
  p.shader=LinearGradient(r.left,r.top,r.left,r.bottom,colors,null,Shader.TileMode.CLAMP);p.alpha=255;c.drawRoundRect(r,radius,radius,p);p.shader=null
 }
 override fun onDraw(c:Canvas){
  super.onDraw(c)
  p.alpha=255;c.drawBitmap(background,null,Rect(0,0,width,height),p);c.drawColor(0xbb173b3d.toInt())
  val w=width.toFloat();val header=118*d
  cell=min((w-24*d)/8,(height-header-45*d)/8).coerceAtLeast(1f);left=(w-cell*8)/2;top=header+max(0f,(height-header-45*d-cell*8)*.38f)
  panel(c,RectF(10*d,4*d,w-10*d,108*d),intArrayOf(0xffeec58a.toInt(),0xffffefd2.toInt()))
  text(c,"Niveli ${engine.level}",w*.5f,29*d,20*d)
  text(c,"${engine.moves}",w*.84f,64*d,30*d)
  text(c,"Lëvizje",w*.84f,88*d,12*d)
  text(c,"${engine.score} / ${engine.spec.target}",w*.31f,53*d,16*d)
  val progress=(engine.score.toFloat()/engine.spec.target).coerceIn(0f,1f)
  panel(c,RectF(25*d,61*d,w*.61f,68*d),intArrayOf(0xffc2a074.toInt(),0xffc2a074.toInt()),4*d)
  if(progress>0)panel(c,RectF(25*d,61*d,25*d+(w*.61f-25*d)*progress,68*d),intArrayOf(0xff95d94c.toInt(),0xff299b42.toInt()),4*d)
  p.alpha=255
  c.drawBitmap(tiles[engine.spec.collectColor],null,RectF(23*d,73*d,49*d,99*d),p)
  text(c,"${engine.collected[engine.spec.collectColor]}/${engine.spec.collectCount}   ❄ ${engine.clearedIce}/${engine.spec.ice}",w*.36f,91*d,14*d)
  panel(c,RectF(left-5*d,top-5*d,left+cell*8+5*d,top+cell*8+5*d),intArrayOf(0xffe7bd76.toInt(),0xff875423.toInt()),10*d)
  for(r in 0..7)for(col in 0..7){
   val i=r*8+col;val x=left+col*cell;val y=top+r*cell
   val frost=engine.ice[r][col]>0
   panel(c,RectF(x+1*d,y+1*d,x+cell-1*d,y+cell-1*d),if(selected==i)intArrayOf(0xffffe786.toInt(),0xffffb849.toInt()) else if(frost)intArrayOf(0xffb4efff.toInt(),0xff70c9e4.toInt()) else if((r+col)%2==0)intArrayOf(0xff377d8e.toInt(),0xff215b71.toInt()) else intArrayOf(0xff326f83.toInt(),0xff194e64.toInt()),5*d)
   var dx=0f;var dy=0f
   if(i==swapA||i==swapB){val to=if(i==swapA)swapB else swapA;dx=(to%8-col)*cell*swapProgress;dy=(to/8-r)*cell*swapProgress}
   val hit=wave?.cells?.contains(i)==true
   val scale=if(hit && effect<.48f)(1f-effect/.48f).coerceAtLeast(0f) else 1f
   val fall=if(wave!=null && effect>=.48f)(1f-(effect-.48f)/.52f).coerceIn(0f,1f)*cell*.65f else 0f
   val value=visual[r][col];val inset=cell*(.04f+(1-scale)*.46f)
   if(scale>0f){p.alpha=if(fall>0)(255*(1-fall/cell*.6f)).toInt() else 255
    c.drawBitmap(tiles[value%6],null,RectF(x+inset+dx,y+inset+dy-fall,x+cell-inset+dx,y+cell-inset+dy-fall),p)
    if(value>=6){p.alpha=255;c.drawBitmap(specialArt[(value/6-1).coerceIn(0,2)],null,RectF(x+cell*.35f,y+cell*.42f,x+cell,y+cell),p)}
   }
   if(hit && effect<.48f){p.alpha=255;p.color=0xffffdf6e.toInt();for(k in 0..5){val a=k*PI/3;p.alpha=(255*(1-effect/.48f)).toInt().coerceIn(0,255);c.drawCircle(x+cell/2+(cos(a)*cell*effect).toFloat(),y+cell/2+(sin(a)*cell*effect).toFloat(),cell*.05f,p)}}
  }
  p.alpha=255
  val caption=targetBooster?.let{"${it.label}: prek figurën"} ?: if(busy && engine.combo>1)"Shkëlqyeshëm! ×${wave?.combo?:1}" else "Bashko 3 · Krijo 4 ose 5 për fuqi speciale"
  text(c,caption,w/2,top+cell*8+29*d,min(13*d,w/29),0xffffefc7.toInt())
 }
 private fun index(x:Float,y:Float):Int {if(x<left||x>=left+8*cell||y<top||y>=top+8*cell)return -1;return ((y-top)/cell).toInt()*8+((x-left)/cell).toInt()}
 override fun onTouchEvent(e:MotionEvent):Boolean {
  if(busy)return true
  if(e.action==MotionEvent.ACTION_DOWN){down=index(e.x,e.y);return true}
  if(e.action==MotionEvent.ACTION_UP){
   performClick();val end=index(e.x,e.y);if(end<0)return true
   targetBooster?.let{kind->val waves=onBoosterTarget?.invoke(kind,end).orEmpty();targetBooster=null;selected=-1;if(waves.isNotEmpty()){busy=true;changed();animate(waves,0)}else invalidate();return true}
   val start=if(down>=0 && down!=end)down else selected
   if(start>=0 && engine.legal(start,end)){
    busy=true;selected=-1;swapA=start;swapB=end
    tween(160){t->swapProgress=t;invalidate()}.also{it.addListener(object:android.animation.AnimatorListenerAdapter(){override fun onAnimationEnd(a:android.animation.Animator){
     if(!isAttachedToWindow)return
     swapA=-1;swapB=-1;val waves=engine.swap(start,end);changed();animate(waves,0)
    }})}
   }else{selected=end;invalidate()}
   return true
  };return true
 }
 override fun performClick():Boolean {super.performClick();return true}
 private fun tween(duration:Long,update:(Float)->Unit):ValueAnimator {
  return ValueAnimator.ofFloat(0f,1f).apply{this.duration=duration;addUpdateListener{update(it.animatedValue as Float)};animator=this;start()}
 }
 fun runBooster(kind:Match3Engine.Booster){if(!busy){targetBooster=if(targetBooster==kind)null else kind;invalidate()}}
 private fun animate(waves:List<Match3Engine.Wave>,i:Int){
  if(i>=waves.size){visual=Array(8){engine.board[it].clone()};wave=null;busy=false;invalidate();if(engine.won||engine.failed)done(engine.won);return}
  wave=waves[i];visual=waves[i].before;effect=0f
  tween(480){t->effect=t;if(t>=.48f)visual=waves[i].after;invalidate()}
  advance=Runnable{if(isAttachedToWindow)animate(waves,i+1)};postDelayed(advance,500)
 }
}
