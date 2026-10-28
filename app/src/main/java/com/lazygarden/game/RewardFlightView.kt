package com.lazygarden.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.View
import kotlin.math.*

/** Reward particles arc from the result card toward the visible balance bar. */
class RewardFlightView(context:Context,private val coins:Boolean=true,private val stars:Boolean=true):View(context),SceneLifecycle{
 private val p=Paint(Paint.ANTI_ALIAS_FLAG);private val star=GameSkin.bitmap(context,R.drawable.reward_star);private var t=0f;private var a:ValueAnimator?=null
 private var paused=false
 internal val animationFraction get()=t
 override fun pauseScene(){paused=true;a?.pause()}
 override fun resumeScene(){paused=false;if(windowVisibility==VISIBLE)a?.resume()}
 override fun onWindowVisibilityChanged(visibility:Int){super.onWindowVisibilityChanged(visibility);if(visibility==VISIBLE&&!paused)a?.resume() else a?.pause()}
 override fun onAttachedToWindow(){super.onAttachedToWindow();a=ValueAnimator.ofFloat(0f,1f).apply{duration=1500;startDelay=250;addListener(object:android.animation.AnimatorListenerAdapter(){override fun onAnimationEnd(animation:android.animation.Animator){(parent as? android.view.ViewGroup)?.removeView(this@RewardFlightView)}});addUpdateListener{t=it.animatedValue as Float;invalidate()};start();if(paused||windowVisibility!=VISIBLE)pause()}}
 override fun onDetachedFromWindow(){a?.removeAllListeners();a?.removeAllUpdateListeners();a?.cancel();super.onDetachedFromWindow()}
 override fun onDraw(c:Canvas){if(t<=0||t>=1)return;for(i in 0..15){if((i==15&&!stars)||(i<15&&!coins))continue;val u=((t-i*.018f)/.70f).coerceIn(0f,1f);if(u<=0||u>=1)continue;val sx=width*.5f;val sy=height*.56f;val ex=width*(if(i==15).85f else .3f);val ey=height*.06f;val cx=sx+sin(i*2.3f)*width*.36f;val cy=height*.24f;val x=(1-u).pow(2)*sx+2*(1-u)*u*cx+u*u*ex;val y=(1-u).pow(2)*sy+2*(1-u)*u*cy+u*u*ey;val r=resources.displayMetrics.density*(if(i==15)18 else 9)
  if(i==15)c.drawBitmap(star,null,RectF(x-r,y-r,x+r,y+r),p) else{p.shader=RadialGradient(x-r*.3f,y-r*.3f,r*1.5f,intArrayOf(0xfffff3a1.toInt(),0xffffbf28.toInt(),0xffbd740f.toInt()),null,Shader.TileMode.CLAMP);c.drawCircle(x,y,r,p);p.shader=null;p.style=Paint.Style.STROKE;p.strokeWidth=r*.12f;p.color=0xffffe284.toInt();c.drawCircle(x,y,r*.7f,p);p.style=Paint.Style.FILL}
 }}
}
