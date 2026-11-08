# Audit FON + SON, itération 1 (lecture seule) — HEAD `c843268`

Méthode : lecture de bout en bout de MainActivity, QuizActivity, DailyReceiver (+ Wall, Alarms), UnlockService, Words, Prefs, Widget, MotsActivity, DetailActivity, SettingsActivity, CollectionActivity, Unlocks, Glose, Sounds ; audits du 6 nov. (`docs/audits/bugs.md`, `fonctionnalites.md`), `BugHuntTest.kt`, rapports `reports/*.md`, `docs/son.md`, `docs/voix.md`, Graphene (décisions, nœuds `sounds`, `mascot`).
Preuves d'exécution :
- `--tests BugHuntTest --tests SoundsTest` sur HEAD : **22/22 et 3/3 verts** (log `night/fon_gradle1.log`).
- Test jetable `FonScratchTest` (5 cas, **retiré** du worktree après exécution, rien de commité) : **4 échecs = 4 bugs nouveaux confirmés** (log `night/fon_gradle2.log`, sorties `SCRATCH …`).
- Sons mesurés avec ffmpeg/numpy (scripts `night/son_measure.py`, `night/son_phone.py`).
Aucun fichier de l'appli modifié, aucun commit.

## 1. État des items des audits du 6 nov. (442fcaf) sur HEAD

### `docs/audits/bugs.md` (B1-B9) et `BugHuntTest`

| # | Item | État | Preuve |
|---|---|---|---|
| B1 | Série plafonnée à 4 (jours citation) | **corrigé** | `QuizActivity.kt:375-386` (`Quiz.unbroken`, jours `Words.quotesOnly` sautés, écart ≤ 7) ; tests `quoteDaysDoNotCapTheStreak`, `moreQuotesStillReachesStreakUnlocks` verts |
| B2 | Quiz fini après minuit compté le lendemain | **corrigé** | jour figé `QuizActivity.kt:30-31,45` ; rappel porte son jour (`DailyReceiver.kt:79`) ; tests `quizFinishedAfterMidnight…`, `reminderTappedAfterMidnightOpensTheEvesQuiz` |
| B3 | « Après la durée » pendant le suivi retire le mot | **corrigé** | `UnlockService.kt:66` (`unlocks == 0` → fin du suivi), `SettingsActivity.kt:81` (`UnlockService.sync`) ; tests `unlocksSetToZero…`, `unlockSettingChangeSyncsTheWatch` |
| B4 | Pas d'alarme exacte (Android 12+) | **corrigé** | `AndroidManifest.xml:13` `SCHEDULE_EXACT_ALARM` ; `Alarms` (`DailyReceiver.kt:219-225`) ; encart Réglages (`SettingsActivity.kt:44,139`) et onboarding (`OnboardingActivity.kt:92,130`) ; reprogrammation sur `ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED` ; test `exactAlarmAccessIsRequestedThenUsed` |
| B5 | Notif d'un mot au hasard ouvre le mot du jour | **corrigé** | `MainActivity.kt:48,94` (`find` sur tout le catalogue) + `onNewIntent` (singleTop) ; tests `randomPickNotificationOpensThatWord`, `notificationTappedWhileOpenShowsItsItem` |
| B6 | Rotation : quiz recommencé, carte perdue | **corrigé** | `QuizActivity.kt:43-61,73-78` (graine, mots, index, score, réponse) ; `MainActivity.kt:86-91` ; tests `quizSurvivesRotation`, `answeredQuestionStaysAnsweredOnRotation`, `mainCardSurvivesRotation`. **Reste l'écran de score** : voir N2 |
| B7 | Deux mots, même définition | **corrigé** (code + données) | `QuizActivity.kt:326` exclut la définition ; words.json : 0 définition en double (vérifié) |
| B8 | Rappel même quiz fait | **corrigé** | `DailyReceiver.kt:75` ; test `noQuizReminderOnceTodaysQuizIsDone` |
| B9 | Heure d'hiver, alarme tardive rejouée | **corrigé** | `DailyReceiver.kt:51-54` (ancré sur la date) ; test `fallBackLateDeliveryDoesNotFireTwice` |
| — | Prefs restaurées/falsifiées qui plantent | **corrigé** | `Prefs.kt:13-17` (types et bornes), `Glose.choice` ; test `tamperedPrefsFallBackToSafeDefaults` |
| — | Animations coupées : poses « le fil écrit » sans mot | **corrigé** | `Glose.kt:76-82` (pas de `reset()`) ; GloseTest |

