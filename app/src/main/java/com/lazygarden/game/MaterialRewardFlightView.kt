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
 override fun onAttachedToWindow(){super.onAttachedToWindow();animator=ValueAnimator.ofFloat(0f,1f).apply{duration=1600;startDelay=150
  addUpdateListener{t=it.animatedValue as Float;invalidate()}
  addListener(object:android.animation.AnimatorListenerAdapter(){override fun onAnimationEnd(a:android.animation.Animator){if(isAttachedToWindow){arrived();(parent as? android.view.ViewGroup)?.removeView(this@MaterialRewardFlightView)}}});start();if(paused||windowVisibility!=VISIBLE)pause()}}
 override fun onDetachedFromWindow(){animator?.removeAllListeners();animator?.removeAllUpdateListeners();animator?.cancel();super.onDetachedFromWindow()}
 override fun onDraw(c:Canvas){if(t<=0||t>=1)return;val d=resources.displayMetrics.density;val u=((t-.22f)/.78f).coerceIn(0f,1f);val x=width*.5f;val y=(1-u).pow(2)*height*.57f+2*(1-u)*u*height*.27f+u*u*85*d
  val size=(if(t<.22f)24+30*(t/.22f) else 54-35*u)*d
  for(i in 0..13){val phase=(t*2+i*.071f)%1f;p.color=if(i%2==0)0xffffd86b.toInt() else 0xff9de9b0.toInt();p.alpha=((1-phase)*220).toInt();c.drawCircle(x+cos(i*2.4f)*size*phase*2,y+sin(i*2.4f)*size*phase*2,3*d*(1-phase),p)}
  p.alpha=255;art.draw(c,p,material,RectF(x-size/2,y-size/2,x+size/2,y+size/2))
  p.color=Color.WHITE;p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.DEFAULT_BOLD;p.textSize=20*d;p.setShadowLayer(2*d,0f,d,0xff173e31.toInt());c.drawText("+$amount",x,y+size*.65f,p);p.clearShadowLayer()
 }
}
