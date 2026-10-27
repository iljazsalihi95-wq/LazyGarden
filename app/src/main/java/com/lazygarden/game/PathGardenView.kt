package com.lazygarden.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.sin

/** Native live scene 3: residents clear, level and rebuild the garden path. */
class PathGardenView(
 context:Context,
 initialElapsed:Long=0L,
 private val checkpoint:(Long)->Unit={},
 private val onComplete:()->Unit={}
):View(context){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 private val garden=GameSkin.bitmap(context,R.drawable.garden_estate)
 private val residents=BitmapFactory.decodeResource(resources,R.drawable.resident_atlas)
 private var ms=initialElapsed.coerceIn(0L,PathSequence.DURATION)
 private var clock:ValueAnimator?=null
 private var pulse=0f
 private var completed=false
 override fun onAttachedToWindow(){super.onAttachedToWindow();resumeScene()}
 override fun onDetachedFromWindow(){pauseScene();super.onDetachedFromWindow()}
 fun resumeScene(){if(clock!=null||completed)return;var last=android.os.SystemClock.uptimeMillis();clock=ValueAnimator.ofFloat(0f,1f).apply{duration=9000;repeatCount=ValueAnimator.INFINITE;interpolator=LinearInterpolator();addUpdateListener{val now=android.os.SystemClock.uptimeMillis();val dt=(now-last).coerceIn(0L,700L);last=now;pulse=(pulse+dt/9000f)%1f;ms=(ms+dt).coerceAtMost(PathSequence.DURATION);if(ms>=PathSequence.DURATION&&!completed){completed=true;checkpoint(ms);post{onComplete()}};invalidate()};start()}}
 fun pauseScene(){checkpoint(ms);clock?.cancel();clock=null}
 override fun onDraw(c:Canvas){super.onDraw(c);val s=maxOf(width.toFloat()/garden.width,height.toFloat()/garden.height);val dw=garden.width*s;val dh=garden.height*s;val dst=RectF((width-dw)/2,(height-dh)/2,(width+dw)/2,(height+dh)/2);c.drawBitmap(garden,null,dst,p);drawPath(c,dst);drawWorkers(c,dst);drawDialogue(c)}
 private fun drawPath(c:Canvas,d:RectF){
  val cx=d.centerX();val top=d.top+d.height()*.48f;val bottom=d.bottom;val old=PathSequence.progress(ms,6500,14500);val laid=PathSequence.progress(ms,18500,26500)
  p.style=Paint.Style.FILL;p.color=0xff795b42.toInt();val path=Path();path.moveTo(cx-d.width()*.08f,top);path.lineTo(cx+d.width()*.08f,top);path.lineTo(cx+d.width()*.31f,bottom);path.lineTo(cx-d.width()*.31f,bottom);path.close();c.drawPath(path,p)
  val stones=18;for(i in 0 until stones){val t=i/(stones-1f);val unlocked=(i+1)/stones.toFloat()<=laid;if(!unlocked)continue;val y=top+(bottom-top)*t;val half=d.width()*(.065f+.22f*t);p.color=if(i%2==0)0xffd8c7a5.toInt() else 0xffc9b58f.toInt();c.drawRoundRect(RectF(cx-half,y-dp(9f),cx+half,y+dp(9f)),dp(7f),dp(7f),p)}
  if(old<1f){p.color=0xaa5a4636.toInt();for(i in 0..9){val t=(i*.103f+pulse*.05f)%1f;val x=cx+d.width()*(sin(i*2.1).toFloat())*.18f*(.3f+t);val y=top+(bottom-top)*t;c.drawCircle(x,y,dp(5f)*(1-old),p)}}
 }
 private fun drawWorkers(c:Canvas,d:RectF){val st=PathSequence.stage(ms);val state=when(st){PathSequence.Stage.ARRIVE->ResidentAnimation.State.WALK;PathSequence.Stage.INSPECT->ResidentAnimation.State.LOOK;PathSequence.Stage.CLEAR,PathSequence.Stage.REMOVE_BROKEN,PathSequence.Stage.LEVEL_GROUND,PathSequence.Stage.LAY_STONES,PathSequence.Stage.BRUSH->ResidentAnimation.State.WORK;PathSequence.Stage.REVEAL->ResidentAnimation.State.HAPPY;PathSequence.Stage.CELEBRATE->ResidentAnimation.State.CELEBRATE};val pts=arrayOf(.25f to .70f,.72f to .66f,.34f to .82f,.66f to .84f);for(i in 0..3){val x=d.left+d.width()*pts[i].first;val y=d.top+d.height()*pts[i].second;drawResident(c,i,x,y,d.width()*.16f,ResidentAnimation.frame(state,(pulse*100).toInt()+i))}}
 private fun drawResident(c:Canvas,id:Int,x:Float,y:Float,w:Float,frame:Int){val cw=residents.width/8;val ch=residents.height/4;val src=Rect(frame*cw,id*ch,(frame+1)*cw,(id+1)*ch);val h=w*ch/cw;c.drawBitmap(residents,src,RectF(x-w/2,y-h,x+w/2,y),p)}
 private fun drawDialogue(c:Canvas){val text=PathSequence.dialogue(ms);if(text.isBlank())return;val r=RectF(dp(18f),height*.23f,width-dp(18f),height*.34f);p.color=0xf5fff8e8.toInt();c.drawRoundRect(r,dp(16f),dp(16f),p);p.color=0xff4b3525.toInt();p.textSize=dp(13f);p.typeface=Typeface.DEFAULT_BOLD;p.textAlign=Paint.Align.CENTER;c.drawText(text.take(62),r.centerX(),r.centerY()+dp(5f),p)}
 private fun dp(v:Float)=v*resources.displayMetrics.density
}
