package com.lazygarden.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

/** Illustrated board with per-piece gravity tracks, responsive swipes and staged power effects. */
class Match3BoardView(c:Context,val engine:Match3Engine,private val changed:()->Unit,private val done:(Boolean)->Unit):View(c){
 init{contentDescription="Tabela Match-3"}
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 private val d=resources.displayMetrics.density
 private val tiles=intArrayOf(R.drawable.tile_apple,R.drawable.tile_flower,R.drawable.tile_leaf,R.drawable.tile_water,R.drawable.tile_berry,R.drawable.tile_orange).map{GameSkin.bitmap(c,it)}
 private val background=GameSkin.bitmap(c,R.drawable.garden_estate)
 private val socket=GameSkin.bitmap(c,R.drawable.hd_socket)
 private val frame=GameSkin.bitmap(c,R.drawable.hd_board_frame)
 private val hud=GameSkin.bitmap(c,R.drawable.hd_hud)
 private val iceArt=GameSkin.bitmap(c,R.drawable.hd_ice)
 private val star=GameSkin.bitmap(c,R.drawable.reward_star)
 private val powerArt=intArrayOf(R.drawable.boost_row,R.drawable.boost_column,R.drawable.boost_bomb).map{GameSkin.bitmap(c,it)}
 private val darkSocket=ColorMatrixColorFilter(ColorMatrix(floatArrayOf(.86f,0f,0f,0f,0f,0f,.89f,0f,0f,0f,0f,0f,.93f,0f,0f,0f,0f,0f,1f,0f)))
 private var selected=-1;private var down=-1;private var downX=0f;private var downY=0f;private var busy=false
 private var wave:Match3Engine.Wave?=null;private var effect=1f
 private var visual=Array(8){engine.board[it].clone()}
 private var displayedIce=Array(8){engine.ice[it].clone()}
 private var displayedScore=engine.score.toFloat()
 private var cell=0f;private var left=0f;private var top=0f
 private var swapA=-1;private var swapB=-1;private var swapProgress=0f
 private var animator:ValueAnimator?=null
 private var boostEffect:Match3Engine.Booster?=null
 private var feedback=""
 var targetBooster:Match3Engine.Booster?=null
 var onBoosterTarget:((Match3Engine.Booster,Int)->List<Match3Engine.Wave>)?=null
 val isBusy get()=busy
 private var advance=Runnable{}
 override fun onDetachedFromWindow(){animator?.removeAllListeners();animator?.cancel();removeCallbacks(advance);super.onDetachedFromWindow()}
 private fun text(c:Canvas,s:String,x:Float,y:Float,size:Float,color:Int=0xff593817.toInt()){
  p.shader=null;p.colorFilter=null;p.alpha=255;p.color=color;p.textSize=size;p.typeface=Typeface.create("sans-serif-condensed",Typeface.BOLD);p.textAlign=Paint.Align.CENTER
  p.setShadowLayer(d,0f,d,0x44552d11);c.drawText(s,x,y,p);p.clearShadowLayer()
 }
 private fun image(c:Canvas,b:Bitmap,r:RectF,alpha:Int=255){p.shader=null;p.colorFilter=null;p.alpha=alpha;c.drawBitmap(b,null,r,p);p.alpha=255}
 override fun onDraw(c:Canvas){
  super.onDraw(c)
  val w=width.toFloat();val h=height.toFloat()
  val scale=max(w/background.width,h/background.height);val bw=background.width*scale;val bh=background.height*scale
  image(c,background,RectF((w-bw)/2,(h-bh)/2,(w+bw)/2,(h+bh)/2));c.drawColor(0x99192e29.toInt())
  val header=126*d
  cell=min((w-30*d)/8,(h-header-28*d)/8).coerceAtLeast(1f)
  left=(w-cell*8)/2;top=header+max(0f,(h-header-28*d-cell*8)*.35f)
  p.alpha=255;GameSkin.nine(c,hud,RectF(7*d,2*d,w-7*d,114*d),20*d,p,.20f)
  text(c,"Niveli ${engine.level}",w*.5f,24*d,21*d)
  text(c,"Lëvizje",w*.83f,48*d,12*d)
  text(c,"${engine.moves}",w*.83f,82*d,35*d)
  text(c,"${displayedScore.toInt()} / ${engine.spec.target}",w*.33f,50*d,18*d)
  val fraction=(displayedScore/engine.spec.target).coerceIn(0f,1f)
  val bar=RectF(w*.09f,57*d,w*.61f,65*d)
  p.color=0xff80633a.toInt();c.drawRoundRect(bar,4*d,4*d,p)
  if(fraction>0){p.shader=LinearGradient(bar.left,bar.top,bar.left,bar.bottom,intArrayOf(0xffffed7a.toInt(),0xffd18a1c.toInt()),null,Shader.TileMode.CLAMP);c.drawRoundRect(RectF(bar.left,bar.top,bar.left+bar.width()*fraction,bar.bottom),4*d,4*d,p);p.shader=null}
  for(i in 0..2){val x=bar.left+bar.width()*(i+1)/3;image(c,star,RectF(x-7*d,52*d,x+7*d,68*d),if(fraction>=(i+1)/3f)255 else 105)}
  if(engine.spec.collectCount>0){image(c,tiles[engine.spec.collectColor],RectF(w*.13f,73*d,w*.13f+28*d,101*d));text(c,"${engine.collected[engine.spec.collectColor]}/${engine.spec.collectCount}",w*.31f,94*d,17*d)}
  if(engine.spec.ice>0){image(c,iceArt,RectF(w*.41f,75*d,w*.41f+24*d,99*d));text(c,"${engine.clearedIce}/${engine.spec.ice}",w*.56f,94*d,17*d)}
  if(engine.spec.collectCount==0 && engine.spec.ice==0)text(c,"Mblidh ${engine.spec.target} pikë",w*.33f,93*d,15*d)
  val boardBounds=RectF(left-12*d,top-12*d,left+8*cell+12*d,top+8*cell+12*d)
  p.color=0xff082d39.toInt();c.drawRoundRect(boardBounds,18*d,18*d,p)
  for(r in 0..7)for(col in 0..7){
   val x=left+col*cell;val y=top+r*cell
   p.alpha=255;p.colorFilter=if((r+col)%2==0)null else darkSocket
   c.drawBitmap(socket,null,RectF(x-.6*d,y-.6*d,x+cell+.6*d,y+cell+.6*d),p);p.colorFilter=null
   if(selected==r*8+col || targetBooster!=null && selected==r*8+col){p.shader=RadialGradient(x+cell/2,y+cell/2,cell*.7f,intArrayOf(0xaaffdf64.toInt(),0x00ffdf64),null,Shader.TileMode.CLAMP);c.drawCircle(x+cell/2,y+cell/2,cell*.7f,p);p.shader=null}
  }
  val clip=c.save();c.clipRect(left,top,left+8*cell,top+8*cell)
  val active=wave
  val falling=active!=null && !active.shuffled && effect>=.35f
  for(r in 0..7)for(col in 0..7){
   val index=r*8+col;val x=left+col*cell;var y=top+r*cell
   var dx=0f;var dy=0f
   if(index==swapA||index==swapB){val to=if(index==swapA)swapB else swapA;dx=(to%8-col)*cell*swapProgress;dy=(to/8-r)*cell*swapProgress}
   val hit=active?.cells?.contains(index)==true
   val size=if(active?.shuffled==true)abs(1f-effect*2).coerceAtLeast(.01f) else if(hit && !falling)(1f-effect/.35f).coerceIn(0f,1f) else 1f
   if(falling){val t=((effect-.35f)/.65f).coerceIn(0f,1f);val ease=1-(1-t).pow(3);val source=active!!.origins[r][col];y=top+(source+(r-source)*ease)*cell}
   val value=visual[r][col]
   val rect=RectF(x+cell*.04f+dx,y+cell*.04f+dy,x+cell*.96f+dx,y+cell*.96f+dy)
   if(size>0){val saved=c.save();c.scale(size,size,x+cell/2+dx,y+cell/2+dy)
    if(value<6)image(c,tiles[value],rect)
    else{image(c,powerArt[(value/6-1).coerceIn(0,2)],rect);image(c,tiles[value%6],RectF(rect.right-cell*.29f,rect.bottom-cell*.29f,rect.right,rect.bottom))}
    c.restoreToCount(saved)
   }
  }
  c.restoreToCount(clip)
  for(r in 0..7)for(col in 0..7)if(displayedIce[r][col]>0){val x=left+col*cell;val y=top+r*cell;image(c,iceArt,RectF(x,y,x+cell,y+cell))}
  p.alpha=255;GameSkin.nine(c,frame,boardBounds,24*d,p,.18f,false)
  if(active!=null && effect<.42f){
   val intensity=(1-effect/.42f).coerceIn(0f,1f)
   for(index in active.cells){
    val x=left+(index%8+.5f)*cell;val y=top+(index/8+.5f)*cell
    val kind=active.before[index/8][index%8]/6
    if(kind>0 || boostEffect!=null){
     p.shader=null;p.color=if(kind==1||boostEffect==Match3Engine.Booster.ROW)0xffeeb94b.toInt() else 0xff9de8ff.toInt();p.alpha=(intensity*160).toInt();p.strokeWidth=cell*.11f*intensity
     if(kind==1||boostEffect==Match3Engine.Booster.ROW||boostEffect==Match3Engine.Booster.CROSS)c.drawLine(left,y,left+8*cell,y,p)
     if(kind==2||boostEffect==Match3Engine.Booster.COLUMN||boostEffect==Match3Engine.Booster.CROSS)c.drawLine(x,top,x,top+8*cell,p)
    }
    if(index%3==0)for(k in 0..3){val a=k*PI/2+index;val travel=cell*(.2f+effect*2f);val sx=x+(cos(a)*travel).toFloat();val sy=y+(sin(a)*travel).toFloat();val sz=cell*.12f*intensity
     val state=c.save();c.rotate(effect*220,sx,sy);image(c,star,RectF(sx-sz,sy-sz,sx+sz,sy+sz),(255*intensity).toInt());c.restoreToCount(state)
    }
   }
  }
  p.alpha=255
  val caption=targetBooster?.let{"${it.label}: zgjidh figurën"} ?: if(busy && (active?.combo?:0)>1)"Shkëlqyeshëm! ×${active?.combo}" else feedback
  if(caption.isNotEmpty())text(c,caption,w/2,top+8*cell+24*d,min(16*d,w/24),0xffffefb5.toInt())
 }
 internal fun cellCenter(index:Int)=PointF(left+(index%8+.5f)*cell,top+(index/8+.5f)*cell)
 private fun index(x:Float,y:Float):Int{if(x<left||x>=left+8*cell||y<top||y>=top+8*cell)return -1;return ((y-top)/cell).toInt()*8+((x-left)/cell).toInt()}
 override fun onTouchEvent(e:MotionEvent):Boolean{
  if(busy)return true
  if(e.actionMasked==MotionEvent.ACTION_DOWN){down=index(e.x,e.y);downX=e.x;downY=e.y;return true}
  if(e.actionMasked==MotionEvent.ACTION_UP){
   performClick();var end=index(e.x,e.y);if(end<0 && down<0)return true
   if(down>=0 && hypot(e.x-downX,e.y-downY)>cell*.25f){
    val dx=e.x-downX;val dy=e.y-downY
    end=if(abs(dx)>abs(dy))down+if(dx>0)1 else -1 else down+if(dy>0)8 else -8
    if(end !in 0..63 || abs(down/8-end/8)+abs(down%8-end%8)!=1){selected=-1;invalidate();return true}
   }
   if(end<0)return true
   targetBooster?.let{kind->val waves=onBoosterTarget?.invoke(kind,end).orEmpty();targetBooster=null;selected=-1
    if(waves.isNotEmpty()){boostEffect=kind;busy=true;changed();animate(waves,0)}else invalidate();return true}
   val start=if(down>=0 && down!=end)down else selected
   if(start>=0 && abs(start/8-end/8)+abs(start%8-end%8)==1){
    val legal=engine.legal(start,end);busy=true;selected=-1;swapA=start;swapB=end;feedback=""
    val a=ValueAnimator.ofFloat(0f,1f).apply{duration=if(legal)150 else 130;repeatCount=if(legal)0 else 1;repeatMode=ValueAnimator.REVERSE;addUpdateListener{swapProgress=it.animatedValue as Float;invalidate()}}
    animator=a;a.addListener(object:android.animation.AnimatorListenerAdapter(){override fun onAnimationEnd(animation:android.animation.Animator){
     if(!isAttachedToWindow)return
     swapA=-1;swapB=-1
     if(legal){val waves=engine.swap(start,end);changed();animate(waves,0)}else{busy=false;feedback="Provo një bashkim tjetër";invalidate()}
    }});a.start()
   }else{selected=end;feedback="";invalidate()}
   return true
  }
  return true
 }
 override fun performClick():Boolean{super.performClick();return true}
 fun runBooster(kind:Match3Engine.Booster){if(!busy){targetBooster=if(targetBooster==kind)null else kind;invalidate()}}
 private fun animate(waves:List<Match3Engine.Wave>,i:Int){
  if(i>=waves.size){visual=Array(8){engine.board[it].clone()};displayedIce=Array(8){engine.ice[it].clone()};displayedScore=engine.score.toFloat();wave=null;boostEffect=null;busy=false;invalidate();if(engine.won||engine.failed)done(engine.won);return}
  val startScore=displayedScore;val endScore=waves[i].scoreAfter.toFloat()
  wave=waves[i];visual=waves[i].before;displayedIce=waves[i].iceBefore;effect=0f
  animator=ValueAnimator.ofFloat(0f,1f).apply{duration=560;addUpdateListener{effect=it.animatedValue as Float;if(effect >= (if(waves[i].shuffled) .5f else .35f)){visual=waves[i].after;displayedIce=waves[i].iceAfter};displayedScore=startScore+(endScore-startScore)*effect;invalidate()};start()}
  advance=Runnable{if(isAttachedToWindow)animate(waves,i+1)};postDelayed(advance,575)
 }
}
