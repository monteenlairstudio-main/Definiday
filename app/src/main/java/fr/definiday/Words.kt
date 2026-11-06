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
    /** A shown word may come back (daily or random) only after this many days (~4 months). */
    const val COOLDOWN_DAYS = 120L

    fun parse(json: String): List<Word> {
        val a = JSONArray(json)
        return List(a.length()) {
            val o = a.getJSONObject(it)
            Word(o.getString("mot"), o.getString("nature"), o.getString("definition"), o.optString("exemple"), o.optString("anecdote"), o.optString("source"))
        }
    }

    /** history: mot -> epoch day it was last shown. */
    private fun cooledDown(w: Word, history: Map<String, Long>, today: Long) =
        history[w.mot]?.let { today - it >= COOLDOWN_DAYS } ?: true

    private fun oldest(words: List<Word>, history: Map<String, Long>) = words.minBy { history[it.mot] ?: Long.MIN_VALUE }

    /** Next daily word: never-shown words first (fixed shuffled order), then the longest-ago shown among cooled-down ones. */
    fun nextDaily(words: List<Word>, history: Map<String, Long>, today: Long): Word {
        val order = words.shuffled(Random(SEED))
        return order.firstOrNull { it.mot !in history }
            ?: order.filter { cooledDown(it, history, today) }.takeIf { it.isNotEmpty() }?.let { oldest(it, history) }
            ?: oldest(order, history)
    }

    /** Random word among never-shown and cooled-down words. */
    fun nextRandom(words: List<Word>, history: Map<String, Long>, today: Long, rnd: Random = Random.Default): Word =
        words.filter { cooledDown(it, history, today) }.randomOrNull(rnd) ?: oldest(words, history)

    /** Today's 1–3 words, picked once per day and remembered (stable across app, notifications and wallpaper). */
    fun today(ctx: Context): List<Word> {
        val p = Prefs(ctx)
        val h = History(ctx)
        val day = LocalDate.now().toEpochDay()
        val all = load(ctx)
        val byMot = all.associateBy { it.mot }
        val picked = (if (p.dailyDay == day) p.dailyMots.mapNotNull(byMot::get) else emptyList()).toMutableList()
        while (picked.size < p.wordsPerDay) picked += nextDaily(all, h.all(), day).also { h.mark(it.mot, day) }
        p.dailyDay = day
        p.dailyMots = picked.map { it.mot }
        return picked.take(p.wordsPerDay)
    }

    fun random(ctx: Context): Word {
        val day = LocalDate.now().toEpochDay()
        val h = History(ctx)
        return nextRandom(load(ctx), h.all(), day).also { h.mark(it.mot, day) }
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
