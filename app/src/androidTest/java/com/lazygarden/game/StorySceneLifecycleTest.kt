package com.lazygarden.game

import android.content.Context
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(AndroidJUnit4::class)
class StorySceneLifecycleTest {
    @Test fun visibleActorsWalkBeforeSpeakingAndPauseWithoutRestartingTheirArrival(){
        val context=ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("garden_progress",0).edit().clear()
            .putBoolean("voice_enabled",false).putBoolean("sfx_enabled",false).commit()
        ActivityScenario.launch(MainActivity::class.java).use{scenario->
            lateinit var scene:StorySceneView
            var cues=0
            val sawWalk=AtomicBoolean(false);val sawTalk=AtomicBoolean(false)
            scenario.onActivity{activity->
                scene=StorySceneView(activity,GardenSave(activity),0,2,"rexha","lazi",{true},{assertTrue(sawWalk.get());cues++})
                scene.onActorRendered={id,state->if(id=="rexha"){
                    if(state==CharacterAnimation.State.WALK)sawWalk.set(true)
                    if(state==CharacterAnimation.State.TALK)sawTalk.set(true)
                }}
                activity.setContentView(scene)
            }
            waitUntil(sawWalk)
            var checkpoint=0L
            scenario.onActivity{
                assertTrue(scene.actorIds.containsAll(listOf("rexha","lazi","meli")))
                assertTrue(sawWalk.get())
                scene.pauseScene();checkpoint=scene.sceneElapsedMillis
            }
            SystemClock.sleep(300)
            scenario.onActivity{assertEquals(checkpoint,scene.sceneElapsedMillis);scene.resumeScene()}
            waitUntil(sawTalk)
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
    private fun waitUntil(flag:AtomicBoolean){
        val deadline=SystemClock.uptimeMillis()+15000
        while(!flag.get()&&SystemClock.uptimeMillis()<deadline)SystemClock.sleep(50)
        assertTrue("Expected a real rendered animation state before the deadline",flag.get())
    }
}
