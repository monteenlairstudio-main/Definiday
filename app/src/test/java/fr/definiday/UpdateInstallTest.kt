package fr.definiday

import android.content.Intent
import android.content.pm.PackageInstaller
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowLooper
import org.robolectric.shadows.ShadowToast
import java.io.File
import java.io.IOException

/** Idles the main looper until [done], for at most 10 s: the update's worker threads post their results there. */
internal fun idleUntil(done: () -> Boolean) {
    val end = System.nanoTime() + 10_000_000_000
    while (!done()) { check(System.nanoTime() < end) { "timed out" }; ShadowLooper.idleMainLooper(); Thread.yield() }
}

/** The install flow (Update.install, UpdateReceiver): what the user sees at each step, PackageInstaller included. */
@RunWith(RobolectricTestRunner::class)
class UpdateInstallTest {
    private val ctx = RuntimeEnvironment.getApplication()
    private val door = Update.open
    private val info = Update.parse("""{"versionCode":999,"versionName":"9.0.0","apk":"https://github.com/monteenlairstudio-main/Definiday/releases/download/v9/d.apk",""" +
        """"sha256":"ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"}""") // SHA-256 of "abc"

    @After fun restore() { Update.open = door }

    private fun receive(status: Int?, confirm: Intent? = null) = UpdateReceiver().onReceive(ctx, Intent().apply {
        status?.let { putExtra(PackageInstaller.EXTRA_STATUS, it) }
        confirm?.let { putExtra(Intent.EXTRA_INTENT, it) }
    })

    @Test fun receiverShowsTheConfirmationScreen() {
        receive(PackageInstaller.STATUS_PENDING_USER_ACTION, Intent("android.content.pm.action.CONFIRM_INSTALL"))
        val started = shadowOf(ctx).nextStartedActivity
        assertEquals("android.content.pm.action.CONFIRM_INSTALL", started.action)
        assertTrue("started from a receiver: needs a new task", started.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        receive(PackageInstaller.STATUS_PENDING_USER_ACTION) // no screen to show: nothing, no crash
        assertNull(shadowOf(ctx).nextStartedActivity)
        assertEquals(0, ShadowToast.shownToastCount())
    }

    @Test fun receiverIsSilentOnSuccess() {
        receive(PackageInstaller.STATUS_SUCCESS) // the new version replaces and restarts the app
        assertEquals(0, ShadowToast.shownToastCount())
        assertNull(shadowOf(ctx).nextStartedActivity)
    }

    @Test fun receiverExplainsEachFailure() {
        for ((status, text) in listOf(
            PackageInstaller.STATUS_FAILURE_ABORTED to R.string.update_aborted,
            PackageInstaller.STATUS_FAILURE_CONFLICT to R.string.update_other_key,
            PackageInstaller.STATUS_FAILURE_STORAGE to R.string.update_storage,
            PackageInstaller.STATUS_FAILURE to R.string.update_failed,
            PackageInstaller.STATUS_FAILURE_INCOMPATIBLE to R.string.update_failed,
            12345 to R.string.update_failed,
            null to R.string.update_failed, // no status at all
        )) {
            receive(status)
            assertEquals("status $status", ctx.getString(text), ShadowToast.getTextOfLatestToast())
        }
    }

    /** Runs Update.install with every request answered by [body] (null: the network fails); returns the last status. */
    private fun install(body: String?): String {
        Update.open = { url ->
            if (body == null) throw IOException("offline")
            File.createTempFile("apk", null).apply { writeText(body); deleteOnExit() }.toURI().toURL().openConnection()
        }
        shadowOf(ctx.packageManager).setCanRequestPackageInstalls(true)
        val a = Robolectric.buildActivity(SettingsActivity::class.java).setup().get()
        val seen = mutableListOf<String>()
        var last: String? = null
        Update.install(a, info) { text, end -> seen += text; if (end) last = text }
        assertEquals("the download starts at once", ctx.getString(R.string.update_downloading, 0), seen.first())
        idleUntil { last != null }
        assertFalse("no APK left in the cache", File(a.cacheDir, "update.apk").exists())
        return last!!
    }

    private val sessions get() = ctx.packageManager.packageInstaller.allSessions

    @Test fun verifiedApkGoesToTheSystemInstaller() {
        assertEquals(ctx.getString(R.string.update_installing), install("abc"))
        val s = sessions.single()
        assertEquals(ctx.packageName, s.appPackageName)
        shadowOf(ctx.packageManager.packageInstaller).setSessionFails(s.sessionId) // Android answers through UpdateReceiver, and only it
        assertEquals(UpdateReceiver::class.java.name, shadowOf(ctx).broadcastIntents.last().component?.className)
    }

    @Test fun tamperedApkIsNeverInstalled() {
        assertEquals(ctx.getString(R.string.update_bad_hash), install("abd"))
        assertTrue(sessions.isEmpty())
    }

    @Test fun networkFailureSaysSo() {
        assertEquals(ctx.getString(R.string.update_failed), install(null))
        assertTrue(sessions.isEmpty())
    }
}
