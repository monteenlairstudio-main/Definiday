# Definiday plus fun et mignonne : brainstorm de nouvelles pistes (hors style, fonds, interactions mascotte, skins)

Conventions. **[fait]** = tiré d'une source citée. **[proposition]** = idée de ce brainstorm, non validée. **[inférence]** = déduction (effort, risque). Effort estimé pour Kotlin + Views de la plateforme, sans AndroidX, sans backend : **S** ≈ 1-2 jours, **M** ≈ 1 semaine, **L** > 1 semaine ou travail éditorial lourd. Hors périmètre (autres équipes) : apparence, fonds, caresses/interactions avec Glose, pelages et skins.

Constat de départ sur les données (vérifié dans `app/src/main/assets/words.json`, 1 342 fiches) : chaque fiche a `mot`, `nature`, `definition`, `exemple`, `anecdote`, `source`, `palette`, `police`, `academie`. **Il n'y a ni champ siècle/date d'attestation, ni langue d'origine, ni thème.** Toute idée qui en dépend (siècle, étymologie, intrus thématique, mots de saison) coûte de l'éditorial sur 1 342 fiches. Le README indique qu'un historique local des mots montrés et de leurs dates existe déjà (règle des 270 jours), ce qui rend faisables hors ligne « il y a un an », « votre année en mots » et la bibliothèque.

## 1. Qu'est-ce qui rend une fonction « fun et mignonne » sans trahir le ton cosy, et qu'en dit la preuve ?

### Takeaway
Les précédents les plus solides sont : la surprise non conditionnelle (cadeaux de Neko Atsume, aventures de Finch), le partage qui montre l'utilisateur sans divulgâcher (grille Wordle, Wrapped) et le jeu de bluff sur définitions (Fictionary, *Call My Bluff*), vieux de plusieurs décennies. La recherche sur la motivation dit que les récompenses **inattendues** et les retours verbaux positifs ne nuisent pas à la motivation intrinsèque, contrairement aux récompenses attendues et liées à la performance, ce qui va dans le sens d'un Glose qui offre au hasard plutôt qu'il ne paie au score.

