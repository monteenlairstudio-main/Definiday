package fr.definiday

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.MetricAffectingSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import android.widget.Toast

/**
 * Couleurs et polices: each palette is a small postcard in itself, each font a sheet with a sample in it; a tap keeps it in the pool
 * or removes it (Prefs, saved at once: rotation and Back keep it), under a live card of today's word. At least one of each stays kept.
 */
class StylesActivity : Activity() {
    private lateinit var p: Prefs
    private lateinit var cat: Styles.Catalog
    private lateinit var word: Word
    private lateinit var palettes: Pool
    private lateinit var polices: Pool
    private val dp by lazy { resources.displayMetrics.density }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_styles)
        p = Prefs(this)
        cat = Styles.load(this)
        word = Words.today(this).filterIsInstance<Word>().firstOrNull() ?: Words.load(this).first() // today's slot may hold a quote
        palettes = Pool(cat.palettes.associate { it.id to it.nom }, { p.palettes }, { p.palettes = it; Widget.update(this) }, R.id.palettes_count, word.palette)
        polices = Pool(cat.polices.associate { it.id to it.nom }, { p.polices }, { p.polices = it }, R.id.polices_count, word.police)
        findViewById<Button>(R.id.palettes_all).setOnClickListener { palettes.all() }
        findViewById<Button>(R.id.palettes_none).setOnClickListener { palettes.none() }
        findViewById<Button>(R.id.polices_all).setOnClickListener { polices.all() }
        findViewById<Button>(R.id.polices_none).setOnClickListener { polices.none() }

        // at large font sizes: the count above its links (a postal mention never squeezed letter by letter)
        if (resources.configuration.fontScale > 1.15f) for (bar in listOf(R.id.palettes_bar, R.id.polices_bar)) findViewById<LinearLayout>(bar).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.START
            getChildAt(0).layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        // two tiles a row; one when a name would break on a letter (palettes past x1.5, font samples past x1.15)
        val (sombres, claires) = cat.palettes.partition { it.sombre }
        grid(R.id.palettes_claires, claires.map(::paletteTile), 1.5f)
        grid(R.id.palettes_sombres, sombres.map(::paletteTile), 1.5f)
        val (deco, texte) = cat.polices.partition { it.id in Styles.DISPLAY_ONLY }
        grid(R.id.polices_texte, texte.map(::policeTile), 1.15f)
        grid(R.id.polices_deco, deco.map(::policeTile), 1.15f)
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
            tiles.forEach { (id, b) ->
                b.isChecked = id in k // the tile's paper draws the outline and the check (tile)
                if (Build.VERSION.SDK_INT >= 30) b.stateDescription = getString(if (id in k) R.string.kept_f else R.string.removed_f)
            }
            findViewById<TextView>(count).text = resources.getQuantityString(R.plurals.pool_count, k.size, k.size, names.size)
            preview()
        }
    }

    /** Today's word as on the home postcard, and which palette and font it wears now. */
    private fun preview() {
        if (!::polices.isInitialized) return
        val st = Styles.of(this, word)
        val pal = st.palette
        findViewById<View>(R.id.preview).background = Postal.postcard(this, pal)
        findViewById<TextView>(R.id.preview_label).setTextColor(pal.nature)
        findViewById<TextView>(R.id.preview_mot).apply { text = word.mot; typeface = st.typeface; setTextColor(pal.mot) }
        findViewById<TextView>(R.id.preview_nature).apply { text = word.nature; setTextColor(pal.nature) }
        findViewById<TextView>(R.id.preview_definition).apply { text = word.definition; setTextColor(pal.texte) }
        val lines = mutableListOf(getString(R.string.preview_wears, pal.nom, st.police?.nom ?: getString(R.string.police_default)))
        for ((pool, own) in listOf(palettes to word.palette, polices to word.police))
            if (own in pool.names && own !in pool.kept()) lines += getString(R.string.preview_prefers, pool.names[own])
        findViewById<TextView>(R.id.preview_hint).text = lines.joinToString(" ")
    }

    /** A small postcard in the palette, its name alone in the palette's word colour (no filler sample, no synthetic italic). */
    private fun paletteTile(pal: Palette) = tile(palettes, pal.id, { Postal.postcard(this, pal, resources.getDimension(R.dimen.radius_small)) }, pal.mot,
        getString(if (pal.sombre) R.string.palette_dark else R.string.palette_light, pal.nom),
        SpannableStringBuilder().add(pal.nom, StyleSpan(Typeface.BOLD), RelativeSizeSpan(1.1f), ForegroundColorSpan(pal.mot)),
    )

    /** A sheet with the sample word in the font itself, its name below in the interface serif (a fraktur name would not help choosing). */
    private fun policeTile(f: Police) = tile(polices, f.id,
        { GradientDrawable().apply { setColor(getColor(R.color.sheet)); setStroke(dp.toInt().coerceAtLeast(1), getColor(R.color.card_edge)); cornerRadius = resources.getDimension(R.dimen.radius_small) } },
        getColor(R.color.accent),
        getString(if (f.id in Styles.DISPLAY_ONLY) R.string.police_deco else R.string.police_text, f.nom),
        SpannableStringBuilder().add(getString(R.string.font_sample), Face(Styles.face(this, f) ?: Typeface.SERIF), RelativeSizeSpan(1.5f), ForegroundColorSpan(getColor(R.color.ink)))
            // « UnifrakturMaguntia » breaks between its two words, never on a lone letter
            .append('\n').add(f.nom.replace(Regex("(?<=\\p{Ll})(?=\\p{Lu})"), "​"), RelativeSizeSpan(0.85f), ForegroundColorSpan(getColor(R.color.ink2))),
    )

    /** A CheckBox (TalkBack: « Braise, palette sombre, gardée »), 56 dp at least, on its [paper]. Kept: the 2 dp plume outline and a check
     *  in [tint] at the end (the one selection look, no checkbox square); focused: 3 dp; ripple over it. */
    private fun tile(pool: Pool, id: String, paper: () -> Drawable, tint: Int, desc: String, label: CharSequence) = CheckBox(this).apply {
        text = label
        contentDescription = desc
        textSize = 16f
        minHeight = (56 * dp).toInt()
        val pad = (14 * dp).toInt()
        setPadding(pad, pad, pad / 2, pad)
        buttonDrawable = null
        fun ring(width: Int) = LayerDrawable(arrayOf(paper(), GradientDrawable().apply {
            setStroke((width * dp).toInt(), getColor(R.color.accent)); cornerRadius = resources.getDimension(R.dimen.radius_small)
        }))
        background = RippleDrawable(ColorStateList.valueOf(getColor(R.color.ripple)), StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_focused), ring(3))
            addState(intArrayOf(android.R.attr.state_checked), ring(2))
            addState(intArrayOf(), paper())
        }, null)
        setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_checked), getDrawable(R.drawable.ic_check)!!.mutate().apply { setTint(tint) })
            val size = (20 * dp).toInt()
            addState(intArrayOf(), GradientDrawable().apply { setSize(size, size) }) // empty, same size: the text does not move
        }, null)
        compoundDrawablePadding = (4 * dp).toInt()
        setOnClickListener { pool.toggle(id) }
        pool.tiles[id] = this
    }

    /** A span in one font file (TypefaceSpan(Typeface) needs API 28). */
    private class Face(val face: Typeface) : MetricAffectingSpan() {
        override fun updateDrawState(tp: TextPaint) { tp.typeface = face }
        override fun updateMeasureState(tp: TextPaint) { tp.typeface = face }
    }

    /** Two tiles a row, one past font scale [single] or on narrow screens; rows share their tallest tile's height. */
    private fun grid(box: Int, tiles: List<View>, single: Float) {
        val cols = if (resources.configuration.fontScale > single || resources.configuration.screenWidthDp < 360) 1 else 2
        val m = (4 * dp).toInt()
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
