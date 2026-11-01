package com.lazygarden.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.View
import kotlin.math.*

/** Premium reward particles arc from the result card toward the visible HUD balances. */
class RewardFlightView(context:Context,private val coins:Boolean=true,private val stars:Boolean=true):View(context),SceneLifecycle{
 private val p=Paint(Paint.ANTI_ALIAS_FLAG)
 private val star=GameSkin.bitmap(context,R.drawable.reward_star)
 private var t=0f
 private var a:ValueAnimator?=null
 private var paused=false
 internal val animationFraction get()=t

 override fun pauseScene(){paused=true;a?.pause()}
 override fun resumeScene(){paused=false;if(windowVisibility==VISIBLE)a?.resume()}
 override fun onWindowVisibilityChanged(visibility:Int){super.onWindowVisibilityChanged(visibility);if(visibility==VISIBLE&&!paused)a?.resume() else a?.pause()}
 override fun onAttachedToWindow(){
  super.onAttachedToWindow()
  a=ValueAnimator.ofFloat(0f,1f).apply{
   duration=1780;startDelay=120
   addListener(object:android.animation.AnimatorListenerAdapter(){override fun onAnimationEnd(animation:android.animation.Animator){(parent as? android.view.ViewGroup)?.removeView(this@RewardFlightView)}})
   addUpdateListener{t=it.animatedValue as Float;invalidate()};start();if(paused||windowVisibility!=VISIBLE)pause()
  }
 }
 override fun onDetachedFromWindow(){a?.removeAllListeners();a?.removeAllUpdateListeners();a?.cancel();super.onDetachedFromWindow()}

 override fun onDraw(c:Canvas){
  if(t<=0||t>=1)return
  val d=resources.displayMetrics.density
  val sx=width*.5f;val sy=height*.56f

  // Layer 1: expanding launch glow + alternating spark burst.
  val burst=(t/.18f).coerceIn(0f,1f)
  if(burst<1f){
   p.shader=RadialGradient(sx,sy,62*d*burst,intArrayOf(0x00ffd65a,0x88ffd65a.toInt(),0x00ffd65a),null,Shader.TileMode.CLAMP)
   c.drawCircle(sx,sy,62*d*burst,p);p.shader=null
   for(j in 0..15){
    val ang=j*(Math.PI*2/16).toFloat();val rr=(24+18*(j%3))*d*burst
    p.color=if(j%2==0)0xffffd55f.toInt() else 0xffffffff.toInt();p.alpha=((1-burst)*220).toInt()
    c.drawCircle(sx+cos(ang)*rr,sy+sin(ang)*rr,(2.4f+(j%3))*d*(1-burst),p)
   }
  }
  p.alpha=255

  // Layer 2: staggered curved flights. Coins fan out; the star gets its own HUD target.
  for(i in 0..17){
   val isStar=i>=16
   if((isStar&&!stars)||(!isStar&&!coins))continue
   val u=((t-i*.015f)/.74f).coerceIn(0f,1f)
   if(u<=0||u>=1)continue
   val ex=width*(if(isStar).85f else .30f);val ey=height*.06f
   val lane=(i%8)-3.5f
   val cx=sx+lane*width*.055f+sin(i*2.3f)*width*.10f
   val cy=height*(.20f+(.025f*(i%3)))
   val x=(1-u).pow(2)*sx+2*(1-u)*u*cx+u*u*ex
   val y=(1-u).pow(2)*sy+2*(1-u)*u*cy+u*u*ey
   val pulse=1f+.18f*sin(u*Math.PI).toFloat();val r=d*(if(isStar)17f else 8.5f)*pulse

   // Motion trail: three fading points communicate direction and speed.
   repeat(3){trail->
    val lag=(trail+1)*.016f
    val tu=(u-lag).coerceAtLeast(0f)
    val tx=(1-tu).pow(2)*sx+2*(1-tu)*tu*cx+tu*tu*ex
    val ty=(1-tu).pow(2)*sy+2*(1-tu)*tu*cy+tu*tu*ey
    p.color=if(isStar)0xffffffff.toInt() else 0xffffe7a0.toInt();p.alpha=(105-trail*25)
    c.drawCircle(tx,ty,r*(.24f-trail*.04f),p)
   }
   p.alpha=255

   if(isStar){
    c.save();c.rotate(28f*u+if(i==17)14f else 0f,x,y);c.drawBitmap(star,null,RectF(x-r,y-r,x+r,y+r),p);c.restore()
   }else{
    p.shader=RadialGradient(x-r*.3f,y-r*.3f,r*1.55f,intArrayOf(0xfffff7b7.toInt(),0xffffc52f.toInt(),0xffa96208.toInt()),null,Shader.TileMode.CLAMP)
    c.drawCircle(x,y,r,p);p.shader=null
    p.style=Paint.Style.STROKE;p.strokeWidth=r*.13f;p.color=0xffffe284.toInt();c.drawCircle(x,y,r*.70f,p);p.style=Paint.Style.FILL
   }
  }

  // Layer 3: HUD acknowledgement rings and a short arrival sparkle.
  if(t>.70f){
   val v=((t-.70f)/.30f).coerceIn(0f,1f)
   p.style=Paint.Style.STROKE;p.strokeWidth=(2.8f-1.2f*v)*d;p.alpha=((1-v)*190).toInt()
   if(coins){p.color=0xffffc93d.toInt();c.drawCircle(width*.30f,height*.06f,9*d+23*d*v,p)}
   if(stars){p.color=0xffffffff.toInt();c.drawCircle(width*.85f,height*.06f,11*d+25*d*v,p)}
   p.style=Paint.Style.FILL
   if(v<.72f){
    val fade=(1-v/.72f).coerceIn(0f,1f);p.alpha=(210*fade).toInt()
    val targets=buildList<Pair<Float,Float>>{if(coins)add(width*.30f to height*.06f);if(stars)add(width*.85f to height*.06f)}
    targets.forEach{(tx,ty)->for(j in 0..7){val ang=j*(Math.PI/4).toFloat();val rr=(12+18*v)*d;p.color=if(j%2==0)0xffffffff.toInt() else 0xffffd55f.toInt();c.drawCircle(tx+cos(ang)*rr,ty+sin(ang)*rr,2.2f*d*fade,p)}}
   }
   p.alpha=255
  }
 }
}
