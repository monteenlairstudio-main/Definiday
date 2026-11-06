---
type: subsystem
summary: Evening quiz on the words seen today (4 options, same part of speech), reminder notification with Glose, counters (incl. passedQuizzes >= 2/3) feeding Unlocks, bravo/encore reaction and unlock announcements
entry_points:
  - app/src/main/java/fr/definiday/QuizActivity.kt
covers:
  - app/src/main/java/fr/definiday/QuizActivity.kt
  - app/src/test/java/fr/definiday/BugHuntTest.kt
last_commit: 6fc6deb
edges:
  - to: word-selection type: depends_on reason: questions = Words.seenToday()
  - to: mascot type: depends_on reason: score reaction, Unlocks.update/announce
---

- Quiz.record counts a quiz once per day; streak = consecutive quiz days; counters quizzes/perfectQuizzes/correctAnswers/streak live in Prefs. ACTION_QUIZ alarm at quizHour:quizMinute (default 21:00), rescheduled with the daily alarm on boot/time changes. <!-- id:96de -->
- Quiz.passed(score,total) = score*3 >= total*2 (owner decision: at least 2/3; 1/1, 2/2, 2/3). Prefs.passedQuizzes defaults to perfectQuizzes for users from before the counter; Quiz.record updates it BEFORE perfectQuizzes so the default is read unchanged. Score screen: Glose writes 'bravo' (passed; + Glose.Eclats overlay when perfect) or 'encore' + 'Demain, on retente ?' once (glose_ecrit_*_fin AVDs: writing animators repeatCount 0, truncated at keyTime 0.55), head icon 56 dp during questions. Test quizPassesAtTwoThirds; demo captures quiz_bravo.png / quiz_encore.png (renderDemoImages plays the quiz by matching definitions, then idles the main looper 4 s so the word is written). <!-- id:c653 -->
- Post-audit fixes (2026-11-06): (1) Quiz.record keeps the streak across days whose daily slots are all quotes (Words.quotesOnly(day, wordsPerDay, moreQuotes), CURRENT settings, gap bounded to 7 days) so streak unlocks are reachable with 1 word/day even with 'Plus de citations'; returns early if lastQuizDay == day or day+1 (eve's quiz finished after today's is not counted). (2) QuizActivity freezes `day` at onCreate (EXTRA_DAY from the reminder, accepted only if today or the eve; else today) and records Quiz.record(..., day); questions = Words.seenToday(ctx, day). Eve shows 'Les mots d'hier'. (3) Reminder PendingIntent carries EXTRA_DAY with FLAG_UPDATE_CURRENT (requestCode 50) and is not posted once lastQuizDay == today. (4) Quiz.build excludes distractors with the answer's definition (aruspice/haruspice). (5) Rotation: onSaveInstanceState keeps day, seed (options), answer mots, index, score and `picked` (answered question is re-revealed, never scored twice). Tests: BugHuntTest (copied from docs/audits/tests, now non-regression). <!-- id:507d -->
- B3/I10 (6fc6deb): after an answer, the right option reads '✓ mot', the picked wrong one '✗ mot', both tinted with background.mutate().setTint(GOOD/BAD) (keeps the Btn shape; setBackgroundColor erased it) and dark text #1F1A14 (7.4:1 / 6.7:1); announceForAccessibility 'Juste' / 'Faux, c’était X' on click only (not on rotation re-reveal). `done` (lastQuizDay == day or day+1 when the quiz opened, saved in the bundle) shows 'Quiz déjà fait aujourd’hui : ce score ne compte pas pour la collection.' on the score screen, also after a rotation. Texts use style Body (serif, ink), title style Label, head icon not important for accessibility. Quiz.unbroken(p, day) is the streak-continuation rule shared by Quiz.record and Unlocks.hint. <!-- id:7cbf -->
