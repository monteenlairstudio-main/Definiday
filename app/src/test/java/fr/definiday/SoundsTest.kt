package fr.definiday

import android.media.AudioManager
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/** Sounds stay silent unless chosen and the phone rings normally (docs/son.md). */
@RunWith(RobolectricTestRunner::class)
class SoundsTest {
    private val ctx = RuntimeEnvironment.getApplication()
    private val am = ctx.getSystemService(AudioManager::class.java)

    @After fun reset() { Sounds.pool?.release(); Sounds.pool = null }

    @Test fun silentByDefault() {
        Sounds.play(ctx, Sounds.RIGHT)
        assertNull(Sounds.pool) // not even loaded
    }

    @Test fun silentUnlessRingerNormal() {
        Prefs(ctx).sounds = true
        for (mode in listOf(AudioManager.RINGER_MODE_SILENT, AudioManager.RINGER_MODE_VIBRATE)) {
            am.ringerMode = mode
            Sounds.play(ctx, Sounds.PAGE)
            assertNull(Sounds.pool)
        }
        am.ringerMode = AudioManager.RINGER_MODE_NORMAL
        Sounds.play(ctx, Sounds.PAGE)
        assertNotNull(Sounds.pool)
    }

    @Test fun everySoundIsPackaged() = Sounds.ALL.forEach { id ->
        ctx.resources.openRawResource(id).use { assertTrue(ctx.resources.getResourceEntryName(id), it.readBytes().size in 1000 until 20_000) }
    }
}
