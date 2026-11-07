# Herbier des mots

Direction de style pour Definiday. Maquettes : `home_light.png`, `home_dark.png`, `quiz.png`, `collection.png`, `lockscreen.png`, planche d'ensemble `board.png` (régénérer : `node build.js` dans ce dossier, Playwright requis). Les dessins gravés sont produits par `gravure.js`.

## 1. Concept en trois lignes

Chaque mot du jour est un spécimen rare, cueilli le matin et monté sur une planche d'herbier : papier vélin, coins de montage, étiquette tapée à la machine et notes de terrain à la plume.
Monsieur Glose est le naturaliste de la maison : il examine le mot à la loupe, épingle l'étiquette et range ses trouvailles dans un cabinet à tiroirs.
Le monde est gravé en taille-douce (traits fins, hachures, encres sépia et botaniques) ; Glose, lui, reste en couleur et bien rond : c'est le seul être vivant de la planche.

## 2. Pourquoi c'est unique

| Concurrent | Ce qu'il montre | Ce que l'Herbier fait autrement |
|---|---|---|
| Proust | Aplats vectoriels marine, pêche et crème, cartes blanches flottantes, personnage moustachu plat, sans-serif pour l'interface | Aucune carte flottante : des **planches** en papier avec double filet gravé, des **montures** tenues par des coins, des **étiquettes** à œillet. Pas un aplat pêche ni marine. La mascotte n'est pas un écrivain mais un chat qui observe |
| Vocabulary | Fonds sombres ou photos, grand mot centré, sans-serif, minimalisme « citation du jour » | Un système d'**objets de collection** (numéro de catalogue, date de cueillette, tampon), une texture de papier, une vraie métaphore de rangement |
| 1 Jour 1 Mot | Carte de mot simple, mise en page utilitaire | Une identité d'objet : chaque mot devient un spécimen numéroté que l'on garde, et la collection devient un meuble |

Le trio « spécimen, étiquette, cabinet » n'existe chez aucune appli de mot du jour. Il raconte aussi ce que fait Definiday mieux que les autres : un mot vraiment rare, hors ligne, gardé pour soi.

## 3. Jetons de couleur

Ratios WCAG 2.x calculés (formule de luminance relative de WCAG 2.x, sans grain) ; les valeurs entre parenthèses sont les fonds testés.

### Jour (« papier d'herbier »)

| Jeton | Hex | Rôle | Contraste |
|---|---|---|---|
| `papier` | `#F2ECDF` | fond de fenêtre | |
| `planche` | `#FBF8F1` | planches, cartes | |
| `etiquette` | `#FFFDF7` | étiquettes, réponses | |
| `bois` | `#ECE3D1` | bâti du cabinet | |
| `encre` | `#2A2018` | texte principal | 13,5 (papier), 15,0 (planche), 15,7 (étiquette), 12,5 (bois) |
| `encre2` | `#5C4C3B` | texte secondaire | 7,0 (papier), 7,8 (planche), 6,5 (bois) |
| `sepia` | `#7B5530` | cotes en machine à écrire, filets, contours actifs | 5,6 (papier), 6,2 (planche), 6,5 (étiquette) |
| `herbier` | `#3D5A36` | action principale, état « porté », « Presque ! » | 6,6 (papier) ; texte `papier` sur bouton 6,6 |
| `garance` | `#9A3B2B` | têtes d'épingle, tampon, progression | 5,9 (papier), 6,5 (planche) |
| `prusse` | `#2E4A6E` | notes manuscrites | 7,7 (papier), 8,9 (étiquette) |
| `juste` | `#D6E2C8` | réponse juste (fond) | `encre` dessus 11,8 |
| `faux` | `#EFD3C9` | réponse choisie fausse (fond) | `encre` dessus 11,3 |
| `trait` | `#B5A58A` | hachures, filets décoratifs | 2,0 à 2,4 : **décor seulement**, jamais seul porteur d'information |
| `kraft` | `#D2B88C` | coins de montage, ficelle | 1,8 : décor |
| `laiton` | `#A8823F` | porte-étiquettes des tiroirs, œillets | 3,5 (étiquette) : contour non textuel suffisant |

### Nuit (« cabinet à la lampe »)

