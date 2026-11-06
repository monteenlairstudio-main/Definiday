package fr.definiday

import android.app.Activity
import android.app.AlertDialog
import android.app.TimePickerDialog
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
        val cat = Styles.load(this)
        pool(R.id.palettes, R.string.palettes, R.string.palettes_title, cat.palettes.map { it.id to it.nom }, { p.palettes }) { p.palettes = it }
        pool(R.id.polices, R.string.polices, R.string.polices_title, cat.polices.map { it.id to it.nom }, { p.polices }) { p.polices = it }
    }

    override fun onResume() {
        super.onResume()
        // back from the system "Alarms & reminders" page
        findViewById<View>(R.id.exact_box).visibility = if (Alarms.requestIntent(this) == null) View.GONE else View.VISIBLE
    }

    /** Button opening a multi-choice list of the catalog; an empty choice means "all". */
    private fun pool(id: Int, label: Int, title: Int, items: List<Pair<String, String>>, get: () -> Set<String>?, set: (Set<String>?) -> Unit) {
        val b = findViewById<Button>(id)
        fun refresh() { b.text = getString(label, get()?.size ?: items.size, items.size) }
        refresh()
        b.setOnClickListener {
            val checked = BooleanArray(items.size) { get()?.contains(items[it].first) ?: true }
            AlertDialog.Builder(this)
                .setTitle(title)
                .setMultiChoiceItems(items.map { it.second }.toTypedArray(), checked) { _, i, on -> checked[i] = on }
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val kept = items.filterIndexed { i, _ -> checked[i] }.map { it.first }.toSet()
                    set(if (kept.isEmpty() || kept.size == items.size) null else kept)
                    refresh()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
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
