# Revue UI/UX : Definiday 0.11.0 (commit 0ee4527)

Méthodes appliquées, dans l’ordre : `design:design-critique` (première impression, utilisabilité, hiérarchie, cohérence), `design:accessibility-review` (WCAG 2.1 AA adaptée à Android : TalkBack, cibles 48 dp, police ×1,3 et ×2,0, w360, sombre, animations) et `design:ux-copy` (clair, concis, cohérent, utile, humain ; ton ; typographie française).

Matériel :
- Captures régénérées par `DefinidayTest.renderDemoImages` (`app/build/demo/ui/*.png`, `app/build/demo/*.png`, `app/build/demo/widget/*.png`), toutes relues, réduites à 540 px (scratchpad `audit011/`).
- Code relu : `MainActivity`, `OnboardingActivity`, `QuizActivity`, `CollectionActivity`, `MotsActivity`, `SettingsActivity` (et `activity_settings.xml`), `res/layout/activity_main.xml`, `values/strings.xml`, `values/themes.xml`, `values/colors.xml`, `values-night/colors.xml`, `drawable/btn*.xml`, `card_soft.xml`, `stamp.xml`, `favori.xml`, `pelote_vide.xml`, `AndroidManifest.xml`.
- Point de départ : `docs/audits/interface.md`, `docs/audits/revue_ui/critique_design.md`, `accessibilite.md`, `textes.md`. Les points corrigés ne sont pas repris ; les points encore ouverts sont rappelés sur une ligne.

Conventions : **[observé]** = visible dans une capture ; **[code]** = lu dans le code ; **[mesuré]** = ratio WCAG calculé ; **[hypothèse]** = jugement ou déduction non vérifiés sur appareil.
Gravité : **Élevé** = gêne la tâche quotidienne, l’apprentissage ou l’accès ; **Moyen** = incohérence ou friction visible chaque jour ; **Faible** = finition.
Renvois : « n° X » = ligne X du tableau classé ; « #X », « AX », « IX », « MX » = constats des revues précédentes.

---

## Verdict

L’appli a nettement mûri depuis la revue précédente : la hiérarchie de l’accueil est enfin lisible (une seule action principale, le Quiz du soir), le quiz est devenu doux et pédagogique (sauge et terracotta, consignes, phrases à trous, révisions espacées, pelotes de progression), la Collection garde son aperçu en vue, l’onboarding demande les permissions en contexte, et la carte du mot, l’écran de verrouillage, le partage et le widget forment une famille visuelle réussie. C’est une appli calme quand on regarde la carte.

Le revers : les fonctions ajoutées en parallèle (Mes mots, Couleurs et polices, widget, sons, notation, onboarding) ont chacune apporté leur propre composant. On compte aujourd’hui **sept rayons d’angle** (0, 4, 12, 14, 16, 20 dp et pilule), **trois sens pour le pointillé** (gagné, verrouillé, caché), **trois façons de signaler une sélection**, **deux systèmes de titre d’écran**, et un mélange Roboto / serif à l’intérieur d’un même composant (tuile de stats, carte de notation, cases de la Collection). Hors de la carte, l’appli parle le langage d’un kit Material générique : disques gris-crème avec pictos au trait, libellés en capitales espacées, tuiles de statistiques, interrupteurs et menus système, encadrés jaunes plats.

Côté navigation, trois entrées clés sont mal placées : **Mes mots** (une icône de tiroir sans libellé, collée à Réglages, qui ouvre ensuite une copie complète de l’accueil), **le widget** (un bouton enfoui dans Réglages, entre deux réglages du mot du jour) et **Couleurs et polices** (déguisé en menu déroulant). Accessibilité : bonne base (cibles, focus, TalkBack sur la carte et le quiz), quelques trous nouveaux (étoiles anonymes dans Mes mots, lien « Plus tard » à 4,22:1, puces qui coupent un mot sur une lettre). Textes : vouvoiement impeccable, ton juste ; la voix de Monsieur Glose change de personne d’un écran à l’autre, et dix chaînes figent son nom alors qu’il deviendra personnalisable.

Priorités : (1) unifier le « kit » (rayons, pointillés, sélection, titres, une seule famille pour l’interface) ; (2) réparer la navigation de Mes mots et donner une vraie place au widget ; (3) les quatre défauts visibles chaque jour : puce coupée en w360, puce unique qui répète la carte, « Choisir » invisible, bandeau plat.

---

## Ce qui a progressé (corrigés vérifiés)

| Réf. précédente | État | Preuve |
|---|---|---|
| Critique #1 aperçu de la Collection hors écran | corrigé | en-tête fixe titre + aperçu + « Choisir » (`CollectionActivity.kt:32-48`) |
| Critique #2 / A14 mot plus petit que la définition | corrigé | plancher 1,3 × définition puis coupe aux syllabes : « ultracrépida-/rianisme » (`home_w360_font200_long.png`, `MainActivity.kt:228-238`) |
| Critique #3 pas d’action principale | corrigé | style `Primary` sur « Quiz du soir », Réglages en icône (`home_light.png`) |
| Critique #4 vert / rouge éblouissants | corrigé | `quiz_good` / `quiz_bad` jour et nuit (`quiz_faux.png`, `quiz_dark_faux.png`) |
| Critique #5 déblocage façon journal | corrigé | timbres « Monsieur Glose a gagné » (`quiz_bravo.png`) ; voir n° 3 pour le pointillé |
| Critique #6 silhouettes claires en sombre | corrigé | `silhouette` nuit `#3A4256` (`collection_dark.png`) |
| Critique #9 alarmes exactes en gris | corrigé | encadré `warn` + bouton dedans (`onboarding_4_light.png`) |
| Critique #13 pelages verrouillés illisibles | corrigé | aperçu en couleur estompée (`collection_light.png`) |
| Critique #14 carte claire fondue dans la page | corrigé | liseré `card_edge` (`home_pale.png`) |
| Critique #16 aide du hasard en mode notification | corrigé | `random_hint` affiché seulement après un tirage, hors mode notification (`MainActivity.kt:63`) |
| Critique #20 apostrophes droites des citations | corrigé | « C’est », « n’y » (`home_quote.png`, `share_2.png`) |
| Critique #21 pas de consigne au quiz | corrigé | « Quel mot correspond à cette définition ? » / « Quel mot complète cette phrase ? » |
| A2 interrupteurs, A3 focus, A5 focus carte, A7 compteur, A12 puce courante | corrigés | `switch_track_*`, `btn.xml` contour 3 dp, `focusCard()`, `quiz_progress`, `isSelected` |
| Textes : `notif_off`, `channel_name`, `<plurals>` du quiz, `unlock_new`, `exact_*`, Glose « Bien vu ! / Pas tout à fait » | corrigés | `strings.xml` |

