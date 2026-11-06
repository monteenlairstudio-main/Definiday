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
edges:
  - to: release-signing type: depends_on reason: CI writes DEFINIDAY_KEYSTORE to GITHUB_ENV and passes the password secrets to the Gradle step
---

- Robolectric 4.17 on JDK 21 needs jvmArgs --add-opens=java.base/java.io=ALL-UNNAMED and --add-exports=java.base/jdk.internal.access=ALL-UNNAMED. Maven Central returned 429 in the cloud container: settings.gradle.kts lists the Google mirror maven-central.storage-download.googleapis.com first; android-all-instrumented jar was fetched from that mirror into ~/.m2 (sha1 checked). <!-- id:258f -->
- CRAP audit (JaCoCo in a throwaway worktree, commit 6e93968): line coverage 79 %, branch 55 %, 1/137 method over 30 (DailyReceiver.onReceive, reboot path untested); fixed by rebootPurgesExpiredWallpaper and rebootReschedulesPendingWallpaperEndAndUnlockWatch tests. JaCoCo overstates  complexity (hashCode+equals in bytecode). <!-- id:2a11 -->
- CI release signing (merged from the duplicate node ci-build, PR #2): secrets DEFINIDAY_KEYSTORE_BASE64, DEFINIDAY_KEYSTORE_PASSWORD, optional DEFINIDAY_KEY_ALIAS / DEFINIDAY_KEY_PASSWORD, passed only to the decode and Gradle steps; no secret (forks) -> debug signing. secrets.* cannot be used in step `if:`, hence a [ -n ] guard in the decode script. Windows: encode with (Resolve-Path definiday.jks).Path, ReadAllBytes resolves against .NET's cwd. Run 53 signs with the owner's key (O=Monte en l'air Studio, cert SHA-256 7dba7d1c...b7fffd); APK is v2-signed only, check with apksigner verify --print-certs. <!-- id:b2ca src:PR #2 merge, duplicate node cleanup -->
