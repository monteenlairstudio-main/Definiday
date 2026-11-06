# Direction « Édition »

Planche : `design/styles/edition/board.png`. Maquettes : `home_light.png`, `home_dark.png`, `quiz.png`, `collection.png`, `lockscreen.png` (360 × 800 dp, rendus ×2). Source unique : `build.js` (génère les `.html` et les `.png` : `node design/styles/edition/build.js`).

## Concept en trois lignes

Chaque mot du jour est un volume numéroté de la collection de Monsieur Glose : « N° 0412 », couverture à la couleur du mot, cadre et filet de la maison.
L’appli se lit comme un livre bien composé : papier vergé crème, une seule encre d’accent (le violet des cahiers d’écolier), petites capitales, folios, notes de bas de page, colophon.
Glose en est le bibliothécaire : il signe l’ex-libris de l’accueil, annote le quiz (« N. d. G. ») et clôt les pages en vignette de colophon.

## Pourquoi c’est distinctif

| Concurrent | Ce qu’il fait | Ce qu’Édition fait autrement |
|---|---|---|
| Proust | Marine, pêche, crème ; cartes blanches flottantes à grand rayon et ombre douce ; serif + sans géométrique ; mascotte « Marcel » surtout dans le store | Aucune carte flottante : la page est structurée par des filets et des petites capitales, comme un livre. Encre violette, absente du créneau. Une seule famille de texte (Garamond) pour tout, y compris les libellés. Glose vit dans l’interface (ex-libris, notes), pas seulement en publicité |
| Vocabulary (Monkey Taps) | Un mot plein écran sur photo ou dégradé, défilement vertical, police modifiable seulement pour le mot | Le mot n’est pas une affiche mais une couverture numérotée qui entre dans une collection ; la numérotation donne une continuité (« j’en suis au N° 0412 ») que le flux infini n’a pas |
| 1 Jour 1 Mot | « Design simple et épuré » annoncé, interface ancienne avec publicité (benchmark) | Même sobriété, mais une grammaire éditoriale visible : rôle de chaque élément nommé par un usage du livre (folio, épigraphe, lettrine, note, catalogue, colophon) |

Garde-fous d’originalité : pas de filet rouge et noir, pas de fond blanc à cadre typographique d’une collection existante, pas de cuir et or façon reliure de bibliothèque, aucun nom, monogramme ou étoile d’éditeur. Le système de filet est propre à Definiday : un filet double maigre coupé en son centre par une pelote (le « filet-pelote »), qui n’existe chez aucun éditeur.

## Jetons de couleur

Contrastes WCAG 2.x calculés (formule de luminance relative, sans arrondi favorable). Le texte courant exige 4,5:1, le grand texte et les éléments d’interface 3:1.

### Jour

| Jeton | Valeur | Rôle | Contraste |
|---|---|---|---|
| `papier` | `#F5EFE2` | fond de fenêtre (vergé) | |
| `feuille` | `#FBF8F1` | fond des planches, feuilles modales | |
| `fond2` | `#EDE3CF` | fond de l’ex-libris, puces | |
| `encre` | `#211A14` | texte | 14,99:1 sur papier ; 16,20:1 sur feuille ; 13,49:1 sur fond2 |
| `encre2` | `#5B5144` | texte secondaire, libellés d’actions | 6,77:1 sur papier ; 7,32:1 sur feuille ; 6,10:1 sur fond2 |
| `violet` (encre violette) | `#5B3B8C` | accent unique : filets, petites capitales de rubrique, bouton principal, sélection | 7,48:1 sur papier ; 8,08:1 sur feuille ; 6,73:1 sur fond2 |
| `sur_violet` | `#F5EFE2` | texte du bouton plein | 7,48:1 sur violet |
| `violet_pale` | `#E9E1F1` | fond d’état « choisi » | violet dessus 6,74:1 |
| `filet_gris` | encre à 18 % | filets simples de séparation (décoratifs) | sans exigence (l’information est portée par le texte) |
| `sauge` | `#B9D1AB` | réponse juste | encre dessus 10,45:1 ; violet dessus 5,21:1 |
| `terre` | `#EBC0AE` | réponse choisie fausse | encre dessus 10,37:1 |
| `grain` | encre à 2,2 % | lignes vergées | invisible au contraste (luminance < 1 %) |

