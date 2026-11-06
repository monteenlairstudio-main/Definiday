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
- Windows owner encodes the keystore in PowerShell: [IO.File]::ReadAllBytes("rel.jks") resolves against .NET's cwd (often C:\WINDOWS\system32), not PowerShell's location -> FileNotFoundException. Use (Resolve-Path definiday.jks).Path. README documents the fixed command. <!-- id:a77b src:user error report 2026-11-06 -->
- First CI run with secrets (run 50, 2026-11-06) failed at :app:packageRelease with "keystore password was incorrect": keystore decoded and read, password secret did not open it. Config issue on the owner side, not code. On PKCS12 a corrupted file gives the same message (MAC check), so check with keytool -list locally first. <!-- id:687d -->
- 2026-11-06 run 53: CI signs with the owner's release key (regenerated keystore, CN=n/a, O=Monte en l'air Studio, cert SHA-256 7dba7d1c...b7fffd). APK has v2 signature only (minSdk 26, AGP drops v1), so keytool -printcert -jarfile won't show it: use apksigner verify --print-certs. Artifact download URL from the GitHub MCP is a short-lived signed blob URL, curl works from the container. <!-- id:5cd4 -->
