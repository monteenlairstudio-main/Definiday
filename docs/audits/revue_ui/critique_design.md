# Critique design : Definiday (après l'audit `interface.md`)

Méthode : `design:design-critique` (première impression, utilisabilité, hiérarchie, cohérence, accessibilité).
Matériel : 38 captures Robolectric (`scratchpad/revue_ui/*.png`, layouts réels), toutes relues ; code relu : `MainActivity`, `SettingsActivity`, `QuizActivity`, `CollectionActivity`, `WordCard`, `Styles`, `res/layout/*.xml`, `values/` et `values-night/`.
Hors périmètre : tout ce que `docs/audits/interface.md` a signalé et qui est corrigé. Les correctifs jugés insuffisants sont dans la section dédiée.

Convention :
- **[observé]** : visible dans une capture (pixels relevés au script quand un chiffre est donné).
- **[code]** : lu dans le code.
- **[hypothèse]** : déduction ou jugement de goût, non vérifié sur appareil ni auprès d'utilisateurs.

Contrastes : formule WCAG (luminance relative), couleurs prélevées dans les PNG ou lues dans `colors.xml`.

---

## Première impression (2 s)

- L'œil va à Monsieur Glose puis au mot : juste. La carte (dégradé de palette, mot en police d'affichage, définition serif) est le meilleur élément de l'appli. [observé, `home_light.png`]
- Ton : cosy et littéraire tenu sur la carte, l'écran de verrouillage et la mascotte. Sous la carte, l'écran redevient une liste de 5 blocs identiques (3 actions, puces, Réglages) : propre depuis la charte `Btn`, mais plat. [observé]
- Le correctif « tous les boutons en `Btn` » (I9) a réglé l'aspect système gris ; il a déplacé le problème : tout a maintenant le même poids. [observé + code `themes.xml:226-244`]

## Ce qui marche

- Charte unifiée crème / or / marine, coins 12 dp, contour accent 4,72:1 ; même rendu clair et sombre. [observé, `home_light.png`, `home_dark.png`]
- Carte du mot : hiérarchie label / mot / nature / définition / exemple nette à ×1,0 et ×1,3. [observé]
- Quiz : ✓/✗ dans le texte, teinte qui garde la forme, options non choisies passées en `surface_off` + `ink2` (6,63:1) ; « Suivant » seul bouton à contour, donc évident. [observé, `quiz_faux.png`]
- Fin de quiz bienveillante (« encore », « Demain, on retente ? », « ce score ne compte pas »). [observé, `quiz_encore.png`, `quiz_fin_refait.png`]
- Réglages sur leur propre écran, en 4 groupes, aides contextuelles selon le mode. [observé, `settings_light.png`]
- Écran de verrouillage : bloc texte dans la moitié basse, horloge libre, palettes sobres et lisibles. [observé, `lockscreen_0.png`, `style_*.png`]
- Collection : condition de déblocage en texte, case portée encadrée, aperçu animé. [observé]

---

## Par écran

### Accueil (`home_*.png`, `app*.png`)

Hiérarchie
- Carte : bonne à ×1,0 et ×1,3. À ×2,0 en w360, le mot long est **plus petit que la définition et que le label** (« ultracrépidarianisme » ≈ taille du corps, définition ≈ 38 sp effectifs) : l'élément à apprendre devient secondaire. Cause : `fit()` (`MainActivity.kt:155-161`) réduit le mot jusqu'à 12 sp sans plancher, alors que la définition grandit avec la police. [observé `home_w360_font200_long.png` + code]
- Sous la carte : « Nouveau mot au hasard », « Quiz du soir », « Collection », les puces et « Réglages » ont le même style `Row`/`Btn` : aucune action principale ; Réglages (usage rare) pèse autant que le quiz (usage quotidien). [observé + code `activity_main.xml:139-176`]
- Puces « Mots d'aujourd'hui » : avec le réglage par défaut (1 mot par jour, `Prefs.kt:51`), une seule puce qui répète la carte (`app.png`). La puce du mot affiché n'est pas distinguée (`home_w360.png` : carte « ultracrépidarianisme », puce identique aux autres). [observé + code `MainActivity.kt:164-182`]

