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
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MainActivity : Activity() {
    private val durations = listOf(15, 30, 60, 120, 240, 480, 720, 1440)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)

        val p = Prefs(this)
        val w = Words.today(this)
        text(R.id.mot, w.mot)
        text(R.id.nature, w.nature)
        text(R.id.definition, w.definition)
        text(R.id.exemple, if (w.exemple.isEmpty()) "" else "«\u00a0${w.exemple}\u00a0»")
        if (w.anecdote.isNotEmpty()) {
            text(R.id.anecdote, w.anecdote)
            findViewById<View>(R.id.anecdote_box).visibility = View.VISIBLE
            findViewById<Button>(R.id.source).setOnClickListener { open(w.source) }
        }
        findViewById<Button>(R.id.academie).setOnClickListener { openAcademie(w.mot) }
        findViewById<Button>(R.id.cnrtl).setOnClickListener { open(cnrtl(w.mot)) }

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
        findViewById<Spinner>(R.id.duration).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, durations.map(::label))
            setSelection(durations.indexOf(p.durationMinutes).coerceAtLeast(0))
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(a: AdapterView<*>?, v: View?, pos: Int, id: Long) { p.durationMinutes = durations[pos] }
                override fun onNothingSelected(a: AdapterView<*>?) {}
            }
        }
        findViewById<Switch>(R.id.wallpaper).apply {
            isChecked = p.wallpaper
            setOnCheckedChangeListener { _, on -> p.wallpaper = on }
        }
        findViewById<Button>(R.id.test).setOnClickListener {
            DailyReceiver.show(this)
            Toast.makeText(this, R.string.test_done, Toast.LENGTH_LONG).show()
        }
        DailyReceiver.scheduleNext(this)
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
