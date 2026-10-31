package com.lazygarden.game

import org.junit.Assert.*
import org.junit.Test

/** Guards the expanded estate campaign from silently falling back to the old 7-job prototype. */
class RestorationCatalogTest {
    @Test fun catalogueCoversEveryRestorationJob() {
        assertEquals(RestorationInventory.TASK_COUNT, RestorationCatalog.entries.size)
        assertEquals(20, RestorationInventory.TASK_COUNT)
        for (task in 0 until RestorationInventory.TASK_COUNT) {
            assertTrue("task $task must require materials", RestorationInventory.forTask(task).isNotEmpty())
        }
    }

    @Test fun everyChapterHasARealTitleInEverySupportedLanguage() {
        val languages = listOf("sq", "de", "it", "mk", "tr")
        for (task in 0 until RestorationInventory.TASK_COUNT) {
            for (language in languages) {
                val title = RestorationCatalog.title(task, language)
                assertTrue("missing $language title for task $task", title.isNotBlank())
                assertFalse("placeholder title for task $task", title.contains("TODO", ignoreCase = true))
            }
        }
    }

    @Test fun materialRewardsStayBoundToCurrentRestoration() {
        for (task in 0 until RestorationInventory.TASK_COUNT) {
            val required = RestorationInventory.forTask(task).map { it.material }.toSet()
            for (level in 1..Campaign.COUNT) {
                val reward = RestorationInventory.rewardForLevel(level, task)
                assertNotNull("task $task level $level must reward restoration stock", reward)
                assertTrue("reward must belong to current restoration", reward!!.first in required)
                assertTrue("reward amount must be useful", reward.second > 0)
            }
        }
    }
}
