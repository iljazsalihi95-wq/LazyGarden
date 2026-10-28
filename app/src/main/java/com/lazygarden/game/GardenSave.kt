package com.lazygarden.game

import android.content.Context
import java.util.Calendar

class GardenSave(ctx:Context) {
 private val p=ctx.getSharedPreferences("garden_progress",Context.MODE_PRIVATE)
 val coins get()=p.getInt("coins",0)
 val xp get()=p.getInt("xp",0).coerceAtLeast(0)
 val stars get()=p.getInt("stars",0)
 val level get()=p.getInt("level",1).coerceIn(1,Campaign.COUNT)
 val tasks get()=p.getInt("tasks",if(p.getBoolean("fountain",false))1 else 0)
 val fountainRestored get()=tasks>0
 val constructing get()=p.getBoolean("constructing",false)
 val restorationElapsed get()=p.getLong("restoration_elapsed",0L).coerceAtLeast(0L)
 val flowerChoice get()=p.getString("flower_choice",null)
 val flowerSceneActive get()=constructing && tasks==1
 val flowerSceneComplete get()=tasks>1 && flowerChoice!=null
 fun checkpointRestoration(ms:Long){if(constructing)p.edit().putLong("restoration_elapsed",ms.coerceAtLeast(0L)).commit()}
 fun chooseFlower(id:String):Boolean {if(!flowerSceneActive||FlowerSequence.choices.none{it.id==id})return false;return p.edit().putString("flower_choice",id).commit()}
 val language get()=p.getString("language","sq")?:"sq"
 val voiceEnabled get()=p.getBoolean("voice_enabled",true)
 val sfxEnabled get()=p.getBoolean("sfx_enabled",true)
 fun setLanguage(value:String){if(value in listOf("sq","de","it","mk","tr"))p.edit().putString("language",value).commit()}
 fun setAudio(voice:Boolean,sfx:Boolean){p.edit().putBoolean("voice_enabled",voice).putBoolean("sfx_enabled",sfx).commit()}
 fun materialCount(m:Material)=p.getInt("material_${m.name}",0).coerceAtLeast(0)
 fun restorationNeeds(task:Int=tasks)=RestorationInventory.forTask(task)
 fun missingRestorationMaterials(task:Int=tasks)=restorationNeeds(task).filter{materialCount(it.material)<it.count}
 fun restorationMaterialsReady(task:Int=tasks)=missingRestorationMaterials(task).isEmpty()
 fun materialStatus(task:Int=tasks)=restorationNeeds(task).joinToString("\n"){"${it.material.label}: ${materialCount(it.material)}/${it.count}"}
 data class MaterialReward(val material:Material,val before:Int,val after:Int,val required:Int)
 val lastMaterialReward:MaterialReward? get(){
  val material=runCatching{Material.valueOf(p.getString("reward_material",null)?:return null)}.getOrNull()?:return null
  return MaterialReward(material,p.getInt("reward_material_before",0),p.getInt("reward_material_after",0),p.getInt("reward_material_required",0))
 }
 private fun grantMaterial(m:Material,count:Int,edit:android.content.SharedPreferences.Editor){if(count>0)edit.putInt("material_${m.name}",materialCount(m)+count)}
 private fun consumeRestorationMaterials(task:Int,edit:android.content.SharedPreferences.Editor){restorationNeeds(task).forEach{edit.putInt("material_${it.material.name}",(materialCount(it.material)-it.count).coerceAtLeast(0))}}
 /** A pending receipt is minted before one SDK show. Only that completed impression can settle it. */
 fun beginAdReward(kind:String):String? {if(constructing||(kind=="life"&&lives>=maxLives)||kind !in setOf("coins","star","moves","life"))return null;val id=java.util.UUID.randomUUID().toString();return if(p.edit().putString("ad_pending",id).putString("ad_kind",kind).commit())id else null}
 fun settleAdReward(id:String):Boolean {if(p.getString("ad_pending",null)!=id)return false;val kind=p.getString("ad_kind","");val edit=p.edit().remove("ad_pending").remove("ad_kind");when(kind){"coins"->edit.putInt("coins",coins+100);"star"->edit.putInt("stars",stars+1);"life"->edit.putInt("lives",(lives+1).coerceAtMost(maxLives));"moves"->{val board=Match3Engine(level);if(activeBoard==null||!board.restore(activeBoard!!)||!board.addMoves(5))return false;edit.putString("active_board",board.encode())};else->return false};return edit.commit()}
 fun cancelAdReward(id:String){if(p.getString("ad_pending",null)==id)p.edit().remove("ad_pending").remove("ad_kind").commit()}
 val activeBoard get()=p.getString("active_board",null)
 val maxLives get()=5
 val lives get()=p.getInt("lives",maxLives).coerceIn(0,maxLives)
 val keys get()=p.getInt("keys",0).coerceAtLeast(0)
 val tickets get()=p.getInt("event_tickets",0).coerceAtLeast(0)
 val chestProgress get()=p.getInt("chest_progress",0).coerceIn(0,5)
 val dailyStreak get()=p.getInt("daily_streak",0).coerceIn(0,7)
 private fun dayStamp():Int {val c=Calendar.getInstance();return c.get(Calendar.YEAR)*1000+c.get(Calendar.DAY_OF_YEAR)}
 val dailyRewardAvailable get()=p.getInt("daily_claim_day",-1)!=dayStamp()
 data class DailyReward(val coins:Int,val keys:Int,val tickets:Int,val streak:Int)
 fun claimDailyReward():DailyReward? {if(!dailyRewardAvailable)return null;val previous=p.getInt("daily_claim_day",-10);val today=dayStamp();val next=if(previous==today-1)(dailyStreak%7)+1 else 1;val rewardCoins=75+next*25;val rewardKeys=if(next==7)2 else if(next==4)1 else 0;val rewardTickets=if(next>=3)1 else 0;p.edit().putInt("daily_claim_day",today).putInt("daily_streak",next).putInt("coins",coins+rewardCoins).putInt("keys",keys+rewardKeys).putInt("event_tickets",tickets+rewardTickets).commit();return DailyReward(rewardCoins,rewardKeys,rewardTickets,next)}
 fun spendLife():Boolean {if(lives<=0)return false;return p.edit().putInt("lives",lives-1).commit()}
 fun rewardLife(){p.edit().putInt("lives",(lives+1).coerceAtMost(maxLives)).commit()}
 fun grantRewardedCoins(amount:Int=100):Boolean {if(amount<=0)return false;return p.edit().putInt("coins",coins+amount).commit()}
 data class ChestReward(val coins:Int,val keys:Int)
 fun openRewardChest():ChestReward? {if(chestProgress<5)return null;val reward=ChestReward(300,1);p.edit().putInt("chest_progress",0).putInt("coins",coins+reward.coins).putInt("keys",keys+reward.keys).commit();return reward}

