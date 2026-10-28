package com.lazygarden.game

/**
 * Single gameplay bridge for the live booster tray.
 * Keeps inventory/purchase policy out of MainActivity and guarantees that a
 * booster is consumed only after Match3Engine actually produces an effect.
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
        val coins: Int = 0
    )

    val gameplayEnabled: Boolean get() = board.level == currentLevel

    fun use(kind: Match3Engine.Booster, at: Int): Result {
        if (!gameplayEnabled) return snapshot(Status.LOCKED_REPLAY, kind)
        if (save.boosterCount(kind) <= 0) return snapshot(Status.NEEDS_STOCK, kind)
        val waves = save.useBooster(board, kind, at)
        return if (waves.isEmpty()) snapshot(Status.INVALID_TARGET, kind)
        else Result(Status.USED, waves, save.boosterCount(kind), save.coins)
    }

    fun buy(kind: Match3Engine.Booster, count: Int = 1): Boolean =
        gameplayEnabled && !board.won && !board.failed && save.buyBoosterStock(kind, count)

    private fun snapshot(status: Status, kind: Match3Engine.Booster) =
        Result(status, stock = save.boosterCount(kind), coins = save.coins)
}