| Jeton | Hex | Contraste |
|---|---|---|
| `papier` | `#16120E` | |
| `planche` | `#211B15` | |
| `etiquette` | `#2A231B` | |
| `bois` | `#1E1813` | |
| `encre` | `#EEE5D3` | 14,9 (papier), 13,6 (planche), 12,4 (étiquette), 14,0 (bois) |
| `encre2` | `#BCAE97` | 8,6 (papier), 7,8 (planche), 7,1 (étiquette) |
| `sepia` | `#D4AA6E` | 8,7 (papier), 7,9 (planche), 7,2 (étiquette) |
| `herbier` | `#A9C79A` | 10,1 (papier) ; sert de bordure 1 dp au bouton principal |
| bouton principal | fond `#3D5A36`, texte `encre` | 6,2 ; bordure `herbier` 10,1 contre le fond (le fond seul ne ferait que 2,4) |
| `garance` | `#E8A08C` | 8,7 (papier) |
| `prusse` | `#9DBAE0` | 9,4 (papier), 7,8 (étiquette) |
| `juste` / `faux` | `#2F4630` / `#5A2E26` | `encre` dessus 8,2 / 9,1 |
| `trait` / `kraft` / `laiton` | `#6A5A45` / `#7A6040` / `#B8924E` | décor |

Halo de lampe : dégradé radial `#E8B464` à 22 % derrière Glose, jamais derrière du texte.

Correspondance avec `colors.xml` actuel : `bg` devient `papier`, `surface` devient `planche`, `accent` se scinde en `sepia` (cotes, filets) et `herbier` (action), `quiz_good` et `quiz_bad` deviennent `juste` et `faux`, `card_edge` devient le filet `nature` de la monture (voir § 9).

## 4. Typographie

Trois familles de `res/font` (OFL) et **une seule police nouvelle** : EB Garamond Italic (OFL, Google Fonts, environ 200 Ko en .ttf, 21 Ko en woff2 latin). Elle est essentielle : le fichier `eb_garamond.ttf` n'a qu'un romain, or la nature grammaticale et l'exemple sont en italique, et un oblique synthétique de Garamond est laid. En attendant, l'italique de Noto Serif du système dépanne.

| Rôle | Police | Taille | Interligne | Couleur |
|---|---|---|---|---|
| Mot du jour | police du mot (règle actuelle : décorative si 8 lettres au plus), sinon EB Garamond | 46 sp | 1,0 | `mot` de la palette |
| Titre d'écran | IM Fell English | 25 à 27 sp | 1,0 | `encre` |
| Question du quiz | IM Fell English | 23 sp | 1,15 | `encre` |
| Définition | EB Garamond | 18,5 à 20 sp | 1,3 | `texte` de la palette ou `encre` |
| Nature, exemple | EB Garamond Italic | 19 sp / 16,5 sp | 1,28 | `nature` / `encre` |
| Boutons, réponses | EB Garamond | 20 à 21 sp | | |
| Corps, aide | EB Garamond | 16 à 17,5 sp | 1,3 | `encre`, `encre2` |
| Cote (catalogue, surtitres, noms de tiroir) | Courier Prime, capitales, interlettrage 0,12 em | 12 sp (minimum) | | `sepia` |
| Note de terrain | Dancing Script 500 | 19 à 24 sp | 1,0 | `prusse` ou `herbier` |

Règles : Garamond a un petit œil, d'où des tailles de 1 à 3 sp au-dessus des actuelles. La note manuscrite n'est jamais seule à porter une information (la date de cueillette est aussi dans la cote « Planche n° 412 » et lue par TalkBack). Espace fine insécable (U+202F) avant `;` `:` `?` `!` et dans les guillemets.

## 5. Espacements, rayons, ombres

