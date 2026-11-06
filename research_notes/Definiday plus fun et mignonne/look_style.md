# Look et style : rendre Definiday plus fun et mignonne (cosy)

Légende : **[fait]** = sourcé (URL) ; **[repo]** = constaté dans le code ou les captures de Definiday ; **[inférence]** = raisonnement à partir des faits ; **[hypothèse]** = à tester.

État de départ, relevé avant la recherche (captures `scratchpad/revue_ui/*.png`, `themes.xml`, `colors.xml`, `critique_design.md`) :
- **[repo]** Palette crème `#FAF7F0`, surface `#F1EADB`, accent or `#886A34` (4,7:1), encre `#1F1A14`. Nuit : `#0F1424`, accent `#C9A96E`. Boutons `Btn` : surface + contour accent 1 dp, coins **12 dp** (`drawable/btn.xml`), `stateListAnimator` à `@null` (aucun retour d'appui hors ripple). Labels en Roboto gras, capitales, interlettrage 0,15. Corps serif. Les 14 polices décoratives servent aux mots.
- **[repo]** Monsieur Glose est déjà fait en `AnimatedVectorDrawable` (AVD, `glose_*.xml` → `glose_*_vd.xml`). `Glose.play()` vérifie `ValueAnimator.areAnimatorsEnabled()`. La pose d'accueil boucle à l'infini : 11 cibles `repeatCount="infinite"` de 3,8 à 6,4 s. Un toucher relance l'animation, il ne la met pas en pause (`MainActivity.kt:58,81`).
- **[repo]** Le code n'a ni haptique, ni son, ni dépendance d'exécution (`build.gradle` : junit et robolectric en test). L'APK de release pèse **1 618 054 octets**.
- **[repo]** Accueil (captures, peut-être antérieures à la 0.8.0, qui ajoute le style `Primary`) : Glose flotte au-dessus d'une carte en dégradé. En dessous, 5 blocs contour de même poids. La carte est « le meilleur élément de l'appli » (`critique_design.md`).
- **[repo]** Fin de quiz (`quiz_bravo.png`) : « Nouveau : Le fil écrit « mot », Le fil écrit « encore », … Nœud papillon » s'affiche en un seul paragraphe gras centré, entre deux Glose, puis viennent deux boutons. Le moment de récompense est le moins mis en scène de l'appli.
- **[repo]** Collection : grille de 3 colonnes. L'élément porté est encadré. Les éléments verrouillés sont des silhouettes grises identiques.
- **[repo]** Réglages : liste plate, avec Roboto et serif mêlés et des menus Material par défaut. C'est l'écran le moins « Definiday ».

---

## 1. Quel langage visuel utilisent les applis et jeux cosy qui marchent ?

### Takeaway
Les marques cosy qui réussissent appliquent peu de règles, mais strictement : formes arrondies sans angles vifs, **peu de formes par dessin**, un personnage exagéré et expressif, beaucoup d'espace vide autour de lui, et un mouvement qui sert à attirer l'œil et à rassurer. La texture et le grain sont rarement documentés comme leviers. Les vrais leviers documentés sont la forme, la silhouette, le timing et la retenue (célébrations rares, haptique discrète).

### Cited Findings
**Formes et dessin**
- Chez Duolingo, toutes les formes ont des bords arrondis et « pointy shapes are off-brand ». Principe affiché : utiliser le moins de formes possible. — [design.duolingo.com/illustration](https://design.duolingo.com/illustration) (via résumé de recherche)
- Duolingo est passé de « static, hard-edged shapes » à des formes plus rondes et plus amicales, avec des « rounded buttons on a white background ». La règle : « the fewest details needed to get the point across », et chaque forme clé doit avoir assez d'espace pour se lire en silhouette. — [Duolingo blog, Shape language](https://blog.duolingo.com/shape-language-duolingos-art-style/)
- Toujours chez Duolingo, personnages et accessoires sont exagérés « almost to the point of caricature ». L'illustration est posée dans un espace vide pour ne pas gêner l'interface, et ses lignes orientent l'œil vers le texte ou l'UI. Le style ludique « makes learners less afraid of mistakes », et les icônes animées retiennent l'attention plus longtemps. — [Duolingo blog, Shape language](https://blog.duolingo.com/shape-language-duolingos-art-style/)
- Lors de la refonte de Duo, l'identité (couleur, espèce) a été figée d'abord, puis la silhouette avant les détails : « if the overall shape is not appealing, then the details within that shape can't compliment it ». Le mouvement et une pose de trois quarts sont venus ensuite. — [Duolingo blog, Reshaping Duo](https://blog.duolingo.com/reshaping-duo/)
- Neko Atsume : les critiques attribuent le charme aux couleurs vives, au trait épais et au peu de détails. L'interface s'efface au profit des chats, et le jeu ne punit pas la négligence. Hit-Point a reçu le CEDEC Award du meilleur game design en 2015. — [Fanlore](https://fanlore.org/wiki/Neko_Atsume), [Know Your Meme](https://knowyourmeme.com/memes/subcultures/neko-atsume) (sources secondaires ; les avis sont des opinions de critiques)
- Headspace, refonte 2024 avec Italic Studio : une police sur mesure (Colophon, dérivée d'Aperçu) et une illustration qui montre une gamme d'émotions plus large (stress, tristesse) que les anciens visages souriants. — [It's Nice That](https://Itsnicethat.com/articles/italic-studio-headspace-graphic-design-project-250424). Le style d'avant 2018 était fait de personnages simples et ronds, de couleurs chaudes et d'animation douce. — [Blush](https://blush.design/blog/post/headspace-mindfulness-app) (secondaire)
- Notion : les illustrations d'origine de Roman Muradov sont en noir et blanc, minimalistes, avec un trait gestuel, à contre-courant de l'aplat vectoriel des autres marques. Buck a ensuite décliné un système (« Notion Faces ») où chaque composant est dessiné à la main. — [Notion blog](https://www.notion.com/es/blog/the-thinking-behind-our-latest-brand-campaign), [Buck](https://buck.co/work/notion-faces)
- Kind Words (Popcannibal) : direction artistique « lo-fi », personnages arrondis, couleurs chaudes, une seule chambre où l'on écrit des lettres. — [Wikipedia](https://en.wikipedia.org/wiki/Kind_Words_(video_game)), [Engadget](https://www.engadget.com/2020-03-28-kind-words-social-network-nice.html)
- Unpacking : pas de dialogue, quasi pas de texte. Le récit passe par les objets posés. — [GamesBeat](https://gamesbeat.com/unpacking-is-a-relaxing-game-with-no-words-and-a-great-story/), [Checkpoint Gaming](https://checkpointgaming.net/news/2019/10/pax-aus-2019-highlightunpacking/)
- Recettes « cosy » des kits de design (sources faibles, chiffres variables) : rayons de 16 à 28 px, ombres diffuses, tons chauds désaturés sur fonds pastel avec une seule couleur primaire amicale, boutons en pilule, et en sombre un charbon doux qui garde des accents chauds (lampe, bougie). — [kit playful-rounded](https://cdn.jsdelivr.net/npm/@wrongstack/core@0.295.1/design-kits/playful-rounded/KIT.md), [Cozy Gamer reference](https://chrislemke.github.io/website_designs/examples/Cozy_Gamer.html). Pour les jeux cosy, un article recommande des menus simples, une palette limitée, une animation subtile et des styles répétés. — [SDLC Corp](https://sdlccorp.com/post/the-art-of-designing-intuitive-user-interfaces-in-cozy-games/) (marketing, faible)

**Expressivité mesurée (Material 3 Expressive, Google)**
- 46 études, plus de 18 000 participants. En eye-tracking, les éléments clés sont repérés jusqu'à **4 fois plus vite**. Les écarts de fixation liés à l'âge s'effacent presque (les 45 ans et plus font comme les plus jeunes). La préférence est positive dans toutes les tranches d'âge, jusqu'à 87 % chez les 18-24 ans. Les notes « playful » et « friendly » montent. — [Google Design](https://design.google/library/expressive-material-design-google-research)
- Limites relevées par la même source : casser des schémas établis fait baisser l'utilisabilité (pochettes en désordre, actions d'e-mail sans libellé), et « No amount of emotion can compensate for a lack of clarity ». Une **minorité forte préfère des designs plus calmes**. Côté accessibilité, plus grandes cibles et contenants contrastés aident, et l'équipe a dépassé les normes de taille de cible et de contraste. — [Google Design](https://design.google/library/expressive-material-design-google-research)

**Célébrations, haptique, son**
- Le jalon de série Duolingo a demandé plusieurs passes d'animation brute pour régler « rhythm and energy » (« Timing is everything in animation »). La célébration est réservée aux jalons (semaine, mois, 100 jours, un an) et s'accompagne d'une carte de partage simple. Aucun chiffre publié, seulement des « early positive signals ». — [Duolingo blog, Streak milestone](https://blog.duolingo.com/streak-milestone-design-animation)
- Chez Duolingo, son, haptique et animation sont conçus ensemble, « neither one is doing the whole job alone ». — post LinkedIn d'un membre de l'équipe son et haptique, informel ([résultat de recherche](https://vn.linkedin.com/in/bacaibuihung) ; non vérifié en direct)
- Android : « less is more ». Entre une haptique « buzzy » et pas d'haptique, il faut choisir pas d'haptique. Il est conseillé de passer par les constantes `HapticFeedbackConstants` (cohérence, repli plateforme, accessibilité), de proportionner la force à l'importance et à la fréquence de l'événement, de concevoir visuel, son et haptique ensemble, et d'éviter `Vibrator.vibrate(long)` et les one-shots. Un bon « clic » dure 10 à 20 ms. — [Android Developers, Haptics principles](https://developer.android.com/develop/ui/views/haptics/haptics-principles)

### Inferences
- **[inférence]** Monsieur Glose coche déjà les cases documentées : silhouette lisible, gros yeux, contour brun épais façon Neko Atsume, accessoires simples. L'écart avec les meilleurs n'est pas le dessin. C'est que **l'interface autour reste générique** (boutons identiques, réglages Material) et que **les moments forts ne sont pas mis en scène** (fin de quiz).
- **[inférence]** La règle « peu de formes, beaucoup d'air » plaide contre l'ajout de motifs partout. Mieux vaut un ou deux objets signatures (marque-page, tampon) que du décor.
- **[inférence]** Chez les meilleurs, les célébrations sont rares et graduées. Pour Definiday : quiz réussi = micro-retour ; nouvel objet débloqué = vraie célébration ; jalons (30 soirs, 100 mots) = célébration un cran au-dessus.

### Gaps
- Je n'ai pas trouvé de source primaire sur le design visuel de Finch (style de l'oiseau, salles, sons). Les critiques consultées ne parlent que d'onboarding et d'encombrement ([Pratt IXD](https://ixd.prattsi.org/2026/02/design-critique-finch-self-care-pet-ios-app/)).
- Je n'ai trouvé aucune analyse sourcée de l'UI d'Animal Crossing NH (bulles, NookPhone), de Stardew Valley, de A Short Hike, de Tsuki, de Bear ou de Goblin Tools. Ce qu'on « sait » d'eux (bulles arrondies, cadres bois, pixel art) relève de l'observation courante, sans citation possible ici. Le Game UI Database n'a pas été consulté.
- Il manque des données publiques sur l'effet mesuré des textures papier ou grain sur la rétention ou la préférence.

---

## 2. Esthétique littéraire et livresque qui reste mignonne

### Takeaway
« Dark academia » (bibliothèques, écriture, littérature classique, gothique universitaire) et « cottagecore » (domesticité rurale, botanique, textures naturelles) sont des esthétiques identifiées, mais la documentation UI sérieuse est quasi inexistante. Pour Definiday, la piste tient en peu d'objets littéraires concrets (marque-page, tampon d'ex-libris, fiche de bibliothèque, enveloppe, timbre), dessinés dans le même trait que Glose, plutôt qu'en un « thème » chargé.

### Cited Findings
- Dark academia : esthétique centrée sur la tenue scolaire traditionnelle, l'intérieur, l'écriture et la poésie, l'art antique, la littérature classique, l'architecture grecque et gothique universitaire. Cottagecore : domesticité rurale, l'autre pôle. — [Wikipedia, Dark academia](https://en.wikipedia.com/wiki/Dark_academia)
- La fiche « cottagecore » de daisyUI la décrit comme une interface tournée vers la nature : formes organiques, botanique, couleurs terreuses, textures naturelles, mouvement doux. — [daisyUI trends cottagecore](https://trends.daisyui.com/trend/cottagecore/), [dark academia](https://trends.daisyui.com/trend/dark-academia/) (site de tendances, pas de recherche)
- Kind Words a bâti tout son jeu sur la lettre manuscrite à un inconnu, dans une chambre lo-fi chaude, et a dépassé les 250 000 lettres livrées. — [TechRaptor](https://techraptor.net/gaming/news/over-250000-letters-have-been-delievered-in-kind-words), [Wikipedia](https://en.wikipedia.org/wiki/Kind_Words_(video_game))
- Notion montre qu'un trait d'encre noir et blanc, gestuel, sur un fond chaud peut porter une marque entière. — [Notion blog](https://www.notion.com/es/blog/the-thinking-behind-our-latest-brand-campaign)
- **[repo]** Definiday a déjà dans sa collection « Marque-page à gland d'or », « Plume d'oie », « Enluminure » (pelage), « Sur la pile de livres », « Endormi dans sa tasse ». Le vocabulaire livresque existe, mais seulement comme accessoire du chat.

### Inferences
- **[inférence]** Les motifs livresques qui restent mignons sont des objets que Glose peut **toucher** : il tire sur le ruban du marque-page, tamponne l'ex-libris, glisse une lettre. Leur mignonnerie vient du chat, l'objet donne le ton littéraire. Les motifs seulement décoratifs (taches d'encre, enluminures en coin, typewriter) basculent plus vite dans le kitsch ou le « déguisement ».
- **[inférence]** Le « dark academia » sombre (bois, cuir, bougie) rejoint naturellement le mode nuit et le quiz du soir (`glose_soir`, tasse, bougie). Le « light academia » crème et or est déjà la palette de jour. Il n'y a donc rien à inventer : il faut assumer ce que l'appli fait déjà.
- **[hypothèse]** La marginalia (petit gribouillis manuscrit de Glose dans la marge, comme « tiens, joli mot ») serait le motif le plus spécifique à Definiday, puisque « glose » veut dire annotation en marge. À tester contre la charge de lecture.

### Gaps
- Je n'ai trouvé aucun cas d'étude sourcé d'application qui exploite cachets de cire, ex-libris ou marginalia en UI. Les résultats « dark academia app icons » sont des sites de faible qualité, non retenus.

---

## 3. Thèmes d'application : faut-il proposer des ambiances, et en faire des récompenses ?

### Takeaway
Les applis de référence monétisent ou récompensent la **personnalisation de l'objet affectif** (la chambre et les vêtements de l'oiseau chez Finch, les essences d'arbres chez Forest), pas un re-thème complet de l'UI. Definiday a déjà l'équivalent (la collection de Glose, 18 palettes, 14 polices). Un système de thèmes app-wide coûterait cher sur notre stack (couleurs `@color` en dur → passage aux attributs de thème, double QA jour et nuit, audit de contraste) pour un gain incertain. Mieux vaut des **décors saisonniers légers autour de Glose** et, au plus, 2 ou 3 ambiances.

### Cited Findings
- Finch : on gagne des « rainbow stones » par les quêtes, les jours d'événement saisonnier et les jalons. On les dépense en vêtements et en mobilier. Les événements saisonniers donnent mobilier, vêtements et un micropet. L'abonnement Plus ouvre une piste premium et débloque plus tôt le micropet (jour 20 contre jour 25). — [Finch help center, Energy vs Rainbow Stones](https://help.finchcare.com/hc/en-us/articles/37780134479757-Energy-vs-Rainbow-Stones), [Seasonal Event Overview](https://help.finchcare.com/hc/en-us/articles/37780438941965-Seasonal-Event-Overview)
- Chez Finch, les communautés créent des pièces à thème (boisé, noir et blanc). Plaintes relevées : trop de meubles sans couleur d'accent, thèmes saisonniers jugés promotionnels, baisse des gains de pierres. — [Engadget](https://www.engadget.com/apps/this-self-care-virtual-pet-is-helping-me-get-my-act-together-160027169.html), [Reddit r/finch, themed rooms](https://redlib.belloworld.it/r/finch/comments/1gjtljl/obsessed_with_themed_rooms) (anecdotique)
- Les critiques de Finch jugent les réglages et les quêtes encombrés, avec trop d'options (fatigue décisionnelle), et une incitation Plus très visible dans les réglages. — [Pratt IXD critique](https://ixd.prattsi.org/2026/02/design-critique-finch-self-care-pet-ios-app/)
- Forest : des pièces gagnées par les sessions (davantage pour les sessions longues) débloquent essences d'arbres et ambiances sonores. Des essences exclusives arrivent par événements. — [Wikipedia, Forest](https://en.wikipedia.org/wiki/Forest_(application)), [App Store](https://apps.apple.com/us/app/id866450515)
- Benchmark interne : Kawaii offre 5 thèmes sur 18 gratuitement, Proust réserve des thèmes au Premium, Nuance a 9 thèmes. Definiday offre déjà 18 palettes et 14 polices. — [Benchmark applis mot du jour.md](../../reports/Benchmark%20applis%20mot%20du%20jour.md), [Kawaii Play](https://play.google.com/store/apps/details?id=com.kawaii.vocabulary)
- Le benchmark interne note aussi que débloquer sur « quiz parfait » récompense une métrique facile (reconnaissance à quelques heures). — [Benchmark applis mot du jour.md](../../reports/Benchmark%20applis%20mot%20du%20jour.md)
- Google : une minorité forte préfère des designs calmes. — [Google Design](https://design.google/library/expressive-material-design-google-research)

### Inferences
- **[inférence]** Un thème app-wide ne vaut le coût que s'il change l'**ambiance** et pas seulement la teinte. Trois ambiances suffisent :
  - « Bibliothèque », l'actuelle : crème, marine, or ;
  - « Salon de thé » : crème rosée, sauge, brun, rose poudré, qui reprend les pelotes ;
  - « Nuit étoilée » : marine, or et une lueur de lampe, liée au mode sombre plutôt que choisie.

  Chaque ambiance a besoin d'une variante jour et nuit et d'un audit de contraste. 2 thèmes de plus, c'est 4 jeux de couleurs à valider.
- **[inférence]** Débloquer des thèmes en récompense est cohérent avec la collection, à condition de **lier le déblocage à la présence (jours avec un mot vu, mots revus) et non au score**, et de garder la palette par défaut complète et belle. Rien d'essentiel à la lisibilité ne doit se trouver derrière un déblocage.
- **[inférence]** Les décors saisonniers (feuille morte en novembre, flocon et écharpe en décembre, fleur au printemps) posés près de Glose, choisis par la date système, donnent l'effet « vivant » de Finch et Animal Crossing pour presque rien : un ou deux vecteurs par saison, aucun réglage.

### Gaps
- Duolingo ne propose pas de thèmes UI au choix, hors mode sombre. Je n'ai pas trouvé de donnée publique sur l'effet des thèmes débloquables sur la rétention, chez Finch, Forest ou ailleurs.

---

## 4. Le mouvement sur Android sans AndroidX

### Takeaway
Tout ce qu'il faut est dans la plateforme pour minSdk 26 : AVD (Glose), `ViewPropertyAnimator` et `ObjectAnimator` (UI), interpolateurs `Overshoot`/`AnticipateOvershoot`/`PathInterpolator`, `StateListAnimator` en XML (appui), `HapticFeedbackConstants`, `SoundPool`. Le seul manque réel est la physique de ressort (`DynamicAnimation` est AndroidX). Un `TimeInterpolator` de ressort amorti tient en une dizaine de lignes. Lottie et Rive sont à écarter : ils sont redondants avec le pipeline Paper.js vers AVD, et Lottie exige AndroidX.

### Cited Findings
- Les AVD animent les attributs de `<group>` (rotation, translation, échelle, pivot) et de `<path>` (dont `pathData`). Le morphing exige des chemins « compatibles », avec le même nombre de commandes et de paramètres. `AnimationDrawable` reste possible pour du image par image. — [Android Developers, Drawable animation](https://developer.android.com/develop/ui/views/animations/drawable-animation)
- `ValueAnimator.areAnimatorsEnabled()` (API **26**) renvoie faux si l'échelle de durée des animateurs vaut 0 ou si l'économiseur de batterie est actif. Google le recommande pour « show a different experience when animators are disabled ». `getDurationScale()` n'arrive qu'en API 33. — [ValueAnimator reference](https://developer.android.com/reference/android/animation/ValueAnimator#areAnimatorsEnabled())
- Côté M3 Expressive, Compose définit les ressorts par `dampingRatio` et `stiffness` (ratio 1 = pas de dépassement). Les valeurs « expressives » qui circulent (raideur 380, amortissement 0,8) viennent de paquets tiers, pas de Google. — [résumé de recherche, pub.dev M3EMotion](https://pub.dev/documentation/m3e_slider/0.0.2/common_m3e_common/M3EMotion-class.html) (non officiel)
- Coût de Lottie, sources contradictoires : environ +68 Ko et +1 084 méthodes selon un test de Tencent Music, version non précisée et datée, contre « 300 KB to 1 MB » selon un billet de 2026 non mesuré. Lottie 2.8.0 et suivantes ne supporteraient que les projets migrés vers AndroidX. — [besthub.dev](https://besthub.dev/articles/lottie-animation-library-introduction-usage-implementation-details-and-performance-on-android-b27fc4c3858c), [codeinsightshub](https://codeinsightshub.blogspot.com/2026/01/kill-lottie-dependency-why-your-android.html) (sources secondaires ; à vérifier sur le dépôt airbnb/lottie-android)
- Haptique : passer par les constantes d'action, éviter les vibrations longues, faire sentir les événements fréquents à peine et les événements importants plus fort. — [Haptics principles](https://developer.android.com/develop/ui/views/haptics/haptics-principles)
- **[repo]** `Glose.play()` respecte déjà `areAnimatorsEnabled()`. Le pipeline Paper.js produit des AVD nommés par groupe (`cadre`, `ombre`, `a0`…), ce qui permet d'ajouter des cibles sans changer d'outil.

### Inferences
- **[inférence, à partir de connaissances générales Android non revérifiées ici]** Les constantes utiles sur minSdk 26 sont les suivantes. `HapticFeedbackConstants.CONFIRM` et `REJECT` sont en API 30, d'où un repli à prévoir sur 26-29 avec `CONTEXT_CLICK` (API 23) ou `VIRTUAL_KEY`, ou pas d'haptique du tout. `View.performHapticFeedback()` respecte le réglage système « retour haptique » sans permission `VIBRATE`. À vérifier sur la [référence HapticFeedbackConstants](https://developer.android.com/reference/android/view/HapticFeedbackConstants).
- **[inférence]** Ressort sans AndroidX : un interpolateur `t -> 1 - e^(-ζωt)·cos(ω_d·t)` normalisé, branché sur `ViewPropertyAnimator.setInterpolator()`. Pour un « pop », `OvershootInterpolator(1.5f)` suffit dans 90 % des cas. Ajouter `DynamicAnimation` coûterait la contrainte « NO AndroidX » pour un gain imperceptible.
- **[inférence]** Le squash and stretch de Glose (atterrissage, saut de joie) se fait en AVD avec `scaleX`/`scaleY` sur un groupe dont le pivot est sous les pattes : 1 → 1,08/0,92 → 0,96/1,04 → 1 sur environ 300 ms. Le trait épais du dessin tolère bien la déformation.
- **[inférence]** Le réglage d'accessibilité Android « Supprimer les animations » met les échelles d'animation à 0, ce que `areAnimatorsEnabled()` capte. Ce comportement est connu mais n'est pas cité sur la page de référence, qui ne mentionne que l'option développeur et l'économiseur de batterie. À tester sur l'appareil.
- **[inférence]** Le son reste optionnel et **désactivé par défaut** : page qui tourne, petit « pop » de pelote, ronron. Avec `SoundPool` (plateforme), 3 à 4 OGG courts tiennent dans quelques dizaines de Ko au total (estimation). Le son est le levier le plus risqué en usage public (transport, bureau).

### Gaps
- Je n'ai pas trouvé de mesure officielle de la taille ou du nombre de méthodes de Lottie ou de Rive. Rive n'a pas été évalué (il demande aussi une dépendance native, à vérifier).
- Coût de rendu des AVD complexes de Glose (nombre de chemins) sur appareils bas de gamme : non mesuré.

---

## 5. Contraintes d'accessibilité d'une UI mignonne

### Takeaway
Trois risques concrets. (1) **Une boucle animée de Glose qui démarre seule et dure plus de 5 s à côté du contenu relève de WCAG 2.2.2, niveau A** : il faut un moyen de la mettre en pause ou un arrêt automatique. L'accueil actuel ne le fait pas. (2) Le texte sur une texture ou un dégradé se mesure au point le plus faible. (3) Les animations déclenchées par l'utilisateur doivent pouvoir être coupées : c'est déjà fait via `areAnimatorsEnabled()`, à étendre à toute nouvelle animation d'UI.

### Cited Findings
- WCAG 2.2.2 Pause, Stop, Hide (**niveau A**) couvre l'information animée qui « (1) starts automatically, (2) lasts more than five seconds » et « (3) is presented in parallel with other content ». Il faut un mécanisme pour la « pause, stop, or hide », sauf si elle est essentielle. Une animation décorative est couverte si elle remplit les trois conditions. — [W3C Understanding 2.2.2](https://www.w3.org/WAI/WCAG22/Understanding/pause-stop-hide.html)
- WCAG 2.3.3 Animation from Interactions (AAA) vise l'animation non essentielle déclenchée par une interaction. Les personnes avec troubles vestibulaires peuvent ressentir distraction, vertiges, maux de tête, nausées. — [W3C Understanding 2.3.3](https://w3c.github.io/wcag/understanding/animation-from-interactions.html)
- Il ne faut pas tout couper en bloc : les indicateurs fonctionnels (progression) doivent rester, éventuellement atténués. — [TestParty](https://testparty.ai/blog/wcag-animation-interactions-guide) (secondaire)
- Contraste : 4,5:1 pour le texte normal, 3:1 pour le grand texte (18 pt, ou 14 pt gras). Le contraste se mesure par rapport au fond réellement rendu. L'échec F83 vise les images de fond insuffisamment contrastées. Les ratios ne s'arrondissent pas (4,499 échoue), et les polices fines ou inhabituelles peuvent demander plus. — [W3C Understanding 1.4.3](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html)
- Les contenants à fort contraste et les grandes cibles améliorent l'usage pour des capacités motrices et visuelles variées. — [Google Design](https://design.google/library/expressive-material-design-google-research)
- **[repo]** L'audit interne relève une carte de palette claire à 1,02:1 contre le fond crème (bord invisible), des silhouettes de collection à 2,03:1 (voulues plus pâles, l'état est aussi écrit) et des polices décoratives qui altèrent les lettres (Uncial). — [critique_design.md](../../docs/audits/revue_ui/critique_design.md)

### Inferences
- **[inférence, repo]** La pose d'accueil boucle à l'infini (`repeatCount="infinite"`, cycles de 3,8 à 6,4 s) dès l'ouverture, et le toucher relance au lieu de mettre en pause. C'est probablement non conforme à 2.2.2 dès que les animations système sont actives. Correctif minimal : arrêter la boucle après environ 5 s (`postDelayed { Glose.stop(v) }`), et faire jouer un seul cycle au toucher. Cela réduit aussi la batterie et la distraction pendant la lecture de la définition.
- **[inférence]** Texture papier : s'il y en a une, elle doit rester sous 3 à 4 % de variation de luminance, pour que le pire pixel garde les ratios actuels (encre 16:1, accent 4,7:1). L'accent `#886A34` à 4,7:1 a peu de marge : une texture ne doit **jamais** passer sous les textes en accent. La mettre sur le fond de page, pas sous la carte ni les boutons.
- **[inférence]** Charge de lecture : la fin de quiz liste aujourd'hui les objets débloqués en un paragraphe gras. Des vignettes nommées une par une sont à la fois plus mignonnes et plus lisibles, et TalkBack peut annoncer chaque objet.
- **[inférence]** Haptique : il en faut toujours une version visuelle (et TalkBack). Ne jamais la rendre seule porteuse de l'information juste/faux.

### Gaps
- La conformité 2.2.2 de la boucle de Glose n'a pas été testée sur l'appareil. On ignore si le cycle d'animation actuel est ressenti comme du « mouvement » ou comme une respiration discrète.

---

## 6. Propositions concrètes pour Definiday (accueil, quiz, collection, réglages)

### Takeaway
La valeur ajoutée se trouve dans trois chantiers : **mettre en scène les récompenses** (fin de quiz), **lier Glose à l'interface** (il s'assoit sur la carte, réagit aux réponses, garde un objet livresque signature) et **donner aux réglages et à la collection le même soin que la carte**. Tout est faisable en Views plateforme, en vecteurs et avec quelques lignes de Kotlin, sans dépendance. Le seul ajout binaire envisageable est une petite tuile de grain (environ 2 à 5 Ko), facultative.

### Cited Findings
- Hiérarchie et taille des actions principales : jusqu'à 4 fois plus rapide à repérer. — [Google Design](https://design.google/library/expressive-material-design-google-research)
- Moins de formes, des silhouettes nettes, de l'espace vide autour de l'illustration. — [Duolingo blog](https://blog.duolingo.com/shape-language-duolingos-art-style/)
- Célébrations réservées aux jalons, timing travaillé. — [Duolingo blog, Streak milestone](https://blog.duolingo.com/streak-milestone-design-animation)
- Haptique discrète, proportionnée et synchronisée avec le visuel. — [Android haptics principles](https://developer.android.com/develop/ui/views/haptics/haptics-principles)
- Recommandations déjà émises en interne : Glose « assis » sur la carte (chevauchement de 8 à 12 dp), bandeau arrondi avec affordance, aperçu des pelages en couleur à 35 % plutôt qu'en silhouette. — [critique_design.md](../../docs/audits/revue_ui/critique_design.md)
- Lottie exigerait AndroidX et ajouterait de 68 Ko à plus de 300 Ko. — sources ci-dessus, section 4 (contradictoires)

### Inferences
Toutes les propositions ci-dessous sont des **[inférences]** tirées des sections 1 à 5, sauf mention **[repo]** pour les faits sur le code. Effort : S = moins d'une demi-journée, M = 1 à 2 jours, L = plus. Les références de fichiers viennent de l'état actuel du dépôt.

**A. Accueil**

| # | Proposition | Effet attendu | Effort / stack | Risques |
|---|---|---|---|---|
| A1 | Glose **assis sur la carte** : `translationY` positif ou marge négative de 8 à 12 dp, et sa queue ou le fil de sa pelote passe devant le bord de la carte (calque AVD séparé, ou ordre en Z de deux `ImageView`) | Relie mascotte et mot. Effet « objet dans le monde » à la Neko Atsume | S. Layout `activity_main.xml` ; queue « devant » = M si le dessin doit être découpé dans Paper.js | Chevauchement avec « MOT DU JOUR » à ×2,0 de police : tester `home_w360_font200` |
| A2 | **Marque-page signature** : ruban vectoriel (couleur de la palette, gland d'or) qui pend du haut de la carte. Au toucher, il bascule légèrement (`rotation` sur un pivot en haut, interpolateur d'overshoot). Il devient l'emplacement des mots favoris si le benchmark les retient | Motif littéraire unique, reconnaissable, cohérent avec l'objet « Marque-page à gland d'or » | S (vecteur + 10 lignes) | Kitsch faible s'il reste unique. Ne pas l'empiler avec d'autres décors |
| A3 | **Coins plus ronds** : carte de 16 à 24 dp, boutons de 12 à 16 dp, puces « mots d'aujourd'hui » en pilule (rayon = hauteur/2) | Lecture « amicale » documentée (formes sans angles vifs) | S (`btn.xml`, `btn_primary.xml`, `WordCard`) | Aucun. Garder le contour de focus de 3 dp |
| A4 | **Hiérarchie** : « Quiz du soir » en `Primary` plein (déjà en 0.8.0, à vérifier sur les captures), Collection et Nouveau mot en tuiles secondaires avec **petite icône vectorielle dans le trait de Glose** (tasse, pile de livres, dé), Réglages en lien ou icône discrète | Action du jour repérée plus vite. L'écran sous la carte cesse d'être une liste | S à M (3 ou 4 icônes Paper.js, `drawableStart`) | Encombrement si les icônes sont trop détaillées : 3 à 5 formes maximum chacune |
| A5 | **Retour d'appui tactile** sur tous les `Btn` : `StateListAnimator` XML qui ramène l'échelle à 0,97 en 80 ms quand le bouton est pressé (aujourd'hui `@null`) | Boutons « mous », effet de jouet, sans coût | S (1 fichier `animator/press.xml`, référencé dans `Btn`) | Pratiquement nul. Conservé même animations coupées ? Non : la plateforme le neutralise via l'échelle 0 |
| A6 | **Arrêter la boucle de Glose** après environ 5 s, un cycle au toucher, plus une petite réaction au toucher (clignement, oreille, ronron haptique `CONTEXT_CLICK`) | Conformité WCAG 2.2.2 (A), moins de distraction pendant la lecture, toucher récompensé façon Neko Atsume | S (`Glose.kt`, `MainActivity.kt:58,81`) | Aucun. Améliore l'accessibilité |
| A7 | **Mode nuit « lampe de lecture »** : halo radial chaud (`GradientDrawable` RADIAL, accent à 15 % d'alpha) derrière Glose, au lieu du marine plat | Nuit plus cosy (bougie, lampe) au moment du quiz du soir | S (drawable XML `values-night`) | Le halo reste derrière l'illustration, pas sous le texte : contraste inchangé |
| A8 | **Décors saisonniers** près de Glose selon la date (feuille d'automne, flocon et buée sur les lunettes, fleur, soleil). Un vecteur par saison, sans réglage | Effet « vivant » et retour à l'appli, coût minime | S par saison (Paper.js) | Kitsch si animé ou trop gros : statique et petit |
| A9 | **Bandeau notifications coupées** arrondi (12 à 16 dp), avec un Glose miniature qui tient une pancarte « Activer › » | État vide transformé en moment de personnage | S | Ne pas culpabiliser : ton neutre, jamais « Glose est triste » |
| A10 | Texture papier facultative : tuile WebP de 64 à 128 px en `BitmapDrawable` `tileMode="repeat"` sur `windowBackground` seulement, à 2 ou 3 % de luminance, plus douce encore la nuit | Chaleur « papier » | S, environ 2 à 5 Ko d'APK (estimation) | Contraste (cf. section 5), effet « filtre » si trop marquée. **À tester en A/B sur captures, à abandonner si on le remarque** |

**B. Quiz du soir**

| # | Proposition | Effet attendu | Effort / stack | Risques |
|---|---|---|---|---|
| B1 | **Bonne réponse** : le bouton fait un « pop » (échelle 1 → 1,04 → 1, overshoot, 220 ms), ✓ dessiné en vecteur (trim-path qui se trace) plutôt que glyphe de repli, haptique `CONFIRM` (API 30+) ou `CONTEXT_CLICK`, et la tête de Glose en haut fait un hochement (AVD 1 cycle) | Satisfaction immédiate, retour multisensoriel cohérent (recommandation Android) | S à M (ViewPropertyAnimator ; AVD tête « oui » en Paper.js) | Haptique : une seule impulsion courte. Animation coupée si les animateurs sont désactivés |
| B2 | **Mauvaise réponse** : léger tremblement horizontal du bouton (±6 dp, 2 oscillations, 250 ms), haptique `REJECT` (API 30+, rien avant), Glose qui **penche la tête, curieux** (jamais moqueur), bonne réponse révélée avec une douce teinte sauge | Erreur dédramatisée (Duolingo : visuels ludiques = moins peur de se tromper) | S à M | Le tremblement est un mouvement déclenché : coupé avec les animations. Éviter un tremblement d'écran entier |
| B3 | **Progression en pelote** : le « 1 / 4 » devient 4 petites pelotes qui se remplissent (vecteur, teinte de la pelote choisie) | Progression lisible et mignonne. Reprend la pelote personnalisée | S | Garder le texte « 1 sur 4 » pour TalkBack (`contentDescription`) |
| B4 | **Fin de quiz = déballage** : chaque objet débloqué apparaît en **vignette-timbre** (bord dentelé vectoriel, nom dessous), en cascade (apparition depuis 0,6 avec overshoot, 80 ms de décalage), avec Glose qui le porte au centre. Le paragraphe gras disparaît | Moment de récompense enfin à la hauteur, inspiré de Finch, Duolingo et de l'idée de sticker. Plus lisible | M (`QuizActivity.kt:124-128`, une vue vignette, un drawable timbre) | Encombrement si plus de 4 objets d'un coup : n'en montrer que 3 et ajouter « + 2 dans la collection » |
| B5 | **Confettis de papier** réservés aux déblocages et jalons (pas à chaque quiz réussi) : environ 20 à 30 petits rectangles et lettres aux couleurs de la palette, dessinés par une `View` personnalisée animée par un seul `ValueAnimator` (≤ 1,2 s), qui retombent en papillonnant | Célébration rare, donc précieuse (logique des jalons Duolingo) | M (environ 60 lignes de Kotlin, aucune dépendance) | Mouvement : absent si animations coupées, jamais plus de 3 flashs/s (pas de clignotement). Kitsch si trop fréquent |
| B6 | Quiz sans déblocage réussi : simple Glose « bravo » et une phrase, **pas** de confettis | Graduation des célébrations | S (logique existante) | Aucun |

**C. Collection**

| # | Proposition | Effet attendu | Effort / stack | Risques |
|---|---|---|---|---|
| C1 | **Album / herbier** : chaque section sur une « page » (surface et léger bord). Les objets débloqués dans un cadre-timbre (le même que B4). L'objet porté reçoit un petit tampon « porté » ou un ruban | Collection qui ressemble à un album qu'on remplit, ce qui motive (logique de collection Neko Atsume) | M (drawable timbre réutilisé, `CollectionActivity.kt`) | Ne pas pivoter les vignettes (scannabilité) : au plus ±1° décoratif sur le cadre, jamais sur le texte |
| C2 | **Emplacements verrouillés** : pelages et pelotes en couleur à 35 % (recommandation interne), poses et accessoires en contour pointillé « emplacement vide » avec un petit « ? », au lieu de 4 silhouettes grises identiques | Désir (« je vois ce que je vais gagner »), moins de monotonie | S (`CollectionActivity.kt:72,99`) | Vérifier que l'état reste écrit (déjà le cas) et que le contraste du nom reste ≥ 4,5:1 |
| C3 | **Essayage animé** : toucher un objet débloqué → l'aperçu de Glose fait un petit saut (squash and stretch) avant d'afficher l'objet | Plaisir de manipulation | S (ViewPropertyAnimator sur l'aperçu) | Coupé si animations coupées |
| C4 | **Nommer la mascotte** (prévu) : champ en tête de la collection, rendu comme une **étiquette d'ex-libris** (« Ex-libris de … »). Le nom est repris dans les textes (« Monsieur Glose » par défaut) | Attachement : la personnalisation par le nom est le premier geste chez Finch | S à M (Prefs + strings avec paramètre ; drawable étiquette) | Longueur et caractères du nom : limiter à environ 20 caractères, tester ×2,0 |

**D. Réglages**

| # | Proposition | Effet attendu | Effort / stack | Risques |
|---|---|---|---|---|
| D1 | **Sections en cartes** arrondies (surface, 16 dp), avec un titre de section accompagné d'une petite icône vectorielle (horloge, cadenas, tasse, palette) | Réglages « faits main » sans perdre leur sobriété | S à M (layout, 4 icônes) | Rester calme : la minorité qui préfère le calme se trouve surtout ici |
| D2 | **Une seule famille** dans les réglages, serif partout ou sans-serif partout (finding 8 de l'audit) | Cohérence, moins « Material par défaut » | S (`themes.xml` : `Field`, `Toggle`, `Help`, `spinner_item`) | Aucun |
| D3 | Remplacer les labels en Roboto capitales et interlettrés par des **petites capitales serif** (`fontFeatureSettings="smcp"` sur une police qui les possède, à vérifier dans les TTF embarqués) ou un serif italique, en 13 ou 14 sp | Plus « livre », moins « tableau de bord » | S | Vérifier la présence de `smcp`. Sinon garder les capitales, mais en serif |
| D4 | **Pied de page Glose** : petite vignette « Glose relit vos réglages » (pose existante), en remplacement d'« À propos » nu | Personnage présent jusqu'au bout, à la manière des états vides | S | Rien de plus, pas d'animation ici |
| D5 | **Ambiances** (option, plus tard) : « Bibliothèque » (défaut), « Salon de thé », éventuellement une saisonnière. Implémentation : déplacer les `@color/...` codées dans les styles et le code vers des attributs de thème (`?attr/...`), un `<style>` par ambiance, `recreate()` au changement | Personnalisation app-wide, récompense possible (liée aux jours de présence) | **L** (passer en revue tout usage de `getColor(R.color.x)`, double jeu jour et nuit, re-audit de contraste) | Charge de QA × 2 par thème. Kitsch si les ambiances divergent du trait de Glose. **Recommandation : ne pas commencer par là, A8 couvre 80 % de l'effet** |
| D6 | **Sons** (option, désactivée par défaut) : interrupteur « Petits bruits de papier » dans Apparence, avec `SoundPool` et 3 ou 4 OGG courts (page, pelote, ronron) | Chaleur sonore pour ceux qui la veulent | M (sons à produire, licence) | Usage en public, fatigue. Toujours couplé au visuel |

**E. Hors périmètre ou déconseillé**
- **Lottie ou Rive** : déconseillés. Lottie demanderait AndroidX selon les sources trouvées, et son coût est de 68 Ko à plus de 300 Ko selon les sources (à vérifier). Il double un pipeline Paper.js vers AVD qui fonctionne déjà.
- **`androidx.dynamicanimation`** : inutile, un interpolateur maison ou `OvershootInterpolator` suffit.
- **Textures partout, taches d'encre, polices manuscrites pour l'UI** : risque de kitsch et de contraste. L'audit montre déjà que les polices décoratives coûtent en lisibilité sur les mots.
- **Célébrer chaque bonne réponse avec des confettis** : la célébration s'use et récompense la métrique facile (cf. benchmark).

**Ordre suggéré (meilleur rapport effet/effort)** : A6 (conformité), A5, A3, B1, B2, B4, A1, A2, C2, D2, A7, A8, puis B5, C1, C4, D1, puis D6 et D5 en option.

### Gaps
- Je n'ai pas vérifié si les TTF embarqués exposent `smcp` (D3).
- La conformité à 2.2.2 et l'effet perçu des boucles de Glose demandent un test sur appareil.
- Je n'ai pas mesuré le coût de rendu des AVD supplémentaires (tête qui hoche, ruban) sur appareil d'entrée de gamme, ni la taille réelle d'une tuile de grain ou de sons OGG. Les chiffres donnés sont des estimations.
- Aucune donnée utilisateur Definiday sur l'envie d'ambiances app-wide. Un sondage in-app, ou au moins une capture A/B, serait le préalable à D5.
