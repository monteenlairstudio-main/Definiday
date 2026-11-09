# Audit UX + GRA, itération 1 (HEAD c843268, v0.14.0)

Équipe UX (UI/UX + graphisme), lecture seule. Méthodes : `design:design-critique`, `design:accessibility-review` (WCAG 2.1 AA adaptée à Android), `design:ux-copy` (typo française). Référence de style : `design/styles/correspondance/style.md`, `implementation.md`, `implementation_surfaces.md` + rigueur `design/styles/edition/style.md`. Revues antérieures revérifiées : `docs/audits/revue_ui/*.md`, `docs/audits/revue_ui_011.md`.

Conventions : **[observé]** capture ; **[code]** lu dans le source ; **[mesuré]** ratio calculé ; **[hypothèse]** non vérifié sur appareil.
Captures citées : `scratchpad/night/captures_1/` (copiées depuis `app/build/demo/`).


## 0. Matériel

- Captures régénérées sur HEAD : `flock … ./gradlew --offline :app:testDebugUnitTest --tests 'fr.definiday.DefinidayTest.renderDemoImages' --tests 'fr.definiday.WidgetTest.renderWidgets' --tests 'fr.definiday.GloseTest'` (BUILD SUCCESSFUL, 2 min 9 s) → `app/build/demo/{,ui,widget,glose}`, 90 PNG. Réduites à 540 px (découpées par 1 100 px) pour lecture ; copies citées dans `captures_1/`.
- Code relu : `themes.xml`, `colors.xml`, `dimens.xml`, `drawable/btn*.xml`, `favori.xml`, `activity_main.xml`, `widget.xml`, `MainActivity`, `DetailActivity`, `MotsActivity`, `QuizActivity`, `OnboardingActivity` (aperçu), `strings.xml` (scan typographique scripté + couverture des glyphes par fontTools).

## 1. Accueil (`home_light`, `home_dark`, `home_quote`, `home_deviner`, `home_w360*`, `home_font*`)

Première impression : l’écran est abouti et tient sa promesse « lettre de Glose » : en-tête Glose + date postale + titre, carte postale à liseré, timbre et cachet, carnet Seyès, sceau sur « Quiz du soir », enveloppes. Hiérarchie claire (mot > définition > actions > exemple > quiz). Corrections de la revue 011 **vérifiées** [observé] : n° 2 (puce coupée), 3 (rayons), 4 (barre du haut), 19 (quiz fait en kraft), 20 (plume pour la source), 21 (bandeau `note_paper` « Les réactiver › »).

- **A1 [observé + code] Le « ! » de la note de Glose sort dans une autre police.** `captures_1/zoom_note_point_exclamation.png` : « à glisser au dîner **!** » : le point d’exclamation est droit et gras (police de repli), pas en Dancing Script. Cause : **aucune des trois polices d’interface ne contient U+202F** (fontTools : `dancing_script.ttf`, `libre_baskerville.ttf`, `courier_prime.ttf` → U+202F absent ; U+00A0 présent). Le moteur de texte prend une police système pour l’espace fine et garde ce repli pour la ponctuation collée derrière. Sur Libre Baskerville le repli (serif système) passe presque inaperçu, sur l’écriture manuscrite il saute aux yeux. Concerne toute chaîne `Note`/`Postal` avec « ; : ! ? » ou « » ». [hypothèse : même comportement sur appareil ; Robolectric utilise le moteur de texte natif].
- **A2 [observé] « 1 MOT » sur le timbre d’une citation** (`captures_1/ui__home_quote.png`) : légende fausse pour une citation. Plus simple : pas de légende pour une citation (`caption = null`).
- **A3 [observé] Police ×2,0 à 360 dp** (`captures_1/ui__home_w360_font200_long_part0.png`) : l’en-tête se tasse à droite de Glose (128 dp) : date sur 2 lignes, titre sur 4 (« Une / lettre de / Monsieur / Glose »), ~470 dp avant la carte. Lisible mais lourd ; `style.md` § 15 prévoit déjà de réduire Glose (112 dp, voire 96 dp au-delà de ×1,6).
- **A4 [observé] Signature de citation « Adolphe Thiers, De la propriété »** : le titre d’œuvre n’est pas distingué (italique banni). Goût → propositions.
- **A5 [observé] Voile « Touchez pour révéler »** (`ui__home_deviner.png`) : seul pointillé de l’écran, conforme à la règle. OK.
- Sombre : conforme ; enveloppe choisie = contour plume 2 dp + point cire ; « Quiz du soir (fait ✓) » en kraft. OK.
- Décor : timbre + cachet (fort), sceau, fil, Seyès, note : six motifs mais dans les quotas du § 7 de `style.md` (un de chaque). Le seul « fort » est le timbre + cachet. OK.

