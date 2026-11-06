package fr.definiday

import android.content.Context
import org.json.JSONArray

data class Quote(val texte: String, val auteur: String, val oeuvre: String, val annee: String, val source: String, val theme: String)

object Quotes {
    fun parse(json: String): List<Quote> {
        val a = JSONArray(json)
        return List(a.length()) {
            a.getJSONObject(it).run { Quote(getString("texte"), getString("auteur"), optString("oeuvre"), optString("annee"), getString("source"), optString("theme")) }
        }
    }

    fun load(ctx: Context): List<Quote> =
        runCatching { parse(ctx.assets.open("citations.json").bufferedReader().use { it.readText() }) }.getOrDefault(emptyList())

    /** A quote about slowness / reading, formatted for the "take your time" dialog. */
    fun slow(ctx: Context): String? =
        load(ctx).filter { it.theme == "lenteur" }.randomOrNull()?.let { "« ${it.texte} »\n\n${it.auteur}" + if (it.oeuvre.isEmpty()) "" else ", ${it.oeuvre}" }
}
