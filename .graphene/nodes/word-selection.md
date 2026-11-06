---
type: subsystem
summary: Picks daily/random words from words.json with a local history and cooldowns (daily 270 d, random 120 d)
entry_points:
  - app/src/main/java/fr/definiday/Words.kt
  - app/src/main/java/fr/definiday/Prefs.kt
covers:
  - app/src/main/java/fr/definiday/Words.kt
  - app/src/main/java/fr/definiday/Prefs.kt
last_commit: 0f2f727
edges:
  - to: lexicon-data type: depends_on reason: reads assets/words.json
---

- Daily queue = never-shown words first (fixed shuffle, SEED), then oldest last-shown. Random picks never touch the first wordsPerDay x 270 queue entries: this is what guarantees a daily word unseen for 9 months whatever the random usage. Random 120-day cooldown is only best effort with 3 words/day (pool outside reserve too small); guaranteed with 1/day. Tests: dailyWordsStayAwayNineMonths, randomWordsStayAwayFourMonths. <!-- id:bcac -->
- Today's words are picked once per day and stored in Prefs.dailyDay/dailyMots (stable across app, notifications, wallpaper). History is a separate SharedPreferences file 'history' (mot -> epoch day). <!-- id:5060 -->