## 2. Mes mots et Détail (`mots_light`, `mots_dark`, `mots_w360_font200`, `mots_detail`)

Mes mots est cohérent avec le kit : `Title`, champ en feuille, registre (`card_soft` à filets) au lieu des six tuiles, lignes réglées, étoiles nommées avec le mot (011 n° 11 en partie et n° 12 **corrigés**). À ×2,0 le registre passe en une colonne, la liste arrive au 2e écran : acceptable.

- **D1 [observé + code] L’écran Détail est resté hors du style Correspondance** (`captures_1/ui__mots_detail.png`, `DetailActivity.kt:39-58`). C’est l’écran le plus récent et le plus en écart :
  - carte en `Styles.card(…, 20f)` : **rayon 20 dp hors système** (6/12/16 seulement), pas de liseré `sheet` (`Postal.postcard`), ni timbre ni cachet ;
  - le mot en style `Title` 20 sp (39 sp sur l’accueil) : l’objet de l’écran est plus petit que bien des titres ;
  - **nature en italique synthétique** (`setTypeface(typeface, Typeface.ITALIC)`, ligne 48) : interdit par la règle 2 du kit ;
  - exemple dans la couleur `texte` de la palette, alors que partout ailleurs une citation est en plume (`Example`) dans le carnet Seyès ;
  - anecdote nue sur le fond (accueil : feuille `card_soft`) ;
  - actions en trois gros boutons kraft pleine largeur (accueil : rangée `Action` icône + libellé sur la carte). Deux grammaires pour les mêmes actions Académie / TLFi / Envoyer.
- **D2 [observé] « 5 mots à revoir »** reste une ligne de registre non actionnable (011 n° 11, moitié restante) : pas de filtre « À revoir ». Fonction plus qu’UX → FON.

## 3. Collection (`collection_light`, `collection_dark`, `collection_w360_font200`)

Album réussi : timbres dentelés 80 × 96, tampon « PORTÉ » sur la tenue gardée, cases verrouillées unies (plus de « ? » pointillé : 011 n° 15 **corrigé**), mentions postales « 18 TIMBRES SUR 39 », « 3 SUR 4 ».

- **C1 [observé + code] Bandes « cristal » au-dessus des dessins.** `CollectionActivity.kt:171-181` : film blanc à 35 % (jour) / 10 % (nuit) **en premier plan** sur le bas du timbre. Il voile les pattes de Glose et le livre, c’est-à-dire ce que l’on collectionne (`captures_1/ui__collection_light_part0.png`) ; de nuit il grise le dessin (`ui__collection_dark_part0.png`). Il traverse aussi les **cases verrouillées vides**, qui paraissent coupées en deux tons (« Réveil dans la tasse », « Smoking ») : lu comme un défaut d’alignement.
- **C2 [observé] En-tête** : titre au-dessus, Glose dessous à gauche, mention à droite alignée en haut, grand vide à droite de Glose. Composition différente de l’accueil. Goût → propositions.

## 4. Quiz (`quiz_question`, `quiz_faux`, `quiz_trous_faux`, `quiz_dark_faux`, `quiz_fin*`, `quiz_w360_font200_faux`)

La lettre fonctionne : « QUESTION 1 SUR 4 » postal, petits timbres de progression, « Bonsoir, » manuscrit, carnet Seyès, réponses kraft, « Suivant » en `Primary`, fin avec « À revoir : … » et texte de liaison (011 n° 10 **corrigé**), « Plus tard » lisible (n° 13 **corrigé**).

