# Donner à Glose sa propre vie

## Synthèse : le charme est déjà dessiné, il manque la mise en scène

Definiday n'a pas besoin d'un nouveau style pour devenir plus fun et plus mignonne : Monsieur Glose coche déjà les critères documentés des marques cosy (silhouette lisible, gros yeux, trait épais, peu de formes), mais l'interface autour de lui reste générique et les moments forts (fin de quiz, déblocage, toucher) ne sont pas mis en scène. Le rendement est donc dans trois leviers. **Premier levier, la mise en scène** : retours d'appui, réactions du quiz, déballage des objets gagnés en timbres, Collection en album ; c'est du S à M en Views de la plateforme, sans dépendance. **Deuxième levier, une vie propre pour Glose** : micro-animations irrégulières, caresse au toucher, bulles contextuelles, réactions au mot du jour, nom choisi par l'utilisateur, le tout encadré par des règles anti-culpabilité testables. **Troisième levier, une collection déterministe** : 21 éléments existants, 14 de base à ajouter (35 au total, environ 30 en 60 soirs), puis un catalogue futur de 170 éléments en 18 collections, gagnés surtout par la mémoire différée et par un calendrier culturel qui revient chaque année. Côté fonds, la meilleure avenue est « ma photo en fond de carte » avec un voile adaptatif qui garantit le contraste. Elle répare aussi la perte structurelle du fond d'origine. Grain, vignette et motif lié à la palette du mot coûtent une demi-journée à trois jours. Deux préalables conditionnent le reste : arrêter la boucle infinie de Glose à l'accueil, probablement non conforme à WCAG 2.2.2 (niveau A), et préparer le pipeline (deux jetons de couleur, calques par emplacement avant le 20e accessoire, nouveaux compteurs). Le quart du catalogue futur (43 éléments sur 170) dépend de la révision espacée recommandée par le benchmark, qui n'est pas encore faite : les avenues fun ne remplacent pas ces priorités, elles en dépendent. Premières échéances : Avent en décembre 2026, Nuits de la lecture du 20 au 24 janvier 2027, Printemps des poètes et Semaine de la langue française en mars 2027.

Lecture. **[fait]** : tiré d'une source citée. **[dépôt]** : constaté dans le code, les ressources ou les captures de Definiday. **[inférence]** : déduction à partir des faits. **[proposition]** : idée de design non validée. Les efforts sont des ordres de grandeur pour un développeur qui connaît le code (j = jour). Ce rapport prolonge [Benchmark applis mot du jour](Benchmark%20applis%20mot%20du%20jour.md), sans le répéter ; ses numéros d'amélioration sont cités sous la forme « benchmark n° X ».

## Quatre règles cadrent tout ajout : surprise, bienveillance, accessibilité, plateforme

