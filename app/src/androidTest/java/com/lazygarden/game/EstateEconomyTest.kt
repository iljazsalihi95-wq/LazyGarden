package com.lazygarden.game

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EstateEconomyTest {
 private val context get()=ApplicationProvider.getApplicationContext<Context>()
 private val prefs get()=context.getSharedPreferences("garden_progress",0)
 @Before fun reset(){prefs.edit().clear().commit()}
 @Test fun keySpendsExactlyOnceAndPreservesUnspentMaterials(){
  prefs.edit().putInt("coins",500).putInt("stars",2).putInt("keys",1).putInt("tasks",1).putInt("material_FLOWER",3).commit()
  val s=GardenSave(context);assertTrue(s.beginRestorationWithKey());assertEquals(380,s.coins);assertEquals(1,s.stars);assertEquals(0,s.keys);assertEquals(3,s.materialCount(Material.FLOWER))
  assertFalse(GardenSave(context).beginRestorationWithKey());assertEquals(380,s.coins)
  s.finishRestoration();s.finishRestoration();assertEquals(2,s.tasks)
 }
 @Test fun materialPurchaseChargesOnlyMissingStockThenConstructionConsumesIt(){
  prefs.edit().putInt("coins",1000).putInt("tasks",1).putInt("stars",1).putInt("material_FLOWER",3).putInt("material_SOIL",4).commit()
  val s=GardenSave(context);assertEquals(100,s.missingMaterialCost());assertTrue(s.buyMissingMaterials());assertEquals(900,s.coins);assertEquals(8,s.materialCount(Material.FLOWER));assertFalse(s.buyMissingMaterials())
  assertTrue(s.beginRestoration());assertEquals(780,s.coins);assertEquals(0,s.stars);assertEquals(0,s.materialCount(Material.FLOWER));assertEquals(0,s.materialCount(Material.SOIL))
 }
 @Test fun insufficientFundsDoNotPartiallySpendOrChangeInventory(){
  prefs.edit().putInt("coins",20).putInt("stars",2).putInt("keys",1).putInt("tasks",1).commit()
  val s=GardenSave(context);assertFalse(s.buyMissingMaterials());assertFalse(s.beginRestorationWithKey());assertEquals(20,s.coins);assertEquals(1,s.keys);assertEquals(2,s.stars);assertFalse(s.constructing)
 }
 @Test fun previewMatchesCommittedRewardAndDailyAndChestCannotRepeat(){
  val s=GardenSave(context);val preview=s.nextMissingMaterialReward(1)!!;val b=Match3Engine(1)
  repeat(500){if(!b.won)b.boost(Match3Engine.Booster.COLOR,it%64)};assertTrue(b.won);assertTrue(s.win(b));assertEquals(preview.second,s.materialCount(preview.first.material));assertEquals(preview.second,s.lastMaterialReward!!.after)
  assertNotNull(s.claimDailyReward());val afterDaily=s.coins;assertNull(s.claimDailyReward());assertEquals(afterDaily,s.coins)
  prefs.edit().putInt("chest_progress",5).commit();val keys=s.keys;assertNotNull(s.openRewardChest());assertEquals(keys+1,s.keys);val coins=s.coins;assertNull(s.openRewardChest());assertEquals(coins,s.coins)
 }
}
