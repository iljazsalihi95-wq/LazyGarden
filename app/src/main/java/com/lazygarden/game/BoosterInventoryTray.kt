package com.lazygarden.game

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/** Premium Match-3 booster inventory backed only by persistent GardenSave stock. */
class BoosterInventoryTray(
    context: Context,
    private val save: GardenSave,
    private val onUse: (Match3Engine.Booster) -> Unit,
    private val onBuy: (Match3Engine.Booster) -> Unit
) : LinearLayout(context) {
    private val badges = linkedMapOf<Match3Engine.Booster, TextView>()
    private val tiles = linkedMapOf<Match3Engine.Booster, View>()
    private val icons = intArrayOf(
        R.drawable.boost_hammer, R.drawable.boost_row, R.drawable.boost_column,
        R.drawable.boost_cross, R.drawable.boost_bomb, R.drawable.boost_color,
        R.drawable.boost_shuffle
    )
    private fun t(key:String)=GameText.get(save.language,key)

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        setPadding(dp(5), dp(2), dp(5), dp(7))
        Match3Engine.Booster.entries.forEachIndexed { index, kind ->
            addView(makeTile(kind, icons[index]), LayoutParams(0, dp(76), 1f).apply {
                setMargins(dp(2), dp(2), dp(2), 0)
            })
        }
        refresh()
    }

    private fun makeTile(kind: Match3Engine.Booster, icon: Int): View {
        val tile = LinearLayout(context).apply {
            orientation = VERTICAL
            gravity = Gravity.CENTER
            background = GameSkin.background(context, R.drawable.hd_hud, 11f)
            setPadding(dp(2), dp(3), dp(2), dp(3))
        }
        tile.addView(ImageView(context).apply {
            setImageResource(icon)
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = kind.label
        }, LayoutParams(dp(38), dp(38)))
        val badge = TextView(context).apply {
            textSize = 12f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        tile.addView(badge, LayoutParams(-1, dp(25)))
        tile.setOnClickListener {
            it.animate().scaleX(.90f).scaleY(.90f).setDuration(70).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
            }.start()
            if (save.boosterCount(kind) > 0) onUse(kind) else onBuy(kind)
        }
        tile.setOnLongClickListener { onBuy(kind); true }
        badges[kind] = badge
        tiles[kind] = tile
        return tile
    }

    /** Refresh immediately after use/purchase: x3 -> x2 -> x1 -> buy state. */
    fun refresh() {
        Match3Engine.Booster.entries.forEach { kind ->
            val count = save.boosterCount(kind)
            badges[kind]?.apply {
                text = if (count > 0) "×$count" else "+ ${kind.cost}"
                setTextColor(if (count > 0) 0xff3f6d36.toInt() else 0xff8b4a2b.toInt())
            }
            tiles[kind]?.apply {
                alpha = if (count > 0) 1f else .72f
                isActivated = count > 0
                contentDescription = if (count > 0)
                    "${kind.label}, $count ${t("stock")}"
                else "${kind.label}, ${t("no_stock")}, ${t("buy")} ${kind.cost} ${t("coins")}"
            }
        }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
