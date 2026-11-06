package com.lazygarden.game

import android.content.Context
import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Real native frames at the choice, planting and finished terrain checkpoints. */
@RunWith(AndroidJUnit4::class)
class FlowerTerrainRenderTest {
    @Test fun sideBedAndCrewAtEveryConstructionStage(){
        val context=ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("garden_progress",0).edit().clear().putBoolean("voice_enabled",false).putBoolean("sfx_enabled",false).commit()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            for((name,time)in listOf("choice" to 14000L,"planting" to 24000L,"watering" to 28500L,"finished" to 33500L)){
                lateinit var scene:FlowerGardenView
                scenario.onActivity{activity->scene=FlowerGardenView(activity,initialChoice=if(name=="choice")null else "tulip",initialElapsed=time);activity.setContentView(scene);scene.pauseScene()}
                InstrumentationRegistry.getInstrumentation().waitForIdleSync()
                SystemClock.sleep(300)
                scenario.onActivity{scene.pauseScene()}
                capture(context,"terrain-flowers-$name")
            }
        }
    }

    @Test fun pavingRestorationLeavesFlowerBedAndRightBankUntouched(){
        val mask=Bitmap.createBitmap(256,384,Bitmap.Config.ARGB_8888)
        fun alphaAt(x:Float,y:Float)=android.graphics.Color.alpha(mask.getPixel((x*256).toInt(),(y*384).toInt()))
        GardenBackdrop.paintRestorationMask(mask,2)
        val originalBed=alphaAt(.16f,.73f)
        assertEquals(0,alphaAt(.55f,.74f))
        GardenBackdrop.paintRestorationMask(mask,3)
        assertEquals(255,alphaAt(.55f,.74f))
        assertEquals(originalBed,alphaAt(.16f,.73f))
        assertEquals(0,alphaAt(.94f,.77f))
        mask.recycle()

        val context=ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("garden_progress",0).edit().clear()
            .putBoolean("voice_enabled",false).putBoolean("sfx_enabled",false).commit()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            for((name,time) in listOf("inspect" to 5000L,"laying" to 24000L,"finished" to 33500L)){
                scenario.onActivity { activity ->
                    val scene=PathGardenView(activity,initialElapsed=time)
                    activity.setContentView(scene)
                    scene.pauseScene()
                }
                InstrumentationRegistry.getInstrumentation().waitForIdleSync()
                SystemClock.sleep(300)
                capture(context,"terrain-path-$name")
            }
        }
    }

    private fun capture(context:Context,name:String){
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val image=instrumentation.uiAutomation.takeScreenshot()
        val file=File(context.getExternalFilesDir(null),"$name.png")
        file.outputStream().use{image.compress(Bitmap.CompressFormat.PNG,100,it)}
        image.recycle()
        for(command in listOf("mkdir -p /data/local/tmp/lazygarden-screenshots","cp ${file.absolutePath} /data/local/tmp/lazygarden-screenshots/${file.name}")){
            ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command)).use{it.readBytes()}
        }
    }
}
