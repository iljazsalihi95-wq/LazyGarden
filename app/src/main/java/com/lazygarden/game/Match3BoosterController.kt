package com.lazygarden.game

/**
 * Single gameplay bridge for the live booster tray.
 * Keeps inventory/purchase policy out of MainActivity and guarantees that a
 * booster is consumed only after Match3Engine actually produces an effect.
 *
 * The result carries impact telemetry for premium UI feedback. Presentation
 * can now drive particles, punch/scale and SFX from the real resolved board
 * result instead of guessing from the booster button that was pressed.
 */
class Match3BoosterController(
    private val save: GardenSave,
    private val board: Match3Engine,
    private val currentLevel: Int
) {
    enum class Status { USED, LOCKED_REPLAY, NEEDS_STOCK, INVALID_TARGET }
    enum class Impact { NONE, LIGHT, STRONG, EPIC }

    data class Feedback(
        val impact: Impact = Impact.NONE,
        val intensity: Float = 0f,
        val particleBursts: Int = 0,
        val sfxCue: String = ""
    )

    data class Result(
        val status: Status,
        val waves: List<Match3Engine.Wave> = emptyList(),
        val stock: Int = 0,
        val coins: Int = 0,
        val affectedTiles: Int = 0,
        val cascadeCount: Int = 0,
        val impact: Impact = Impact.NONE,
        val feedback: Feedback = Feedback()
    )

    val gameplayEnabled: Boolean get() = board.level == currentLevel

    fun use(kind: Match3Engine.Booster, at: Int): Result {
        if (!gameplayEnabled) return snapshot(Status.LOCKED_REPLAY, kind)
        if (save.boosterCount(kind) <= 0) return snapshot(Status.NEEDS_STOCK, kind)
        val waves = save.useBooster(board, kind, at)
        if (waves.isEmpty()) return snapshot(Status.INVALID_TARGET, kind)

        // Normal boosters expose hit cells directly. SHUFFLE intentionally has
        // no cleared cells, so measure the actual before/after board changes to
        // give its transition the correct visual/audio weight.
        val cleared = waves.asSequence().flatMap { it.cells.asSequence() }.toSet()
        val shuffledChanged = waves.asSequence().filter { it.shuffled }.flatMap { wave ->
            (0..63).asSequence().filter { i ->
                wave.before[i / 8][i % 8] != wave.after[i / 8][i % 8]
            }
        }.toSet()
        val affected = (cleared + shuffledChanged).size
        val cascades = waves.count { !it.shuffled }
        val impact = when {
            affected >= 24 || cascades >= 4 -> Impact.EPIC
            affected >= 9 || cascades >= 2 -> Impact.STRONG
            affected > 0 -> Impact.LIGHT
            else -> Impact.NONE
        }
        val feedback = feedbackFor(kind, impact, affected, cascades)
        return Result(
            status = Status.USED,
            waves = waves,
            stock = save.boosterCount(kind),
            coins = save.coins,
            affectedTiles = affected,
            cascadeCount = cascades,
            impact = impact,
            feedback = feedback
        )
    }

    fun buy(kind: Match3Engine.Booster, count: Int = 1): Boolean =
        gameplayEnabled && !board.won && !board.failed && count > 0 &&
            save.buyBoosterStock(kind, count)

    private fun feedbackFor(
        kind: Match3Engine.Booster,
        impact: Impact,
        affected: Int,
        cascades: Int
    ): Feedback {
        if (impact == Impact.NONE) return Feedback()
        val boardCoverage = (affected / 24f).coerceIn(0f, 1f)
        val cascadeEnergy = (cascades / 4f).coerceIn(0f, 1f)
        val intensity = (0.35f + boardCoverage * 0.45f + cascadeEnergy * 0.20f).coerceIn(0f, 1f)
        val bursts = when (impact) {
            Impact.LIGHT -> 1
            Impact.STRONG -> 2
            Impact.EPIC -> 4
            Impact.NONE -> 0
        }
        val cue = when (kind) {
            Match3Engine.Booster.HAMMER -> "booster_hammer"
            Match3Engine.Booster.ROW -> "booster_row"
            Match3Engine.Booster.COLUMN -> "booster_column"
            Match3Engine.Booster.CROSS -> "booster_cross"
            Match3Engine.Booster.BOMB -> "booster_bomb"
            Match3Engine.Booster.COLOR -> "booster_color"
            Match3Engine.Booster.SHUFFLE -> "booster_shuffle"
        }
        return Feedback(impact, intensity, bursts, cue)
    }

    private fun snapshot(status: Status, kind: Match3Engine.Booster) =
        Result(status, stock = save.boosterCount(kind), coins = save.coins)
}