### Nuit

| Jeton | Valeur | Rôle | Contraste |
|---|---|---|---|
| `papier` (nuit d’encre) | `#15110D` | fond | |
| `feuille` | `#201A15` | planches, feuilles | |
| `fond2` / halo | `#2A221B`, halo radial `#5A4630` | ex-libris : la « lampe de lecture » derrière Glose | |
| `encre` (craie) | `#EEE6D6` | texte | 15,14:1 sur nuit |
| `encre2` | `#B6AA96` | secondaire | 8,21:1 sur nuit ; 7,52:1 sur feuille |
| `violet` (violet de nuit) | `#B79CE8` | accent | 7,99:1 sur nuit ; 7,32:1 sur feuille |
| `sur_violet` | `#15110D` | texte du bouton plein | 7,99:1 |
| `sauge` | `#3E5A3A` | juste | craie dessus 6,20:1 |
| `terre` | `#6E3A2C` | faux | craie dessus 7,34:1 |

Le violet a été choisi pour trois raisons : c’est l’encre des dictées et des cahiers français (le quiz du soir est une petite dictée), il est absent des concurrents (marine, or, orange), et il s’accorde avec les deux couleurs de Glose (pelote bordeaux, livre marine) sans les concurrencer. L’or actuel `#886A34` disparaît de l’interface ; il ne reste que dans les lunettes de Glose.

## Typographie

Toutes les polices sont OFL. Une seule famille de texte, une seule de titre.

| Rôle | Police | Taille / interligne | Réglages |
|---|---|---|---|
| Mot (couverture, écran verrouillé) | police du mot (styles.json) ; par défaut **Playfair Display** 500 (Didone, `playfair_display.ttf`) | 42 sp carte, 46 sp écran verrouillé, 1,08 | règle actuelle conservée : police décorative seulement pour 8 lettres au plus |
| Titre d’écran | Playfair Display 400 | 34 sp, 1,15 | « Catalogue », « Réglages » |
| Lettrine (quiz) | Playfair Display, violet | 2 lignes (62 sp) | suivie du premier mot en petites capitales |
| Texte courant, définitions, réponses | **EB Garamond** (`eb_garamond.ttf`) | 17 à 20 sp, 1,36 à 1,40 | `onum`, `liga`, `kern` |
| Exemples, nature, date, notes | **EB Garamond Italic** : seul ajout, OFL, Google Fonts | 15 à 17 sp | sous-ensemble Latin-1 + œ, Œ, ’, « », espaces fines (environ 60 Ko en TTF) |
| Rubriques, libellés, boutons, folios | EB Garamond en petites capitales | 12 à 15 sp | `smcp, c2sc, lnum`, interlettrage 0,06 à 0,09 em |
| Numéros de catalogue | EB Garamond petites capitales | 13 sp | chiffres alignés (`lnum`), quatre chiffres : « N° 0412 » |
| Marques d’usage (Vieilli, Littér., Fam.) | EB Garamond petites capitales, couleur `nature` ou violet | taille du texte | repérées dans la définition par une liste fermée |

L’italique est indispensable : un faux italique de Garamond (oblique calculé) trahit tout le style. Tenor Sans n’est plus utilisé dans l’interface (seulement pour simuler l’horloge système dans la maquette). Roboto disparaît : aides et réglages passent en Garamond.

Typographie française : espace fine insécable avant `; : ! ?` et à l’intérieur des guillemets « », apostrophe typographique ’, « N° » avec espace insécable, nombres en lettres dans les phrases courtes (« quatre épreuves », « trente-neuf planches »). EB Garamond n’a pas de glyphe U+202F : on ajoute une entrée de `cmap` U+202F vers le glyphe de U+2009 (script fontTools d’une dizaine de lignes, une fois), sinon Android va chercher la fine dans Roboto.