### `docs/audits/fonctionnalites.md` (P1-P19, hors doublons ci-dessus)

| # | Item | État | Preuve |
|---|---|---|---|
| P1 | = B1 | corrigé | idem |
| P2 | = B4 | corrigé | idem |
| P3 | Changer le nombre de mots en cours de journée brûle des mots | **corrigé autrement** | seul ce qui est montré est marqué (`Words.kt:54-57,82-86`, `DailyReceiver.kt:118-119`) ; hausse après l'heure → affichage immédiat (`SettingsActivity.kt:45-50`) ; test `onlyReallyShownItemsAreSeen`. Effet de bord mineur : la hausse ré-affiche **tout** (re-notif du 1er mot, fond et compteur de déverrouillages remis à zéro) → N9 |
| P4 | Mode verrouillage seul + 2-3 mots | **corrigé** | seul le 1er (fond) est marqué ; les autres restent en enveloppes sur l'accueil (`MainActivity.kt:260-262`) |
| P5 | = B6 | corrigé (sauf écran de score, N2) | |
| P6 | = B5 | corrigé | |
| P7 | = B8 | corrigé | |
| P8 | Indice de série figé en Collection | **corrigé** | `Unlocks.kt:100` (`Quiz.unbroken`) ; Mes mots aussi (`MotsActivity.kt:113`) |
| P9 | = B3 | corrigé | |
| P10 | Distracteur = réponse d'une autre question du soir | **atténué** | `QuizActivity.kt:324-327` : les mots du soir ne sont plus préférés (pris après les mots déjà vus), mais pas exclus ; pour un nouvel utilisateur (aucun mot « connu ») la proba reste ≈ 2/(taille de la nature). Acceptable, non prioritaire |
| P11 | 68 définitions contiennent le mot vedette ; 2 paires identiques | **corrigé (données)** | analyse words.json : 0 et 0 |
| P12 | Citations : années d'édition, typo, « « » doublés | **presque corrigé** | 0 année > 1956, 0 apostrophe droite, 0 espace simple ; **reste 2 citations qui commencent par « »** (Aloysius Bertrand « Que tu meures… », Staël « [...] la gloire… ») → affichées « « … » » sur carte, notif, fond, widget, dialogue lenteur (`MainActivity.kt:181`, `Quotes.kt:25`) |
| P13 | Changement de jour appli ouverte | **ouvert, aggravé** → N1 | |
| P14 | Chaîne d'alarmes rompue si `show` lève | **ouvert** | `DailyReceiver.kt:26-27` : `show()`/`quizReminder()` avant `scheduleNext()`/`scheduleQuiz()` |
| P15 | Jour de citation seule : pas de quiz, message trompeur | **partiel** | la citation a maintenant son enveloppe (`MainActivity.kt:275`) ; mais le quiz dit « Aucun mot vu aujourd'hui pour l'instant. Lisez le mot du jour : je vous attends ce soir. » alors que le mot du jour **était** une citation (sortie SCRATCH E) |
| P16 | « Nouveau mot » agit (notif + fond) avec le mot du jour désactivé ; annule les 2-3 notifs du jour | **ouvert (intention à confirmer)** | `MainActivity.kt:59`, `DailyReceiver.kt:113` |
| P17 | Quiz avant l'heure du mot : aucun rappel ce jour-là | **ouvert** | `DailyReceiver.kt:75` (`seenToday` vide) ; aucun avertissement dans Réglages |
| P18 | Champ `academie` jamais lu | **ouvert (inoffensif)** | présent dans les 1 342 entrées de words.json |
| P19 | Textes de l'interface | **partiel** | espaces U+202F, `plurals`, « Quiz du soir » : corrigés. Restent : libellés des menus déroulants écrits en dur (`SettingsActivity.kt:45,75,80,83,200`), `credits` (strings.xml:23) ne cite pas Wikiquote |

### `docs/audits/interface.md` (bloquants)

| # | Item | État |
|---|---|---|
| UI-B1 | Notif ouvre le mauvais mot | corrigé (= B5) |
| UI-B2 | Notifications refusées → rien en silence | **corrigé en mode Notif/Les deux** (bandeau `MainActivity.kt:119`) ; **ouvert** dans un cas : N3 |
| UI-B3 | Juste/faux par la couleur seule | corrigé (✓/✗ dans le texte + zone live, `QuizActivity.kt:147-162`) |

