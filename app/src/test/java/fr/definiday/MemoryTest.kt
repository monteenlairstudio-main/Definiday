package fr.definiday

import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
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
import kotlin.random.Random

private fun views(a: android.app.Activity) = generateSequence(listOf<View>(a.window.decorView)) { l ->
    l.flatMap { v -> (v as? android.view.ViewGroup)?.let { g -> (0 until g.childCount).map(g::getChildAt) } ?: emptyList() }.ifEmpty { null }
}.flatten()

/** The right option of the quiz question on screen, asked by its definition or by its example with a blank: that button's word. */
internal fun quizAnswer(a: android.app.Activity): String {
    val shown = views(a).filterIsInstance<TextView>().map { it.text.toString() }.toSet()
    val right = Words.load(a).filter { it.definition in shown || Quiz.cloze(it) in shown }.map { it.mot }.toSet()
    return views(a).filterIsInstance<Button>().map { it.text.toString() }.first { it in right }
}

/** Evening review (ladder J+2, J+7, J+30, J+90), distractors, fill-in-the-blank, favourites and « Mes mots ». */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "fr-w411dp-h891dp-xxhdpi")
class MemoryTest {
    private val ctx = RuntimeEnvironment.getApplication()
    private val all by lazy { Words.load(ctx) }
    private fun word(mot: String) = all.first { it.mot == mot }

    @Test fun ladderDueDatesClimbAndMissesGoBackToJ2() {
        var e = Memory.Entry(start = 100)
        assertEquals(102L, Quiz.dueDay(e))
        assertEquals(e, Quiz.after(e, right = true, day = 100)) // tonight's word, right: nothing moves before J+2
        listOf(102L to 107L, 107L to 130L, 130L to 190L).forEach { (day, next) ->
            e = Quiz.after(e, right = true, day = day)
            assertEquals(next, Quiz.dueDay(e))
        }
        assertNull(Quiz.dueDay(Quiz.after(e, true, 190))) // past J+90: learnt, never due again
        e = Quiz.after(e, right = false, day = 140) // missed (in a redone or late quiz): J+2 from that day
        assertEquals(Memory.Entry(140, 0, 1), e)
        assertEquals(142L, Quiz.dueDay(e))
        assertEquals(Memory.Entry(0, 3, 0), Quiz.after(Memory.Entry(0), true, 40)) // 40 days late: next rung still ahead is J+90, not J+7 every evening
    }

    @Test fun atMostTwoReviewsMostOverdueFirstAndNeverTonightsWords() {
        val day = 1000L
        val history = mapOf("a" to day - 2, "b" to day - 50, "c" to day - 1, "d" to day - 3, "e" to day)
        val entries = mapOf("b" to Memory.Entry(day - 50, step = Quiz.LADDER.size), "d" to Memory.Entry(day - 10, step = 1)) // b learnt, d due day - 3
        assertEquals(listOf("d", "a"), Quiz.due(entries, history, day, exclude = emptySet()))
        assertEquals(listOf("a"), Quiz.due(entries, history, day, exclude = setOf("d")))
        assertTrue(Quiz.due(entries, history, day - 4, emptySet()).isEmpty())
        // mixed with tonight's words: theirs first, then the reviews, marked
        val q = Quiz.build(all.take(2), all, Random(1), review = all.drop(5).take(2))
        assertEquals(all.take(2) + all.drop(5).take(2), q.map { it.answer })
        assertEquals(listOf(false, false, true, true), q.map { it.review })
    }

    @Test fun distractorsAreSeenWordsOfTheSamePartOfSpeech() {
        val w = word("abjurer")
        val seen = all.filter { it.nature == w.nature && it != w }.take(10).map { it.mot }.toSet()
        repeat(50) { s ->
            val q = Quiz.build(listOf(w), all, Random(s), seen).single()
            assertTrue(q.options.toString(), (q.options - w).all { it.mot in seen })
        }
        val otherNature = all.filter { it.nature != w.nature }.take(10).map { it.mot }.toSet() // seen, but not a verb: verbs first
        repeat(50) { s -> assertTrue(Quiz.build(listOf(w), all, Random(s), otherNature).single().options.all { it.nature == w.nature }) }
        val w2 = all.first { it.nature == w.nature && it != w } // tonight's other word is no favoured distractor
        val pick = (0 until 200).count { s -> w2 in Quiz.build(listOf(w, w2), all, Random(s), setOf(w2.mot) + seen).first().options }
        assertEquals(0, pick)
    }

