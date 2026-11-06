---
type: subsystem
summary: First-launch onboarding (OnboardingActivity): welcome, where (mode + lock preview + wallpaper warning), when (time, words/day, quiz), permission in context; no paywall, skippable
entry_points:
  - app/src/main/java/fr/definiday/OnboardingActivity.kt
  - app/src/main/res/layout/activity_onboarding.xml
covers:
  - app/src/main/java/fr/definiday/OnboardingActivity.kt
  - app/src/main/res/layout/activity_onboarding.xml
  - app/src/test/java/fr/definiday/OnboardingTest.kt
  - app/src/test/resources/robolectric.properties
last_commit: f80b7fa
---

- f80b7fa: Prefs.onboarded = bool('onboarded', sp.contains('dailyDay')) so existing users never see it; preview uses Words.nextDaily(load, History.all()) (not today()), so quitting mid-way shows it again with choices kept. MainActivity starts it and finishes when !onboarded (first-launch POST_NOTIFICATIONS request moved here; refusers covered by notif_banner). Step 2 preselects current mode (notification only); preview + wallpaper warning only once a lock mode is picked. Permission step only when needed (API 33+, notifications used, not granted; exact alarm block only if unlock removal on and exact refused). Finish/skip: onboarded, scheduleNext + scheduleQuiz. targetSdk 36: Android 16 never calls onBackPressed -> also OnBackInvokedCallback (API 33+). Tests: robolectric.properties + TestApp mark onboarding done for every test; a test needing a fresh install must clear 'definiday' prefs. Welcome uses Glose.mainPose, not ecrit_coucou (a reward). <!-- id:7d1a -->
