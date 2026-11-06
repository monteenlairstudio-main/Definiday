---
type: subsystem
summary: Single Activity: today's word card, random word, seen-today chips, settings (time, count, duration, mode, unlocks, style pools)
entry_points:
  - app/src/main/java/fr/definiday/MainActivity.kt
  - app/src/main/res/layout/activity_main.xml
covers:
  - app/src/main/java/fr/definiday/MainActivity.kt
  - app/src/main/res/layout/
  - app/src/main/res/values/
  - app/src/main/res/values-night/
last_commit: 6e93968
edges:
  - to: word-selection type: depends_on reason: today/random/seenToday
  - to: styles type: depends_on reason: card colors, pool settings
  - to: quotes type: depends_on reason: slow-down dialog
---

- Académie link: no deterministic article URL; on tap, one POST to dictionnaire-academie.fr/search with X-Requested-With and Accept: application/json (without Accept the server answers 302 then 404), URL accepted only if it starts with https://www.dictionnaire-academie.fr/, CNRTL fallback. ACTION_VIEW wrapped in try/catch ActivityNotFoundException. <!-- id:ea96 -->
- Past 3 random picks in a day, a 'Prenez le temps' dialog shows once (Prefs.warnedDay) with a quote of theme 'lenteur' (fallback string if citations.json missing). <!-- id:bf9e -->