### Cited Findings
**Surprise, cadeaux, absence**
- **[fait]** Neko Atsume : les chats visitent le jardin pendant l'absence du joueur et laissent des poissons (et, plus rarement, des poissons dorés) en partant ; le jeu a reçu le prix CEDEC du meilleur game design en 2015 — [Know Your Meme](https://knowyourmeme.com/memes/subcultures/neko-atsume) (source secondaire). Quartz souligne aussi le côté « cadeaux » des chats — [Qz](https://qz.com/656505/this-blissfully-addictive-japanese-cat-game-will-help-you-understand-your-cats-in-real-life).
- **[fait]** Finch : chaque activité terminée donne de l'énergie qui envoie l'oiseau en « aventure » ; le cœur de l'appli est gratuit, l'abonnement sert surtout à la personnalisation — [Bustle](https://www.bustle.com/wellness/finch-app-review-features-price), [Headway](https://makeheadway.com/blog/finch-app-review/). Avis cité : Finch « makes me feel like I'm being cared for by my pet » — [App Store](https://apps.apple.com/ng/app/id1528595748) (via résumé de recherche).
- **[fait]** Tsuki Adventure 2 : un avis demande de pouvoir « write them letters », ce qui indique que les lettres ne sont pas une fonction mais une envie exprimée — [App Store](https://apps.apple.com/us/app/id6447011561?l=en-us). Des jeux cosy de lapins bâtissent tout leur concept sur les lettres (Letter Bunny) — [Gematsu](https://www.gematsu.com/games/letter-bunny). Je n'ai **pas** trouvé de jeu Tsuki avec mécanique de cartes postales : la référence du brief est inexacte ou non vérifiable.
- **[fait]** Méta-analyse de Deci, Koestner et Ryan (1999, 128 expériences) : les récompenses tangibles **inattendues** n'affectent pas la motivation intrinsèque (9 études, d = 0,01, IC −0,20 ; 0,22) ; les récompenses attendues et liées à la performance la diminuent (d de −0,28 à −0,40) ; les retours verbaux positifs l'augmentent (d = 0,33 sur le comportement libre) — [PDF Psychological Bulletin](https://depts.washington.edu/techdocs/papers/deciExtrinsicRewardsAndIntrinsicMotivation99.pdf).
- **[fait]** Une étude sur jeu vidéo trouve que les joueurs préfèrent les récompenses variables aux fixes, **sauf** si la plage inclut des récompenses nulles ou minuscules — [Princeton talks](https://lists.cs.princeton.edu/hyperkitty/list/talks@lists.cs.princeton.edu/message/ZW7VDHD6H237Z4P5XHTMIH5RYJL6IJL6) (résumé de séminaire, source faible). La définition du « delight » comme surprise + joie (Oliver, Rust et Varki 1997) n'est trouvée que via un blog de fidélisation — [Loyalty Reward Co](https://loyaltyrewardco.com/inject-randomness-into-your-loyalty-program-to-boost-engagement/) (secondaire, non vérifié à la source).
- **[fait]** UX Magazine recommande des surprises qui répondent à une action de l'utilisateur et restent « non-intrusive » — [UX Magazine](https://uxmag.com/articles/increasing-user-engagement-with-rewarding-interactions).

**Partage sans compte**
- **[fait]** La grille d'emojis de Wordle, ajoutée en décembre 2021, est créditée de la viralité ; elle montre la performance sans révéler la réponse et passe partout car c'est de l'Unicode — [The Spinoff](https://thespinoff.co.nz/media/13-01-2022/those-coloured-boxes-on-twitter-are-new-zealands-fault), [Besthub](https://besthub.dev/articles/why-wordle-became-a-global-phenomenon-game-design-social-mechanics-and-cultural-impact-6a0ac3e820ef) (blogs, à prendre avec prudence). Le puzzle unique quotidien entretient l'attente et la conversation — même source.
- **[fait]** Spotify Wrapped : un responsable marketing de Spotify attribue l'attrait à la connaissance de soi, l'expression de soi et la communauté ; Wrapped se partage parce qu'il montre l'utilisateur, pas la marque — [ADMA](https://adma.com.au/resources/personal-shareable-no-strings-attached-the-winning-formula-behind-spotify-wrapped). Apple Music Replay, sans fonctions de partage équivalentes, est jugé en retrait (opinion) — [Substack J. Tang](https://justinqytang.substack.com/p/the-spotify-wrapped-model-why-companies).
- **[fait]** Duolingo Year in Review 2020 : premier bilan intégré à l'appli, réservé aux apprenants actifs au moins 7 jours avec 20 leçons ; les 10 % meilleurs en XP font plus de la moitié des partages — [Duolingo Blog](https://blog.duolingo.com/duolingo-2020-year-in-review), [Behind the scenes](https://blog.duolingo.com/year-in-review-behind-the-scenes).

**Jeux de mots à précédent ancien**
- **[fait]** Fictionary (« Dictionary Game ») : un joueur annonce un mot obscur, les autres inventent des définitions ; Balderdash (1984) en est la version commerciale ; *Call My Bluff* (BBC, 1965-1988) en était l'adaptation télévisée — [Wikipedia Balderdash](https://en.wikipedia.org/wiki/Balderdash), [Wikipedia Fictionary](https://wikipedia.com/wiki/Fictionary).
- **[fait]** Un ouvrage français (Talon et Vervisch) recense des mots « qui n'existent pas » mais qu'on emploie, vérifiés contre le Nouveau Petit Robert ; les auteurs notent que la frontière est parfois difficile à tracer — [Editis PDF](https://extranet.editis.com/it-yonixweb/images/OMN/art/doc/8/8565ef341c3134313338383638393539353538.pdf). Je n'ai pas trouvé de source fiable sur « Le Petit Robert des mots qui n'existent pas » ni sur un « Dictionnaire des mots inventés » précis.

**Apprentissage**
- **[fait]** Effet de génération : on retient mieux ce qu'on produit soi-même que ce qu'on lit (méta-analyse Bertsch et al. 2007, mise à jour par McCurdy et al. 2020) — [McCurdy et al. 2020](https://link.springer.com/content/pdf/10.3758/s13423-020-01762-3.pdf). Une seule étude vocabulaire (Badgett 2003, 22 mots) : écrire une phrase avec le mot bat la définition seule à J+2 et J+21 — [UNLV](https://oasis.library.unlv.edu/rtds/1577). Preuve directe mince.
- **[fait]** Rappelé du benchmark : les QCM aident si les mauvaises réponses sont plausibles ([Little et Bjork 2014](https://bjorklab.psych.ucla.edu/wp-content/uploads/sites/13/2017/01/LittleBjorkMC2014.pdf)) ; le rappel l'emporte sur la reconnaissance ([Rowland 2014](https://pubmed.ncbi.nlm.nih.gov/25150680/)) ; la gamification mal employée et les notifications culpabilisantes sont des schémas à éviter ([Mogavi et al.](https://arxiv.org/abs/2203.16175), [Castro et Valença 2025](https://sol.sbc.org.br/index.php/ihc/article/view/37677)).

### Inferences
- **[inférence]** Règle de conception qui découle de Deci et al. : les cadeaux de Glose (lettres, mot doré) doivent être **non conditionnels** (arriver au hasard, ou à l'ancienneté), et les félicitations rester **verbales** ; ne pas lier de nouveaux déblocages au score du soir.
- **[inférence]** Règle « pas de zéro » : une surprise aléatoire ne doit jamais tomber sur « rien » ; mieux vaut une fréquence basse et toujours positive.
- **[inférence]** Le bluff sur définitions est le jeu qui colle le mieux à Definiday : même matière (mots rares + définitions), précédent de 60 ans, et des distracteurs inventés mais plausibles jouent le rôle de distracteurs concurrents.
- **[inférence]** Tout partage doit montrer l'utilisateur (ses mots, son année) et non la marque, et ne rien divulgâcher du quiz du jour.

### Gaps
- Pas d'étude rigoureuse trouvée sur la boucle cadeau/absence de Neko Atsume (un chapitre de l'UGA existe, texte non trouvé : [UGA](https://grady.uga.edu/news/tags/neko-atsume/)).
- L'origine précise de la grille Wordle et la citation de Wardle ne sont vues que dans des blogs secondaires.
- Aucune donnée sur des adultes natifs apprenant des mots rares de leur langue avec ces mécaniques.

## 2. Liste brute des pistes (40 idées)

### Takeaway
Sur 40 pistes, les plus prometteuses réutilisent ce qui existe déjà (historique daté, palette et police par mot, quiz, pelote de Glose, dessin de la carte) ; celles qui demandent un champ éditorial absent (siècle, étymologie, thème) ou une infrastructure (QR, Wear OS, liens web) sont plus chères qu'elles n'en ont l'air.

### Cited Findings
**Calendrier culturel vérifié (pour les pistes C)**
- **[fait]** Nuits de la lecture 2027 : du 20 au 24 janvier 2027, thème « L'enfance » (11e édition) — [Sortir à Paris](https://www.sortiraparis.com/en/what-to-visit-in-paris/walks/guides/267162-nuits-de-la-lecture-2027-in-paris-and-ile-de-france-dates-and-theme), [Paris.fr](https://www.paris.fr/evenements/les-nuits-de-la-lecture-2027-dans-les-bibliotheques-de-la-ville-de-paris-126792).
- **[fait]** Dis-moi dix mots 2026-2027 : « Dis-moi dix mots d'un monde à savourer », thème gastronomie ; les dix mots : boucané, caramélisé, concocter, épluchette, ferment, mandoline, millésime, papilles, pimenter, zeste ; valorisation pendant la Semaine de la langue française et de la Francophonie en mars 2027 — [Éduscol](https://eduscol.education.gouv.fr/1912/dis-moi-dix-mots-concours-des-dix-mots), [Ministère de la Culture](https://www.culture.gouv.fr/fr/espace-documentation/bases-de-donnees/Fiches-bases-de-donnees/dis-moi-dix-mots-une-operation-de-sensibilisation-a-la-langue-francaise).
- **[fait]** Printemps des poètes 2027 : du 8 au 31 mars 2027, thème « La poésie : zones d'action » (orthographe singulier/pluriel variable selon les sources) — [Livres Hebdo](https://www.livreshebdo.fr/article/le-printemps-des-poetes-devoile-le-theme-de-son-edition-2027), [ActuaLitté](https://actualitte.com/article/132087/salons-festivals/apres-la-liberte-le-printemps-des-poetes-2027-choisit-l-engagement).
- **[fait]** Précédents de partage cités par le benchmark : cartes partageables chez Idiome et Kawaii ; widget Gratitude, +25 % de rétention chez ses utilisateurs (corrélationnel) ; Glance exige AndroidX — voir [Benchmark](../../reports/Benchmark%20applis%20mot%20du%20jour.md), [Android Developers Blog](https://android-developers.googleblog.com/2026/05/how-gratitude-widgets-boosted-user-retention-25-percent.html).
- **[fait]** API de plateforme utiles sans AndroidX : retour haptique `View.performHapticFeedback` — [Android Developers](https://developer.android.com/reference/android/view/View#performHapticFeedback(int)) ; tuile de Réglages rapides `TileService` (API 24) — [Android Developers](https://developer.android.com/reference/android/service/quicksettings/TileService) ; épinglage de widget `AppWidgetManager.requestPinAppWidget` (API 26) — [Android Developers](https://developer.android.com/reference/android/appwidget/AppWidgetManager#requestPinAppWidget(android.content.ComponentName,%20android.os.Bundle,%20android.app.PendingIntent)). (Liens de référence, non relus pendant cette session.)

### Inferences
Toutes les entrées ci-dessous sont des **[propositions]**. Format : *quoi* — *pourquoi fun (précédent)* — *lien avec l'apprentissage* — *effort* — *risques*.

#### A. Mini-jeux de mots
1. **Le Bluff de Glose** (« Une seule est vraie ») : un mot rare, trois définitions, dont deux inventées par Glose avec malice ; on choisit la vraie. — Fictionary / *Call My Bluff* — distracteurs plausibles (Little et Bjork), et l'anecdote révélée après ancre le vrai sens. — **S** de code (réutilise `QuizActivity`) + **M** d'éditorial (écrire 2 fausses définitions pour ~200 mots, ou les tirer des définitions d'autres mots de même nature, gratuit mais moins drôle). — Risque : retenir la fausse définition ; montrer la vraie en clair avec l'exemple, jamais de fausse définition sur l'écran de verrouillage.
2. **Vrai ou faux mot ?** : trois mots, un existe, deux sont inventés (« ploutrer », « gavanche ») ; trouver le vrai. — même lignée, plus le plaisir des néologismes. — reconnaissance de forme, faible valeur sémantique ; intérêt surtout ludique. — **S** code, **M** éditorial (créer ~150 faux mots crédibles et vérifier qu'ils n'existent pas, TLFi/Wiktionnaire). — Risque : un faux mot qui existe réellement (vérification), confusion.
3. **Phrase à trous** depuis le champ `exemple` (déjà benchmark n° 9). — format connu des jeux de lettres. — rappel > reconnaissance. — **S**. — Risque : exemples où le mot est fléchi (accord, conjugaison) ; gérer la forme exacte.
4. **Le pendu doux de la pelote** : deviner un mot déjà vu lettre à lettre ; chaque erreur déroule un tour de la pelote de Glose (pas de potence). — pendu universel, retourné en image tendre ; la pelote existe déjà dans les dessins. — rappel de forme sur mots vus, avec la définition comme indice. — **S/M** (dessin d'une pelote qui se déroule = autre équipe ?). — Risque : mots composés, locutions latines (« ab irato ») ; limiter aux mots simples.
5. **Anagramme du mot vu** (tuiles de lettres à remettre en ordre, indice = définition). — jeux d'anagrammes de presse. — rappel. — **S**. — Risque : mots rares inconnus = injouables ; uniquement sur mots déjà vus.
6. **« Motus » des mots rares** (grille type Wordle, cible tirée des mots déjà vus, 5 à 9 lettres). — viralité de Wordle. — rappel actif. — **L** : il faut un lexique de validation des essais (dizaines de milliers de formes, licence à vérifier : Lexique, Grammalecte, Wiktionnaire CC BY-SA) et un poids d'APK. — Risque : avec une cible rare, la partie est frustrante ; pas de puzzle commun à tous sans calendrier fixe partagé (possible hors ligne si la graine est la date, mais alors la cible n'est pas tirée des mots vus).
7. **Mini mots croisés 5×5** avec les mots de la semaine. — mots fléchés/croisés très populaires. — rappel par définition. — **L** (génération de grilles avec mots rares longs quasi impossible ; grilles écrites à la main = éditorial). — Risque : coût élevé pour un jeu hebdo.
8. **À quel siècle ?** (associer un mot à son époque d'attestation). — frise, jeu de culture. — ancre historique. — **L** éditorial (champ absent des fiches). — Risque : dates d'attestation discutables.
9. **D'où vient-il ?** (latin, grec, arabe, occitan, anglais…). — « devine l'étymologie ». — l'étymologie aide à retenir le sens. — **L** éditorial (champ absent ; une partie est extractible des ~800 anecdotes). — Risque : erreurs d'étymologie.
10. **L'intrus** : 4 mots, 3 partagent un thème (cuisine, marine, vêtements). — jeux de catégories type « Connections » (NYT, précédent non sourcé ici). — organisation sémantique. — **L** (tags thématiques absents). — Risque : thèmes flous.
11. **Mémory mot ↔ définition** (6 paires de mots vus, cartes à retourner). — jeu de paires. — associer forme et sens. — **S**. — Risque : lisibilité des définitions longues sur petites cartes (360 dp, police ×2).
12. **Le Mot de la semaine** (dimanche) : récap des 7 à 21 mots de la semaine, 5 questions, Glose remet un « signet » de la semaine. — rendez-vous hebdo, rituel. — révision à J+1 à J+7, cohérente avec l'échelle J+7 du benchmark. — **S** (réutilise le quiz). — Risque : une notification de plus ; la fondre dans le quiz du dimanche soir.

#### B. Métaphores de collection
13. **La bibliothèque de Glose** : chaque mot « appris » (réussi en révision différée) devient un livre sur une étagère, dos coloré à la **palette du mot** et titré dans **sa police** ; toucher un dos ouvre la carte. — collections visibles qui grandissent (Neko Atsume, Finch). — c'est l'historique du benchmark (n° 3) rendu désirable, et la récompense est l'apprentissage réel. — **M** (vue Canvas de dos de livres, défilement d'étagères ; dépend de l'historique). — Risque : 1 000 dos à dessiner = performance ; regrouper par étagère mensuelle.
14. **L'herbier des mots** : chaque mot pressé comme une fleur sur une page datée. — carnets d'herbier, papeterie. — même rôle que 13. — **M**. — Risque : doublon de 13 ; choisir une seule métaphore.
15. **Le cabinet de curiosités** : les mots à anecdote rangés en tiroirs (« Bizarreries », « Mots de cour »…). — goût des curiosités. — **L** (classement thématique). — Risque : tags absents.
16. **Le carnet de timbres** : un timbre par semaine complétée, à coller dans un album. — philatélie, ancrage du rendez-vous hebdo. — faible lien direct. — **S/M** (dessins). — Risque : impression de « devoir » si on manque une semaine ; aucune case vide culpabilisante.
17. **Ex-libris** : un tampon personnalisable (initiales, nom donné à Glose) apposé sur les livres de la bibliothèque et les cartes partagées. — personnalisation, appropriation. — nul. — **S**. — Risque : faible.
18. **Le lierre / arbre qui pousse** au fil des mots. — trope classique (Forest). — nul. — **S/M**. — Risque : générique, peu littéraire.

#### C. Calendrier et événements (offline : calendrier codé dans les assets, mis à jour avec les versions)
19. **Calendrier culturel** : Nuits de la lecture (20-24 janv. 2027, « L'enfance » : mots de l'enfance d'autrefois), Semaine de la langue française et Dis-moi dix mots (mars 2027, gastronomie : semaine de mots rares de la table), Printemps des poètes (8-31 mars : citations de poètes du domaine public), 20 mars Francophonie (mots de Belgique, Suisse, Québec, Afrique), rentrée littéraire (fin août), prix Goncourt (début novembre), Noël. — rendez-vous partagés, rareté. — thèmes = mémorisation par réseau. — **S** par événement en code, **M** éditorial par an. — Risque : les dix mots officiels 2027 sont surtout courants (« zeste », « papilles ») : les citer comme clin d'œil, pas comme mots du jour ; vérifier les dates chaque année.
20. **Calendrier de l'Avent des mots** (1er-24 déc.) : une case par jour, un mot d'hiver ou de fête, petite décoration de la carte. — calendrier de l'Avent. — 24 mots thématiques. — **S/M**. — Risque : décor = autre équipe ; ici, seulement la mécanique des cases.
21. **Poisson d'avril de Glose** : le 1er avril, l'un des mots du jour est inventé ; le soir, le quiz demande « lequel était le poisson ? » et révèle. — tradition française du 1er avril. — attention au sens (repérer l'invraisemblable). — **S**. — Risque : **confiance dans les définitions**, et le mot inventé reste sur l'écran de verrouillage toute la journée ; option désactivable, mention « poisson » révélée dans l'appli dès qu'on l'ouvre, jamais dans l'historique comme vrai mot.
22. **Mots de saison** (vendanges en septembre, neige en janvier). — **L** (tags absents).
23. **Anniversaire d'installation** : Glose porte un chapeau de fête et offre une lettre récapitulant « nos N mots ». — précédent Finch (anniversaires, non sourcé ici). — rappel des mots. — **S**. — Risque : faible.

#### D. Partage et social sans compte
24. **Résultat partageable façon Wordle** : texte Unicode sans le mot (« Definiday · 6 nov. · 🟩🟩⬜ · Glose ronronne »). — grille Wordle. — nul, sinon le rendez-vous. — **S** (`Intent.ACTION_SEND`). — Risque : ton « score » contraire au cosy ; préférer un « 3 mots rencontrés » à un score.
25. **Carte postale « Je vous envoie un mot »** : la carte image (benchmark n° 7) au format carte postale, avec timbre, ex-libris et une ligne de dédicace manuscrite. — cartes Idiome/Kawaii ; plaisir d'offrir. — expliquer un mot à quelqu'un = génération. — **S/M** (réutilise le dessin de carte, `FileProvider` ou `MediaStore`, pas d'AndroidX requis avec un `ContentProvider` maison). — Risque : faible.
26. **QR « cadeau de mot »** : un QR sur la carte qui ouvre le mot chez un ami qui a l'appli. — **M/L** : pas de générateur QR dans la plateforme (écrire un encodeur ou embarquer ZXing core, Java pur sans AndroidX) ; un lien `https` cliquable demande un domaine statique avec `assetlinks.json` (hébergement statique, pas un backend), sinon schéma `definiday://` non cliquable dans la plupart des messageries ; sans l'appli, le QR doit mener quelque part. — Risque : **dépend d'un domaine hébergé** ; à signaler.
27. **Mode « à table »** (famille) : le Bluff de Glose en passe-et-joue sur un seul téléphone, chacun propose une définition à l'oral, Glose donne la vraie. — Fictionary est un jeu de salon. — génération + discussion. — **S**. — Risque : faible ; aucun compte.
28. **Défi « Placez le mot »** : invitation facultative à glisser le mot dans une conversation ; on coche soi-même, Glose tamponne le carnet. — défis du quotidien. — usage actif. — **S**. — Risque : à formuler en invitation, jamais en obligation.

#### E. Sensoriel
29. **Sons doux opt-in** : page qui tourne au changement de mot, plume qui gratte sur une bonne réponse, ronron en fin de quiz réussi. — ASMR, jeux cosy. — renforcement verbal/émotionnel. — **S** (`SoundPool`, sons CC0). — Risque : respecter le mode silencieux, désactivé par défaut, poids des fichiers.
30. **Haptique** : petit tic sur bonne réponse, vibration « ronron » (`VibrationEffect`, API 26). — **S**. — Risque : réglage pour couper, appareils sans moteur fin.
31. **Ambiance du quiz** (feu de cheminée, pluie). — **S/M**. — Risque : licence et poids des boucles audio.
32. **Widget avec Glose** (RemoteViews, benchmark n° 11), qui dort la nuit sur le widget. — **M**. — Risque : déjà au benchmark ; la variante « Glose dort » touche à la mascotte (autre équipe).
33. **Tuile de Réglages rapides « Un mot »** (`TileService`) : affiche le mot du jour, ouvre la carte. — **S**. — Risque : surface peu vue, peu « mignonne ».
34. **Tuile Wear OS** : l'API Tiles passe par des bibliothèques Jetpack (`androidx.wear.tiles`) **[inférence non vérifiée en session]** : conflit avec « pas d'AndroidX ». **L**, à écarter.

#### F. Surprises et ravissement
35. **Les lettres de Glose** : de temps en temps (hasard, environ une fois toutes les 2 à 3 semaines), Glose a « voyagé » pendant la nuit et laisse une carte postale d'un lieu littéraire (Croisset, Illiers-Combray, la maison de Hugo à Guernesey) avec un mot rare lié au lieu. — Neko Atsume (cadeaux pendant l'absence), Finch (aventures), envie de lettres chez Tsuki. — un mot en contexte narratif. — **S** code, **M** éditorial (~30 lettres). — Risque : déclencheur purement aléatoire, **pas** une récompense de performance (Deci et al.) ; pas de notification « Glose vous a écrit » insistante.
36. **Le mot doré** : environ un jour sur 30, un mot du jour à carte dorée et une anecdote plus longue. — récompense variable, prix « poisson doré » de Neko Atsume. — attention accrue ce jour-là. — **S**. — Risque : compulsion ; rester cosmétique, jamais « à ne pas manquer », retrouvable dans l'historique.
37. **« Il y a un an aujourd'hui »** : le mot vu il y a 365 jours revient en souvenir, avec « vous l'aviez rencontré un mardi pluvieux ». — souvenirs de Google Photos (précédent non sourcé ici). — **révision espacée très longue**, gratuite. — **S** (historique daté existant). — Risque : interaction avec la règle des 270 jours (le mot n'est pas « mot du jour » mais souvenir).
38. **« Votre année en mots »** (décembre) : nombre de mots, mot préféré, plus long, palette la plus vue, quiz faits, nouvelles poses, 5 à 7 cartes partageables. — Spotify Wrapped, Duolingo Year in Review. — rappel des mots marquants de l'année. — **M** (tout est local). — Risque : seuil minimal d'usage (Duolingo : 7 jours actifs) pour ne pas montrer un bilan vide.
39. **Succès aux noms littéraires** : « Les Mots » (100 mots), « À la recherche du mot perdu » (retrouvé un mot d'il y a un an), « Exercices de style » (10 phrases écrites dans le carnet). — badges. — peut récompenser des actes d'apprentissage. — **S**. — Risque : glisser vers la gamification à score ; pas de classement, pas de badge pour le nombre de jours d'affilée.
40. **Œufs de Pâques** : la carte du mot « ronronner » fait ronronner Glose, « matou » déclenche un clin d'œil, toucher 7 fois l'anecdote fait tomber les lunettes. — easter eggs. — faible. — **S** par œuf. — Risque : touche à la mascotte (autre équipe) ; se coordonner.

#### G. Créativité
41. **Le carnet de Glose** : écrire sa propre phrase avec le mot ; Glose appose un tampon (« Joliment tourné »), jamais de note. — journaux intimes, carnets. — **effet de génération** (Bertsch, Badgett). — **S/M** (`EditText`, stockage local). — Risque : clavier = friction ; rester facultatif ; aucune correction automatique.
42. **Autocollants de mots favoris** : exporter un mot favori en PNG détouré. — stickers. — faible. — **S**. — Risque : Android n'a pas d'API d'autocollants système simple ; partage d'image ordinaire.

### Gaps
- Effort non mesuré sur le code réel (`QuizActivity.kt`, `Unlocks.kt`, `WordCard.kt` non lus en détail) : les estimations sont des ordres de grandeur.
- Licence d'un lexique français libre pour un jeu type Wordle non vérifiée.
- Dates 2027 de la rentrée littéraire et du Goncourt non recherchées (variables chaque année).
- Exigence AndroidX des tuiles Wear OS non vérifiée sur la documentation pendant cette session.

## 3. Classement : top 12

### Takeaway
En tête : la bibliothèque de Glose, les lettres de Glose et le Bluff de Glose, car ils sont mignons, ancrés dans des précédents solides, liés à l'apprentissage réel et faisables sans backend. Viennent ensuite les surprises tirées de l'historique local (il y a un an, année en mots) et le calendrier culturel français, qui donne des rendez-vous sans réseau.

### Cited Findings
- Récompenses inattendues neutres, retours verbaux positifs favorables, récompenses de performance défavorables — [Deci et al. 1999](https://depts.washington.edu/techdocs/papers/deciExtrinsicRewardsAndIntrinsicMotivation99.pdf).
- Bluff sur définitions : précédent Fictionary/Balderdash/*Call My Bluff* — [Wikipedia](https://en.wikipedia.org/wiki/Balderdash).
- Partage centré sur l'utilisateur — [ADMA](https://adma.com.au/resources/personal-shareable-no-strings-attached-the-winning-formula-behind-spotify-wrapped), [Duolingo](https://blog.duolingo.com/duolingo-2020-year-in-review) ; partage sans divulgâcher — [The Spinoff](https://thespinoff.co.nz/media/13-01-2022/those-coloured-boxes-on-twitter-are-new-zealands-fault).
- Effet de génération — [McCurdy et al. 2020](https://link.springer.com/content/pdf/10.3758/s13423-020-01762-3.pdf), [Badgett 2003](https://oasis.library.unlv.edu/rtds/1577).
- Dates culturelles 2027 — [Éduscol](https://eduscol.education.gouv.fr/1912/dis-moi-dix-mots-concours-des-dix-mots), [Livres Hebdo](https://www.livreshebdo.fr/article/le-printemps-des-poetes-devoile-le-theme-de-son-edition-2027), [Sortir à Paris](https://www.sortiraparis.com/en/what-to-visit-in-paris/walks/guides/267162-nuits-de-la-lecture-2027-in-paris-and-ile-de-france-dates-and-theme).

### Inferences
Critères (inférence) : charme/ton cosy, lien avec l'apprentissage, effort sur la pile actuelle, risque (confiance, culpabilisation, backend). Toutes sont des **[propositions]**.

| # | Piste (n° brut) | Fun | Apprentissage | Effort | Dépendance / risque principal |
|---|---|---|---|---|---|
| 1 | **Bibliothèque de Glose** : un livre par mot appris, dos à la palette et à la police du mot (13 + ex-libris 17) | fort, collection visible | fort : récompense un rappel différé réussi | M | dépend de l'historique et de la révision (benchmark n° 2-3) ; perf. au-delà de quelques centaines de dos |
| 2 | **Lettres de Glose** depuis des lieux littéraires, au hasard (35) | fort, surprise narrative | moyen : un mot en contexte | S + M éditorial | doit rester aléatoire et non conditionnel ; pas de notification insistante |
| 3 | **Le Bluff de Glose** + mode « à table » (1 + 27) | fort, jeu de salon éprouvé | fort : distracteurs plausibles, révélation avec anecdote | S + M éditorial | ne jamais laisser une fausse définition sans correction visible |
| 4 | **« Il y a un an aujourd'hui »** + anniversaire d'installation (37 + 23) | moyen à fort, nostalgie | fort : révision à 12 mois gratuite | S | rien avant la 1re année : à combiner avec 5-6 |
| 5 | **Calendrier culturel** : Nuits de la lecture, Semaine de la langue / Dis-moi dix mots, Printemps des poètes, Francophonie, Goncourt, Avent (19 + 20) | moyen à fort, rendez-vous | moyen : séries thématiques | S par événement, M éditorial/an | dates à revérifier chaque année ; décor à coordonner avec l'équipe style |
| 6 | **« Votre année en mots »** en décembre, cartes partageables (38) | fort, identité | moyen : rappel des mots marquants | M | seuil d'usage minimal ; tout local |
| 7 | **Carte postale « Je vous envoie un mot »** + résultat façon Wordle sans score (25 + 24) | moyen à fort | moyen : expliquer un mot | S/M | aucun ; acquisition sans compte |
| 8 | **Carnet de Glose** : écrire sa phrase, tampon tendre (41) | moyen | fort : effet de génération (preuve vocabulaire mince) | S/M | friction du clavier ; strictement facultatif |
| 9 | **Le Mot de la semaine** le dimanche (12) | moyen, rituel | fort : révision J+1 à J+7 | S | pas de notification en plus |
| 10 | **Pendu doux de la pelote** / anagramme sur mots vus (4 + 5) | moyen à fort, mignon | moyen à fort : rappel de forme | S/M | locutions et mots composés exclus ; dessin de pelote à coordonner |
| 11 | **Le mot doré** (36) | moyen, rareté | faible à moyen | S | rester cosmétique, sans FOMO |
| 12 | **Sons et haptique opt-in** : page, plume, ronron (29 + 30) | moyen, sensoriel | faible | S | désactivés par défaut, mode silencieux respecté |

Écartés ou reportés (inférence) : Motus/Wordle (6, lexique et licence), mots croisés (7), siècle/étymologie/intrus/mots de saison (8, 9, 10, 15, 22 : champs éditoriaux absents des 1 342 fiches ; à reconsidérer si l'éditorial enrichit les fiches), QR cadeau (26 : domaine statique requis, à signaler comme dépendance d'hébergement), tuile Wear OS (34 : AndroidX), poisson d'avril (21 : bon une fois par an mais risque sur la confiance tant que le mot reste sur l'écran de verrouillage ; possible dans l'appli seulement), succès littéraires (39 : à n'adopter que s'ils récompensent des actes d'apprentissage, sans série), œufs de Pâques et widget Glose (40, 32 : périmètre mascotte).

Ordre de construction suggéré (inférence) : 4 et 9 d'abord (S, réutilisent l'historique et le quiz), puis 3 et 2 (éditorial en parallèle), puis 1 et 6 (dépendent de l'historique consultable du benchmark), 5 à caler sur le calendrier : Nuits de la lecture le 20 janvier 2027 et la Semaine de la langue française en mars 2027 sont les deux premières échéances.

### Gaps
- Aucune donnée d'usage de Definiday pour pondérer « fun » : le classement est un jugement.
- Volume éditorial réel (lettres, fausses définitions) à chiffrer avec l'auteur des fiches.
- Pas de test utilisateur du Bluff : risque de mémoriser la fausse définition non mesuré pour ce public.
