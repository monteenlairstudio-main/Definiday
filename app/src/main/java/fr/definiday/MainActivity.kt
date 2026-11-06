package fr.definiday

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Layout
import android.util.TypedValue
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

class MainActivity : Activity() {
    companion object {
        const val EXTRA_MOT = "mot"; private const val STATE_ITEM = "item"

        /** Small still prop beside Glose by month: snowflake in winter, leaf in autumn, flower in spring, none in summer. */
        fun season(month: Int) = when (month) { 12, 1, 2 -> R.drawable.saison_flocon; in 9..11 -> R.drawable.saison_feuille; in 3..5 -> R.drawable.saison_fleur; else -> 0 }
    }

    private var current: Item? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // asked once (not at each rotation); refused: banner, see onResume
        if (Build.VERSION.SDK_INT >= 33 && savedInstanceState == null && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        // after a rotation: the item on the card; else the notification's item (today's, a random pick's or an earlier day's). Shown in onResume.
        current = find(savedInstanceState?.getString(STATE_ITEM) ?: intent.getStringExtra(EXTRA_MOT)) ?: Words.today(this).first()
        findViewById<Button>(R.id.new_word).setOnClickListener {
            val w = Words.random(this)
            pick(w)
            Sounds.play(this, Sounds.PAGE)
            DailyReceiver.show(this, listOf(w)) // the random pick replaces the word on the lock screen (said under the button)
            warnIfHurried()
        }
        findViewById<Button>(R.id.quiz_open).setOnClickListener { startActivity(Intent(this, QuizActivity::class.java)) }
        findViewById<Button>(R.id.collection_open).setOnClickListener { startActivity(Intent(this, CollectionActivity::class.java)) }
        findViewById<Button>(R.id.mots_open).setOnClickListener { startActivity(Intent(this, MotsActivity::class.java)) }
        findViewById<Button>(R.id.settings_open).setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        findViewById<View>(R.id.notif_banner).setOnClickListener {
            runCatching { startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName)) }
        }
        DailyReceiver.scheduleNext(this)
        DailyReceiver.scheduleQuiz(this)
        val taps = Glose.Taps()
        findViewById<ImageView>(R.id.glose).setOnClickListener { Glose.tap(it as ImageView, taps, Glose.mainPose(Prefs(this))) } // reacts, then its idle life
    }

    /** singleTop: a notification tapped while the app is open shows its item here instead of stacking a new screen. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        find(intent.getStringExtra(EXTRA_MOT))?.let(::show)
    }

    override fun onSaveInstanceState(out: Bundle) {
        super.onSaveInstanceState(out)
        current?.let { out.putString(STATE_ITEM, it.key) }
    }

    /** Any word or quote by key (history keys: mot, or "q:" + text). */
    private fun find(key: String?): Item? = key?.let { k -> (Words.load(this) + Quotes.load(this)).firstOrNull { it.key == k } }

    /** Also on the way back from Settings (palettes, fonts, words a day, mode), the quiz (done ✓) and the system notification page. */
    override fun onResume() {
        super.onResume()
        val p = Prefs(this)
        current?.let { show(it, top = false) }
        findViewById<ImageView>(R.id.glose).apply { setImageDrawable(Glose.mine(context, Glose.mainPose(p))); Glose.play(this) }
        findViewById<ImageView>(R.id.season).apply { val r = season(java.time.LocalDate.now().monthValue); setImageResource(r); visibility = if (r == 0) View.GONE else View.VISIBLE }
        val done = p.lastQuizDay == java.time.LocalDate.now().toEpochDay()
        findViewById<Button>(R.id.quiz_open).setText(if (done) R.string.quiz_done else R.string.quiz_open)
        findViewById<View>(R.id.notif_banner).visibility = if (p.enabled && p.mode != Prefs.MODE_LOCK && notificationsOff()) View.VISIBLE else View.GONE
    }

    /** Refused at the Android 13+ prompt, turned off for the app, or the word's channel turned off. */
    private fun notificationsOff(): Boolean {
        val nm = getSystemService(NotificationManager::class.java)
        return !nm.areNotificationsEnabled() || nm.getNotificationChannel(DailyReceiver.CHANNEL)?.importance == NotificationManager.IMPORTANCE_NONE
    }

    override fun onPause() {
        super.onPause()
        Glose.stop(findViewById(R.id.glose))
    }

    /** Past 3 random picks in a day, once: invite to slow down. */
    private fun warnIfHurried() {
        val p = Prefs(this)
        val day = java.time.LocalDate.now().toEpochDay()
        if (p.warnedDay == day || Words.seenToday(this).count { it.mot !in p.dailyMots } <= 3) return
        p.warnedDay = day
        AlertDialog.Builder(this)
            .setTitle(R.string.slow_title)
            .setMessage(Quotes.slow(this) ?: getString(R.string.slow_default))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    /** Displays one word or quote in the card, in its palette and font; links follow it. [top]: scroll back to the card. */
    private fun show(item: Item, top: Boolean = true) {
        current = item
        Words.shown(this, listOf(item)) // the card shows it: seen today (history, quiz)
        val st = Styles.of(this, item)
        findViewById<View>(R.id.card).background = Styles.card(this, st.palette, 20f)
        findViewById<TextView>(R.id.mot).apply { typeface = st.typeface; setTextColor(st.palette.mot) }
        listOf(R.id.label to st.palette.nature, R.id.nature to st.palette.nature, R.id.definition to st.palette.texte, R.id.exemple to st.palette.exemple)
            .forEach { (id, c) -> findViewById<TextView>(id).setTextColor(c) }
        val daily = item.key in Prefs(this).dailyMots
        val academie = findViewById<Button>(R.id.academie)
        val cnrtl = findViewById<Button>(R.id.cnrtl)
        when (item) {
            is Quote -> {
                text(R.id.label, getString(R.string.quote_label))
                text(R.id.mot, "«\u00a0${item.texte}\u00a0»")
                text(R.id.nature, item.signature)
                text(R.id.definition, "")
                text(R.id.exemple, "")
                findViewById<View>(R.id.anecdote_box).visibility = View.GONE
                academie.visibility = View.GONE
                cnrtl.text = getText(R.string.wikiquote)
                cnrtl.setOnClickListener { open(item.source) }
            }
            is Word -> {
                text(R.id.label, getString(if (daily) R.string.today else R.string.random))
                text(R.id.mot, item.mot)
                text(R.id.nature, item.nature)
                text(R.id.definition, item.definition)
                text(R.id.exemple, if (item.exemple.isEmpty()) "" else "«\u00a0${item.exemple}\u00a0»")
                text(R.id.anecdote, item.anecdote)
                findViewById<View>(R.id.anecdote_box).visibility = if (item.anecdote.isEmpty()) View.GONE else View.VISIBLE
                findViewById<Button>(R.id.source).setOnClickListener { open(item.source) }
                academie.visibility = View.VISIBLE
                academie.setOnClickListener { openAcademie(item.mot) }
                cnrtl.text = getText(R.string.cnrtl)
                cnrtl.setOnClickListener { open(cnrtl(item.mot)) }
            }
        }
        findViewById<android.widget.CheckBox>(R.id.favori).apply { // words only; set before listening: showing is not a tap
            visibility = if (item is Word) View.VISIBLE else View.GONE
            setOnCheckedChangeListener(null); isChecked = item.key in Memory(context).favourites
            setOnCheckedChangeListener { _, on -> Memory(context).favourite(item.key, on) }
        }
        fit(findViewById(R.id.mot), if (item is Quote) 24f else 40f)
        showSeen()
        if (top) findViewById<ScrollView>(R.id.scroll)?.smoothScrollTo(0, 0)
    }

    /** Chosen by the user (random pick, chip): shown, and TalkBack moves to the card, which reads the new item. */
    private fun pick(item: Item) {
        show(item)
        findViewById<View>(R.id.card).run { post { performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null) } }
    }

    /** Largest size up to [max] sp at which each piece (between spaces, or after a hyphen) holds on one line,
     *  but never below 1.3 x the definition: there the word breaks between syllables instead (never anywhere). */
    private fun fit(v: TextView, max: Float) {
        val width = (resources.configuration.screenWidthDp - 2 * 16 - 2 * 20) * resources.displayMetrics.density // page and card padding (activity_main.xml)
        val pieces = v.text.split(Regex("[ \u00a0]|(?<=-)"))
        var sp = max
        v.textSize = sp
        val floor = minOf(v.textSize, 1.3f * findViewById<TextView>(R.id.definition).textSize) // px: both follow the font scale
        while (v.textSize > floor && pieces.any { v.paint.measureText(it) > width }) { sp -= 2; v.textSize = sp }
        if (v.textSize < floor) v.setTextSize(TypedValue.COMPLEX_UNIT_PX, floor)
        v.textLocale = Locale.FRENCH // French syllables
        v.hyphenationFrequency = if (pieces.any { v.paint.measureText(it) > width }) Layout.HYPHENATION_FREQUENCY_FULL else Layout.HYPHENATION_FREQUENCY_NONE
    }

    /** One chip per item of the day (word or quote, shown or not yet, e.g. 2nd-3rd in lock-only mode) and per random pick seen today. */
    private fun showSeen() {
        val box = findViewById<LinearLayout>(R.id.seen)
        box.removeAllViews()
        val gap = (8 * resources.displayMetrics.density).toInt()
        val width = (resources.configuration.screenWidthDp - 2 * 16) * resources.displayMetrics.density // page padding
        var row: LinearLayout? = null
        var used = 0
        (Words.today(this) + Words.seenToday(this)).distinctBy { it.key }.forEach { w ->
            val chip = Button(this, null, 0, R.style.Chip).apply {
                text = if (w is Word) w.mot else getString(R.string.quote_chip)
                setOnClickListener { pick(w) }
                measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            }
            if (row == null || used + chip.measuredWidth > width) { row = LinearLayout(this).also(box::addView); used = 0 }
            row!!.addView(chip, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                .apply { marginEnd = gap; topMargin = gap })
            used += chip.measuredWidth + gap
        }
    }

    private fun text(id: Int, s: String) = findViewById<TextView>(id).run { text = s; visibility = if (s.isEmpty()) View.GONE else View.VISIBLE }

    /** Opens https links only (data-provided URLs), in a browser. */
    private fun open(url: String) {
        val uri = Uri.parse(url)
        if (uri.scheme != "https") return
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.no_browser, Toast.LENGTH_LONG).show()
        }
    }

    /** One lookup on the Académie's own search, triggered by the user's tap, to reach the article page. Falls back to CNRTL. */
    private fun openAcademie(mot: String) {
        val b = findViewById<Button>(R.id.academie).apply { isEnabled = false } // one lookup at a time
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

    private fun cnrtl(mot: String) = "https://www.cnrtl.fr/definition/" + Uri.encode(mot)
}
