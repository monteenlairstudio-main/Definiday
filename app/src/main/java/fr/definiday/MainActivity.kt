package fr.definiday

import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
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
        show(daily.firstOrNull { it.mot == intent.getStringExtra(EXTRA_MOT) } ?: daily.first())
        findViewById<Button>(R.id.new_word).setOnClickListener { show(Words.random(this)) }

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
        val unlocks = listOf(R.id.unlocks_label, R.id.unlocks).map { findViewById<View>(it) }
        fun showUnlocks() = unlocks.forEach { it.visibility = if (p.mode == Prefs.MODE_NOTIF) View.GONE else View.VISIBLE }
        // order matches Prefs.MODE_BOTH, MODE_NOTIF, MODE_LOCK
        spinner(R.id.mode, listOf("Notification et écran de verrouillage", "Notification seulement", "Écran de verrouillage seulement"), p.mode) {
            p.mode = it; showUnlocks()
        }
        showUnlocks()
        spinner(R.id.unlocks, listOf("Après la durée choisie", "Au 1er déverrouillage", "Au 2e déverrouillage", "Au 3e déverrouillage"), p.unlocks) {
            p.unlocks = it
        }
        findViewById<Button>(R.id.test).setOnClickListener {
            DailyReceiver.show(this)
            Toast.makeText(this, R.string.test_done, Toast.LENGTH_LONG).show()
        }
        DailyReceiver.scheduleNext(this)
    }

    /** Displays one word in the card; links follow it. */
    private fun show(w: Word) {
        text(R.id.label, getString(if (w.mot in Prefs(this).dailyMots) R.string.today else R.string.random))
        text(R.id.mot, w.mot)
        text(R.id.nature, w.nature)
        text(R.id.definition, w.definition)
        text(R.id.exemple, if (w.exemple.isEmpty()) "" else "«\u00a0${w.exemple}\u00a0»")
        text(R.id.anecdote, w.anecdote)
        findViewById<View>(R.id.anecdote_box).visibility = if (w.anecdote.isEmpty()) View.GONE else View.VISIBLE
        findViewById<Button>(R.id.source).setOnClickListener { open(w.source) }
        findViewById<Button>(R.id.academie).setOnClickListener { openAcademie(w.mot) }
        findViewById<Button>(R.id.cnrtl).setOnClickListener { open(cnrtl(w.mot)) }
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

    private fun open(url: String) = try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, R.string.no_browser, Toast.LENGTH_LONG).show()
    }

    /** One lookup on the Académie's own search, triggered by the user's tap, to reach the article page. Falls back to CNRTL. */
    private fun openAcademie(mot: String) = Thread {
        val url = runCatching {
            val c = URL("https://www.dictionnaire-academie.fr/search").openConnection() as HttpURLConnection
            c.requestMethod = "POST"; c.doOutput = true; c.connectTimeout = 5000; c.readTimeout = 5000
            c.setRequestProperty("X-Requested-With", "XMLHttpRequest")
            c.setRequestProperty("Accept", "application/json") // without it the server answers with a redirect
            c.outputStream.use { it.write("term=${URLEncoder.encode(mot, "UTF-8")}&options=1".toByteArray()) }
            JSONObject(c.inputStream.bufferedReader().use { it.readText() }).getJSONArray("result").getJSONObject(0).getString("url")
                .also { require(it.startsWith("https://www.dictionnaire-academie.fr/")) }
        }.getOrDefault(cnrtl(mot))
        runOnUiThread { open(url) }
    }.start()

    private fun cnrtl(mot: String) = "https://www.cnrtl.fr/definition/" + Uri.encode(mot)
}
