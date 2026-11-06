package fr.definiday

import android.app.NotificationManager
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.provider.Settings
import android.text.Layout
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.LocalDate

/** Home / Settings / Quiz interface after the interface audit (docs/audits/interface.md). */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "fr-w411dp-h891dp-xxhdpi")
class UiTest {
    private val ctx = RuntimeEnvironment.getApplication()
    private val nm = ctx.getSystemService(NotificationManager::class.java)

    private fun views(a: android.app.Activity) = generateSequence(listOf<View>(a.window.decorView)) { l ->
        l.flatMap { v -> (v as? android.view.ViewGroup)?.let { g -> (0 until g.childCount).map(g::getChildAt) } ?: emptyList() }.ifEmpty { null }
    }.flatten()
    private fun buttons(a: android.app.Activity) = views(a).filterIsInstance<Button>().toList()
    private fun pick(s: Spinner, pos: Int) = s.onItemSelectedListener!!.onItemSelected(s, null, pos, pos.toLong())

    @Test fun settingsButtonOpensSettings() {
        val a = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        a.findViewById<Button>(R.id.settings_open).performClick()
        assertEquals(SettingsActivity::class.java.name, shadowOf(a).nextStartedActivity.component!!.className)
        Robolectric.buildActivity(SettingsActivity::class.java).setup() // inflates and wires every control
    }

    /** B2: notifications refused while the mode uses them -> banner opening the app's notification settings. */
    @Test fun bannerWhenNotificationsAreOff() {
        fun banner() = Robolectric.buildActivity(MainActivity::class.java).setup().get().let { it to it.findViewById<View>(R.id.notif_banner) }
        Prefs(ctx).mode = Prefs.MODE_NOTIF
        assertEquals(View.GONE, banner().second.visibility)
        shadowOf(nm).setNotificationsEnabled(false)
        val (a, b) = banner()
        assertEquals(View.VISIBLE, b.visibility)
        b.performClick()
        val i = shadowOf(a).nextStartedActivity
        assertEquals(Settings.ACTION_APP_NOTIFICATION_SETTINGS, i.action)
        assertEquals(ctx.packageName, i.getStringExtra(Settings.EXTRA_APP_PACKAGE))
        Prefs(ctx).mode = Prefs.MODE_LOCK // the wallpaper needs no notification
        assertEquals(View.GONE, banner().second.visibility)
    }

    /** B3: right and wrong are written (✓ / ✗), and the tint keeps the button's rounded shape. */
    @Test fun quizMarksAnswersInText() {
        while (Words.seenToday(ctx).isEmpty()) Words.random(ctx)
        val a = Robolectric.buildActivity(QuizActivity::class.java).setup().get()
        val defs = Words.load(ctx).associateBy { it.definition }
        val answer = views(a).filterIsInstance<TextView>().firstNotNullOf { defs[it.text.toString()] }
        val wrong = buttons(a).first { b -> b.text != answer.mot }
        val wrongMot = wrong.text.toString()
        wrong.performClick()
        assertEquals("✗ $wrongMot", wrong.text.toString())
        assertTrue(buttons(a).any { it.text == "✓ ${answer.mot}" })
        assertTrue(views(a).filterIsInstance<TextView>().any { // visible, and read by TalkBack as a live region
            it.text == ctx.getString(R.string.quiz_wrong, answer.mot) && it.accessibilityLiveRegion == View.ACCESSIBILITY_LIVE_REGION_POLITE })
        assertTrue(buttons(a).none { it.background is ColorDrawable })
    }

    /** I10: a second quiz the same day says its score does not count; the home button shows the quiz is done. */
    @Test fun quizRedoneSameDaySaysItDoesNotCount() {
        while (Words.seenToday(ctx).isEmpty()) Words.random(ctx)
        val today = LocalDate.now().toEpochDay()
        val main = Robolectric.buildActivity(MainActivity::class.java).setup()
        assertEquals(ctx.getString(R.string.quiz_open), main.get().findViewById<Button>(R.id.quiz_open).text.toString())
        Prefs(ctx).lastQuizDay = today
        main.pause().resume()
        assertEquals(ctx.getString(R.string.quiz_done), main.get().findViewById<Button>(R.id.quiz_open).text.toString())
        val c = Robolectric.buildActivity(QuizActivity::class.java).setup()
        repeat(Words.seenToday(ctx).size) {
            buttons(c.get()).first().performClick()
            buttons(c.get()).first { it.text == ctx.getString(R.string.quiz_next) }.performClick()
        }
        assertTrue(views(c.get()).filterIsInstance<TextView>().any { it.text == ctx.getString(R.string.quiz_not_counted) })
        c.recreate() // still said after a rotation on the score screen
        assertTrue(views(c.get()).filterIsInstance<TextView>().any { it.text == ctx.getString(R.string.quiz_not_counted) })
    }

