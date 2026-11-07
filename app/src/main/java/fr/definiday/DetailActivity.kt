package fr.definiday

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Layout
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

/** A word of « Mes mots » read again: its card (word, nature, definition, example), anecdote, Académie, TLFi, star and share.
 *  A review, not a new display: History is left alone (not « seen today », not in tonight's quiz). Back returns to the list. */
class DetailActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val w = Words.load(this).firstOrNull { it.mot == intent.getStringExtra(MainActivity.EXTRA_MOT) } ?: return finish()
        title = w.mot // TalkBack and the recent apps name the screen
        val st = Styles.of(this, w)
        val dp = resources.displayMetrics.density
        fun px(v: Int) = (v * dp).toInt()
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(px(16), px(24), px(16), px(24)) }
        fun text(s: String, parent: LinearLayout, color: Int, style: Int = R.style.Body) = TextView(this, null, 0, style).apply {
            text = s; setTextColor(color); visibility = if (s.isEmpty()) View.GONE else View.VISIBLE; parent.addView(this)
        }
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; background = Styles.card(context, st.palette, 20f); setPadding(px(20), px(8), px(8), px(20))
        }.also(box::addView)
        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }.also(card::addView)
        top.addView(TextView(this, null, 0, R.style.Label).apply { setText(R.string.deja_vu); setTextColor(st.palette.nature) }, LinearLayout.LayoutParams(0, -2, 1f))
        top.addView(CheckBox(this).apply { setButtonDrawable(R.drawable.favori); background = null; buttonTintList = ColorStateList.valueOf(st.palette.mot); star(this, w.mot) })
        text(w.mot, card, st.palette.mot, R.style.Title).apply { // a long word breaks between syllables, never anywhere
            typeface = st.typeface; textLocale = Locale.FRENCH; hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_FULL
        }
        text(w.nature, card, st.palette.nature).apply { setTypeface(typeface, Typeface.ITALIC) }
        text(w.definition, card, st.palette.texte).setPadding(0, px(8), px(12), 0)
        text(if (w.exemple.isEmpty()) "" else "«\u00a0${w.exemple}\u00a0»", card, st.palette.texte).setPadding(0, px(8), px(12), 0)
        if (w.anecdote.isNotEmpty()) {
            text(getString(R.string.anecdote), box, getColor(R.color.accent), R.style.Label).setPadding(0, px(20), 0, px(4))
            text(w.anecdote, box, getColor(R.color.ink))
            if (w.source.isNotEmpty()) box.addView(Button(this, null, 0, R.style.Link).apply { text = getText(R.string.source); setOnClickListener { open(w.source) } })
        }
        fun button(label: Int, desc: Int, go: (View) -> Unit) = box.addView(Button(this, null, 0, R.style.Row).apply {
            setText(label); contentDescription = getString(desc); setOnClickListener(go)
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = px(8) })
        button(R.string.action_academie, R.string.action_academie_desc) { openAcademie(w.mot, it) }
        button(R.string.action_tlfi, R.string.action_tlfi_desc) { open(cnrtl(w.mot)) }
        button(R.string.action_share, R.string.action_share_desc) { share(w) }
        setContentView(ScrollView(this).apply { fitsSystemWindows = true; addView(box) })
    }
}

// Card actions shared by the home card (MainActivity) and this screen.

/** A word's star, checked from Memory; TalkBack says what a tap does, with the word (« Ajouter estramaçon aux favoris »). */
fun star(cb: CheckBox, mot: String, then: () -> Unit = {}) {
    fun label() { cb.contentDescription = cb.context.getString(if (cb.isChecked) R.string.favori_remove else R.string.favori_add, mot) }
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
