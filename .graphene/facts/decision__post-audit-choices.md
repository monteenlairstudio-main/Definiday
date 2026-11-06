---
category: decision
subject: post-audit-choices
---

Owner decisions after the 4 audits (2026-11-06): (1) separate Settings screen (Activity) with 4 groups: Mot du jour, Écran de verrouillage, Quiz du soir, Apparence; home keeps card, links, anecdote, 3 actions (random, quiz with 'fait ✓', Collection), seen-today, Settings button. (2) Ask the exact-alarm permission: declare SCHEDULE_EXACT_ALARM, settings button opening ACTION_REQUEST_SCHEDULE_EXACT_ALARM; inexact fallback otherwise. (3) CI signs release ONLY on main and tags (environment 'release'); other branches build debug-signed; tests run without secrets. Consequence: release-signed APKs for the owner need a merge to main or a tag.