## 2. Bugs fonctionnels nouveaux (lecture de bout en bout)

Les quatre premiers sont **prouvés** par le test jetable (échec sur HEAD).

**N1 (P1) — Appli laissée ouverte (ou restaurée) après minuit : le mot de la veille est ré-étiqueté « Mot du jour » et re-marqué vu aujourd'hui.** PROUVÉ (`SCRATCH A card=estramaçon label=Mot du jour history=D+1 seenToday=[estramaçon]`).
- `MainActivity.onResume` (`:100`) rappelle `show(current)`, qui appelle `Words.shown` (`:149`) : l'élément de la veille prend la date du jour dans History. Conséquences : il retombe au quiz du soir, son délai de 270 j repart, il apparaît dans « Déjà vus aujourd'hui ». L'étiquette lit `item.key in dailyMots` (`:174`) avant que `showSeen()` (`:265`) n'appelle `Words.today()` : « Mot du jour » sur le mot d'hier ; le vrai mot du jour n'est qu'une enveloppe. Même chose après une mort de processus et une restauration le lendemain (`STATE_ITEM`, `:48`), et pour le widget touché entre minuit et l'heure d'affichage (N7).
- Correctif minimal : mémoriser le jour d'affichage ; dans `onResume`, si `LocalDate.now()` a changé et que la carte montrait un élément du jour, `current = Words.today(this).first()` ; et ne pas ré-appeler `Words.shown` à chaque `onResume` (seulement à l'affichage d'un nouvel élément).

**N2 (P1) — Rotation sur l'écran de score : l'annonce des nouveautés est perdue pour toujours.** PROUVÉ (`SCRATCH B after rotation: [Quiz du soir, 1 / 1, Joli travail…, Fermer]`).
- `finishQuiz()` est rejoué à la recréation (`QuizActivity.kt:107`). `Unlocks.update(p)` (`:226`) ne renvoie plus rien car `p.announced` a déjà été écrit (`:227`) : plus de timbres, plus de « … a gagné », Glose « bravo » à la place, et le son passe de `STAMP` à `CHIME` (rejoué, cf. note Graphene `sounds`). La carte de notation disparaît aussi (`rateAsked` déjà vrai, `:243`).
- Correctif : sauver dans le Bundle les ids de `fresh`, `rate` montré, et un drapeau « fin déjà jouée » (pas de `Sounds.play`, pas de `Quiz.record`, déjà gardé).

**N3 (P2) — Mode « écran de verrouillage seul », fond personnel (Samsung…) et notifications refusées : rien ne s'affiche, aucun bandeau.** PROUVÉ (`expected:<0> but was:<8>`).
- Le garde-fou `Wall` bascule le mot en notification (`DailyReceiver.kt:116-119`, `lockBlocked`), mais le bandeau ne s'affiche que si `mode != MODE_LOCK` (`MainActivity.kt:119`).
- Correctif 1 ligne : `(p.mode != Prefs.MODE_LOCK || p.lockBlocked)`.

**N4 (P2) — Ouvrir Réglages remplace une durée hors liste par 15 min.** PROUVÉ (`expected:<90> but was:<15>`).
- `spinner(R.id.duration, …, durations.indexOf(p.durationMinutes))` (`SettingsActivity.kt:79`) : -1 → `setSelection(0)` → le rappel initial écrit 15. Une durée valide (1..1440) venue d'un profil importé ou d'une restauration est écrasée sans rien toucher.
- Correctif : insérer la valeur courante dans la liste si absente (ou ignorer le premier rappel).

**N5 (P2) — Libellé d'Activity de la Collection = chaîne de format brute.** Lecture : `AndroidManifest.xml:43` `android:label="@string/collection_title"` = « Collection %1$s » ; `CollectionActivity` ne fixe jamais `title` → TalkBack (titre de fenêtre) et l'écran des applis récentes lisent « Collection %1$s ». Correctif : `title = getString(R.string.collection_title, de(p.gloseName))` dans `onCreate` (et/ou label neutre « Collection » dans le manifeste). Lié au renommage.

**N6 (P3) — Chaîne d'alarmes fragile (ex-P14).** Reprogrammer avant d'afficher : `ACTION_SHOW -> { scheduleNext(ctx); if (enabled) show(ctx) else Widget.update(ctx) }`, idem pour le quiz.

