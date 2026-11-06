# Richer backgrounds for Definiday: lock-screen card and in-app background (research as of Nov 2026)

Scope: how to make the lock-screen card (today a 2-stop vertical gradient `fond_haut -> fond_bas`, text bottom-anchored at 84 % of height, block capped at 40 % of height, see `app/src/main/java/fr/definiday/WordCard.kt`) and the in-app background (flat cream `@color/bg`, see `res/values/themes.xml`) more fun, cute and personal, without losing legibility. Stack: Kotlin, platform APIs only (no AndroidX), minSdk 26, static bitmap set with `WallpaperManager.setBitmap(..., FLAG_LOCK)` (`DailyReceiver.kt:120`).

Labels: **[fact]** = stated by the cited source; **[inference]** = my reasoning from sources plus the codebase; **[code]** = observed in the Definiday repo. Effort estimates are for one developer who knows this codebase: S = up to 1 day, M = 2 to 4 days, L = 1 to 2 weeks.

Codebase facts that shape every option **[code]**:
- 18 palettes in `assets/styles.json`, 9 dark (`encre_de_chine`, `bleu_nuit`, `ardoise`, `vert_de_gris`, `lie_de_vin`, `braise`, `sous_bois`, `violine`, `abysses`) and 9 light (`parchemin`, `sepia`, `sanguine`, `safran`, `lavande`, `givre`, `aube`, `pistache`, `craie`). Each one has a `sombre` flag and an `evoque` keyword list (for example `bleu_nuit`: "nuit, rêve, étoiles, mélancolie, sommeil"), so a decorative motif can be chosen per palette without new data.
- Each word already suggests a palette and a font (`Styles.of`), so a palette-keyed motif automatically "matches the word".
- The mascot already exists as platform VectorDrawables (`res/drawable/glose_*.xml`, `glose_*_vd.xml`; the `_vd` files are about 170 KB of XML each, for example `glose_soir_vd.xml` is 171 KB). A `Drawable` can be drawn into the existing `Canvas` with `setBounds()` + `draw(c)`, so drawing Monsieur Glose on the card needs no new pipeline.
- The text sizes on the card: word 0.12 x width (0.09 if longer than 12 letters), definition 0.05 x width, nature/example 0.042 x width. On a 1080 px wide screen that is 130 px, 54 px and 45 px.
- Screenshot `lockscreen_0.png` (1080x2340): the top 55 % of the card is empty gradient; that empty area is where the system clock sits.

---

## Q1. What the best lock-screen, wallpaper and quote apps do for backgrounds, and how to offer "my photo"

### Takeaway
The closest analogue (Bible Lock Screen) and the big quote apps all let the user pick their own photo, then protect the text with blur, shadow and colour controls. Muzei blurs and dims its artwork. Pixel and iOS push personalisation through user-built compositions (emoji patterns, depth effect). On our stack a photo background works without any storage permission: `MediaStore.ACTION_PICK_IMAGES` on API 33+ (and on API 30+ devices with SDK extension 2), and `ACTION_OPEN_DOCUMENT`/`ACTION_GET_CONTENT` below that. The photo also fixes Definiday's biggest perception risk, the lost original lock wallpaper.

