package fr.definiday

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Handler
import android.os.Looper

/** Optional little sounds (docs/son.md): off by default, never when the phone is on silent or vibrate, always paired with a visual.
 *  Call from the main thread, e.g. `Sounds.play(this, Sounds.MRR)` on a tap on Glose, `Sounds.PURR` on the 4-tap special. */
object Sounds {
    val MRR = R.raw.son_mrr
    val PURR = R.raw.son_ronron
    val RIGHT = R.raw.son_tic
    val WRONG = R.raw.son_hmm
    val CHIME = R.raw.son_carillon
    val STAMP = R.raw.son_tampon
    val PAGE = R.raw.son_page
    val ALL = listOf(MRR, PURR, RIGHT, WRONG, CHIME, STAMP, PAGE)

    internal var pool: SoundPool? = null // created on the first sound, released after IDLE ms without one
    private val ids = mutableMapOf<Int, Int>() // raw res -> SoundPool id
    private val main = Handler(Looper.getMainLooper())
    private val release = Runnable { pool?.release(); pool = null; ids.clear() }
    private const val IDLE = 30_000L

    fun on(ctx: Context) = Prefs(ctx).sounds && ctx.getSystemService(AudioManager::class.java).ringerMode == AudioManager.RINGER_MODE_NORMAL

    /** Plays [res] (one of the constants; 0 = nothing) if sounds are on; a first play waits for its file to load. */
    fun play(ctx: Context, res: Int) {
        if (res == 0 || !on(ctx)) return
        val p = pool ?: SoundPool.Builder().setMaxStreams(2).setAudioAttributes(
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        ).build().apply { setOnLoadCompleteListener { sp, id, status -> if (status == 0) sp.play(id, 1f, 1f, 0, 0, 1f) } }.also { pool = it }
        ids[res]?.let { p.play(it, 1f, 1f, 0, 0, 1f) } ?: run { ids[res] = p.load(ctx.applicationContext, res, 1) }
        main.removeCallbacks(release)
        main.postDelayed(release, IDLE)
    }
}
