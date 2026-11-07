package fr.definiday

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import java.text.Normalizer
import java.time.LocalDate
import java.util.Locale

/** « Mes mots »: light stats, then every word seen (newest first) with a search, a « Favoris » filter and a star each.
 *  A tap opens the word in DetailActivity (a review: not shown again as today's). Search and filter stay fixed above the scrolling list. */
class MotsActivity : Activity() {
    private lateinit var memory: Memory
    private lateinit var search: EditText
    private lateinit var favOnly: Switch
    private lateinit var stats: GridLayout
    private lateinit var empty: TextView
    private var all = emptyList<Word>()
    private var rows = emptyList<Word>()
    private val adapter = Rows()
    private val dp by lazy { resources.displayMetrics.density }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        memory = Memory(this)
        all = Words.load(this)
        val pad = (24 * dp).toInt()
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; fitsSystemWindows = true }
        val head = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(pad, pad, pad, 0) }.also(root::addView)
        head.addView(TextView(this, null, 0, R.style.Label).apply { text = getString(R.string.mots_title) })
        search = EditText(this).apply {
            hint = getString(R.string.mots_search); textSize = 16f; minHeight = (48 * dp).toInt()
            inputType = InputType.TYPE_CLASS_TEXT; imeOptions = EditorInfo.IME_ACTION_SEARCH // one line to type (Enter searches)...
            setHorizontallyScrolling(false); maxLines = 3 // ...but the hint wraps at large font sizes instead of being cut
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
                override fun afterTextChanged(s: Editable?) = refresh()
            })
        }
        head.addView(search, LinearLayout.LayoutParams(-1, -2).apply { topMargin = (8 * dp).toInt() })
        favOnly = Switch(this).apply { // R.style.Toggle by hand: as a defStyleRes it would drop the platform switch style
            text = getString(R.string.mots_favourites); textSize = 16f; setTextColor(getColor(R.color.ink)); minHeight = (48 * dp).toInt()
            thumbTintList = getColorStateList(R.color.switch_thumb); trackTintList = getColorStateList(R.color.switch_track); trackTintMode = android.graphics.PorterDuff.Mode.SRC_OVER
            setOnCheckedChangeListener { _, _ -> refresh() }
        }
        head.addView(favOnly, LinearLayout.LayoutParams(-1, -2))
        stats = GridLayout(this).apply { setPadding(0, (8 * dp).toInt(), 0, (8 * dp).toInt()) }
        empty = TextView(this, null, 0, R.style.Body).apply { setTextColor(getColor(R.color.ink2)); setPadding(0, (16 * dp).toInt(), 0, 0) }
        val list = ListView(this).apply {
            divider = null; setPadding(pad, 0, pad, pad); clipToPadding = false
            addHeaderView(stats, null, false); addFooterView(empty, null, false)
            adapter = this@MotsActivity.adapter
        }
        root.addView(list, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    /** Also on the way back from a word's detail: it may have been starred there. */
    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val history = History(this).all()
        val favs = memory.favourites
        val seen = list(all, history)
        rows = list(all, history, search.text.toString(), favs.takeIf { favOnly.isChecked })
        adapter.notifyDataSetChanged()
        empty.text = getString(when { seen.isEmpty() -> R.string.mots_none; favOnly.isChecked && favs.isEmpty() -> R.string.mots_none_favourites; else -> R.string.mots_none_found })
        empty.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE
        showStats(seen.size, favs.count { it in history })
    }

    /** Six tiles: words seen, favourites, quizzes, review success, current streak (quote-only days don't break it), fragile words (missed 2+ times). */
    private fun showStats(seen: Int, favs: Int) {
        val p = Prefs(this)
        val rate = memory.reviews.takeIf { it > 0 }?.let { memory.reviewsRight * 100 / it }
        val streak = if (Quiz.unbroken(p, LocalDate.now().toEpochDay())) p.streak else 0
        val fragile = memory.entries().count { it.value.misses >= 2 }
        fun q(id: Int, n: Int) = n.toString() to resources.getQuantityString(id, n)
        val tiles = listOf(
            q(R.plurals.stat_seen, seen), q(R.plurals.stat_favourites, favs), q(R.plurals.stat_quizzes, p.quizzes),
            rate?.let { getString(R.string.stat_rate, it) to getString(R.string.stat_rate_label) } ?: ("–" to getString(R.string.stat_rate_none)),
            q(R.plurals.stat_streak, streak), q(R.plurals.stat_fragile, fragile),
        )
        stats.removeAllViews()
        stats.columnCount = (resources.configuration.screenWidthDp / (130 * resources.configuration.fontScale)).toInt().coerceIn(1, 3) // « découverts » holds on a line
        tiles.forEach { (value, label) ->
            stats.addView(LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = GradientDrawable().apply { cornerRadius = 12 * dp; setColor(getColor(R.color.surface)) }
                val pad = (12 * dp).toInt()
                setPadding(pad, pad, pad, pad)
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                contentDescription = getString(R.string.stat_tile, if (value == "–") "" else value, label).trim() // one stop, « – » not spelt out
                addView(TextView(context, null, 0, R.style.Title).apply { text = value; textSize = 24f; importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO })
                addView(TextView(context).apply { text = label; textSize = 14f; setTextColor(getColor(R.color.ink2)); importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO })
            }, GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL), GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)).apply {
                width = 0; val m = (4 * dp).toInt(); setMargins(m, m, m, m)
            })
        }
    }

    /** One row per word: the word, its definition on 2 lines at most, its star. */
    private inner class Rows : BaseAdapter() {
        override fun getCount() = rows.size
        override fun getItem(i: Int) = rows[i]
        override fun getItemId(i: Int) = i.toLong()
        override fun getView(i: Int, convert: View?, parent: ViewGroup): View {
            val row = convert as? LinearLayout ?: LinearLayout(this@MotsActivity).apply {
                gravity = Gravity.CENTER_VERTICAL; minimumHeight = (48 * dp).toInt(); setPadding(0, (8 * dp).toInt(), 0, (8 * dp).toInt())
                background = getDrawable(TypedValue().also { theme.resolveAttribute(android.R.attr.selectableItemBackground, it, true) }.resourceId)
                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(TextView(context, null, 0, R.style.Body).apply { textSize = 18f; setTypeface(typeface, android.graphics.Typeface.BOLD) })
                    addView(TextView(context, null, 0, R.style.Body).apply { textSize = 14f; setTextColor(getColor(R.color.ink2)); maxLines = 2; ellipsize = TextUtils.TruncateAt.END })
                }, LinearLayout.LayoutParams(0, -2, 1f))
                addView(CheckBox(context).apply { setButtonDrawable(R.drawable.favori); background = null })
            }
            val w = rows[i]
            val texts = row.getChildAt(0) as LinearLayout
            (texts.getChildAt(0) as TextView).text = w.mot
            (texts.getChildAt(1) as TextView).text = w.definition
            row.setOnClickListener { startActivity(Intent(this@MotsActivity, DetailActivity::class.java).putExtra(MainActivity.EXTRA_MOT, w.mot)) }
            star(row.getChildAt(1) as CheckBox, w.mot) { refresh() }
            return row
        }
    }

    companion object {
        /** Words of [history] (mot -> day last shown), newest first, whose word or definition holds [query] (case, accents and ligatures ignored);
         *  only those of [only] when given (the favourites). */
        fun list(all: List<Word>, history: Map<String, Long>, query: String = "", only: Set<String>? = null): List<Word> {
            val q = fold(query.trim())
            return all.filter { w -> w.mot in history && (only == null || w.mot in only) && (q.isEmpty() || q in fold(w.mot) || q in fold(w.definition)) }
                .sortedWith(compareByDescending<Word> { history[it.mot] }.thenBy { fold(it.mot) })
        }

        fun fold(s: String) = Normalizer.normalize(s.lowercase(Locale.FRENCH).replace("œ", "oe").replace("æ", "ae"), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")
    }
}
