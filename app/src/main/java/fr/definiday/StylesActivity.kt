package fr.definiday

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import android.widget.Toast

/**
 * Couleurs et polices: each palette and font is a tile drawn in itself; a tap keeps it in the pool or removes it
 * (Prefs, saved at once: rotation and Back keep it), under a live card of today's word. At least one of each stays kept.
 */
class StylesActivity : Activity() {
    private lateinit var p: Prefs
    private lateinit var cat: Styles.Catalog
    private lateinit var word: Word
    private lateinit var palettes: Pool
    private lateinit var polices: Pool

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_styles)
        p = Prefs(this)
        cat = Styles.load(this)
        word = Words.today(this).filterIsInstance<Word>().firstOrNull() ?: Words.load(this).first() // today's slot may hold a quote
        palettes = Pool(cat.palettes.associate { it.id to it.nom }, { p.palettes }, { p.palettes = it }, R.id.palettes_count, word.palette)
        polices = Pool(cat.polices.associate { it.id to it.nom }, { p.polices }, { p.polices = it }, R.id.polices_count, word.police)
        findViewById<Button>(R.id.palettes_all).setOnClickListener { palettes.all() }
        findViewById<Button>(R.id.palettes_none).setOnClickListener { palettes.none() }
        findViewById<Button>(R.id.polices_all).setOnClickListener { polices.all() }
        findViewById<Button>(R.id.polices_none).setOnClickListener { polices.none() }

        val (sombres, claires) = cat.palettes.partition { it.sombre }
        grid(R.id.palettes_claires, claires.map(::paletteTile))
        grid(R.id.palettes_sombres, sombres.map(::paletteTile))
        val (deco, texte) = cat.polices.partition { it.id in Styles.DISPLAY_ONLY }
        grid(R.id.polices_texte, texte.map(::policeTile))
        grid(R.id.polices_deco, deco.map(::policeTile))
        palettes.refresh()
        polices.refresh()
    }

    /** Kept ids of one catalog ([names]: id -> nom, in catalog order); [fallback]: what « Aucune » keeps (the word's own). */
    private inner class Pool(val names: Map<String, String>, val get: () -> Set<String>?, val set: (Set<String>?) -> Unit, val count: Int, fallback: String) {
        val tiles = mutableMapOf<String, CheckBox>()
        val fallback = fallback.takeIf { it in names } ?: names.keys.first()
        fun kept() = Styles.kept(names.keys.toList(), get()) { it }.toSet()
        fun save(k: Set<String>) { set(if (k.size == names.size) null else k); refresh() }
        fun all() = save(names.keys)
        fun none() { save(setOf(fallback)); min(fallback) }
        fun toggle(id: String) {
            val k = kept()
            if (k == setOf(id)) { refresh(); min(id) } else save(if (id in k) k - id else k + id)
        }
        private fun min(id: String) = Toast.makeText(this@StylesActivity, getString(R.string.pool_min, names[id]), Toast.LENGTH_SHORT).show()
        fun refresh() {
            val k = kept()
            val d = resources.displayMetrics.density
            tiles.forEach { (id, b) ->
                b.isChecked = id in k
                // kept: a 3 dp ring, as a photo picker; removed: the 1 dp edge that keeps pale tiles visible on the page
                ((b.background as RippleDrawable).getDrawable(0) as GradientDrawable)
                    .setStroke((if (id in k) 3 * d else d).toInt().coerceAtLeast(1), getColor(if (id in k) R.color.accent else R.color.card_edge))
                if (Build.VERSION.SDK_INT >= 30) b.stateDescription = getString(if (id in k) R.string.kept_f else R.string.removed_f)
            }
            findViewById<TextView>(count).text = resources.getQuantityString(R.plurals.pool_count, k.size, k.size, names.size)
            preview()
        }
    }

    /** Today's word as on the home card, and which palette and font it wears now. */
    private fun preview() {
        if (!::polices.isInitialized) return
        val st = Styles.of(this, word)
        val pal = st.palette
        findViewById<View>(R.id.preview).background = Styles.card(this, pal, 20f)
        findViewById<TextView>(R.id.preview_label).setTextColor(pal.nature)
        findViewById<TextView>(R.id.preview_mot).apply { text = word.mot; typeface = st.typeface; setTextColor(pal.mot) }
        findViewById<TextView>(R.id.preview_nature).apply { text = word.nature; setTextColor(pal.nature) }
        findViewById<TextView>(R.id.preview_definition).apply { text = word.definition; setTextColor(pal.texte) }
        val lines = mutableListOf(getString(R.string.preview_wears, pal.nom, st.police?.nom ?: getString(R.string.police_default)))
        for ((pool, own) in listOf(palettes to word.palette, polices to word.police))
            if (own in pool.names && own !in pool.kept()) lines += getString(R.string.preview_prefers, pool.names[own])
        findViewById<TextView>(R.id.preview_hint).text = lines.joinToString(" ")
    }

    private fun paletteTile(pal: Palette) = tile(palettes, pal.id, Styles.card(this, pal, 12f), pal.mot,
        getString(if (pal.sombre) R.string.palette_dark else R.string.palette_light, pal.nom),
        SpannableStringBuilder().add(pal.nom, StyleSpan(Typeface.BOLD), RelativeSizeSpan(1.2f), ForegroundColorSpan(pal.mot))
            .append('\n').add(getString(R.string.tile_sample), StyleSpan(Typeface.ITALIC), ForegroundColorSpan(pal.texte)),
    )

    /** The sample word in the font itself, its name below in plain serif (a fraktur name would not help choosing). */
    private fun policeTile(f: Police) = tile(polices, f.id,
        GradientDrawable().apply { setColor(getColor(R.color.surface)); cornerRadius = 12 * resources.displayMetrics.density },
        getColor(R.color.accent),
        getString(if (f.id in Styles.DISPLAY_ONLY) R.string.police_deco else R.string.police_text, f.nom),
        SpannableStringBuilder().add(getString(R.string.font_sample), RelativeSizeSpan(1.5f), ForegroundColorSpan(getColor(R.color.ink)))
            .append('\n').add(f.nom, TypefaceSpan("serif"), RelativeSizeSpan(0.85f), ForegroundColorSpan(getColor(R.color.ink2))),
    ).apply { typeface = Styles.face(context, f) ?: Typeface.SERIF }

    /** A CheckBox (TalkBack: « Braise, palette sombre, gardée »), 48 dp at least, ripple over its own background. */
    private fun tile(pool: Pool, id: String, bg: GradientDrawable, tint: Int, desc: String, label: CharSequence) = CheckBox(this).apply {
        text = label
        contentDescription = desc
        typeface = Typeface.SERIF
        textSize = 16f
        minHeight = (56 * resources.displayMetrics.density).toInt()
        val pad = (12 * resources.displayMetrics.density).toInt()
        setPadding(pad / 2, pad, pad, pad)
        background = RippleDrawable(ColorStateList.valueOf(getColor(R.color.ripple)), bg, null)
        buttonTintList = ColorStateList.valueOf(tint)
        setOnClickListener { pool.toggle(id) }
        pool.tiles[id] = this
    }

    /** Two tiles a row, one at large font sizes or on narrow screens; rows share their tallest tile's height. */
    private fun grid(box: Int, tiles: List<View>) {
        val cols = if (resources.configuration.fontScale > 1.15f || resources.configuration.screenWidthDp < 360) 1 else 2
        val m = (4 * resources.displayMetrics.density).toInt()
        tiles.chunked(cols).forEach { row ->
            findViewById<LinearLayout>(box).addView(LinearLayout(this).apply {
                (row + List(cols - row.size) { Space(context) }).forEach {
                    addView(it, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply { setMargins(m, m, m, m) })
                }
            })
        }
    }

    private fun SpannableStringBuilder.add(s: CharSequence, vararg spans: Any) = apply {
        val start = length
        append(s)
        spans.forEach { setSpan(it, start, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
    }
}
