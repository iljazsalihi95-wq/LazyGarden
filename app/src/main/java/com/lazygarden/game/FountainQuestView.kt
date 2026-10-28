package com.lazygarden.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.*

/** Interactive native estate: taps are world actions, never a sequence of story cards. */
class FountainQuestView(context:Context,private val save:GardenSave,private val play:()->Unit,private val inventory:()->Unit,private val close:()->Unit,private val complete:()->Unit):View(context),SceneLifecycle {
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 private val before=GardenBackdrop(context,0);private val restored=GardenBackdrop(context,1)
 private val background=GameSkin.bitmap(context,R.drawable.garden_before)
 private val family=GameSkin.bitmap(context,R.drawable.resident_atlas)
 private val arrivals=GameSkin.bitmap(context,R.drawable.fountain_family_atlas)
 private val hardware=GameSkin.bitmap(context,R.drawable.fountain_worksite)
 private val leaf=GameSkin.bitmap(context,R.drawable.tile_leaf)
 private val materialArt=MaterialArt(context)
 private val sfx=GardenSfx(context)
 private val voice=GardenVoice(context){save.voiceEnabled}
 private var clock:ValueAnimator?=null;private var paused=false
 private var stage=save.fountainQuestStage;private var elapsed=save.fountainQuestElapsed
 private var checkpoint=elapsed;private var life=0L;private var rewardElapsed=-1L
 private var rewardCoins=0;private var rewardStars=0;private var rewardXp=0
 private var lastSpoken:FountainText.Cue?=null
 private var lastWork=0L
 private var collectedLeaf=-1;private var collectStarted=-1000L
 private val actionRect=RectF();private val playRect=RectF();private val inventoryRect=RectF();private val closeRect=RectF()
 private val worldTarget=RectF();private val leaves=Array(3){RectF()}
 private val players=mutableMapOf<String,CharacterAnimation.Player>()
 private val actorFacing=mutableMapOf<String,Boolean>()
 private val pumpRect=RectF();private val phoneRect=RectF()
 private val actorRects=mutableMapOf<String,RectF>()
 private val d=resources.displayMetrics.density
 internal fun actorState(id:String)=players[id]?.state
 internal fun actorGround(id:String)=actorRects[id]?.let{PointF(it.centerX(),it.bottom)}
 internal fun actorFacesRight(id:String)=actorFacing[id]
 internal fun dispose(){stopClock();voice.release();sfx.release()}
 internal fun leafCenter(index:Int)=PointF(leaves[index].centerX(),leaves[index].centerY())
 internal var onActorRendered:((String,CharacterAnimation.State,Int,Boolean)->Unit)?=null
 var onStageChanged:((FountainQuest.Stage)->Unit)?=null
 internal val currentStage get()=stage
 internal fun actionCenter()=PointF(actionRect.centerX(),actionRect.centerY())
 init {voice.onTalkingChanged={invalidate()};isClickable=true;contentDescription=FountainText.objective(save.language,stage)}
 override fun onAttachedToWindow(){super.onAttachedToWindow();startClock()}
 override fun onDetachedFromWindow(){stopClock();voice.release();sfx.release();super.onDetachedFromWindow()}
 override fun onWindowVisibilityChanged(v:Int){super.onWindowVisibilityChanged(v);if(v==VISIBLE&&!paused)startClock() else stopClock()}
 override fun pauseScene(){paused=true;stopClock()}
 override fun resumeScene(){paused=false;startClock()}
 private fun stopClock(){if(rewardElapsed<0)save.checkpointFountain(stage,elapsed);clock?.removeAllUpdateListeners();clock?.cancel();clock=null;voice.stop();sfx.pause();lastSpoken=null}
 private fun startClock(){if(paused||clock!=null||!isAttachedToWindow||windowVisibility!=VISIBLE)return
  var last=android.os.SystemClock.uptimeMillis()
  clock=ValueAnimator.ofFloat(0f,1f).apply{duration=12000;repeatCount=ValueAnimator.INFINITE;interpolator=LinearInterpolator();addUpdateListener{
   val now=android.os.SystemClock.uptimeMillis();val dt=(now-last).coerceIn(0L,1000L);last=now;life+=dt
   if(rewardElapsed>=0){val beforeReward=rewardElapsed;rewardElapsed+=dt;if(beforeReward<1100&&rewardElapsed>=1100)sound("coin");if(beforeReward<1350&&rewardElapsed>=1350)sound("star");if(rewardElapsed>=1800){rewardElapsed=-1;complete();return@addUpdateListener}}
   else if(stage.duration>0){elapsed+=dt;if(FountainQuest.canAdvance(stage,elapsed,voice.isTalking)&&save.advanceFountainTimed(stage,elapsed))refreshStage() else if(elapsed-checkpoint>=1000){save.checkpointFountain(stage,elapsed);checkpoint=elapsed}}
   val cue=FountainText.cue(stage);if(cue!=lastSpoken){lastSpoken=cue;if(cue!=null)voice.speakLine("fountain_quest",stage.name.lowercase(),cue.character,save.language,FountainText.get(save.language,cue.textKey),stage.duration)}
   if(stage==FountainQuest.Stage.REPAIR&&life-lastWork>850){lastWork=life;sound("hammer")}
   invalidate()
  };start()}
 }
 private fun sound(key:String){if(save.sfxEnabled)sfx.play(key)}
 private fun refreshStage(){
  val next=save.fountainQuestStage
  if(FountainText.cue(next)!=FountainText.cue(stage)){voice.stop();lastSpoken=null}
  stage=next;elapsed=save.fountainQuestElapsed;checkpoint=elapsed
  contentDescription=FountainText.objective(save.language,stage);onStageChanged?.invoke(stage);invalidate()
 }
 private fun text(c:Canvas,s:String,x:Float,y:Float,size:Float,color:Int=0xff543b25.toInt()){
  p.shader=null;p.colorFilter=null;p.alpha=255;p.style=Paint.Style.FILL;p.color=color;p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.DEFAULT_BOLD;p.textSize=size*d;c.drawText(s,x,y,p)
 }
 private fun panel(c:Canvas,r:RectF){GameSkin.nine(c,GameSkin.bitmap(context,R.drawable.hd_hud),r,18*d,p)}
 private fun button(c:Canvas,r:RectF,label:String,enabled:Boolean=true){
  p.alpha=if(enabled)255 else 140;GameSkin.nine(c,GameSkin.bitmap(context,if(enabled)R.drawable.hd_button_green else R.drawable.hd_button_blue),r,20*d,p,.35f);p.alpha=255
  text(c,label,r.centerX(),r.centerY()+5*d,min(16f,r.width()/d/17),Color.WHITE)
 }
 override fun onDraw(c:Canvas){super.onDraw(c);val w=width.toFloat();val h=height.toFloat();if(w==0f||h==0f)return
  val scale=max(w/background.width,h/background.height)*1.15f
  val dw=background.width*scale;val dh=background.height*scale
  val dst=RectF(w*.50f-dw*.55f,h*.51f-dh*.515f,w*.50f+dw*.45f,h*.51f+dh*.485f)
  before.draw(c,dst)
  val f=FountainQuest.fraction(stage,elapsed)
  val water=when {stage==FountainQuest.Stage.WATER_FLOW->f;stage.ordinal>FountainQuest.Stage.WATER_FLOW.ordinal->1f;else->0f}
  if(water>0){val layer=c.saveLayer(null,Paint().apply{alpha=(water*255).toInt()});restored.draw(c,dst);WorldRestorationArt.water(c,p,dst,life.toFloat());c.restoreToCount(layer)}
  val fx=w*.50f;val fy=h*.51f
  pumpRect.setEmpty();phoneRect.setEmpty()
  worldTarget.set(fx-w*.18f,fy-w*.10f,fx+w*.18f,fy+w*.22f)
  if(stage.ordinal>=FountainQuest.Stage.REXHA_INSPECT.ordinal&&stage.ordinal<=FountainQuest.Stage.START_WATER.ordinal){
   val pump=pumpRect.apply{set(fx-w*.10f,fy+w*.07f,fx+w*.25f,fy+w*.33f)}
   SpriteArt.draw(c,p,hardware,2,2,if(stage.ordinal>=FountainQuest.Stage.CLEAN.ordinal)1 else 0,pump)
   SpriteArt.draw(c,p,hardware,2,2,2,RectF(fx-w*.28f,fy+w*.18f,fx-w*.13f,fy+w*.31f))
   if(stage==FountainQuest.Stage.PUMP_DIAGNOSIS){
    val gx=pump.left+pump.width()*.47f;val gy=pump.top+pump.height()*.18f
    val angle=(-125+sin(elapsed/180f)*25)*PI.toFloat()/180
    p.color=0xffb45339.toInt();p.strokeWidth=2*d
    c.drawLine(gx,gy,gx+cos(angle)*pump.width()*.042f,gy+sin(angle)*pump.height()*.055f,p)
   }
   if(stage==FountainQuest.Stage.REPAIR){
    val layer=c.save();c.clipRect(pump.left,pump.top,pump.left+pump.width()*f,pump.bottom);SpriteArt.draw(c,p,hardware,2,2,1,pump);c.restoreToCount(layer)
    for(i in 0..12){val t=(life/700f+i*.077f)%1f;p.color=0xffe4c38b.toInt();p.alpha=((1-t)*210).toInt();c.drawCircle(pump.centerX()+sin(i*3f)*w*.12f*t,pump.centerY()-t*w*.17f,3*d*(1-t),p)};p.alpha=255
   }
  }
  drawActors(c,w,h,fx,fy,f)
  for(i in 0..2){val x=fx+w*(i-1)*.14f;val y=fy+w*(.29f+(i%2)*.07f);leaves[i].set(x-24*d,y-24*d,x+24*d,y+24*d)
   if(stage.ordinal<=FountainQuest.Stage.CLEAN.ordinal&&(save.fountainCleanedMask and (1 shl i))==0){for(k in 0..3){val lx=x+(k%2-.5f)*16*d;val ly=y+(k/2-.5f)*11*d;val l=c.save();c.rotate(k*53f,lx,ly);c.drawBitmap(leaf,null,RectF(lx-10*d,ly-10*d,lx+10*d,ly+10*d),p);c.restoreToCount(l)};if(stage==FountainQuest.Stage.CLEAN)glow(c,x,y,24*d)}
  }
  if(stage==FountainQuest.Stage.TRY_WATER){
   // A failed valve test visibly rattles the dry fountain, without granting water or materials.
   val pulse=sin(elapsed/65f)*3*d
   p.style=Paint.Style.STROKE;p.strokeWidth=2*d;p.color=0xdddc9c67.toInt()
   c.drawArc(RectF(fx-24*d+pulse,fy-9*d,fx+24*d+pulse,fy+9*d),25f,130f,false,p)
   p.style=Paint.Style.FILL
  }
  if(stage==FountainQuest.Stage.START_WATER){
   val x=fx+w*.17f;val y=fy+w*.15f;p.color=0xffbc4931.toInt();p.style=Paint.Style.STROKE;p.strokeWidth=5*d;c.drawCircle(x,y,17*d,p)
   for(i in 0..3){val a=i*PI/2;c.drawLine(x,y,x+cos(a).toFloat()*17*d,y+sin(a).toFloat()*17*d,p)};p.style=Paint.Style.FILL;glow(c,x,y,25*d)
   worldTarget.set(x-32*d,y-32*d,x+32*d,y+32*d)
  }
  // The phone is attached to Lazi's hand throughout the in-world call.
  if(stage in listOf(FountainQuest.Stage.CALL_REXHA,FountainQuest.Stage.PHONE_CALL,FountainQuest.Stage.PHONE_REPLY)){actorRects["lazi"]?.let{r->val x=r.right-8*d;val y=r.top+r.height()*.42f
   phoneRect.set(x-22*d,y-28*d,x+22*d,y+22*d)
   val rotation=c.save();c.rotate(if(stage!=FountainQuest.Stage.CALL_REXHA)-22f else 12f,x,y)
   p.color=0xff143c46.toInt();c.drawRoundRect(RectF(x-8*d,y-17*d,x+8*d,y+10*d),3*d,3*d,p);p.color=0xff8ceacf.toInt();c.drawRect(x-5*d,y-13*d,x+5*d,y+4*d,p);c.restoreToCount(rotation)
   if(stage!=FountainQuest.Stage.CALL_REXHA){for(i in 0..2){val t=(life/1100f+i*.33f)%1f;p.style=Paint.Style.STROKE;p.strokeWidth=2*d;p.color=0xffc5ffe5.toInt();p.alpha=((1-t)*220).toInt();c.drawCircle(x,y,(18+20*t)*d,p)};p.style=Paint.Style.FILL;p.alpha=255}
  }}
  drawHud(c,w,h)
  if(stage==FountainQuest.Stage.REPAIR&&elapsed<1600){
   val u=(elapsed/1600f).coerceIn(0f,1f)
   for((i,need)in save.restorationNeeds(0).withIndex()){
    val sx=w*(.15f+i*.23f);val sy=h-170*d;val ex=fx+w*.17f;val ey=fy+w*.12f
    val x=(1-u).pow(2)*sx+2*(1-u)*u*fx+u*u*ex
    val y=(1-u).pow(2)*sy+2*(1-u)*u*(fy-w*.2f)+u*u*ey
    val size=42*d*(1-.45f*u);materialArt.draw(c,p,need.material,RectF(x-size/2,y-size/2,x+size/2,y+size/2))
   }
  }
  if(stage in listOf(FountainQuest.Stage.TEST_WATER,FountainQuest.Stage.INSPECT,FountainQuest.Stage.REXHA_INSPECT))glow(c,worldTarget.centerX(),worldTarget.centerY(),worldTarget.width()*.53f)
  FountainText.cue(stage)?.let{cue->SceneBubble.draw(c,p,width,height,cue.character.replaceFirstChar{it.uppercase()},FountainText.get(save.language,cue.textKey),d,actorRects[cue.character]?.centerX()?:w*.5f)}
  if(rewardElapsed>=0)drawRewards(c,w,h)
 }
 private fun glow(c:Canvas,x:Float,y:Float,r:Float){p.shader=RadialGradient(x,y,r,intArrayOf(0x44fff3a5,0x00fff3a5),null,Shader.TileMode.CLAMP);c.drawCircle(x,y,r,p);p.shader=null;p.style=Paint.Style.STROKE;p.strokeWidth=2*d;p.color=0xffffdf78.toInt();p.alpha=(140+80*sin(life/200f)).toInt().coerceIn(0,255);c.drawOval(RectF(x-r*.6f,y-r*.2f,x+r*.6f,y+r*.2f),p);p.style=Paint.Style.FILL;p.alpha=255}
 private fun drawActors(c:Canvas,w:Float,h:Float,fx:Float,fy:Float,f:Float){
  val index=stage.ordinal
  fun progress(at:FountainQuest.Stage)=when{index<at.ordinal->0f;index==at.ordinal->f;else->1f}
  val m=progress(FountainQuest.Stage.MELI_APPROACH);val children=progress(FountainQuest.Stage.CHILDREN_APPROACH);val lazi=progress(FountainQuest.Stage.LAZI_APPROACH);val rexha=progress(FountainQuest.Stage.REXHA_ARRIVAL)
  fun route(x:Float,y:Float,tx:Float,ty:Float,t:Float)=CharacterAnimation.sampleRoute(CharacterAnimation.Position(x,y),CharacterAnimation.Position(tx,ty),t,fx)
  val mp=route(-w*.15f,h*.85f,fx-w*.32f,fy+w*.28f,m)
  val lp=route(w*.60f,h*.44f,fx-w*.10f,fy+w*.30f,lazi)
  val toWater=progress(FountainQuest.Stage.CHILDREN_WATER_APPROACH)
  val cp=route(w*1.15f,h*.88f,fx+w*.12f,fy+w*.49f,children)
  val rp=route(w*1.3f,h*.95f,fx+w*.33f,fy+w*.52f,children)
  val nearCufi=CharacterAnimation.move(cp.position,CharacterAnimation.Position(fx+w*.06f,fy+w*.22f),toWater)
  val nearRona=CharacterAnimation.move(rp.position,CharacterAnimation.Position(fx+w*.34f,fy+w*.28f),toWater)
  val plumber=route(-w*.2f,h*.91f,fx+w*.20f,fy+w*.25f,rexha)
  data class Actor(val id:String,val row:Int,val new:Boolean,val x:Float,val y:Float,val moving:Boolean,val child:Boolean=false,val routeRight:Boolean=true)
  val people=mutableListOf(
   Actor("meli",2,false,mp.position.x,mp.position.y,mp.moving,routeRight=mp.facingRight),
   Actor("lazi",0,false,lp.position.x,lp.position.y,lp.moving,routeRight=lp.facingRight),
   Actor("cufi",1,true,nearCufi.x,nearCufi.y,children<1||toWater in .001f.. .999f,true,false),
   Actor("rona",2,true,nearRona.x,nearRona.y,children<1||toWater in .001f.. .999f,true,toWater>0)
  )
  if(index>=FountainQuest.Stage.REXHA_ARRIVAL.ordinal)people.add(Actor("rexha",0,true,plumber.position.x,plumber.position.y,plumber.moving,routeRight=plumber.facingRight))
  actorRects.clear();actorFacing.clear()
  for(base in people.sortedBy{it.y}){
   if(base.id=="lazi"&&index<FountainQuest.Stage.LAZI_APPROACH.ordinal)continue
   val age=life-collectStarted
   val collector=if(collectedLeaf==1)"rona" else "cufi"
   val picking=collectedLeaf>=0&&base.id==collector&&age in 0..1600
   val travel=if(age<500)age/500f else if(age<1100)1f else (1600-age)/500f
   val a=if(picking){val t=CharacterAnimation.ease(travel);base.copy(x=base.x+(leaves[collectedLeaf].centerX()-base.x)*t,y=base.y+(leaves[collectedLeaf].bottom-base.y)*t,moving=age<500||age>1100,routeRight=if(age<500)leaves[collectedLeaf].centerX()>base.x else base.x>leaves[collectedLeaf].centerX())}else base

   val talking=voice.isTalking&&voice.currentLine?.characterId==a.id
   val happy=index>=FountainQuest.Stage.CELEBRATE.ordinal
   val work=(a.id=="rexha"&&stage in listOf(FountainQuest.Stage.REPAIR,FountainQuest.Stage.PUMP_DIAGNOSIS))||(a.id=="lazi"&&stage==FountainQuest.Stage.LAZI_INSPECT)||(a.id=="meli"&&stage==FountainQuest.Stage.TRY_WATER)
   val collecting=picking&&age in 500..1100
   val state=when{
    a.moving->CharacterAnimation.State.WALK
    talking->CharacterAnimation.State.TALK
    work->CharacterAnimation.State.WORK
    collecting->CharacterAnimation.State.COLLECT
    happy->CharacterAnimation.State.HAPPY
    (a.child&&save.fountainCleanedMask>0)||(a.id=="rexha"&&stage==FountainQuest.Stage.REXHA_INSPECT)->CharacterAnimation.State.CARRY
    stage in listOf(FountainQuest.Stage.INSPECT,FountainQuest.Stage.TEST_WATER,FountainQuest.Stage.REXHA_INSPECT)->CharacterAnimation.State.THINK
    else->CharacterAnimation.State.IDLE
   }
   val player=players.getOrPut(a.id){CharacterAnimation.Player()};player.update(state,life)
   val frame=player.frame(life,a.new)
   val speaking=FountainText.cue(stage)?.character
   val lookAt=if(a.moving){if(a.routeRight)w*2 else -w}else if(speaking!=null&&speaking!=a.id)people.firstOrNull{it.id==speaking}?.x?:fx else fx
   val facesRight=CharacterAnimation.facesRight(a.x,lookAt);actorFacing[a.id]=facesRight
   val size=w*(if(a.child).15f else .23f);val sheet=if(a.new)arrivals else family
   val rows=if(a.new)floatArrayOf(0f,280/768f,536/768f,1f) else floatArrayOf(0f,.25f,.5f,.75f,1f)
   val sw=sheet.width/8;val sh=(rows[a.row+1]-rows[a.row])*sheet.height
   val ah=size*sh/sw;val bob=if(happy)-abs(sin(life/140f+a.row))*8*d else if(a.moving)-abs(sin(life/100f))*2*d else sin(life/700f)*d
   val y=a.y+bob;val rect=RectF(a.x-size/2,y-ah,a.x+size/2,y);actorRects[a.id]=rect
   p.color=0x44302010;c.drawOval(RectF(a.x-size*.3f,y-size*.07f,a.x+size*.3f,y+size*.04f),p)
   p.alpha=255;val layer=c.save();c.scale(if(facesRight)1f else -1f,1f,a.x,y)
   c.drawBitmap(sheet,Rect(frame*sw,(rows[a.row]*sheet.height).toInt(),(frame+1)*sw,(rows[a.row+1]*sheet.height).toInt()),rect,p);c.restoreToCount(layer)
   onActorRendered?.invoke(a.id,state,frame,facesRight)
   if(a.id=="rexha"&&stage==FountainQuest.Stage.REPAIR){val x=a.x-12*d;val yy=y-ah*.28f;val tool=c.save();c.rotate(sin(life/120f)*18,x,yy);c.drawBitmap(GameSkin.bitmap(context,R.drawable.boost_hammer),null,RectF(x-18*d,yy-30*d,x+18*d,yy+6*d),p);c.restoreToCount(tool)}
  }
 }
 private fun drawHud(c:Canvas,w:Float,h:Float){
  val top=RectF(10*d,8*d,w-10*d,58*d);panel(c,top)
  closeRect.set(12*d,10*d,54*d,55*d);text(c,"‹",closeRect.centerX(),43*d,28f)
  val bounce=if(rewardElapsed in 900L..1550L)1f+.13f*abs(sin((rewardElapsed-900)/120f)) else 1f
  fun counter(value:String,x:Float,y:Float,size:Float,color:Int=0xff543b25.toInt()){val layer=c.save();c.scale(bounce,bounce,x,y);text(c,value,x,y,size,color);c.restoreToCount(layer)}
  val reward=if(rewardElapsed>=0)(rewardElapsed/1400f).coerceIn(0f,1f) else 1f
  counter("${if(rewardElapsed>=0)rewardCoins+(75*reward).toInt() else save.coins} ${GameText.get(save.language,"coins")}",w*.35f,39*d,15f)
  counter("★ ${if(rewardElapsed>=0)rewardStars+if(reward>.7f)1 else 0 else save.stars}",w*.69f,39*d,17f)
  inventoryRect.set(w-58*d,12*d,w-14*d,53*d);SpriteArt.draw(c,p,hardware,2,2,2,inventoryRect)
  val objective=FountainText.objective(save.language,stage)
  val objectiveBox=RectF(12*d,65*d,w-12*d,124*d);panel(c,objectiveBox)
  wrap(c,objective,objectiveBox,14f)
  counter("XP ${if(rewardElapsed>=0)rewardXp+(50*reward).toInt() else save.xp}",w*.83f,h-18*d,12f,Color.WHITE)
  actionRect.set(30*d,h-88*d,w-30*d,h-35*d);playRect.setEmpty()
  when(stage){
   FountainQuest.Stage.COLLECT->{
    val needs=save.restorationNeeds(0);val row=RectF(14*d,h-240*d,w-14*d,h-110*d);panel(c,row)
    for((i,need)in needs.withIndex()){val x=row.left+row.width()*(i+.5f)/needs.size;materialArt.draw(c,p,need.material,RectF(x-26*d,row.top+9*d,x+26*d,row.top+62*d));text(c,FountainText.get(save.language,need.material.name),x,row.top+77*d,9.5f);text(c,"${save.materialCount(need.material)}/${need.count}",x,row.top+98*d,15f,if(save.materialCount(need.material)>=need.count)0xff267539.toInt() else 0xff985029.toInt())}
    text(c,if(save.restorationMaterialsReady())FountainText.get(save.language,"ready") else GameText.get(save.language,"materials"),row.centerX(),row.bottom-15*d,13f)
    playRect.set(18*d,h-99*d,w*.48f,h-40*d);actionRect.set(w*.51f,h-99*d,w-18*d,h-40*d)
    button(c,playRect,GameText.get(save.language,"play")+" Match-3")
    button(c,actionRect,"120 · "+FountainText.get(save.language,"repair"),save.restorationMaterialsReady()&&save.coins>=120)
   }
   FountainQuest.Stage.CLAIM->button(c,actionRect,FountainText.get(save.language,"claim")+" · 75 ★1 XP50")
   FountainQuest.Stage.COMPLETE->Unit
   else->{if(stage.duration==0L&&stage!=FountainQuest.Stage.CLEAN)button(c,actionRect,when(stage){FountainQuest.Stage.CALL_REXHA->"☎ Rexha";else->FountainText.objective(save.language,stage).substringBefore('·').trim()})
    else if(stage.duration>0){val bar=RectF(38*d,h-68*d,w-38*d,h-53*d);p.color=0xaa173d31.toInt();c.drawRoundRect(bar,8*d,8*d,p);p.color=0xffafe06c.toInt();c.drawRoundRect(RectF(bar.left,bar.top,bar.left+bar.width()*FountainQuest.fraction(stage,elapsed),bar.bottom),8*d,8*d,p)}
   }
  }
 }
 private fun wrap(c:Canvas,value:String,box:RectF,size:Float){p.textSize=size*d;p.typeface=Typeface.DEFAULT_BOLD;val lines=mutableListOf<String>();var row="";for(word in value.split(' ')){if(row.isNotEmpty()&&p.measureText("$row $word")>box.width()-20*d){lines.add(row);row=word}else row=if(row.isEmpty())word else "$row $word"};if(row.isNotEmpty())lines.add(row);for((i,s)in lines.withIndex())text(c,s,box.centerX(),box.centerY()+(i-(lines.size-1)/2f)*18*d+5*d,size)}
 private fun drawRewards(c:Canvas,w:Float,h:Float){val t=(rewardElapsed/1700f).coerceIn(0f,1f);for(i in 0..13){val u=((t-i*.018f)/.72f).coerceIn(0f,1f);if(u<=0||u>=1)continue;val x=(1-u).pow(2)*w*.5f+2*(1-u)*u*w*(.15f+i*.05f)+u*u*w*(if(i==12).69f else if(i==13).83f else .35f);val y=(1-u).pow(2)*h*.55f+2*(1-u)*u*h*.2f+u*u*(if(i==13)h-18*d else 35*d)
  if(i==13){p.color=0xff80d9ff.toInt();c.drawCircle(x,y,14*d,p);text(c,"XP",x,y+4*d,10f,0xff174a64.toInt())}else if(i==12)c.drawBitmap(GameSkin.bitmap(context,R.drawable.reward_star),null,RectF(x-16*d,y-16*d,x+16*d,y+16*d),p)else{p.color=0xffffd65b.toInt();c.drawCircle(x,y,9*d,p);p.color=0xfffff1a4.toInt();c.drawCircle(x-2*d,y-2*d,4*d,p)}
 }}
 override fun onTouchEvent(e:MotionEvent):Boolean{if(e.actionMasked==MotionEvent.ACTION_DOWN)return true;if(e.actionMasked!=MotionEvent.ACTION_UP)return true;performClick()
  if(rewardElapsed>=0)return true
  if(closeRect.contains(e.x,e.y)){save.checkpointFountain(stage,elapsed);close();return true}
  if(inventoryRect.contains(e.x,e.y)){save.checkpointFountain(stage,elapsed);inventory();return true}
  if(stage==FountainQuest.Stage.COLLECT&&playRect.contains(e.x,e.y)){save.checkpointFountain(stage,elapsed);play();return true}
  if(stage==FountainQuest.Stage.CLEAN){val i=leaves.indexOfFirst{it.contains(e.x,e.y)};if(i>=0&&save.actFountain(FountainQuest.Action.CLEAN_LEAF,i)){collectedLeaf=i;collectStarted=life;sound("brush");refreshStage()};return true}
  val target=when(stage){
   FountainQuest.Stage.CALL_LAZI->actorRects["meli"]
   FountainQuest.Stage.CALL_REXHA->phoneRect
   FountainQuest.Stage.REXHA_INSPECT,FountainQuest.Stage.COLLECT->pumpRect
   FountainQuest.Stage.TEST_WATER,FountainQuest.Stage.INSPECT,FountainQuest.Stage.START_WATER->worldTarget
   else->null
  }
  if(!actionRect.contains(e.x,e.y)&&target?.contains(e.x,e.y)!=true)return true
  val action=when(stage){
   FountainQuest.Stage.TEST_WATER->FountainQuest.Action.TEST_WATER
   FountainQuest.Stage.CALL_LAZI->FountainQuest.Action.CALL_LAZI
   FountainQuest.Stage.INSPECT->FountainQuest.Action.INSPECT
   FountainQuest.Stage.CALL_REXHA->FountainQuest.Action.CALL_REXHA
   FountainQuest.Stage.REXHA_INSPECT->FountainQuest.Action.INSPECT_PUMP
   FountainQuest.Stage.COLLECT->FountainQuest.Action.RESTORE
   FountainQuest.Stage.START_WATER->FountainQuest.Action.START_WATER
   FountainQuest.Stage.CLAIM->FountainQuest.Action.CLAIM
   else->return true
  }
  rewardCoins=save.coins;rewardStars=save.stars;rewardXp=save.xp
  if(save.actFountain(action)){if(action==FountainQuest.Action.CLAIM){rewardElapsed=0;sound("reward")}else sound(when(action){FountainQuest.Action.START_WATER->"water";FountainQuest.Action.CALL_REXHA->"phone";else->"coin"});refreshStage()}
  else if(action==FountainQuest.Action.RESTORE)android.widget.Toast.makeText(context,FountainText.get(save.language,"collect"),android.widget.Toast.LENGTH_SHORT).show()
  return true
 }
 override fun performClick():Boolean{super.performClick();return true}
}
