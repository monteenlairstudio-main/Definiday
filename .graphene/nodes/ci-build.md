---
type: subsystem
summary: GitHub Actions: unit tests + assembleRelease, decodes base64 keystore secret, uploads APK and demo PNGs
entry_points:
  - .github/workflows/build.yml
covers:
  - .github/workflows/build.yml
last_commit: 1973c647fc673c6b0d35418ec31cba8e3dbadff8
edges:
  - to: release-signing type: depends_on reason: CI writes DEFINIDAY_KEYSTORE to GITHUB_ENV and passes the password secrets to the Gradle step
---

- Secrets: DEFINIDAY_KEYSTORE_BASE64, DEFINIDAY_KEYSTORE_PASSWORD, optional DEFINIDAY_KEY_ALIAS / DEFINIDAY_KEY_PASSWORD. Passed only to the decode and Gradle steps (not to third-party actions). No secret (forks) -> debug signing. Not yet run with real secrets. <!-- id:8f4a -->
- secrets.* cannot be used in step if: conditions, hence the [ -n ] guard inside the decode script. <!-- id:8f78 -->
