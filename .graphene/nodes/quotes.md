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
last_commit: 1638e6e
---

- The "annee" field of tools/citations/citations_*.json held edition years (Voltaire, Micromégas (1991)), not first-publication dates: it was emptied for all 450 quotes (texts untouched) and citations.json regenerated with python3 tools/merge_quotes.py. Quote.signature omits the parentheses when annee is empty. Only re-add a year from a reliable source for the work's first publication. <!-- id:d53f -->
