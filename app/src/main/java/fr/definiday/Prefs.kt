package fr.definiday

import android.content.Context
import org.json.JSONArray

class Prefs(val ctx: Context) {
    companion object { const val MODE_BOTH = 0; const val MODE_NOTIF = 1; const val MODE_LOCK = 2 }

    private val sp = ctx.getSharedPreferences("definiday", Context.MODE_PRIVATE)
    /** Device-local state, excluded from backup/transfer (res/xml backup and data-extraction rules): a restored wallpaper id could match someone else's wallpaper. */
    private val local = ctx.getSharedPreferences("wallpaper_state", Context.MODE_PRIVATE)

    // Backed-up values may come back restored or tampered with: wrong type or out of range -> default, never a crash.
    private fun int(k: String, d: Int, ok: IntRange = Int.MIN_VALUE..Int.MAX_VALUE) = runCatching { sp.getInt(k, d) }.getOrNull()?.takeIf { it in ok } ?: d
    private fun long(k: String, d: Long) = runCatching { sp.getLong(k, d) }.getOrDefault(d)
    private fun bool(k: String, d: Boolean) = runCatching { sp.getBoolean(k, d) }.getOrDefault(d)
    private fun str(k: String, d: String) = runCatching { sp.getString(k, d) }.getOrNull() ?: d
    private fun strings(k: String) = runCatching { sp.getStringSet(k, null)?.toSet() }.getOrNull()

