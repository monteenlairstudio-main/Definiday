# Definiday tient l'écran, pas encore la mémoire

Definiday a déjà le meilleur support du créneau et le contenu que les natifs réclament. Le fond de l'écran de verrouillage est visible toute la journée, sans widget à installer. Les mots sont vraiment rares, avec une anecdote et des liens Académie/TLFi. Tout cela est gratuit et sans publicité, quand les leaders anglophones font payer 35 à 60 $ par an. Ce qui manque n'est pas du contenu mais de la **mémoire** : aucune révision des mots passés, pas d'historique, pas de favoris, pas de widget d'accueil, pas de partage. Or la recherche sur la mémorisation indique qu'une exposition unique suivie d'un QCM le soir même donne un bon score du jour et un faible souvenir un mois plus tard. Côté présentation, la faiblesse se joue avant la carte du mot, à quatre endroits. La permission de notification est demandée au premier lancement. La plupart des utilisateurs d'Android 14+ voient un bandeau d'alarmes exactes dès les Réglages. Le fond de verrouillage d'origine est perdu, sans moyen de le choisir ni de le rétablir. Il n'y a ni onboarding ni fiche store. Les défauts de finition relevés par l'audit interne (accueil, quiz, Collection, TalkBack) sont corrigés dans la 0.8.0. Les trois chantiers au meilleur rendement :

1. des distracteurs de quiz tirés des mots déjà vus, plus 1 à 2 questions de révision espacée chaque soir ;
2. un historique consultable avec favoris ;
3. un onboarding court qui demande la permission au bon moment et prévient pour le fond d'écran.

Le widget d'accueil vient après : l'effort est moyen et le gain n'est démontré qu'en corrélation. Aucun des atouts propres à Definiday ne doit être sacrifié : fond d'écran, mascotte bienveillante, série tolérante, zéro publicité, zéro compte.

Comment lire ce rapport. **[fait]** signale un fait tiré d'une source citée : fiche store, documentation, article. Les chiffres des stores ont été relevés le 2026-11-06. **[inférence]** signale une déduction raisonnée à partir de ces faits. **[hypothèse]** signale un jugement non vérifié, sur appareil ou auprès d'utilisateurs. Les audits internes cités (`docs/audits/`) sont des constats faits sur captures et sur le code de Definiday.

## Sur Android, le créneau francophone des mots rares reste mal servi

**[fait]** Les institutions n'ont aucune appli « mot du jour ». Le Robert tient une rubrique web « Le mot du jour » qui sert à vendre ses dictionnaires ([Le Robert](https://dictionnaire.lerobert.com/dis-moi-robert/raconte-moi-robert/mot-jour)). Larousse et l'Académie française font du dictionnaire ou du « Dire, ne pas dire ». Les deux applis Android installées depuis longtemps sont datées et vivent de la publicité :

