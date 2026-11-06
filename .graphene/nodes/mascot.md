---
type: subsystem
summary: Monsieur Glose mascot: Paper.js sources + Node pipeline (design/mascotte) generating VectorDrawable/AVD XML into app res; accessories and rare-skin details toggled by color theme attributes
entry_points:
  - design/mascotte/android/make_android.js
  - design/mascotte/android/svg2avd.js
  - design/mascotte/glose_lib.js
  - app/src/main/res/values/glose_styles.xml
covers:
  - design/mascotte/
  - app/src/main/res/drawable/glose_*
  - app/src/main/res/values/glose_attrs.xml
  - app/src/main/res/values/glose_styles.xml
  - app/src/main/res/interpolator/
edges:
  - to: build-ci type: related_to reason: generated XML is versioned because CI does not run Node
---

- Regenerate with `node android/make_android.js` from design/mascotte (needs node_modules/paper and PLAYWRIGHT=/opt/node-tools/node_modules/playwright). Each pose is exported as VARIANT {all:true}: every accessory, ribbon AND bookmark, gilt+star sparkles and glints drawn in place; book/cup covers use sentinel hexes (#2C3A01..#2C3A04, #9C6B03) mapped to tokens gloseBook/gloseBookTop/gloseBookLow/gloseCup. svg2avd GATES map group ids (noeud, echarpe(_dos), bonnet, plume, marque_page, ruban, paillettes_*, etoiles_*, *eclats) to per-color attrs glose<Gate>_<hex>, transparent in style Glose, opened by Glose.Acc.* / Glose.Pelage.*. Gate values that are fur tokens are ?attr/gloseFur (attr-to-attr reference in a style). <!-- id:d485 -->
- Accessory choice (2026-11-06, no emulator): NOT scaleX=?attr on groups (undocumented) and NOT overlay drawables (z-order breaks: quill is behind the skull, scarf under the chin, bookmark replaces the ribbon; redrawing translucent shading doubles it). Color theme attrs only, documented and verified in Robolectric. Cost: all-mode VDs ~180 KB text each, release APK 1.24 -> 1.61 MB. <!-- id:1db9 -->
- Step 1 verification without emulator: GloseTest (Robolectric GraphicsMode NATIVE, real framework VectorDrawable inflation) inflates every glose_* VD/AVD with and without all overlays, asserts each Glose.* overlay changes pixels, and writes app/build/demo/glose/{poses,overlays_assis,combos}.png. Attr-to-attr gate values (bonnet ear tips = ?attr/gloseFur) resolve correctly. Icons (glose_icone*) crop the bonnet and quill: apply only pelage/pelote overlays to icons. Glose.drawable(ctx, res, styles) = ContextThemeWrapper(ctx, R.style.Glose) + theme.applyStyle(each, true). <!-- id:2b0e -->
