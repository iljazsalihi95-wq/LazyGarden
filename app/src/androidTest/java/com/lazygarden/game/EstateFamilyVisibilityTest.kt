package com.lazygarden.game

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EstateFamilyVisibilityTest {
 @Test fun aNewGardenHasTheFamilyBeforeAnyQuestOrRestoration(){
  val context=ApplicationProvider.getApplicationContext<Context>()
  context.getSharedPreferences("garden_progress",0).edit().clear().putBoolean("voice_enabled",false).putBoolean("sfx_enabled",false).commit()
  fun all(view:View):List<View> = listOf(view)+if(view is ViewGroup)(0 until view.childCount).flatMap{all(view.getChildAt(it))}else emptyList()
  ActivityScenario.launch(MainActivity::class.java).use{scenario->
   scenario.onActivity{activity->
    val world=all(activity.window.decorView).filterIsInstance<GardenWorldView>().single()
    assertEquals(setOf("lazi","meli","cufi","rona"),world.residentCast.toSet())
    assertFalse(world.residentCast.contains("rexha"))
    world.pauseScene()
   }
   scenario.recreate()
   scenario.onActivity{activity->
    val world=all(activity.window.decorView).filterIsInstance<GardenWorldView>().single()
    assertTrue(world.residentCast.containsAll(listOf("lazi","meli","cufi","rona")))
    world.pauseScene()
   }
  }
 }
}
