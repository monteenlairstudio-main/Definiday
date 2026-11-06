package fr.definiday

import android.app.Notification
import android.app.NotificationManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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
        q.forEach { assertTrue(it.texte, '\'' !in it.texte && !Regex(" [;:!?]").containsMatchIn(it.texte)) } // ’ and U+202F, as words.json
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
        assertTrue(Words.seenToday(ctx).isEmpty()) // picked, not shown yet
        DailyReceiver.show(ctx) // notifications: shown
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
        p.polices = setOf("unifraktur_maguntia") // only a display face kept: words of 8 letters or less keep it, longer ones fall back to serif
        val (short, long) = Words.load(ctx).partition { it.mot.length <= 8 }
        assertNotEquals(android.graphics.Typeface.SERIF, Styles.of(ctx, short.first()).typeface)
        assertEquals(android.graphics.Typeface.SERIF, Styles.of(ctx, long.first()).typeface)
        p.polices = null
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

    @Test fun quizNotificationShowsSleepingGlose() {
        Words.today(ctx); Words.random(ctx)
        Prefs(ctx).unlocked = setOf("soir")
        DailyReceiver().onReceive(ctx, android.content.Intent(ctx, DailyReceiver::class.java).setAction(DailyReceiver.ACTION_QUIZ))
        val n = ctx.getSystemService(NotificationManager::class.java).activeNotifications.single { it.id == 50 }.notification
        val picture = n.extras.getParcelable(Notification.EXTRA_PICTURE, Bitmap::class.java)!!
        assertEquals(512, picture.width); assertEquals(256, picture.height)
        assertTrue(n.getLargeIcon() != null)
        File("build/demo").mkdirs()
        save(picture, File("build/demo/notif_quiz.png"))
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
        val head = Glose.bitmap(Glose.mine(ctx, R.drawable.glose_icone_vd, accessory = false), 128, 128)
        (listOf(Words.today(ctx).first(), words.maxBy { it.definition.length + it.exemple.length }) + Quotes.load(ctx).first { it.texte.length > 120 })
            .forEachIndexed { i, w -> save(WordCard.share(w, Styles.of(ctx, w), head), File(out, "share_$i.png")) }
        listOf(R.drawable.ic_launcher_fg to "icon_fg.png", R.drawable.ic_launcher_mono to "icon_mono.png").forEach { (r, f) ->
            save(Glose.bitmap(android.graphics.drawable.LayerDrawable(arrayOf(android.graphics.drawable.ColorDrawable(ctx.getColor(R.color.icon_bg)), ctx.getDrawable(r)!!)), 432, 432), File(out, f))
        }
        capture(Robolectric.buildActivity(MainActivity::class.java).setup().get(), File(out, "app.png"))
        Prefs(ctx).apply { unlocked = setOf("livres", "nuit", "sauge", "bonnet"); glosePose = "livres"; glosePelage = "nuit"; glosePelote = "sauge"; gloseAcc = "bonnet" }
        capture(Robolectric.buildActivity(MainActivity::class.java).setup().get(), File(out, "app_choice.png"))
        Prefs(ctx).apply { unlocked = emptySet(); glosePose = "assis"; glosePelage = "tigre"; glosePelote = "bordeaux"; gloseAcc = "" }
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
        renderInterface(File(out, "ui").apply { mkdirs() })
    }

    /** Interface captures (app/build/demo/ui): home light/dark, font x1.3 and x2.0, w360dp, Settings, onboarding steps, Couleurs et polices, quiz steps, Collection. */
    private fun renderInterface(ui: File) {
        val p = Prefs(ctx).apply { lastQuizDay = Long.MIN_VALUE; mode = Prefs.MODE_BOTH }
        fun main(key: String? = null) = Robolectric.buildActivity(MainActivity::class.java,
            android.content.Intent(ctx, MainActivity::class.java).putExtra(MainActivity.EXTRA_MOT, key)).setup().get()
        fun quiz() = Robolectric.buildActivity(QuizActivity::class.java).setup().get()
        // Couleurs et polices with a few palettes and fonts removed, so both tile states show
        fun styles(): android.app.Activity {
            val cat = Styles.load(ctx)
            p.palettes = cat.palettes.map { it.id }.filterIndexed { i, _ -> i % 3 != 1 }.toSet(); p.polices = cat.polices.map { it.id }.drop(2).toSet()
            return Robolectric.buildActivity(StylesActivity::class.java).setup().get().also { p.palettes = null; p.polices = null }
        }
        fun views(a: android.app.Activity) = generateSequence(listOf<View>(a.window.decorView)) { l -> l.flatMap { v -> (v as? android.view.ViewGroup)?.let { g -> (0 until g.childCount).map(g::getChildAt) } ?: emptyList() }.ifEmpty { null } }.flatten()
        fun answer(a: android.app.Activity, right: Boolean) {
            val mot = quizAnswer(a)
            views(a).filterIsInstance<android.widget.Button>().first { (it.text == mot) == right }.performClick()
        }
        // onboarding steps: lock screen and quiz chosen, removal at the 1st unlock without exact alarms, so step 4 shows both requests
        fun onboarding(suffix: String) {
            p.unlocks = 1; org.robolectric.shadows.ShadowAlarmManager.setCanScheduleExactAlarms(false)
            (0..3).forEach { n -> capture(Robolectric.buildActivity(OnboardingActivity::class.java).setup().get()
                .also { a -> repeat(n) { a.findViewById<View>(R.id.next).performClick() } }, File(ui, "onboarding_${n + 1}$suffix.png")) }
            p.unlocks = 0; org.robolectric.shadows.ShadowAlarmManager.setCanScheduleExactAlarms(true)
        }
        val long = Words.load(ctx).filter { ' ' !in it.mot && '-' !in it.mot }.maxBy { it.mot.length }.key
        capture(main(), File(ui, "home_light.png"))
        p.palettes = setOf("craie") // palest card on cream: 1 dp edge (Styles.needsEdge)
        capture(main(), File(ui, "home_pale.png"))
        p.palettes = null
        listOf(R.drawable.saison_flocon, R.drawable.saison_feuille, R.drawable.saison_fleur).forEachIndexed { i, r -> save(Glose.bitmap(ctx.getDrawable(r)!!, 144, 144), File(ui, "season_$i.png")) }
        capture(main(Quotes.load(ctx).first { it.texte.length > 120 }.key), File(ui, "home_quote.png"))
        RuntimeEnvironment.setFontScale(1.3f)
        capture(main(), File(ui, "home_font130.png"))
        RuntimeEnvironment.setFontScale(2f)
        capture(main(), File(ui, "home_font200.png"))
        RuntimeEnvironment.setQualifiers("+w360dp-h740dp")
        capture(main(long), File(ui, "home_w360_font200_long.png"))
        capture(Robolectric.buildActivity(SettingsActivity::class.java).setup().get(), File(ui, "settings_w360_font200.png"))
        onboarding("_w360_font200")
        capture(styles(), File(ui, "styles_w360_font200.png"))
        capture(quiz().also { answer(it, right = false) }, File(ui, "quiz_w360_font200_faux.png"))
        RuntimeEnvironment.setFontScale(1.3f)
        capture(main(long), File(ui, "home_w360_font130_long.png"))
        RuntimeEnvironment.setFontScale(1f)
        capture(main(long), File(ui, "home_w360.png"))
        RuntimeEnvironment.setQualifiers("+w411dp-h891dp")
        org.robolectric.Shadows.shadowOf(ctx.getSystemService(NotificationManager::class.java)).setNotificationsEnabled(false)
        capture(main(), File(ui, "home_notifications_off.png"))
        org.robolectric.Shadows.shadowOf(ctx.getSystemService(NotificationManager::class.java)).setNotificationsEnabled(true)
        capture(Robolectric.buildActivity(SettingsActivity::class.java).setup().get(), File(ui, "settings_light.png"))
        onboarding("_light")
        capture(styles(), File(ui, "styles_light.png"))
        capture(quiz(), File(ui, "quiz_question.png"))
        capture(quiz().also { answer(it, right = true) }, File(ui, "quiz_juste.png"))
        capture(quiz().also { answer(it, right = false) }, File(ui, "quiz_faux.png"))
        capture(quizEnd(right = 2), File(ui, "quiz_fin.png"))
        capture(quizEnd(right = 3), File(ui, "quiz_fin_refait.png")) // same day: score not counted
        p.apply { lastQuizDay = Long.MIN_VALUE; passedQuizzes = 4 }
        capture(quizEnd(right = 3), File(ui, "quiz_fin_noter.png")) // 5th passed quiz: the rate card, once
        p.guessFirst = true
        capture(main(), File(ui, "home_deviner.png"))
        p.guessFirst = false
        RuntimeEnvironment.setQualifiers("+night")
        capture(main(), File(ui, "home_dark.png"))
        capture(Robolectric.buildActivity(SettingsActivity::class.java).setup().get(), File(ui, "settings_dark.png"))
        onboarding("_dark")
        capture(styles(), File(ui, "styles_dark.png"))
        capture(quiz().also { answer(it, right = false) }, File(ui, "quiz_dark_faux.png"))
        p.lastQuizDay = Long.MIN_VALUE
        capture(quizEnd(right = 3), File(ui, "quiz_dark_fin.png"))
        p.apply { quizzes = 12; perfectQuizzes = 6; passedQuizzes = 9; streak = 8; lastQuizDay -= 5 }
        capture(Robolectric.buildActivity(CollectionActivity::class.java).setup().get(), File(ui, "collection_dark.png"))
        RuntimeEnvironment.setQualifiers("+notnight")
        capture(Robolectric.buildActivity(CollectionActivity::class.java).setup().get(), File(ui, "collection_light.png"))
        // Mes mots and the review questions: 9 past words, 2 missed twice (fragile), 6 reviewed right and due again tonight, 3 favourites
        val today = java.time.LocalDate.now().toEpochDay()
        val past = Words.load(ctx).shuffled(kotlin.random.Random(7)).take(9)
        past.forEachIndexed { i, w -> History(ctx).mark(w.mot, today - 2 - i * 3) }
        Memory(ctx).run {
            past.take(2).forEach { w -> repeat(2) { answered(w.mot, false, today - 1, today - 10, review = true) } }
            past.drop(2).take(6).forEach { answered(it.mot, true, today - 1, today - 30, review = true) }
            favourites = setOf(past[3].mot, past[5].mot, Words.seenToday(ctx).first().mot)
        }
        p.apply { lastQuizDay = today - 1; streak = 8 }
        fun mots() = Robolectric.buildActivity(MotsActivity::class.java).setup().get()
        capture(mots(), File(ui, "mots_light.png"))
        capture(quiz().also { q -> repeat(Words.seenToday(ctx).size) { answer(q, right = true); views(q).filterIsInstance<android.widget.Button>().first { it.text == ctx.getString(R.string.quiz_next) }.performClick() } },
            File(ui, "quiz_revision.png"))
        val cloze = generateSequence { quiz() }.take(60).first { q -> views(q).any { (it as? android.widget.TextView)?.text == ctx.getString(R.string.quiz_prompt_cloze) } }
        capture(cloze.also { answer(it, right = false) }, File(ui, "quiz_trous_faux.png"))
        RuntimeEnvironment.setFontScale(2f)
        RuntimeEnvironment.setQualifiers("+w360dp-h740dp")
        capture(mots(), File(ui, "mots_w360_font200.png"))
        RuntimeEnvironment.setFontScale(1f)
        RuntimeEnvironment.setQualifiers("+w411dp-h891dp")
        RuntimeEnvironment.setQualifiers("+night")
        capture(mots(), File(ui, "mots_dark.png"))
        RuntimeEnvironment.setQualifiers("+notnight")
    }

    /** Plays the evening quiz with [right] good answers and returns the activity on its score screen. */
    private fun quizEnd(right: Int): android.app.Activity {
        val a = Robolectric.buildActivity(QuizActivity::class.java).setup().get()
        fun views() = generateSequence(listOf<View>(a.window.decorView)) { l -> l.flatMap { v -> (v as? android.view.ViewGroup)?.let { g -> (0 until g.childCount).map(g::getChildAt) } ?: emptyList() }.ifEmpty { null } }.flatten()
        for (i in Words.seenToday(ctx).indices) {
            val answer = quizAnswer(a)
            views().filterIsInstance<android.widget.Button>().first { (it.text == answer) == (i < right) }.performClick()
            views().filterIsInstance<android.widget.Button>().first { it.text == ctx.getString(R.string.quiz_next) }.performClick()
        }
        return a
    }

    /** Draws the activity at its configured width; animations are started then run for 4 s so the word is written. */
    private fun capture(a: android.app.Activity, f: File) {
        val root = a.window.decorView
        val w = (a.resources.configuration.screenWidthDp * a.resources.displayMetrics.density).toInt()
        fun draw(): Bitmap {
            root.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(16000, View.MeasureSpec.AT_MOST))
            root.layout(0, 0, w, root.measuredHeight)
            return Bitmap.createBitmap(w, root.measuredHeight, Bitmap.Config.ARGB_8888).also { root.draw(Canvas(it)) }
        }
        draw()
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(4))
        save(draw(), f)
    }

    private fun save(b: Bitmap, f: File) = f.outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
}
