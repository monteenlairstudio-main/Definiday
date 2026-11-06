package fr.definiday

import org.junit.Assert.assertEquals
import org.junit.Test

/** Glose.Taps on a fake clock: immunity, streak of 4, timeout, longer calm after the special. */
class GloseTapsTest {
    private var t = 10_000L
    private val taps = Glose.Taps { t }
    private fun at(ms: Long) = run { t = ms; taps.next() }

    @Test fun ignoresTapsWhileReacting() {
        assertEquals("_tap", at(10_000))
        assertEquals(null, at(10_300)) // mid-reaction: no restart
        assertEquals(null, at(10_699))
        assertEquals("_tap", at(10_700))
    }

    @Test fun fourTapsInARowMakeTheSpecialThenACalm() {
        assertEquals(listOf("_tap", null, "_tap", "_tap", null, "_special"), listOf(0L, 200, 800, 1600, 2000, 2400).map { at(10_000 + it) })
        assertEquals(null, at(12_400 + Glose.Taps.SPECIAL_QUIET_MS - 1))
        assertEquals("_tap", at(12_400 + Glose.Taps.SPECIAL_QUIET_MS)) // the counter started again
        assertEquals(listOf("_tap", "_tap", "_special"), listOf(1000L, 2000, 3000).map { at(15_400 + it) })
    }

    @Test fun aPauseRestartsTheStreak() {
        assertEquals(listOf("_tap", "_tap", "_tap"), listOf(0L, 1000, 2000).map { at(10_000 + it) })
        assertEquals("_tap", at(12_000 + Glose.Taps.STREAK_MS + 1)) // too late: counts as the first again
        assertEquals(listOf("_tap", "_tap", "_special"), listOf(1000L, 2000, 3000).map { at(13_501 + it) })
    }
}
