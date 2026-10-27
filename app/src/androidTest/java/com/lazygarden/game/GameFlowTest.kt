package com.lazygarden.game

import android.content.Context
import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class GameFlowTest {
 private val context get()=ApplicationProvider.getApplicationContext<Context>()
 @Before fun reset(){context.getSharedPreferences("garden_progress",Context.MODE_PRIVATE).edit().clear().commit()}
 private fun shot(name:String){
  val instrument=InstrumentationRegistry.getInstrumentation()
  SystemClock.sleep(1000) // Let window entrance/reward finish; a living scene never becomes idle.
  val image=instrument.uiAutomation.takeScreenshot()
  val file=File(context.getExternalFilesDir(null),"$name.png")
  file.outputStream().use{image.compress(Bitmap.CompressFormat.PNG,100,it)}
  // UTP uninstalls the APK after tests. Keep QA screenshots outside app-owned storage.
  for(command in listOf("mkdir -p /data/local/tmp/lazygarden-screenshots","cp ${file.absolutePath} /data/local/tmp/lazygarden-screenshots/$name.png")){
   ParcelFileDescriptor.AutoCloseInputStream(instrument.uiAutomation.executeShellCommand(command)).use{it.readBytes()}
  }
 }
 private fun recordFountain():Thread {
  val ui=InstrumentationRegistry.getInstrumentation().uiAutomation
  ParcelFileDescriptor.AutoCloseInputStream(ui.executeShellCommand("mkdir -p /data/local/tmp/lazygarden-screenshots")).use{it.readBytes()}
  val output=ui.executeShellCommand("screenrecord --size 720x1280 --bit-rate 1800000 --time-limit 35 /data/local/tmp/lazygarden-screenshots/15-fountain-sequence.mp4")
  return Thread{ParcelFileDescriptor.AutoCloseInputStream(output).use{it.readBytes()}}.apply{start()}
 }
 @Test fun nativeNavigationAndBoardAreVisible(){
  ActivityScenario.launch(MainActivity::class.java).use{scenario->
   onView(withContentDescription("Logo LazyGarden")).check(matches(isDisplayed()));shot("01-home-before")
   onView(withText("Nivelet")).perform(click())
   onView(withText("Kapitulli 1 · 1–25")).check(matches(isDisplayed()));shot("02-map")
   onView(withText("‹ Kopshti")).perform(click())
   onView(withText("▶ Luaj · 1")).perform(click())
   onView(withContentDescription("Tabela Match-3")).check(matches(isDisplayed()));shot("03-match")
   scenario.recreate()
   onView(withContentDescription("Logo LazyGarden")).check(matches(isDisplayed()))
  }
 }
 @Test fun solvedSaveAwardsOnceAndRestorationSurvivesRecreation(){
  val board=Match3Engine(1,Random(404))
  repeat(500){if(!board.won)board.boost(Match3Engine.Booster.COLOR,it%64)}
  assertTrue(board.won)
  val save=GardenSave(context);save.saveBoard(board)
  ActivityScenario.launch(MainActivity::class.java).use{scenario->
   onView(withText("▶ Vazhdo · 1")).perform(click())
   onView(withText("Niveli i përfunduar!")).check(matches(isDisplayed()));shot("04-victory")
   assertEquals(150,save.coins);assertEquals(2,save.level);assertEquals(1,save.stars)
   assertFalse(save.win(board));assertEquals(150,save.coins)
   onView(withText("Vazhdo")).perform(click())
   assertTrue(save.beginRestoration());save.finishRestoration()
   scenario.recreate();assertEquals(1,GardenSave(context).tasks);assertEquals(30,save.coins)
   onView(withText("Lexo më vonë")).perform(click())
   shot("05-fountain-restored")
  }
 }
 @Test fun fountainChoreographyCompletesOnlyAfterWaterAndSurvivesInterruption(){
  context.getSharedPreferences("garden_progress",Context.MODE_PRIVATE).edit().putInt("coins",150).putInt("stars",1).commit()
  val save=GardenSave(context);assertTrue(save.beginRestoration())
  val main=android.os.Handler(android.os.Looper.getMainLooper())
  val finished=java.util.concurrent.CountDownLatch(1)
  val registered=java.util.concurrent.CountDownLatch(1)
  val error=java.util.concurrent.atomic.AtomicReference<Throwable?>()
  val pictures=java.util.concurrent.ConcurrentLinkedQueue<Thread>()
  val seen=java.util.Collections.newSetFromMap(java.util.IdentityHashMap<android.app.Activity,Boolean>())
  val monitor=androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance()
  fun checked(block:()->Unit){try{block()}catch(t:Throwable){error.compareAndSet(null,t);finished.countDown()}}
  fun capture(name:String){pictures.add(Thread{shot(name)}.apply{start()})}
  fun garden(v:android.view.View):GardenWorldView? {
   if(v is GardenWorldView)return v
   if(v is android.view.ViewGroup)for(i in 0 until v.childCount)garden(v.getChildAt(i))?.let{return it}
   return null
  }
  val observer=object:androidx.test.runner.lifecycle.ActivityLifecycleCallback {
   override fun onActivityLifecycleChanged(activity:android.app.Activity,stage:androidx.test.runner.lifecycle.Stage){
    if(activity !is MainActivity || stage!=androidx.test.runner.lifecycle.Stage.RESUMED || !seen.add(activity))return
    checked {
     when(seen.size){
      1 -> main.postDelayed({checked{assertEquals(0,save.tasks);assertTrue(save.constructing);activity.recreate()}},5500)
      2 -> {
       assertEquals(0,save.tasks);assertTrue(save.constructing);assertTrue(save.restorationElapsed>=1000)
       val live=garden(activity.findViewById(android.R.id.content))!!
       assertTrue("The replacement scene must resume its checkpoint",kotlin.math.abs(live.sceneElapsedMillis-save.restorationElapsed)<500)
       capture("12-fountain-inspect");main.postDelayed({capture("13-fountain-work")},8000)
       lateinit var poll:Runnable
       poll=Runnable{checked {
        if(save.constructing)main.postDelayed(poll,200) else {
         assertEquals(1,save.tasks);assertEquals(30,save.coins)
         save.finishRestoration();assertEquals(1,save.tasks);activity.recreate()
        }
       }};main.postDelayed(poll,500)
      }
      3 -> {
       assertTrue(GardenSave(context).fountainRestored);assertFalse(save.constructing)
       assertEquals(1,save.tasks);assertEquals(30,save.coins)
       activity.onBackPressedDispatcher.onBackPressed();capture("14-fountain-flowing");finished.countDown()
      }
     }
    }
   }
  }
  val recording=recordFountain()
  main.post{monitor.addLifecycleCallback(observer);registered.countDown()}
  assertTrue(registered.await(5,java.util.concurrent.TimeUnit.SECONDS))
  try {
   // Launch directly: ActivityScenario.launch itself waits for idle, which a continuously
   // animated game need not reach until long after the entire choreography has completed.
   context.startActivity(android.content.Intent(context,MainActivity::class.java).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK))
   assertTrue("The live restoration must finish",finished.await(100,java.util.concurrent.TimeUnit.SECONDS))
   error.get()?.let{throw AssertionError("Native scene verification failed",it)}
   recording.join(40000);pictures.forEach{it.join(5000)}
  }finally{
   val closed=java.util.concurrent.CountDownLatch(1)
   main.post{monitor.removeLifecycleCallback(observer);monitor.getActivitiesInStage(androidx.test.runner.lifecycle.Stage.RESUMED).filterIsInstance<MainActivity>().forEach{it.finish()};main.removeCallbacksAndMessages(null);closed.countDown()}
   closed.await(5,java.util.concurrent.TimeUnit.SECONDS)
  }
 }
 @Test fun fountainReplayNeverChangesRewardsOrProgress(){
  context.getSharedPreferences("garden_progress",Context.MODE_PRIVATE).edit().putInt("coins",300).putInt("stars",4).putInt("tasks",1).putInt("story_position",3).commit()
  val save=GardenSave(context)
  ActivityScenario.launch(MainActivity::class.java).use{
   onView(withText("Historia")).perform(click())
   onView(withText("▶ Riluaj shatërvanin")).perform(click())
   SystemClock.sleep(29000)
   assertEquals(300,save.coins);assertEquals(4,save.stars);assertEquals(1,save.tasks);assertFalse(save.constructing)
   assertEquals(3,save.storyPosition)
  }
 }
 @Test fun allRestorationPurchasesAndBoostersPersist(){
  context.getSharedPreferences("garden_progress",Context.MODE_PRIVATE).edit().putInt("coins",2000).putInt("stars",7).commit()
  val save=GardenSave(context)
  repeat(7){assertTrue(save.beginRestoration());save.finishRestoration()}
  assertEquals(7,save.tasks);assertFalse(save.beginRestoration())
  val board=Match3Engine(1,Random(31));val coins=save.coins
  assertTrue(save.buyBooster(board,Match3Engine.Booster.HAMMER,0).isNotEmpty())
  assertEquals(coins-40,GardenSave(context).coins)
  val restored=Match3Engine(1);assertTrue(restored.restore(save.activeBoard!!));assertEquals(board.encode(),restored.encode())
  context.getSharedPreferences("garden_progress",Context.MODE_PRIVATE).edit().putInt("story_position",21).commit()
  ActivityScenario.launch(MainActivity::class.java).use{shot("06-estate-after")}
 }
 @Test fun decorationTransactionsAndWorldSelectionSurviveRecreation(){
  val prefs=context.getSharedPreferences("garden_progress",Context.MODE_PRIVATE)
  prefs.edit().putInt("coins",300).putInt("tasks",1).putInt("story_position",3).commit()
  val save=GardenSave(context)
  assertEquals(GardenSave.DecorResult.LOCKED,save.selectDecoration("bench"));assertEquals(300,save.coins)
  assertEquals(GardenSave.DecorResult.INVALID,save.selectDecoration("missing"));assertEquals(300,save.coins)
  assertEquals(GardenSave.DecorResult.PURCHASED,save.selectDecoration("classic_fountain"));assertEquals(150,save.coins)
  assertEquals(GardenSave.DecorResult.SELECTED,save.selectDecoration("classic_fountain"));assertEquals(150,save.coins)
  assertEquals(GardenSave.DecorResult.INSUFFICIENT,save.selectDecoration("lotus_fountain"));assertEquals(150,save.coins)
  assertEquals("classic_fountain",save.selectedDecoration(DecorCategory.FOUNTAINS)?.id)
  ActivityScenario.launch(MainActivity::class.java).use{scenario->
   onView(withText("Dekoro")).perform(click())
   onView(withContentDescription("Kategoria FOUNTAINS")).perform(click());shot("07-decoration-shop")
   onView(withContentDescription("Shiko lotus_fountain")).perform(click())
   assertEquals("classic_fountain",GardenSave(context).selectedDecoration(DecorCategory.FOUNTAINS)?.id)
   assertEquals(150,save.coins)
   onView(withText("‹ Kopshti")).perform(click());scenario.recreate()
   assertEquals("classic_fountain",GardenSave(context).selectedDecoration(DecorCategory.FOUNTAINS)?.id)
   onView(withContentDescription("Shiko pronën")).perform(click())
   shot("08-garden-with-decoration")
  }
  save.removeDecoration(DecorCategory.FOUNTAINS);assertNull(save.selectedDecoration(DecorCategory.FOUNTAINS));assertTrue(save.ownsDecoration("classic_fountain"))
  assertEquals(GardenSave.DecorResult.SELECTED,save.selectDecoration("classic_fountain"));assertEquals(150,save.coins)
 }
 @Test fun storyContinuesAtSavedLineAndReplayDoesNotAdvance(){
  context.getSharedPreferences("garden_progress",Context.MODE_PRIVATE).edit().putInt("tasks",1).commit()
  val save=GardenSave(context)
  ActivityScenario.launch(MainActivity::class.java).use{scenario->
   onView(withContentDescription("Personazhi Meli")).check(matches(isDisplayed()));shot("09-story-meli")
   onView(withText("Vazhdo tregimin")).perform(click());assertEquals(1,save.storyPosition)
   scenario.recreate();onView(withContentDescription("Personazhi Bejdi")).check(matches(isDisplayed()));shot("10-story-resumed")
   assertFalse(save.advanceStory(0));assertEquals(1,save.storyPosition)
   onView(withText("Vazhdo tregimin")).perform(click())
   onView(withText("Vazhdo tregimin")).perform(click());assertEquals(3,save.storyPosition);assertFalse(save.hasPendingStory)
   onView(withText("Historia")).perform(click());onView(withContentDescription("Kapitulli 1")).perform(click())
   onView(withContentDescription("Personazhi Meli")).check(matches(isDisplayed()))
   onView(withText("Vazhdo tregimin")).perform(click());assertEquals(3,save.storyPosition)
  }
 }
 @Test fun allNewCatalogAndCharacterArtworkDecodes(){
  val ids=GardenDecor.items.map{it.art}+GardenCharacter.entries.map{it.portrait}
  for(id in ids){val bmp=android.graphics.BitmapFactory.decodeResource(context.resources,id);assertNotNull(bmp);assertTrue(bmp.width>100);bmp.recycle()}
 }
 @Test fun realSwipeConsumesOneMoveAndInvalidSwapReturnsWithoutCost(){
  ActivityScenario.launch(MainActivity::class.java).use{
   onView(withText("▶ Luaj · 1")).perform(click())
   onView(withContentDescription("Tabela Match-3")).perform(object:androidx.test.espresso.ViewAction{
    override fun getDescription()="Play a legal swipe through the native game view"
    override fun getConstraints()=isDisplayed()
    override fun perform(controller:androidx.test.espresso.UiController,view:android.view.View){
     val board=view as Match3BoardView;val e=board.engine
     val invalid=(0..63).flatMap{a->listOf(a to a+1,a to a+8)}.first{(a,b)->b<64 && kotlin.math.abs(a/8-b/8)+kotlin.math.abs(a%8-b%8)==1 && !e.legal(a,b)}
     fun swipe(pair:Pair<Int,Int>){
      val from=board.cellCenter(pair.first);val to=board.cellCenter(pair.second);val now=SystemClock.uptimeMillis()
      board.dispatchTouchEvent(android.view.MotionEvent.obtain(now,now,android.view.MotionEvent.ACTION_DOWN,from.x,from.y,0))
      board.dispatchTouchEvent(android.view.MotionEvent.obtain(now,now+90,android.view.MotionEvent.ACTION_UP,to.x,to.y,0))
     }
     val state=e.encode();swipe(invalid);controller.loopMainThreadForAtLeast(400);assertEquals(state,e.encode())
     val legal=(0..63).flatMap{a->listOf(a to a+1,a to a+8)}.first{e.legal(it.first,it.second)}
     val moves=e.moves;swipe(legal);controller.loopMainThreadForAtLeast(1000)
     assertEquals(moves-1,e.moves);assertTrue(e.score>0)
    }
   });shot("11-match-after-real-swipe")
  }
 }
}
