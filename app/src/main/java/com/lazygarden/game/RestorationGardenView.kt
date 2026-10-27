package com.lazygarden.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.*

/** Shared rendering machinery; work plans, actors' routes and object transformations differ per scene. */
open class RestorationGardenView(context:Context,private val task:Int,initialElapsed:Long=0,private val checkpoint:(Long)->Unit={},private val onComplete:()->Unit={}):View(context),SceneLifecycle {
 private val plan=RestorationSequence.plan(task)
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 private val art=GameSkin.bitmap(context,R.drawable.restoration_objects)
 private val props=GameSkin.bitmap(context,R.drawable.garden_work_props)
 private val workResidents=GameSkin.bitmap(context,R.drawable.resident_work_atlas)
 private val residents=GameSkin.bitmap(context,R.drawable.resident_atlas)
 private val background=GameSkin.bitmap(context,R.drawable.garden_before)
 private val after=GameSkin.bitmap(context,R.drawable.garden_estate)
 private val save=GardenSave(context)
 private val voice=GardenVoice(context){save.voiceEnabled}
 private val sfx=GardenSfx(context)
 private var ms=initialElapsed.coerceIn(0,plan.duration);private var clock:ValueAnimator?=null
 private var completed=false;private var paused=false;private var lastStep=-1;private var lastFoley=0L
 internal val sceneElapsedMillis get()=ms
 override fun onAttachedToWindow(){super.onAttachedToWindow();resumeScene()}
 internal fun dispose(){pauseScene();voice.release();sfx.release()}
 override fun onDetachedFromWindow(){dispose();super.onDetachedFromWindow()}
 override fun onWindowVisibilityChanged(v:Int){super.onWindowVisibilityChanged(v);if(v==VISIBLE&&!paused)startClock() else stopClock()}
 override fun pauseScene(){paused=true;stopClock()}
 override fun resumeScene(){paused=false;startClock()}
 private fun stopClock(){checkpoint(ms);voice.stop();sfx.pause();clock?.removeAllUpdateListeners();clock?.cancel();clock=null}
 private fun startClock(){if(clock!=null||!isAttachedToWindow||windowVisibility!=VISIBLE||paused||completed)return
  var last=android.os.SystemClock.uptimeMillis();clock=ValueAnimator.ofFloat(0f,1f).apply{duration=12000;repeatCount=ValueAnimator.INFINITE;interpolator=LinearInterpolator();addUpdateListener{
   val now=android.os.SystemClock.uptimeMillis();ms=(ms+(now-last).coerceIn(0,500)).coerceAtMost(plan.duration);last=now
   val i=plan.index(ms);val step=plan.steps[i]
   if(i!=lastStep){lastStep=i;if(step.line.isNotBlank())voice.speak(SceneText.line(step.line,save.language),save.language,role(step.speaker),"$task-$i");if(save.sfxEnabled)sfx.play(step.sound)}
   if(ms-lastFoley>1100&&step.sound.isNotBlank()){lastFoley=ms;if(save.sfxEnabled&&step.key !in listOf("CELEBRATE","REVEAL","LIGHT"))sfx.play(step.sound)}
   if(ms>=plan.duration&&!completed){completed=true;checkpoint(ms);post{if(isAttachedToWindow)onComplete()}}
   invalidate()
  };start()}
 }
 private fun role(id:Int)=when(id){0->GardenVoice.Role.MAN;1->GardenVoice.Role.BOY;2->GardenVoice.Role.WOMAN;else->GardenVoice.Role.GIRL}
 override fun onDraw(c:Canvas){super.onDraw(c);val scale=max(width.toFloat()/background.width,height.toFloat()/background.height);val w=background.width*scale;val h=background.height*scale
  val camera=(ms/4000f).coerceIn(0f,1f)*(1-((ms-plan.duration+3000)/3000f).coerceIn(0f,1f));val zoom=1+camera*.15f;val d=RectF((width-w*zoom)/2,(height-h*zoom)/2,(width+w*zoom)/2,(height+h*zoom)/2)
  // Retain preceding work without displaying unearned future restorations.
  c.drawBitmap(background,null,d,p);val clip=c.save();val areas=arrayOf(RectF(.30f,.42f,.81f,.64f),RectF(.12f,.62f,.91f,1f),RectF(.75f,.31f,1f,.99f),RectF(0f,.46f,.32f,.65f),RectF(0f,.28f,.36f,.48f),RectF(.34f,.36f,.79f,.46f))
  for(i in 0 until task.coerceAtMost(6)){val a=areas[if(i==1)2 else if(i==2)1 else i];val s=c.save();c.clipRect(d.left+a.left*d.width(),d.top+a.top*d.height(),d.left+a.right*d.width(),d.top+a.bottom*d.height());c.drawBitmap(after,null,d,p);c.restoreToCount(s)};c.restoreToCount(clip)
  WorldRestorationArt.draw(context,c,d,task,ms/12000f)
  val step=plan.steps[plan.index(ms)];val f=plan.fraction(ms)
  if(task==2){WorldRestorationArt.path(context,c,p,d,plan.progress(ms,"REMOVE"),plan.progress(ms,"PLACE"));tool(c,d,8,.73f,.83f,.18f)}else{
   val row=task-3;val r=when(task){3->WorldRestorationArt.rect(d,.50f,.65f,.57f,.29f);4->WorldRestorationArt.rect(d,.52f,.67f,.66f,.38f);5->WorldRestorationArt.rect(d,.52f,.59f,.69f,.44f);else->WorldRestorationArt.rect(d,.51f,.59f,.88f,.52f)}
   val repaired=if(task==4)plan.progress(ms,"PLANT") else plan.progress(ms,"REPAIR")
   val finished=when(task){3,5,6->plan.progress(ms,"PAINT");else->plan.progress(ms,"GROW")}
   SpriteArt.draw(c,p,art,3,4,row*3,r);reveal(c,r,repaired){SpriteArt.draw(c,p,art,3,4,row*3+1,r)};reveal(c,r,finished){SpriteArt.draw(c,p,art,3,4,row*3+2,r)}
   if(task==6&&step.key in listOf("SCAFFOLD","CLEAN","REPAIR","PLASTER","PAINT","WINDOWS","DECORATE"))tool(c,d,10,.19f,.63f,.23f)
   if(task==5&&step.key in listOf("LIGHT","REVEAL","CELEBRATE")){p.shader=RadialGradient(r.centerX(),r.top+r.height()*.25f,r.width()*.45f,intArrayOf(0x55ffe9a0,0x00ffe9a0),null,Shader.TileMode.CLAMP);c.drawOval(r,p);p.shader=null}
  }
  val work=step.key !in listOf("ARRIVAL","INSPECT","TRY","REVEAL","CELEBRATE")
  if(work){tool(c,d,if(task==2)7 else 9,.27f,.80f,.19f);particles(c,d,step.sound,f)}
  if(step.key in listOf("WATER","GROW")){tool(c,d,6,.69f,.65f,.16f);for(i in 0..15){val t=((ms%1100)/1100f+i*.07f)%1f;p.color=0xbb9ce9ff.toInt();c.drawCircle(d.left+d.width()*(.59f-t*.22f),d.top+d.height()*(.53f+t*.19f),d.width()*.0025f,p)}}
  actors(c,d,step,work)
  if(step.key=="CELEBRATE"){for(i in 0..27){val t=((ms%2300)/2300f+i*.037f)%1f;p.color=if(i%2==0)0xffffd86b.toInt() else 0xffb1efc6.toInt();p.alpha=((1-t)*210).toInt();c.drawCircle(width*(i*0.173f%1),height*(.25f+t*.60f),dp(2f),p)};p.alpha=255}
  if(step.line.isNotBlank())SceneBubble.draw(c,p,width,height,SceneText.name(step.speaker),SceneText.line(step.line,save.language),dp(1f))
  val bar=RectF(width*.23f,height*.91f,width*.77f,height*.925f);p.color=0xcc184736.toInt();c.drawRoundRect(bar,dp(6f),dp(6f),p);p.color=0xffa8df61.toInt();c.drawRoundRect(RectF(bar.left,bar.top,bar.left+bar.width()*ms/plan.duration,bar.bottom),dp(6f),dp(6f),p)
 }
 private inline fun reveal(c:Canvas,r:RectF,f:Float,draw:()->Unit){if(f<=0)return;val s=c.save();c.clipRect(r.left,r.top,r.left+r.width()*f,r.bottom);draw();c.restoreToCount(s)}
 private fun tool(c:Canvas,d:RectF,i:Int,x:Float,y:Float,w:Float){SpriteArt.draw(c,p,props,4,3,i,WorldRestorationArt.rect(d,x,y,w,w*.65f))}
 private fun particles(c:Canvas,d:RectF,sound:String,f:Float){for(i in 0..11){val t=((ms%1200)/1200f+i*.083f)%1f;p.color=if(sound=="water")0xff8cdeff.toInt() else 0xffd7bd87.toInt();p.alpha=((1-t)*150).toInt();c.drawCircle(d.centerX()+sin(i*4f)*d.width()*.22f*t,d.top+d.height()*(.67f-t*.09f),d.width()*.004f*(1-t),p)};p.alpha=255}
 private fun actors(c:Canvas,d:RectF,step:RestorationSequence.Step,work:Boolean){for(id in 0..3){val arrive=plan.progress(ms,"ARRIVAL");val baseX=when(task){2->floatArrayOf(.30f,.68f,.20f,.80f);4->floatArrayOf(.24f,.72f,.38f,.82f);6->floatArrayOf(.20f,.78f,.34f,.66f);else->floatArrayOf(.27f,.74f,.19f,.82f)}[id]
   val move=if(work)sin(ms/2200f+id)*.025f else 0f;var x=d.left+d.width()*((if(id%2==0).06f else .94f)*(1-arrive)+(baseX+move)*arrive);var y=d.top+d.height()*(.88f+((.72f+id%2*.065f)-.88f)*arrive)
   if(step.key=="TRY"&&id==3){x=d.centerX()+d.width()*.07f;y=d.top+d.height()*.61f}
   val state=when{step.key=="ARRIVAL"->ResidentAnimation.State.WALK;step.key=="CELEBRATE"->ResidentAnimation.State.CELEBRATE;step.speaker==id->ResidentAnimation.State.TALK;work&&(id<2||task==4||task==6)->ResidentAnimation.State.WORK;step.key=="REVEAL"->ResidentAnimation.State.HAPPY;else->ResidentAnimation.State.LOOK}
   val w=d.width()*.16f;val frame=ResidentAnimation.frame(state,(ms/130).toInt()+id);val cw=residents.width/8;val ch=residents.height/4;val s=c.save();c.scale(if(x<d.centerX())1f else -1f,1f,x,y);if(state==ResidentAnimation.State.CELEBRATE)y-=abs(sin(ms/140f+id))*d.height()*.01f
   val workFrame=when{step.key=="TRY"&&id==3->7;state!=ResidentAnimation.State.WORK->-1;step.key=="PAINT"->4+((ms/320+id)%2).toInt();step.key=="WATER"->6;task==4&&step.key=="PLANT"->2+((ms/340+id)%2).toInt();step.key in listOf("WEED","SOIL","LEVEL","CLEAR","PLASTER")->((ms/300+id)%2).toInt();else->-1}
   if(workFrame>=0)SpriteArt.draw(c,p,workResidents,8,4,id*8+workFrame,RectF(x-w*.63f,y-w*1.26f,x+w*.63f,y)) else SpriteArt.draw(c,p,residents,8,4,id*8+frame,RectF(x-w/2,y-w*ch/cw,x+w/2,y));c.restoreToCount(s)
  }}
 private fun dp(v:Float)=v*resources.displayMetrics.density
}
object SceneBubble {
 fun draw(c:Canvas,p:Paint,w:Int,h:Int,name:String,line:String,d:Float){val r=RectF(18*d,h*.19f,w-18*d,h*.19f+94*d);p.color=0xf9fff7df.toInt();p.style=Paint.Style.FILL;c.drawRoundRect(r,18*d,18*d,p);p.textAlign=Paint.Align.LEFT;p.typeface=Typeface.DEFAULT_BOLD;p.textSize=14*d;p.color=0xff26724a.toInt();c.drawText(name,r.left+14*d,r.top+23*d,p);p.typeface=Typeface.DEFAULT;p.textSize=13*d;p.color=0xff543b25.toInt();val words=line.split(' ');var row="";var y=r.top+46*d;for(word in words){if(p.measureText("$row $word")>r.width()-28*d){c.drawText(row.trim(),r.left+14*d,y,p);y+=17*d;row=word}else row+=" $word"};c.drawText(row.trim(),r.left+14*d,y,p)}
}
