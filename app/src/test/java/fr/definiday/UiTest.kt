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

    /** N3: lock-only mode over the user's own lock wallpaper sends the word by notification (lockBlocked): refused, the banner says so. */
    @Test fun bannerWhenTheLockScreenIsBlockedAndNotificationsAreOff() {
        Prefs(ctx).apply { mode = Prefs.MODE_LOCK; lockBlocked = true }
        shadowOf(nm).setNotificationsEnabled(false)
        assertEquals(View.VISIBLE, Robolectric.buildActivity(MainActivity::class.java).setup().get().findViewById<View>(R.id.notif_banner).visibility)
    }

    /** B3: right and wrong are marked by a check / cross icon in the box's place (the word does not move) and said to TalkBack,
     *  the other boxes go ink2 (no plume box on a disabled option), and the tint keeps the button's rounded shape. */
    @Test fun quizMarksAnswersInText() {
        while (Words.seenToday(ctx).isEmpty()) Words.random(ctx)
        val a = Robolectric.buildActivity(QuizActivity::class.java).setup().get()
        val answer = quizAnswer(a)
        val wordX = buttons(a).map { it.compoundPaddingStart }.distinct().single()
        val wrong = buttons(a).first { b -> b.text != answer }
        val wrongMot = wrong.text.toString()
        wrong.performClick()
        val options = buttons(a).filter { it.text != ctx.getString(R.string.quiz_next) }
        assertEquals(4, options.size)
        assertEquals(listOf(wordX), options.map { it.compoundPaddingStart }.distinct()) // every word starts at the same x
        assertEquals(wrongMot, wrong.text.toString())
        assertEquals(ctx.getString(R.string.quiz_answer_picked, wrongMot), wrong.contentDescription)
        val right = options.single { it.text == answer }
        assertEquals(ctx.getString(R.string.quiz_answer_right, answer), right.contentDescription)
        assertEquals(R.drawable.ic_check, shadowOf(right.compoundDrawablesRelative[0]).createdFromResId)
        assertEquals(R.drawable.ic_cross, shadowOf(wrong.compoundDrawablesRelative[0]).createdFromResId)
        (options - right - wrong).forEach { // their box stays, disabled: ink2 (quiz_box_edge, drawn in DefinidayTest)
            assertEquals(null, it.contentDescription)
            assertEquals(R.drawable.quiz_box, shadowOf(it.compoundDrawablesRelative[0]).createdFromResId)
            assertTrue(!it.isEnabled)
        }
        assertTrue(java.io.File("src/main/java/fr/definiday/QuizActivity.kt").readText().none { it == '✓' || it == '✗' })
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

    /** P19: the menus read their labels from string-array resources, unchanged; the unlocks help shows only when the word leaves at
     *  an unlock, and says which one (« 2e »). */
    @Test fun settingsMenusAndUnlocksHint() {
        Prefs(ctx).apply { mode = Prefs.MODE_BOTH; unlocks = 0; durationMinutes = 90 }
        val a = Robolectric.buildActivity(SettingsActivity::class.java).setup().get()
        fun labels(id: Int) = a.findViewById<Spinner>(id).adapter.let { ad -> (0 until ad.count).map { ad.getItem(it).toString() } }
        assertEquals(listOf("1 mot", "2 mots", "3 mots"), labels(R.id.per_day))
        assertEquals(listOf("Notification et écran de verrouillage", "Notification seulement", "Écran de verrouillage seulement"), labels(R.id.mode))
        assertEquals(listOf("Après la durée choisie", "Au 1er déverrouillage", "Au 2e déverrouillage", "Au 3e déverrouillage"), labels(R.id.unlocks))
        assertEquals(listOf("En bas (laisse la place à l’horloge)", "Au milieu"), labels(R.id.text_pos))
        assertEquals(listOf("15 min", "30 min", "1 h", "1 h 30", "2 h", "4 h", "8 h", "12 h", "24 h"), labels(R.id.duration))
        val hint = a.findViewById<TextView>(R.id.unlocks_hint)
        assertEquals(View.GONE, hint.visibility) // « Après la durée choisie »: no notification to explain
        pick(a.findViewById(R.id.unlocks), 2)
        assertEquals(View.VISIBLE, hint.visibility)
        assertEquals("Pour guetter le 2e déverrouillage, une petite notification reste affichée. Le mot part quand même au bout de la durée choisie.", hint.text.toString())
        pick(a.findViewById(R.id.unlocks), 1)
        assertTrue(hint.text.startsWith("Pour guetter le 1er déverrouillage,"))
        pick(a.findViewById(R.id.mode), Prefs.MODE_NOTIF)
        assertEquals(View.GONE, hint.visibility)
        pick(a.findViewById(R.id.mode), Prefs.MODE_LOCK)
        assertEquals(View.VISIBLE, hint.visibility)
        pick(a.findViewById(R.id.unlocks), 0)
        assertEquals(View.GONE, hint.visibility)
    }

    /** P17: a quiz reminder set before the word's time never comes (no word seen yet): Settings says so under the quiz time. */
    @Test fun quizBeforeTheWordIsExplained() {
        Prefs(ctx).apply { enabled = true; hour = 8; minute = 0; quizEnabled = true; quizHour = 21; quizMinute = 0 }
        val a = Robolectric.buildActivity(SettingsActivity::class.java).setup().get()
        val hint = a.findViewById<TextView>(R.id.quiz_early)
        assertEquals(View.GONE, hint.visibility)
        a.findViewById<Button>(R.id.quiz_time).performClick()
        (org.robolectric.shadows.ShadowDialog.getLatestDialog() as android.app.TimePickerDialog).apply { updateTime(7, 0) }
            .getButton(android.content.DialogInterface.BUTTON_POSITIVE).performClick()
        assertEquals(7, Prefs(ctx).quizHour)
        assertEquals(View.VISIBLE, hint.visibility)
        assertEquals(ctx.getString(R.string.quiz_early), hint.text.toString())
        a.findViewById<android.widget.Switch>(R.id.quiz_enabled).performClick() // no quiz reminder: nothing to warn about
        assertEquals(View.GONE, hint.visibility)
        Prefs(ctx).apply { quizEnabled = true; quizHour = 8 } // same minute as the word: the order of the two alarms is not guaranteed
        assertEquals(View.VISIBLE, Robolectric.buildActivity(SettingsActivity::class.java).setup().get().findViewById<View>(R.id.quiz_early).visibility)
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

    /** N4: a duration outside the list (restored profile) is kept and shown, not replaced by the first choice. */
    @Test fun settingsKeepADurationOutsideTheList() {
        Prefs(ctx).durationMinutes = 90
        val a = Robolectric.buildActivity(SettingsActivity::class.java).setup().get()
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        assertEquals(90, Prefs(ctx).durationMinutes)
        assertEquals("1 h 30", a.findViewById<android.widget.Spinner>(R.id.duration).selectedItem)
    }

    /** I8 / A14: the longest word, small screen, font x2: the headword shrinks down to 1.3 x the definition at most;
     *  if it still does not hold on one line, it breaks between syllables (never anywhere). */
    @Test @Config(qualifiers = "fr-w360dp-h740dp-xxhdpi") fun longWordFitsAtLargeFont() {
        RuntimeEnvironment.setFontScale(2f)
        val w = Words.load(ctx).maxBy { it.mot.length * (if (' ' in it.mot || '-' in it.mot) 0 else 1) }
        History(ctx).mark(w.key, java.time.LocalDate.now().toEpochDay() - 1) // seen on an earlier day: the extra opens only such items or today's (S4)
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
        History(ctx).mark(w.key, java.time.LocalDate.now().toEpochDay() - 1) // seen on an earlier day: the extra opens only such items or today's (S4)
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
        while (Words.seenToday(ctx).isEmpty()) Words.random(ctx)
        fun fifth() = Robolectric.buildActivity(QuizActivity::class.java).also { p.apply { rateAsked = false; passedQuizzes = 4; lastQuizDay = Long.MIN_VALUE } }
            .setup().get().also { q -> repeat(Words.seenToday(ctx).size) { // a question may be the definition or a blanked example: quizAnswer() reads both
                val answer = quizAnswer(q)
                buttons(q).first { it.text == answer }.performClick()
                buttons(q).first { it.text == ctx.getString(R.string.quiz_next) }.performClick()
            } }
        // Q4: installed from the GitHub releases (no installer, or another one), « Noter » would open a missing listing: no card, not spent
        assertTrue(views(fifth()).none { (it as? TextView)?.text == ctx.getString(R.string.rate_text) })
        assertTrue(!p.rateAsked)
        shadowOf(ctx.packageManager).setInstallSourceInfo(ctx.packageName, "com.android.vending", "com.android.vending")
        val q = fifth()
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
        History(ctx).mark(w.key, java.time.LocalDate.now().toEpochDay() - 1) // seen on an earlier day: the extra opens only such items or today's (S4)
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

    /** Couleurs et polices: a tap removes or keeps a tile at once, Toutes = null, the pool survives recreation. The last one cannot be
     *  removed and says why before any tap (audit 011 n° 23): disabled, its state read « gardée, la dernière : il en faut au moins une »,
     *  a help line under the count; « Celle du mot » (not « Aucune », refused afterwards) keeps the word's own. */
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
        val none = ctrl.get().findViewById<Button>(R.id.palettes_none)
        assertEquals("Celle du mot", none.text.toString())
        assertTrue(views(ctrl.get()).filterIsInstance<android.widget.CheckBox>().all { it.isEnabled })
        none.performClick()
        val own = (Words.today(ctx).filterIsInstance<Word>().firstOrNull() ?: Words.load(ctx).first()).palette
            .takeIf { id -> cat.palettes.any { it.id == id } } ?: cat.palettes.first().id
        assertEquals(setOf(own), p.palettes) // « Aucune » keeps the word's own palette
        val last = cat.palettes.first { it.id == own }.nom
        val why = "Il en faut au moins une\u202F: $last reste."
        fun minShown() = views(ctrl.get()).filterIsInstance<TextView>().any { it.text.toString() == why && it.isShown }
        assertTrue(!tile(last).isEnabled && tile(last).isChecked) // said before any tap
        assertEquals("gardée, la dernière\u202F: il en faut au moins une", tile(last).stateDescription)
        assertTrue(minShown())
        assertTrue(views(ctrl.get()).filterIsInstance<android.widget.CheckBox>().count { !it.isEnabled } == 1) // only that one; the fonts all free
        tile(last).performClick()
        assertEquals(setOf(own), p.palettes) // the last one cannot be removed
        assertTrue(tile(last).isChecked)
        assertEquals(null, org.robolectric.shadows.ShadowToast.getTextOfLatestToast()) // no refusal after the fact
        tile("Cinzel").performClick()
        ctrl.recreate() // rotation: read back from Prefs
        assertTrue(!tile("Cinzel").isChecked && tile(last).isChecked && tile("Braise").isChecked == (own == "braise") && !tile(last).isEnabled && minShown())
        assertEquals(ctx.resources.getQuantityString(R.plurals.pool_count, 1, 1, cat.palettes.size), ctrl.get().findViewById<TextView>(R.id.palettes_count).text)
        ctrl.get().findViewById<Button>(R.id.polices_all).performClick()
        assertEquals(null, p.polices)
        tile(cat.palettes.first { it.id != own }.nom).performClick() // a second palette kept: the first one is free again
        assertTrue(tile(last).isEnabled && !minShown())
        p.palettes = null
    }
}
