package fr.definiday

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MainActivity : Activity() {
    companion object { const val EXTRA_MOT = "mot" }

    private val durations = listOf(15, 30, 60, 120, 240, 480, 720, 1440)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)

        val p = Prefs(this)
        val daily = Words.today(this)
        show(daily.firstOrNull { it.key == intent.getStringExtra(EXTRA_MOT) } ?: daily.first())
        findViewById<Button>(R.id.new_word).setOnClickListener {
            val w = Words.random(this)
            show(w)
            DailyReceiver.show(this, listOf(w)) // the random pick replaces the word on the lock screen
            warnIfHurried()
        }
        findViewById<Button>(R.id.quiz_open).setOnClickListener { startActivity(Intent(this, QuizActivity::class.java)) }
        findViewById<Button>(R.id.collection_open).setOnClickListener { startActivity(Intent(this, CollectionActivity::class.java)) }

        findViewById<Switch>(R.id.enabled).apply {
            isChecked = p.enabled
            setOnCheckedChangeListener { _, on -> p.enabled = on; DailyReceiver.scheduleNext(context) }
        }
        val time = findViewById<Button>(R.id.time)
        fun showTime() { time.text = getString(R.string.time, p.hour, p.minute) }
        showTime()
        time.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                p.hour = h; p.minute = m; showTime(); DailyReceiver.scheduleNext(this)
            }, p.hour, p.minute, true).show()
        }
        spinner(R.id.duration, durations.map(::label), durations.indexOf(p.durationMinutes)) { p.durationMinutes = durations[it] }
        spinner(R.id.per_day, listOf("1 mot", "2 mots", "3 mots"), p.wordsPerDay - 1) {
            if (p.wordsPerDay != it + 1) { p.wordsPerDay = it + 1; Words.today(this); showSeen() }
        }
        val unlocks = listOf(R.id.unlocks_label, R.id.unlocks, R.id.text_pos_label, R.id.text_pos).map { findViewById<View>(it) }
        fun showUnlocks() = unlocks.forEach { it.visibility = if (p.mode == Prefs.MODE_NOTIF) View.GONE else View.VISIBLE }
        // order matches Prefs.MODE_BOTH, MODE_NOTIF, MODE_LOCK
        spinner(R.id.mode, listOf("Notification et écran de verrouillage", "Notification seulement", "Écran de verrouillage seulement"), p.mode) {
            p.mode = it; showUnlocks()
        }
        showUnlocks()
        spinner(R.id.unlocks, listOf("Après la durée choisie", "Au 1er déverrouillage", "Au 2e déverrouillage", "Au 3e déverrouillage"), p.unlocks) {
            p.unlocks = it
        }
        spinner(R.id.text_pos, listOf("En bas (laisse la place à l’horloge)", "Au milieu"), if (p.textBottom) 0 else 1) {
            p.textBottom = it == 0
        }
        findViewById<Switch>(R.id.more_quotes).apply {
            isChecked = p.moreQuotes
            setOnCheckedChangeListener { _, on -> p.moreQuotes = on } // applies to the next days' picks
        }
        findViewById<Switch>(R.id.quiz_enabled).apply {
            isChecked = p.quizEnabled
            setOnCheckedChangeListener { _, on -> p.quizEnabled = on; DailyReceiver.scheduleQuiz(context) }
        }
        val quizTime = findViewById<Button>(R.id.quiz_time)
        fun showQuizTime() { quizTime.text = getString(R.string.quiz_time, p.quizHour, p.quizMinute) }
        showQuizTime()
        quizTime.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                p.quizHour = h; p.quizMinute = m; showQuizTime(); DailyReceiver.scheduleQuiz(this)
            }, p.quizHour, p.quizMinute, true).show()
        }
        val cat = Styles.load(this)
        pool(R.id.palettes, R.string.palettes, cat.palettes.map { it.id to it.nom }, { p.palettes }) { p.palettes = it }
        pool(R.id.polices, R.string.polices, cat.polices.map { it.id to it.nom }, { p.polices }) { p.polices = it }
        findViewById<Button>(R.id.test).setOnClickListener {
            DailyReceiver.show(this)
            Toast.makeText(this, R.string.test_done, Toast.LENGTH_LONG).show()
        }
        DailyReceiver.scheduleNext(this)
        DailyReceiver.scheduleQuiz(this)
        findViewById<ImageView>(R.id.glose).setOnClickListener { Glose.play(it as ImageView) } // a tap replays it
    }

    override fun onResume() {
        super.onResume()
        findViewById<ImageView>(R.id.glose).apply { setImageDrawable(Glose.mine(context, Glose.mainPose(Prefs(context)))); Glose.play(this) }
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

    private var current: Item? = null

    /** Displays one word or quote in the card, in its palette and font; links follow it. */
    private fun show(item: Item) {
        current = item
        val st = Styles.of(this, item)
        findViewById<View>(R.id.card).background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(st.palette.haut, st.palette.bas),
        ).apply { cornerRadius = 16 * resources.displayMetrics.density }
        findViewById<TextView>(R.id.mot).apply { typeface = st.typeface; setTextColor(st.palette.mot); textSize = if (item is Quote) 24f else 40f }
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
                cnrtl.text = getString(R.string.wikiquote)
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
                cnrtl.text = getString(R.string.cnrtl)
                cnrtl.setOnClickListener { open(cnrtl(item.mot)) }
            }
        }
        showSeen()
        findViewById<ScrollView>(R.id.scroll)?.smoothScrollTo(0, 0)
    }

    /** One tappable chip per word already shown today. */
    private fun showSeen() {
        val row = findViewById<LinearLayout>(R.id.seen)
        row.removeAllViews()
        Words.seenToday(this).forEach { w ->
            row.addView(Button(this, null, android.R.attr.borderlessButtonStyle).apply {
                text = w.mot
                isAllCaps = false
                setOnClickListener { show(w) }
            })
        }
    }

    /** Button opening a multi-choice list of the catalog; an empty choice means "all". */
    private fun pool(id: Int, label: Int, items: List<Pair<String, String>>, get: () -> Set<String>?, set: (Set<String>?) -> Unit) {
        val b = findViewById<Button>(id)
        fun refresh() { b.text = getString(label, get()?.size ?: items.size, items.size) }
        refresh()
        b.setOnClickListener {
            val checked = BooleanArray(items.size) { get()?.contains(items[it].first) ?: true }
            AlertDialog.Builder(this)
                .setMultiChoiceItems(items.map { it.second }.toTypedArray(), checked) { _, i, on -> checked[i] = on }
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val kept = items.filterIndexed { i, _ -> checked[i] }.map { it.first }.toSet()
                    set(if (kept.isEmpty() || kept.size == items.size) null else kept)
                    refresh()
                    current?.let(::show)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    private fun spinner(id: Int, labels: List<String>, selected: Int, onPick: (Int) -> Unit) =
        findViewById<Spinner>(id).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, labels)
            setSelection(selected.coerceAtLeast(0))
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(a: AdapterView<*>?, v: View?, pos: Int, id: Long) = onPick(pos)
                override fun onNothingSelected(a: AdapterView<*>?) {}
            }
        }

    private fun text(id: Int, s: String) { findViewById<TextView>(id).text = s }

    private fun label(min: Int) = if (min < 60) "$min min" else "${min / 60} h"

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
    private fun openAcademie(mot: String) = Thread {
        val started = System.currentTimeMillis()
        val url = runCatching {
            val c = URL("https://www.dictionnaire-academie.fr/search").openConnection() as HttpURLConnection
            c.requestMethod = "POST"; c.doOutput = true; c.connectTimeout = 5000; c.readTimeout = 5000
            c.setRequestProperty("X-Requested-With", "XMLHttpRequest")
            c.setRequestProperty("Accept", "application/json") // without it the server answers with a redirect
            c.outputStream.use { it.write("term=${URLEncoder.encode(mot, "UTF-8")}&options=1".toByteArray()) }
            JSONObject(c.inputStream.bufferedReader().use { it.readText() }).getJSONArray("result").getJSONObject(0).getString("url")
                .also { require(it.startsWith("https://www.dictionnaire-academie.fr/")) }
        }.getOrDefault(cnrtl(mot))
        // DNS isn't bounded by the socket timeouts: give up if the lookup took too long or the screen is gone
        runOnUiThread { if (!isDestroyed && System.currentTimeMillis() - started < 12_000) open(url) }
    }.start()

    private fun cnrtl(mot: String) = "https://www.cnrtl.fr/definition/" + Uri.encode(mot)
}
