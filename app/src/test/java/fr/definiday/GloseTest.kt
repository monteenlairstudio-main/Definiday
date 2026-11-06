package fr.definiday

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.widget.ImageView
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
        overlays.forEach { (name, style) -> assertTrue(name, diff(base, render(R.drawable.glose_assis_vd, listOf(style))) > 50) }
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
