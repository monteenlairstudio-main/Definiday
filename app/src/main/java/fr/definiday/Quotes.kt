package fr.definiday

import android.content.Context
import org.json.JSONArray

/** [cle]: the text the quote was first published with, when its wording was corrected later: history keys must not change (cooldown, counters). */
data class Quote(val texte: String, val auteur: String, val oeuvre: String, val annee: String, val source: String, val theme: String, val cle: String = "") : Item {
    override val key get() = "q:" + cle.ifEmpty { texte }
    /** "Auteur, Œuvre (1850)" */
    val signature get() = (auteur + (if (oeuvre.isEmpty()) "" else ", $oeuvre") + (if (annee.isEmpty()) "" else " ($annee)"))
        .replace(Regex("[ \u00A0\u202F]?([;:!?])"), "\u202F$1") // some titles came without French spacing (« Paris: vie »)
}

object Quotes {
    fun parse(json: String): List<Quote> {
        val a = JSONArray(json)
        return List(a.length()) {
            a.getJSONObject(it).run { Quote(getString("texte"), getString("auteur"), optString("oeuvre"), optString("annee"), getString("source"), optString("theme"), optString("cle")) }
        }
    }

    fun load(ctx: Context): List<Quote> =
        runCatching { parse(ctx.assets.open("citations.json").bufferedReader().use { it.readText() }) }.getOrDefault(emptyList())

    /** A quote about slowness / reading, formatted for the "take your time" dialog. */
    fun slow(ctx: Context): String? =
        load(ctx).filter { it.theme == "lenteur" }.randomOrNull()?.let { "«\u202F${it.texte}\u202F»\n\n${it.signature}" }
}