Régressions et correctifs partiels :

| Réf. | État | Pourquoi |
|---|---|---|
| I8 (« jamais couper un mot n’importe où ») | **régression ailleurs** | la puce « ultracrépidarianism / e » à police ×1,0 en w360 (`home_w360.png`, voir n° 2) ; le widget 2×2 coupe « suivez-moi-jeun / e-homme » (`widget/2x2_long_word.png`). |
| Critique #7 Uncial Antiqua | partiel | limitée aux mots de 8 lettres au plus ; « lendit » (6 lettres) reste « leɴdiꞇ » sur l’écran de verrouillage et l’image partagée (`lockscreen_1.png`, `share_1.png`). |
| Critique #10 mots à revoir en fin de quiz | partiel | les révisions espacées existent, mais l’écran de fin ne rappelle toujours pas les mots ratés (`quiz_fin.png`). |
| Critique #11 puce unique | partiel | la puce courante est marquée ; avec 1 mot par jour (défaut), « AUJOURD’HUI » + une puce identique à la carte restent (`app.png`). |
| Critique #12 bandeau | non corrigé | rectangle plat sans coins, Roboto, sans signe d’action (`home_notifications_off.png`). |
| Critique #15 « Choisir » | non corrigé | pastille `surface_off` sur `bg` ≈ 1,05:1, libellé vague (`collection_light.png`). |
| Critique #18 titres d’écran | non corrigé, aggravé | deux nouveaux écrans, chacun sur un système différent (n° 7). |
| Critique #19 boucle des scripts | non corrigé | le « z » de « zinzolin » déborde encore la marge gauche (`style_zinzolin.png`). |
| A6 titres d’Activity | non corrigé | toujours aucun `android:label` sur les 6 `<activity>` secondaires (`AndroidManifest.xml:31-36`). |
| A9 boucle infinie de Glose | non corrigé | un toucher relance la réaction, pas de pause. |

---

## Par écran

### Premier lancement (`onboarding_1..4_*.png`)

Ce qui marche : Glose se présente à la première personne (« Enchanté. », « À quelle heure passerai-je ? »), aperçu vivant de l’écran de verrouillage avec l’heure choisie, permissions demandées seulement si les choix les exigent, « Passer » partout, Retour = étape précédente. C’est le meilleur flux de l’appli.

- **Étape 2, l’avertissement arrive avant l’envie.** Le mode par défaut (« Notification et écran de verrouillage ») affiche d’emblée un encadré jaune de 4 lignes : « votre ancien fond de verrouillage n’est pas rétabli ». C’est vrai et nécessaire, mais c’est la première chose en couleur après l’aperçu, sur l’écran qui doit séduire. [observé `onboarding_2_light.png`] Proposition : le garder, en `Help` sous l’aperçu, et réserver le jaune à l’étape 4 ; ou une ligne courte « Votre fond de verrouillage actuel sera remplacé. » avec la suite dans Réglages.
- **Étape 2, l’aide parle d’une durée jamais choisie** : « Le mot part quand même au bout de la durée choisie. » L’onboarding ne propose pas la durée. [observé + code `strings.xml:unlocks_hint`] Texte propre à l’onboarding : « Sinon, il part de lui-même au bout de 2 h. »
- **Étape 4, deux permissions empilées, la secondaire d’abord.** Encadré jaune avec bouton « Autoriser « Alarmes et rappels » » au-dessus du bouton principal « Autoriser les notifications » : deux CTA, l’œil va au jaune. [observé `onboarding_4_light.png`] Faire deux temps : notifications d’abord (principal), puis, au retour, l’encadré alarmes seul (le code sait déjà rester sur l’étape tant qu’il reste quelque chose, `OnboardingActivity.kt:go`).
- **« Trois petites questions »** : faux quand l’étape 4 n’est pas nécessaire (`last() = 2`, soit 2 questions) et l’étape 4 n’est pas une question. [code] « Deux ou trois réglages, et je vous laisse lire. »
- **Mélange de familles** : titre serif 28 sp, progression en capitales Roboto espacées, boutons radio et menus Roboto, boutons serif. [observé] Voir n° 6.

### Accueil (`home_*.png`, `app*.png`)

Première impression (2 s) : Glose, puis la carte, puis le gros bouton or. Juste. La carte est superbe dans toutes les palettes, le mode « Deviner avant de voir » (voile pointillé « Touchez pour révéler ») est une trouvaille, la feuille d’automne près de Glose est charmante.

