---
type: subsystem
summary: Main Activity: Monsieur Glose header, today's word card, random word, seen-today chips, quiz and collection buttons, settings (time, count, duration, mode, unlocks, style pools)
entry_points:
  - app/src/main/java/fr/definiday/MainActivity.kt
  - app/src/main/res/layout/activity_main.xml
covers:
  - app/src/main/java/fr/definiday/MainActivity.kt
  - app/src/main/res/layout/
  - app/src/main/res/values/
  - app/src/main/res/values-night/
last_commit: 0ce50ba
edges:
  - to: word-selection type: depends_on reason: today/random/seenToday
  - to: styles type: depends_on reason: card colors, pool settings
  - to: quotes type: depends_on reason: slow-down dialog
  - to: mascot type: depends_on reason: Glose header, Collection button
---

- Académie link: no deterministic article URL; on tap, one POST to dictionnaire-academie.fr/search with X-Requested-With and Accept: application/json (without Accept the server answers 302 then 404), URL accepted only if it starts with https://www.dictionnaire-academie.fr/, CNRTL fallback. ACTION_VIEW wrapped in try/catch ActivityNotFoundException. <!-- id:ea96 -->
- Past 3 random picks in a day, a 'Prenez le temps' dialog shows once (Prefs.warnedDay) with a quote of theme 'lenteur' (fallback string if citations.json missing). <!-- id:bf9e -->
- Monsieur Glose (ImageView R.id.glose, 160 dp) sits above the word card: drawable refreshed in onResume (pose/overlays may have changed in Collection), Glose.play() = AVD.reset()+start() only if ValueAnimator.areAnimatorsEnabled(), stopped in onPause, tap replays. <!-- id:d9cd -->
- Step 8: Glose.mainPose(p, hour) returns glose_soir from 21:00 local time until midnight once 'soir' is unlocked, else the collection pose; evaluated in onResume (a screen left open across 21:00 switches on next resume). Version 0.6.0 (versionCode 6) ships the mascot; release APK 1.61 MB. <!-- id:38cc -->
