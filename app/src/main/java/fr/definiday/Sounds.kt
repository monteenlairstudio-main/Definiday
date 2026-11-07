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

    internal var pool: SoundPool? = null // created on the first sound (or a quiz opening), released after IDLE ms without one
    private val ids = mutableMapOf<Int, Int>() // raw res -> SoundPool id, loading or loaded
    private val ready = mutableSetOf<Int>() // ids loaded
    private val waiting = mutableSetOf<Int>() // ids to play once loaded
    private var held = false // a quiz is open: the pool stays
    private val main = Handler(Looper.getMainLooper())
    private val drop = Runnable(::release)
    private const val IDLE = 30_000L

    fun on(ctx: Context) = Prefs(ctx).sounds && ctx.getSystemService(AudioManager::class.java).ringerMode == AudioManager.RINGER_MODE_NORMAL

    /** Plays [res] (one of the constants; 0 = nothing) if sounds are on; a first play waits for its file to load. */
    fun play(ctx: Context, res: Int) {
        if (res == 0 || !on(ctx)) return
        val id = load(ctx, res)
        if (id in ready) pool!!.play(id, 1f, 1f, 0, 0, 1f) else waiting += id
        idle()
    }

    /** A quiz opens (sounds on): its sounds load now, so the first tic is not late, and the pool stays until [letGo]. */
    fun hold(ctx: Context) {
        if (!on(ctx)) return
        listOf(RIGHT, WRONG, CHIME, STAMP).forEach { load(ctx, it) }
        held = true
        main.removeCallbacks(drop)
    }

    fun letGo() { held = false; idle() }

    private fun idle() {
        main.removeCallbacks(drop)
        if (!held) main.postDelayed(drop, IDLE)
    }

    private fun load(ctx: Context, res: Int): Int {
        val p = pool ?: SoundPool.Builder().setMaxStreams(2).setAudioAttributes(
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        ).build().apply {
            setOnLoadCompleteListener { sp, id, status ->
                if (sp !== pool) return@setOnLoadCompleteListener // released meanwhile
                if (status == 0) { ready += id; if (waiting.remove(id)) sp.play(id, 1f, 1f, 0, 0, 1f) }
                else { ids.values.remove(id); waiting.remove(id) } // a failed load is tried again next time, not muted until the release
            }
        }.also { pool = it }
        return ids.getOrPut(res) { p.load(ctx.applicationContext, res, 1) }
    }

    internal fun release() { pool?.release(); pool = null; ids.clear(); ready.clear(); waiting.clear(); held = false }
}
