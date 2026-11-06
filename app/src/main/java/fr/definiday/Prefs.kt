package fr.definiday

import android.content.Context

class Prefs(ctx: Context) {
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
    var wallpaper: Boolean
        get() = sp.getBoolean("wallpaper", false)
        set(v) = sp.edit().putBoolean("wallpaper", v).apply()
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
