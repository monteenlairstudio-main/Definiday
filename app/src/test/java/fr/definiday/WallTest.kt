package fr.definiday

import android.app.Notification
import android.app.NotificationManager
import android.app.WallpaperManager
import android.app.WallpaperManager.FLAG_LOCK
import android.app.WallpaperManager.FLAG_SYSTEM
import android.view.View
import android.widget.TextView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowBuild

/** The lock-wallpaper guard (docs/bug_samsung_fond.md): never write over a lock wallpaper that is not ours. TestApp: shared lock. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "fr-w411dp-h891dp-xxhdpi")
class WallTest {
    private val ctx = RuntimeEnvironment.getApplication()
    private val p get() = Prefs(ctx)
    private val wm get() = shadowOf(WallpaperManager.getInstance(ctx))
    private val nm get() = ctx.getSystemService(NotificationManager::class.java)

    @Test fun sharedLockGetsTheCardThenItsRemoval() {
        p.mode = Prefs.MODE_LOCK
        DailyReceiver.show(ctx)
        assertNotNull(wm.getBitmap(FLAG_LOCK))
        assertNull("the home screen is never written", wm.getBitmap(FLAG_SYSTEM))
        assertEquals(0, nm.activeNotifications.size)
        assertFalse(p.lockBlocked)
        assertEquals(p.wallpaperId, Wall.lockId(ctx)) // our card, so the removal clears it
        p.wallpaperEndAt = 1
        DailyReceiver.clearWallpaper(ctx)
        assertEquals(0L, p.wallpaperEndAt)
    }

    @Test fun separateLockIsNeverTouchedAndTheWordComesByNotification() {
        Wall.lockId = { 42 }
        p.mode = Prefs.MODE_LOCK // lock only: the word must still reach the user
        DailyReceiver.show(ctx)
        assertNull(wm.getBitmap(FLAG_LOCK))
        assertEquals(0L, p.wallpaperEndAt)
        assertTrue(p.lockBlocked)
        val n = nm.activeNotifications.single().notification
        assertEquals(DailyReceiver.texts(Words.today(ctx).first()).first, n.extras.getString(Notification.EXTRA_TITLE))
    }

    @Test fun ourOwnCardIsReplaced() {
        p.mode = Prefs.MODE_BOTH
        DailyReceiver.show(ctx)
        val first = wm.getBitmap(FLAG_LOCK)
        DailyReceiver.show(ctx, listOf(Words.random(ctx))) // the lock id is now ours (TestApp)
        assertNotSame(first, wm.getBitmap(FLAG_LOCK))
        assertFalse(p.lockBlocked)
    }

    @Test fun liveOrUnreadableLockIsRefused() {
        p.mode = Prefs.MODE_BOTH
        Wall.lockLive = { true }
        DailyReceiver.show(ctx)
        assertNull(wm.getBitmap(FLAG_LOCK)); assertTrue(p.lockBlocked)
        Wall.lockLive = { false }
        Wall.lockId = { throw SecurityException("OEM") }
        DailyReceiver.show(ctx)
        assertNull(wm.getBitmap(FLAG_LOCK)); assertTrue(p.lockBlocked)
    }

    @Test fun randomPickRespectsTheGuard() {
        Wall.lockId = { 42 }
        p.mode = Prefs.MODE_LOCK
        val a = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        a.findViewById<View>(R.id.new_word).performClick()
        assertNull(wm.getBitmap(FLAG_LOCK))
        assertEquals(1, nm.activeNotifications.size)
        assertEquals("« replaces the lock-screen word » would be false", View.GONE, a.findViewById<View>(R.id.random_hint).visibility)
    }

    @Test fun settingsAndOnboardingExplainAnOwnLockWallpaper() {
        p.mode = Prefs.MODE_LOCK
        fun settings() = Robolectric.buildActivity(SettingsActivity::class.java).setup().get()
        fun android.app.Activity.note() = findViewById<TextView>(R.id.lock_own)
        fun android.app.Activity.hint() = findViewById<View>(R.id.wallpaper_hint).visibility
        settings().let { assertEquals(View.GONE, it.note().visibility); assertEquals(View.VISIBLE, it.hint()) }
        Wall.lockId = { 42 }
        settings().let {
            assertEquals(View.VISIBLE, it.note().visibility); assertEquals(View.GONE, it.hint())
            assertEquals(ctx.getString(R.string.lock_own), it.note().text.toString())
        }
        ShadowBuild.setManufacturer("samsung")
        assertTrue(settings().note().text.endsWith(ctx.getString(R.string.lock_own_samsung)))
        p.mode = Prefs.MODE_NOTIF
        settings().let { assertEquals(View.GONE, it.note().visibility); assertEquals(View.GONE, it.hint()) }

        p.mode = Prefs.MODE_BOTH
        p.onboarded = false
        val o = Robolectric.buildActivity(OnboardingActivity::class.java).setup().get()
        o.findViewById<View>(R.id.next).performClick() // step 2, « Où »
        assertEquals(View.VISIBLE, o.note().visibility); assertEquals(View.GONE, o.hint())
    }
}
