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

    fun lines(item: Item, style: Style): List<Line> {
        val pal = style.palette
        val serif = { s: Int -> Typeface.create(Typeface.SERIF, s) }
        if (item is Quote) return listOf(
            Line("«\u00a0${item.texte}\u00a0»", if (item.texte.length > 140) 0.055f else 0.065f, style.typeface, pal.mot),
            Line(item.signature, 0.042f, serif(Typeface.ITALIC), pal.nature),
        )
        val w = item as Word
        return listOf(
            Line(w.mot, if (w.mot.length > 12) 0.09f else 0.12f, style.typeface, pal.mot),
            Line(w.nature, 0.042f, serif(Typeface.ITALIC), pal.nature),
            Line(w.definition, 0.05f, serif(Typeface.NORMAL), pal.texte),
            Line(if (w.exemple.isEmpty()) "" else "«\u00a0${w.exemple}\u00a0»", 0.042f, serif(Typeface.ITALIC), pal.exemple),
        ).filter { it.text.isNotEmpty() }
    }

    fun render(item: Item, width: Int, height: Int, style: Style, bottom: Boolean = true) =
        render(lines(item, style), style.palette, width, height, bottom)

    /** Shared picture (4:5, fits social feeds): the card centred, [badge] (Glose's head) and « Definiday » signed at the foot. */
    fun share(item: Item, style: Style, badge: Bitmap?, width: Int = 1080): Bitmap {
        val height = width * 5 / 4
        val b = render(lines(item, style), style.palette, width, height, bottom = false, centre = true)
        val c = Canvas(b)
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = width * 0.036f; typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC); color = style.palette.nature
        }
        val size = width * 0.08f
        val text = "Definiday"
        var x = (width - paint.measureText(text) - (if (badge != null) size * 1.15f else 0f)) / 2
        val base = height * 0.93f
        badge?.let { c.drawBitmap(it, null, android.graphics.RectF(x, base - size * 0.78f, x + size, base + size * 0.22f), null); x += size * 1.15f }
        c.drawText(text, x, base, paint)
        return b
    }

    /**
     * Full-screen lock wallpaper. bottom = text block ends above the shortcut row, because the system clock
     * grows to fill the upper half when no notification is shown; otherwise it starts at 42 % (old layout).
     * The block shrinks until it fits in the lower half ([centre]: centred, in up to 70 % of the height).
     */
    fun render(lines: List<Line>, pal: Palette, width: Int, height: Int, bottom: Boolean, centre: Boolean = false): Bitmap {
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
        val room = if (centre) 0.7f else 0.4f
        while (total() > height * room && scale > 0.6f) { scale -= 0.05f; blocks = layouts(scale) }
        var y = when { centre -> (height * 0.95f - total()) / 2; bottom -> height * 0.84f - total(); else -> height * 0.42f }
        for (l in blocks) {
            c.save(); c.translate(margin, y); l.draw(c); c.restore()
            y += l.height + gap
        }
        return bmp
    }
}
