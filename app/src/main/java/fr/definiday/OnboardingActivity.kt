package fr.definiday

import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher

/**
 * First launch, before the home screen: welcome, where (mode, lock-screen preview, wallpaper warning), when (time, words a day,
 * quiz), then the permissions in context, only those the choices need. No paywall; « Passer » at every step.
 * Choices go to Prefs as they are made (as in Settings): skipping keeps them and the defaults for the rest. Only the step is
 * instance state. Back goes to the previous step.
 */
class OnboardingActivity : Activity() {
    companion object { private const val STATE_STEP = "step"; private const val STATE_ASKED = "asked"; private const val WHERE = 1; private const val ASK = 3 }

    private lateinit var p: Prefs
    private var step = 0
    private var shown = -1 // step on screen: scroll and TalkBack move only when it changes
    private var asked = false // the notification prompt was shown once: not offered again, whatever the answer
    private val back by lazy { OnBackInvokedCallback { go(step - 1) } }
    private val modes = listOf(R.id.mode_both, R.id.mode_notif, R.id.mode_lock) // index = Prefs.MODE_*

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        p = Prefs(this)
        step = savedInstanceState?.getInt(STATE_STEP) ?: 0
        asked = savedInstanceState?.getBoolean(STATE_ASKED) ?: false

        val glose = findViewById<ImageView>(R.id.glose)
        val taps = Glose.Taps()
        glose.setOnClickListener { Glose.tap(glose, taps, Glose.mainPose(p)) }

        findViewById<RadioGroup>(R.id.mode).apply {
            check(modes[p.mode])
            setOnCheckedChangeListener { _, id -> p.mode = modes.indexOf(id); go(step) }
        }
        findViewById<Switch>(R.id.unlock_once).apply {
            isChecked = p.unlocks > 0
            setOnCheckedChangeListener { _, on -> p.unlocks = if (on) 1 else 0; go(step) } // 2nd, 3rd unlock: in Settings
        }

