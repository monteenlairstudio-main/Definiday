package fr.definiday

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint

/** Draws the word as a full-screen lock wallpaper. Text sits in the lower half so the system clock stays readable. */
object WordCard {
    fun render(w: Word, width: Int, height: Int): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawPaint(Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, height.toFloat(), 0xFF1B2440.toInt(), 0xFF0B0F1C.toInt(), Shader.TileMode.CLAMP)
        })
        val margin = width * 0.09f
        val textW = (width - 2 * margin).toInt()
        fun paint(sizeRatio: Float, style: Int, color: Int) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = width * sizeRatio; typeface = Typeface.create(Typeface.SERIF, style); this.color = color
        }
        val blocks = listOf(
            w.mot to paint(if (w.mot.length > 12) 0.09f else 0.12f, Typeface.BOLD, Color.WHITE),
            w.nature to paint(0.042f, Typeface.ITALIC, 0xFFC9A96E.toInt()),
            w.definition to paint(0.05f, Typeface.NORMAL, 0xFFE8E8EE.toInt()),
            (if (w.exemple.isEmpty()) "" else "«\u00a0${w.exemple}\u00a0»") to paint(0.042f, Typeface.ITALIC, 0xFFA0A6B8.toInt()),
        ).filter { it.first.isNotEmpty() }.map {
            StaticLayout.Builder.obtain(it.first, 0, it.first.length, it.second, textW)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(0f, 1.15f).build()
        }
        var y = height * 0.42f
        for (l in blocks) {
            c.save(); c.translate(margin, y); l.draw(c); c.restore()
            y += l.height + width * 0.035f
        }
        c.drawText("DEFINIDAY", margin, height * 0.93f, paint(0.03f, Typeface.BOLD, 0x66FFFFFF))
        return bmp
    }
}
