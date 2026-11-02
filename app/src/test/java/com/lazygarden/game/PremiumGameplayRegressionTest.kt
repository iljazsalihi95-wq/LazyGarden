package com.lazygarden.game

import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

/** Regression coverage for the premium Match-3 systems that must survive future level tuning. */
class PremiumGameplayRegressionTest {
    @Test fun campaignKeepsRealObstacleFamiliesAcrossChapters() {
        assertEquals(0, Campaign.level(1).terrainKind)
        assertEquals(1, Campaign.level(8).terrainKind)   // crates
        assertEquals(3, Campaign.level(26).terrainKind)  // layered stone
        assertEquals(4, Campaign.level(51).terrainKind)  // soil
        assertEquals(5, Campaign.level(76).terrainKind)  // water/flower goal
        assertTrue(Campaign.level(176).rescue >= 2)
    }

    @Test fun modernBoardRoundTripPreservesPremiumObjectives() {
        val source = Match3Engine(80, Random(8080))
        val encoded = source.encode()
        val restored = Match3Engine(80, Random(9090))
        assertTrue(restored.restore(encoded))
        assertEquals(source.moves, restored.moves)
        assertEquals(source.score, restored.score)
        assertEquals(source.clearedIce, restored.clearedIce)
        assertEquals(source.rescued, restored.rescued)
        assertEquals(source.rescueTarget, restored.rescueTarget)
        assertArrayEquals(source.collected, restored.collected)
        for (row in 0..7) {
            assertArrayEquals(source.board[row], restored.board[row])
            assertArrayEquals(source.ice[row], restored.ice[row])
            assertArrayEquals(source.terrain[row], restored.terrain[row])
            assertArrayEquals(source.cargo[row], restored.cargo[row])
        }
    }

    @Test fun boostersProduceRealBoardWavesInsteadOfCosmeticOnlyFeedback() {
        val engine = Match3Engine(30, Random(3030))
        val beforeRevision = engine.revision
        val waves = engine.boost(Match3Engine.Booster.HAMMER, 27)
        assertTrue(waves.isNotEmpty())
        assertTrue(engine.revision > beforeRevision)
        assertTrue(waves.first().cells.isNotEmpty())
    }
}
