package fr.definiday

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import java.time.LocalDate
import java.util.Locale

/**
 * The word as a postcard from Glose (« Correspondance »): the full-screen lock wallpaper, text in the lower half so the system
 * clock stays readable, and the shared picture. Postal motifs come from [Postal]; no Glose drawing on the lock screen.
 */
object WordCard {
    /** One paragraph of the card: text, size as a fraction of the card width, face, color. */
    class Line(val text: String, val size: Float, val face: Typeface, val color: Int)

    /** First line = the word (or the quote), underlined by the thread. One text serif (Libre Baskerville, no italic cut: color marks the example). */
    fun lines(ctx: Context, item: Item, style: Style): List<Line> {
        val pal = style.palette
        val serif = ctx.resources.getFont(R.font.libre_baskerville)
        if (item is Quote) return listOf(
            Line("«\u202F${Styles.fine(item.texte, style)}\u202F»", if (item.texte.length > 140) 0.055f else 0.065f, style.typeface, pal.mot),
            Line(item.signature, 0.04f, serif, pal.nature),
        )
        val w = item as Word
        return listOf(
            Line(w.mot, if (w.mot.length > 12) 0.09f else 0.12f, style.typeface, pal.mot),
            Line(w.nature, 0.04f, serif, pal.nature),
            Line(w.definition, 0.046f, serif, pal.texte),
            Line(if (w.exemple.isEmpty()) "" else "«\u202F${w.exemple}\u202F»", 0.039f, serif, pal.exemple),
        ).filter { it.text.isNotEmpty() }
    }

    private fun gradient(pal: Palette, top: Float, bottom: Float) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = LinearGradient(0f, top, 0f, bottom, pal.haut, pal.bas, Shader.TileMode.CLAMP) }

    /**
     * Full-screen lock wallpaper. bottom = text block ends above the shortcut row (12-16 % kept bare), because the system clock
     * grows to fill the upper half when no notification is shown; otherwise it starts at 42 % (old layout).
     * The text shrinks until the block fits in 40 % of the height.
     */
    fun render(ctx: Context, item: Item, width: Int, height: Int, style: Style, bottom: Boolean = true, date: LocalDate = LocalDate.now()): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawPaint(gradient(style.palette, 0f, height.toFloat()))
        block(ctx, c, item, style, RectF(0f, 0f, width.toFloat(), height.toFloat()), height * 0.4f, null, date) { if (bottom) height * 0.84f - it else height * 0.42f }
        return bmp
    }

    /** Shared picture (4:5, fits social feeds): the postcard on kraft, Glose's head as the stamp's vignette, « VIA DEFINIDAY » as a postal mention. */
    fun share(ctx: Context, item: Item, style: Style, badge: Bitmap?, width: Int = 1080, date: LocalDate = LocalDate.now()): Bitmap {
        val height = width * 5 / 4
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Postal.KRAFT)
        val m = width * 0.05f
        val card = RectF(m, m, width - m, height - m)
        c.drawRoundRect(card, m * 0.6f, m * 0.6f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Postal.SHEET; setShadowLayer(m * 0.3f, 0f, m * 0.1f, 0x29000000) })
        val face = RectF(card).apply { inset(width * 0.014f, width * 0.014f) }
        c.drawRoundRect(face, m * 0.4f, m * 0.4f, gradient(style.palette, face.top, face.bottom))
        block(ctx, c, item, style, face, face.height() * 0.74f, badge, date) { face.top + (face.height() * 0.92f - it) / 2 }
        val u = face.width()
        c.drawText(ctx.getString(R.string.share_via).uppercase(Locale.FRENCH), face.left + u * 0.09f, face.bottom - u * 0.07f,
            Postal.mention(ctx, u * 0.03f, style.palette.nature))
        return bmp
    }

    /**
     * The text block in [face]: « LETTRE DU 6 NOVEMBRE » with the postmark and stamp, the word on Glose's thread, the lines, then
     * the signature (words only). [top] places the block from its height; the lines shrink until it fits in [room] px.
     */
    private fun block(ctx: Context, c: Canvas, item: Item, style: Style, face: RectF, room: Float, vignette: Bitmap?, date: LocalDate, top: (Float) -> Float) {
        val pal = style.palette
        val u = face.width()
        val pad = u * 0.09f
        val left = face.left + pad
        val gap = u * 0.035f
        val (stampW, stampH) = u * 0.12f to u * 0.14f
        val threadRoom = u * 0.02f
        val sign = if (item is Word) TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = ctx.resources.getFont(R.font.dancing_script); textSize = u * 0.052f; color = pal.nature; textAlign = Paint.Align.RIGHT
        } else null
        val lines = lines(ctx, item, style)
        fun layouts(scale: Float) = lines.map { l ->
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = u * l.size * scale; typeface = l.face; color = l.color }
            StaticLayout.Builder.obtain(l.text, 0, l.text.length, paint, (u - 2 * pad).toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(0f, 1.15f).build()
        }
        var scale = 1f
        var blocks = layouts(scale)
        fun total() = stampH + gap * 0.5f + blocks.sumOf { it.height } + gap * (blocks.size - 1) + threadRoom + (sign?.textSize ?: 0f)
        while (total() > room && scale > 0.6f) { scale -= 0.05f; blocks = layouts(scale) }
        var y = top(total())

        val label = ctx.getString(if (item is Quote) R.string.card_quote else R.string.card_letter, Postal.day(date)).uppercase(Locale.FRENCH)
        c.drawText(label, left, y + stampH * 0.42f, Postal.mention(ctx, u * 0.03f, pal.nature))
        fun Postal.Paper.drawIn(l: Float, t: Float, r: Float, b: Float) { setBounds(l.toInt(), t.toInt(), r.toInt(), b.toInt()); draw(c) }
        val stampLeft = face.right - pad - stampW
        Postal.Stamp(ctx, pal.mot, pal.haut, Postal.initial(item), Styles.initialFace(ctx, item, style), caption = null, vignette = vignette)
            .drawIn(stampLeft, y, face.right - pad, y + stampH)
        // ring of radius r centred 1.45 r left of the stamp (the drawable puts it r + 1 dp from its left edge), waves onto the stamp
        val r = u * 0.058f
        val d = Postal.dp(ctx)
        val cx = stampLeft - r * 1.45f
        Postal.Postmark(ctx, pal.nature, date).drawIn(cx - r - d, y + stampH / 2 - r - d, stampLeft + stampW * 0.3f, y + stampH / 2 + r + d)
        y += stampH + gap * 0.5f

        blocks.forEachIndexed { i, l ->
            c.save(); c.translate(left, y); l.draw(c); c.restore()
            if (i == 0) {
                val last = l.lineCount - 1
                val end = (left + l.getLineRight(last) + u * 0.012f).coerceAtMost(face.right - pad - u * 0.04f)
                Postal.drawYarn(c, face.left, end, y + l.getLineBaseline(last) + l.paint.descent(), u / 411, pal) // just under the descenders
                y += threadRoom
            }
            y += l.height + gap
        }
        sign?.let { // Glose's full name (Prefs.gloseName), cut with « … » if a wide 24-letter name overflows the card
            val name = TextUtils.ellipsize(Prefs(ctx).gloseName, it, u - 2 * pad, TextUtils.TruncateAt.END).toString()
            c.drawText(name, face.right - pad, y - gap + it.textSize * 0.95f, it)
        }
    }
}
