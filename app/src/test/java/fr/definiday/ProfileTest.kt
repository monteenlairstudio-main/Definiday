package fr.definiday

import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Button
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowDialog
import org.robolectric.shadows.ShadowLooper
import org.robolectric.shadows.ShadowToast
import java.io.File

/** Mon profil: export / import (Profile), and the test build installed beside the real app. */
@RunWith(RobolectricTestRunner::class)
class ProfileTest {
    private val ctx = RuntimeEnvironment.getApplication()
    private fun sp(f: String) = ctx.getSharedPreferences(f, Context.MODE_PRIVATE)
    private fun profile() = Profile.FILES.associateWith { sp(it).all.toMap() }
    private fun uri(text: String? = null) = Uri.fromFile(File(ctx.cacheDir, "profil.json").apply { if (text != null) writeText(text) })

    private fun fill() {
        sp("definiday").edit().putInt("hour", 7).putLong("dailyDay", 20_400).putBoolean("sounds", true).putString("glosePose", "livres")
            .putStringSet("unlocked", setOf("livres", "nuit")).putStringSet("palettes", emptySet()).commit()
        sp("history").edit().putLong("abaque", 20_399).putLong("q:x", 20_400).commit()
        sp("memoire").edit().putString("m:abaque", "20399/2/1").putStringSet("favoris", setOf("abaque")).putInt("reviews", 4).putInt("reviewsRight", 3).commit()
        sp("wallpaper_state").edit().putInt("wallpaperId", 42).putLong("wallpaperEndAt", 99).commit()
    }

    @Test fun roundTripKeepsEveryValueAndTypeButNotTheWallpaperState() {
        fill()
        val before = profile()
        assertTrue(Profile.export(ctx, uri()))
        val json = File(ctx.cacheDir, "profil.json").readText()
        assertFalse("wallpaper" in JSONObject(json).getJSONObject("files").keys().asSequence().joinToString())
        assertFalse("wallpaperId" in json)
        Profile.FILES.forEach { sp(it).edit().clear().putInt("hour", 3).commit() }
        sp("wallpaper_state").edit().putInt("wallpaperId", 7).commit()
        assertTrue(Profile.import(ctx, uri()))
        assertEquals(before, profile()) // Map equality: an Int 7 is not a Long 7
        assertEquals(7, sp("wallpaper_state").getInt("wallpaperId", 0)) // device-local, untouched
        assertEquals("livres", Prefs(ctx).glosePose)
        assertEquals(setOf("abaque"), Memory(ctx).favourites)
    }

    /** Golden file: a profile as exported by 0.14.0 (the first in-app update) must import into HEAD unchanged; keys and files from a later version are ignored. */
    @Test fun profileFrom0140ImportsUnchanged() {
        val text = javaClass.classLoader!!.getResource("profil-0.14.0.json")!!.readText()
        assertTrue(Profile.import(ctx, uri(text)))
        val files = JSONObject(text).getJSONObject("files")
        Profile.FILES.forEach { f ->
            val o = files.getJSONObject(f)
            val expected = o.keys().asSequence().filter { it != "futureKey" && it != "autreFutur" }.associateWith { k ->
                val e = o.getJSONObject(k)
                when (e.getString("t")) {
                    "int" -> e.getInt("v"); "long" -> e.getLong("v"); "boolean" -> e.getBoolean("v")
                    "set" -> e.getJSONArray("v").let { a -> List(a.length()) { a.getString(it) }.toSet() }
                    else -> e.getString("v")
                }
            }
            assertEquals(f, expected, sp(f).all.toMap())
        }
        assertEquals("Monsieur Glose", Prefs(ctx).gloseName)
        assertEquals(setOf("abaque"), Memory(ctx).favourites)
    }

