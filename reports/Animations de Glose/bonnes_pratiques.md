# Animation de mascotte : bonnes pratiques pour Monsieur Glose (Definiday)

Brief de recherche, novembre 2026. Contexte : Android, Kotlin, Views natives, pas d'AndroidX/Lottie/Rive.
Les animations sont des AnimatedVectorDrawable (AVD) générés depuis des sources Paper.js et empilés dans un
LayerDrawable (corps + accessoires + contours de nuit). Problème signalé : certaines animations sont
« saccadées » ou « décalées ».

Convention de marquage, à chaque affirmation :
- **[F]** fait sourcé (URL juste à côté) ;
- **[D]** déduction (raisonnement à partir de faits sourcés, ou mesure faite sur le dépôt) ;
- **[H]** hypothèse ou règle empirique de métier, à vérifier sur appareil.

---

## 0. Ce qu'il faut retenir (constats sur le dépôt, à lire en premier)

En préparant le brief, j'ai lu le code source AOSP de l'AVD et parcouru `app/src/main/res/{drawable,animator}`.
Quatre mécanismes expliquent très probablement une bonne partie des saccades :

1. **[F]** Un `objectAnimator` sans `android:interpolator` prend l'interpolateur par défaut de ValueAnimator,
   `AccelerateDecelerateInterpolator` (« By default, ValueAnimator uses non-linear time interpolation, via the
   AccelerateDecelerateInterpolator class », https://developer.android.com/reference/android/animation/ValueAnimator)). Confirmé dans le code AOSP : `AnimatorInflater` n'appelle `setInterpolator` que si l'attribut est présent (https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/core/java/android/animation/AnimatorInflater.java), et `ValueAnimator.mInterpolator = sDefaultInterpolator = new AccelerateDecelerateInterpolator()` (https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/core/java/android/animation/ValueAnimator.java).
   **[D]** Les 297 `objectAnimator` Glose n'ont **aucun** `android:interpolator` (comptage sur le dépôt). Toute la
   timeline de chaque animateur (y compris les boucles idle de 29 000 ms) est donc déformée par un ease-in-out
   global, appliqué *en plus* des interpolateurs de keyframes. Au milieu du cycle le temps passe environ 1,57 fois
   plus vite (pente max de la courbe accelerate-decelerate = π/2). Au début et à la fin, il passe plus lentement.
   Conséquences : (a) le timing écrit dans Paper.js n'est pas celui joué ; (b) deux calques de durées différentes
   ne subissent pas la même déformation, donc des poses prévues pour être simultanées se **décalent**.
   Correctif : mettre `android:interpolator="@android:interpolator/linear"` sur chaque `objectAnimator` à
   keyframes, et laisser les keyframes porter l'easing.
2. **[F]** Chaque keyframe porte un interpolateur qui s'applique à l'intervalle **qui la précède** (« defines the
   time interpolation over the interval preceding the keyframe »,
   https://developer.android.com/reference/android/animation/Keyframe). **[D]** Glose met `glose_ease`
   (cubic-bezier 0.45, 0, 0.55, 1, un ease-in-out symétrique) sur **toutes** les keyframes. La vitesse retombe donc à
   zéro à **chaque** keyframe, y compris au milieu d'un mouvement continu. Exemple : un balancier 0 → 6° → 0 → −6° → 0
   s'arrête au passage par 0°, là où un vrai balancier va le plus vite. C'est la signature visuelle du
   « saccadé » (un mouvement en « stop and go »). Correctif : ease-in-out seulement sur les vraies poses extrêmes ;
   les keyframes de passage reçoivent un easing qui conserve la vitesse (voir §2.3).
3. **[F]** Sur API 25+, l'AVD tourne sur le RenderThread. Les animations à keyframes y sont **pré-échantillonnées**
   au pas de la frame (`getFrameIntervalNanos()` tronqué en ms entières : 16, 11 ou 8), avec au plus
   `MAX_SAMPLE_POINTS = 300` points, puis **interpolées linéairement** entre les points (code AOSP :
   https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/graphics/java/android/graphics/drawable/AnimatedVectorDrawable.java ,
   `getFrameCount` / `createFloatDataPoints` ; et
   https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/libs/hwui/PropertyValuesHolder.cpp ,
   `getValueFromData`). Au-delà de 300 points, un log signale : « the animation will subsample the keyframe or
   path data ». **[D]** Un animateur de 29 000 ms n'a donc qu'un échantillon toutes les ~97 ms, quel que soit l'écran.
   Un événement court au milieu de la boucle (clignement, frémissement d'oreille) est réduit à quelques segments
   de droite, voire **sauté**. Correctif : pas de keyframes à cadence rapide dans un animateur long. Un
   clignement doit être un animateur court, relancé par du code (voir §2.2).
4. **[D]** Le script de contrôle du pré-échantillonnage (`scratchpad/anim/tools/avd_sampling.py`) signale 16 segments
   de keyframes (sur 4 animateurs) qui bougent en 12 à 36 ms, soit moins de 1 à 2 frames à 60 Hz. Exemple :
   `animator/glose_k_37bcf792.xml` (tap de 600 ms, cible `tete`/`tete_halo`) fait translateY −14,2 → −13,3 en 12 ms,
   puis repart. Un segment plus court que 2 frames n'est pas une animation, c'est un saut : on le fusionne avec le
   segment voisin ou on l'allonge.

---

## 1. Principes d'animation

### 1.1 Timing et espacement (spacing)
- **[F]** Le *timing* est le nombre d'images (de frames) consacrées à une action. Le *slow in / slow out*
  consiste à mettre plus d'images près du début et de la fin d'une action
  (https://en.wikipedia.org/wiki/Twelve_basic_principles_of_animation, d'après Thomas & Johnston,
  *The Illusion of Life*, 1981).
- **[D]** En AVD, on ne dessine pas d'images : l'*espacement* est entièrement porté par l'interpolateur. Un mauvais
  interpolateur produit donc un mauvais spacing, même avec de bonnes poses.
- **[H]** Mesurer le spacing réel : tracer la valeur interpolée frame par frame (pas de 16,67 ms, 11,1 ms et
  8,33 ms) et regarder (a) les deltas trop grands (sauts) et (b) les vitesses qui retombent à 0 hors d'une pose
  voulue (« stop and go »).

### 1.2 Courbes d'easing : laquelle pour quel mouvement
- **[F]** Le mouvement purement linéaire paraît « weird and unnatural » ; l'ease-out (départ rapide, fin lente)
  est le choix le plus fréquent en UI ; l'ease-in peut sembler peu réactif
  (https://www.nngroup.com/articles/animation-duration/).
- **[F]** Jetons Material 3 (valeurs reprises dans la doc Material Components Android,
  https://github.com/material-components/material-components-android/blob/master/docs/theming/Motion.md) :
  - standard `cubic-bezier(0.2, 0, 0, 1)` : mouvement qui commence et finit à l'écran ;
  - standard decelerate `(0, 0, 0, 1)` : entrée ; standard accelerate `(0.3, 0, 1, 1)` : sortie ;
  - emphasized : `path M 0,0 C 0.05,0,0.133333,0.06,0.166666,0.4 C 0.208333,0.82,0.25,1,1,1` ;
  - emphasized decelerate `(0.05, 0.7, 0.1, 1)` ; emphasized accelerate `(0.3, 0, 0.8, 0.15)` ;
  - linear : « simple, non-stylized motion ».
- **[F]** `PathInterpolator` (API 21) accepte une Bézier quadratique ou cubique, ou un `Path` de (0,0) à (1,1)
  qui doit rester une fonction y = f(x) (https://developer.android.com/reference/android/view/animation/PathInterpolator).
  Interpolateurs système disponibles : AccelerateDecelerate, Accelerate, Anticipate, AnticipateOvershoot, Bounce,
  Cycle, Decelerate, Linear, Overshoot
  (https://developer.android.com/guide/topics/graphics/prop-animation). Les ressources
  `@android:interpolator/fast_out_slow_in` (et `linear`, `fast_out_linear_in`, `linear_out_slow_in`) sont
  utilisées dans la doc officielle des AVD (https://developer.android.com/develop/ui/views/graphics/vector-drawable-resources).
- Correspondance proposée pour Glose **[H]**, en partant des faits ci-dessus :

  | Mouvement de Glose | Courbe | Pourquoi |
  |---|---|---|
  | Respiration, balancement de queue, pelote (boucles) | sinusoïde : keyframes aux extrêmes seulement, ease-in-out symétrique `(0.45,0,0.55,1)` ou `(0.37,0,0.63,1)` | vitesse nulle aux extrêmes, maximale au centre : ne jamais poser de keyframe sur le passage au centre |
  | Clignement (fermeture) | accelerate `(0.3,0,1,1)`, 60 à 80 ms | la paupière tombe |
  | Clignement (ouverture) | decelerate `(0,0,0,1)`, 100 à 150 ms | ouverture un peu plus lente que la fermeture |
  | Réaction au tap : impulsion | emphasized decelerate `(0.05,0.7,0.1,1)` | réponse immédiate, perçue comme réactive |
  | Retour à la pose idle | standard `(0.2,0,0,1)` ou emphasized | atterrissage doux, pas d'arrêt sec |
  | Hochement/inclinaison (quiz) | anticipation + overshoot : `AnticipateOvershoot` (tension ~1,0 à 1,5) ou deux keyframes | lisibilité du « oui » |
  | Écriture au fil (trimPathEnd) | quasi linéaire au milieu, ease-in/out court aux bouts : `(0.3,0,0.7,1)` | vitesse d'écriture constante, départ/arrêt naturels |
  | Fondus de calques (jour/nuit, accessoires) | linear ou standard, sans overshoot | un alpha ne doit jamais rebondir (cf. ressorts « effects » sans overshoot dans Material) |

- **[F]** Linéaire acceptable : Material le réserve au mouvement « non stylisé » (même source Motion.md). **[D]** Pour
  Glose : uniquement pour (a) l'interpolateur *global* d'un animateur à keyframes (cf. §0.1), (b) les rotations
  continues (pelote qui roule), (c) le trimPath au milieu de son tracé.

### 1.3 Anticipation, follow-through, overlap, drag
- **[F]** L'anticipation prépare le spectateur à l'action. L'*overlapping action* : les parties du corps bougent à
  des vitesses différentes. Le *drag* : certaines parties « mettent quelques images à rattraper » le mouvement
  (https://en.wikipedia.org/wiki/Twelve_basic_principles_of_animation).
- **[F]** En idle, l'overlap « remonte le long de la colonne puis dans les bras »
  (https://blog.animschool.edu/2024/06/14/breathing-life-into-idle-animations).
- **[H]** Ordres de grandeur pour un tap de 600 ms : anticipation de 60 à 100 ms (10 à 15 % de la durée, amplitude de 20 à
  30 % de l'action principale, en sens inverse). Décalage d'overlap de 1 à 3 frames (16 à 50 ms) par maillon :
  corps → tête → oreilles → lunettes (ou chaîne) → queue.
- **[D]** Chez Glose : lunettes, écharpe, grelot, ruban et queue sont les maillons « traînants » naturels. Ils doivent
  démarrer après la tête et s'arrêter après elle, jamais en même temps.

### 1.4 Holds (temps de pose)
- **[F]** *Moving hold* : même immobile, un personnage garde un léger mouvement pour ne pas paraître mort
  (Wikipedia, principes, même page).
- **[H]** Une pose « figée » de plus de ~250 ms dans une réaction se lit comme un bug. On garde plutôt un micro-mouvement
  de 1 à 2 % d'échelle ou de 0,5 à 1° de rotation, en ease-in-out.
- **[D]** La pose « repos » après les ~26 s d'idle doit garder la respiration (moving hold), sinon on lit un arrêt
  (et pour WCAG 2.2.2 on ne peut pas tout arrêter brutalement non plus, voir §2.6).

### 1.5 Arcs
- **[F]** La plupart des actions naturelles suivent une trajectoire en arc (Wikipedia, principes).
- **[D]** En AVD, un arc s'obtient en combinant une **rotation de groupe avec un pivot excentré** (la tête qui
  s'incline autour du cou) plutôt que translateX + translateY indépendants. Deux propriétés avec le même
  easing tracent une droite, pas un arc. `pathMotion`/`propertyXName`/`propertyYName` est aussi possible, mais
  il est pré-échantillonné sur RenderThread (voir §0.3).

### 1.6 Squash & stretch
- **[F]** Le volume doit rester constant quand on écrase ou étire
  (https://en.wikipedia.org/wiki/Twelve_basic_principles_of_animation).
- **[H]** Amplitudes pour un personnage « posé » et calme comme Glose : respiration 1 à 3 % (scaleY +2 % ⇒ scaleX −1 %
  environ), rebond de tap 5 à 8 %, special de 4 taps jusqu'à 10 à 15 % au maximum. Vérification : scaleX × scaleY ≈ 1
  (±0,03) à chaque frame. Pivot du squash à la base (sur le livre), jamais au centre, sinon le chat « flotte ».

### 1.7 Fréquences d'affichage (60/90/120 Hz)
- **[F]** Budget de frame : 16 ms à 60 fps, 11 ms à 90, 8 ms à 120. Dépasser d'1 ms ne retarde pas la frame : le
  Choreographer la **saute** (https://developer.android.com/topic/performance/vitals/render).
- **[D]** Sur RenderThread, les keyframes sont échantillonnées au pas tronqué (16/11/8 ms) relevé **au moment de
  la construction** de l'animateur (code AOSP cité au §0.3). Si l'écran change de fréquence pendant la boucle
  (fréquence adaptative), l'échantillonnage ne suit pas. **[H]** C'est sans effet visible tant que les segments
  font plus de 3 échantillons.
- **[H]** Critère de régularité : déplacement maximal entre deux frames consécutives ≤ 2 dp pour un mouvement idle
  et ≤ 8 dp pour un tap à 60 Hz (la moitié à 120 Hz, pour le même rendu temporel).

### 1.8 Durées de référence
- **[F]** UI : 100 à 500 ms au total, ~100 ms pour un retour simple, 200 à 300 ms pour un changement plus grand,
  « à 500 ms, ça commence à traîner » (https://www.nngroup.com/articles/animation-duration/). Jetons Material :
  50 à 1000 ms, en quatre familles (short 50–200, medium 250–400, long 450–600, extra long 700–1000), avec une
  durée qui croît avec la surface ou la distance parcourue (Motion.md cité ci-dessus).
- **[F]** Clignement humain : 100 à 150 ms (UCL) ou 100 à 400 ms (Harvard BioNumbers), un intervalle de 2 à 10 s
  entre deux clignements, environ 17/min au repos et 4 à 5/min **en lisant** (https://en.wikipedia.org/wiki/Blinking).
- **[D]/[H]** Pour Glose :
  - accusé de tap (le premier mouvement visible) : il doit commencer dans les 1 à 2 frames et culminer avant 100 à 150 ms ;
  - réaction au tap complète : 400 à 700 ms. Les 600 ms actuelles sont cohérentes, au plafond de Material « long4 » ;
  - special de 4 taps : 1200 à 2000 ms (1800 ms : ok, c'est une « récompense », on peut dépasser la norme UI) ;
  - clignement : 120 à 200 ms en tout. Intervalle aléatoire de 2 à 10 s, plus long quand Glose « lit » (4 à 5/min, cohérent avec un bibliothécaire) ;
  - cycle de respiration : 3 à 5 s (les 3 800 ms actuelles sont ok) ;
  - hochement de quiz : 350 à 600 ms.

### 1.9 Variation de l'idle (éviter la répétition mécanique)
- **[F]** Structure classique : une boucle de base (respiration) plus 2 à 4 idles secondaires déclenchés au hasard
  (https://mocaponline.com/blogs/mocap-news/idle-animation-design-guide). Il faut ajouter des « macro variations
  sur trois à six boucles » et ne pas répéter le même fidget à la suite
  (https://blog.animschool.edu/2024/06/14/breathing-life-into-idle-animations). Le tirage purement aléatoire peut
  ressortir deux fois la même variante (https://opsive.com/support/documentation/ultimate-character-controller/character/abilities/included-abilities/idle/).
- **[F]** Duolingo gère l'idle (hochements, clignements, sourcils) comme des comportements distincts des états de leçon, dans
  une machine à états Rive (https://blog.duolingo.com/world-character-visemes/).
- **[F]** Le « principe de la cigale » : superposer des cycles de longueurs **premières** entre elles retarde
  énormément la répétition visible du motif global
  (https://www.sitepoint.com/the-cicada-principle-and-why-it-matters-to-web-designers/).
- **[D]** Pour Glose : respiration 3 800 ms, queue 7 300 ms, pelote 11 000 ms… En choisissant des durées sans
  diviseur commun (ex. 3 700 / 7 300 / 11 300 ms), la combinaison ne se répète qu'après des minutes au lieu de
  quelques secondes. Les clignements et les fidgets ne doivent **pas** être cuits dans la boucle de 29 s : il vaut
  mieux un planificateur Kotlin (Handler/postDelayed) qui tire un intervalle 2 à 10 s, sans répéter la même variante
  deux fois de suite.

### 1.10 Échelonnement (staggering)
- **[F]** La durée doit croître avec la distance ou la surface (Motion.md). L'overlap fait bouger les parties à
  des vitesses différentes (Wikipedia).
- **[H]** Décalage entre maillons : 16 à 50 ms. Entre éléments indépendants (accessoire qui apparaît, puis
  étincelles) : 30 à 80 ms. Un échelonnement de plus de 100 ms par élément se lit comme une séquence et non plus
  comme un même geste.
- **[D]** Échelonner exprès est très différent de se décaler par accident (§0.1) : un décalage voulu se met en
  `startOffset`, jamais en jouant sur des durées différentes.

### 1.11 Raccords de boucle (loop seams)
- **[F]** Garder le personnage constamment en mouvement aide à masquer le raccord de boucle (blog AnimSchool
  ci-dessus).
- **[D]** Raccord propre = valeur **et** vitesse identiques à f = 0 et f = 1. Avec keyframes : première et dernière
  keyframe égales, et vitesse nulle des deux côtés (ease) **ou** tangentes égales. Avec `repeatMode="reverse"`, le
  raccord est automatiquement continu en valeur. Attention : l'interpolateur global (cf. §0.1) s'applique à chaque
  répétition et crée un ralenti à chaque raccord.

  Pour Glose, `Glose.play()` arrête l'idle par `stop()` au bout de 26 s (voir §2.2) : le raccord qui compte
  vraiment est celui de la **fin forcée**, pas celui de la boucle.

> Sources complémentaires (principe de la cigale appliqué à l'animation) :
> https://sfcgeorge.pages.dev/posts/2015/06/08/cicada-css-animation ,
> https://lea0.verou.me/2020/07/the-cicada-principle-revisited-with-css-variables

---

## 2. Spécificités Android (API 26+, Views, AVD)

### 2.1 Performance des AnimatedVectorDrawable
- **[F]** Depuis l'API 25, l'AVD tourne sur le RenderThread et reste fluide même si le thread UI est chargé. En
  contrepartie, on ne peut pas le coordonner précisément avec des animations du thread UI, et
  `onAnimationEnd` arrive **une frame après** la fin réelle
  (https://developer.android.com/reference/android/graphics/drawable/AnimatedVectorDrawable).
- **[F]** L'AVD repasse sur le thread UI si la vue est dessinée sur un calque logiciel (`LAYER_TYPE_SOFTWARE`) :
  méthode `fallbackOntoUI()` dans le code AOSP cité au §0.3. **[D]** Il ne faut donc jamais mettre
  `setLayerType(SOFTWARE)` sur l'ImageView de Glose. Aujourd'hui aucun appel `setLayerType` n'existe dans le code.
- **[F]** Chaque VectorDrawable a **un cache bitmap**. Si plusieurs références à une même instance n'ont pas la même
  taille, ce bitmap est recréé à chaque changement de taille
  (https://developer.android.com/reference/android/graphics/drawable/VectorDrawable).
- **[F]** Le premier chargement d'un vecteur coûte plus de CPU qu'un bitmap. Google recommande de limiter un
  vecteur à **200 × 200 dp**, sinon il peut être trop long à dessiner
  (https://developer.android.com/studio/write/vector-asset-studio).
  **[D]** Les vecteurs Glose font 256 × 256 dp (viewport 820). On est au-dessus de la recommandation, mais le
  découpage en calques limite la casse : seuls les calques animés à un instant donné sont re-rastérisés. Pour la
  vue principale, on peut viser ≤ 200 dp ou au minimum le vérifier au profileur (§2.5).
- **[F]** « Don't modify shapes too often » : chaque modification de chemin crée un nouveau masque côté GPU
  (https://developer.android.com/develop/ui/views/graphics/hardware-accel). Les gros chemins ne doivent pas être
  modifiés d'une frame à l'autre (https://developer.android.com/topic/performance/vitals/render, « Animating large Paths »).
  **[D]** Pour les animateurs de Glose, cela donne : transformations de **groupe** (rotation, scale, translate) <
  alpha < `trimPath*` < morphing de `pathData`. Il faut réserver le morphing aux petits chemins (paupières,
  bouche) et animer les grosses masses (corps, livre) par transformations de groupe.
- **[F]** Morphing : les deux chemins doivent avoir les mêmes commandes, dans le même ordre, avec le même nombre de
  paramètres (AVD reference). **[F]** Sur RenderThread, une animation de `pathData` ne passe que la valeur de
  départ et la valeur d'arrivée (`createRTAnimatorForPath` : `startValue` / `endValue`, code AOSP §0.3).
  **[D]** Un morphing à plus de 2 keyframes dans un seul animateur risque de perdre ses étapes intermédiaires
  sur RT : il faut l'enchaîner en plusieurs `objectAnimator` séquentiels.
- **[F]** `trimPathStart`, `trimPathEnd` et `trimPathOffset` sont animables sur `<path>` (AVD reference). **[D]** Pour le
  mot écrit au fil : un seul chemin continu par tracé ; un `trimPathEnd` sur un chemin de N sous-tracés répartit le
  « temps » selon la longueur, donc les sauts de plume (move-to) apparaissent instantanés. On met un
  sous-chemin par lettre ou par trait, chacun avec son propre `startOffset`, si la vitesse d'écriture doit
  paraître constante.
- **[F]** Calques matériels : ils accélèrent les animations de propriétés de **vue** (alpha, translation, scale,
  rotation), à activer seulement pendant l'animation ; un calque n'est pas redessiné tant que la vue n'est pas
  invalidée (https://developer.android.com/develop/ui/views/graphics/hardware-accel). **[D]** Un AVD invalide son
  contenu à chaque frame : un `LAYER_TYPE_HARDWARE` sur l'ImageView serait re-rendu à chaque frame et
  n'apporterait rien (voire coûterait de la mémoire vidéo). Il ne sert que si on anime la *vue* entière
  (ex. Glose qui glisse depuis le bas) alors que les AVD sont à l'arrêt.
- **[F]** Alpha sur une vue entière = rendu hors écran, donc fill-rate doublé (même page). **[D]** On anime
  `fillAlpha`/`strokeAlpha` des chemins (ou l'alpha du groupe racine) plutôt que `View.alpha` pour les fondus
  jour/nuit.

### 2.2 Garder plusieurs AVD synchrones (le LayerDrawable de Glose)
- **[F]** Sur RT, `start()` démarre **immédiatement** si l'AVD a déjà été dessiné et que son RenderNode est attaché.
  Sinon, il met l'action en attente et l'exécute au prochain dessin (`useLastSeenTarget` /
  `addPendingAction(START_ANIMATION)`, code AOSP §0.3). **[D]** Dans `Glose.once()`, on fait `setImageDrawable(d)` puis
  `start()` sur tous les calques. Aucun n'a encore été dessiné, donc tous démarrent au même prochain dessin :
  c'est correct. En revanche, dans `play()` sur un drawable **déjà affiché**, si un calque a été dessiné et un
  autre non (accessoire ajouté, calque masqué par `setVisible(false)` ou par des bounds vides), ils peuvent
  démarrer à une frame d'écart.
- **[F]** `onAnimationEnd` est appelé la frame **après** la fin sur RT (AVD reference). **[D]** Dans `once()`, le retour
  à l'idle attend le **dernier** calque terminé (`--left > 0`), puis remplace le drawable. Il peut donc y avoir 1 à 2 frames
  où la pose finale de la réaction reste affichée, puis la pose idle. Si les deux poses ne sont pas
  **identiques au pixel près** (cf. checklist), on voit un « pop ». Remède : la dernière keyframe de chaque
  réaction = la première frame de l'idle, exactement (mêmes valeurs, mêmes calques visibles).
- **[F]** `stop()` d'un AVD appelle `end()`, ce qui **saute aux valeurs de fin** (code AOSP : `mAnimatorSet.end()`
  puis `nEnd`). **[D]** `Glose.play()` arrête l'idle au bout de `IDLE_MS = 26 000` ms par `stop()`. Toute boucle
  dont la durée ne divise pas 26 000 (3 800, 4 400, 7 000, 7 300, 11 000, 29 000…) est alors coupée au milieu de son
  cycle et **saute** à sa valeur finale. D'après le script `scratchpad/anim/tools/avd_stop_snap.py` (modèle à valider
  sur appareil), 133 propriétés en boucle ne sont pas à leur valeur de fin à 26 s. Certaines sont sans doute
  invisibles à cet instant (balayage de reflet hors masque), mais la pose « soir » (translateY de 37 à 52 unités de
  viewport, soit ~12 à 16 dp) ou des rotations de 150° méritent un contrôle visuel. Remède : arrêter sur une
  frontière de cycle commune (PPCM des durées, ou un `IDLE_MS` qui soit un multiple de toutes les boucles), ou
  laisser finir le cycle en cours (`repeatCount` fini calculé pour tomber juste sur ~26 s), plutôt que `stop()`.
- **[D]** « Même horloge » : sur RT, tous les AVD d'un même RenderNode avancent avec le même temps de frame.
  Ce qui les désynchronise, ce n'est pas l'horloge mais (a) des **départs** à des frames différentes, (b) des
  **interpolateurs globaux** différents ou absents (§0.1), (c) des durées différentes pour des mouvements censés
  être liés. Règle : pour deux calques qui doivent bouger ensemble (corps + accessoire porté), on utilise **exactement le
  même animateur** (même fichier `@animator/glose_k_*`, ce que le générateur fait déjà par hachage). On vérifie
  que la tête et le chapeau référencent bien le même.
- **[F]** `getAlpha()` (et les getters en général) ne sont pas fiables pendant une animation RT (AVD reference) :
  on ne pilote jamais une logique de synchronisation en lisant l'état d'un AVD en cours.

### 2.3 Interpolateurs disponibles (API 26+) et comment les écrire dans le générateur
- **[F]** `PathInterpolator` (API 21) en XML : `<pathInterpolator android:controlX1 … controlY2/>` ou
  `android:pathData`. Interpolateurs système et `@android:interpolator/fast_out_slow_in` (sources §1.2).
- **[F]** Les keyframes acceptent chacune un interpolateur pour l'intervalle qui les précède ; `null` = linéaire
  (https://developer.android.com/reference/android/animation/Keyframe).
- **[D]** Recette pour le générateur Paper.js → AVD :
  1. `android:interpolator="@android:interpolator/linear"` sur **chaque** `objectAnimator` qui porte des
     keyframes (sinon AccelerateDecelerate global, §0.1).
  2. Pour chaque keyframe, l'interpolateur du segment se choisit selon la nature des deux bornes :
     extrême → extrême : ease-in-out ; extrême → passage : ease-in `(0.42,0,1,1)` ; passage → extrême :
     ease-out `(0,0,0.58,1)` ; passage → passage : linéaire. Le résultat est une vitesse continue aux points
     de passage, nulle seulement aux extrêmes.
  3. Une alternative plus robuste consiste à échantillonner la courbe Paper.js à 60 points/s et à écrire des
     keyframes linéaires. C'est fidèle au timing dessiné, mais cela grossit le XML et reste soumis à la limite de
     300 points par animateur sur RT (§0.3), donc ≤ 5 s par animateur à 60 points/s.
- **[F]** Ressorts Material (`motionSpring*`) : ils passent par AndroidX DynamicAnimation (Motion.md). **[D]** Ils ne sont
  donc pas disponibles ici, mais on peut les **cuire** en keyframes (overshoot amorti) dans le générateur.

### 2.4 Guidance Material motion utile pour une mascotte
- **[F]** Choisir la courbe selon la direction (decelerate pour ce qui entre, accelerate pour ce qui sort) ; durée
  proportionnelle à la distance ou surface ; ressorts « effects » sans overshoot pour couleur et opacité (Motion.md).
- **[F]** Les durées Material vont de 50 à 1000 ms (Motion.md) ; NN/g conseille 100 à 400 ms pour l'UI (§1.8).
  **[D]** Une mascotte n'est pas un composant d'UI : ses réactions peuvent dépasser 400 ms. En revanche, la
  **première réponse visible** au tap doit rester dans la fenêtre UI (≤ 100 à 150 ms jusqu'au pic d'anticipation).

### 2.5 Mesurer le jank
- **[F]** Budgets 16/11/8 ms (60/90/120 Hz). Outils : Profile GPU Rendering (barres à l'écran), Systrace et
  Perfetto (FrameTimeline), `FrameMetricsAggregator` en production. Il faut tester une build **non debuggable** :
  ART désactive des optimisations en debug (https://developer.android.com/topic/performance/vitals/render).
- **[F]** `adb shell dumpsys gfxinfo <package>` et `adb shell dumpsys gfxinfo <package> framestats`
  (https://developer.android.com/tools/dumpsys). La sortie donne le nombre et le pourcentage de « Janky frames »
  et les percentiles 50/90/95/99. Framestats donne les timestamps des ~120 dernières frames ; durée de frame =
  `FRAME_COMPLETED − INTENDED_VSYNC` pour les lignes à FLAGS = 0 (copie archivée de la doc officielle :
  https://webarchive.library.unt.edu/web/20160706090017mp_/https://developer.android.com/training/testing/performance.html).
- **[D]** Protocole Glose : `dumpsys gfxinfo fr.definiday reset`, laisser tourner 26 s d'idle sur l'écran principal,
  `dumpsys gfxinfo fr.definiday`, puis noter le 90e et le 99e percentile. Recommencer avec 10 taps et 1 special.
  À faire sur un appareil 60 Hz d'entrée de gamme et un appareil 120 Hz. **Attention** : une saccade due à la
  **courbe** (§0, points 1 et 2) ne se voit pas dans gfxinfo, car les frames sont livrées à l'heure. Elle se voit
  seulement en traçant les valeurs (tests unitaires sur l'évaluation des keyframes) ou en filmant au ralenti
  (240 fps).

### 2.6 Accessibilité : réduction des animations
- **[F]** WCAG 2.2.2 (niveau A) : un contenu en mouvement qui démarre seul, dure plus de 5 s et s'affiche à côté
  d'autre contenu doit pouvoir être mis en pause, arrêté ou masqué
  (https://www.w3.org/WAI/WCAG22/Understanding/pause-stop-hide.html).
- **[F]** WCAG 2.3.3 (niveau AAA) : une animation de mouvement déclenchée par une interaction doit pouvoir être
  désactivée, sauf si elle est essentielle. Une des techniques consiste à respecter le réglage « réduire les
  animations » du système (https://www.w3.org/WAI/WCAG22/Understanding/animation-from-interactions.html).
- **[F]** `ValueAnimator.areAnimatorsEnabled()` (API 26) renvoie false si l'échelle de durée d'animation est à 0 ou
  si l'économiseur de batterie est actif. `getDurationScale()` et `registerDurationScaleChangeListener()` sont en
  API 33 (https://developer.android.com/reference/android/animation/ValueAnimator).
- **[F]** Le réglage d'accessibilité Android « Supprimer les animations » (Remove animations) met l'échelle de
  durée de l'animateur à Off (rapport de développeurs :
  https://lightrun.com/answers/facebook-react-native-android-remove-animations-setting-isnt-reflected-when-using-accessibilityinfo ;
  emplacement du réglage : https://mcmw.abilitynet.org.uk/how-to-disable-interface-animations-in-android-14).
- **[F]** L'AVD sur RT multiplie `startDelay` et `duration` par `ValueAnimator.getDurationScale()` **au moment où
  l'animateur est construit** (`createNativeChildAnimator`, code AOSP §0.3). **[D]** Avec une échelle de 0,5 ou de 2
  (réglage développeur), Glose se joue deux fois plus vite ou plus lentement, mais `IDLE_MS` (26 s, minuterie
  `postDelayed`) ne change pas. L'idle est alors coupé à un autre endroit du cycle. Il faut multiplier `IDLE_MS` par
  l'échelle (lisible via `Settings.Global.ANIMATOR_DURATION_SCALE` en API 26 à 32).
- **[D]** État actuel de Glose : `areAnimatorsEnabled()` est testé avant `play`, `once` et le long-press, et la pause
  manuelle (WCAG 2.2.2) existe via appui long et l'action TalkBack. C'est conforme à l'esprit des deux critères. Il manque
  la gestion d'une échelle **≠ 0 et ≠ 1** (ci-dessus).

---

## 3. Modèles : micro-animations de personnages à étudier

Les images de référence sont rangées hors du dépôt, dans
`/tmp/claude-0/-home-user-Definiday/5dd8da8e-acaa-53f1-b2be-feeef333d005/scratchpad/anim/modeles/<NN_nom>/`
(`source.*` = og:image ou image de la page, `tile.png` = vignette choisie, `source.txt` = URL d'origine). Planche
contact : `/tmp/claude-0/-home-user-Definiday/5dd8da8e-acaa-53f1-b2be-feeef333d005/scratchpad/anim/modeles/modeles.jpg`.
Ce sont des œuvres de tiers, à garder comme **référence seulement** : on n'en reprend que des principes, jamais le dessin.

### 3.1 Duolingo : les personnages du « World » (Lily, Junior, Oscar…)
- Page : https://blog.duolingo.com/world-character-visemes/
- **[F]** Une machine à états Rive par personnage, avec des états de poses pour la leçon et des états de bouche
  (plus de 20 visèmes) qui « doivent tourner en même temps que les états d'animation du personnage ». Les
  comportements d'idle (hochements, clignements, sourcils) sont pris en charge à part. Le timing de la parole vient
  des horodatages de mots et de phonèmes. Si l'apprenant finit avant, le personnage s'arrête de parler à temps,
  puis passe à un état final selon la réponse (juste ou fausse).
- Ce qui la rend bonne **[D]** : les couches sont orthogonales (idle, corps, bouche), l'interruption est propre, et la
  fin d'exercice amène un état final explicite qui dépend du résultat.
- À appliquer à Glose : séparer clairement l'idle de fond (respiration, clignement) et les réactions ; chaque
  réaction se termine sur un état final défini (hochement « oui », inclinaison « hmm »). Une réaction doit pouvoir
  couper l'idle **sans saut** : on part de la pose courante ou on revient d'abord à une pose pivot.

### 3.2 Duolingo Lily en appel vidéo (Rive)
- Page : https://rive.app/blog/duolingo-s-ai-powered-video-call-brings-lily-to-life
- **[F]** « Eight different head animations and eight body animations that dynamically combine », soit plus de 64
  variations neutres, ce qui « prevented repetitive idle loops ». La tête et le corps sont séparés (artboards
  imbriqués) pour bouger indépendamment. Pendant les délais de traitement, Lily penche la tête ou « réfléchit »
  pour que l'attente paraisse « intentional rather than mechanical ». Fichier final de moins d'1 Mo.
- Ce qui la rend bonne **[D]** : une variété combinatoire (8 + 8 éléments donnent 64 combinaisons) et des
  attentes habitées.
- À appliquer à Glose : 3 ou 4 micro-idles de tête (regard, oreille, lunettes remontées) et 3 ou 4 de corps/queue,
  planifiés **indépendamment** par du code. Une pose « réflexion » pendant le chargement de la
  définition ou du quiz. Le découpage en calques AVD de Glose s'y prête déjà.

### 3.3 Notion AI (studio Buck)
- Page : https://buck.co/work/notion-ai
- **[F]** Une animation commencée en cel « to give the performances an innately handmade feel ». Avec « just eyes,
  brows, and a nose », des comportements qui réinventent les états d'UI : « eyebrows that wave while thinking »,
  « a face that momentarily falls apart for errors ». Le visage doit « transition seamlessly between any state ». Un
  personnage « alert and engaged, but not distracting ». Réalisé avec la State Machine de Rive.
- Ce qui la rend bonne **[D]** : très peu d'éléments, une forte lisibilité, et des transitions possibles de
  n'importe quel état vers n'importe quel autre.
- À appliquer à Glose : les **lunettes et les sourcils** sont ses meilleurs vecteurs d'expression, et ce sont de
  petits chemins (donc bon marché à animer, §2.1). On définit une **pose pivot** neutre par laquelle tout peut
  transiter. L'idle doit rester en dessous du seuil de distraction : amplitudes faibles (§1.6), pas de mouvement
  rapide sans déclencheur.

### 3.4 Headspace (Nexus Studios)
- Page : https://nexusstudios.com/work/headspace/ (GIF analysé : `HS_BalloonPuff.gif`)
- **[F]** Nexus a conçu « over 150 unique characters and environments » pour Headspace, dont une bibliothèque
  d'animations dans l'app au style « bold and playful ».
- **[D]** (mesure sur le GIF public) : 131 images × 40 ms = **5,24 s**. Une image sur deux est identique à la
  précédente (différence moyenne ≈ 0), soit une animation « en deux » (12,5 dessins/s). Ce n'est **pas** une boucle
  sans raccord : grosse différence entre la dernière et la première image, c'est une transition.
- Ce qui la rend bonne **[D]** : la fluidité perçue vient du spacing, des poses fortes et des tenues, pas de la
  cadence. Un 12,5 i/s bien espacé paraît plus vivant qu'un 60 i/s en « stop and go ».
- À appliquer à Glose : soigner les poses-clés et les tenues avant de chercher à lisser. Ne **pas** imiter
  l'animation en deux en AVD (elle serait lue comme du jank sur un rendu vectoriel lisse) ; garder 60/120 Hz
  continus.

### 3.5 Headspace Ebb (compagnon IA, blog Figma)
- Page : https://www.figma.com/blog/headspace-ebb-ai-companion/
- **[F]** Un « friendly blob », référence « lava lamp » pour l'idée que les émotions vont et viennent ; « You want
  to feel like it's listening, but not too happy », car les membres arrivent parfois avec des émotions lourdes ; le
  mouvement final est « supportive, but gives you space ».
- Ce qui la rend bonne **[D]** : l'intensité de l'animation est calibrée sur l'état émotionnel probable de
  l'utilisateur.
- À appliquer à Glose : après une mauvaise réponse au quiz, une réaction **sobre et empathique** (petite
  inclinaison, durée courte), jamais moqueuse ni sautillante. La joie se garde pour la réussite et les specials.

### 3.6 Google : les points de l'Assistant (identité Google)
- Page : https://design.google/library/evolving-google-identity
- **[F]** « The Google dots are a dynamic and perpetually moving state of the logo. » Des expressions pour
  « listening, thinking, replying, incomprehension, and confirmation ». « Their motion is rooted in consistent paths
  and timing, with the dots moving along geometric arcs and following a standard set of snappy easing curves. »
- **[D]** (mesure sur le GIF de la page) : 51 images × 30 ms = 1,53 s pour la séquence logo → points → G.
- Ce qui la rend bonne **[D]** : un **vocabulaire** de mouvement fermé (quelques trajectoires en arc, quelques
  courbes) réutilisé partout, ce qui donne une identité cohérente.
- À appliquer à Glose : définir 3 ou 4 courbes « maison » (respiration, impulsion, retour, écriture) et une gamme
  de durées, puis n'utiliser que celles-là (des « tokens » de mouvement, cf. §1.2). Les trajectoires de tête et de
  queue se font en arc (rotation autour d'un pivot), pas en ligne droite.

### 3.7 Mailchimp : le « high five » de Freddie
- Page : https://mailchimp.com/resources/designing-the-freddie-high-five-animation-for-facebook-ads/
- **[F]** La main de Freddie **tremble** au-dessus du gros bouton rouge, on clique sur envoyer, puis vient le high
  five. Il n'apparaît que lorsqu'on envoie **vraiment** un email. Selon Mailchimp, c'est « one of the most
  satisfying pieces of the Mailchimp experience ». Pour la variante publicité Facebook, l'équipe a choisi la
  fronde pour son ressenti « launch and wait », fidèle au fait que les pubs s'affichent avec le temps. Le
  processus : croquis « moches », raffinage, conseils d'une animatrice, « a lot of tweaking », deux semaines.
- Ce qui la rend bonne **[D]** : une tension (anticipation) puis une récompense méritée et rare, et une métaphore
  qui correspond au fonctionnement réel du produit.
- À appliquer à Glose : le special à 4 taps et la réussite au quiz sont des **récompenses méritées**. On les annonce
  par une petite anticipation (3 frames de « tension »), on les garde rares, et on itère au ralenti avant de valider.

### 3.8 Finch (essai d'animation par Nikita Wibisono)
- Page : https://contra.com/p/seOAX3PZ-expressive-character-animation-for-finch-app
- **[F]** Un rig à « Joysticks so the head and body could rotate and deform », pour un effet 2,5D subtil sans
  changer le design. Un rig réutilisable qui accélère les animations suivantes. Pour le « Heart Beam » :
  « anticipation before the beam, gradually building energy before the payoff ». Export compatible Lottie, en
  évitant les vidéos et GIF lourds. Les jambes restent hors champ pour garder les personnages grands à l'écran.
- Ce qui la rend bonne **[D]** : une fausse 3D bon marché (les traits se décalent dans la tête), une charge
  d'énergie progressive avant le gros mouvement, et des choix de cadrage pragmatiques.
- À appliquer à Glose : pour un regard de côté, translater yeux, lunettes et museau de 2 à 4 % **à l'intérieur** du
  contour de la tête, plutôt que tourner toute la tête (c'est l'effet joystick, faisable avec des groupes AVD). Pour
  les specials, une montée en énergie sur 200 à 400 ms avant le pic.

### 3.9 Pokémon Sleep
- Page : https://www.pokemonsleep.net/en/ (et https://www.pokemonsleep.net/en/pokemon/)
- **[F]** On collectionne des « sleep styles » : les Pokémon qui dorment comme vous viennent se rassembler autour de
  Ronflex (Snorlax), et le sommeil est classé en trois stades, Dozing, Snoozing et Slumbering. La page ne décrit
  pas l'animation elle-même.
- **[H]** Ce qui la rend bonne (observation de l'app, non sourcée ici) : des boucles de sommeil très lentes et
  amples, peu nombreuses, sans geste brusque.
- **[F]** Repère physiologique : un adulte au repos respire 12 à 15 fois par minute, soit environ 4 à 5 s par cycle
  (https://en.wikipedia.org/wiki/Respiratory_rate).
- À appliquer à Glose : en pose « soir » (endormi), respiration lente de 4 à 6 s par cycle, amplitude faible,
  aucun fidget rapide. C'est aussi la pose où le saut d'arrêt à 26 s (§2.2) se verrait le plus : il faut la traiter
  en priorité.

### 3.10 Apple Human Interface Guidelines : Motion (référence de principes, pas une mascotte)
- Page : https://developer.apple.com/design/human-interface-guidelines/motion
- **[F]** « Add motion purposefully… Don't add motion for the sake of adding motion. » « Make motion optional. »
  « Aim for brevity and precision in feedback animations. » « In apps, generally avoid adding motion to UI
  interactions that occur frequently. » « Let people cancel motion… don't make people wait for an animation to
  complete. » Pour les jeux, « a consistent frame rate of 30 to 60 fps ».
- À appliquer à Glose : les taps répétés ne doivent pas rejouer à chaque fois la même grande réaction. Le
  `QUIET_MS` actuel y contribue ; on peut aussi atténuer l'amplitude au 2e ou 3e tap. Aucune action de
  l'utilisateur (répondre, passer au mot suivant) ne doit attendre la fin d'une animation de Glose.

> Non traités ici : Calm, Slack, Habitica, Forest, Memoji/Animoji. Je n'ai pas trouvé, dans le temps imparti,
> de page officielle décrivant leur animation de personnage avec des détails exploitables ; ils restent à
> explorer. Les articles de tiers (dev.to, etc.) qui reconstituent des rigs « façon Duolingo » ne sont pas des
> sources primaires et n'ont pas été retenus.

---

## 4. Checklist testable pour les animations de Glose (28 points)

Unités : viewport Glose = 820 unités pour 256 dp, donc **1 unité ≈ 0,31 dp** (lu dans `glose_assis_vd_1.xml`).
« Statique » = vérifiable par script sur les XML ; « Appareil » = mesure sur téléphone. La colonne « État » donne ce
que j'ai déjà mesuré sur le dépôt ([D], modèle à confirmer sur appareil).

| # | Critère | Seuil / règle | Méthode | État connu |
|---|---|---|---|---|
| 1 | Chaque `objectAnimator` à keyframes a un interpolateur global **linéaire** | 100 % | Statique (regex) | **0/297** : échec (§0.1) |
| 2 | Pas d'arrêt de vitesse sur une keyframe de passage (valeur strictement entre ses voisines) | vitesse entrante et sortante > 0, rapport entre 0,5 et 2 | Statique (évaluer dérivée gauche/droite) | à mesurer ; `glose_ease` partout ⇒ échec probable (§0.2) |
| 3 | Aucun segment en mouvement plus court que 2 frames à 60 Hz | ≥ 33 ms, sauf « snap » volontaire documenté | Statique | 16 segments signalés (`avd_sampling.py`) |
| 4 | Le pré-échantillonnage RT ne rogne aucun segment | chaque segment ≥ 3 pas d'échantillon (pas = durée / min(300, durée / 8 ms)) | Statique (`avd_sampling.py`) | ok pour les boucles longues ; voir #3 |
| 5 | Morphing de `pathData` : 2 valeurs par animateur, chemins compatibles | mêmes commandes, même ordre, même nombre de paramètres | Statique | à mesurer |
| 6 | Raccord de boucle continu | `repeatCount=infinite` ⇒ 1re keyframe = dernière, et vitesses égales aux deux bouts | Statique | à mesurer |
| 7 | Les boucles idle ne se « re-synchronisent » pas vite | PPCM des durées de boucles d'une pose ≥ 60 s ; pas de rapport 1:2 ou 1:3 involontaire | Statique | à mesurer |
| 8 | L'arrêt de l'idle (`stop()` = saut à la fin) ne fait rien bouger | à `IDLE_MS`, chaque propriété en boucle est à sa valeur finale à ±0,5 unité, ±0,2°, ±0,005 d'échelle, ±0,01 d'alpha | Statique (`avd_stop_snap.py`) puis appareil | **133 propriétés signalées** (certaines masquées) |
| 9 | Fin de réaction = première frame de l'idle | mêmes tolérances que #8, pour chaque cible et chaque calque visible | Statique | à mesurer |
| 10 | Corps, accessoire porté et contour de nuit d'une même partie partagent le même animateur | même `@animator/…` sur `tete`, chapeau et `tete_halo` | Statique | partiel (ex. `glose_k_37bcf792` partagé) |
| 11 | Squash & stretch à volume constant | scaleX × scaleY ∈ [0,97 ; 1,03] à chaque pas de 16 ms (hors apparitions 0 → 1) | Statique (évaluation) | à mesurer |
| 12 | Pas de saut de position entre deux frames | à 60 Hz : ≤ 2 dp/frame en idle, ≤ 8 dp/frame en réaction | Statique (évaluation à 16,67 ms) | à mesurer |
| 13 | Pas de saut de rotation | ≤ 3°/frame en idle, ≤ 12°/frame en réaction (60 Hz) | Statique | à mesurer |
| 14 | Pas d'easing linéaire sur un mouvement non bouclé | linéaire autorisé seulement : interpolateur global, rotation continue, milieu de trimPath | Statique | à mesurer |
| 15 | Alpha et couleurs ne dépassent jamais [0 ; 1] | pas d'overshoot sur `fillAlpha`/`strokeAlpha`/`alpha` | Statique | à mesurer |
| 16 | Overlap : les parties traînantes suivent | lunettes, oreilles, queue et accessoires démarrent 16 à 50 ms après la partie motrice et se posent après elle | Statique (comparer les keyframes de pic) | à mesurer |
| 17 | Réponse au tap immédiate | 1er mouvement ≤ 2 frames après le tap ; pic d'anticipation ≤ 150 ms | Appareil (vidéo 240 i/s) + statique | à mesurer |
| 18 | Durées dans les gammes | réaction 400 à 700 ms ; special 1200 à 2000 ms ; hochement 350 à 600 ms ; clignement 120 à 200 ms (fermeture 60 à 80, ouverture 100 à 150) | Statique | réactions 600 ms, specials 1800 ms : ok |
| 19 | Clignements et fidgets non périodiques | intervalle tiré dans [2 ; 10] s ; jamais deux fois la même variante d'affilée | Code (test unitaire du planificateur) | aujourd'hui intégrés dans la boucle de 29 s : échec |
| 20 | Respiration crédible | cycle de 3 à 6 s ; amplitude d'échelle de 1 à 3 % ; pivot à la base | Statique | 3 800 ms : ok (amplitude à mesurer) |
| 21 | Tous les calques démarrent sur la même frame | 0 frame d'écart entre les `start()` effectifs | Appareil (Perfetto, ou build debug qui journalise `onAnimationStart` avec `Choreographer` frameTime) | probable ok dans `once()`, à vérifier dans `play()` (§2.2) |
| 22 | Les calques d'une réaction finissent ensemble | même `getTotalDuration()` pour tous les AVD d'une pose one-shot | Test instrumenté | à mesurer |
| 23 | Échelle de durée respectée | `IDLE_MS` multiplié par l'échelle ; à 0, image fixe ; à 0,5 et 2, l'arrêt tombe encore sur une frontière de cycle | Test instrumenté + appareil (options développeur) | échelles ≠ 0/1 non gérées (§2.6) |
| 24 | Pause WCAG 2.2.2 sans saut | appui long ou action TalkBack ⇒ arrêt sur la pose de repos, reprise propre | Appareil + TalkBack | fonction présente ; saut à vérifier (#8) |
| 25 | Pas de calque logiciel ; taille raisonnable | aucun `LAYER_TYPE_SOFTWARE` sur Glose ; ≤ 200 dp ou profilage | Statique (grep) + profilage | grep : ok ; taille : 256 dp |
| 26 | Pas de jank de rendu | pendant 26 s d'idle + 10 taps : « Janky frames » < 1 %, 99e percentile < 1,5 × budget de frame, sur un appareil 60 Hz d'entrée de gamme et un 120 Hz | Appareil (`dumpsys gfxinfo … reset` puis `framestats`) | à mesurer |
| 27 | Pas de log de sous-échantillonnage | 0 occurrence de « Duration for the animation is too long » | Appareil (`adb logcat -s AnimatedVectorDrawable`) | 240 animateurs à keyframes > 2 400 ms : présence probable à 120 Hz |
| 28 | Revue au ralenti | aucune pause visible entre keyframes, aucun « pop » au changement de drawable | Appareil (vidéo 240 i/s, revue image par image) | à faire |

### Outils déjà écrits (scratchpad, hors dépôt)
- `/tmp/claude-0/-home-user-Definiday/5dd8da8e-acaa-53f1-b2be-feeef333d005/scratchpad/anim/tools/avd_sampling.py <res>` : points 3 et 4.
- `/tmp/claude-0/-home-user-Definiday/5dd8da8e-acaa-53f1-b2be-feeef333d005/scratchpad/anim/tools/avd_stop_snap.py <res> [ms]` : point 8.
  C'est un modèle : il applique AccelerateDecelerate puis `glose_ease`, comme le framework, mais ne gère pas
  `startOffset` au-delà du premier cycle ni les calques masqués.
- Les points 2, 11, 12, 13, 15 et 16 relèvent d'un même évaluateur de keyframes : échantillonner chaque propriété à
  16,67 ms et à 8,33 ms, puis appliquer les seuils. C'est un petit script à écrire à partir de `avd_stop_snap.py`.

---

## 5. Ordre d'action suggéré ([D])
1. Générateur : interpolateur global `linear` sur tous les animateurs à keyframes (§0.1). C'est un changement en
   une ligne dans le générateur, au plus fort effet attendu sur le « saccadé » comme sur le « décalé ».
2. Générateur : easing par keyframe selon le rôle extrême/passage (§2.3). C'est le deuxième plus fort effet sur
   le « saccadé ».
3. `Glose.play()` : arrêt sur une frontière de cycle et `IDLE_MS` multiplié par l'échelle de durée (§2.2, §2.6). Cela
   supprime le saut de fin d'idle.
4. Sortir clignements et fidgets de la boucle de 29 s : animateurs courts relancés par le code avec des
   intervalles aléatoires (§1.9). On gagne en variété et on évite le sous-échantillonnage.
5. Repasser la checklist, puis faire la revue vidéo 240 i/s.
