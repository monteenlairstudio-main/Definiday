package fr.definiday

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.text.TextPaint
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** The « Correspondance » objects drawn in code (design/styles/correspondance/implementation.md): stamp, postmark, Seyès ruling,
 *  Glose's yarn, the postcard and focus rings in a palette's colours. All decorative: their views get importantForAccessibility no. */
object Postal {
    /** Base class: a still drawable sized by its bounds. */
    abstract class Paper : Drawable() {
        override fun setAlpha(alpha: Int) {}
        override fun setColorFilter(colorFilter: ColorFilter?) {}
        @Deprecated("Deprecated in Java") override fun getOpacity() = PixelFormat.TRANSLUCENT
    }

    fun dp(ctx: Context) = ctx.resources.displayMetrics.density
    fun courier(ctx: Context): Typeface = ctx.resources.getFont(R.font.courier_prime)

    // Day tokens for the self-drawn surfaces (lock card, shared picture, widget), which ignore the app's night theme.
    const val YARN = 0xFF9A4250.toInt()
    const val YARN_LIGHT = 0xFFD88A97.toInt()
    const val KRAFT = 0xFFEBDDC2.toInt()
    const val SHEET = 0xFFFFFCF5.toInt()

    /** (solid strand, dashed strand) over [pal]'s gradient: the one that clears 3:1 on it carries the shape (min 3.6:1, Sépia). */
    fun strands(pal: Palette) = if (pal.sombre) YARN_LIGHT to YARN else YARN to YARN_LIGHT

