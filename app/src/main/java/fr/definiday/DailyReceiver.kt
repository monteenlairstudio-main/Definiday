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
import android.os.Build
import java.time.ZonedDateTime

/** Daily alarm -> show the word; END alarm -> remove our lock wallpaper; boot/update -> reschedule. */
class DailyReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        when (intent.action) {
            ACTION_SHOW -> { show(ctx); scheduleNext(ctx) }
            ACTION_END -> clearWallpaper(ctx)
            else -> { // BOOT_COMPLETED, MY_PACKAGE_REPLACED, TIME_SET, TIMEZONE_CHANGED: re-anchor alarms
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
            set(am, at, pi)
        }

        private fun scheduleEnd(ctx: Context, at: Long) =
            set(ctx.getSystemService(AlarmManager::class.java), at, pending(ctx, ACTION_END))

        // Exact when allowed (always before API 31). Otherwise inexact: Android only promises delivery within the hour.
        // ponytail: no SCHEDULE_EXACT_ALARM prompt; add one if users on 13+ complain about late words
        private fun set(am: AlarmManager, at: Long, pi: PendingIntent) =
            if (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)

        private fun pending(ctx: Context, action: String) = PendingIntent.getBroadcast(
            ctx, action.hashCode(), Intent(ctx, DailyReceiver::class.java).setAction(action),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        fun show(ctx: Context) {
            val p = Prefs(ctx)
            val words = Words.today(ctx)
            val durationMs = p.durationMinutes * 60_000L
            words.forEachIndexed { i, w -> notify(ctx, w, durationMs, id = i + 1) }
            val w = words.first() // ponytail: wallpaper shows only the first daily word; stack them if 2-3 words should fit
            if (p.wallpaper) {
                val m = ctx.resources.displayMetrics // portrait size whatever the current orientation
                val (width, height) = minOf(m.widthPixels, m.heightPixels) to maxOf(m.widthPixels, m.heightPixels)
                runCatching {
                    p.wallpaperId = WallpaperManager.getInstance(ctx).setBitmap(
                        WordCard.render(w, width, height), null, true, WallpaperManager.FLAG_LOCK,
                    )
                    p.wallpaperEndAt = System.currentTimeMillis() + durationMs
                    scheduleEnd(ctx, p.wallpaperEndAt)
                }
            }
        }

        private fun clearWallpaper(ctx: Context) {
            val p = Prefs(ctx)
            if (p.wallpaperEndAt == 0L || p.wallpaperEndAt > System.currentTimeMillis()) return // stale END after a newer SHOW
            val wm = WallpaperManager.getInstance(ctx)
            // Only remove it if it is still ours (user may have picked a new one, or prefs were restored on another phone).
            // Lock screen then falls back to the home wallpaper (the previous lock image can't be read without storage permission).
            if (wm.getWallpaperId(WallpaperManager.FLAG_LOCK) == p.wallpaperId) runCatching { wm.clear(WallpaperManager.FLAG_LOCK) }
            p.wallpaperEndAt = 0
        }

        private fun notify(ctx: Context, w: Word, durationMs: Long, id: Int) {
            val nm = ctx.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL, ctx.getString(R.string.channel_name), NotificationManager.IMPORTANCE_DEFAULT)
                    .apply { lockscreenVisibility = Notification.VISIBILITY_PUBLIC; setShowBadge(false) },
            )
            val open = PendingIntent.getActivity(
                ctx, id, Intent(ctx, MainActivity::class.java).putExtra(MainActivity.EXTRA_MOT, w.mot),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            val body = "${w.nature} · ${w.definition}" + (if (w.exemple.isNotEmpty()) "\n«\u00a0${w.exemple}\u00a0»" else "") +
                (if (w.anecdote.isNotEmpty()) "\n\n${w.anecdote}" else "")
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
            runCatching { nm.notify(id, n) } // SecurityException if POST_NOTIFICATIONS denied: wallpaper mode still works
        }
    }
}
