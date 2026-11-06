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
last_commit: 6e93968
edges:
  - to: lexicon-data type: related_to reason: each word carries palette/police ids
---

- Fonts are used only for the headword; body text stays system serif. 13 of 14 fonts lack U+202F (narrow nbsp) but have U+00A0; Android falls back per glyph. Palettes pass WCAG: text/example >= 4.5:1, word/nature >= 3:1 on both gradient ends (checked by the design agent's build.py). <!-- id:6e67 -->
