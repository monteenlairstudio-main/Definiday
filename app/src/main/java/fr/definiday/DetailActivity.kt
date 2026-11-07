package fr.definiday

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Layout
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDate
import java.util.Locale

/** A word of « Mes mots » read again: its card (word, nature, definition, example), anecdote, Académie, TLFi, star and share.
 *  A review, not a new display: History is left alone (not « seen today », not in tonight's quiz). Back returns to the list. */
class DetailActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val w = Words.load(this).firstOrNull { it.mot == intent.getStringExtra(MainActivity.EXTRA_MOT) } ?: return finish()
        title = w.mot // TalkBack and the recent apps name the screen
        val st = Styles.of(this, w)
        val pal = st.palette
        val dp = resources.displayMetrics.density
        fun px(v: Int) = (v * dp).toInt()
        fun lp(top: Int = 0) = LinearLayout.LayoutParams(-1, -2).apply { topMargin = px(top) }
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(px(16), px(24), px(16), px(24)) }
        fun text(s: String, parent: LinearLayout, color: Int, style: Int = R.style.Body, top: Int = 0) = TextView(this, null, 0, style).apply {
            text = s; setTextColor(color); visibility = if (s.isEmpty()) View.GONE else View.VISIBLE; parent.addView(this, lp(top))
        }
        // the home card (activity_main.xml): rubric, stamp + postmark of the day it came, word, nature and star, definition, rule, actions
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; background = Postal.postcard(context, pal); setPadding(px(22), px(20), px(22), px(10))
            clipChildren = false; clipToPadding = false // the star sits 12 dp into the padding: its focus outline shows whole
        }.also(box::addView)
        val top = LinearLayout(this).also(card::addView)
        top.addView(TextView(this, null, 0, R.style.Label).apply { setText(R.string.deja_vu); setTextColor(pal.nature) }, LinearLayout.LayoutParams(0, -2, 1f).apply { gravity = Gravity.BOTTOM })
        val day = History(this).all()[w.mot]?.let(LocalDate::ofEpochDay) ?: LocalDate.now()
        top.addView(FrameLayout(this).apply { // decoration, as on the home card
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            addView(ImageView(context).apply { setImageDrawable(Postal.Stamp(context, pal.mot, pal.haut, Postal.initial(w), Styles.initialFace(context, w, st))) },
                FrameLayout.LayoutParams(px(56), px(64), Gravity.END))
            addView(ImageView(context).apply { setImageDrawable(Postal.Postmark(context, pal.nature, day)) }, FrameLayout.LayoutParams(px(100), px(56), Gravity.CENTER_VERTICAL))
        }, LinearLayout.LayoutParams(px(100), px(64)))
        val mot = TextView(this).apply { // a long word breaks between syllables, never anywhere
            text = w.mot; typeface = st.typeface; setTextColor(pal.mot); isAccessibilityHeading = true; breakStrategy = Layout.BREAK_STRATEGY_SIMPLE
        }.also(card::addView)
        val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }.also(card::addView)
        row.addView(TextView(this).apply { text = w.nature; textSize = 16f; setTextColor(pal.nature) }, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(CheckBox(this).apply { setButtonDrawable(R.drawable.favori); buttonTintList = ColorStateList.valueOf(pal.mot); star(this, w.mot) },
            LinearLayout.LayoutParams(-2, -2).apply { marginEnd = px(-12) })
        val def = text(w.definition, card, pal.texte, top = 4).apply { textSize = 17f; setLineSpacing(0f, 1.25f) }
        fitWord(mot, (resources.configuration.screenWidthDp - 2 * 16) * dp - card.paddingStart - card.paddingEnd, 40f, 1.3f * def.textSize)
        card.addView(View(this).apply { setBackgroundColor(pal.nature and 0xFFFFFF or 0x59000000); importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO },
            LinearLayout.LayoutParams(-1, maxOf(1, px(1))).apply { topMargin = px(18) })
        val actions = GridLayout(this).apply { columnCount = if (resources.configuration.fontScale > 1.5f) 2 else 3 } // labels at full size, never cut
        card.addView(actions, lp(6))
        fun action(label: Int, desc: Int, icon: Int, go: (View) -> Unit) = actions.addView(Button(this, null, 0, R.style.Action).apply {
            setText(label); contentDescription = getString(desc); setOnClickListener(go)
            setCompoundDrawablesRelativeWithIntrinsicBounds(0, icon, 0, 0); compoundDrawableTintList = ColorStateList.valueOf(pal.texte)
            setTextColor(pal.texte); background = Postal.ring(context, pal.texte)
        }, GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED, 1f)).apply { width = 0 })
        action(R.string.action_academie, R.string.action_academie_desc, R.drawable.ic_academie) { openAcademie(w.mot, it) }
        action(R.string.action_tlfi, R.string.action_tlfi_desc, R.drawable.ic_tlfi) { open(cnrtl(w.mot)) }
        action(R.string.action_share, R.string.action_share_desc, R.drawable.ic_partager) { share(w) }
        // the example in the Seyès notebook, the anecdote on a sheet: as on home
        if (w.exemple.isNotEmpty()) {
            val notebook = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(px(22), px(10), px(16), px(14)) }
            box.addView(notebook, lp(14))
            val head = LinearLayout(this).apply { gravity = Gravity.BOTTOM }.also(notebook::addView)
            head.addView(TextView(this, null, 0, R.style.Label).apply { setText(R.string.exemple); setTextColor(getColor(R.color.accent)); setPadding(0, 0, 0, px(4)) })
            head.addView(TextView(this, null, 0, R.style.Note).apply { setText(R.string.exemple_note); gravity = Gravity.END },
                LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = px(12) })
            val ex = text("«\u202F${w.exemple}\u202F»", notebook, getColor(R.color.accent), R.style.Example, top = 2)
            notebook.background = LayerDrawable(arrayOf(getDrawable(R.drawable.card_soft)!!, Postal.Seyes(this, ex)))
        }
        if (w.anecdote.isNotEmpty()) {
            val sheet = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundResource(R.drawable.card_soft); setPadding(px(22), px(16), px(22), px(6)) }
            box.addView(sheet, lp(12))
            text(getString(R.string.anecdote), sheet, getColor(R.color.ink2), R.style.Label)
            text(w.anecdote, sheet, getColor(R.color.ink), top = 6)
            if (w.source.isNotEmpty()) sheet.addView(Button(this, null, 0, R.style.Link).apply { text = getText(R.string.source); setOnClickListener { open(w.source) } })
        }
        setContentView(ScrollView(this).apply { fitsSystemWindows = true; addView(box) })
    }
}

