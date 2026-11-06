---
category: decision
subject: github-branches
---

PR #1 merged 2026-11-06 (65dd718). PR #10 (interface 0.7.0, same branch claude/android-daily-word-definitions-8ngyty) merged by owner request the same day (6e86082). Workflow: work continues on the same branch, each batch goes to a NEW PR into main; release-signed APK = artifact 'definiday-release' of the main run (job 'release', environment 'release'). Owner still has to move the signing secrets from repository level to the 'release' environment (deferred by owner).
