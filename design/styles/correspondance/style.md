# Direction visuelle « Correspondance »

Rendus : `board.png` (planche), `home_light.png`, `home_dark.png`, `quiz.png`, `collection.png`, `lockscreen.png`. Source : `screens.src.html` ; `node design/styles/correspondance/render.js` reconstruit `screens.html` (Glose en SVG inline) et les PNG (Playwright, facteur 2).

## 1. Concept

Chaque matin, Monsieur Glose vous écrit : le mot du jour arrive comme une carte postale, avec un timbre aux couleurs du mot et un cachet daté.
L'exemple est recopié à l'encre bleue dans un carnet à lignes Seyès ; le soir, il vous pose sa question sur papier à lettres, et vous collez ses timbres dans un album.
Un seul fil de laine, celui de sa pelote, part de Glose, descend la marge de chaque écran et relie la carte, le carnet et le sceau du quiz.

## 2. Pourquoi c'est unique

| Appli | Ce qu'elle montre (audit `research_notes/Audit Proust/design.md` pour Proust ; captures de store pour les autres) | Ce que Correspondance fait autrement |
|---|---|---|
| Proust | Trois aplats (marine, pêche, crème), cartes blanches plates, mascotte posée à côté de l'interface, flamme de série et chiffres. | Pas d'aplat ni de carte générique : chaque surface est un **objet de papier** (carte, carnet, enveloppe, album). La mascotte n'est pas décorative, elle est l'**expéditeur**, et son fil structure la mise en page. Pas de flamme ni de score : on reçoit, on ne performe pas. |
| Vocabulary | Photo plein écran, sans-serif, mot centré, glisser pour passer. Joli mais interchangeable, aucun personnage, aucune trace du temps. | Un **rituel daté** (cachet du jour, « Lettre du 6 novembre »), un carnet qui se remplit, des timbres qui se collectionnent : le temps devient visible et doux. |
| 1 Jour 1 Mot | Carte de définition sobre, ton dictionnaire. | Même sérieux du contenu (Académie, TLFi), mais mis en scène comme une lettre personnelle, avec la malice d'un bibliothécaire qui annote la marge. |

Le registre « courrier, cahier d'écolier, kraft » est très français (cahier Seyès, carte postale, cachet de cire) sans être une marque : aucun jaune ni logo postal, aucune Marianne, le cachet dit « DÉFINIDAY ». Aucun concurrent observé n'utilise la métaphore épistolaire, ni un trait continu qui relie les écrans.

## 3. Couleurs

Ratios WCAG 2.x calculés (formule de luminance relative, script `wcag.py` de ce dossier, une paire « nom hex nom hex » par ligne sur stdin). Texte normal : 4,5:1 ; grand texte et graphiques utiles : 3:1.

### Jour

| Jeton | Hex | Rôle | Contraste |
|---|---|---|---|
| `papier` | #F8F3E9 | fond de fenêtre (remplace `bg`) | |
| `feuillet` | #FFFCF5 | carnet, lettre du quiz, liseré de la carte | 1,08 sur papier (bord obligatoire) |
| `kraft` | #EBDDC2 | enveloppes, réponses, page d'album, boutons ronds (remplace `surface`) | |
| `kraft_fonce` | #C4A274 | rabat d'enveloppe, contour secondaire, creux d'album | 2,17 sur papier, 1,79 sur kraft (décor) |
| `bord` | #D9C7A4 | filet de 1 dp autour des papiers (remplace `card_edge`) | 1,50 sur papier |
| `encre` | #1E2236 | texte principal, noir bleuté d'encre | 14,2 papier ; 11,7 kraft ; 15,3 feuillet ; 10,1 sur ligne Seyès forte |
| `encre2` | #4E4A5E | texte secondaire, état verrouillé | 7,7 papier ; 6,4 kraft |
| `plume` | #2C4A9A | accent (remplace l'or `#886A34`), mentions postales, exemple recopié, bouton principal | 7,4 papier ; 6,1 kraft ; 8,0 feuillet ; 5,3 sur ligne forte ; papier sur plume 7,4 |
| `cire` | #A23A33 | sceau, tampon « PORTÉ », point de l'enveloppe choisie | 5,95 papier ; 4,9 kraft |
| `fil` | #9A4250 | brin sombre du fil (= `gloseBall` du dessin) | 5,8 papier ; 4,8 kraft |
| `fil_clair` | #D88A97 | brin clair torsadé du fil | décor |
| `fil_note` | #86364A | annotations manuscrites de Glose | 7,2 papier ; 5,9 kraft ; 5,1 sur ligne forte |
| `seyes` | #DFE7F5 | lignes fines du carnet | 1,21 sur feuillet |
| `seyes_fort` | #BFD0EC | ligne de base du carnet | 1,52 sur feuillet |

