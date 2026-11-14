package com.lazygarden.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.PointF
import android.graphics.RectF
import kotlin.math.sin

/** Chapter 9's floor, actors and window hardware use the same camera transform. */
object WindowWorkshop {
    const val TASK = 8

    fun frame(context: Context, width: Float, height: Float): RectF {
        val bitmap = GameSkin.bitmap(context, R.drawable.window_workshop)
        // Show the complete width so characters can enter from both sides.
        val imageHeight = width * bitmap.height / bitmap.width
        val bottom = height * .98f
        return RectF(0f, bottom - imageHeight, width, bottom)
    }

    fun drawRoom(context: Context, canvas: Canvas, paint: Paint, room: RectF, removal: Float, replacement: Float) {
        val background = GameSkin.bitmap(context, R.drawable.window_workshop)
        paint.shader = null
        paint.alpha = 255
        paint.color = 0xff382918.toInt()
        // Extend the upper edge of the room behind the dialogue on tall displays.
        val ceiling = Rect(0, 0, background.width, 2)
        if (room.top > 0f) canvas.drawBitmap(background, ceiling, RectF(room.left, 0f, room.right, room.top), paint)
        canvas.drawBitmap(background, null, room, paint)
        canvas.drawRect(room.left, room.bottom, room.right, canvas.height.toFloat(), paint)

        val pane = pane(room)
        if (replacement < 1f) {
            paint.color = 0xff544538.toInt()
            paint.alpha = ((1f - replacement) * 100).toInt()
            canvas.drawRect(pane, paint)
            if (removal < 1f) {
                val oldPane = RectF(pane)
                oldPane.offset(-pane.width() * .55f * removal, pane.height() * .12f * removal)
                paint.color = 0xffc8dfeb.toInt()
                paint.alpha = ((1f - removal) * 145).toInt()
                canvas.drawRect(oldPane, paint)
                paint.color = 0xffebefea.toInt()
                paint.alpha = ((1f - removal) * 225).toInt()
                paint.strokeWidth = room.width() * .003f
                canvas.drawLine(oldPane.centerX(), oldPane.top, oldPane.centerX() - oldPane.width() * .14f, oldPane.centerY(), paint)
                canvas.drawLine(oldPane.centerX() - oldPane.width() * .14f, oldPane.centerY(), oldPane.right, oldPane.bottom, paint)
            } else {
                val newPane = RectF(pane)
                newPane.offset(-pane.width() * .65f * (1f - replacement), pane.height() * .22f * (1f - replacement))
                paint.color = 0xffd2f1f9.toInt()
                paint.alpha = ((1f - replacement) * 120).toInt()
                canvas.drawRect(newPane, paint)
            }
        }
        paint.alpha = 255
    }

    fun pane(room: RectF): RectF = RectF(
        room.left + room.width() * .491f, room.top + room.height() * .284f,
        room.left + room.width() * .56f, room.top + room.height() * .382f
    )

    fun position(room: RectF, index: Int, progress: Float, working: Boolean): CharacterAnimation.RouteSample {
        val stations = if (working) workStations else storyStations
        val (x, y) = stations[index]
        val from = CharacterAnimation.Position(room.left + room.width() * (if (x < .5f) -.05f else 1.05f), room.top + room.height() * y)
        val target = CharacterAnimation.Position(room.left + room.width() * x, from.y)
        return CharacterAnimation.sampleRoute(from, target, progress, room.centerX())
    }

    fun actorHeight(room: RectF, child: Boolean, foreground: Boolean): Float =
        room.height() * (if (child) .30f else if (foreground) .48f else .58f)

