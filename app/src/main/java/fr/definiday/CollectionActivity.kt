package fr.definiday

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** Glose's collection as a stamp album (« Correspondance »): a fixed header (Glose trying the outfit, title, count, « Choisir » once
 *  something changed), then one kraft album page. Unlocked items are perforated stamps held in transparent strips, the kept outfit
 *  cancelled « PORTÉ »; a locked item is an empty slot with what is missing (« Encore 8 quiz », « Revient en décembre »).
 *  A tap on an unlocked stamp tries it (selection outline), « Choisir » keeps it. */
class CollectionActivity : Activity() {
    private lateinit var p: Prefs
    private lateinit var preview: ImageView
    private lateinit var choose: Button
    private lateinit var trial: MutableMap<Unlocks.Kind, String>
    private val cells = mutableMapOf<String, Pair<View, ImageView?>>() // cell, its stamp (null when locked)
    private val dp by lazy { resources.displayMetrics.density }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        p = Prefs(this)
        Unlocks.update(p)
        trial = Glose.choice(p).toMutableMap()
        val pad = (16 * dp).toInt()
        // fixed header (Glose, title, count, "Choisir") above the scrolling album: what is tried stays in sight
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; fitsSystemWindows = true } // the insets replace its padding
        // the title on its own line (it stays short at large font sizes), then Glose beside the count and « Choisir »
        root.addView(TextView(this, null, 0, R.style.Title).apply { text = getString(R.string.collection_title, de(p.gloseName)); setPadding(pad, pad, pad, 0) })
        val head = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(pad, 0, pad, pad / 2) }.also(root::addView)
        preview = ImageView(this).apply {
            contentDescription = getString(R.string.glose, p.gloseName)
            val taps = Glose.Taps()
            setOnClickListener { Glose.tap(this, taps, Glose.pose(trial)) { Glose.drawable(context, it, Glose.styles(trial)) } }
        }
        head.addView(preview, LinearLayout.LayoutParams((112 * dp).toInt(), (112 * dp).toInt()))
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding((12 * dp).toInt(), 0, 0, 0) }
        head.addView(col, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        val got = (p.unlocked + Unlocks.of(emptyMap())).count(Unlocks.byId::containsKey) // the default look counts, as on the home bar
        col.addView(TextView(this, null, 0, R.style.Postal).apply { text = getString(R.string.collection_stamps, got, Unlocks.items.size); setPadding(0, (4 * dp).toInt(), 0, 0) })
        // the main action, shown only once something is tried (audit 011 A5: a disabled kraft button on the paper was invisible)
        choose = Button(this, null, 0, R.style.Primary).apply {
            text = getString(R.string.collection_choose)
            setOnClickListener { Glose.save(p, trial); refresh(); Widget.update(context) }
        }
        col.addView(choose, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = (8 * dp).toInt() })
        val album = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = resources.getDimension(R.dimen.radius_paper); setColor(getColor(R.color.surface)); setStroke(maxOf(1, dp.toInt()), getColor(R.color.card_edge))
            }
            setPadding((8 * dp).toInt(), 0, (8 * dp).toInt(), pad)
        }
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(pad, (4 * dp).toInt(), pad, pad * 3 / 2); addView(album) }
        root.addView(ScrollView(this).apply { addView(box) }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)
        Unlocks.Kind.values().forEach { kind ->
            val some = Unlocks.items.filter { it.kind == kind }
            val title = LinearLayout(this).apply { gravity = Gravity.BOTTOM; setPadding((8 * dp).toInt(), (20 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt()) }
            title.addView(TextView(this, null, 0, R.style.Label).apply { text = kind.titre }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            title.addView(TextView(this, null, 0, R.style.Postal).apply { text = getString(R.string.collection_section, some.count { it.id in p.unlocked }, some.size) })
            album.addView(title)
            album.addView(GridLayout(this).apply {
                columnCount = if (resources.configuration.fontScale >= 1.5f) 2 else 3 // names and hints stay whole words at large font sizes
                some.forEach { addView(cell(it)) }
                repeat(maxOf(0, columnCount - some.size)) { addView(android.widget.Space(context).apply { layoutParams = column() }) } // a short row keeps the column width
            })
        }
        refresh()
    }

    override fun onPause() {
        super.onPause()
        Glose.stop(preview)
    }

    private fun column() = GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED, 1f)).apply { width = 0 }

    private fun cell(item: Unlocks.Item): View {
        val open = item.id in p.unlocked
        val cell = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(0, (6 * dp).toInt(), 0, (8 * dp).toInt()) // no side padding: the strips of a row meet
            layoutParams = column()
        }
        // unlocked: a stamp (one bitmap, not one cache per layer); locked: the empty slot in the page, solid edge (dashed = « touch to reveal »)
        val stamp = if (open) stamp(this, Glose.preview(this, item)) else null
        cell.addView(stamp ?: View(this).apply {
            background = GradientDrawable().apply { setColor(getColor(R.color.kraft_dark) and 0xFFFFFF or 0x30000000); setStroke(maxOf(1, dp.toInt()), getColor(R.color.kraft_dark)) }
            layoutParams = LinearLayout.LayoutParams((80 * dp).toInt(), (96 * dp).toInt())
        })
        cell.foreground = Strip(this, cell.getChildAt(0))
        cell.addView(TextView(this, null, 0, R.style.Body).apply { text = item.nom; textSize = 14f; gravity = Gravity.CENTER; setLineSpacing(0f, 1f); setPadding((4 * dp).toInt(), (6 * dp).toInt(), (4 * dp).toInt(), 0) })
        if (!open) cell.addView(TextView(this, null, 0, R.style.Body).apply { text = Unlocks.hint(item, p); textSize = 13f; setLineSpacing(0f, 1f); gravity = Gravity.CENTER; setTextColor(getColor(R.color.ink2)); setPadding((4 * dp).toInt(), 0, (4 * dp).toInt(), 0) })
        if (open) {
            cell.background = RippleDrawable(ColorStateList.valueOf(getColor(R.color.ripple)), StateListDrawable().apply {
                addState(intArrayOf(android.R.attr.state_focused), outline(3))
                addState(intArrayOf(android.R.attr.state_selected), outline(2))
            }, GradientDrawable().apply { cornerRadius = resources.getDimension(R.dimen.radius_paper); setColor(0xFFFFFFFF.toInt()) })
            cell.setOnClickListener {
                // a second tap on the worn accessory takes it off
                trial[item.kind.slot] = if (item.kind in Unlocks.ACC && trial[item.kind] == item.id) "" else item.id
                refresh()
            }
        }
        cells[item.id] = cell to stamp
        return cell
    }

    /** The one selection look: an accent outline, 2 dp (3 dp for keyboard focus). */
    private fun outline(w: Int) = GradientDrawable().apply { cornerRadius = resources.getDimension(R.dimen.radius_paper); setStroke((w * dp).toInt(), getColor(R.color.accent)) }

    private fun refresh() {
        preview.setImageDrawable(Glose.drawable(this, Glose.pose(trial), Glose.styles(trial)))
        Glose.play(preview)
        val kept = Glose.choice(p)
        choose.visibility = if (trial != kept) View.VISIBLE else View.INVISIBLE // keeps its room: the header does not jump
        cells.forEach { (id, v) ->
            val (cell, stamp) = v
            val item = Unlocks.byId.getValue(id)
            val worn = id in kept.values
            cell.isSelected = id in trial.values && !worn // being tried: said to TalkBack, not only drawn
            stamp?.foreground = if (worn) Cancel(this) else null
            stamp?.foregroundGravity = Gravity.TOP or Gravity.END // over the plain sky of the stamp, above the strip: « PORTÉ » stays legible
            cell.contentDescription = item.nom + when {
                stamp == null -> ", " + getString(R.string.collection_locked) + ", " + Unlocks.hint(item, p)
                worn -> ", " + getString(R.string.collection_worn)
                else -> ""
            }
        }
    }

    /** The cancel on a kept stamp: a wax double ring, « PORTÉ » across it, slightly turned; decorative (TalkBack says « dans la tenue »). */
    private class Cancel(ctx: Context) : Postal.Paper() {
        private val d = Postal.dp(ctx)
        private val word = ctx.getString(R.string.collection_cancel)
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ctx.getColor(R.color.wax); alpha = 230; typeface = Postal.courier(ctx); textAlign = Paint.Align.CENTER; isFakeBoldText = true }
        override fun getIntrinsicWidth() = (44 * d).toInt()
        override fun getIntrinsicHeight() = (44 * d).toInt()

        override fun draw(c: Canvas) {
            val cx = bounds.exactCenterX(); val cy = bounds.exactCenterY(); val r = minOf(bounds.width(), bounds.height()) / 2f - d
            c.save(); c.rotate(-16f, cx, cy)
            paint.style = Paint.Style.STROKE; paint.strokeWidth = 1.4f * d
            c.drawCircle(cx, cy, r, paint)
            paint.strokeWidth = 0.8f * d
            c.drawCircle(cx, cy, r * 0.8f, paint)
            paint.style = Paint.Style.FILL; paint.textSize = r * 0.5f
            c.drawText(word, cx, cy - (paint.ascent() + paint.descent()) / 2, paint)
            c.restore()
        }
    }

    /** The album's transparent holding strip across the lower part of the cell's [stamp] (cell foreground: the strips of a row meet). */
    private class Strip(ctx: Context, private val stamp: View) : Postal.Paper() {
        private val d = Postal.dp(ctx)
        private val night = ctx.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        private val film = Paint().apply { color = if (night) 0x1AFFFFFF else 0x59FFFFFF }
        private val edge = Paint().apply { color = ctx.getColor(R.color.card_edge); strokeWidth = maxOf(1f, d) }

        override fun draw(c: Canvas) {
            val top = stamp.top + stamp.height * 0.5f
            c.drawRect(bounds.left.toFloat(), top, bounds.right.toFloat(), stamp.bottom + 3 * d, film)
            c.drawLine(bounds.left.toFloat(), top, bounds.right.toFloat(), top, edge)
        }
    }

    companion object {
        /** A collection item as an album stamp, 80 x 96 dp: perforated paper ([bg]: sheet on the kraft page, kraft on a sheet),
         *  Glose [glose] at its foot as one still bitmap. Decorative. */
        fun stamp(ctx: Context, glose: Drawable, bg: Int = R.color.sheet) = ImageView(ctx).apply {
            val d = ctx.resources.displayMetrics.density
            background = Postal.Stamp(ctx, ctx.getColor(bg), ctx.getColor(R.color.card_edge), "", Typeface.SERIF, caption = null)
            val pad = (8 * d).toInt()
            setPadding(pad, pad, pad, pad)
            scaleType = ImageView.ScaleType.FIT_END
            val side = (80 * d).toInt() - 2 * pad
            setImageBitmap(Glose.bitmap(glose, side, side))
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            layoutParams = LinearLayout.LayoutParams((80 * d).toInt(), (96 * d).toInt())
        }
    }
}