### Nuit (« correspondance à la bougie »)

| Jeton | Hex | Contraste |
|---|---|---|
| `papier` | #141829 | |
| `feuillet` | #1B2136 | 1,10 sur papier (bord obligatoire) |
| `kraft` | #2E2719 | kraft de nuit, brun tabac |
| `kraft_fonce` | #6B5A3C | 2,64 papier ; 2,22 kraft |
| `bord` | #3A4566 | 1,86 papier |
| `encre` | #F0E8D8 | 14,5 papier ; 12,1 kraft ; 8,2 sur ligne forte |
| `encre2` | #BEB4A3 | 8,6 papier ; 7,2 kraft ; 4,9 sur ligne forte |
| `plume` | #A3B8F0 | 9,0 papier ; 7,5 kraft ; 8,1 feuillet ; 5,1 ligne forte ; papier sur plume 8,95 |
| `cire` | #C8574C | 4,1 papier ; 3,5 kraft (graphique, plus de 3:1) |
| `fil` | #B4596C | 3,85 papier |
| `fil_clair` / `fil_note` | #EBA5B2 | annotations : 8,9 papier ; 8,0 feuillet ; 5,0 sur ligne forte |
| `seyes` | #232A43 | 1,13 sur feuillet |
| `seyes_fort` | #34416A | 1,60 sur feuillet |

`quiz_good` et `quiz_bad` (sauge et terre cuite, déjà validés à 10,3 et 9,8:1) restent tels quels.

## 4. Typographie

Toutes les polices sont déjà dans `app/src/main/res/font/` (OFL) : **aucune nouvelle police, 0 Ko**.

| Rôle | Police | Taille (sp) / interligne | Notes |
|---|---|---|---|
| Mot du jour | police du mot (`styles.json`), règle des 8 lettres inchangée | 39 carte, 0,12 × largeur sur l'écran verrouillé | « estramaçon » (10 lettres) tombe sur Tenor Sans comme aujourd'hui |
| Initiale du timbre | police **suggérée** du mot, même décorative | 30 à 34 | une seule lettre : la fraktur d'« estramaçon » revient sur le timbre, sans gêner la lecture |
| Titres d'écran | Libre Baskerville Bold | 20 / 26 | « Une lettre de Glose », « L'album de Glose » |
| Question du quiz | Libre Baskerville Bold | 22 / 30 | guillemets français avec espaces insécables |
| Définition, corps | Libre Baskerville Regular | 15 / 23 (carte), 16 / 25 (écran verrouillé) | |
| Exemple | Libre Baskerville Regular en `plume` | 15 / 24 (pas Seyès) | pas d'italique : le fichier n'a pas de coupe italique, on évite l'italique synthétique ; c'est la couleur d'encre qui signale la citation |
| Mentions postales | Courier Prime Bold, capitales, interlettrage 0,13 em | 11,5 | « MOT DU JOUR », « QUESTION 2 SUR 3 » : au plus 4 mots |
| Libellés d'actions | Courier Prime Regular | 11,5 | sous les icônes de la carte |
| Annotations de Glose | Dancing Script SemiBold en `fil_note` | 17 à 21 (16 minimum) | jamais d'information unique : tout ce qui y figure est redondant ou accessoire |
| Boutons | Libre Baskerville Bold | 16,5 | |

Si l'on veut une main plus « écolier » que Dancing Script, Caveat (Google Fonts, OFL, environ 400 Ko en variable) serait le seul ajout justifiable, réservé aux annotations. Pas nécessaire pour livrer.

## 5. Espacements et rayons

