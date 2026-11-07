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

/** Each test reproduced one bug found at 442fcaf (they failed on that commit); kept as non-regression tests. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "fr-w411dp-h891dp-xxhdpi")
class BugHuntTest {
    private val ctx = RuntimeEnvironment.getApplication()

    private fun views(a: android.app.Activity) = generateSequence(listOf<View>(a.window.decorView)) { l ->
        l.flatMap { v -> (v as? android.view.ViewGroup)?.let { g -> (0 until g.childCount).map(g::getChildAt) } ?: emptyList() }.ifEmpty { null }
    }.flatten()
    private fun texts(a: android.app.Activity) = views(a).filterIsInstance<TextView>().map { it.text.toString() }.toList()
    /** The quiz progress (« Question 2 sur 4 », with drawn stamps): its TalkBack text is read. */
    private fun progress(a: android.app.Activity) = views(a).mapNotNull { it.contentDescription?.toString() }.toList()
    /** Answers the current question (first option) and goes to the next one. */
    private fun answerOne(a: android.app.Activity) {
        val mot = quizAnswer(a)
        views(a).filterIsInstance<Button>().first { it.text == mot }.performClick()
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
        assertTrue(ctx.getString(R.string.quiz_progress, 2, total) in progress(c.get()))
        c.recreate()
        assertTrue("after rotation: ${progress(c.get())}", ctx.getString(R.string.quiz_progress, 2, total) in progress(c.get()))
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

    /** Streak unlocks (soir 3, écharpe 7, bonnet 14, nuit 30) stay reachable with 1 word a day and "Plus de citations" (2 quote days in 5). */
    @Test fun moreQuotesStillReachesStreakUnlocks() {
        val p = Prefs(ctx).apply { moreQuotes = true }
        var best = 0
        for (d in 20_000L until 20_365L) if (!Words.quotesOnly(d, 1, true)) { Quiz.record(p, 1, 1, d); best = maxOf(best, p.streak) }
        assertTrue("best = $best", best >= 30)
        Quiz.record(p, 1, 1, 20_400) // a real gap still breaks it
        assertEquals(1, p.streak)
    }

    /** The 9 pm reminder tapped after midnight opens the eve's quiz (not an empty one) and records it for the eve. */
    @Test fun reminderTappedAfterMidnightOpensTheEvesQuiz() {
        val tz = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Etc/GMT+12"))
            val d = LocalDate.now().toEpochDay()
            while (Words.seenToday(ctx).isEmpty()) Words.random(ctx)
            val n = Words.seenToday(ctx).size
            DailyReceiver().onReceive(ctx, Intent(ctx, DailyReceiver::class.java).setAction(DailyReceiver.ACTION_QUIZ))
            val tap = shadowOf(ctx.getSystemService(NotificationManager::class.java).activeNotifications.single { it.id == 50 }.notification.contentIntent).savedIntent
            TimeZone.setDefault(TimeZone.getTimeZone("Etc/GMT-12")) // past midnight
            val a = Robolectric.buildActivity(QuizActivity::class.java, tap).setup().get()
            assertTrue(texts(a).toString(), ctx.getString(R.string.quiz_progress, 1, n) in progress(a) && ctx.getString(R.string.quiz_eve) in texts(a))
            repeat(n) { answerOne(a) }
            assertEquals(d, Prefs(ctx).lastQuizDay)
        } finally { TimeZone.setDefault(tz) }
    }

    /** Answering then rotating does not let the same question be scored twice. */
    @Test fun answeredQuestionStaysAnsweredOnRotation() {
        while (Words.seenToday(ctx).isEmpty()) Words.random(ctx)
        val c = Robolectric.buildActivity(QuizActivity::class.java).setup()
        val mot = quizAnswer(c.get())
        views(c.get()).filterIsInstance<Button>().first { it.text == mot }.performClick()
        c.recreate()
        assertTrue(views(c.get()).filterIsInstance<Button>().filter { it.text != ctx.getString(R.string.quiz_next) }.none { it.isEnabled })
        assertTrue(views(c.get()).filterIsInstance<Button>().any { it.text == ctx.getString(R.string.quiz_next) })
    }

    /** Lock-only mode with 3 words: only the wallpaper's word is seen; raising the count mid-day burns nothing until shown. */
    @Test fun onlyReallyShownItemsAreSeen() {
        val p = Prefs(ctx).apply { mode = Prefs.MODE_LOCK; wordsPerDay = 3 }
        val today = Words.today(ctx)
        DailyReceiver.show(ctx)
        val day = LocalDate.now().toEpochDay()
        assertEquals(listOf(today.first().key), History(ctx).all().filterValues { it == day }.keys.toList())
        p.mode = Prefs.MODE_NOTIF; p.wordsPerDay = 1
        DailyReceiver.show(ctx)
        p.wordsPerDay = 3; Words.today(ctx) // two more picked mid-day, not shown
        assertEquals(1, History(ctx).all().count { it.value == day })
        Robolectric.buildActivity(MainActivity::class.java).setup() // the card shows the first
        assertEquals(1, History(ctx).all().count { it.value == day })
    }

    /** Granting "Alarms & reminders" reschedules the daily alarm as exact; the settings intent is offered only while denied. */
    @Test fun exactAlarmAccessIsRequestedThenUsed() {
        val am = ctx.getSystemService(android.app.AlarmManager::class.java)
        org.robolectric.shadows.ShadowAlarmManager.setCanScheduleExactAlarms(false)
        assertEquals(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Alarms.requestIntent(ctx)!!.action)
        assertEquals("package:${ctx.packageName}", Alarms.requestIntent(ctx)!!.dataString)
        DailyReceiver.scheduleNext(ctx)
        assertTrue(shadowOf(am).scheduledAlarms.single().windowLengthMs != 0L) // inexact
        org.robolectric.shadows.ShadowAlarmManager.setCanScheduleExactAlarms(true)
        assertEquals(null, Alarms.requestIntent(ctx))
        DailyReceiver().onReceive(ctx, Intent(android.app.AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED))
        val show = shadowOf(am).scheduledAlarms.first { shadowOf(it.operation).savedIntent.action == DailyReceiver.ACTION_SHOW }
        assertEquals(0L, show.windowLengthMs) // exact
    }

    /** UnlockService itself: counts USER_PRESENT, removes the word at the Nth unlock and stops; its notification shows N. */
    @Test fun unlockServiceCountsUnlocks() {
        val p = Prefs(ctx).apply { mode = Prefs.MODE_LOCK; unlocks = 2; durationMinutes = 60 }
        DailyReceiver.show(ctx)
        val c = Robolectric.buildService(UnlockService::class.java).create().startCommand(0, 1)
        assertTrue(shadowOf(c.get()).lastForegroundNotification.extras.getString(android.app.Notification.EXTRA_TEXT)!!.contains("2"))
        fun unlock() { ctx.sendBroadcast(Intent(Intent.ACTION_USER_PRESENT)); shadowOf(android.os.Looper.getMainLooper()).idle() }
        unlock()
        assertEquals(1, p.unlocksSeen); assertTrue(p.wallpaperEndAt != 0L); assertTrue(!shadowOf(c.get()).isStoppedBySelf)
        unlock()
        assertEquals(0L, p.wallpaperEndAt); assertTrue(shadowOf(c.get()).isStoppedBySelf)
        c.destroy()
    }

    /** Switching from "Après la durée" to 1-3 unlocks while the word is shown starts the watch; back to 0 stops it. */
    @Test fun unlockSettingChangeSyncsTheWatch() {
        val p = Prefs(ctx).apply { mode = Prefs.MODE_LOCK; unlocks = 0; durationMinutes = 60 }
        DailyReceiver.show(ctx)
        assertEquals(null, shadowOf(ctx).nextStartedService)
        p.unlocks = 1; UnlockService.sync(ctx)
        assertEquals(UnlockService::class.java.name, shadowOf(ctx).nextStartedService.component!!.className)
        p.unlocks = 0; UnlockService.sync(ctx)
        assertEquals(UnlockService::class.java.name, shadowOf(ctx).nextStoppedService.component!!.className)
    }

    /** Restored or tampered prefs (wrong types, out-of-range values, wrong-kind Glose ids, junk history) never crash and read as defaults. */
    @Test fun tamperedPrefsFallBackToSafeDefaults() {
        ctx.getSharedPreferences("definiday", 0).edit()
            .putInt("wordsPerDay", 1_000_000).putInt("hour", 25).putString("minute", "x").putInt("quizHour", -1).putInt("quizMinute", 99)
            .putInt("mode", 9).putInt("unlocks", 7).putInt("duration", -5).putString("enabled", "oui").putString("streak", "beaucoup")
            .putStringSet("unlocked", setOf("nuit", "soir", "noeud")).putString("glosePose", "nuit").putString("glosePelage", "soir")
            .putString("glosePelote", "noeud").putString("gloseAcc", "tigre").putString("dailyMots", "[1,2").putInt("dailyDay", 3)
            .commit()
        ctx.getSharedPreferences("history", 0).edit().putString("abaque", "hier").putInt("q:x", 2).commit()
        val p = Prefs(ctx)
        assertEquals(listOf(1, 8, 0, 21, 0, Prefs.MODE_NOTIF, 0, 120, 0), listOf(p.wordsPerDay, p.hour, p.minute, p.quizHour, p.quizMinute, p.mode, p.unlocks, p.durationMinutes, p.streak))
        assertTrue(p.enabled)
        // "nuit" is an unlocked PELAGE stored as the pose: getDrawable(0) used to crash every launch
        assertEquals(mapOf(Unlocks.Kind.POSE to "assis", Unlocks.Kind.PELAGE to "tigre", Unlocks.Kind.PELOTE to "bordeaux") + Unlocks.ACC.associateWith { "" }, Glose.choice(p))
        assertEquals(R.drawable.glose_assis, Glose.pose(mapOf(Unlocks.Kind.POSE to "nuit")))
        assertTrue(History(ctx).all().isEmpty())
        Robolectric.buildActivity(MainActivity::class.java).setup()
        listOf(Intent.ACTION_BOOT_COMPLETED, DailyReceiver.ACTION_SHOW, DailyReceiver.ACTION_QUIZ).forEach {
            DailyReceiver().onReceive(ctx, Intent(ctx, DailyReceiver::class.java).setAction(it))
        }
        Robolectric.buildActivity(QuizActivity::class.java).setup()
        Robolectric.buildActivity(CollectionActivity::class.java).setup()
        assertEquals(1, Words.today(ctx).size)
    }

    /** dailyMots keys may contain the old "|" separator; the old "a|b" format still reads. */
    @Test fun dailyMotsKeepsAnyKey() {
        val p = Prefs(ctx)
        p.dailyMots = listOf("q:Oui | non", "abaque")
        assertEquals(listOf("q:Oui | non", "abaque"), p.dailyMots)
        ctx.getSharedPreferences("definiday", 0).edit().putString("dailyMots", "abaque|q:x").commit()
        assertEquals(listOf("abaque", "q:x"), p.dailyMots)
    }

    /** A notification tapped while the app is open (singleTop): onNewIntent shows that item, here an earlier day's quote. */
    @Test fun notificationTappedWhileOpenShowsItsItem() {
        val c = Robolectric.buildActivity(MainActivity::class.java).setup()
        val q = Quotes.load(ctx).last()
        History(ctx).mark(q.key, java.time.LocalDate.now().toEpochDay() - 1) // seen on an earlier day: the extra opens only such items or today's (S4)
        c.newIntent(Intent(ctx, MainActivity::class.java).putExtra(MainActivity.EXTRA_MOT, q.key))
        assertEquals("«\u00a0${q.texte}\u00a0»", c.get().findViewById<TextView>(R.id.mot).text.toString())
    }

    /** MainActivity.show, quote branch: quote text and signature, Wikiquote link, no Académie button. */
    @Test fun mainScreenShowsADailyQuote() {
        val q = Quotes.load(ctx).first()
        Prefs(ctx).apply { dailyDay = LocalDate.now().toEpochDay(); dailyMots = listOf(q.key) }
        val a = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        assertEquals("«\u00a0${q.texte}\u00a0»", a.findViewById<TextView>(R.id.mot).text.toString())
        assertEquals(q.signature, a.findViewById<TextView>(R.id.nature).text.toString())
        assertEquals(ctx.getString(R.string.quote_label), a.findViewById<TextView>(R.id.label).text.toString())
        assertEquals(View.GONE, a.findViewById<View>(R.id.academie).visibility)
        assertEquals(View.GONE, a.findViewById<View>(R.id.anecdote_box).visibility)
        assertEquals(ctx.getString(R.string.action_source), a.findViewById<Button>(R.id.cnrtl).text.toString())
        assertTrue(History(ctx).all()[q.key] == LocalDate.now().toEpochDay()) // shown on the card
    }

    /** warnIfHurried: past 3 random picks a day, one "take your time" dialog, once. */
    @Test fun fourthRandomPickWarnsOnce() {
        val a = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val random = a.findViewById<Button>(R.id.new_word)
        repeat(3) { random.performClick() }
        assertEquals(0, org.robolectric.shadows.ShadowDialog.getShownDialogs().size)
        random.performClick()
        assertEquals(1, org.robolectric.shadows.ShadowDialog.getShownDialogs().size)
        assertEquals(ctx.getString(R.string.slow_title), shadowOf(org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog()).title.toString())
        repeat(2) { random.performClick() }
        assertEquals(1, org.robolectric.shadows.ShadowDialog.getShownDialogs().size)
    }

    private fun card(a: android.app.Activity) = a.findViewById<TextView>(R.id.mot).text.toString() to a.findViewById<TextView>(R.id.label).text.toString()
    private fun card(i: Item) = if (i is Word) i.mot to ctx.getString(R.string.today) else "« ${(i as Quote).texte} »" to ctx.getString(R.string.quote_label)

    /** N1: the app left open (or restored) past midnight: yesterday's word of the day gives way to today's and is not marked seen again;
     *  a random pick left on the card stays, not marked seen again either. */
    @Test fun newDayWhileOpenShowsTodaysWordAndKeepsYesterdaysHistory() {
        val tz = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Etc/GMT+12")) // UTC-12: day D
            val d = LocalDate.now().toEpochDay()
            val c = Robolectric.buildActivity(MainActivity::class.java).setup()
            val old = Words.today(ctx).first()
            val saved = android.os.Bundle().also { c.saveInstanceState(it) }
            val r = Robolectric.buildActivity(MainActivity::class.java).setup()
            r.get().findViewById<Button>(R.id.new_word).performClick()
            val random = card(r.get()).first
            r.pause(); c.pause()
            TimeZone.setDefault(TimeZone.getTimeZone("Etc/GMT-12")) // same instant seen 24 h later: D+1
            c.resume(); r.resume()
            val now = Words.today(ctx).first()
            assertTrue(now.key != old.key)
            assertEquals(card(now), card(c.get()))
            assertEquals(card(now), card(Robolectric.buildActivity(MainActivity::class.java).setup(saved).get())) // process death, restored
            assertEquals(d, History(ctx).all()[old.key]) // not back in tonight's quiz, cooldown not restarted
            assertEquals(d + 1, History(ctx).all()[now.key])
            assertEquals(random to ctx.getString(R.string.random), card(r.get()))
            assertEquals(d, History(ctx).all()[random])
        } finally { TimeZone.setDefault(tz) }
    }

    /** S4: MainActivity is exported: an extra naming a word neither of today's nor ever seen shows today's word, History untouched;
     *  a word already seen (« Mes mots », an earlier notification) still opens, « Déjà vu ». */
    @Test fun foreignExtraCannotMarkAWordSeen() {
        fun open(key: String) = Intent(ctx, MainActivity::class.java).putExtra(MainActivity.EXTRA_MOT, key)
        val today = Words.today(ctx).first()
        val (stranger, seen) = Words.load(ctx).filter { it.key != today.key }.take(2)
        History(ctx).mark(seen.key, LocalDate.now().toEpochDay() - 3)
        val c = Robolectric.buildActivity(MainActivity::class.java, open(stranger.key)).setup()
        assertEquals(card(today), card(c.get()))
        c.newIntent(open(stranger.key))
        assertEquals(card(today), card(c.get()))
        assertEquals(null, History(ctx).all()[stranger.key])
        val a = Robolectric.buildActivity(MainActivity::class.java, open(seen.key).putExtra(MainActivity.EXTRA_DEJA_VU, true)).setup().get()
        assertEquals(seen.mot to ctx.getString(R.string.deja_vu), card(a))
    }

    /** N2 (audit FON 1): rotating the score screen lost the unlocks (« a gagné », stamps) and the rate card for good, and played the chime. */
    @Test fun scoreScreenSurvivesRotation() {
        val p = Prefs(ctx).apply { sounds = true; passedQuizzes = 4 } // this quiz is the 5th passed one: the rate card shows
        ctx.getSystemService(android.media.AudioManager::class.java).ringerMode = android.media.AudioManager.RINGER_MODE_NORMAL
        while (Words.seenToday(ctx).isEmpty()) Words.random(ctx)
        val c = Robolectric.buildActivity(QuizActivity::class.java).setup()
        try {
            repeat(Words.seenToday(ctx).size) { answerOne(c.get()) }
            val end = listOf(ctx.getString(R.string.unlock_new, p.gloseName), Unlocks.items.last { it.id in p.announced }.nom, ctx.getString(R.string.rate_text))
            assertTrue(texts(c.get()).containsAll(end))
            shadowOf(Sounds.pool).clearPlayed()
            c.recreate()
            assertTrue("after rotation: ${texts(c.get())}", texts(c.get()).containsAll(end))
            runCatching { shadowOf(Sounds.pool).notifyResourceLoaded(Sounds.CHIME, true) } // a chime asked again would play now (throws: never loaded)
            assertTrue(Sounds.ALL.none { shadowOf(Sounds.pool).wasResourcePlayed(it) })
        } finally { Sounds.pool?.release(); Sounds.pool = null }
    }
}
