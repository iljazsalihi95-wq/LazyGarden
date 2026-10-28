package com.lazygarden.game

import android.content.Context
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class LocalizedGameplayFlowTest {
 private val context get()=ApplicationProvider.getApplicationContext<Context>()
 private lateinit var activity:MainActivity
 private fun views(v:View):List<View> = listOf(v)+if(v is ViewGroup)(0 until v.childCount).flatMap{views(v.getChildAt(it))}else emptyList()
 private fun onUi(block:()->Unit){val done=java.util.concurrent.CountDownLatch(1);val error=java.util.concurrent.atomic.AtomicReference<Throwable?>();android.os.Handler(android.os.Looper.getMainLooper()).post{try{block()}catch(t:Throwable){error.set(t)}finally{done.countDown()}};assertTrue(done.await(15,java.util.concurrent.TimeUnit.SECONDS));error.get()?.let{throw it}}
 private fun reset(lang:String){context.getSharedPreferences("garden_progress",0).edit().clear().putString("language",lang).putBoolean("voice_enabled",false).putBoolean("sfx_enabled",false).commit()}
 private fun recreate(){val old=activity;val ready=java.util.concurrent.CountDownLatch(1);val monitor=androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance();val listener=object:androidx.test.runner.lifecycle.ActivityLifecycleCallback{override fun onActivityLifecycleChanged(a:android.app.Activity,s:androidx.test.runner.lifecycle.Stage){if(a is MainActivity&&a!==old&&s==androidx.test.runner.lifecycle.Stage.RESUMED){activity=a;ready.countDown()}}};onUi{monitor.addLifecycleCallback(listener);activity.recreate()};try{assertTrue(ready.await(20,java.util.concurrent.TimeUnit.SECONDS))}finally{onUi{monitor.removeLifecycleCallback(listener)}}}
 private fun clickPrimary(){views(activity.window.decorView).filterIsInstance<Button>().first{it.text.toString().startsWith("▶ ")&&it.text.toString().endsWith("· 1")}.performClick()}
 @Test fun homeBoardVictoryAndReturnHonorAllFiveLanguages(){
  reset("sq")
  ActivityScenario.launch(MainActivity::class.java).use{s->s.onActivity{activity=it}
   for(lang in listOf("sq","de","it","mk","tr")){
    reset(lang);recreate();val save=GardenSave(context)
    onUi{
     val labels=views(activity.window.decorView).filterIsInstance<Button>().map{it.text.toString()}
     assertTrue(labels.contains(GameText.get(lang,"build")));assertTrue(labels.contains(GameText.get(lang,"settings")))
     clickPrimary()
     val board=views(activity.window.decorView).filterIsInstance<Match3BoardView>().single()
     assertEquals(GameText.get(lang,"match_board"),board.contentDescription)
     assertTrue(views(activity.window.decorView).filterIsInstance<MaterialObjectiveHud>().single().contentDescription.toString().contains(GameText.get(lang,"reward")))
     activity.onBackPressedDispatcher.onBackPressed()
    }
    val solved=Match3Engine(1,Random(344));repeat(500){if(!solved.won)solved.boost(Match3Engine.Booster.COLOR,it%64)};assertTrue(solved.won);save.saveBoard(solved)
    onUi{
     clickPrimary()
     val labels=views(activity.window.decorView).filterIsInstance<TextView>().map{it.text.toString()}
     assertTrue("$lang victory",labels.contains(GameText.get(lang,"level_complete")))
     assertTrue("$lang material",labels.any{it.contains(FountainText.get(lang,"PIPE"))&&it.contains("/4")})
     views(activity.window.decorView).filterIsInstance<Button>().first{it.text.toString()==GameText.get(lang,"continue")}.performClick()
     assertTrue(views(activity.window.decorView).filterIsInstance<Button>().any{it.text.toString()==GameText.get(lang,"build")})
    }
    assertEquals(2,save.level);assertEquals(150,save.coins);assertEquals(2,save.materialCount(Material.PIPE))
   }
   onUi{views(activity.window.decorView).filterIsInstance<SceneLifecycle>().forEach{it.pauseScene()}}
  }
 }
}
