package com.lazygarden.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class RestorationFlowTest {
 private val ctx get()=ApplicationProvider.getApplicationContext<Context>()
 @Before fun clear(){ctx.getSharedPreferences("garden_progress",0).edit().clear().commit()}
 private fun stock(task:Int){val prefs=ctx.getSharedPreferences("garden_progress",0);RestorationInventory.forTask(task).forEach{prefs.edit().putInt("material_${it.material.name}",it.count).commit()}}
 @Test fun allRestorationsSpendOnceAndRemainCompleted(){val prefs=ctx.getSharedPreferences("garden_progress",0);prefs.edit().putInt("coins",2000).putInt("stars",8).commit();val s=GardenSave(ctx)
  for(task in 0..6){assertEquals(task,s.tasks);stock(task);assertTrue(s.restorationMaterialsReady());assertTrue(s.beginRestoration());RestorationInventory.forTask(task).forEach{assertEquals(0,s.materialCount(it.material))};val coins=s.coins;assertFalse(s.beginRestoration());if(task==1){assertTrue(s.chooseFlower("lavender"));assertFalse(s.chooseFlower("fake"))};s.checkpointRestoration(12345);assertEquals(12345L,GardenSave(ctx).restorationElapsed);s.finishRestoration();s.finishRestoration();assertEquals(task+1,GardenSave(ctx).tasks);assertEquals(coins,s.coins);assertFalse(s.constructing)}
  assertFalse(s.beginRestoration());assertEquals("lavender",GardenSave(ctx).flowerChoice);assertTrue(s.flowerSceneComplete)
 }
 @Test fun rewardReceiptsRejectCancelUnknownAndDuplicateCallbacks(){val s=GardenSave(ctx);val a=s.beginAdReward("coins")!!;assertEquals(0,s.coins);assertFalse(s.settleAdReward("unknown"));s.cancelAdReward(a);assertFalse(s.settleAdReward(a));assertEquals(0,s.coins);val b=s.beginAdReward("coins")!!;assertTrue(GardenSave(ctx).settleAdReward(b));assertFalse(s.settleAdReward(b));assertEquals(100,s.coins);val star=s.beginAdReward("star")!!;assertTrue(s.settleAdReward(star));assertEquals(1,s.stars);assertNull(s.beginAdReward("cash"));assertNull(s.beginAdReward("life"));assertTrue(s.spendLife());val life=s.beginAdReward("life")!!;assertTrue(s.settleAdReward(life));assertFalse(s.settleAdReward(life));assertEquals(s.maxLives,s.lives)}
 @Test fun rewardedMovesKeepTheExactSavedBoard(){val s=GardenSave(ctx);val e=Match3Engine(1);s.saveBoard(e);val old=e.encode().split(',');val id=s.beginAdReward("moves")!!;assertTrue(s.settleAdReward(id));assertFalse(s.settleAdReward(id));val copy=Match3Engine(1);assertTrue(copy.restore(s.activeBoard!!));assertEquals(e.moves+5,copy.moves);assertEquals(old.drop(2),copy.encode().split(',').drop(2))}
 @Test fun texturedScenesRenderTheirDistinctNativeStages(){val instrument=InstrumentationRegistry.getInstrumentation();val error=java.util.concurrent.atomic.AtomicReference<Throwable?>();val latch=CountDownLatch(1)
  android.os.Handler(android.os.Looper.getMainLooper()).post{try{val themed=android.view.ContextThemeWrapper(ctx,R.style.Theme_LazyGarden)
   for(task in 2..6){val plan=RestorationSequence.plan(task);for(ms in listOf(0L,plan.duration/2,plan.duration-1500)){val view=RestorationGardenView(themed,task,ms);view.measure(View.MeasureSpec.makeMeasureSpec(720,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1280,View.MeasureSpec.EXACTLY));view.layout(0,0,720,1280);val b=Bitmap.createBitmap(720,1280,Bitmap.Config.ARGB_8888);view.draw(Canvas(b));assertEquals("Camera must cover the viewport",255,android.graphics.Color.alpha(b.getPixel(360,8)));val dir=File(ctx.getExternalFilesDir(null),"native-stages").apply{mkdirs()};File(dir,"scene-${task+1}-$ms.png").outputStream().use{b.compress(Bitmap.CompressFormat.PNG,95,it)};b.recycle();view.dispose()}}
  }catch(t:Throwable){error.set(t)}finally{latch.countDown()}}
  assertTrue(latch.await(90,TimeUnit.SECONDS));error.get()?.let{throw it}
  export(File(ctx.getExternalFilesDir(null),"native-stages"))
 }
 @Test fun flowerPickerAndPlantingRenderTexturedArtwork(){val done=CountDownLatch(1);val error=java.util.concurrent.atomic.AtomicReference<Throwable?>();val instrument=InstrumentationRegistry.getInstrumentation()
  android.os.Handler(android.os.Looper.getMainLooper()).post{try{val themed=android.view.ContextThemeWrapper(ctx,R.style.Theme_LazyGarden);for(ms in listOf(8000L,18000L,27000L)){val v=FlowerGardenView(themed,initialChoice=if(ms==8000L)null else "lavender",initialElapsed=ms);v.measure(View.MeasureSpec.makeMeasureSpec(720,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1280,View.MeasureSpec.EXACTLY));v.layout(0,0,720,1280);val b=Bitmap.createBitmap(720,1280,Bitmap.Config.ARGB_8888);v.draw(Canvas(b));File(ctx.getExternalFilesDir(null),"flowers-$ms.png").outputStream().use{b.compress(Bitmap.CompressFormat.PNG,95,it)};v.dispose();b.recycle()}}catch(t:Throwable){error.set(t)}finally{done.countDown()}}
  assertTrue(done.await(45,TimeUnit.SECONDS));error.get()?.let{throw it};export(ctx.getExternalFilesDir(null)!!,"flowers-")
 }
 private fun export(dir:File,prefix:String=""){val ui=InstrumentationRegistry.getInstrumentation().uiAutomation;val files=dir.listFiles()?.filter{it.name.startsWith(prefix)&&it.extension=="png"}.orEmpty();assertTrue(files.isNotEmpty());val commands=listOf("mkdir -p /data/local/tmp/lazygarden-screenshots")+files.map{"cp ${it.absolutePath} /data/local/tmp/lazygarden-screenshots/${it.name}"};for(command in commands)android.os.ParcelFileDescriptor.AutoCloseInputStream(ui.executeShellCommand(command)).use{it.readBytes()};for(file in files){val target="/data/local/tmp/lazygarden-screenshots/${file.name}";val listed=android.os.ParcelFileDescriptor.AutoCloseInputStream(ui.executeShellCommand("ls $target")).use{it.readBytes().toString(Charsets.UTF_8).trim()};assertEquals(target,listed)}}
 @Test fun pathRoutePausesAndResumesAcrossRealActivityRecreation(){val s=GardenSave(ctx);ctx.getSharedPreferences("garden_progress",0).edit().putInt("tasks",2).putBoolean("constructing",true).putLong("restoration_elapsed",11000).putBoolean("voice_enabled",false).putBoolean("sfx_enabled",false).commit()
  val main=android.os.Handler(android.os.Looper.getMainLooper());val done=CountDownLatch(1);val registered=CountDownLatch(1);val error=java.util.concurrent.atomic.AtomicReference<Throwable?>();val monitor=androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance();var launches=0;var checkpoint=0L
  fun scene(v:View):RestorationGardenView? {if(v is RestorationGardenView)return v;if(v is android.view.ViewGroup)for(i in 0 until v.childCount)scene(v.getChildAt(i))?.let{return it};return null}
  fun check(block:()->Unit){try{block()}catch(t:Throwable){error.set(t);done.countDown()}}
  val callback=object:androidx.test.runner.lifecycle.ActivityLifecycleCallback{override fun onActivityLifecycleChanged(a:android.app.Activity,stage:androidx.test.runner.lifecycle.Stage){if(a !is MainActivity||stage!=androidx.test.runner.lifecycle.Stage.RESUMED)return;launches++;if(launches==1)main.postDelayed({check{val v=scene(a.window.decorView)!!;assertTrue(v is PathGardenView);v.pauseScene();checkpoint=s.restorationElapsed;main.postDelayed({check{assertEquals(checkpoint,v.sceneElapsedMillis);assertEquals(2,s.tasks);a.recreate()}},400)}},500) else if(launches==2)main.postDelayed({check{val v=scene(a.window.decorView)!!;assertTrue(v.sceneElapsedMillis>=checkpoint);assertTrue(v.sceneElapsedMillis<checkpoint+1800);assertTrue(s.constructing);a.finish();done.countDown()}},300)}}
  main.post{monitor.addLifecycleCallback(callback);registered.countDown()};assertTrue(registered.await(10,TimeUnit.SECONDS));try{ctx.startActivity(android.content.Intent(ctx,MainActivity::class.java).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK));assertTrue(done.await(30,TimeUnit.SECONDS));error.get()?.let{throw it}}finally{main.post{monitor.removeLifecycleCallback(callback)}}
 }
}
