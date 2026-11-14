package com.lazygarden.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF

enum class WorkshopTool(val grip: Float = .50f, val reach: Float = .43f) {
    SUCTION_LIFTER(.46f, .43f),
    DRILL(.37f, .55f),
    CLAW_HAMMER,
    WRENCH,
    PAINT_ROLLER,
    TROWEL,
    HAND_SHOVEL,
    CAULKING_GUN(.31f, .62f),
    CLOTH(.35f, .45f);

    companion object {
        fun forWindowStep(step: String): WorkshopTool = when (step) {
            "REMOVE" -> DRILL
            "WINDOWS" -> SUCTION_LIFTER
            "REPAIR" -> CAULKING_GUN
            else -> CLOTH
        }

        fun forTask(task: Int): WorkshopTool = when (task) {
            0, 12 -> WRENCH
            1, 4, 19 -> HAND_SHOVEL
            2, 7 -> TROWEL
            6 -> PAINT_ROLLER
            8, 13 -> SUCTION_LIFTER
            9 -> CLAW_HAMMER
            else -> DRILL
        }
    }
}

/** Pre-cropped transparent hardware plates shared by the work scene and Match-3. */
class WorkshopToolArt(private val context: Context) {
    fun draw(canvas: Canvas, paint: Paint, tool: WorkshopTool, bounds: RectF) {
        paint.shader = null
        paint.colorFilter = null
        paint.alpha = 255
        if (tool == WorkshopTool.CLOTH) {
            val cloth = RectF(bounds)
            cloth.inset(bounds.width() * .20f, bounds.height() * .26f)
            paint.shader = android.graphics.LinearGradient(cloth.left, cloth.top, cloth.right, cloth.bottom,
                0xffc8ecf7.toInt(), 0xff408cab.toInt(), android.graphics.Shader.TileMode.CLAMP)
            canvas.drawRoundRect(cloth, cloth.width() * .12f, cloth.height() * .12f, paint)
            paint.shader = null
            paint.color = 0xff9ed6ec.toInt()
            paint.strokeWidth = cloth.height() * .065f
            canvas.drawLine(cloth.left + cloth.width() * .18f, cloth.top + cloth.height() * .26f,
                cloth.right - cloth.width() * .16f, cloth.top + cloth.height() * .34f, paint)
            return
        }
        val bitmap = GameSkin.bitmap(context, R.drawable.workshop_tools)
        val cell = bitmap.width / 4
        val x = (tool.ordinal % 4) * cell
        val y = (tool.ordinal / 4) * cell
        val source = Rect(x, y, x + cell, y + cell)
        val size = minOf(bounds.width(), bounds.height())
        canvas.drawBitmap(bitmap, source, RectF(bounds.centerX() - size / 2, bounds.centerY() - size / 2,
            bounds.centerX() + size / 2, bounds.centerY() + size / 2), paint)
    }
}
