package com.lazygarden.game

/**
 * Single UI-facing model for the expanded 20-restoration estate campaign.
 * Keeps build/profile/archive screens from falling back to the old seven-job prototype.
 */
object RestorationUiModel {
    enum class State { DONE, CURRENT, LOCKED }

    data class Chapter(
        val index: Int,
        val title: String,
        val state: State
    )

    val total: Int get() = RestorationCatalog.entries.size

    fun chapters(completed: Int, language: String): List<Chapter> {
        val safeCompleted = completed.coerceIn(0, total)
        return RestorationCatalog.entries.mapIndexed { index, entry ->
            Chapter(
                index = index,
                title = entry.title(language),
                state = when {
                    index < safeCompleted -> State.DONE
                    index == safeCompleted && safeCompleted < total -> State.CURRENT
                    else -> State.LOCKED
                }
            )
        }
    }

    fun completedTitle(completed: Int, language: String): String? =
        if (completed in 1..total) RestorationCatalog.title(completed - 1, language) else null

    fun currentTitle(completed: Int, language: String): String? {
        val safeCompleted = completed.coerceIn(0, total)
        return if (safeCompleted < total) RestorationCatalog.title(safeCompleted, language) else null
    }

    fun archive(completed: Int, language: String): List<Chapter> =
        chapters(completed, language).filter { it.state == State.DONE }

    fun progress(completed: Int): String = "${completed.coerceIn(0, total)}/$total"

    fun allCompleted(completed: Int): Boolean = completed >= total
}