    /** "Fin sur l'écran de verrouillage" changed in Settings while the word is shown: the unlock watch follows. */
    @Test fun settingsUnlocksSyncTheWatch() {
        Prefs(ctx).apply { mode = Prefs.MODE_LOCK; unlocks = 0; durationMinutes = 60 }
        DailyReceiver.show(ctx)
        val a = Robolectric.buildActivity(SettingsActivity::class.java).setup().get()
        while (shadowOf(ctx).nextStartedService != null) Unit
        pick(a.findViewById(R.id.unlocks), 2)
        assertEquals(2, Prefs(ctx).unlocks)
        assertEquals(UnlockService::class.java.name, shadowOf(ctx).nextStartedService.component!!.className)
    }

    /** More words a day once today's time has passed: the new ones are shown at once. */
    @Test fun raisingWordsPerDayAfterTheTimeShowsThem() {
        Prefs(ctx).apply { mode = Prefs.MODE_NOTIF; wordsPerDay = 1; hour = 0; minute = 0 }
        DailyReceiver.show(ctx)
        assertEquals(1, nm.activeNotifications.size)
        val a = Robolectric.buildActivity(SettingsActivity::class.java).setup().get()
        pick(a.findViewById(R.id.per_day), 2)
        assertEquals(3, nm.activeNotifications.size)
        pick(a.findViewById(R.id.per_day), 0) // fewer: nothing re-shown
        assertEquals(3, nm.activeNotifications.size)
    }

    /** Duration help follows the mode: in notification mode it speaks of the notification, not the lock screen. */
    @Test fun durationHelpFollowsTheMode() {
        Prefs(ctx).mode = Prefs.MODE_NOTIF
        val a = Robolectric.buildActivity(SettingsActivity::class.java).setup().get()
        assertEquals(ctx.getString(R.string.duration_notif), a.findViewById<TextView>(R.id.duration_hint).text.toString())
        assertEquals(View.GONE, a.findViewById<View>(R.id.unlocks).visibility)
        pick(a.findViewById(R.id.mode), Prefs.MODE_LOCK)
        assertEquals(ctx.getString(R.string.duration_lock), a.findViewById<TextView>(R.id.duration_hint).text.toString())
        assertEquals(View.VISIBLE, a.findViewById<View>(R.id.unlocks).visibility)
    }

    /** I8 / A14: the longest word, small screen, font x2: the headword shrinks down to 1.3 x the definition at most;
     *  if it still does not hold on one line, it breaks between syllables (never anywhere). */
    @Test @Config(qualifiers = "fr-w360dp-h740dp-xxhdpi") fun longWordFitsAtLargeFont() {
        RuntimeEnvironment.setFontScale(2f)
        val w = Words.load(ctx).maxBy { it.mot.length * (if (' ' in it.mot || '-' in it.mot) 0 else 1) }
        val a = Robolectric.buildActivity(MainActivity::class.java, Intent(ctx, MainActivity::class.java).putExtra(MainActivity.EXTRA_MOT, w.key)).setup().get()
        val mot = a.findViewById<TextView>(R.id.mot)
        assertEquals(w.mot, mot.text.toString())
        assertTrue(mot.paint.measureText(w.mot) <= (360 - 72) * ctx.resources.displayMetrics.density || mot.hyphenationFrequency == Layout.HYPHENATION_FREQUENCY_FULL)
        assertTrue(mot.textSize >= a.findViewById<TextView>(R.id.definition).textSize)
    }

    /** A5: a random pick moves TalkBack to the card, a single stop; opening the screen does not. */
    @Test fun randomPickMovesTalkBackToTheCard() {
        shadowOf(ctx.getSystemService(AccessibilityManager::class.java)).apply { setEnabled(true); setTouchExplorationEnabled(true) }
        val a = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val card = a.findViewById<View>(R.id.card)
        assertTrue(card.isScreenReaderFocusable)
        shadowOf(android.os.Looper.getMainLooper()).idle()
        assertTrue(!card.isAccessibilityFocused)
        a.findViewById<Button>(R.id.new_word).performClick()
        shadowOf(android.os.Looper.getMainLooper()).idle()
        assertTrue(card.isAccessibilityFocused)
    }
}