Justification : texte de plus de trois lignes (quiz, fiche) justifié avec césure française ; définition courte de la couverture et de l’écran verrouillé centrée, comme une page de titre. Exemple en épigraphe : italique, aligné à droite, retrait gauche de 56 dp.

## Espacements, rayons, filets

- Grille de 4 dp ; marges latérales 20 dp (16 dp pour la couverture, qui respire plus large que le texte).
- Rayons : 4 dp pour la couverture et les boutons, 2 dp pour le cadre intérieur, 0 pour les planches de la collection. Seul arrondi franc : l’arche de l’ex-libris. Le côté mignon vient de Glose et des pelotes, pas des coins.
- Cibles tactiles 48 dp au moins (lignes de réponse 56 dp, actions 62 dp).
- **Filet-pelote** (signature) : deux filets de 1 dp séparés de 2 dp, coupés au centre par une pelote de 9 dp (disque d’encre et deux brins). Au plus deux par écran : sous le titre, ou avant l’action principale.
- **Filet simple** : 1 dp, encre à 18 %, entre les lignes d’une liste ou de la table des réponses.
- **Cadre de couverture** : filet de 1 dp à 8 dp du bord, couleur `nature` du mot à 55 %, interrompu en haut par le numéro « N° 0412 ».
- **Filet de note** : 84 dp, 1 dp, `encre2`, au-dessus de la note de Glose (le filet court des notes de bas de page).
- **Cadre double** (bouton secondaire, planche choisie) : 1 dp + contour intérieur à 4 dp, violet.

## Iconographie

Trait de 1,5 dp, extrémités arrondies, grille de 24 dp, sans remplissage, couleur `encre` : gravures au trait plutôt que pictogrammes Material. Jeu de base : pelote (compteur de collection), rayonnage (collection), curseurs (réglages), fronton (Académie), livre ouvert (TLFi), envoi (partager), dé (au hasard), croix, flèche. Le favori devient un **signet** : ruban qui pend du bord haut de la couverture, à la couleur `nature` du mot, vide (contour) ou plein (enregistré). Les libellés d’action sont en petites capitales sous l’icône, cellules séparées par des filets verticaux.

## Motifs

- **Vergé** : lignes horizontales tous les 3 dp et lignes de chaîne verticales tous les 27 dp, à 2,2 % (jour) et 1,4 % (nuit). Assez discret pour qu’on le sente sans le voir ; à retirer si un test sur captures le fait remarquer.
- **Pelote** : unique ornement (filet, progression du quiz, compteur). Pas de fleurons, pas d’enluminures, pas de taches d’encre.
- **Hachures** à 135° pour ce qui est « à paraître » dans la collection.
- Vocabulaire d’édition dans les textes : « épreuves » (quiz), « planches » (objets), « parue », « à paraître », « réédition en décembre » (saisonniers : jamais « expiré »), « au sommaire du jour », « achevé d’imprimer ».

## Mouvement

Principe : « l’encre se pose ». Rien ne rebondit sauf Glose.

- Apparition d’un bloc : fondu de 0 à 1 et translation de 4 dp vers le haut, 180 ms, `DecelerateInterpolator`. Les filets se tracent du centre vers les bords (`scaleX` 0 vers 1, 240 ms), la pelote apparaît en dernier avec une demi-rotation.
- Signet : au toucher, le ruban descend de 6 dp puis remonte (`OvershootInterpolator(1.5)`, 220 ms) et se remplit.
- Quiz : bonne réponse, fond sauge qui monte de bas en haut (`clipBounds`, 200 ms) et coche tracée ; mauvaise réponse, ligne terre sans tremblement, la bonne apparaît en sauge, puis la note de Glose s’écrit en fondu. Fin de quiz : tampon « Bon à tirer » (échelle 1,12 vers 1, 160 ms, une seule fois), Glose « bravo ».
- Tout passe par `ValueAnimator.areAnimatorsEnabled()` ; sans animations, état final direct. Aucune boucle de plus de 5 s.