| Appli | Note Play | Installations | Remarque |
|---|---|---|---|
| « Mot du jour » (Vocabulous) | **3,90/5** | **500 k+** | version iOS non mise à jour depuis 2020-08-22 ([Play](https://play.google.com/store/apps/details?id=com.motdujour.fr), [App Store](https://apps.apple.com/fr/app/id1233244667)) |
| « 1 Jour 1 Mot » | **4,73/5** (1 932 avis) | **100 k+** | publicité, retrait à 14,99 € ([Play](https://play.google.com/store/apps/details?id=com.and.jag.unjour1mot)) |

Au-dessus d'elles, Vocabulary de Monkey Taps, une appli générique traduite, cumule **5 M+** installations ([Play](https://play.google.com/store/apps/details?id=com.hrd.vocabulary)). En anglais, la même appli domine le créneau avec **222 086 notes à 4,77** sur iOS. Elle a été finaliste des Apple Design Awards 2025 dans la catégorie « Visuals and Graphics » ([Apple Newsroom](https://www.apple.com/newsroom/2025/06/apple-unveils-winners-and-finalists-of-the-2025-apple-design-awards/)).

**[fait]** La concurrence qui monte est une vague d'indépendants sortis en 2025-2026, presque tous sur **iOS uniquement**, positionnés « français soutenu / mots rares » : Vocabule, Swann, Nuance, Idiome, IpsoVerbo, Proust. La seule à avoir une vraie traction est Proust, avec 4,58 sur 376 avis ([App Store](https://apps.apple.com/fr/app/id6758053954)). Ces applis ont presque toutes widget, audio, révision espacée et une offre premium. **[inférence]** Il reste donc une fenêtre sur Android pour une appli soignée, adulte et sans publicité : c'est exactement la place de Definiday.

**[fait]** Aucune appli de vocabulaire étudiée n'écrit le mot dans l'image du fond de verrouillage. Toutes passent par des widgets ou des notifications. Widget Vocabulary (Orbit Studios) annonce l'écran de verrouillage, mais par des widgets ([Play](https://play.google.com/store/apps/details?id=io.orbitstudios.widgetvocabulary)). L'équivalent le plus proche se trouve hors de la catégorie : « Bible Lock Screen » pose chaque matin un verset sur une photo choisie par l'utilisateur ([F-Droid](https://f-droid.org/en/packages/com.daklok.biblelockscreen/)).

## Ce qui manque : la révision, l'archive et le widget, pas le contenu

Le tableau compare Definiday 0.8.0 aux pratiques relevées chez les concurrents. Toutes les lignes « Qui le fait » sont des faits sourcés : la fiche citée annonce la fonction.

| Fonction | Definiday 0.8.0 | Qui le fait | Verdict |
|---|---|---|---|
| Révision des mots passés | non, aucun retour avant 9 mois | IpsoVerbo, Swann (« 2 bonnes réponses d'affilée = apprise »), Dogan Ekici (« Daily Review »), WordPal, Un Mot Par Jour (VOID), Soutenu ([IpsoVerbo](https://apps.apple.com/fr/app/id6791702813), [Swann](https://apps.apple.com/fr/app/id6821071446), [Dogan Ekici](https://itunes.apple.com/lookup?id=1536210624&country=us)) | **manque majeur** |
| Historique et recherche | non | 1 Jour 1 Mot (archive avec recherche), JpCrow, Carem (historique par date), TypoTouches ([1 Jour 1 Mot](https://apps.apple.com/fr/app/1-jour-1-mot-d%C3%A9finition/id1517541133), [Carem](https://play.google.com/store/apps/details?id=com.carem.motdujour)) | **manque majeur** |
| Favoris et listes | non | 1 Jour 1 Mot, Kawaii (cœur depuis le widget), Atlas (« Vocabulary Journal »), MyVocab ([Kawaii](https://play.google.com/store/apps/details?id=com.kawaii.vocabulary)) | **manque majeur** |
| Widget d'accueil | non | 1 Jour 1 Mot, TypoTouches, IpsoVerbo, Proust, Capte, Vocabulary ([TypoTouches](https://play.google.com/store/apps/details?id=com.typotouches.unmotparjour)) | manque, effort moyen |
| Carte image partageable | non | Idiome (« carte pensée pour être partagée »), Kawaii, Vocabulous ([Idiome](https://apps.apple.com/fr/app/id6768006177)) | manque, effort faible |
| Onboarding | non, la permission est demandée au premier lancement | Vocabulary (20 à 42 écrans), Atlas et Vocabulous (test de niveau) ([ScreensDesign](https://screensdesign.com/showcase/vocabulary-learn-words-daily)) | manque, en version courte |
| Prononciation audio | non | 1 Jour 1 Mot (TTS depuis la v1.4.0), Carem, Vocabule, Proust, IpsoVerbo ([Vocabule](https://apps.apple.com/fr/app/id6793131936)) | attente de base chez les nouveaux entrants |
| Synonymes et antonymes | non | Vocabule, Proust, Nuance, Dicode, VOID ([Dicode](https://play.google.com/store/apps/details?id=com.nuagic.dicode)) | secondaire, coût éditorial élevé |
| Niveaux et thèmes | non | Nuance (3 niveaux, 9 thèmes), Capte, Vocabule ; Dicode propose un retour « trop facile / trop difficile » ([Nuance](https://apps.apple.com/fr/app/id6809574575)) | secondaire, puisque tout le corpus est rare |
| Étymologie | **oui**, par l'anecdote | 1 Jour 1 Mot (depuis la v1.7.0), Vocabule, Proust | à jour |
| 1 à 3 mots par jour | **oui** | Nuance (1, 3 ou 5), Dogan Ekici (3 en gratuit) | à parité |
| Remplacer le mot du jour | **oui** (mot au hasard, délai de 4 mois) | Dogan Ekici (« Get a new one, once a day ») | à parité, en mieux |
| Quiz quotidien | **oui**, sur les mots du jour seulement | Vocabulous (quiz d'une minute en révision espacée), VOID (4 définitions) | à parité sur la forme, faible sur le fond |

**[inférence]** Le constat central : Definiday a l'**inverse** du profil du marché. Les concurrents empilent des mécaniques sur un contenu souvent faible. La plainte n° 1 des natifs anglophones est que les mots sont trop faciles. Un avis Play sur Vocabulary parle de « dictionary roulette, instead of thoughtful content » ([Play](https://play.google.com/store/apps/details?id=com.hrd.vocabulary)). Un autre, sur l'appli de Dogan Ekici, demande « Would be better if it had etymologies too » ([App Store](https://apps.apple.com/us/app/id1536210624)). Definiday a le contenu et le support, mais aucune boucle qui ramène un mot après sa journée. L'historique, les favoris et la recherche sont aussi la condition de presque tout le reste : révision, partage d'un mot ancien, sentiment de constituer un trésor.

**[fait]** Le contenu rare a sa face sombre. Chez Vocabulous, des utilisateurs se plaignent que « beaucoup de mots sont du vieux français » et de définitions difficilement compréhensibles ([Play](https://play.google.com/store/apps/details?id=com.motdujour.fr)). Chez 1 Jour 1 Mot, certaines définitions sont jugées « assez compliquées » ([Play](https://play.google.com/store/apps/details?id=com.and.jag.unjour1mot)). **[inférence]** Un simple retour « trop obscur » sur la carte, à la manière de Dicode, coûterait peu. Il renseignerait sur les 1 342 fiches sans changer le positionnement.

## Une seule rencontre et un quiz le soir même ne fixent pas un mot rare

**[fait]** Les deux leviers de mémorisation les mieux répliqués sont l'espacement et le rappel différé.

- Cepeda et al. (2008), plus de 1 350 participants : pour retenir une information un an, l'intervalle optimal avant révision vaut environ **5 à 10 % du délai**, soit 3 à 5 semaines. Un intervalle trop court nuit plus qu'un intervalle trop long ([PubMed](https://pubmed.ncbi.nlm.nih.gov/19076480/), [York University](https://yfile.news.yorku.ca/?p=9454)).
- Roediger et Karpicke (2006) : au bout d'une semaine, le groupe testé se souvient d'environ **61 %** contre 40 % pour le groupe qui a relu. Ces chiffres viennent de résumés secondaires ([PDF](https://psychology.ecu.edu/wp-content/pv-uploads/sites/216/2019/03/Roediger-Karpicke-2006.pdf)).
- Rowland (2014) : les tests de rappel profitent plus à la mémoire que les tests de reconnaissance ([PubMed](https://pubmed.ncbi.nlm.nih.gov/25150680/)).
- Le manuel d'Anki résume la conséquence pratique : « repeating a card several times in one day does not significantly contribute to long-term memory » ([Anki](https://docs.ankiweb.net/deck-options.html)).

**[fait]** L'exposition passive aide, mais peu.

- La corrélation entre le nombre de rencontres d'un mot et son apprentissage incident est moyenne (r = .34) ([Uchihara et al. 2019](https://zendy.io/title/10.1111/lang.12343)).
- Les estimations du nombre de rencontres nécessaires vont de 6 à plus de 20 ([Uchihara et al. 2022](https://discovery.ucl.ac.uk/id/eprint/10154988/1/Uchihara%20et%20al.%20%282022%29%20Language%20Learning.pdf)).
- Une méta-analyse ne trouve que **9 à 18 %** de mots appris au post-test immédiat ([Webb et al. 2023](https://todoele.net/bibliografia/how-effective-second-language-incidental-vocabulary-learning-meta-analysis)).

Réserve importante : presque toute cette recherche porte sur des apprenants d'une langue étrangère. Les données sur des adultes qui apprennent des mots rares de leur propre langue, c'est-à-dire le cas de Definiday, sont minces.

**[inférence]** Appliqué à Definiday :

- L'écran de verrouillage tient de la relecture (« restudy ») plutôt que du rappel. Sa valeur se révèle surtout s'il est suivi d'un rappel plus tardif.
- Le quiz du soir mesure une **reconnaissance à quelques heures**, donc un score flatteur. Débloquer des éléments de la mascotte sur un « quiz parfait » du jour récompense une métrique facile.
- La règle des 9 mois sans retour se place bien au-delà de l'intervalle optimal, et le quiz du même soir bien en deçà.
- Les mots rares ne se rencontrent presque jamais dans la vie courante. C'est donc à l'appli de fournir les répétitions.

**[fait]** Les QCM aident la mémoire quand les mauvaises réponses sont **plausibles et concurrentes**. Le bénéfice s'étend alors aux informations liées à ces mauvaises réponses ([Little et Bjork 2014](https://bjorklab.psych.ucla.edu/wp-content/uploads/sites/13/2017/01/LittleBjorkMC2014.pdf)). **[inférence]** Les distracteurs de Definiday sont aujourd'hui tirés au hasard dans la même nature grammaticale. Si on les tirait dans les **mots déjà vus par l'utilisateur**, chaque quiz deviendrait une révision cachée, avec des distracteurs concurrents (même registre, même rareté), sans écran supplémentaire.

Une échelle fixe ajouterait 1 à 2 questions par soir, sur des mots vus à J+2, J+7, J+30 puis J+90. Elle capterait l'essentiel du bénéfice de l'espacement sans algorithme FSRS, qui n'est pas justifié pour 1 à 3 mots par jour. Aucune étude ne compare cette échelle fixe à FSRS sur si peu d'éléments : c'est une extrapolation. Le champ « exemple » de chaque fiche permettrait aussi un format plus exigeant : une phrase à trous à 4 options, ou le sens inversé (le mot est donné, on choisit la définition).

**[fait]** Ce que la recherche dit de l'engagement valide les choix de Definiday.

- Chez Duolingo, découpler la série de l'objectif quotidien, pour qu'une seule leçon suffise à la prolonger, a augmenté la rétention à J14 de 3,3 % ([Duolingo](https://blog.duolingo.com/improving-the-streak)).
- Une série rompue réduit l'engagement ultérieur, surtout si l'utilisateur s'en blâme. L'effet s'atténue quand la série peut être « réparée » ([Silverman et Barasch 2023](https://www.colorado.edu/business/faculty-research/2023/04/19/or-track-how-broken-streaks-affect-consumer-decisions)).
- Un jour manqué n'a guère d'effet sur la formation d'une habitude ([Lally et al. 2010](https://bps.org.uk/research-digest/how-form-habit)).

La série de Definiday, que les jours sans mot ne cassent pas, et Monsieur Glose, qui ne se moque jamais, sont donc alignés sur les preuves. Les travaux d'interaction humain-machine sur Duolingo classent les notifications culpabilisantes et les visuels émotionnellement chargés parmi les schémas manipulateurs ([Castro et Valença 2025](https://sol.sbc.org.br/index.php/ihc/article/view/37677)).

**[inférence]** Un point de vigilance : en mode notification, le mot du jour et le rappel de 21 h peuvent faire jusqu'à 14 notifications par semaine. Dans une enquête d'intention déclarée, 22 % des répondants disent qu'ils arrêteraient d'utiliser une appli à 2 à 5 notifications par semaine ([Business of Apps](https://www.businessofapps.com/marketplace/push-notifications/research/push-notifications-statistics/)). Le contenu est choisi par l'utilisateur, ce qui atténue le risque. Mais un rappel de quiz au libellé qui varie, comme le recommande Duolingo ([Yancey et Settles 2020](https://research.duolingo.com/papers/yancey.kdd20.pdf)), et facile à couper, reste préférable.

## La présentation pèche avant même la carte : permission, alarmes, fond perdu, pas de fiche

### Les premières minutes contredisent les recommandations de Google

**[fait]** Depuis Android 13, les notifications sont désactivées par défaut. Google recommande de laisser l'utilisateur découvrir l'appli, de déclencher la demande par une action de sa part et de la faire « in context » ([Android Developers](https://developer.android.com/develop/ui/views/notifications/notification-permission)). Definiday la demande au premier lancement.

**[fait]** Sur Android 14+, `SCHEDULE_EXACT_ALARM` est refusée par défaut aux nouvelles installations des applis qui ciblent l'API 33 ou plus. Google demande de dégrader proprement, par exemple avec `setWindow`, dont la fenêtre minimale est de 10 minutes ([Android 14](https://developer.android.com/about/versions/14/changes/schedule-exact-alarms)). **[inférence]**, recoupée par l'audit interne : la plupart des nouveaux utilisateurs verront donc, en tête des Réglages, le bandeau d'avertissement. Depuis la 0.8.0, il nomme la page système « Alarmes et rappels » et porte le bouton d'autorisation, mais il reste vu par presque tous.

**[hypothèse]** Le retrait au déverrouillage démarre un service de premier plan depuis l'arrière-plan, et a probablement besoin de l'exemption que donne une alarme exacte. Abandonner les alarmes exactes partout casserait cette option. Pour le seul affichage du mot, une fenêtre de 10 minutes suffit.

**[fait]** Depuis Android 14, une appli ne peut plus lire le fond d'écran en place, sauf avec `MANAGE_EXTERNAL_STORAGE`. `clear(FLAG_LOCK)` fait réapparaître le fond d'accueil sur l'écran de verrouillage ([WallpaperManager](https://developer.android.com/reference/android/app/WallpaperManager)). La perte du fond de verrouillage d'origine est donc **structurelle**. Depuis la 0.8.0, le texte des Réglages le dit clairement (« votre ancien fond de verrouillage n'est pas rétabli »), mais l'utilisateur ne le lit qu'après coup, et n'a aucun moyen d'agir.

**[inférence]** C'est le principal risque de perception : « l'appli a remplacé ma photo et je ne la retrouve pas ». L'analogue Bible Lock Screen contourne le problème en faisant choisir à l'utilisateur sa propre photo comme fond de la carte ([F-Droid](https://f-droid.org/en/packages/com.daklok.biblelockscreen/)).

**[fait]** Les leaders bâtissent l'investissement dès l'onboarding : objectifs, créneau horaire, thème, icône. Chez Vocabulary, cela prend 20 à 42 écrans et se termine sur un paywall ([ScreensDesign](https://screensdesign.com/showcase/vocabulary-learn-words-daily), [AppLlama](https://appllama.io/apps/1084540807/vocabulary-learn-words-daily)). Pour son widget, Duolingo a constaté que le méconnaître et avoir du mal à l'installer étaient les principaux obstacles. Une animation explicative montrée dans l'appli a fait nettement monter les installations ([Duolingo](https://blog.duolingo.com/widget-feature)).

**[inférence]** Il faut à Definiday 3 ou 4 écrans, pas 20 :

1. le choix du support, avec un aperçu de la carte sur un faux écran de verrouillage et l'avertissement sur le fond d'écran ;
2. l'heure ;
3. la palette et la police ;
4. la permission, demandée seulement si la notification a été choisie.

### Pas de fiche store, et des obstacles réglementaires à anticiper

**[fait]** Definiday est distribué en APK : il n'a pas de vitrine. Les leaders Play mettent les mots-clés dans le titre : « Mot du Jour : Mots Rares », « 1 Jour - 1 Mot : Dictionnaire » ([JpCrow](https://play.google.com/store/apps/details?id=com.motdujour)). Les règles de Google pour une fiche sont les suivantes ([Play Console](https://support.google.com/googleplay/android-developer/answer/13393723?hl=en)) :

- une description courte de **80 caractères** au plus ;
- 2 à 8 captures, la fonction la plus forte en premier ;
- aucune mention « n° 1 » ni récompense.

Trois obstacles restent à lever pour la publication et la distribution :

- Le type de service de premier plan `specialUse` doit être déclaré. Il n'est admis que « in limited scenarios » et passe en revue ([Play Console](https://support.google.com/googleplay/android-developer/answer/13392821?hl=en)).
- `USE_EXACT_ALARM` est réservé aux réveils et calendriers ([Android 14](https://developer.android.com/about/versions/14/changes/schedule-exact-alarms)).
- Même hors Play, la vérification de l'identité du développeur pour les APK installés à côté du store se généralise en **2027** ([Android Authority](https://www.androidauthority.com/android-sideloading-changes-timeline-3679204/)).

**[inférence]** Le service `specialUse`, qui écoute les déverrouillages, est le point le plus exposé à un refus Play. Il vaut mieux préparer sa justification, ou rendre l'option retirable, avant de soumettre l'appli.

### Dans l'appli, l'audit interne est traité

**[fait, audit interne]** La carte du mot est jugée « le meilleur élément de l'appli » ([critique_design.md](../docs/audits/revue_ui/critique_design.md)). La 0.8.0 a corrigé les défauts relevés : action principale sur « Quiz du soir », couleurs de quiz adoucies pour la nuit, déblocage avec un seul Glose, aperçu fixe dans Collection, polices décoratives réservées aux mots de 8 lettres au plus, plancher de taille des mots longs, retour TalkBack du quiz ([accessibilite.md](../docs/audits/revue_ui/accessibilite.md)). Reste ouvert : une palette claire sur le fond crème fait presque disparaître le bord de la carte (contraste de 1,02:1).

**[fait]** Face à cela, l'identité visuelle des meilleurs est forte. Vocabulary a été primée pour ses visuels. Swann photographie ses lettres sur des façades ([App Store](https://apps.apple.com/fr/app/id6821071446)). Vocabulous, Atlas et Kawaii illustrent chaque mot ([Vocabulous](https://itunes.apple.com/lookup?id=987136347&country=us)). **[hypothèse]** Monsieur Glose est l'actif d'identité le plus fort de Definiday, mais il est absent de la surface la plus vue, l'écran de verrouillage. Une petite signature de Glose sur la carte serait un marqueur de marque, à tester contre le risque d'encombrement. **[inférence]** Le ton cosy et littéraire de Definiday est déjà celui que la vague 2026 adopte en store, par exemple « Mieux dire, mieux penser » chez Vocabule ([App Store](https://apps.apple.com/fr/app/id6793131936)) : il n'y a pas à le changer.

## Améliorations classées par rapport impact / effort

L'impact est estimé sur la rétention des mots, la rétention des utilisateurs ou la distribution. L'effort est estimé pour Kotlin sans AndroidX. Ces estimations sont des **[inférences]**, sauf la justification, qui renvoie aux sources des sections précédentes.

| # | Amélioration | Impact | Effort | Justification |
|---|---|---|---|---|
| 1 | Tirer les distracteurs du quiz parmi les mots déjà vus, de même nature | Fort | Faible | Distracteurs concurrents (Little et Bjork) et révision cachée sans écran nouveau |
| 2 | Ajouter 1 à 2 questions de révision par soir, sur une échelle fixe J+2, J+7, J+30, J+90 | Fort | Faible à moyen | Espacement et rappel différé (Cepeda, Roediger et Karpicke) ; la règle des 9 mois reste valable pour les mots nouveaux |
| 3 | Historique des mots vus, avec favoris et recherche | Fort | Moyen | Standard chez 1 Jour 1 Mot, JpCrow, Carem et MyVocab ; support de la révision et du partage |
| 4 | Onboarding de 3 à 4 écrans : support avec aperçu, heure, style, permission au bon moment, avertissement clair sur le fond | Fort | Faible à moyen | Recommandation Google pour les permissions ; levier d'investissement des leaders |
| 5 | Alarmes : `setWindow` de 10 min par défaut, alarme exacte seulement si le retrait au déverrouillage est actif, bandeau affiché seulement dans ce cas | Moyen | Faible | Refus par défaut sur Android 14+ ; présentation et conformité Play |
| 6 | Bord de carte visible avec les palettes claires (contraste carte / fond crème 1,02:1) | Faible | Faible | `critique_design.md` ; les autres correctifs d'audit sont livrés en 0.8.0 |
| 7 | Carte image partageable par la feuille de partage Android, à partir du dessin de carte existant | Moyen | Faible | Idiome, Kawaii : acquisition sans compte |
| 8 | Débloquer les éléments de Glose sur des réussites différées (mot vu il y a 7 jours ou plus) plutôt que sur le quiz parfait du jour | Moyen | Faible | Récompenser l'apprentissage, pas la métrique facile (Mogavi et al.) |
| 9 | Nouveaux formats de quiz : phrase à trous à partir du champ « exemple », sens inversé | Moyen | Faible à moyen | Le rappel l'emporte sur la reconnaissance (Rowland) ; le contenu existe déjà |
| 10 | Option « ma photo en fond de carte » et bouton « Rétablir l'écran de verrouillage » (`clear(FLAG_LOCK)`) | Moyen | Moyen | Perte structurelle du fond ; modèle de Bible Lock Screen |
| 11 | Widget d'accueil en `RemoteViews` (Glance exige AndroidX), épinglage depuis l'appli | Moyen à fort | Moyen | Gratitude : +25 % de rétention chez les utilisateurs du widget, adopté par 10 % des utilisateurs actifs quotidiens, chiffres corrélationnels ([Android Developers Blog](https://android-developers.googleblog.com/2026/05/how-gratitude-widgets-boosted-user-retention-25-percent.html)) ; donne aussi accès au mode hub d'Android 16 QPR2 |
| 12 | Fiche Play (titre avec « mot du jour » et « mots rares », première capture sur l'écran de verrouillage), déclarations `specialUse` et alarmes exactes, inscription à la vérification développeur | Fort (distribution) | Moyen | Règles de la Play Console ; échéance 2027 |
| 13 | Prononciation par le `TextToSpeech` du système | Faible à moyen | Faible | Attente de base, mais la synthèse vocale se trompe souvent, ce qui est une plainte sur Vocabulary ; **[hypothèse]** le risque est fort sur des mots rares |
| 14 | Synonymes et antonymes ; niveaux et thèmes ; retour « trop obscur » | Faible à moyen | Fort (éditorial, 1 342 fiches), sauf le retour (faible) | Le retour « trop facile / trop difficile » de Dicode est la version bon marché |

**[inférence]** À ne pas faire :

- **classements et cercles compétitifs**, liés au « gamification misuse » ([Mogavi et al.](https://arxiv.org/abs/2203.16175)) ;
- **Live Updates**, que Google interdit pour un contenu d'ambiance ou lié aux centres d'intérêt ([Android Developers](https://developer.android.com/develop/ui/views/notifications/live-update)) ;
- **`USE_EXACT_ALARM`** et **`MANAGE_EXTERNAL_STORAGE`** ;
- toute notification du type « Glose s'ennuie de vous » ;
- toute personnalisation payante.

## Ce que Definiday fait déjà mieux, à garder tel quel

| Atout | Pourquoi c'est mieux que la concurrence | Source |
|---|---|---|
| Mot dessiné sur le fond de verrouillage | Aucune appli de vocabulaire ne le fait ; c'est la seule surface toujours visible sur Android sans mode hub, et elle évite la friction d'installation d'un widget. Sur la notification, les plaintes portent sur sa disparition : « on n'a pas le temps de mémoriser le mot » | [Duolingo](https://blog.duolingo.com/widget-feature), [Vocabulous Play](https://play.google.com/store/apps/details?id=com.motdujour.fr) |
| Le même mot toute la journée | Demande explicite : « I would want the same word displayed all day » | [MyVocab](https://apps.apple.com/us/app/id1614457796) |
| Mots vraiment rares, avec anecdote historique et étymologique | Répond à la plainte n° 1 des natifs (mots trop faciles) et à la demande d'étymologies | [Vocabulary Play](https://play.google.com/store/apps/details?id=com.hrd.vocabulary), [Dogan Ekici](https://apps.apple.com/us/app/id1536210624) |
| Liens Académie et TLFi | Le lien vers un dictionnaire de référence est loué par les utilisateurs. Soutenu en fait un gage de vérification. L'appli TLFi à 1,99 € a 3 455 notes à 4,73 | [Dogan Ekici](https://apps.apple.com/us/app/id1536210624), [Soutenu](https://apps.apple.com/fr/app/id6816549155), [TLFi](https://apps.apple.com/fr/app/id294194645) |
| Gratuit, sans pub, sans compte, hors ligne | Le marché facture 35 à 60 $ par an ; la publicité et la longueur des écrans de consentement RGPD font partie des plaintes | [1 Jour 1 Mot Play](https://play.google.com/store/apps/details?id=com.and.jag.unjour1mot), [Merriam-Webster Play](https://play.google.com/store/apps/details?id=com.merriamwebster) |
| 18 palettes et 14 polices offertes | Kawaii en offre 5 sur 18 gratuitement ; Proust réserve des thèmes au Premium | [Kawaii](https://play.google.com/store/apps/details?id=com.kawaii.vocabulary), [Proust](https://apps.apple.com/fr/app/id6758053954) |
| Monsieur Glose, jamais moqueur, et ses éléments à débloquer | Aucun concurrent anglophone ne propose de mascotte à débloquer ; la recherche condamne la culpabilisation | [Castro et Valença](https://sol.sbc.org.br/index.php/ihc/article/view/37677) |
| Série que les jours sans mot ne cassent pas | C'est ce que montrent les travaux sur les séries rompues et sur l'habitude | [Silverman et Barasch](https://www.colorado.edu/business/faculty-research/2023/04/19/or-track-how-broken-streaks-affect-consumer-decisions), [Lally](https://bps.org.uk/research-digest/how-form-habit) |
| Citations peu connues du domaine public, mêlées aux mots | Seul Swann s'en approche (citations d'écrivains) ; Monkey Taps les cantonne à une autre appli | [Swann](https://apps.apple.com/fr/app/id6821071446) |
| Thème sombre et passe d'accessibilité | Le thème sombre manque chez 1 Jour 1 Mot, Larousse et Le Robert Ortho ; le support de TalkBack est incomplet chez Le Robert Ortho | [Larousse Play](https://play.google.com/store/apps/details?id=fr.larousse.dico.francais), [Le Robert Ortho Play](https://play.google.com/store/apps/details?id=com.lerobert.ortho) |
| Mot au hasard, freiné en douceur ; durée d'affichage et retrait au déverrouillage | Plus souple que le « une fois par jour » de Dogan Ekici ; aucun équivalent trouvé pour le retrait au déverrouillage | [Dogan Ekici](https://itunes.apple.com/lookup?id=1536210624&country=us) |

## Conclusion

Le benchmark déplace la question posée. Definiday ne manque pas de ce que les autres vendent (thèmes, mots, mascotte), mais de ce qu'ils considèrent comme acquis : un mot qui revient. Les quatre premières améliorations de la liste réutilisent ce qui existe déjà. La règle des 9 mois suppose que les mots affichés et leurs dates sont mémorisés : c'est une **[inférence]** sur le code, à confirmer. Les fiches ont déjà des exemples. Le dessin de la carte existe. Le quiz et sa mascotte sont en place. Ces chantiers transforment donc un affichage quotidien en apprentissage sans toucher à la promesse « simple, sans compte, hors ligne ».

Le second enseignement est que la distribution pèsera plus que les fonctions. La revue du service `specialUse`, le refus par défaut des alarmes exactes et la vérification des développeurs en 2027 touchent directement l'architecture actuelle. Il vaut mieux trancher avant de soumettre l'appli au Play Store qu'après un refus. Trois incertitudes demeurent. Aucune donnée d'usage de Definiday n'existe. Les avis de store lus sont ceux affichés en tête de fiche, pas un échantillon. Le gain du widget et celui de la révision pour des adultes qui apprennent leur propre langue sont extrapolés, pas mesurés. Un test simple serait de comparer le taux de bonne réponse sur les questions J+7 et J+30, avec et sans la révision : il dirait vite si le chantier n° 2 tient ses promesses.
