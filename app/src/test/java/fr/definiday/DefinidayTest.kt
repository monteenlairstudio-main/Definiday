package fr.definiday

import android.app.Notification
import android.app.NotificationManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.ZonedDateTime

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "fr-w411dp-h891dp-xxhdpi")
class DefinidayTest {
    private val ctx = RuntimeEnvironment.getApplication()

    @Test fun datasetIsClean() {
        val words = Words.load(ctx)
        assertTrue("need 3 years without repeat, got ${words.size}", words.size >= 3 * 365)
        assertEquals(words.size, words.map { it.mot.lowercase() }.toSet().size)
        words.forEach { assertTrue(it.mot, it.nature.isNotBlank() && it.nature != "n." && it.definition.length in 1..200 && it.exemple.length <= 120) }
        words.filter { it.anecdote.isNotEmpty() }.forEach {
            assertTrue(it.mot, it.anecdote.length <= 280 && Regex("https://fr\\.(wiktionary|wikipedia)\\.org/.+").matches(it.source))
        }
    }

    /** Simulates 3 years of daily + random picks and asserts the cooldowns. */
    private fun simulate(perDay: Int, randomPerDay: Int, checkRandom: Boolean) {
        val words = Words.load(ctx)
        val history = mutableMapOf<String, Long>()
        val rnd = kotlin.random.Random(1)
        for (day in 0L until 3 * 365) {
            repeat(perDay) { Words.nextDaily(words, history).let { w ->
                history[w.mot]?.let { assertTrue("daily ${w.mot} back after ${day - it} days", day - it >= Words.DAILY_COOLDOWN_DAYS) }
                if (history.size < words.size) assertTrue("${w.mot} repeated while unseen words remain", w.mot !in history)
                history[w.mot] = day
            } }
            repeat(randomPerDay) { Words.nextRandom(words, history, day, perDay, rnd).let { w ->
                if (checkRandom) history[w.mot]?.let { assertTrue("random ${w.mot} back after ${day - it} days", day - it >= Words.RANDOM_COOLDOWN_DAYS) }
                history[w.mot] = day
            } }
        }
    }

    /** 9 months for daily words holds even with 3 daily words and heavy random use. */
    @Test fun dailyWordsStayAwayNineMonths() = simulate(perDay = 3, randomPerDay = 3, checkRandom = false)

    /** With 1 daily word, random picks also keep their 4 months (with 3 daily words that is best effort only). */
    @Test fun randomWordsStayAwayFourMonths() = simulate(perDay = 1, randomPerDay = 2, checkRandom = true)

    @Test fun quoteSlotsFollowTheRatio() {
        fun quotes(perDay: Int, more: Boolean) = (0L until 365).sumOf { d -> (0 until perDay).count { Words.isQuoteSlot(d, it, more) } }
        assertEquals(73, quotes(1, false)) // 1 in 5
        assertEquals(146, quotes(1, true)) // 2 in 5
        assertEquals(219, quotes(3, false))
        val q = Quotes.load(ctx)
        assertTrue(q.size >= 300)
        q.forEach { assertTrue(it.texte, it.texte.length <= 260 && it.source.startsWith("https://fr.wikiquote.org/wiki/")) }
        assertTrue(q.count { it.theme == "lenteur" } >= 10)
        Prefs(ctx).polices = setOf("unifraktur_maguntia") // only a display face kept -> quote falls back to serif
        assertEquals(android.graphics.Typeface.SERIF, Styles.of(ctx, q.first()).typeface)
        Prefs(ctx).polices = null
        DailyReceiver.show(ctx, listOf(q.first())) // a quote renders as notification + lock-screen card
        assertEquals(q.first().signature, ctx.getSystemService(NotificationManager::class.java).activeNotifications.single().notification.extras.getString(Notification.EXTRA_TITLE))
    }

