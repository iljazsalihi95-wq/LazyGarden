package com.lazygarden.game

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class LiveContentFlowTest {
 private val context get()=ApplicationProvider.getApplicationContext<Context>()
 private fun manifest(version:Int)=JSONObject().put("schemaVersion",1).put("minAppContentApi",1).put("version",version)
  .put("levels",org.json.JSONArray()).put("chapters",org.json.JSONArray()).put("assets",org.json.JSONArray()).put("events",org.json.JSONArray())
 @Before fun reset(){File(context.filesDir,"live_content").deleteRecursively();GardenContent.load(context)}
 @Test fun activationWaitsForNextLaunchAndRejectsDowngrades(){
  assertTrue(GardenContent.stage(context,manifest(2).toString()));assertNull(GardenContent.active)
  GardenContent.load(context);assertEquals(2,GardenContent.active!!.version)
  assertFalse(GardenContent.stage(context,manifest(1).toString()));GardenContent.load(context);assertEquals(2,GardenContent.active!!.version)
 }
 @Test fun badReleaseCannotReplaceLastWorkingVersion(){
  assertTrue(GardenContent.stage(context,manifest(1).toString()))
  val bad=manifest(2).put("schemaVersion",99)
  assertTrue(runCatching{GardenContent.stage(context,bad.toString())}.isFailure)
  GardenContent.load(context);assertEquals(1,GardenContent.active!!.version)
  val asset=JSONObject().put("id","garden_before").put("kind","image").put("url","https://raw.githubusercontent.com/iljazsalihi95-wq/LazyGarden/main/no.webp").put("sha256","0".repeat(64))
  val corrupt=manifest(3).put("assets",org.json.JSONArray().put(asset))
  assertTrue(runCatching{GardenContent.stage(context,corrupt.toString()){_,_->byteArrayOf(1,2)}}.isFailure)
  GardenContent.load(context);assertEquals(1,GardenContent.active!!.version)
 }
 @Test fun pinnedBoardKeepsOriginalGoalsAfterContentChanges(){
  val board=Match3Engine(8);val raw=board.encode();val original=board.spec
  val newSpec=JSONObject().put("id",8).put("moves",40).put("target",1800).put("ice",12).put("collectColor",2).put("collectCount",30).put("terrainKind",3).put("terrainCount",9).put("rescue",2)
  assertTrue(GardenContent.stage(context,manifest(1).put("levels",org.json.JSONArray().put(newSpec)).toString()))
  GardenContent.load(context);val next=Match3Engine(8);assertEquals(40,next.spec.moves)
  assertTrue(next.restore(raw));assertEquals(original,next.spec);assertEquals(raw,next.encode())
 }
 @Test fun invalidLevelsAndUntrustedAssetHostsAreRejected(){
  val bad=manifest(1).put("assets",org.json.JSONArray().put(JSONObject().put("id","garden_before").put("kind","image").put("url","http://example.com/a").put("sha256","0".repeat(64))))
  assertTrue(runCatching{GardenContent.parse(bad.toString())}.isFailure)
  assertFalse(GardenContent.validSpec(LevelSpec(1,100,0,0,0)))
 }
}
