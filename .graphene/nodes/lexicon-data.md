---
type: dataset
summary: 1342 rare words with original definitions (none containing the headword), sourced anecdotes and per-word style; generated from tools/lexique batches + QA patches
entry_points:
  - tools/merge_words.py
  - tools/check_copies.py
  - app/src/main/assets/words.json
covers:
  - tools/
  - app/src/main/assets/words.json
last_commit: 1638e6e
edges:
  - to: styles type: related_to reason: each word carries palette/police ids
---

- CGU of dictionnaire-academie.fr forbid reproduction and automated extraction: never copy or scrape Académie/CNRTL. Definitions are original, checked against Wiktionnaire (CC BY-SA). check_copies.py compares each definition with the Wiktionnaire dump; 90 near-copies were reformulated, last run 0 flagged. <!-- id:e419 -->
- Regenerate words.json with: python3 tools/merge_words.py tools/lexique/words_*.json tools/lexique/qa_*.json tools/lexique/enrich_*.json tools/lexique/style_*.json. Later files patch field by field; {mot,_supprimer:true} deletes; partial patches (no 'nature') for removed words are ignored. typo() applies curly apostrophe and U+202F before ; : ! ? (not to 'source'). <!-- id:34a7 -->
- Wikimedia API rate-limits (429) when several agents query in parallel. Use the local dump indexes instead: frwiktionary pages-articles -> sqlite page(title, fr) (2,036,794 French sections), frwikiquote -> sqlite page(title, text). Built in the session scratchpad, not committed (rebuild from dumps.wikimedia.org). <!-- id:12b5 -->
- Audit 2 data fix (tools/lexique/qa_audit2.json): 150 definitions that contained the headword or its obvious family (bombance « faire bombance », guerroyer/guerre, vipérin/vipère, Marivaux/marivaudage…) were reformulated; in set phrases the headword is replaced by « … » (e.g. « faire … » : festoyer). aruspice deleted (graphic variant of haruspice); mercuriale/semonce, gourmander/tancer, faribole/billevesée, faquin/bélître differentiated. 1342 words. GOTCHA: merge order is glob-alphabetical, so qa_audit2 is overridden by later qa_lead/qa_pz/qa_rare_*/qa_reformulation*/qa_v4* for the same word: those 9 overrides (devantier, jaboter, philippique, trotte-menu, embéguiner, empourprer, incarnadin, tracassin) were edited in the later files too. Always check the merged output, not the patch. Check headword leaks with a normalized-accent prefix scan of each lemma part against the definition tokens (remaining hits are false positives: intercesseur, irrépréhensible, prestolet, superfétatoire). check_copies: 0 flagged. <!-- id:1334 -->
