package fr.definiday

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import java.text.Normalizer
import java.time.LocalDate
import java.util.Locale

/** « Mes mots »: light stats, then every word seen (newest first) with a search, « Favoris » and « À revoir » filters and a star each.
 *  A tap opens the word in DetailActivity (a review: not shown again as today's). Search and filters stay fixed above the scrolling list
 *  and survive a rotation (N11). */
class MotsActivity : Activity() {
    private lateinit var memory: Memory
    private lateinit var search: EditText
    private lateinit var favOnly: Switch
    private lateinit var dueOnly: Switch
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
        val pad = (16 * dp).toInt()
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; fitsSystemWindows = true }
        val head = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(pad, (24 * dp).toInt(), pad, 0) }.also(root::addView)
        head.addView(TextView(this, null, 0, R.style.Title).apply { text = getString(R.string.mots_title) })
        search = EditText(this).apply { // a sheet of paper, not the platform underline (audit 011, « Mes mots »)
            hint = getString(R.string.mots_search); textSize = 16f; minHeight = (48 * dp).toInt()
            background = getDrawable(R.drawable.field); setPadding(pad, pad / 2, pad, pad / 2)
            setTextColor(getColor(R.color.ink)); setHintTextColor(getColor(R.color.ink2))
            inputType = InputType.TYPE_CLASS_TEXT; imeOptions = EditorInfo.IME_ACTION_SEARCH // one line to type (Enter searches)...
            setHorizontallyScrolling(false); maxLines = 3 // ...but the hint wraps at large font sizes instead of being cut
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
                override fun afterTextChanged(s: Editable?) = refresh()
            })
        }
        head.addView(search, LinearLayout.LayoutParams(-1, -2).apply { topMargin = (16 * dp).toInt() })
        fun toggle(label: Int) = Switch(this).apply { // R.style.Toggle by hand: as a defStyleRes it would drop the platform switch style
            text = getString(label); textSize = 16f; setTextColor(getColor(R.color.ink)); minHeight = (48 * dp).toInt()
            thumbTintList = getColorStateList(R.color.switch_thumb); trackTintList = getColorStateList(R.color.switch_track); trackTintMode = android.graphics.PorterDuff.Mode.SRC_OVER
            head.addView(this, LinearLayout.LayoutParams(-1, -2))
        }
        favOnly = toggle(R.string.mots_favourites)
        dueOnly = toggle(R.string.mots_due)
        // the ledger: an index card (card_soft) of ruled lines, not dashboard tiles (audit 011 n° 11)
        stats = GridLayout(this).apply { background = getDrawable(R.drawable.card_soft); setPadding(pad, (4 * dp).toInt(), pad, (4 * dp).toInt()) }
        val ledger = FrameLayout(this).apply { setPadding(0, (8 * dp).toInt(), 0, (8 * dp).toInt()); addView(stats) } // a list header takes no margins
        empty = TextView(this, null, 0, R.style.Body).apply { setTextColor(getColor(R.color.ink2)); setPadding(0, (16 * dp).toInt(), 0, 0) }
        val list = ListView(this).apply {
            divider = null; setPadding(pad, 0, pad, pad); clipToPadding = false
            itemsCanFocus = true // the keyboard reaches each row's star, not only the row
            addHeaderView(ledger, null, false); addFooterView(empty, null, false)
            adapter = this@MotsActivity.adapter
        }
        root.addView(list, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        savedInstanceState?.run { search.setText(getString(STATE_QUERY)); favOnly.isChecked = getBoolean(STATE_FAV); dueOnly.isChecked = getBoolean(STATE_DUE) }
        favOnly.setOnCheckedChangeListener { _, _ -> refresh() }
        dueOnly.setOnCheckedChangeListener { _, _ -> refresh() }
    }

    /** Views without ids: their state is kept by hand. */
    override fun onSaveInstanceState(out: Bundle) {
        super.onSaveInstanceState(out)
        out.putString(STATE_QUERY, search.text.toString()); out.putBoolean(STATE_FAV, favOnly.isChecked); out.putBoolean(STATE_DUE, dueOnly.isChecked)
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
        // due on the review ladder today, however many (the quiz asks Quiz.MAX_REVIEWS an evening); words only, not quotes
        val due = list(all, history, only = Quiz.due(memory.entries(), history, LocalDate.now().toEpochDay(), emptySet(), Int.MAX_VALUE).toSet()).map { it.mot }.toSet()
        rows = list(all, history, search.text.toString(), listOfNotNull(favs.takeIf { favOnly.isChecked }, due.takeIf { dueOnly.isChecked }).reduceOrNull(Set<String>::intersect))
        adapter.notifyDataSetChanged()
        empty.text = getString(when {
            seen.isEmpty() -> R.string.mots_none
            favOnly.isChecked && favs.isEmpty() -> R.string.mots_none_favourites
            dueOnly.isChecked && due.isEmpty() -> R.string.mots_none_due
            else -> R.string.mots_none_found
        })
        empty.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE
        showStats(seen.size, favs.count { it in history }, due.size)
    }

    /** Six ledger lines: words seen, favourites, quizzes, review success, current streak (quote-only days don't break it),
     *  words to review (the « À revoir » filter's). */
    private fun showStats(seen: Int, favs: Int, due: Int) {
        val p = Prefs(this)
        val rate = memory.reviews.takeIf { it > 0 }?.let { memory.reviewsRight * 100 / it }
        val streak = if (Quiz.unbroken(p, LocalDate.now().toEpochDay())) p.streak else 0
        fun q(id: Int, n: Int) = n.toString() to resources.getQuantityString(id, n)
        val tiles = listOf(
            q(R.plurals.stat_seen, seen), q(R.plurals.stat_favourites, favs), q(R.plurals.stat_quizzes, p.quizzes),
            rate?.let { getString(R.string.stat_rate, it) to getString(R.string.stat_rate_label) } ?: ("–" to getString(R.string.stat_rate_none)),
            q(R.plurals.stat_streak, streak), q(R.plurals.stat_due, due),
        )
        stats.removeAllViews()
        val cols = (resources.configuration.screenWidthDp / (180 * resources.configuration.fontScale)).toInt().coerceIn(1, 2)
        stats.columnCount = cols
        tiles.forEachIndexed { i, (value, label) ->
            // one ruled line of the ledger: the figure in bold ink, its words in ink2; no rule under the last line
            stats.addView(TextView(this, null, 0, R.style.Body).apply {
                text = SpannableStringBuilder(value).apply { setSpan(StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }.append(' ').append(label,
                    ForegroundColorSpan(getColor(R.color.ink2)), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                textSize = 15f
                if (i / cols < (tiles.size - 1) / cols) background = line() // before the padding: a background resets it
                setPadding(0, (10 * dp).toInt(), (8 * dp).toInt(), (10 * dp).toInt())
                contentDescription = getString(R.string.stat_tile, if (value == "–") "" else value, label).trim() // « – » not spelt out
            }, GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL), GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)).apply { width = 0 })
        }
    }

    /** A ledger rule: 1 dp card_edge along the bottom only (the stroke's other sides pushed out of the bounds). */
    private fun line(): Drawable = InsetDrawable(GradientDrawable().apply { setStroke(dp.toInt().coerceAtLeast(1), getColor(R.color.card_edge)) },
        -(2 * dp).toInt(), -(2 * dp).toInt(), -(2 * dp).toInt(), 0)

    /** One row per word: the word, its definition on 2 lines at most, its star. */
    private inner class Rows : BaseAdapter() {
        override fun getCount() = rows.size
        override fun getItem(i: Int) = rows[i]
        override fun getItemId(i: Int) = i.toLong()
        override fun getView(i: Int, convert: View?, parent: ViewGroup): View {
            val row = convert as? LinearLayout ?: LinearLayout(this@MotsActivity).apply {
                background = RippleDrawable(ColorStateList.valueOf(getColor(R.color.ripple)), line(), ColorDrawable(Color.WHITE)) // ruled like the ledger
                gravity = Gravity.CENTER_VERTICAL; minimumHeight = (48 * dp).toInt(); setPadding(0, (12 * dp).toInt(), 0, (12 * dp).toInt())
                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(TextView(context, null, 0, R.style.Body).apply { textSize = 18f; setTypeface(typeface, android.graphics.Typeface.BOLD) })
                    addView(TextView(context, null, 0, R.style.Body).apply { textSize = 14f; setTextColor(getColor(R.color.ink2)); maxLines = 2; ellipsize = TextUtils.TruncateAt.END })
                }, LinearLayout.LayoutParams(0, -2, 1f))
                addView(CheckBox(context).apply { setButtonDrawable(R.drawable.favori) }) // its focus ring: star()
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
        private const val STATE_QUERY = "query"
        private const val STATE_FAV = "favourites"
        private const val STATE_DUE = "due"

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
