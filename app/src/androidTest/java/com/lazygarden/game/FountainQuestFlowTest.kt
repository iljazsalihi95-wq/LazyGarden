package com.lazygarden.game

import android.content.Context
import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class FountainQuestFlowTest {
 private val context get()=ApplicationProvider.getApplicationContext<Context>()
 private val save get()=GardenSave(context)
 private var currentActivity:MainActivity?=null
 private data class DrawnPose(val id:String,val state:CharacterAnimation.State,val frame:Int,val facingRight:Boolean)
 private val drawn=java.util.concurrent.ConcurrentLinkedQueue<DrawnPose>()
 private val observed=java.util.concurrent.ConcurrentHashMap.newKeySet<FountainQuest.Stage>()
 @Before fun clear(){currentActivity=null;observed.clear();drawn.clear();context.getSharedPreferences("garden_progress",0).edit().clear().putBoolean("voice_enabled",false).putBoolean("sfx_enabled",false).commit()}
 private fun views(v:View):List<View> = listOf(v)+if(v is ViewGroup)(0 until v.childCount).flatMap{views(v.getChildAt(it))}else emptyList()
 private fun quest(a:MainActivity)=views(a.window.decorView).filterIsInstance<FountainQuestView>().single()
 private fun tap(v:View,x:Float,y:Float){val now=SystemClock.uptimeMillis();for(action in listOf(MotionEvent.ACTION_DOWN,MotionEvent.ACTION_UP)){val e=MotionEvent.obtain(now,now,action,x,y,0);v.dispatchTouchEvent(e);e.recycle()}}
 private fun onUi(block:(MainActivity)->Unit){
  val done=java.util.concurrent.CountDownLatch(1);val error=java.util.concurrent.atomic.AtomicReference<Throwable?>()
  android.os.Handler(android.os.Looper.getMainLooper()).post{try{block(checkNotNull(currentActivity))}catch(t:Throwable){error.set(t)}finally{done.countDown()}}
  assertTrue("Native action must reach the main thread",done.await(15,java.util.concurrent.TimeUnit.SECONDS));error.get()?.let{throw it}
 }
 private fun recreateNative(){
  val previous=checkNotNull(currentActivity)
  val resumed=java.util.concurrent.CountDownLatch(1)
  val monitor=androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance()
  val callback=object:androidx.test.runner.lifecycle.ActivityLifecycleCallback{
   override fun onActivityLifecycleChanged(a:android.app.Activity,state:androidx.test.runner.lifecycle.Stage){
    if(a is MainActivity&&a!==previous&&state==androidx.test.runner.lifecycle.Stage.RESUMED){
     currentActivity=a
     views(a.window.decorView).filterIsInstance<FountainQuestView>().singleOrNull()?.let{v->v.onActorRendered={id,pose,frame,facing->drawn.add(DrawnPose(id,pose,frame,facing))};v.onStageChanged={observed.add(it)}}
     resumed.countDown()
    }
   }
  }
  onUi{monitor.addLifecycleCallback(callback);it.recreate()}
  try{assertTrue("Recreated native Activity must resume",resumed.await(20,java.util.concurrent.TimeUnit.SECONDS))}
  finally{onUi{monitor.removeLifecycleCallback(callback)}}
 }
 private fun awaitQuestLayout(){
  val end=SystemClock.uptimeMillis()+10000
  while(SystemClock.uptimeMillis()<end){var laidOut=false;onUi{a->val v=quest(a);laidOut=v.isLaidOut&&v.width>0&&v.height>0};if(laidOut)return;SystemClock.sleep(30)}
  fail("Fountain scene must lay out before world coordinates are tapped")
 }
 private fun action(s:ActivityScenario<MainActivity>){onUi{val v=quest(it);val point=v.actionCenter();tap(v,point.x,point.y)}}
 private fun waitFor(s:ActivityScenario<MainActivity>,stage:FountainQuest.Stage,timeout:Long=15000){
  val end=SystemClock.uptimeMillis()+timeout
  while(SystemClock.uptimeMillis()<end){if(save.fountainQuestStage==stage||stage in observed)return;SystemClock.sleep(50)}
  fail("Expected $stage, actual ${save.fountainQuestStage}")
 }
 private fun shot(name:String){SystemClock.sleep(250);val ui=InstrumentationRegistry.getInstrumentation().uiAutomation;val file=File(context.getExternalFilesDir(null),"$name.png");val bitmap=ui.takeScreenshot();file.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle();for(cmd in listOf("mkdir -p /data/local/tmp/lazygarden-screenshots","cp ${file.absolutePath} /data/local/tmp/lazygarden-screenshots/$name.png"))ParcelFileDescriptor.AutoCloseInputStream(ui.executeShellCommand(cmd)).use{it.readBytes()}}
 private fun solve(level:Int):Match3Engine {val e=Match3Engine(level,Random(level*71));repeat(500){if(!e.won)e.boost(Match3Engine.Booster.COLOR,it%64)};assertTrue(e.won);return e}
 @Test fun realWorldTapsArrivalEarnedMaterialsWorkCleaningWaterAndRewardsPersist(){
  ActivityScenario.launch(MainActivity::class.java).use{s->
   s.onActivity{a->currentActivity=a;views(a.window.decorView).filterIsInstance<Button>().first{it.text.toString()=="Ndërto"}.performClick();quest(a).onStageChanged={observed.add(it)};quest(a).onActorRendered={id,state,frame,facing->drawn.add(DrawnPose(id,state,frame,facing))}}
   waitFor(s,FountainQuest.Stage.TEST_WATER);shot("premium-01-meli-tests-water");action(s)
   waitFor(s,FountainQuest.Stage.CALL_LAZI)
   assertTrue("Meli must visibly try the dry fountain",drawn.any{it.id=="meli"&&it.state==CharacterAnimation.State.WORK})
   onUi{a->val v=quest(a);tap(v,v.width*.5f,v.height*.51f)}
   assertEquals("The fountain cannot substitute for calling Meli",FountainQuest.Stage.CALL_LAZI,save.fountainQuestStage)
   action(s);waitFor(s,FountainQuest.Stage.INSPECT);action(s)
   waitFor(s,FountainQuest.Stage.CALL_REXHA)
   assertTrue(drawn.any{it.id=="lazi"&&it.state==CharacterAnimation.State.WORK})
   assertTrue(drawn.filter{it.id=="lazi"&&it.state==CharacterAnimation.State.WALK}.let{it.isNotEmpty()&&it.all{pose->!pose.facingRight}})
   action(s)
   waitFor(s,FountainQuest.Stage.REXHA_ARRIVAL);shot("premium-02-rexha-arrived")
   waitFor(s,FountainQuest.Stage.REXHA_INSPECT);action(s);waitFor(s,FountainQuest.Stage.COLLECT)
   assertTrue(drawn.any{it.id=="rexha"&&it.state==CharacterAnimation.State.WORK})
   assertTrue(drawn.filter{it.id=="rexha"&&it.state==CharacterAnimation.State.WALK}.let{it.isNotEmpty()&&it.all{pose->pose.facingRight}})
   assertFalse(save.restorationMaterialsReady());val oldCoins=save.coins;action(s);assertEquals(FountainQuest.Stage.COLLECT,save.fountainQuestStage);assertEquals(oldCoins,save.coins)
   // Earn through the actual Match-3 win transaction; no test stock injection.
   repeat(5){assertTrue(save.win(solve(save.level)))}
   assertTrue(save.restorationMaterialsReady());shot("premium-03-materials-ready")
   val stock=save.restorationNeeds().associate{it.material to save.materialCount(it.material)}
   val coins=save.coins;val stars=save.stars;action(s);assertEquals(stars,save.stars);assertEquals(FountainQuest.Stage.MATERIAL_DELIVERY,save.fountainQuestStage);assertEquals(coins-120,save.coins)
   save.restorationNeeds().forEach{assertEquals(stock.getValue(it.material)-it.count,save.materialCount(it.material))}
   assertFalse(save.actFountain(FountainQuest.Action.RESTORE));waitFor(s,FountainQuest.Stage.REPAIR);assertTrue(drawn.any{it.id=="rexha"&&it.state==CharacterAnimation.State.CARRY});SystemClock.sleep(900);shot("premium-04-rexha-work")
   recreateNative();assertTrue(save.constructing);assertEquals(0,save.tasks)
   waitFor(s,FountainQuest.Stage.CLEAN)
   for(i in 0..2){
    val oldMask=save.fountainCleanedMask
    onUi{a->val v=quest(a);val point=v.leafCenter(i);tap(v,point.x,point.y);assertEquals("A leaf is not granted before the child reaches it",oldMask,save.fountainCleanedMask)}
    val end=SystemClock.uptimeMillis()+10000;var busy=true
    while(busy&&SystemClock.uptimeMillis()<end){SystemClock.sleep(50);onUi{a->busy=quest(a).isCollecting}}
    assertFalse("Child collection animation must finish",busy)
    assertTrue(save.fountainCleanedMask and (1 shl i)!=0)
   }
   assertTrue(drawn.any{it.id=="cufi"&&it.state==CharacterAnimation.State.COLLECT})
   assertTrue(drawn.any{it.id=="rona"&&it.state==CharacterAnimation.State.COLLECT})
   waitFor(s,FountainQuest.Stage.START_WATER)
   var cufiBefore=0f;var ronaBefore=0f
   onUi{a->cufiBefore=quest(a).actorGround("cufi")!!.y;ronaBefore=quest(a).actorGround("rona")!!.y}
   action(s);waitFor(s,FountainQuest.Stage.CLAIM)
   onUi{a->val v=quest(a);val d=a.resources.displayMetrics.density
    assertTrue("Cufi approaches the running water",v.actorGround("cufi")!!.y<cufiBefore-50*d)
    assertTrue("Rona approaches the running water",v.actorGround("rona")!!.y<ronaBefore-50*d)
    assertEquals(CharacterAnimation.State.HAPPY,v.actorState("cufi"));assertEquals(CharacterAnimation.State.HAPPY,v.actorState("rona"))
   }
   shot("premium-05-restored-fountain");assertEquals(0,save.tasks)
   val beforeCoins=save.coins;val beforeStars=save.stars;val beforeXp=save.xp
   action(s);assertEquals(1,save.tasks);assertFalse(save.constructing)
   assertEquals(beforeCoins+75,save.coins);assertEquals(beforeStars+1,save.stars);assertEquals(beforeXp+50,save.xp)
   assertFalse(save.actFountain(FountainQuest.Action.CLAIM));assertFalse(save.hasPendingStory);assertEquals(3,save.storyPosition);shot("premium-06-rewards-in-flight")
   recreateNative();assertEquals(1,save.tasks);assertEquals(beforeCoins+75,save.coins);assertEquals(beforeStars+1,save.stars);assertEquals(beforeXp+50,save.xp);onUi{a->assertTrue(views(a.window.decorView).any{it.contentDescription=="Logo LazyGarden"});assertEquals(CharacterSprites.identities.toSet(),views(a.window.decorView).filterIsInstance<GardenWorldView>().single().residentCast.toSet())};shot("premium-10-family-estate-after-restore")
  }
 }
 @Test fun actualResultScreenDeliversEarnedMaterialIntoItsCounter(){
  save.saveBoard(solve(1))
  ActivityScenario.launch(MainActivity::class.java).use{s->
   s.onActivity{a->views(a.window.decorView).filterIsInstance<Button>().first{it.text.toString()=="▶ Vazhdo · 1"}.performClick()}
   assertEquals(2,save.materialCount(Material.PIPE));assertEquals(2,save.level)
   shot("premium-07-match-material-reward");SystemClock.sleep(2000)
   s.onActivity{a->assertTrue(views(a.window.decorView).filterIsInstance<TextView>().any{it.text.toString()=="Tuba  2/4"})}
   assertFalse(save.win(solve(1)));assertEquals(2,save.materialCount(Material.PIPE))
  }
 }
 @Test fun incompleteMaterialsAndInvalidActionsNeverConsumeInventory(){
  assertTrue(save.startFountainQuest());assertFalse(save.actFountain(FountainQuest.Action.CLAIM));assertFalse(save.actFountain(FountainQuest.Action.RESTORE))
  assertFalse(save.advanceFountainTimed(FountainQuest.Stage.MELI_APPROACH,1));assertEquals(FountainQuest.Stage.MELI_APPROACH,save.fountainQuestStage)
  assertTrue(save.advanceFountainTimed(FountainQuest.Stage.MELI_APPROACH,2800));assertFalse(save.actFountain(FountainQuest.Action.CALL_REXHA));assertEquals(0,save.coins);assertEquals(0,save.materialCount(Material.PIPE))
 }
 @Test fun repairCanPauseAtHomeWithoutStartingTheLegacyAutomaticScene(){
  context.getSharedPreferences("garden_progress",0).edit().putBoolean("fountain_quest_active",true).putBoolean("constructing",true).putString("fountain_quest_stage","REPAIR").putLong("fountain_quest_elapsed",1000).commit()
  ActivityScenario.launch(MainActivity::class.java).use{s->
   s.onActivity{a->val v=quest(a);val d=a.resources.displayMetrics.density;tap(v,30*d,30*d);assertTrue(views(a.window.decorView).any{it.contentDescription=="Logo LazyGarden"});assertTrue(views(a.window.decorView).none{it is FountainQuestView})}
   val checkpoint=save.fountainQuestElapsed;SystemClock.sleep(700);assertEquals(checkpoint,save.fountainQuestElapsed);assertEquals(0,save.tasks);assertTrue(save.constructing)
   s.onActivity{a->views(a.window.decorView).filterIsInstance<Button>().first{it.text.toString()=="Ndërto"}.performClick();assertTrue(quest(a).currentStage in listOf(FountainQuest.Stage.REPAIR,FountainQuest.Stage.CLEAN))}
   assertEquals(0,save.tasks);assertEquals(0,save.coins)
  }
 }

 @Test fun match3WinReturnsToTheSameFountainAndInventoryCounters(){
  context.getSharedPreferences("garden_progress",0).edit().putBoolean("fountain_quest_active",true).putString("fountain_quest_stage","COLLECT").commit()
  save.saveBoard(solve(1))
  ActivityScenario.launch(MainActivity::class.java).use{s->
   s.onActivity{a->currentActivity=a;views(a.window.decorView).filterIsInstance<Button>().first{it.text.toString()=="Ndërto"}.performClick()}
   awaitQuestLayout()
   onUi{a->val v=quest(a);val d=a.resources.displayMetrics.density;tap(v,v.width*.25f,v.height-70*d)}
   assertEquals(2,save.level);assertEquals(2,save.materialCount(Material.PIPE));assertEquals(150,save.coins)
   onUi{a->views(a.window.decorView).filterIsInstance<Button>().first{it.text.toString()=="Vazhdo"}.performClick();assertEquals(FountainQuest.Stage.COLLECT,quest(a).currentStage)}
   awaitQuestLayout()
   onUi{a->val v=quest(a);val d=a.resources.displayMetrics.density;tap(v,v.width-36*d,30*d);assertTrue(views(a.window.decorView).any{it is InventoryScreen});assertTrue(views(a.window.decorView).filterIsInstance<TextView>().any{it.text.toString()=="2 / 4"})}
   shot("premium-08-earned-material-inventory")
   onUi{a->views(a.window.decorView).first{it.contentDescription=="inventory_boosters"}.performClick();assertEquals(7,views(a.window.decorView).filterIsInstance<TextView>().count{it.text.toString()=="×3"})}
   onUi{a->views(a.window.decorView).filterIsInstance<Button>().first{it.text.toString()=="‹"}.performClick();assertEquals(FountainQuest.Stage.COLLECT,quest(a).currentStage)}
   assertEquals(0,save.tasks);assertEquals(2,save.materialCount(Material.PIPE));assertEquals(150,save.coins)
  }
 }

 @Test fun checkpointsFromThePreviousFountainVersionKeepTheirStageAndInventory(){
  val oldStages=listOf("MELI_APPROACH","TEST_WATER","CHILDREN_APPROACH","CALL_LAZI","LAZI_APPROACH","INSPECT","CALL_REXHA","PHONE_CALL","REXHA_ARRIVAL","REXHA_INSPECT","COLLECT","REPAIR","CLEAN","START_WATER","WATER_FLOW","CELEBRATE","CLAIM")
  val p=context.getSharedPreferences("garden_progress",0)
  for(name in oldStages){
   p.edit().putBoolean("fountain_quest_active",true).putString("fountain_quest_stage",name).putLong("fountain_quest_elapsed",1375).putInt("material_PIPE",2).putInt("coins",150).commit()
   val restored=GardenSave(context)
   assertEquals(name,restored.fountainQuestStage.name);assertEquals(1375L,restored.fountainQuestElapsed)
   assertEquals(2,restored.materialCount(Material.PIPE));assertEquals(150,restored.coins)
   assertTrue(restored.startFountainQuest());assertEquals(name,restored.fountainQuestStage.name)
  }
 }

 @Test fun sharedCastSamplerKeepsFiveDistinctIdentitiesAcrossAllEightStates(){
  val ids=CharacterSprites.identities
  assertEquals(setOf("lazi","meli","cufi","rona","rexha"),ids.toSet())
  assertEquals(5,ids.map{CharacterSprites.definition(it)!!.let{d->d.resource to d.row}}.toSet().size)
  val image=Bitmap.createBitmap(960,800,Bitmap.Config.ARGB_8888)
  val canvas=android.graphics.Canvas(image);val paint=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
  for((row,id)in ids.withIndex())for((column,state)in CharacterAnimation.State.entries.withIndex()){
   val aspect=CharacterSprites.aspect(context,id);val x=column*120f+60;val y=row*160f+150
   val bounds=android.graphics.RectF(x-40,y-80*aspect,x+40,y)
   val frame=CharacterAnimation.frame(state,0,CharacterSprites.definition(id)!!.arrivalAtlas)
   assertTrue(CharacterSprites.draw(context,canvas,paint,id,frame,bounds,true))
   var visible=0
   for(py in row*160 until (row+1)*160 step 4)for(px in column*120 until (column+1)*120 step 4)if(android.graphics.Color.alpha(image.getPixel(px,py))>20)visible++
   assertTrue("$id/$state must sample a visible full-body pose",visible>20)
  }
  val file=File(context.getExternalFilesDir(null),"premium-09-shared-cast-frames.png")
  file.outputStream().use{image.compress(Bitmap.CompressFormat.PNG,100,it)};image.recycle()
  val ui=InstrumentationRegistry.getInstrumentation().uiAutomation
  for(cmd in listOf("mkdir -p /data/local/tmp/lazygarden-screenshots","cp ${file.absolutePath} /data/local/tmp/lazygarden-screenshots/${file.name}"))ParcelFileDescriptor.AutoCloseInputStream(ui.executeShellCommand(cmd)).use{it.readBytes()}
 }

}