## Monsieur Glose dans l’édition

1. **Ex-libris** (accueil) : arche de 112 × 120 dp, double filet violet, fond `fond2` (de jour) ou halo de lampe (de nuit). Glose, en couleurs entières, déborde par le haut : les oreilles sortent du cadre. C’est le seul « débord » autorisé du système, et c’est lui qui garde la tendresse.
2. **Note de Glose** (quiz) : appel de note « ¹ » sur la réponse choisie, note en bas de page avec sa tête de 40 dp, italique, signée « N. d. G. » en petites capitales. Ton : curieux, jamais moqueur (« Presque ! Le capitan est un fanfaron de comédie. L’estramaçon, lui, tranche. »).
3. **Colophon** : médaillon rond à double filet, tête de Glose, « achevé d’imprimer par Monsieur Glose » en pied de Réglages et à la fin du quiz.
4. **Catalogue** : chaque objet est une planche numérotée (« Pl. 4 »), Glose est le sujet de chaque planche.

Écran verrouillé : pas de Glose (décision en vigueur). Si le propriétaire rouvre la question, le médaillon de colophon de 24 dp sous la dernière ligne, en réglage désactivé par défaut.

## Palettes par mot

Les 18 palettes de `styles.json` restent la « couverture » de chaque volume : dégradé `fond_haut` vers `fond_bas`, mot en `mot`, définition en `texte`, exemple en `exemple`. Ce qui reste constant d’un mot à l’autre, c’est la maison : cadre intérieur, numéro, petites capitales, filet-pelote et signet, tous dessinés dans la couleur `nature` du mot. Le cadre et le numéro vont donc partout sans nouvelle donnée.

Vérifié sur les 18 palettes : `nature` sur `fond_haut` de 5,17:1 (vert-de-gris) à 7,16:1 (encre de Chine), sur `fond_bas` de 4,61:1 (sanguine, lavande, craie) à 8,91:1 : le numéro de 13 sp et les petites capitales passent partout. Quand la couverture se fond dans la page (palettes claires sur papier, de 1,01:1 à 1,19:1 ; palettes sombres sur nuit, de 1,06:1 à 1,95:1), un bord extérieur de 1 dp en `nature` la détache (7,13:1 pour braise sur nuit) : c’est la règle `Styles.needsEdge` actuelle, généralisée.

## Carte d’écran verrouillé

- Haut 45 % libre pour l’horloge système, vergé à 1,8 % seulement.
- Bloc centré, marges de 32 dp, ancré à 50 % de la hauteur, fini avant les 90 % : « Definiday · N° 0412 » en petites capitales `nature`, filet-pelote de 150 dp, mot (46 sp), nature en italique, définition centrée (18,5 sp), exemple en italique `exemple`, puis ligne de colophon « achevé d’imprimer le 6 novembre » (11,5 sp, petites capitales, 9,13:1 sur `fond_bas` pour braise).
- Bas 10 % nu pour les raccourcis système.
- Toujours le contrôle de contraste mesuré sur le fond réel quand une photo est choisie (rapport « plus fun », rang 2).

## Widget

La **fiche de bibliothèque** (4 × 2) : bristol `feuille`, coin haut droit coupé, « N° 0412 » en haut à gauche, mot en Playfair, nature, deux lignes de définition, lignes de réglure très pâles, et un trou de fiche rond de 8 dp au centre du bas (le motif « fiche de catalogue » est universel et ne renvoie à aucun éditeur). Variante 2 × 1 : numéro et mot seuls. Rendu par le même code de dessin que la couverture (`WordCard`), en bitmap dans un `RemoteViews`.

## Capture de store