### Cited Findings
- **[fact]** Bible Lock Screen (F-Droid, Apache-2.0) lets the user pick any gallery image as the base. It writes the daily verse on it and sets the result as the lock wallpaper each morning. It offers an adjustable background blur "to make the text stand out", text colour presets plus a full colour wheel, adjustable text transparency, text shadows, a choice of font styles (Modern, Book, Cursive and others), bold, and a full-screen editor to drag the text's size, width and vertical position. The listing does not mention dimming. — [F-Droid](https://f-droid.org/en/packages/com.daklok.biblelockscreen/); source code: [GitHub DaKlok/BibleLockScreen](https://github.com/DaKlok/BibleLockScreen)
- **[fact]** Bible Lock Screen v2.3 asks for broad permissions: read shared storage and image files, modify or delete shared storage, foreground service, run at startup, ignore battery optimisations, exact alarms, set wallpaper, wake lock. — [F-Droid](https://f-droid.org/en/packages/com.daklok.biblelockscreen/)
- **[fact]** Muzei is a live wallpaper that "gently refreshes your home screen each day with famous works of art". It "recedes into the background, blurring and dimming artwork to keep your icons and widgets in the spotlight". Licence Apache-2.0. — [GitHub muzei/muzei](https://github.com/muzei/muzei)
- **[fact]** Monkey Taps' "Motivation - Daily quotes" puts "background photo customization" in its premium tier, next to all themes and categories. Its sister app "I am - Daily Affirmations" advertises more than 1,000 themes, 75+ categories and customisable widgets. I could not confirm whether "I am" accepts a user photo. — [mwm.ai, Motivation](https://mwm.ai/>/apps/motivation-daily-quotes/876080126), [mwm.ai, I am](https://mwm.ai/apps/i-am-daily-affirmations/874656917), [App Store, Motivation](https://apps.apple.com/ie/app/id876080126?l=en-gb)
- **[fact]** "Glow" (Daily Affirmations) says users "can upload their own background photos or browse our curated gallery". A review of the separate "Affirm" app asks for exactly that feature, which suggests user photos are a requested feature in the category. — [App Store, Glow](https://apps.apple.com/us/app/-/id6753347459) (secondary, via search summary)
- **[fact]** Pixel Emoji Workshop (June 2023 feature drop, Android 13+) builds a wallpaper from up to 14 emojis, a pattern and a colour, with a "Randomize" button. Cinematic wallpapers use AI to split subject from background and add a depth effect that reacts to movement (Pixel 6+). PopSci notes that animated backdrops use more battery. — [PopSci](https://www.popsci.com/diy/cinematic-emoji-wallpaper-pixel/), [PhoneArena](https://www.phonearena.com/news/how-to-use-emoji-wallpaper-maker-and-cinematic-wallpaper_id148234), [Android Authority](https://www.androidauthority.com/generative-cinematic-emoji-wallpaper-coming-to-pixel-phones-3323233/)
- **[fact, secondary sources]** iOS 16+ "depth effect" places the lock clock between the photo's subject and its background. In iOS 26 the clock stretches taller when the top of the photo is empty, and shrinks back when widgets sit under it "to maintain spacing and legibility". Glass and Solid clock styles trade looks against readability, and tinting helps when the wallpaper tones are close to the clock colour. — [MacRumors iOS 26 lock screen](https://www.macrumors.com/2025/09/16/ios-26-whats-new-iphone-lock-screen/), [allthings.how](https://allthings.how/customize-the-ios-26-lock-screen-liquid-glass-spatial-scenes-and-widgets/), [MacRumors resize clock](https://www.macrumors.com/how-to/ios-resize-iphone-lock-screen-clock/)
- **[fact]** Pixel's At a Glance text, "laid directly over your wallpaper, could become unreadable against light, patterned, or busy backgrounds". Google rolled out a legibility fix, visible mainly on Android 16 QPR3 Beta. — [Android Central](https://www.androidcentral.com/phones/google-pixel/pixels-at-a-glance-just-got-easier-to-read-and-i-can-finally-use-my-favorite-wallpaper)
- **[fact]** `MediaStore.ACTION_PICK_IMAGES` was "Added in API level 33, also in R Extensions 2". The "caller gets read access to user picked items even without storage permissions". The returned "picker" URIs "can only be opened for read". The docs prefer `openFileDescriptor(...)` for opening them. — [MediaStore reference](https://developer.android.com/reference/android/provider/MediaStore#ACTION_PICK_IMAGES)
- **[fact]** The photo picker is on Android 11+ devices that get modular system updates. Android 4.4 to 10 devices (and Go devices on 11/12) can get a backport through Google Play services if the app's manifest declares a `com.google.android.gms.metadata.ModuleDependencies` service with `photopicker_activity:0:required`. Without the picker, the AndroidX contract falls back to `ACTION_OPEN_DOCUMENT` (API 19+). By default access lasts "until the device is restarted or until your app stops". `takePersistableUriPermission()` makes it longer. — [Photo picker guide](https://developer.android.com/training/data-storage/shared/photopicker)
- **[fact]** Since Android 13/14, `WallpaperManager.getDrawable(which)`, `peekDrawable(which)`, `getFastDrawable(which)` and `getWallpaperFile` need `MANAGE_EXTERNAL_STORAGE` or the system permission `READ_WALLPAPER_INTERNAL`. "Only apps with MANAGE_EXTERNAL_STORAGE should use this method. Otherwise, a SecurityException will be thrown." `getWallpaperColors(which)` returns only the primary colours. — [WallpaperManager](https://developer.android.com/reference/android/app/WallpaperManager)
- **[fact]** The RenderScript blur is deprecated from Android 12. Its replacement `RenderEffect.createBlurEffect` (API 31) applies to Views and RenderNodes. Blurring a plain Bitmap with it means rendering through `HardwareRenderer`. The guide points to the "RenderScript Intrinsics Replacement Toolkit" (`Toolkit.blur`) for bitmap blur. — [Migrate from RenderScript](https://developer.android.com/guide/topics/renderscript/migrate)

### Inferences
- **[inference]** "Ma photo en fond de carte" is the option with the most value. It solves the structural "my photo is gone" problem from the benchmark: the user re-picks the photo they had, and Definiday composes on top of it. It is also the most personal background possible. Bible Lock Screen proves the pattern on F-Droid.
- **[inference] Picker without AndroidX:** on API 33+, or when `SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2` (`SdkExtensions` is API 30), launch `Intent(MediaStore.ACTION_PICK_IMAGES)`. Otherwise launch `Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*").addCategory(CATEGORY_OPENABLE)`. That is about 10 lines with `startActivityForResult`, which is deprecated but still works in platform `Activity`. Do not rely on persisted URI grants: picker grants may not survive and the user may delete the photo. **Copy the picked image once into `filesDir`**, downscaled to the screen size (`BitmapFactory.Options.inSampleSize`, then a center-crop to width x height). Then the daily alarm never touches a content URI. This also avoids the storage permissions Bible Lock Screen asks for. Skip the Play services backport manifest entry: `ACTION_OPEN_DOCUMENT` is enough on API 26 to 29.
- **[inference] Blur on API 26 without RenderScript:** downscale the photo (for example to 1/8 with `Bitmap.createScaledBitmap(..., filter = true)`), then upscale it back with filtering. Two passes give a cheap, soft blur. A small box or stack blur over an `IntArray` from `getPixels` is about 30 lines of Kotlin if better quality is needed. The work runs once per photo choice, so it is not a hot path.
- **[inference]** Copy Bible Lock Screen's controls, but fewer of them: blur (off / soft / strong), dim (auto), text in palette colours or "auto light/dark". Our gradient presets already cover the colour wheel's job. The drag editor is not needed because the layout is already adaptive (`textBottom`, shrink loop).
- **[inference]** Pixel's emoji pattern is the model for a cute, procedural, user-built background with no photo. A "motif" chosen from a few small vector glyphs (cat paws, quills, little books, stars, leaves) tiled at low alpha over the palette gradient is the Definiday version (see Q2).
- **[inference]** Sampling the user's existing wallpaper (to blur it, as some apps did before Android 13) is not possible without `MANAGE_EXTERNAL_STORAGE`. That permission is a Play-policy and trust problem, so the user has to re-pick the photo.

### Gaps
- I did not read Bible Lock Screen's source to see how it blurs (library or hand-rolled) or which blur radius it uses.
- I found no primary listing for Walli or Samsung Good Lock (LockStar) backgrounds within the call budget. Their practices are not documented here.
- I could not verify whether picker URIs accept `takePersistableUriPermission` on every API level. The copy-to-`filesDir` approach avoids the question.

---

## Q2. Illustrated and procedural backgrounds that stay small; time, season and calendar variants; Monsieur Glose on the card

### Takeaway
Everything here is plain Canvas work on top of the existing gradient, so the risk is low and each layer can be shipped on its own. Procedural texture (grain, vignette, palette-keyed motif) is cheap and adds charm at zero asset cost. Illustrated scenes cost mostly art time. Monsieur Glose fits best as a small "signature" next to the word, not in a corner: the bottom corners belong to the system lock shortcuts and the top half to the clock.

### Cited Findings
- **[fact]** Pixel's Emoji Workshop shows that a few repeated glyphs + pattern + colour + "Randomize" is enough for a playful, personal wallpaper. — [PopSci](https://www.popsci.com/diy/cinematic-emoji-wallpaper-pixel/), [Tom's Guide](https://tomsguide.com/how-to/how-to-make-and-set-emoji-wallpapers-on-a-google-pixel-phone)
- **[fact]** iOS deliberately shrinks or re-layers the clock so decoration never hides it "to maintain spacing and legibility". — [allthings.how](https://allthings.how/customize-the-ios-26-lock-screen-liquid-glass-spatial-scenes-and-widgets/)
- **[fact]** Pixel notes that busy or patterned wallpapers make overlaid system text unreadable. — [Android Central](https://www.androidcentral.com/phones/google-pixel/pixels-at-a-glance-just-got-easier-to-read-and-i-can-finally-use-my-favorite-wallpaper)
- **[code]** `WordCard.render` comment: "the system clock grows to fill the upper half when no notification is shown". The text block ends at 84 % of the height "above the shortcut row".

### Inferences (options with effort)
- **[inference] A. Paper grain + vignette (S, about 0.5 day).** Generate a 256x256 noise tile once (random grey with alpha 4 to 8 %), draw it with `BitmapShader(TileMode.REPEAT)` over the gradient, then add a `RadialGradient` vignette (edges 10 to 15 % darker). It is warm, "bookish" and adds no asset weight. It cannot hurt legibility at that alpha. Keep the same noise seed per day so the look stays stable.
- **[inference] B. Palette-keyed motif layer (S to M, 1 to 3 days including drawing 6 to 10 tiny glyphs).** Map `evoque` to a motif: `bleu_nuit`/`abysses` get a sparse constellation (dots + 1 px lines, generated from the word's hash, so each word has "its" constellation, which is a nice personal touch); `sous_bois`/`pistache`/`vert_de_gris` get scattered leaves; `parchemin`/`sepia` get ink blots and quills; `givre` gets snowflakes; `braise` gets embers; `lavande` gets small flowers. Glyphs are small paths in Kotlin (`Path` + `Paint`) or tiny VectorDrawables. Draw them only outside the text block's rectangle, at 6 to 12 % alpha, with some jitter. Seed from `item.key` so the layout is stable all day and different each day.
- **[inference] C. Seasonal and French-calendar overlays (S each once B exists).** Swap or add motif glyphs by date: Chandeleur (2 Feb) crêpes, 1 April "poisson d'avril" (a fish on Glose's back), 1 May muguet, 21 June Fête de la musique notes, 14 July small fireworks, Toussaint leaves, December snow, Epiphany galette crown on Glose. A `when (MonthDay)` table, no network and no weather needed. Spring/summer/autumn/winter glyph sets cover the rest of the year.
- **[inference] D. Time of day (S, but it needs a second render).** The card is rendered once per day by the daily alarm. A "soir" variant (darker gradient, a few stars, Glose in his `glose_soir` pose) would need a second `setBitmap` at the evening quiz time. An alarm already exists for the quiz notification, so it can be reused. Ship it as an opt-in, because each `setBitmap` makes the system recompute wallpaper colours.
- **[inference] E. Illustrated scenes (M to L, mostly art).** For example: a window with rain, a bookshelf, a starry roof with Glose's silhouette, a garden. Draw them in the existing Paper.js pipeline and export as VectorDrawables. Budget about 100 to 200 KB of XML per scene, like the existing `_vd` mascot files. Place the scene in the band between the clock and the text (about 45 to 60 % of the height) or as a low-contrast silhouette at the top. A full-detail scene behind the clock would hurt clock legibility (see Q4). Four seasonal scenes is a reasonable first set. This option takes the most time and gives the most "cute"; it is better after A to C have shown which look users choose.
- **[inference] F. Monsieur Glose on the lock card (S, about 0.5 day).** Draw the existing `glose_icone`/`glose_assis` vector at about 12 to 16 % of the screen width. The best spot is "perched" on the top-right of the text block, next to the word's first line, like a signature or an ex-libris. Other good spots: peeking over the top edge of the block (only his ears and eyes), or curled up after the example line. Avoid the bottom-left and bottom-right corners, where Pixel and Samsung draw the lock shortcut buttons (camera, flashlight, wallet), and avoid the top half, where the clock is. To limit clutter: one Glose, one size, a pose matching the context (`glose_soir` in the evening variant, `glose_livres` for quotes), and an on/off switch in Réglages. Use his customisable name only in-app, never on the card (more text means more clutter). Keep him out of the shrink-to-fit math, or reserve his width in the first line, so long words never collide with him.
- **[inference]** Clutter guard for all layers: at most one "hero" decoration (Glose or the scene) plus one ambient layer (grain or motif). Do not draw decoration inside the text rectangle plus a margin.

### Gaps
- There is no user data on whether Definiday users want a mascot on the lock screen. A Collection unlock ("Glose signe votre carte") is one way to test it, but I have no evidence either way.
- I did not measure the render time of a 170 KB VectorDrawable into a 1080x2340 bitmap on a low-end device. I expect it to be well under a second, but that is unverified.

---

## Q3. Live wallpaper (WallpaperService) vs static bitmap

### Takeaway
Stay with the static bitmap. A live wallpaper cannot be set programmatically: the user has to confirm it in the system picker. Lock-only live wallpapers depend on Android 14+ and the OEM. On many devices it would take over the home screen too. It also adds battery and complexity for an image that changes once a day. Parallax brings nothing on a lock screen.

### Cited Findings
- **[fact]** `WallpaperService` (API 7) "is responsible for showing a live wallpaper behind applications". Each instance (real wallpaper, preview) is its own `Engine`. — [WallpaperService](https://developer.android.com/reference/android/service/wallpaper/WallpaperService), [Engine](https://developer.android.com/reference/android/service/wallpaper/WallpaperService.Engine)
- **[fact]** `Engine.onVisibilityChanged`: "It is very important that a wallpaper only use CPU while it is visible." — [Engine](https://developer.android.com/reference/android/service/wallpaper/WallpaperService.Engine)
- **[fact]** `Engine.getWallpaperFlags()` (API 34) "Returns the current wallpaper flags indicating which screen this Engine is rendering to" (`FLAG_SYSTEM`, `FLAG_LOCK`). `onWallpaperFlagsChanged(int)` reports changes. `onApplyWallpaper(int which)` gives the destination "home, lock, or both". — [Engine](https://developer.android.com/reference/android/service/wallpaper/WallpaperService.Engine)
- **[fact]** Third-party apps can only *propose* a live wallpaper: `ACTION_CHANGE_LIVE_WALLPAPER` "Directly launch[es] live wallpaper preview, allowing the user to immediately confirm to switch", with `EXTRA_LIVE_WALLPAPER_COMPONENT`. — [WallpaperManager](https://developer.android.com/reference/android/app/WallpaperManager)
- **[fact, preview-era reporting]** `setWallpaperComponentWithFlags()` (which allows home, lock or both) is reserved for system apps with `SET_WALLPAPER_COMPONENT`. Independent lock-screen live wallpapers were gated behind `config_independentLockscreenLiveWallpaper` in Android 14 previews. — [MobileSyrup](https://mobilesyrup.com/2023/03/24/android-14-could-bring-separate-live-wallpapers-for-home-and-lock-screens/), [XDA](https://www.xda-developers.com/android-14-independent-lock-screen-live-wallpaper/). An AOSP LivePicker change shows that `setWallpaperComponent` moves the static imagery to the lock screen and cannot keep a static lock image in some cases — [AOSP LivePicker commit](https://android.googlesource.com/platform/packages/wallpapers/LivePicker/+/777ac7d%5E%21/)
- **[fact]** Animated backdrops (Pixel Cinematic) use more battery. — [PopSci](https://www.popsci.com/diy/cinematic-emoji-wallpaper-pixel/)
- **[fact]** Muzei proves a daily-changing *live* wallpaper is viable, but it targets the home screen. — [GitHub muzei/muzei](https://github.com/muzei/muzei)

### Inferences
- **[inference]** On Android 14+ Pixel, the system picker may offer "Lock screen" as the destination. On older or OEM builds a live wallpaper usually covers both screens. That would *replace the user's home wallpaper*, a worse version of today's lock-wallpaper loss. This alone rules it out for Definiday.
- **[inference]** Gains would be small animations (a blinking Glose, falling snow, a parallax tilt). On the lock screen they show for a few seconds per unlock, and a frame loop costs battery and code (Surface lifecycle, preview engine, `onComputeColors`). Effort: L (1 to 2 weeks with testing across OEMs), for little visible gain. The current `DailyReceiver` + `setBitmap` flow stays the right core.
- **[inference]** Parallax: `onOffsetsChanged` is driven by home-screen page scrolling, so it does nothing on the lock screen. Gyroscope parallax would need sensors while visible, which is more battery. Skip it.
- **[inference]** If animation is ever wanted, put it **in the app** (for example an animated Glose on the home screen via `AnimatedVectorDrawable`, which is platform API 21+), not on the wallpaper.

### Gaps
- I found no official final Android 14/15/16 documentation saying how the system picker chooses home/lock/both for a third-party live wallpaper, or how OEMs (Samsung One UI, Xiaomi) handle it. Sources are from the preview period.
- Google Play policy pages specific to live wallpapers and battery were not fetched. I found no evidence of a special policy beyond general battery and background rules.

---

## Q4. Readability guarantees on images and decorated backgrounds

### Takeaway
Readability can be guaranteed by measuring it, not by hoping. Sample the background under the text block, compute the WCAG contrast of each text colour against the worst pixels, and raise a scrim (dim or gradient) until body text reaches 4.5:1 and the word reaches 3:1. Add a soft text shadow as a backstop on photos. Keep the top half (clock) calm, because the system clock is drawn over the wallpaper too.

### Cited Findings
- **[fact]** WCAG 1.4.3: text needs "a contrast ratio of at least 4.5:1", and large-scale text "at least 3:1". Large is "at least 18 point or 14 point bold" (about 24 px / 18.5 px CSS). The ratios are thresholds and must not be rounded ("4.499:1 would not meet the 4.5:1 threshold"). Failure F83 covers "using background images that do not provide sufficient contrast with foreground text". A wide halo around letters counts as background. — [W3C Understanding 1.4.3](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html)
- **[fact]** Techniques for text over images: a full overlay that makes the image recede, a scrim ("semi-transparent ... gradient layer", not only black), a solid or semi-transparent strip behind the text, copy space (text in the quiet area of the image, checked against the minimum ratio), and blur. Contrast must be checked against the real image area behind the text. — [Smashing Magazine, part 1](https://smashingmagazine.com/2023/08/designing-accessible-text-over-images-part1)
- **[fact, practitioner opinion]** Scrim starting points of 30 to 70 % black opacity (about 40 % suggested), fading over about half the image height. A gradient fails for text that runs across the middle of the image. — [uxdesign.cc](https://uxdesign.cc/techniques-to-display-text-over-background-images-b089de1cc8bc)
- **[fact]** Amazon's Alexa design docs recommend 4.5:1 for legible text and note that gradient overlays make a safe text area at the bottom of the screen. — [Amazon APL components](https://developer.amazon.com/en-US/alexa/alexa-haus/apl-components)
- **[fact]** `WallpaperColors` (API 27) exposes three representative colours. The hints `HINT_SUPPORTS_DARK_TEXT` ("dark text is preferred over the current wallpaper ... A launcher may set its text color to black") and `HINT_SUPPORTS_DARK_THEME` were added in API 31. `fromBitmap(Bitmap)` extracts colours from a bitmap. — [WallpaperColors](https://developer.android.com/reference/android/app/WallpaperColors)
- **[fact]** Muzei blurs and dims to keep foreground UI readable. Bible Lock Screen uses blur + text shadow + colour choice. — [Muzei](https://github.com/muzei/muzei), [F-Droid](https://f-droid.org/en/packages/com.daklok.biblelockscreen/)
- **[fact]** Busy or light wallpapers make Pixel's overlaid system text unreadable. — [Android Central](https://www.androidcentral.com/phones/google-pixel/pixels-at-a-glance-just-got-easier-to-read-and-i-can-finally-use-my-favorite-wallpaper)

### Inferences
- **[inference] Which thresholds apply to the card.** At about 2.6 px/dp (a 1080 px wide phone at about 420 dpi), the word (130 px, about 50 dp) is "large" and needs 3:1. Definition (54 px, about 21 dp) and nature/example (45 px, about 17 dp) are *not* large and need 4.5:1. The example and nature lines use the dimmer `exemple`/`nature` colours, so they will fail first on photos.
- **[inference] Adaptive scrim algorithm (S to M, 1 to 2 days, pure Kotlin).**
  1. Lay out the text first (already done in `render`) to get the block rectangle plus a margin.
  2. Downscale that region of the background (for example to 64 px wide) and read the pixels.
  3. For each text colour, compute the WCAG relative luminance contrast against the 5th-percentile-worst pixel, not the average (the WCAG "lowest contrast area" spirit, F83). `Color.luminance()` (API 24) gives relative luminance. Contrast = (L1 + 0.05) / (L2 + 0.05).
  4. If a ratio is below target, draw a vertical `LinearGradient` scrim behind the block (transparent at the block top minus 15 % of the height, then darker toward the bottom) in the palette's `fond_bas` colour (for dark text, the palette's light `fond_haut`). Raise its alpha step by step (for example +10 % per step, up to 85 %) and retest.
  5. If it still fails at the cap, switch to a solid rounded "card" behind the text (the in-app card style, already proven in `home_light.png`).
  6. Always add `TextPaint.setShadowLayer(radius = 0.004 x width, 0, 0.002 x width, 40 % black)` on photo backgrounds. It is a narrow shadow, so WCAG counts it as part of the letter, not as background. It is a backstop, not the guarantee.
  This loop runs once per render, so cost does not matter. Leave one `assert`-style unit check that a white photo + `encre_de_chine` text ends with at least 4.5:1.
- **[inference] Automatic text colour on photos.** Pick light or dark text from the measured background luminance (or `WallpaperColors.fromBitmap(bg).colorHints and HINT_SUPPORTS_DARK_TEXT` on API 31+). Then map it to a dark or light *palette* (`sombre` flag), so the text keeps Definiday's colour identity instead of plain white or black.
- **[inference] Clock zone.** The system draws its own clock and notifications over the top half. Android picks the clock colour from the wallpaper colours (the hint mechanism above). A busy illustration or a high-contrast photo area under the clock hurts the clock, which Definiday does not control. Rule: in the top 45 %, decoration stays low-contrast (alpha at most 12 % for motifs, or a 20 to 30 % dim on photos). Glose and scenes go in the 45 to 60 % band or next to the text.
- **[inference] Bottom zone.** Keep the bottom 10 to 12 % plain (shortcut buttons, "swipe to unlock" hint, fingerprint icon). The current 84 % text end already respects this.
- **[inference] Large fonts.** The shrink loop (`scale` down to 0.6 while the block exceeds 40 % of the height) has to stay. A scrim or solid card must use the *final* block height, so do the measurement after the loop. Long definitions + Glose: reserve his width only on the word line, so he never pushes definitions to a smaller scale.
- **[inference] Existing light-palette issue.** The benchmark notes a 1.02:1 contrast between a light palette card and the cream app background. The same measuring function can enforce a 3:1 card border (WCAG 1.4.11 non-text contrast) in-app (see Q5).

### Gaps
- I did not fetch the official Material Design "text protection / scrim" page. The 30 to 70 % values come from a practitioner article, not from Google.
- I found no AOSP-level documentation of how SystemUI picks the lock clock colour on Android 15/16. The link to `WallpaperColors` hints is my inference from the API docs.

---

## Q5. In-app background: textures, subtle illustrations, palette-driven themes

### Takeaway
The app background can reuse the card layers: grain texture, a very faint palette motif, and a tint taken from the day's word palette. This makes the app feel like "today's page" without touching the layouts. Effort is S per layer, because the drawing code can be shared with `WordCard`.

### Cited Findings
- **[code]** The theme sets `android:windowBackground` to the flat `@color/bg` (cream in light, dark in night). The home card uses a `GradientDrawable` from the word palette (`MainActivity.kt:116`). The mascot sits above the card on the flat background (`home_light.png`).
- **[fact]** Muzei's design principle, "recedes into the background ... to keep your icons and widgets in the spotlight", applies to any decorative background behind UI. — [GitHub muzei/muzei](https://github.com/muzei/muzei)
- **[fact]** WCAG contrast thresholds (4.5:1 text, 3:1 large text) apply to in-app text over textures as well. — [W3C Understanding 1.4.3](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html)

### Inferences (options with effort)
- **[inference] A. Paper grain window background (S, about 0.5 day).** Generate the same noise tile as the card, wrap it in a `BitmapDrawable` with `setTileModeXY(REPEAT, REPEAT)` layered over `@color/bg` in a `LayerDrawable`, and set it as the window background in `onCreate`. Use alpha of 3 to 5 %, so text contrast is unchanged (each grain shifts luminance by about 1 %).
- **[inference] B. Day tint (S).** Blend 6 to 10 % of the word palette's `fond_haut` into `@color/bg` for the home screen (and Collection detail). The whole screen "follows" today's word. Recheck that `@color/texte` on the tinted background stays at least 4.5:1, using the same contrast helper as Q4.
- **[inference] C. Faint corner illustration (S to M).** Reuse the palette motif glyphs from Q2-B at 4 to 6 % alpha in the bottom margin of the home screen, or a seasonal scene silhouette behind the mascot. Keep them out of button areas.
- **[inference] D. Seasonal mascot accessories (S each).** Swap the home mascot drawable by date (scarf in winter, crown at Epiphany, muguet on 1 May). It is cuter and cheaper than background art, and it is consistent with the calendar table from Q2-C.
- **[inference] E. Fix the light-palette card edge (S).** When the contrast between the card and the background is below 3:1 (the `parchemin`/`craie` cases), add a 1 dp stroke in the palette's `nature` colour, or a slight shadow. That fixes the 1.02:1 issue from the benchmark.
- **[inference]** Do not let users pick a photo for the *app* background. It costs legibility of dense UI (quiz, settings) and adds nothing the lock card does not already give.

### Gaps
- I have no user research on whether a moving tint (it changes every day) feels charming or unstable. A setting or a fixed tint for the quiz screen may be needed.

---

### Ranked shortlist for the report writer (inference, combining all questions)

| Rank | Option | Surface | Effort | Why |
|---|---|---|---|---|
| 1 | "My photo" background: platform picker (`ACTION_PICK_IMAGES` / `ACTION_OPEN_DOCUMENT`), copy to `filesDir`, cheap blur, adaptive scrim, auto light/dark palette | Lock card | M (2 to 4 days) | Most personal; repairs the lost-wallpaper problem; proven by Bible Lock Screen; no storage permission |
| 2 | Contrast measurement + adaptive scrim + narrow shadow (needed for rank 1, useful for every layer) | Lock card (+ app) | S to M (1 to 2 days) | Turns "readable" into a guarantee (WCAG 4.5:1 / 3:1) |
| 3 | Paper grain + vignette | Lock card + app | S (0.5 day) | Charm at zero risk and zero asset weight |
| 4 | Monsieur Glose "signature" perched on the text block, toggle in Réglages | Lock card | S (0.5 day) | Mascot is the brand's cute asset; vectors already exist |
| 5 | Palette-keyed motif (constellation from the word's hash, leaves, quills, snow) | Lock card + faint in app | S to M (1 to 3 days) | Varies every day; matches the word through the existing palette choice |
| 6 | French-calendar and season glyph/accessory table | Lock card + app mascot | S per event | Playful surprises, no network |
| 7 | Evening variant (second render at quiz time) | Lock card | S | Links the card to the evening quiz; opt-in |
| 8 | Illustrated seasonal scenes (Paper.js to VectorDrawable) | Lock card | M to L (mostly art) | Cutest result, but highest cost; do after 3 to 5 |
| — | Live wallpaper / parallax | Lock | L | Not recommended: user must confirm in the system picker, lock-only is not reliable, may take over the home screen, battery |