- **Q1 [observé + code] ✓ / ✗ : glyphes absents des polices, mot décalé.** U+2713 et U+2717 n’existent dans aucune police de `res/font` (fontTools) : ils sortent en police système (✗ penché, ✓ gras). Et `QuizActivity.kt:155` compense l’indentation avec `measureText("✓ ")` **pour les deux signes** : le mot après « ✗ » glisse de ~3 px à gauche (`captures_1/ui__quiz_trous_faux.png` : « manant » vs « gredin »). `drawable/ic_check.xml` existe déjà : l’icône en `compoundDrawable` à la place de la case garde le mot immobile et la police unique.
- **Q2 [code] TalkBack sur la réponse** : le bouton devient « ✓ estramaçon, désactivé » / « ✗ manant, désactivé » ; le signe est lu au mieux « coche » / « multiplication » selon la synthèse, jamais « bonne réponse » / « votre réponse ». La région vivante (« Pas tout à fait : c’était… ») porte l’information, donc pas bloquant ; `contentDescription` = « estramaçon, bonne réponse » / « manant, votre réponse » rend chaque bouton juste.
- **Q3 [observé + mesuré] Options désactivées** : la case reste plume vif sur une option grisée (elle a l’air encore active) ; de nuit `surface_off` #1F2233 sur `sheet` #1B2136 = **1,01:1** : les options non choisies perdent leur forme (`captures_1/ui__quiz_dark_faux.png`). Exempté par WCAG, mais on ne voit plus qu’il y avait quatre choix. Un filet `card_edge` dans l’état désactivé de `btn.xml` suffit.
- **Q4 [observé + code] Carte de notation vers le Play Store** (`captures_1/ui__quiz_fin_noter.png`, `QuizActivity.kt:254-269`) : l’appli est distribuée par les versions GitHub (`Update.kt`, `docs/mise_a_jour.md` : pas encore de version Play). « Noter » ouvre une fiche `fr.definiday` inexistante : impasse. N’afficher la carte que si l’installateur est le Play Store (`getInstallSourceInfo` API 30+, `getInstallerPackageName` avant).
- Fin de quiz : timbres gagnés sans tampon (§ 8 de `style.md` : « PORTÉ » ou cachet du jour) → propositions.

## 5. Réglages, Couleurs et polices, onboarding

Réglages : rubriques + feuilles, « Ailleurs sur votre téléphone » (011 n° 5 **corrigé**), « Couleurs et polices › » en ligne de navigation (n° 14 **corrigé**), « Apparence et sons » (n° 26, moitié **corrigée**). Couleurs et polices : sélection = contour plume 2 dp + ✓ (`ic_check`), plus de « sa définition » (n° 23 en partie **corrigé**). Onboarding : « Deux ou trois réglages » (n° 27 **corrigé**), notifications d’abord, alarmes en 4b sur `note_paper` (n° 8, 9 **corrigés**).

- **R1 [observé] Deux modèles de libellé de valeur** (rappel 011 n° 26) : « Heure d’affichage : 08:00 » dans le menu, contre « Nombre de mots par jour » au-dessus de « 1 mot » (`captures_1/ui__settings_light_part0.png`). Idem « Heure du quiz : 21:00 ».
- **R2 [observé + code] Aide hors contexte** : « Pour compter les déverrouillages, une petite notification reste affichée… » s’affiche alors que « Après la durée choisie » est sélectionné (`captures_1/ui__settings_light_part1.png` ; `SettingsActivity.kt:66` ne la masque qu’en mode notification seule).
- **R3 [code] « au déverrouillage n° %1$d »** (`watch_text`, rappel 011) : « au 1er / 2e / 3e déverrouillage ».
- **R4 [observé] Aide en `Help` 14 sp sans interligne** : blocs de 4-5 lignes serrés (Sons, Mon profil, Mises à jour) quand `Body` a 1,2. `lineSpacingMultiplier 1.15` dans `Help`.
- **R5 [observé + code] Aperçu de l’onboarding à angles vifs** (`captures_1/ui__onboarding_2_light_part0.png`) alors que `OnboardingActivity.kt:183` pose `Styles.card(…, 16f)` + `clipToOutline` : le bitmap couvre le fond sans être découpé. **[hypothèse] artefact de capture** : `clipToOutline` n’agit qu’en rendu matériel, la capture logicielle de Robolectric l’ignore. À vérifier sur appareil avant d’y toucher ; pas d’item au backlog.
- **R6 [observé] « Aucune »** dans Couleurs et polices (rappel 011 n° 23) : toujours refusé ensuite (« Il en faut au moins une »).

