package fr.definiday

import android.graphics.Typeface
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File
import java.nio.ByteBuffer

/** U+202F in the bundled fonts (tools/fonts_nnbsp.py): without it « ; : ! ? » fall back to a system font after the space. */
@RunWith(RobolectricTestRunner::class)
class FontsTest {
    private val ctx = RuntimeEnvironment.getApplication()
    private val fonts = File("src/main/res/font").listFiles { f -> f.extension == "ttf" }!!.sortedBy { it.name }

    /** Does the font's Windows Unicode BMP cmap (format 4) map [cp]? Read from the file: Paint.hasGlyph counts the system fallback too. */
    private fun maps(font: File, cp: Int): Boolean {
        val b = ByteBuffer.wrap(font.readBytes())
        fun u16(at: Int) = b.getShort(at).toInt() and 0xFFFF
        val cmap = (0 until u16(4)).map { 12 + 16 * it }.first { b.getInt(it) == 0x636D6170 /* "cmap" */ }.let { b.getInt(it + 8) }
        val sub = (0 until u16(cmap + 2)).map { cmap + 4 + 8 * it }.first { u16(it) == 3 && u16(it + 2) == 1 }.let { cmap + b.getInt(it + 4) }
        assertEquals(4, u16(sub))
        val seg2 = u16(sub + 6); val ends = sub + 14; val starts = ends + seg2 + 2; val deltas = starts + seg2; val ranges = deltas + seg2
        val i = (0 until seg2 step 2).first { u16(ends + it) >= cp }
        if (u16(starts + i) > cp) return false
        val ro = u16(ranges + i)
        val glyph = if (ro == 0) cp + u16(deltas + i) else u16(ranges + i + ro + 2 * (cp - u16(starts + i))).let { if (it == 0) 0 else it + u16(deltas + i) }
        return glyph and 0xFFFF != 0
    }

    @Test fun everyFontHasTheNarrowNoBreakSpaceUnlessItsNameIsReserved() {
        assertEquals(14, fonts.size)
        fonts.forEach { f ->
            assertEquals(f.name, f.nameWithoutExtension !in Styles.NO_NNBSP, maps(f, 0x202F))
            assertTrue(f.name, maps(f, 0xA0) && maps(f, '!'.code) && !maps(f, 0xE000)) // the text-level stand-in exists; the reader holds
        }
    }

    @Test fun reservedFacesGetANoBreakSpaceBeforePunctuation() {
        listOf(R.string.exemple_note, R.string.quiz_hello, R.string.onb_welcome_title) // style Note: Dancing Script
            .forEach { assertFalse(ctx.getString(it), ' ' in ctx.getString(it)) }
        val pal = Styles.load(ctx).palettes.first()
        val text = "Quelle joie ! Dit-il ; « bien »"
        assertEquals("Quelle joie ! Dit-il ; « bien »", Styles.fine(text, Style(pal, Typeface.SERIF, Police("playfair_display", "", ""))))
        assertEquals(text, Styles.fine(text, Style(pal, Typeface.SERIF, Police("eb_garamond", "", ""))))
        assertEquals(text, Styles.fine(text, Style(pal, Typeface.SERIF)))
    }
}