    @Test fun clozeBlanksTheWordAsWritten() {
        assertEquals("Sous la menace du bûcher, il dut _____ sa foi en public.", Quiz.cloze(word("abjurer")))
        assertEquals("L’hiver venu, il s’_____ au coin du feu jusqu’à midi.", Quiz.cloze(word("acagnarder"))) // inflected verb
        assertEquals("Cette lettre de démission, écrite _____, il la regretta dès le lendemain.", Quiz.cloze(word("ab irato")))
        fun w(mot: String, ex: String) = Word(mot, "n. f.", "d", ex)
        assertEquals("_____ et bouliers.", Quiz.cloze(w("abaque", "Abaques et bouliers."))) // case, plural
        assertEquals("_____, les roses passent.", Quiz.cloze(w("éphémère", "Éphémères, les roses passent."))) // accented capital
        assertEquals("Ce mérite tient au _____.", Quiz.cloze(w("rite", "Ce mérite tient au rite."))) // « é » is a letter: not inside « mérite »
        assertEquals("Elle est _____.", Quiz.cloze(w("fallacieux", "Elle est fallacieuse.")))
        assertNull(Quiz.cloze(word("bayer"))) // « bayait »: not found
        assertNull(Quiz.cloze(w("lire", "Il lut longtemps.")))
        assertTrue((0 until 100).all { s -> Quiz.build(listOf(word("bayer")), all, Random(s)).single().cloze == null }) // definition question instead
        assertTrue((0 until 100).any { s -> Quiz.build(listOf(word("abjurer")), all, Random(s)).single().cloze != null })
        assertTrue(all.count { Quiz.cloze(it) != null } > all.size * 0.99)
    }

    /** Past words due tonight join the quiz after today's, count in the score, survive a rotation and climb the ladder. */
    @Test fun eveningQuizAddsTwoReviewsThatCount() {
        val today = LocalDate.now().toEpochDay()
        while (Words.seenToday(ctx).isEmpty()) Words.random(ctx)
        val n = Words.seenToday(ctx).size
        val h = History(ctx)
        h.mark("abjurer", today - 2); h.mark("acagnarder", today - 9); h.mark("abâtardir", today - 1); h.mark("q:Le style est l’homme même.", today - 30)
        val c = Robolectric.buildActivity(QuizActivity::class.java).setup()
        fun progress() = views(c.get()).mapNotNull { it.contentDescription?.toString() }.toList()
        assertTrue(progress().toString(), ctx.getString(R.string.quiz_progress, 1, n + 2) in progress())
        repeat(n) {
            val mot = quizAnswer(c.get())
            views(c.get()).filterIsInstance<Button>().first { it.text == mot }.performClick()
            views(c.get()).filterIsInstance<Button>().first { it.text == ctx.getString(R.string.quiz_next) }.performClick()
        }
        c.recreate() // the reviews stay
        assertTrue(views(c.get()).filterIsInstance<TextView>().any { it.text == ctx.getString(R.string.quiz_review) })
        assertTrue(ctx.getString(R.string.quiz_progress, n + 1, n + 2) in progress())
        repeat(2) {
            val mot = quizAnswer(c.get())
            assertTrue(mot in setOf("acagnarder", "abjurer"))
            views(c.get()).filterIsInstance<Button>().first { it.text == mot }.performClick()
            views(c.get()).filterIsInstance<Button>().first { it.text == ctx.getString(R.string.quiz_next) }.performClick()
        }
        assertEquals(n + 2, Prefs(ctx).correctAnswers)
        val m = Memory(ctx)
        assertEquals(2 to 2, m.reviews to m.reviewsRight)
        assertEquals(today + 5, Quiz.dueDay(m.entries().getValue("abjurer"))) // J+7
        assertEquals(today + 21, Quiz.dueDay(m.entries().getValue("acagnarder"))) // J+30
        assertTrue(Words.seenToday(ctx).none { it.mot == "abjurer" }) // reviewed in the quiz, not shown again as a daily word
    }

