package com.lazygarden.game
import android.content.Context
import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import android.graphics.Canvas
import androidx.core.view.doOnLayout
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class ConstructionBoardRenderTest {
 @Test fun constructionTokensAndObstacleSitesRenderInTheRealNativeBoard(){
  val context=ApplicationProvider.getApplicationContext<Context>()
  context.getSharedPreferences("garden_progress",0).edit().clear().putBoolean("voice_enabled",false).putBoolean("sfx_enabled",false).commit()
  ActivityScenario.launch(MainActivity::class.java).use{scenario->
   for(level in listOf(1,8,26,80)){
    lateinit var board:Match3BoardView
    val rendered=CountDownLatch(1);val failure=AtomicReference<Throwable?>()
    val file=File(context.getExternalFilesDir(null),"construction-match3-$level.png")
    scenario.onActivity{activity->
     val engine=Match3Engine(level,Random(level))
     assertEquals(engine.spec.terrainCount,engine.terrainRemaining)
     board=Match3BoardView(activity,engine,{},{})
     activity.setContentView(board);board.pauseScene()
     // Render the actual laid-out Android View. Window screenshots can capture the
     // preceding level's surface while the emulator GPU is still presenting it.
     board.doOnLayout{
      try{
       assertEquals(level,board.engine.level);assertTrue(board.isShown)
       val image=Bitmap.createBitmap(board.width,board.height,Bitmap.Config.ARGB_8888)
       board.draw(Canvas(image))
       file.outputStream().use{image.compress(Bitmap.CompressFormat.PNG,100,it)};image.recycle()
      }catch(error:Throwable){failure.set(error)}finally{rendered.countDown()}
     }
    }
    assertTrue("The requested level must finish native rendering",rendered.await(20,TimeUnit.SECONDS))
    failure.get()?.let{throw it};assertTrue(file.length()>10000)
    val instrumentation=InstrumentationRegistry.getInstrumentation()
    for(command in listOf("mkdir -p /data/local/tmp/lazygarden-screenshots","cp ${file.absolutePath} /data/local/tmp/lazygarden-screenshots/${file.name}")){
     ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command)).use{it.readBytes()}
    }
   }
  }
 }
}