## 6. Surfaces dessinées : écran verrouillé, partage, widget

Écran verrouillé et partage : très bons (`captures_1/lockscreen_0.png`, `share_1.png`) : mention, timbre + cachet, fil sous le mot, signature « Glose » sans dessin (décision respectée), contraste testé par `lockCardKeepsContrastOnEveryPalette`.

- **W1 [observé] Widget : italique et minuscules** (`captures_1/widget__4x2.png`, `widget__4x2_quote.png`) : nature « n. f. » en italique, « Citation du jour » en italique minuscule (rappel 011 n° 24), signature en italique. Le reste de l’appli n’a plus d’italique (la serif système de RemoteViews a une vraie italique : écart de cohérence, pas défaut technique).
- **W2 [observé] Citation coupée en pleine phrase** dans le widget 4 × 2 (« Elle c… ») : rappel 011 n° 24.
- **W3 [observé] « zinzolin » (Parisienne)** : la boucle du z sort de la marge du texte et le fil traverse les jambages (`captures_1/style_zinzolin.png`). Décor, faible.
- **W4 [observé] Uncial « lendit »** se lit « lexdic » (`captures_1/share_1.png`, rappel 011 n° 16) : goût → propositions.

## 7. Graphisme (GRA)

- **Icône** : tête de Glose sur marine `#1B2440` (jeton `icon_bg`, d’avant Correspondance ; le build de test est en cire `#7A2924`, `src/dev/res/values/dev.xml`). Lisible et attachante, mais sans lien avec le papier, la plume ou le timbre. Trois variantes rendues à partir du vrai dessin : `captures_1/gra_icone_avant_apres.png` (A fond plume, B timbre feuillet dentelé sur plume, C timbre kraft sur papier ; vue 432 px et taille lanceur). Goût → propositions.
- **Fond de l’appli** : papier uni `#F8F3E9`. `style.md` § 7 réservait un grain (bitmap 128 px à 4 %, ~6 Ko) « si les surfaces paraissent trop plates sur appareil ». Avant/après : `captures_1/gra_grain_avant_apres.png` (grain seulement sur le fond, jamais sous le texte des papiers). Goût → propositions.
- **Finitions Postal** : timbre, cachet, sceau, enveloppes, Seyès nets sur toutes les captures, jour et nuit. Défauts relevés : bandes cristal (C1), ✓/✗ (Q1), rayons hors système `DetailActivity` 20 dp et anneau de focus de `btn_primary.xml:12` à **8 dp** (un 4e rayon), légende « 1 MOT » sur la citation (A2).

## 8. Accessibilité (WCAG 2.1 AA, adaptée à Android)

Résumé : pas de bloquant ; 1 manque AA (focus de l’étoile), 2 majeurs, le reste mineur.

| # | Constat | Critère | Gravité | Correctif |
|---|---|---|---|---|
| 1 | Étoile « favori » sans indicateur de focus : `background="@null"` (`activity_main.xml:189`, `DetailActivity.kt:44`, `MotsActivity.kt:155`) ; au clavier ou au contacteur on ne voit pas où l’on est | 2.4.7 | Majeur | fond `btn_round` (focus 3 dp) ou `Postal.ring(ctx, palette.mot)` sur la carte |
| 2 | Réponses du quiz lues « ✓ / ✗ » (Q2) | 1.3.1, 4.1.2 | Mineur (région vivante) | `contentDescription` explicite |
| 3 | Options désactivées invisibles de nuit, 1,01:1 (Q3) | 1.4.11 (exempté) | Mineur | filet `card_edge` |
| 4 | Interrupteur éteint de nuit sur feuille : piste #6E6A62 sur `sheet` #1B2136 = 2,96:1 [mesuré] | 1.4.11 | Mineur (le pouce #BEB4A3 porte l’état) | piste éteinte de nuit un cran plus claire (≥ 3:1) |
| 5 | En-tête à ×2,0 : ~470 dp avant le contenu (A3) | 1.4.4 (confort) | Mineur | Glose réduit au-delà de ×1,6 |
| 6 | Repli de police sur U+202F (A1) | rendu | Majeur visuel | voir backlog |

