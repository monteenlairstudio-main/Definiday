---
type: subsystem
summary: Release APK signing: external keystore from ~/.gradle/gradle.properties or env vars, debug-key fallback
entry_points:
  - app/build.gradle.kts: signingValue(), signingConfigs.release, buildTypes.release.signingConfig
covers:
  - app/build.gradle.kts
last_commit: 88119a8c7d54c8c4aa44ef6f96d75c02add6d07c
---

- Vars (same name as gradle property or env var): DEFINIDAY_KEYSTORE (absolute path, ~ not expanded), DEFINIDAY_KEYSTORE_PASSWORD; optional DEFINIDAY_KEY_ALIAS (default definiday), DEFINIDAY_KEY_PASSWORD (default = store password, PKCS12). <!-- id:73c4 -->
- No keystore -> debug key + logger.warn. Keystore set but password missing or file absent -> build fails on purpose (no silent debug signing). Wrong password fails at :app:packageRelease. <!-- id:65d8 -->
- Verified 2026-11-06: assembleRelease OK with and without keystore; apksigner verify --print-certs shows the keystore CN, not CN=Android Debug, when configured. <!-- id:4990 -->
- Switching from debug-signed to release-signed APK requires an uninstall (signature mismatch) which wipes local word history. Same between sideloaded APK and Play (Play App Signing). <!-- id:4dc4 -->
- .gitignore excludes *.jks and *.keystore so a keystore dropped in the clone cannot be committed by accident. <!-- id:da60 -->
- versionCode/versionName were left at 1 / 0.1.0 until commit 'Version 0.5.0': bump versionCode on every APK handed to the owner, otherwise Android may refuse the update or the version label is meaningless. First release-signed APK handed over: CI run 58 (d3cf0b4), cert SHA-256 7dba7d1c...b7fffd. <!-- id:49c1 -->
- Since the CI hardening: release key used only by the `release` job (main + tags, environment `release` with environment secrets DEFINIDAY_KEYSTORE_BASE64 / DEFINIDAY_KEYSTORE_PASSWORD / optional DEFINIDAY_KEY_ALIAS / DEFINIDAY_KEY_PASSWORD). Other branches and PRs get a debug-signed release APK (artifact definiday-debug-signed); a release-signed APK for the owner therefore needs a merge to main or a tag (artifact definiday-release). Empty env vars from absent optional secrets are fine: signingValue() drops blank values. Owner manual steps are in README > Signature release > CI GitHub Actions. <!-- id:af4b src:CI hardening session 2026-11-06 -->