    @Test fun dailyWordsAreStableAndMarkedSeen() {
        val p = Prefs(ctx).apply { wordsPerDay = 2 }
        val first = Words.today(ctx)
        assertEquals(2, first.size)
        assertEquals(first, Words.today(ctx)) // same words all day
        val r = Words.random(ctx)
        assertTrue(r !in first)
        assertEquals(first.filterIsInstance<Word>() + r, Words.seenToday(ctx)) // quotes are not quiz material
        p.wordsPerDay = 3
        assertEquals(first, Words.today(ctx).take(2)) // raising the count keeps the earlier picks
    }

    @Test fun nextTriggerIsTodayOrTomorrow() {
        val now = ZonedDateTime.parse("2026-11-06T09:30:00+01:00[Europe/Paris]")
        assertEquals(now.withHour(10).withMinute(0), DailyReceiver.nextTrigger(now, 10, 0))
        assertEquals(now.plusDays(1).withHour(8).withMinute(0), DailyReceiver.nextTrigger(now, 8, 0))
        assertEquals(now.plusDays(1), DailyReceiver.nextTrigger(now, 9, 30)) // exactly now -> tomorrow
    }

    @Test fun showPostsPublicTimedNotification() {
        Prefs(ctx).durationMinutes = 30
        DailyReceiver.show(ctx)
        val n = ctx.getSystemService(NotificationManager::class.java).activeNotifications.single().notification
        assertEquals(Notification.VISIBILITY_PUBLIC, n.visibility)
        assertEquals(30 * 60_000L, n.timeoutAfter)
        assertEquals(DailyReceiver.texts(Words.today(ctx).single()).first, n.extras.getString(Notification.EXTRA_TITLE))
        Prefs(ctx).wordsPerDay = 3
        DailyReceiver.show(ctx)
        assertEquals(3, ctx.getSystemService(NotificationManager::class.java).activeNotifications.size)
    }

    @Test fun wallpaperIsRemovedOnlyWhenDueAndStillOurs() {
        val p = Prefs(ctx).apply { mode = Prefs.MODE_BOTH; durationMinutes = 15 }
        DailyReceiver.show(ctx)
        assertTrue(p.wallpaperEndAt > System.currentTimeMillis())
        val end = android.content.Intent(ctx, DailyReceiver::class.java).setAction(DailyReceiver.ACTION_END)
        DailyReceiver().onReceive(ctx, end) // early/stale END: keep it
        assertTrue(p.wallpaperEndAt != 0L)
        p.wallpaperEndAt = 1 // now due
        DailyReceiver().onReceive(ctx, end)
        assertEquals(0L, p.wallpaperEndAt)
    }

    @Test fun lockOnlyModePostsNoNotificationAndLeavesAtSecondUnlock() {
        val p = Prefs(ctx).apply { mode = Prefs.MODE_LOCK; unlocks = 2; durationMinutes = 60 }
        DailyReceiver.show(ctx)
        assertEquals(0, ctx.getSystemService(NotificationManager::class.java).activeNotifications.size)
        assertTrue(p.wallpaperEndAt != 0L)
        assertEquals(false, UnlockService.countUnlock(ctx)) // 1st unlock: still there
        assertTrue(p.wallpaperEndAt != 0L)
        assertEquals(true, UnlockService.countUnlock(ctx)) // 2nd unlock: removed before the hour is up
        assertEquals(0L, p.wallpaperEndAt)
    }

    @Test fun notifOnlyModeLeavesTheWallpaperAlone() {
        val p = Prefs(ctx).apply { mode = Prefs.MODE_NOTIF }
        DailyReceiver.show(ctx)
        assertEquals(0L, p.wallpaperEndAt)
        assertEquals(1, ctx.getSystemService(NotificationManager::class.java).activeNotifications.size)
    }