Vérifiés conformes [observé/code] : cibles 48 dp (disques, actions 56 dp, étoile 48, enveloppes, liens), titres d’écran (`android:label` par Activity, `title = w.mot` dans Détail ; 011 n° 28 **corrigé**), rubriques `accessibilityHeading`, objets dessinés décoratifs (`noHideDescendants` sur timbre + cachet, progression du quiz), réponses annoncées en région vivante, mouvement coupé si animations désactivées, contrastes du texte ≥ 4,5:1 (recalcul `wcag.py` : plume sur `warn` de nuit 6,82:1 ; `ink2` sur `surface_off` de nuit 7,68:1).

## 9. Textes (ux-copy, typographie)

Scan scripté de `strings.xml` et des littéraux Kotlin : **aucune apostrophe droite, aucune espace ordinaire avant « ; : ! ? »**, 41 chaînes en U+202F. Restent :

| Chaîne | Actuel | Problème | Proposé |
|---|---|---|---|
| `quiz_retry` (`strings.xml:58`) | `Demain, on retente ?` | seule chaîne en U+00A0 avant « ? » | ` ` |
| guillemets dans le code (`MainActivity.kt:181,197`, `DetailActivity.kt:50`, `Widget.kt:77`, `Quotes.kt:25`, `DailyReceiver.kt:159,161`) | `« … »` | `strings.xml` met U+202F dans « » : deux espaces pour un même signe | une seule règle, U+202F partout **après** correction des polices (A1) |
| `watch_text` | au déverrouillage n° %1$d | administratif | au 1er / 2e / 3e déverrouillage |
| `unlocks_hint` | affichée sans compte de déverrouillages | hors contexte (R2) | masquée selon le choix |
| légende du timbre d’une citation | 1 MOT | faux | pas de légende |
| `rate_text` + « Noter » | vers le Play Store | appli non publiée sur Play (Q4) | carte masquée hors Play |

Voix (`docs/voix.md`) respectée sur les écrans revus : 1re personne de Glose, vouvoiement, `%1$s` pour son nom.

## 10. État des points de la revue 011

Corrigés et vérifiés : n° 1, 2, 3, 4, 5, 6 (sauf widget, contrainte RemoteViews), 7, 8, 9, 10, 12, 13, 14, 15, 17, 18, 19, 20, 21, 22 (« Garder cette tenue »), 27, 28 ; textes `collection_desc`, `stat_fragile`, `tile_sample`, `preview_wears`, `widget_add_hint`, `test_done`, `slow_default`, `quiz_score` (U+202F), `credits`, `OK` → `R.string.ok`.
Encore ouverts : n° 11 (filtre « À revoir »), 16 (Uncial), 23 (« Aucune »), 24 (widget : italique, citation coupée), 25 (progression muette juste / faux, décoratif), 26 (deux modèles de libellé), `watch_text` « n° », `quote_chip` « Citation » (non revu en capture).

## Backlog proposé

Fichiers marqués **(M)** : réservés au maître (amendement 1 du plan) ; l’équipe livre le code hors de ces fichiers et donne les lignes à appliquer dans son compte rendu.

