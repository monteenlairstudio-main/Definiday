# Correspondance : implémentation (phase 1, fondation + accueil)

Décision : « Correspondance + rigueur Édition » (`style.md` de ce dossier ; `../edition/style.md` pour la discipline typographique).
Phase 1 livre les jetons, les styles, les objets dessinés et l’accueil. Les autres écrans (quiz, album, Mes mots, réglages, onboarding, couleurs et polices, écran verrouillé, widget) se restylent en phase 2 **avec ces pièces, sans en créer de parallèles**.

Captures de référence : `DefinidayTest.renderDemoImages` → `app/build/demo/ui/home_*.png`.

## 1. Règles du kit

1. **Une seule serif pour l’interface** : Libre Baskerville, posée dans le thème (`AppTheme` → `android:fontFamily="@font/ui_serif"`). Ne pas écrire `fontFamily="serif"` ni `sans-serif` dans un layout : retirer ceux qui restent. `ui_serif.xml` déclare le fichier variable deux fois (400 et 700) : `textStyle="bold"` donne le vrai gras.
2. **Pas d’italique** dans l’interface : la police n’a pas de coupe italique (l’italique serait synthétique). Une citation se marque par la couleur `accent` (style `Example`), une nature grammaticale reste en romain.
3. **Courier Prime** seulement pour les mentions postales courtes (style `Postal` : date, « N° », « Question 2 sur 3 », légendes de timbre), quatre mots au plus.
4. **Dancing Script** seulement pour la note de Glose (style `Note`) : 2 à 6 mots, une par écran, jamais seule porteuse d’une information.
5. **Rubriques** : style `Label` (capitales de la serif, interlettrage 0,1, 12 sp, `ink2`, titre pour TalkBack). Fini les capitales Roboto très espacées. Libre Baskerville n’a pas de `smcp` : ce sont des capitales réduites, pas de vraies petites capitales.
6. **Titres d’écran** : style `Title` (20 sp gras) sur **tous** les écrans de destination (audit 011 n° 7) ; `Label` reste pour les rubriques internes.
7. **Un seul élément décoratif fort par écran** (accueil : timbre + cachet sur la carte). Le fil de Glose est une ligne calme, derrière les papiers.
8. **Trois rayons** (`dimens.xml`) : `radius_small` 6 dp (enveloppes, tuiles, pastilles), `radius_paper` 12 dp (feuilles, boutons, bandeaux, cartes douces), `radius_card` 16 dp (la carte postale ; face intérieure = 16 − `card_rim`). Aucun autre rayon, ni en XML ni en code (`resources.getDimension(R.dimen.radius_*)`).
9. **Une seule sélection** : contour `accent` 2 dp (`state_selected`, déjà dans `btn.xml` et `btn_envelope.xml`) ; le focus clavier est un contour 3 dp. Pas de case + cadre épais, pas de bordure dorée.
10. **Le pointillé a un seul sens** : « caché, touchez pour révéler » (voile de « Deviner avant de voir »). Le timbre gagné (`stamp.xml`), les objets verrouillés, les séparateurs : traits pleins.
11. **Filets** : style `Rule` (1 dp `card_edge`, ignoré par TalkBack). Sur une carte colorée, teinter avec `nature` à 35 % (voir `MainActivity.show`).
12. **Accessibilité** : cibles 48 dp, chaque objet dessiné (timbre, cachet, sceau, fil, Seyès) est décoratif (`importantForAccessibility="no"` ou `noHideDescendants`), le texte réel porte l’information, rien ne s’anime de plus.

## 2. Jetons (`values/colors.xml`, `values-night/colors.xml`)

Les noms existants sont gardés (le code les utilise) ; ratios WCAG en commentaire dans les deux fichiers.

| Jeton | Rôle Correspondance | Jour | Nuit |
|---|---|---|---|
| `bg` | papier, fond de fenêtre | #F8F3E9 | #141829 |
| `sheet` | feuillet : carnet, lettre, liseré de carte | #FFFCF5 | #1B2136 |
| `surface` | kraft : boutons, enveloppes, disques | #EBDDC2 | #2E2719 |
| `surface_off` | bouton désactivé | #F1E9D8 | #1F2233 |
| `kraft_dark` | rabat, filet des boutons (décor) | #C4A274 | #6B5A3C |
| `card_edge` | bord : filet 1 dp de tout papier | #D9C7A4 | #3A4566 |
| `ink`, `ink2` | encre, encre 2 | #1E2236, #4E4A5E | #F0E8D8, #BEB4A3 |
| `accent` | plume (remplace l’or) | #2C4A9A | #A3B8F0 |
| `accent_dark` | tranche du bouton principal (décor) | #1B2F66 | #6C80B8 |
| `wax`, `wax_dark` | cire : sceau, point de l’enveloppe choisie | #A23A33, #7A2924 | #C8574C, #8E3A33 |
| `yarn`, `yarn_light` | les deux brins du fil | #9A4250, #D88A97 | #B4596C, #EBA5B2 |
| `yarn_note` | note manuscrite de Glose | #86364A | #EBA5B2 |
| `seyes`, `seyes_strong` | lignes du carnet | #DFE7F5, #BFD0EC | #232A43, #34416A |
| `warn` | papier à note de Glose (avertissements) | #F4E4C1 | #3A2D14 |

