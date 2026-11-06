---
type: subsystem
summary: Alarms, notifications, lock-screen wallpaper card, display modes and unlock-count removal
entry_points:
  - app/src/main/java/fr/definiday/DailyReceiver.kt
  - app/src/main/java/fr/definiday/UnlockService.kt
  - app/src/main/java/fr/definiday/WordCard.kt
covers:
  - app/src/main/java/fr/definiday/DailyReceiver.kt
  - app/src/main/java/fr/definiday/UnlockService.kt
  - app/src/main/java/fr/definiday/WordCard.kt
  - app/src/main/AndroidManifest.xml
last_commit: 9bd48ae
edges:
  - to: word-selection type: depends_on reason: shows Words.today()
  - to: styles type: depends_on reason: WordCard renders with Styles.of()
---

- ACTION_USER_PRESENT cannot be received by a manifest receiver since Android 8: unlock counting needs a running process, hence UnlockService (foreground service type specialUse, silent IMPORTANCE_MIN notification). startForegroundService may be refused from background when the alarm is inexact: then only the duration END alarm removes the wallpaper (duration is always the upper bound). <!-- id:a2ba -->
- Wallpaper removal only if WallpaperManager.getWallpaperId(FLAG_LOCK) still equals the id returned by setBitmap (user may have changed it, or prefs restored on another phone). Stale END after a newer SHOW is ignored unless force=true (Nth unlock). clear(FLAG_LOCK) makes the lock screen fall back to the home wallpaper; the original lock image cannot be restored without storage permission. <!-- id:3e7d -->
- Alarms: setExactAndAllowWhileIdle when SDK<31 or canScheduleExactAlarms(), else setAndAllowWhileIdle (Android only promises delivery within the hour). Rescheduled on BOOT_COMPLETED, MY_PACKAGE_REPLACED, TIME_SET, TIMEZONE_CHANGED (all exempt from implicit-broadcast limits). <!-- id:e5de -->
- Known UX issue reported by the owner: with no notification on the lock screen, the system's big clock covers the text that starts at 42% of the height. Fix in progress: bottom-anchored text block. <!-- id:d268 -->
- Lock-screen text is now bottom-anchored (ends at 84 % of the height, shrinks down to 60 % size to fit in 40 % of the screen) so the big system clock shown when there is no notification no longer covers it. Setting 'Position du texte' keeps the old mid-screen layout as an option (OEM clocks differ). <!-- id:f30e -->
- DailyReceiver.show(ctx, words) cancels notifications 1..3 then posts/renders the given words: the random button calls it with the random pick, so it replaces the lock-screen word. <!-- id:58c1 -->
- Quiz reminder notification (DailyReceiver.quizReminder): BigPictureStyle 512x256 bitmap of the user's Glose (glose_soir_vd once 'soir' is unlocked, else glose_assis_vd) and large icon = sleeping head glose_icone_dort_vd (192 px, no accessory) via Icon.createWithBitmap. Notifications cannot play AVDs, so Glose.bitmap draws the VD. Transparent background: legibility on dark notification shades not checked on a device. No mascot on the lock-screen word (owner decision). <!-- id:3248 -->
- Exact alarms (2026-11-06, owner decision): manifest declares SCHEDULE_EXACT_ALARM (not USE_EXACT_ALARM: Play reserves it for alarm/calendar apps). Per developer.android.com: granted at install on 12-13, NOT pre-granted to fresh installs targeting 33+ on Android 14+ (and denied after backup-restore to 14); revoking stops the app and cancels exact alarms; granting sends AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED, now in DailyReceiver's intent-filter and handled like BOOT (reschedule + re-arm END/UnlockService). Exact alarms are exempt from FGS background-start limits, so 'retrait au Nᵉ déverrouillage' works from the morning alarm once granted. API for the Settings screen: Alarms.canExact(ctx) and Alarms.requestIntent(ctx) (Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM + package: URI, null when nothing to ask), in DailyReceiver.kt. nextTrigger now anchors on now.toLocalDate() (ZonedDateTime.of resolves the fall-back overlap to the earlier offset): a late delivery in the repeated hour no longer re-shows the same day. Robolectric: ShadowAlarmManager.setCanScheduleExactAlarms, exact = windowLengthMs 0. <!-- id:3d0c -->
