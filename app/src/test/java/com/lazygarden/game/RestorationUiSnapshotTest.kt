package com.lazygarden.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RestorationUiSnapshotTest {
    @Test fun snapshotTracksTwentyChapterCampaign() {
        val p = RestorationUiModel.snapshot(7, "sq")
        assertEquals(7, p.completed)
        assertEquals(20, p.total)
        assertEquals(13, p.remaining)
        assertEquals(35, p.percent)
        assertEquals(RestorationUiModel.State.CURRENT, p.current?.state)
        assertEquals(7, p.current?.index)
        assertEquals(6, p.completedChapter?.index)
    }

    @Test fun snapshotClampsCorruptProgressSafely() {
        val below = RestorationUiModel.snapshot(-4, "de")
        assertEquals(0, below.completed)
        assertEquals(0, below.percent)
        assertEquals(20, below.remaining)
        assertNull(below.completedChapter)

        val above = RestorationUiModel.snapshot(999, "de")
        assertEquals(20, above.completed)
        assertEquals(100, above.percent)
        assertEquals(0, above.remaining)
        assertNull(above.current)
        assertTrue(RestorationUiModel.allCompleted(above.completed))
    }

    @Test fun snapshotUsesLocalizedCatalogTitle() {
        val p = RestorationUiModel.snapshot(7, "de")
        assertEquals(RestorationCatalog.title(7, "de"), p.current?.title)
        assertEquals(RestorationCatalog.title(6, "de"), p.completedChapter?.title)
    }
}