`quiz_good`, `quiz_bad`, `quiz_on`, `silhouette`, `icon_bg` inchangés. Sur `sheet` et `warn` (1,08 et 1,14:1 sur `bg`), toujours un filet `card_edge`.

## 3. Styles (`values/themes.xml`)

| Style | Usage |
|---|---|
| `Label` | rubrique (« Exemple », « Autres lettres du jour », « Poses ») |
| `Postal` | mention postale Courier Prime en plume (date, « Question 2 sur 3 », « 14 timbres sur 39 ») |
| `Title` | titre d’écran |
| `Body` | texte courant (césure normale) |
| `Example` | citation recopiée, en plume |
| `Note` | note de Glose (Dancing Script SemiBold, `yarn_note`, 18 sp) |
| `Help` | aide sous un réglage |
| `Rule` | filet 1 dp |
| `Btn` (style par défaut de tout `Button`) | kraft + filet `kraft_dark`, sélection 2 dp, focus 3 dp |
| `Row` | `Btn` pleine largeur |
| `Primary` | l’action principale de l’écran : plume sur tranche `accent_dark`. Une fois faite, repasser en `btn` (accueil : « Quiz du soir (fait ✓) ») |
| `Envelope` | lettre à choisir : enveloppe kraft, mot sous le rabat, une ligne |
| `Disc` | bouton de barre du haut : disque kraft 48 dp (pilule s’il a du texte) ; donner `tooltipText` en plus de `contentDescription` |
| `Action` | icône sur libellé, sans fond (actions de la carte) |
| `Link`, `Field`, `Toggle`, `Setting` | inchangés sur le fond, serif par le thème |
| `Rubric` + `Sheet` | (phase 2b) rubrique `Label` au-dessus d’une feuille `card_soft` qui groupe des réglages (Réglages, onboarding) |
| `NoteBox` | (phase 2b) encadré d’avertissement sur `note_paper`, texte dégagé de la marge cire (alarmes exactes) |
| `Choice` | (phase 2b) `Spinner` sous son libellé `Field` |
| `Nav` | (phase 2b) ligne qui ouvre un autre écran : sans fond, chevron `ic_chevron` au bout (Réglages > Couleurs et polices) |

## 4. Drawables

| Fichier | Quoi |
|---|---|
| `btn.xml` | fond de `Btn` (états : désactivé, focus, sélectionné, repos) |
| `btn_primary.xml` | fond de `Primary` |
| `btn_disc.xml` | fond de `Disc` |
| `btn_round.xml` | cible sans fond, focus 3 dp (actions, boutons-icônes sur papier) |
| `btn_envelope.xml` + `envelope_flap.xml` | enveloppe (rabat en V, point de cire si sélectionnée) |
| `card_soft.xml` | feuille : `sheet` + filet `card_edge`, `radius_paper` (anecdote, carte de notation, toute carte douce) |
| `note_paper.xml` | billet de Glose cliquable : `warn`, filet, marge rouge cire ; pour le bandeau et **les encadrés `warn` de l’onboarding et des réglages** (remplacer `@color/warn` par ce fond) |
| `stamp.xml` | cadre du timbre gagné en fin de quiz (trait plein) ; à remplacer par `Postal.Stamp` en phase 2 si l’on veut la dentelure |
| `wax_seal.xml` | sceau de cire au croissant de lune : **réservé à l’action du soir** |
| `ic_*.xml` | icônes 24 dp sans disque, trait 1,75 arrondi, en `ink` : `ic_academie` (colonnade), `ic_tlfi` (livre), `ic_partager` (enveloppe + flèche, « Envoyer »), `ic_hasard` (dé), `ic_collection` (timbre dentelé : l’album), `ic_mots` (bannette à courrier), `ic_reglages` (curseurs), `ic_source` (plume, source d’une citation). Sur une carte colorée : `compoundDrawableTintList` = `texte` de la palette |

## 5. Objets dessinés en code (`Postal.kt`)

Tous sont des `Drawable` : ils vont dans un `ImageView`, en fond de vue, ou se dessinent sur un `Canvas` (écran verrouillé, partage, widget en bitmap).

