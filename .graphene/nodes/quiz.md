---
type: subsystem
summary: Evening quiz on the words seen today (4 options, same part of speech), daily reminder alarm, counters for future mascot unlocks
entry_points:
  - app/src/main/java/fr/definiday/QuizActivity.kt
covers:
  - app/src/main/java/fr/definiday/QuizActivity.kt
last_commit: 1a7535d
edges:
  - to: word-selection type: depends_on reason: questions = Words.seenToday()
---

- Quiz.record counts a quiz once per day; streak = consecutive quiz days; counters quizzes/perfectQuizzes/correctAnswers/streak live in Prefs. ACTION_QUIZ alarm at quizHour:quizMinute (default 21:00), rescheduled with the daily alarm on boot/time changes. <!-- id:96de -->
- Quiz.passed(score,total) = score*3 >= total*2 (owner decision: at least 2/3; 1/1, 2/2, 2/3). Prefs.passedQuizzes defaults to perfectQuizzes for users from before the counter; Quiz.record updates it BEFORE perfectQuizzes so the default is read unchanged. Score screen: Glose writes 'bravo' (passed; + Glose.Eclats overlay when perfect) or 'encore' + 'Demain, on retente ?' once (glose_ecrit_*_fin AVDs: writing animators repeatCount 0, truncated at keyTime 0.55), head icon 56 dp during questions. Test quizPassesAtTwoThirds; demo captures quiz_bravo.png / quiz_encore.png (renderDemoImages plays the quiz by matching definitions, then idles the main looper 4 s so the word is written). <!-- id:c653 -->
