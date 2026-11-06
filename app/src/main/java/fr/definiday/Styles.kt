package fr.definiday

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import org.json.JSONObject

data class Palette(
    val id: String, val nom: String,
    val haut: Int, val bas: Int, val mot: Int, val nature: Int, val texte: Int, val exemple: Int,
)

data class Police(val id: String, val nom: String, val fichier: String)

data class Style(val palette: Palette, val typeface: Typeface)

/** Catalog in assets/styles.json; each word suggests a palette and a font, used only if the user kept them in the pool. */
object Styles {
    class Catalog(val palettes: List<Palette>, val polices: List<Police>)

    fun parse(json: String): Catalog {
        val o = JSONObject(json)
        val p = o.getJSONArray("palettes")
        val f = o.getJSONArray("polices")
        fun c(j: JSONObject, k: String) = Color.parseColor(j.getString(k))
        return Catalog(
            List(p.length()) { p.getJSONObject(it).run { Palette(getString("id"), getString("nom"), c(this, "fond_haut"), c(this, "fond_bas"), c(this, "mot"), c(this, "nature"), c(this, "texte"), c(this, "exemple")) } },
            List(f.length()) { f.getJSONObject(it).run { Police(getString("id"), getString("nom"), getString("fichier")) } },
        )
    }

    fun load(ctx: Context): Catalog = parse(ctx.assets.open("styles.json").bufferedReader().use { it.readText() })

    /** The word's own choice if it is in the pool, else a stable pick from the pool (same word, same fallback). */
    fun <T> pick(suggested: String, pool: List<T>, id: (T) -> String, mot: String): T =
        pool.firstOrNull { id(it) == suggested } ?: pool[Math.floorMod(mot.hashCode(), pool.size)]

    fun of(ctx: Context, w: Word): Style {
        val cat = load(ctx)
        val p = Prefs(ctx)
        val palettes = cat.palettes.filter { p.palettes?.contains(it.id) ?: true }.ifEmpty { cat.palettes }
        val polices = cat.polices.filter { p.polices?.contains(it.id) ?: true }.ifEmpty { cat.polices }
        val police = pick(w.police, polices, Police::id, w.mot)
        val font = ctx.resources.getIdentifier(police.fichier, "font", ctx.packageName)
        return Style(pick(w.palette, palettes, Palette::id, w.mot), if (font == 0) Typeface.SERIF else ctx.resources.getFont(font))
    }
}