- `Postal.Stamp(ctx, bg, fg, initial, face, caption = "1 MOT")` : timbre dentelé (trous de 2,5 dp au pas de 8 dp, `Path.op`), cadre et initiale en `fg`. Accueil : `bg = palette.mot`, `fg = palette.haut`, initiale dans `Styles.initialFace` (la police suggérée du mot si elle est gardée, même décorative). Tailles : 56 × 64 dp (carte), 80 × 96 (album), 24 × 30 sans légende (`caption = null`, progression du quiz) ; la dentelure suit la taille.
- `Postal.Postmark(ctx, color, date = today)` : cachet à double anneau, « DEFINIDAY » et le jour sur les arcs, « 7 NOV / 2026 » au centre, trois ondes vers la droite ; `color` = `palette.nature`, tracé à 75 %. 100 × 56 dp : l’anneau dans les 56 dp de gauche, les ondes passent sur un timbre posé à droite (voir `activity_main.xml`, `@id/stamp` + `@id/postmark`). Pour le tampon « PORTÉ » de l’album, même principe (classe sœur à écrire en phase 2 si besoin).
- `Postal.Seyes(ctx, textView)` : réglure Seyès tracée à partir des vraies lignes de base de `textView` (ligne forte sous chaque ligne, trois fines entre), à poser **au-dessus** de `card_soft` dans un `LayerDrawable`, en fond du parent direct du texte. Rien au-delà d’une police × 1,6. Une seule surface Seyès par écran ; jamais sur la carte du mot.
- `Postal.Yarn(ctx, host, from, to, x)` : le fil de Glose, en fond de `host` (ancêtre commun) : part du bout du fil de son dessin (`from` = l’`ImageView` de Glose), rejoint la marge à `x` puis descend jusqu’au milieu de `to`. Il passe **derrière** les papiers et ne se voit qu’entre eux : c’est voulu (ligne calme). Un fil par écran. Accueil : `x` = 36 dp, centre du sceau.
- `Postal.postcard(ctx, palette)` : la carte postale (dégradé de la palette dans un liseré `sheet` de 5 dp et son filet). Remplace `Styles.card` partout où le mot est montré en carte ; plus besoin de `Styles.needsEdge` dans ce cas.
- `Postal.ring(ctx, color)` : cible sans fond dont le focus et l’ondulation prennent `color` (actions posées sur une carte colorée).

## 6. Accueil (référence)

`activity_main.xml` et `MainActivity.show/onResume/showSeen` :

- En-tête : Glose 128 dp en haut à gauche (accessoire de saison et réactions au toucher inchangés), barre de trois `Disc` à droite (album « 8/39 », Mes mots, Réglages, avec bulles d’aide), date en `Postal` et « Une lettre de Glose » en `Title`.
- Carte postale : rubrique, timbre + cachet en haut à droite, mot (taille ajustée à la largeur réelle de la carte), nature et étoile, définition, filet, actions (`Action`, teintées par la palette ; « Envoyer » ; plume pour la source d’une citation).
- Carnet Seyès : « Exemple » + note de Glose « à glisser au dîner ! », exemple en plume.
- « Quiz du soir » en `Primary` avec le sceau à cheval sur le bord, là où arrive le fil ; une fois fait, bouton simple sans sceau.
- « Autres lettres du jour » : enveloppes, trois par ligne quand les mots sont courts, jamais coupées (un mot plus large que la ligne est seul et se coupe aux syllabes), masquées quand la seule lettre est celle de la carte.
- Bandeau de notifications : `Button` sur `note_paper` (rôle de bouton pour TalkBack, « Les réactiver › »).

## 7. Phase 2 : à faire écran par écran

- Remplacer chaque titre en `Label` par `Title` (Mes mots, Collection, Quiz) et ajouter la mention `Postal` au-dessus quand elle a du sens.
- Retirer `fontFamily`, `textStyle="italic"` et les `TextView` sans style (Roboto) restants : `CollectionActivity` (noms), `MotsActivity` (tuiles), `StylesActivity` (« sa définition » en italique), `QuizActivity`, `activity_onboarding.xml`, `activity_settings.xml`.
- Encadrés `@color/warn` (onboarding, réglages) → `@drawable/note_paper`.
- Sélection : `StylesActivity` (case + cadre), `CollectionActivity` (2 dp ?) → `state_selected` de `btn.xml`. Rayons codés en dur (`12f`, `14`, `20f` dans `CollectionActivity`, `MotsActivity`, `StylesActivity`, `Styles.card`) → `R.dimen.radius_*`.
- Collection en album : `Postal.Stamp` (80 × 96) pour les cases, « ? » pointillé → enveloppe ou estompé (le pointillé est réservé au voile à révéler).
- Quiz en lettre : `card_soft` + `Postal.Seyes`, « Bonsoir, » en `Note`, réponses en `Btn`, progression en petits `Postal.Stamp` sans légende, `Primary` pour « Suivant », fil `Postal.Yarn`.
- Écran verrouillé, partage, widget : dessiner `Postal.Stamp` et `Postal.Postmark` sur le `Canvas` de `WordCard` (même code).
