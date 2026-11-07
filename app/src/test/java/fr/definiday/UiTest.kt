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
        a.findViewById<View>(R.id.settings_open).performClick()
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
        val answer = quizAnswer(a)
        val wrong = buttons(a).first { b -> b.text != answer }
        val wrongMot = wrong.text.toString()
        wrong.performClick()
        assertEquals("✗ $wrongMot", wrong.text.toString())
        assertTrue(buttons(a).any { it.text == "✓ $answer" })
        assertTrue(views(a).filterIsInstance<TextView>().any { // visible, and read by TalkBack as a live region
            it.text == ctx.getString(R.string.quiz_wrong, answer) && it.accessibilityLiveRegion == View.ACCESSIBILITY_LIVE_REGION_POLITE })
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

    /** Pale palettes on cream (craie 1.02:1) and dark ones at night take a 1 dp edge; contrasted ones don't. */
    @Test fun cardEdgeWhenThePaletteMeltsIntoThePage() {
        val cream = android.graphics.Color.parseColor("#FAF7F0")
        val night = android.graphics.Color.parseColor("#0F1424")
        assertEquals(21.0, Styles.contrast(android.graphics.Color.BLACK, android.graphics.Color.WHITE), 0.01)
        val pal = Styles.load(ctx).palettes.associateBy { it.id }
        assertTrue(Styles.needsEdge(pal.getValue("craie"), cream))
        assertTrue(Styles.needsEdge(pal.getValue("sepia"), cream)) // 1.66:1 at the bottom but 1.27:1 at the top
        assertTrue(!Styles.needsEdge(pal.getValue("encre_de_chine"), cream))
        assertTrue(Styles.needsEdge(pal.getValue("encre_de_chine"), night))
        assertTrue(!Styles.needsEdge(pal.getValue("craie"), night))
    }

    /** End-of-quiz unboxing: at most 3 stamps, the rest counted. */
    @Test fun quizStampsShowAtMostThree() {
        assertEquals(listOf(1, 2) to 0, Quiz.stamps(listOf(1, 2)))
        assertEquals(listOf(1, 2, 3) to 0, Quiz.stamps(listOf(1, 2, 3)))
        assertEquals(listOf(3, 4, 5) to 2, Quiz.stamps(listOf(1, 2, 3, 4, 5)))
        // first quiz, all right: 4 items at once (3 poses and the bow tie) -> 3 stamps and "+ 1 dans la collection"
        while (Words.seenToday(ctx).isEmpty()) Words.random(ctx)
        val a = Robolectric.buildActivity(QuizActivity::class.java).setup().get()
        repeat(Words.seenToday(ctx).size) {
            val mot = quizAnswer(a)
            buttons(a).first { it.text == mot }.performClick()
            buttons(a).first { it.text == ctx.getString(R.string.quiz_next) }.performClick()
        }
        val texts = views(a).filterIsInstance<TextView>().map { it.text.toString() }.toList()
        val fresh = Unlocks.items.filter { it.id in Prefs(ctx).announced }
        assertEquals(4, fresh.size)
        assertEquals(3, fresh.count { it.nom in texts })
        assertTrue(ctx.getString(R.string.unlock_more, 1) in texts)
        assertTrue(buttons(a).any { it.text == ctx.getString(R.string.collection_see) })
    }

    /** « Partager »: a chooser over ACTION_SEND with the text (word, nature, definition, example, « via Definiday ») and the card
     *  picture served read-only by our provider; a quote shares its author and work. */
    @Test fun shareSendsTextAndCardPicture() {
        val w = Words.load(ctx).first { it.exemple.isNotEmpty() }
        val a = Robolectric.buildActivity(MainActivity::class.java, Intent(ctx, MainActivity::class.java).putExtra(MainActivity.EXTRA_MOT, w.key)).setup().get()
        a.findViewById<View>(R.id.share).performClick()
        val chooser = shadowOf(a).nextStartedActivity
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        @Suppress("DEPRECATION") val send = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
        assertEquals(Intent.ACTION_SEND, send.action)
        val text = send.getStringExtra(Intent.EXTRA_TEXT)!!
        listOf(w.mot, w.nature, w.definition, w.exemple, "via Definiday").forEach { assertTrue(it, it in text) }
        @Suppress("DEPRECATION") val uri = send.getParcelableExtra<android.net.Uri>(Intent.EXTRA_STREAM)!!
        assertEquals("image/png", send.type)
        assertEquals("fr.definiday.dev.share", uri.authority) // ${applicationId}.share: the test build never shares the real app's
        assertTrue(send.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        assertEquals(uri, send.clipData!!.getItemAt(0).uri)
        val provider = Robolectric.setupContentProvider(ShareProvider::class.java, ShareProvider.authority(ctx))
        val png = android.os.ParcelFileDescriptor.AutoCloseInputStream(provider.openFile(uri, "r")).use { android.graphics.BitmapFactory.decodeStream(it) }
        assertEquals(1080 to 1350, png.width to png.height)
        val q = Quotes.load(ctx).first { it.oeuvre.isNotEmpty() }
        val qt = ShareProvider.text(ctx, q)
        assertTrue(q.texte in qt && q.auteur in qt && q.oeuvre in qt)
    }

    /** Rate card: once, from the 5th passed quiz, only after a passed quiz that counted. */
    @Test fun rateCardOnceAfterTheFifthPassedQuiz() {
        val p = Prefs(ctx).apply { passedQuizzes = 4 }
        assertTrue(!Quiz.askRate(p, passed = true, counted = true))
        p.passedQuizzes = 5
        assertTrue(!Quiz.askRate(p, passed = false, counted = true)) // never after a failed quiz
        assertTrue(!Quiz.askRate(p, passed = true, counted = false)) // nor after a redone one
        assertTrue(Quiz.askRate(p, passed = true, counted = true))
        assertTrue(!Quiz.askRate(Prefs(ctx).apply { passedQuizzes = 9 }, passed = true, counted = true)) // only once
        // on the score screen: shown after the 5th passed quiz, « Plus tard » folds it
        p.apply { rateAsked = false; passedQuizzes = 4; lastQuizDay = Long.MIN_VALUE }
        while (Words.seenToday(ctx).isEmpty()) Words.random(ctx)
        val q = Robolectric.buildActivity(QuizActivity::class.java).setup().get()
        repeat(Words.seenToday(ctx).size) { // a question may be the definition or a blanked example: quizAnswer() reads both
            val answer = quizAnswer(q)
            buttons(q).first { it.text == answer }.performClick()
            buttons(q).first { it.text == ctx.getString(R.string.quiz_next) }.performClick()
        }
        val rate = views(q).filterIsInstance<TextView>().single { it.text == ctx.getString(R.string.rate_text) }
        buttons(q).first { it.text == ctx.getString(R.string.rate_later) }.performClick()
        assertTrue(!rate.isShown)
    }

    /** « Deviner avant de voir » (off by default): the definition waits under « Touchez pour révéler », and stays revealed on resume. */
    @Test fun guessFirstHidesTheDefinitionUntilTouched() {
        assertTrue(!Prefs(ctx).guessFirst)
        val s = Robolectric.buildActivity(SettingsActivity::class.java).setup().get()
        s.findViewById<android.widget.Switch>(R.id.guess).performClick()
        assertTrue(Prefs(ctx).guessFirst)
        val w = Words.load(ctx).first { it.exemple.isNotEmpty() }
        val c = Robolectric.buildActivity(MainActivity::class.java, Intent(ctx, MainActivity::class.java).putExtra(MainActivity.EXTRA_MOT, w.key)).setup()
        val a = c.get()
        val reveal = a.findViewById<Button>(R.id.reveal)
        assertEquals(View.INVISIBLE, a.findViewById<View>(R.id.definition).visibility)
        assertEquals(View.GONE, a.findViewById<View>(R.id.exemple_box).visibility)
        assertEquals(View.VISIBLE, reveal.visibility)
        assertEquals(ctx.getString(R.string.reveal_desc), reveal.contentDescription)
        reveal.performClick()
        assertEquals(View.VISIBLE, a.findViewById<View>(R.id.definition).visibility)
        assertEquals(View.VISIBLE, a.findViewById<View>(R.id.exemple_box).visibility)
        assertEquals(View.GONE, reveal.visibility)
        c.pause().resume()
        assertEquals(View.VISIBLE, a.findViewById<View>(R.id.definition).visibility)
        a.findViewById<Button>(R.id.new_word).performClick() // a new word: hidden again
        assertEquals(View.VISIBLE, reveal.visibility)
        Prefs(ctx).guessFirst = false
    }

    @Test fun seasonalPropByMonth() {
        assertEquals(listOf(R.drawable.saison_flocon, R.drawable.saison_flocon, R.drawable.saison_fleur, R.drawable.saison_feuille, 0),
            listOf(12, 2, 4, 11, 7).map(MainActivity::season))
    }

    /** Couleurs et polices: a tap removes or keeps a tile at once, the last one stays kept, Toutes = null, the pool survives recreation. */
    @Test fun stylesTilesToggleThePool() {
        val p = Prefs(ctx).apply { palettes = null; polices = null }
        val settings = Robolectric.buildActivity(SettingsActivity::class.java).setup().get()
        settings.findViewById<Button>(R.id.styles_open).performClick()
        assertEquals(StylesActivity::class.java.name, shadowOf(settings).nextStartedActivity.component!!.className)
        val cat = Styles.load(ctx)
        val ctrl = Robolectric.buildActivity(StylesActivity::class.java).setup()
        fun tile(nom: String) = views(ctrl.get()).filterIsInstance<android.widget.CheckBox>().first { it.contentDescription.startsWith("$nom,") }
        assertEquals(cat.palettes.size + cat.polices.size, views(ctrl.get()).count { it is android.widget.CheckBox })
        assertEquals("Braise, palette sombre", tile("Braise").contentDescription)
        tile("Braise").performClick()
        assertEquals(cat.palettes.map { it.id }.toSet() - "braise", p.palettes)
        assertTrue(!tile("Braise").isChecked)
        tile("Braise").performClick()
        assertEquals(null, p.palettes) // all kept again: stored as null
        ctrl.get().findViewById<Button>(R.id.palettes_none).performClick()
        val own = (Words.today(ctx).filterIsInstance<Word>().firstOrNull() ?: Words.load(ctx).first()).palette
            .takeIf { id -> cat.palettes.any { it.id == id } } ?: cat.palettes.first().id
        assertEquals(setOf(own), p.palettes) // « Aucune » keeps the word's own palette
        val last = cat.palettes.first { it.id == own }.nom
        tile(last).performClick()
        assertEquals(setOf(own), p.palettes) // the last one cannot be removed
        assertTrue(tile(last).isChecked)
        assertEquals(ctx.getString(R.string.pool_min, last), org.robolectric.shadows.ShadowToast.getTextOfLatestToast())
        tile("Cinzel").performClick()
        ctrl.recreate() // rotation: read back from Prefs
        assertTrue(!tile("Cinzel").isChecked && tile(last).isChecked && tile("Braise").isChecked == (own == "braise"))
        assertEquals(ctx.resources.getQuantityString(R.plurals.pool_count, 1, 1, cat.palettes.size), ctrl.get().findViewById<TextView>(R.id.palettes_count).text)
        ctrl.get().findViewById<Button>(R.id.polices_all).performClick()
        assertEquals(null, p.polices)
        p.palettes = null
    }
}
