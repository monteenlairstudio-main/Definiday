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
last_commit: 88119a8c7d54c8c4aa44ef6f96d75c02add6d07c
edges:
  - to: release-signing type: depends_on reason: the release job (main/tags, environment release) decodes the keystore and passes DEFINIDAY_* to assembleRelease
  - to: mascot type: related_to reason: generated XML is versioned because CI does not run Node
---

- Robolectric 4.17 on JDK 21 needs jvmArgs --add-opens=java.base/java.io=ALL-UNNAMED and --add-exports=java.base/jdk.internal.access=ALL-UNNAMED. Maven Central returned 429 in the cloud container: settings.gradle.kts lists the Google mirror maven-central.storage-download.googleapis.com first; android-all-instrumented jar was fetched from that mirror into ~/.m2 (sha1 checked). <!-- id:258f -->
- CRAP audit (JaCoCo in a throwaway worktree, commit 6e93968): line coverage 79 %, branch 55 %, 1/137 method over 30 (DailyReceiver.onReceive, reboot path untested); fixed by rebootPurgesExpiredWallpaper and rebootReschedulesPendingWallpaperEndAndUnlockWatch tests. JaCoCo overstates  complexity (hashCode+equals in bytecode). <!-- id:2a11 -->
- CI release signing (merged from the duplicate node ci-build, PR #2): secrets DEFINIDAY_KEYSTORE_BASE64, DEFINIDAY_KEYSTORE_PASSWORD, optional DEFINIDAY_KEY_ALIAS / DEFINIDAY_KEY_PASSWORD, passed only to the decode and Gradle steps; no secret (forks) -> debug signing. secrets.* cannot be used in step `if:`, hence a [ -n ] guard in the decode script. Windows: encode with (Resolve-Path definiday.jks).Path, ReadAllBytes resolves against .NET's cwd. Run 53 signs with the owner's key (O=Monte en l'air Studio, cert SHA-256 7dba7d1c...b7fffd); APK is v2-signed only, check with apksigner verify --print-certs. <!-- id:b2ca src:PR #2 merge, duplicate node cleanup -->
- CI hardened after audit N1/N2/N3/N8 (owner decision: release key only on main and tags): job `test` (all branches/PRs) has NO secret: wrapper-validation, testDebugUnitTest + assembleRelease debug-signed, artifact definiday-debug-signed (APK + app/build/demo/). Job `release` (needs test, if refs/heads/main or refs/tags/*, environment: release): decode keystore in $RUNNER_TEMP with umask 077, assembleRelease only, DEFINIDAY_KEYSTORE passed via step env (${{ runner.temp }}, no GITHUB_ENV), rm .jks in if: always() BEFORE upload-artifact, fails if the keystore secret is missing (N6). All actions pinned by commit SHA (# v4.x comment; checkout v4.4.0, setup-java v4.9.1, upload-artifact v4.6.2, gradle/actions/wrapper-validation v4.4.4); newer majors exist (v5/v6), Dependabot (.github/dependabot.yml, github-actions + gradle, weekly) will propose them. Verified in docs.github.com: environment secrets only reach jobs referencing the environment; on GitHub Free they work only in PUBLIC repos (Definiday is public); a workflow referencing a missing environment auto-creates it without rules; repository secrets stay readable from any branch, so the owner must delete them after moving them to the environment. actionlint 1.7.12 (pip actionlint-py) passes. Residual: N9 (no verification-metadata.xml) still open; Gradle still resolves deps in the job holding the key. <!-- id:da2c src:CI hardening session 2026-11-06 -->
- Robolectric 4.17 ShadowWallpaperManager starts with lock id 0 and doesn't shadow clear(): TestApp overrides Wall.lockId / Wall.lockLive (shared lock, our id while wallpaperEndAt != 0); WallTest changes them per case. 108 tests at 0.12.0. <!-- id:b981 -->
- 2f261b6: versionCode 15 / versionName 0.15.0; PR #18 opened (night improvements + owner choices). Release notes come from the merge commit message (first 12 lines without attribution): set a French user-facing title/body at merge time. <!-- id:2d47 -->