- Grille de 4 dp ; gouttière 16 dp.
- **Colonne du fil** : le fil court à x = 36 dp ; le texte des papiers commence à 52 dp. C'est la marge rouge du cahier, remplacée par la laine de Glose.
- Carte postale : liseré `feuillet` de 5 dp, rayon extérieur 16 dp, intérieur 11 dp ; filet `bord` de 1 dp, ombre 6 dp floue à 16 %. Le liseré règle aussi le problème des cartes qui se fondent dans le fond (`Styles.needsEdge`) : il n'y a plus de cas limite.
- Carnet 10 dp, bouton principal 14 dp (ombre portée de 3 dp façon tampon encreur), enveloppes 6 dp, page d'album 8 dp côté reliure et 14 dp côté tranche.
- Timbre : pas de rayon, dents de perforation au pas de 8 dp (rayon de trou 2,5 dp) ; tailles multiples de 8 dp (56 × 64 sur la carte, 80 × 96 dans l'album, 24 × 30 au pas de 6 dp pour la progression du quiz).
- Cibles tactiles 48 dp partout (actions de la carte 69 × 58 dp).

## 6. Iconographie

Grille de 24 dp, trait de 1,75 dp, extrémités et angles arrondis, `currentColor`. Le vocabulaire suit la métaphore quand elle est claire :

- Album : un timbre (contour pointillé, vignette intérieure).
- Mes mots : une bannette à courrier.
- Partager devient **Envoyer** : enveloppe et flèche.
- Académie (colonnade), TLFi (livre ouvert), Hasard (dé), Réglages (curseurs), Retour, Garder (étoile).
- Case du quiz : carré légèrement tremblé, « dessiné à la plume ».

Les icônes restent lisibles seules ; la métaphore ne remplace jamais le libellé.

## 7. Motifs (et leur quota)

| Motif | Où | Règle |
|---|---|---|
| Fil de laine bicolore | marge de l'accueil, du quiz et de l'album ; soulignement du mot sur l'écran verrouillé | **un seul fil par écran**. Deux brins (`fil` 2,6 dp + `fil_clair` 1,1 dp en tirets 1,6/2,8) : sur n'importe quel fond, l'un des deux dépasse 3:1 (`fil` 4,7 à 6,0 sur les 9 palettes claires, `fil_clair` 3,7 à 6,8 sur les 9 sombres) |
| Timbre | coin de la carte, progression du quiz, album, fin de quiz | un groupe timbre + cachet par écran |
| Cachet daté | sur le timbre de la carte et de l'écran verrouillé | double anneau, date sur l'arc, 3 ondes d'oblitération ; couleur `nature` de la palette à 75 % ; décoratif (la date est aussi écrite en clair) |
| Sceau de cire | bouton « Quiz du soir », où le fil se noue | réservé à l'action du soir (croissant de lune en relief) |
| Seyès | carnet de l'exemple, lettre du quiz | jamais sur la carte du mot, jamais sur l'écran verrouillé |
| Enveloppe kraft | « Autres lettres du jour », widget | |
| Annotation manuscrite | une par écran au plus | 2 à 6 mots |

Pas de grain papier dans cette version : à ajouter seulement si les surfaces paraissent trop « plates » sur appareil (bitmap de 128 px à 4 %, environ 6 Ko).

## 8. Mouvement

Toutes les animations sont courtes, une seule fois par événement, et désactivées si `Settings.Global.ANIMATOR_DURATION_SCALE` vaut 0. Aucune boucle infinie (WCAG 2.2.2).

- **Arrivée de la lettre** (premier affichage du jour) : la carte monte de 24 dp avec fondu, 280 ms, `DecelerateInterpolator` ; puis le cachet se pose (échelle 1,15 vers 1, alpha 0 vers 0,8, 180 ms) avec `performHapticFeedback(CLOCK_TICK)`.
- **Le fil se déroule** : `trimPathEnd` de 0 à 1 en 600 ms le long de la marge, uniquement au premier lancement du jour.
- **Quiz, « réponse au dos »** : la réponse touchée se retourne comme une carte (`rotationY` 0 vers 90, changement de contenu, 90 vers 0 ; 2 × 160 ms, `cameraDistance` élevé). Le verso montre la correction en `plume`.
- **Fin de quiz** : le déballage des timbres existant (B4) garde sa cascade ; chaque timbre gagné reçoit le tampon « PORTÉ » ou le cachet du jour.
- **Appui** : le bouton principal s'enfonce de 2 dp (son ombre de tampon disparaît), 90 ms.

## 9. Monsieur Glose

- **Le coin de Glose** : en haut à gauche de chaque écran, assis sur son livre, 132 à 142 dp. Le fil sort du dessin (la fin du fil peint dans `glose_assis`, point 312,806 de la viewBox) et devient la marge. C'est la règle qui rend le système cohérent : Glose est l'origine de la ligne.
- Accueil : `glose_assis` et « Une lettre de Glose », annotation « rien que pour vous » ; le fil va jusqu'au sceau du quiz.
- Quiz : même pose, « Prenez votre temps, la poste n'est pas pressée. », « Bonsoir, » en tête de lettre, et l'option « Je donne ma langue au chat » (si l'on garde un bouton pour passer). Le fil finit en boucle lâche.
- Album : même pose ; le fil devient la cordelette de reliure qui passe dans les œillets de la page.
- Écran verrouillé : pas de dessin (zone de l'horloge calme), seulement son fil sous le mot et sa signature « Glose » en `nature`.
- Ton : bienveillant, jamais moqueur, jamais culpabilisant (« et rien ne presse » sous le compteur de l'album).

## 10. Palettes par mot (les 18 de `styles.json`)

La carte postale reste le seul endroit coloré par le mot, comme aujourd'hui ; l'interface autour est neutre (papier, kraft, encre).

| Élément | Couleur | Pire cas sur les 18 palettes |
|---|---|---|
| Face de la carte | dégradé `fond_haut` vers `fond_bas` | |
| Mot | `mot` | inchangé (déjà validé) |
| Mentions et nature | `nature` | 5,2:1 sur `fond_haut` (vert-de-gris) ; 4,6:1 sur `fond_bas` (sanguine, lavande, craie) |
| Définition, libellés d'actions | `texte` | 7,4:1 (vert-de-gris) |
| Exemple (écran verrouillé) | `exemple` | 5,5:1 |
| Timbre | fond `mot`, initiale en `fond_haut` | 7,5:1 (aube) |
| Cachet | `nature` à 75 % | décor |
| Fil | brins `fil` et `fil_clair` | voir le quota ci-dessus |

Les mots `evoque` peuvent plus tard choisir la vignette du timbre (plume pour parchemin, flocon pour givre), sans nouvelle donnée.

## 11. Carte de l'écran verrouillé

- Fond : le dégradé de la palette, plein écran, comme aujourd'hui.
- Zones : haut 45 % vide (horloge système) ; bloc de texte de 48 à 88 % ; bas 12 % nu (raccourcis système).
- Bloc, de haut en bas : « LETTRE DU 6 NOVEMBRE » en Courier Prime + timbre 48 × 56 dp avec cachet à droite ; le mot ; le fil qui entre par le bord gauche, ondule sous le mot et finit en boucle ; nature ; définition ; exemple entre guillemets ; signature « Glose ».
- Dessin : tout en `Canvas` dans le générateur existant (`WordCard.kt`) : `Path` du fil (deux `Paint`, dont une avec `DashPathEffect`), timbre par `Path.op(DIFFERENCE)` (rectangle moins cercles), cachet par `drawCircle` et `drawTextOnPath`. La boucle de réduction de taille existante s'applique avant le placement du fil.

## 12. Widget

Widget 4 × 2 « enveloppe » : fond kraft avec rabat en V (vector drawable), une carte qui dépasse de l'enveloppe avec le mot et le début de la définition (TextView `serif` de RemoteViews, comme aujourd'hui), timbre aux couleurs du mot en haut à droite. Le timbre et son initiale dans la police suggérée sont rendus en bitmap (`setImageViewBitmap`), comme Glose l'est déjà. En 2 × 2 : seulement le mot et le timbre. Nuit : kraft de nuit.

## 13. Idée de captures de store

Une **frise continue** : les 5 captures posées côte à côte sur un fond kraft, traversées par un seul fil de laine qui passe de l'une à l'autre (l'utilisateur qui fait défiler suit le fil). Légendes en Libre Baskerville, mentions en Courier Prime :

1. « Chaque matin, une lettre de Monsieur Glose » (accueil, enveloppe ouverte).
2. « Un mot rare sur votre écran verrouillé » (cadre **Android**, pas iPhone : défaut relevé chez Proust).
3. « Le soir, il vous pose une question » (quiz, Seyès).
4. « Collez ses timbres dans l'album » (album).
5. « Gratuit, sans compte, hors ligne » avec le sceau de cire.

## 14. Notes d'implémentation (Views de la plateforme, sans AndroidX)

| Pièce | Moyen | Effort |
|---|---|---|
| Jetons jour et nuit | `values/colors.xml` et `values-night/colors.xml` : renommer ou réaffecter `bg`, `surface`, `surface_off`, `accent`, `card_edge` ; ajouter `kraft_fonce`, `cire`, `cire_fonce`, `fil`, `fil_clair`, `fil_note`, `seyes`, `seyes_fort` | 0,5 j |
| Polices | `res/font` déjà présent ; styles `Label` (Courier Prime), `Title`/`Body` (Libre Baskerville), `Note` (Dancing Script) | 0,5 j |
| Fil | une `View` superposée au contenu du `ScrollView`, qui lit la position des ancres (Glose, carte, carnet, sceau) dans `onLayout` et dessine un `Path` à deux `Paint` ; suit la taille de police ×2,0 sans réglage | 1 j |
| Carte postale | `GradientDrawable` actuel dans un `LayerDrawable` avec liseré ; timbre en `Drawable` maison (`Path.op`) ; cachet en vector drawable + texte sur arc ; sceau en vector drawable (`?attr` de thème) | 1,5 j |
| Carnet Seyès | `Drawable` maison qui trace les lignes **à partir de `TextView.getLineBaseline`** : la ligne forte suit l'interligne réel, donc la taille de police | 0,5 j |
| Quiz (lettre, réponses kraft, retournement) | layouts + `ObjectAnimator` | 1 j |
| Album (bandes cristal, œillets, tampon « PORTÉ », creux) | `GridLayout` existant, nouveaux fonds | 1,5 j |
| Écran verrouillé | `Canvas` dans `WordCard.kt` | 1 à 1,5 j |
| Widget | vector drawables + bitmap du timbre | 0,5 j |
| Mouvement | `ObjectAnimator`, AVD `trimPath` | 1 j |
| QA (jour, nuit, police ×1,3 et ×2,0, 360 dp, TalkBack) | captures du pipeline démo existant | 1,5 j |
| **Total** | | **10 à 11 jours** |

`contentDescription` à `null` pour le fil, le cachet, le timbre et le sceau ; l'étiquette « Mot du jour » et la date restent du vrai texte.

## 15. Risques et parades

- **Encombrement.** Six motifs, c'est beaucoup. Parade : les quotas du point 7 (un fil, un groupe timbre et cachet, une surface Seyès, une annotation par écran) et la règle « le papier porte le décor, le texte reste nu » : jamais de motif derrière la définition.
- **Lisibilité des polices de caractère.** Courier Prime en capitales fatigue au-delà de 4 mots : réservé aux étiquettes. Dancing Script n'apporte jamais d'information unique et ne descend pas sous 16 sp. Libre Baskerville est large : prévoir la réduction automatique des mots longs (déjà en place sur l'écran verrouillé, à étendre à la carte).
- **Seyès et accessibilité.** Les lignes restent sous 1,6:1 face au papier pour ne pas concurrencer le texte, et tout texte posé dessus reste au-dessus de 4,5:1 **même sur le pixel de la ligne forte** (pire cas : `encre2` de nuit 4,9:1, `fil_note` de jour 5,1:1). Les lignes suivent l'interligne réel, donc la taille de police. Au-delà d'une police ×1,6, ou si « texte à contraste élevé » est actif, le carnet passe en fond uni.
- **Fil peu visible.** Sur les palettes sombres, le brin `fil` tombe à 1,5:1 (ardoise, vert-de-gris) : c'est le brin `fil_clair` qui porte la forme. Le fil est purement décoratif, son absence ne coûte rien.
- **Glose et la navigation.** Le coin de Glose voisine la flèche de retour : garder 8 dp entre les oreilles et la cible de 48 dp, et réduire Glose à 112 dp si la barre du haut grandit.
- **Ressemblance postale.** Éviter le jaune et le bleu d'un opérateur, tout logo, la Marianne ; les timbres ne portent ni pays ni valeur monétaire (« 1 MOT »).
- **Changement d'accent.** L'or actuel (`#886A34`) cède la place à `plume` ; l'or reste dans les lunettes de Glose et sur la pelote, où il est déjà chez lui.