Cohérence
- Liens Académie / TLFi : chacun centré dans sa moitié (poids 1), donc ni alignés sur le bord gauche de la carte ni entre eux ; « Source de l'anecdote » décalé de 8 dp (padding du style `Link`) par rapport au texte de l'anecdote. À ×1,3 « Académie française » passe sur 2 lignes, TLFi reste sur 1. [observé `home_w360.png`, `home_font130.png` + code `themes.xml:246-253`]
- Bandeau « Notifications désactivées » : rectangle plat sans coins, Roboto 15 sp, sans signe d'action ; seul élément hors charte, et il ne ressemble pas à quelque chose qu'on touche. [observé `home_notifications_off.png` + code `activity_main.xml:16-27`]
- Aide « Il remplace aussi le mot affiché sur l'écran de verrouillage » toujours visible, y compris en mode « Notification seulement » où c'est faux. [code `activity_main.xml:145-147`, pas d'id ni de visibilité]

Qualité visuelle
- Palette claire du mot sur fond crème : la carte se fond dans la page (haut de carte `#F7F5F0` sur `#FAF7F0` : 1,02:1 ; bas 1,17:1). La forme de carte disparaît. [observé `home_w360.png`, pixels]
- Glose flotte au-dessus de la carte avec un petit jour ; [hypothèse] le faire « s'asseoir » sur la carte (chevauchement de 8-12 dp) serait plus mignon et lierait mascotte et mot.

Utilisabilité
- Rien d'autre de bloquant : cibles ≥ 48 dp, état « fait ✓ » du quiz visible (`home_dark.png`).

### Réglages (`settings_*.png`)

Hiérarchie
- Titre 28 sp serif + sous-titres or : clair. [observé]
- Le premier contenu après « Heure d'affichage » est un avertissement de 3 lignes + un bouton « Autoriser les alarmes exactes », en gris d'aide standard. [observé `settings_light.png`] [hypothèse forte] Sur Android 14+, `SCHEDULE_EXACT_ALARM` est refusée par défaut aux nouvelles installations ciblant l'API 33+ (`targetSdk = 36`, `AndroidManifest.xml:11`) : la plupart des utilisateurs verront ce bloc, au ton inquiétant (« peut ne pas démarrer »), traité comme une simple aide.

Cohérence
- Trois langages visuels pour des réglages : interrupteur (texte sans-serif à gauche), menu déroulant (texte sans-serif sans bordure, indenté de 8 dp par rapport à son libellé), bouton serif centré à contour pour une valeur (« Heure d'affichage : 08:00 », « Palettes utilisées : 18 sur 18 »). Ces boutons-valeurs sont identiques aux boutons d'action (« Afficher le mot maintenant », « Autoriser… ») : on ne distingue pas « régler » de « faire ». [observé + code `activity_settings.xml:159-191`, `spinner_item.xml:6`]
- Deux familles mêlées dans un même groupe : Roboto (Toggle, Field, Help, spinner) et serif (Btn). [observé + code `themes.xml:219-268`]

Utilisabilité
- À ×2,0 en w360, la valeur se coupe : « Heure d'affichage : / 08:00 », « Palettes utilisées : 18 / sur 18 ». [observé `settings_w360_font200.png`]
- Interrupteur éteint : pouce `#ECECEC` sur `#FAF7F0` (≈1,1:1), état lisible seulement par la piste. [observé, valeur Material par défaut supposée] Impact faible, l'état est aussi annoncé.

### Quiz (`quiz_*.png`, `notif_quiz.png`)

Hiérarchie
- Question : définition serif 20 sp dominante, options pleine largeur : bon. Pas de consigne (« Quel mot correspond ? ») ; [hypothèse] compréhensible au 2e quiz, ambigu au 1er.
- Fin : « QUIZ DU SOIR » aligné à gauche, tout le reste centré. [observé `quiz_fin.png`]
- Fin avec déblocage : « Nouveau : Le fil écrit « mot », Le fil écrit « encore », Le fil écrit « bravo », Nœud papillon » en gras 18 sp centré sur 3 lignes, puis un 2e Glose (160 dp) sous le 1er (200 dp) qui ne montre que le dernier élément. C'est le moment de récompense de l'appli et il se lit comme un journal. [observé `quiz_bravo.png` + code `QuizActivity.kt:113-117, 127-134`]

Cohérence / qualité visuelle
- Vert `#7FB77E` et rouge `#E08A7E` en aplat saturé : les seules couleurs « appli de quiz » d'une charte crème / or / marine. En sombre, à 21 h, deux grands blocs clairs sur `#0F1424` (7,8:1 et 7,1:1 de luminance contre le fond) : éblouissants. Couleurs codées en dur, sans variante nuit. [observé `quiz_dark_faux.png` + code `QuizActivity.kt:147-149`]
- Glyphes ✓ / ✗ rendus par la police de repli (sans-serif, ✗ penché) à côté d'un mot serif. [observé] Mineur.

Utilisabilité
- Après une erreur, on voit le bon mot mais rien sur le mot choisi par erreur, et la fin ne rappelle pas les mots ratés (M9, 2e moitié, non faite). Pour une appli d'apprentissage, c'est l'instant où l'on retient. [observé + code]
- Notification du quiz : image de Glose endormi dans sa tasse (`notif_quiz.png`). [hypothèse] Le « dodo » peut se lire comme « rien à faire ce soir » ; une pose qui tend la pelote ou écrit « quiz » inviterait mieux. Texte du volet non vérifié (rendu système non capturé).

### Collection (`collection*.png`)

Utilisabilité
- L'aperçu (200 dp) est en haut d'une page d'environ 1 850 dp (5 553 px à ×3) ; les accessoires commencent vers 1 300 dp. Toucher « Nœud papillon » change un aperçu hors écran : on essaie à l'aveugle, puis il faut remonter pour « Choisir ». [observé `collection.png`, géométrie + code `CollectionActivity.kt:32-45` : aperçu et bouton dans le même `ScrollView`]
- « Choisir » désactivé : pastille `surface_off` à 1,05:1 sur le fond, texte `ink2` : on ne voit ni que c'est un bouton ni pourquoi il est inactif ; libellé vague (M6 non appliqué). [observé + calcul]

Hiérarchie
- Noms des éléments plus pâles que les conditions : nom `#73716E` (4,55:1, couleur de `TextView` par défaut) contre condition `#5C5446` (6,98:1). L'œil lit « Encore 30 soirs de quiz d'affilée » avant « Nuit d'encre ». [observé, pixels + code `CollectionActivity.kt:75-76`]
- En sombre, les silhouettes verrouillées `#B8AFA0` (8,45:1 sur `#0F1424`) sont les formes les plus claires de l'écran : le verrouillé domine le débloqué. [observé `collection_dark.png` + code `CollectionActivity.kt:99`]

Qualité visuelle
- Pelages verrouillés : 4 silhouettes identiques (même pose, même gris). Un pelage est une couleur ; la silhouette n'en dit rien. Pelotes : même chose (la pelote est peinte en gris). [observé]
- Titre d'écran en `Label` 12 sp (Collection, Quiz) alors que Réglages a un `Title` 28 sp : trois écrans, deux systèmes de titre. [observé + code]
- Grille 3 colonnes avec éléments orphelins (Enluminure, Pelote rose poudré seuls sur leur ligne) : acceptable.

### Écran de verrouillage et citations (`lockscreen_*.png`, `quote_*.png`, `style_*.png`)

- Bon : bloc dans la moitié basse, marges 9 %, dégradés doux, signature italique.
- « lendit » en Uncial Antiqua : `n` en petite capitale, `t` oncial ; le mot à apprendre se lit « leɴdiꞇ ». [observé `lockscreen_1.png`] La police est affectée par mot (`words.json`, champ `police`), le filtre `DISPLAY_ONLY` ne vaut que pour les citations (`Styles.kt:48`).
- Scripts (zinzolin, potron-minet) : la boucle initiale dépasse la marge gauche (z à x=33 px contre 49 px de marge, sur 540). [observé `style_zinzolin.png`, `style_potron-minet.png`] Les scripts fins (marivaudage, zinzolin) pèsent visuellement moins que la définition ; [hypothèse] acceptable, c'est le caractère du mot.
- Citations : apostrophes droites (« C'est », « n'est », « l'évidence ») alors que tout le reste emploie ’. [observé `home_quote.png`, `quote_0.png`]

---

## Correctifs de l'audit précédent jugés insuffisants

| Réf. | État | Pourquoi |
|---|---|---|
| I8 (mots longs) | partiel | Plus de coupe au milieu, mais `fit()` sans plancher inverse la hiérarchie à ×2,0 (finding 2). |
| I9 (styles) | partiel | Charte unique appliquée ; reste l'absence de niveau principal / secondaire (finding 3) et le mélange sans-serif / serif des réglages (finding 8). |
| M6 (textes) | partiel | « Choisir » inchangé (finding 15). |
| M9 (quiz) | partiel | Serif + `ink` faits ; pas de mots à revoir (finding 10). |
| M13 (polices décoratives) | non traité côté mots | Option « polices très décoratives » non faite ; Uncial Antiqua altère l'orthographe (finding 7). |
| M14 (silhouettes) | aggravé en sombre | Même couleur claire codée en dur sur fond nuit (finding 6). |

---

## Findings classés

Gravité : **Élevé** (gêne la tâche principale ou l'apprentissage) ; **Moyen** (hiérarchie ou cohérence visibles chaque jour) ; **Faible** (finition).

| # | Gravité | Écran | Capture | Constat | Correctif |
|---|---|---|---|---|---|
| 1 | Élevé | Collection | `collection.png` | L'aperçu défile hors écran : essayer un accessoire ou une pelote modifie une image invisible. [observé + code] | Sortir aperçu + « Choisir » du défilement : racine `LinearLayout` vertical = titre, aperçu 160 dp, bouton, puis `ScrollView` (`layout_weight=1`) contenant les grilles. `CollectionActivity.kt:32-45` (≈6 lignes). |
| 2 | Élevé | Accueil | `home_w360_font200_long.png` | À grande police, le mot long devient plus petit que la définition et le label. [observé + code] | `MainActivity.kt:155-161` : plancher à `1.3f * 19f` sp ; s'il est atteint, laisser le mot passer à la ligne aux syllabes (`v.hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_FULL`, au lieu de `none` en `activity_main.xml:56`). |
| 3 | Moyen | Accueil | `home_light.png` | 5 blocs de même poids ; Réglages pèse autant que le quiz ; aucune action principale. [observé] | Style `Primary` (parent `Btn`, fond `accent`, texte `bg`) pour « Quiz du soir » ; « Réglages » en style `Link` aligné à gauche (ou icône en tête). `themes.xml` + `activity_main.xml:149-176`. |
| 4 | Moyen | Quiz | `quiz_dark_faux.png`, `quiz_faux.png` | Vert / rouge saturés hors charte, éblouissants en sombre au moment du quiz. [observé + code] | `colors.xml` : `good`, `bad`, `on_good` ; `values-night/colors.xml` : versions sombres désaturées (ex. vert mousse, brique) avec texte `ink`, à valider ≥ 4,5:1 ; jour : sauge / terracotta plus doux. `QuizActivity.kt:147-149` → `getColor(R.color.good)`. |
| 5 | Moyen | Fin de quiz | `quiz_bravo.png` | Déblocage = liste en gras façon journal + deux Glose empilés. [observé] | Remplacer le Glose « bravo » par l'aperçu du nouvel élément quand il y a un déblocage ; texte « Monsieur Glose a gagné : » puis un nom par ligne, 16 sp normal, noms en minuscule initiale dans la phrase. `QuizActivity.kt:113-117, 127-134`. |
| 6 | Moyen | Collection | `collection.png`, `collection_dark.png` | Noms plus pâles (4,55:1) que les conditions (6,98:1) ; en sombre, silhouettes plus lumineuses (8,45:1) que les éléments débloqués. [observé, pixels] | `CollectionActivity.kt:75` : `setTextColor(getColor(R.color.ink))` + `typeface = Typeface.SERIF` ; `:99` : `R.color.silhouette` (`#B8AFA0` jour, ≈`#3A4256` nuit dans `values-night`). |
| 7 | Moyen | Verrouillage, accueil | `lockscreen_1.png` | Uncial Antiqua change la forme des lettres du mot à apprendre (« lendit »). [observé] | Retirer `uncial_antiqua` des polices des mots (`Styles.kt:48` : filtrer aussi pour `Word`, ou retirer le champ `police` des mots concernés dans `words.json`). Garder Fraktur et scripts, qui gardent les lettres. |
| 8 | Moyen | Réglages | `settings_light.png`, `settings_w360_font200.png` | Valeurs (heure, palettes) en boutons d'action centrés serif ; menus indentés de 8 dp ; deux familles mêlées ; valeur coupée à ×2,0. [observé + code] | Style `Setting` (parent `Btn`, `gravity="start|center_vertical"`) pour `time`, `quiz_time`, `palettes`, `polices` (`activity_settings.xml:159-161, 270-286`) ; `spinner_item.xml:6` `paddingHorizontal="0dp"` ; `fontFamily serif` dans `Field`, `Toggle`, `Help` et `spinner_item` (ou sans-serif dans `Btn`, mais un seul choix). |
| 9 | Moyen | Réglages | `settings_light.png` | Avertissement alarmes exactes en gris d'aide, 3 lignes, en tête ; [hypothèse] visible par défaut sur Android 14+. | Même traitement que le bandeau de l'accueil (fond `warn`, coins 12 dp), texte court « Le mot peut arriver jusqu'à 1 h en retard. » et bouton dedans. `activity_settings.xml:163-170`, `strings.xml:68`. |
| 10 | Moyen | Quiz | `quiz_faux.png`, `quiz_fin.png` | Rien à retenir après une erreur ; fin sans mots à revoir. [observé] | Au `reveal()` faux : ligne `ink2` 14 sp « thrène : chant funèbre. » (définition du mot choisi) ; à la fin, liste « À revoir » des réponses fausses (garder `missed`, et l'ajouter à `onSaveInstanceState`). `QuizActivity.kt:81-92, 105-124`. |
| 11 | Moyen | Accueil | `app.png`, `home_w360.png` | Avec 1 mot par jour (défaut), une puce qui répète la carte ; puce courante non signalée. [observé + code] | `MainActivity.kt:171` : si la liste a ≤ 1 élément, masquer libellé + boîte (donner un id au libellé, `activity_main.xml:160`) ; `chip.isSelected = w.key == current?.key` + état `selected` dans `btn.xml` (fond `accent` clair ou contour 2 dp). |
| 12 | Moyen | Accueil | `home_notifications_off.png` | Bandeau plat, sans coins, sans affordance. [observé] | Fond arrondi 12 dp (`GradientDrawable` ou drawable `banner.xml`), serif comme le reste, fin de texte « Activer › ». `activity_main.xml:16-27`. |
| 13 | Faible | Collection | `collection.png` | Pelages et pelotes verrouillés : silhouettes identiques, la couleur (l'objet même) est cachée. [observé] [hypothèse : la surprise est peut-être voulue] | Pour `Kind.PELAGE` et `PELOTE` : aperçu en couleur à `alpha 0.35f` au lieu du filtre `SRC_IN`. `CollectionActivity.kt:72`. |
| 14 | Faible | Accueil | `home_w360.png` | Carte de palette claire invisible sur le fond (1,02:1). [observé] | `MainActivity.kt:112-114` : `setStroke(1dp, palette.nature avec alpha 0x40)` sur le `GradientDrawable`. |
| 15 | Faible | Collection | `collection.png` | « Choisir » inactif quasi invisible (1,05:1), libellé vague. [observé + calcul] | Masquer quand rien n'a changé (`choose.visibility`), libellé « Garder cette tenue ». `CollectionActivity.kt:41-45, 90`, `strings.xml:53`. |
| 16 | Faible | Accueil | `home_light.png` | Aide « remplace aussi le mot de l'écran de verrouillage » affichée en mode notification seule. [code] | Id `new_word_hint` ; dans `onResume` (`MainActivity.kt:73-81`) : `visibility = if (p.mode == Prefs.MODE_NOTIF) GONE else VISIBLE`. |
| 17 | Faible | Accueil | `home_w360.png`, `home_font130.png` | Liens centrés dans leurs moitiés, non alignés ; « Source » décalé de 8 dp. [observé] | `Link` : `gravity="start|center_vertical"`, `paddingStart="0dp"` ; liens en `wrap_content` avec `marginEnd="24dp"` au lieu du poids. `themes.xml:246-253`, `activity_main.xml:95-109`. |
| 18 | Faible | Collection, Quiz | `collection.png`, `quiz_fin.png` | Titres d'écran : `Title` 28 sp (Réglages) contre `Label` 12 sp (Collection) ; fin de quiz : label à gauche, contenu centré. [observé] | `CollectionActivity.kt:34` : style `Title` ; `QuizActivity.kt:106` : `gravity = CENTER` sur le titre de fin. |
| 19 | Faible | Verrouillage | `style_zinzolin.png` | Boucle des scripts hors marge gauche. [observé] | `WordCard.kt:279` : `translate(margin - minOf(0f, bounds.left), y)` avec `paint.getTextBounds` de la 1re ligne. |
| 20 | Faible | Citations | `home_quote.png`, `quote_0.png` | Apostrophes droites. [observé] | `Quotes.kt:16` : `getString("texte").replace('\'', '’')`. |
| 21 | Faible | Quiz | `quiz_question.png` | Pas de consigne. [hypothèse sur l'impact] | « 1 / 4 · Quel mot ? » (`QuizActivity.kt:78`). |
| 22 | Faible | Quiz | `quiz_juste.png` | ✓ / ✗ en police de repli à côté du serif. [observé] | Accepter, ou glyphes d'une police embarquée qui les contient. |

## Limites

- Robolectric : pas de vrai volet de notifications, pas de rendu constructeur, pas d'animation (Glose figé).
- La taille d'écran utilisée pour le finding 1 (≈ 891 dp de haut) est supposée ; l'écart aperçu / accessoires (≈ 1 100 dp) reste hors écran sur tout téléphone.
- Les couleurs proposées (findings 4, 6) sont des directions ; leurs ratios sont à recalculer avant intégration.
- Aucun code modifié.
