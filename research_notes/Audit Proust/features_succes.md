# Audit « Proust : Vocabulaire français » : fonctionnalités et raisons du succès

Relevé du 2026-11-06. Sources principales :
- App Store FR : https://apps.apple.com/fr/app/proust-vocabulaire-fran%C3%A7ais/id6758053954
- iTunes lookup : https://itunes.apple.com/lookup?id=6758053954&country=fr
- Google Play : https://play.google.com/store/apps/details?id=com.proust.app&hl=fr&gl=FR
- Avis App Store (RSS) : https://itunes.apple.com/fr/rss/customerreviews/id=6758053954/sortBy=mostRecent/json
- Site : https://proustapp.org
- TikTok : https://www.tiktok.com/@proustapp
- Estimations tierces : https://mwm.ai/apps/proust/6758053954

Convention : **[F]** = fait sourcé, **[I]** = inférence de ma part.

---

## 1. Version Google Play : elle existe

| Champ | Valeur | Source |
|---|---|---|
| Package | `com.proust.app` (même bundleId que l'iOS) | Play, lookup |
| Titre Play | « Proust : vocabulaire français » | Play |
| Résumé (short description) | « Mots rares, expressions, origines : enrichis ton vocabulaire chaque jour » | Play |
| Développeur affiché | MKa Studio (site proustapp.org) | Play |
| Sortie Play | 29 août 2026 (7 mois après l'iOS) | Play (via google-play-scraper) |
| Dernière mise à jour | 6 nov. 2026, version 2.1.1 | Play |
| Installations | 10 k+ (compteur brut 10 256) | Play |
| Note | 4,3 / 5 sur 556 notes, 546 avis affichés, 37 avis écrits comptés par Google | Play |
| Histogramme | 5★ 413, 4★ 52, 3★ 8, 2★ 0, 1★ 76 | Play |
| Prix | Gratuit, achats intégrés « 8,49 € à 19,99 € si facturé via Play », sans pub (containsAds = false) | Play |
| Catégorie | Enseignement, PEGI 3 | Play |
| Sécurité des données | collecte « Informations personnelles, Informations financières et 3 autres » ; partage « Informations financières et ID de l'appareil » ; suppression possible | Play |

[F] Le site proustapp.org renvoie vers les deux stores (« Gratuit sur iPhone et Android »).

## 2. Fiche App Store (rappel des chiffres)

- [F] Éditeur affiché « MKA », vendeur « kazouini », id 6758053954, sortie 2026-02-03, version 2.1.0 du 2026-11-04, 113,9 Mo, iOS 16+, langues FR, IT, PT.
- [F] Note 4,58 sur 376 notes. Histogramme (JSON de la page) : 5★ 311, 4★ 28, 3★ 9, 2★ 1, 1★ 27.
- [F] Classement visible sur la page : **Nº 58 Éducation** (FR, 6 nov.).
- [F] Genres : Éducation (principal), Quiz, Jeux.
- [F] Sous-titre : « Améliore ton vocabulaire ». Événement in-app en cours : « La nouvelle saison du parcours », sous-titre « 20 chapitres inédits, de la cuisine aux sciences ».
- [F] Estimation mwm.ai : 25 k+ téléchargements App Store, seuil atteint en octobre 2026 (top 2,3 % des apps sorties en 2026). Chiffre tiers, à prendre comme ordre de grandeur.
- [F] Captures marketing : « 4,8 » et « +50 000 utilisateurs » (non vérifiable, ne correspond pas aux stores : 4,6 et 4,3).

### Prix des abonnements (App Store, page « Achats intégrés »)

| Pays | Hebdo | Mensuel | Annuel (3 produits) |
|---|---|---|---|
| France | 6,99 € | 9,99 € | 9,99 € ; 19,99 € ; 19,99 € |
| Belgique | 6,99 € | 9,99 € | 9,99 € ; 19,99 € ; 19,99 € |
| Canada | 7,99 $ CA | 11,99 $ CA | 11,99 ; 24,99 ; 24,99 $ CA |
| États-Unis | 5,99 $ | 8,99 $ | 8,99 ; 17,99 ; 17,99 $ |
| Royaume-Uni | 5,99 £ | 8,99 £ | 8,99 ; 17,99 ; 17,99 £ |
| Play FR | fourchette 8,49 € à 19,99 € | | |

[I] Les trois annuels correspondent probablement à un prix plein, un prix promo (9,99 €) et une variante avec essai. Essai gratuit de 7 jours ajouté en 1.1.1 ; des avis parlent aussi d'un essai de 3 jours.

## 3. Liste complète des fonctionnalités

Croisement App Store (description + notes de version), Play (description + Nouveautés), site, captures.

### Contenu
- [F] Fiche mot : définition, **étymologie**, **synonymes**, **exemples d'usage**, **prononciation audio** (deux voix, « Louis et Mélanie », version 2.0.4 ; choix de voix depuis 1.1.4), « famille du mot », encart « Le savais-tu ? » (anecdote, 1.1.6).
- [F] **Expressions françaises** et leur histoire (« des centaines », 1.1.2) ; rubrique « pourquoi on dit… » (avoir le cafard, verlan).
- [F] **Citations** dans le fil (2.0.4), citations favorites (2.0.7).
- [F] **Coin lecture** : « de grands textes à lire quelques pages par jour, épisode après épisode » (2.1.0).
- [F] Volume : site « +1000 mots » ; réponse du développeur sur Play (3 nov.) : « Chaque fiche est écrite et vérifiée à la main, plus de 4 000 à ce jour ». À comparer aux 1 342 mots de Definiday.
- [F] **18 catégories** sur Play (histoire, environnement, société, art, corps humain, étymologie, cuisine, conversation, caractères, expressions, littérature, émotions, technologie, sciences, voyage, bureau, musique, argot). Site et App Store en annoncent encore 4.
- [F] Feed vertical à défilement infini (« Tu ouvres l'application, tu fais défiler »), un nouveau mot « rejoint ton flux chaque matin ».

### Mécanique d'apprentissage
- [F] **Définition cachée, « Devine avant de révéler »** : bouton « Découvrir le sens » (site + capture 1-2).
- [F] Quiz courts : QCM, phrase à trous (« Quel mot complète la phrase ? »), « Que veut dire [mot] ? » ; questions mélangées (1.0.4), quiz repensé (1.1.0), « adaptatifs » selon le site.
- [F] **Révision le soir** : « le soir l'application te repropose ce que tu as failli oublier » (Play) ; onglet « Ma révision : revoir au bon moment » (2.0.5), qui met marque-pages et favoris en tête (Play 2.1.1).
- [F] **Parcours en chapitres** avec niveaux, épreuves et mini-jeux : 8 mini-jeux (2.0.5) puis 13 (Play 2.1.1) ; « saisons » de 20 chapitres sur 6 thèmes.
- [F] Statistiques mémoire : mots « acquis, en cours, fragiles » (Play 2.1.1) ; stats par catégorie (1.1.0) ; écran stats avec série, jours de la semaine cochés, compteurs découvertes / favoris / marque-pages, « Ce que tu retiens », courbe de prédiction (capture 5).

### Motivation et social
- [F] Série quotidienne (streak), « paliers de réussite du Novice à la Légende », objectifs journaliers, badges et succès (1.1.0), **179 médailles** (2.0.8).
- [F] **Ligue** compétitive (2.0.6) « du Salon à l'Empyrée », 10 paliers, fiche de chaque joueur, emblème de ligue (2.0.7), **avatar composable** (2.0.9). Slogan de version : « Les autres ne t'attendront pas ! ».
- [F] Parrainage (1.1.0). Bouton « Partager cette histoire » sur la fiche (capture 3). News mensuelles (Play 2.1.1).
- [F] Mascotte : caricature dessinée de Marcel Proust présente dans les captures et l'icône.

### Surfaces et personnalisation
- [F] **Widget** (écran d'accueil, et écran verrouillé iOS : capture 4 « Ton mot du jour sans déverrouiller ton tel »), affiche aussi les expressions (1.1.3).
- [F] Notifications avec **choix de l'heure** (1.1.7), rappels « à l'heure qui t'arrange ».
- [F] Thèmes visuels (6 nouveaux en 1.1.0, certains Premium), **icônes alternatives** (1.0.9, 1.1.0), vibrations (adoucies en 2.0.8).
- [F] Favoris, signets (marque-pages), historique complet (Play).
- [F] Mode hors-ligne (site). Le site affirme « Tout reste sur ton appareil. Rien ne sort, jamais », alors que la ligue et les fiches de sécurité des données impliquent un serveur et des identifiants.

### Onboarding (reconstitué depuis les notes de version et les avis)
- [F] Refait au moins 7 fois en 9 mois (1.0.6, 1.0.8, 1.0.9, 1.1.2, 1.1.3, 1.1.4, 1.1.6) : barre de progression, « sélection de niveau au démarrage », plan d'apprentissage personnalisé avec récapitulatif, « parcours d'inscription », animation des avantages Premium, vidéo en boucle sur le paywall.
- [F] Il se termine par l'écran d'essai Premium. Nombreux avis : impossible de le fermer sans démarrer l'essai (voir section 5).

### Gratuit contre Premium
- [F] Gratuit selon le développeur : « des mots, des expressions et des quiz chaque jour, sans abonnement ».
- [F] Premium (Play) : retire la **limite quotidienne**, ouvre tout le parcours, toutes les catégories, tous les thèmes et icônes, révision et entraînement « sans compteur ». App Store : « sans publicité » (Play : pas de pub déclarée).
- [F] Selon deux avis App Store, le **widget / mot sur l'écran verrouillé est payant** alors qu'il est mis en avant dans la publicité Instagram.

## 4. Historique des versions (cadence et contenu)

[F] 25 versions visibles sur l'App Store entre le 9 févr. et le 4 nov. 2026 (plus 1.0.1 à 1.0.3 vues dans les avis), soit environ **une version toutes les 11 jours**, avec un trou du 27 avril au 14 août puis une refonte 2.0 synchronisée avec la sortie Android.

| Version | Date | Contenu |
|---|---|---|
| 1.0.4 | 9 févr. | lisibilité cartes, quiz mélangé, défilement infini, indicateur de swipe, notifications simplifiées |
| 1.0.5 | 10 févr. | animation Premium dans l'onboarding, paywall vidéo, page « limite quotidienne », milliers de définitions améliorées |
| 1.0.6 | 17 févr. | refonte onboarding, widget amélioré, tri du feed |
| 1.0.7 / 1.0.8 | 21 et 26 févr. | gestion abonnements, nouvel accueil, **abonnement hebdomadaire** |
| 1.0.9 | 5 mars | nouvelles icônes d'app, barre de progression onboarding, animations |
| 1.1.0 | 9 mars | stats par catégorie, quiz repensé, parrainage, 6 thèmes, badges |
| 1.1.1 | 12 mars | **essai gratuit 7 jours**, paywall dans l'onboarding |
| 1.1.2 | 22 mars | centaines d'expressions, suivi de progression |
| 1.1.3 | 28 mars | onboarding personnalisé + plan, widget expressions |
| 1.1.4 | 29 mars | écrans d'abonnement, choix de la voix |
| 1.1.5 / 1.1.6 | 31 mars, 22 avr. | lisibilité, inscription, « Le saviez-vous ? » |
| 1.1.7 | 27 avr. | **choix de l'heure des notifs**, catégories argot / étymologie / expressions, niveau au démarrage |
| 2.0.0 à 2.0.3 | 14 au 30 août | refonte visuelle, rendu plus rapide, « le Parcours devient la progression » |
| 2.0.4 | 7 sept. | voix Louis et Mélanie, citations dans le fil |
| 2.0.5 | 13 sept. | parcours en 8 mini-jeux, onglet « Ma révision » |
| 2.0.6 | 2 oct. | **ligue** compétitive |
| 2.0.7 | 10 oct. | médailles, emblème de ligue, citations favorites |
| 2.0.8 | 27 oct. | 179 médailles, profil redessiné, vibrations plus sobres |
| 2.0.9 | 28 oct. | avatar personnalisable |
| 2.1.0 | 4 nov. | coin lecture |
| Play 2.1.1 | 6 nov. | profil, stats mémoire (acquis / en cours / fragiles), ligue 10 paliers, révision, 13 jeux, news |

Lecture :
- [F] Toutes les notes depuis 2.0.0 commencent par une demande d'avis (« laissez-nous un avis : nous lisons tout »).
- [I] Février et mars : itérations surtout sur la monétisation (paywall, hebdo, essai, onboarding). Ce que les utilisateurs semblent avoir demandé : heure des notifications (1.1.7), vibrations plus sobres (2.0.8), widget pour les expressions, lisibilité (1.0.4, 1.1.5), voix. Depuis août : course à la rétention de type Duolingo (parcours, mini-jeux, ligue, médailles, avatar).

## 5. Avis : ce qui est loué, ce qui est reproché

Corpus lu : **21 avis écrits App Store** (le RSS n'en expose pas plus : pages 2 à 10 vides, idem FR/BE/CH/CA) et **57 avis Play** (tous ceux renvoyés par l'API, du 10 sept. au 5 nov.). Les 376 notes iOS sont surtout des notes sans texte.

### App Store (21 avis) : 8 à 5★, 1 à 3★, 12 à 1★
- Paywall / carte bancaire obligatoire / essai forcé : **12** (11 à 1★ + le 3★).
  - « Pas de formule gratuite, obligé de rentrer sa carte » (avis App Store, 21 oct. 2026)
  - « Perdez pas votre temps, c'est payant pour mettre le mot du jour sur l'écran verrouillé comme sur leur pub Insta. » (avis App Store, 28 oct. 2026)
  - « j'aurai vraiment aimé qu'on puisse mettre les widgets sans avoir besoin de payer l'app. C'est vraiment grâce aux widgets que j'aurai pu retenir les mots » (avis App Store, 23 oct. 2026, 3★)
  - « une période d'essai gratuite de sept jours pour au final ponctionner au bout de trois jours » (avis App Store, 12 oct. 2026)
- Bug : 1 (mini-jeu du parcours qui se fige, 15 oct.).
- Louanges : 8, sur le côté ludique, soigné, découvrir des mots, **le widget** :
  - « J'aime bien mon petit widget pour voir du vocabulaire que je ne connais pas. Très cool et pas prise de tête » (avis App Store, 8 févr. 2026)
  - « Il y a bien longtemps que je n'avais pas trouvé une application aussi ludique et sympathique ! » (avis App Store, 29 oct. 2026)
  - « Très bonne appli, tout est soigné, rien à dire. » (avis App Store, 25 août 2026)

### Google Play (57 avis) : 19 à 5★, 8 à 4★, 2 à 3★, 2 à 2★, 26 à 1★
- Paywall / essai forcé : **27 avis** (24 à 1★, 2 à 2★, 1 à 3★), tous depuis le 13 oct. Ils cumulent **137 des 144 votes « utile »** : ce sont eux que voient les visiteurs de la fiche.
  - « Je n'ai jamais eu autant de difficultés à refuser l'abonnement payant d'une application. » (avis Play, 23 oct. 2026, 18 votes utiles)
  - « On peut soit choisir l'abonnement avec essai gratuit, soit choisir l'abonnement avec essai gratuit. » (avis Play, 27 oct. 2026)
  - « l'app vous demande de payer seulement une fois que vous avez tout configurer. » (avis Play, 27 oct. 2026)
  - « ça avait l'air génial mais bloqué par un mur de payement obligatoire. » (avis Play, 25 oct. 2026)
- Contenu jugé trop exotique : 1 (« le mot du jour"schibboleth" Je apprends le français et non l'hébreu. », avis Play, 22 sept. 2026).
- Louanges : 27 à 4-5★, très courtes (« génial », « parfait », « formidable ») ; quelques-unes plus parlantes :
  - « Fine appli pour devenir le collaborateur insupportable de l'Open Space par son verbe » (avis Play, 31 oct. 2026)
  - « Super application pour améliorer mon vocabulaire au quotidien, travail etc... » (avis Play, 10 sept. 2026)
  - « je l'attendais depuis longtemps merci » (avis Play, 12 sept. 2026)
- Avant le 11 oct. : 15 avis dont 13 à 4-5★. Depuis : 42 avis dont 25 à 1★. [I] Le changement coïncide avec un durcissement du paywall et/ou une campagne de pub qui amène un public moins captif.
- [F] Le développeur répond à **tous** les avis Play, par 5 ou 6 phrases types (« Proust est gratuit au quotidien… », « L'essai est un choix, pas une obligation »). Plusieurs auteurs ont mis à jour leur avis pour contester la réponse.

Synthèse :
- Loué : concept « un mot rare par jour », qualité / soin visuel, ludique, **widget**, utilité sociale et pro (parler mieux).
- Reproché : essai forcé avec carte bancaire, gratuité annoncée mais non tenue, widget / écran verrouillé payants alors que c'est l'argument publicitaire.

## 6. Pourquoi ça marche : facteurs de succès

### Faits
- **TikTok @proustapp** : 31,9 k abonnés, 346,4 k j'aime, 168 vidéos, compte créé le 26 janv. 2026 (une semaine avant la sortie), bio « Viens améliorer ton vocabulaire avec Proust / Télécharge Proust ». [F] https://www.tiktok.com/@proustapp
- **Publicité payante Instagram** : citée par trois avis (« comme sur leur pub Insta », 28 oct. ; « The ad is presenting that app as free », 22 oct. ; « Ne vous faites pas avoir par le gros budget communication », avis Play 3 nov.). Compte Instagram non vérifiable (connexion requise) ; aucun lien social sur le site.
- **Visuel publicitaire = mot du jour sur l'écran verrouillé** (capture 4 et avis du 28 oct.).
- **ASO** : titre avec mot-clé exact « Vocabulaire français » ; sous-titre « Améliore ton vocabulaire » ; résumé Play « Mots rares, expressions, origines » ; triple catégorie Éducation + Quiz + Jeux ; événements in-app App Store (« nouvelle saison ») ; captures-slogans courtes avec preuve sociale (« 4,8 », « +50 000 »).
- **Rating** : demandes d'avis dans chaque note de version, réponses systématiques sur Play. Sur iOS, 311 notes 5★ pour seulement 8 avis écrits positifs.
- **Cadence** : une version tous les 11 jours environ, refonte complète en août pour la sortie Android.
- **Studio en série** : l'éditeur publie aussi BigBrain, Manu (aide aux devoirs), Newral, Khint (App Store « Plus d'apps par MKA »).
- **Presse** : aucun article ni interview trouvé (recherches web sur le nom, l'éditeur, TikTok).
- **Contexte** : vague d'apps « mot du jour » françaises en 2026 (voir `research_notes/Benchmark applis mot du jour/apps_francaises.md`), Proust y a la plus forte traction.

### Inférences
- [I] Le moteur d'acquisition est le contenu court sur TikTok + la pub Instagram, avec un crochet très visuel (le mot sur l'écran verrouillé, le mot rare « qui claque »). Le nom « Proust » + la caricature donnent une identité mémorable et partageable.
- [I] Le moteur de revenu est un playbook d'app « growth » : long onboarding qui fait investir l'utilisateur (niveau, objectif, plan), puis paywall avec essai, hebdo à 6,99 €, prix d'ancrage. Il fonctionne sur iOS (note 4,6 grâce aux invites de notation) mais abîme la fiche Play (76 notes 1★ sur 556, 14 %).
- [I] La rétention repose sur l'empilement Duolingo : série, paliers, médailles, ligue, avatar, parcours à chapitres, révision espacée.
- [I] Le widget est à la fois l'argument n°1 et une frustration n°1 parce qu'il est payant : c'est **la faille concurrentielle exacte de Definiday**, dont l'écran verrouillé est gratuit, sans compte ni pub.

## 7. Comparatif Proust / Definiday

| Fonction Proust | Definiday |
|---|---|
| Définition | Oui (originale) |
| Exemple d'usage | Oui |
| Étymologie / anecdote « Le savais-tu » | Oui (anecdote histoire, étymologie, personnages) |
| Liens dictionnaires de référence | Oui (Académie, TLFi), Proust non |
| Synonymes | Non |
| Famille du mot | Non |
| Audio prononciation | Non |
| Expressions et leur origine | Non (partiel si une expression est dans le corpus) |
| Citations | Oui |
| Coin lecture (textes longs) | Non |
| Catégories (18) | Non |
| Définition cachée à deviner | Non |
| Quiz QCM | Oui (soir, mots du jour) |
| Quiz à trous / autres formats | Non |
| Révision espacée des mots « fragiles » | Non (quiz limité aux mots du jour) |
| Parcours, chapitres, mini-jeux | Non |
| Série | Oui (tolérante) |
| Paliers Novice à Légende / niveaux | Non |
| Objectif quotidien | Partiel (1 à 3 mots/jour choisis) |
| Médailles / badges | Partiel (collection Monsieur Glose, 35+ objets) |
| Ligue, avatar, social | Non (volontairement) |
| Statistiques | Non |
| Favoris, signets, historique | Non |
| Partage de la fiche en image | Non |
| Parrainage | Non |
| Widget écran d'accueil | Non |
| Mot sur l'écran verrouillé | **Oui, gratuit** (fond d'écran carte) ; Proust : widget payant |
| Notification quotidienne + choix de l'heure | Oui |
| Thèmes | Oui (18 palettes, 14 polices, tout gratuit) |
| Icônes alternatives | Non |
| Mascotte | Oui (Monsieur Glose, animée, à collectionner) ; Proust : caricature statique |
| Sons / vibrations | Oui (sons optionnels) |
| Mode sombre | Oui |
| Onboarding | Non |
| Hors-ligne | Oui (total) ; Proust : partiel (ligue en ligne) |
| Compte / données collectées | Aucun ; Proust : identifiants, données financières |
| Gratuit sans pub ni paywall | **Oui** ; Proust : freemium, limite quotidienne, essai forcé |

## 8. À copier (motifs et fonctions, pas les textes, l'art, le nom ni la marque)

Classement par rapport impact / effort sur notre stack (Kotlin, Views plateforme, sans AndroidX, hors-ligne, sans backend, gratuit). Effort : XS < 0,5 j, S ≈ 1 j, M ≈ 2 à 4 j, L > 1 semaine.

1. **Widget écran d'accueil du mot du jour, gratuit** (AppWidgetProvider + RemoteViews + `res/xml/appwidget-info`, mise à jour par le `DailyReceiver` existant). Impact très fort : c'est l'argument publicitaire de Proust et la première frustration de ses utilisateurs. Effort S à M. À mettre dans la fiche Play : « widget gratuit ».
2. **Définition cachée, « devine d'abord »** : sur la carte du jour dans l'app, afficher le mot puis un bouton qui révèle la définition, éventuellement 3 propositions. Rappel actif, effet « suspense ». Impact fort, effort XS à S.
3. **Révision espacée dans le quiz du soir** : ajouter 1 ou 2 mots passés ratés ou vus il y a 1, 3, 7 jours (boîtes de Leitner dans les Prefs). C'est ce que Proust vend comme « ce que tu as failli oublier ». Impact fort sur la mémorisation, effort S.
4. **Historique + favoris** : liste des mots déjà reçus (dérivable de la date et de l'ordre du corpus), étoile pour marquer. Demandé implicitement par tout utilisateur qui veut retrouver un mot. Impact fort, effort S à M (une Activity + ListView).
5. **Partage de la carte en image** : on rend déjà la carte en bitmap pour le fond d'écran ; l'envoyer via `ACTION_SEND` (sans FileProvider AndroidX : MediaStore ou petit ContentProvider maison). Moteur viral gratuit (stories, TikTok). Impact moyen à fort, effort S.
6. **Écran statistiques léger** : semaine en pastilles, série, mots vus, taux de réussite au quiz, mots « acquis / fragiles » (sortie du point 3). Impact moyen, effort S.
7. **Onboarding court (3 écrans)** : rythme 1 à 3 mots, heure de notification et du quiz, activation de l'écran verrouillé, présentation de Monsieur Glose. Sans paywall ni « plan » artificiel. Proust l'a retouché 7 fois : c'est là que se joue l'activation. Impact moyen à fort, effort S.
8. **Invite de notation au bon moment** : après un quiz réussi ou une série de 7 jours, une boîte de dialogue maison qui ouvre `market://details?id=…` (pas de lib Play Core). Proust a 4,6 sur iOS grâce à cela. Impact fort sur l'ASO, effort XS.
9. **Prononciation audio** via `android.speech.tts.TextToSpeech` (plateforme, hors-ligne si la voix française est installée). Proust l'a dans chaque fiche. Impact moyen, effort S.
10. **Paliers de progression nommés** (titres non compétitifs liés au nombre de mots appris), branchés sur la collection Glose existante. Impact moyen, effort XS à S.
11. **Icônes alternatives** via `activity-alias` activés par `PackageManager.setComponentEnabledSetting`, à débloquer dans la collection Glose. Impact faible à moyen, effort S.
12. **ASO et contenu court** (hors code) : titre avec « mot du jour » et « vocabulaire français », résumé Play explicite « gratuit, sans pub, sans compte, widget et écran verrouillé », captures-slogans courtes, vidéos verticales « un mot rare en 15 secondes » avec la carte et Glose. Impact fort, effort M en continu.

À ne pas copier : essai forcé avec carte bancaire, limite quotidienne payante, abonnement hebdo, ligue et classements (backend, compte, pression sociale contraire au positionnement doux de Definiday), chiffres marketing invérifiables, réponses types aux avis. Synonymes, expressions et catégories : utiles mais coût de contenu élevé (L), à garder pour plus tard.

## 9. Limites de ce relevé
- Le RSS App Store ne donne que 21 avis écrits ; Play 57 avis (API limitée aux plus récents).
- Instagram non consulté (connexion requise) ; vues des vidéos TikTok non disponibles dans la page.
- Téléchargements iOS : estimation tierce mwm.ai, pas un chiffre Apple.
- Prix Suisse non lus (page en allemand non analysée).
