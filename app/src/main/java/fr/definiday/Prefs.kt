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
    /** Lock-screen text anchored at the bottom (clear of the big clock) or starting mid-screen. */
    var textBottom: Boolean
        get() = sp.getBoolean("textBottom", true)
        set(v) = sp.edit().putBoolean("textBottom", v).apply()
    var quizEnabled: Boolean
        get() = sp.getBoolean("quizEnabled", true)
        set(v) = sp.edit().putBoolean("quizEnabled", v).apply()
    var quizHour: Int
        get() = sp.getInt("quizHour", 21)
        set(v) = sp.edit().putInt("quizHour", v).apply()
    var quizMinute: Int
        get() = sp.getInt("quizMinute", 0)
        set(v) = sp.edit().putInt("quizMinute", v).apply()
    // Quiz counters (read by future mascot unlocks)
    var lastQuizDay: Long
        get() = sp.getLong("lastQuizDay", Long.MIN_VALUE)
        set(v) = sp.edit().putLong("lastQuizDay", v).apply()
    var streak: Int
        get() = sp.getInt("streak", 0)
        set(v) = sp.edit().putInt("streak", v).apply()
    var quizzes: Int
        get() = sp.getInt("quizzes", 0)
        set(v) = sp.edit().putInt("quizzes", v).apply()
    var perfectQuizzes: Int
        get() = sp.getInt("perfectQuizzes", 0)
        set(v) = sp.edit().putInt("perfectQuizzes", v).apply()
    var correctAnswers: Int
        get() = sp.getInt("correctAnswers", 0)
        set(v) = sp.edit().putInt("correctAnswers", v).apply()
    /** Random picks warned about today (warning shown once a day, past 3 picks). */
    var warnedDay: Long
        get() = sp.getLong("warnedDay", Long.MIN_VALUE)
        set(v) = sp.edit().putLong("warnedDay", v).apply()

    /** Palettes/fonts the user kept in the pool; null = all. */
    var palettes: Set<String>?
        get() = sp.getStringSet("palettes", null)
        set(v) = sp.edit().putStringSet("palettes", v).apply()
    var polices: Set<String>?
        get() = sp.getStringSet("polices", null)
        set(v) = sp.edit().putStringSet("polices", v).apply()
}

/** mot -> epoch day it was last shown (daily or random). */
class History(ctx: Context) {
    private val sp = ctx.getSharedPreferences("history", Context.MODE_PRIVATE)
    fun all(): Map<String, Long> = sp.all.mapValues { it.value as Long }
    fun mark(mot: String, day: Long) = sp.edit().putLong(mot, day).apply()
}
