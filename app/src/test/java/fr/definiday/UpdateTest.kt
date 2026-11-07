package fr.definiday

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
import java.io.ByteArrayOutputStream
import java.io.File

/** The self-update (Update.kt): what update.json may say, the APK's hash, and no request the user didn't allow. */
@RunWith(RobolectricTestRunner::class)
class UpdateTest {
    private val ctx = RuntimeEnvironment.getApplication()
    private val p = Prefs(ctx)
    private val door = Update.open
    private var calls = 0
    private val apkUrl = "https://github.com/monteenlairstudio-main/Definiday/releases/download/v0.13.0/definiday-0.13.0.apk"
    private val abc = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad" // SHA-256 of "abc"

    @After fun restore() { Update.open = door }

    /** Every request is answered with [body] (a local file), and counted. */
    private fun serve(body: String) {
        val f = File.createTempFile("served", null).apply { writeText(body); deleteOnExit() }
        Update.open = { calls++; f.toURI().toURL().openConnection() }
    }

    private fun json(code: Long = 13, apk: String = apkUrl, sha: String = abc) =
        """{"versionCode":$code,"versionName":"0.13.0","apk":"$apk","sha256":"$sha","minSdk":26,"notes":"Mise à jour sans perte."}"""

    @Test fun parsesAndComparesVersions() {
        val i = Update.parse(json())
        assertEquals(13L, i.code); assertEquals("0.13.0", i.name); assertEquals(apkUrl, i.apk); assertEquals("Mise à jour sans perte.", i.notes)
        assertTrue(Update.newer(i, current = 12, sdk = 26))
        assertFalse(Update.newer(i, current = 13, sdk = 36)) // same version
        assertFalse(Update.newer(i, current = 14, sdk = 36)) // never a downgrade
        assertFalse(Update.newer(i.copy(minSdk = 40), current = 12, sdk = 36)) // too recent for this phone
        assertEquals(abc, Update.parse(json(sha = abc.uppercase())).sha256)
        for (bad in listOf("{}", json(sha = "abc"), json(sha = abc.drop(1) + "g"), """{"versionCode":"x"}""", "pas du JSON"))
            assertTrue(bad, runCatching { Update.parse(bad) }.isFailure)
    }

    @Test fun onlyThisRepositoryOverHttps() {
        assertTrue(Update.trusted(apkUrl))
        assertTrue(Update.trusted(Update.LATEST))
        for (bad in listOf(
            apkUrl.replace("https:", "http:"),
            apkUrl.replace("github.com", "github.com.evil.example"),
            apkUrl.replace("github.com", "objects.githubusercontent.com"),
            apkUrl.replace("github.com", "evil@github.com"),
            apkUrl.replace("github.com", "github.com:8443"),
            apkUrl.replace("monteenlairstudio-main/Definiday", "someone/Definiday"),
            "https://github.com/monteenlairstudio-main/Definiday/releases/../../other/x.apk",
            "file:///sdcard/x.apk", "javascript:alert(1)", "",
        )) {
            assertFalse(bad, Update.trusted(bad))
            assertTrue(bad, runCatching { Update.parse(json(apk = bad)) }.isFailure)
        }
        serve("x")
        assertTrue(runCatching { Update.get("http://github.com/monteenlairstudio-main/Definiday/releases/x", ByteArrayOutputStream(), 10) }.isFailure)
        assertEquals("refused before any request", 0, calls)
    }

    @Test fun apkIsKeptOnlyWithTheAnnouncedHash() {
        val f = File(ctx.cacheDir, "update.apk")
        serve("abc")
        assertTrue(Update.download(Update.parse(json()), f))
        assertEquals(abc, Update.sha256(f))
        serve("abd") // tampered or truncated
        assertFalse(Update.download(Update.parse(json()), f))
        assertFalse("a bad file is not left behind", f.exists())
        assertTrue("size limit", runCatching { Update.get(apkUrl, ByteArrayOutputStream(), 2) }.isFailure)
    }

    @Test fun asksToAllowInstallsBeforeAnyDownload() {
        serve("abc")
        org.robolectric.Shadows.shadowOf(ctx.packageManager).setCanRequestPackageInstalls(false)
        val a = Robolectric.buildActivity(SettingsActivity::class.java).setup().get()
        var last = false
        Update.install(a, Update.parse(json())) { _, l -> last = l }
        assertTrue(last); assertEquals(0, calls)
        org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog().getButton(android.app.AlertDialog.BUTTON_POSITIVE).performClick()
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        assertEquals(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, org.robolectric.Shadows.shadowOf(a).nextStartedActivity.action)
    }

    @Test fun weeklyThrottle() {
        val day = 24 * 3600 * 1000L
        val now = 100 * day
        p.updateAuto = false; p.updateCheckedAt = 0
        assertFalse(Update.due(p, now))
        p.updateAuto = true
        assertTrue("never checked", Update.due(p, now))
        p.updateCheckedAt = now - day
        assertFalse(Update.due(p, now))
        p.updateCheckedAt = now - 7 * day
        assertTrue(Update.due(p, now))
        p.updateCheckedAt = now + day // the clock went back
        assertTrue(Update.due(p, now))
    }

    @Test fun noRequestUnlessAllowedThenOnceAWeek() {
        serve(json(code = 999))
        p.updateAuto = false; p.updateCheckedAt = 0
        Robolectric.buildActivity(MainActivity::class.java).setup()
        Robolectric.buildActivity(SettingsActivity::class.java).setup()
        assertEquals("toggle off: no network at all", 0, calls)
        p.updateAuto = true
        val a = Robolectric.buildActivity(SettingsActivity::class.java).setup().get()
        val now = System.currentTimeMillis()
        Update.autoCheck(a, now)!!.join()
        assertEquals(1, calls)
        assertNull("checked a moment ago", Update.autoCheck(a, now + 3600_000))
        assertEquals(1, calls)
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        val d = org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog()
        assertTrue("the newer version is offered", d?.isShowing == true)
        p.updateAuto = false
    }
}
