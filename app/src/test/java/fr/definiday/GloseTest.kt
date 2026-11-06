package fr.definiday

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.widget.ImageView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "fr-w411dp-h891dp-xxhdpi")
class GloseTest {
    private val ctx = RuntimeEnvironment.getApplication()
    private val all = R.drawable::class.java.fields.filter { it.name.startsWith("glose_") }.associate { it.name to it.getInt(null) }
    private val overlays = R.style::class.java.fields.filter { it.name.startsWith("Glose_") }.associate { it.name to it.getInt(null) }

    private fun render(res: Int, styles: List<Int> = emptyList(), size: Int = 256) = Glose.bitmap(ctx, res, size, size, styles)
    private fun painted(b: Bitmap) = IntArray(b.width * b.height).also { b.getPixels(it, 0, b.width, 0, 0, b.width, b.height) }.count { Color.alpha(it) > 0 }
    private fun diff(a: Bitmap, b: Bitmap) = (0 until a.width * a.height).count { a.getPixel(it % a.width, it / a.width) != b.getPixel(it % a.width, it / a.width) }

    /** Every generated VD/AVD inflates and draws, alone and with every overlay at once. */
    @Test fun everyDrawableInflatesAndDraws() {
        assertTrue(all.size >= 20)
        all.forEach { (name, res) ->
            assertTrue(name, painted(render(res)) > 2000)
            assertTrue(name, painted(render(res, overlays.values.toList())) > 2000)
        }
    }

    /** Theme attributes reach the paths: each accessory/pelage/pelote changes the picture, and only when applied. */
    @Test fun overlaysChangeThePicture() {
        val base = render(R.drawable.glose_assis_vd)
        assertTrue(diff(base, render(R.drawable.glose_assis_vd)) == 0)
        // éclats are scaled to 0 in the still frame
        overlays.filterKeys { it != "Glose_Eclats" }.forEach { (name, style) -> assertTrue(name, diff(base, render(R.drawable.glose_assis_vd, listOf(style))) > 50) }
    }

    /** The unlock table, as agreed with the owner (thresholds count quizzes). */
    @Test fun unlockTable() {
        val table = mapOf("assis" to "q0", "tigre" to "q0", "bordeaux" to "q0", "ecrit_mot" to "q1", "ecrit_encore" to "q1", "ecrit_bravo" to "r1",
            "noeud" to "p1", "soir" to "s3", "ecrit_dodo" to "s3", "moutarde" to "q5", "echarpe" to "s7", "livres" to "q10", "creme" to "p5",
            "bonnet" to "s14", "sauge" to "q20", "ecrit_rare" to "r30", "plume" to "r60", "rose" to "p15", "marque" to "q45",
            "enluminure" to "r150", "nuit" to "s30")
        assertEquals(table, Unlocks.items.associate { it.id to "${it.on}${it.need}" })
        fun of(c: Char, n: Int) = Unlocks.of(if (c == 'q') n else 0, if (c == 'p') n else 0, if (c == 'r') n else 0, if (c == 's') n else 0)
        Unlocks.items.filter { it.need > 0 }.forEach {
            assertTrue(it.id, it.id in of(it.on, it.need))
            assertFalse(it.id, it.id in of(it.on, it.need - 1))
        }
        assertEquals(setOf("assis", "tigre", "bordeaux"), Unlocks.of(0, 0, 0, 0))
        assertEquals(Unlocks.items.map { it.id }.toSet(), Unlocks.of(150, 150, 150, 150))
    }

    @Test fun unlocksAreKeptAndAnnouncedOnce() {
        val p = Prefs(ctx).apply { quizzes = 1; streak = 3 }
        assertEquals(setOf("ecrit_mot", "ecrit_encore", "soir", "ecrit_dodo"), Unlocks.update(p).map { it.id }.toSet())
        p.announced = p.announced + Unlocks.update(p).map { it.id }
        p.streak = 1 // broken streak: nothing is lost, nothing new
        assertTrue(Unlocks.update(p).isEmpty())
        assertTrue("soir" in p.unlocked)
        assertEquals("Encore 2 soirs de quiz d’affilée", Unlocks.hint(Unlocks.byId.getValue("soir"), p))
    }

    @Test fun collectionTriesThenKeepsAChoice() {
        val p = Prefs(ctx).apply { quizzes = 5 }
        val a = Robolectric.buildActivity(CollectionActivity::class.java).setup().get()
        fun find(desc: String): android.view.View { val l = ArrayList<android.view.View>(); a.window.decorView.findViewsWithText(l, desc, android.view.View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION); return l.first() }
        assertFalse(find("Pelote sauge, à débloquer, Encore 15 quiz").isClickable)
        find("Pelote moutarde").performClick()
        assertEquals("bordeaux", p.glosePelote) // only tried
        val choose = a.window.decorView.let { val l = ArrayList<android.view.View>(); it.findViewsWithText(l, ctx.getString(R.string.collection_choose), android.view.View.FIND_VIEWS_WITH_TEXT); l.first() }
        choose.performClick()
        assertEquals("moutarde", p.glosePelote)
        assertEquals(listOf(R.style.Glose_Pelote_Moutarde), Glose.styles(Glose.choice(p)))
    }

    @Test fun mainScreenAnimatesGloseOnlyWhileVisible() {
        val c = Robolectric.buildActivity(MainActivity::class.java).setup()
        val v = c.get().findViewById<ImageView>(R.id.glose)
        val d = v.drawable as AnimatedVectorDrawable
        v.draw(Canvas(Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888))) // the AVD's animator really starts at its first draw
        assertTrue(d.isRunning)
        c.pause()
        assertFalse(d.isRunning)
    }

    /** Not a check: control PNGs (every pose, every overlay on the seated pose) in app/build/demo/glose. */
    @Test fun renderControlImages() {
        val out = File("build/demo/glose").apply { mkdirs() }
        fun grid(cells: List<Bitmap>, name: String) {
            val n = 4; val s = cells.first().width
            val g = Bitmap.createBitmap(n * s, (cells.size + n - 1) / n * s, Bitmap.Config.ARGB_8888)
            Canvas(g).apply { drawColor(Color.parseColor("#F6EEDF")); cells.forEachIndexed { i, b -> drawBitmap(b, (i % n) * s.toFloat(), (i / n) * s.toFloat(), null) } }
            File(out, "$name.png").outputStream().use { g.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        grid(all.filterKeys { it.endsWith("_vd") }.toSortedMap().values.map { render(it, size = 384) }, "poses")
        grid(overlays.toSortedMap().values.map { render(R.drawable.glose_assis_vd, listOf(it), 384) }, "overlays_assis")
        val combos = listOf(listOf(R.style.Glose_Pelage_Nuit, R.style.Glose_Acc_Bonnet), listOf(R.style.Glose_Pelage_Enluminure, R.style.Glose_Acc_Noeud),
            listOf(R.style.Glose_Pelage_Creme, R.style.Glose_Pelote_Sauge, R.style.Glose_Acc_Echarpe), listOf(R.style.Glose_Acc_Marque, R.style.Glose_Acc_Plume))
        grid(listOf(R.drawable.glose_livres_vd, R.drawable.glose_soir_vd, R.drawable.glose_ecrit_bravo_vd, R.drawable.glose_icone_vd)
            .flatMap { res -> combos.map { render(res, it, 384) } }, "combos")
    }
}
