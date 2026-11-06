package fr.definiday

import android.app.Notification
import android.app.NotificationManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.ZonedDateTime

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "fr-w411dp-h891dp-xxhdpi")
class DefinidayTest {
    private val ctx = RuntimeEnvironment.getApplication()

    @Test fun datasetIsCleanAndCyclesWithoutRepeat() {
        val words = Words.load(ctx)
        assertTrue(words.size >= 100)
        assertEquals(words.size, words.map { it.mot.lowercase() }.toSet().size)
        words.forEach { assertTrue(it.mot, it.nature.isNotBlank() && it.definition.isNotBlank()) }
        assertEquals(words.toSet(), (0L until words.size).map { Words.forDay(words, 20_000 + it) }.toSet())
    }

    @Test fun nextTriggerIsTodayOrTomorrow() {
        val now = ZonedDateTime.parse("2026-11-06T09:30:00+01:00[Europe/Paris]")
        assertEquals(now.withHour(10).withMinute(0), DailyReceiver.nextTrigger(now, 10, 0))
        assertEquals(now.plusDays(1).withHour(8).withMinute(0), DailyReceiver.nextTrigger(now, 8, 0))
        assertEquals(now.plusDays(1), DailyReceiver.nextTrigger(now, 9, 30)) // exactly now -> tomorrow
    }

    @Test fun showPostsPublicTimedNotification() {
        Prefs(ctx).durationMinutes = 30
        DailyReceiver.show(ctx)
        val n = ctx.getSystemService(NotificationManager::class.java).activeNotifications.single().notification
        assertEquals(Notification.VISIBILITY_PUBLIC, n.visibility)
        assertEquals(30 * 60_000L, n.timeoutAfter)
        assertEquals(Words.today(ctx).mot, n.extras.getString(Notification.EXTRA_TITLE))
    }

    @Test fun wallpaperIsRemovedOnlyWhenDueAndStillOurs() {
        val p = Prefs(ctx).apply { wallpaper = true; durationMinutes = 15 }
        DailyReceiver.show(ctx)
        assertTrue(p.wallpaperEndAt > System.currentTimeMillis())
        val end = android.content.Intent(ctx, DailyReceiver::class.java).setAction(DailyReceiver.ACTION_END)
        DailyReceiver().onReceive(ctx, end) // early/stale END: keep it
        assertTrue(p.wallpaperEndAt != 0L)
        p.wallpaperEndAt = 1 // now due
        DailyReceiver().onReceive(ctx, end)
        assertEquals(0L, p.wallpaperEndAt)
    }

    /** Not a check: writes the demo PNGs (lock-screen card + main screen) to app/build/demo. */
    @Test fun renderDemoImages() {
        val out = File("build/demo").apply { mkdirs() }
        val words = Words.load(ctx)
        listOf(Words.today(ctx), words.maxBy { it.definition.length + it.exemple.length }).forEachIndexed { i, w ->
            save(WordCard.render(w, 1080, 2340), File(out, "lockscreen_$i.png"))
        }
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val root = activity.window.decorView
        root.measure(View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(4000, View.MeasureSpec.AT_MOST))
        root.layout(0, 0, 1080, root.measuredHeight)
        val bmp = Bitmap.createBitmap(1080, root.measuredHeight, Bitmap.Config.ARGB_8888)
        root.draw(Canvas(bmp))
        save(bmp, File(out, "app.png"))
    }

    private fun save(b: Bitmap, f: File) = f.outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
}
