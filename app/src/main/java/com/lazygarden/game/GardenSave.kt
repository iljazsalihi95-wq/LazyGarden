package com.lazygarden.game

import android.content.Context

class GardenSave(ctx:Context){
 private val p=ctx.getSharedPreferences("garden_progress",Context.MODE_PRIVATE)
 var coins:Int get()=p.getInt("coins",0); private set(v){p.edit().putInt("coins",v).apply()}
 val fountainRestored:Boolean get()=p.getBoolean("fountain",false)
 fun addCoins(n:Int){coins+=n}
 fun spendAndRestore(){ if(coins>=120) p.edit().putInt("coins",coins-120).putBoolean("fountain",true).apply() }
}
