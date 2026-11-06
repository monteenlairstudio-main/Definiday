package fr.definiday

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import java.time.LocalTime

/** Settings in 4 groups: Mot du jour, Écran de verrouillage, Quiz du soir, Apparence. Back to the home screen, which re-reads them. */
class SettingsActivity : Activity() {
    private val durations = listOf(15, 30, 60, 120, 240, 480, 720, 1440)
    private lateinit var p: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        p = Prefs(this)

        // Mot du jour
        val time = findViewById<Button>(R.id.time)
        fun showTime() { time.text = getString(R.string.time, p.hour, p.minute); time.isEnabled = p.enabled }
        showTime()
        findViewById<Switch>(R.id.enabled).apply {
            isChecked = p.enabled
            setOnCheckedChangeListener { _, on -> p.enabled = on; showTime(); DailyReceiver.scheduleNext(context) }
        }
        time.setOnClickListener {
            TimePickerDialog(this, { _, h, m -> p.hour = h; p.minute = m; showTime(); DailyReceiver.scheduleNext(this) }, p.hour, p.minute, true).show()
        }
        findViewById<Button>(R.id.exact_ask).setOnClickListener { Alarms.requestIntent(this)?.let { runCatching { startActivity(it) } } }
        spinner(R.id.per_day, listOf("1 mot", "2 mots", "3 mots"), p.wordsPerDay - 1) {
            val more = it + 1 > p.wordsPerDay
            if (p.wordsPerDay != it + 1) { p.wordsPerDay = it + 1; Words.today(this) }
            // today's time already passed: the new picks are shown now rather than only reachable from the home chips
            if (more && p.enabled && !LocalTime.now().isBefore(LocalTime.of(p.hour, p.minute))) DailyReceiver.show(this)
        }
        findViewById<Switch>(R.id.more_quotes).apply {
            isChecked = p.moreQuotes
            setOnCheckedChangeListener { _, on -> p.moreQuotes = on } // applies to the next days' picks
        }
        findViewById<Button>(R.id.test).setOnClickListener {
            DailyReceiver.show(this)
            Toast.makeText(this, if (p.mode == Prefs.MODE_NOTIF) R.string.test_done_notif else R.string.test_done, Toast.LENGTH_LONG).show()
        }
        findViewById<Button>(R.id.widget_add).setOnClickListener { Widget.pin(this) }

        // Écran de verrouillage
        val lockOnly = listOf(R.id.wallpaper_hint, R.id.unlocks_label, R.id.unlocks, R.id.unlocks_hint, R.id.text_pos_label, R.id.text_pos).map { findViewById<View>(it) }
        fun showMode() {
            lockOnly.forEach { it.visibility = if (p.mode == Prefs.MODE_NOTIF) View.GONE else View.VISIBLE }
            findViewById<TextView>(R.id.duration_hint).setText(when (p.mode) {
                Prefs.MODE_NOTIF -> R.string.duration_notif; Prefs.MODE_LOCK -> R.string.duration_lock; else -> R.string.duration_both
            })
        }
        // order matches Prefs.MODE_BOTH, MODE_NOTIF, MODE_LOCK
        spinner(R.id.mode, listOf("Notification et écran de verrouillage", "Notification seulement", "Écran de verrouillage seulement"), p.mode) {
            p.mode = it; showMode()
        }
        showMode()
        spinner(R.id.duration, durations.map(::label), durations.indexOf(p.durationMinutes)) { p.durationMinutes = durations[it] }
        spinner(R.id.unlocks, listOf("Après la durée choisie", "Au 1er déverrouillage", "Au 2e déverrouillage", "Au 3e déverrouillage"), p.unlocks) {
            p.unlocks = it; UnlockService.sync(this) // applies to the word already on the lock screen
        }
        spinner(R.id.text_pos, listOf("En bas (laisse la place à l’horloge)", "Au milieu"), if (p.textBottom) 0 else 1) { p.textBottom = it == 0 }

        // Quiz du soir
        val quizTime = findViewById<Button>(R.id.quiz_time)
        fun showQuizTime() { quizTime.text = getString(R.string.quiz_time, p.quizHour, p.quizMinute); quizTime.isEnabled = p.quizEnabled }
        showQuizTime()
        findViewById<Switch>(R.id.quiz_enabled).apply {
            isChecked = p.quizEnabled
            setOnCheckedChangeListener { _, on -> p.quizEnabled = on; showQuizTime(); DailyReceiver.scheduleQuiz(context) }
        }
        quizTime.setOnClickListener {
            TimePickerDialog(this, { _, h, m -> p.quizHour = h; p.quizMinute = m; showQuizTime(); DailyReceiver.scheduleQuiz(this) }, p.quizHour, p.quizMinute, true).show()
        }

        // Apparence
        findViewById<Button>(R.id.styles_open).setOnClickListener { startActivity(Intent(this, StylesActivity::class.java)) }
        findViewById<Switch>(R.id.sounds).apply {
            isChecked = p.sounds
            setOnCheckedChangeListener { _, on -> p.sounds = on; if (on) Sounds.play(context, Sounds.PAGE) } // a sample, so the choice is heard
        }
    }

    override fun onResume() {
        super.onResume()
        // back from the system "Alarms & reminders" page
        findViewById<View>(R.id.exact_box).visibility = if (Alarms.requestIntent(this) == null) View.GONE else View.VISIBLE
        // back from Couleurs et polices
        val cat = Styles.load(this)
        findViewById<TextView>(R.id.styles_hint).text = getString(R.string.styles_row_hint,
            Styles.kept(cat.palettes, p.palettes, Palette::id).size, cat.palettes.size, Styles.kept(cat.polices, p.polices, Police::id).size, cat.polices.size)
    }

    private fun spinner(id: Int, labels: List<String>, selected: Int, onPick: (Int) -> Unit) =
        findViewById<Spinner>(id).apply {
            adapter = ArrayAdapter(context, R.layout.spinner_item, labels)
            setSelection(selected.coerceAtLeast(0))
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(a: AdapterView<*>?, v: View?, pos: Int, id: Long) = onPick(pos)
                override fun onNothingSelected(a: AdapterView<*>?) {}
            }
        }

    private fun label(min: Int) = if (min < 60) "$min min" else "${min / 60} h"
}
