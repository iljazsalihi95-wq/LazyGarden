package com.lazygarden.game

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

/** Regression coverage for the last-objective/last-move Match-3 failure path. */
class Match3ObjectiveResolutionTest {
    private fun terrainLevel(): Int = (1..250).first { Campaign.level(it).terrainCount > 0 }

    /**
     * Builds a valid saved state with every non-terrain objective already satisfied and exactly
     * one terrain cell left. This uses the public save format so the regression also protects
     * backwards-compatible progress restoration.
     */
    private fun oneTerrainLeft(moves: Int = 1): Match3Engine {
        val level = terrainLevel()
        val seed = Match3Engine(level, Random(731))
        val tokens = seed.encode().split(',').toMutableList()
        val spec = seed.spec
        tokens[1] = moves.toString()
        tokens[2] = spec.target.toString()
        tokens[3] = spec.ice.toString()
        for (color in 0..5) tokens[4 + color] = if (color == spec.collectColor) spec.collectCount.toString() else "0"
        tokens[138] = spec.rescue.toString()
        // Terrain occupies tokens 140..203. Leave one real objective cell at board index 27.
        for (i in 0..63) tokens[140 + i] = "0"
        tokens[140 + 27] = when (spec.terrainKind) {
            3 -> "2" // final remaining stone layer: one more impact clears it
            else -> spec.terrainKind.coerceAtLeast(1).toString()
        }
        val restored = Match3Engine(level, Random(732))
        assertTrue("prepared objective state must restore", restored.restore(tokens.joinToString(",")))
        assertEquals(1, restored.terrainRemaining)
        assertFalse(restored.won)
        return restored
    }

    @Test fun lastTerrainClearedByBoosterWinsImmediately() {
        val e = oneTerrainLeft()
        val waves = e.boost(Match3Engine.Booster.HAMMER, 27)
        assertTrue(waves.isNotEmpty())
        assertEquals(0, e.terrainRemaining)
        assertTrue("final objective must be evaluated after booster resolution", e.won)
        assertFalse(e.failed)
    }

    @Test fun objectiveReachedOnLastMoveCannotBecomeFailure() {
        val e = oneTerrainLeft(moves = 1)
        // A booster does not consume moves; force the terminal boundary through the save format
        // after the real board/objective mutation, matching the state produced by a last swap.
        e.boost(Match3Engine.Booster.HAMMER, 27)
        val tokens = e.encode().split(',').toMutableList()
        tokens[1] = "0"
        val terminal = Match3Engine(e.level, Random(733))
        assertTrue(terminal.restore(tokens.joinToString(",")))
        assertEquals(0, terminal.moves)
        assertEquals(0, terminal.terrainRemaining)
        assertTrue("win has priority over moves exhausted", terminal.won)
        assertFalse("completed objectives must never be reported as failure", terminal.failed)
    }

    @Test fun oneTerrainCellIsCountedOnlyOnceAcrossChainResolution() {
        val e = oneTerrainLeft()
        val waves = e.boost(Match3Engine.Booster.CROSS, 27)
        assertTrue(waves.isNotEmpty())
        assertEquals(0, e.terrainRemaining)
        assertTrue(e.won)
        assertTrue("terrain must stay cleared after all cascades", waves.last().terrainAfter.sumOf { row -> row.count { it > 0 } } == 0)
    }

    @Test fun completedObjectiveSurvivesSaveRestore() {
        val e = oneTerrainLeft()
        e.boost(Match3Engine.Booster.BOMB, 27)
        assertTrue(e.won)
        val copy = Match3Engine(e.level, Random(734))
        assertTrue(copy.restore(e.encode()))
        assertEquals(0, copy.terrainRemaining)
        assertTrue(copy.won)
        assertFalse(copy.failed)
    }
}