    @Test fun everyWordStyleExistsAndPoolIsHonoured() {
        val cat = Styles.load(ctx)
        cat.polices.forEach { assertTrue(it.id, ctx.resources.getIdentifier(it.fichier, "font", ctx.packageName) != 0) }
        val ids = cat.palettes.map { it.id }.toSet() to cat.polices.map { it.id }.toSet()
        Words.load(ctx).filter { it.palette.isNotEmpty() }.forEach { assertTrue(it.mot, it.palette in ids.first && it.police in ids.second) }
        val w = Words.load(ctx).first()
        val p = Prefs(ctx).apply { palettes = setOf("craie") }
        assertEquals("craie", Styles.of(ctx, w).palette.id) // word's own palette left out -> pool wins
        p.palettes = null
    }

    @Test fun rebootPurgesExpiredWallpaper() {
        val p = Prefs(ctx).apply { mode = Prefs.MODE_BOTH }
        DailyReceiver.show(ctx)
        p.wallpaperEndAt = 1 // expired while the phone was off
        DailyReceiver().onReceive(ctx, android.content.Intent(android.content.Intent.ACTION_BOOT_COMPLETED))
        assertEquals(0L, p.wallpaperEndAt)
    }

    @Test fun rebootReschedulesPendingWallpaperEndAndUnlockWatch() {
        Prefs(ctx).apply { mode = Prefs.MODE_BOTH; unlocks = 2 }
        DailyReceiver.show(ctx)
        val app = org.robolectric.Shadows.shadowOf(ctx)
        while (app.nextStartedService != null) Unit // drop the start done by show()
        DailyReceiver().onReceive(ctx, android.content.Intent(android.content.Intent.ACTION_BOOT_COMPLETED))
        assertEquals(UnlockService::class.java.name, app.nextStartedService?.component?.className)
        val am = org.robolectric.Shadows.shadowOf(ctx.getSystemService(android.app.AlarmManager::class.java))
        assertTrue(am.scheduledAlarms.size >= 2) // daily SHOW + wallpaper END (+ quiz)
    }

    @Test fun quizOffersFourDistinctOptionsAndCountsStreaks() {
        val all = Words.load(ctx)
        val seen = all.take(3)
        Quiz.build(seen, all, kotlin.random.Random(3)).forEach { q ->
            assertEquals(4, q.options.toSet().size)
            assertTrue(q.answer in q.options)
        }
        val p = Prefs(ctx)
        Quiz.record(p, 3, 3, day = 100)
        Quiz.record(p, 3, 3, day = 100) // same day counts once
        Quiz.record(p, 1, 3, day = 101)
        assertEquals(2, p.quizzes); assertEquals(1, p.perfectQuizzes); assertEquals(1, p.passedQuizzes); assertEquals(4, p.correctAnswers); assertEquals(2, p.streak)
        Quiz.record(p, 2, 3, day = 105)
        assertEquals(1, p.streak) // broken streak restarts
    }

    @Test fun quizPassesAtTwoThirds() {
        listOf(1 to 1, 2 to 2, 2 to 3, 3 to 3).forEach { (s, t) -> assertTrue("$s/$t", Quiz.passed(s, t)) }
        listOf(0 to 1, 1 to 2, 1 to 3, 0 to 3).forEach { (s, t) -> assertTrue("$s/$t", !Quiz.passed(s, t)) }
        val p = Prefs(ctx).apply { perfectQuizzes = 4 } // user from before passedQuizzes
        assertEquals(4, p.passedQuizzes)
        Quiz.record(p, 2, 3, day = 200)
        assertEquals(5, p.passedQuizzes); assertEquals(4, p.perfectQuizzes)
        Quiz.record(p, 3, 3, day = 201)
        assertEquals(6, p.passedQuizzes); assertEquals(5, p.perfectQuizzes)
    }

    @Test fun randomPickReplacesTheLockScreenWord() {
        Prefs(ctx).apply { mode = Prefs.MODE_BOTH; wordsPerDay = 3 }
        DailyReceiver.show(ctx)
        val r = Words.random(ctx)
        DailyReceiver.show(ctx, listOf(r))
        val n = ctx.getSystemService(NotificationManager::class.java).activeNotifications.single().notification
        assertEquals(r.mot, n.extras.getString(Notification.EXTRA_TITLE))
    }