 private fun boosterKey(kind:Match3Engine.Booster)="booster_${kind.name.lowercase()}"
 fun boosterCount(kind:Match3Engine.Booster)=p.getInt(boosterKey(kind),3).coerceAtLeast(0)
 fun grantBooster(kind:Match3Engine.Booster,count:Int=1):Boolean {if(count<=0)return false;return p.edit().putInt(boosterKey(kind),boosterCount(kind)+count).commit()}
 fun buyBoosterStock(kind:Match3Engine.Booster,count:Int=1):Boolean {if(count<=0)return false;val total=kind.cost.toLong()*count;val stock=boosterCount(kind).toLong()+count;if(total>coins||stock>Int.MAX_VALUE)return false;return p.edit().putInt("coins",coins-total.toInt()).putInt(boosterKey(kind),stock.toInt()).commit()}
 /** Uses owned stock first. One successful board effect consumes exactly one booster. */
 fun useBooster(board:Match3Engine,kind:Match3Engine.Booster,at:Int):List<Match3Engine.Wave>{
  if(board.won||board.failed||boosterCount(kind)<=0)return emptyList()
  val waves=board.boost(kind,at)
  if(waves.isNotEmpty())p.edit().putInt(boosterKey(kind),(boosterCount(kind)-1).coerceAtLeast(0)).putString("active_board",board.encode()).commit()
  return waves
 }
 /** Kept for old callers: buying now adds stock, it no longer silently spends coins per tap. */
 fun buyBooster(board:Match3Engine,kind:Match3Engine.Booster,at:Int):List<Match3Engine.Wave>{
  if(boosterCount(kind)<=0&&!buyBoosterStock(kind))return emptyList()
  return useBooster(board,kind,at)
 }
 val storyPosition get()=p.getInt("story_position",0).coerceIn(0,21)
 val availableStoryLines get()=tasks.coerceIn(0,7)*GardenStory.LINES_PER_CHAPTER
 val hasPendingStory get()=storyPosition<availableStoryLines
 fun advanceStory(expectedPosition:Int):Boolean {if(expectedPosition!=storyPosition||!hasPendingStory)return false;return p.edit().putInt("story_position",storyPosition+1).commit()}
 fun ownsDecoration(id:String)=GardenDecor.find(id)!=null&&p.getBoolean("decor_owned_$id",false)
 fun selectedDecoration(category:DecorCategory):DecorItem? {val item=p.getString("decor_selected_${category.name}",null)?.let{GardenDecor.find(it)};return item?.takeIf{it.category==category&&ownsDecoration(it.id)&&tasks>=it.requiredTasks}}
 val decorations get()=DecorCategory.entries.mapNotNull{selectedDecoration(it)}
 enum class DecorResult{PURCHASED,SELECTED,LOCKED,INSUFFICIENT,INVALID}
 fun selectDecoration(id:String):DecorResult {val item=GardenDecor.find(id)?:return DecorResult.INVALID;if(tasks<item.requiredTasks||constructing)return DecorResult.LOCKED;val owned=ownsDecoration(id);if(!owned&&coins<item.cost)return DecorResult.INSUFFICIENT;val edit=p.edit().putString("decor_selected_${item.category.name}",id);if(!owned)edit.putInt("coins",coins-item.cost).putBoolean("decor_owned_$id",true);return if(edit.commit())if(owned)DecorResult.SELECTED else DecorResult.PURCHASED else DecorResult.INVALID}
 fun removeDecoration(category:DecorCategory){p.edit().remove("decor_selected_${category.name}").commit()}
 fun rating(level:Int)=p.getInt("rating_$level",0)
 fun saveBoard(board:Match3Engine){if(board.level==level&&p.getInt("rewarded_level",0)<board.level)p.edit().putString("active_board",board.encode()).commit()}
 fun clearBoard(){p.edit().remove("active_board").commit()}
 fun win(board:Match3Engine):Boolean {if(!board.won)return false;if(board.level<level||p.getInt("rewarded_level",0)>=board.level){p.edit().putInt("rating_${board.level}",maxOf(rating(board.level),board.earnedStars)).commit();return false};if(board.level!=level)return false;val newChest=(chestProgress+1).coerceAtMost(5);val newKeys=keys+if(level%10==0)1 else 0;val newTickets=tickets+if(level%5==0)1 else 0;val edit=p.edit().putInt("coins",coins+150).putInt("stars",stars+1).putInt("rewarded_level",level).putInt("rating_$level",board.earnedStars).putInt("chest_progress",newChest).putInt("keys",newKeys).putInt("event_tickets",newTickets).putInt("level",(level+1).coerceAtMost(Campaign.COUNT)).remove("active_board").remove("reward_material");RestorationInventory.rewardForLevel(level,tasks)?.let{val before=materialCount(it.first);grantMaterial(it.first,it.second,edit);edit.putString("reward_material",it.first.name).putInt("reward_material_before",before).putInt("reward_material_after",before+it.second).putInt("reward_material_required",restorationNeeds().first{need->need.material==it.first}.count)};return edit.commit()}
 fun beginRestoration(fountainQuest:Boolean=false):Boolean {if(fountainQuest&&tasks!=0)return false;if(constructing||tasks>=7||coins<120||(tasks>0&&stars<1)||!restorationMaterialsReady())return false;val edit=p.edit().putInt("coins",coins-120).putInt("stars",(stars-if(tasks==0)0 else 1).coerceAtLeast(0)).putBoolean("constructing",true).putLong("restoration_elapsed",0L);consumeRestorationMaterials(tasks,edit);if(fountainQuest)edit.putString("fountain_quest_stage",FountainQuest.Stage.REPAIR.name).putLong("fountain_quest_elapsed",0);if(tasks==1)edit.remove("flower_choice");return edit.commit()}
 fun finishRestoration(){if(constructing)p.edit().putInt("tasks",tasks+1).putBoolean("constructing",false).putBoolean("fountain",true).remove("restoration_elapsed").commit()}
 val fountainQuestActive get()=tasks==0&&p.getBoolean("fountain_quest_active",false)
 val fountainQuestStage get()=if(tasks>0)FountainQuest.Stage.COMPLETE else runCatching{FountainQuest.Stage.valueOf(p.getString("fountain_quest_stage",null)?:"MELI_APPROACH")}.getOrDefault(FountainQuest.Stage.MELI_APPROACH)
 val fountainQuestElapsed get()=p.getLong("fountain_quest_elapsed",0).coerceAtLeast(0)
 val fountainCleanedMask get()=p.getInt("fountain_cleaned_mask",0) and 7
 fun startFountainQuest():Boolean {
  if(tasks>0||constructing&&!fountainQuestActive)return false
  return p.edit().putBoolean("fountain_quest_active",true).commit()
 }
 fun checkpointFountain(stage:FountainQuest.Stage,elapsed:Long){
  if(fountainQuestActive&&fountainQuestStage==stage)p.edit().putLong("fountain_quest_elapsed",elapsed.coerceAtLeast(0)).commit()
 }
 fun advanceFountainTimed(stage:FountainQuest.Stage,elapsed:Long):Boolean {
  if(!fountainQuestActive||fountainQuestStage!=stage||stage.duration<=0||elapsed<stage.duration)return false
  val next=FountainQuest.timedNext(stage)?:return false
  return p.edit().putString("fountain_quest_stage",next.name).putLong("fountain_quest_elapsed",0).commit()
 }
 fun actFountain(action:FountainQuest.Action,leaf:Int=-1):Boolean {
  if(!fountainQuestActive)return false
  val stage=fountainQuestStage
  if(stage==FountainQuest.Stage.COLLECT&&action==FountainQuest.Action.RESTORE)return beginRestoration(true)
  if(stage==FountainQuest.Stage.CLEAN&&action==FountainQuest.Action.CLEAN_LEAF){
   if(leaf !in 0..2||fountainCleanedMask and (1 shl leaf)!=0)return false
   val mask=fountainCleanedMask or (1 shl leaf)
   val edit=p.edit().putInt("fountain_cleaned_mask",mask)
   if(mask==7)edit.putString("fountain_quest_stage",FountainQuest.Stage.START_WATER.name).putLong("fountain_quest_elapsed",0)
   return edit.commit()
  }
  if(stage==FountainQuest.Stage.CLAIM&&action==FountainQuest.Action.CLAIM){
   if(!constructing||tasks!=0)return false
   return p.edit().putInt("tasks",1).putBoolean("constructing",false).putBoolean("fountain",true)
    .putString("fountain_quest_stage",FountainQuest.Stage.COMPLETE.name).putInt("coins",coins+75)
    .putInt("stars",stars+1).putInt("xp",xp+50).putInt("story_position",maxOf(storyPosition,GardenStory.LINES_PER_CHAPTER)).remove("restoration_elapsed").putLong("fountain_quest_elapsed",0).commit()
  }
  val next=FountainQuest.actionNext(stage,action)?:return false
  return p.edit().putString("fountain_quest_stage",next.name).putLong("fountain_quest_elapsed",0).commit()
 }

}
