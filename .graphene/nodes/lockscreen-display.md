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
last_commit: 6e93968
edges:
  - to: word-selection type: depends_on reason: shows Words.today()
  - to: styles type: depends_on reason: WordCard renders with Styles.of()
---

- ACTION_USER_PRESENT cannot be received by a manifest receiver since Android 8: unlock counting needs a running process, hence UnlockService (foreground service type specialUse, silent IMPORTANCE_MIN notification). startForegroundService may be refused from background when the alarm is inexact: then only the duration END alarm removes the wallpaper (duration is always the upper bound). <!-- id:a2ba -->
- Wallpaper removal only if WallpaperManager.getWallpaperId(FLAG_LOCK) still equals the id returned by setBitmap (user may have changed it, or prefs restored on another phone). Stale END after a newer SHOW is ignored unless force=true (Nth unlock). clear(FLAG_LOCK) makes the lock screen fall back to the home wallpaper; the original lock image cannot be restored without storage permission. <!-- id:3e7d -->
- Alarms: setExactAndAllowWhileIdle when SDK<31 or canScheduleExactAlarms(), else setAndAllowWhileIdle (Android only promises delivery within the hour). Rescheduled on BOOT_COMPLETED, MY_PACKAGE_REPLACED, TIME_SET, TIMEZONE_CHANGED (all exempt from implicit-broadcast limits). <!-- id:e5de -->
- Known UX issue reported by the owner: with no notification on the lock screen, the system's big clock covers the text that starts at 42% of the height. Fix in progress: bottom-anchored text block. <!-- id:d268 -->
