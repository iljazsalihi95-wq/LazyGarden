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

    data class Result(
        val status: Status,
        val waves: List<Match3Engine.Wave> = emptyList(),
        val stock: Int = 0,
        val coins: Int = 0,
        val affectedTiles: Int = 0,
        val cascadeCount: Int = 0
    )

    val gameplayEnabled: Boolean get() = board.level == currentLevel

    fun use(kind: Match3Engine.Booster, at: Int): Result {
        if (!gameplayEnabled) return snapshot(Status.LOCKED_REPLAY, kind)
        if (save.boosterCount(kind) <= 0) return snapshot(Status.NEEDS_STOCK, kind)
        val waves = save.useBooster(board, kind, at)
        if (waves.isEmpty()) return snapshot(Status.INVALID_TARGET, kind)

        // Wave exposes the impacted board cells as `cells`. Count unique cells
        // across cascades so repeated hits do not exaggerate the FX intensity.
        val affected = waves.asSequence()
            .flatMap { it.cells.asSequence() }
            .distinct()
            .count()
        return Result(
            status = Status.USED,
            waves = waves,
            stock = save.boosterCount(kind),
            coins = save.coins,
            affectedTiles = affected,
            cascadeCount = waves.count { !it.shuffled }
        )
    }

    fun buy(kind: Match3Engine.Booster, count: Int = 1): Boolean =
        gameplayEnabled && !board.won && !board.failed && count > 0 &&
            save.buyBoosterStock(kind, count)

    private fun snapshot(status: Status, kind: Match3Engine.Booster) =
        Result(status, stock = save.boosterCount(kind), coins = save.coins)
}
