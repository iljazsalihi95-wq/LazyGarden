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
 private val workResidents=GameSkin.bitmap(context,R.drawable.resident_work_atlas)
 private val residents=BitmapFactory.decodeResource(resources,R.drawable.resident_atlas)
 private var ms=initialElapsed.coerceIn(0L,FlowerSequence.DURATION)
 private var life=0f
 private var clock:ValueAnimator?=null
 private var chosen=FlowerSequence.choices.firstOrNull{it.id==initialChoice}
 private var completed=false
 private val voice=GardenVoice(context)
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
    if(line!=lastLine){lastLine=line;if(line.isNotBlank()){val speaker=FlowerSequence.speaker(ms)?:"meli";voice.speakLine("flowers",st.name.lowercase(),speaker,language,line)}}
    if(st!=lastStage){lastStage=st;sfx.play(when(st){FlowerSequence.Stage.GEZIMI_ARRIVAL->"walk";FlowerSequence.Stage.PREPARE->"shovel";FlowerSequence.Stage.PLANT->"plant";FlowerSequence.Stage.WATER->"water";FlowerSequence.Stage.BLOOM->"bloom";FlowerSequence.Stage.CELEBRATE->"reward";else->""})}
    invalidate()
   };start()
  }
 }
 override fun onDraw(c:Canvas){
  super.onDraw(c);val s=maxOf(width.toFloat()/garden.width,height.toFloat()/garden.height);val dw=garden.width*s;val dh=garden.height*s
  val dst=RectF((width-dw)/2,(height-dh)/2,(width+dw)/2,(height+dh)/2);backdrop.draw(c,dst);WorldRestorationArt.water(c,p,dst,life*12000f)
  bed.set(dst.left+dw*.12f,dst.top+dh*.61f,dst.left+dw*.88f,dst.top+dh*.78f);drawBed(c);drawPeople(c,dst,dw)
  val st=FlowerSequence.stage(ms)
  if(st in listOf(FlowerSequence.Stage.PREPARE,FlowerSequence.Stage.PLANT)){for(i in 0..10){val t=(life*8+i*.09f)%1f;p.color=0xffc8a471.toInt();p.alpha=((1-t)*150).toInt();c.drawCircle(bed.left+bed.width()*(.1f+i*.075f),bed.top+bed.height()*(.4f-t*.45f),dp(2f)*(1-t),p)};p.alpha=255}
  if(st in listOf(FlowerSequence.Stage.BLOOM,FlowerSequence.Stage.CELEBRATE)){for(i in 0..2){val x=bed.centerX()+kotlin.math.sin(life*12+i*2f)*bed.width()*.4f;val y=bed.top-dp(30f)-kotlin.math.sin(life*18+i)*dp(15f);val saved=c.save();c.scale(.65f+kotlin.math.abs(kotlin.math.sin(life*30+i))*.35f,1f,x,y);SpriteArt.draw(c,p,props,4,3,11,RectF(x-dp(12f),y-dp(12f),x+dp(12f),y+dp(12f)));c.restoreToCount(saved)}}
  if(st==FlowerSequence.Stage.CHOOSE&&chosen==null)drawPicker(c)
 }
 private fun drawBed(c:Canvas){
  val prep=FlowerSequence.prepare(ms);p.color=Color.rgb((96-28*prep).toInt(),(70-24*prep).toInt(),(43-12*prep).toInt());SpriteArt.draw(c,p,props,4,3,3,bed)
  val count=(FlowerSequence.planting(ms)*12).toInt().coerceIn(0,12);val bloom=FlowerSequence.bloom(ms)
  for(i in 0 until count){val col=i%6;val row=i/6;drawPlant(c,bed.left+bed.width()*(.10f+col*.16f),bed.top+bed.height()*(.42f+row*.36f),bloom)}
  if(FlowerSequence.stage(ms)==FlowerSequence.Stage.WATER){SpriteArt.draw(c,p,props,4,3,6,RectF(bed.right-dp(60f),bed.top-dp(70f),bed.right+dp(10f),bed.top));p.color=0xaa8be9ff.toInt();for(i in 0..16){val t=(life*3+i*.071f)%1f;c.drawCircle(bed.left+bed.width()*(.12f+.76f*t),bed.top-bed.height()*.15f+t*bed.height()*.55f,dp(2.4f),p)}}
 }
 private fun drawPlant(c:Canvas,x:Float,y:Float,bloom:Float){plants.draw(c,p,chosen?.id?:"tulip",x,y,dp(28f)*(0.3f+0.7f*bloom))}
 private fun drawPeople(c:Canvas,dst:RectF,dw:Float){
  val ids=arrayOf("rona","meli","lazi","gezimi");val points=arrayOf(.34f to .61f,.67f to .61f,.24f to .68f,.78f to .68f);val stage=FlowerSequence.stage(ms)
  for(i in ids.indices){val id=ids[i];val a=if(id=="gezimi")FlowerSequence.approach(ms) else 1f;val sx=if(i%2==0).08f else .92f;val x=dst.left+dw*(sx+(points[i].first-sx)*a);val y=dst.top+dst.height()*(.83f+(points[i].second-.83f)*a);val state=FlowerSequence.actorState(ms,id)
   val residentState=when(state){CharacterAnimation.State.WALK->ResidentAnimation.State.WALK;CharacterAnimation.State.TALK->ResidentAnimation.State.TALK;CharacterAnimation.State.WORK->ResidentAnimation.State.WORK;CharacterAnimation.State.HAPPY->ResidentAnimation.State.HAPPY;else->ResidentAnimation.State.IDLE}
   val workFrame=if(state==CharacterAnimation.State.WORK)when(stage){FlowerSequence.Stage.PREPARE->((ms/300+i)%2).toInt();FlowerSequence.Stage.PLANT->2+((ms/380+i)%2).toInt();FlowerSequence.Stage.WATER->6;else->-1}else -1
   if(workFrame>=0){val ww=dw*.19f;val yy=y+dst.height()*.035f;val saved=c.save();c.scale(if(x<dst.centerX())1f else -1f,1f,x,yy);WorkerArt.draw(c,p,workResidents,i,workFrame,RectF(x-ww/2,yy-ww,x+ww/2,yy));c.restoreToCount(saved)}else drawResident(c,i,x,y,dw*.16f,ResidentAnimation.frame(residentState,(life*90).toInt()+i),x<dst.centerX())
  }
  val text=FlowerSequence.dialogue(ms,language);if(text.isNotBlank()){val who=FlowerSequence.speaker(ms)?:"meli";SceneBubble.draw(c,p,width,height,displayName(who),text,dp(1f))}
 }
 private fun displayName(id:String)=when(id){"lazi"->"Lazi";"meli"->"Meli";"rona"->"Rona";"gezimi"->"Gëzimi";else->id}
 private fun drawResident(c:Canvas,id:Int,x:Float,y:Float,w:Float,frame:Int,right:Boolean){val cw=residents.width/8;val ch=residents.height/4;val src=Rect(frame*cw,id*ch,(frame+1)*cw,(id+1)*ch);val h=w*ch/cw;val save=c.save();c.scale(if(right)1f else -1f,1f,x,y);c.drawBitmap(residents,src,RectF(x-w/2,y-h,x+w/2,y),p);c.restoreToCount(save)}
 private fun drawPicker(c:Canvas){val box=RectF(dp(18f),height*.24f,width-dp(18f),height*.58f);p.color=0xf5fff5dc.toInt();c.drawRoundRect(box,dp(20f),dp(20f),p);p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.DEFAULT_BOLD;p.textSize=dp(18f);p.color=0xff5a3923.toInt();c.drawText(pickerTitle(),box.centerX(),box.top+dp(32f),p);FlowerSequence.choices.forEachIndexed{i,f->val w=box.width()/3;val cx=box.left+w*(i+.5f);plants.draw(c,p,f.id,cx,box.centerY()+dp(24f),dp(60f));p.color=0xff4b3422.toInt();p.textSize=dp(12f);c.drawText(localName(f),cx,box.bottom-dp(22f),p)}}
 override fun onTouchEvent(e:MotionEvent):Boolean{if(e.action==MotionEvent.ACTION_UP&&FlowerSequence.stage(ms)==FlowerSequence.Stage.CHOOSE&&chosen==null){val l=dp(18f);val r=width-dp(18f);if(e.x>=l&&e.x<=r&&e.y>=height*.24f&&e.y<=height*.58f){val i=(((e.x-l)/(r-l))*3).toInt().coerceIn(0,2);chosen=FlowerSequence.choices[i];onChoice(chosen!!);ms=17000L;checkpoint(ms);invalidate();performClick();return true}};return true}
 override fun performClick():Boolean{super.performClick();return true}
 private fun localName(f:FlowerSequence.FlowerChoice)=when(language){"de"->f.nameDe;"it"->f.nameIt;"mk"->f.nameMk;"tr"->f.nameTr;else->f.nameSq}
 private fun pickerTitle()=when(language){"de"->"Blumen wählen";"it"->"Scegli i fiori";"mk"->"Избери цвеќиња";"tr"->"Çiçekleri seç";else->"Zgjidh lulet"}
 private fun dp(v:Float)=v*resources.displayMetrics.density
}
