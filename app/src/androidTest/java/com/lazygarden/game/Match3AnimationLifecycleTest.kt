package com.lazygarden.game

import android.content.Context
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Match3AnimationLifecycleTest {
 private val context get()=ApplicationProvider.getApplicationContext<Context>()
 private lateinit var board:Match3BoardView
 private fun views(v:View):List<View> = listOf(v)+if(v is ViewGroup)(0 until v.childCount).flatMap{views(v.getChildAt(it))}else emptyList()
 @Before fun reset(){context.getSharedPreferences("garden_progress",0).edit().clear().putInt("level",250).putInt("coins",200).putBoolean("voice_enabled",false).putBoolean("sfx_enabled",false).commit()}
 private fun onUi(block:()->Unit){val done=java.util.concurrent.CountDownLatch(1);val error=java.util.concurrent.atomic.AtomicReference<Throwable?>();android.os.Handler(android.os.Looper.getMainLooper()).post{try{block()}catch(t:Throwable){error.set(t)}finally{done.countDown()}};assertTrue(done.await(15,java.util.concurrent.TimeUnit.SECONDS));error.get()?.let{throw it}}
 private fun open(s:ActivityScenario<MainActivity>){
  s.onActivity{a->views(a.window.decorView).filterIsInstance<Button>().first{it.text.toString()=="▶ Luaj · 250"}.performClick();board=views(a.window.decorView).filterIsInstance<Match3BoardView>().single()}
  val end=SystemClock.uptimeMillis()+10000;var ready=false
  while(!ready&&SystemClock.uptimeMillis()<end){onUi{ready=board.cellCenter(0).x>0&&board.cellCenter(63).y>board.cellCenter(0).y};if(!ready)SystemClock.sleep(30)}
  assertTrue("Board must draw before a native swipe",ready)
 }
 private fun gesture(a:Int,b:Int){val from=board.cellCenter(a);val to=board.cellCenter(b);val now=SystemClock.uptimeMillis();for((action,p)in listOf(MotionEvent.ACTION_DOWN to from,MotionEvent.ACTION_UP to to)){val event=MotionEvent.obtain(now,now,action,p.x,p.y,0);board.dispatchTouchEvent(event);event.recycle()}}
 private fun settled(){val end=SystemClock.uptimeMillis()+20000;var busy=true;while(busy&&SystemClock.uptimeMillis()<end){SystemClock.sleep(50);onUi{busy=board.isBusy}};assertFalse("Visible cascade must settle",busy)}
 @Test fun pausedBoosterCannotAdvanceItsCascadeOrConsumeAnotherStockItem(){
  val save=GardenSave(context)
  ActivityScenario.launch(MainActivity::class.java).use{s->open(s)
   var effect=0f;var model="";var moves=0
   onUi{moves=board.engine.moves;board.runBooster(Match3Engine.Booster.HAMMER);gesture(0,0);assertTrue(board.isBusy);board.pauseScene();effect=board.animationFraction;model=board.engine.encode()}
   assertEquals(2,save.boosterCount(Match3Engine.Booster.HAMMER));SystemClock.sleep(1500)
   onUi{assertTrue(board.isBusy);assertEquals(effect,board.animationFraction,0f);assertEquals(model,board.engine.encode());board.resumeScene()}
   settled();assertEquals(2,save.boosterCount(Match3Engine.Booster.HAMMER));assertEquals(200,save.coins);onUi{assertEquals(moves,board.engine.moves);assertEquals(board.engine.encode(),save.activeBoard)}
  }
 }
 @Test fun legalSwapWaitsForResumeBeforeSpendingItsMove(){
  ActivityScenario.launch(MainActivity::class.java).use{s->open(s)
   var before="";var moves=0
   onUi{val e=board.engine;val legal=(0..63).flatMap{a->listOf(a to a+1,a to a+8)}.first{e.legal(it.first,it.second)};before=e.encode();moves=e.moves;gesture(legal.first,legal.second);board.pauseScene()}
   SystemClock.sleep(500)
   onUi{assertEquals(before,board.engine.encode());assertTrue(board.isBusy);board.resumeScene()}
   settled();onUi{assertEquals(moves-1,board.engine.moves);assertTrue(board.engine.score>0)}
  }
 }
}
