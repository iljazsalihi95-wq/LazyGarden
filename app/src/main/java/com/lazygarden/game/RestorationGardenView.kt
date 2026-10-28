package com.lazygarden.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import kotlin.math.*

/** Shared rendering machinery; work plans, actors' routes and object transformations differ per scene. */
open class RestorationGardenView(context:Context,private val task:Int,initialElapsed:Long=0,private val checkpoint:(Long)->Unit={},private val onComplete:()->Unit={}):View(context),SceneLifecycle {
 private val plan=RestorationSequence.plan(task)
 private val workTarget=RectF()
 private val toolSlots=Array(3){RectF()}
 private val materialArt=MaterialArt(context)
 private var selectedTool:Material?=null
 private var wrongToolAt=-3000L
 private var lastWorker=""
 private var workerArrival=0L
 private var contactX=0f;private var contactY=0f;private var contactAt=-2000L
 init {isClickable=true;contentDescription=plan.title}
 override fun performClick():Boolean {super.performClick();return true}
 override fun onTouchEvent(event:MotionEvent):Boolean {
  if(event.actionMasked==MotionEvent.ACTION_DOWN)return true
  if(event.actionMasked!=MotionEvent.ACTION_UP)return true
  performClick()
  if(paused||completed)return true
  val step=plan.steps[plan.index(ms)]
  val choice=toolSlots.indexOfFirst{it.contains(event.x,event.y)}
  if(choice>=0){selectedTool=RestorationJobs.choices(task,step.key).getOrNull(choice)
   if(selectedTool!=RestorationJobs.tool(task,step.key))wrongToolAt=ms
   invalidate();return true}
  if(!workTarget.contains(event.x,event.y))return true
  if(selectedTool!=null&&selectedTool!=RestorationJobs.tool(task,step.key)){wrongToolAt=ms;invalidate();return true}
  if(step.key in listOf("ARRIVAL","INSPECT","TRY","REVEAL","CELEBRATE"))return true
  contactX=event.x;contactY=event.y;contactAt=ms
  if(save.sfxEnabled)sfx.play(step.sound)
  // Contribute work without skipping the next action or its dialogue.
  val end=plan.steps.take(plan.index(ms)+1).sumOf{it.duration}
  ms=(ms+180).coerceAtMost(end-1)
  checkpoint(ms);invalidate();return true
 }
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 private val art=GameSkin.bitmap(context,R.drawable.restoration_objects)
 private val props=GameSkin.bitmap(context,R.drawable.garden_work_props)
 private val workResidents=GameSkin.bitmap(context,R.drawable.resident_work_atlas)
 private val residents=GameSkin.bitmap(context,R.drawable.resident_atlas)
 private val backdrop=GardenBackdrop(context,task)
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
   val now=android.os.SystemClock.uptimeMillis()
   val current=plan.steps[plan.index(ms)]
   if(selectedTool==null||selectedTool==RestorationJobs.tool(task,current.key))ms=(ms+(now-last).coerceIn(0,500)).coerceAtMost(plan.duration)
   last=now
   val i=plan.index(ms);val step=plan.steps[i]
   if(i!=lastStep){lastStep=i;selectedTool=null
    val worker=RestorationJobs.worker(task,step.key).id
    if(worker!=lastWorker){lastWorker=worker;workerArrival=ms}
    if(step.line.isNotBlank())voice.speakLine("restoration_$task",step.key.lowercase(),if(step.speaker==1)RestorationJobs.worker(task,step.key).id else listOf("lazi","greku","meli","rona")[step.speaker],save.language,SceneText.line(step.line,save.language),step.duration);if(save.sfxEnabled)sfx.play(step.sound)}
   if(ms-lastFoley>1100&&step.sound.isNotBlank()){lastFoley=ms;if(save.sfxEnabled&&step.key !in listOf("CELEBRATE","REVEAL","LIGHT"))sfx.play(step.sound)}
   if(ms>=plan.duration&&!completed){completed=true;checkpoint(ms);post{if(isAttachedToWindow)onComplete()}}
   invalidate()
  };start()}
 }
 override fun onDraw(c:Canvas){super.onDraw(c);val scale=max(width.toFloat()/background.width,height.toFloat()/background.height);val w=background.width*scale;val h=background.height*scale
  val camera=(ms/4000f).coerceIn(0f,1f)*(1-((ms-plan.duration+3000)/3000f).coerceIn(0f,1f));val maxZoom=when(task){3->2f;4->1.5f;5->1.35f;6->1.15f;else->1.25f};val zoom=1+camera*(maxZoom-1);val d=RectF((width-w*zoom)/2,(height-h*zoom)/2,(width+w*zoom)/2,(height+h*zoom)/2)
  val anchor=WorldRestorationArt.objectRect(d,task);d.offset((width*.5f-anchor.centerX())*camera,(height*.64f-anchor.bottom)*camera)
  d.offset(d.left.coerceIn((width-d.width()).coerceAtMost(0f),0f)-d.left,d.top.coerceIn((height-d.height()).coerceAtMost(0f),0f)-d.top)
  backdrop.draw(c,d);WorldRestorationArt.water(c,p,d,ms.toFloat())
  WorldRestorationArt.draw(context,c,d,task,ms/12000f)
  val step=plan.steps[plan.index(ms)];val f=plan.fraction(ms)
  if(task==2){WorldRestorationArt.path(context,c,p,d,plan.progress(ms,"REMOVE"),plan.progress(ms,"PLACE"));WorldRestorationArt.draw(context,c,d,task,ms/12000f);tool(c,d,8,.73f,.83f,.18f)}else{
   val row=task-3;val r=WorldRestorationArt.objectRect(d,task)
   if(task==3&&plan.progress(ms,"REMOVE")>0){val clean=c.save();c.clipRect(d.left,d.top+d.height()*.44f,d.left+d.width()*.34f,d.top+d.height()*.68f);p.alpha=(255*plan.progress(ms,"REMOVE")).toInt();c.drawBitmap(after,null,d,p);p.alpha=255;c.restoreToCount(clean)}
   val repaired=if(task==4)plan.progress(ms,"PLANT") else plan.progress(ms,"REPAIR")
   val finished=when(task){3->.6f*plan.progress(ms,"PAINT")+.3f*plan.progress(ms,"ASSEMBLE")+.1f*plan.progress(ms,"CLEAN");5->.65f*plan.progress(ms,"PAINT")+.2f*plan.progress(ms,"DECORATE")+.15f*plan.progress(ms,"LIGHT");6->.55f*plan.progress(ms,"PAINT")+.2f*plan.progress(ms,"WINDOWS")+.15f*plan.progress(ms,"DECORATE")+.1f*plan.progress(ms,"LIGHT");else->plan.progress(ms,"GROW")}
   if(task==6){
    // Restore the actual manor in the estate illustration; avoid placing a second toy house in front of it.
    val roof=RectF(d.left+d.width()*.30f,d.top,d.right,d.top+d.height()*.20f)
    val roofLayer=c.save();c.clipRect(roof.left,roof.top,roof.left+roof.width()*plan.progress(ms,"ROOF"),roof.bottom);c.drawBitmap(after,null,d,p);c.restoreToCount(roofLayer)
    val house=RectF(d.left+d.width()*.30f,d.top,d.right,d.top+d.height()*.43f)
    val s=c.save();c.clipRect(house.left,house.top,house.left+house.width()*finished,house.bottom);c.drawBitmap(after,null,d,p);c.restoreToCount(s)
   }else{RestorationObjectArt.draw(c,p,art,row*3,r);reveal(c,r,repaired){RestorationObjectArt.draw(c,p,art,row*3+1,r)};reveal(c,r,finished){RestorationObjectArt.draw(c,p,art,row*3+2,r)}}
   if(task==6&&step.key !in listOf("ARRIVAL","INSPECT","CELEBRATE"))SpriteArt.draw(c,p,props,4,3,10,RectF(d.left+d.width()*.28f,d.top+d.height()*.14f,d.left+d.width()*.43f,d.top+d.height()*.60f),(255*(1-plan.progress(ms,"REVEAL"))).toInt())
   if(task==5&&step.key in listOf("LIGHT","REVEAL","CELEBRATE")){p.shader=RadialGradient(r.centerX(),r.top+r.height()*.25f,r.width()*.45f,intArrayOf(0x55ffe9a0,0x00ffe9a0),null,Shader.TileMode.CLAMP);c.drawOval(r,p);p.shader=null}
  }
  workTarget.set(WorldRestorationArt.objectRect(d,task));workTarget.inset(-dp(24f),-dp(24f))
  val work=step.key !in listOf("ARRIVAL","INSPECT","TRY","REVEAL","CELEBRATE")
  if(work){tool(c,d,if(task==2)7 else 9,.27f,.80f,.19f);particles(c,d,step.sound,f)}
  if(step.key in listOf("WATER","GROW")){tool(c,d,6,.69f,.65f,.16f);for(i in 0..15){val t=((ms%1100)/1100f+i*.07f)%1f;p.color=0xbb9ce9ff.toInt();c.drawCircle(width*(.59f-t*.22f),height*(.53f+t*.19f),d.width()*.0025f,p)}}
  actors(c,d,WorldRestorationArt.objectRect(d,task),step,work)
  drawWorkTools(c,step,work)
  val contact=(ms-contactAt)/650f
  if(contact in 0f..1f){
   p.style=Paint.Style.STROKE;p.strokeWidth=dp(2f);p.color=0xffffd47a.toInt();p.alpha=((1-contact)*230).toInt()
   c.drawCircle(contactX,contactY,dp(8f)+contact*dp(32f),p);p.style=Paint.Style.FILL
   for(i in 0..9){val angle=i*PI.toFloat()/5;val radius=contact*dp(42f);c.drawCircle(contactX+cos(angle)*radius,contactY+sin(angle)*radius-contact*dp(18f),dp(3f)*(1-contact),p)}
   p.alpha=255
  }
  if(step.key=="CELEBRATE"){for(i in 0..27){val t=((ms%2300)/2300f+i*.037f)%1f;p.color=if(i%2==0)0xffffd86b.toInt() else 0xffb1efc6.toInt();p.alpha=((1-t)*210).toInt();c.drawCircle(width*(i*0.173f%1),height*(.25f+t*.60f),dp(2f),p)};p.alpha=255}
  if(step.line.isNotBlank())SceneBubble.draw(c,p,width,height,if(step.speaker==1)RestorationJobs.worker(task,step.key).name else listOf("Lazi","Greku","Meli","Rona").getOrElse(step.speaker){"Lazi"},SceneText.line(step.line,save.language),dp(1f),width*(if(step.speaker%2==0).26f else .74f))
  val bar=RectF(width*.23f,height*.91f,width*.77f,height*.925f);p.color=0xcc184736.toInt();c.drawRoundRect(bar,dp(6f),dp(6f),p);p.color=0xffa8df61.toInt();c.drawRoundRect(RectF(bar.left,bar.top,bar.left+bar.width()*ms/plan.duration,bar.bottom),dp(6f),dp(6f),p)
 }
 private fun drawWorkTools(c:Canvas,step:RestorationSequence.Step,work:Boolean){
  toolSlots.forEach{it.setEmpty()};if(!work)return
  val y=height*.79f;val size=min(width*.14f,dp(58f));val gap=dp(12f)
  val choices=RestorationJobs.choices(task,step.key);val cx=width*.5f
  p.shader=null;p.style=Paint.Style.FILL;p.color=0xe51b3b37.toInt()
  c.drawRoundRect(RectF(dp(14f),y-dp(34f),width-dp(14f),y+size+dp(10f)),dp(16f),dp(16f),p)
  p.color=0xffffe3a7.toInt();p.textSize=dp(12f);p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.DEFAULT_BOLD
  c.drawText(RestorationJobs.title(task,step.key,save.language),cx,y-dp(13f),p)
  choices.forEachIndexed{i,m->val x=cx+(i-1)*(size+gap);val r=toolSlots[i];r.set(x-size/2,y,x+size/2,y+size)
   p.color=if((selectedTool?:RestorationJobs.tool(task,step.key))==m)0xffd9bd72.toInt() else 0xff698d81.toInt()
   c.drawRoundRect(r,dp(10f),dp(10f),p);val inner=RectF(r);inner.inset(dp(7f),dp(7f));materialArt.draw(c,p,m,inner)
  }
 }
 private inline fun reveal(c:Canvas,r:RectF,f:Float,draw:()->Unit){if(f<=0)return;val s=c.save();c.clipRect(r.left,r.top,r.left+r.width()*f,r.bottom);draw();c.restoreToCount(s)}
 private fun tool(c:Canvas,d:RectF,i:Int,x:Float,y:Float,w:Float){val cx=width*x;val bottom=height*y;val ww=width*w;SpriteArt.draw(c,p,props,4,3,i,RectF(cx-ww/2,bottom-ww*.65f,cx+ww/2,bottom))}
 private fun particles(c:Canvas,d:RectF,sound:String,f:Float){for(i in 0..11){val t=((ms%1200)/1200f+i*.083f)%1f;p.color=if(sound=="water")0xff8cdeff.toInt() else 0xffd7bd87.toInt();p.alpha=((1-t)*150).toInt();c.drawCircle(width*.5f+sin(i*4f)*width*.22f*t,height*(.67f-t*.09f),d.width()*.004f*(1-t),p)};p.alpha=255}
 private fun actors(c:Canvas,d:RectF,target:RectF,step:RestorationSequence.Step,work:Boolean){for(id in 0..3){val arrive=CharacterAnimation.ease(plan.progress(ms,"ARRIVAL"));val offsets=floatArrayOf(-.57f,.57f,-.84f,.84f);val tx=if(task==6)width*floatArrayOf(.16f,.40f,.60f,.84f)[id] else (target.centerX()+offsets[id]*target.width()).coerceIn(width*.13f,width*.87f);val move=0f
   val ground=if(task==6)height*.88f else target.bottom+height*(if(id<2).10f else .19f)
   var x=(if(id%2==0)-width*.12f else width*1.12f)*(1-arrive)+(tx+move)*arrive;var y=height*.98f*(1-arrive)+ground.coerceAtMost(height*.88f)*arrive
   val entering=if(id==1)((ms-workerArrival)/900f).coerceIn(0f,1f) else 1f
   if(id==1&&step.key!="ARRIVAL")x=width*1.15f+(x-width*1.15f)*CharacterAnimation.ease(entering)
   if(task==6&&id==1){val platform=plan.progress(ms,"SCAFFOLD")*(1-plan.progress(ms,"REVEAL"));val px=d.left+d.width()*(if(id==0).37f else .34f);val py=d.top+d.height()*(if(id==0).42f else .56f);x+=(px-x)*platform;y+=(py-y)*platform}
   if(step.key=="TRY"&&id==3){x=target.centerX()+target.width()*.12f;y=target.top+target.height()*.60f+width*.115f}
   val state=when{step.key=="ARRIVAL"||entering<1f->ResidentAnimation.State.WALK;step.key=="CELEBRATE"->ResidentAnimation.State.CELEBRATE;step.speaker==id->ResidentAnimation.State.TALK;work&&(id==1||id==0&&step.key in listOf("CLEAR","CLEAN","REMOVE","ASSEMBLE"))->ResidentAnimation.State.WORK;step.key=="REVEAL"->ResidentAnimation.State.HAPPY;else->ResidentAnimation.State.LOOK}
   p.shader=null;p.alpha=255;p.color=0x44302010
   c.drawOval(RectF(x-width*.065f,y-width*.012f,x+width*.065f,y+width*.012f),p)
   val mood=when{
    ms-wrongToolAt in 0..1400->ActorGesture.Mood.SAD
    step.key=="INSPECT"->if(id==1)ActorGesture.Mood.ANALYSE else ActorGesture.Mood.SURPRISE
    step.key=="CELEBRATE"||step.key=="REVEAL"->ActorGesture.Mood.JOY
    state==ResidentAnimation.State.WORK->ActorGesture.Mood.WORK
    else->ActorGesture.Mood.THINK
   }
   val w=width*.23f;val frame=ResidentAnimation.frame(state,(ms/130).toInt()+id);val cw=residents.width/8;val ch=residents.height/4;val s=c.save();c.rotate(ActorGesture.lean(mood,ms),x,y);c.scale(if(x<target.centerX())1f else -1f,1f,x,y);if(state==ResidentAnimation.State.CELEBRATE)y-=abs(sin(ms/140f+id))*d.height()*.01f
   val workFrame=when{step.key=="TRY"&&id==3->7;state!=ResidentAnimation.State.WORK->-1;step.key=="PAINT"->4+((ms/320+id)%2).toInt();step.key=="WATER"->6;task==4&&step.key=="PLANT"->2+((ms/340+id)%2).toInt();step.key in listOf("WEED","SOIL","LEVEL","CLEAR","PLASTER")->((ms/300+id)%2).toInt();else->-1}
   if(workFrame>=0)WorkerArt.draw(c,p,workResidents,id,workFrame,RectF(x-w*.63f,y-w*1.26f,x+w*.63f,y)) else SpriteArt.draw(c,p,residents,8,4,id*8+frame,RectF(x-w/2,y-w*ch/cw,x+w/2,y));c.restoreToCount(s)
   if(id==1&&state==ResidentAnimation.State.WORK){
    val hx=x+if(x<target.centerX())w*.28f else -w*.28f;val hy=y-w*.55f
    val held=c.save();c.rotate(sin(ms/160f)*18f,hx,hy)
    materialArt.draw(c,p,selectedTool?:RestorationJobs.tool(task,step.key),RectF(hx-w*.16f,hy-w*.20f,hx+w*.16f,hy+w*.12f));c.restoreToCount(held)
   }
   ActorGesture.draw(c,p,mood,x,y-w*1.38f,w,ms)
   if(id==1){p.color=0xffffe3a7.toInt();p.textSize=dp(11f);p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.DEFAULT_BOLD
    c.drawText(RestorationJobs.worker(task,step.key).name,x,y+dp(16f),p)}
  }}
 private fun dp(v:Float)=v*resources.displayMetrics.density
}
object SceneBubble {
 fun draw(c:Canvas,p:Paint,w:Int,h:Int,name:String,line:String,d:Float,tailX:Float=w*.5f){
  val left=18*d;val right=w-18*d;p.typeface=Typeface.DEFAULT;p.textSize=13*d;val lines=mutableListOf<String>();var row=""
  for(word in line.split(' ')){if(row.isNotBlank()&&p.measureText("$row $word")>right-left-28*d){lines.add(row);row=word}else row=if(row.isBlank())word else "$row $word"};if(row.isNotBlank())lines.add(row)
  val r=RectF(left,h*.19f,right,h*.19f+(60+(lines.size-1).coerceAtLeast(0)*17)*d);p.color=0xf9fff7df.toInt();p.style=Paint.Style.FILL;c.drawRoundRect(r,18*d,18*d,p)
  val tx=tailX.coerceIn(r.left+24*d,r.right-24*d);c.drawPath(Path().apply{moveTo(tx-8*d,r.bottom-d);lineTo(tx+8*d,r.bottom-d);lineTo(tx+5*d,r.bottom+12*d);close()},p)
  p.textAlign=Paint.Align.LEFT;p.typeface=Typeface.DEFAULT_BOLD;p.textSize=14*d;p.color=0xff26724a.toInt();c.drawText(name,r.left+14*d,r.top+23*d,p)
  p.typeface=Typeface.DEFAULT;p.textSize=13*d;p.color=0xff543b25.toInt();lines.forEachIndexed{i,text->c.drawText(text,r.left+14*d,r.top+(46+i*17)*d,p)}
 }
}
