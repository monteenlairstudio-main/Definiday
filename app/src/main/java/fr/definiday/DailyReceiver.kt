package fr.definiday

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.ZonedDateTime

/** Daily alarm -> show the word; END alarm -> remove our lock wallpaper; boot/update -> reschedule. */
class DailyReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        when (intent.action) {
            ACTION_SHOW -> { show(ctx); scheduleNext(ctx) }
            ACTION_END -> clearWallpaper(ctx)
            else -> { // BOOT_COMPLETED, MY_PACKAGE_REPLACED: alarms don't survive either
                scheduleNext(ctx)
                val end = Prefs(ctx).wallpaperEndAt
                if (end != 0L) { if (end <= System.currentTimeMillis()) clearWallpaper(ctx) else scheduleEnd(ctx, end) }
            }
        }
    }

    companion object {
        const val ACTION_SHOW = "fr.definiday.SHOW"
        const val ACTION_END = "fr.definiday.END"
        private const val CHANNEL = "daily_word"

        fun nextTrigger(now: ZonedDateTime, hour: Int, minute: Int): ZonedDateTime {
            val t = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
            return if (t.isAfter(now)) t else t.plusDays(1)
        }

        fun scheduleNext(ctx: Context) {
            val p = Prefs(ctx)
            val am = ctx.getSystemService(AlarmManager::class.java)
            val pi = pending(ctx, ACTION_SHOW)
            if (!p.enabled) { am.cancel(pi); return }
            val at = nextTrigger(ZonedDateTime.now(), p.hour, p.minute).toInstant().toEpochMilli()
            // ponytail: inexact alarm (no SCHEDULE_EXACT_ALARM permission), Doze can delay it a few minutes; switch to setExactAndAllowWhileIdle if users want to-the-minute
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }

        private fun scheduleEnd(ctx: Context, at: Long) =
            ctx.getSystemService(AlarmManager::class.java)
                .setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending(ctx, ACTION_END))

        private fun pending(ctx: Context, action: String) = PendingIntent.getBroadcast(
            ctx, action.hashCode(), Intent(ctx, DailyReceiver::class.java).setAction(action),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        fun show(ctx: Context) {
            val p = Prefs(ctx)
            val w = Words.today(ctx)
            val durationMs = p.durationMinutes * 60_000L
            notify(ctx, w, durationMs)
            if (p.wallpaper) {
                val m = ctx.resources.displayMetrics
                runCatching {
                    WallpaperManager.getInstance(ctx).setBitmap(
                        WordCard.render(w, m.widthPixels, m.heightPixels), null, true, WallpaperManager.FLAG_LOCK,
                    )
                    p.wallpaperEndAt = System.currentTimeMillis() + durationMs
                    scheduleEnd(ctx, p.wallpaperEndAt)
                }
            }
        }

        private fun clearWallpaper(ctx: Context) {
            val p = Prefs(ctx)
            if (p.wallpaperEndAt == 0L) return
            // Lock screen falls back to the home wallpaper (Android can't hand us the previous lock image without storage permission).
            runCatching { WallpaperManager.getInstance(ctx).clear(WallpaperManager.FLAG_LOCK) }
            p.wallpaperEndAt = 0
        }

        private fun notify(ctx: Context, w: Word, durationMs: Long) {
            val nm = ctx.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL, ctx.getString(R.string.channel_name), NotificationManager.IMPORTANCE_DEFAULT)
                    .apply { lockscreenVisibility = Notification.VISIBILITY_PUBLIC; setShowBadge(false) },
            )
            val open = PendingIntent.getActivity(
                ctx, 0, Intent(ctx, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE,
            )
            val body = "${w.nature} · ${w.definition}" + if (w.exemple.isNotEmpty()) "\n« ${w.exemple} »" else ""
            val n = Notification.Builder(ctx, CHANNEL)
                .setSmallIcon(R.drawable.ic_stat)
                .setContentTitle(w.mot)
                .setContentText("${w.nature} · ${w.definition}")
                .setStyle(Notification.BigTextStyle().bigText(body))
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(open)
                .setAutoCancel(true)
                .setTimeoutAfter(durationMs)
                .build()
            runCatching { nm.notify(1, n) } // SecurityException if POST_NOTIFICATIONS denied: wallpaper mode still works
        }
    }
}
