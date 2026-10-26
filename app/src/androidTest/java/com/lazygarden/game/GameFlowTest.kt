package com.lazygarden.game

import android.content.Context
import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
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
  instrument.waitForIdleSync()
  val image=instrument.uiAutomation.takeScreenshot()
  val file=File(context.filesDir,"$name.png")
  file.outputStream().use{image.compress(Bitmap.CompressFormat.PNG,100,it)}
  // UTP uninstalls the APK after tests. Keep QA screenshots outside app-owned storage.
  val command="sh -c 'mkdir -p /data/local/tmp/lazygarden-screenshots; run-as com.lazygarden.game cat ${file.absolutePath} > /data/local/tmp/lazygarden-screenshots/$name.png'"
  ParcelFileDescriptor.AutoCloseInputStream(instrument.uiAutomation.executeShellCommand(command)).use{it.readBytes()}
 }
 @Test fun nativeNavigationAndBoardAreVisible(){
  ActivityScenario.launch(MainActivity::class.java).use{scenario->
   onView(withText("LazyGarden")).check(matches(isDisplayed()));shot("01-home-before")
   onView(withText("Nivelet")).perform(click())
   onView(withText("Kapitulli 1 · 1–25")).check(matches(isDisplayed()));shot("02-map")
   onView(withText("‹ Kopshti")).perform(click())
   onView(withText("▶ Luaj · 1")).perform(click())
   onView(withContentDescription("Tabela Match-3")).check(matches(isDisplayed()));shot("03-match")
   scenario.recreate()
   onView(withText("LazyGarden")).check(matches(isDisplayed()))
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
   shot("05-fountain-restored")
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
  ActivityScenario.launch(MainActivity::class.java).use{shot("06-estate-after")}
 }
}