    /** Every key Prefs keeps in the backed-up "definiday" file must be in Profile.SETTINGS, or export/import silently drops it. */
    @Test fun everyPrefsKeyIsInTheProfile() {
        val src = File("src/main/java/fr/definiday/Prefs.kt").readText().substringBefore("\nclass History")
        val keys = Regex("""(?:\bsp\.(?:edit\(\)\.)?\w+|\b(?:int|long|bool|str|strings))\("(\w+)"""").findAll(src).map { it.groupValues[1] }.toSet()
        assertTrue(keys.size > 30)
        assertEquals(emptySet<String>(), keys - Profile.SETTINGS.keys)
    }

    @Test fun junkIsRejectedAndChangesNothing() {
        fill()
        val before = profile()
        val good = Profile.json(ctx)
        fun bad(f: String, k: String, t: String, v: Any) = JSONObject(good.toString()).apply { getJSONObject("files").getJSONObject(f).put(k, JSONObject().put("t", t).put("v", v)) }.toString()
        listOf(
            "", "pas du json", "{}", "[1,2]",
            JSONObject(good.toString()).put("format", "autre").toString(),
            JSONObject(good.toString()).put("version", 2).toString(),
            JSONObject(good.toString()).apply { getJSONObject("files").remove("memoire") }.toString(),
            bad("definiday", "hour", "string", "8"), // known key, wrong type
            bad("definiday", "hour", "int", 1.5),
            bad("definiday", "hour", "int", 1L shl 40),
            bad("definiday", "unlocked", "set", org.json.JSONArray().put(1)),
            bad("history", "abaque", "long", "hier"),
            bad("memoire", "favoris", "set", "abaque"),
            bad("definiday", "glosePose", "string", "x".repeat(20_000)),
            "{\"format\":\"definiday-profil\",\"version\":1,\"files\":{\"definiday\":{\"x\":\"" + "x".repeat(5 shl 20) + "\"}}}", // too big
        ).forEach { junk ->
            assertFalse(junk.take(80), Profile.import(ctx, uri(junk)))
            assertEquals(before, profile())
        }
        assertFalse(Profile.import(ctx, Uri.fromFile(File(ctx.cacheDir, "absent.json"))))
    }

    @Test fun exportOverALongerFileStaysReadableAndABomIsTolerated() {
        fill()
        val before = profile()
        File(ctx.cacheDir, "profil.json").writeText(" ".repeat(10_000) + "x")
        assertTrue(Profile.export(ctx, uri()))
        assertTrue(Profile.read(ctx, uri()) != null)
        val text = File(ctx.cacheDir, "profil.json").readText()
        Profile.FILES.forEach { sp(it).edit().clear().commit() }
        assertTrue(Profile.import(ctx, uri("\uFEFF" + text)))
        assertEquals(before, profile())
    }

    @Test fun unknownKeysAndFilesAreIgnored() {
        fill()
        val json = Profile.json(ctx).apply {
            getJSONObject("files").getJSONObject("definiday").put("pirate", JSONObject().put("t", "string").put("v", "arr"))
            getJSONObject("files").getJSONObject("memoire").put("autre", JSONObject().put("t", "int").put("v", 1))
            getJSONObject("files").put("wallpaper_state", JSONObject().put("wallpaperId", JSONObject().put("t", "int").put("v", 5)))
            put("futur", true)
        }
        sp("wallpaper_state").edit().putInt("wallpaperId", 9).commit()
        assertTrue(Profile.import(ctx, uri(json.toString())))
        assertFalse(sp("definiday").contains("pirate"))
        assertFalse(sp("memoire").contains("autre"))
        assertEquals(9, sp("wallpaper_state").getInt("wallpaperId", 0))
        assertEquals(7, Prefs(ctx).hour)
    }

    @Test fun importReschedulesTheAlarms() {
        val am = shadowOf(ctx.getSystemService(AlarmManager::class.java))
        Prefs(ctx).apply { enabled = true; quizEnabled = true }
        val file = Profile.json(ctx).toString()
        Prefs(ctx).apply { enabled = false; quizEnabled = false }
        DailyReceiver.scheduleNext(ctx); DailyReceiver.scheduleQuiz(ctx)
        assertEquals(0, am.scheduledAlarms.size)
        assertTrue(Profile.import(ctx, uri(file)))
        assertEquals(2, am.scheduledAlarms.size) // daily word + evening quiz
    }

    /** Profile I/O runs on a worker thread: wait (bounded) until its result is posted back to the UI thread. */
    private fun settle(done: () -> Boolean) {
        val end = System.currentTimeMillis() + 5_000
        while (!done() && System.currentTimeMillis() < end) { Thread.sleep(5); ShadowLooper.idleMainLooper() }
        assertTrue("profile I/O did not finish", done())
    }

    @Test fun settingsExportThenRestoreAfterConfirmation() {
        fill()
        val a = Robolectric.buildActivity(SettingsActivity::class.java).setup().get()
        a.findViewById<Button>(R.id.profile_export).performClick()
        val create = shadowOf(a).nextStartedActivityForResult
        assertEquals(Intent.ACTION_CREATE_DOCUMENT, create.intent.action)
        assertTrue(create.intent.getStringExtra(Intent.EXTRA_TITLE)!!.matches(Regex("definiday-profil-\\d{4}-\\d{2}-\\d{2}\\.json")))
        shadowOf(a).receiveResult(create.intent, Activity.RESULT_OK, Intent().setData(uri()))
        assertFalse(a.findViewById<Button>(R.id.profile_import).isEnabled) // no second operation meanwhile
        settle { ShadowToast.getTextOfLatestToast() != null }
        assertTrue(a.findViewById<Button>(R.id.profile_import).isEnabled)
        assertEquals(ctx.getString(R.string.profile_saved), ShadowToast.getTextOfLatestToast())
        val before = profile()
        sp("definiday").edit().putInt("hour", 3).commit()

        a.findViewById<Button>(R.id.profile_import).performClick()
        val open = shadowOf(a).nextStartedActivityForResult
        assertEquals(Intent.ACTION_OPEN_DOCUMENT, open.intent.action)
        shadowOf(a).receiveResult(open.intent, Activity.RESULT_OK, Intent().setData(Uri.fromFile(File(ctx.cacheDir, "absent.json"))))
        settle { ShadowToast.getTextOfLatestToast() == ctx.getString(R.string.profile_bad) }
        shadowOf(a).receiveResult(open.intent, Activity.RESULT_OK, Intent().setData(uri()))
        settle { ShadowDialog.getLatestDialog()?.isShowing == true }
        assertEquals(3, Prefs(ctx).hour) // not before the confirmation
        (ShadowDialog.getLatestDialog() as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).performClick()
        ShadowLooper.idleMainLooper()
        assertEquals(before, profile())
    }

    /** debug-signed builds (this one, and release without the keystore) never replace the real app fr.definiday. */
    @Test fun testBuildInstallsBesideTheRealApp() {
        assertEquals("fr.definiday.dev", ctx.packageName)
        assertEquals("Definiday (test)", ctx.getString(R.string.app_name))
        assertEquals("fr.definiday.dev.share", ShareProvider.authority(ctx))
    }
}
