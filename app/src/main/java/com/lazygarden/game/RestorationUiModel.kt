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

    data class Progress(
        val completed: Int,
        val total: Int,
        val remaining: Int,
        val percent: Int,
        val current: Chapter?,
        val completedChapter: Chapter?
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

    /**
     * Canonical task-list text for the restoration sheet. Keeping the state marker here
     * prevents individual screens from re-introducing their own 7-item title arrays.
     */
    fun taskList(completed: Int, language: String): String =
        chapters(completed, language).joinToString("\n\n") { chapter ->
            val marker = when (chapter.state) {
                State.DONE -> "✓"
                State.CURRENT -> "→"
                State.LOCKED -> "🔒"
            }
            "$marker ${chapter.title}"
        }

    /** Canonical snapshot for premium HUD/profile/archive screens. */
    fun snapshot(completed: Int, language: String): Progress {
        val safeCompleted = completed.coerceIn(0, total)
        val list = chapters(safeCompleted, language)
        return Progress(
            completed = safeCompleted,
            total = total,
            remaining = total - safeCompleted,
            percent = if (total == 0) 100 else (safeCompleted * 100 / total),
            current = list.firstOrNull { it.state == State.CURRENT },
            completedChapter = list.lastOrNull { it.state == State.DONE }
        )
    }

    fun progress(completed: Int): String = "${completed.coerceIn(0, total)}/$total"

    fun allCompleted(completed: Int): Boolean = completed >= total
}