**N7 (P3) — Widget en retard entre minuit et l'heure d'affichage.** `Widget.update` n'est appelé qu'au SHOW / `show()` / Collection (`Widget.kt:243-249`) : de 0 h à 8 h il montre l'élément et le cachet de la veille ; le toucher ouvre ce mot (→ N1). Correctif léger : `Widget.update(this)` dans `MainActivity.onResume` quand le jour a changé, et/ou une alarme inexacte à minuit (coût faible, pas de nouvelle permission).

**N8 (P3) — Changer l'heure du mot à une heure plus tardive le jour même ré-affiche les mêmes mots.** `TimePickerDialog` → `scheduleNext` (`SettingsActivity.kt:42`) : à 8 h 30, passer de 8 h à 9 h ⇒ second SHOW à 9 h (notifs re-postées, fond ré-appliqué, durée et compteur de déverrouillages remis à zéro). Correctif : mémoriser le jour du dernier SHOW automatique (`wallpaper_state`, local, hors profil) et sauter un second SHOW automatique le même jour.

**N9 (P3) — Hausse du nombre de mots après l'heure : tout est ré-affiché.** `DailyReceiver.show(this)` (`SettingsActivity.kt:49`) re-poste le 1er mot et remet fond, durée et compteur à zéro. Correctif : `show(this, Words.today(this).drop(ancien))` pour les seuls nouveaux (le fond garde le 1er).

**N10 (P3) — Service de déverrouillage relancé (START_STICKY) sans mot affiché : sa notification reste jusqu'au prochain déverrouillage.** `UnlockService.onStartCommand` (`UnlockService.kt:31-38`) ne vérifie pas `wallpaperEndAt`. Correctif : après `startForeground`, `if (p.wallpaperEndAt == 0L || p.unlocks == 0) stopSelf()`.

**N11 (P3) — États perdus à la rotation hors quiz/accueil.** Collection : l'essayage non validé (`trial`) revient au choix enregistré ; Mes mots : recherche et filtre « Favoris » perdus (vues sans id, pas de `onSaveInstanceState`). Mineur.

**N12 (P3) — Écran vide du quiz un jour de citation seule (ex-P15).** Texte trompeur (« Lisez le mot du jour ») alors qu'il était une citation. Proposer `quiz_empty_quote` (« Aujourd'hui, je vous ai apporté une citation : pas de quiz ce soir. ») choisi si `Words.quotesOnly(day, wordsPerDay, moreQuotes)`.

