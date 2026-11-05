package com.lazygarden.game

import android.content.Context
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StorySceneLifecycleTest {
    @Test fun visibleActorsWalkBeforeSpeakingAndPauseWithoutRestartingTheirArrival(){
        val context=ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("garden_progress",0).edit().clear()
            .putBoolean("voice_enabled",false).putBoolean("sfx_enabled",false).commit()
        ActivityScenario.launch(MainActivity::class.java).use{scenario->
            lateinit var scene:StorySceneView
            var cues=0
            scenario.onActivity{activity->
                scene=StorySceneView(activity,GardenSave(activity),0,2,"rexha","lazi",{true},{cues++})
                activity.setContentView(scene)
            }
            SystemClock.sleep(250)
            var checkpoint=0L
            scenario.onActivity{
                assertTrue(scene.actorIds.containsAll(listOf("rexha","lazi","meli")))
                assertEquals(CharacterAnimation.State.WALK,scene.actorState("rexha"))
                assertEquals(0,cues)
                scene.pauseScene();checkpoint=scene.sceneElapsedMillis
            }
            SystemClock.sleep(300)
            scenario.onActivity{assertEquals(checkpoint,scene.sceneElapsedMillis);scene.resumeScene()}
            SystemClock.sleep(3300)
            scenario.onActivity{
                assertEquals(1,cues);assertTrue(scene.dialogueReady)
                assertEquals(CharacterAnimation.State.TALK,scene.actorState("rexha"))
                assertEquals(CharacterAnimation.State.IDLE,scene.actorState("lazi"))
                scene.pauseScene();checkpoint=scene.sceneElapsedMillis
            }
            SystemClock.sleep(200)
            scenario.onActivity{assertEquals(checkpoint,scene.sceneElapsedMillis);scene.resumeScene()}
            SystemClock.sleep(150)
            scenario.onActivity{assertEquals(1,cues);scene.pauseScene()}
        }
    }
}
