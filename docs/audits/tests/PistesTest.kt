package fr.definiday

import android.widget.Button
import android.widget.TextView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.ZonedDateTime

/** Pistes checked: these PASS (no bug). */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "fr-w411dp-h891dp-xxhdpi")
class PistesTest {
    private val ctx = RuntimeEnvironment.getApplication()

    @Test fun removedWordInDailyMotsIsReplacedOnce() {
        val p = Prefs(ctx).apply { wordsPerDay = 2 }
        val first = Words.today(ctx)
        p.dailyMots = listOf("mot-disparu-de-words-json") + first.drop(1).map { it.key }
        val t = Words.today(ctx)
        assertEquals(2, t.size); assertEquals(t, Words.today(ctx))
        assertTrue(Words.seenToday(ctx).none { it.mot == "mot-disparu-de-words-json" })
    }

    @Test fun perDay1to3to1IsStable() {
        val p = Prefs(ctx)
        val one = Words.today(ctx)
        p.wordsPerDay = 3; val three = Words.today(ctx)
        p.wordsPerDay = 1; assertEquals(one, Words.today(ctx))
        p.wordsPerDay = 3; assertEquals(three, Words.today(ctx))
        assertEquals(3, three.toSet().size)
    }

    @Test fun unknownStyleIdsFallBack() {
        val w = Words.load(ctx).first().copy(palette = "inconnue", police = "inconnue")
        Prefs(ctx).apply { palettes = setOf("n'existe plus"); polices = setOf("n'existe plus") }
        Styles.of(ctx, w); Styles.of(ctx, Quotes.load(ctx).first())
    }

    @Test fun springForwardGapIsSafe() {
        val fired = ZonedDateTime.parse("2026-03-29T03:30:00.100+02:00[Europe/Paris]") // 02:30 does not exist -> 03:30
        assertEquals(LocalDate.of(2026, 3, 30), DailyReceiver.nextTrigger(fired, 2, 30).toLocalDate())
    }

    @Test fun quizWithAFewWordsInTheCatalog() {
        val all = Words.load(ctx).take(2)
        Quiz.build(all, all).forEach { assertTrue(it.answer in it.options); assertEquals(it.options.size, it.options.toSet().size) }
    }
}
