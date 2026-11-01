package com.lazygarden.game

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Guards the real save/inventory path for the expanded 20-restoration campaign. */
@RunWith(AndroidJUnit4::class)
class TwentyChapterPersistenceTest {
    private val ctx get() = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun clear() {
        ctx.getSharedPreferences("garden_progress", Context.MODE_PRIVATE).edit().clear().commit()
    }

    private fun stock(task: Int) {
        val edit = ctx.getSharedPreferences("garden_progress", Context.MODE_PRIVATE).edit()
        RestorationInventory.forTask(task).forEach {
            edit.putInt("material_${it.material.name}", it.count)
        }
        assertTrue(edit.commit())
    }

    @Test
    fun allTwentyRestorationsConsumeMaterialsAndPersistExactlyOnce() {
        val prefs = ctx.getSharedPreferences("garden_progress", Context.MODE_PRIVATE)
        assertTrue(prefs.edit().putInt("coins", 5000).putInt("stars", 25).commit())
        val save = GardenSave(ctx)

        assertEquals(20, RestorationInventory.TASK_COUNT)
        assertEquals(RestorationUiModel.total, RestorationInventory.TASK_COUNT)

        for (task in 0 until RestorationInventory.TASK_COUNT) {
            assertEquals(task, save.tasks)
            stock(task)
            assertTrue("task $task materials should be ready", save.restorationMaterialsReady())
            val coinsBefore = save.coins
            val starsBefore = save.stars
            assertTrue("task $task should start", save.beginRestoration())
            assertFalse("task $task must not start twice", save.beginRestoration())
            RestorationInventory.forTask(task).forEach {
                assertEquals("${it.material} must be consumed for task $task", 0, save.materialCount(it.material))
            }
            assertEquals(coinsBefore - 120, save.coins)
            assertEquals(starsBefore - if (task == 0) 0 else 1, save.stars)

            save.finishRestoration()
            save.finishRestoration()
            assertEquals(task + 1, GardenSave(ctx).tasks)
            assertFalse(save.constructing)
        }

        assertEquals("20/20", RestorationUiModel.progress(save.tasks))
        assertTrue(RestorationUiModel.allCompleted(save.tasks))
        assertNull(RestorationUiModel.currentTitle(save.tasks, save.language))
        assertFalse("campaign must not create a 21st restoration", save.beginRestoration())
    }
}
