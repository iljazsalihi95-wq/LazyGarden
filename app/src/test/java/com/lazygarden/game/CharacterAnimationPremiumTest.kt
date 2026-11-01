package com.lazygarden.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class CharacterAnimationPremiumTest {
    @Test
    fun `state motion eases in instead of snapping`() {
        CharacterAnimation.State.entries.forEach { state ->
            val atStart = CharacterAnimation.poseMotion(state, 0L)
            assertEquals(1f, atStart.scaleX, 0.0001f)
            assertEquals(1f, atStart.scaleY, 0.0001f)
            assertEquals(0f, atStart.rotation, 0.0001f)
            assertEquals(0f, atStart.lift, 0.0001f)

            val settled = CharacterAnimation.poseMotion(state, 320L)
            assertTrue(settled.scaleX in 0.90f..1.10f)
            assertTrue(settled.scaleY in 0.90f..1.10f)
            assertTrue(abs(settled.rotation) <= 4f)
            assertTrue(abs(settled.lift) <= 0.02f)
        }
    }

    @Test
    fun `walking keeps root feet planted while stride remains animated`() {
        val from = CharacterAnimation.Position(0.1f, 0.7f)
        val to = CharacterAnimation.Position(0.9f, 0.7f)
        var sawStride = false
        var previousX = from.x

        for (step in 1..19) {
            val progress = step / 20f
            val gait = CharacterAnimation.gait(from, to, progress)
            assertEquals(0f, gait.bob, 0.0001f)
            assertTrue(gait.position.x >= previousX)
            assertEquals(0.7f, gait.position.y, 0.0001f)
            assertTrue(abs(gait.lean) <= 0.0091f)
            if (abs(gait.step) > 0.05f) sawStride = true
            previousX = gait.position.x
        }
        assertTrue(sawStride)
    }

    @Test
    fun `player resets transition clock only when animation state changes`() {
        val player = CharacterAnimation.Player()
        player.update(CharacterAnimation.State.WALK, 1_000L)
        assertEquals(180L, player.elapsed(1_180L))

        player.update(CharacterAnimation.State.WALK, 1_250L)
        assertEquals(300L, player.elapsed(1_300L))

        player.update(CharacterAnimation.State.WORK, 1_300L)
        assertEquals(0L, player.elapsed(1_300L))
        assertEquals(CharacterAnimation.State.WORK, player.state)
    }

    @Test
    fun `route facing follows travel direction then returns to target`() {
        val left = CharacterAnimation.Position(0.8f, 0.5f)
        val right = CharacterAnimation.Position(0.2f, 0.5f)
        assertFalse(CharacterAnimation.routeFacing(left, right, 0.5f, 1f))
        assertTrue(CharacterAnimation.routeFacing(left, right, 1f, 1f))
    }
}
