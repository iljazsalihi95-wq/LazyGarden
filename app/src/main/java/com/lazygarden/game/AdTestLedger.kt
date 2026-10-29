package com.lazygarden.game

import android.content.Context

/** Local production-test counters. No personal data and no ad identifiers are stored. */
class AdTestLedger(ctx: Context) {
    private val p = ctx.getSharedPreferences("ad_test_ledger", Context.MODE_PRIVATE)

    data class Snapshot(
        val unityAttempts: Int,
        val unityCompleted: Int,
        val admobAttempts: Int,
        val admobCompleted: Int
    ) {
        val attempts get() = unityAttempts + admobAttempts
        val completed get() = unityCompleted + admobCompleted
    }

    fun attempt(provider: String) {
        if (provider !in setOf("unity", "admob")) return
        val key = "${provider}_attempts"
        p.edit().putInt(key, p.getInt(key, 0).coerceAtLeast(0) + 1).apply()
    }

    fun completed(provider: String) {
        if (provider !in setOf("unity", "admob")) return
        val key = "${provider}_completed"
        p.edit().putInt(key, p.getInt(key, 0).coerceAtLeast(0) + 1).apply()
    }

    fun snapshot() = Snapshot(
        p.getInt("unity_attempts", 0).coerceAtLeast(0),
        p.getInt("unity_completed", 0).coerceAtLeast(0),
        p.getInt("admob_attempts", 0).coerceAtLeast(0),
        p.getInt("admob_completed", 0).coerceAtLeast(0)
    )
}
