package fr.definiday

import android.content.Context

class Prefs(ctx: Context) {
    companion object { const val MODE_BOTH = 0; const val MODE_NOTIF = 1; const val MODE_LOCK = 2 }

    private val sp = ctx.getSharedPreferences("definiday", Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = sp.getBoolean("enabled", true)
        set(v) = sp.edit().putBoolean("enabled", v).apply()
    var hour: Int
        get() = sp.getInt("hour", 8)
        set(v) = sp.edit().putInt("hour", v).apply()
    var minute: Int
        get() = sp.getInt("minute", 0)
        set(v) = sp.edit().putInt("minute", v).apply()
    var durationMinutes: Int
        get() = sp.getInt("duration", 120)
        set(v) = sp.edit().putInt("duration", v).apply()
    /** MODE_BOTH, MODE_NOTIF or MODE_LOCK; defaults from the former "wallpaper" switch. */
    var mode: Int
        get() = sp.getInt("mode", if (sp.getBoolean("wallpaper", false)) MODE_BOTH else MODE_NOTIF)
        set(v) = sp.edit().putInt("mode", v).apply()
    /** 0 = lock-screen word stays for the duration; 1-3 = removed at the 1st/2nd/3rd unlock (duration still caps it). */
    var unlocks: Int
        get() = sp.getInt("unlocks", 0)
        set(v) = sp.edit().putInt("unlocks", v).apply()
    var unlocksSeen: Int
        get() = sp.getInt("unlocksSeen", 0)
        set(v) = sp.edit().putInt("unlocksSeen", v).apply()
    /** When our lock-screen wallpaper must be removed; 0 = none of ours is set. */
    var wallpaperEndAt: Long
        get() = sp.getLong("wallpaperEndAt", 0)
        set(v) = sp.edit().putLong("wallpaperEndAt", v).apply()
    var wallpaperId: Int
        get() = sp.getInt("wallpaperId", -1)
        set(v) = sp.edit().putInt("wallpaperId", v).apply()
    var wordsPerDay: Int
        get() = sp.getInt("wordsPerDay", 1)
        set(v) = sp.edit().putInt("wordsPerDay", v).apply()
    /** Epoch day and lemmas of the daily words already picked for that day. */
    var dailyDay: Long
        get() = sp.getLong("dailyDay", Long.MIN_VALUE)
        set(v) = sp.edit().putLong("dailyDay", v).apply()
    var dailyMots: List<String>
        get() = sp.getString("dailyMots", "")!!.split('|').filter { it.isNotEmpty() }
        set(v) = sp.edit().putString("dailyMots", v.joinToString("|")).apply()
}

/** mot -> epoch day it was last shown (daily or random). */
class History(ctx: Context) {
    private val sp = ctx.getSharedPreferences("history", Context.MODE_PRIVATE)
    fun all(): Map<String, Long> = sp.all.mapValues { it.value as Long }
    fun mark(mot: String, day: Long) = sp.edit().putLong(mot, day).apply()
}
