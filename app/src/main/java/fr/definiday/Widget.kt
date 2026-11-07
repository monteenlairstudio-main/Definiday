package fr.definiday

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.TextPaint
import android.util.SizeF
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import java.time.LocalDate

/**
 * Free home-screen widget: today's first item as a postcard in its palette, a stamp and today's postmark in a corner, a tap opens it.
 * Plain TextViews (crisp, read by TalkBack, autosized by the layout); the gradient is two tinted shapes, so the only bitmap is
 * the small stamp ([stamp]). No updatePeriodMillis: [update] runs from the daily alarm (DailyReceiver), which keeps firing while a widget
 * exists even with the daily word off; the system calls onUpdate itself after a boot or an app update.
 */
class Widget : AppWidgetProvider() {
    override fun onUpdate(ctx: Context, m: AppWidgetManager, ids: IntArray) = update(ctx)
    override fun onAppWidgetOptionsChanged(ctx: Context, m: AppWidgetManager, id: Int, options: Bundle) = update(ctx)
    override fun onEnabled(ctx: Context) = DailyReceiver.scheduleNext(ctx)
    override fun onDisabled(ctx: Context) = DailyReceiver.scheduleNext(ctx)

    companion object {
        // null without FEATURE_APP_WIDGETS (some Go / car / watch builds)
        private fun manager(ctx: Context): AppWidgetManager? = AppWidgetManager.getInstance(ctx)

        fun ids(ctx: Context): IntArray = manager(ctx)?.getAppWidgetIds(ComponentName(ctx, Widget::class.java)) ?: IntArray(0)

        /** Redraws every widget with today's first item: call it wherever today's items or their style change. */
        fun update(ctx: Context) {
            val m = manager(ctx) ?: return
            val ids = ids(ctx).takeIf { it.isNotEmpty() } ?: return
            val item = Words.today(ctx).first()
            ids.forEach { m.updateAppWidget(it, views(ctx, item, m.getAppWidgetOptions(it))) }
        }

        /** One layout per size the launcher may show (API 31+: it picks), else for the smallest one (portrait width, landscape height). */
        fun views(ctx: Context, item: Item, options: Bundle): RemoteViews {
            @Suppress("DEPRECATION") // the typed overload is API 33
            val sizes = if (Build.VERSION.SDK_INT >= 31) options.getParcelableArrayList<SizeF>(AppWidgetManager.OPTION_APPWIDGET_SIZES) else null
            return if (!sizes.isNullOrEmpty()) RemoteViews(sizes.associateWith { views(ctx, item, it.width, it.height) })
            else views(ctx, item, options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 110).toFloat(),
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110).toFloat())
        }

        /** The widget [wDp] x [hDp]: the word as large as fits beside Glose, else Glose steps aside for it (then 2 lines at 16 sp if it must);
         *  the text block gets the lines that fit at its smallest autosize, then an ellipsis. */
        fun views(ctx: Context, item: Item, wDp: Float, hDp: Float): RemoteViews {
            val style = Styles.of(ctx, item)
            val pal = style.palette
            val v = RemoteViews(ctx.packageName, R.layout.widget)
            v.setInt(R.id.widget_top, "setColorFilter", pal.haut)
            v.setInt(R.id.widget_bottom, "setColorFilter", pal.bas)
            fun text(id: Int, s: String, color: Int) {
                v.setTextViewText(id, s); v.setTextColor(id, color); v.setViewVisibility(id, if (s.isEmpty()) View.GONE else View.VISIBLE)
            }
            when (item) {
                is Word -> {
                    text(R.id.widget_mot, item.mot, pal.mot); text(R.id.widget_nature, item.nature, pal.nature)
                    text(R.id.widget_texte, item.definition, pal.texte); text(R.id.widget_auteur, "", pal.nature)
                }
                is Quote -> {
                    text(R.id.widget_mot, "", pal.mot); text(R.id.widget_nature, ctx.getString(R.string.quote_label), pal.nature)
                    text(R.id.widget_texte, "«\u00a0${item.texte}\u00a0»", pal.mot); text(R.id.widget_auteur, item.signature, pal.nature)
                }
            }
            // ponytail: sizes estimated from widget.xml (padding 2 x 16 + 6 dp, Glose 40 + 8 dp, lines ~1.2 em); measure the layout if they drift
            val fs = ctx.resources.configuration.fontScale
            val serif = TextPaint().apply { typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD); textSize = 28 * fs } // in dp
            fun fit(w: Float) = if (item is Word) 28 * w / serif.measureText(item.mot) else 28f
            val glose = fit(wDp - 80) >= 18 // beside Glose (40 dp + 8 dp margin)
            val full = fit(wDp - 32)
            val (motSp, motLines) = if (glose) minOf(fit(wDp - 80), 28f) to 1 else if (full >= 16) minOf(full, 28f) to 1 else 16f to 2
            v.setTextViewTextSize(R.id.widget_mot, TypedValue.COMPLEX_UNIT_SP, motSp)
            v.setInt(R.id.widget_mot, "setMaxLines", motLines)
            val used = 38 + maxOf(if (glose) 40f else 0f, (if (item is Word) motLines * motSp * 1.2f + 16 else 16f) * fs) + if (item is Quote) (if (item.signature.length * 7 > wDp - 32) 32 else 16) * fs else 0f
            v.setInt(R.id.widget_texte, "setMaxLines", ((hDp - used) / (14.5f * fs)).toInt().coerceAtLeast(1))
            val px = (40 * ctx.resources.displayMetrics.density).toInt() // ~120 px square: far below the RemoteViews bitmap budget
            v.setViewVisibility(R.id.widget_stamp, if (glose) View.VISIBLE else View.GONE)
            v.setImageViewBitmap(R.id.widget_stamp, stamp(ctx, item, style, px))
            // request code 100: apart from the notifications' 1-3 and the quiz's 50 (same component, extras do not count)
            v.setOnClickPendingIntent(android.R.id.background, PendingIntent.getActivity(ctx, 100,
                Intent(ctx, MainActivity::class.java).putExtra(MainActivity.EXTRA_MOT, item.key),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
            return v
        }

        /** The postcard's corner, [px] square: a stamp with the item's initial in its colors, today's postmark over its left edge.
         *  A bitmap because RemoteViews cannot draw paths nor set the word's font. */
        fun stamp(ctx: Context, item: Item, style: Style, px: Int, date: LocalDate = LocalDate.now()): Bitmap {
            val b = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
            val c = Canvas(b)
            val s = px.toFloat()
            val (initial, face) = Postal.initial(ctx, item, style)
            Postal.drawStamp(c, RectF(s * 0.36f, s * 0.04f, s * 0.98f, s * 0.78f), style.palette.mot, style.palette.haut, initial, face)
            Postal.drawPostmark(c, ctx, s * 0.3f, s * 0.68f, s * 0.28f, style.palette.nature, date, s * 0.62f, arcs = false)
            return b
        }

        /** « Ajouter le widget »: the launcher's pin dialog, or how to add it by hand when the launcher has none. */
        fun pin(ctx: Context) {
            val m = manager(ctx)
            if (m != null && m.isRequestPinAppWidgetSupported) m.requestPinAppWidget(ComponentName(ctx, Widget::class.java), null, null)
            else Toast.makeText(ctx, R.string.widget_manual, Toast.LENGTH_LONG).show()
        }
    }
}
