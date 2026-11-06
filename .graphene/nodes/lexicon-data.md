---
type: dataset
summary: 1343 rare words with original definitions, 801 sourced anecdotes and per-word style; generated from tools/lexique batches + QA patches
entry_points:
  - tools/merge_words.py
  - tools/check_copies.py
  - app/src/main/assets/words.json
covers:
  - tools/
  - app/src/main/assets/words.json
last_commit: 6e93968
edges:
  - to: styles type: related_to reason: each word carries palette/police ids
---

- CGU of dictionnaire-academie.fr forbid reproduction and automated extraction: never copy or scrape Académie/CNRTL. Definitions are original, checked against Wiktionnaire (CC BY-SA). check_copies.py compares each definition with the Wiktionnaire dump; 90 near-copies were reformulated, last run 0 flagged. <!-- id:e419 -->
- Regenerate words.json with: python3 tools/merge_words.py tools/lexique/words_*.json tools/lexique/qa_*.json tools/lexique/enrich_*.json tools/lexique/style_*.json. Later files patch field by field; {mot,_supprimer:true} deletes; partial patches (no 'nature') for removed words are ignored. typo() applies curly apostrophe and U+202F before ; : ! ? (not to 'source'). <!-- id:34a7 -->
- Wikimedia API rate-limits (429) when several agents query in parallel. Use the local dump indexes instead: frwiktionary pages-articles -> sqlite page(title, fr) (2,036,794 French sections), frwikiquote -> sqlite page(title, text). Built in the session scratchpad, not committed (rebuild from dumps.wikimedia.org). <!-- id:12b5 -->
