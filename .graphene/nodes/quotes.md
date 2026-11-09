---
type: subsystem
summary: French quotes (Wikiquote FR, public-domain French-language authors) mixed into daily slots; 'lenteur' quotes for the take-your-time dialog
entry_points:
  - app/src/main/java/fr/definiday/Quotes.kt
  - tools/citations/
covers:
  - app/src/main/java/fr/definiday/Quotes.kt
  - tools/citations/
  - app/src/main/assets/citations.json
last_commit: 7b46493
---

- The "annee" field of tools/citations/citations_*.json held edition years (Voltaire, Micromégas (1991)), not first-publication dates: it was emptied for all 450 quotes (texts untouched) and citations.json regenerated with python3 tools/merge_quotes.py. Quote.signature omits the parentheses when annee is empty. Only re-add a year from a reliable source for the work's first publication. <!-- id:d53f -->
- 7b46493: tools/merge_quotes.py applies typo() (copied, 3 lines: merge_words.py runs its merge at import time and would overwrite words.json) to texte/auteur/oeuvre, never source (URLs). citations.json regenerated, wording unchanged. Test quoteSlotsFollowTheRatio asserts no straight apostrophe nor plain space before ;:!? in texte. 8 quotes with NO space before ;:!? are not fixed by typo(). <!-- id:f217 -->
- 900742b: no quote text may start with « (the card adds guillemets itself; Bertrand and Staël were fixed in assets and tools/citations sources); DefinidayTest checks it. Credits string now cites Wikiquote. <!-- id:238e -->
- f3e19b5: Quote has optional `cle` (JSON field): when a quote's wording is corrected, cle keeps the original text so the history key "q:" + cle never changes (cooldown, k counter). Used for Bertrand and Staël. DefinidayTest asserts unique keys and the legacy key. <!-- id:0b3c -->
- 098c306: Quote.signature normalizes French spacing (U+202F before ; : ! ?), as 22 oeuvre fields lacked it; DefinidayTest checks every signature. Key unaffected. <!-- id:1504 -->