    /** Postal mention: Courier Prime capitals spaced 0.13 em (« LETTRE DU 6 NOVEMBRE », « VIA DEFINIDAY »), at most four words.
     *  Fake bold: the file has no bold cut, and the thin regular fades on the gradients at label size. */
    fun mention(ctx: Context, size: Float, color: Int) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = courier(ctx); textSize = size; letterSpacing = 0.13f; this.color = color; isFakeBoldText = true
    }

    /** « 6 novembre », « 1er mai ». */
    fun day(d: LocalDate) = (if (d.dayOfMonth == 1) "1er" else "${d.dayOfMonth}") + " " + d.month.getDisplayName(TextStyle.FULL, Locale.FRENCH)

    /** The stamp's letter: the word's initial, the author's for a quote (its face: Styles.initialFace). */
    fun initial(item: Item) = (if (item is Word) item.mot else (item as Quote).auteur).take(1).uppercase(Locale.FRENCH)

    /** A stamp in the word's colours: perforated edge (8 dp pitch, 2.5 dp holes), [bg] face, inner frame and [initial] in [fg], [caption] at the foot;
     *  or a [vignette] picture on [fg] instead of the letter (shared picture: Glose's head).
     *  Bounds: 56 x 64 dp on the card (multiples of 8 dp elsewhere: 80 x 96 in the album, 24 x 30 in the quiz progress, without caption). */
    class Stamp(ctx: Context, private val bg: Int, private val fg: Int, private val initial: String, private val face: Typeface, private val caption: String? = "1 MOT",
                private val vignette: Bitmap? = null) : Paper() {
        private val d = dp(ctx)
        private val courier = courier(ctx)
        private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
        override fun getIntrinsicWidth() = (56 * d).toInt()
        override fun getIntrinsicHeight() = (64 * d).toInt()

        override fun draw(c: Canvas) {
            val b = RectF(bounds)
            val hole = 2.5f * d * (b.width() / (56 * d)).coerceIn(0.5f, 1.5f)
            val path = Path().apply { addRect(b, Path.Direction.CW) }
            val holes = Path()
            fun edge(x0: Float, y0: Float, x1: Float, y1: Float) { // holes centred on the edge, evenly spread, about 8 dp apart
                val len = Math.hypot((x1 - x0).toDouble(), (y1 - y0).toDouble()).toFloat()
                val n = (len / (8 * d * hole / (2.5f * d))).toInt().coerceAtLeast(2)
                for (i in 0..n) holes.addCircle(x0 + (x1 - x0) * i / n, y0 + (y1 - y0) * i / n, hole, Path.Direction.CW)
            }
            edge(b.left, b.top, b.right, b.top); edge(b.left, b.bottom, b.right, b.bottom)
            edge(b.left, b.top, b.left, b.bottom); edge(b.right, b.top, b.right, b.bottom)
            path.op(holes, Path.Op.DIFFERENCE)
            fill.style = Paint.Style.FILL; fill.color = bg
            c.drawPath(path, fill)
            val inset = hole * 2
            val frame = RectF(b.left + inset, b.top + inset, b.right - inset, b.bottom - inset)
            if (vignette != null) {
                fill.color = fg; c.drawRect(frame, fill)
                val s = minOf(frame.width(), frame.height()) * 0.92f
                c.drawBitmap(vignette, null, RectF(frame.centerX() - s / 2, frame.bottom - s, frame.centerX() + s / 2, frame.bottom), Paint(Paint.FILTER_BITMAP_FLAG))
            }
            fill.style = Paint.Style.STROKE; fill.strokeWidth = maxOf(1f, d * 0.8f); fill.color = fg
            c.drawRect(frame, fill)
            val foot = if (caption != null) frame.height() * 0.22f else 0f
            text.color = fg; text.typeface = face; text.textSize = (frame.height() - foot) * 0.62f
            val fm = text.fontMetrics
            if (vignette == null) c.drawText(initial, frame.centerX(), frame.top + (frame.height() - foot) / 2 - (fm.ascent + fm.descent) / 2, text)
            if (caption != null) {
                text.typeface = courier; text.textSize = foot * 0.55f; text.isFakeBoldText = true
                c.drawText(caption, frame.centerX(), frame.bottom - foot * 0.35f, text)
                text.isFakeBoldText = false
            }
        }
    }

    /** A dated postmark: double ring with « DEFINIDAY » and the weekday on the arcs, « 6 NOV / 2026 » in the middle (Courier Prime),
     *  three cancel waves running right over the stamp. [color] = the palette's nature, drawn at 75 %. The date is also written in clear
     *  elsewhere: this is decoration. Bounds: 100 x 56 dp, the ring in the left 56 dp (it overlaps a stamp placed at the right).
     *  Without [arcs] (under ~16 dp of radius, e.g. the widget, where arc text is a smudge): only the date, larger. */
    class Postmark(ctx: Context, color: Int, date: LocalDate = LocalDate.now(), private val arcs: Boolean = true) : Paper() {
        private val d = dp(ctx)
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; alpha = 191; typeface = courier(ctx); textAlign = Paint.Align.CENTER }
        private val day = date.format(DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH)).uppercase(Locale.FRENCH).replace(".", "")
        private val year = date.year.toString()
        private val weekday = date.format(DateTimeFormatter.ofPattern("EEEE", Locale.FRENCH)).uppercase(Locale.FRENCH)
        override fun getIntrinsicWidth() = (100 * d).toInt()
        override fun getIntrinsicHeight() = (56 * d).toInt()

        override fun draw(c: Canvas) {
            val b = bounds
            val r = b.height() / 2f - d
            val cx = b.left + r + d
            val cy = b.exactCenterY()
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.3f * d
            c.drawCircle(cx, cy, r, paint)
            paint.strokeWidth = 0.9f * d
            c.drawCircle(cx, cy, r * 0.66f, paint)
            // waves: from the inner ring out to the right edge
            val waves = Path()
            for (k in -1..1) {
                val y = cy + k * r * 0.42f
                waves.moveTo(cx + r * 0.75f, y)
                var x = cx + r * 0.75f
                val step = r * 0.5f
                var up = k % 2 == 0
                while (x < b.right - step / 2) { waves.rQuadTo(step / 2, if (up) -r * 0.12f else r * 0.12f, step, 0f); x += step; up = !up }
            }
            paint.strokeWidth = 1.1f * d
            c.drawPath(waves, paint)
            paint.style = Paint.Style.FILL
            val k = if (arcs) 1f else 1.25f
            paint.textSize = r * 0.3f * k
            paint.isFakeBoldText = true
            c.drawText(day, cx, cy - r * 0.04f * k, paint)
            paint.textSize = r * 0.26f * k
            c.drawText(year, cx, cy + r * 0.3f * k, paint)
            paint.isFakeBoldText = false
            if (!arcs) return
            paint.textSize = r * 0.24f
            // between the rings, centred on the arcs; upright on top and at the bottom
            paint.textAlign = Paint.Align.LEFT
            fun arc(s: String, radius: Float, start: Float, sweep: Float) = c.drawTextOnPath(s,
                Path().apply { addArc(RectF(cx - radius, cy - radius, cx + radius, cy + radius), start, sweep) },
                (radius * Math.toRadians(Math.abs(sweep).toDouble()).toFloat() - paint.measureText(s)) / 2, 0f, paint)
            arc("DEFINIDAY", r * 0.76f, 200f, 140f)
            arc(weekday, r * 0.9f, 160f, -140f)
            paint.textAlign = Paint.Align.CENTER
        }
    }

    /** Seyès ruling over a sheet, drawn from the real baselines of [text] (a child of the view this drawable is the background of):
     *  the strong line under each line of text, three thin ones between, continued to the top and bottom of the sheet.
     *  Text on it keeps 4.5:1 even on the strong line (colors.xml). Above font scale 1.6 the sheet stays plain (style.md § 15). */
    class Seyes(ctx: Context, private val text: TextView) : Paper() {
        private val d = dp(ctx)
        private val plain = ctx.resources.configuration.fontScale > 1.6f
        private val thin = Paint().apply { color = ctx.getColor(R.color.seyes); strokeWidth = maxOf(1f, d * 0.75f) }
        private val strong = Paint().apply { color = ctx.getColor(R.color.seyes_strong); strokeWidth = maxOf(1f, d) }
        private val radius = ctx.resources.getDimension(R.dimen.radius_paper)

        override fun draw(c: Canvas) {
            val l = text.layout ?: return
            if (plain || l.lineCount == 0) return
            val b = bounds
            val first = text.top + text.totalPaddingTop + l.getLineBaseline(0) + 1.5f * d // just under the letters
            val pitch = (if (l.lineCount > 1) l.getLineBaseline(1) - l.getLineBaseline(0) else l.getLineBottom(0) - l.getLineTop(0)) / 4f
            if (pitch < 2 * d) return
            var i = Math.floor(((b.top + radius - first) / pitch).toDouble()).toInt() + 1
            while (true) {
                val y = first + i * pitch
                if (y > b.bottom - radius / 2) break
                c.drawLine(b.left + d, y, b.right - d, y, if (Math.floorMod(i, 4) == 0 && y < text.bottom) strong else thin)
                i++
            }
        }
    }

    /** Glose's yarn, the screen's one quiet line: from the end of the thread in his drawing ([from], at 38 % / 97 % of the view) down the margin
     *  at [x] to the middle of [to]. Set as the background of [host] (a common ancestor), so it passes behind the papers and shows in the gaps.
     *  Two strands: yarn and a dashed yarn_light, one of which stays above 3:1 on any background. */
    class Yarn(ctx: Context, private val host: ViewGroup, private val from: View, private val to: View, private val x: Float) : Paper() {
        private val d = dp(ctx)
        private val dark = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeWidth = 2.4f * d; color = ctx.getColor(R.color.yarn) }
        private val light = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = 1f * d; color = ctx.getColor(R.color.yarn_light); pathEffect = DashPathEffect(floatArrayOf(1.6f * d, 2.8f * d), 0f)
        }
        private val r = Rect()

        override fun draw(c: Canvas) {
            if (from.width == 0 || to.width == 0 || !from.isShown || !to.isShown) return
            r.set(0, 0, from.width, from.height); host.offsetDescendantRectToMyCoords(from, r)
            val sx = r.left + r.width() * 0.38f
            val sy = r.top + r.height() * 0.97f
            r.set(0, 0, to.width, to.height); host.offsetDescendantRectToMyCoords(to, r)
            val ey = r.exactCenterY()
            if (ey < sy) return
            val drop = minOf(48 * d, (ey - sy) / 2)
            val p = Path().apply { moveTo(sx, sy); cubicTo(sx, sy + drop * 0.6f, x, sy + drop * 0.4f, x, sy + drop); lineTo(x, ey) }
            c.drawPath(p, dark)
            c.drawPath(p, light)
        }
    }

    /**
     * Glose's yarn as a free line (lock card, shared picture): from ([x0], [y]) to [x1], gently waving, ending in a curl. [pal]'s
     * strands: the solid one clears 3:1 on its gradient, the other is twisted over it in short dashes. [dp] = px of one dp at the picture's scale.
     */
    fun drawYarn(c: Canvas, x0: Float, x1: Float, y: Float, dp: Float, pal: Palette) {
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

    /** The postcard: the word's palette gradient inside a sheet rim and its card_edge rule (no Styles.needsEdge case left).
     *  [outer]: radius_card for the word's card, radius_small for a palette tile (Couleurs et polices). */
    fun postcard(ctx: Context, p: Palette, outer: Float = ctx.resources.getDimension(R.dimen.radius_card)): Drawable {
        val res = ctx.resources
        val rim = res.getDimension(R.dimen.card_rim)
        val sheet = GradientDrawable().apply { cornerRadius = outer; setColor(ctx.getColor(R.color.sheet)); setStroke(maxOf(1, dp(ctx).toInt()), ctx.getColor(R.color.card_edge)) }
        val face = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(p.haut, p.bas)).apply { cornerRadius = outer - rim }
        return LayerDrawable(arrayOf(sheet, face)).apply { val i = rim.toInt(); setLayerInset(1, i, i, i, i) }
    }

    /** Borderless target in [color] (actions on the postcard): ripple, 3 dp outline when focused. */
    fun ring(ctx: Context, color: Int): Drawable {
        val radius = ctx.resources.getDimension(R.dimen.radius_paper)
        val focus = GradientDrawable().apply { cornerRadius = radius; setStroke((3 * dp(ctx)).toInt(), color) }
        val mask = GradientDrawable().apply { cornerRadius = radius; setColor(0xFFFFFFFF.toInt()) }
        return RippleDrawable(ColorStateList.valueOf(color and 0xFFFFFF or 0x33000000),
            StateListDrawable().apply { addState(intArrayOf(android.R.attr.state_focused), focus); addState(intArrayOf(), android.graphics.drawable.ColorDrawable(0)) }, mask)
    }
}
