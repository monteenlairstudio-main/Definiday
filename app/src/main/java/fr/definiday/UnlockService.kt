package fr.definiday

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build

/**
 * Counts unlocks while our word is the lock wallpaper, and removes it at the Nth one.
 * USER_PRESENT can't be received from the manifest since Android 8, hence a (silent) foreground service.
 */
class UnlockService : Service() {
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) { if (countUnlock(ctx)) stopSelf() }
    }

    override fun onCreate() {
        super.onCreate()
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(NotificationChannel(CHANNEL, getString(R.string.watch_channel), NotificationManager.IMPORTANCE_MIN))
        registerReceiver(receiver, IntentFilter(Intent.ACTION_USER_PRESENT))
    }

    // Count lives in prefs, so a restarted service keeps it; the duration END alarm is the fallback if we're killed for good.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // posted at each start: sync() restarts it when the setting changes, so the text shows the current count
        val n = Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentText(getString(R.string.watch_text, resources.getStringArray(R.array.unlocks_ordinals)[(Prefs(this).unlocks - 1).coerceIn(0, 2)]))
            .build()
        if (Build.VERSION.SDK_INT >= 34) startForeground(ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE) else startForeground(ID, n)
        // restarted (START_STICKY) after the word was removed or the setting went back to the duration: nothing to watch
        if (Prefs(this).let { it.wallpaperEndAt == 0L || it.unlocks == 0 }) stopSelf()
        return START_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    companion object {
        private const val CHANNEL = "unlock_watch"
        private const val ID = 100

        /** May be refused when started from the background without an exact alarm: the duration then applies alone. */
        fun start(ctx: Context) {
            runCatching { ctx.startForegroundService(Intent(ctx, UnlockService::class.java)) }
        }

        /** After "Fin sur l'écran de verrouillage" changed: watch while our word is shown and 1-3 unlocks are chosen, else stop. */
        fun sync(ctx: Context) {
            val p = Prefs(ctx)
            if (p.wallpaperEndAt != 0L && p.unlocks > 0) start(ctx) else ctx.stopService(Intent(ctx, UnlockService::class.java))
        }

        /** One unlock; true when the watch is over (wallpaper removed, or gone, or the setting is back to the duration alone). */
        fun countUnlock(ctx: Context): Boolean {
            val p = Prefs(ctx)
            if (p.wallpaperEndAt == 0L || p.unlocks == 0) return true
            p.unlocksSeen += 1
            if (p.unlocksSeen < p.unlocks) return false
            DailyReceiver.clearWallpaper(ctx, force = true)
            return true
        }
    }
}
