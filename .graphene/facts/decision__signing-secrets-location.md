---
category: decision
subject: signing-secrets-location
---

2026-11-06: owner chose to keep the DEFINIDAY_* signing secrets as repository secrets (not moved to the `release` environment). Accepted risk: any workflow on any pushed branch (collaborators, Claude session branches) can read them; fork PRs and Dependabot cannot. Revisit when adding collaborators or opening the repo to outside contributions. build.yml comment line 23 says they are environment secrets: it still works because the job-level `environment: release` falls back to repo secrets.
