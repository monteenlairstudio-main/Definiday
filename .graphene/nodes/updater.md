---
type: subsystem
summary: Self-update from GitHub Releases: CI publishes v<versionName> with signed APK + update.json; Réglages › Mises à jour checks, downloads, verifies SHA-256 and installs via PackageInstaller
entry_points:
  - app/src/main/java/fr/definiday/Update.kt
  - .github/workflows/build.yml
  - docs/mise_a_jour.md
covers:
  - app/src/main/java/fr/definiday/Update.kt
  - app/src/test/java/fr/definiday/UpdateTest.kt
  - docs/mise_a_jour.md
last_commit: 20cf913
---

- 20cf913: CI release job (main only, after signed build, gh + GITHUB_TOKEN, job-level contents: write, concurrency: release) reads versionName/versionCode/minSdk from app/build.gradle.kts; creates release v<versionName> (latest) with definiday-<versionName>.apk + update.json {versionCode, versionName, apk, sha256, minSdk, notes}; tag exists with same versionCode -> skip, different -> fail 'bump versionName'; never replaces a published release; tags made with GITHUB_TOKEN don't re-trigger workflows; manual tags publish nothing. App: Update.kt fetches releases/latest/download/update.json (64 KiB cap, timeouts, https), newer if versionCode higher and minSdk fits; Install: canRequestPackageInstalls else ACTION_MANAGE_UNKNOWN_APP_SOURCES, download to cache (64 MiB cap, progress), SHA-256 check (bad file deleted), APK package must equal ctx.packageName, PackageInstaller MODE_FULL_INSTALL setAppPackageName, UpdateReceiver explicit PendingIntent (STATUS_PENDING_USER_ACTION -> confirm intent; signature mismatch explained). Update.trusted accepts only https://github.com/monteenlairstudio-main/Definiday/releases/... (exact case): renaming repo/owner breaks updates for installed phones. Prefs.updateAuto (weekly check, default OFF, in Profile.SETTINGS); updateCheckedAt in wallpaper_state (device-local). Update.open is the single network seam for tests. BuildConfig.SELF_UPDATE=true (buildConfig enabled); a Play build must set false and remove REQUEST_INSTALL_PACKAGES (tools:node='remove'). Optional « Exporter mon profil d’abord » on the offer. To publish: bump versionCode AND versionName, merge to main. <!-- id:8586 -->
- 2026-11-07: first real release v0.13.0 published by CI from merge 3936686: assets definiday-0.13.0.apk (1,974,598 B) + update.json; releases/latest/download/update.json resolves anonymously; downloaded APK sha256 matches update.json, signed with owner key (7dba7d1c…), versionCode 13, has REQUEST_INSTALL_PACKAGES. PR #17 = 0.14.0 version-only bump for the first in-app update test (0.13 -> 0.14). <!-- id:77d6 -->
- 2026-11-07: release v0.14.0 published by main CI after merge 0dd774b. update.json reachable at releases/latest/download (versionCode 14, sha256 324e92f3…ca8f matches APK), APK signed with owner cert 7dba7d1c…, versionCode 14. Ready for first real on-device 0.13 -> 0.14 in-app update test. <!-- id:592d -->
