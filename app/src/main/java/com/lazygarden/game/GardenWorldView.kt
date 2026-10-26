package com.lazygarden.game

import android.content.Context
import android.graphics.*
import android.util.Base64
import android.view.View
import kotlin.math.sin

class GardenWorldView(
    context: Context,
    private val tasks: Int,
    private val constructing: Boolean = false
) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var phase = 0f
    private val artwork: Bitmap by lazy { BitmapFactory.decodeResource(resources, R.drawable.garden_estate) }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val src = Rect(0, 0, artwork.width, artwork.height)
        val scale = maxOf(width.toFloat()/artwork.width, height.toFloat()/artwork.height)
        val dw=(artwork.width*scale).toInt(); val dh=(artwork.height*scale).toInt()
        val dst = Rect((width-dw)/2, (height-dh)/2, (width+dw)/2, (height+dh)/2)
        canvas.drawBitmap(artwork, src, dst, paint)

        val w = width.toFloat(); val h = height.toFloat()
        paint.shader = LinearGradient(0f, 0f, 0f, h,
            intArrayOf(0x22000000, 0x00000000, 0x66000000),
            floatArrayOf(0f, .58f, 1f), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null

        phase = (phase + .018f) % 1f
        for (i in 0 until 10) {
            val x = w * (.08f + ((i * .091f) % .84f))
            val y = h * (.18f + ((i * .073f + phase) % .58f))
            paint.color = if (i % 2 == 0) 0x66FFD765 else 0x55FFFFFF
            canvas.drawCircle(x, y, w * (.003f + (i % 3) * .0013f), paint)
        }

        if (constructing) {
            paint.color = 0xB0000000.toInt()
            canvas.drawRoundRect(w*.16f,h*.43f,w*.84f,h*.58f,30f,30f,paint)
            paint.color = 0xFFFFD54F.toInt(); paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.DEFAULT_BOLD; paint.textSize = w*.052f
            canvas.drawText("DUKE RESTAURUAR PRONËN",w*.5f,h*.515f,paint)
        }
        if (tasks > 0) {
            paint.color = 0xB0155A2A.toInt()
            canvas.drawRoundRect(w*.08f,h*.80f,w*.92f,h*.89f,28f,28f,paint)
            paint.color = Color.WHITE; paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.DEFAULT_BOLD; paint.textSize = w*.034f
            canvas.drawText("RESTAURIME  $tasks/7",w*.5f,h*.855f,paint)
        }
        postInvalidateDelayed(32)
    }

}
