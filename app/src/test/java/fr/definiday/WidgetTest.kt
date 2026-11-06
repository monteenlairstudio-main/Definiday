package fr.definiday

import android.app.AlarmManager
import android.appwidget.AppWidgetManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "fr-w411dp-h891dp-xxhdpi")
class WidgetTest {
    private val ctx = RuntimeEnvironment.getApplication()
    private val am = AppWidgetManager.getInstance(ctx)
    private val widgets = shadowOf(am)

    /** Today's items are [items] (first = the widget's). */
    private fun today(vararg items: Item) = Prefs(ctx).run { dailyDay = LocalDate.now().toEpochDay(); dailyMots = items.map { it.key } }

    private fun word(mot: String) = Words.load(ctx).first { it.mot == mot }
    private fun texts(v: View) = listOf(R.id.widget_mot, R.id.widget_nature, R.id.widget_texte, R.id.widget_auteur)
        .map { v.findViewById<TextView>(it).run { if (visibility == View.VISIBLE) text.toString() else "" } }

    @Test fun showsTodaysWordAndOpensIt() {
        val w = word("zinzolin")
        today(w)
        val id = widgets.createWidget(Widget::class.java, R.layout.widget)
        val v = widgets.getViewFor(id)
        assertEquals(listOf(w.mot, w.nature, w.definition, ""), texts(v))
        assertEquals(Styles.of(ctx, w).palette.mot, v.findViewById<TextView>(R.id.widget_mot).currentTextColor)
        v.performClick()
        val open = shadowOf(ctx).nextStartedActivity
        assertEquals(MainActivity::class.java.name, open.component?.className)
        assertEquals(w.mot, open.getStringExtra(MainActivity.EXTRA_MOT))
    }

    @Test fun showsAQuote() {
        val q = Quotes.load(ctx).first()
        today(q)
        val v = widgets.getViewFor(widgets.createWidget(Widget::class.java, R.layout.widget))
        assertEquals(listOf("", ctx.getString(R.string.quote_label), "«\u00a0${q.texte}\u00a0»", q.signature), texts(v))
    }

    /** The daily alarm's show() redraws it; a resize keeps the texts. */
    @Test fun followsTheDailyShowAndResizes() {
        today(word("zinzolin"))
        val id = widgets.createWidget(Widget::class.java, R.layout.widget)
        val w = word("incunable")
        today(w)
        DailyReceiver.show(ctx)
        assertEquals(w.mot, texts(widgets.getViewFor(id))[0])
        // pre-12 options, then the 12+ list of sizes (one layout each)
        val sizes = arrayListOf(android.util.SizeF(110f, 110f), android.util.SizeF(340f, 160f))
        listOf(Bundle().apply { putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 110); putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110) },
            Bundle().apply { putParcelableArrayList(AppWidgetManager.OPTION_APPWIDGET_SIZES, sizes) }).forEach { o ->
            am.updateAppWidgetOptions(id, o)
            widgets.getAppWidgetProviderFor(id).onAppWidgetOptionsChanged(ctx, am, id, o)
            assertEquals(listOf(w.mot, w.nature, w.definition, ""), texts(widgets.getViewFor(id)))
        }
    }

    /** Daily word off: the alarm still runs while a widget is on the home screen, to change it. */
    @Test fun alarmKeptForTheWidget() {
        Prefs(ctx).enabled = false
        widgets.createWidget(Widget::class.java, R.layout.widget)
        DailyReceiver.scheduleNext(ctx)
        assertNotNull(shadowOf(ctx.getSystemService(AlarmManager::class.java)).nextScheduledAlarm)
    }

    /** Not a check: widget PNGs in app/build/demo/widget (2x2, 4x2, dark palette, quote, long word, preview). */
    @Test fun renderWidgets() {
        val out = File("build/demo/widget").apply { mkdirs() }
        val d = ctx.resources.displayMetrics.density
        fun render(item: Item?, wDp: Int, hDp: Int, name: String, scale: Float = 1f) {
            val parent = FrameLayout(ctx)
            val v = if (item == null) android.view.LayoutInflater.from(ctx).inflate(R.layout.widget, parent, false)
                else Widget.views(ctx, item, wDp.toFloat(), hDp.toFloat()).apply(ctx, parent)
            parent.addView(v)
            val (w, h) = (wDp * d).toInt() to (hDp * d).toInt()
            parent.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY))
            parent.layout(0, 0, w, h)
            val b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            if (item != null) b.eraseColor(0xFF5A6B7D.toInt()) // a wallpaper-ish backdrop; the preview stays transparent
            parent.draw(Canvas(b))
            val o = if (scale == 1f) b else Bitmap.createScaledBitmap(b, (w * scale).toInt(), (h * scale).toInt(), true)
            File(out, "$name.png").outputStream().use { o.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        val words = Words.load(ctx)
        render(word("zinzolin"), 160, 160, "2x2_dark")
        render(word("sérendipité"), 160, 160, "2x2")
        render(word("sérendipité"), 340, 160, "4x2")
        render(word("zinzolin"), 340, 160, "4x2_dark")
        render(words.maxBy { it.mot.length }, 160, 160, "2x2_long_word")
        render(words.maxBy { it.definition.length }, 160, 160, "2x2_long_def")
        render(words.maxBy { it.definition.length }, 340, 110, "4x1.5_long_def")
        render(Quotes.load(ctx).maxBy { it.texte.length }, 340, 160, "4x2_quote")
        render(null, 340, 160, "preview", scale = 1 / 1.5f) // -> res/drawable-nodpi/widget_preview.png (picker before Android 12)
    }
}