    /** First-launch onboarding done (or skipped). Users from before it count as done: they already had a day picked. */
    var onboarded: Boolean
        get() = bool("onboarded", sp.contains("dailyDay"))
        set(v) = sp.edit().putBoolean("onboarded", v).apply()
    var enabled: Boolean
        get() = bool("enabled", true)
        set(v) = sp.edit().putBoolean("enabled", v).apply()
    var hour: Int
        get() = int("hour", 8, 0..23)
        set(v) = sp.edit().putInt("hour", v).apply()
    var minute: Int
        get() = int("minute", 0, 0..59)
        set(v) = sp.edit().putInt("minute", v).apply()
    var durationMinutes: Int
        get() = int("duration", 120, 1..1440)
        set(v) = sp.edit().putInt("duration", v).apply()
    /** MODE_BOTH, MODE_NOTIF or MODE_LOCK; defaults from the former "wallpaper" switch. */
    var mode: Int
        get() = int("mode", if (bool("wallpaper", false)) MODE_BOTH else MODE_NOTIF, MODE_BOTH..MODE_LOCK)
        set(v) = sp.edit().putInt("mode", v).apply()
    /** 0 = lock-screen word stays for the duration; 1-3 = removed at the 1st/2nd/3rd unlock (duration still caps it). */
    var unlocks: Int
        get() = int("unlocks", 0, 0..3)
        set(v) = sp.edit().putInt("unlocks", v).apply()
    var unlocksSeen: Int
        get() = local.getInt("unlocksSeen", 0)
        set(v) = local.edit().putInt("unlocksSeen", v).apply()
    /** When our lock-screen wallpaper must be removed; 0 = none of ours is set. */
    var wallpaperEndAt: Long
        get() = local.getLong("wallpaperEndAt", 0)
        set(v) = local.edit().putLong("wallpaperEndAt", v).apply()
    var wallpaperId: Int
        get() = local.getInt("wallpaperId", -1)
        set(v) = local.edit().putInt("wallpaperId", v).apply()
    var wordsPerDay: Int
        get() = int("wordsPerDay", 1, 1..3)
        set(v) = sp.edit().putInt("wordsPerDay", v).apply()
    /** Epoch day and lemmas of the daily words already picked for that day. */
    var dailyDay: Long
        get() = long("dailyDay", Long.MIN_VALUE)
        set(v) = sp.edit().putLong("dailyDay", v).apply()
    /** JSON array (keys may contain any character); older versions stored "a|b|c". */
    var dailyMots: List<String>
        get() = str("dailyMots", "").let { s ->
            runCatching { JSONArray(s).let { a -> List(a.length()) { a.getString(it) } } }.getOrElse { s.split('|').filter(String::isNotEmpty) }
        }
        set(v) = sp.edit().putString("dailyMots", JSONArray(v).toString()).apply()
    /** Lock-screen text anchored at the bottom (clear of the big clock) or starting mid-screen. */
    var textBottom: Boolean
        get() = bool("textBottom", true)
        set(v) = sp.edit().putBoolean("textBottom", v).apply()
    /** "Plus de citations": 2 slots in 5 hold a quote instead of 1. */
    var moreQuotes: Boolean
        get() = bool("moreQuotes", false)
        set(v) = sp.edit().putBoolean("moreQuotes", v).apply()
    /** Little sounds (Sounds), off by default: the app is often used in public. */
    var sounds: Boolean
        get() = bool("sounds", false)
        set(v) = sp.edit().putBoolean("sounds", v).apply()
    /** « Deviner avant de voir »: the home card hides the definition until touched (never on the lock screen). */
    var guessFirst: Boolean
        get() = bool("guessFirst", false)
        set(v) = sp.edit().putBoolean("guessFirst", v).apply()
    /** The rate card was shown once (Quiz.askRate). */
    var rateAsked: Boolean
        get() = bool("rateAsked", false)
        set(v) = sp.edit().putBoolean("rateAsked", v).apply()
    var quizEnabled: Boolean
        get() = bool("quizEnabled", true)
        set(v) = sp.edit().putBoolean("quizEnabled", v).apply()
    var quizHour: Int
        get() = int("quizHour", 21, 0..23)
        set(v) = sp.edit().putInt("quizHour", v).apply()
    var quizMinute: Int
        get() = int("quizMinute", 0, 0..59)
        set(v) = sp.edit().putInt("quizMinute", v).apply()
    // Quiz counters (read by Unlocks)
    var lastQuizDay: Long
        get() = long("lastQuizDay", Long.MIN_VALUE)
        set(v) = sp.edit().putLong("lastQuizDay", v).apply()
    var streak: Int
        get() = int("streak", 0)
        set(v) = sp.edit().putInt("streak", v).apply()
    var quizzes: Int
        get() = int("quizzes", 0)
        set(v) = sp.edit().putInt("quizzes", v).apply()
    var perfectQuizzes: Int
        get() = int("perfectQuizzes", 0)
        set(v) = sp.edit().putInt("perfectQuizzes", v).apply()
    /** Quizzes with at least 2/3 right; users from before this counter start with their perfect quizzes. */
    var passedQuizzes: Int
        get() = int("passedQuizzes", perfectQuizzes)
        set(v) = sp.edit().putInt("passedQuizzes", v).apply()
    var correctAnswers: Int
        get() = int("correctAnswers", 0)
        set(v) = sp.edit().putInt("correctAnswers", v).apply()
    /** Glose's collection: ids ever unlocked (kept when a streak breaks) and ids already announced. */
    var unlocked: Set<String>
        get() = strings("unlocked").orEmpty()
        set(v) = sp.edit().putStringSet("unlocked", v).apply()
    var announced: Set<String>
        get() = strings("announced").orEmpty()
        set(v) = sp.edit().putStringSet("announced", v).apply()
    /** Glose as chosen in the collection (Unlocks ids; "" = no accessory). */
    var glosePose: String
        get() = str("glosePose", "assis")
        set(v) = sp.edit().putString("glosePose", v).apply()
    var glosePelage: String
        get() = str("glosePelage", "tigre")
        set(v) = sp.edit().putString("glosePelage", v).apply()
    var glosePelote: String
        get() = str("glosePelote", "bordeaux")
        set(v) = sp.edit().putString("glosePelote", v).apply()
    var gloseAcc: String
        get() = str("gloseAcc", "")
        set(v) = sp.edit().putString("gloseAcc", v).apply()
    /** Random picks warned about today (warning shown once a day, past 3 picks). */
    var warnedDay: Long
        get() = long("warnedDay", Long.MIN_VALUE)
        set(v) = sp.edit().putLong("warnedDay", v).apply()

    /** Palettes/fonts the user kept in the pool; null = all. */
    var palettes: Set<String>?
        get() = strings("palettes")
        set(v) = sp.edit().putStringSet("palettes", v).apply()
    var polices: Set<String>?
        get() = strings("polices")
        set(v) = sp.edit().putStringSet("polices", v).apply()
}

/** mot -> epoch day it was last shown (daily or random). */
class History(ctx: Context) {
    private val sp = ctx.getSharedPreferences("history", Context.MODE_PRIVATE)
    fun all(): Map<String, Long> = sp.all.mapNotNull { (k, v) -> (v as? Long)?.let { k to it } }.toMap() // restored junk ignored
    fun mark(mot: String, day: Long) = sp.edit().putLong(mot, day).apply()
}