    @Test fun favouritesPersistAndJunkIsIgnored() {
        Memory(ctx).favourite("abaque", true)
        assertEquals(setOf("abaque"), Memory(ctx).favourites)
        val w = all.first { it.mot != "abaque" }
        val a = Robolectric.buildActivity(MainActivity::class.java, Intent(ctx, MainActivity::class.java).putExtra(MainActivity.EXTRA_MOT, w.mot)).setup().get()
        val star = a.findViewById<CheckBox>(R.id.favori)
        assertTrue(!star.isChecked && star.visibility == View.VISIBLE)
        star.performClick()
        assertEquals(setOf("abaque", w.mot), Memory(ctx).favourites)
        val again = Robolectric.buildActivity(MainActivity::class.java, Intent(ctx, MainActivity::class.java).putExtra(MainActivity.EXTRA_MOT, w.mot)).setup().get()
        assertTrue(again.findViewById<CheckBox>(R.id.favori).isChecked)
        val quote = Quotes.load(ctx).first().key
        assertEquals(View.GONE, Robolectric.buildActivity(MainActivity::class.java, Intent(ctx, MainActivity::class.java).putExtra(MainActivity.EXTRA_MOT, quote))
            .setup().get().findViewById<View>(R.id.favori).visibility)
        // restored junk: wrong types, bad numbers
        ctx.getSharedPreferences("memoire", 0).edit().putString("favoris", "x").putInt("m:a", 3).putString("m:b", "x/1/2").putString("m:c", "5/99/-4")
            .putString("reviews", "?").commit()
        assertEquals(emptySet<String>(), Memory(ctx).favourites)
        assertEquals(mapOf("c" to Memory.Entry(5, Quiz.LADDER.size, 0)), Memory(ctx).entries())
        assertEquals(0, Memory(ctx).reviews)
    }

    @Test fun searchIgnoresCaseAccentsAndListsNewestFirst() {
        val h = mapOf("abâtardir" to 10L, "ab irato" to 12L, "abjurer" to 11L)
        assertEquals(listOf("ab irato", "abjurer", "abâtardir"), MotsActivity.list(all, h).map { it.mot })
        assertEquals(listOf("abâtardir"), MotsActivity.list(all, h, " ABATARD ").map { it.mot })
        assertEquals(listOf("ab irato"), MotsActivity.list(all, h, "colere").map { it.mot }) // in the definition
        assertEquals(listOf("abjurer"), MotsActivity.list(all, h, only = setOf("abjurer", "zinzolin")).map { it.mot }) // never-seen favourite: not listed
        assertTrue(MotsActivity.list(all, h, "zinzolin").isEmpty())
        assertEquals("coeur", MotsActivity.fold("Cœur"))
    }

    @Test fun mesMotsOpensACardFiltersFavouritesAndStars() {
        val today = LocalDate.now().toEpochDay()
        History(ctx).run { mark("abjurer", today); mark("abâtardir", today - 1); mark("q:Le style est l’homme même.", today) }
        val main = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        main.findViewById<View>(R.id.mots_open).performClick()
        assertEquals(MotsActivity::class.java.name, shadowOf(main).nextStartedActivity.component!!.className)
        val c = Robolectric.buildActivity(MotsActivity::class.java).setup()
        val a = c.get()
        fun rows() = views(a).filterIsInstance<TextView>().map { it.text.toString() }.filter { t -> all.any { it.mot == t } }.toList()
        fun layout() = a.window.decorView.run { measure(View.MeasureSpec.makeMeasureSpec(1233, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(2600, View.MeasureSpec.EXACTLY)); layout(0, 0, 1233, 2600) }
        layout()
        val shownToday = History(ctx).all().filter { (k, v) -> v == today && !k.startsWith("q:") }.keys
        assertEquals(shownToday.size + 1, rows().size) // MainActivity showed today's item too
        assertEquals("abâtardir", rows().last())
        views(a).filterIsInstance<View>().first { (it as? TextView)?.text == "abâtardir" }.let { (it.parent.parent as View).performClick() }
        val open = shadowOf(a).nextStartedActivity
        assertEquals("abâtardir", open.getStringExtra(MainActivity.EXTRA_MOT))
        assertTrue(open.getBooleanExtra(MainActivity.EXTRA_DEJA_VU, false))
        // the card it opens says « Déjà vu », not « Mot au hasard »
        val card = Robolectric.buildActivity(MainActivity::class.java, open).setup().get()
        assertEquals(ctx.getString(R.string.deja_vu), card.findViewById<TextView>(R.id.label).text.toString())
        val star = views(a).filterIsInstance<CheckBox>().last()
        star.performClick()
        assertEquals(setOf("abâtardir"), Memory(ctx).favourites)
        views(a).filterIsInstance<Switch>().single().performClick() // « Favoris seulement »
        layout()
        assertEquals(listOf("abâtardir"), rows())
        views(a).filterIsInstance<EditText>().single().setText("jur")
        layout()
        assertTrue(rows().isEmpty())
        assertTrue(views(a).filterIsInstance<TextView>().any { it.text == ctx.getString(R.string.mots_none_found) && it.visibility == View.VISIBLE })
        assertTrue(views(a).any { it.contentDescription == "1 " + ctx.resources.getQuantityString(R.plurals.stat_favourites, 1) })
    }
}
