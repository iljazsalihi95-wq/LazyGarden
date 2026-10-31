package com.lazygarden.game

import org.junit.Assert.*
import org.junit.Test

class RestorationUiModelTest {
    @Test fun modelAlwaysExposesFullCampaign() {
        assertEquals(20, RestorationUiModel.total)
        assertEquals("0/20", RestorationUiModel.progress(-5))
        assertEquals("7/20", RestorationUiModel.progress(7))
        assertEquals("20/20", RestorationUiModel.progress(99))
    }

    @Test fun chapterStatesFollowRealProgress() {
        val chapters = RestorationUiModel.chapters(7, "sq")
        assertEquals(20, chapters.size)
        assertEquals(7, chapters.count { it.state == RestorationUiModel.State.DONE })
        assertEquals(RestorationUiModel.State.CURRENT, chapters[7].state)
        assertEquals(RestorationUiModel.State.LOCKED, chapters[19].state)
        assertEquals(RestorationCatalog.title(7, "sq"), chapters[7].title)
    }

    @Test fun completedTitleAndLanguagesComeFromCanonicalCatalog() {
        for (language in listOf("sq", "de", "it", "mk", "tr")) {
            assertEquals(RestorationCatalog.title(19, language), RestorationUiModel.completedTitle(20, language))
            assertNull(RestorationUiModel.completedTitle(0, language))
        }
    }

    @Test fun completionOnlyHappensAfterAllTwentyJobs() {
        assertFalse(RestorationUiModel.allCompleted(7))
        assertFalse(RestorationUiModel.allCompleted(19))
        assertTrue(RestorationUiModel.allCompleted(20))
    }
}
