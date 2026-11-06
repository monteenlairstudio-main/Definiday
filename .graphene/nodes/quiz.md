---
type: subsystem
summary: Evening quiz on the words seen today (4 options, same part of speech), daily reminder alarm, counters for future mascot unlocks
entry_points:
  - app/src/main/java/fr/definiday/QuizActivity.kt
covers:
  - app/src/main/java/fr/definiday/QuizActivity.kt
edges:
  - to: word-selection type: depends_on reason: questions = Words.seenToday()
---

- Quiz.record counts a quiz once per day; streak = consecutive quiz days; counters quizzes/perfectQuizzes/correctAnswers/streak live in Prefs. ACTION_QUIZ alarm at quizHour:quizMinute (default 21:00), rescheduled with the daily alarm on boot/time changes. <!-- id:96de -->
