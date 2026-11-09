package fr.definiday

import android.content.Intent
import android.media.AudioManager
import android.view.View
import android.widget.Button
import android.widget.TextView
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.util.TimeZone

/** Characterization of QuizActivity's opening (day, done, empty, restore) and score screen (sounds, rotation): pinned before its split. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "fr-w411dp-h891dp-xxhdpi")
class QuizActivityTest {
    private val ctx = RuntimeEnvironment.getApplication()
    private val today = LocalDate.now().toEpochDay()

    @After fun reset() = Sounds.release()

    private fun views(a: android.app.Activity) = generateSequence(listOf<View>(a.window.decorView)) { l ->
        l.flatMap { v -> (v as? android.view.ViewGroup)?.let { g -> (0 until g.childCount).map(g::getChildAt) } ?: emptyList() }.ifEmpty { null }
    }.flatten()
    private fun texts(a: android.app.Activity) = views(a).filterIsInstance<TextView>().map { it.text.toString() }.toList()
    private fun descs(a: android.app.Activity) = views(a).mapNotNull { it.contentDescription?.toString() }.toList()
    private fun options(a: android.app.Activity) = views(a).filterIsInstance<Button>().map { it.text.toString() }.toList()
    /** Answers the question on screen, right or wrong, and goes on. */
    private fun answer(a: android.app.Activity, right: Boolean = true) {
        val good = quizAnswer(a)
        views(a).filterIsInstance<Button>().first { if (right) it.text == good else it.text != good }.performClick()
        views(a).filterIsInstance<Button>().first { it.text == ctx.getString(R.string.quiz_next) }.performClick()
    }
    private fun quiz(day: Long? = null) =
        Robolectric.buildActivity(QuizActivity::class.java, Intent(ctx, QuizActivity::class.java).apply { day?.let { putExtra(QuizActivity.EXTRA_DAY, it) } }).setup()
    private fun soundsOn() {
        Prefs(ctx).sounds = true
        ctx.getSystemService(AudioManager::class.java).ringerMode = AudioManager.RINGER_MODE_NORMAL
    }
    /** Sounds asked so far play now (each load completes); returns the ones played. */
    private fun played(): List<Int> {
        val pool = shadowOf(Sounds.pool ?: return emptyList())
        Sounds.ALL.forEach { runCatching { pool.notifyResourceLoaded(it, true) } }
        return Sounds.ALL.filter { pool.wasResourcePlayed(it) }
    }

    /** A day extra other than today or the eve opens today's quiz, recorded for today. */
    @Test fun dayExtraOutOfBoundsMeansToday() {
        while (Words.seenToday(ctx).isEmpty()) Words.random(ctx)
        for (d in listOf(today + 1, today - 2, Long.MIN_VALUE)) {
            Prefs(ctx).lastQuizDay = Long.MIN_VALUE
            val a = quiz(d).get()
            assertTrue(ctx.getString(R.string.quiz_progress, 1, Words.seenToday(ctx).size) in descs(a))
            assertTrue(ctx.getString(R.string.quiz_eve) !in texts(a))
            repeat(Words.seenToday(ctx).size) { answer(a) }
            assertEquals(today, Prefs(ctx).lastQuizDay)
            assertTrue(ctx.getString(R.string.quiz_not_counted) !in texts(a))
        }
    }

    /** The eve's quiz opened after today's was counted: played, said not counted, nothing recorded, no rate card. */
    @Test fun evesQuizAfterTodaysDoesNotCount() {
        History(ctx).mark("abjurer", today - 1)
        shadowOf(ctx.packageManager).setInstallSourceInfo(ctx.packageName, "com.android.vending", "com.android.vending")
        val p = Prefs(ctx).apply { lastQuizDay = today; passedQuizzes = 5; quizzes = 3 }
        val c = quiz(today - 1)
        assertTrue(ctx.getString(R.string.quiz_eve) in texts(c.get()))
        answer(c.get())
        val end = texts(c.get())
        assertTrue(end.toString(), ctx.getString(R.string.quiz_not_counted) in end && ctx.getString(R.string.quiz_score, 1, 1) in end)
        assertTrue(ctx.getString(R.string.rate_text) !in end)
        assertEquals(today to 3, p.lastQuizDay to p.quizzes)
        assertTrue(!p.rateAsked)
    }

    /** Nothing seen today: no question, not even a due review; nothing recorded, the same after a rotation. */
    @Test fun emptyQuizAsksNoReview() {
        History(ctx).mark("abjurer", today - 10) // due (J+2 long past), but it only joins an evening with words of its own
        val c = quiz()
        assertEquals(listOf(ctx.getString(R.string.quiz_title), ctx.getString(R.string.quiz_empty), ctx.getString(R.string.quiz_close)), texts(c.get()))
        c.recreate()
        assertEquals(listOf(ctx.getString(R.string.quiz_title), ctx.getString(R.string.quiz_empty), ctx.getString(R.string.quiz_close)), texts(c.get()))
        assertEquals(Long.MIN_VALUE to 0, Prefs(ctx).lastQuizDay to Prefs(ctx).quizzes)
        assertTrue(Memory(ctx).entries().isEmpty())
    }

    /** A rotation mid-quiz keeps the day, the words, their options and the score, even past midnight and with a word seen meanwhile. */
    @Test fun rotationMidQuizKeepsDayWordsAndScore() {
        val tz = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Etc/GMT+12"))
            val d = LocalDate.now().toEpochDay()
            while (Words.seenToday(ctx).size < 2) Words.random(ctx)
            val n = Words.seenToday(ctx).size
            val c = quiz()
            answer(c.get(), right = false)
            val before = options(c.get())
            TimeZone.setDefault(TimeZone.getTimeZone("Etc/GMT-12")) // past midnight
            c.recreate()
            assertEquals(before, options(c.get()))
            Words.random(ctx) // a word seen on the new day: same questions (its options may change: the distractors read History)
            c.recreate()
            assertTrue(descs(c.get()).toString(), ctx.getString(R.string.quiz_progress, 2, n) in descs(c.get()))
            assertTrue(ctx.getString(R.string.quiz_eve) in texts(c.get()))
            repeat(n - 1) { answer(c.get()) }
            assertTrue(ctx.getString(R.string.quiz_score, n - 1, n) in texts(c.get()))
            assertEquals(d to n - 1, Prefs(ctx).lastQuizDay to Prefs(ctx).correctAnswers)
        } finally { TimeZone.setDefault(tz) }
    }

    /** Passed, nothing new to unlock: the bravo and the chime once; a rotation redraws it silently and records nothing twice. */
    @Test fun passedScoreScreenWithoutUnlocks() {
        soundsOn()
        val p = Prefs(ctx).apply { announced = Unlocks.items.map { it.id }.toSet() }
        while (Words.seenToday(ctx).isEmpty()) Words.random(ctx)
        val n = Words.seenToday(ctx).size
        val c = quiz()
        repeat(n) { answer(c.get()) }
        val end = texts(c.get())
        assertTrue(end.toString(), ctx.getString(R.string.quiz_passed) in end && ctx.getString(R.string.quiz_score, n, n) in end)
        assertTrue(ctx.getString(R.string.unlock_new, p.gloseName) !in end && ctx.getString(R.string.collection_see) !in end)
        assertTrue(ctx.getString(R.string.glose_bravo, p.gloseName) in descs(c.get()))
        assertEquals(listOf(Sounds.RIGHT, Sounds.CHIME), played())
        shadowOf(Sounds.pool).clearPlayed()
        c.recreate()
        assertEquals(end, texts(c.get()))
        assertEquals(emptyList<Int>(), played())
        assertEquals(1 to 1, p.quizzes to p.passedQuizzes)
    }

    /** Failed: « encore », the missed words, « on retente », and silence after the answers' sounds; sounds off: nothing loaded at all. */
    @Test fun failedScoreScreenIsSilent() {
        for (on in listOf(true, false)) {
            Sounds.release()
            if (on) soundsOn() else Prefs(ctx).sounds = false
            val p = Prefs(ctx).apply { announced = Unlocks.items.map { it.id }.toSet(); lastQuizDay = Long.MIN_VALUE }
            while (Words.seenToday(ctx).isEmpty()) Words.random(ctx)
            val c = quiz()
            repeat(Words.seenToday(ctx).size) { answer(c.get(), right = false) }
            val end = texts(c.get())
            assertTrue(end.toString(), ctx.getString(R.string.quiz_retry) in end && ctx.getString(R.string.quiz_passed) !in end)
            assertTrue(end.any { it.startsWith(ctx.getString(R.string.quiz_missed, "")) })
            assertTrue(ctx.getString(R.string.glose_encore, p.gloseName) in descs(c.get()))
            if (on) assertEquals(listOf(Sounds.WRONG), played()) else assertNull(Sounds.pool)
            c.recreate()
            assertEquals(end, texts(c.get()))
        }
    }
}
