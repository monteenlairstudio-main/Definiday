package fr.definiday

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Postal motifs of the « Correspondance » style, drawn on any Canvas (lock card, shared picture, widget stamp, later the app).
 * Sizes are in px; [dp] arguments are the px of one dp at the drawing's scale. All purely decorative: the date and the word are
 * always written in clear text elsewhere, so none of this needs a contentDescription.
 */
object Postal {
    /** Wool strands (Glose's ball): dark `fil`, light `fil_clair`. On the 18 palettes one of them always clears 3:1 (min 3.6, sepia). */
    const val FIL = 0xFF9A4250.toInt()
    const val FIL_CLAIR = 0xFFD88A97.toInt()
    const val KRAFT = 0xFFEBDDC2.toInt()
    const val FEUILLET = 0xFFFFFCF5.toInt()

    /** (solid strand, twisted dashes) for [pal]: the strand that stands out on its background carries the shape. */
    fun strands(pal: Palette) = if (pal.sombre) FIL_CLAIR to FIL else FIL to FIL_CLAIR

    /** Courier Prime capitals, spaced 0.13 em: « LETTRE DU 6 NOVEMBRE », « VIA DEFINIDAY ». At most four words. */
    fun mention(ctx: Context, size: Float, color: Int) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = ctx.resources.getFont(R.font.courier_prime); textSize = size; letterSpacing = 0.13f; this.color = color
        isFakeBoldText = true // the file has no bold cut; at label size the thin regular fades on the gradients
    }

    /** « 6 novembre », « 1er mai ». */
    fun day(d: LocalDate) = (if (d.dayOfMonth == 1) "1er" else "${d.dayOfMonth}") + " " + d.month.getDisplayName(TextStyle.FULL, Locale.FRENCH)

    /** The stamp's initial and its face: the word's suggested font even when decorative (one letter stays legible), the author's for a quote. */
    fun initial(ctx: Context, item: Item, style: Style): Pair<String, Typeface> = when (item) {
        is Word -> item.mot.take(1).uppercase(Locale.FRENCH) to
            (Styles.load(ctx).polices.firstOrNull { it.id == item.police }?.let { Styles.face(ctx, it) } ?: style.typeface)
        is Quote -> item.auteur.take(1).uppercase(Locale.FRENCH) to style.typeface
    }

    /** Perforated stamp in [r]: [paper] sheet with round teeth cut out (Path.op), thin [ink] frame, then the [initial] in [face] or the [vignette]. */
    fun drawStamp(c: Canvas, r: RectF, paper: Int, ink: Int, initial: String? = null, face: Typeface? = null, vignette: Bitmap? = null) {
        val hole = r.width() / 22
        val sheet = Path().apply { addRect(r, Path.Direction.CW) }
        val teeth = Path()
        fun edge(len: Float, at: (Float) -> Unit) { val n = (len / (hole * 3.2f)).toInt().coerceAtLeast(2); for (i in 0..n) at(i * len / n) }
        edge(r.width()) { x -> teeth.addCircle(r.left + x, r.top, hole, Path.Direction.CW); teeth.addCircle(r.left + x, r.bottom, hole, Path.Direction.CW) }
        edge(r.height()) { y -> teeth.addCircle(r.left, r.top + y, hole, Path.Direction.CW); teeth.addCircle(r.right, r.top + y, hole, Path.Direction.CW) }
        sheet.op(teeth, Path.Op.DIFFERENCE)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = paper }
        c.drawPath(sheet, p)
        val inner = RectF(r).apply { inset(hole * 2.4f, hole * 2.4f) }
        if (vignette != null) {
            p.color = ink; c.drawRect(inner, p)
            val s = minOf(inner.width(), inner.height()) * 0.92f
            c.drawBitmap(vignette, null, RectF(inner.centerX() - s / 2, inner.bottom - s, inner.centerX() + s / 2, inner.bottom), Paint(Paint.FILTER_BITMAP_FLAG))
        }
        p.apply { color = ink; style = Paint.Style.STROKE; strokeWidth = hole * 0.45f }
        c.drawRect(inner, p)
        if (initial == null) return
        val t = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = face; color = ink; textSize = inner.height() * 0.62f }
        val glyph = android.graphics.Rect().also { t.getTextBounds(initial, 0, initial.length, it) } // centre the ink, not the font's box
        c.drawText(initial, inner.centerX() - glyph.exactCenterX(), inner.centerY() - glyph.exactCenterY(), t)
    }

    /**
     * Dated postmark centred on ([cx], [cy]), radius [r], in [color] at 75 %: double ring, « DEFINIDAY » on the upper arc, the
     * weekday on the lower one, « 6 NOV » and the year inside, and three cancellation waves running right up to [wavesTo].
     * Without [arcs] (small sizes, e.g. the widget, where arc text would be a smudge) only the larger date remains.
     */
    fun drawPostmark(c: Canvas, ctx: Context, cx: Float, cy: Float, r: Float, color: Int, date: LocalDate, wavesTo: Float, arcs: Boolean = true) {
        val ink = color and 0xFFFFFF or (0xBF shl 24)
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = ink; style = Paint.Style.STROKE; strokeWidth = r * 0.045f }
        c.drawCircle(cx, cy, r, line)
        c.drawCircle(cx, cy, r * 0.7f, line)
        val t = mention(ctx, r * 0.19f, ink).apply { letterSpacing = 0.08f; isFakeBoldText = false }
        fun arc(text: String, radius: Float, top: Boolean) {
            val oval = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
            val path = Path().apply { addArc(oval, 180f, if (top) 180f else -180f) } // lower arc counter-clockwise: reads left to right
            c.drawTextOnPath(text, path, ((Math.PI * radius).toFloat() - t.measureText(text)) / 2, 0f, t)
        }
        if (arcs) {
            arc("DEFINIDAY", r * 0.77f, top = true)
            arc(date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.FRENCH).uppercase(Locale.FRENCH), r * 0.93f, top = false)
        }
        val k = if (arcs) 1f else 1.3f
        t.textAlign = Paint.Align.CENTER
        t.textSize = r * 0.27f * k; t.isFakeBoldText = true
        c.drawText("${date.dayOfMonth} " + date.month.getDisplayName(TextStyle.SHORT, Locale.FRENCH).trimEnd('.').uppercase(Locale.FRENCH), cx, cy - r * 0.02f * k, t)
        t.textSize = r * 0.21f * k; t.isFakeBoldText = false
        c.drawText("${date.year}", cx, cy + r * 0.3f * k, t)
        val wave = Path()
        for (k in -1..1) {
            val y = cy + k * r * 0.32f
            var x = cx + r * 0.92f
            wave.moveTo(x, y)
            val step = r * 0.45f
            var up = true
            while (x < wavesTo) { wave.quadTo(x + step / 2, y + (if (up) -1 else 1) * r * 0.16f, x + step, y); x += step; up = !up }
        }
        c.drawPath(wave, line)
    }

    /**
     * Glose's wool thread from ([x0], [y]) to [x1], gently waving, ending in a curl: [pal]'s two strands (strands), the second as
     * short dashes twisted over the first, so it reads on any gradient.
     */
    fun drawThread(c: Canvas, x0: Float, x1: Float, y: Float, dp: Float, pal: Palette) {
        val path = Path().apply {
            moveTo(x0, y)
            var x = x0
            var up = true
            val step = 26 * dp
            while (x + step < x1) { quadTo(x + step / 2, y + (if (up) -1.6f else 1.6f) * dp, x + step, y); x += step; up = !up }
            lineTo(x1, y)
            // the curl: up and back over itself, then a short loose end
            cubicTo(x1 + 7 * dp, y, x1 + 10 * dp, y - 5 * dp, x1 + 8 * dp, y - 10 * dp)
            cubicTo(x1 + 6 * dp, y - 15 * dp, x1, y - 13 * dp, x1 + 1.5f * dp, y - 8 * dp)
            cubicTo(x1 + 3 * dp, y - 3 * dp, x1 + 9 * dp, y + 2.5f * dp, x1 + 15 * dp, y + 1.5f * dp)
        }
        val (solid, twist) = strands(pal)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; color = solid; strokeWidth = 2.6f * dp }
        c.drawPath(path, p)
        p.apply { color = twist; strokeWidth = 1.1f * dp; pathEffect = DashPathEffect(floatArrayOf(1.6f * dp, 2.8f * dp), 0f) }
        c.drawPath(path, p)
    }
}