        val time = findViewById<Button>(R.id.time)
        fun showTime() { time.text = getString(R.string.time, p.hour, p.minute) }
        showTime()
        time.setOnClickListener { TimePickerDialog(this, { _, h, m -> p.hour = h; p.minute = m; showTime() }, p.hour, p.minute, true).show() }
        findViewById<Spinner>(R.id.per_day).apply {
            adapter = ArrayAdapter(context, R.layout.spinner_item, listOf("1 mot", "2 mots", "3 mots"))
            setSelection(p.wordsPerDay - 1)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(a: AdapterView<*>?, v: View?, pos: Int, id: Long) { p.wordsPerDay = pos + 1 }
                override fun onNothingSelected(a: AdapterView<*>?) {}
            }
        }
        val quizTime = findViewById<Button>(R.id.quiz_time)
        fun showQuizTime() { quizTime.text = getString(R.string.quiz_time, p.quizHour, p.quizMinute); quizTime.isEnabled = p.quizEnabled }
        showQuizTime()
        findViewById<Switch>(R.id.quiz_enabled).apply {
            isChecked = p.quizEnabled
            setOnCheckedChangeListener { _, on -> p.quizEnabled = on; showQuizTime(); go(step) } // may add or drop the last step
        }
        quizTime.setOnClickListener {
            TimePickerDialog(this, { _, h, m -> p.quizHour = h; p.quizMinute = m; showQuizTime() }, p.quizHour, p.quizMinute, true).show()
        }

        findViewById<Button>(R.id.exact_ask).setOnClickListener { Alarms.requestIntent(this)?.let { runCatching { startActivity(it) } } }
        findViewById<Button>(R.id.next).setOnClickListener {
            if (step == ASK && needNotif()) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0) else go(step + 1)
        }
        findViewById<Button>(R.id.skip).setOnClickListener { done() }
    }

    /** Also back from the permission prompt and from « Alarmes et rappels »: the last step drops what is granted, and ends when nothing is left. */
    override fun onResume() {
        super.onResume()
        if (isFinishing) return
        go(step)
        findViewById<ImageView>(R.id.glose).apply { setImageDrawable(Glose.mine(context, Glose.mainPose(p))); Glose.play(this) }
    }

    override fun onPause() {
        super.onPause()
        Glose.stop(findViewById(R.id.glose))
    }

    override fun onSaveInstanceState(out: Bundle) {
        super.onSaveInstanceState(out)
        out.putInt(STATE_STEP, step); out.putBoolean(STATE_ASKED, asked)
    }

    override fun onRequestPermissionsResult(code: Int, permissions: Array<String>, results: IntArray) {
        asked = true // granted or not, we go on: the home banner offers it again
        go(ASK)
    }

    @Deprecated("Android 13+ with predictive back calls the OnBackInvokedCallback instead")
    override fun onBackPressed() = if (step > 0) go(step - 1) else super.onBackPressed()

    /** Notifications carry the word (unless lock screen only) or the quiz reminder, and Android 13+ has not granted them yet. */
    private fun needNotif() = Build.VERSION.SDK_INT >= 33 && !asked && (p.mode != Prefs.MODE_LOCK || p.quizEnabled) &&
        checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED

    /** The removal at the 1st unlock needs an exact alarm to start its watch from the background (Alarms). */
    private fun needExact() = p.mode != Prefs.MODE_NOTIF && p.unlocks > 0 && Alarms.requestIntent(this) != null

    private fun last() = if (needNotif() || needExact()) ASK else ASK - 1

    /** Shows step [to] (past the last one: done), with its actions. */
    private fun go(to: Int) {
        if (to > last()) return done()
        step = to
        val steps = listOf(R.id.step_welcome, R.id.step_where, R.id.step_when, R.id.step_ask)
        steps.forEachIndexed { i, id -> findViewById<View>(id).visibility = if (i == step) View.VISIBLE else View.GONE }
        findViewById<TextView>(R.id.progress).text = getString(R.string.onb_progress, step + 1, last() + 1)
        if (step == WHERE) where()
        val notif = needNotif()
        findViewById<View>(R.id.notif_why).visibility = if (notif) View.VISIBLE else View.GONE
        findViewById<TextView>(R.id.notif_why).text = getString(R.string.onb_notif_why, getString(when {
            p.mode == Prefs.MODE_LOCK -> R.string.onb_notif_quiz; p.quizEnabled -> R.string.onb_notif_both; else -> R.string.onb_notif_word }))
        findViewById<View>(R.id.exact_box).visibility = if (needExact()) View.VISIBLE else View.GONE
        findViewById<Button>(R.id.next).setText(when {
            step == 0 -> R.string.onb_start; step == ASK && notif -> R.string.onb_allow; step == last() -> R.string.onb_done; else -> R.string.onb_next })
        findViewById<Button>(R.id.skip).apply {
            setText(if (step == ASK) R.string.onb_later else R.string.onb_skip)
            visibility = if (step == ASK && !notif) View.GONE else View.VISIBLE
        }
        if (Build.VERSION.SDK_INT >= 33) onBackInvokedDispatcher.run {
            unregisterOnBackInvokedCallback(back)
            if (step > 0) registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, back)
        }
        if (shown == step) return
        shown = step
        findViewById<ScrollView>(R.id.scroll).scrollTo(0, 0)
        val title = findViewById<View>(listOf(R.id.title_welcome, R.id.title_where, R.id.title_when, R.id.title_ask)[step])
        title.post { title.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null) }
    }

    /** Step 2: the lock-screen part only for the modes that use it, with the first daily word (not picked yet) as the system would draw it. */
    private fun where() {
        val lock = p.mode != Prefs.MODE_NOTIF
        findViewById<View>(R.id.lock_box).visibility = if (lock) View.VISIBLE else View.GONE
        if (!lock) return
        val w = Words.nextDaily(Words.load(this), History(this).all())
        val st = Styles.of(this, w)
        val m = resources.displayMetrics
        val width = (180 * m.density).toInt()
        val height = width * maxOf(m.widthPixels, m.heightPixels) / minOf(m.widthPixels, m.heightPixels) // portrait screen ratio
        val bmp = WordCard.render(this, w, width, height, st, p.textBottom)
        // the clock the lock screen draws above the word, at the chosen time
        Canvas(bmp).drawText("%02d:%02d".format(p.hour, p.minute), width / 2f, height * 0.22f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = width * 0.22f; color = st.palette.mot; textAlign = Paint.Align.CENTER; typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        })
        findViewById<ImageView>(R.id.preview).apply {
            setImageBitmap(bmp)
            background = Styles.card(context, st.palette, 16f); clipToOutline = true
            contentDescription = getString(R.string.onb_preview, w.mot)
        }
    }

    /** Finished or skipped: never shown again; alarms set as Settings does; the home screen. */
    private fun done() {
        p.onboarded = true
        DailyReceiver.scheduleNext(this)
        DailyReceiver.scheduleQuiz(this)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
