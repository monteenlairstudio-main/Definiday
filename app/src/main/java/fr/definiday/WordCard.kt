package fr.definiday

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint

/** Draws the word as a full-screen lock wallpaper. Text sits in the lower half so the system clock stays readable. */
object WordCard {
    /** One paragraph of the card: text, size as a fraction of the screen width, face, color. */
    class Line(val text: String, val size: Float, val face: Typeface, val color: Int)

    fun lines(w: Word, style: Style): List<Line> {
        val pal = style.palette
        val serif = { s: Int -> Typeface.create(Typeface.SERIF, s) }
        return listOf(
            Line(w.mot, if (w.mot.length > 12) 0.09f else 0.12f, style.typeface, pal.mot),
            Line(w.nature, 0.042f, serif(Typeface.ITALIC), pal.nature),
            Line(w.definition, 0.05f, serif(Typeface.NORMAL), pal.texte),
            Line(if (w.exemple.isEmpty()) "" else "«\u00a0${w.exemple}\u00a0»", 0.042f, serif(Typeface.ITALIC), pal.exemple),
        ).filter { it.text.isNotEmpty() }
    }

    fun render(w: Word, width: Int, height: Int, style: Style, bottom: Boolean = true) =
        render(lines(w, style), style.palette, width, height, bottom)

    /**
     * Full-screen lock wallpaper. bottom = text block ends above the shortcut row, because the system clock
     * grows to fill the upper half when no notification is shown; otherwise it starts at 42 % (old layout).
     * The block shrinks until it fits in the lower half.
     */
    fun render(lines: List<Line>, pal: Palette, width: Int, height: Int, bottom: Boolean): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawPaint(Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, height.toFloat(), pal.haut, pal.bas, Shader.TileMode.CLAMP)
        })
        val margin = width * 0.09f
        val gap = width * 0.035f
        fun layouts(scale: Float) = lines.map { l ->
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = width * l.size * scale; typeface = l.face; color = l.color }
            StaticLayout.Builder.obtain(l.text, 0, l.text.length, paint, (width - 2 * margin).toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(0f, 1.15f).build()
        }
        var scale = 1f
        var blocks = layouts(scale)
        fun total() = blocks.sumOf { it.height } + gap * (blocks.size - 1)
        while (total() > height * 0.4f && scale > 0.6f) { scale -= 0.05f; blocks = layouts(scale) }
        var y = if (bottom) height * 0.84f - total() else height * 0.42f
        for (l in blocks) {
            c.save(); c.translate(margin, y); l.draw(c); c.restore()
            y += l.height + gap
        }
        return bmp
    }
}
