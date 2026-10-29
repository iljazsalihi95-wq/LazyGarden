package com.lazygarden.game

import android.content.Context

/** Local production-test counters. No personal data and no ad identifiers are stored. */
class AdTestLedger(ctx: Context) {
    private val p = ctx.getSharedPreferences("ad_test_ledger", Context.MODE_PRIVATE)

    data class Snapshot(
        val unityAttempts: Int,
        val unityCompleted: Int,
        val admobAttempts: Int,
        val admobCompleted: Int,
        val coinRewards: Int,
        val starRewards: Int,
        val moveRewards: Int
    ) {
        val attempts get() = unityAttempts + admobAttempts
        val completed get() = unityCompleted + admobCompleted
        val failedOrUnavailable get() = (attempts - completed).coerceAtLeast(0)
        val completionRate get() = if (attempts == 0) 0f else completed.toFloat() / attempts
        val unityCompletionRate get() = if (unityAttempts == 0) 0f else unityCompleted.toFloat() / unityAttempts
        val admobCompletionRate get() = if (admobAttempts == 0) 0f else admobCompleted.toFloat() / admobAttempts
        val trackedRewards get() = coinRewards + starRewards + moveRewards
    }

    fun attempt(provider: String) {
        if (provider !in setOf("unity", "admob")) return
        increment("${provider}_attempts")
    }

    fun completed(provider: String, rewardKind: String = "") {
        if (provider !in setOf("unity", "admob")) return
        increment("${provider}_completed")
        when (rewardKind) {
            "coins" -> increment("reward_coins")
            "star" -> increment("reward_stars")
            "moves" -> increment("reward_moves")
        }
    }

    private fun increment(key: String) {
        p.edit().putInt(key, p.getInt(key, 0).coerceAtLeast(0) + 1).apply()
    }

    fun snapshot() = Snapshot(
        p.getInt("unity_attempts", 0).coerceAtLeast(0),
        p.getInt("unity_completed", 0).coerceAtLeast(0),
        p.getInt("admob_attempts", 0).coerceAtLeast(0),
        p.getInt("admob_completed", 0).coerceAtLeast(0),
        p.getInt("reward_coins", 0).coerceAtLeast(0),
        p.getInt("reward_stars", 0).coerceAtLeast(0),
        p.getInt("reward_moves", 0).coerceAtLeast(0)
    )
}
