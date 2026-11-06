package fr.definiday

import android.content.Context
import org.json.JSONArray
import java.time.LocalDate
import kotlin.random.Random

data class Word(val mot: String, val nature: String, val definition: String, val exemple: String)

object Words {
    fun parse(json: String): List<Word> {
        val a = JSONArray(json)
        return List(a.length()) {
            val o = a.getJSONObject(it)
            Word(o.getString("mot"), o.getString("nature"), o.getString("definition"), o.optString("exemple"))
        }
    }

    /** Fixed shuffle + day index: every word is shown once before any repeats, same word for everyone on a given day. */
    fun forDay(words: List<Word>, epochDay: Long): Word =
        words.shuffled(Random(SEED))[Math.floorMod(epochDay, words.size)]

    fun today(ctx: Context): Word = forDay(load(ctx), LocalDate.now().toEpochDay())

    fun load(ctx: Context): List<Word> = parse(ctx.assets.open("words.json").bufferedReader().use { it.readText() })

    private const val SEED = 20261106
}
