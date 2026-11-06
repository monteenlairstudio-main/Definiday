package fr.definiday

import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.view.View
import android.widget.Button
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
import java.time.ZonedDateTime
import java.util.TimeZone

/** Each test reproduces one bug found at 442fcaf: they FAIL on that commit. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "fr-w411dp-h891dp-xxhdpi")
class BugHuntTest {
    private val ctx = RuntimeEnvironment.getApplication()

    private fun views(a: android.app.Activity) = generateSequence(listOf<View>(a.window.decorView)) { l ->
        l.flatMap { v -> (v as? android.view.ViewGroup)?.let { g -> (0 until g.childCount).map(g::getChildAt) } ?: emptyList() }.ifEmpty { null }
    }.flatten()
    private fun texts(a: android.app.Activity) = views(a).filterIsInstance<TextView>().map { it.text.toString() }.toList()
    /** Answers the current question (first option) and goes to the next one. */
    private fun answerOne(a: android.app.Activity) {
        val defs = Words.load(ctx).map { it.definition }.toSet()
        val q = texts(a).first { it in defs }
        views(a).filterIsInstance<Button>().first { b -> Words.load(ctx).any { it.mot == b.text.toString() && it.definition == q } }.performClick()
        views(a).filterIsInstance<Button>().first { it.text == ctx.getString(R.string.quiz_next) }.performClick()
    }

    /** BUG 1: with the default settings (1 item a day, 1 quote in 5), a quote day has no quiz and resets the streak: max streak 4. */
    @Test fun quoteDaysDoNotCapTheStreak() {
        // Robolectric part: a day whose only daily slot is a quote (what Words.today() picks when isQuoteSlot(day, 0, false))
        val p = Prefs(ctx)
        val today = LocalDate.now().toEpochDay()
        p.dailyDay = today; p.dailyMots = listOf(Quotes.load(ctx).first().key)
        assertTrue(Words.seenToday(ctx).isEmpty())
        val a = Robolectric.buildActivity(QuizActivity::class.java).setup().get()
        assertTrue(ctx.getString(R.string.quiz_empty) in texts(a)) // nothing to answer, nothing recorded
        assertEquals(Long.MIN_VALUE, p.lastQuizDay)
        // A full year of perfect evenings, quizzing every day the app gives quiz material.
        var best = 0
        for (d in 20_000L until 20_365L) if (!Words.isQuoteSlot(d, 0, false)) { Quiz.record(p, 1, 1, d); best = maxOf(best, p.streak) }
        assertTrue("best streak in a year of daily quizzes = $best: écharpe (7), bonnet (14), nuit (30) are unreachable", best >= 30)
    }

    /** BUG 2: a quiz started before midnight and finished after counts for the next day: streak broken, next evening's quiz ignored. */
    @Test fun quizFinishedAfterMidnightCountsForTheDayItWasStarted() {
        val tz = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Etc/GMT+12")) // UTC-12: evening of day D
            val d = LocalDate.now().toEpochDay()
            val p = Prefs(ctx).apply { lastQuizDay = d - 1; streak = 6 }
            while (Words.seenToday(ctx).isEmpty()) Words.random(ctx)
            val a = Robolectric.buildActivity(QuizActivity::class.java).setup().get()
            TimeZone.setDefault(TimeZone.getTimeZone("Etc/GMT-12")) // same instant seen 24 h later: past midnight, day D+1
            assertEquals(d + 1, LocalDate.now().toEpochDay())
            repeat(Words.run { TimeZone.setDefault(TimeZone.getTimeZone("Etc/GMT+12")); seenToday(ctx).size.also { TimeZone.setDefault(TimeZone.getTimeZone("Etc/GMT-12")) } }) { answerOne(a) }
            assertEquals("quiz of day D recorded as", d, p.lastQuizDay)
            assertEquals(7, p.streak)
        } finally { TimeZone.setDefault(tz) }
    }

    /** BUG 3: switching "Fin sur l'écran de verrouillage" to "Après la durée choisie" while the watch runs removes the word at the next unlock. */
    @Test fun unlocksSetToZeroKeepsTheWallpaperForTheDuration() {
        val p = Prefs(ctx).apply { mode = Prefs.MODE_LOCK; unlocks = 2; durationMinutes = 60 }
        DailyReceiver.show(ctx) // UnlockService started
        p.unlocks = 0 // MainActivity spinner: only the pref changes, the service keeps counting
        UnlockService.countUnlock(ctx) // next unlock
        assertTrue("wallpaper removed at the 1st unlock although the user chose the duration", p.wallpaperEndAt != 0L)
    }

    /** BUG 4: without an exact-alarm permission the daily SHOW alarm is inexact on Android 12+, so the background start of UnlockService is refused. */
    @Test fun dailyAlarmMayStartTheUnlockWatch() {
        val perms = ctx.packageManager.getPackageInfo(ctx.packageName, PackageManager.GET_PERMISSIONS).requestedPermissions.orEmpty()
        assertTrue("canScheduleExactAlarms() is always false: ${perms.toList()}",
            perms.any { it == android.Manifest.permission.SCHEDULE_EXACT_ALARM || it == android.Manifest.permission.USE_EXACT_ALARM })
    }

    /** BUG 5: tapping the notification of a random pick opens the app on the daily word, not on the word tapped. */
    @Test fun randomPickNotificationOpensThatWord() {
        Words.today(ctx)
        val r = Words.random(ctx)
        DailyReceiver.show(ctx, listOf(r))
        val n = ctx.getSystemService(NotificationManager::class.java).activeNotifications.single().notification
        val tap = shadowOf(n.contentIntent).savedIntent
        val a = Robolectric.buildActivity(MainActivity::class.java, tap).setup().get()
        assertEquals(r.mot, a.findViewById<TextView>(R.id.mot).text.toString())
    }

    /** BUG 6: rotating the phone mid-quiz restarts it from question 1 (score lost, options reshuffled). */
    @Test fun quizSurvivesRotation() {
        while (Words.seenToday(ctx).size < 2) Words.random(ctx)
        val c = Robolectric.buildActivity(QuizActivity::class.java).setup()
        answerOne(c.get())
        val total = Words.seenToday(ctx).size
        assertTrue("2 / $total" in texts(c.get()))
        c.recreate()
        assertTrue("after rotation: ${texts(c.get()).filter { "/" in it }}", "2 / $total" in texts(c.get()))
    }

    /** BUG 7: two words share a definition (aruspice/haruspice, mercuriale/semonce): the quiz can show both, one is marked wrong. */
    @Test fun quizNeverOffersTwoWordsWithTheAnswersDefinition() {
        val all = Words.load(ctx)
        val w = all.first { it.mot == "haruspice" }
        val bad = (0 until 2000).count { s -> Quiz.build(listOf(w), all, kotlin.random.Random(s)).single().options.count { it.definition == w.definition } > 1 }
        assertEquals("seeds offering aruspice AND haruspice for the same definition", 0, bad)
    }

    /** BUG 8: the evening reminder is posted even when today's quiz is already done. */
    @Test fun noQuizReminderOnceTodaysQuizIsDone() {
        while (Words.seenToday(ctx).isEmpty()) Words.random(ctx)
        Quiz.record(Prefs(ctx), 1, 1, LocalDate.now().toEpochDay())
        DailyReceiver().onReceive(ctx, Intent(ctx, DailyReceiver::class.java).setAction(DailyReceiver.ACTION_QUIZ))
        assertTrue(ctx.getSystemService(NotificationManager::class.java).activeNotifications.none { it.id == 50 })
    }

    /** BUG 9 (edge): fall-back night, daily time 02:30, inexact alarm delivered 40 min late (02:10 second pass) -> rescheduled 20 min later the same night. */
    @Test fun fallBackLateDeliveryDoesNotFireTwice() {
        val late = ZonedDateTime.parse("2026-10-25T02:10:00+01:00[Europe/Paris]") // the 02:30+02:00 alarm, 40 min late
        val next = DailyReceiver.nextTrigger(late, 2, 30)
        assertEquals("next show", LocalDate.of(2026, 10, 26), next.toLocalDate())
    }

    /** BUG 6b: the random word shown in the card is lost on rotation (daily word comes back). */
    @Test fun mainCardSurvivesRotation() {
        val c = Robolectric.buildActivity(MainActivity::class.java).setup()
        c.get().findViewById<Button>(R.id.new_word).performClick()
        val shown = c.get().findViewById<TextView>(R.id.mot).text.toString()
        c.recreate()
        assertEquals(shown, c.get().findViewById<TextView>(R.id.mot).text.toString())
    }
}