« Une collection qui s’écrit un mot par jour » : un rayonnage de 12 dos de livres, chacun à la palette d’un vrai mot du corpus, le mot écrit en long sur le dos dans sa police, un numéro en pied ; Glose assis sur le dernier volume, sa pelote qui pend. Sous le rayonnage, le téléphone (cadre Android) qui montre la couverture du jour. Fond papier vergé, légende en Playfair, mot-clé souligné d’un filet-pelote violet. Les captures suivantes reprennent la même grammaire : « Le soir, quatre épreuves » (quiz et note de Glose), « Trente-neuf planches à collectionner » (catalogue).

## Mise en œuvre (Views de la plateforme, sans AndroidX)

- **Couleurs** : `values/colors.xml` et `values-night/colors.xml` (déjà présents) : remplacer `accent` par `violet`, ajouter `feuille`, `fond2`, `violet_pale`, `filet`. Le thème `Theme.Material.Light` et le qualificatif `night` suffisent.
- **Polices** : `res/font/eb_garamond_italic.ttf` (sous-ensemble) et une `<font-family>` XML `garamond` (normal + italique), native dès l’API 26. Correctif `cmap` U+202F dans `eb_garamond.ttf`.
- **Petites capitales** : style `Label` réécrit : `fontFamily @font/garamond`, `fontFeatureSettings "smcp, c2sc, lnum"`, `letterSpacing 0.09`, `textAllCaps false`. Le texte reste en casse normale (TalkBack lit « Mot du jour », pas des initiales).
- **Justification et césure** : `justificationMode inter_word` (API 26), `hyphenationFrequency full`, `breakStrategy high_quality`, `textLocale fr-FR`.
- **Filet-pelote** : une classe `Filet : Drawable` (deux `drawLine`, un `drawCircle`, deux `drawArc`), posée en fond d’une `View` de 9 dp ; la même méthode de dessin sert dans le `Canvas` de `WordCard`.
- **Couverture** : dans `WordCard.render`, ajouter le cadre intérieur, la coupure du cadre sous le numéro (`drawRect` de `fond_haut` derrière le texte) et le signet (`Path`). Le reste (dégradé, mot, réduction de taille) ne change pas.
- **Ex-libris** : `Drawable` à `Path` (arche) en deux traits, sous l’AVD de Glose, `clipChildren=false` sur le parent pour le débord.
- **Lettrine** : `LeadingMarginSpan.LeadingMarginSpan2` sur deux lignes, la lettre dessinée dans `drawLeadingMargin`.
- **Note de bas de page** : `SuperscriptSpan` + `RelativeSizeSpan(0.6)` pour l’appel, bloc ancré en bas au-dessus du bouton.
- **Vergé** : `BitmapShader` d’une tuile de 4 × 27 px générée en code (aucun fichier), en `windowBackground` via un `Drawable` maison.
- **Animations** : `ViewPropertyAnimator` et `ValueAnimator`, garde `areAnimatorsEnabled()`.

## Effort

| Lot | Effort |
|---|---|
| Jetons jour et nuit, styles `Label` / `Title` / `Body` / `Btn`, audit de contraste | 1 j |
| Polices (italique, `cmap`, famille XML) | 0,5 j |
| `Filet`, vergé, icônes au trait (9 vecteurs) | 1 j |
| Couverture (cadre, numéro, signet) dans `WordCard` + écran verrouillé + widget fiche | 1,5 à 2 j |
| Accueil (ex-libris, actions, épigraphe, sommaire) | 1 j |
| Quiz (table, lettrine, note de Glose, bon à tirer) | 1,5 j |
| Catalogue (planches, « à paraître », numérotation) | 1 à 1,5 j |
| QA jour et nuit, police ×2, TalkBack, captures | 1 j |
| **Total** | **8,5 à 10 j** |

Risques : poids de l’APK (+ 60 Ko environ pour l’italique, rien d’autre) ; petites capitales à 12 sp à vérifier sur écran réel de faible densité ; en police ×2, la rangée d’actions passe en 2 × 2 comme aujourd’hui et la note de Glose passe sous les réponses (plus en pied).
