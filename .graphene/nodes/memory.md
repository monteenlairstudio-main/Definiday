---
type: subsystem
summary: Spaced review ladder (J+2/7/30/90) in the evening quiz, cloze questions, favourites and the « Mes mots » screen with light stats
entry_points:
  - app/src/main/java/fr/definiday/Memory.kt
  - app/src/main/java/fr/definiday/MotsActivity.kt
  - app/src/main/java/fr/definiday/QuizActivity.kt
covers:
  - app/src/main/java/fr/definiday/Memory.kt
  - app/src/main/java/fr/definiday/MotsActivity.kt
  - app/src/test/java/fr/definiday/MemoryTest.kt
  - app/src/main/res/drawable/favori.xml
last_commit: 5219918
---

- 5219918: SharedPreferences 'memoire' (backed up like history): m:<mot> -> 'start/step/misses' (step 4 = learnt), favoris StringSet, reviews/reviewsRight Ints; junk ignored/clamped. Due at start + J+2/7/30/90; start = first shown (History day if no entry) or last miss; miss -> back to J+2; right on an overdue word jumps to the next rung still ahead. Max 2 reviews per evening, most overdue first, never tonight's words, only on evenings with own words (quote-only days still no quiz). Reviews come after tonight's questions (header quiz_review), count in score/passed/correctAnswers/unlocks; memory updates on every answer even in a redone quiz. Distractors: same nature first, preferring seen words (not tonight's answers). Cloze 1 question in 3 (Quiz.cloze: headword or its stem minus last 2 letters, case-insensitive, \p{L} not \w/\b; 1336/1342 words), TalkBack '(mot manquant)'. Saved state stores review count. MotsActivity: ListView, search ignores case/accents/œ on word or definition, 'Favoris seulement', six stat tiles, tap opens MainActivity EXTRA_MOT (which marks it seen today: labelled 'Mot au hasard'). Gotchas: one vector with a colour state list shares state across a list (use selector of two vectors, favori.xml); Switch(ctx,null,0,R.style.Toggle) crashes (Toggle has no parent switch style). Tests must find answers via quizAnswer() (definition or cloze). <!-- id:8fac -->
- da29bc4: tapping a word in « Mes mots » opens DetailActivity (lightweight card: word, nature, definition, example, anecdote, links, favourite, share) WITHOUT marking it seen (no History write); back returns to the list (no more Home > Mes mots > Home loop). Star TalkBack label includes the word (favori_add / favori_remove). Quiz end lists missed words « À revoir : ». <!-- id:d359 -->
- 18a6904 (fon-E): Mes mots has a second switch « À revoir seulement » (Quiz.due without the cap of 2; with favourites = intersection), empty state text; search + both filters survive rotation (saved by hand, views have no ids). The register line now counts due words (stat_due) instead of words missed >= 2 (stat_fragile): owner to confirm. Memory.Entry comment in Prefs.kt still says « fragile ». <!-- id:fc42 -->
