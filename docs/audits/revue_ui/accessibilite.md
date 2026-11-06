# Revue accessibilité : Definiday (WCAG 2.1 AA, commit 79ba046)

Méthode : `design:accessibility-review` (WCAG 2.1 AA), adaptée Android : TalkBack, cibles 48 dp, police ×1,3 / ×2,0, w360dp, mode sombre, animations système coupées.
Matériel : captures Robolectric `scratchpad/revue_ui/*.png` (home, settings, quiz, collection, lockscreen, quote, notif_quiz) ; code `fr/definiday/*.kt`, `res/layout`, `res/values*`, `drawable/btn.xml`, `color/btn_text.xml`, `assets/styles.json`, `AndroidManifest.xml`.
Point de départ : `docs/audits/interface.md` (commit 442fcaf). Les points corrigés ne sont pas repris, sauf correctif insuffisant.

Convention : **[mesuré]** = ratio calculé (luminance relative WCAG) sur une couleur du code ou un pixel de capture ; **[code]** = lu dans le code ; **[hypothèse]** = conséquence probable du code et du comportement Android/TalkBack, non vérifiée sur appareil.
Scripts : `contrast.py` (ratios, palettes) et `wallsim.py` (estimation de la réduction du texte du fond d'écran), dans le scratchpad de la session, non versionnés.

## Synthèse

| Gravité | Nombre |
|---|---|
| Critique | 0 |
| Majeur | 5 |
| Mineur | 13 |

Le gros de l'audit précédent est corrigé et vérifié (voir « Corrigés vérifiés »). Il reste surtout : le retour du quiz au lecteur d'écran, deux indicateurs d'état peu contrastés (interrupteurs, focus), et le mode « écran de verrouillage seulement », où le mot n'existe qu'en image.

## Constats

| # | Constat | WCAG | Gravité | Écran | Preuve | Correctif |
|---|---|---|---|---|---|---|
| A1 | Quiz : la réponse « Juste / Faux, c'était X » n'est donnée à TalkBack que par `announceForAccessibility`, déprécié en API 36 (targetSdk 36). Rien ne déplace le focus d'accessibilité : après une réponse, il reste sur le bouton désactivé, et « Suivant » est ajouté en bas. `next()` reconstruit l'écran par `removeAllViews()` : le focus disparaît à chaque question et à l'écran de score (score, « Nouveau : … » non lus). | 4.1.3, 2.4.3 | Majeur | Quiz | [code] `QuizActivity.kt:99` (annonce), `:72` (removeAllViews), `:91` (Suivant en fin de liste). [hypothèse] focus perdu et annonces ignorées par une future version de TalkBack (non testable en Robolectric). | Ajouter après la réponse un `TextView` visible « Juste » / « Faux, c'était X » avec `accessibilityLiveRegion = ACCESSIBILITY_LIVE_REGION_POLITE` (sert aussi aux voyants) et supprimer l'annonce. Après `next()` et `finishQuiz()` : `compteur.post { compteur.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null) }` (sur le score en fin de quiz). |
| A2 | Interrupteurs (`Switch`) en position « off », thème clair : pouce et piste quasi invisibles sur le fond. | 1.4.11 | Majeur | Réglages (3 interrupteurs) | [mesuré, pixels `settings_light.png`] pouce off `#ECECEC` / fond `#FAF7F0` = 1,10:1 ; piste off `#B9B7B2` = 1,87:1 (exigé 3:1). Sombre : pouce off 9,34:1, ok. « On » clair : pouce `#856833` 4,88:1, ok. | `android:trackTint` (API 23+) dans le style `Toggle` avec un `ColorStateList` : non coché `#7A7263` (4,45:1 sur `bg`), coché `@color/accent`. Revérifier sur appareil (la piste Material applique sa propre opacité). |
| A3 | Focus clavier / Switch Access : `btn.xml` est un `ripple` sans état `state_focused` ; le focus n'est montré que par le voile du ripple, couleur claire codée en dur, à peine visible, surtout en sombre. Concerne tous les `Button` (thème `Btn`), donc quiz, puces, réglages. | 2.4.7 | Majeur | Tous | [code] `drawable/btn.xml:3` `android:color="#33886A34"`. [mesuré] voile sur `surface` clair `#DCD0BA` / `#F1EADB` = 1,27:1 ; sombre `#303035` / `#1A2135` = 1,22:1. [hypothèse] rendu réel du focus non capturé. | Dans le `selector` de `btn.xml`, ajouter avant l'item par défaut `<item android:state_focused="true">` : même forme, `stroke` 3 dp `@color/accent` (4,72:1 clair, 8,19:1 sombre). Couleur du ripple : `?android:attr/colorControlHighlight` ou une valeur par thème dans `values-night`. |
| A4 | Mode « Écran de verrouillage seulement » : le mot n'est qu'une image (bitmap du fond d'écran), sans aucune notification. Un utilisateur de TalkBack n'a rien à lire sur l'écran de verrouillage ; l'aide ne le signale pas. Le texte du fond d'écran ne suit pas non plus la taille de police système. | 1.1.1, 1.4.5, 1.4.4 | Majeur | Écran de verrouillage, Réglages | [code] `DailyReceiver.kt:114` (`MODE_LOCK` : aucune notification) ; `WordCard.kt:27-30` (tailles en fraction de la largeur, pas de `fontScale`). [mesuré, calcul] sur 411 dp de large : mot 49 dp, définition 20,6 dp, nature/exemple 17,3 dp ; sur 360 dp : 18 / 15,1 dp, à toute échelle de police. [estimation `wallsim.py`, polices de bureau] la réduction (`:58`, jusqu'à ×0,6) touche 5 mots sur 1 342, au plus ×0,9. | Aide sous « Affichage » : « Seule la notification est lue par les lecteurs d'écran. » Si `AccessibilityManager.isTouchExplorationEnabled`, la montrer en évidence et proposer « Notification et écran de verrouillage ». Option : multiplier les tailles de `WordCard` par `min(fontScale, 1,3)`. |
| A5 | « Nouveau mot au hasard » et puces « Mots d'aujourd'hui » : la carte change et défile en haut, mais le focus TalkBack reste sur le bouton ; le nouveau mot n'est ni lu ni signalé. La carte se lit en 5 éléments séparés (label, mot, nature, définition, exemple). | 4.1.3, 2.4.3 | Majeur | Accueil | [code] `MainActivity.kt:40-45` et `:172-176` appellent `show()`, qui ne fait que `smoothScrollTo` (`:150`). [hypothèse] comportement TalkBack. | `android:screenReaderFocusable="true"` sur `@id/card` (API 28, ignoré en 26-27), puis dans les deux écouteurs de clic seulement (pas dans `onResume`) : `card.post { card.performAccessibilityAction(ACTION_ACCESSIBILITY_FOCUS, null) }`. |
| A6 | Aucune Activity n'a de titre propre : TalkBack annonce « Definiday » en ouvrant Réglages, Quiz et Collection ; même nom dans les Récents. | 2.4.2 | Mineur | Réglages, Quiz, Collection | [code] `AndroidManifest.xml:31-33` (pas de `android:label`, seul celui de l'application). | `android:label="@string/settings"`, `@string/quiz_title`, `@string/collection_title` sur les trois `<activity>`. |
| A7 | Compteur « 1 / 4 » et score « 3 / 3 » lus tels quels. Boutons révélés « ✓ mot » / « ✗ mot » : le symbole porte seul l'état pour TalkBack. | 1.3.1, 4.1.2 | Mineur | Quiz | [code] `QuizActivity.kt:78`, `:118`, `:86`. [hypothèse] synthèse fr : « 1 barre oblique 4 », « ✗ » lu de façon variable ou ignoré. | `contentDescription` : « Question 1 sur 4 », « Score : 3 sur 3 » ; boutons : « estramaçon, bonne réponse » / « thrène, votre réponse, fausse ». |
| A8 | Lien « Académie française » désactivé pendant la recherche (jusqu'à 12 s) sans aucun changement visible (`Link` a une couleur de texte fixe) ni message d'attente. | 4.1.3 | Mineur | Accueil | [code] `MainActivity.kt:199` ; `values/themes.xml:68-75` (style `Link`, `textColor` = couleur simple, pas de sélecteur). | `res/color/link_text.xml` (désactivé = `ink2`) ; texte temporaire « Recherche… » avec `accessibilityLiveRegion` polite, rétabli dans `runOnUiThread`. |
| A9 | Animations en boucle infinie (11 AVD, cycles de 3,8 à 6,4 s) à côté du contenu ; aucun moyen de les mettre en pause dans l'appli : un toucher sur Glose les relance. Le réglage système « Supprimer les animations » est respecté (`Glose.kt:59`). Non corrigé depuis M12. | 2.2.2 | Mineur | Accueil, Quiz (fin), Collection | [code] `repeatCount="infinite"` dans `glose_assis.xml` (×11), `glose_ecrit_*`, `glose_soir.xml` ; `MainActivity.kt:54`. | Toucher = pause/lecture : `if (d.isRunning) d.stop() else play(v)` ; nommer l'action pour TalkBack (`AccessibilityDelegate`, `AccessibilityAction(ACTION_CLICK, "Mettre en pause")`). |
| A10 | Glose de l'accueil cliquable : TalkBack dit « Monsieur Glose, chat bibliothécaire, appuyer deux fois pour activer » sans dire quoi, et le clic ne fait rien quand les animations sont coupées. La description ne change pas selon la pose (« le fil écrit mot / dodo… »). Aperçu de la Collection : description fixe, ne nomme pas la tenue essayée (M7, partie non corrigée). | 4.1.2, 1.1.1 | Mineur | Accueil, Collection | [code] `activity_main.xml:29-34`, `MainActivity.kt:54` ; `CollectionActivity.kt:35-36` (jamais mise à jour dans `refresh()`, `:87-97`). | Accueil : nommer l'action (voir A9) ou `isClickable = ValueAnimator.areAnimatorsEnabled()`. Collection, dans `refresh()` : `preview.contentDescription = getString(R.string.glose) + " : " + trial.values.mapNotNull { Unlocks.byId[it]?.nom }.joinToString()`. |
| A11 | Cases verrouillées de la Collection : le `LinearLayout` porte une `contentDescription` complète, mais il n'est pas cliquable ; ses `TextView` enfants (nom, condition) restent lisibles séparément : lecture en double. | 1.3.1 | Mineur | Collection | [code] `CollectionActivity.kt:75-77`. [hypothèse] règle de regroupement TalkBack (un parent non actionnable ne regroupe pas ses enfants). | Sur chaque case : `isScreenReaderFocusable = true` (API 28) et `importantForAccessibility = NO` sur l'image et les deux `TextView`. |
| A12 | Puces « Mots d'aujourd'hui » : rien n'indique laquelle est affichée, ni à l'écran ni pour TalkBack. | 1.3.1 | Mineur | Accueil | [code] `MainActivity.kt:172-176` ; capture `home_light.png`. | `isSelected = w.key == current?.key` + contour 2 dp accent, comme les cases de la Collection (`CollectionActivity.kt:92-95`). |
| A13 | Bandeau « Notifications désactivées » : `TextView` cliquable, sans rôle bouton. | 4.1.2 | Mineur | Accueil | [code] `activity_main.xml:16-27`, `MainActivity.kt:49`. | `accessibilityDelegate` qui pose `className = Button::class.java.name`, ou un `Button` style `Row` sur fond `warn`. Contraste ok : `ink`/`warn` 13,6:1 clair, 10,4:1 sombre [mesuré]. |
| A14 | Police ×2,0 en w360 : le mot est réduit sous la taille de la définition (`fit()` descend jusqu'à 12 sp) ; la hiérarchie s'inverse (le mot à apprendre paraît secondaire). | 1.3.1, 1.4.4 | Mineur | Accueil | [mesuré, capture `home_w360_font200_long.png`] « ultracrépidarianisme » nettement plus petit que la définition ; [code] `MainActivity.kt:155-161`. | Plancher de `fit()` = 24 sp ; en dessous, garder 24 sp et laisser couper aux syllabes (`hyphenationFrequency = HYPHENATION_FREQUENCY_FULL`, `textLocale = Locale.FRENCH`). |
| A15 | Langue : les mots, définitions et citations sont en français, mais rien ne fixe la langue du texte. Sur un téléphone réglé dans une autre langue, TalkBack lirait le mot avec la synthèse de cette langue (prononciation fausse, alors que c'est le mot à apprendre). | 3.1.1, 3.1.2 | Mineur | Accueil, Quiz, notifications | [code] pas de `LocaleSpan` ni de `textLocale` (grep). [hypothèse] dépend de la langue de l'appareil. | `textLocale = Locale.FRENCH` sur les `TextView` de contenu, et texte en `SpannableString` avec `LocaleSpan(Locale.FRENCH)` (TalkBack change de voix sur ce span). Même chose pour le titre et le texte des notifications. |
| A16 | Fond d'écran, palettes claires : marge faible au-dessus de 4,5:1 pour la nature et le label (min 4,61:1). Si le système assombrit le fond d'écran (option « le thème sombre assombrit le fond d'écran », assombrissement de l'écran de verrouillage selon les constructeurs), le ratio passe sous le seuil. | 1.4.3 | Mineur | Écran de verrouillage | [mesuré] sans voile : ok (tableau ci-dessous). Avec voile noir 10 % : craie 3,69, lavande 3,70, sanguine 3,72, givre 3,73 ; 20 % : environ 2,9. [hypothèse] application de ce voile à l'écran de verrouillage, selon la version et le constructeur. | Viser 5,5:1 pour `nature` sur les 9 palettes claires (`assets/styles.json`, à la main, cf. graphene b671) ; tester sur appareil, thème sombre activé. |
| A17 | Noms des éléments de la Collection en couleur secondaire par défaut de la plateforme (`#8A000000`), pas dans les jetons de l'appli : 4,55:1, juste au seuil. Le nom d'un élément débloqué est plus pâle que la condition d'un élément verrouillé (`ink2`, 6,98:1). | 1.4.3 | Mineur | Collection | [mesuré, pixel `collection_light.png`] `#73716E` / `#FAF7F0` = 4,55:1 (sombre 9,33:1) ; [code] `CollectionActivity.kt:75` (pas de couleur). | `setTextColor(getColor(R.color.ink))` sur le nom. |
| A18 | Notification du quiz : grande image (Glose endormi) sans description. | 1.1.1 | Mineur | Notification | [code] `DailyReceiver.kt:84`. | API 31+ : `BigPictureStyle().setContentDescription(getString(R.string.glose))`, sinon rien (image décorative, le texte suffit). |

Hors AA, rappel (M13, non repris comme constat) : les polices de titrage gothique, onciale et manuscrites restent dans le pool par défaut pour le mot (`lockscreen_1.png` : « lendit » en Uncial, n et t ambigus). L'utilisateur peut les retirer (« Polices utilisées ») : la parade existe mais reste cachée.

## Contrastes

| Élément | Avant-plan | Fond | Ratio | Exigé | Résultat |
|---|---|---|---|---|---|
| Libellés `Label`, liens, clair | `#886A34` | `#FAF7F0` | 4,72 | 4,5 | ok |
| Même chose, sombre | `#C9A96E` | `#0F1424` | 8,19 | 4,5 | ok |
| Texte des boutons, clair / sombre | `ink` | `surface` | 14,42 / 12,87 | 4,5 | ok |
| Bouton désactivé (options du quiz non choisies), clair / sombre | `ink2` | `surface_off` | 6,63 / 7,70 | 4,5 | ok |
| Aides `Help`, clair / sombre | `ink2` | `bg` | 6,98 / 8,14 | 4,5 | ok |
| Contour des boutons, clair / sombre | `accent` | `bg` | 4,72 / 8,19 | 3 | ok |
| Quiz : texte sur vert / rouge | `#1F1A14` | `#7FB77E` / `#E08A7E` | 7,38 / 6,67 | 4,5 | ok |
| Quiz : vert contre rouge | `#7FB77E` | `#E08A7E` | 1,11 | n/a | ok : l'état est dit par ✓/✗, pas par la couleur (1.4.1) |
| Quiz : bouton vert / rouge sur fond clair | GOOD / BAD | `#FAF7F0` | 2,19 / 2,42 | exempté | boutons désactivés (inactifs, exemptés de 1.4.11) ; texte et symbole suffisent |
| Interrupteur off clair : pouce / piste | `#ECECEC` / `#B9B7B2` | `#FAF7F0` | 1,10 / 1,87 | 3 | **échec** (A2) |
| Focus des boutons (voile ripple), clair / sombre | `#DCD0BA` / `#303035` | `surface` | 1,27 / 1,22 | 3 | **échec** (A3) |
| Silhouette verrouillée, clair | `#B8AFA0` | `#FAF7F0` | 2,03 | 3 | info : l'état est aussi dit en texte |
| Bandeau notifications, clair / sombre | `ink` | `warn` | 13,62 / 10,35 | 4,5 | ok |
| Flèche des menus déroulants, clair | `#5C5446` | `#FAF7F0` | 6,98 | 3 | ok |

### Palettes de `assets/styles.json` (18), pire extrémité du dégradé

Revendication vérifiée : texte, exemple, nature ≥ 4,5:1 et mot ≥ 3:1 sur `fond_haut` et `fond_bas` : **aucun échec** [mesuré].

| Palette | sombre | mot | nature | texte | exemple |
|---|---|---|---|---|---|
| encre_de_chine | oui | 15,35 | 7,16 | 12,50 | 7,16 |
| ardoise | oui | 8,86 | 5,56 | 8,00 | 5,94 |
| vert_de_gris | oui | 8,22 | 5,17 | 7,45 | 5,49 |
| braise | oui | 8,92 | 5,97 | 11,73 | 7,45 |
| parchemin | non | 9,87 | 4,84 | 8,74 | 5,69 |
| sepia | non | 7,96 | 4,69 | 7,53 | 5,30 |
| sanguine | non | 6,33 | 4,61 | 9,13 | 6,26 |
| lavande | non | 8,06 | 4,61 | 9,43 | 6,02 |
| givre | non | 8,91 | 4,65 | 10,56 | 6,15 |
| aube | non | 6,15 | 4,67 | 9,61 | 6,55 |
| pistache | non | 7,37 | 4,72 | 9,76 | 6,20 |
| craie | non | 13,42 | 4,61 | 11,43 | 6,92 |

Les 6 autres (bleu_nuit, lie_de_vin, sous_bois, violine, abysses, safran) : nature ≥ 4,72, le reste plus haut. Le mot (≥ 40 sp gras, 49 dp sur le fond d'écran) relève du texte large : 3:1 suffit, minimum mesuré 6,15. Limite : dégradé mesuré à ses deux extrémités, pas en pixels ; le voile système éventuel n'est pas compté (A16).

## Cibles tactiles, titres, texte agrandi

- Cibles ≥ 48 dp [code] : `Btn` (`minHeight 48dp`, `themes.xml:48-60`), `Link`, `Toggle`, menus (`minHeight 48dp`), éléments de liste (`spinner_item.xml`), bandeau, puces (boutons `Btn`), cases de Collection (image 96 dp). Aucun écart trouvé.
- Titres : `Label` et `Title` portent `accessibilityHeading` (`themes.xml:13-31`) ; sections des Réglages, de la Collection et du quiz couvertes.
- Police ×2,0 en w360 : les Réglages se replient sans coupure (`settings_w360_font200.png`), les menus s'adaptent en hauteur, les liens TLFi / Académie passent sur deux lignes sans sortir de l'écran. Reste A14.

## Corrigés vérifiés (audit `interface.md`)

| Ancien | État | Preuve |
|---|---|---|
| B3 couleur seule au quiz | corrigé | ✓/✗ dans le texte (`QuizActivity.kt:86`), texte 7,38 / 6,67:1, forme conservée (`quiz_faux.png`, `quiz_dark_faux.png`). Retour TalkBack : insuffisant, voir A1. |
| I6 menus sans nom | corrigé | `android:labelFor` sur les 5 libellés (`activity_settings.xml:44, 71, 87, 103, 120`). |
| I7 contrastes | corrigé | accent clair 4,72:1 ; nature ≥ 4,61:1 sur 18 palettes ; indice de Collection `ink2` 6,98:1 sans alpha. |
| I8 grande police | corrigé | poids 1 sur les liens (`activity_main.xml:95-109`), `fit()` du mot, menus en `minHeight`. Reste A14. |
| I12 animations coupées | corrigé | `Glose.kt:59`, pas de `reset()` quand les animations sont coupées. |
| M7 titres, état porté, tête du quiz | corrigé en partie | `accessibilityHeading`, `isSelected` (`CollectionActivity.kt:92`), tête `IMPORTANT_FOR_ACCESSIBILITY_NO` (`QuizActivity.kt:74`). Reste A10 (aperçu). |
| M12 boucle infinie | non corrigé | A9. |
| M13 boîtes sans titre | corrigé | `setTitle(title)` (`SettingsActivity.kt:107`). |

## Limites

- Robolectric : pas de vrai TalkBack ni de Switch Access ; ordre de focus, annonces et lecture des symboles = [hypothèse] à confirmer sur appareil (TalkBack fr, Android 16).
- Contrastes des interrupteurs et du focus : rendu Robolectric du widget plateforme, et calcul du voile de ripple ; le rendu constructeur peut différer.
- `wallsim.py` : métriques DejaVu / Liberation Serif, pas Noto Serif Android ; ordre de grandeur seulement.
- Aucune modification du code de l'appli.
