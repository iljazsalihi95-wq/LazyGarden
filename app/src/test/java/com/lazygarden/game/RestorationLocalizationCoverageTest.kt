package com.lazygarden.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RestorationLocalizationCoverageTest {
    private val languages = listOf("sq", "de", "it", "mk", "tr")

    @Test fun everyPremiumChapterHasAUsableTitleInEverySupportedLanguage() {
        assertEquals(20, RestorationUiModel.total)
        languages.forEach { language ->
            val chapters = RestorationUiModel.chapters(0, language)
            assertEquals(20, chapters.size)
            chapters.forEachIndexed { index, chapter ->
                assertEquals(index, chapter.index)
                assertTrue("$language chapter ${index + 1} is blank", chapter.title.isNotBlank())
                assertFalse("$language chapter ${index + 1} leaks placeholder", chapter.title.contains("TODO", ignoreCase = true))
            }
        }
    }

    @Test fun campaignTitlesAreActuallyLocalizedRatherThanOneSharedFallback() {
        val chapterIndexes = 0 until RestorationUiModel.total
        chapterIndexes.forEach { index ->
            val localized = languages.map { RestorationCatalog.title(index, it) }
            assertTrue("chapter ${index + 1} should expose localized copy", localized.toSet().size >= 3)
            assertNotEquals("", localized.first())
        }
    }

    @Test fun archiveAndCurrentChapterKeepTheRequestedLanguage() {
        languages.forEach { language ->
            val snapshot = RestorationUiModel.snapshot(7, language)
            assertEquals(RestorationCatalog.title(7, language), snapshot.current?.title)
            assertEquals(RestorationCatalog.title(6, language), snapshot.completedChapter?.title)
            val archive = RestorationUiModel.archive(7, language)
            assertEquals(7, archive.size)
            archive.forEachIndexed { index, chapter ->
                assertEquals(RestorationCatalog.title(index, language), chapter.title)
            }
        }
    }
}
