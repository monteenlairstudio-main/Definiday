---
type: subsystem
summary: Settings screen (SettingsActivity, Activity without AndroidX) in 4 groups: Mot du jour, Écran de verrouillage, Quiz du soir, Apparence
entry_points:
  - app/src/main/java/fr/definiday/SettingsActivity.kt
  - app/src/main/res/layout/activity_settings.xml
covers:
  - app/src/main/java/fr/definiday/SettingsActivity.kt
  - app/src/main/res/layout/activity_settings.xml
  - app/src/main/res/layout/spinner_item.xml
  - app/src/test/java/fr/definiday/UiTest.kt
last_commit: 6f188e7
edges:
  - to: lockscreen-display type: depends_on reason: mode, duration, unlocks (UnlockService.sync), DailyReceiver.show/scheduleNext, Alarms.requestIntent
  - to: styles type: depends_on reason: palette/font pools
---

- Owner decision (post-audit): separate Settings Activity. Groups: Mot du jour (enabled, time, exact-alarm state + 'Autoriser les alarmes exactes' button shown only while Alarms.requestIntent != null, refreshed in onResume; words a day; Plus de citations; 'Afficher le mot maintenant' with a toast per mode), Écran de verrouillage (Affichage mode; wallpaper hint, Fin and Position hidden in notification mode; 'Durée d’affichage' with help text per mode so it is not misleading in notification mode), Quiz du soir (enabled, time), Apparence (palettes/polices dialogs with a title, 'X sur Y'), À propos (credits). Time buttons disabled when their switch is off. Every Spinner has a labelFor label and uses res/layout/spinner_item.xml (wraps, minHeight 48dp) instead of simple_spinner_dropdown_item (single line, ellipsized at large font). <!-- 6fc6deb --> <!-- id:feaa -->
- Side effects kept from the old main screen: unlocks spinner -> UnlockService.sync(ctx); words a day raised while today's display time has passed and the daily word is enabled -> DailyReceiver.show(ctx) (all today's items again, wallpaper/duration restart). Spinner onItemSelected also fires once at first layout with the current value: listeners are written to be idempotent. Tests in UiTest call the listener directly (spinner.onItemSelectedListener.onItemSelected) instead of relying on Robolectric layout. <!-- 6fc6deb --> <!-- id:e94c -->
- 0.8.0: exact-alarm warning is a warn-colored banner (exact_box) with the button inside, hidden once granted (exact_ok removed); strings name the system page « Alarmes et rappels ». Value pickers (times, palettes, fonts) use style Setting (parent android:Widget.Material.Spinner on a Button: dropdown arrow, no outline, paddingEnd 48dp) so they differ from action buttons. more_quotes_hint line under the toggle. channel_name is « Mot du jour et quiz » (same channel carries the quiz reminder). <!-- id:580d -->
- 442c8d1: duration spinner adds the current value when it is outside the fixed list (restored profile, e.g. 90 -> « 1 h 30 ») instead of silently resetting to 15 (UiTest.settingsKeepADurationOutsideTheList). <!-- id:b435 -->
- c1dbecf (fon-C): Réglages › Apparence has « Nom du chat » (EditText, Done, autofill off, maxLength 64 for pastes); each keystroke checked by Prefs.nameError() (trimmed, 1..24 code points, no control/format/U+2028-9/lone surrogate; emoji ok but ZWJ sequences refused); error via setError; saved on Done, focus loss and onPause with compare-and-set (a profile restored meanwhile wins); « Rétablir » resets to default. Owner assumptions to confirm: 24 max, emoji, full-name signature. <!-- id:ed7c -->
