---
type: infra
summary: Gradle (AGP 8.13.2, Kotlin 2.2.21, no AndroidX), Robolectric tests, GitHub Actions APK build
entry_points:
  - app/build.gradle.kts
  - settings.gradle.kts
  - .github/workflows/build.yml
  - app/src/test/java/fr/definiday/DefinidayTest.kt
covers:
  - app/build.gradle.kts
  - settings.gradle.kts
  - build.gradle.kts
  - gradle.properties
  - gradle/
  - .github/
  - app/src/test/
last_commit: a2935fa
---

- Robolectric 4.17 on JDK 21 needs jvmArgs --add-opens=java.base/java.io=ALL-UNNAMED and --add-exports=java.base/jdk.internal.access=ALL-UNNAMED. Maven Central returned 429 in the cloud container: settings.gradle.kts lists the Google mirror maven-central.storage-download.googleapis.com first; android-all-instrumented jar was fetched from that mirror into ~/.m2 (sha1 checked). <!-- id:258f -->
- Release APK is signed with the debug key (ponytail comment in app/build.gradle.kts); Play Protect flags the sideloaded APK. A separate suggested task covers a real release keystore via env vars / GitHub secrets. <!-- id:dfdf -->
- CRAP audit (JaCoCo in a throwaway worktree, commit 6e93968): line coverage 79 %, branch 55 %, 1/137 method over 30 (DailyReceiver.onReceive, reboot path untested); fixed by rebootPurgesExpiredWallpaper and rebootReschedulesPendingWallpaperEndAndUnlockWatch tests. JaCoCo overstates  complexity (hashCode+equals in bytecode). <!-- id:2a11 -->
