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
    fun render(w: Word, width: Int, height: Int, style: Style): Bitmap {
        val pal = style.palette
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawPaint(Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, height.toFloat(), pal.haut, pal.bas, Shader.TileMode.CLAMP)
        })
        val margin = width * 0.09f
        val textW = (width - 2 * margin).toInt()
        fun paint(sizeRatio: Float, face: Typeface, color: Int) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = width * sizeRatio; typeface = face; this.color = color
        }
        val serif = { s: Int -> Typeface.create(Typeface.SERIF, s) }
        val blocks = listOf(
            w.mot to paint(if (w.mot.length > 12) 0.09f else 0.12f, style.typeface, pal.mot),
            w.nature to paint(0.042f, serif(Typeface.ITALIC), pal.nature),
            w.definition to paint(0.05f, serif(Typeface.NORMAL), pal.texte),
            (if (w.exemple.isEmpty()) "" else "«\u00a0${w.exemple}\u00a0»") to paint(0.042f, serif(Typeface.ITALIC), pal.exemple),
        ).filter { it.first.isNotEmpty() }.map {
            StaticLayout.Builder.obtain(it.first, 0, it.first.length, it.second, textW)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(0f, 1.15f).build()
        }
        var y = height * 0.42f
        for (l in blocks) {
            c.save(); c.translate(margin, y); l.draw(c); c.restore()
            y += l.height + width * 0.035f
        }
        c.drawText("DEFINIDAY", margin, height * 0.93f, paint(0.03f, serif(Typeface.BOLD), pal.nature))
        return bmp
    }
}
