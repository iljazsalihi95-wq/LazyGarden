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
 private val observed=java.util.concurrent.ConcurrentHashMap.newKeySet<FountainQuest.Stage>()
 @Before fun clear(){currentActivity=null;observed.clear();context.getSharedPreferences("garden_progress",0).edit().clear().putBoolean("voice_enabled",false).putBoolean("sfx_enabled",false).commit()}
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
    if(a is MainActivity&&a!==previous&&state==androidx.test.runner.lifecycle.Stage.RESUMED){currentActivity=a;resumed.countDown()}
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
   s.onActivity{a->currentActivity=a;views(a.window.decorView).filterIsInstance<Button>().first{it.text.toString()=="Ndërto"}.performClick();quest(a).onStageChanged={observed.add(it)}}
   waitFor(s,FountainQuest.Stage.TEST_WATER);shot("premium-01-meli-tests-water");action(s)
   waitFor(s,FountainQuest.Stage.CALL_LAZI);action(s);waitFor(s,FountainQuest.Stage.INSPECT);action(s)
   assertEquals(FountainQuest.Stage.CALL_REXHA,save.fountainQuestStage);action(s)
   waitFor(s,FountainQuest.Stage.REXHA_ARRIVAL);shot("premium-02-rexha-arrived")
   waitFor(s,FountainQuest.Stage.REXHA_INSPECT);action(s);waitFor(s,FountainQuest.Stage.COLLECT)
   assertFalse(save.restorationMaterialsReady());val oldCoins=save.coins;action(s);assertEquals(FountainQuest.Stage.COLLECT,save.fountainQuestStage);assertEquals(oldCoins,save.coins)
   // Earn through the actual Match-3 win transaction; no test stock injection.
   repeat(5){assertTrue(save.win(solve(save.level)))}
   assertTrue(save.restorationMaterialsReady());shot("premium-03-materials-ready")
   val stock=save.restorationNeeds().associate{it.material to save.materialCount(it.material)}
   val coins=save.coins;val stars=save.stars;action(s);assertEquals(stars,save.stars);assertEquals(FountainQuest.Stage.REPAIR,save.fountainQuestStage);assertEquals(coins-120,save.coins)
   save.restorationNeeds().forEach{assertEquals(stock.getValue(it.material)-it.count,save.materialCount(it.material))}
   assertFalse(save.actFountain(FountainQuest.Action.RESTORE));SystemClock.sleep(900);shot("premium-04-rexha-work")
   recreateNative();assertTrue(save.constructing);assertEquals(0,save.tasks)
   waitFor(s,FountainQuest.Stage.CLEAN)
   for(i in 0..2){onUi{a->val v=quest(a);val point=v.leafCenter(i);tap(v,point.x,point.y)};assertTrue(save.fountainCleanedMask and (1 shl i)!=0)}
   waitFor(s,FountainQuest.Stage.START_WATER);action(s);waitFor(s,FountainQuest.Stage.CLAIM)
   shot("premium-05-restored-fountain");assertEquals(0,save.tasks)
   val beforeCoins=save.coins;val beforeStars=save.stars;val beforeXp=save.xp
   action(s);assertEquals(1,save.tasks);assertFalse(save.constructing)
   assertEquals(beforeCoins+75,save.coins);assertEquals(beforeStars+1,save.stars);assertEquals(beforeXp+50,save.xp)
   assertFalse(save.actFountain(FountainQuest.Action.CLAIM));assertFalse(save.hasPendingStory);assertEquals(3,save.storyPosition);shot("premium-06-rewards-in-flight")
   recreateNative();assertEquals(1,save.tasks);assertEquals(beforeCoins+75,save.coins);assertEquals(beforeStars+1,save.stars);assertEquals(beforeXp+50,save.xp);onUi{a->assertTrue(views(a.window.decorView).any{it.contentDescription=="Logo LazyGarden"})}
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

}
