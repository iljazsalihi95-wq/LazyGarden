package com.lazygarden.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator

/** Native scene 2: flower choice, Gëzimi arrival, soil preparation, planting, watering and bloom. */
class FlowerGardenView(
 context: Context,
 private val language:String="sq",
 private val initialChoice:String?=null,
 private val initialElapsed:Long=0L,
 private val checkpoint:(Long)->Unit={},
 private val onChoice:(FlowerSequence.FlowerChoice)->Unit={},
 private val onComplete:()->Unit={}
):View(context),SceneLifecycle{
 private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
 private val backdrop=GardenBackdrop(context,1)
 private val garden=GameSkin.bitmap(context,R.drawable.garden_estate)
 private val actors=mutableMapOf<String,CharacterAnimation.Player>()
 private var ms=initialElapsed.coerceIn(0L,FlowerSequence.DURATION)
 private var life=0f
 private var clock:ValueAnimator?=null
 private var chosen=FlowerSequence.choices.firstOrNull{it.id==initialChoice}
 private var completed=false
 private val voice=GardenVoice(context){GardenSave(context).voiceEnabled}
 private val sfx=GardenSfx(context)
 private var lastLine=""
 private var lastStage:FlowerSequence.Stage?=null
 internal val sceneElapsedMillis get()=ms
 private val plants=PlantArt(context)
 private val props=GameSkin.bitmap(context,R.drawable.garden_work_props)
 private val bed=RectF()
 override fun onWindowVisibilityChanged(v:Int){super.onWindowVisibilityChanged(v);if(v==VISIBLE)resumeScene() else pauseScene()}
 override fun onAttachedToWindow(){super.onAttachedToWindow();resumeScene()}
 internal fun dispose(){pauseScene();voice.release();sfx.release()}
 override fun onDetachedFromWindow(){dispose();super.onDetachedFromWindow()}
 override fun resumeScene(){startClock()}
 override fun pauseScene(){checkpoint(ms);voice.stop();sfx.pause();clock?.removeAllUpdateListeners();clock?.cancel();clock=null}
 private fun startClock(){
  if(clock!=null || completed || !isAttachedToWindow || windowVisibility!=VISIBLE)return
  var last=android.os.SystemClock.uptimeMillis()
  clock=ValueAnimator.ofFloat(0f,1f).apply{
   duration=12000L;repeatCount=ValueAnimator.INFINITE;interpolator=LinearInterpolator()
   addUpdateListener{
    val now=android.os.SystemClock.uptimeMillis();val dt=(now-last).coerceIn(0L,1000L);last=now
    life=(life+dt/12000f)%1f
    if(FlowerSequence.stage(ms)!=FlowerSequence.Stage.CHOOSE || chosen!=null)ms=(ms+dt).coerceAtMost(FlowerSequence.DURATION)
    if(ms>=FlowerSequence.DURATION && !completed){completed=true;checkpoint(ms);post{if(isAttachedToWindow)onComplete()}}
    val st=FlowerSequence.stage(ms);val line=FlowerSequence.dialogue(ms,language)
    if(line!=lastLine){lastLine=line;if(line.isNotBlank()){val speaker=FlowerSequence.speaker(ms)?:"meli";voice.speakLine("flower_garden",voiceLineId(st,ms),speaker,language,line)}}
    if(st!=lastStage){lastStage=st;sfx.play(when(st){FlowerSequence.Stage.GEZIMI_ARRIVAL->"walk";FlowerSequence.Stage.PREPARE->"shovel";FlowerSequence.Stage.PLANT->"plant";FlowerSequence.Stage.WATER->"water";FlowerSequence.Stage.BLOOM->"bloom";FlowerSequence.Stage.CELEBRATE->"reward";else->""})}
    invalidate()
   };start()
  }
 }
 private fun voiceLineId(stage:FlowerSequence.Stage,elapsed:Long)=when(stage){
  FlowerSequence.Stage.TALK->if(elapsed<5200L)"choice_01" else "call_01"
  FlowerSequence.Stage.CALL_GEZIMI->"collect_01"
  FlowerSequence.Stage.GEZIMI_ARRIVAL->"arrival_01"
  FlowerSequence.Stage.PREPARE->"plan_01"
  FlowerSequence.Stage.PLANT->"plant_01"
  FlowerSequence.Stage.WATER->if(elapsed<28750L)"water_01" else "water_02"
  FlowerSequence.Stage.CELEBRATE->"finish_01"
  else->stage.name.lowercase()
 }
 override fun onDraw(c:Canvas){
  super.onDraw(c);val s=FlowerSitePlan.cameraWidth(width,height)/garden.width;val dw=garden.width*s;val dh=garden.height*s
  val left=(width*.44f-dw*.20f).coerceIn(width-dw,0f);val top=(height*.67f-dh*.715f).coerceIn(height-dh,0f)
  val dst=RectF(left,top,left+dw,top+dh);backdrop.draw(c,dst);WorldRestorationArt.water(c,p,dst,life*12000f)
  bed.set(dst.left+dw*.055f,dst.top+dh*.65f,dst.left+dw*.285f,dst.top+dh*.78f);drawBed(c,dst);drawPeople(c,dst,dw);drawWatering(c,dst)
  val st=FlowerSequence.stage(ms)
  if(st in listOf(FlowerSequence.Stage.PREPARE,FlowerSequence.Stage.PLANT)){for(i in 0..10){val t=(life*8+i*.09f)%1f;p.color=0xffc8a471.toInt();p.alpha=((1-t)*150).toInt();c.drawCircle(bed.left+bed.width()*(.1f+i*.075f),bed.top+bed.height()*(.4f-t*.45f),dp(2f)*(1-t),p)};p.alpha=255}
  if(st in listOf(FlowerSequence.Stage.BLOOM,FlowerSequence.Stage.CELEBRATE)){for(i in 0..2){val x=bed.centerX()+kotlin.math.sin(life*12+i*2f)*bed.width()*.4f;val y=bed.top-dp(30f)-kotlin.math.sin(life*18+i)*dp(15f);val saved=c.save();c.scale(.65f+kotlin.math.abs(kotlin.math.sin(life*30+i))*.35f,1f,x,y);SpriteArt.draw(c,p,props,4,3,11,RectF(x-dp(12f),y-dp(12f),x+dp(12f),y+dp(12f)));c.restoreToCount(saved)}}
  if(st==FlowerSequence.Stage.CHOOSE&&chosen==null)drawPicker(c)
 }
 private fun drawBed(c:Canvas,dst:RectF){
  val outline=Path().apply{addOval(bed,Path.Direction.CW)}
  val layer=c.save();c.clipPath(outline)
  p.shader=null;p.alpha=255
  SpriteArt.draw(c,p,props,4,3,3,bed)
  // The alpha-cut soil artwork supplies its own organic stone edge; no opaque backing.
  val count=(FlowerSequence.planting(ms)*12).toInt().coerceIn(0,12)
  val bloom=FlowerSequence.bloom(ms)
  FlowerSitePlan.plants.take(count).forEach { point->plants.draw(c,p,chosen?.id?:"tulip",dst.left+dst.width()*point.x,dst.top+dst.height()*point.y,dst.width()*.055f*(.45f+.55f*bloom)) }
  c.restoreToCount(layer)
  val stage=FlowerSequence.stage(ms)
  val worker=FlowerSitePlan.gardener
  val wx=dst.left+dst.width()*worker.x;val wy=dst.top+dst.height()*worker.y
  if(stage==FlowerSequence.Stage.PREPARE){
   val target=FlowerSitePlan.plants[((FlowerSequence.prepare(ms)*12).toInt()).coerceIn(0,11)]
   val x=dst.left+dst.width()*target.x;val y=dst.top+dst.height()*target.y
   p.strokeWidth=dp(4f);p.color=0xffad804c.toInt();c.drawLine(wx-dp(5f),wy-dp(28f),x,y,p)
   p.color=0xffbac5c3.toInt();c.drawOval(RectF(x-dp(5f),y-dp(3f),x+dp(5f),y+dp(6f)),p)
  }
  if(stage==FlowerSequence.Stage.PLANT&&count<12){
   val target=FlowerSitePlan.plants[count]
   val progress=(FlowerSequence.planting(ms)*12-count).coerceIn(0f,1f)
   // Place at the root socket instead of flying the seedling across the scene.
   if(progress>.5f)plants.draw(c,p,chosen?.id?:"tulip",dst.left+dst.width()*target.x,
    dst.top+dst.height()*target.y,dst.width()*.027f*((progress-.5f)*2f))
  }
 }
 private fun drawWatering(c:Canvas,dst:RectF){
  if(FlowerSequence.stage(ms)!=FlowerSequence.Stage.WATER)return
  val progress=FlowerSequence.watering(ms)
  val foot=FlowerSitePlan.wateringPosition(progress)
  val actor=SceneCast.actor("rona")
  val aw=dst.width()*.075f;val ah=aw*CharacterSprites.aspect(context,actor.sprite)
  val fx=dst.left+dst.width()*foot.x;val fy=dst.top+dst.height()*foot.y
  // Can, hand and body share the same world scale and foot anchor.
  val handX=fx-aw*.22f;val handY=fy-ah*.52f
  val canWidth=aw*.55f;val canHeight=aw*.43f
  val can=c.save();c.scale(-1f,1f,handX,handY)
  SpriteArt.draw(c,p,props,4,3,6,RectF(handX-canWidth*.45f,handY-canHeight*.25f,
   handX+canWidth*.55f,handY+canHeight*.75f));c.restoreToCount(can)
  if(!FlowerSitePlan.isPouring(progress))return
  val nozzle=FlowerSitePlan.Point((handX-canWidth*.55f-dst.left)/dst.width(),
   (handY+canHeight*.15f-dst.top)/dst.height())
  val root=FlowerSitePlan.wateringTarget(progress)
  p.shader=null;p.style=Paint.Style.FILL;p.color=0xaa8be9ff.toInt()
  for(i in 0..12){
   val t=(ms/650f+i/13f)%1f
   val drop=FlowerSitePlan.waterDrop(nozzle,root,t)
   c.drawCircle(dst.left+dst.width()*drop.x,dst.top+dst.height()*drop.y,dst.width()*.0015f,p)
  }
  p.color=0x553a6551
  val x=dst.left+dst.width()*root.x;val y=dst.top+dst.height()*root.y
  c.drawOval(RectF(x-aw*.1f,y-aw*.035f,x+aw*.1f,y+aw*.035f),p)
  p.alpha=255
 }

 private fun drawPeople(c:Canvas,dst:RectF,dw:Float){
  val ids=listOf("rona","meli","lazi","gezimi")
  val visible=ids.filter{it!="gezimi"||ms>=9500L}
  val positions=visible.associateWith{id->if(id=="gezimi")FlowerSitePlan.arrival(FlowerSequence.approach(ms)) else if(id=="rona"&&FlowerSequence.stage(ms)==FlowerSequence.Stage.WATER)FlowerSitePlan.wateringPosition(FlowerSequence.watering(ms)) else FlowerSitePlan.waiting.getValue(id)}
  // Farther feet first: people standing behind the bed cannot paint over people in front.
  visible.sortedBy{positions.getValue(it).y}.forEach{id->
   val point=positions.getValue(id);val x=dst.left+dw*point.x;val y=dst.top+dst.height()*point.y
   val planned=FlowerSequence.actorState(ms,id)
   val state=when{
    FlowerSequence.stage(ms)==FlowerSequence.Stage.CHOOSE->CharacterAnimation.State.IDLE
    id=="rona"&&FlowerSequence.stage(ms)==FlowerSequence.Stage.WATER->if(FlowerSitePlan.isPouring(FlowerSequence.watering(ms)))CharacterAnimation.State.WORK else CharacterAnimation.State.WALK
    planned==CharacterAnimation.State.TALK&&!voice.isTalking->CharacterAnimation.State.IDLE
    planned==CharacterAnimation.State.WORK&&id!="gezimi"&&!(id=="rona"&&FlowerSequence.stage(ms)==FlowerSequence.Stage.WATER)->CharacterAnimation.State.IDLE
    else->planned
   }
   val player=actors.getOrPut(id){CharacterAnimation.Player()};player.update(state,ms)
   val actor=SceneCast.actor(id);val aw=dw*(if(actor.child).075f else .095f)
   val ah=aw*CharacterSprites.aspect(context,actor.sprite)
   p.shader=null;p.alpha=255;p.color=0x49302010
   c.drawOval(RectF(x-aw*.30f,y-aw*.025f,x+aw*.30f,y+aw*.025f),p)
   SceneActorArt.draw(context,c,p,id,player,ms,RectF(x-aw/2,y-ah,x+aw/2,y),false)
  }
  val text=FlowerSequence.dialogue(ms,language)
  if(text.isNotBlank()){val who=FlowerSequence.speaker(ms)?:"meli";SceneBubble.draw(c,p,width,height,SceneCast.name(who),text,dp(1f))}
 }
 private fun drawPicker(c:Canvas){val box=RectF(dp(18f),height*.24f,width-dp(18f),height*.58f);p.color=0xf5fff5dc.toInt();c.drawRoundRect(box,dp(20f),dp(20f),p);p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.DEFAULT_BOLD;p.textSize=dp(18f);p.color=0xff5a3923.toInt();c.drawText(pickerTitle(),box.centerX(),box.top+dp(32f),p);FlowerSequence.choices.forEachIndexed{i,f->val w=box.width()/3;val cx=box.left+w*(i+.5f);plants.draw(c,p,f.id,cx,box.centerY()+dp(24f),dp(60f));p.color=0xff4b3422.toInt();p.textSize=dp(12f);c.drawText(localName(f),cx,box.bottom-dp(22f),p)}}
 override fun onTouchEvent(e:MotionEvent):Boolean{if(e.action==MotionEvent.ACTION_UP&&FlowerSequence.stage(ms)==FlowerSequence.Stage.CHOOSE&&chosen==null){val l=dp(18f);val r=width-dp(18f);if(e.x>=l&&e.x<=r&&e.y>=height*.24f&&e.y<=height*.58f){val i=(((e.x-l)/(r-l))*3).toInt().coerceIn(0,2);chosen=FlowerSequence.choices[i];onChoice(chosen!!);ms=17000L;checkpoint(ms);invalidate();performClick();return true}};return true}
 override fun performClick():Boolean{super.performClick();return true}
 private fun localName(f:FlowerSequence.FlowerChoice)=when(language){"de"->f.nameDe;"it"->f.nameIt;"mk"->f.nameMk;"tr"->f.nameTr;else->f.nameSq}
 private fun pickerTitle()=when(language){"de"->"Blumen wählen";"it"->"Scegli i fiori";"mk"->"Избери цвеќиња";"tr"->"Çiçekleri seç";else->"Zgjidh lulet"}
 private fun dp(v:Float)=v*resources.displayMetrics.density
}