Cas limites vérifiés sans nouveau bug : heure d'été (printemps/automne, B9 + `springForwardGapIsSafe`), minuit pendant le quiz (B2), fuseau/TIME_SET (receiver), redémarrage pendant l'affichage, alarme exacte refusée (repli inexact + encart Réglages/onboarding, reprogrammation à l'octroi), notifications refusées en modes Notif/Les deux (bandeau), mort de processus du quiz (Bundle complet), double enregistrement du quiz (garde `lastQuizDay`), prefs falsifiées.

## 3. Décidé mais non implémenté

### 3.1 Renommer Glose (décision `mascot-custom-name`)

Déjà en place : `Prefs.gloseName` (getter : trim, 1..24, pas de caractère de contrôle, défaut `glose_name`) ; `de()` (élision) ; toutes les chaînes prennent `%1$s` ; `VoixTest` (seule `glose_name` écrit « Glose ») ; clé `gloseName` déjà dans `Profile.SETTINGS` (`Profile.kt:35`) ; noms d'objets sans accord ni « Glose ».

Manque :
1. **Aucun champ** : rien dans Réglages (`activity_settings.xml`, `SettingsActivity.kt`) pour saisir le nom. Proposition : feuille « Apparence », un `EditText` « Nom du chat » (une ligne, `IME_ACTION_DONE`, `maxLength` 24, `inputType` `textPersonName|textCapWords`), aide « Il signe vos cartes et vos quiz », bouton lien « Rétablir “Monsieur Glose” ». Enregistrement à la validation (Done / perte de focus), pas à chaque frappe.
2. **Validation** : le setter ne valide pas (seul le getter retombe sur le défaut, en silence). Il faut refuser à la saisie, avec message, ce que le getter rejette, et durcir les deux : rejeter aussi les caractères de format Unicode (`Character.FORMAT`, ex. U+202E qui retourne le texte), U+2028/U+2029, et les surrogates isolés ; compter la longueur en points de code (`codePointCount`) plutôt qu'en unités UTF-16 (un émoji compte 2 aujourd'hui).
3. **« Glose » encore en dur dans le code** : `WordCard.kt:89,127` signe la carte de l'écran de verrouillage **et** l'image partagée « Glose » (commentaire `ponytail:` qui l'annonce). Un nom choisi doit y figurer (question : nom complet ou court, cf. Q1). `VoixTest` ne voit pas le Kotlin : ajouter un contrôle (aucun littéral `"Glose"` hors identifiants dans `app/src/main/java`).
4. **Format brut** : `AndroidManifest.xml:43` (N5) ; `activity_main.xml:48` et `activity_onboarding.xml:41` ont `@string/glose` brut en `contentDescription` (écrasé dans le code à `onResume`/`onCreate` : OK, mais fragile ; le retirer du XML).
5. **Rafraîchissement** : l'accueil relit le nom à `onResume` (OK) ; la carte d'écran de verrouillage déjà posée garde l'ancienne signature jusqu'au lendemain (acceptable, à dire dans l'aide ou ré-afficher si `wallpaperEndAt != 0`).
6. Tests : saisie valide → titre d'accueil « Une lettre d'Anatole », Collection, quiz ; saisie refusée (vide, 25 car., U+202E) → message et nom inchangé ; signature de `WordCard` lue depuis les prefs ; export/import du profil garde le nom (déjà couvert par `Profile.SETTINGS`).
Fichiers réservés au maître (`SettingsActivity.kt`, `strings.xml`, `Prefs.kt`, `AndroidManifest.xml`) : à attribuer explicitement à ce lot.

### 3.2 Texte de fin de quiz

Fait depuis la revue 011 : « En attendant, X a gagné : » (`unlock_new_anyway`), liste « À revoir », « Joli travail… », « Quiz déjà fait… ».
Reste à resserrer (proposition, à arbitrer par UX/propriétaire) :
- Jusqu'à **5 messages empilés** sous le score (À revoir, Demain on retente / Joli travail, Quiz déjà fait…, carte de notation, a gagné + timbres) : règle proposée, une seule phrase de Glose sous le score (priorité : déjà fait > raté > réussi), « À revoir » en liste, puis les gains.
- `quiz_passed` dit « ce soir » même pour le quiz de la veille fini après minuit (`quiz_eve`) : tournure sans moment (« Ces mots sont un peu plus à vous. »).
- `quiz_not_counted` : « ne compte pas pour la collection » alors que la mémoire (révisions) compte : « … ne change pas la collection, mais vos révisions sont notées. »
- Écran vide un jour de citation (N12).

### 3.3 Feuille de route « fun et mignonne », lots 1-2 (approuvés)

Lot 1 : A6 (arrêt de la boucle à 26 s, `Glose.IDLE_MS`), A5 (`@animator/press`), A3, B1, B2, B3 (timbres de progression), B6, bord des cartes claires, C1-C3 : **faits**. **Reste** : la décision `fun-cute-decisions` (1) demande « un moyen de mettre en pause » la vie au repos de 20-30 s : un toucher la **relance** (réaction), rien ne la met en pause avant l'arrêt automatique (WCAG 2.2.2 : un mouvement de plus de 5 s doit pouvoir être arrêté). À traiter avec UX (action TalkBack « Mettre en pause »).
Lot 2 : jetons `gloseSpecs`/`gloseStripe`, `Kind` divisé, compteurs c/m/k, fenêtres, éléments de base, déballage B4, hiver, flocon A8 : **faits** (39 éléments). **Reste** : `cadre_dore` (q ≥ 7) sauté faute de type CADRE (note Graphene `mascot`) ; à décider (Q5).
Échéance : les fenêtres d'hiver ouvrent le 1er décembre (`12-01`), donc la version doit sortir avant ≈ 25 novembre.

## 4. Son

### 4.1 Déclencheurs ↔ événements

| Événement | Son | Code | Constat |
|---|---|---|---|
| Bonne réponse | `son_tic` | `QuizActivity.kt:202` | OK, doublé par ✓ + texte live + haptique CONFIRM |
| Mauvaise réponse | `son_hmm` | `QuizActivity.kt:202` | OK, doublé par ✗ + texte + haptique REJECT |
| Quiz réussi sans gain | `son_carillon` | `QuizActivity.kt:228` | OK ; **rejoué à la rotation** (N2) |
| Nouveautés | `son_tampon` | `QuizActivity.kt:228` | joué aussi sur un quiz **raté** avec gain (« En attendant… ») : cohérent avec le texte, mais `docs/son.md` dit « quiz raté : silence » sans nuance → préciser la doc ; à la rotation devient `CHIME` (N2) |
| Nouveau mot au hasard | `son_page` | `MainActivity.kt:58` | OK |
| Toucher Glose / 4 touchers | `son_mrr` / `son_ronron` | `Glose.kt:142` (accueil et aperçu de la Collection) | OK, limité par `Taps` (700 ms / 3 s) ; `docs/son.md` dit encore « (à brancher) » : doc périmée |
| Réglage « Sons » activé | `son_page` (échantillon) | `SettingsActivity.kt:101` | **muet sans explication** si le téléphone est en vibreur/silencieux ou si le volume système est à 0 : l'utilisateur croit que ça ne marche pas → aide contextuelle |

Pas de son manquant notable : ouverture, navigation et notifications restent muettes (conforme à la doc). Défaut : désactivé (`Prefs.kt:84-86`, `false`), test `silentByDefault`.

### 4.2 Mesures (ffmpeg/numpy, mono 44,1 kHz Vorbis)

| Fichier | Taille | Durée | Crête | max 50 ms plein | **max 50 ms après passe-haut 300 Hz** | écart | centroïde | énergie < 300 Hz |
|---|---|---|---|---|---|---|---|---|
| son_tic | 4,4 Ko | 0,16 s | −12,4 | −20,9 | −21,1 | −0,2 dB | 1 795 Hz | 0 % |
| son_page | 7,2 Ko | 0,45 s | −9,8 | −20,1 | −20,2 | −0,1 dB | 2 982 Hz | 0 % |
| son_carillon | 7,4 Ko | 0,95 s | −12,8 | −20,3 | −20,4 | −0,2 dB | 1 600 Hz | 0 % |
| son_mrr | 5,9 Ko | 0,32 s | −11,6 | −19,8 | −25,1 | −5,3 dB | 691 Hz | 66 % |
| son_hmm | 4,7 Ko | 0,38 s | −15,3 | −20,0 | −26,9 | −6,9 dB | 416 Hz | 76 % |
| son_ronron | 15,6 Ko | 1,70 s | −12,2 | −20,1 | **−36,1** | **−16,1 dB** | 828 Hz | 96 % |
| son_tampon | 5,0 Ko | 0,20 s | −11,5 | −20,9 | **−39,0** | **−18,1 dB** | 1 803 Hz | 99 % |

Lecture : l'égalisation de `make_sounds.py` (`TARGET = -20` sur la bande pleine, `:13,121-123`) tient à ±1 dB **en bande pleine**, comme le dit la doc. Mais un haut-parleur de téléphone coupe sous ≈ 300 Hz (la doc le sait) : à travers ce filtre, **le tampon (déblocage, le son le plus « récompense ») est ≈ 18 dB plus faible que le tic, le ronron ≈ 16 dB**, le hmm et le mrr ≈ 5-7 dB. Sur haut-parleur, le tampon sera presque inaudible juste après un carillon/tic bien présents. C'est un **défaut de volume** (backlog), pas une question de goût ; le correctif garde les timbres : niveler sur le signal filtré à 300 Hz (et renforcer la « claque » 300-2 500 Hz du tampon, les harmoniques du ronron), puis mesurer de nouveau. Fichiers régénérés = à faire écouter au propriétaire avant fusion (règle 7 du plan).
Durées : toutes < 2 s, attaque ≤ 21 ms, fondus OK ; tailles 49 Ko au total, conformes.

### 4.3 Mode silencieux, flux, cycle de vie

- **Silencieux / vibreur** : respecté (`Sounds.kt:28`, `ringerMode == NORMAL`) ; test `silentUnlessRingerNormal`. Ne Pas Déranger : géré par la plateforme pour `USAGE_ASSISTANCE_SONIFICATION` (catégorie « système ») — à vérifier sur appareil, pas de code à ajouter.
- **Flux** : `USAGE_ASSISTANCE_SONIFICATION` → volume « système/sonnerie », pas « multimédia » (choix documenté). Le réglage Android « Sons des touches » n'est pas consulté (Q4).
- **Cycle de vie** : `SoundPool` paresseux, `applicationContext` pour `load`, libéré 30 s après le dernier son par un `Handler` principal : **pas de fuite d'Activity**, rien chargé pour rien. Points faibles :
  - **Latence au premier son** : chaque `play()` à froid charge le fichier puis joue dans `OnLoadCompleteListener` (décodage Vorbis asynchrone, typiquement quelques dizaines de ms). Comme on lit souvent une définition plus de 30 s, **presque chaque réponse du quiz part d'un pool libéré** : le tic arrive en retard sur le ✓ et le pool est recréé à chaque question. Correctif : précharger RIGHT/WRONG/CHIME/STAMP à l'ouverture du quiz (sans jouer) et garder le pool tant que le quiz est ouvert.
  - Un chargement en échec garde son id dans `ids` : ce son reste muet jusqu'à la libération (mineur, `Sounds.kt:35`).
  - Deux `play()` du même son avant la fin du chargement : le second est perdu (mineur).
- **Accessibilité** : aucun son n'est seul porteur d'information (✓/✗ + texte live + haptique au quiz ; texte et timbres aux gains ; changement de carte ; réaction visuelle de Glose, qui n'est que décorative). Conforme.

## Backlog proposé

- [P1] axe(fon) Carte de l'accueil après minuit : remettre le mot du jour, ne plus re-marquer la veille (N1) — `MainActivity.kt` (+ `Widget.update` si le jour change, N7) — test : activité créée à J (fuseau GMT+12), reprise à J+1 (GMT-12) → étiquette et `History[mot de la veille]` inchangés, carte = `Words.today().first()`, widget mis à jour — ~40 lignes
- [P1] axe(fon) Écran de score qui survit à la rotation : timbres, « a gagné », carte de notation, sans rejouer le son (N2) — `QuizActivity.kt` — test : premier quiz (déblocages), `recreate()` → texte `unlock_new` et noms toujours affichés, `Sounds` non rejoué (compteur de lecture ou `pool` inchangé) — ~40 lignes
- [P1] axe(fon) Renommer Glose dans Réglages › Apparence : champ, validation à la saisie (vide, > 24 points de code, contrôle, format Unicode U+202E/U+2028), « Rétablir », signature de `WordCard` lue depuis les prefs, label de Collection (N5), XML sans `@string/glose` brut, contrôle anti-littéral « Glose » dans le Kotlin — `activity_settings.xml`, `SettingsActivity.kt`, `strings.xml`, `Prefs.kt`, `WordCard.kt`, `CollectionActivity.kt`, `AndroidManifest.xml`, `VoixTest.kt` (fichiers réservés : attribution explicite) — critère : « Anatole » saisi → accueil « Une lettre d'Anatole », Collection, quiz, signature de la carte partagée ; saisies invalides refusées avec message ; profil exporté/importé garde le nom — ~150 lignes
- [P2] axe(son) Niveler les sons sur ce qu'entend un haut-parleur de téléphone : mesure après passe-haut 300 Hz dans `make_sounds.py`, tampon et ronron relevés (≈ +16-18 dB perçus), hmm/mrr (+5-7 dB) — `tools/sounds/make_sounds.py`, `res/raw/son_tampon.ogg`, `son_ronron.ogg`, `son_hmm.ogg`, `son_mrr.ogg`, `docs/son.md`, `SoundsTest.kt` — critère : max 50 ms après passe-haut 300 Hz à −20 ± 1,5 dB pour les 7, crête ≤ −3 dBFS, chaque fichier < 20 Ko ; nouveaux fichiers livrés en écoute au propriétaire avant fusion — ~60 lignes (+ binaires)
- [P2] axe(fon) Bandeau « notifications coupées » aussi quand le mot passe par notification faute de fond libre (N3) — `MainActivity.kt:119` — test `lockBlocked` + notifs coupées → bandeau visible — ~15 lignes
- [P2] axe(fon) Réglages : ne plus écraser une durée hors liste (N4) — `SettingsActivity.kt:79` — test : durée 90 restaurée, ouvrir Réglages → 90 conservé et affiché « 1 h 30 » — ~15 lignes
- [P2] axe(fon) Texte de fin de quiz resserré : une phrase de Glose sous le score, `quiz_passed` sans « ce soir », `quiz_not_counted` qui mentionne les révisions, `quiz_empty_quote` les jours de citation (N12) — `QuizActivity.kt`, `strings.xml` — test : jour de citation → nouveau texte ; quiz raté + gain + déjà fait → au plus une phrase avant « À revoir » — ~50 lignes
- [P3] axe(son) Préchauffer les sons du quiz à l'ouverture et garder le pool pendant le quiz ; oublier l'id d'un chargement raté — `Sounds.kt`, `QuizActivity.kt`, `SoundsTest.kt` — test : sons activés, `QuizActivity` créé → pool créé avant toute réponse ; `pool == null` si sons coupés — ~30 lignes
- [P3] axe(son) Aide sous l'interrupteur « Sons » quand le téléphone est en silencieux/vibreur (l'échantillon est muet) ; doc à jour (mrr/ronron branchés, tampon aussi sur quiz raté avec gain) — `SettingsActivity.kt`, `strings.xml`, `docs/son.md` — test : ringer VIBRATE + activer → aide visible — ~20 lignes
- [P3] axe(fon) Reprogrammer avant d'afficher (N6) — `DailyReceiver.kt:26-27` — test : `onReceive(SHOW)` avec un `show` qui lève (ex. `Wall.lockId` qui jette hors `runCatching`… ou words.json absent simulé) → alarme SHOW toujours programmée — ~10 lignes
- [P3] axe(fon) Pas de second affichage automatique le même jour (changement d'heure, N8) et hausse du nombre de mots qui n'affiche que les nouveaux (N9) — `DailyReceiver.kt`, `SettingsActivity.kt:49`, `Prefs.kt` (clé locale `wallpaper_state`, hors profil, justifiée) — test : SHOW à 8 h, heure → 9 h, SHOW 9 h → pas de re-notif ; hausse 1→2 → seule la notif id 2 postée — ~40 lignes
- [P3] axe(fon) Service de déverrouillage relancé sans mot : s'arrête (N10) — `UnlockService.kt` — test : `wallpaperEndAt = 0`, `startCommand` → `isStoppedBySelf` — ~8 lignes
- [P3] axe(fon) Rappel du quiz réglé avant l'heure du mot : avertissement sous l'heure du quiz (P17) — `SettingsActivity.kt`, `strings.xml` — test : quiz 7 h, mot 8 h → aide visible — ~15 lignes
- [P3] axe(fon) Données : retirer le « « » initial des 2 citations (Bertrand, Staël) + contrôle dans `datasetIsClean` ; citer Wikiquote dans `credits` (P12, P19) — `assets/citations.json` (et sa source `tools/citations`), `strings.xml`, `DefinidayTest.kt` — test : aucune citation ne commence par « « » — ~10 lignes
- [P3] axe(fon) Libellés des menus déroulants des Réglages en ressources (`string-array`) (P19) — `SettingsActivity.kt`, `strings.xml` — test existant UiTest vert, libellés identiques — ~30 lignes
- [P3] axe(fon) États conservés à la rotation : essayage de la Collection, recherche et filtre de Mes mots (N11) — `CollectionActivity.kt`, `MotsActivity.kt` — test : `recreate()` garde l'essai et la recherche — ~30 lignes
- [P3] axe(fon) Pause de la vie au repos de Glose (décision fun-cute n° 1, WCAG 2.2.2), à coordonner avec UX — `Glose.kt`, `MainActivity.kt` — test : action d'accessibilité « Mettre en pause » → AVD arrêtées, pas de relance avant un nouveau toucher — ~40 lignes

## Questions pour le propriétaire

1. **Nom de Glose** : longueur maximale 20 ou 24 caractères (le getter accepte 24) ? Émojis permis ? La signature manuscrite des cartes (écran de verrouillage, image partagée) doit-elle porter le **nom complet** (« Anatole », « Monsieur Glose ») ou rester courte (« Glose » aujourd'hui, alors que le défaut est « Monsieur Glose ») ?
2. **« Nouveau mot au hasard » avec le mot du jour désactivé** : doit-il quand même poster une notification et poser le fond d'écran ? Et doit-il effacer les 2-3 notifications du jour (aujourd'hui oui) ?
3. **Fin de quiz** : d'accord pour « une seule phrase de Glose sous le score » (priorité déjà fait > raté > réussi) ?
4. **Sons** : suivre aussi le réglage Android « Sons des touches » (s'il est coupé, Definiday se tait) ? Et le niveau relevé du tampon/ronron doit-il être écouté avant fusion (recommandé) ?
5. **`cadre_dore`** (lot 2, cadre doré de la Collection, q ≥ 7) : l'ajouter avec un type CADRE limité à la Collection, ou l'abandonner ?
6. **Jour de citation seule** : garder « pas de quiz ce soir » (et le dire), ou ajouter un mot au quiz ces jours-là (change le ratio 1/5) ?