**Offrir au hasard, ne pas payer au score.** **[fait]** La méta-analyse de Deci, Koestner et Ryan (1999, 128 expériences) trouve que les récompenses tangibles **inattendues** n'affectent pas la motivation intrinsèque (9 études, d = 0,01), que les récompenses attendues et liées à la performance la diminuent (d de -0,28 à -0,40) et que les retours verbaux positifs l'augmentent (d = 0,33) ([Deci et al. 1999](https://depts.washington.edu/techdocs/papers/deciExtrinsicRewardsAndIntrinsicMotivation99.pdf)). **[inférence]** Une collection à seuils visibles est par nature une récompense attendue. On en limite le coût de trois façons : la lier à des actes d'apprentissage réels (rappel différé) plutôt qu'au sans-faute du soir ; la garder purement cosmétique ; ajouter en parallèle des surprises non conditionnelles (lettres de Glose, mot doré) et des félicitations verbales. Règle « pas de zéro » : une surprise ne tombe jamais sur « rien ».

**Jamais culpabiliser, jamais retenir.** **[fait]** Sur 1 200 adieux analysés dans les applis compagnons les plus téléchargées, 37 % utilisent une tactique de manipulation (culpabilité, peur de manquer, « retenue »). Ces adieux multiplient jusqu'à 14 l'engagement après l'au revoir, mais augmentent la manipulation perçue, l'intention de partir et le bouche-à-oreille négatif ([De Freitas et al., arXiv 2508.19258](https://arxiv.org/abs/2508.19258v3)). Pokémon Sleep a délibérément évité de récompenser un horaire de sommeil ([The Pokémon Company](https://corporate.pokemon.co.jp/en/topics/detail/t-9/)). Selon les critiques, Finch ne pénalise pas un jour manqué ([Engadget](https://www.engadget.com/apps/this-self-care-virtual-pet-is-helping-me-get-my-act-together-160027169.html)). Sur la rareté limitée dans le temps : « The entire point of FOMO is to drive up the value of content with that fear » ([Game Developer](https://www.gamedeveloper.com/design/how-video-games-abuse-the-fear-of-missing-out)). **[proposition]** Cinq règles vérifiables en revue : aucune réplique n'exprime un manque, une tristesse due à l'utilisateur ou une attente ; aucune ne parle de série en danger ; Glose ne dit jamais au revoir pour retenir ; aucune jauge ne baisse ; un objet « prêté » pour la journée n'est jamais « perdu » le lendemain. Les fenêtres saisonnières reviennent chaque année et durent au moins une semaine. Un objet hors fenêtre affiche « Revient en mars », jamais « Expiré ». Un test JUnit scanne les répliques contre une liste de mots interdits (« manqu », « attend », « triste », « série », « perdre »).

**Accessibilité d'abord, l'émotion ensuite.** **[fait]** WCAG 2.2.2 (niveau A) exige un moyen de mettre en pause une animation qui démarre seule, dure plus de 5 s et s'affiche à côté d'autres contenus ([W3C](https://www.w3.org/WAI/WCAG22/Understanding/pause-stop-hide.html)). Le texte doit atteindre 4,5:1, ou 3:1 en grand texte. Le contraste se mesure sur le fond réellement rendu et ne s'arrondit pas (4,499 échoue) ([W3C 1.4.3](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html)). Google a mené 46 études sur plus de 18 000 participants pour Material 3 Expressive : les éléments clés y sont repérés jusqu'à 4 fois plus vite. La même source note qu'une minorité forte préfère des designs plus calmes et que « No amount of emotion can compensate for a lack of clarity » ([Google Design](https://design.google/library/expressive-material-design-google-research)). **[dépôt]** La pose d'accueil boucle à l'infini (11 cibles `repeatCount="infinite"`, cycles de 3,8 à 6,4 s), et le toucher la relance au lieu de la mettre en pause (`MainActivity.kt:58,81`). **[inférence]** C'est probablement non conforme à 2.2.2 dès que les animations système sont actives. Toute nouvelle animation doit tester `ValueAnimator.areAnimatorsEnabled()`, comme le fait déjà `Glose.play()`. L'haptique et le son ne portent jamais seuls l'information juste/faux. TalkBack lit chaque objet gagné.

**Rester dans la plateforme.** **[dépôt]** Kotlin, Views de la plateforme, sans AndroidX, minSdk 26, aucune dépendance d'exécution, APK de release de 1 618 054 octets. Glose est produit par un pipeline Paper.js vers `AnimatedVectorDrawable` (AVD). **[fait]** Les AVD animent rotation, échelle et translation des groupes, ainsi que `pathData`, couleurs et `trimPath` des chemins. Depuis l'API 25, ils tournent sur le RenderThread et ne se synchronisent pas précisément avec le fil UI ([AnimatedVectorDrawable](https://developer.android.com/reference/android/graphics/drawable/AnimatedVectorDrawable)). Côté haptique, Android recommande `performHapticFeedback` et les constantes d'action, et préfère l'absence d'haptique à une vibration « buzzy » ([Haptics principles](https://developer.android.com/develop/ui/views/haptics/haptics-principles)). **[inférence]** Lottie (qui exigerait AndroidX selon des sources secondaires contradictoires sur son poids, de 68 Ko à plus de 300 Ko) et `DynamicAnimation` (AndroidX) sont à écarter : `OvershootInterpolator` ou un interpolateur de ressort d'une dizaine de lignes suffit. `CONFIRM` et `REJECT` sont en API 30, d'où un repli sur `CONTEXT_CLICK` ou rien en API 26 à 29 (à vérifier sur la référence).

## Look et style : l'interface autour de Glose reste générique

**[fait]** Chez Duolingo, toutes les formes sont arrondies, avec « the fewest details needed to get the point across ». Les personnages sont exagérés et posés dans un espace vide qui oriente l'œil vers l'interface. Le style ludique « makes learners less afraid of mistakes » ([Duolingo, Shape language](https://blog.duolingo.com/shape-language-duolingos-art-style/)). Les grandes célébrations sont réservées aux jalons, et leur timing a demandé plusieurs passes ([Duolingo, Streak milestone](https://blog.duolingo.com/streak-milestone-design-animation)). Les critiques attribuent le charme de Neko Atsume aux couleurs vives, au trait épais et au peu de détails ; le jeu a reçu le CEDEC Award 2015 ([Wikipedia](https://en.wikipedia.org/wiki/Neko_Atsume)). **[dépôt]** Definiday a une palette crème `#FAF7F0`, un accent or `#886A34` à 4,7:1 et des boutons à coins de 12 dp avec `stateListAnimator` à `@null` (aucun retour d'appui hors ripple). Les réglages sont une liste plate qui mêle Roboto et serif. Les éléments verrouillés de la Collection sont des silhouettes grises identiques. La 0.8.0 a déjà livré le bouton principal « Quiz du soir » et un seul Glose au déblocage, avec un objet par ligne.

**[inférence]** La règle « peu de formes, beaucoup d'air » plaide pour un ou deux objets signatures, pas pour du décor partout. Les motifs livresques restent mignons quand Glose peut les **toucher** (il tire le ruban, tamponne l'ex-libris), alors que taches d'encre et enluminures en coin basculent vite dans le kitsch. Le « light academia » crème et or est déjà la palette de jour, et le « dark academia » (bois, bougie) rejoint la nuit et le quiz du soir : il faut assumer ce que l'appli fait déjà. Un re-thème complet de l'interface coûte cher (couleurs `@color` en dur à passer en attributs de thème, double QA jour et nuit, audit de contraste) pour un gain non mesuré. Les applis de référence récompensent d'ailleurs la personnalisation de l'objet affectif, pas de l'interface : la chambre de l'oiseau chez Finch ([Finch help center](https://help.finchcare.com/hc/en-us/articles/37780134479757-Energy-vs-Rainbow-Stones)).

| # | Proposition | Effort | Garde-fou |
|---|---|---|---|
| A6 | Arrêter la boucle de Glose après environ 5 s ; un cycle au toucher, avec une petite réaction | 0,5 j | Conformité 2.2.2, batterie |
| A5 | `StateListAnimator` d'appui (échelle 0,97 en 80 ms) sur tous les `Btn` | 0,5 j | Neutralisé quand les animations sont coupées |
| A3 | Carte à 16-24 dp, boutons à 12-16 dp, puces en pilule | 0,5 j | Garder le contour de focus de 3 dp |
| B1 | Bonne réponse : « pop » (1 → 1,04 → 1, overshoot, 220 ms), coche tracée en `trimPath`, haptique courte, Glose qui hoche la tête | 1 j | Une seule impulsion |
| B2 | Mauvaise réponse : léger tremblement (±6 dp, 250 ms), Glose qui penche la tête, curieux, et bonne réponse révélée en sauge | 1 j | Jamais moqueur, pas de tremblement d'écran |
| B3 | Progression « 1 / 4 » en pelotes qui se remplissent | 0,5 j | `contentDescription` « 1 sur 4 » |
| B4 | Fin de quiz = déballage : chaque objet en vignette-timbre, en cascade, porté par Glose | 1-2 j | Au plus 3 vignettes, puis « + 2 dans la collection » |
| B5 | Confettis de papier (une `View`, un `ValueAnimator`, 1,2 s au plus) réservés aux déblocages et jalons | 1-2 j | Jamais à chaque quiz, pas de flash |
| B6 | Quiz réussi sans déblocage : un Glose « bravo » et une phrase, pas de confettis | inclus | Graduation des célébrations |
| A1 | Glose assis sur la carte (chevauchement de 8 à 12 dp), queue devant le bord | 0,5-2 j | Tester à la taille de police ×2,0 |
| A2 | Marque-page signature qui pend de la carte et bascule au toucher ; futur emplacement des favoris (benchmark n° 3) | 0,5 j | Ne pas l'empiler avec d'autres décors |
| A4 | Icônes de 3 à 5 formes dans le trait de Glose sur Collection et Nouveau mot ; Réglages en lien discret | 1 j | La hiérarchie Primary existe déjà |
| C1-C3 | Collection en album : cadres-timbres, tampon « porté », pelages verrouillés en couleur à 35 %, accessoires en pointillé « ? », petit saut à l'essayage | 2 j | Nom lisible à 4,5:1, pas de vignette pivotée |
| A7 | Nuit « lampe de lecture » : halo radial chaud derrière Glose | 0,5 j | Halo hors texte |
| A8 | Décor saisonnier près de Glose selon la date (feuille, flocon, fleur) | 0,5 j par saison | Statique et petit |
| A9 | Bandeau « notifications coupées » arrondi, avec Glose et une pancarte « Activer › » | 0,5 j | Jamais « Glose est triste » |
| D1-D4 | Réglages en cartes à icônes, une seule famille de police, petites capitales serif si `smcp` existe (non vérifié), pied de page Glose | 1-2 j | Rester calme : c'est l'écran de la minorité qui aime le calme |
| A10 | Grain papier en tuile de 2 à 5 Ko sur `windowBackground`, à 2-3 % de luminance | 0,5 j | A/B sur captures, à abandonner si on le remarque |
| D6 | Sons opt-in, désactivés par défaut (page, pelote, ronron) via `SoundPool` | 2 j + sons | Usage en public ; toujours couplés au visuel |
| D5 | Ambiances d'appli (« Bibliothèque », « Salon de thé ») débloquées par la présence | L | Pas en premier : A8 donne l'essentiel de l'effet |

## Fonds : la photo de l'utilisateur répare le fond perdu, le grain et les motifs font le reste

**[dépôt]** La carte est un dégradé à deux arrêts. Le bloc de texte finit à 84 % de la hauteur et en occupe au plus 40 %. Les 55 % du haut restent vides, car l'horloge système s'y affiche. Le mot fait 0,12 fois la largeur, la définition 0,05 et l'exemple 0,042 (130, 54 et 45 px sur 1 080 px). La carte est posée par `WallpaperManager.setBitmap(..., FLAG_LOCK)`. Les 18 palettes portent un drapeau `sombre` et une liste `evoque` (« nuit, rêve, étoiles… »), qui permet de choisir un motif par palette sans nouvelle donnée.

**[fait]** Bible Lock Screen pose chaque jour un verset sur une photo choisie, avec flou réglable, couleurs, ombre et polices. En contrepartie, sa v2.3 demande des permissions étendues sur le stockage ([F-Droid](https://f-droid.org/en/packages/com.daklok.biblelockscreen/)). Muzei floute et assombrit ses œuvres pour garder icônes et widgets lisibles ([GitHub](https://github.com/muzei/muzei)). Le texte « At a Glance » des Pixel devient illisible sur des fonds clairs ou chargés ([Android Central](https://www.androidcentral.com/phones/google-pixel/pixels-at-a-glance-just-got-easier-to-read-and-i-can-finally-use-my-favorite-wallpaper)). `MediaStore.ACTION_PICK_IMAGES` (API 33, ou R Extensions 2) donne un accès en lecture sans permission de stockage ([MediaStore](https://developer.android.com/reference/android/provider/MediaStore#ACTION_PICK_IMAGES)). Lire le fond en place exige en revanche `MANAGE_EXTERNAL_STORAGE` ([WallpaperManager](https://developer.android.com/reference/android/app/WallpaperManager)). Un fond animé ne peut être que proposé (`ACTION_CHANGE_LIVE_WALLPAPER`) : l'utilisateur le confirme dans le sélecteur système ([WallpaperManager](https://developer.android.com/reference/android/app/WallpaperManager)).

**[inférence]** Sur la carte, le mot (environ 50 dp) est du grand texte, soumis à 3:1. La définition et l'exemple ne le sont pas et doivent atteindre 4,5:1 : sur photo, l'exemple, plus pâle, échouera le premier. Le contraste se garantit en le mesurant. On réduit la zone sous le bloc (64 px de large), on calcule le contraste WCAG contre le 5e centile des pixels les plus défavorables, puis on monte un voile en dégradé de la couleur `fond_bas` par pas de 10 %, jusqu'à 85 %. Si le seuil n'est toujours pas atteint, une carte pleine arrondie prend le relais. Une ombre étroite sert de filet de sécurité. Le calcul se fait après la boucle de réduction de taille. Une seule vérification `assert` suffit : photo blanche + texte `encre_de_chine` donne au moins 4,5:1. On découpe trois zones : le haut (45 %) reste calme (motifs à 12 % d'alpha au plus, photo assombrie de 20 à 30 %) pour ne pas nuire à l'horloge ; le décor et l'éventuel Glose vont dans la bande de 45 à 60 % ou près du texte ; le bas (10 à 12 %) reste nu, car il porte les raccourcis système. Le fond animé est exclu : il remplace souvent aussi le fond d'accueil, coûte en batterie et ne se règle pas par code.

| Rang | Option (carte de verrouillage, sauf mention) | Effort | Intérêt |
|---|---|---|---|
| 1 | « Ma photo en fond de carte » (benchmark n° 10) : sélecteur de la plateforme, `ACTION_OPEN_DOCUMENT` sous l'API 33, copie unique réduite dans `filesDir`, flou bon marché par réduction et agrandissement, palette claire ou sombre choisie selon la luminance mesurée, plus un bouton « Rétablir l'écran de verrouillage » | 2-4 j | Le plus personnel ; répare le fond perdu ; aucune permission |
| 2 | Mesure de contraste + voile adaptatif + ombre étroite (requis par 1, utile à toutes les couches et à l'appli) | 1-2 j | « Lisible » devient une garantie |
| 3 | Grain papier (tuile de bruit générée, 4 à 8 % d'alpha) + vignette radiale, sur la carte et dans l'appli (3 à 5 %) | 0,5 j | Charme sans poids d'APK |
| 4 | Motif lié à la palette, hors du rectangle du texte, entre 6 et 12 % d'alpha et ancré sur `item.key` : constellation tirée du hachage du mot pour `bleu_nuit`, feuilles pour `sous_bois`, plumes pour `parchemin`, flocons pour `givre`, braises pour `braise` | 1-3 j | Change chaque jour et suit le mot |
| 5 | Table de glyphes du calendrier français (Chandeleur, muguet, Fête de la musique, Toussaint, neige) | 0,5 j par événement | Surprises hors ligne |
| 6 | Variante du soir : second rendu à l'heure du quiz, plus sombre, avec quelques étoiles ; **opt-in** | 0,5-1 j | Relie la carte au quiz du soir |
| 7 | Scènes illustrées saisonnières en vecteur (100 à 200 Ko de XML chacune), dans la bande de 45 à 60 % | M à L, surtout du dessin | Le plus mignon, après 3 à 5 |
| Option propriétaire | Petit Glose « signature » perché sur le bloc de texte (12 à 16 % de la largeur), et `glose_soir` dans la variante du soir. **Contraire à la décision « pas de mascotte sur l'écran de verrouillage »** : à présenter seulement comme réglage opt-in, désactivé par défaut, si le propriétaire rouvre la question. Le nom personnalisé n'irait jamais sur la carte | 0,5 j | Marqueur de marque ; risque d'encombrement |

Dans l'appli, les mêmes couches se réutilisent. **[proposition]** Grain sur le fond de fenêtre ; teinte du jour (6 à 10 % de `fond_haut` mêlé au crème, avec contrôle de contraste) ; motif à 4-6 % dans la marge basse ; accessoire saisonnier sur Glose selon la date. S'y ajoute un bord de 1 dp de la couleur `nature` quand une carte claire descend sous 3:1 contre le fond, ce qui corrige le 1,02:1 encore ouvert (benchmark n° 6). Pas de photo en fond d'appli : elle nuirait à la lisibilité des écrans denses. Le mot écrit sur la carte reste soumis à la règle déjà décidée : polices décoratives seulement pour les mots de 8 lettres au plus.

## Glose gagne une vie propre, une voix et un nom choisi

**[fait]** Les compagnons appréciés sans culpabilité ont une vie propre. Les chats de Neko Atsume viennent quand l'appli est fermée et laissent des cadeaux ([nippon.com](https://nippon.com/en/nipponblog/m00083)). Ils célèbrent sans punir l'absence (Finch, [Engadget](https://www.engadget.com/apps/this-self-care-virtual-pet-is-helping-me-get-my-act-together-160027169.html)) et réagissent à de petits gestes : Bongo Cat, un chat qui tape à chaque frappe et rien d'autre, a dépassé 100 000 joueurs simultanés sur Steam ([Windows Central](https://www.windowscentral.com/gaming/this-free-steam-game-is-being-played-more-than-stellar-blade-and-elden-ring-nightreign-even-though-its-just-a-cat)). Clippy a échoué par l'interruption à contretemps ([Mental Floss](https://www.mentalfloss.com/article/504767/tragic-life-clippy-worlds-most-hated-virtual-assistant), secondaire). **[dépôt]** Le dessin garde des groupes nommés animables (`tete`, `queue`, `paupieres`, `pelote`, `brin`, `lunettes`, `reflet`, `corps`, `pattes`, `livre`, `marque_page`…). Il n'existe pas de groupe `oreille` : un frémissement d'oreille demande de le nommer dans `pose_*.js` et `svg2avd.js`.

**[inférence]** Le modèle à suivre : Neko Atsume et Finch, sans compteur angoissant. Glose est un bibliothécaire qui lit, dort et joue, et qu'on retrouve ; on ne s'occupe pas de lui comme d'un animal à nourrir, et il n'a aucune jauge de faim ni d'humeur. Il ne parle qu'à l'ouverture ou quand on le touche, jamais par-dessus une définition ou un quiz en cours. Matin et soir, on **décore** l'heure, on ne la **récompense** pas.

| # | Interaction | Implémentation | Effort |
|---|---|---|---|
| 1 | Vie au repos irrégulière : clignement, double clignement, queue, reflet des lunettes, bâillement rare | Durées premières entre elles dans le générateur (paupières 4,7 s, queue 7,3 s, reflet 11 s, bâillement 29 s) : aucun code Kotlin. **À concilier avec A6** (voir décisions) | 0,5 j |
| 2 | Toucher gradué : tap = clin d'œil et tête qui tourne ; appui long = caresse, yeux fermés et « rrr… » ; haptique légère | AVD one-shot sur le même VectorDrawable, retour à la boucle de repos par `registerAnimationCallback` | 1 j |
| 3 | Bulle de réplique contextuelle (matin, soir, premier lancement, retour, palier, déblocage) | `TextView` arrondi, fonction pure `Repliques.pick` + sac mélangé dans `Prefs`, au plus une bulle par ouverture et pas à chaque fois (1 sur 2), disparition au toucher ou après 6 s | 1,5 j + écriture |
| 4 | Réaction au mot : interjection dite dans la bulle (« Saperlipopette ! ») ; `givre` = écharpe prêtée pour la journée ; `braise` = « Ça sent le roussi… » ; anecdote = « Psst, il y a une histoire derrière ce mot » | `Map` palette → réaction (6 à 8 palettes d'abord) ; accessoire prêté par surcouche `Glose.Acc.*`, sans déblocage | 1 j |
| 5 | Nom personnalisable (ci-dessous) | Champ dans la Collection, chaînes paramétrées | 1 j |
| 6 | Rituel du soir : après le quiz, un tap souffle la bougie de la tasse | One-shot sur `glose_soir` | 0,5 j |
| 7 | Rituel du matin (avant 11 h) : étirement, bâillement, « J'ai réchauffé le mot du jour » | One-shot puis boucle de repos | 0,5-1 j |
| 8 | Retour après au moins 3 jours : Glose sur une pile plus haute, « J'ai avancé dans mon roman. Et vous, quoi de neuf ? » | Pose `livres` existante, date de dernière ouverture | 0,25 j |
| 9 | Le fil écrit le nom choisi | `Paint.getTextPath` + `PathMeasure` animé. **Conflit** avec la règle des 8 lettres pour les polices décoratives, et le fil ne sait écrire que d'un trait (sans i, j ni accent) : limiter aux noms courts compatibles, sinon écrire l'initiale | 1,5-2 j |
| 10 | Ronron sonore, coupé par défaut | `SoundPool`, un OGG d'environ 1 s, jamais en notification | 0,5 j |
| 11 | Coin de Glose : un dos de livre tous les N mots | Recouvre la « bibliothèque de Glose » du brainstorm : n'en garder qu'une | 2 j |
| 12 | Mini-dialogue à deux choix | **À éviter** : risque Clippy, et incite à rester | - |

Règles transverses **[proposition]** : une seule animation « événement » à la fois. Animations coupées : image fixe, mais la bulle et l'haptique restent. Aucune nouvelle notification ; celle du soir peut varier son libellé dans la voix de Glose. Pour TalkBack, la `contentDescription` de Glose donne son nom et ce qu'il fait ; la bulle est un `TextView` normal, pas une région « live ».

**La voix.** **[fait]** Le guide de Duolingo insiste sur la cohérence du personnage, tout en assumant que Duo culpabilise ([design.duolingo.com](https://design.duolingo.com/writing/duo), extrait du moteur), un trait que Definiday rejette. **[proposition]** Glose est un vieux bibliothécaire tendre et un peu pédant, qui rit de lui-même. Il vouvoie, comme les chaînes actuelles. Ses phrases font 70 caractères au plus. Son humour de lettré (La Fontaine, Proust, Perec, Queneau, Colette) évite les citations longues. Après une erreur au quiz, il parle de lui ou du mot (« Ce mot m'a eu aussi, en 1987. »). **[inférence]** Avec une bulle une ouverture sur deux, 15 répliques par tranche horaire tiennent environ un mois sans répétition. Il faut donc 80 à 100 répliques au lancement : matin 15, après-midi 12, soir 15, caresse 10, retour 8, paliers 6, déblocages 6, les 15 interjections, et 6 à 8 palettes × 2.

**Le nom personnalisable.** **[fait]** Selon le résumé de Stoner, Loken et Blank (2018), nommer un produit renforce le sentiment de propriété et son évaluation. Le PDF n'a pas pu être lu : tailles d'effet et limites non vérifiées, et l'étude porte sur des produits, pas sur des mascottes ([Stoner et al.](https://carlsonschool-d8.prd.umn.edu/sites/carlsonschool.umn.edu/files/2020-03/Stoner_et_al-2018-Journal_of_Consumer_Psychology.pdf)). L'OQLF pose qu'on élide devant une voyelle ou un h muet (« d'Isabelle »), que l'usage tolère « de Anouk » devant un prénom court ou d'allure étrangère, et qu'on n'élide ni devant un h aspiré ni devant une initiale ([OQLF](https://vitrinelinguistique.oqlf.gouv.qc.ca/23685/lorthographe/elision-et-apostrophe/elision-avec-un-nom-propre)). **[proposition]** Le renommage se fait en tête de la Collection : le titre devient le nom, touchable, avec un crayon, via un `AlertDialog` et un `EditText` de la plateforme. Une invitation unique et ignorable suit la réaction au premier quiz : « Il répond à Monsieur Glose, mais il accepte les surnoms. » Pas d'étape dans l'onboarding (benchmark n° 4), qui doit rester court. Validation : `trim()`, de 1 à 24 caractères (les notes de style proposent 20), sans caractère de contrôle ni retour à la ligne ; un nom vide revient au défaut, et un bouton « Revenir à Monsieur Glose » est prévu. À tester à la police ×2,0 et dans la notification.

Deux problèmes de grammaire, réglés une fois par l'écriture plutôt que par du code. **Le genre** : « Monsieur » fixe le masculin, mais un nom choisi (« Ophélie ») ne le fait plus. On écrit donc sans accord : sujet nommé + verbe, groupes nominaux pour les objets, répliques à la première personne sans adjectif accordé au locuteur (« J'ai sommeil » plutôt que « Je suis fatigué »). **L'élision** : on évite « de %1$s ». S'il en reste un, une fonction `de(nom)` met « d' » devant une voyelle, accentuée ou non, et « de » sinon. Le cas du h reste à trancher (voir décisions). Un test JUnit de 5 lignes l'accompagne. Aucun nom d'objet ne contient « Glose », et les descriptions affichées non plus.

| Clé ou objet | Texte actuel **[dépôt]** | Réécriture **[proposition]** |
|---|---|---|
| `glose` | Monsieur Glose, chat bibliothécaire | %1$s, chat bibliothécaire |
| `glose_bravo` | Monsieur Glose écrit « bravo » avec le fil de sa pelote | %1$s écrit « bravo » avec le fil de sa pelote |
| `glose_encore` | Monsieur Glose écrit « encore » avec le fil de sa pelote | %1$s écrit « encore » avec le fil de sa pelote |
| `unlock_new` | Monsieur Glose a gagné : | %1$s a gagné : |
| `collection_open` | Collection de Monsieur Glose | La collection |
| `collection_title` | Collection de Monsieur Glose | %1$s (titre touchable) + sous-titre « Sa collection » |
| `quiz_empty` | … Lisez le mot du jour, Monsieur Glose vous attend ce soir. | … Lisez le mot du jour : ce soir, %1$s le fera écrire à sa pelote. (« vous attend » se lit comme une attente envers l'utilisateur) |
| `assis` | Assis sur son volume | Sur son volume |
| `soir` | Endormi dans sa tasse | Sieste dans la tasse |
| `reveil_tasse` (base) | Réveillé dans sa tasse | Réveil dans la tasse |
| `lecture`, `entre_livres`, `sur_la_lune` (futur) | Plongé dans sa lecture, Caché entre deux livres, Assis sur la lune | En pleine lecture, Entre deux livres, Sur la lune |
| Descriptions du catalogue | « yeux restant ceux de Glose », « dans le dos de Glose » | « ses yeux restent les siens », « dans son dos » |

## Une collection déterministe de 35 puis 205 éléments, gagnée par la mémoire

**[fait]** Les références cosy ont un grand catalogue fini, visible dès le départ, avec des conditions qui découlent du comportement voulu. Neko Atsume compte 86 chats, dont 26 rares qui ne viennent qu'avec certains objets ([Wikipedia](https://en.wikipedia.org/wiki/Neko_Atsume)). Le Centre communautaire de Stardew Valley a 6 salles et 30 paquets : chaque paquet complété donne une récompense, chaque salle une plus grande ([Stardew Valley Wiki](https://stardewvalleywiki.com/Bundles)). La première île de Pokémon Sleep compte 157 styles de sommeil. La rareté y est notée en étoiles, de 1 à 4 ou de 1 à 5 selon la version de la page ([Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Sleep_style), [Greengrass Isle](https://m.bulbapedia.bulbagarden.net/wiki/Greengrass_Isle)). Dans Animal Crossing, les objets du solstice d'été ont eu la même fenêtre (7 juin au 6 juillet) dans deux articles d'années différentes, ce qui suggère une récurrence ([Animal Crossing World](https://animalcrossingworld.com/2020/06/make-sure-to-get-your-summer-solstice-crown-winter-solstice-sweater-in-animal-crossing-new-horizons)). **[dépôt]** Definiday a 21 éléments en 4 types. Chacun dépend d'un seul compteur (`q` quiz, `p` sans-faute, `r` réussis, `s` série stricte) et d'un seuil. Rien n'est jamais retiré. Un élément verrouillé affiche ce qui manque (« Encore 3 soirs de quiz d'affilée »). Un seul accessoire se porte à la fois.

**[proposition]** La structure : **sans tirage, donc sans doublon ni boîte à butin**, car un tirage brouillerait le lien « j'ai appris, donc j'obtiens ». Quatre raretés nommées comme des éditions : ★ Poche, ★★ Broché, ★★★ Relié (scintillement possible), ★★★★ Édition originale (éclats animés, silhouette « ? » et indice poétique). Environ 18 collections de 3 à 17 éléments ; compléter une collection donne un cadre ou un décor (condition `e:`), à la manière des salles de Stardew. Les éléments saisonniers sont un bonus : ils ne sont jamais requis pour compléter une collection. Les silhouettes et l'indice restent. On ajoute en fin de quiz « Prochain : Monture écaille (encore 4 bonnes réponses) », sans jamais montrer plus de 3 objectifs, faute de quoi la liste devient une corvée. Rythme visé : environ 15 déblocages le premier mois, 10 le deuxième, 4 par mois du troisième au sixième, puis 2 ou 3 par mois plus les fêtes ; la complétion prend 2 à 3 ans à 1 mot par jour. Tout se gagne, sans monnaie, car une monnaie impose une économie à équilibrer. Enfin, les nouveaux déblocages évitent `p` (sans-faute du soir même), que le benchmark déconseille (benchmark n° 8).

**Base existante (21 éléments, [dépôt] `Unlocks.kt`).** Les lunettes dorées sont toujours portées et ne font pas partie de la collection. Au bout de 60 soirs, environ 17 éléments sur 21 sont obtenus.

| id | Nom | Type | Condition | Soir estimé (1 / 3 mots) |
|---|---|---|---|---|
| `assis` | Assis sur son volume | pose | défaut | 0 |
| `ecrit_mot` | Le fil écrit « mot » | pose | q≥1 | 1 |
| `ecrit_encore` | Le fil écrit « encore » | pose | q≥1 | 1 |
| `ecrit_bravo` | Le fil écrit « bravo » | pose | r≥1 | ~1 |
| `soir` | Endormi dans sa tasse | pose | s≥3 | 3 |
| `ecrit_dodo` | Le fil écrit « dodo » | pose | s≥3 | 3 |
| `livres` | Sur la pile de livres | pose | q≥10 | 10 |
| `ecrit_rare` | Le fil écrit « rare » | pose | r≥30 | ~35 |
| `tigre` | Gris tigré | pelage | défaut | 0 |
| `creme` | Crème caramel | pelage | p≥5 | ~6 / ~10 |
| `nuit` | Nuit d'encre (rare) | pelage | s≥30 | 30 |
| `enluminure` | Enluminure (rare) | pelage | r≥150 | ~175 |
| `bordeaux` | Pelote bordeaux | pelote | défaut | 0 |
| `moutarde` | Pelote moutarde | pelote | q≥5 | 5 |
| `sauge` | Pelote sauge | pelote | q≥20 | 20 |
| `rose` | Pelote rose poudré | pelote | p≥15 | ~19 / ~30 |
| `noeud` | Nœud papillon | acc. cou | p≥1 | ~1 / ~2 |
| `echarpe` | Écharpe tricotée | acc. cou | s≥7 | 7 |
| `bonnet` | Bonnet de nuit | acc. tête | s≥14 | 14 |
| `plume` | Plume d'oie | acc. tête | r≥60 | ~70 |
| `marque` | Marque-page à gland d'or | acc. livre | q≥45 | 45 |

**Base élargie (14 nouveaux éléments, [proposition]).** Ils n'utilisent que des compteurs existants ou déductibles (`c` bonnes réponses, `m` mots vus, `k` citations lues) et s'obtiennent tous avant 60 soirs. Cela fait 35 éléments, dont environ 30 au bout de 2 mois.

| id | Nom | Description | Emplacement | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|---|
| `roux` | Roux pain d'épices | Pelage orangé chaud, rayures cannelle, plastron crème. | pelage | P | q≥3 | S |
| `smoking` | Smoking | Chat noir et blanc : pelage anthracite (#4A4448, pas noir pur, pour garder le trait d'encre lisible), plastron et museau blancs. | pelage | B | q≥30 | S\* (rayures quasi invisibles) |
| `lavande` | Pelote lavande | Mauve gris, fil doux. | pelote | P | q≥8 | S |
| `bleu_prusse` | Pelote bleu de Prusse | Bleu profond des encres anciennes. | pelote | P | c≥25 | S |
| `vert_eau` | Pelote vert d'eau | Vert pâle et frais (le fil reste lisible grâce à son liseré d'encre). | pelote | B | r≥12 | S |
| `reliure_bordeaux` | Reliure bordeaux | Le volume sous Glose habillé de bordeaux, plaque d'or. | livre | P | q≥2 | S |
| `reliure_verte` | Reliure vert bouteille | Toile verte, nerfs dorés. | livre | B | m≥40 | S |
| `monture_ecaille` | Monture écaille | Lunettes rondes couleur écaille de tortue. | yeux | B | c≥20 | S\* (`gloseSpecs`) |
| `ecrit_coucou` | Le fil écrit « coucou » | Un bonjour en laine. | mot | P | q≥3 | M |
| `ecrit_ronron` | Le fil écrit « ronron » | Le mot du contentement. | mot | B | s≥10 | M |
| `grelot` | Grelot doré | Petit grelot au cou, sur un ruban. | cou | P | q≥12 | M (3 placements, 2 formes) |
| `beret` | Béret de peintre | Béret penché, queue de feutre au sommet. | tête | B | k≥15 | M (une fois dans `gloseHead`) |
| `reveil_tasse` | Réveillé dans sa tasse | La pose du soir, yeux ouverts : le thé du matin. | pose | B | q≥21 | M (pose `soir` et tête éveillée) |
| `cadre_dore` | Cadre doré | Fine moulure dorée autour de l'aperçu de la Collection. | cadre | P | q≥7 | S (interface) |

Faisabilité : S = surcouche de style ou forme d'interface (environ 15 min) ; S\* = simple après la séparation de jetons (environ 1 h en tout) ; M = nouveau groupe de formes simples (1 à 3 h) ; C = nouvelle pose, silhouette modifiée ou transparence (une demi-journée ou plus).

**Catalogue futur (170 éléments, [proposition], tables complètes en annexe).** Sur 170 éléments : 23 S, 8 S\*, 124 M et 15 C. Côté conditions, 51 reposent sur des compteurs existants ou déductibles (`q`, `s`, `c`, `m`, `k`), 30 sur deux petits compteurs nouveaux (`h` semaines actives, `l` liens ouverts), 37 sur une fenêtre du calendrier (`d:`) et 9 sur la complétion d'une série. Les 43 restants, soit **un quart**, dépendent de la révision espacée (`v`, `V`, `g`).

| Collection | N | Récompense de complétion | Points forts |
|---|---|---|---|
| A. Robes de chat (pelages) | 12 | cadre « Médaillon de race » | Chartreux bleu ardoise (h≥8), Siamois sépia (V≥40), Écaille de tortue (l≥60), Palimpseste (O, g≥150) |
| B. Nuancier (pelotes) | 14 | cadre « Nuancier » | Mimosa (fenêtre de février), Pelote chinée, Veilleuse qui luit, Fil d'or (O, g≥200) |
| C. Mots du fil | 17 | décor « Abécédaire » | « chut », « zut », « ode » (Printemps des poètes), « zeste » (dix mots 2026-2027), « hourra » (V≥100) |
| D. Cabinet de lecture (livres) | 11 | décor « Bibliothèque » | Dictionnaire à onglets (l≥100), Grimoire à fermoir (Halloween), Bandeau de prix, Édition originale |
| E. Lunettes et lorgnons | 6 | cadre « Étui à lunettes » | Monture argent, Verres ambrés d'été, Pince-nez (C) |
| F. Grands auteurs (objets, jamais de visage) | 10 | décor « Cabinet de l'écrivain » | Chapeau à panache, Sa madeleine (O, g≥100), Fromage du corbeau, Casque de scaphandrier (c≥2000) |
| G. Genres littéraires | 10 | cadre « Rayon de librairie » | Feutre et loupe de détective, Chapeau d'enchanteur, Bulle de cosmonaute, Masques de théâtre |
| H. Métiers du livre | 9 | décor « Atelier d'imprimerie » | Caractères de plomb, La coquille, Crayon rouge du correcteur, Boîte de bouquiniste |
| I. Calendrier des lettres | 8 | aucune | Lampe de chevet et Lecture sous la couette (Nuits de la lecture), Couronne de laurier, Ruban aux dix mots |
| J. Saisons et fêtes | 12 | aucune | Couronne des rois, Poisson d'avril, Brin de muguet, Écharpe sucre d'orge, Bougie d'un an (h≥40) |
| K. Pâtisseries françaises | 9 | décor « Salon de thé » | Croissant du matin, Tarte Tatin (« caramélisé »), Toque, Mille-feuille (O, c≥1000) |
| L. Régions et francophonie | 11 | décor « Carte de France » | Coiffe bigoudène (C), Cigale de faïence, Chapeau de gardian, Tuque (Semaine de la langue) |
| M. Calligraphie et encres | 7 | cadre « Lettrine » | Encrier de porcelaine (v≥8), Sceau de cire, Écritoire, Lettrine enluminée |
| N. Belle Époque | 8 | décor « Affiche de cabaret » | Canotier, Haut-de-forme, Chat noir de cabaret (contour à tester), Vélocipède |
| O. Nuit et étoiles | 7 | cadre « Constellation » | Barrette croissant de lune, Lunette d'astronome, Pelote d'étoiles (O), Assis sur la lune (h≥104) |
| P. Poses | 7 | cadre « Album » | Plongé dans sa lecture (v≥10, à faire en premier), Caché entre deux livres, Fenêtre un jour de pluie |
| Q. Cadres et décors | 9 | aucune | Timbre-poste, Photo instantanée, Ex-libris au nom choisi (V≥200), Coin du feu, Scriptorium (O) |
| R. Les Immortels (évocation, sans logo ni institution) | 3 | titre « Immortel » + cadre « Habit vert » | Bicorne (V≥250), Col brodé de rameaux, Épée d'Immortel (g≥365) |
| S. Pack de soutien (hors collection) | 3 | aucune | Médaille de mécène, Cadre des amis des livres, Salle de lecture. **Option propriétaire** : l'appli est gratuite et le benchmark exclut toute personnalisation payante |

**[proposition]** Revue des noms : sont écartés le Petit Prince, Moleskine, Sergent-Major, Polaroid, Bleu Klein, Pierre de Ronsard, ainsi que les noms et emblèmes du prix Goncourt et de l'Académie française. Aucun visage ni portrait de personne réelle : on évoque par un objet ou un personnage du domaine public. Coiffes régionales : dessin respectueux, à relire avec une personne concernée ; le madras est écarté pour l'instant. Le statut juridique des noms n'a pas été vérifié auprès de l'INPI. Ce sont des choix de prudence.

## Idées émergentes : bibliothèque, lettres et bluff en tête du brainstorm

**[fait]** Trois précédents fondent le haut du classement. Le jeu de bluff sur définitions est vieux de plusieurs décennies : Fictionary, Balderdash (1984), *Call My Bluff* (BBC, 1965-1988) ([Wikipedia](https://en.wikipedia.org/wiki/Balderdash)). Le partage montre l'utilisateur sans divulgâcher : la grille d'emojis de Wordle, ajoutée en décembre 2021, est créditée de sa viralité ([The Spinoff](https://thespinoff.co.nz/media/13-01-2022/those-coloured-boxes-on-twitter-are-new-zealands-fault)) ; Spotify Wrapped se partage parce qu'il montre l'utilisateur, pas la marque ([ADMA](https://adma.com.au/resources/personal-shareable-no-strings-attached-the-winning-formula-behind-spotify-wrapped)) ; le premier bilan annuel de Duolingo n'était proposé qu'aux apprenants actifs au moins 7 jours avec 20 leçons ([Duolingo](https://blog.duolingo.com/duolingo-2020-year-in-review)). Enfin, l'effet de génération : on retient mieux ce qu'on produit que ce qu'on lit ([McCurdy et al. 2020](https://link.springer.com/content/pdf/10.3758/s13423-020-01762-3.pdf)), mais la preuve directe sur le vocabulaire tient en une étude de 22 mots ([Badgett 2003](https://oasis.library.unlv.edu/rtds/1577)). **[dépôt]** Les 1 342 fiches n'ont ni siècle, ni langue d'origine, ni thème. Toute idée qui en dépend coûte de l'éditorial. L'historique local daté (règle des 270 jours) rend faisables hors ligne les idées tirées de l'historique.

| # | Piste [proposition] | Fun | Apprentissage | Effort | Dépendance ou risque |
|---|---|---|---|---|---|
| 1 | **Bibliothèque de Glose** : un livre par mot retenu, dos à la palette du mot et titré dans sa police (règle des 8 lettres respectée), ex-libris au nom choisi ; toucher un dos ouvre la carte | fort | fort : récompense un rappel différé | M | Historique (benchmark n° 3) et révision (n° 2) ; étagères mensuelles pour la performance |
| 2 | **Lettres de Glose** : une fois toutes les 2 à 3 semaines, au hasard, une carte postale d'un lieu littéraire (Croisset, Illiers-Combray, Guernesey) avec un mot rare lié | fort | moyen | S + 30 lettres | Strictement aléatoire, non conditionnel ; pas de notification insistante |
| 3 | **Le Bluff de Glose** (« une seule est vraie ») + mode « à table » en passe-et-joue | fort | fort : distracteurs plausibles, anecdote révélée ensuite | S + fausses définitions pour environ 200 mots | Vraie définition toujours affichée ensuite ; jamais de fausse définition sur l'écran de verrouillage |
| 4 | **« Il y a un an aujourd'hui »** + anniversaire d'installation | moyen à fort | fort : révision à 12 mois, gratuite | S | Rien la première année |
| 5 | **Calendrier culturel** (voir feuille de route) + calendrier de l'Avent des mots | moyen à fort | moyen | S par événement, éditorial annuel | Dates à revérifier chaque année |
| 6 | **« Votre année en mots »** en décembre, 5 à 7 cartes partageables | fort | moyen | M | Seuil minimal d'usage ; tout local |
| 7 | **Carte postale « Je vous envoie un mot »** (benchmark n° 7), plus un résultat façon Wordle sans score | moyen à fort | moyen | S/M | Dédicace en serif italique, pas en police décorative |
| 8 | **Carnet de Glose** : écrire sa phrase, recevoir un tampon (« Joliment tourné »), jamais une note | moyen | fort (preuve mince) | S/M | Facultatif ; clavier = friction |
| 9 | **Le Mot de la semaine** le dimanche, fondu dans le quiz du soir | moyen | fort : révision J+1 à J+7 | S | Pas de notification en plus |
| 10 | **Pendu doux de la pelote** (chaque erreur déroule un tour, pas de potence), anagramme sur mots vus | moyen à fort | moyen à fort | S/M | Exclure locutions et mots composés |
| 11 | **Le mot doré**, environ un jour sur 30 | moyen | faible à moyen | S | Cosmétique, sans FOMO, retrouvable |
| 12 | **Sons et haptique opt-in** | moyen | faible | S | Coupés par défaut, mode silencieux respecté |

**[inférence]** Sont écartés ou reportés : Motus et mots croisés (lexique de validation, licence et poids) ; siècle, étymologie, intrus et mots de saison (champs absents) ; succès littéraires (seulement s'ils récompensent des actes d'apprentissage, sans série) ; tuile Wear OS (passe par Jetpack, non vérifié en session). Deux idées touchent aux décisions du propriétaire. Le QR « cadeau de mot » exige un domaine hébergé avec `assetlinks.json` pour être cliquable, une dépendance réseau dans une appli hors ligne. Le poisson d'avril sous forme de mot inventé met en jeu la confiance dans les définitions et ne se fait que dans l'appli, jamais sur l'écran de verrouillage ; l'accessoire `poisson_avril`, lui, ne pose pas de problème. Le widget avec un Glose qui dort (benchmark n° 11) n'est pas l'écran de verrouillage et ne contredit donc pas la décision, mais il reste derrière les priorités du benchmark.

## Prérequis techniques : deux jetons, des calques par emplacement, huit compteurs

**[dépôt]** Un pelage, ce sont 3 jetons de thème (`gloseFur`, `gloseFurD`, `gloseCream`) ; une pelote, 2 jetons (`gloseBall`, `gloseBallD`), qui colorent aussi le fil. Les jetons des livres sont déjà séparés. Les accessoires sont des groupes nommés présents, cachés, dans chaque pose, avec des couleurs-attributs transparentes par défaut : une variante de couleur d'un accessoire existant ne coûte qu'une surcouche. Les lunettes utilisent `C.gold`, la même teinte que les pois du nœud, les rayures de l'écharpe et le gland du marque-page. Les rayures du tigré sont des bandes de la couleur du pelage. Chaque pose compte environ 180 chemins. La notification « tête seule » coupe chapeaux et plumes (`accessory = false`).

**Jetons `gloseSpecs` et `gloseStripe` (environ 1 h, à faire d'abord).** **[proposition]** Séparer la monture de `C.gold` et les rayures du pelage ouvre les éléments S\* (8 dans le futur, plus `smoking` et `monture_ecaille` dans la base). Une règle d'ordre est à coder : un livre ou une monture choisis s'appliquent avant le pelage, pour que « Nuit d'encre » garde son socle bordeaux. Un pelage noir (`chat_cabaret`) demandera sans doute un troisième jeton, celui du contour, pour éclaircir le trait `#3B2A20` (non rendu à ce jour).

**Calques par emplacement (avant le 20e accessoire).** **[inférence]** Dessiner tous les accessoires, cachés, dans chaque pose ferait grossir chaque VectorDrawable bien au-delà des 180 chemins, et le coût de chargement croît avec les chemins, même transparents. Le seuil de 20 est une estimation, pas une mesure. La solution de repli prévue dans `mise_en_place.md` superpose deux `ImageView` : le corps, et un AVD « accessoire » de même viewport et de mêmes durées. Les accessoires de tête restent les moins chers, dessinés une seule fois dans `gloseHead`, mais leur calque doit rejouer les animations de la tête dans la même image. Comme les AVD tournent sur le RenderThread, le synchronisme des deux calques est à vérifier sur appareil (Perfetto, `dumpsys gfxinfo`, entrée de gamme). Les décors (dessin statique en aplats, clair et sombre) et les cadres (forme XML autour de l'aperçu) restent hors du pipeline du personnage.

**Division de `Kind`.** **[proposition]** `Kind.ACC` se divise en `TETE`, `COU`, `YEUX`, `OBJET` et `LIVRE`, et on ajoute `MOT`, `DECOR` et `CADRE`. `Glose.choice` gère déjà une valeur par type : cela revient à ajouter des entrées à l'enum, et l'on porte un accessoire par emplacement. `Item` gagne deux champs nullables, `window` et `set`. Un test JUnit vérifie la table, comme pour l'existant.

| Code | Compteur | Disponible aujourd'hui | Récompense |
|---|---|---|---|
| `q` `p` `r` `s` | quiz, sans-faute, réussis, série stricte | oui | régularité ; `p` à éviter désormais |
| `c` | bonnes réponses | oui (`correctAnswers`, une ligne dans `Unlocks.value`) | effort |
| `m` | mots vus (historique, hors « q: ») | déductible de l'historique | lecture quotidienne |
| `k` | citations lues (clés « q: ») | déductible de l'historique | goût des citations |
| `l` | mots distincts dont on a ouvert un lien Académie ou TLFi | non : un `Set` dans `Prefs` | curiosité, sans tapoter en boucle |
| `h` | semaines actives (au moins 4 soirs de quiz sur 7) | non : une ligne dans `Quiz.record` | série tolérante |
| `v` `V` `g` | mots retenus à J+7, ancrés à J+30, gravés à J+90 | non : révision espacée (benchmark n° 2) | rappel différé |
| `d:MM-JJ..MM-JJ` | au moins un quiz dans la fenêtre, n'importe quelle année | non : champ `window` | calendrier, sans FOMO |
| `e:<série>` | tous les éléments non saisonniers de la série | non : champ `set` | complétion |

**[inférence]** La règle « un seul compteur par élément » souffre une exception dans le catalogue : `sous_couette` demande une fenêtre et 3 quiz dans cette fenêtre. Le plus simple est un seuil optionnel sur `d:` (par exemple `d:01-15..01-31×3`), sans conditions composées générales. Les conditions de rechange pour `creme`, `rose`, `noeud` (fondés sur `p`) et `nuit` (30 soirs stricts) demanderaient en revanche un « ou ». Les estimations de rythme de `v`, `V` et `g` (environ 0,8 par jour à 1 mot par jour et 80 % de réussite) restent théoriques tant que la révision n'existe pas.

**Animation et voix.** **[proposition]** Un aiguilleur `Glose.once(v, res, idle)` dans `Glose.kt` joue un one-shot, puis revient à la boucle de repos par `registerAnimationCallback` ; il est annulé en `onPause` par `clearAnimationCallbacks()` + `stop()`. Ce qu'il advient du callback après un `stop()` prématuré est à tester. `svg2avd.js` doit pouvoir produire un AVD « animateurs seuls » qui pointe vers un VD existant, soit environ 0,5 Ko par animation. Il faut aussi nommer `oreille_g` et `oreille_d` dans les sources Paper.js. Restent deux fonctions pures testées en JUnit : `Repliques.pick` avec son sac mélangé, et l'aide `de(nom)`. Un seul module de contraste sert à la carte de verrouillage, au fond d'appli et au bord des cartes claires.

## Feuille de route en sept lots, calée sur décembre, janvier et mars

**[inférence]** Les lots sont classés par rapport impact sur effort et par échéance. Ils s'entrelacent avec les priorités du benchmark (distracteurs tirés des mots vus, révision espacée, historique et favoris, onboarding, widget), qui restent à faire et conditionnent le lot 7. Une fenêtre `d:` reste atteignable tant que la version sort quelques jours avant sa fin. Toutes reviennent chaque année : manquer 2026 n'est pas grave.

| Lot | Contenu | Impact | Effort | Dépend de | Échéance |
|---|---|---|---|---|---|
| 1. Corrections et micro-plaisirs | A6 (arrêt de la boucle), A5, A3, B1, B2, B3, B6, bord des cartes claires, C2 | fort (conformité 2.2.2, sensation « jouet ») | 3-4 j | rien | novembre 2026 |
| 2. Collection, première vague | Jetons `gloseSpecs` et `gloseStripe` ; `Kind` divisé ; compteurs `c`, `m`, `k` ; champ `window` et `d:` ; les 14 éléments de base ; B4 déballage ; hiver : `echarpe_sucre_orge` et `bonnet_neige` (S), `ecrit_flocon` et `sapin_papier` (M), A8 flocon | fort | 1,5-2 sem. | lot 1 | avant le 1er décembre 2026 (fenêtre `12-01..12-31`, au plus tard vers le 20 décembre) ; `couronne_rois` avant le 2 janvier |
| 3. Glose vivant et nommé | Vie au repos, toucher gradué, bulles (80 à 100 répliques), réactions au mot, rituels, retour après absence, nom personnalisable, réécriture des chaînes, `de(nom)`, test des mots interdits | fort (attachement) | 1,5 sem. + écriture | lot 1 (A6) | décembre 2026 à janvier 2027 |
| 4. Nuits de la lecture | `lampe_chevet`, `sous_couette` (fenêtre `01-15..01-31`, seuil ×3), réplique et glyphe « lecture sous la couette » (thème 2027 : l'enfance) ; Chandeleur (`crepe`, `mimosa`) | moyen | 2-3 j | lot 2 | avant le 15 janvier 2027 (Nuits du **20 au 24 janvier 2027**) |
| 5. Fonds | Grain et vignette ; module de contraste et voile ; ma photo + « Rétablir » (benchmark n° 10) ; motif par palette ; glyphes du calendrier ; variante du soir opt-in | fort (surface la plus vue) | 1,5-2 sem. | rien | janvier à février 2027 |
| 6. Mars des mots | Printemps des poètes (**8 au 31 mars 2027**) : `laurier`, `ecrit_ode`, citations de poètes du domaine public ; Semaine de la langue française et de la Francophonie (**13 au 21 mars 2027**) : `ruban_dix_mots`, `ecrit_zeste`, `tarte_tatin`, `tuque`, clin d'œil aux dix mots ; puis `poisson_avril` (`03-29..04-05`), `rose_livre` et `rose_matin` (23 avril) | moyen | 3-4 j + éditorial | lot 2 | avant le 1er mars 2027 |
| 7. Mémoire et grandes collections | Après la révision espacée et l'historique : compteurs `v`, `V`, `g`, `h`, `l` ; pose `lecture` (v≥10) ; bibliothèque de Glose ; Bluff ; Mot de la semaine ; carnet ; carte postale (benchmark n° 7) ; « il y a un an » ; calques par emplacement avant le 20e accessoire, puis déploiement des collections A à R ; « Votre année en mots » en décembre 2027 | fort (fun lié à l'apprentissage) | plusieurs mois, surtout du dessin et de l'écriture | benchmark n° 1 à 3 | 2027 |
| Hors lots | Ambiances d'appli (L), sons opt-in, scènes illustrées, fil qui écrit le nom | moyen | M à L | lots 2 et 5 | selon les retours |

**[fait]** Dates : Nuits de la lecture du 20 au 24 janvier 2027, thème « L'enfance » ([Sortir à Paris](https://www.sortiraparis.com/en/what-to-visit-in-paris/walks/guides/267162-nuits-de-la-lecture-2027-in-paris-and-ile-de-france-dates-and-theme), [Paris.fr](https://www.paris.fr/evenements/les-nuits-de-la-lecture-2027-dans-les-bibliotheques-de-la-ville-de-paris-126792)). Printemps des poètes du 8 au 31 mars 2027, thème « La poésie : zones d'action » ([Livres Hebdo](https://www.livreshebdo.fr/article/le-printemps-des-poetes-devoile-le-theme-de-son-edition-2027)). Semaine de la langue française et de la Francophonie du 13 au 21 mars 2027 ([education.gouv.fr](https://www.education.gouv.fr/semaine-de-la-langue-francaise-et-de-la-francophonie-7643)). Dis-moi dix mots 2026-2027 : boucané, caramélisé, concocter, épluchette, ferment, mandoline, millésime, papilles, pimenter, zeste ([Éduscol](https://eduscol.education.gouv.fr/1912/dis-moi-dix-mots-concours-des-dix-mots)). Ces mots sont surtout courants : on les cite en clin d'œil, pas comme mots du jour. Journée mondiale du livre le 23 avril ([education.gouv.fr](https://www.education.gouv.fr/journee-mondiale-du-livre-et-du-droit-d-auteur-467526)). Partir en Livre 2026 a lieu du 17 juin au 19 juillet ; les dates 2027 ne sont pas publiées ([CNL](https://centrenationaldulivre.fr/sites/default/files/2026-03/CP_Partir%20en%20Livre%2026_Lancement_VF.pdf)). Les fenêtres de la rentrée littéraire (`08-20..10-15`) et des prix (`11-01..11-20`) sont des conventions non sourcées ; celle des prix est déjà ouverte et reviendra en novembre 2027.

## Décisions à prendre par le propriétaire

| # | Décision | Options | Recommandation par défaut |
|---|---|---|---|
| 1 | Glose sur la carte de verrouillage (signature, `glose_soir` dans la variante du soir, objet « Glose signe votre carte ») | maintenir le refus déjà décidé ; ou rouvrir sous forme de réglage opt-in, désactivé par défaut | maintenir le refus ; l'opt-in reste une option documentée |
| 2 | Boucle de Glose à l'accueil (WCAG 2.2.2) | arrêt automatique après 5 s ; ou boucle de repos irrégulière de 20 à 30 s avec un moyen de pause | arrêt à 5 s, cycle complet au toucher |
| 3 | Longueur maximale du nom | 20 ou 24 caractères | 20 si la notification à ×2,0 casse à 24 |
| 4 | Élision devant un nom en h | « de » partout devant h ; ou « d' » devant h, au risque d'un h aspiré | réécrire pour ne jamais avoir besoin de « de %1$s » |
| 5 | Genre des noms d'objets | écrire sans accord (« Sur son volume », « Sieste dans la tasse ») ; ou garder le masculin de « Monsieur » | écrire sans accord |
| 6 | Le fil qui écrit le nom | noms compatibles seulement (8 lettres au plus, d'un trait) ; initiale seule ; abandon | initiale seule, ou rien |
| 7 | Éléments fondés sur `p` (`creme`, `rose`, `noeud`) et `nuit` strict | garder ; ajouter une condition de rechange (`v` ou `h`) via un « ou » | garder pour l'instant, revoir une fois `v` et `h` en place |
| 8 | Pack de soutien | jamais ; dons avec remerciements purement cosmétiques, hors collections | jamais tant que l'appli est gratuite ; contradiction avec « personnalisation payante exclue » |
| 9 | Ambiances d'appli | aucune ; 2 ou 3 débloquées par la présence | aucune avant les décors saisonniers |
| 10 | Sons | aucun ; opt-in désactivé par défaut | opt-in, plus tard |
| 11 | Poisson d'avril (mot inventé) | jamais ; dans l'appli seulement, désactivable | dans l'appli seulement, ou l'accessoire seul |
| 12 | QR « cadeau de mot » | non ; domaine statique hébergé | non (dépendance réseau) |
| 13 | Texture papier | non ; A/B sur captures | A/B, abandon si on la remarque |
| 14 | Coiffes régionales et Immortels | publier ; relecture par des personnes concernées, puis publication | relecture d'abord ; pas de madras pour l'instant |
| 15 | Fréquence des bulles | à chaque ouverture ; une sur deux | une sur deux, au plus une par ouverture |

## Conclusion

Les recherches déplacent la question du « look » vers le **comportement**. Ce qui rend une appli cosy mignonne, c'est un personnage qui a sa propre vie, qui réagit au moindre geste et qui ne réclame jamais rien. Or Definiday possède déjà le dessin, les groupes animables, les palettes d'ambiance et les interjections nécessaires : la plupart des gains tiennent en quelques jours de Kotlin de la plateforme et beaucoup d'écriture. La contrainte la plus structurante n'est pas technique, elle est morale et mesurable. Chaque réplique, chaque fenêtre saisonnière et chaque seuil se vérifient contre une liste de règles (pas de manque, pas d'expiration, pas de score du soir), et ces règles peuvent vivre dans des tests JUnit plutôt que dans la bonne volonté.

Le second enseignement est une dépendance. Le fun le plus durable (bibliothèque de Glose, pose de lecture, légendaires gravés à J+90) est adossé à la mémoire réelle, donc à la révision espacée et à l'historique que le benchmark place en tête. Construire d'abord la collection sur les compteurs existants, puis la faire basculer vers le rappel différé, transforme la récompense : on gagne un objet parce qu'on a retenu un mot, et plus seulement parce qu'on a été présent. Il reste trois inconnues à lever par la mesure, pas par l'opinion : le coût de rendu des calques et des one-shots sur un appareil d'entrée de gamme, la perception de la boucle de Glose (respiration ou mouvement), et l'envie réelle des utilisateurs pour les ambiances et pour une mascotte sur la carte.

## Annexe : catalogue futur complet

Copie fidèle des tableaux de `research_notes/Definiday plus fun et mignonne/catalogue_skins.md` (section 3.3). Rareté : P = Poche ★, B = Broché ★★, R = Relié ★★★, O = Édition originale ★★★★. Faisabilité : S, S\*, M, C (voir la section sur la collection). La récompense de complétion de chaque série figure entre crochets.

### A. Robes de chat (pelages) [cadre « Médaillon de race »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `chartreux` | Chartreux bleu ardoise | Gris bleuté uni, yeux restant ceux de Glose. | B | h≥8 | S\* |
| `velin` | Vélin | Blanc crème uni, comme le papier fin ; nez et oreilles roses. | B | m≥90 | S\* |
| `cacao` | Cacao | Brun chocolat, rayures à peine plus sombres. | P | c≥120 | S |
| `ambre` | Ambre | Blond doré, plus clair que l’Enluminure, sans paillettes. | P | v≥20 | S |
| `siamois` | Siamois sépia | Corps crème, masque, oreilles, pattes et queue sépia. | R | V≥40 | M (groupe « points » clippé, caché par défaut) |
| `courtepointe` | Tricolore courtepointe | Calico : taches blanches, rousses et noires comme un patchwork. | R | v≥80 | M |
| `ecaille` | Écaille de tortue | Marbrures noires et rousses mêlées. | R | l≥60 | M |
| `mouchete` | Moucheté | Pelage sable à petites taches rondes. | B | h≥20 | M |
| `lavis` | Lavis d’encre | Dégradé d’encre du gris au blanc, comme un lavis (dégradés VD autorisés, API 24 et plus). | R | k≥120 | M |
| `persan_nuage` | Persan nuage | Poil long et gonflé : silhouette plus ronde. | R | V≥120 | C (silhouette) |
| `palimpseste` | Palimpseste | Pelage parchemin où transparaissent des lettres effacées. | O | g≥150 | C (chemins de texte clippés) |
| `papier_marbre` | Papier marbré | Robe aux volutes de papier dominoté, comme une garde de reliure. | O | e:robes | C |

### B. Nuancier (pelotes) [cadre « Nuancier »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `prune` | Pelote prune | Violet sombre. | P | q≥40 | S |
| `corail` | Pelote corail | Orange rosé. | P | m≥60 | S |
| `mimosa` | Pelote mimosa | Jaune vif, celle de février. | P | d:01-28..03-10 | S |
| `coquelicot` | Pelote coquelicot | Rouge franc. | P | v≥10 | S |
| `pervenche` | Pelote pervenche | Bleu lilas. | P | k≥40 | S |
| `pistache` | Pelote pistache | Vert tendre. | P | h≥6 | S |
| `chataigne` | Pelote châtaigne | Brun d’automne. | P | d:09-22..11-15 | S |
| `ardoise` | Pelote ardoise | Gris bleu des toits. | P | c≥200 | S |
| `ivoire` | Pelote ivoire | Blanc cassé. | B | v≥40 | S |
| `encre_chine` | Pelote encre de Chine | Noir profond, reflet bleu. | B | l≥30 | S |
| `chinee` | Pelote chinée | Deux brins mêlés (bordeaux et crème). | B | V≥30 | M (brins en 2e couleur) |
| `arlequin` | Pelote d’Arlequin | Losanges de couleur, clin d’œil à la commedia. | R | e:nuancier | M |
| `veilleuse` | Pelote veilleuse | Pelote qui luit doucement la nuit (halo en dégradé). | R | h≥30 | M |
| `fil_or` | Fil d’or | Pelote dorée qui scintille ; le fil écrit en or. | O | g≥200 | M (réutilise les éclats) |

### C. Mots du fil (poses « le fil écrit… », d’un seul trait, 6 lettres au plus, ni i, ni j, ni accent) [décor « Abécédaire »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `ecrit_chut` | Le fil écrit « chut » | Pour les soirs calmes. | P | s≥5 | M |
| `ecrit_zut` | Le fil écrit « zut » | Après une erreur, sans drame. | P | q≥15 | M |
| `ecrit_lu` | Le fil écrit « lu » | Le plus court des mots du fil. | P | m≥30 | M |
| `ecrit_ode` | Le fil écrit « ode » | Pour les amateurs de poésie. | B | d:03-08..03-31 | M |
| `ecrit_conte` | Le fil écrit « conte » | Il était une fois… | B | k≥30 | M |
| `ecrit_fable` | Le fil écrit « fable » | Avec sa morale. | B | v≥30 | M |
| `ecrit_prose` | Le fil écrit « prose » | Pour monsieur Jourdain. | B | c≥150 | M |
| `ecrit_verbe` | Le fil écrit « verbe » | Au commencement. | B | l≥20 | M |
| `ecrit_plume` | Le fil écrit « plume » | Le mot de l’écrivain. | B | h≥12 | M |
| `ecrit_encre` | Le fil écrit « encre » | Le mot de la page. | B | V≥20 | M |
| `ecrit_lettre` | Le fil écrit « lettre » | Un mot qui en contient une autre. | B | d:02-10..02-16 | M |
| `ecrit_recto` | Le fil écrit « recto » | Le devant de la page… | P | m≥120 | M |
| `ecrit_verso` | Le fil écrit « verso » | …et le dos. | B | v≥60 | M |
| `ecrit_flocon` | Le fil écrit « flocon » | Le mot de l’hiver. | B | d:12-01..12-31 | M |
| `ecrit_brume` | Le fil écrit « brume » | Le mot de l’automne. | B | d:10-01..11-15 | M |
| `ecrit_zeste` | Le fil écrit « zeste » | Un des « dix mots » 2026-2027. | B | d:03-13..03-23 | M |
| `ecrit_hourra` | Le fil écrit « hourra » | Pour les grandes occasions. | R | V≥100 | M |

### D. Cabinet de lecture (livres sous Glose) [décor « Bibliothèque »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `cuir_fauve` | Cuir fauve | Reliure cuir clair, coins usés. | P | h≥4 | S |
| `livre_poche` | Livre de poche | Couverture blanche à bandeau de couleur. | P | m≥80 | M |
| `toile_jouy` | Toile de Jouy | Couverture à scènes champêtres monochromes. | B | k≥60 | M (motif clippé) |
| `reliure_marbree` | Reliure à papier marbré | Plats marbrés, dos de cuir. | B | v≥50 | M |
| `dictionnaire` | Dictionnaire à onglets | Gros volume à encoches alphabétiques sur la tranche. | R | l≥100 | M |
| `grimoire` | Grimoire à fermoir | Cuir sombre, fermoir de laiton. | B | d:10-24..11-02 | M |
| `carnet` | Carnet à élastique | Carnet de notes noir, élastique et ruban. | P | c≥80 | M |
| `herbier` | Herbier | Volume d’où dépasse une fleur séchée. | B | d:04-25..05-08 | M |
| `atlas` | Atlas | Grand format bleu, rose des vents dorée. | B | h≥16 | M |
| `bandeau_prix` | Bandeau de prix | Le bandeau rouge des livres primés, posé sur le volume. | B | d:11-01..11-20 | M |
| `edition_originale` | Édition originale | Tranche dorée, coiffe usée, éclats d’or. | O | e:cabinet | M (réutilise les éclats) |

### E. Lunettes et lorgnons (yeux ; les lunettes restent, seule la monture change) [cadre « Étui à lunettes »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `monture_argent` | Monture argent | Fil d’argent fin. | P | q≥60 | S\* |
| `monture_nuit` | Monture bleu nuit | Acétate bleu nuit. | P | v≥15 | S\* |
| `monture_rose` | Monture rose poudré | Assortie à la pelote rose. | P | k≥25 | S\* |
| `monture_verte` | Monture vert d’Immortel | Vert sombre brodé d’or. | B | l≥80 | S\* |
| `verres_ambres` | Verres ambrés | Verres légèrement teintés pour l’été (les yeux restent visibles). | B | d:06-15..08-31 | S\* (`fillAlpha`) |
| `lorgnon` | Pince-nez | Deux verres sans branches, pincés sur le nez. | R | V≥80 | C (forme des lunettes dans `gloseHead`) |

### F. Grands auteurs (des évocations d’œuvres et d’objets, jamais un visage ni une ressemblance) [décor « Cabinet de l’écrivain »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `panache` | Chapeau à panache | Feutre à grande plume blanche : un panache de cape et d’épée (Cyrano, personnage). | R | V≥60 | M |
| `madeleine` | Sa madeleine | Une madeleine sur une soucoupe : la mémoire qui revient d’un coup. Récompense de la mémoire durable. | O | g≥100 | M (objet) |
| `canne_flaneur` | Canne de flâneur | Canne à pommeau, pour arpenter les passages couverts. | B | m≥200 | M |
| `cafetiere_nuit` | Cafetière des nuits blanches | Petite cafetière d’écrivain qui veille (évocation des romanciers noctambules). | B | h≥24 | M |
| `rose_matin` | Rose du matin | Rose à peine éclose (« Mignonne, allons voir… »). | B | d:04-16..04-30 | M |
| `fromage_corbeau` | Fromage du corbeau | Un fromage tenu… et bien gardé (La Fontaine). | B | v≥45 | M |
| `col_claudine` | Col Claudine | Petit col rond blanc d’écolière (le personnage de Colette). | P | k≥50 | M |
| `gilet_hernani` | Gilet rouge d’Hernani | Gilet écarlate des soirs de bataille romantique. | R | e:auteurs | C (vêtement de torse, 3 poses) |
| `ballon_verne` | Cinq semaines en ballon | Petite montgolfière qui flotte au-dessus de Glose. | R | m≥365 | M (objet en hauteur) |
| `scaphandre` | Casque de scaphandrier | Casque de cuivre à hublot : vingt mille lieues de mots. | O | c≥2000 | C (transparence du hublot) |

### G. Genres littéraires [cadre « Rayon de librairie »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `feutre_detective` | Feutre de détective | Chapeau mou de roman policier. | B | v≥25 | M |
| `loupe` | Loupe de détective | Grosse loupe posée contre le livre. | P | l≥15 | M |
| `couronne_conte` | Couronne de conte | Petite couronne d’or à trois pointes. | P | k≥20 | M |
| `chapeau_enchanteur` | Chapeau d’enchanteur | Chapeau pointu bleu nuit semé d’étoiles. | B | h≥10 | M |
| `bulle_cosmonaute` | Bulle de cosmonaute | Casque de verre de science-fiction rétro. | R | V≥150 | C (transparence) |
| `col_gothique` | Col de cape gothique | Haut col noir doublé de rouge. | B | d:10-24..11-02 | M |
| `bandeau_pirate` | Bandeau de corsaire | Bandeau de tissu sur le front, nœud sur le côté (sans cacher un œil). | B | c≥300 | M |
| `boussole` | Boussole d’aventurier | Boussole de laiton ouverte. | P | m≥150 | M |
| `masques_theatre` | Masques de théâtre | Comédie et tragédie sur un ruban. | B | k≥80 | M |
| `parchemin` | Parchemin de fable | Rouleau scellé d’une morale. | P | v≥35 | M |

### H. Métiers du livre [décor « Atelier d’imprimerie »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `bonnet_imprimeur` | Bonnet de papier d’imprimeur | Chapeau plié dans une feuille imprimée. | B | c≥250 | M |
| `caracteres_plomb` | Caractères de plomb | Quelques lettres de plomb dans un composteur. | B | v≥70 | M |
| `coquille` | La coquille | Un coquillage posé sur le livre (en typographie, une coquille est une faute). | B | l≥40 | M |
| `crayon_correcteur` | Crayon rouge du correcteur | Crayon bicolore rouge et bleu. | P | c≥100 | M |
| `tampon_bibliotheque` | Tampon de bibliothécaire | Tampon dateur et sa fiche de prêt. | P | h≥8 | M |
| `plioir` | Plioir du relieur | Plioir d’os pour marquer les plis. | B | V≥50 | M |
| `boite_bouquiniste` | Boîte de bouquiniste | Décor : boîte verte ouverte sur les quais, gravures et poches. | R | m≥300 | M (décor) |
| `sacoche_facteur` | Sacoche de facteur | Besace de cuir pleine de lettres. | B | k≥100 | M |
| `tablier_libraire` | Tablier de libraire | Tablier de toile à poche à crayons. | R | e:metiers | C (vêtement) |

### I. Calendrier des lettres (temps forts du livre et de la langue, chaque année)
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `lampe_chevet` | Lampe de chevet | Lumière douce pour lire tard (Nuits de la lecture). | B | d:01-15..01-31 | M |
| `sous_couette` | Lecture sous la couette | Décor : couverture tirée, lampe de poche, livre d’enfance (thème 2027 : l’enfance). | R | d:01-15..01-31 + q≥3 dans la fenêtre | M (décor) |
| `laurier` | Couronne de laurier | Couronne de poète (Printemps des Poètes). | B | d:03-08..03-31 | M |
| `ruban_dix_mots` | Ruban aux dix mots | Ruban au cou brodé de petits mots (Semaine de la langue française). | B | d:03-13..03-23 | M |
| `rose_livre` | Une rose et un livre | Rose posée sur un livre ficelé (Journée mondiale du livre). | B | d:04-16..04-30 | M |
| `chapeau_soleil` | Chapeau de lecture estivale | Chapeau de paille à ruban (été, Partir en Livre). | B | d:06-15..08-31 | M |
| `cartable_cuir` | Cartable de cuir | Cartable à rabat, la rentrée littéraire. | B | d:08-20..10-15 | M |
| `feuille_automne` | Feuille d’automne | Une feuille rousse tombée entre les oreilles. | P | d:09-22..11-15 | M |

### J. Saisons et fêtes (chaque année)
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `couronne_rois` | Couronne des rois | Couronne de papier doré de la galette. | P | d:01-02..01-20 | M |
| `crepe` | Crêpe dorée | Une crêpe dans une petite poêle. | P | d:01-28..02-08 | M |
| `billet_doux` | Billet doux | Enveloppe scellée d’un cœur. | P | d:02-10..02-16 | M |
| `poisson_avril` | Poisson d’avril | Poisson de papier collé dans le dos de Glose. | B | d:03-29..04-05 | M (3 placements) |
| `muguet` | Brin de muguet | Brin glissé derrière l’oreille, comme la plume. | P | d:04-25..05-08 | M (ancre de la plume) |
| `couronne_fleurs` | Couronne des champs | Pâquerettes et boutons d’or. | B | d:04-01..06-20 | M |
| `chapeau_sorciere` | Chapeau de sorcière | Pointu, bord large, boucle dorée. | B | d:10-24..11-02 | M |
| `citrouille` | Lanterne-citrouille | Petite citrouille sculptée, bougie dedans. | P | d:10-24..11-02 | M |
| `echarpe_sucre_orge` | Écharpe sucre d’orge | L’écharpe tricotée en rouge et blanc. | P | d:12-01..12-31 | S (variante de couleur) |
| `bonnet_neige` | Bonnet de nuit des neiges | Le bonnet de nuit en bleu glacier, pompon blanc. | P | d:12-01..02-28 | S (variante de couleur) |
| `sapin_papier` | Petit sapin de papier | Sapin plié posé sur la pile. | B | d:12-01..12-31 | M |
| `bougie_an` | Bougie d’un an | Gâteau à une bougie : une année ensemble (personnel, pas une date du calendrier). | R | h≥40 (≈ un an tolérant) | M |

### K. Pâtisseries françaises (lien avec « Dis-moi dix mots d’un monde à savourer », 2026-2027) [décor « Salon de thé »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `croissant` | Croissant du matin | Croissant doré sur une soucoupe. | P | q≥30 | M |
| `macaron` | Macaron framboise | Un macaron rose et sa collerette. | P | v≥12 | M |
| `eclair` | Éclair au café | Glaçage brun, une rayure blanche. | P | c≥60 | M |
| `chou_creme` | Chou à la crème | Chou poudré de sucre glace. | P | m≥50 | M |
| `religieuse` | Religieuse | Deux choux superposés, collerette de crème. | B | V≥25 | M |
| `tarte_tatin` | Tarte Tatin | Pommes caramélisées, « caramélisé » étant un des dix mots. | B | d:03-13..03-23 | M |
| `toque` | Toque de pâtissier | Haute toque plissée. | B | h≥14 | M |
| `praline` | Praliné | Pelage noisette caramel. | B | c≥400 | S |
| `mille_feuille` | Mille-feuille | Mille feuilles, comme mille pages : récompense de mille bonnes réponses. | O | c≥1000 | M (et éclats) |

### L. Régions de France et francophonie (traits respectueux ; coiffes traditionnelles stylisées, pas de caricature) [décor « Carte de France »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `coiffe_bigoudene` | Coiffe bigoudène | Haute coiffe de dentelle blanche (Bretagne). | R | V≥70 | C (haute, dentelle) |
| `col_marin` | Col marin | Col rayé bleu et blanc (Bretagne). | P | h≥5 | M |
| `kouign_amann` | Kouign-amann | Gâteau feuilleté au beurre. | B | m≥100 | M |
| `noeud_alsacien` | Grand nœud alsacien | Large nœud noir de coiffe. | B | v≥55 | M |
| `brin_lavande` | Brin de lavande | Glissé derrière l’oreille (Provence). | P | d:06-15..08-31 | M (ancre de la plume) |
| `cigale` | Cigale de faïence | Petite cigale de Provence posée sur le livre. | B | k≥70 | M |
| `canele` | Canelé | Petit gâteau cannelé de Bordeaux. | P | c≥180 | M |
| `chapeau_gardian` | Chapeau de gardian | Feutre noir à large bord (Camargue). | B | l≥50 | M |
| `edelweiss` | Edelweiss | Fleur des Alpes à la boutonnière. | B | h≥18 | M |
| `tuque` | Tuque | Bonnet à pompon québécois (francophonie). | B | d:03-13..03-23 | M |
| `phare` | Phare breton | Décor : phare rayé et embruns. | R | e:regions | M (décor) |

### M. Calligraphie et encres [cadre « Lettrine »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `encrier` | Encrier de porcelaine | Encrier blanc à filet bleu. | P | v≥8 | M |
| `buvard` | Buvard rose | Buvard taché d’encre. | P | q≥50 | S (rectangle) |
| `plume_ecolier` | Plume d’écolier | Porte-plume de bois et plume métallique. | P | m≥70 | M |
| `pinceau_calligraphe` | Pinceau de calligraphe | Pinceau et goutte d’encre. | B | k≥90 | M |
| `sceau_cire` | Sceau de cire | Médaillon de cire rouge au cou. | B | V≥35 | M |
| `ecritoire` | Écritoire | Décor : pupitre incliné, feuilles et chandelle. | R | e:calligraphie | M (décor) |
| `lettrine` | Lettrine enluminée | Cadre : bordure à lettrine fleurie, assortie au pelage Enluminure. | R | v≥150 | S (interface) |

### N. Belle Époque [décor « Affiche de cabaret »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `canotier` | Canotier | Chapeau de paille plat à ruban. | B | h≥22 | M |
| `haut_de_forme` | Haut-de-forme | Tube de soie noire. | B | V≥45 | M |
| `lavalliere` | Lavallière | Grand nœud souple de poète. | B | c≥160 | M |
| `ombrelle` | Ombrelle de dentelle | Ombrelle posée contre le livre. | B | k≥110 | M |
| `chat_cabaret` | Chat noir de cabaret | Pelage noir de jais aux yeux d’or (évocation des affiches de Montmartre). | R | V≥90 | S\* (trait d’encre éclairci, à tester) |
| `boa` | Boa de plumes | Boa duveteux autour du cou. | B | d:12-28..01-05 | M |
| `bouquet_violettes` | Bouquet de violettes | Petit bouquet à la boutonnière. | P | v≥18 | M |
| `velocipede` | Vélocipède | Décor : grand-bi posé contre un réverbère. | R | e:belle_epoque | M (décor) |

### O. Nuit et étoiles (prolonge « Nuit d’encre » et la pose du soir) [cadre « Constellation »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `bonnet_etoile` | Bonnet de nuit bleu nuit | Le bonnet de nuit en bleu nuit, pompon or. | P | s≥21 | S (variante de couleur) |
| `croissant_lune` | Barrette croissant de lune | Petite lune d’or entre les oreilles. | B | h≥26 | M |
| `lanterne` | Lanterne de papier | Lanterne ronde qui luit (halo comme la bougie). | B | k≥130 | M |
| `telescope` | Lunette d’astronome | Une lunette sur trépied (un jeu de mots pour un chat à lunettes). | R | V≥110 | M |
| `ciel_ete` | Ciel d’été | Décor : nuit bleue, étoiles, croissant. | R | h≥34 | M (décor) |
| `pelote_etoiles` | Pelote d’étoiles | Pelote bleu nuit semée d’étoiles pâles. | O | g≥250 | M (réutilise les étoiles) |
| `sur_la_lune` | Assis sur la lune | Pose : Glose sur un croissant de lune, le fil pend vers la Terre. | O | h≥104 (≈ 2 ans tolérants) | C (nouvelle pose) |

### P. Poses (hors mots du fil) [cadre « Album »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `lecture` | Plongé dans sa lecture | Assis, un livre ouvert entre les pattes, lunettes sur le nez. La pose « cœur » de l’appli : à faire en premier. | B | v≥10 | C |
| `sieste_pile` | Sieste sur la pile | La pose « pile » avec la tête endormie. | P | s≥12 | M |
| `entre_livres` | Caché entre deux livres | Tête et pattes qui dépassent d’un rayon. | B | l≥25 | M |
| `carton_livres` | Dans un carton de livres | Assis dans un carton de déménagement plein de livres. | B | m≥250 | C |
| `poursuite_fil` | À la poursuite du fil | Bond après la pelote qui roule. | R | V≥130 | C |
| `etirement` | Grand étirement | Le dos rond du réveil. | B | h≥28 | C |
| `fenetre_pluie` | À la fenêtre un jour de pluie | Pose avec son décor : gouttes, vitre, coussin. | R | k≥150 | C |

### Q. Cadres et décors (hors récompenses de série) [aucune récompense de complétion]
| id | Nom | Description | Emplacement | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|---|
| `timbre_poste` | Timbre-poste | Bordure dentelée. | cadre | P | k≥10 | S |
| `carte_postale` | Carte postale | Coin d’oblitération et filet. | cadre | P | m≥45 | S |
| `photo_instantanee` | Photo instantanée | Bordure blanche épaisse en bas. | cadre | P | h≥3 | S |
| `frise_laurier` | Frise de laurier | Rameaux d’olivier et de laurier. | cadre | B | v≥100 | S |
| `ex_libris` | Ex-libris | Cadre gravé « ex-libris » portant le nom choisi pour la mascotte. | cadre | R | V≥200 | M (texte et nom personnalisable) |
| `vitrail` | Vitrail | Cadre de plombs et verres colorés. | cadre | R | e:auteurs+genres | M |
| `coin_feu` | Coin du feu | Fauteuil, cheminée et plaid. | décor | B | d:11-15..02-28 | M |
| `quais_seine` | Quais au crépuscule | Parapet, réverbère, péniche. | décor | R | m≥400 | M |
| `scriptorium` | Scriptorium | Arcades, pupitres et manuscrits : le décor de l’Enluminure. | décor | O | g≥300 | M |

### R. Les Immortels (légendaire, sur le thème du dictionnaire ; une évocation de l’habit d’académicien, sans logo ni nom d’institution) [titre « Immortel » + cadre « Habit vert »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `bicorne` | Bicorne d’Immortel | Bicorne noir à plumes. | O | V≥250 | M |
| `col_rameaux` | Col brodé de rameaux | Col vert brodé de feuillages d’or. | R | l≥150 | M |
| `epee_immortel` | Épée d’Immortel | Épée de cérémonie posée contre le livre. | O | g≥365 | M |

### S. Pack de soutien facultatif (si un jour il y a des dons ; hors collections, hors rareté, aucun ne compte pour une complétion)
| id | Nom | Description | Emplacement | Faisab. |
|---|---|---|---|---|
| `merci_medaille` | Médaille de mécène | Petite médaille « Merci » au ruban bordeaux. | cou | M |
| `merci_cadre` | Cadre des amis des livres | Bordure dorée avec une branche. | cadre | S |
| `merci_salle` | Salle de lecture | Décor : longues tables, lampes vertes. | décor | M |
