package com.lazygarden.game

import android.content.Context
import java.util.Calendar

class GardenSave(ctx:Context) {
 private val p=ctx.getSharedPreferences("garden_progress",Context.MODE_PRIVATE)
 val coins get()=p.getInt("coins",0)
 val stars get()=p.getInt("stars",0)
 val level get()=p.getInt("level",1).coerceIn(1,Campaign.COUNT)
 val tasks get()=p.getInt("tasks",if(p.getBoolean("fountain",false))1 else 0)
 val fountainRestored get()=tasks>0
 val constructing get()=p.getBoolean("constructing",false)
 val restorationElapsed get()=p.getLong("restoration_elapsed",0L).coerceIn(0L,FountainSequence.DURATION)
 fun checkpointRestoration(ms:Long){if(constructing)p.edit().putLong("restoration_elapsed",ms).commit()}
 val activeBoard get()=p.getString("active_board",null)

 // Live-game resources. They are virtual gameplay items only and have no cash value.
 val maxLives get()=5
 val lives get()=p.getInt("lives",maxLives).coerceIn(0,maxLives)
 val keys get()=p.getInt("keys",0).coerceAtLeast(0)
 val tickets get()=p.getInt("event_tickets",0).coerceAtLeast(0)
 val chestProgress get()=p.getInt("chest_progress",0).coerceIn(0,5)
 val dailyStreak get()=p.getInt("daily_streak",0).coerceIn(0,7)

 private fun dayStamp():Int {
  val c=Calendar.getInstance()
  return c.get(Calendar.YEAR)*1000+c.get(Calendar.DAY_OF_YEAR)
 }
 val dailyRewardAvailable get()=p.getInt("daily_claim_day",-1)!=dayStamp()
 data class DailyReward(val coins:Int,val keys:Int,val tickets:Int,val streak:Int)
 fun claimDailyReward():DailyReward? {
  if(!dailyRewardAvailable)return null
  val previous=p.getInt("daily_claim_day",-10)
  val today=dayStamp()
  val next=if(previous==today-1)(dailyStreak%7)+1 else 1
  val rewardCoins=75+next*25
  val rewardKeys=if(next==7)2 else if(next==4)1 else 0
  val rewardTickets=if(next>=3)1 else 0
  p.edit().putInt("daily_claim_day",today).putInt("daily_streak",next)
   .putInt("coins",coins+rewardCoins).putInt("keys",keys+rewardKeys)
   .putInt("event_tickets",tickets+rewardTickets).commit()
  return DailyReward(rewardCoins,rewardKeys,rewardTickets,next)
 }
 fun spendLife():Boolean {
  if(lives<=0)return false
  return p.edit().putInt("lives",lives-1).commit()
 }
 fun rewardLife(){p.edit().putInt("lives",(lives+1).coerceAtMost(maxLives)).commit()}
 fun grantRewardedCoins(amount:Int=100):Boolean {
  if(amount<=0)return false
  return p.edit().putInt("coins",coins+amount).commit()
 }
 data class ChestReward(val coins:Int,val keys:Int)
 fun openRewardChest():ChestReward? {
  if(chestProgress<5)return null
  val reward=ChestReward(300,1)
  p.edit().putInt("chest_progress",0).putInt("coins",coins+reward.coins).putInt("keys",keys+reward.keys).commit()
  return reward
 }

 fun buyBooster(board:Match3Engine, kind:Match3Engine.Booster, at:Int):List<Match3Engine.Wave> {
  if(coins<kind.cost || board.won || board.failed)return emptyList()
  val waves=board.boost(kind,at)
  if(waves.isNotEmpty())p.edit().putInt("coins",coins-kind.cost).putString("active_board",board.encode()).commit()
  return waves
 }
 val storyPosition get()=p.getInt("story_position",0).coerceIn(0,21)
 val availableStoryLines get()=tasks.coerceIn(0,7)*GardenStory.LINES_PER_CHAPTER
 val hasPendingStory get()=storyPosition<availableStoryLines
 fun advanceStory(expectedPosition:Int):Boolean {
  if(expectedPosition!=storyPosition || !hasPendingStory)return false
  return p.edit().putInt("story_position",storyPosition+1).commit()
 }
 fun ownsDecoration(id:String)=GardenDecor.find(id)!=null && p.getBoolean("decor_owned_$id",false)
 fun selectedDecoration(category:DecorCategory):DecorItem? {
  val item=p.getString("decor_selected_${category.name}",null)?.let{GardenDecor.find(it)}
  return item?.takeIf{it.category==category && ownsDecoration(it.id) && tasks>=it.requiredTasks}
 }
 val decorations get()=DecorCategory.entries.mapNotNull{selectedDecoration(it)}
 enum class DecorResult{PURCHASED,SELECTED,LOCKED,INSUFFICIENT,INVALID}
 fun selectDecoration(id:String):DecorResult {
  val item=GardenDecor.find(id) ?: return DecorResult.INVALID
  if(tasks<item.requiredTasks || constructing)return DecorResult.LOCKED
  val owned=ownsDecoration(id)
  if(!owned && coins<item.cost)return DecorResult.INSUFFICIENT
  val edit=p.edit().putString("decor_selected_${item.category.name}",id)
  if(!owned)edit.putInt("coins",coins-item.cost).putBoolean("decor_owned_$id",true)
  return if(edit.commit())if(owned)DecorResult.SELECTED else DecorResult.PURCHASED else DecorResult.INVALID
 }
 fun removeDecoration(category:DecorCategory){p.edit().remove("decor_selected_${category.name}").commit()}
 fun rating(level:Int)=p.getInt("rating_$level",0)
 fun saveBoard(board:Match3Engine){if(board.level==level && p.getInt("rewarded_level",0)<board.level)p.edit().putString("active_board",board.encode()).commit()}
 fun clearBoard(){p.edit().remove("active_board").commit()}
 fun win(board:Match3Engine):Boolean {
  if(!board.won)return false
  if(board.level<level || p.getInt("rewarded_level",0)>=board.level){
   p.edit().putInt("rating_${board.level}",maxOf(rating(board.level),board.earnedStars)).commit()
   return false
  }
  if(board.level!=level)return false
  val newChest=(chestProgress+1).coerceAtMost(5)
  val newKeys=keys+if(level%10==0)1 else 0
  val newTickets=tickets+if(level%5==0)1 else 0
  p.edit().putInt("coins",coins+150).putInt("stars",stars+1).putInt("rewarded_level",level).putInt("rating_$level",board.earnedStars)
   .putInt("chest_progress",newChest).putInt("keys",newKeys).putInt("event_tickets",newTickets)
   .putInt("level",(level+1).coerceAtMost(Campaign.COUNT)).remove("active_board").commit()
  return true
 }
 fun beginRestoration():Boolean {
  if(constructing || tasks>=7 || coins<120 || (tasks>0 && stars<1))return false
  p.edit().putInt("coins",coins-120).putInt("stars",(stars-1).coerceAtLeast(0)).putBoolean("constructing",true).putLong("restoration_elapsed",0L).commit()
  return true
 }
 fun finishRestoration(){if(constructing)p.edit().putInt("tasks",tasks+1).putBoolean("constructing",false).putBoolean("fountain",true).remove("restoration_elapsed").commit()}
}
