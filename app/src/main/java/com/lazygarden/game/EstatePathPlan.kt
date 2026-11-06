package com.lazygarden.game

import android.graphics.Path
import android.graphics.RectF

/** Survey of the paving edges on garden_before and garden_estate, in estate coordinates. */
internal object EstatePathPlan {
    private data class CrossSection(val y: Float, val left: Float, val right: Float)

    private val sections = listOf(
        CrossSection(.56f, .36f, .82f),
        CrossSection(.60f, .34f, .80f),
        CrossSection(.64f, .40f, .77f),
        CrossSection(.69f, .27f, .76f),
        CrossSection(.75f, .24f, .83f),
        CrossSection(.84f, .24f, .86f),
        CrossSection(.92f, .30f, .95f),
        CrossSection(1f, .30f, 1f)
    )

    const val top = .56f
    const val workX = .54f
    const val workY = .74f

    private val crewStations = listOf(
        CharacterAnimation.Position(.42f, .80f),
        CharacterAnimation.Position(.64f, .76f),
        CharacterAnimation.Position(.73f, .83f),
        CharacterAnimation.Position(.37f, .87f),
        CharacterAnimation.Position(.50f, .91f)
    )

    fun crewPosition(index: Int, arrival: Float): CharacterAnimation.Position {
        val station = crewStations[index]
        val entry = CharacterAnimation.Position(station.x, .97f)
        return CharacterAnimation.move(entry, station, arrival)
    }

    fun outline(dst: RectF): Path = Path().apply {
        val first = sections.first()
        moveTo(dst.left + first.left * dst.width(), dst.top + first.y * dst.height())
        for (section in sections.drop(1)) {
            lineTo(dst.left + section.left * dst.width(), dst.top + section.y * dst.height())
        }
        for (section in sections.asReversed()) {
            lineTo(dst.left + section.right * dst.width(), dst.top + section.y * dst.height())
        }
        close()
    }

    fun workBounds(dst: RectF): RectF = WorldRestorationArt.rect(dst, workX, .84f, .30f, .16f)
}
