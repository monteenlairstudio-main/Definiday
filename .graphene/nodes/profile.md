---
type: subsystem
summary: User profile safety: JSON export/import of prefs files (definiday, history, memoire) in Réglages › Mon profil; test builds install side by side as fr.definiday.dev
entry_points:
  - app/src/main/java/fr/definiday/Profile.kt
  - docs/profil.md
  - app/src/dev/res/values/dev.xml
covers:
  - app/src/main/java/fr/definiday/Profile.kt
  - app/src/test/java/fr/definiday/ProfileTest.kt
  - app/src/dev/
  - docs/profil.md
last_commit: 7385d43
---

- Why profiles were lost: installing an APK with another signature forces an uninstall; CI test builds were signed with a throwaway debug key per run. Fix (dcfb5dc): every debug-signed build (debug, or release without keystore) gets applicationIdSuffix '.dev', label « Definiday (test) », wax_dark launcher bg (src/dev/res); share authority ${applicationId}.share (ShareProvider.authority(ctx)); CI test artifact renamed definiday-test. Robolectric tests run as fr.definiday.dev: never hard-code 'fr.definiday'. Export/import (7385d43): Profile.FILES = definiday, history, memoire (wallpaper_state is device-local, excluded from export and Auto Backup). Format {format:'definiday-profil', version:1, app, date, files:{file:{key:{t:string|int|long|boolean|float|set, v}}}}. Accepted keys: definiday = Profile.SETTINGS (35 Prefs keys) -> ADD ANY NEW Prefs KEY THERE or import drops it; history any key as long; memoire m:* string, favoris set, reviews/reviewsRight int. Any wrong type/range/size rejects the whole file (4 MiB per file, 10k chars, 100k entries); unknown keys/files ignored. Profile.read validates only; Profile.apply commits then scheduleNext/scheduleQuiz, UnlockService.sync, Widget.update. InputStream.readNBytes is API 33: bounded manual loop. <!-- id:bb26 -->
- 2026-11-08 (1ca8efc): golden fixture app/src/test/resources/profil-0.14.0.json (all SETTINGS keys + later-version key/file) must import unchanged (ProfileTest.profileFrom0140ImportsUnchanged). ProfileTest.everyPrefsKeyIsInTheProfile scans Prefs.kt source for keys used on `sp` (definiday file) and fails if one is missing from Profile.SETTINGS (now internal). Keys on `local` (wallpaper_state: unlocksSeen, wallpaperId, wallpaperEndAt, lockBlocked, updateCheckedAt) are device-local by design. Adding a Prefs key without SETTINGS now breaks the build. <!-- id:0e1b -->
