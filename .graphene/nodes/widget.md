---
type: subsystem
summary: Free home-screen widget of the word of the day (AppWidgetProvider + RemoteViews, no Glance): word/quote in its palette, Glose head, tap opens the item
entry_points:
  - app/src/main/java/fr/definiday/Widget.kt
  - app/src/main/res/layout/widget.xml
  - app/src/main/res/xml/widget_info.xml
covers:
  - app/src/main/java/fr/definiday/Widget.kt
  - app/src/main/res/layout/widget.xml
  - app/src/main/res/xml/widget_info.xml
  - app/src/main/res/drawable/widget_bg.xml
  - app/src/main/res/drawable/widget_fade.xml
  - app/src/main/res/drawable-nodpi/widget_preview.webp
  - app/src/test/java/fr/definiday/WidgetTest.kt
last_commit: a67bd73
edges:
  - to: lockscreen-display type: depends_on reason: refreshed by DailyReceiver.show and its daily alarm
---

- a67bd73: Widget.update(ctx) called from DailyReceiver.show() end (daily alarm, random pick, test button), StylesActivity pool changes, CollectionActivity « Choisir ». Shows today's first item (not the random pick: lock screen only). Gradient without bitmap: white rounded shape tinted top colour under transparent-to-white shape tinted bottom colour (setColorFilter at runtime, android:tint for preview); only bitmap = Glose head ~120 px. System serif (RemoteViews can't use custom fonts). Word sized with TextPaint.measureText (18-28 sp; hides Glose and wraps at 16 sp when too long); definition maxLines from height + ellipsize. 4x2 default, down to 2x2; per-size layouts on API 31+ (getParcelableArrayList typed only API 33: untyped on 31-32); 1-row strip unsupported. updatePeriodMillis 0: DailyReceiver.scheduleNext keeps the daily alarm while any widget exists even with daily word off (onEnabled/onDisabled reschedule). Pin via requestPinAppWidget from Settings « Ajouter le widget » (toast how-to fallback). android:theme='@style/Glose' on the ImageView renders the themed vector in previewLayout. AppWidgetManager.getInstance may be null. Tests: call am.updateAppWidgetOptions before onAppWidgetOptionsChanged; ShadowAppWidgetManager.createWidget/getViewFor work. +27 KB (18.7 KB preview webp). Device checks: picker preview, corners on launchers, hyphen breaks, 2x2 real sizes. <!-- id:4bc6 -->
- e4179a2: widget nature and author lines no longer italic (roman like the home card). Help style has lineSpacingMultiplier 1.15. <!-- id:dbd2 -->