/** Largest size up to [max] sp at which each piece of [v]'s word (between spaces, or after a hyphen) holds on one line of [width] px,
 *  but never below [floor] px: there the word breaks between syllables instead (never anywhere). The home card's rule (MainActivity.fit). */
fun fitWord(v: TextView, width: Float, max: Float, floor: Float) {
    val pieces = v.text.split(Regex("[ \u00a0]|(?<=-)"))
    var sp = max
    v.textSize = sp
    val min = minOf(v.textSize, floor) // px: both follow the font scale
    while (v.textSize > min && pieces.any { v.paint.measureText(it) > width }) { sp -= 2; v.textSize = sp }
    if (v.textSize < min) v.setTextSize(TypedValue.COMPLEX_UNIT_PX, min)
    v.textLocale = Locale.FRENCH // French syllables
    v.hyphenationFrequency = if (pieces.any { v.paint.measureText(it) > width }) Layout.HYPHENATION_FREQUENCY_FULL else Layout.HYPHENATION_FREQUENCY_NONE
}

// Card actions shared by the home card (MainActivity) and this screen.

/** A word's star, checked from Memory; TalkBack says what a tap does, with the word (« Ajouter estramaçon aux favoris »). */
fun star(cb: CheckBox, mot: String, then: () -> Unit = {}) {
    fun label() { cb.contentDescription = cb.context.getString(if (cb.isChecked) R.string.favori_remove else R.string.favori_add, mot) }
    // keyboard focus shows (WCAG 2.4.7): a 3 dp outline in the star's own tint on a card (Postal.ring), accent on a plain page
    cb.background = cb.buttonTintList?.let { Postal.ring(cb.context, it.defaultColor) } ?: cb.context.getDrawable(R.drawable.btn_round)
    cb.setOnCheckedChangeListener(null) // showing it, or a recycled row, is not a tap
    cb.isChecked = mot in Memory(cb.context).favourites
    label()
    cb.setOnCheckedChangeListener { _, on -> Memory(cb.context).favourite(mot, on); label(); then() }
}

/** Text and picture of the card (its palette and font), signed with Glose's head. */
fun Activity.share(item: Item) {
    val head = Glose.bitmap(Glose.mine(this, R.drawable.glose_icone_vd, accessory = false), 128, 128)
    // ponytail: drawn and compressed on the UI thread (~0.2 s on a tap), move to a thread if it stutters
    val picture = runCatching { WordCard.share(this, item, Styles.of(this, item), head) }.getOrNull()
    runCatching { startActivity(ShareProvider.intent(this, item, picture)) }
}

/** Opens https links only (data-provided URLs), in a browser. */
fun Activity.open(url: String) {
    val uri = Uri.parse(url)
    if (uri.scheme != "https") return
    try {
        startActivity(Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, R.string.no_browser, Toast.LENGTH_LONG).show()
    }
}

/** One lookup on the Académie's own search, triggered by the user's tap on [b], to reach the article page. Falls back to CNRTL. */
fun Activity.openAcademie(mot: String, b: View) {
    b.isEnabled = false // one lookup at a time
    Thread {
        val started = System.currentTimeMillis()
        val url = runCatching {
            val c = URL("https://www.dictionnaire-academie.fr/search").openConnection() as HttpURLConnection
            c.requestMethod = "POST"; c.doOutput = true; c.connectTimeout = 5000; c.readTimeout = 5000
            c.setRequestProperty("X-Requested-With", "XMLHttpRequest")
            c.setRequestProperty("Accept", "application/json") // without it the server answers with a redirect
            c.outputStream.use { it.write("term=${URLEncoder.encode(mot, "UTF-8")}&options=1".toByteArray()) }
            JSONObject(c.inputStream.bufferedReader().use { r -> // at most 64 KiB: a truncated answer fails to parse and falls back to the CNRTL
                buildString { generateSequence { r.read().takeIf { it >= 0 } }.take(64 * 1024).forEach { append(it.toChar()) } } }).getJSONArray("result").getJSONObject(0).getString("url")
                .also { require(it.startsWith("https://www.dictionnaire-academie.fr/")) }
        }.getOrDefault(cnrtl(mot))
        // DNS isn't bounded by the socket timeouts: give up if the lookup took too long or the screen is gone
        runOnUiThread { b.isEnabled = true; if (!isDestroyed && System.currentTimeMillis() - started < 12_000) open(url) }
    }.start()
}

fun cnrtl(mot: String) = "https://www.cnrtl.fr/definition/" + Uri.encode(mot)
