package fr.definiday

import android.content.Context
import org.json.JSONArray
import java.time.LocalDate
import kotlin.random.Random

/** Something shown in a daily slot: a word or a quote. key is its history / storage id. */
sealed interface Item { val key: String }

data class Word(
    val mot: String, val nature: String, val definition: String, val exemple: String,
    val anecdote: String = "", val source: String = "",
    val palette: String = "", val police: String = "",
) : Item { override val key get() = mot }

object Words {
    /** A daily word was not shown (daily or random) in the previous 9 months... */
    const val DAILY_COOLDOWN_DAYS = 270L
    /** ...a random pick, in the previous 4 months when possible. */
    const val RANDOM_COOLDOWN_DAYS = 120L

    fun parse(json: String): List<Word> {
        val a = JSONArray(json)
        return List(a.length()) {
            val o = a.getJSONObject(it)
            Word(o.getString("mot"), o.getString("nature"), o.getString("definition"), o.optString("exemple"), o.optString("anecdote"), o.optString("source"),
                o.optString("palette"), o.optString("police"))
        }
    }

    /**
     * Daily queue: never-shown words first (fixed shuffled order), then by oldest last-shown day (history: mot -> epoch day).
     * Random picks never touch the first wordsPerDay x 270 entries, so each of them needs 270 days of daily
     * consumption to reach the head: a daily word is always unseen for 9 months, however often "random" is used.
     */
    private fun <T : Item> queue(items: List<T>, history: Map<String, Long>) =
        items.shuffled(Random(SEED)).sortedBy { history[it.key] ?: Long.MIN_VALUE }

    fun <T : Item> nextDaily(items: List<T>, history: Map<String, Long>): T = queue(items, history).first()

    /** Slot `slot` of day `day` holds a quote for 1 slot in 5 (2 in 5 with "more quotes"), evenly over days and slots. */
    fun isQuoteSlot(day: Long, slot: Int, moreQuotes: Boolean) = Math.floorMod(day * 3 + slot, 5L) < if (moreQuotes) 2 else 1

    /** Every daily slot of [day] is a quote: no quiz material that day (unless random picks). */
    fun quotesOnly(day: Long, wordsPerDay: Int, moreQuotes: Boolean) = (0 until wordsPerDay).all { isQuoteSlot(day, it, moreQuotes) }

    /** Random word outside the daily reserve, unseen for 4 months; else the oldest-shown outside the reserve. */
    fun nextRandom(words: List<Word>, history: Map<String, Long>, today: Long, wordsPerDay: Int, rnd: Random = Random.Default): Word {
        val pool = queue(words, history).drop((wordsPerDay * DAILY_COOLDOWN_DAYS).toInt()).ifEmpty { words }
        return pool.filter { w -> history[w.mot]?.let { today - it >= RANDOM_COOLDOWN_DAYS } ?: true }.randomOrNull(rnd) ?: pool.first()
    }

    /**
     * Today's 1–3 items (words and quotes), picked once per day and remembered (stable across app, notifications and wallpaper).
     * Picking does not mark them seen: [shown] does, once a notification, the wallpaper or the app card really shows one.
     */
    fun today(ctx: Context): List<Item> {
        val p = Prefs(ctx)
        val day = LocalDate.now().toEpochDay()
        val all = load(ctx)
        val quotes = Quotes.load(ctx)
        val byKey = (all + quotes).associateBy { it.key }
        val picked = (if (p.dailyDay == day) p.dailyMots.mapNotNull(byKey::get) else emptyList()).toMutableList()
        val h = History(ctx).all().toMutableMap()
        picked.forEach { h[it.key] = day } // reserved: not picked twice
        while (picked.size < p.wordsPerDay) {
            val quote = quotes.isNotEmpty() && isQuoteSlot(day, picked.size, p.moreQuotes)
            picked += (if (quote) nextDaily(quotes, h) else nextDaily(all, h)).also { h[it.key] = day }
        }
        p.dailyDay = day
        p.dailyMots = picked.map { it.key }
        return picked.take(p.wordsPerDay)
    }

    fun random(ctx: Context): Word {
        val day = LocalDate.now().toEpochDay()
        val h = History(ctx)
        return nextRandom(load(ctx), h.all(), day, Prefs(ctx).wordsPerDay).also { h.mark(it.mot, day) }
    }

    /** Marks items as seen today (history, quiz): called only where they were really shown. */
    fun shown(ctx: Context, items: List<Item>) {
        val day = LocalDate.now().toEpochDay()
        History(ctx).run { items.forEach { mark(it.key, day) } }
    }

    /** Every word (not quote) shown on [day] (today by default), daily ones first, then random picks: the quiz material. */
    fun seenToday(ctx: Context, day: Long = LocalDate.now().toEpochDay()): List<Word> {
        val byMot = load(ctx).associateBy { it.mot }
        val shown = History(ctx).all().filterValues { it == day }.keys
        val daily = Prefs(ctx).run { if (dailyDay == day) dailyMots else emptyList() }.filter { it in shown }
        return (daily + (shown - daily.toSet()).sorted()).mapNotNull(byMot::get)
    }

    // ponytail: re-parses the asset on each call (~1 ms for 1 200 words); cache it if it ever shows in a profile
    fun load(ctx: Context): List<Word> = parse(ctx.assets.open("words.json").bufferedReader().use { it.readText() })

    private const val SEED = 20261106
}
