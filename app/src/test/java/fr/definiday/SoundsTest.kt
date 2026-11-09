package fr.definiday

import android.media.AudioManager
import android.os.Looper
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import java.time.Duration

/** Sounds stay silent unless chosen and the phone rings normally (docs/son.md). */
@RunWith(RobolectricTestRunner::class)
class SoundsTest {
    private val ctx = RuntimeEnvironment.getApplication()
    private val am = ctx.getSystemService(AudioManager::class.java)

    @After fun reset() = Sounds.release()

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

    /** A quiz opening with sounds on loads its sounds before the first answer (no late tic) and keeps them while it is open. */
    @Test fun quizPreloadsItsSounds() {
        val idle = { shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMinutes(1)) }
        Robolectric.buildActivity(QuizActivity::class.java).setup()
        assertNull(Sounds.pool) // off by default: nothing loaded
        Prefs(ctx).sounds = true
        am.ringerMode = AudioManager.RINGER_MODE_NORMAL
        val c = Robolectric.buildActivity(QuizActivity::class.java).setup()
        val pool = shadowOf(Sounds.pool!!)
        listOf(Sounds.RIGHT, Sounds.WRONG, Sounds.CHIME, Sounds.STAMP).forEach { pool.notifyResourceLoaded(it, true) } // throws if not loaded
        assertFalse(pool.wasResourcePlayed(Sounds.CHIME)) // loaded, not played
        idle()
        assertNotNull(Sounds.pool) // kept while the quiz is open
        c.pause()
        idle()
        assertNull(Sounds.pool) // released once it is left
    }

    /** A failed load is forgotten: the next play loads the sound again instead of playing a dead id. */
    @Test fun failedLoadIsRetried() {
        Prefs(ctx).sounds = true
        am.ringerMode = AudioManager.RINGER_MODE_NORMAL
        Sounds.play(ctx, Sounds.RIGHT)
        val pool = shadowOf(Sounds.pool!!)
        pool.notifyResourceLoaded(Sounds.RIGHT, false)
        Sounds.play(ctx, Sounds.RIGHT)
        assertFalse(pool.wasResourcePlayed(Sounds.RIGHT))
        pool.notifyResourceLoaded(Sounds.RIGHT, true)
        assertTrue(pool.wasResourcePlayed(Sounds.RIGHT))
    }
}
