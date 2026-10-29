package com.lazygarden.game

import android.content.Context
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class BoosterFlowTest {
 private val context get()=ApplicationProvider.getApplicationContext<Context>()
 private val hammer=Match3Engine.Booster.HAMMER
 @Before fun reset(){context.getSharedPreferences("garden_progress",0).edit().clear().putInt("level",30).putInt("coins",200).commit()}
 private fun descendants(v:View):List<View> = listOf(v)+if(v is ViewGroup)(0 until v.childCount).flatMap{descendants(v.getChildAt(it))} else emptyList()
 private fun stockLabel(tile:View):TextView = descendants(tile).filterIsInstance<TextView>().first{it.text.toString().startsWith("×") || it.text.toString().startsWith("+")}
 @Test fun liveHammerStockDecrementsAndPersistsWithoutSpendingCoins(){
  val save=GardenSave(context)
  ActivityScenario.launch(MainActivity::class.java).use{scenario->
   scenario.onActivity{a->descendants(a.window.decorView).filterIsInstance<Button>().first{it.text.toString()=="▶ Luaj · 30"}.performClick()}
   repeat(3){used->
    scenario.onActivity{a->
     val views=descendants(a.window.decorView)
     val tray=views.filterIsInstance<BoosterInventoryTray>().single()
     val board=views.filterIsInstance<Match3BoardView>().single()
     assertFalse(board.isBusy)
     assertEquals("×${3-used}",stockLabel(tray.getChildAt(0)).text.toString())
     tray.getChildAt(0).performClick()
     assertEquals(hammer,board.targetBooster)
     val p=board.cellCenter(0);val time=SystemClock.uptimeMillis()
     for(action in listOf(MotionEvent.ACTION_DOWN,MotionEvent.ACTION_UP)){
      val event=MotionEvent.obtain(time,time,action,p.x,p.y,0)
      board.dispatchTouchEvent(event);event.recycle()
     }
     assertEquals(2-used,save.boosterCount(hammer))
     assertEquals(200,save.coins)
    }
    // Each cascade has a 635 ms animation; inspect busy on the main thread.
    var busy=true;val end=SystemClock.uptimeMillis()+20000
    while(busy&&SystemClock.uptimeMillis()<end){SystemClock.sleep(100);scenario.onActivity{a->busy=descendants(a.window.decorView).filterIsInstance<Match3BoardView>().single().isBusy}}
    assertFalse("Booster animation must settle",busy)
   }
   scenario.onActivity{a->val tray=descendants(a.window.decorView).filterIsInstance<BoosterInventoryTray>().single();tray.getChildAt(0).performClick();assertEquals(0,save.boosterCount(hammer));assertEquals(200,save.coins)}
   // Opening the purchase offer spends nothing, and stock survives Activity recreation.
   scenario.recreate();assertEquals(0,GardenSave(context).boosterCount(hammer));assertEquals(200,save.coins)
  }
 }
 @Test fun controllerRejectsInvalidTargetEmptyStockReplayAndOverflow(){
  val save=GardenSave(context);val board=Match3Engine(30,Random(411));val controller=Match3BoosterController(save,board,30)
  val before=board.encode()
  assertEquals(Match3BoosterController.Status.INVALID_TARGET,controller.use(hammer,-1).status)
  assertEquals(before,board.encode());assertEquals(3,save.boosterCount(hammer));assertEquals(200,save.coins)
  val replay=Match3BoosterController(save,board,31)
  assertEquals(Match3BoosterController.Status.LOCKED_REPLAY,replay.use(hammer,0).status);assertFalse(replay.buy(hammer))
  assertFalse(save.buyBoosterStock(hammer,Int.MAX_VALUE));assertEquals(200,save.coins)
  context.getSharedPreferences("garden_progress",0).edit().putInt("booster_hammer",0).commit()
  assertEquals(Match3BoosterController.Status.NEEDS_STOCK,controller.use(hammer,0).status);assertEquals(before,board.encode())
  assertTrue(controller.buy(hammer));assertEquals(1,GardenSave(context).boosterCount(hammer));assertEquals(160,save.coins)
  assertEquals(Match3BoosterController.Status.USED,controller.use(hammer,0).status);assertEquals(0,GardenSave(context).boosterCount(hammer))
  assertEquals(board.encode(),save.activeBoard)
 }
 @Test fun boosterTextExistsInEverySupportedLanguage(){
  for(language in listOf("sq","de","it","mk","tr")){
   for(kind in Match3Engine.Booster.entries)assertNotEquals("booster_${kind.name}",GameText.booster(language,kind))
   assertNotEquals("locked",GameText.get(language,"locked"))
  }
 }
}