    fun drawActor(
        context: Context, canvas: Canvas, paint: Paint, id: String,
        player: CharacterAnimation.Player, now: Long, feet: CharacterAnimation.Position,
        height: Float, right: Boolean, installerPose: Int? = null, walkDistance: Float = 0f
    ) {
        val elapsed = player.elapsed(now)
        val spriteWidth = height * .67f
        paint.shader = null
        paint.colorFilter = null
        paint.alpha = 255
        paint.color = 0x54302010
        canvas.drawOval(RectF(feet.x - spriteWidth * .23f, feet.y - height * .018f,
            feet.x + spriteWidth * .23f, feet.y + height * .025f), paint)
        val resource = when {
            installerPose != null -> R.drawable.window_installer
            id == "lazi" || id == "meli" || id == "besi" -> R.drawable.window_casual_cast
            id == "medina" || id == "rona" -> R.drawable.window_child_cast
            else -> {
                val width = height / CharacterSprites.aspect(context, SceneCast.actor(id).sprite)
                SceneActorArt.draw(context, canvas, paint, id, player, now,
                    RectF(feet.x - width / 2, feet.y - height, feet.x + width / 2, feet.y), right)
                return
            }
        }
        val frame = installerPose ?: when (player.state) {
            CharacterAnimation.State.WALK -> {
                // The stride advances with the feet's travel through the scene.
                val phase = (walkDistance / (height * .12f)).toInt() % 4
                when (phase) { 0 -> 0; 1 -> 1; 2 -> 0; else -> 2 }
            }
            CharacterAnimation.State.TALK -> if ((elapsed / 230L) % 4L < 3L) 3 else 0
            else -> 0
        }
        val row = if (resource == R.drawable.window_casual_cast && id == "meli") 1 else 0
        val bitmap = GameSkin.bitmap(context, resource)
        val cell = bitmap.width / 4
        val src = Rect(frame * cell, row * cell, (frame + 1) * cell, (row + 1) * cell)
        val layer = canvas.save()
        canvas.scale(if (right) 1f else -1f, 1f, feet.x, feet.y)
        val motion = player.motion(now)
        canvas.scale(motion.scaleX, motion.scaleY, feet.x, feet.y)
        canvas.drawBitmap(bitmap, src, RectF(feet.x - height / 2, feet.y - height, feet.x + height / 2, feet.y), paint)
        canvas.restoreToCount(layer)
    }

    /** The active tool is drawn in the actor's grip. The drill bit/nozzle reaches the frame. */
    fun drawTool(art: WorkshopToolArt, canvas: Canvas, paint: Paint, tool: WorkshopTool, hand: PointF, tip: PointF) {
        val offsetX = tip.x - hand.x
        val offsetY = tip.y - hand.y
        val distance = kotlin.math.hypot(offsetX, offsetY).coerceAtLeast(1f)
        val size = distance / tool.reach
        val bounds = RectF(hand.x - size * tool.grip, hand.y - size * .40f,
            hand.x + size * (1f - tool.grip), hand.y + size * .60f)
        val transform = canvas.save()
        val angle = Math.toDegrees(kotlin.math.atan2(offsetY.toDouble(), offsetX.toDouble())).toFloat()
        canvas.rotate(angle, hand.x, hand.y)
        art.draw(canvas, paint, tool, bounds)
        canvas.restoreToCount(transform)
    }

    fun contact(room: RectF, step: String, progress: Float): PointF {
        val pane = pane(room)
        val cycle = sin(progress * 12f)
        return when (step) {
            "REMOVE" -> PointF(pane.right, pane.centerY() + pane.height() * .24f * cycle)
            "WINDOWS" -> PointF(pane.centerX() - pane.width() * .65f * (1f - progress),
                pane.centerY() + pane.height() * .22f * (1f - progress))
            "REPAIR" -> PointF(pane.right, pane.centerY() + pane.height() * .20f * cycle)
            else -> PointF(pane.centerX(), pane.bottom - pane.height() * (.12f + .17f * cycle))
        }
    }

    fun walkDistance(room: RectF, feet: CharacterAnimation.Position, right: Boolean): Float =
        kotlin.math.abs(feet.x - if (right) room.left - room.width() * .05f else room.right + room.width() * .05f)

    private val storyStations = listOf(.29f to .89f, .68f to .88f, .46f to .79f, .86f to .95f)
    private val workStations = listOf(.15f to .92f, .39f to .75f, .84f to .93f, .67f to .96f)
}