- Grille de 4 dp ; gouttière d'écran 12 à 16 dp ; marge intérieure de planche 20 à 24 dp ; écart entre réponses 10 dp.
- Rayons : **2 dp** pour planches et montures (du papier, pas des pilules), 4 dp pour les boutons, 0 pour les étiquettes (l'œillet fait la forme). C'est une rupture voulue avec les 12 à 24 dp actuels et avec les cartes très arrondies de Proust.
- Double filet gravé à 5 dp du bord de chaque planche : 1 dp puis 0,5 dp, couleur `trait`.
- Ombre : une seule, basse et chaude (`0 6 18 -8` à 16 % de brun), pour que la planche se pose sur le bureau ; pas d'élévation Material.
- Cibles tactiles 48 dp partout (les icônes gravées de 24 dp sont centrées dans 48 dp).

## 6. Iconographie

Icônes de 24 dp « gravées » : contour de 1,5 dp à bouts ronds, plus 2 à 4 hachures de 0,75 dp côté ombre (bas droit), pas de remplissage sauf un point de `garance` (sceau de la lettre, tête d'épingle). Jeu dessiné dans `gravure.js` : cabinet (Collection), réglages, Académie (fronton), TLFi (livre ouvert), Envoyer (enveloppe et cachet, remplace « Partager »), Au hasard (dé), épingle (favori, remplace l'étoile), loupe. Les libellés restent sous les icônes de l'accueil.

## 7. Motifs

| Motif | Où | Règle |
|---|---|---|
| Planche (papier, double filet) | conteneur de tout contenu | une planche par écran, deux au plus |
| Monture tenue par 4 coins kraft | carte du mot | la monture prend la palette du mot |
| Étiquette d'herbier (en-tête Courier, filet, note manuscrite, épingle) | exemple du mot, widget | toujours en bas à droite de la planche, comme dans un vrai herbier |
| Spécimen gravé | un par planche, à côté de l'étiquette ; écran de verrouillage | hachures parallèles, demi-face dense (pas 2,4 unités) et demi-face claire (pas 7) ; jamais derrière du texte |
| Tampon « HERBIER DE M. GLOSE » | coin de l'étiquette | `garance` à 72 %, tête de chat au trait avec lunettes ; seule apparition du chat en version gravée |
| Étiquette à œillet | réponses du quiz | pointe à gauche, œillet laiton, bout de ficelle kraft |
| Cabinet à tiroirs | Collection | porte-étiquette laiton par tiroir, compteur en dessous ; tiroir ouvert = plateau à cases |
| Grain de papier | fond de fenêtre | bruit à 5 % le jour, 7 % la nuit ; aucun autre effet de vieillissement |

Interdits : taches de café, déchirures, brûlures, bords « parchemin », insectes épinglés (une épingle ne pique que du papier), plus d'un spécimen gravé par écran.

## 8. Mouvement

Toutes les durées passent par `ValueAnimator.areAnimatorsEnabled()` et l'échelle d'animation du système.

- **Cueillette du matin** (première ouverture du jour) : une feuille de papier cristal (glassine, blanc à 55 %) couvre la monture puis glisse vers le haut (translationY, 420 ms, décélération) ; la monture se pose (échelle 1,02 vers 1, 240 ms).
- **Loupe** : au toucher, Glose fait glisser la lentille de son œil vers le mot puis la ramène (900 ms en tout, une fois). Sinon Glose garde la respiration actuelle, arrêtée après environ 5 s.
- **Épingler en favori** : l'épingle tombe de 8 dp avec un léger rebond (180 ms) et un retour haptique court.
- **Quiz juste** : coche tracée à l'encre (`trimPathEnd` 0 vers 1, 300 ms), l'étiquette se teinte en `juste`.
- **Quiz faux** : l'étiquette choisie se balance doucement autour de son œillet (rotation ±3°, deux allers, 400 ms) : un pendule, pas un tremblement. La bonne étiquette se teinte, la note « Presque ! » s'écrit.
- **Cabinet** : le tiroir s'ouvre (le plateau glisse de 24 dp et s'opacifie, 260 ms) ; un nouvel objet arrive avec la chute de son épingle.

## 9. Glose dans l'Herbier

- Accueil : pose choisie dans la collection, 150 dp, perchée au-dessus de la planche ; une **loupe à pied** en laiton posée à côté, dont la lentille grossit son œil (dessin de Glose dessiné deux fois, la seconde à l'échelle 1,4 dans un cercle de découpe).
- Quiz : tête de 108 dp, une loupe à main devant l'œil droit. Elle reste immobile pendant la question.
- Cabinet : la tête dépasse du haut du meuble, les deux pattes posées sur la corniche.
- Fin de quiz : poses « le fil écrit » existantes, inchangées.
- Écran de verrouillage : **pas de Glose**, conformément à la décision actuelle. Le tampon gravé reste réservé à l'appli.
- Glose n'est jamais gravé ni sépia (sauf la silhouette d'un objet à trouver) : son trait épais et ses couleurs le séparent du décor et gardent le côté mignon.

## 10. Palettes par mot