- [P1] axe ux **Détail au style de l’accueil** — `DetailActivity.kt` — carte `Postal.postcard` (rayon 16 + liseré) avec timbre + cachet comme l’accueil, mot à la taille de la carte d’accueil (même calcul ou 32 sp mini), nature en romain (plus de `Typeface.ITALIC`), exemple en `Example` dans un carnet `card_soft` + `Postal.Seyes`, anecdote sur `card_soft`, actions Académie / TLFi / Envoyer en rangée `Action` (icône + libellé) ; aucun `20f` restant ; capture `mots_detail` jour + nuit + ×2,0 à l’appui ; TalkBack inchangé (titre = mot, étoile nommée) — ~120 lignes.
- [P1] axe gra **Espace fine insécable dans les polices (repli de police)** — `res/font/libre_baskerville.ttf`, `res/font/courier_prime.ttf` (ajout d’un glyphe U+202F ≈ ½ espace par fontTools ; pas de nom réservé, OFL permet la modification), script `tools/fonts_nnbsp.py`, `strings.xml` **(M)** : `exemple_note` en U+00A0 (Dancing Script a un nom réservé : pas de modification sans renommer) ; test Robolectric ou JVM qui vérifie la présence de U+202F dans le `cmap` des deux fichiers — critère : sur `home_light`, le « ! » de « à glisser au dîner ! » est en Dancing Script (zoom avant/après dans le compte rendu), plus aucun repli visible sur « ; : ! ? » — ~40 lignes + 2 binaires.
- [P1] axe ux **Focus visible sur l’étoile** — `activity_main.xml:189`, `DetailActivity.kt:44`, `MotsActivity.kt:155` — l’étoile garde une ondulation et un contour de focus 3 dp (`btn_round` sur fond neutre, `Postal.ring(ctx, palette.mot)` sur la carte) ; critère : capture avec `requestFocus()` montrant le contour, WCAG 2.4.7 — ~10 lignes.
- [P2] axe ux **Quiz : ✓ / ✗ en icônes, mot immobile, TalkBack explicite** — `QuizActivity.kt` (reveal), `drawable/ic_cross.xml` (nouveau, trait 1,75 comme `ic_check`), `strings.xml` **(M)** : `quiz_answer_right` « %1$s, bonne réponse », `quiz_answer_picked` « %1$s, votre réponse » — critère : plus de glyphe U+2713/2717 dans le code ; sur `quiz_trous_faux` les mots des quatre options commencent au même x (au pixel) ; `contentDescription` testé dans `quizOffersFourDistinctOptionsAndCountsStreaks` ou un test voisin — ~45 lignes.
- [P2] axe ux **Carte de notation seulement si installée par le Play Store** — `QuizActivity.kt` (`rate()` / `Quiz.askRate`) — `getInstallSourceInfo(packageName).installingPackageName == "com.android.vending"` (API 30+), `getInstallerPackageName` avant ; critère : test Robolectric, installateur nul → pas de carte, `com.android.vending` → carte — ~20 lignes.
- [P2] axe gra **Bandes cristal de l’album** — `CollectionActivity.kt:171-181` — pas de bande sur les cases verrouillées ; sur un timbre, film limité aux 25 % du bas et alpha jour 0x33 (au lieu de 0x59) pour ne plus voiler Glose ; critère : `collection_light`/`dark` avant/après, cases verrouillées d’un seul ton — ~15 lignes.
- [P2] axe ux **En-tête compact en grande police** — `MainActivity.kt` (onCreate), `CollectionActivity.kt` (en-tête) — Glose à 96 dp quand `fontScale ≥ 1.6` (§ 15 de `style.md`), 8 dp gardés autour des cibles ; critère : `home_w360_font200_long` : carte visible plus haut d’au moins 100 dp — ~12 lignes.
- [P3] axe ux **Options du quiz désactivées lisibles** — `drawable/btn.xml` (état désactivé : filet 1 dp `card_edge`), `QuizActivity.kt` (case `quiz_box` teintée `ink2` une fois désactivée) — critère : `quiz_dark_faux` montre quatre options délimitées ; aucune case plume sur une option désactivée — ~8 lignes.
- [P3] axe gra **Timbre d’une citation sans « 1 MOT »** — `MainActivity.kt:159` (`caption = if (item is Quote) null else …`) — critère : `home_quote` sans légende, `home_light` inchangé — ~2 lignes.
- [P3] axe gra **Un seul jeu de rayons** — `drawable/btn_primary.xml:12` (anneau de focus 8 dp → `radius_paper` moins l’inset, ou `radius_small`) — critère : `grep` de `radius="[0-9]` dans `res/drawable` hors `glose_*` et `btn_disc` vide — ~2 lignes.
- [P3] axe ux **Réglages : aide des déverrouillages seulement quand elle sert, ordinal** — `SettingsActivity.kt` **(M)**, `strings.xml` **(M)** (`watch_text` « au %1$s déverrouillage » avec 1er/2e/3e) — critère : `unlocks_hint` masquée si « Après la durée choisie » ; capture `settings_light_part1` — ~15 lignes.
- [P3] axe ux **Un seul modèle libellé / valeur dans Réglages** — `activity_settings.xml`, `SettingsActivity.kt` **(M)**, `strings.xml` **(M)** — « Heure d’affichage » et « Heure du quiz » en `Field` au-dessus d’un menu « 08:00 », comme « Nombre de mots par jour » — critère : capture, `Field` + `Choice` partout — ~30 lignes.
- [P3] axe ux **Aide plus aérée** — `themes.xml` **(M)** (`Help` : `lineSpacingMultiplier 1.15`) — critère : capture Réglages ; aucun texte coupé à ×2,0 — 1 ligne.
- [P3] axe ux **Typographie : U+202F unique** — `strings.xml` **(M)** (`quiz_retry`), `MainActivity.kt`, `DetailActivity.kt`, `Widget.kt`, `Quotes.kt`, `DailyReceiver.kt` (guillemets ` ` → ` `) — **après** l’item polices P1 ; critère : `grep -P '\\\\u00[aA]0[;:!?»]|«\\\\u00[aA]0'` vide sur `app/src/main` — ~10 lignes.
- [P3] axe ux **Widget sans italique** — `res/layout/widget.xml` (retirer `textStyle="italic"`), `Widget.kt:77` (rubrique « CITATION DU JOUR » en capitales `letterSpacing 0.1`) — critère : `widget/4x2*.png` sans italique ; la coupe des citations reste un sujet de données (W2) — ~10 lignes.

