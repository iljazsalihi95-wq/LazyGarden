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
 private val observed=java.util.concurrent.ConcurrentHashMap.newKeySet<FountainQuest.Stage>()
 @Before fun clear(){observed.clear();context.getSharedPreferences("garden_progress",0).edit().clear().putBoolean("voice_enabled",false).putBoolean("sfx_enabled",false).commit()}
 private fun views(v:View):List<View> = listOf(v)+if(v is ViewGroup)(0 until v.childCount).flatMap{views(v.getChildAt(it))}else emptyList()
 private fun quest(a:MainActivity)=views(a.window.decorView).filterIsInstance<FountainQuestView>().single()
 private fun tap(v:View,x:Float,y:Float){val now=SystemClock.uptimeMillis();for(action in listOf(MotionEvent.ACTION_DOWN,MotionEvent.ACTION_UP)){val e=MotionEvent.obtain(now,now,action,x,y,0);v.dispatchTouchEvent(e);e.recycle()}}
 private fun action(s:ActivityScenario<MainActivity>){s.onActivity{val v=quest(it);val point=v.actionCenter();tap(v,point.x,point.y)}}
 private fun waitFor(s:ActivityScenario<MainActivity>,stage:FountainQuest.Stage,timeout:Long=15000){
  val end=SystemClock.uptimeMillis()+timeout
  while(SystemClock.uptimeMillis()<end){if(save.fountainQuestStage==stage||stage in observed)return;SystemClock.sleep(50)}
  fail("Expected $stage, actual ${save.fountainQuestStage}")
 }
 private fun shot(name:String){SystemClock.sleep(250);val ui=InstrumentationRegistry.getInstrumentation().uiAutomation;val file=File(context.getExternalFilesDir(null),"$name.png");val bitmap=ui.takeScreenshot();file.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle();for(cmd in listOf("mkdir -p /data/local/tmp/lazygarden-screenshots","cp ${file.absolutePath} /data/local/tmp/lazygarden-screenshots/$name.png"))ParcelFileDescriptor.AutoCloseInputStream(ui.executeShellCommand(cmd)).use{it.readBytes()}}
 private fun solve(level:Int):Match3Engine {val e=Match3Engine(level,Random(level*71));repeat(500){if(!e.won)e.boost(Match3Engine.Booster.COLOR,it%64)};assertTrue(e.won);return e}
 @Test fun realWorldTapsArrivalEarnedMaterialsWorkCleaningWaterAndRewardsPersist(){
  ActivityScenario.launch(MainActivity::class.java).use{s->
   s.onActivity{a->views(a.window.decorView).filterIsInstance<Button>().first{it.text.toString()=="Ndërto"}.performClick();quest(a).onStageChanged={observed.add(it)}}
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
   s.recreate();assertTrue(save.constructing);assertEquals(0,save.tasks)
   waitFor(s,FountainQuest.Stage.CLEAN)
   for(i in 0..2){s.onActivity{a->val v=quest(a);val point=v.leafCenter(i);tap(v,point.x,point.y)};assertTrue(save.fountainCleanedMask and (1 shl i)!=0)}
   waitFor(s,FountainQuest.Stage.START_WATER);action(s);waitFor(s,FountainQuest.Stage.CLAIM)
   shot("premium-05-restored-fountain");assertEquals(0,save.tasks)
   val beforeCoins=save.coins;val beforeStars=save.stars;val beforeXp=save.xp
   action(s);assertEquals(1,save.tasks);assertFalse(save.constructing)
   assertEquals(beforeCoins+75,save.coins);assertEquals(beforeStars+1,save.stars);assertEquals(beforeXp+50,save.xp)
   assertFalse(save.actFountain(FountainQuest.Action.CLAIM));shot("premium-06-rewards-in-flight")
   s.recreate();assertEquals(1,save.tasks);assertEquals(beforeCoins+75,save.coins);assertEquals(beforeStars+1,save.stars);assertEquals(beforeXp+50,save.xp)
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
}
