package fr.definiday

import android.Manifest
import android.app.AlarmManager
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
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
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowAlarmManager

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "fr-w411dp-h891dp-xxhdpi")
class OnboardingTest {
    private val ctx = RuntimeEnvironment.getApplication()
    private val p get() = Prefs(ctx)

    private fun onboarding() = Robolectric.buildActivity(OnboardingActivity::class.java).setup()
    private fun OnboardingActivity.click(id: Int) = findViewById<View>(id).performClick()
    private fun OnboardingActivity.shows(id: Int) = findViewById<View>(id).visibility == View.VISIBLE
    private fun OnboardingActivity.text(id: Int) = findViewById<TextView>(id).text.toString()
    private fun OnboardingActivity.ended() = isFinishing && shadowOf(this).nextStartedActivity?.component?.className == MainActivity::class.java.name

    @Test fun firstLaunchShowsOnboarding() {
        ctx.getSharedPreferences("definiday", 0).edit().clear().commit() // fresh install (TestApp marks it done)
        val main = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        assertTrue(main.isFinishing)
        assertEquals(OnboardingActivity::class.java.name, shadowOf(main).nextStartedActivity.component!!.className)
        assertNull("no prompt at first launch", shadowOf(main).lastRequestedPermission)
    }

    @Test fun existingUserDoesNotSeeIt() {
        ctx.getSharedPreferences("definiday", 0).edit().clear().putLong("dailyDay", 20_000).commit() // used 0.10.0, no onboarded key
        val main = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        assertFalse(main.isFinishing)
        assertNull(shadowOf(main).nextStartedActivity)
        assertTrue(p.onboarded)
    }

    @Test fun choicesPersist() {
        val a = onboarding().get()
        assertEquals("Étape 1 sur 4", a.text(R.id.progress))
        a.click(R.id.next)
        assertTrue(a.shows(R.id.step_where))
        assertFalse("notification only by default: no wallpaper part", a.shows(R.id.lock_box))
        a.findViewById<RadioButton>(R.id.mode_lock).performClick()
        assertEquals(Prefs.MODE_LOCK, p.mode)
        assertTrue(a.shows(R.id.lock_box))
        assertTrue(a.findViewById<View>(R.id.preview).contentDescription.isNotEmpty())
        a.findViewById<Switch>(R.id.unlock_once).performClick()
        assertEquals(1, p.unlocks)
        a.click(R.id.next)
        assertTrue(a.shows(R.id.step_when))
        assertEquals("Heure d’affichage : 08:00", a.text(R.id.time))
        a.findViewById<Spinner>(R.id.per_day).run { onItemSelectedListener!!.onItemSelected(this, null, 2, 2) }
        assertEquals(3, p.wordsPerDay)
        a.findViewById<Switch>(R.id.quiz_enabled).performClick()
        assertFalse(p.quizEnabled)
        assertFalse(a.findViewById<Button>(R.id.quiz_time).isEnabled)
        assertEquals(21, p.quizHour)
    }

    @Test fun permissionAskedOnlyWhenNeeded() {
        // lock screen only, no quiz, exact alarms allowed: nothing to ask, step 3 ends it
        ShadowAlarmManager.setCanScheduleExactAlarms(true)
        p.mode = Prefs.MODE_LOCK; p.quizEnabled = false; p.unlocks = 1
        var a = onboarding().get()
        a.click(R.id.next); a.click(R.id.next)
        assertEquals("Étape 3 sur 3", a.text(R.id.progress))
        assertEquals(ctx.getString(R.string.onb_done), a.text(R.id.next))
        a.click(R.id.next)
        assertTrue(a.ended())
        assertNull(shadowOf(a).lastRequestedPermission)

        // removal at the 1st unlock without exact alarms: only the « Alarmes et rappels » block
        ShadowAlarmManager.setCanScheduleExactAlarms(false)
        a = onboarding().get()
        a.click(R.id.next); a.click(R.id.next); a.click(R.id.next)
        assertTrue(a.shows(R.id.step_ask) && a.shows(R.id.exact_box) && !a.shows(R.id.notif_why) && !a.shows(R.id.skip))
        a.click(R.id.exact_ask)
        assertEquals(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, shadowOf(a).nextStartedActivity.action)
        ShadowAlarmManager.setCanScheduleExactAlarms(true)
        a.click(R.id.next)
        assertTrue(a.ended())
        assertNull(shadowOf(a).lastRequestedPermission)

        // the quiz reminder needs notifications: explained, then asked once at the tap, then home whatever the answer
        p.quizEnabled = true
        a = onboarding().get()
        a.click(R.id.next); a.click(R.id.next); a.click(R.id.next)
        assertTrue(a.shows(R.id.notif_why) && !a.shows(R.id.exact_box))
        assertTrue(a.text(R.id.notif_why), a.text(R.id.notif_why).contains("le rappel du quiz du soir dans"))
        assertNull(shadowOf(a).lastRequestedPermission)
        a.click(R.id.next)
        assertEquals(listOf(Manifest.permission.POST_NOTIFICATIONS), shadowOf(a).lastRequestedPermission.requestedPermissions.toList())
        a.onRequestPermissionsResult(0, arrayOf(Manifest.permission.POST_NOTIFICATIONS), intArrayOf(-1)) // refused
        assertTrue(a.ended())
        assertTrue(p.onboarded)
    }

    @Test fun skippingKeepsSafeDefaults() {
        val a = onboarding().get()
        assertEquals(ctx.getString(R.string.onb_skip), a.text(R.id.skip))
        a.click(R.id.skip)
        assertTrue(a.ended())
        assertNull(shadowOf(a).lastRequestedPermission)
        assertTrue(p.onboarded)
        assertEquals(Prefs.MODE_NOTIF, p.mode) // the lock wallpaper is never replaced without the warning read
        assertEquals(8 to 0, p.hour to p.minute)
        assertEquals(1, p.wordsPerDay)
        assertTrue(p.quizEnabled)
        assertEquals("word and quiz alarms", 2, shadowOf(ctx.getSystemService(AlarmManager::class.java)).scheduledAlarms.size)
        assertFalse(Robolectric.buildActivity(MainActivity::class.java).setup().get().isFinishing)
    }

    @Test fun rotationKeepsTheStepAndBackGoesBack() {
        ctx.getSharedPreferences("definiday", 0).edit().clear().commit()
        val c = onboarding()
        c.get().click(R.id.next); c.get().click(R.id.next)
        c.recreate()
        val a = c.get()
        assertTrue(a.shows(R.id.step_when) && !a.shows(R.id.step_welcome))
        assertEquals("Étape 3 sur 4", a.text(R.id.progress))
        @Suppress("DEPRECATION") a.onBackPressed()
        assertTrue(a.shows(R.id.step_where))
        @Suppress("DEPRECATION") a.onBackPressed()
        @Suppress("DEPRECATION") a.onBackPressed() // from the first step: leaves, not marked done
        assertTrue(a.isFinishing)
        assertFalse(p.onboarded)
    }
}
