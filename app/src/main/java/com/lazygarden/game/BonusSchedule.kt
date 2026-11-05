package com.lazygarden.game

/** Display and settlement use the same seven-day schedule. No extra currency is minted by previews. */
object BonusSchedule {
    data class Reward(val coins:Int,val keys:Int,val tickets:Int,val day:Int)
    fun daily(day:Int):Reward {
        require(day in 1..7)
        return Reward(75+day*25,if(day==7)2 else if(day==4)1 else 0,if(day>=3)1 else 0,day)
    }
    const val CHEST_WINS=5
    const val CHEST_COINS=300
    const val CHEST_KEYS=1
}
