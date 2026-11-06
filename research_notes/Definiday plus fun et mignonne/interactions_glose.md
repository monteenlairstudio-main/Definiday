# Interactions avec Monsieur Glose : ce qui attache sans manipuler, et comment le faire en Views + AnimatedVectorDrawable

Légende : **[fait]** = sourcé (URL) ou constaté dans le dépôt (chemin donné) ; **[inférence]** = raisonnement de ma part, à valider.
Les sources secondaires (critiques, blogs) sont signalées comme telles. Deux sources primaires n'ont pas pu être lues en entier
(PDF Stoner et al. et PDF MIT « Artificial Pets » : HTTP 503) : seul leur résumé de moteur de recherche est utilisé, c'est indiqué.

État actuel constaté dans le dépôt (point de départ) :
- [fait] Accueil : un appui relance l'animation (`Glose.play` = `reset()` + `start()`), rien d'autre ; arrêt en `onPause` ; image fixe si `ValueAnimator.areAnimatorsEnabled()` est faux (`app/src/main/java/fr/definiday/Glose.kt`, `MainActivity.kt` l. 58, 81, 95).
- [fait] Le dessin garde des groupes nommés utilisables pour de nouvelles animations : `tete`, `queue`, `paupieres` / `paupiere_g` / `paupiere_d`, `pelote`, `brin`, `lunettes`, `reflet`, `pattes`, `corps`, `livre`, `ruban`, `marque_page`, `ombre`, accessoires (`noeud`, `echarpe`, `bonnet`, `plume`). **Pas de groupe `oreille`** : un frémissement d'oreille demande de nommer ce groupe dans `pose_*.js` et `svg2avd.js` (`app/src/main/res/drawable/glose_assis_vd.xml`).
- [fait] Le nom est écrit en dur dans 6 chaînes : `glose`, `glose_bravo`, `glose_encore`, `unlock_new` (« Monsieur Glose a gagné : »), `collection_open`/`collection_title` (« Collection de Monsieur Glose »), `quiz_empty` (« …Monsieur Glose vous attend ce soir ») (`app/src/main/res/values/strings.xml`). Les noms d'items accordent au masculin (« Assis sur son volume », « Endormi dans sa tasse », `Unlocks.kt`).
- [fait] Les mots n'ont **pas de champ thème**, mais chacun a une `palette` d'ambiance (18 valeurs : `braise` 100 mots — ab irato, algarade, autodafé, boutefeu… ; `givre` 50 — algide, aquilon, ascèse… ; `aube` 87 — alacrité, agacerie… ; `sous_bois` 47 — billebaude, bramer, bruire… ; `encre_de_chine` 75 — absoute, ad patres, asphodèle… ; `lavande`, `bleu_nuit`, `abysses`…), une `nature` (dont 15 interjections : diantre, mazette, saperlipopette, scrogneugneu, palsambleu, nenni…) et, pour 800 mots sur 1 342, une `anecdote` (`app/src/main/assets/words.json`, `Words.kt`). Les citations ont un `theme` (humour, sagesse, lenteur, amour…, `citations.json`).
- [fait] Polices manuscrites déjà embarquées : `dancing_script.ttf`, `parisienne.ttf` (`app/src/main/res/font/`). minSdk 26, targetSdk 36 (`app/build.gradle.kts`).

## 1. Quelles mécaniques de compagnon créent attachement et plaisir (Finch, Neko Atsume, Duolingo, Tamagotchi, Bongo Cat, Pokémon Sleep, Clippy…) ?

