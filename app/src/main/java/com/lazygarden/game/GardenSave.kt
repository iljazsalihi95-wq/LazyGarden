package com.lazygarden.game

import android.content.Context

class GardenSave(ctx:Context) {
 private val p=ctx.getSharedPreferences("garden_progress",Context.MODE_PRIVATE)
 val coins get()=p.getInt("coins",0)
 val stars get()=p.getInt("stars",0)
 val level get()=p.getInt("level",1).coerceIn(1,Campaign.COUNT)
 val tasks get()=p.getInt("tasks",if(p.getBoolean("fountain",false))1 else 0)
 val fountainRestored get()=tasks>0
 val constructing get()=p.getBoolean("constructing",false)
 val activeBoard get()=p.getString("active_board",null)
 fun rating(level:Int)=p.getInt("rating_$level",0)
 fun saveBoard(board:Match3Engine){p.edit().putString("active_board",board.encode()).commit()}
 fun clearBoard(){p.edit().remove("active_board").commit()}
 fun win(board:Match3Engine):Boolean {
  if(!board.won || board.level!=level || p.getInt("rewarded_level",0)>=level)return false
  p.edit().putInt("coins",coins+150).putInt("stars",stars+1).putInt("rewarded_level",level).putInt("rating_$level",board.earnedStars)
   .putInt("level",(level+1).coerceAtMost(Campaign.COUNT)).remove("active_board").commit()
  return true
 }
 fun beginRestoration():Boolean {
  if(constructing || tasks>=7 || coins<120 || (tasks>0 && stars<1))return false
  p.edit().putInt("coins",coins-120).putInt("stars",(stars-1).coerceAtLeast(0)).putBoolean("constructing",true).commit()
  return true
 }
 fun finishRestoration(){if(constructing)p.edit().putInt("tasks",tasks+1).putBoolean("constructing",false).putBoolean("fountain",true).commit()}
}
