package fr.definiday

import android.app.Application

/** Test application (robolectric.properties): the onboarding is done, so MainActivity opens on the home screen. */
class TestApp : Application() {
    override fun onCreate() { super.onCreate(); Prefs(this).onboarded = true }
}