Hors UX (signalés à FON) : filtre « À revoir » dans Mes mots (D2, 011 n° 11) ; « Aucune » dans Couleurs et polices (R6).

## Propositions de goût (à décider par le propriétaire)

Livrées en captures seulement, rien dans le code.

1. **Icône** : `captures_1/gra_icone_avant_apres.png`. Actuel marine `#1B2440` ; A fond plume `#2C4A9A` (même dessin, accorde l’icône à l’accent de l’appli) ; B Glose dans un timbre feuillet dentelé à cadre plume, sur plume (le plus « Correspondance », un peu moins lisible à 48 dp) ; C timbre kraft à cadre cire sur papier (chaleureux, mais se perd sur fonds clairs). Recommandation de l’équipe : **A** (gain de cohérence, risque nul), B si l’on veut une icône signature. L’icône monochrome (API 33) ne change pas.
2. **Grain de papier sur le fond** : `captures_1/gra_grain_avant_apres.png` : grain de 128 px à ~4 % sur `bg` seulement (pas sous les feuilles ni la carte), ~6 Ko, jour seulement ou jour et nuit. Prévu par `style.md` § 7 « si les surfaces paraissent trop plates sur appareil » : à juger sur le téléphone.
3. **Uncial Antiqua** : la retirer du pool des mots (ou des mots contenant n/t), « lendit » se lit « lexdic » (`captures_1/share_1.png`).
4. **En-tête de la Collection** composé comme l’accueil (Glose à gauche, mention postale + titre à droite), et le fil de Glose en cordelette de reliure (§ 9 de `style.md`, non réalisé).
5. **Fin de quiz** : chaque timbre gagné reçoit le cachet du jour ou « PORTÉ » (§ 8 de `style.md`), et la progression marque juste / faux (plume pleine / cire) en plus du texte.
6. **Signature de citation** : titre d’œuvre distingué sans italique, par exemple « Adolphe Thiers, « De la propriété » » ou « Adolphe Thiers — De la propriété ».
7. **Widget en enveloppe kraft** (§ 12 de `style.md`) au lieu du liseré de carte postale actuel, surtout de nuit où le liseré clair ressort fort sur les fonds sombres (`captures_1/widget__4x2.png` et `widget/4x2_dark.png`).

