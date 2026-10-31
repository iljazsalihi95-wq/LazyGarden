package com.lazygarden.game

import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResidentAnimationTest {
    @Test fun premiumResidentStatesAnimateAcrossMultipleAtlasPoses() {
        listOf(
            ResidentAnimation.State.WALK,
            ResidentAnimation.State.TALK,
            ResidentAnimation.State.WORK,
            ResidentAnimation.State.HAPPY,
            ResidentAnimation.State.CELEBRATE
        ).forEach { state ->
            val frames=(0 until 16).map { ResidentAnimation.frame(state,it) }.toSet()
            assertTrue("$state should not be a static pose", frames.size >= 3)
        }
    }

    @Test fun familyMembersCanRunOutOfPhase() {
        val lazi=ResidentAnimation.phase("lazi")
        val meli=ResidentAnimation.phase("meli")
        val lina=ResidentAnimation.phase("lina")
        val meti=ResidentAnimation.phase("meti")
        assertTrue(setOf(lazi,meli,lina,meti).size >= 3)
        assertNotEquals(
            (0 until 12).map { ResidentAnimation.frame(ResidentAnimation.State.WORK,it,lazi) },
            (0 until 12).map { ResidentAnimation.frame(ResidentAnimation.State.WORK,it,meli) }
        )
    }

    @Test fun secondaryMotionStaysSubtleAndFootSafe() {
        ResidentAnimation.State.entries.forEach { state ->
            repeat(24) { tick ->
                val m=ResidentAnimation.motion(state,tick,ResidentAnimation.phase("lina"))
                assertTrue(m.scale in .97f..1.04f)
                assertTrue(m.lift in 0f..0.03f)
                assertTrue(m.sway in -.03f..0.03f)
            }
        }
    }
}
