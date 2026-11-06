package fr.definiday

import android.content.Context
import org.json.JSONArray
import java.time.LocalDate
import kotlin.random.Random

data class Word(
    val mot: String, val nature: String, val definition: String, val exemple: String,
    val anecdote: String = "", val source: String = "",
)

object Words {
    /** A daily word was not shown (daily or random) in the previous 9 months... */
    const val DAILY_COOLDOWN_DAYS = 270L
    /** ...a random pick, in the previous 4 months when possible. */
    const val RANDOM_COOLDOWN_DAYS = 120L

    fun parse(json: String): List<Word> {
        val a = JSONArray(json)
        return List(a.length()) {
            val o = a.getJSONObject(it)
            Word(o.getString("mot"), o.getString("nature"), o.getString("definition"), o.optString("exemple"), o.optString("anecdote"), o.optString("source"))
        }
    }

    /**
     * Daily queue: never-shown words first (fixed shuffled order), then by oldest last-shown day (history: mot -> epoch day).
     * Random picks never touch the first wordsPerDay x 270 entries, so each of them needs 270 days of daily
     * consumption to reach the head: a daily word is always unseen for 9 months, however often "random" is used.
     */
    private fun queue(words: List<Word>, history: Map<String, Long>) =
        words.shuffled(Random(SEED)).sortedBy { history[it.mot] ?: Long.MIN_VALUE }

    fun nextDaily(words: List<Word>, history: Map<String, Long>): Word = queue(words, history).first()

    /** Random word outside the daily reserve, unseen for 4 months; else the oldest-shown outside the reserve. */
    fun nextRandom(words: List<Word>, history: Map<String, Long>, today: Long, wordsPerDay: Int, rnd: Random = Random.Default): Word {
        val pool = queue(words, history).drop((wordsPerDay * DAILY_COOLDOWN_DAYS).toInt()).ifEmpty { words }
        return pool.filter { w -> history[w.mot]?.let { today - it >= RANDOM_COOLDOWN_DAYS } ?: true }.randomOrNull(rnd) ?: pool.first()
    }

    /** Today's 1–3 words, picked once per day and remembered (stable across app, notifications and wallpaper). */
    fun today(ctx: Context): List<Word> {
        val p = Prefs(ctx)
        val h = History(ctx)
        val day = LocalDate.now().toEpochDay()
        val all = load(ctx)
        val byMot = all.associateBy { it.mot }
        val picked = (if (p.dailyDay == day) p.dailyMots.mapNotNull(byMot::get) else emptyList()).toMutableList()
        while (picked.size < p.wordsPerDay) picked += nextDaily(all, h.all()).also { h.mark(it.mot, day) }
        p.dailyDay = day
        p.dailyMots = picked.map { it.mot }
        return picked.take(p.wordsPerDay)
    }

    fun random(ctx: Context): Word {
        val day = LocalDate.now().toEpochDay()
        val h = History(ctx)
        return nextRandom(load(ctx), h.all(), day, Prefs(ctx).wordsPerDay).also { h.mark(it.mot, day) }
    }

    /** Every word shown today (daily ones first, then random picks). */
    fun seenToday(ctx: Context): List<Word> {
        val day = LocalDate.now().toEpochDay()
        val byMot = load(ctx).associateBy { it.mot }
        val daily = Prefs(ctx).run { if (dailyDay == day) dailyMots else emptyList() }
        val random = History(ctx).all().filterValues { it == day }.keys.filter { it !in daily }.sorted()
        return (daily + random).mapNotNull(byMot::get)
    }

    // ponytail: re-parses the asset on each call (~1 ms for 1 200 words); cache it if it ever shows in a profile
    fun load(ctx: Context): List<Word> = parse(ctx.assets.open("words.json").bufferedReader().use { it.readText() })

    private const val SEED = 20261106
}