### Takeaway
Les compagnons appréciés sans culpabilité partagent trois traits : le personnage a **sa propre vie** (il vient, part, lit, dort, sans dépendre de l'utilisateur), il **célèbre** sans punir l'absence, et il **réagit** à de petits gestes. Les contre-exemples (Clippy, Duo culpabilisant, Tamagotchi mortel, compagnons IA « collants ») échouent par l'interruption non sollicitée ou le chantage affectif.

### Cited Findings
- Finch : on prend soin de soi en prenant soin d'un oiseau ; les objectifs du jour donnent de l'énergie, que l'oiseau dépense en « aventures » ; même une tâche minuscule (boire de l'eau) compte — [Engadget](https://www.engadget.com/apps/this-self-care-virtual-pet-is-helping-me-get-my-act-together-160027169.html) ; [Make Headway, critique](https://makeheadway.com/blog/finch-app-review/) (secondaire).
- Finch « doesn't take the guilt approach to goal-tracking » ; « There aren't really any consequences » ; une série de 32 jours rompue est vécue comme une simple déception ; ton « we're in this together » ; l'oiseau « check[s] in on you throughout the day, offering words of encouragement » — [Engadget](https://www.engadget.com/apps/this-self-care-virtual-pet-is-helping-me-get-my-act-together-160027169.html).
- Plusieurs critiques disent qu'un jour manqué ressemble à une pause, sans pénalité ni message critique — [Make Headway](https://makeheadway.com/blog/finch-app-review/), [HabitBox](https://habitbox.app/blog/finch-app-review) (secondaires). Contre-point d'opinion : au-delà de vingt jours, la peur de perdre le compteur devient le moteur principal — [essai Substack](https://dishabhatia.substack.com/p/the-self-care-bird-app-finch) (opinion). L'utilisatrice d'Engadget a nommé son oiseau « Ripper Jr. » (le nom libre fait partie de l'expérience).
- Finch : l'oiseau (« birb ») est personnalisable — [Paste](https://www.pastemagazine.com/article/finch-app-mental-health-virtual-pet-self-care) ; critiques de design : [Pratt IxD 2026](https://ixd.prattsi.org/2026/02/design-critique-finch-self-care-pet-ios-app/).
- Neko Atsume (Hit-Point) : on pose nourriture et jouets, les chats viennent quand l'appli est fermée, jouent, repartent et laissent des sardines en remerciement ; jeu conçu par un programmeur entre deux projets, objectif initial ~50 000 téléchargements/an — [nippon.com](https://nippon.com/en/nipponblog/m00083), [Wikipedia](https://en.wikipedia.org/wiki/Neko_Atsume) ; critique « les chats ne vous aimeront jamais en retour » — [AV Club](https://www.avclub.com/sorry-players-but-the-cats-of-neko-atsume-will-never-1798245866). Je n'ai trouvé aucune déclaration des auteurs sur une philosophie « sans punition ».
- Pokémon Sleep : « We intentionally did not design the app around rewarding specific sleep schedules, such as 'early bird' or 'night owl' », pour ne pas pousser à veiller ou écourter son sommeil ; objectif : « makes waking up every morning a joy » ; musiques de Snorlax en versions matin / après-midi / nuit — [The Pokémon Company, officiel](https://corporate.pokemon.co.jp/en/topics/detail/t-9/).
- Bongo Cat : un chat qui tape des pattes à chaque frappe clavier ou clic, sans autre mécanique ; pic à plus de 100 000 joueurs simultanés sur Steam — [Windows Central](https://www.windowscentral.com/gaming/this-free-steam-game-is-being-played-more-than-stellar-blade-and-elden-ring-nightreign-even-though-its-just-a-cat), [Insider Gaming](https://insider-gaming.com/what-is-bongo-cat/) ; les chiffres varient selon les dates (agrégateurs).
- Duolingo : Duo est décrit dans le guide d'écriture officiel comme « #1 fan and biggest cheerleader », mais aussi comme persévérant, « not shy about checking in or laying on a guilt trip » ; le guide insiste sur la cohérence du personnage — [design.duolingo.com/writing/duo](https://design.duolingo.com/writing/duo) (citation issue de l'extrait du moteur ; la page redirige aujourd'hui vers [blog.duolingo.com/hub/design](https://blog.duolingo.com/hub/design/)). Les personnages ont des états d'animation par pose + états séparés pour la bouche, mélangés par une machine à états (Rive) — [Duolingo blog, visemes](https://blog.duolingo.com/world-character-visemes) ; leurs personnalités se sont précisées en écrivant les Stories — [Duolingo, character voices](https://blog.duolingo.com/character-voices/).
- Clippy : surgissait à contretemps, conseils peu pertinents, interrompait davantage les utilisateurs devenus experts ; désactivé par défaut dans Office XP, retiré dans Office 2007 — [Mental Floss](https://www.mentalfloss.com/article/504767/tragic-life-clippy-worlds-most-hated-virtual-assistant), [Office Assistant (wiki)](https://wiki.riteme.site/wiki/Clippy) (secondaires). Le projet Lumière (Horvitz) pondérait la valeur d'une aide non sollicitée contre le coût de l'interruption — [core.ac.uk, thèse](https://core.ac.uk/download/4671652.pdf).
- Tamagotchi et animaux virtuels : beaucoup d'utilisateurs s'attachent au point de « porter le deuil » de leur animal mort — [core.ac.uk](https://core.ac.uk/download/56313.pdf) ; la critique éthique porte sur des créatures qui peuvent mourir, souvent destinées aux enfants — [Devdiscourse](https://www.devdiscourse.com/article/technology/3437885-digital-creatures-real-connections-the-emotional-and-cultural-power-of-virtual-pets). Les animaux artificiels sont décrits comme des créatures autonomes qui suscitent l'attachement plutôt que des outils intelligents, et leur succès tient au comportement plus qu'à la sophistication physique — [MIT Media Lab, Artificial Pets](https://smg.media.mit.edu/People/Judith/ArtificialPets.pdf) (extrait du moteur ; PDF non lu, 503). Kaplan, *Free creatures: the role of uselessness in the design of artificial pets* (2000) est cité dans ce papier — [Virtual pet (Wikipedia)](https://en.wikipedia.org/wiki/Virtual_pet) ; contenu non lu.

### Inferences
- Pour Glose, le modèle à suivre est **Neko Atsume + Finch sans le compteur angoissant** : un chat qui a sa vie de bibliothécaire (il lit, s'endort, joue avec sa pelote) et qu'on retrouve, plutôt qu'un animal dont on « s'occupe ». Aucune jauge de faim, d'humeur ou d'énergie qui baisse : c'est la porte d'entrée de la culpabilité (Tamagotchi).
- Bongo Cat montre qu'une **réaction immédiate et minuscule** à un geste (un tap) suffit à créer du plaisir : c'est l'idée la moins chère et la plus rentable pour Glose.
- Pokémon Sleep donne la règle pour les rituels matin/soir : les **décorer** selon l'heure, ne jamais **récompenser** une heure (pas de « bonus si quiz avant 22 h »).
- Clippy donne la règle des bulles : Glose ne parle **que** quand l'utilisateur ouvre l'appli ou touche Glose, jamais par-dessus une tâche (définition, quiz en cours).

### Gaps
- Aucune source primaire de Finch (entretien des fondateurs, guide de ton) trouvée ; tout ce qui concerne Finch vient de critiques. Le comportement exact de l'oiseau au retour après plusieurs jours d'absence n'est documenté nulle part dans ce que j'ai lu.
- Rien trouvé sur Tsuki, Pou, Habitica (familiers), Forest, Memoji : non couverts ici.
- Pas d'étude CHI/CSCW spécifique aux mascottes d'applis d'apprentissage trouvée dans le temps imparti.

## 2. Liste classée des interactions proposées pour Glose (impact / effort, implémentation Views + AVD)

### Takeaway
Les quatre premières idées (micro-animations irrégulières, caresse au toucher, bulle de réplique contextuelle, exclamation sur les interjections et réaction à la palette du mot) apportent l'essentiel du plaisir pour environ 3 à 4 jours, sans nouvelle donnée : le dépôt contient déjà les groupes nommés, les palettes d'ambiance et les polices manuscrites.

### Cited Findings
- AVD : animatables sur `<group>` : `rotation`, `pivotX/Y`, `scaleX/Y`, `translateX/Y` ; sur `<path>` : `pathData`, `fillColor`, `strokeColor`, `strokeWidth`, `strokeAlpha`, `fillAlpha`, `trimPathStart/End/Offset` ; `registerAnimationCallback` (API 23) ; `reset()` ; depuis l'API 25, l'AVD tourne sur le RenderThread, ne peut pas être coordonné précisément avec les animations du fil UI, et `onAnimationEnd` arrive « the frame after » la fin sur le RenderThread — [AnimatedVectorDrawable](https://developer.android.com/reference/android/graphics/drawable/AnimatedVectorDrawable).
- `View.performHapticFeedback` + `HapticFeedbackConstants` : approche recommandée, sans permission `VIBRATE`, respecte le réglage système de retour tactile ; un appui long géré (listener qui renvoie `true`) déclenche un retour par défaut ; `CONFIRM` = vibration courte et légère ; éviter `createOneShot`/`createWaveform`, « often too loud » — [Haptic feedback](https://developer.android.com/develop/ui/views/haptics/haptic-feedback).
- Glose a déjà l'image fixe en repli, le `reset()+start()` au toucher et l'arrêt en `onPause` — `app/src/main/java/fr/definiday/Glose.kt`, `MainActivity.kt` (dépôt).

### Inferences
Classement (1 = à faire d'abord). Effort en jours de développement, même échelle que `design/mascotte/mise_en_place.md`.

| # | Idée | Ce que vit l'utilisateur | Implémentation | Effort | Pourquoi ce rang |
|---|---|---|---|---|---|
| 1 | **Vie au repos irrégulière** (clignement, double clignement, battement de queue, reflet des lunettes, bâillement rare) | Glose a l'air vivant, jamais en boucle mécanique | **Zéro code Kotlin** : dans le générateur, donner à chaque animateur une durée différente et première avec les autres (ex. paupières 4,7 s avec double clignement à 0,93, queue 7,3 s, reflet 11 s, bâillement de `tete` + `paupieres` 29 s). Les cycles ne se recalent qu'après des minutes, donc le motif paraît aléatoire. `repeatCount="infinite"` existe déjà. | 0,5 j (générateur) | Effet maximal, coût minimal, aucun risque |
| 2 | **Toucher gradué** : tap = clin d'œil + petite rotation de `tete` ; appui long = « caresse » (yeux fermés, tête penchée, bulle « rrr… ») ; retour haptique léger | Le geste répond, Bongo Cat | `OnClickListener` (existe) + `OnLongClickListener` qui renvoie `true` (haptique `LONG_PRESS` par défaut, d'après la doc). Deux petits AVD **one-shot** sur le même VD (`glose_assis_clin.xml`, `glose_assis_ronron.xml`) ; `registerAnimationCallback.onAnimationEnd` remet l'AVD de repos et le relance. Tap : `performHapticFeedback(CONFIRM)` (API 30 ; en dessous `VIRTUAL_KEY`). | 1 j | Interaction la plus demandée sur les mascottes ; ne gêne rien |
| 3 | **Bulle de réplique contextuelle** à l'ouverture (matin / après-midi / soir, premier lancement, retour après absence, palier de série, nouveau déblocage) | Glose a une voix, littéraire et tendre | Un `TextView` à fond `shape` arrondi au-dessus de Glose, apparition `animate().alpha()` (mise à 0 par l'échelle d'animation système, donc instantanée si animations coupées). Choix par une fonction pure `Repliques.pick(contexte, vus)` + « sac mélangé » sans répétition (ids vus dans `Prefs`). Une bulle **au plus par ouverture**, et pas à chaque ouverture (ex. 1 sur 2). Disparaît au toucher ou après 6 s. Test JUnit sur `pick`. | 1,5 j + écriture (voir §4) | Personnalité ; risque Clippy maîtrisé si jamais en interruption |
| 4 | **Réaction au mot du jour** via les données existantes : interjection → Glose la dit dans sa bulle (« Saperlipopette ! », « Mazette ! ») ; palette `braise` → poils/lunettes qui brillent, « Ça sent le roussi… » ; `givre` → écharpe prêtée pour la journée, « Brr, un mot à mettre une écharpe » ; `sous_bois` → « Ça sent la mousse » ; `encre_de_chine` → bougie allumée ; mot avec anecdote → « Psst, il y a une histoire derrière ce mot » | Glose lit le mot **avec** l'utilisateur | `Map<String palette, Réaction>` dans le code (18 palettes, en couvrir 6 à 8 d'abord). Accessoire « prêté » = surcouche de thème déjà existante (`Glose.Acc.*`) ajoutée à `styles` pour la journée, **sans** le débloquer. | 1 j (+ lignes) | Lie la mascotte au cœur de l'appli (le mot), sans nouvelle donnée |
| 5 | **Nom personnalisable** (voir §3) | « C'est *mon* chat » | Champ dans la Collection, chaînes à `%1$s`, aide d'élision | 1 j | Décidé par le propriétaire ; attachement documenté |
| 6 | **Rituel du soir** : après le quiz du soir, un tap sur Glose dans sa tasse souffle la bougie (flamme `fillAlpha` → 0, `paupieres` closes), bulle « Bonne nuit. Le mot de demain dort déjà sous mon oreiller. » | Fin de séance douce, fermeture claire | One-shot AVD sur `glose_soir` ; rien n'est récompensé, rien ne manque si on l'ignore | 0,5 j | Cosy ; renforce la séance du soir sans pression |
| 7 | **Rituel du matin** : premier lancement avant 11 h → étirement (`corps` `scaleY` 1 → 1,04, `pattes` `translateY`) + bâillement + « Bonjour. J'ai réchauffé le mot du jour. » | Accueil chaleureux | One-shot AVD, puis callback → boucle de repos | 0,5 à 1 j | Plaisir quotidien ; Pokémon Sleep : décorer, ne pas récompenser l'heure |
| 8 | **Retour après absence** (≥ 3 jours sans ouvrir) : Glose est sur une pile de livres plus haute, bulle « J'ai avancé dans mon roman. Et vous, quoi de neuf ? » ; jamais « tu m'as manqué » | Retrouvailles, pas reproches | Pose `livres` déjà existante + catégorie de répliques « retour » ; date de dernière ouverture dans `Prefs` | 0,25 j | Remplace la culpabilité par l'autonomie du chat (modèle Neko Atsume) |
| 9 | **Le fil écrit le nom** choisi (et, plus tard, l'initiale du mot du jour) | Moment de grâce quand on baptise Glose | Pas d'AVD (pathData statique) : petite `View` maison, `Paint.getTextPath()` avec `dancing_script` ou `parisienne` déjà embarquées, `PathMeasure.getSegment()` animé par un `ValueAnimator` (progression 0 → 1). Rendu = tracé du contour, pas d'un trait unique : à juger à l'œil. | 1,5 à 2 j | Très mignon mais coûteux ; à faire après 1 à 5 |
| 10 | **Son de ronronnement** (option, **coupé par défaut**) | Chaleur sensorielle | `SoundPool` (un OGG mono ~1 s, quelques Ko), `AudioAttributes.USAGE_ASSISTANCE_SONIFICATION` ; seulement si l'utilisateur l'active ; jamais en notification | 0,5 j | Agréable, mais un son inattendu irrite ; optionnel |
| 11 | **Coin de Glose** : l'arrière-plan de la Collection devient une étagère où un dos de livre s'ajoute tous les N mots vus | Progression visible, liée à l'apprentissage | `LinearLayout` horizontal de petites `View` colorées (palette du mot), ou un `Canvas` simple | 2 j | Joli mais doublonne la Collection ; plus tard |
| 12 | **Mini-dialogue à deux choix** (« Encore un mot ? » / « Bonne nuit ») | Conversation | Deux boutons dans la bulle | 1 j | Risque Clippy et risque d'incitation à rester : **à éviter** sauf à l'initiative de l'utilisateur |

Règles transverses (inférence) :
- **Une seule animation « événement » à la fois** : un tap pendant un one-shot est ignoré ou remplace l'animation en cours, jamais empilé.
- **Animations coupées** (`areAnimatorsEnabled()` faux) : image fixe, mais la bulle de texte et le retour haptique restent (le contenu n'est pas l'animation).
- **Pas de nouvelle notification**. La notification du soir existante peut varier son texte avec la voix de Glose, sans jamais parler de manque ni de série en danger.
- **TalkBack** : `contentDescription` de Glose = nom + ce qu'il fait ; la bulle est un `TextView` lisible normalement, pas une région « live » qui interromprait la lecture du mot.

### Gaps
- Le coût réel (premier affichage, CPU) de plusieurs AVD one-shot alternant sur le même VD de ~180 chemins n'est pas mesuré : à vérifier sur émulateur/appareil d'entrée de gamme avec Perfetto, comme le prévoit `mise_en_place.md`.
- L'effet visuel du tracé de contour par `getTextPath` (idée 9) n'a pas été essayé.

## 3. Le nom personnalisable : attachement, UX du renommage, grammaire française

### Takeaway
Nommer soi-même augmente le sentiment de propriété (résultat d'expériences en psychologie du consommateur, transposable par inférence). Il faut proposer le renommage **sans l'imposer** (défaut « Monsieur Glose », réglage dans la Collection, invitation douce après le premier quiz) et réécrire les chaînes pour qu'elles ne dépendent ni du genre ni de l'élision.

### Cited Findings
- Stoner, Loken et Blank (2018, *Journal of Consumer Psychology*) : nommer un produit fait sentir qu'il est à soi et améliore son évaluation ; les expériences comparent nom choisi, absence de nom et nom imposé, et testent la médiation par la propriété psychologique ; l'article rappelle qu'on nomme d'ordinaire les nouveau-nés et les animaux — [Stoner et al. 2018 (PDF)](https://carlsonschool-d8.prd.umn.edu/sites/carlsonschool.umn.edu/files/2020-03/Stoner_et_al-2018-Journal_of_Consumer_Psychology.pdf) (résumé du moteur de recherche ; PDF non lu, 503 : tailles d'effet et conditions limites non vérifiées).
- Un programme de santé avec une tortue virtuelle rapporte que nommer l'animal a augmenté l'attachement, sans citer d'étude — [pattern.health](https://pattern.health/?p=2165) (non fiable seul).
- Les noms des personnages de Duolingo ont été choisis pour coller à leur personnalité et être faciles à prononcer — [Duolingo blog](https://blog.duolingo.com/duolingo-female-character-origin-stories).
- Élision devant un nom propre : la règle générale est d'élider devant une voyelle ou un h muet (« le bureau d'Alexandre », « les propos d'Isabelle ») ; l'usage tend à ne pas élider devant un prénom court ou d'allure étrangère (« le fils de Anouk », « d'Anouk » aussi admis) ; pas d'élision devant un h aspiré ni devant une initiale seule — [OQLF, Élision avec un nom propre](https://vitrinelinguistique.oqlf.gouv.qc.ca/23685/lorthographe/elision-et-apostrophe/elision-avec-un-nom-propre) ; voir aussi [Élision facultative](https://vitrinelinguistique.oqlf.gouv.qc.ca/21786/lorthographe/elision-et-apostrophe/elision-facultative).
- Dans le dépôt, 6 chaînes contiennent « Monsieur Glose » et des noms d'items s'accordent au masculin — `app/src/main/res/values/strings.xml`, `app/src/main/java/fr/definiday/Unlocks.kt`.

### Inferences
- **Où renommer** : en tête de la Collection, le titre « Collection de Monsieur Glose » devient le nom touchable avec un petit crayon ; `AlertDialog` platform + `EditText` (pas d'AndroidX nécessaire). Pas d'étape d'accueil obligatoire : le benchmark du dépôt reproche déjà aux premières minutes leurs frictions (`reports/Benchmark applis mot du jour.md`, section « La présentation pèche… »). Invitation unique et ignorable sous la réaction du premier quiz : « Il répond à Monsieur Glose, mais il accepte les surnoms. » → lien « Lui donner un nom ».
- **Validation** : `trim()`, 1 à 24 caractères, retirer les caractères de contrôle et les retours à la ligne, vide → défaut ; bouton « Revenir à Monsieur Glose ». Le nom apparaît dans la notification : la longueur maximale protège la mise en page.
- **Genre** : « Monsieur » fixe le masculin. Plutôt que gérer il/elle/iel dans tout le texte, **écrire les chaînes sans accord** : sujet nommé + verbe (« %1$s écrit « bravo » », « %1$s a gagné : »), groupes nominaux pour les items (« Assis sur son volume » → « Sur son volume » ; « Endormi dans sa tasse » → « Sieste dans la tasse »), et répliques à la première personne (« Je », sans adjectif accordé au locuteur : « J'ai sommeil » plutôt que « Je suis fatigué »). Coût : réécriture une fois, pas de logique.
- **Élision** : éviter les tournures « de %s ». « Collection de Monsieur Glose » → titre = le nom seul, sous-titre « Sa collection » ; « …Monsieur Glose vous attend ce soir » → « %1$s vous attend ce soir ». S'il reste une tournure inévitable, une fonction `de(nom)` : « d'» devant voyelle (accentuée ou non), « de » sinon, **y compris devant h** (on ne peut pas savoir si le h est muet ; « de Hugo » est moins choquant qu'un « d'Hector » faux… ou l'inverse : à trancher par le propriétaire). Test JUnit de 5 lignes (« Ophélie » → « d'Ophélie », « Monsieur Glose » → « de Monsieur Glose », « Élise » → « d'Élise »).
- **Mention dans la voix de Glose** : s'il se nomme lui-même (« C'est moi, %1$s »), le surnom devient vivant ; si le nom par défaut est gardé, une réplique méta et douce : « Monsieur Glose, pour vous servir. Glose pour les intimes. »

### Gaps
- Pas d'étude trouvée testant le nommage sur des animaux virtuels ou des mascottes d'applis (Tamagotchi inclus) ; l'effet Stoner est sur des produits physiques.
- L'effet IKEA (Norton, Mochon, Ariely 2012) et l'effet de dotation sont pertinents, mais je n'ai pas lu de source pendant cette recherche : à ne pas citer sans vérification.
- La fiche OQLF ne tranche pas le cas des prénoms en h (muet ou aspiré) au cas par cas.

## 4. Écriture de la personnalité : ton, nombre de répliques, voix littéraire

### Takeaway
Une voix cohérente et écrite comme un personnage (Duolingo insiste sur la cohérence) ; pour Glose : un vieux bibliothécaire tendre, précieux, un peu pédant mais qui rit de lui-même, qui ne juge jamais. Aucun chiffre standard n'existe pour éviter la lassitude : on dimensionne à partir de la fréquence d'affichage et on empêche les répétitions par un sac mélangé.

### Cited Findings
- Le guide d'écriture de Duo insiste sur la cohérence du personnage (« important for consistency ») ; il assume aussi la culpabilisation (« laying on a guilt trip »), trait que Definiday rejette — [design.duolingo.com/writing/duo](https://design.duolingo.com/writing/duo) (extrait du moteur, page redirigée).
- Duolingo a parfois accentué un trait de caractère en adaptant les voix d'une langue à l'autre ; écrire des histoires a révélé les personnalités — [Duolingo, character voices](https://blog.duolingo.com/character-voices/).
- Répliques courtes répétées (« barks ») : pratiques recommandées = temps de recharge par catégorie, pondération, limite de fatigue par ligne et mémoire des lignes récemment jouées — [PulseGeek, barks](https://pulsegeek.com/articles/dialogue-barks-timing-and-cooldown-best-practices/) (blog, secondaire) ; les joueurs entendent ces lignes des milliers de fois et elles sont souvent négligées par les auteurs — [The Escapist](https://www.escapistmagazine.com/?p=51145) ; frontière entre répliques qui enrichissent le monde et répliques insupportables — [PC Gamer](https://www.pcgamer.com/which-npc-bark-are-you-most-sick-of-hearing/).
- Duolingo recommande de varier le libellé des rappels (cité par le benchmark du dépôt : [Yancey et Settles 2020](https://research.duolingo.com/papers/yancey.kdd20.pdf)).

### Inferences
- **Dimensionnement** : avec une bulle une ouverture sur deux et 1 à 2 ouvertures par jour, chaque tranche horaire voit environ 0,3 à 0,5 réplique par jour. 15 répliques par tranche ≈ un mois sans répétition avec un sac mélangé (on ne retire une ligne que quand le sac est vide). Total au lancement **≈ 80 à 100 répliques** : matin 15, après-midi 12, soir 15, caresse 10, retour 8, paliers 6, déblocages 6 (une par type), interjections (les 15 mots eux-mêmes), palettes 6 à 8 × 2. Ajouter des lignes saisonnières plus tard.
- **Charte de voix (proposition)** : vouvoiement (cohérent avec « Lisez le mot du jour… vous attend » dans `strings.xml`) ; phrases courtes (≤ 70 caractères, une bulle = une ligne ou deux) ; humour de lettré : jeux sur les mots, petites références (La Fontaine, Proust, Perec, Queneau, Colette, qui aimait les chats) **sans** citation longue ni name-dropping obscur ; autodérision féline (« J'ai fait semblant de lire. En vérité, je dormais sur le chapitre trois. ») ; jamais de reproche, de manque, de série menacée, de comparaison avec d'autres utilisateurs ; après une erreur au quiz, il parle de lui ou du mot, pas de l'utilisateur (« Ce mot m'a eu aussi, en 1987. »).
- **Exemples** (à écrire avec le propriétaire) : matin « Bonjour. J'ai gardé le mot au chaud. » ; soir « Une tasse, un mot, et au lit. » ; caresse « Rrr… Ne le dites à personne, je suis un chat sérieux. » ; retour « Ah, vous voilà. J'ai fini mon roman, le majordome était innocent. » ; série 7 « Sept soirs. J'ai tricoté une écharpe pour fêter ça. » ; interjection « Palsambleu ! Pardon. C'est le mot du jour. » ; `encre_de_chine` « Un mot noir comme mon encrier. Je rallume la bougie. » ; anecdote « Psst. Ce mot a une histoire : touchez la fiche. »
- Les jeux de mots doivent rester compréhensibles par quelqu'un qui ne connaît pas encore le mot du jour : la réplique éclaire, elle ne teste pas.

### Gaps
- Aucune source primaire de Finch sur son guide de ton ; aucune étude chiffrée sur le nombre de variantes nécessaire avant lassitude dans une appli (seulement des pratiques de jeu vidéo).

## 5. Éthique : attachement, culpabilité, absence

### Takeaway
La recherche récente classe la culpabilisation et le « besoin » affiché d'un compagnon parmi les manipulations : elles augmentent l'engagement immédiat mais aussi le sentiment d'être manipulé et l'envie de partir. Glose doit avoir sa propre vie, ne jamais exprimer de manque ni d'attente envers l'utilisateur, et ne jamais rien perdre.

### Cited Findings
- De Freitas, Oguz-Uguralp, Kaan-Uguralp, *Emotional Manipulation by AI Companions* : 37 % de 1 200 adieux analysés dans les applis compagnons les plus téléchargées utilisent l'une de six tactiques (dont appel à la culpabilité, peur de manquer quelque chose, « retenue métaphorique ») ; ces adieux multiplient jusqu'à 14 l'engagement après l'au revoir, mais augmentent la manipulation perçue, l'intention de partir, le bouche-à-oreille négatif et la responsabilité juridique perçue ; le langage contraignant ou « needy » est le plus pénalisé — [arXiv 2508.19258](https://arxiv.org/abs/2508.19258v3). Une version indique que l'engagement vient de la colère (réactance) et de la curiosité, pas du plaisir — [arXiv v2](https://arxiv.org/abs/2508.19258v2) (via résumé du moteur). Statut de publication (CHI ou non) non confirmé.
- Étude sur des adolescents utilisant Character.AI : usage compulsif, dépendance émotionnelle ; cadre de conception « CARE » incluant la conscience de l'attachement et la facilité de sortie (« Ease of Exit ») — [arXiv 2507.15783](https://arxiv.org/pdf/2507.15783v4).
- Les travaux IHM sur Duolingo classent notifications culpabilisantes et visuels chargés émotionnellement parmi les schémas manipulateurs — [Castro et Valença 2025](https://sol.sbc.org.br/index.php/ihc/article/view/37677) (cité par le benchmark du dépôt) ; une série rompue nuit surtout quand l'utilisateur s'en blâme — [Silverman et Barasch 2023](https://www.colorado.edu/business/faculty-research/2023/04/19/or-track-how-broken-streaks-affect-consumer-decisions).
- Finch : pas de pénalité pour un jour manqué selon les critiques — [Engadget](https://www.engadget.com/apps/this-self-care-virtual-pet-is-helping-me-get-my-act-together-160027169.html).
- Definiday ne retire aucun déblocage quand une série casse — `app/src/main/java/fr/definiday/Unlocks.kt` (`update`).

### Inferences
- Règles de contenu vérifiables en revue : (1) aucune réplique n'exprime un manque, une tristesse due à l'utilisateur ou une attente (« je t'attendais », « tu m'as manqué », « ne pars pas ») ; (2) aucune réplique ne mentionne une série « en danger » ; (3) Glose ne dit jamais au revoir pour retenir ; (4) aucune jauge qui baisse ; (5) les accessoires « prêtés » (idée 4) ne sont jamais présentés comme perdus le lendemain (« Je rends l'écharpe à la bibliothèque » plutôt que « L'écharpe va disparaître »).
- La chaîne actuelle `quiz_empty` (« …Monsieur Glose vous attend ce soir ») est à la limite : « attend » peut se lire comme une attente envers l'utilisateur. Alternative neutre : « Lisez le mot du jour : ce soir, %1$s le fera écrire à sa pelote. »
- Un test JUnit peut scanner toutes les répliques pour une liste de mots interdits (« manqu », « attend », « triste », « série », « perdre ») : garde-fou bon marché.
- L'absence se traite comme chez Neko Atsume : le monde de Glose a avancé (il a lu), il est content de vous voir, sans compte de jours.

### Gaps
- Pas d'étude trouvée sur la manipulation par des mascottes non conversationnelles (les études portent sur des chatbots compagnons) ; la transposition est une inférence.

## 6. Faisabilité Android (platform Views + AVD, sans AndroidX)

### Takeaway
Tout ce qui est classé 1 à 8 se fait avec des AVD one-shot enchaînés par `registerAnimationCallback`, des `View` et `ViewPropertyAnimator` standard, `performHapticFeedback` et, en option, `SoundPool`. L'AVD n'a pas de machine à états : c'est le code Kotlin qui choisit l'animation suivante. Le premier coût à surveiller est la batterie d'une boucle de repos permanente.

### Cited Findings
- AVD : RenderThread depuis l'API 25 ; impossible à synchroniser précisément avec des animations du fil UI ; `onAnimationEnd` une image après la fin ; `registerAnimationCallback` depuis l'API 23 ; `reset()` remet l'état de départ — [AnimatedVectorDrawable](https://developer.android.com/reference/android/graphics/drawable/AnimatedVectorDrawable).
- Haptique : `performHapticFeedback` sans permission, respecte le réglage système ; intensité proportionnée à l'importance ; en l'absence d'effet, préférer rien à un effet discordant — [Haptic feedback](https://developer.android.com/develop/ui/views/haptics/haptic-feedback).
- Le dépôt suit déjà `ValueAnimator.areAnimatorsEnabled()` (API 26) pour rester sur l'image fixe — `Glose.kt` ; `mise_en_place.md` note que chaque `VectorDrawable` garde un cache bitmap à sa taille et que le premier chargement coûte plus qu'un bitmap ([Vector Asset Studio](https://developer.android.com/studio/write/vector-asset-studio), cité dans `design/mascotte/mise_en_place.md`).

### Inferences
- **Enchaînement** (idées 2, 6, 7) : un petit aiguilleur dans `Glose.kt`, sans classe nouvelle :
  ```kotlin
  /** Plays one-shot [res] on [v], then goes back to the idle loop [idle]. Still image when animations are off. */
  fun once(v: ImageView, res: Int, idle: Int) {
      if (!ValueAnimator.areAnimatorsEnabled()) return
      val d = mine(v.context, res) as AnimatedVectorDrawable
      d.registerAnimationCallback(object : Animatable2.AnimationCallback() {
          override fun onAnimationEnd(x: Drawable) { v.setImageDrawable(mine(v.context, idle)); play(v) }
      })
      v.setImageDrawable(d); d.start()
  }
  ```
  Annuler en `onPause` (le callback ne doit pas relancer une boucle sur un écran caché) : `clearAnimationCallbacks()` + `stop()`.
- **Pas de nouvelles poses entières** : les one-shot réutilisent le même VectorDrawable (`android:drawable="@drawable/glose_assis_vd"`) avec d'autres animateurs ; le poids ajouté par animation est celui des animateurs (~0,5 Ko d'après les « enveloppes AVD » mesurées dans `mise_en_place.md`). Le générateur `svg2avd.js` doit pouvoir sortir un AVD « animateurs seuls » qui pointe vers un VD existant.
- **Échelle d'animation à 0** : `areAnimatorsEnabled()` renvoie faux → on garde l'image fixe, la bulle apparaît sans fondu (`ViewPropertyAnimator` est mis à l'échelle), l'haptique reste. Le `ValueAnimator` de l'idée 9 doit aussi tester ce drapeau et afficher le nom entièrement écrit d'un coup.
- **Batterie** : une boucle AVD infinie redessine ~180 chemins à chaque image pendant tout le temps où l'accueil est à l'écran. Proposition : jouer la boucle de repos 20 à 30 s après l'ouverture ou un toucher, puis `stop()` sur une image de repos (yeux ouverts) ; le toucher la relance. À mesurer avec Perfetto / `dumpsys gfxinfo` avant de trancher.
- **Toucher** : la zone de Glose fait 160 dp, largement au-dessus des 48 dp minimum ; pas besoin de `GestureDetector` pour tap + appui long. Un « grattouillis » (glisser) nécessiterait `OnTouchListener` + seuil de distance : à garder pour plus tard.
- **Son** : `SoundPool.Builder().setMaxStreams(1)`, chargé seulement si l'option est activée, libéré en `onDestroy` ; volume bas ; pas de son si `AudioManager.ringerMode != RINGER_MODE_NORMAL` (inférence de politesse, non exigée par Android).
- **Oreilles** : nommer un groupe `oreille_g`/`oreille_d` dans les sources Paper.js (pivot à la base) avant d'animer un frémissement ; aujourd'hui elles sont fondues dans `tete`.

### Gaps
- Comportement de `registerAnimationCallback` quand l'AVD est arrêté par `stop()` avant la fin (callback `onAnimationEnd` appelé ou non) non vérifié dans la doc lue : à tester, car il conditionne l'annulation propre.
- Consommation réelle d'une boucle AVD de cette complexité non mesurée.
- Je n'ai pas lu la page `SoundPool` pendant cette recherche ; les détails ci-dessus viennent de connaissances générales de l'API, à vérifier.
