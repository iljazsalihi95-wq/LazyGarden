package com.lazygarden.game
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.View
import kotlin.math.*

/** Earned material grows out of the result, then flies into its inventory counter. */
class MaterialRewardFlightView(context:Context,private val material:Material,private val amount:Int,private val arrived:()->Unit):View(context),SceneLifecycle{
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 private val art=MaterialArt(context);private var t=0f;private var animator:ValueAnimator?=null
 private var paused=false
 internal val animationFraction get()=t
 override fun pauseScene(){paused=true;animator?.pause()}
 override fun resumeScene(){paused=false;if(windowVisibility==VISIBLE)animator?.resume()}
 override fun onWindowVisibilityChanged(visibility:Int){super.onWindowVisibilityChanged(visibility);if(visibility==VISIBLE&&!paused)animator?.resume() else animator?.pause()}
 override fun onAttachedToWindow(){super.onAttachedToWindow();animator=ValueAnimator.ofFloat(0f,1f).apply{duration=1750;startDelay=120
  addUpdateListener{t=it.animatedValue as Float;invalidate()}
  addListener(object:android.animation.AnimatorListenerAdapter(){override fun onAnimationEnd(a:android.animation.Animator){if(isAttachedToWindow){arrived();(parent as? android.view.ViewGroup)?.removeView(this@MaterialRewardFlightView)}}});start();if(paused||windowVisibility!=VISIBLE)pause()}}
 override fun onDetachedFromWindow(){animator?.removeAllListeners();animator?.removeAllUpdateListeners();animator?.cancel();super.onDetachedFromWindow()}
 override fun onDraw(c:Canvas){if(t<=0||t>=1)return
  val d=resources.displayMetrics.density
  val launch=(t/.20f).coerceIn(0f,1f)
  val u=((t-.18f)/.82f).coerceIn(0f,1f)
  val eased=1f-(1f-u).pow(3)
  val startX=width*.5f;val startY=height*.60f;val endX=width*.5f;val endY=82*d
  val controlX=width*.72f;val controlY=height*.25f
  val inv=1f-eased
  val x=inv*inv*startX+2*inv*eased*controlX+eased*eased*endX
  val y=inv*inv*startY+2*inv*eased*controlY+eased*eased*endY
  val base=(if(t<.20f)24+34*launch else 58-37*eased)*d
  val pulse=1f+sin(t*PI.toFloat()*8f)*.055f*(1f-eased)
  val size=base*pulse

  // Soft comet trail makes the inventory destination visually obvious without changing accounting.
  for(i in 1..5){val q=(eased-i*.035f).coerceAtLeast(0f);val qi=1f-q
   val tx=qi*qi*startX+2*qi*q*controlX+q*q*endX;val ty=qi*qi*startY+2*qi*q*controlY+q*q*endY
   p.color=0xffffd86b.toInt();p.alpha=(95-i*13).coerceAtLeast(20);c.drawCircle(tx,ty,(7-i*.8f)*d,p)
  }
  // Launch and travel sparks.
  for(i in 0..15){val phase=(t*2.2f+i*.061f)%1f;p.color=if(i%3==0)0xfffff2a8.toInt() else if(i%2==0)0xffffd86b.toInt() else 0xff9de9b0.toInt();p.alpha=((1-phase)*210).toInt();c.drawCircle(x+cos(i*2.35f)*size*phase*2.15f,y+sin(i*2.35f)*size*phase*2.15f,3.2f*d*(1-phase),p)}

  // Arrival ring previews the inventory-counter impact; arrived() still fires only at animation end.
  if(eased>.82f){val hit=((eased-.82f)/.18f).coerceIn(0f,1f);p.style=Paint.Style.STROKE;p.strokeWidth=(3.5f*(1-hit)+1f)*d;p.color=0xffffe38a.toInt();p.alpha=((1-hit)*210).toInt();c.drawCircle(endX,endY,(16+28*hit)*d,p);p.style=Paint.Style.FILL}

  p.alpha=255;art.draw(c,p,material,RectF(x-size/2,y-size/2,x+size/2,y+size/2))
  p.color=Color.WHITE;p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.DEFAULT_BOLD;p.textSize=(19+3*(1-eased))*d;p.setShadowLayer(2*d,0f,d,0xff173e31.toInt());c.drawText("+$amount",x,y+size*.67f,p);p.clearShadowLayer()
 }
}