    /** Not a check: writes the demo PNGs (lock-screen card + main screen) to app/build/demo. */
    @Test fun renderDemoImages() {
        val out = File("build/demo").apply { mkdirs() }
        val words = Words.load(ctx)
        listOf(Words.today(ctx).first(), words.maxBy { it.definition.length + it.exemple.length }).forEachIndexed { i, w ->
            save(WordCard.render(w, 1080, 2340, Styles.of(ctx, w)), File(out, "lockscreen_$i.png"))
        }
        words.filter { it.mot in setOf("zinzolin", "patibulaire", "bergamasque", "potron-minet", "incunable", "marivaudage") }.forEach { w ->
            save(WordCard.render(w, 540, 1170, Styles.of(ctx, w)), File(out, "style_${w.mot}.png"))
        }
        Quotes.load(ctx).filter { it.auteur.contains("Joubert") || it.auteur.contains("Monselet") }.take(2).forEachIndexed { i, q ->
            save(WordCard.render(q, 540, 1170, Styles.of(ctx, q)), File(out, "quote_$i.png"))
        }
        capture(Robolectric.buildActivity(MainActivity::class.java).setup().get(), File(out, "app.png"))
        Prefs(ctx).apply { quizzes = 12; perfectQuizzes = 6; passedQuizzes = 9; streak = 8 }
        Unlocks.update(Prefs(ctx))
        Prefs(ctx).apply { glosePelage = "creme"; gloseAcc = "echarpe" }
        val col = Robolectric.buildActivity(CollectionActivity::class.java).setup().get()
        capture(col, File(out, "collection.png"))
        Prefs(ctx).apply { quizzes = 0; perfectQuizzes = 0; passedQuizzes = 0; streak = 0; unlocked = emptySet(); announced = emptySet(); gloseAcc = "" }
        while (Words.seenToday(ctx).size < 3) Words.random(ctx) // daily slots may hold quotes, which are not quiz material
        capture(quizEnd(right = 3), File(out, "quiz_bravo.png"))
        Prefs(ctx).lastQuizDay = Long.MIN_VALUE
        capture(quizEnd(right = 1), File(out, "quiz_encore.png"))
    }

    /** Plays the evening quiz with [right] good answers and returns the activity on its score screen. */
    private fun quizEnd(right: Int): android.app.Activity {
        val a = Robolectric.buildActivity(QuizActivity::class.java).setup().get()
        val defs = Words.load(ctx).associateBy { it.definition }
        fun views() = generateSequence(listOf<View>(a.window.decorView)) { l -> l.flatMap { v -> (v as? android.view.ViewGroup)?.let { g -> (0 until g.childCount).map(g::getChildAt) } ?: emptyList() }.ifEmpty { null } }.flatten()
        for (i in Words.seenToday(ctx).indices) {
            val answer = views().filterIsInstance<android.widget.TextView>().firstNotNullOf { defs[it.text.toString()] }
            views().filterIsInstance<android.widget.Button>().first { (it.text == answer.mot) == (i < right) }.performClick()
            views().filterIsInstance<android.widget.Button>().first { it.text == ctx.getString(R.string.quiz_next) }.performClick()
        }
        return a
    }

    /** Draws the activity at phone width; animations are started then run for 4 s so the word is written. */
    private fun capture(a: android.app.Activity, f: File) {
        val root = a.window.decorView
        fun draw(): Bitmap {
            root.measure(View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(8000, View.MeasureSpec.AT_MOST))
            root.layout(0, 0, 1080, root.measuredHeight)
            return Bitmap.createBitmap(1080, root.measuredHeight, Bitmap.Config.ARGB_8888).also { root.draw(Canvas(it)) }
        }
        draw()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(4))
        save(draw(), f)
    }

    private fun save(b: Bitmap, f: File) = f.outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
}