- **Densité** : 9 blocs pour un mot (barre du haut à 3 disques + compteur, Glose + accessoire de saison, carte + étoile, 4 actions rondes, Exemple, Anecdote, bouton Quiz, rubrique Aujourd’hui, puces) et **7 libellés en capitales espacées** quand tout est affiché (MOT DU JOUR, EXEMPLE, ANECDOTE, AUJOURD’HUI…). [observé `home_w360.png`] Le calme de la carte se dissout dessous. Proposition : rentrer l’exemple dans la carte (comme sur l’écran de verrouillage, le partage et l’ancien accueil), ce qui retire un libellé et une boîte ; libellés en petites capitales serif (n° 6).
- **Compteur « 8/39 » en haut à gauche** : premier texte lu de l’écran, c’est un score. Pour une appli « cosy », il met la collecte avant la lecture. [observé, hypothèse sur l’effet] Le garder dans la Collection ; dans la barre, la pelote seule, ou une pelote qui « se remplit ».
- **Barre du haut : trois icônes, aucun libellé.** La pelote (Collection) ressemble à un symbole « interdit » rayé à 24 dp ; le tiroir de Mes mots se lit comme une imprimante ou une caisse ; les curseurs sont standard. [observé `home_light.png`, zoom] Voir IA, n° 4.
- **Puce coupée sur une lettre** (n° 2) : « ultracrépidarianism / e » à police normale en w360. [observé `home_w360.png`] Cause [code `MainActivity.kt:250-256`] : le test de place ne compte pas la marge `gap` du nouveau chip (`used + chip.measuredWidth > width`), la dernière puce de la ligne est écrasée. Correctif : `used + chip.measuredWidth + gap > width` et `isSingleLine = true` sur la puce.
- **Puce unique** (rappel #11) : avec 1 mot par jour, masquer rubrique et puces quand la liste a un seul élément égal à la carte.
- **« Quiz du soir (fait ✓) » reste le bouton plein or** après le quiz : l’action principale de l’écran devient une action qui « ne compte pas ». [observé `home_dark.png`] Une fois fait, style `Row` (contour) ; le plein revient le lendemain.
- **Citation du jour : l’icône « Source » est le livre du TLFi.** [observé `home_quote.png`] Même picto, deux destinations ; prendre un guillemet ou une plume.
- **Bandeau notifications** (rappel #12) : encore plat et carré, seul élément à angle droit avec les encadrés `warn` (n° 3).
- **Grande police** : les libellés des actions grossissent mais pas les pictos (24 dp) ni les disques ; à ×2,0 le picto paraît minuscule sous « Académie ». [observé `home_font200.png`] Acceptable (WCAG ne l’exige pas) ; disques à 56 dp au-delà de ×1,5 si l’on veut l’équilibre.

### Quiz (`quiz_*.png`, `quiz_bravo.png`, `notif_quiz.png`)

Très bonne progression : consigne, phrases à trous, « Un mot déjà croisé, pour la mémoire », pelotes de progression, réactions de la petite tête de Glose, couleurs douces, retour texte « Bien vu ! » / « Pas tout à fait : c’était « estramaçon ». ».

- **Fin de quiz raté avec gain** : « 2 / 4 », « Demain, on retente ? », puis « Monsieur Glose a gagné : » deux timbres. Message double (échec puis récompense) sans transition. [observé `quiz_fin.png`] Dans ce cas : « Demain, on retente ? En attendant, Monsieur Glose a gagné : ».
- **Rien à retenir après une erreur** (rappel #10) : on ne voit pas la définition du mot choisi par erreur, la fin ne liste pas les ratés. C’est le cœur pédagogique ; les révisions espacées rattrapent le mot plus tard mais l’utilisateur ne le sait pas.
- **Pelotes de progression** : elles marquent « où j’en suis », pas « juste / faux ». [observé `quiz_revision.png`] Belle idée sous-exploitée : pelote pleine si juste, pelote dévidée (fil lâche) si faux, et la ligne de pelotes reprise à l’écran de fin comme résumé.
- **Carte de notation** dans le moment de récompense : « Plus tard » en Roboto gras or (lien) à côté de « Noter » en serif à contour ; deux familles dans un composant, et « Plus tard » à 4,22:1 (A1). [observé `quiz_fin_noter.png`, mesuré]
- **Titre « QUIZ DU SOIR » à gauche, tout le reste centré** en fin de quiz (rappel #18). Deux boutons de fin de même poids (« Voir la collection », « Fermer ») : « Fermer » en `Primary` ou en lien.
- **Notification du quiz** : toujours Glose endormi dans sa tasse (`notif_quiz.png`) pour un appel à jouer. [hypothèse, rappel] Pose « coucou » ou « le fil écrit quiz ».

### Collection (`collection_*.png`)

L’aperçu fixe règle l’essai à l’aveugle. Les noms sont beaux.

- **Trois traitements du verrouillé** : silhouette pleine (Poses, Mots du fil), couleur estompée (Pelages, Pelotes, Montures, Reliures), boîte pointillée « ? » (Sur la tête, Au cou saisonnier, Objets). [observé `collection_light_part0..3`] L’utilisateur doit réapprendre la règle à chaque rubrique. Choisir une règle : estompé pour ce qui se voit en entier, « ? » seulement pour les objets saisonniers (« Revient en décembre »).
- **Le « ? » pointillé est le même motif que le timbre gagné** en fin de quiz (bord pointillé or). Le pointillé veut dire « gagné » dans le quiz, « inconnu » ici, « caché » sur la carte. [observé + code `stamp.xml`, `CollectionActivity.kt:87`, `MainActivity.kt:153`] Voir n° 3.
- **« Choisir »** (rappel #15) : désactivé, invisible (≈1,05:1), libellé vague. « Garder cette tenue », masqué tant que rien n’a changé.
- **Montures** : « Monture écaille » estompée sur une tête où les lunettes sont déjà dorées ; la différence ne se voit pas à 72 dp. [observé] Zoom sur la tête pour les accessoires de visage.
- **Noms en Roboto**, alors que le timbre de fin de quiz affiche les mêmes noms en serif. [observé]

### Mes mots (`mots_*.png`)

Bonne idée, liste claire (mot gras serif, définition sur 2 lignes, étoile 48 dp), recherche qui ignore accents et ligatures.

- **Tuiles de statistiques** : six tuiles « chiffre + libellé » façon tableau de bord, avant la liste. À ×2,0 en w360 elles passent en une colonne et repoussent la liste de deux écrans. [observé `mots_w360_font200_part0`] « 5 mots fragiles » est un terme maison non expliqué et non cliquable. [observé] Proposition : une seule ligne de résumé en serif (« 13 mots découverts, 3 favoris, 8 soirs d’affilée ») ; « mots fragiles » devient un filtre (« À revoir (5) ») à côté de « Favoris ».
- **Toucher un mot ouvre une seconde copie de l’accueil** (libellé « DÉJÀ VU »), avec barre du haut, bouton Quiz et puces du jour. [code `MotsActivity.kt:144` : `startActivity(MainActivity)` sans drapeau ; `MainActivity` est `singleTop` mais Mes mots est au-dessus, donc nouvelle instance] On peut boucler Accueil > Mes mots > Accueil > Mes mots… et « Au hasard » depuis cette fiche remplace l’écran de verrouillage. Voir IA.
- **Titre** en `Label` 12 sp, champ de recherche souligné système, interrupteur « Favoris seulement » : trois composants plateforme. [observé]
- **Ordre** : du plus récent au plus ancien (`MotsActivity.kt:list`) mais rien ne le dit ; pas de date. [code] Une ligne « Du plus récent au plus ancien » ou des intertitres par mois (« Novembre ») donneraient un air de carnet.

### Réglages (`settings_*.png`)

Groupes clairs, aides sous chaque réglage, encadré alarmes cohérent.

- **« Couleurs et polices ▾ »** est un écran entier présenté comme un menu déroulant (style `Setting`, flèche vers le bas). [observé `settings_light_part1` + code `activity_settings.xml:189`] Chevron « › » à droite, ou ligne de navigation distincte.
- **« Ajouter le widget »** et **« Afficher le mot maintenant »** : deux gros boutons à contour au milieu du groupe « Mot du jour », entre des interrupteurs. [observé] Ce sont des actions, pas des réglages ; un groupe final « Ailleurs sur votre téléphone » (widget, test) ou un bouton widget sur l’accueil (IA).
- **« Sons » rangé dans « Apparence »**. [observé] Renommer le groupe « Apparence et sons » ou créer « Ambiance ».
- **Libellés de valeur** : « Heure d’affichage : 08:00 » (libellé dans la valeur) mais « Nombre de mots par jour » au-dessus de « 1 mot » (libellé séparé). Deux modèles côte à côte. [observé] Un seul : libellé au-dessus, valeur dessous.
- **Titre « Réglages » 28 sp serif** alors que Mes mots, Collection et Quiz ont un `Label` 12 sp. [observé] n° 7.

### Couleurs et polices (`styles_*.png`)

Écran le plus « atelier » de l’appli, utile, avec un aperçu honnête (« Il porte la palette Braise et la police EB Garamond. »).

- **« sa définition »** répété dans les 18 tuiles : se lit comme un texte de remplissage. [observé] Mettre le nom de la palette seul, plus grand, ou un mot exemple qui porte vraiment la palette (« zinzolin »).
- **Sélection = case à cocher + bordure or épaisse** : avec 12 palettes gardées sur 18, l’écran devient un mur de cadres or ; la sélection de la Collection (2 dp) et des puces (1,5 dp) est différente. [observé + code] n° 3.
- **« Aucune »** est proposé alors qu’il faut garder au moins une palette (`pool_min` : « Il en faut au moins une : … reste. »). [code `strings.xml`] Promettre ce qu’on refuse : retirer « Aucune » ou le renommer « Seulement celle-ci ».
- **À ×2,0 en w360**, tuiles en une colonne : 32 tuiles, environ 14 écrans de défilement. [observé `styles_w360_font200.png`, 15 323 px] Garder 2 colonnes pour les palettes (le nom tient) et réduire l’aperçu des polices à « Grimoire » seul.
- **« Il porte… »** : « il » renvoie au mot, mais l’appli parle beaucoup de Glose ; ambigu. « Ce mot porte… ».

### Écran de verrouillage, partage, widget, icône (`lockscreen_*`, `quote_*`, `style_*`, `share_*`, `widget/*`, `icon_*`)

La partie la plus forte de l’identité : grandes marges, mot dans sa police, dégradés doux, signature italique. Le partage reprend la même mise en page avec la tête de Glose et « Definiday » en italique, discret et élégant. Icône (tête de Glose sur marine) et icône monochrome lisibles.

- **Uncial** sur « lendit » (rappel #7, partiel) : sur l’image partagée, l’erreur de lecture sort de l’appli.
- **Widget 2×2, mot long coupé n’importe où** : « suivez-moi-jeun / e-homme ». [observé `widget/2x2_long_word.png`] Couper aux traits d’union (le mot en a) : réduire la taille jusqu’à ce que chaque morceau tienne, comme `fit()` ; ou, dans `RemoteViews`, insérer des U+00AD aux coupures syllabiques précalculées.
- **Widget citation** : « Citation du jour » en italique minuscule alors que l’appli écrit « CITATION DU JOUR » en capitales ; texte plus petit que les définitions des autres widgets et coupé au milieu d’une phrase (« Elle c… »). [observé `widget/4x2_quote.png`] Citations courtes seulement dans le widget (filtre de longueur), sinon le mot du jour.
- **Tête de Glose présente ou absente selon la place** (absente sur 3 des 4 rendus 2×2). [observé] Acceptable ; une règle fixe (toujours, en bas à droite, 20 dp) donnerait une signature.

---

## Architecture de l’information et navigation

Carte actuelle :

```
Lanceur ─► (1er lancement) Onboarding 3 ou 4 étapes ─► Accueil
Accueil
  barre : [pelote 8/39] Collection   ·   [tiroir] Mes mots   ·   [curseurs] Réglages
  carte (étoile) · Académie · TLFi · Partager · Au hasard · Exemple · Anecdote
  [Quiz du soir] ─► Quiz ─► fin ─► [Voir la collection] ─► Collection
  Aujourd’hui : puces
Mes mots ─► toucher un mot ─► NOUVELLE copie de l’Accueil (« Déjà vu ») ─► sa barre ─► Mes mots ─► …
Réglages ─► Mot du jour (… [Afficher le mot maintenant] [Ajouter le widget]) · Écran de verrouillage · Quiz · Apparence ([Couleurs et polices ▾] ─► écran) · Sons · À propos
Widget ─► Accueil sur le mot
```

Constats :
1. **Mes mots est mal adressé** (Élevé). Icône de tiroir sans libellé, collée à Réglages : l’endroit dit « outil », le contenu est le carnet personnel, ce que l’utilisateur accumule. Puis la fiche d’un mot est l’accueil complet, empilé. Proposition : (a) icône « carnet » ou « fiche bristol » avec libellé court sous les trois icônes de la barre (« Collection », « Mes mots », « Réglages ») ou, plus calme, un lien « Mes mots (13) » sous les puces « Aujourd’hui », là où l’on pense à ses mots ; (b) fiche : `MainActivity` en mode lecture quand `EXTRA_DEJA_VU` est vrai (barre, Quiz et puces masqués, `Au hasard` masqué), ou lancement avec `FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP` pour réutiliser l’accueil existant ; Retour revient alors à Mes mots si on garde la pile, à décider.
2. **Le widget n’a pas de porte d’entrée** (Moyen). Seul accès : Réglages > Mot du jour > bouton. C’est pourtant la meilleure vitrine gratuite de l’appli. Proposition : une carte douce, une seule fois, sur l’accueil au 3e jour (« Monsieur Glose peut aussi s’asseoir sur votre écran d’accueil. [Ajouter le widget] [Non merci] »), et une dernière étape facultative de l’onboarding quand le mode est « Notification seulement ».
3. **Deux « collections »** (Moyen, textes). « Collection de Monsieur Glose » (tenues) et « Mes mots » (mots) ; la description TalkBack du compteur dit « 8 objets sur 39 » alors que « Objets » est aussi une rubrique (Marque-page, Petit sapin). Nommer : « Garde-robe de Monsieur Glose » ou « Trousseau » pour les tenues, « Mes mots » ou « Carnet » pour les mots ; compteur « 8 trouvailles sur 39 ».
4. **Couleurs et polices** déguisé en menu (Moyen) : voir Réglages.
5. **Retour** : aucun écran secondaire n’affiche de flèche de retour ni de titre cliquable ; le geste suffit sur Android (rappel M11, acceptable), mais sur Mes mots et Couleurs et polices, deux écrans longs, un titre « ‹ Mes mots » aiderait les utilisateurs de navigation à 3 boutons masquée. Faible.
6. **Découvrabilité de « Deviner avant de voir »** : excellente fonction, cachée dans Réglages ; un toucher long sur la carte, ou une mention unique à la 5e visite. Faible.

---

## Accessibilité (WCAG 2.1 AA)

Synthèse : 0 critique, 3 majeurs, 7 mineurs. Base solide : cibles ≥ 48 dp (barre, actions, étoiles `favori.xml` 48 dp, puces), focus 3 dp, carte lue en un arrêt (`screenReaderFocusable`), retour du quiz en `liveRegion`, compteur « Question 1 sur 4 », animations coupées respectées (quiz, timbres, révélation).

| # | Constat | WCAG | Gravité | Écran | Preuve | Correctif |
|---|---|---|---|---|---|---|
| A1 | « Plus tard » de la carte de notation : style `Link` (accent `#886A34`, Roboto gras ~15 sp) sur `surface` `#F1EADB` : **4,22:1**. | 1.4.3 | Majeur | Fin de quiz | [mesuré] ; `QuizActivity.kt:rate()`, `card_soft.xml` | Lien sur `bg` (sortir la rangée de la carte) ou couleur `ink2` (6,24:1) ; serif comme « Noter ». |
| A2 | Étoiles de Mes mots : toutes nommées « Favori ». TalkBack lit « Favori, case à cocher, non cochée » 13 fois, sans le mot. | 4.1.2, 2.4.6 | Majeur | Mes mots | [code] `MotsActivity.kt:139` | Dans `getView` : `contentDescription = getString(R.string.favori_de, w.mot)` (« Favori : adret »). Idem étoile de la carte (« Favori : estramaçon »). |
| A3 | Puce de l’accueil coupée sur une lettre (« ultracrépidarianism / e ») à police normale en w360 ; le mot à apprendre est mal écrit. | 1.4.10, 1.3.1 | Majeur | Accueil | [observé] `home_w360.png` ; [code] `MainActivity.kt:254` | Compter `gap` dans le test de ligne ; `isSingleLine = true`. |
| A4 | Mot long coupé n’importe où dans le widget 2×2. | 1.4.10 | Mineur | Widget | [observé] `widget/2x2_long_word.png` | Voir écran Widget. |
| A5 | « Choisir » désactivé quasi invisible (≈1,05:1 contre le fond) et sans raison donnée (rappel #15). | 1.4.11 (info), 3.3.2 | Mineur | Collection | [observé] | Masquer tant que rien n’a changé. |
| A6 | Activities sans titre propre : TalkBack et les Récents disent « Definiday » sur Mes mots, Couleurs et polices, Quiz, Collection, Réglages (rappel A6, non corrigé). | 2.4.2 | Mineur | Tous les écrans secondaires | [code] `AndroidManifest.xml:31-36` | `android:label` sur chaque `<activity>`. |
| A7 | Glose bouge en boucle sans pause possible dans l’appli (rappel A9). | 2.2.2 | Mineur | Accueil, Collection, fin de quiz | [code] | Toucher = pause / lecture, action nommée pour TalkBack. |
| A8 | Encadrés `warn` (bandeau, onboarding, réglages) : le bandeau de l’accueil reste un `TextView` cliquable sans rôle de bouton (rappel A13). | 4.1.2 | Mineur | Accueil | [code] `activity_main.xml:16-27` | `Button` style maison sur fond `warn`, ou délégué d’accessibilité `className = Button`. |
| A9 | Tuiles de stats de Mes mots : sous ×2,0, 6 tuiles pleine largeur repoussent la liste de 2 écrans ; aucune information perdue, mais la tâche (retrouver un mot) recule. | 1.4.10 (confort) | Mineur | Mes mots | [observé] `mots_w360_font200_part0` | Résumé en une phrase (voir Mes mots). |
| A10 | Langue : `textLocale = FRENCH` posé seulement sur le mot de la carte (`fit()`) ; définitions, Mes mots, quiz, widget, notifications restent sans langue (rappel A15, partiel). | 3.1.2 | Mineur | Tous | [code] `MainActivity.kt:236` | `LocaleSpan(Locale.FRENCH)` sur les textes de contenu, ou `textLocale` dans le style `Body`. |

Contrastes nouveaux mesurés :

| Élément | Avant-plan | Fond | Ratio | Exigé | Résultat |
|---|---|---|---|---|---|
| « Plus tard » (carte notation), clair | `#886A34` | `#F1EADB` | 4,22 | 4,5 | **échec** (A1) |
| Libellé de stat, aide sur `surface` | `#5C5446` | `#F1EADB` | 6,24 | 4,5 | ok |
| Texte d’aide dans l’encadré `warn` | `#5C5446` | `#F6E3B4` | 5,89 | 4,5 | ok |
| Lien or dans un encadré `warn` (si un jour) | `#886A34` | `#F6E3B4` | 3,98 | 4,5 | à éviter |
| Bouton `surface` dans l’encadré `warn` (onboarding 4) | `#F1EADB` | `#F6E3B4` | 1,06 | 3 (bord) | ok grâce au contour accent 1 dp |
| Disques des actions et de la barre | `#F1EADB` | `#FAF7F0` | 1,12 | n/a | le picto `ink` porte l’identification ; ok |
| Pelote vide du quiz | `#5C5446` | `#FAF7F0` | 6,98 | 3 | ok |
| Liseré des cartes claires | `#C9BC9F` | `#FAF7F0` | 1,76 | n/a | décor ; la carte est identifiée par son contenu |

Taille de texte et mouvement : rien ne se coupe ni ne sort de l’écran à ×2,0 sur les écrans relus, en dehors de A3 et A4. Les pictos ne grossissent pas avec la police (acceptable). Les réactions du quiz (pop, secousse ±6 dp, 250 ms) sont courtes et coupées si les animations le sont.

---

## Textes (ux-copy)

### Voix et ton
- **Vouvoiement tenu partout**, aucun « tu ». « Demain, on retente ? » (le « on » complice) fonctionne.
- **La voix de Glose change de personne** : 1re personne dans l’onboarding (« je vous apporte », « passerai-je », « je vous demande »), 3e ailleurs (« Monsieur Glose a gagné », « Monsieur Glose vous attend ce soir »), et l’appli parle d’elle-même dans la carte de notation (« Definiday vous plaît ? »). [code `strings.xml`] Règle proposée : **Glose dit « je » quand il s’adresse à vous** (onboarding, état vide, retour du quiz, carte de notation : « Je vous plais ? Un mot sur le Play Store m’aiderait beaucoup. ») ; **on parle de lui à la 3e personne quand on décrit l’interface** (titres, Collection, TalkBack). Les réglages restent neutres.
- **Nom personnalisable à venir** : 10 chaînes contiennent « Monsieur Glose » en dur (`glose`, `glose_bravo`, `glose_encore`, `unlock_new`, `collection_title`, `collection_desc`, `quiz_empty`, `sounds_hint`, `onb_welcome`, plus le commentaire). Préparer : `%1$s` partout ; prévoir l’élision (« Collection de Glose » / « Collection d’Anatole ») avec deux variantes de chaîne ou une tournure sans « de » (« La garde-robe : Anatole ») ; éviter les accords (« Enchanté. » suppose un masculin : « Enchanté » peut devenir « Ravi de vous rencontrer » si le nom est féminin, ou rester tel quel si Glose reste « Monsieur »).

### Tableau des textes

| Prio | Clé ou fichier | Texte actuel | Problème | Proposition |
|---|---|---|---|---|
| 1 | `onb_welcome` | Trois petites questions, et je vous laisse lire. | Faux avec 3 étapes (2 questions) ; l’étape 4 est une permission. | Deux ou trois réglages, et je vous laisse lire. |
| 1 | `unlocks_hint` (onboarding) | … au bout de la durée choisie. | La durée n’est pas proposée dans l’onboarding. | Chaîne dédiée : Sinon, il part de lui-même au bout de 2 h. |
| 1 | `quiz_retry` + `unlock_new` | Demain, on retente ? / Monsieur Glose a gagné : | Échec puis récompense sans lien. | Si échec et gain : Demain, on retente ? En attendant, Monsieur Glose a gagné : |
| 2 | `collection_desc` | Collection de Monsieur Glose, %1$d objets sur %2$d | « objets » = aussi une rubrique. | Collection de Monsieur Glose : %1$d trouvailles sur %2$d |
| 2 | `stat_fragile` | mots fragiles | Terme maison, non expliqué, non actionnable. | à revoir (et filtre « À revoir ») |
| 2 | `tile_sample` | sa définition | Lu comme un texte de remplissage. | (supprimer) ou un mot réel dans la palette |
| 2 | `none_f` | Aucune | Action refusée ensuite (« Il en faut au moins une »). | Retirer, ou « Seulement la palette du mot » |
| 2 | `preview_wears` | Il porte la palette %1$s… | « Il » ambigu (le mot ou Glose ?). | Ce mot porte la palette %1$s et la police %2$s. |
| 2 | `rate_text` | Definiday vous plaît ? Un mot sur le Play Store aide beaucoup. | 3e voix (l’appli) au milieu de Glose. | Je vous plais ? Un mot sur le Play Store m’aiderait beaucoup. |
| 2 | `collection_choose` | Choisir | (rappel) ne dit pas quoi. | Garder cette tenue |
| 2 | `widget_add_hint` | … Touchez-le pour ouvrir sa fiche. | « fiche » n’existe nulle part ailleurs (carte, mot). | … Touchez-le pour retrouver le mot dans l’appli. |
| 3 | `test_done`, `slow_default` | « Mot affiché : », « effleurés : » | Espace ordinaire U+0020 avant « : » ; les 150 autres chaînes ont U+202F. | `\u202F` |
| 3 | `quiz_score` | %1$d / %2$d | Espaces sécables : « 2 / » et « 4 » peuvent se séparer en très grande police. | `%1$d\u202F/\u202F%2$d` |
| 3 | `MotsActivity.kt:103` | un tiret seul (U+2013) quand le taux manque | Tiret seul, lu « tiret » ou rien. | « bientôt » ou masquer la tuile |
| 3 | `watch_text` | … au déverrouillage n° %1$d. | (rappel) « n° » administratif. | au 1er / 2e / 3e déverrouillage |
| 3 | `quote_chip` | Citation | (rappel) deux puces identiques possibles. | Citation de %1$s |
| 3 | `MainActivity.kt:warnIfHurried` | OK (système) | (rappel) seul « OK » sec. | D’accord |
| 3 | `credits` | … Les boutons ouvrent les articles… | Juste à nouveau (actions rondes) ; préciser lesquels. | … Les boutons Académie et TLFi ouvrent… |

Typographie : apostrophe courbe ’ partout dans `strings.xml` et désormais dans les citations ; U+202F correct partout sauf 2 chaînes ; guillemets « » avec espaces fines. Heures en « 08:00 » (accepté en Android FR ; « 8 h » serait plus littéraire, facultatif).

---

## Tableau classé

| # | Gravité | Écran | Preuve (capture / fichier) | Constat | Correctif |
|---|---|---|---|---|---|
| 1 | Élevé | Mes mots > fiche | `MotsActivity.kt:144`, `AndroidManifest.xml:24` | Toucher un mot ouvre une nouvelle copie complète de l’accueil (barre, Quiz, puces, Au hasard) ; boucle Accueil > Mes mots > Accueil possible. | Mode lecture de `MainActivity` si `EXTRA_DEJA_VU` (masquer `top_bar`, `quiz_open`, puces, `new_word`), ou `FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_SINGLE_TOP`. |
| 2 | Élevé | Accueil | `home_w360.png` ; `MainActivity.kt:254` | Puce coupée sur une lettre (« ultracrépidarianism / e ») à police normale, 360 dp. | `used + chip.measuredWidth + gap > width` ; `isSingleLine = true`. |
| 3 | Élevé | Toute l’appli | `card_soft.xml` (20), `btn.xml` (16), `MainActivity.kt:153` (14), `CollectionActivity.kt:87,114` / `MotsActivity.kt:111` / `StylesActivity.kt:110` (12), `stamp.xml` (4), `activity_main.xml:22` (0) | Sept rayons, trois sens du pointillé, trois sélections différentes : le kit s’est fragmenté avec les ajouts. | Jetons : rayon 20 (cartes), 14 (boutons, tuiles), pilule (puces) ; pointillé réservé à « caché / à révéler » ; sélection = contour accent 2 dp partout ; encadré `warn` à 14 dp. Fichier `values/dimens.xml` + 3 drawables partagés. |
| 4 | Moyen | Accueil (barre) | `home_light.png` | Mes mots = icône de tiroir sans libellé, à côté de Réglages ; pelote lue comme « interdit » ; compteur « 8/39 » en premier texte. | Libellés courts sous les icônes, picto carnet pour Mes mots ; ou lien « Mes mots (13) » sous Aujourd’hui ; pelote sans compteur. |
| 5 | Moyen | Réglages, accueil | `settings_light_part0` ; `activity_settings.xml:90-98` | Widget accessible seulement par un bouton dans Réglages > Mot du jour. | Carte unique sur l’accueil au 3e jour ; bouton déplacé dans un groupe « Ailleurs sur votre téléphone ». |
| 6 | Moyen | Toute l’appli | `themes.xml` (`Label`, `Help`, `Field`, `Toggle`, `Action`, `Setting` sans serif ; `Btn`, `Body` serif) | Roboto et serif mêlés dans un même composant (tuile de stat, carte de notation, cases de Collection vs timbres). | Interface en serif (Noto Serif) partout ; Roboto seulement pour les chiffres d’heure si besoin ; libellés en petites capitales serif (`fontFeatureSettings="smcp"` ou `textAllCaps` + `letterSpacing 0.05`). |
| 7 | Moyen | Mes mots, Collection, Quiz, Réglages, Styles | `mots_light.png`, `collection_light.png`, `quiz_question.png`, `settings_light.png`, `styles_light.png` | Deux systèmes de titre : `Title` 28 sp (Réglages, Couleurs et polices, onboarding) contre `Label` 12 sp (Mes mots, Collection, Quiz). | `Title` sur tous les écrans de destination ; `Label` réservé aux rubriques internes. |
| 8 | Moyen | Onboarding 4 | `onboarding_4_light.png` | Deux permissions et deux CTA à l’écran ; la secondaire (alarmes, encadré jaune) passe avant. | Notifications d’abord, encadré alarmes au retour du dialogue. |
| 9 | Moyen | Onboarding 2 | `onboarding_2_light.png` | Avertissement jaune de 4 lignes sur l’écran qui doit séduire ; aide qui renvoie à une durée jamais choisie. | `Help` sous l’aperçu, texte court ; chaîne d’aide propre à l’onboarding. |
| 10 | Moyen | Fin de quiz | `quiz_fin.png` ; `QuizActivity.kt:finishQuiz` | Échec et gain sans transition ; pas de mots à revoir. | Texte de liaison ; liste « À revoir » des ratés (mot : définition). |
| 11 | Moyen | Mes mots | `mots_light.png`, `mots_w360_font200_part0` | Six tuiles de tableau de bord avant la liste ; « mots fragiles » non actionnable ; à ×2,0 la liste recule de 2 écrans. | Une phrase de résumé ; filtre « À revoir (n) » à côté de « Favoris ». |
| 12 | Moyen | Mes mots | `MotsActivity.kt:139` | Étoiles toutes nommées « Favori » pour TalkBack. | « Favori : %1$s ». |
| 13 | Moyen | Fin de quiz | `quiz_fin_noter.png` | « Plus tard » à 4,22:1, Roboto à côté d’un « Noter » serif. | Couleur `ink2` ou sur `bg`, serif. |
| 14 | Moyen | Réglages | `settings_light_part1` ; `activity_settings.xml:189` | « Couleurs et polices ▾ » ouvre un écran mais ressemble à un menu. | Chevron « › », style de ligne de navigation. |
| 15 | Moyen | Collection | `collection_light_part0..3` | Trois traitements du verrouillé ; « ? » pointillé = même motif que le timbre gagné. | Estompé partout ; « ? » plein (sans pointillé) pour le saisonnier seulement. |
| 16 | Moyen | Verrouillage, partage | `lockscreen_1.png`, `share_1.png` | Uncial déforme « lendit » (rappel #7, partiel). | Retirer Uncial du pool des mots, ou le réserver aux mots sans n ni t. |
| 17 | Moyen | Textes | `strings.xml` (10 chaînes) | Nom « Monsieur Glose » figé ; voix 1re / 3e personne / appli mélangées. | `%1$s` + règle de voix (section Textes). |
| 18 | Faible | Accueil | `app.png` | Puce unique identique à la carte (rappel #11). | Masquer rubrique + puces si une seule puce = carte. |
| 19 | Faible | Accueil | `home_dark.png` | « Quiz du soir (fait ✓) » reste l’action pleine or. | Style contour une fois fait. |
| 20 | Faible | Accueil | `home_quote.png` | « Source » de la citation = picto du TLFi. | Picto plume ou guillemet. |
| 21 | Faible | Accueil | `home_notifications_off.png` | Bandeau plat carré, Roboto, sans affordance (rappel #12). | Rayon 14 dp, serif, « Activer › ». |
| 22 | Faible | Collection | `collection_light_part0` | « Choisir » invisible et vague (rappel #15). | « Garder cette tenue », masqué sans changement. |
| 23 | Faible | Couleurs et polices | `styles_light_part0` | « sa définition » ×18 ; « Aucune » refusé ensuite ; mur de cadres or ; 14 écrans à ×2,0. | Voir l’écran ; sélection 2 dp ; 2 colonnes conservées pour les palettes. |
| 24 | Faible | Widget | `widget/2x2_long_word.png`, `widget/4x2_quote.png` | Mot coupé n’importe où ; citation tronquée en pleine phrase ; libellé en italique minuscule. | Coupe aux traits d’union ; citations courtes seulement ; « CITATION DU JOUR » en `Label`. |
| 25 | Faible | Quiz | `quiz_juste.png`, `quiz_revision.png` | Pelotes de progression muettes sur juste / faux. | Pelote pleine / dévidée ; résumé en fin. |
| 26 | Faible | Réglages | `settings_light_part1` | « Sons » dans « Apparence » ; deux modèles de libellé de valeur. | « Apparence et sons » ; libellé au-dessus partout. |
| 27 | Faible | Onboarding 1 | `strings.xml:onb_welcome` | « Trois petites questions » faux dans le cas à 3 étapes. | « Deux ou trois réglages… ». |
| 28 | Faible | Manifest | `AndroidManifest.xml:31-36` | Écrans sans titre pour TalkBack (rappel A6). | `android:label` par Activity. |
| 29 | Faible | Verrouillage | `style_zinzolin.png` | Boucle du script hors marge (rappel #19). | Décaler de `-bounds.left`. |
| 30 | Faible | Notification | `notif_quiz.png` | Glose endort l’appel au quiz (rappel). | Pose « coucou ». |

---

## Ce qui rend l’appli générique

À l’usage des équipes de style. Ce qui a une identité forte et doit servir de référence : **Monsieur Glose** (trait brun, lunettes dorées, livre marine), **la carte du mot** (palette par mot, police par mot, dégradé), **l’écran de verrouillage et l’image partagée**, **les pelotes du quiz**, **le voile « Touchez pour révéler »**, **les accessoires de saison**, **l’idée du timbre** en fin de quiz. Tout le reste vient d’un kit d’interface standard :

1. **Disques gris-crème + pictos au trait** (barre du haut, 4 actions) : banque à colonnes pour l’Académie, livre ouvert, trois points reliés du partage Android, dé, curseurs de réglages, tiroir. Ce sont les pictos de n’importe quelle appli. Piste : pictos dessinés dans le trait de Glose (contour brun `#3B2A1F` épais, bouts arrondis, petite touche or), objets de bibliothèque : plume pour l’Académie, loupe sur un in-folio pour le TLFi, enveloppe cachetée pour partager, pelote qui roule pour le hasard, clé ancienne pour les réglages, fichier à tiroir de bibliothèque (avec étiquette) pour Mes mots.
2. **Libellés en capitales Roboto très espacées** (MOT DU JOUR, EXEMPLE, ANECDOTE, AUJOURD’HUI, QUIZ DU SOIR, MES MOTS…) : le « surtitre » de tous les gabarits web. Piste : petites capitales de la serif, espacement léger, éventuellement précédées d’un fleuron (❧) ou d’un filet court ; ou italique serif façon rubrique de dictionnaire.
3. **Tuiles de statistiques chiffre + libellé** (Mes mots) : tableau de bord SaaS. Piste : une phrase d’ex-libris (« 13 mots découverts depuis le 2 octobre ») ou un marque-page dont le ruban porte les chiffres.
4. **Bouton plein pleine largeur, rayon 16 dp** (Quiz du soir, Commencer, Suivant) : CTA standard. Piste : bouton « étiquette de reliure » (rectangle à coins coupés ou cartouche comme l’étiquette dorée du livre de Glose), ou bouton en relief léger façon tampon encreur.
5. **Pilules de puces** (Aujourd’hui) : composant « chip » Material. Piste : petites fiches bristol inclinées d’un degré, ou onglets de répertoire alphabétique.
6. **Interrupteurs, menus déroulants, boutons radio, champ souligné** (Réglages, onboarding, Mes mots) : widgets plateforme teintés. Piste minimale : garder les widgets (accessibilité) mais leur donner les couleurs maison partout (déjà fait pour les interrupteurs) et remplacer le soulignement du champ de recherche par une carte `surface` arrondie avec une loupe.
7. **Encadrés jaunes plats à angles droits** (bandeau, onboarding, alarmes) : alerte générique, et seul élément carré de l’appli. Piste : un « billet » de Glose (papier légèrement plus chaud, coin replié, petite tête de Glose à gauche) pour les avertissements, qui deviennent alors sa voix.
8. **Cases à cocher + bordure épaisse** (Couleurs et polices) : sélecteur de formulaire. Piste : un signet (ruban) qui dépasse du haut de la tuile gardée.
9. **Boîtes pointillées « ? »** (Collection) : la « mystery box » des jeux mobiles. Piste : silhouette sous papier de soie (voile crème) ou enveloppe cachetée « Revient en décembre ».
10. **Compteur « 8/39 »** en badge : gamification standard. Piste : pelote qui grossit avec la collection, sans chiffre à l’accueil.
11. **Écrans sans en-tête dessiné** : chaque écran commence par un texte nu. Piste : un en-tête commun (titre serif + petite tête de Glose dans la pose de l’écran : lunettes sur le nez pour Mes mots, tasse pour les Réglages du soir), qui règlerait aussi n° 7.

Règle générale à donner aux équipes : **tout ce qui n’est pas la carte doit avoir l’air d’un objet de bibliothèque** (papier, reliure, tampon, fiche, ruban, fil de pelote), dessiné dans le trait de Glose ; et **une seule famille de caractères pour l’interface**, la serif, la police d’affichage restant réservée au mot.

---

## Limites

- Captures Robolectric : pas de vrai TalkBack, pas de rendu constructeur, pas de volet de notifications réel, Glose figé (animations non vues).
- Pile d’activités (finding 1) déduite du code et des règles `singleTop`, non testée sur appareil.
- Contrastes calculés sur les couleurs des ressources ; police exacte du lien « Plus tard » supposée Roboto (style `Link` sans `fontFamily`).
- Les pistes « générique » sont des directions de style, pas des maquettes ; leurs contrastes sont à vérifier avant intégration.
- Aucun code de l’appli modifié.
