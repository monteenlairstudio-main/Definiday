package fr.definiday

import android.animation.ValueAnimator
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Layout
import android.util.TypedValue
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
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
import java.util.Locale

class MainActivity : Activity() {
    companion object {
        const val EXTRA_MOT = "mot"; private const val STATE_ITEM = "item"; private const val STATE_REVEALED = "revealed"
        /** Opened from « Mes mots »: the card says « Déjà vu », not « Mot au hasard ». */
        const val EXTRA_DEJA_VU = "dejaVu"; private const val STATE_DEJA_VU = "dejaVu"

        /** Small still prop beside Glose by month: snowflake in winter, leaf in autumn, flower in spring, none in summer. */
        fun season(month: Int) = when (month) { 12, 1, 2 -> R.drawable.saison_flocon; in 9..11 -> R.drawable.saison_feuille; in 3..5 -> R.drawable.saison_fleur; else -> 0 }
    }

    private var current: Item? = null
    /** Key of the word whose definition was revealed (« Deviner avant de voir »): stays shown on the way back to the screen. */
    private var revealed: String? = null
    private var dejaVu = false // the card's word came from « Mes mots »

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // first launch: the onboarding asks for notifications in context (refused: banner, see onResume), then comes back here
        if (!Prefs(this).onboarded) { startActivity(Intent(this, OnboardingActivity::class.java)); finish(); return }
        // after a rotation: the item on the card; else the notification's item (today's, a random pick's or an earlier day's). Shown in onResume.
        current = find(savedInstanceState?.getString(STATE_ITEM) ?: intent.getStringExtra(EXTRA_MOT)) ?: Words.today(this).first()
        revealed = savedInstanceState?.getString(STATE_REVEALED)
        dejaVu = savedInstanceState?.getBoolean(STATE_DEJA_VU) ?: intent.getBooleanExtra(EXTRA_DEJA_VU, false)
        if (resources.configuration.fontScale > 1.5f) findViewById<GridLayout>(R.id.actions).columnCount = 2 // labels at full size, never cut
        // the notebook: a sheet ruled from the example's own baselines; Glose's yarn behind the papers, from him to the quiz's seal (x of the seal)
        findViewById<View>(R.id.exemple_box).background = LayerDrawable(arrayOf(getDrawable(R.drawable.card_soft)!!, Postal.Seyes(this, findViewById(R.id.exemple))))
        findViewById<LinearLayout>(R.id.page).let { it.background = Postal.Yarn(this, it, findViewById(R.id.glose), findViewById(R.id.quiz_open), 36 * Postal.dp(this)) }
        findViewById<Button>(R.id.new_word).setOnClickListener {
            val w = Words.random(this)
            pick(w)
            Sounds.play(this, Sounds.PAGE)
            DailyReceiver.show(this, listOf(w)) // the random pick replaces the word on the lock screen: said under the actions
            if (Prefs(this).mode != Prefs.MODE_NOTIF) findViewById<View>(R.id.random_hint).visibility = View.VISIBLE
            warnIfHurried()
        }
        findViewById<View>(R.id.share).setOnClickListener { current?.let(::share) }
        findViewById<View>(R.id.reveal).setOnClickListener { reveal() }
        findViewById<Button>(R.id.quiz_open).setOnClickListener { startActivity(Intent(this, QuizActivity::class.java)) }
        findViewById<View>(R.id.collection_open).setOnClickListener { startActivity(Intent(this, CollectionActivity::class.java)) }
        findViewById<View>(R.id.mots_open).setOnClickListener { startActivity(Intent(this, MotsActivity::class.java)) }
        findViewById<View>(R.id.settings_open).setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
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
        find(intent.getStringExtra(EXTRA_MOT))?.let { dejaVu = intent.getBooleanExtra(EXTRA_DEJA_VU, false); show(it) }
    }

    override fun onSaveInstanceState(out: Bundle) {
        super.onSaveInstanceState(out)
        current?.let { out.putString(STATE_ITEM, it.key) }
        out.putString(STATE_REVEALED, revealed)
        out.putBoolean(STATE_DEJA_VU, dejaVu)
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
        val today = java.time.LocalDate.now()
        findViewById<TextView>(R.id.date).text = today.format(java.time.format.DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH))
        val done = p.lastQuizDay == today.toEpochDay()
        findViewById<Button>(R.id.quiz_open).apply { // done: back to a plain Row, the fill and the seal are for what is still to do (audit 011 n° 19)
            setText(if (done) R.string.quiz_done else R.string.quiz_open)
            background = getDrawable(if (done) R.drawable.btn else R.drawable.btn_primary)
            if (done) setTextColor(getColorStateList(R.color.btn_text)) else setTextColor(getColor(R.color.bg))
        }
        findViewById<View>(R.id.seal).visibility = if (done) View.GONE else View.VISIBLE
        val got = (p.unlocked + Unlocks.of(emptyMap())).count(Unlocks.byId::containsKey) // the default look counts from the start
        findViewById<Button>(R.id.collection_open).apply {
            text = getString(R.string.collection_count, got, Unlocks.items.size)
            contentDescription = getString(R.string.collection_desc, got, Unlocks.items.size)
            tooltipText = contentDescription
        }
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
        val pal = st.palette
        findViewById<View>(R.id.card).background = Postal.postcard(this, pal)
        findViewById<TextView>(R.id.mot).apply { typeface = st.typeface; setTextColor(pal.mot) }
        findViewById<android.widget.CheckBox>(R.id.favori).buttonTintList = ColorStateList.valueOf(pal.mot) // readable on the card like the word
        listOf(R.id.label to pal.nature, R.id.nature to pal.nature, R.id.definition to pal.texte)
            .forEach { (id, c) -> findViewById<TextView>(id).setTextColor(c) }
        // stamp in the word's colours (its initial in its suggested font), today's postmark in the nature colour, a rule over the actions
        val initial = (if (item is Word) item.mot else (item as Quote).auteur).take(1).uppercase(Locale.FRENCH)
        findViewById<ImageView>(R.id.stamp).setImageDrawable(Postal.Stamp(this, pal.mot, pal.haut, initial, Styles.initialFace(this, item, st)))
        findViewById<ImageView>(R.id.postmark).setImageDrawable(Postal.Postmark(this, pal.nature))
        findViewById<View>(R.id.card_rule).setBackgroundColor(pal.nature and 0xFFFFFF or 0x59000000) // 35 %, decoration
        listOf(R.id.academie, R.id.cnrtl, R.id.share, R.id.new_word).forEach { id -> findViewById<Button>(id).apply {
            setTextColor(pal.texte); background = Postal.ring(context, pal.texte)
        } }
        findViewById<Button>(R.id.reveal).apply { // a veil in the card's own ink, dashed edge; focused: solid 3 dp edge
            setTextColor(st.palette.texte)
            val dp = resources.displayMetrics.density
            fun veil(edge: Float, dash: Float) = GradientDrawable().apply {
                cornerRadius = resources.getDimension(R.dimen.radius_paper); setColor(st.palette.texte and 0xFFFFFF or 0x1A000000); setStroke((edge * dp).toInt(), st.palette.texte, dash * dp, 4 * dp)
            }
            background = RippleDrawable(ColorStateList.valueOf(st.palette.texte and 0xFFFFFF or 0x33000000),
                StateListDrawable().apply { addState(intArrayOf(android.R.attr.state_focused), veil(3f, 0f)); addState(intArrayOf(), veil(1f, 6f)) }, null)
        }
        val daily = item.key in Prefs(this).dailyMots
        val academie = findViewById<Button>(R.id.academie)
        val cnrtl = findViewById<Button>(R.id.cnrtl)
        findViewById<View>(R.id.random_hint).visibility = View.GONE // shown again by the random pick itself
        when (item) {
            is Quote -> {
                text(R.id.label, getString(R.string.quote_label))
                text(R.id.mot, "«\u00a0${item.texte}\u00a0»")
                text(R.id.nature, item.signature)
                text(R.id.definition, "")
                text(R.id.exemple, "")
                text(R.id.anecdote, "")
                academie.visibility = View.GONE
                cnrtl.setText(R.string.action_source)
                cnrtl.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_source, 0, 0) // a quill, not the TLFi's book (audit 011 n° 20)
                cnrtl.contentDescription = getString(R.string.action_source_desc)
                cnrtl.setOnClickListener { open(item.source) }
            }
            is Word -> {
                text(R.id.label, getString(if (daily) R.string.today else if (dejaVu) R.string.deja_vu else R.string.random))
                text(R.id.mot, item.mot)
                text(R.id.nature, item.nature)
                text(R.id.definition, item.definition)
                text(R.id.exemple, if (item.exemple.isEmpty()) "" else "«\u00a0${item.exemple}\u00a0»")
                text(R.id.anecdote, item.anecdote)
                findViewById<Button>(R.id.source).setOnClickListener { open(item.source) }
                academie.visibility = View.VISIBLE
                academie.setOnClickListener { openAcademie(item.mot) }
                cnrtl.setText(R.string.action_tlfi)
                cnrtl.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_tlfi, 0, 0)
                cnrtl.contentDescription = getString(R.string.action_tlfi_desc)
                cnrtl.setOnClickListener { open(cnrtl(item.mot)) }
            }
        }
        listOf(R.id.academie, R.id.cnrtl, R.id.share, R.id.new_word).forEach { findViewById<Button>(it).compoundDrawableTintList = ColorStateList.valueOf(pal.texte) }
        hide(Prefs(this).guessFirst && item is Word && item.key != revealed)
        findViewById<android.widget.CheckBox>(R.id.favori).apply { // words only; set before listening: showing is not a tap
            visibility = if (item is Word) View.VISIBLE else View.GONE
            setOnCheckedChangeListener(null); isChecked = item.key in Memory(context).favourites
            setOnCheckedChangeListener { _, on -> Memory(context).favourite(item.key, on) }
        }
        fit(findViewById(R.id.mot), if (item is Quote) 24f else 40f)
        showSeen()
        if (top) findViewById<ScrollView>(R.id.scroll)?.smoothScrollTo(0, 0)
    }

    /** « Deviner avant de voir »: the definition keeps its room, invisible under « Touchez pour révéler »; example and anecdote wait too. */
    private fun hide(hidden: Boolean) {
        val def = findViewById<TextView>(R.id.definition)
        if (def.text.isNotEmpty()) def.visibility = if (hidden) View.INVISIBLE else View.VISIBLE
        findViewById<View>(R.id.reveal).visibility = if (hidden) View.VISIBLE else View.GONE
        listOf(R.id.exemple to R.id.exemple_box, R.id.anecdote to R.id.anecdote_box).forEach { (t, box) ->
            findViewById<View>(box).visibility = if (hidden || findViewById<TextView>(t).text.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun reveal() {
        revealed = current?.key
        hide(false)
        if (ValueAnimator.areAnimatorsEnabled()) findViewById<View>(R.id.definition).run { alpha = 0f; animate().alpha(1f).setDuration(300) }
        focusCard() // the button is gone: TalkBack reads the card, definition included
    }

    /** Text and picture of the card (its palette and font), signed with Glose's head. */
    private fun share(item: Item) {
        val head = Glose.bitmap(Glose.mine(this, R.drawable.glose_icone_vd, accessory = false), 128, 128)
        // ponytail: drawn and compressed on the UI thread (~0.2 s on a tap), move to a thread if it stutters
        val picture = runCatching { WordCard.share(this, item, Styles.of(this, item), head) }.getOrNull()
        runCatching { startActivity(ShareProvider.intent(this, item, picture)) }
    }

    private fun focusCard() = findViewById<View>(R.id.card).run { post { performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null) } }

    /** Chosen by the user (random pick, chip): shown, and TalkBack moves to the card, which reads the new item. */
    private fun pick(item: Item) {
        dejaVu = false // a random pick or one of today's chips
        show(item)
        focusCard()
    }

    /** Largest size up to [max] sp at which each piece (between spaces, or after a hyphen) holds on one line,
     *  but never below 1.3 x the definition: there the word breaks between syllables instead (never anywhere). */
    private fun fit(v: TextView, max: Float) {
        val card = findViewById<View>(R.id.card)
        val width = (resources.configuration.screenWidthDp - 2 * 16) * resources.displayMetrics.density - card.paddingStart - card.paddingEnd // page and card padding
        val pieces = v.text.split(Regex("[ \u00a0]|(?<=-)"))
        var sp = max
        v.textSize = sp
        val floor = minOf(v.textSize, 1.3f * findViewById<TextView>(R.id.definition).textSize) // px: both follow the font scale
        while (v.textSize > floor && pieces.any { v.paint.measureText(it) > width }) { sp -= 2; v.textSize = sp }
        if (v.textSize < floor) v.setTextSize(TypedValue.COMPLEX_UNIT_PX, floor)
        v.textLocale = Locale.FRENCH // French syllables
        v.hyphenationFrequency = if (pieces.any { v.paint.measureText(it) > width }) Layout.HYPHENATION_FREQUENCY_FULL else Layout.HYPHENATION_FREQUENCY_NONE
    }

    /** One envelope per item of the day (word or quote, shown or not yet, e.g. 2nd-3rd in lock-only mode) and per random pick seen today;
     *  none when the only one is the card (audit 011 n° 18). Three to a row when the words are short; a word never cut (n° 2). */
    private fun showSeen() {
        val box = findViewById<LinearLayout>(R.id.seen)
        box.removeAllViews()
        val items = (Words.today(this) + Words.seenToday(this)).distinctBy { it.key }
        val alone = items.size <= 1 && items.firstOrNull()?.key == current?.key
        listOf<View>(box, findViewById(R.id.seen_label)).forEach { it.visibility = if (alone) View.GONE else View.VISIBLE }
        if (alone) return
        val gap = (8 * resources.displayMetrics.density).toInt()
        val width = ((resources.configuration.screenWidthDp - 2 * 16) * resources.displayMetrics.density).toInt() // page padding
        var row: LinearLayout? = null
        var used = 0
        items.forEach { w ->
            val env = Button(this, null, 0, R.style.Envelope).apply {
                text = if (w is Word) w.mot else getString(R.string.quote_chip)
                minWidth = (width - 2 * gap) / 3; minimumWidth = minWidth
                setOnClickListener { pick(w) }
                isSelected = w.key == current?.key // the one in the card: outlined, and said by TalkBack
                measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            }
            val fits = env.measuredWidth <= width
            if (!fits) env.apply { isSingleLine = false; textLocale = Locale.FRENCH; hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_FULL } // alone on its row, broken between syllables
            if (row == null || used + gap + env.measuredWidth > width) { row = LinearLayout(this).also(box::addView); used = -gap } // the gap goes between envelopes only
            row!!.addView(env, LinearLayout.LayoutParams(if (fits) LinearLayout.LayoutParams.WRAP_CONTENT else LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT).apply { if (used >= 0) marginStart = gap; topMargin = gap })
            used += gap + env.measuredWidth
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
