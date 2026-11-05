package com.lazygarden.game

import org.junit.Assert.*
import org.junit.Test

class BonusScheduleTest {
    @Test fun sevenDayPreviewMatchesEstablishedEconomy(){
        assertEquals(listOf(100,125,150,175,200,225,250),(1..7).map{BonusSchedule.daily(it).coins})
        assertEquals(listOf(0,0,0,1,0,0,2),(1..7).map{BonusSchedule.daily(it).keys})
        assertEquals(5,(1..7).sumOf{BonusSchedule.daily(it).tickets})
        assertEquals(300,BonusSchedule.CHEST_COINS)
        assertEquals(1,BonusSchedule.CHEST_KEYS)
    }
    @Test(expected=IllegalArgumentException::class) fun noUnboundedRewardDay(){BonusSchedule.daily(8)}
}
