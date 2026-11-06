---
category: audit
subject: ui-review-skills-2026-11-06
---

UI review with design skills (design-critique, accessibility-review, ux-copy) on 38 Robolectric captures (renderDemoImages -> app/build/demo/ui). Reports: docs/audits/revue_ui/{critique_design,accessibilite,textes}.md. Verified by me: citations.json has 245/450 quotes with straight apostrophe and 125 with plain space before ;:!? (0 U+202F) -> quotes never went through typo(). Key open items: Collection preview scrolls away; fit() no floor at font 2.0; no primary action on home; quiz GOOD/BAD hardcoded saturated; quiz TalkBack (announceForAccessibility deprecated API 36, removeAllViews drops focus); unchecked Switch 1.10:1 light; btn.xml no focused state; lock-only mode has no accessible text and wallpaper ignores fontScale; random word not announced; display fonts (Uncial) distort words on lock screen; exact alarm denied by default Android 14+ so warning needs prominence; notif_off wording wrong in BOTH mode, channel 'Mot du jour' also carries quiz reminder; hardcoded strings in Unlocks.kt / Settings spinners. Lock palettes all pass (min nature 4.61:1, word 6.15:1).
