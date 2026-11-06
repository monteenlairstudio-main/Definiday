---
type: subsystem
summary: French word list app/src/main/assets/words.json generated from tools/lexique batches by tools/merge_words.py; tools/check_copies.py checks against Wiktionary
entry_points:
  - python3 tools/merge_words.py tools/lexique/words_*.json tools/lexique/qa_*.json tools/lexique/enrich_*.json
covers:
  - app/src/main/assets/words.json
  - tools/merge_words.py
  - tools/check_copies.py
  - tools/lexique/
last_commit: 1973c647fc673c6b0d35418ec31cba8e3dbadff8
---
