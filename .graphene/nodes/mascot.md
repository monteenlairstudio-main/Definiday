---
type: subsystem
summary: Monsieur Glose mascot: Paper.js sources + Node pipeline (design/mascotte) generating VectorDrawable/AVD XML into app res; accessories and rare-skin details toggled by color theme attributes
entry_points:
  - design/mascotte/android/make_android.js
  - design/mascotte/android/svg2avd.js
  - app/src/main/java/fr/definiday/Glose.kt
  - app/src/main/java/fr/definiday/Unlocks.kt
  - app/src/main/java/fr/definiday/CollectionActivity.kt
  - app/src/main/res/values/glose_styles.xml
covers:
  - design/mascotte/
  - app/src/main/java/fr/definiday/Glose.kt
  - app/src/main/java/fr/definiday/Unlocks.kt
  - app/src/main/java/fr/definiday/CollectionActivity.kt
  - app/src/test/java/fr/definiday/GloseTest.kt
  - app/src/main/res/drawable/glose_*
  - app/src/main/res/values/glose_attrs.xml
  - app/src/main/res/values/glose_styles.xml
  - app/src/main/res/interpolator/
last_commit: 6fc6deb
edges:
  - to: build-ci type: related_to reason: generated XML is versioned because CI does not run Node
---

- Regenerate with `node android/make_android.js` from design/mascotte (needs node_modules/paper and PLAYWRIGHT=/opt/node-tools/node_modules/playwright). Each pose is exported as VARIANT {all:true}: every accessory, ribbon AND bookmark, gilt+star sparkles and glints drawn in place; book/cup covers use sentinel hexes (#2C3A01..#2C3A04, #9C6B03) mapped to tokens gloseBook/gloseBookTop/gloseBookLow/gloseCup. svg2avd GATES map group ids (noeud, echarpe(_dos), bonnet, plume, marque_page, ruban, paillettes_*, etoiles_*, *eclats) to per-color attrs glose<Gate>_<hex>, transparent in style Glose, opened by Glose.Acc.* / Glose.Pelage.*. Gate values that are fur tokens are ?attr/gloseFur (attr-to-attr reference in a style). <!-- id:d485 -->
- Accessory choice (2026-11-06, no emulator): NOT scaleX=?attr on groups (undocumented) and NOT overlay drawables (z-order breaks: quill is behind the skull, scarf under the chin, bookmark replaces the ribbon; redrawing translucent shading doubles it). Color theme attrs only, documented and verified in Robolectric. Cost: all-mode VDs ~180 KB text each, release APK 1.24 -> 1.61 MB. <!-- id:1db9 -->
- Step 1 verification without emulator: GloseTest (Robolectric GraphicsMode NATIVE, real framework VectorDrawable inflation) inflates every glose_* VD/AVD with and without all overlays, asserts each Glose.* overlay changes pixels, and writes app/build/demo/glose/{poses,overlays_assis,combos}.png. Attr-to-attr gate values (bonnet ear tips = ?attr/gloseFur) resolve correctly. Icons (glose_icone*) crop the bonnet and quill: apply only pelage/pelote overlays to icons. Glose.drawable(ctx, res, styles) = ContextThemeWrapper(ctx, R.style.Glose) + theme.applyStyle(each, true). <!-- id:2b0e -->
- AVD gotcha: framework AVD uses the RenderThread animator, which only really starts at the first draw; isRunning() stays false until then (in Robolectric, draw the view on a software canvas first, which falls back to the UI-thread animator). <!-- id:b91f -->
- Unlocks.items is the owner's table (thresholds on quizzes/perfect/passed/streak, never correctAnswers): written words are pose items (ecrit_mot, ecrit_encore, ecrit_bravo, ecrit_dodo, ecrit_rare). Unlocks.of() is pure; Unlocks.update(p) unions into Prefs.unlocked (never shrinks, broken streak loses nothing) and returns items not in Prefs.announced; QuizActivity announces them once ('Nouveau : ...', Glose previewing the last/rarest). Test unlockTable holds the table as data + boundary checks. <!-- id:add7 -->
- CollectionActivity (programmatic layout, no AndroidX): 200 dp animated preview of the trial choice, 'Choisir' saves via Glose.save into Prefs.glosePose/glosePelage/glosePelote/gloseAcc (one accessory or none; second tap on the worn accessory removes it). Grid of 96 dp still thumbnails (AVD never started); locked = ColorFilter SRC_IN #B8AFA0 silhouette + Unlocks.hint. Glose.choice(p) falls back to defaults for ids not in Prefs.unlocked. Entry: main-screen button and 'Voir la collection' after an announcement. Demo capture app/build/demo/collection.png. Opening it inflates ~21 VDs (each theme is a separate drawable cache entry): watch first-open time on low-end phones. <!-- id:c303 -->
- Step 6: Glose.mine(ctx, res, accessory, extra) = the user's saved choice applied wherever Glose shows (main screen pose+overlays, quiz head icon without accessory, bravo/encore reaction, unlock preview = choice with the new item substituted). Demo capture app/build/demo/app_choice.png (livres + nuit + sauge + bonnet). <!-- id:ab7f -->
- Step 7: quiz notification pictures (see lockscreen-display). Test quizNotificationShowsSleepingGlose writes app/build/demo/notif_quiz.png. <!-- id:430a -->
- Glose.play with animators disabled (developer option or battery saver) now returns WITHOUT reset(): AVD.reset() applies the animators' start values (trimPathEnd 0), which erased the word of the 'le fil écrit…' poses; the untouched AVD draws its VD still with the word fully written. Test GloseTest.writtenPoseKeepsItsWordWithoutAnimations (duration scale 0 via reflective ValueAnimator.setDurationScale; fails with reset: 2245 px differ). Also Glose.choice/pose ignore ids of the wrong Kind (tampered prefs). <!-- id:cebe -->
- P8 fixed (6fc6deb): Unlocks.hint(item, p, today) uses p.streak only if Quiz.unbroken(p, today) (last quiz today, the eve, or before quote-only days within 7), else 0; p.streak itself still restarts only at the next quiz. Collection: labels via style Label (no .uppercase()), hint text 12 sp ink2 without alpha (was 2.6:1), worn cells isSelected for TalkBack. GloseTest.unlocksAreKeptAndAnnouncedOnce now sets lastQuizDay = today for its restarted streak and checks the broken-streak hint. <!-- id:4008 -->
