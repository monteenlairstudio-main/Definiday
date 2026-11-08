package fr.definiday

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import org.json.JSONObject

data class Palette(
    val id: String, val nom: String,
    val haut: Int, val bas: Int, val mot: Int, val nature: Int, val texte: Int, val exemple: Int, val sombre: Boolean = false,
)

data class Police(val id: String, val nom: String, val fichier: String)

/** [police]: the font picked, null = serif fallback (only decorative faces kept for a long word or a quote). */
data class Style(val palette: Palette, val typeface: Typeface, val police: Police? = null)

/** Catalog in assets/styles.json; each word suggests a palette and a font, used only if the user kept them in the pool. */
object Styles {
    val DISPLAY_ONLY = setOf("unifraktur_maguntia", "uncial_antiqua", "dancing_script", "parisienne", "cinzel", "abril_fatface")

    /** Faces without U+202F: their Reserved Font Name forbids adding it unrenamed (tools/fonts_nnbsp.py; FontsTest checks the list). */
    val NO_NNBSP = setOf("abril_fatface", "dancing_script", "im_fell_english", "parisienne", "playfair_display",
        "tenor_sans", "uncial_antiqua", "unifraktur_maguntia")

    /** [text] for [st]'s face: in a face without U+202F, « ; : ! ? » would follow the space into the system fallback font, so U+00A0. */
    fun fine(text: String, st: Style) = if (st.police?.id in NO_NNBSP) text.replace(Regex("\u202F(?=[;:!?])"), "\u00A0") else text

    class Catalog(val palettes: List<Palette>, val polices: List<Police>)

    fun parse(json: String): Catalog {
        val o = JSONObject(json)
        val p = o.getJSONArray("palettes")
        val f = o.getJSONArray("polices")
        fun c(j: JSONObject, k: String) = Color.parseColor(j.getString(k))
        return Catalog(
            List(p.length()) { p.getJSONObject(it).run { Palette(getString("id"), getString("nom"), c(this, "fond_haut"), c(this, "fond_bas"), c(this, "mot"), c(this, "nature"), c(this, "texte"), c(this, "exemple"), optBoolean("sombre")) } },
            List(f.length()) { f.getJSONObject(it).run { Police(getString("id"), getString("nom"), getString("fichier")) } },
        )
    }

    /** WCAG contrast ratio of two opaque colors, 1 to 21. */
    fun contrast(a: Int, b: Int): Double {
        val (dark, light) = listOf(a, b).map { Color.luminance(it).toDouble() }.sorted()
        return (light + 0.05) / (dark + 0.05)
    }

    /** The card's gradient nearly melts into the [page] at one end (pale palettes on cream, dark ones at night): it takes a 1 dp edge. */
    fun needsEdge(p: Palette, page: Int) = minOf(contrast(p.haut, page), contrast(p.bas, page)) < 1.5

    fun load(ctx: Context): Catalog = parse(ctx.assets.open("styles.json").bufferedReader().use { it.readText() })

    /** The catalog entries in the user's pool (null = all; none left, e.g. restored junk ids = all). */
    fun <T> kept(all: List<T>, ids: Set<String>?, id: (T) -> String) = all.filter { ids?.contains(id(it)) ?: true }.ifEmpty { all }

    /** The word's own choice if it is in the pool, else a stable pick from the pool (same word, same fallback). */
    fun <T> pick(suggested: String, pool: List<T>, id: (T) -> String, mot: String): T =
        pool.firstOrNull { id(it) == suggested } ?: pool[Math.floorMod(mot.hashCode(), pool.size)]

    /** A word uses its own palette/font when kept; a quote gets a stable pick from the pools. */
    fun of(ctx: Context, item: Item): Style {
        val (pal, pol) = if (item is Word) item.palette to item.police else "" to ""
        val cat = load(ctx)
        val p = Prefs(ctx)
        val palettes = kept(cat.palettes, p.palettes, Palette::id)
        val polices = kept(cat.polices, p.polices, Police::id)
            // a whole sentence or a long word in fraktur or script is unreadable: those only take text faces
            .let { if (item is Quote || item is Word && item.mot.length > 8) it.filter { f -> f.id !in DISPLAY_ONLY } else it }
        val police = polices.takeIf { it.isNotEmpty() }?.let { pick(pol, it, Police::id, item.key) }
        return Style(pick(pal, palettes, Palette::id, item.key), police?.let { face(ctx, it) } ?: Typeface.SERIF, police)
    }

    /** A stamp's single letter: the word's own font when kept in the pool, even a display face (one letter stays readable, style.md § 4). */
    fun initialFace(ctx: Context, item: Item, st: Style): Typeface = (item as? Word)?.let { w ->
        kept(load(ctx).polices, Prefs(ctx).polices, Police::id).firstOrNull { it.id == w.police }?.let { face(ctx, it) }
    } ?: st.typeface

    fun face(ctx: Context, p: Police): Typeface? = ctx.resources.getIdentifier(p.fichier, "font", ctx.packageName).takeIf { it != 0 }?.let(ctx.resources::getFont)

    /** A card in the palette's gradient, rounded [radiusDp]; a 1 dp edge when it melts into the page (needsEdge). */
    fun card(ctx: Context, p: Palette, radiusDp: Float) = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(p.haut, p.bas)).apply {
        val d = ctx.resources.displayMetrics.density
        cornerRadius = radiusDp * d
        if (needsEdge(p, ctx.getColor(R.color.bg))) setStroke(d.toInt().coerceAtLeast(1), ctx.getColor(R.color.card_edge))
    }
}
