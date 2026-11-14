package com.lazygarden.game

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

internal enum class Parcel(val chapter: Int, val level: Int, val names: String, val phases: List<String>) {
    VEGETABLES(5, 15, "Kopshti i perimeve|Gemüsegarten|Orto|Зеленчукова градина|Sebze bahçesi", listOf(
        "Përgatit tokën|Boden vorbereiten|Prepara il terreno|Подготви ја почвата|Toprağı hazırla",
        "Mbill perimet|Gemüse pflanzen|Pianta gli ortaggi|Засади зеленчук|Sebzeleri ek",
        "Ujit perimet|Gemüse gießen|Annaffia gli ortaggi|Наводни го зеленчукот|Sebzeleri sula")),
    ORCHARD(10, 35, "Pemishtja|Obstgarten|Frutteto|Овоштарник|Meyve bahçesi", listOf(
        "Hap gropat|Pflanzlöcher ausheben|Scava le buche|Ископај дупки|Çukurları kaz",
        "Mbill mollë e dardha|Apfel- und Birnbäume pflanzen|Pianta meli e peri|Засади јаболка и круши|Elma ve armut dik",
        "Ujit pemët|Bäume gießen|Annaffia gli alberi|Наводни ги дрвјата|Ağaçları sula")),
    GREENHOUSE(20, 60, "Sera e fidanëve|Gewächshaus|Serra|Стаклена градина|Sera", listOf(
        "Përgatit bazën|Fundament vorbereiten|Prepara la base|Подготви ја основата|Temeli hazırla",
        "Monto serën|Gewächshaus montieren|Monta la serra|Монтирај ја стаклената градина|Serayı kur",
        "Mbill fidanët|Setzlinge pflanzen|Pianta le piantine|Засади садници|Fideleri dik"));

    fun requiredLevel(step: Int) = level + step
    fun unlocked(chapters: Int, wins: Int, step: Int) = chapters >= chapter && wins >= requiredLevel(step)
}

internal fun expansionText(value: String, language: String) = value.split('|')[when (language) {
    "de" -> 1
    "it" -> 2
    "mk" -> 3
    "tr" -> 4
    else -> 0
}]

internal class ExpansionScreen(
    context: Context,
    private val save: GardenSave,
    back: () -> Unit,
    private val play: () -> Unit
) : ScrollView(context) {
    private val atlas = GameSkin.bitmap(context, R.drawable.estate_expansion)
    private val column = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(12), dp(10), dp(12), dp(18))
    }
    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()
    private fun t(value: String) = expansionText(value, save.language)

    init {
        setBackgroundColor(0xff123b31.toInt())
        addView(column)
        column.addView(GameButton(context, false).apply {
            text = "‹ " + UiText.get(save.language, "estate_plan")
            isAllCaps = false
            minHeight = dp(48)
            setOnClickListener { back() }
        })
        column.addView(label(title(save.language), 24f))
        Parcel.entries.forEach(::addParcel)
    }

    private fun addParcel(parcel: Parcel) {
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xff244e3f.toInt())
        }
        column.addView(card, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })
        val name = t(parcel.names)
        card.addView(label(name, 20f))
        val art = ParcelView(context, atlas, parcel, save.parcelProgress(parcel)).apply { contentDescription = name }
        card.addView(art, LinearLayout.LayoutParams(-1, dp(164)))
        val status = label("", 14f)
        card.addView(status)
        val action = GameButton(context, false).apply {
            isAllCaps = false
            minHeight = dp(48)
        }
        card.addView(action)

        fun refresh() {
            val progress = save.parcelProgress(parcel)
            val requiredLevel = parcel.requiredLevel(progress.coerceAtMost(parcel.phases.lastIndex))
            val unlocked = parcel.unlocked(save.tasks, save.completedLevels, progress)
            val complete = progress == parcel.phases.size
            val chapter = UiText.get(save.language, "chapter")
            val level = UiText.get(save.language, "level")
            status.text = if (unlocked || complete) "$progress / ${parcel.phases.size}"
                else "🔒 $chapter ${save.tasks}/${parcel.chapter}  ·  $level ${save.completedLevels}/${requiredLevel}"
            action.isEnabled = if (unlocked) progress < parcel.phases.size else save.completedLevels < requiredLevel
            action.text = when {
                progress == parcel.phases.size -> t("✓ Përfunduar|✓ Fertig|✓ Completato|✓ Завршено|✓ Tamamlandı")
                unlocked -> t(parcel.phases[progress])
                save.completedLevels < requiredLevel -> UiText.get(save.language, "play") + " Match‑3"
                else -> t("Përfundo kapitullin|Kapitel abschließen|Completa il capitolo|Заврши го поглавјето|Bölümü tamamla")
            }
            action.setOnClickListener {
                if (complete) return@setOnClickListener
                if (!unlocked) play()
                else if (save.workParcel(parcel, progress)) {
                    art.reveal(progress + 1)
                    refresh()
                }
            }
        }
        refresh()
    }

    private fun label(value: String, size: Float) = TextView(context).apply {
        text = value
        textSize = size
        setTextColor(0xffffe6ae.toInt())
        gravity = Gravity.CENTER
        setPadding(dp(10), dp(8), dp(10), dp(8))
    }

    companion object {
        fun title(language: String) = expansionText(
            "Zgjerimi i pronës|Anwesen erweitern|Espansione della tenuta|Проширување на имотот|Mülk genişletme", language
        )
    }
}

private class ParcelView(context: Context, private val atlas: Bitmap, private val parcel: Parcel, progress: Int) : View(context) {
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val cellWidth = atlas.width / 3
    private val cellHeight = atlas.height / 2
    private val bare = Rect(parcel.ordinal * cellWidth, 0, (parcel.ordinal + 1) * cellWidth, cellHeight)
    private val restored = Rect(bare).apply { offset(0, cellHeight) }
    private val bounds = RectF()
    private var visibleProgress = progress.toFloat() / parcel.phases.size
    private var animator: ValueAnimator? = null

    fun reveal(step: Int) {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(visibleProgress, step.toFloat() / parcel.phases.size).apply {
            duration = 670
            addUpdateListener {
                visibleProgress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val scale = maxOf(width.toFloat() / cellWidth, height.toFloat() / cellHeight)
        val halfWidth = cellWidth * scale / 2
        val halfHeight = cellHeight * scale / 2
        bounds.set(width / 2f - halfWidth, height / 2f - halfHeight,
            width / 2f + halfWidth, height / 2f + halfHeight)
        canvas.drawBitmap(atlas, bare, bounds, paint)
        val clip = canvas.save()
        canvas.clipRect(0f, 0f, width * visibleProgress, height.toFloat())
        canvas.drawBitmap(atlas, restored, bounds, paint)
        canvas.restoreToCount(clip)
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        animator = null
        super.onDetachedFromWindow()
    }
}
