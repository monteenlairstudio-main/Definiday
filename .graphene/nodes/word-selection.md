---
type: subsystem
summary: Picks daily/random words from words.json with a local history and cooldowns (daily 270 d, random 120 d)
entry_points:
  - app/src/main/java/fr/definiday/Words.kt
  - app/src/main/java/fr/definiday/Prefs.kt
covers:
  - app/src/main/java/fr/definiday/Words.kt
  - app/src/main/java/fr/definiday/Prefs.kt
last_commit: 7cf6d3d
edges:
  - to: lexicon-data type: depends_on reason: reads assets/words.json
---

- Daily queue = never-shown words first (fixed shuffle, SEED), then oldest last-shown. Random picks never touch the first wordsPerDay x 270 queue entries: this is what guarantees a daily word unseen for 9 months whatever the random usage. Random 120-day cooldown is only best effort with 3 words/day (pool outside reserve too small); guaranteed with 1/day. Tests: dailyWordsStayAwayNineMonths, randomWordsStayAwayFourMonths. <!-- id:bcac -->
- Today's words are picked once per day and stored in Prefs.dailyDay/dailyMots (stable across app, notifications, wallpaper). History is a separate SharedPreferences file 'history' (mot -> epoch day). <!-- id:5060 -->
- Picked != seen (2026-11-06 fix): Words.today() only reserves the day's items in Prefs.dailyMots (uses a local copy of history so the loop never picks twice) and NO LONGER marks History. Words.shown(ctx, items) marks history with today; called by DailyReceiver.show (notifications actually posted and notifications enabled; the wallpaper's first item when setBitmap succeeded) and by MainActivity.show (card displayed). Words.seenToday(ctx, day) = history entries == day, daily (dailyMots order) first then the rest sorted: so lock-only mode with 2-3 words and a mid-day wordsPerDay raise burn nothing that was not shown. Unshown picks stay at the queue head for the next day. Home chips list today's daily words (shown or not) + seen words, so words 2-3 in lock-only mode stay reachable. <!-- id:dbed -->
