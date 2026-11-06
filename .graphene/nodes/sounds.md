---
type: subsystem
summary: Optional UI sounds (off by default): Sounds object over SoundPool, 7 synthesized OGG in res/raw, toggle « Sons » in Settings > Apparence
entry_points:
  - app/src/main/java/fr/definiday/Sounds.kt
  - tools/sounds/make_sounds.py
  - docs/son.md
covers:
  - app/src/main/java/fr/definiday/Sounds.kt
  - app/src/main/res/raw/
  - tools/sounds/
  - docs/son.md
  - app/src/test/java/fr/definiday/SoundsTest.kt
last_commit: 6daa00a
---

- 0.10 (6daa00a): Sounds.play(ctx, res) silent unless Prefs.sounds (default false) and AudioManager.ringerMode == NORMAL; SoundPool lazy, released after 30 s idle; AudioAttributes USAGE_ASSISTANCE_SONIFICATION (follows system-sounds/ringer volume, not media; USAGE_GAME would follow media). Files son_tic (right), son_hmm (wrong), son_carillon (quiz passed), son_tampon (unlock), son_page (random word), son_mrr (Glose tap), son_ronron (4-tap special): mono Vorbis 44.1 kHz, ~-20 dBFS loudest 50 ms, 49 KB total, synthesized from scratch (numpy + ffmpeg/libvorbis, fixed seed) so they belong to the project. Phone speakers drop < ~300 Hz: rely on harmonics; script prints spectral centroid (keep < ~3 kHz). Vorbis fixed overhead ~3-4 KB per file; raw OGG not recompressed in APK (+52 KB). Known: finishQuiz() replays its sound after a rotation (counting guarded, sound not). Tests: SoundsTest checks Sounds.pool == null when silent, ringerMode settable in Robolectric. <!-- id:80d8 -->