Les 18 palettes de `styles.json` restent inchangées et gardent leur rôle : elles teignent la **monture** du mot dans l'appli et le **fond entier** de l'écran de verrouillage. Le champ `nature` sert aussi de couleur au spécimen gravé et au filet de la monture. Chaque palette choisit un spécimen gravé (correspondance fixe, sans nouvelle donnée dans `words.json`) :

| Palette | Spécimen | Palette | Spécimen |
|---|---|---|---|
| Encre de Chine | noix de galle (l'encre ferro-gallique) | Parchemin | épi de blé |
| Bleu nuit | pavot | Sépia | os de seiche (d'où vient la sépia) |
| Ardoise | lichen | Sanguine | églantine |
| Vert-de-gris | ammonite | Safran | crocus |
| Lie-de-vin | vrille de vigne | Lavande | brin de lavande |
| Braise | chêne roussi et gland (maquette) | Givre | rameau givré |
| Sous-bois | fougère | Aube | coquelicot |
| Violine | aconit | Pistache | pois de senteur |
| Abysses | corail | Craie | nautile |

Lisibilité : les couleurs de texte des palettes ne changent pas, donc leurs contrastes sur leur propre fond non plus (Braise : mot 8,9, définition 11,7, exemple 7,5, nature 6,0). Quand la monture se fond dans la planche (palettes claires le jour : 1,02 à 1,28 ; palettes sombres la nuit : 1,04 à 1,77), on trace un filet de 1 dp en `nature`, qui atteint 5,5 à 9,4 contre la planche, plus les coins kraft. Le spécimen gravé est à 18 % d'opacité au plus dans la monture, 42 % sur l'écran de verrouillage, toujours hors du rectangle du texte.

## 11. Carte de l'écran de verrouillage

Rendu par `WordCard.render` sur le dégradé `fond_haut` vers `fond_bas`, grain à 6 % :
- haut (45 %) calme : rien sous l'horloge ;
- bande de 36 à 55 % : spécimen gravé de la palette, à droite, couleur `nature`, 42 % ;
- de 56 à 88 % : **étiquette de spécimen** à double filet `nature` (55 % et 40 %) et quatre coins pleins `nature` ; ligne de cote en Courier Prime « N° 412 » à gauche et « CUEILLI LE 6 NOV. » à droite, puis mot, nature, définition, exemple ;
- bas (12 %) nu pour les raccourcis du système.
La mesure de contraste et le voile adaptatif déjà proposés pour la photo personnelle s'appliquent tels quels : l'étiquette devient une monture pleine si le fond est une photo.

## 12. Widget

Widget 4 × 2 « étiquette du jour » : une étiquette d'herbier rendue en bitmap par `WordCard` (les RemoteViews n'acceptent pas les polices de l'appli) sur fond `planche`, avec une épingle `garance` en haut, la cote « N° 412 · 6 nov. », le mot en grand, la nature, une définition coupée à deux lignes et le tampon en bas à droite. Version nuit sur `planche` nuit. Un toucher ouvre la planche du jour. Variante 2 × 2 : le mot seul et sa cote, comme une étiquette de tiroir.

## 13. Idée de capture pour le store

Image 1, « Un mot rare, cueilli chaque matin » : vue de dessus d'une vraie planche d'herbier (papier `papier`, double filet) sur laquelle le téléphone est **monté avec quatre coins kraft**, comme un spécimen. L'écran montre l'écran de verrouillage Braise. À droite, l'étiquette d'herbier imprimée porte les annotations à la main en `prusse` : une flèche « votre écran de verrouillage », une autre « gratuit, hors ligne ». Les images suivantes reprennent la même planche : le quiz avec les étiquettes à œillet, le cabinet ouvert avec Glose qui dépasse, puis une mosaïque de six montures de palettes avec leurs spécimens gravés. Même mot sur les trois premières images (estramaçon).

## 14. Mise en œuvre (Views de la plateforme, dessins vectoriels, sans AndroidX)

- **Jetons** : `colors.xml` et `values-night/colors.xml` avec les noms ci-dessus ; les couleurs en dur deviennent des attributs de thème là où le code les pose.
- **Polices** : `res/font/eb_garamond_italic.ttf` ajouté ; une `font-family` XML (`eb_garamond_famille.xml`) qui associe romain et italique (API 26 pour les familles XML ; sinon `Typeface.create` à la main). `TextAppearance` dédiées : `Cote`, `Note`, `Titre`.
- **Grain** : une tuile de bruit 64 × 64 générée au premier lancement (`Bitmap`, 4 Ko) en `BitmapDrawable` répété, dans un `layer-list` de `windowBackground`.
- **Planche** : `layer-list` de deux `shape` à contour (insets 5 et 9 dp). Coins de montage : quatre `vector` de 14 dp dans le même `layer-list` avec `android:gravity` (API 23).
- **Filet de monture** : contour 1 dp en `nature`, posé dans `WordCard` quand `Styles.needsEdge` est vrai (logique déjà présente).
- **Spécimens gravés** : 18 `vector` exportés par un script Node (paper.js est déjà dans `design/mascotte/node_modules`) qui **pré-découpe** les hachures (opérations booléennes hors appareil) : aucun `clip-path` à l'exécution, 150 à 300 segments par dessin, 6 à 12 Ko de XML chacun.
- **Étiquette à œillet** : une petite classe `TagDrawable : Drawable()` (un `Path` avec pointe, un cercle pour l'œillet) posée en fond des boutons de réponse, avec états par `setTint`.
- **Loupe** : `GloseView.onDraw` dessine le `Drawable` de Glose, puis `canvas.clipPath(cercle)`, `canvas.scale(1,4)` et le redessine ; la monture de la loupe est un `vector`.
- **Cabinet** : `GridLayout` de tiroirs (fond `layer-list`, porte-étiquette en `shape` à contour `laiton`) ; le tiroir ouvert affiche un `GridLayout` de cases ; animation par `ViewPropertyAnimator`.
- **Écran de verrouillage et widget** : dessin sur `Canvas` dans `WordCard.render` (rectangles de filets, `Path` des coins, `VectorDrawable.draw` du spécimen avec `setAlpha`).

### Effort estimé

| Lot | Effort |
|---|---|
| Jetons jour et nuit, police italique, styles de texte | 1 j |
| Grain, planche, monture, coins, filets | 1,5 j |
| Accueil (étiquette, épingle favori, icônes gravées) | 1,5 j |
| Quiz (étiquettes à œillet, pendule, coche tracée) | 1 j |
| Cabinet (tiroirs, plateau, animations) | 2 à 3 j |
| Loupe de Glose (accueil et quiz) | 0,5 j |
| Écran de verrouillage et widget | 1,5 j |
| 18 spécimens gravés (dessin et script d'export) | 3 à 5 j, surtout du dessin |
| QA jour, nuit, police × 2,0, TalkBack, contrastes | 1,5 j |
| **Total** | **13 à 17 j** ; un premier lot sans les 18 gravures (une seule, réutilisée) tient en 8 j |

## 15. Risques et garde-fous

- **Kitsch** : l'empilement papier ancien, épingles, tampons, manuscrite, gravures bascule vite dans le décor de brocante. Garde-fous : un seul spécimen gravé par écran, un seul tampon, grain à 5 % au plus, aucun effet de vieillissement, la note manuscrite limitée à une ligne par écran.
- **Lisibilité sur textures** : aucune hachure ni grain sous un bloc de texte ; les spécimens restent hors du rectangle du texte (vérifié sur les maquettes) ; les contrastes ci-dessus sont mesurés sans grain ; l'effet du grain (5 % d'alpha, valeurs moyennes proches du papier) est faible mais reste à mesurer sur captures d'appareil.
- **Écriture manuscrite** : Dancing Script est moins lisible (dyslexie, malvoyance) : jamais seule porteuse d'information, 19 sp au moins, désactivable si l'on ajoute un réglage « texte simple ».
- **Petites tailles et moiré** : hachures à 2 dp d'écart au moins, traits de 0,6 dp au moins ; à la police × 2,0, l'étiquette passe sous la monture en pleine largeur et le spécimen se cache.
- **Choc de styles** : Glose épais et coloré contre un décor au trait fin. C'est voulu (le vivant contre l'archive), mais il ne faut pas « graver » Glose ni le mettre en sépia, sinon il perd son charme.
- **Connotation de l'herbier** : une collection de choses séchées, voire d'insectes épinglés. Le vocabulaire reste doux (« cueilli », « à cueillir », « porté ») et les épingles ne piquent que du papier.
- **Sombre** : un vrai papier n'existe pas la nuit ; la nuit est donc un cabinet en bois à la lampe, pas un papier inversé.
- **Coût de dessin** : 18 gravures de qualité sont le poste le plus cher ; on peut livrer avec une seule gravure par famille (6 familles de palettes) puis compléter.
