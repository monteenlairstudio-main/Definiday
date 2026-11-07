package fr.definiday

/** Test application (robolectric.properties): the onboarding is done, so MainActivity opens on the home screen.
 *  The lock screen shares the home wallpaper, except while our card is on it (Robolectric's lock id is 0 from the start). */
class TestApp : android.app.Application() {
    override fun onCreate() {
        super.onCreate()
        Prefs(this).onboarded = true
        Wall.lockId = { Prefs(it).run { if (wallpaperEndAt != 0L) wallpaperId else -1 } }
        Wall.lockLive = { false }
    }
}
