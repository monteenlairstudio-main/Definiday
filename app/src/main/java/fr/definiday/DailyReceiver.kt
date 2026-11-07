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
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.TextView
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

/** Daily alarm -> show the word (only refresh the home widget when the daily word is off); END alarm -> remove our lock wallpaper; boot/update -> reschedule. */
class DailyReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        when (intent.action) {
            ACTION_SHOW -> { if (Prefs(ctx).enabled) show(ctx) else Widget.update(ctx); scheduleNext(ctx) }
            ACTION_QUIZ -> { quizReminder(ctx); scheduleQuiz(ctx) }
            ACTION_END -> clearWallpaper(ctx)
            // BOOT_COMPLETED, MY_PACKAGE_REPLACED, TIME_SET, TIMEZONE_CHANGED, exact-alarm access granted: re-anchor alarms
            else -> {
                scheduleNext(ctx)
                scheduleQuiz(ctx)
                val end = Prefs(ctx).wallpaperEndAt
                if (end != 0L) {
                    if (end <= System.currentTimeMillis()) clearWallpaper(ctx)
                    else { scheduleEnd(ctx, end); if (Prefs(ctx).unlocks > 0) UnlockService.start(ctx) }
                }
            }
        }
    }

    companion object {
        const val ACTION_SHOW = "fr.definiday.SHOW"
        const val ACTION_END = "fr.definiday.END"
        const val ACTION_QUIZ = "fr.definiday.QUIZ"
        private const val QUIZ_ID = 50
        const val CHANNEL = "daily_word"

        /** Anchored on today's date (not on now's offset): on the fall-back night an ambiguous time resolves to its earlier
         *  instant, so a late delivery in the repeated hour schedules tomorrow, not the same time again 1 h later. */
        fun nextTrigger(now: ZonedDateTime, hour: Int, minute: Int): ZonedDateTime {
            val t = ZonedDateTime.of(now.toLocalDate(), LocalTime.of(hour, minute), now.zone)
            return if (t.isAfter(now)) t else t.plusDays(1)
        }

        fun scheduleNext(ctx: Context) {
            val p = Prefs(ctx)
            val am = ctx.getSystemService(AlarmManager::class.java)
            val pi = pending(ctx, ACTION_SHOW)
            if (!p.enabled && Widget.ids(ctx).isEmpty()) { am.cancel(pi); return } // a home widget still changes daily
            val at = nextTrigger(ZonedDateTime.now(), p.hour, p.minute).toInstant().toEpochMilli()
            set(am, at, pi)
        }

        fun scheduleQuiz(ctx: Context) {
            val p = Prefs(ctx)
            val am = ctx.getSystemService(AlarmManager::class.java)
            val pi = pending(ctx, ACTION_QUIZ)
            if (!p.quizEnabled) { am.cancel(pi); return }
            set(am, nextTrigger(ZonedDateTime.now(), p.quizHour, p.quizMinute).toInstant().toEpochMilli(), pi)
        }

        private fun quizReminder(ctx: Context) {
            val today = LocalDate.now().toEpochDay()
            if (Words.seenToday(ctx).isEmpty() || Prefs(ctx).lastQuizDay == today) return
            val nm = ctx.getSystemService(NotificationManager::class.java)
            channel(nm, ctx)
            // carries its day: tapped after midnight it opens the eve's quiz, not an empty one (UPDATE_CURRENT: the extra changes daily)
            val open = PendingIntent.getActivity(ctx, QUIZ_ID, Intent(ctx, QuizActivity::class.java).putExtra(QuizActivity.EXTRA_DAY, today),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            // a notification cannot play an AVD: still images of the user's Glose, asleep in his cup once that pose is unlocked
            val pose = if ("soir" in Prefs(ctx).unlocked) R.drawable.glose_soir_vd else R.drawable.glose_assis_vd
            val n = Notification.Builder(ctx, CHANNEL)
                .setSmallIcon(R.drawable.ic_stat)
                .setLargeIcon(Icon.createWithBitmap(Glose.bitmap(Glose.mine(ctx, R.drawable.glose_icone_dort_vd, accessory = false), 192, 192)))
                .setStyle(Notification.BigPictureStyle().bigPicture(Glose.bitmap(Glose.mine(ctx, pose), 512, 256)))
                .setContentTitle(ctx.getString(R.string.quiz_title))
                .setContentText(Words.seenToday(ctx).size.let { ctx.resources.getQuantityString(R.plurals.quiz_notif, it, it) })
                .setContentIntent(open)
                .setAutoCancel(true)
                .build()
            runCatching { nm.notify(QUIZ_ID, n) }
        }

        private fun scheduleEnd(ctx: Context, at: Long) =
            set(ctx.getSystemService(AlarmManager::class.java), at, pending(ctx, ACTION_END))

        // Exact when allowed (see Alarms). Otherwise inexact: Android only promises delivery within the hour, and the
        // background start of UnlockService is then refused on 12+ (exact alarms are exempt from that restriction).
        private fun set(am: AlarmManager, at: Long, pi: PendingIntent) =
            if (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)

        private fun pending(ctx: Context, action: String) = PendingIntent.getBroadcast(
            ctx, action.hashCode(), Intent(ctx, DailyReceiver::class.java).setAction(action),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        /** Puts words on the lock screen (today's by default, or a random pick that replaces them). */
        fun show(ctx: Context, words: List<Item> = Words.today(ctx)) {
            val p = Prefs(ctx)
            val nm = ctx.getSystemService(NotificationManager::class.java)
            (1..3).forEach { nm.cancel(it) }
            val durationMs = p.durationMinutes * 60_000L
            // never over the user's own lock wallpaper (Wall): the word then comes by notification, even in lock-only mode
            val lock = p.mode != Prefs.MODE_NOTIF && Wall.lockFree(ctx)
            if (p.mode != Prefs.MODE_NOTIF) p.lockBlocked = !lock
            // only what is really shown counts as seen (history, quiz): lock-only mode with 2-3 words shows the first one
            val shown = if (p.mode == Prefs.MODE_LOCK && lock) mutableListOf() else words.filterIndexed { i, w -> notify(ctx, w, durationMs, id = i + 1) }.toMutableList()
            val w = words.first() // ponytail: wallpaper shows only the first daily word; stack them if 2-3 words should fit
            if (lock) {
                val m = ctx.resources.displayMetrics // portrait size whatever the current orientation
                val (width, height) = minOf(m.widthPixels, m.heightPixels) to maxOf(m.widthPixels, m.heightPixels)
                runCatching {
                    p.wallpaperId = WallpaperManager.getInstance(ctx).setBitmap(
                        WordCard.render(ctx, w, width, height, Styles.of(ctx, w), p.textBottom), null, true, WallpaperManager.FLAG_LOCK,
                    )
                    p.wallpaperEndAt = System.currentTimeMillis() + durationMs // duration stays the upper bound
                    p.unlocksSeen = 0
                    scheduleEnd(ctx, p.wallpaperEndAt)
                    if (p.unlocks > 0) UnlockService.start(ctx)
                    if (w !in shown) shown += w
                }
            }
            Words.shown(ctx, shown)
            Widget.update(ctx)
        }

        /** force = Nth unlock reached (before the duration ran out). */
        fun clearWallpaper(ctx: Context, force: Boolean = false) {
            val p = Prefs(ctx)
            if (p.wallpaperEndAt == 0L || (!force && p.wallpaperEndAt > System.currentTimeMillis())) return // stale END after a newer SHOW
            val wm = WallpaperManager.getInstance(ctx)
            // Only remove it if it is still ours (user may have picked a new one, or prefs were restored on another phone).
            // Lock screen then falls back to the home wallpaper: what it showed before, as Wall only lets us write over a shared one.
            runCatching { if (Wall.lockId(ctx) == p.wallpaperId) wm.clear(WallpaperManager.FLAG_LOCK) }
            p.wallpaperEndAt = 0
            ctx.stopService(Intent(ctx, UnlockService::class.java))
        }

        private fun channel(nm: NotificationManager, ctx: Context) = nm.createNotificationChannel(
            NotificationChannel(CHANNEL, ctx.getString(R.string.channel_name), NotificationManager.IMPORTANCE_DEFAULT)
                .apply { lockscreenVisibility = Notification.VISIBILITY_PUBLIC; setShowBadge(false) },
        )

        /** Notification title and text of an item. */
        fun texts(item: Item): Pair<String, String> = when (item) {
            is Word -> item.mot to "${item.nature} · ${item.definition}" +
                (if (item.exemple.isNotEmpty()) "\n«\u00a0${item.exemple}\u00a0»" else "") +
                (if (item.anecdote.isNotEmpty()) "\n\n${item.anecdote}" else "")
            is Quote -> item.signature to "«\u00a0${item.texte}\u00a0»"
        }

        /** True when posted (and notifications are on). */
        private fun notify(ctx: Context, w: Item, durationMs: Long, id: Int): Boolean {
            val nm = ctx.getSystemService(NotificationManager::class.java)
            channel(nm, ctx)
            val open = PendingIntent.getActivity(
                ctx, id, Intent(ctx, MainActivity::class.java).putExtra(MainActivity.EXTRA_MOT, w.key),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            val (title, body) = texts(w)
            val n = Notification.Builder(ctx, CHANNEL)
                .setSmallIcon(R.drawable.ic_stat)
                .setContentTitle(title)
                .setContentText(body.substringBefore('\n'))
                .setStyle(Notification.BigTextStyle().bigText(body))
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(open)
                .setAutoCancel(true)
                .setTimeoutAfter(durationMs)
                .build()
            // SecurityException if POST_NOTIFICATIONS denied: wallpaper mode still works
            return runCatching { nm.notify(id, n) }.isSuccess && nm.areNotificationsEnabled()
        }
    }
}

/**
 * Lock-screen wallpaper guard (docs/bug_samsung_fond.md). Our card only goes over a lock screen that shows the home wallpaper
 * (no lock entry: clear(FLAG_LOCK) later gives back exactly that) or our own card. A separate lock wallpaper can't be read
 * back on Android 13+, so writing over it would lose it. The reads are vars: a seam for tests (Robolectric keeps one lock id).
 */
object Wall {
    var lockId: (Context) -> Int = { WallpaperManager.getInstance(it).getWallpaperId(WallpaperManager.FLAG_LOCK) }
    /** Live (animated, video, service) lock wallpaper; null info for an image. Needs the WallpaperService <queries>. */
    var lockLive: (Context) -> Boolean = { Build.VERSION.SDK_INT >= 34 && WallpaperManager.getInstance(it).getWallpaperInfo(WallpaperManager.FLAG_LOCK) != null }

    /** False also when a read throws (some OEMs): never guess over the user's wallpaper. */
    fun lockFree(ctx: Context) = runCatching { lockId(ctx).let { it < 0 || it == Prefs(ctx).wallpaperId } && !lockLive(ctx) }.getOrDefault(false)

    /** Settings and onboarding, lock modes: [hint] (wallpaper_hint), or Glose's [note] on why the word will come by notification. */
    fun explain(hint: View, note: TextView) {
        val ctx = note.context
        val lock = Prefs(ctx).mode != Prefs.MODE_NOTIF
        val own = lock && !lockFree(ctx)
        if (own) note.text = ctx.getString(R.string.lock_own) +
            if (Build.MANUFACTURER.equals("samsung", ignoreCase = true)) "\n\n" + ctx.getString(R.string.lock_own_samsung) else ""
        note.visibility = if (own) View.VISIBLE else View.GONE
        hint.visibility = if (lock && !own) View.VISIBLE else View.GONE
    }
}

/**
 * "Alarms & reminders" access (SCHEDULE_EXACT_ALARM): granted at install on Android 12-13, denied by default on 14+ for new
 * installs (and after a restore). Granting it sends ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED, which DailyReceiver
 * handles like a boot; revoking it stops the app and cancels exact alarms, so inexact ones are set at the next scheduling.
 */
object Alarms {
    fun canExact(ctx: Context) = Build.VERSION.SDK_INT < 31 || ctx.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    /** Opens this app's "Alarms & reminders" page; null when there is nothing to ask (before Android 12, or already granted). */
    fun requestIntent(ctx: Context): Intent? =
        if (canExact(ctx)) null else Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + ctx.packageName))
}
