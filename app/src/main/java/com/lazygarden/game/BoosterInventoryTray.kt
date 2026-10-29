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
    private val names = linkedMapOf<Match3Engine.Booster, TextView>()
    private val tiles = linkedMapOf<Match3Engine.Booster, View>()
    private val lastCounts = linkedMapOf<Match3Engine.Booster, Int>()
    private var gameplayEnabled = true
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
            addView(makeTile(kind, icons[index]), LayoutParams(0, dp(84), 1f).apply {
                setMargins(dp(2), dp(2), dp(2), 0)
            })
        }
        refresh(false)
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
            contentDescription = GameText.booster(save.language,kind)
        }, LayoutParams(dp(34), dp(34)))
        val name = TextView(context).apply {
            textSize = 8.5f
            maxLines = 1
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xff654123.toInt())
        }
        tile.addView(name, LayoutParams(-1, dp(17)))
        val badge = TextView(context).apply {
            textSize = 11.5f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        tile.addView(badge, LayoutParams(-1, dp(23)))
        tile.setOnClickListener {
            if (!gameplayEnabled) return@setOnClickListener
            press(tile)
            if (save.boosterCount(kind) > 0) onUse(kind) else onBuy(kind)
        }
        tile.setOnLongClickListener {
            if (!gameplayEnabled) false else { press(tile); onBuy(kind); true }
        }
        badges[kind] = badge
        names[kind] = name
        tiles[kind] = tile
        return tile
    }

    private fun press(view: View) {
        view.animate().cancel()
        view.animate().scaleX(.90f).scaleY(.90f).setDuration(70).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
        }.start()
    }

    /** Locks boosters on replay/archived levels while keeping their stock visible. */
    fun setGameplayEnabled(enabled: Boolean) {
        gameplayEnabled = enabled
        refresh(false)
    }

    /** Refresh immediately after use/purchase: x3 -> x2 -> x1 -> buy state. */
    fun refresh(animateChanges: Boolean = true) {
        Match3Engine.Booster.entries.forEach { kind ->
            val count = save.boosterCount(kind)
            val previous = lastCounts.put(kind, count)
            names[kind]?.text = GameText.booster(save.language,kind)
            badges[kind]?.apply {
                text = if (count > 0) "×$count" else "+ ${kind.cost}"
                setTextColor(if (count > 0) 0xff3f6d36.toInt() else 0xff8b4a2b.toInt())
                if (animateChanges && previous != null && previous != count) {
                    animate().cancel()
                    scaleX = .72f; scaleY = .72f
                    animate().scaleX(1.18f).scaleY(1.18f).setDuration(120).withEndAction {
                        animate().scaleX(1f).scaleY(1f).setDuration(140).start()
                    }.start()
                }
            }
            tiles[kind]?.apply {
                isEnabled = gameplayEnabled
                alpha = when { !gameplayEnabled -> .42f; count > 0 -> 1f; else -> .72f }
                isActivated = gameplayEnabled && count > 0
                contentDescription = when {
                    !gameplayEnabled -> "${GameText.booster(save.language,kind)}, ${t("locked")}"
                    count > 0 -> "${GameText.booster(save.language,kind)}, $count ${t("stock")}"
                    else -> "${GameText.booster(save.language,kind)}, ${t("no_stock")}, ${t("buy")} ${kind.cost} ${t("coins")}"
                }
            }
        }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
