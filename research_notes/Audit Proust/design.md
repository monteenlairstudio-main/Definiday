# Audit design : Proust : Vocabulaire français (comparé à Definiday)

Date : 6 novembre 2026. Périmètre : design visuel, mise en page, navigation, captures de store, ASO visuelle. Pas d'audit du contenu ni du modèle économique (voir les autres notes du dossier).

## Sources regardées

- Fiche App Store (API iTunes lookup, `id6758053954`, pays FR) : version 2.1.0 du 4 novembre 2026, note 4,58 sur 376 avis, gratuit avec abonnement, catégories Éducation / Quiz / Jeux, FR IT PT, 114 Mo, iOS 16+. Éditeur affiché « kazouini », compte développeur « MKa ». 7 captures iPhone, pas de captures iPad.
- Fiche Google Play : elle existe, `com.proust.app` (lien trouvé sur le site de l'éditeur), titre « Proust : vocabulaire français », éditeur « MKa Studio », 4,3 étoiles sur 554 avis, 10 k+ téléchargements, achats via l'appli. Description courte : « Mots rares, expressions, origines : enrichis ton vocabulaire chaque jour ». 6 captures.
- Site éditeur : https://proustapp.org (Next.js ; polices Newsreader et Plus Jakarta Sans ; sections « Chaque matin », « Quiz adaptatifs », « Progression claire », « Données privées », « Mode hors-ligne », bandeau défilant de mots rares, puces « ❦ »).
- Captures téléchargées (données non fiables, traitées avec `python -I`) dans `/tmp/claude-0/-home-user-Definiday/5dd8da8e-acaa-53f1-b2be-feeef333d005/scratchpad/proust_shots/` : `ios_1.jpg` à `ios_7.jpg`, `icon.jpg`, `play_1.png` à `play_6.png`, `play_icon.png`.
- Nos écrans : `app/build/demo/ui/home_light.png`, `home_dark.png`, `quiz_question.png`, `quiz_faux.png`, `quiz_fin.png`, `collection_light.png`, `settings_light.png`, `styles_light.png`, `app/build/demo/app.png`, `lockscreen_0.png`.
- Cadre de critique : skill `design:design-critique` (première impression, utilisabilité, hiérarchie, cohérence, accessibilité).

## 1. Ce que montrent les captures de Proust

Ordre App Store (noms de fichiers de l'éditeur : fiche-1-feed, 2-definition, 3-fiche-mot, 4-widget, 6-stats, 5-quiz, 7-avis) :

| # | Légende (titre / sous-titre) | Écran montré | Annotations flottantes |
|---|---|---|---|
| 1 | « Un mot, une expression, et tu ne parles plus pareil » ; « ★★★★★ 4,8 · Le vocabulaire en 1 minute par jour » | Accueil : mot seul, bouton « Découvrir le sens », 4 boutons ronds (i, partager, cœur, signet), compteur trophée 523/1000 | « CHAQUE MATIN · Un mot rare, sa définition cachée », « Devine avant de révéler » ; mascotte en pied en haut |
| 2 | « La définition se dévoile, le mot reste » | Même écran, définition révélée en italique | « LE JEU · Ce que tu trouves seul, tu le retiens », « Une seconde de suspense, un mot pour la vie » |
| 3 | « Deviens celui qui sait d'où viennent les mots » | Fiche complète : mot, définition, carte « L'HISTOIRE », « La famille du mot », « L'usage » (exemples entre guillemets), bouton « Partager cette histoire » | « LE SAVAIS-TU ? [Anecdote étymologique · une phrase courte] » (texte gabarit oublié) |
| 4 | « Ton mot du jour sans déverrouiller ton tel » (fond marine) | Écran verrouillé iOS : widget texte au-dessus de l'heure et grand widget avec mascotte qui lit, bouton « Réviser » | Pastilles « À la pause café », « Dans le métro » |
| 5 | « Un peu chaque jour, retenu pour toujours » | Statistiques : flamme + « 5 jours », rangée de 7 jours cochés, 3 tuiles chiffrées (523 découvertes, 18 favoris, 8 marque-pages), barres par catégorie | « SÉRIE EN COURS · La régularité bat le bachotage » ; mascotte bras levés |
| 6 | « Teste-toi. Retiens vraiment. » | Quiz 1/9 avec barre, phrase à trous en italique, 3 réponses avec rond à gauche | « Que veut dire [mot] ? ✓ · Tu l'as lu hier dans Proust » (gabarit oublié) |
| 7 | « Chaque mot a une histoire. Et maintenant, tu la connais. » (fond marine) | Pas d'écran : « 4,8 ★ de note moyenne », « +50 000 utilisateurs », 2 avis 5 étoiles, icône, mascotte pouce levé | |

Google Play : les 6 premières images, mêmes fichiers (le « ★★★★★ 4,8 » est retiré de la n° 1, la n° 7 « avis » est absente). Elles gardent le cadre iPhone à Dynamic Island et l'écran verrouillé iOS, sur une fiche Android.

## 2. Identité visuelle de Proust

- **Palette** : 3 couleurs plus un accent. Marine `#1D3A64` (texte, boutons, aplats), pêche `#F9DBB7` (aplats, surlignage), crème `#FBF4EA` (fond), blanc pour les cartes, orange `#E8952C` environ pour les surtitres et l'icône trophée. Mêmes couleurs dans l'icône, la mascotte, l'appli et les captures : c'est ce qui donne l'impression de marque cohérente.
- **Typographie** : deux familles seulement. Serif de texte (Newsreader, d'après le CSS du site) pour le mot, la définition en italique, les titres d'écran (« Statistiques ») et les réponses du quiz ; sans-serif géométrique (Plus Jakarta Sans) pour les légendes de store, les boutons et les petits libellés. Surtitres en petites capitales espacées, orange (« L'HISTOIRE », « LE SAVAIS-TU ? », « CHAQUE MATIN »). Le widget ajoute une chasse fixe pour « MOT DU JOUR ».
- **Illustration** : un personnage masculin moustachu en costume (évocation de Marcel Proust), style vectoriel plat, contours absents, 4 couleurs. Plusieurs poses (présente, lit un livre, bras levés, pouce levé). Il sert dans le store et le widget, très peu dans l'appli visible.
- **Icône** : buste du même personnage, plat, sur aplat pêche. Lisible, distinctive, cohérente avec la palette. La description annonce des icônes alternatives (premium).
- **Iconographie UI** : traits fins, style SF Symbols, dans des boutons ronds blancs à ombre douce (48 dp environ).
- **Surfaces** : cartes blanches à grand rayon (environ 20 dp), ombre portée très diffuse, sur fond crème. Pas de bordures. Peu d'éléments par écran, beaucoup de vide.

## 3. Mise en page de la fiche mot (hiérarchie)

1. Le mot, serif, très grand, centré, marine.
2. Pastille « prononciation » avec haut-parleur (audio).
3. Définition en italique serif, centrée, gris marine. Sur l'accueil elle est d'abord **cachée** derrière « Découvrir le sens ⌄ ».
4. Rangée de 4 actions rondes (infos, partager, favori, signet).
5. Fiche complète (écran modal fermé par une croix ronde) : définition en tête, puis carte « L'HISTOIRE » (étymologie), titre « La famille du mot » (mots apparentés), titre « L'usage » avec carte d'exemples entre guillemets, bouton plein « Partager cette histoire ».
6. Pas de nature grammaticale sur l'accueil ni sur la fiche ; le widget l'affiche (« n.f. » en petit italique à côté du mot).

Critique : la hiérarchie est très lisible (un seul élément dominant par écran) ; l'alternance cartes blanches / titres serif structure bien une fiche longue. Faiblesses : étymologie tautologique dans la capture (« Du grec 'opopanax'. »), nature grammaticale absente de la fiche, texte gabarit visible.

## 4. Navigation

- Pas de barre d'onglets en bas. Accueil en flux vertical (« fiche-1-feed », on fait défiler les mots).
- Barre du haut à 3 boutons ronds : couronne (Premium), compteur trophée « 523/1000 » au centre (progression globale), profil.
- Statistiques, fiche complète, quiz : écrans modaux (feuille qui monte, croix ou flèche ronde en haut à gauche).
- Le compteur central sert à la fois de jauge de progression et d'entrée vers les statistiques : une seule pastille remplace un onglet.

## 5. Onboarding

Rien de visible dans les captures. Le site parle de « 3 étapes ». Lacune : on ne peut pas juger leur onboarding sans installer l'appli (iOS uniquement accessible ici via captures).

## 6. Quiz

- En-tête : retour rond, « 1/9 » centré, fine barre de progression sous l'en-tête.
- Type montré : phrase à trous (« L'______ romane orne la façade de l'église. ») en italique serif centré, consigne grise « Quel mot complète la phrase ? » juste au-dessus des réponses.
- Réponses : 3 cellules pleine largeur, fond beige, bordure fine, **rond de sélection à gauche**, texte aligné à gauche, puis un bouton de validation (bas d'écran coupé).
- Le site promet « feedback immédiat, zéro jugement » et une difficulté adaptative.

## 7. Progression, niveaux, stats

- Série en jours avec flamme, rangée de 7 jours (carrés marine cochés / vides), jour courant en gras.
- 3 tuiles chiffrées de même taille : grand chiffre serif, libellé gris en dessous.
- « Ce que tu retiens » (carte vide qui invite au premier quiz), « catégories de prédilection » en barres horizontales.
- Paliers « du Novice à la Légende », objectifs journaliers (description), compteur 523/1000 en tête d'accueil.

## 8. Widget

- Petit widget texte au-dessus de l'heure (mot en gras, définition en 2 lignes).
- Grand widget : carte crème en dégradé, surtitre chasse fixe orange « MOT DU JOUR » avec fleuron, mot en serif italique, nature « n.f. », définition en sans-serif, bouton plein « Réviser → » et bouton rond « ↻ », mascotte qui lit dans le coin droit.
- La mascotte dans le widget est le meilleur usage de leur illustration : le personnage habite l'écran de l'utilisateur.

## 9. Thèmes, mouvement

- Thèmes et icônes alternatives annoncés, en partie réservés au Premium. Aucune capture de thème sombre.
- Mouvement : non visible sur captures fixes. Le geste « révéler la définition » suggère une animation de dévoilement (flèche ⌄). Le site a un bandeau défilant de mots.

## 10. Storytelling des captures et ASO visuelle

Ce qui marche :
- **Une promesse par capture**, en 2 lignes de titre gras + 1 sous-titre gris. Tutoiement, phrases courtes, bénéfice avant fonction (« sans déverrouiller ton tel » plutôt que « widget »).
- **Mot clé surligné** dans un bloc pêche arrondi (effet marqueur), une seule fois par titre.
- **Appareil grand, légèrement incliné ou rogné en bas**, qui déborde du cadre : l'écran se lit sans zoom.
- **Annotations flottantes** (cartes blanches légèrement tournées, surtitre orange + phrase serif soulignée au marqueur) qui expliquent le geste sur l'écran même.
- **Arc narratif** : mot caché → révélé → fiche → widget → stats → quiz → preuve sociale. Le même mot (« opopanax ») traverse les 3 premières images, ce qui montre un parcours réel.
- **Rythme de fond** : crème avec grand quart de cercle pêche, alternance avec deux fonds marine (widget, avis) pour créer des respirations.
- Mascotte sur la 1re et la dernière image (ouverture, clôture), icône rappelée à la fin.

Ce qui est mal fait (à ne pas reproduire) :
- Chiffres non cohérents avec les stores : « 4,8 » et « +50 000 utilisateurs » contre 4,58 (376 avis) sur l'App Store et 4,3 (554 avis), 10 k+ téléchargements sur Play. Risque de règles store (affirmations non vérifiables) et de perte de confiance.
- Textes gabarits oubliés : « [Anecdote étymologique · une phrase courte] » (capture 3) et « Que veut dire [mot] ? » (capture 6).
- Sur Google Play : cadre iPhone, écran verrouillé iOS, Dynamic Island. Google déconseille les cadres d'un autre système ; un utilisateur Android ne se reconnaît pas.
- Photo de fond d'écran verrouillé banale (étagère de livres en anglais, titres lisibles), sans rapport avec le français.
- Annotations qui masquent l'écran qu'elles commentent (définition coupée en capture 2 sur Play, stats masquées par la mascotte en capture 5).
- Sous-titres gris environ `#6F7279` sur `#FBF4EA` : environ 4,2 à 4,4:1 au pixel le plus sombre, donc sous 4,5:1 en réalité pour du texte de 14 à 16 pt.

## 11. Critique structurée (skill design-critique)

### Impression d'ensemble
Proust paraît « premium » par sa retenue : 3 couleurs, 2 polices, une seule chose importante par écran, des cartes blanches qui flottent. Definiday a un contenu plus riche et plus fiable, et une mascotte plus attachante, mais son accueil ressemble à un formulaire : boutons pleine largeur empilés et bordés, liens soulignés, 3 familles de texte (Noto Serif, Roboto, police du mot).

### Utilisabilité
| Constat | Gravité | Recommandation |
|---|---|---|
| Definiday : 3 gros boutons empilés + 2 liens + lien « Réglages » en bas ; aucune action n'est visuellement prioritaire à part « Quiz du soir » | Moyenne | Rangée d'actions rondes sous la carte, Réglages et Collection en haut |
| Definiday : pas de partage du mot | Moyenne | Bouton rond « Partager » (texte, puis image de la carte) |
| Definiday : pas de widget d'accueil, alors que l'écran de verrouillage est notre force | Moyenne | Widget qui réutilise `WordCard.render` |
| Proust : définition cachée par défaut, un tap de plus à chaque mot | Faible | Chez nous, en option ou seulement pour le 1er mot du jour |
| Proust : annotations de store qui masquent l'écran | Faible | Placer les annotations dans les marges |

### Hiérarchie visuelle
- Proust : l'œil va au mot, puis à la définition, puis aux actions. Correct.
- Definiday : l'œil va à Monsieur Glose, puis à la carte sombre (très forte), puis au bouton doré « Quiz du soir ». La carte est bien hiérarchisée (surtitre, mot, nature, définition, exemple). Sous la carte, tout a le même poids (boutons bordés identiques) : c'est là que se perd l'effet premium.

### Cohérence
| Élément | Constat | Recommandation |
|---|---|---|
| Typo | Definiday mélange Noto Serif (boutons, définitions) et Roboto (aides, Réglages, Collection) sans règle visible | Règle : serif pour le contenu et les titres, sans-serif pour l'aide et les libellés techniques, partout |
| Surfaces | Boutons, puces « Aujourd'hui », vignettes de styles : tous beiges à bordure dorée 1 dp | Réserver la bordure au focus et à la sélection ; surfaces blanches à ombre douce pour le reste |
| Quiz | Réponses centrées en pilules, marquage ✓ / ✗ (bon point) | Garder ✓ / ✗, aligner à gauche avec un repère rond |

### Accessibilité
- Definiday est meilleur : contrastes vérifiés (accent 4,7:1, `ink2` 7:1), cibles 48 dp, police ×2,0 testée, ✓ / ✗ en plus de la couleur, mode sombre complet.
- Proust : gris des sous-titres et du libellé « Quel mot complète la phrase ? » proches ou sous 4,5:1 ; pas de mode sombre montré.

### Ce que Definiday fait mieux
- Vrai écran de verrouillage plein écran (fond) et notification publique ; Proust ne montre que des widgets iOS.
- 18 palettes et 14 polices gratuites, chaque mot avec sa palette ; Proust met des thèmes derrière l'abonnement.
- Mode sombre partout, accessibilité testée (×1,3, ×2,0, 360 dp, TalkBack).
- Sources visibles (Académie française, TLFi), anecdotes sourcées ; Proust montre une étymologie creuse et des gabarits.
- Monsieur Glose évolue (poses, pelages, accessoires) : la mascotte est un système de récompense, pas une décoration.
- Pas de pression de série (charte de voix de `interactions_glose.md`) ; Proust pousse la flamme.
- Nature grammaticale toujours affichée.

## 12. Motifs à reprendre dans Definiday (classés)

Règle : on reprend des **motifs** d'interface, jamais leurs dessins, textes, nom ni marque. Ne pas reprendre : un personnage humain moustachu ou en costume d'époque, le trio marine / pêche / crème avec leurs grands quarts de cercle, leurs phrases de légende (« Teste-toi. Retiens vraiment. », « Un peu chaque jour, retenu pour toujours », « Devine avant de révéler », « La définition se dévoile, le mot reste », « Ton mot du jour sans déverrouiller ton tel »), le mot exemple « opopanax », le compteur « x/1000 » avec trophée, ni les surtitres « LE JEU » / « L'HISTOIRE » / « LE SAVAIS-TU ? » mot pour mot (notre « Le saviez-vous ? » existe déjà, au vouvoiement, c'est une formule courante).

Effort sur notre pile (Kotlin, Views de la plateforme, vector drawables, pas d'AndroidX) : S = moins d'une demi-journée, M = 1 à 2 jours, L = plus.

| Rang | Motif | Changement concret chez nous | Effort | Risque |
|---|---|---|---|---|
| 1 | **Captures de store « une promesse par image »** : titre 2 lignes + mot clé surligné, appareil grand qui déborde, 1 ou 2 annotations, même mot sur les 3 premières images | Nouveau gabarit de captures (voir § 13) généré à partir de `app/build/demo/` ; cadre Android (pas iPhone) ; nos palettes, Monsieur Glose | M (outil hors appli, par ex. script dans `tools/`) | Faible ; risque de ressemblance si on reprend leur trio de couleurs ou leurs formes : utiliser Braise / Bleu nuit / Parchemin et des motifs de pelote ou de reliure |
| 2 | **Icône d'appli = la mascotte**, plate, 3 à 4 couleurs, lisible à 48 dp | Remplacer `res/drawable/ic_launcher_fg.xml` (glyphe livre générique doré sur `#1B2440`) par la tête de Monsieur Glose simplifiée (lunettes dorées = signe distinctif) ; garder une version monochrome dédiée pour les icônes à thème ; `icon_bg` en couleur de marque | S à M (la tête existe déjà dans les dessins du quiz, à simplifier) | Faible ; vérifier la lisibilité à 48 dp et dans le masque rond |
| 3 | **Actions rondes sous le mot** (boutons ronds 48 dp, icône fine, surface claire à ombre douce) au lieu de boutons pleine largeur empilés | `activity_main.xml` + `MainActivity.kt` : rangée « Académie », « TLFi », « Partager », « Nouveau mot » (icônes vectorielles, libellés TalkBack, libellé court sous chaque rond) ; « Quiz du soir » reste le seul bouton plein ; nouveau drawable `btn_round.xml` | M | Moyen : découvrabilité des icônes, garder un libellé texte sous chaque rond ; la phrase « Il remplace aussi le mot affiché sur l'écran de verrouillage » doit rester accessible (confirmation au tap) |
| 4 | **Partage d'une carte du mot** | Bouton « Partager » : `Intent.ACTION_SEND` en texte d'abord (mot, nature, définition, « via Definiday ») ; ensuite image via `WordCard.render` exposée par un petit `ContentProvider` maison (pas de FileProvider sans AndroidX) | S (texte), M (image) | Faible ; image : penser au nettoyage du cache |
| 5 | **Widget d'écran d'accueil** : carte du mot avec surtitre, mot, nature, définition, Monsieur Glose dans un coin, bouton « nouveau mot » rond | `AppWidgetProvider` + `RemoteViews` (API plateforme) ; les polices personnalisées n'existent pas dans RemoteViews, donc dessiner la carte en bitmap avec `WordCard.render` (palette et police du mot) dans un `ImageView` ; mise à jour par `DailyReceiver.kt` | L | Moyen : taille mémoire du bitmap (limite des RemoteViews), redimensionnement, cohérence avec le mot du verrouillage |
| 6 | **Hiérarchie de fiche en cartes** : sous la carte du mot, l'exemple et l'anecdote dans des cartes claires sans bordure, surtitre en petites capitales accent, ombre douce | `activity_main.xml` (`anecdote_box`, exemple) + nouveau drawable `card_soft.xml` (coins 20 dp, `android:elevation`) ; supprimer les bordures 1 dp des boutons secondaires dans `btn.xml` (bordure seulement au focus) | S | Faible ; en sombre les ombres ne se voient pas : surface un ton plus clair que le fond |
| 7 | **Barre du haut à boutons ronds** avec une pastille de progression | Haut de `activity_main.xml` : rond « Collection » avec pastille « 12/21 » (objets débloqués, à partir de `Unlocks.kt`) et rond « Réglages » ; retirer le bouton « Collection de Monsieur Glose » et le lien « Réglages » du bas | S | Faible ; rester à 21 objets réels, pas de compteur gonflé |
| 8 | **Quiz : barre de progression + « 1/4 », réponses alignées à gauche avec repère rond, nouveau type « phrase à trous »** à partir de nos exemples (les 1 342 mots en ont un) | `QuizActivity.kt` : garder les pelotes comme indicateur (notre signature) mais ajouter « 1 / 4 » ; type de question « Quel mot complète la phrase ? » en masquant le mot dans `exemple` ; drawable de repère rond (pelote vide `pelote_vide.xml` existe déjà) | S à M | Moyen : formes fléchies (pluriel, conjugaison) que le masquage ne trouve pas ; repli sur la question définition |
| 9 | **Rangée de la semaine + 3 tuiles chiffrées**, sans flamme ni « série en cours » | En tête de `CollectionActivity.kt` : 7 jours (quiz fait = pelote pleine, sinon pelote vide), tuiles « mots vus », « quiz réussis », « objets débloqués » | M | Moyen : contredire la charte « jamais de série menacée » ; donc pas de chiffre de série, pas de perte affichée |
| 10 | **Révéler la définition** (deviner avant de lire) | Option dans Réglages (désactivée par défaut) ; sur l'accueil, définition et exemple masqués derrière un bouton « Voir la définition » avec fondu (`ObjectAnimator`, respect de « animations désactivées ») ; jamais sur l'écran de verrouillage | S | Moyen : friction pour qui veut juste lire ; motif proche de leur argument central, ne pas le mettre en avant dans nos captures |
| 11 | **Règle typographique à 2 familles** | Serif (Noto Serif) pour mot, définitions, titres d'écran, boutons ; sans-serif pour aides et libellés techniques ; aligner `settings_light` et `collection_light` (titres de section en serif) ; `themes.xml` | S | Faible |
| 12 | **Mascotte dans les moments clés hors appli** (widget, notification, fin de quiz, store) | Déjà fait pour la notification et la fin de quiz ; ajouter au widget (rang 5) et aux captures (rang 1) ; garder l'écran de verrouillage sans mascotte (choix existant) | inclus | Faible |

Non retenu :
- Prononciation audio : pas de données phonétiques chez nous (`words.json` n'a ni API ni audio) ; TTS Android possible plus tard, hors design.
- Couronne Premium et paliers « Novice à Légende » : hors modèle Definiday.
- Flux vertical infini de mots : contraire à notre « un mot par jour, pas de noyade ».
- Onboarding : rien à copier (non visible). Besoin propre chez nous : un écran de permission (notifications, alarmes exactes) avec Monsieur Glose, à traiter séparément.

## 13. Proposition de captures de store pour Definiday (Google Play)

Contraintes : 1080 × 1920 (9:16) ou 1080 × 2340, 2 à 8 images, cadre Android neutre ou sans cadre, pas de note ni de nombre d'utilisateurs tant que nous n'en avons pas, pas de texte gabarit, vouvoiement (cohérent avec l'appli). Fond : nos palettes (alternance claire Parchemin, sombre Braise ou Bleu nuit), motif graphique propre (fil de pelote qui relie les images d'un bord à l'autre, au lieu de formes géométriques). Titre en serif gras (Noto Serif ou Libre Baskerville), mot clé souligné au fil de pelote (trait ondulé bordeaux) au lieu d'un surlignage en bloc. Un même mot rare suit les images 1 à 3 (par ex. « estramaçon »).

| # | Promesse (titre provisoire) | Écran | Fond | Annotation |
|---|---|---|---|---|
| 1 | « Un mot rare, là où vous regardez le plus : votre écran de verrouillage » | `lockscreen_0.png` dans un cadre Android, horloge système visible au-dessus | Braise (sombre) | Monsieur Glose assis sur un livre en bas à droite, hors de l'écran |
| 2 | « Définition, exemple, et les sources officielles à un tap » | Accueil (`home_light.png`) recadré sur la carte et la rangée d'actions | Parchemin | Pastille « Académie française · TLFi » pointant les boutons |
| 3 | « Chaque mot a sa couleur et sa police » | Mosaïque de 3 ou 4 cartes `style_*.png` en éventail | Bleu nuit | « 18 palettes, 14 polices, toutes gratuites » |
| 4 | « Le soir, quatre questions avec Monsieur Glose » | `quiz_question.png` puis bulle de fin « bravo » écrite au fil | Parchemin | Pelotes de progression mises en avant |
| 5 | « Plus vous jouez, plus sa garde-robe s'agrandit » | `collection_light.png` recadrée sur 2 rangées + silhouettes verrouillées | Sauge ou Lavande | 3 vignettes de pelages qui sortent du cadre |
| 6 | « De jour comme de nuit » | `home_light.png` et `home_dark.png` côte à côte | Moitié claire, moitié sombre | Aucune |
| 7 | « Lisible pour tous » (optionnel) | `home_font200.png` | Craie | « Grande police, TalkBack, contrastes vérifiés » |
| 8 | Clôture : nouvelle icône (Glose) + « Definiday · un mot par jour, sans compte, sans pub » | Pas d'écran | Braise | Glose qui salue (pose existante) |

Notes :
- L'image 1 vend notre différence (le vrai écran de verrouillage) : Proust la met en 4e et seulement sous forme de widget iOS.
- Ne pas afficher d'avis inventés ; ajouter une image « avis » seulement avec de vrais avis Play, cités exactement.
- Légendes à relire par la charte de voix (`research_notes/Definiday plus fun et mignonne/interactions_glose.md`).
- Graphique de fonctionnalité Play (1024 × 500) : Glose à gauche, carte du mot à droite sur fond Braise.
- Fabrication : un script qui compose titre + capture Robolectric + fond, pour régénérer après chaque changement d'interface.
