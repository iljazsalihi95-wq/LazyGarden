package com.lazygarden.game

/**
 * Single gameplay bridge for the live booster tray.
 * Keeps inventory/purchase policy out of MainActivity and guarantees that a
 * booster is consumed only after Match3Engine actually produces an effect.
 *
 * The result also carries compact impact telemetry for premium UI feedback:
 * affectedTiles can drive a punch/particle burst and cascadeCount can drive
 * combo intensity without re-running or guessing the engine result in the UI.
 */
class Match3BoosterController(
    private val save: GardenSave,
    private val board: Match3Engine,
    private val currentLevel: Int
) {
    enum class Status { USED, LOCKED_REPLAY, NEEDS_STOCK, INVALID_TARGET }
    enum class Impact { NONE, LIGHT, STRONG, EPIC }

    data class Result(
        val status: Status,
        val waves: List<Match3Engine.Wave> = emptyList(),
        val stock: Int = 0,
        val coins: Int = 0,
        val affectedTiles: Int = 0,
        val cascadeCount: Int = 0,
        val impact: Impact = Impact.NONE
    )

    val gameplayEnabled: Boolean get() = board.level == currentLevel

    fun use(kind: Match3Engine.Booster, at: Int): Result {
        if (!gameplayEnabled) return snapshot(Status.LOCKED_REPLAY, kind)
        if (save.boosterCount(kind) <= 0) return snapshot(Status.NEEDS_STOCK, kind)
        val waves = save.useBooster(board, kind, at)
        if (waves.isEmpty()) return snapshot(Status.INVALID_TARGET, kind)

        // Normal boosters expose hit cells directly. SHUFFLE intentionally has
        // no cleared cells, so measure the actual before/after board changes to
        // give its premium transition the correct visual/audio weight.
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
        return Result(
            status = Status.USED,
            waves = waves,
            stock = save.boosterCount(kind),
            coins = save.coins,
            affectedTiles = affected,
            cascadeCount = cascades,
            impact = impact
        )
    }

    fun buy(kind: Match3Engine.Booster, count: Int = 1): Boolean =
        gameplayEnabled && !board.won && !board.failed && count > 0 &&
            save.buyBoosterStock(kind, count)

    private fun snapshot(status: Status, kind: Match3Engine.Booster) =
        Result(status, stock = save.boosterCount(kind), coins = save.coins)
}
