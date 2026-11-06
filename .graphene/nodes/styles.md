---
type: subsystem
summary: Palette/font catalog (18 palettes, 14 OFL fonts) and per-word style picked within the user's pools
entry_points:
  - app/src/main/java/fr/definiday/Styles.kt
  - app/src/main/assets/styles.json
covers:
  - app/src/main/java/fr/definiday/Styles.kt
  - app/src/main/assets/styles.json
  - app/src/main/res/font/
  - app/src/main/assets/licences/
last_commit: 6fc6deb
edges:
  - to: lexicon-data type: related_to reason: each word carries palette/police ids
---

- Fonts are used only for the headword; body text stays system serif. 13 of 14 fonts lack U+202F (narrow nbsp) but have U+00A0; Android falls back per glyph. Palettes pass WCAG: text/example >= 4.5:1, word/nature >= 3:1 on both gradient ends (checked by the design agent's build.py). <!-- id:6e67 -->
- Contrast pass (6fc6deb, audit I7): nature is also the card label (12 sp bold) and the signature, so it must reach 4.5:1, not 3:1 as in the design agent's build.py. Darkened nature: aube #904352, pistache #4E6729, givre #466884, lavande #644F87, sepia #69462D, sanguine #963921. All 18 palettes now: texte/exemple/nature >= 4.5:1 and mot >= 3:1 against both fond_haut and fond_bas. Edit styles.json by hand (build.py writes to the main checkout and would revert these). <!-- id:b671 -->
