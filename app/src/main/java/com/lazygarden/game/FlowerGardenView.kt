package com.lazygarden.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.sin

/** Native scene 2. No HTML/image demo: flower choice, soil, planting, watering and bloom are rendered live. */
class FlowerGardenView(
 context: Context,
 private val language:String="sq",
 private val initialChoice:String?=null,
 private val onChoice:(FlowerSequence.FlowerChoice)->Unit={},
 private val onComplete:()->Unit={}
):View(context){
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 private val garden=GameSkin.bitmap(context,R.drawable.garden_estate)
 private val residents=BitmapFactory.decodeResource(resources,R.drawable.resident_atlas)
 private var ms=0L; private var life=0f; private var clock:ValueAnimator?=null
 private var chosen=FlowerSequence.choices.firstOrNull{it.id==initialChoice}
 private var completed=false
 private val bed=RectF()

 override fun onAttachedToWindow(){super.onAttachedToWindow();start()}
 override fun onDetachedFromWindow(){clock?.cancel();clock=null;super.onDetachedFromWindow()}
 private fun start(){if(clock!=null)return;var last=android.os.SystemClock.uptimeMillis();clock=ValueAnimator.ofFloat(0f,1f).apply{duration=12000;repeatCount=ValueAnimator.INFINITE;interpolator=LinearInterpolator();addUpdateListener{val now=android.os.SystemClock.uptimeMillis();val dt=(now-last).coerceIn(0,1000);last=now;life=(life+dt/12000f)%1f;if(FlowerSequence.stage(ms)!=FlowerSequence.Stage.CHOOSE||chosen!=null)ms=(ms+dt).coerceAtMost(FlowerSequence.DURATION);if(ms>=FlowerSequence.DURATION&&!completed){completed=true;post{onComplete()}};invalidate()};start()}}

 override fun onDraw(c:Canvas){super.onDraw(c);val s=maxOf(width.toFloat()/garden.width,height.toFloat()/garden.height);val dw=garden.width*s;val dh=garden.height*s;val dst=RectF((width-dw)/2,(height-dh)/2,(width+dw)/2,(height+dh)/2);c.drawBitmap(garden,null,dst,p)
  bed.set(dst.left+dw*.12f,dst.top+dh*.61f,dst.left+dw*.88f,dst.top+dh*.78f)
  drawBed(c);drawPeople(c,dst,dw,dh);if(FlowerSequence.stage(ms)==FlowerSequence.Stage.CHOOSE&&chosen==null)drawPicker(c)
 }
 private fun drawBed(c:Canvas){val prep=FlowerSequence.prepare(ms);p.color=Color.rgb((96-28*prep).toInt(),(70-24*prep).toInt(),(43-12*prep).toInt());c.drawRoundRect(bed,dp(24f),dp(24f),p)
  val count=(FlowerSequence.planting(ms)*12).toInt().coerceIn(0,12);val bloom=FlowerSequence.bloom(ms);for(i in 0 until count){val col=i%6;val row=i/6;val x=bed.left+bed.width()*(.10f+col*.16f);val y=bed.top+bed.height()*(.42f+row*.36f);drawPlant(c,x,y,bloom,i)}
  val water=FlowerSequence.watering(ms);if(water>0){p.color=0xaa8be9ff.toInt();for(i in 0..16){val t=(life*3+i*.071f)%1f;val x=bed.left+bed.width()*(.12f+.76f*t);val y=bed.top-bed.height()*.15f+t*bed.height()*.55f;c.drawCircle(x,y,dp(2.4f),p)}}
 }
 private fun drawPlant(c:Canvas,x:Float,y:Float,bloom:Float,i:Int){val h=dp(9f+20f*bloom);p.strokeWidth=dp(3f);p.color=0xff3e9b42.toInt();c.drawLine(x,y,x,y-h,p);if(bloom>.15f){val r=dp(3f+7f*bloom);p.color=chosen?.color?:0xff9a78df.toInt();for(k in 0..4){val a=(Math.PI*2*k/5).toFloat();c.drawCircle(x+kotlin.math.cos(a)*r*.65f,y-h+kotlin.math.sin(a)*r*.65f,r*.52f,p)};p.color=0xffffd45a.toInt();c.drawCircle(x,y-h,r*.35f,p)} }
 private fun drawPeople(c:Canvas,dst:RectF,dw:Float,dh:Float){val stage=FlowerSequence.stage(ms);val points=arrayOf(.34f to .61f,.67f to .61f,.24f to .68f,.78f to .68f);for(id in 0..3){val approach=FlowerSequence.approach(ms);val sx=if(id%2==0).08f else .92f;val x=dst.left+dw*(sx+(points[id].first-sx)*approach);val y=dst.top+dh*(.83f+(points[id].second-.83f)*approach);val state=when(stage){FlowerSequence.Stage.GATHER->ResidentAnimation.State.WALK;FlowerSequence.Stage.TALK->if(id==0||id==2)ResidentAnimation.State.TALK else ResidentAnimation.State.LOOK;FlowerSequence.Stage.PREPARE,FlowerSequence.Stage.PLANT,FlowerSequence.Stage.WATER->ResidentAnimation.State.WORK;FlowerSequence.Stage.BLOOM->ResidentAnimation.State.HAPPY;FlowerSequence.Stage.CELEBRATE->ResidentAnimation.State.CELEBRATE;else->ResidentAnimation.State.IDLE};drawResident(c,id,x,y,dw*.16f,ResidentAnimation.frame(state,(life*90).toInt()+id),x<dst.centerX())}
  val text=FlowerSequence.dialogue(ms,language);if(text.isNotBlank())bubble(c,width*.5f,height*.38f,if(FlowerSequence.speaker(ms)=="merjem")"Merjem" else "Meli",text)
 }
 private fun drawResident(c:Canvas,id:Int,x:Float,y:Float,w:Float,frame:Int,right:Boolean){val cw=residents.width/8;val ch=residents.height/4;val src=Rect(frame*cw,id*ch,(frame+1)*cw,(id+1)*ch);val h=w*ch/cw;val save=c.save();c.scale(if(right)1f else -1f,1f,x,y);c.drawBitmap(residents,src,RectF(x-w/2,y-h,x+w/2,y),p);c.restoreToCount(save)}
 private fun drawPicker(c:Canvas){val box=RectF(dp(18f),height*.24f,width-dp(18f),height*.58f);p.color=0xf5fff5dc.toInt();c.drawRoundRect(box,dp(20f),dp(20f),p);p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.DEFAULT_BOLD;p.textSize=dp(18f);p.color=0xff5a3923.toInt();c.drawText("Zgjidh lulet",box.centerX(),box.top+dp(32f),p);FlowerSequence.choices.forEachIndexed{i,f->val w=box.width()/3;val cx=box.left+w*(i+.5f);p.color=f.color;c.drawCircle(cx,box.centerY(),dp(25f),p);p.color=0xff4b3422.toInt();p.textSize=dp(12f);c.drawText(localName(f),cx,box.bottom-dp(22f),p)}}
 override fun onTouchEvent(e:MotionEvent):Boolean{if(e.action==MotionEvent.ACTION_UP&&FlowerSequence.stage(ms)==FlowerSequence.Stage.CHOOSE&&chosen==null){val boxLeft=dp(18f);val boxRight=width-dp(18f);if(e.x in boxLeft..boxRight&&e.y in height*.24f..height*.58f){val i=(((e.x-boxLeft)/(boxRight-boxLeft))*3).toInt().coerceIn(0,2);chosen=FlowerSequence.choices[i];onChoice(chosen!!);ms=12000L;invalidate();performClick();return true}}return true}
 override fun performClick():Boolean{super.performClick();return true}
 private fun localName(f:FlowerSequence.FlowerChoice)=when(language){"de"->f.nameDe;"it"->f.nameIt;"mk"->f.nameMk;"tr"->f.nameTr;else->f.nameSq}
 private fun bubble(c:Canvas,x:Float,y:Float,name:String,text:String){val r=RectF(dp(18f),y-dp(64f),width-dp(18f),y);p.color=0xf7fff9e9.toInt();c.drawRoundRect(r,dp(16f),dp(16f),p);p.textAlign=Paint.Align.LEFT;p.typeface=Typeface.DEFAULT_BOLD;p.textSize=dp(12f);p.color=0xff2f7d45.toInt();c.drawText(name,r.left+dp(14f),r.top+dp(21f),p);p.typeface=Typeface.DEFAULT;p.textSize=dp(11f);p.color=0xff4c3927.toInt();c.drawText(text.take(55),r.left+dp(14f),r.top+dp(45f),p)}
 private fun dp(v:Float)=v*resources.displayMetrics.density
}
