# Audit fonctionnel Definiday (commit 442fcaf)

Périmètre : README.md, `.graphene/nodes|facts`, `design/mascotte/mise_en_place.md`, `app/src/main/java/fr/definiday/*.kt`, manifeste, `res/`, `assets/*.json`, tests `app/src/test/`.
Méthode : lecture du code ; analyse Python des données ; 12 tests jetables Robolectric (`AuditTest.kt`) dans un worktree séparé, retiré à la fin ; 45 requêtes réelles sur dictionnaire-academie.fr. Aucun fichier du dépôt modifié.

Légende : **CONFIRMÉ** = lu dans le code ou reproduit par un test jetable (sortie `AUDIT …` citée) ; **PLAUSIBLE** = déduit, non reproduit (vrai comportement Android, appareil réel).

Bilan : **38 fonctionnalités vérifiées : 26 OK, 11 partielles, 1 incohérente, 0 manquante** (le seul vrai manque, la permission d'alarme exacte, est rangé dans les lignes 4 et 6).

## 1. Tableau fonctionnalité → état → preuve

| # | Fonctionnalité (README / décision) | État | Preuve |
|---|---|---|---|
| 1 | Notification publique, retirée après la durée (`setTimeoutAfter`) | OK | DailyReceiver.kt:145-164 ; test `showPostsPublicTimedNotification` |
| 2 | Fond d'écran de verrouillage, retiré à la durée seulement s'il est toujours le nôtre | OK | DailyReceiver.kt:105-130 ; tests `wallpaperIsRemovedOnlyWhenDueAndStillOurs`, `rebootPurgesExpiredWallpaper` |
| 3 | Modes : les deux / notification seule / écran de verrouillage seul | Partiel | DailyReceiver.kt:103-105 : en mode « écran de verrouillage seul », avec 2 ou 3 mots, les mots 2 et 3 ne sont affichés nulle part hors de l'appli, mais ils sont marqués vus et posés au quiz (P4) ; tests `lockOnlyMode…`, `notifOnlyMode…` |
| 4 | Fin au 1er/2e/3e déverrouillage (service de premier plan) | Partiel | AndroidManifest.xml (pas de `SCHEDULE_EXACT_ALARM`) + DailyReceiver.kt:89 + UnlockService.kt:216 : sur Android 12+, refus probable depuis l'alarme (P2) ; changement du réglage pendant le suivi (P9) ; test `lockOnlyModePostsNoNotificationAndLeavesAtSecondUnlock` (logique seule) |
| 5 | Position du texte (bas / milieu) | OK | WordCard.kt:42-64 ; test jetable `wordCardFit` : 1 793 mots et citations tiennent (haut du bloc ≥ 44 % en mode bas, bas ≤ 82 % en mode milieu). Option non testée dans le dépôt. |
| 6 | Affichage « à l'heure choisie » | Partiel | DailyReceiver.kt:88-90 : sur Android 12+, `canScheduleExactAlarms()` est toujours faux (permission non déclarée), donc l'alarme est inexacte (P2) ; test `nextTriggerIsTodayOrTomorrow` |
| 7 | 1 à 3 mots par jour | Partiel | MainActivity.kt:64-66, Words.kt:180-195 : un changement en cours de journée marque des mots jamais affichés (P3) ; test `dailyWordsAreStableAndMarkedSeen` (hausse seulement) |
| 8 | Citations : ratio 1/5, 2/5 avec « Plus de citations » | OK (ratio) | Words.kt:171 ; test `quoteSlotsFollowTheRatio`. Effet de bord sur les séries : ligne 29 |
| 9 | Mot du jour : pas avant 270 jours, jamais vus en premier, réserve exclue du hasard | OK | Words.kt:165-177 ; test `dailyWordsStayAwayNineMonths`. Test jetable `switchingWordsPerDayBreaksNineMonths` (1 mot + 3 hasards par jour pendant 400 j, puis 3 mots/j) : aucune violation, écart minimal 397 j |
| 10 | Hasard : pas avant 120 jours (garanti à 1 mot/j, au mieux à 3) | OK (comme documenté) | Words.kt:174-177 ; test `randomWordsStayAwayFourMonths` |
| 11 | Mot au hasard : remplace l'écran de verrouillage, compte dans l'historique | Partiel | MainActivity.kt:42-47 ; test `randomPickReplacesTheLockScreenWord`. Toucher la notification du mot au hasard ouvre le mot du jour (P6, CONFIRMÉ) |
| 12 | Avertissement au-delà de 3 mots au hasard (une fois par jour, citation « lenteur ») | OK | MainActivity.kt:119-129, Quotes.kt:240 ; non testé (35 citations « lenteur » ≥ 10 testées) |
| 13 | Liste des mots vus aujourd'hui | OK | MainActivity.kt:178-188 ; les citations n'y figurent pas (P15) |
| 14 | Quiz du soir : 4 propositions, même nature | Partiel | QuizActivity.kt:111-116 ; rotation = quiz recommencé (P5), distracteurs pris parmi les autres réponses du quiz (P10), définitions qui contiennent le mot (P11) ; test `quizOffersFourDistinctOptionsAndCountsStreaks` |
| 15 | Rappel du quiz à l'heure choisie | Partiel | DailyReceiver.kt:64-81 : rappel envoyé même quand le quiz est déjà fait (P7) ; aucun rappel les jours de citation seule (P1) ; test `quizNotificationShowsSleepingGlose` |
| 16 | Compteurs : quiz, réussis (≥ 2/3), sans faute, série, une fois par jour | OK | QuizActivity.kt:119-130 ; tests `quizPassesAtTwoThirds`, `quizOffersFourDistinct…` |
| 17 | Styles : 18 palettes, 14 polices, mot assorti si gardé, sinon choix stable dans la sélection | OK | Styles.kt:103-118 ; styles.json 18/14, 14 fichiers dans res/font ; test `everyWordStyleExistsAndPoolIsHonoured` |
| 18 | Citations : polices de texte seulement | OK | Styles.kt:113-114 ; test `quoteSlotsFollowTheRatio` |
| 19 | Anecdote « Le saviez-vous ? » + lien source | OK | MainActivity.kt:164-166 ; 801 anecdotes, toutes sourcées (fr.wiktionary / fr.wikipedia) ; test `datasetIsClean` |
| 20 | Lien Académie : recherche au clic, repli CNRTL | OK | MainActivity.kt:236-249 ; vérifié en réseau : 34 sur 40 mots marqués `academie:false` trouvés (dont `aboucher`, `abstème`), les autres (`agélaste`, `shibboleth`, …) renvoient `result:[]`, donc repli sur le CNRTL. Non testé dans le dépôt |
| 21 | Lien TLFi `cnrtl.fr/definition/<mot>` | OK | MainActivity.kt:251 |
| 22 | Lien Wikiquote pour une citation (le bouton Académie est masqué) | OK | MainActivity.kt:147-157 ; 450 sources `https://fr.wikiquote.org/wiki/` |
| 23 | Glose animé en tête, arrêté hors de l'écran, image fixe si animations coupées | OK | Glose.kt:241-247, MainActivity.kt:108-116 ; test `mainScreenAnimatesGloseOnlyWhileVisible` |
| 24 | Pose du soir après 21 h une fois débloquée | OK | Glose.kt:222-223 ; test `eveningPoseAfterNinePmOnceUnlocked`. Calculée seulement dans `onResume`, et seulement de 21 h à minuit |
| 25 | Fin de quiz : « bravo » (+ éclats si sans faute) / « encore » + « Demain, on retente ? » | OK | QuizActivity.kt:66-75 ; vu seulement dans des captures (`renderDemoImages`, « Not a check ») : aucune assertion |
| 26 | Notification du quiz : Glose endormi + tête endormie | OK | DailyReceiver.kt:70-74 ; test `quizNotificationShowsSleepingGlose`. Assis tant que « soir » n'est pas débloqué (le README ne le précise pas) |
| 27 | Pas de Glose sur l'écran de verrouillage | OK | WordCard.kt (aucun dessin de Glose) |
| 28 | Déblocages comptés en quiz (table du propriétaire) | OK | Unlocks.kt:141-163 ; test `unlockTable` |
| 29 | « Mêmes seuils avec 1 ou 3 mots par jour » | **Incohérent** | P1 : avec 1 mot/j, série max 4 ; avec « plus de citations », série max 2 (test jetable `streakVsQuoteDays`) |
| 30 | Rien ne se perd, chaque nouveauté annoncée une fois | OK | Unlocks.kt:174-177, QuizActivity.kt:82-92 ; test `unlocksAreKeptAndAnnouncedOnce` |
| 31 | Collection : 21 éléments, silhouette + ce qui manque | Partiel | CollectionActivity.kt:184-206 ; 21 éléments comptés ; l'indice de série reste figé après une série cassée (P8) ; test `collectionTriesThenKeepsAChoice` |
| 32 | Le choix (pose, pelage, pelote, accessoire) s'applique partout | OK | Glose.kt:207-238 ; test `collectionTriesThenKeepsAChoice` + captures `app_choice.png` |
| 33 | Réglages persistants | OK | Prefs.kt (SharedPreferences ; `wallpaper_state` exclu de la sauvegarde, xml/*.xml) |
| 34 | Reprogrammation des alarmes : démarrage, mise à jour, heure, fuseau | OK | DailyReceiver.kt:23-31 + manifeste ; tests `rebootPurgesExpiredWallpaper`, `rebootReschedulesPendingWallpaperEndAndUnlockWatch` |
| 35 | Désactivation puis réactivation (mot, quiz) | OK | MainActivity.kt:51-54, 84-87 ; `scheduleNext`/`scheduleQuiz` annulent ou reprogramment ; le fond et les notifications en cours restent jusqu'à la fin de la durée |
| 36 | Changement de jour pendant que l'appli est ouverte | Partiel | MainActivity.kt:34-41, 108-111 : carte, étiquette et puces de la veille jusqu'à ce que l'activité soit recréée (P13) |
| 37 | Données des mots | OK avec réserves | 1 343 mots, 801 anecdotes, typographie propre (apostrophes courbes, U+202F) ; 68 définitions contiennent le mot vedette ; 2 paires de définitions identiques ; champ `academie` jamais lu et peu fiable (P11, P18) |
| 38 | Données des citations | Partiel | 450 citations, 153 auteurs ; 153 « années » > 1956 (dates d'édition), 245 apostrophes droites, 125 espaces simples avant `; : ! ?`, 2 citations qui commencent par « (P12) |

## 2. Problèmes classés par gravité

### Élevée

**P1. Les jours de citation seule cassent la série : « soir », « dodo », écharpe, bonnet et Nuit d'encre sont hors d'atteinte avec 1 mot par jour.** CONFIRMÉ (test jetable `streakVsQuoteDays`).
- Avec 1 mot par jour, le seul créneau est une citation un jour sur 5 (`Words.isQuoteSlot`, Words.kt:171), et 2 jours sur 5 avec « Plus de citations ». Ces jours-là, `seenToday` est vide (les citations ne sont pas du matériel de quiz, Words.kt:204-210) : pas de rappel (DailyReceiver.kt:65), pas de quiz possible (« Aucun mot vu aujourd'hui pour l'instant. »), et `Quiz.record` remet la série à 1 le lendemain (QuizActivity.kt:125).
- Mesuré sur 365 jours, avec un quiz fait chaque soir où c'est possible et sans « mot au hasard » :
  - 1 mot/j : 292 quiz, **série max 4** → écharpe (7), bonnet (14) et Nuit d'encre (30) impossibles ;
  - 1 mot/j + plus de citations : 219 quiz, **série max 2** → même « soir » (3) est impossible, et avec lui « dodo », la pose de 21 h et Glose endormi dans la notification ;
  - 2 mots/j + plus de citations : série max 4 ;
  - 2 ou 3 mots/j sans ce réglage, ou 3 mots/j avec : série illimitée.
- Contredit la décision du propriétaire (« mêmes seuils avec 1 ou 3 mots par jour ») et le README. Le seul contournement est d'appuyer sur « Nouveau mot au hasard » les jours de citation, ce que rien n'indique.
- Correctif minimal : dans `Quiz.record`, ne pas casser la série quand tous les jours sautés sont des jours sans mot, calculé avec les réglages actuels :
  `val gapOk = (p.lastQuizDay + 1 until day).all { d -> (0 until p.wordsPerDay).all { Words.isQuoteSlot(d, it, p.moreQuotes) } }` puis `p.streak = if (p.lastQuizDay == day - 1 || gapOk) p.streak + 1 else 1`. Ajouter le test : 1 mot/j pendant 10 jours → série ≥ 7. Autre voie : un jour de citation seule, garder un mot en plus de la citation.

**P2. Android 12 et plus : l'alarme est toujours inexacte, donc la fin au Nᵉ déverrouillage ne démarre probablement pas depuis l'alarme quotidienne.** CONFIRMÉ en lecture, PLAUSIBLE à l'exécution (non reproductible sous Robolectric).
- Le manifeste ne déclare ni `SCHEDULE_EXACT_ALARM` ni `USE_EXACT_ALARM` (targetSdk 36). Sans cette déclaration, `canScheduleExactAlarms()` renvoie toujours faux sur Android 12+, donc `set()` prend toujours `setAndAllowWhileIdle` (DailyReceiver.kt:89-90).
- Conséquences :
  - le mot n'arrive pas « à l'heure choisie » : la note Graphene parle d'une livraison dans l'heure ;
  - le démarrage d'un service de premier plan depuis l'arrière-plan n'est exempté que pour une alarme exacte. `UnlockService.start` est alors refusé en silence (`runCatching`, UnlockService.kt:217), et « Au 1er/2e/3e déverrouillage » ne fonctionne plus que pour « Afficher maintenant », le mot au hasard (appli au premier plan) ou après un redémarrage.
- Le nœud Graphene `lockscreen-display` décrit le refus « quand l'alarme est inexacte » sans voir qu'elle l'est toujours sur 12+. La majorité des appareils actuels est concernée.
- Correctif minimal :
  - déclarer `<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM"/>` (accordée par défaut sur 12-13 ; refusée par défaut sur 14+ pour une nouvelle installation) ;
  - quand `unlocks > 0` et `!canScheduleExactAlarms()`, proposer d'ouvrir `Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM` ;
  - écouter `AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED` pour reprogrammer ;
  - `USE_EXACT_ALARM` est réservé par Play aux applis réveil ou agenda.
- À vérifier sur un appareil Android 14 : `adb shell dumpsys alarm` et logcat `ForegroundServiceStartNotAllowedException`.

### Moyenne

**P3. Changer le nombre de mots en cours de journée marque des mots jamais affichés.** CONFIRMÉ (test jetable `raisingCountMidDay`).
- Scénario : 1 mot affiché à 8 h, puis l'utilisateur passe à 3. `Words.today` tire 2 éléments de plus et les inscrit dans l'historique. Mesuré : notifications = 1, historique du jour = 3, `seenToday` = 2 (un des ajouts était une citation).
- `DailyReceiver.show` n'est pas rappelé (MainActivity.kt:65). Ces mots ne sont donc jamais notifiés ni mis sur l'écran de verrouillage, mais ils sont « brûlés » pour 9 mois, figurent dans « Déjà vus aujourd'hui » et tombent au quiz.
- À l'inverse, passer de 3 à 1 avant l'heure de l'alarme : `today()` renvoie 1 élément, mais `dailyMots` en garde 3 et le quiz porte toujours sur les 2 mots jamais montrés (mesuré : `seenToday` = 2 après la baisse).
- Correctif minimal :
  - hausse : dans le rappel du spinner, appeler `DailyReceiver.show(this)` si l'heure du jour est déjà passée (`nextTrigger(now, hour, minute)` tombe demain) ;
  - baisse : dans `seenToday`, prendre `dailyMots.take(wordsPerDay)`.

**P4. Mode « écran de verrouillage seul » avec 2-3 mots : les mots 2 et 3 ne s'affichent que dans l'appli, mais ils comptent comme vus et tombent au quiz.** CONFIRMÉ en lecture.
- DailyReceiver.kt:103-104 : aucune notification dans ce mode, et le fond n'affiche que `words.first()` (commentaire `ponytail:`).
- Le README signale « premier mot du jour en fond d'écran », mais le réglage « 3 mots » reste proposé sans avertissement.
- Correctif minimal : dans ce mode, ajouter au texte d'aide « seul le premier mot est affiché, les autres sont dans l'appli », ou ne marquer « vu » que ce qui a été affiché.

**P5. Une rotation pendant le quiz le fait recommencer.** CONFIRMÉ (test jetable `quizRestartsOnRecreate` : `before=2 / 3 after=1 / 3`).
- `questions`, `index` et `score` ne sont pas sauvegardés, et les propositions sont retirées au hasard. Sur l'écran de score, une rotation relance aussi le quiz. Les compteurs ne sont pas doublés (`record` une fois par jour), mais la réaction et l'annonce sont perdues.
- Correctif minimal : `android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"` sur `QuizActivity` dans le manifeste. Version complète : `onSaveInstanceState` (index, score, graine du `Random`).

**P6. Toucher la notification d'un mot au hasard ouvre le mot du jour.** CONFIRMÉ (test jetable : `random=atermoiement card=euphonie label=MOT DU JOUR`).
- MainActivity.kt:41 ne cherche `EXTRA_MOT` que dans `daily`.
- Correctif minimal : `daily.firstOrNull { … } ?: Words.load(this).firstOrNull { it.key == extra } ?: daily.first()`.

### Basse

**P7. Le rappel du quiz part même quand le quiz du jour est déjà fait.** CONFIRMÉ (test jetable : `reminder posted=true`). Correctif : `if (Prefs(ctx).lastQuizDay == LocalDate.now().toEpochDay()) return` au début de `quizReminder`.

**P8. Indice de série figé dans la Collection.** CONFIRMÉ (test jetable : « Encore 1 soir de quiz d'affilée » alors que la série est cassée depuis 10 jours ; il en faut 7).
- `Unlocks.hint` lit `p.streak`, qui ne repart à 1 qu'au quiz suivant.
- Correctif : valeur `s` = `if (p.lastQuizDay >= today - 1) p.streak else 0`.

**P9. Passer « Fin » de N déverrouillages à « Après la durée » pendant le suivi retire le mot au déverrouillage suivant.** CONFIRMÉ (test jetable : `removed at 1st unlock=true`).
- `countUnlock` compare à `unlocks = 0` (UnlockService.kt:225). À l'inverse, passer de 0 à N pendant l'affichage ne démarre pas le service.
- Correctif : `if (p.unlocks == 0) return false` dans `countUnlock`, et appeler `UnlockService.start`/`stopService` dans le rappel du spinner quand `wallpaperEndAt != 0`.

**P10. Un distracteur peut être la réponse d'une autre question du même quiz.** CONFIRMÉ (test jetable : 1 083 questions sur 3 000 avec 3 mots de même nature).
- `Quiz.build` n'exclut que le mot lui-même (QuizActivity.kt:112), alors que son commentaire laisse entendre le contraire.
- Correctif : `val others = all.filter { it !in seen }` (il reste largement assez de mots).

**P11. Données et quiz : 68 définitions contiennent le mot vedette, 2 paires de définitions identiques.** CONFIRMÉ (analyse de words.json).
- Mot vedette dans la définition : `bombance` (« faire bombance : festoyer »), `capilotade`, `céans`, `choir`, `apothicaire`, … Au quiz, la réponse est donnée.
- `aruspice`/`haruspice` (même définition, même nature `n. m.` : deux bonnes réponses possibles si l'un sert de distracteur à l'autre) ; `mercuriale`/`semonce` (`n. f.`).
- Correctif : dans `Quiz.build`, exclure les distracteurs dont la définition est celle de la réponse ; à l'affichage de la question, remplacer le mot vedette par « … » (`Regex("\\b" + Regex.escape(mot) + "\\b", IGNORE_CASE)`).

**P12. Citations : années d'édition affichées comme dates d'œuvre, typographie non normalisée, guillemets doublés.** CONFIRMÉ (analyse de citations.json).
- 153 citations ont `annee` > 1956 : par exemple « Voltaire, Micromégas (1991) », « Madame de La Fayette, La Princesse de Clèves (2019) », « Madame de Sévigné, Lettres (2023) ». `Quote.signature` (Quotes.kt:225) les affiche sur la carte, dans la notification et sur le fond d'écran.
- 245 citations ont une apostrophe droite et 125 une espace simple avant `; : ! ?`, alors que words.json passe par `typo()`.
- 2 citations commencent par « (Aloysius Bertrand « Que tu meures… », et « [...] la gloire… »), d'où « « … » » (MainActivity.kt:149, WordCard.kt:22, DailyReceiver.kt:142).
- Correctif : appliquer le `typo()` de `merge_words.py` aux citations ; vider `annee` quand c'est une date d'édition ; retirer le « initial.
- Non vérifié : la règle « auteurs morts avant 1956 » pour chacun des 153 auteurs (relu à la main : aucun cas évident hors règle).

**P13. Changement de jour pendant que l'appli est ouverte.** CONFIRMÉ en lecture.
- `onResume` (MainActivity.kt:108-111) ne rafraîchit que Glose. Laissée ouverte en arrière-plan pendant la nuit, l'appli montre le mot de la veille sous « MOT DU JOUR » et les puces de la veille, jusqu'à ce qu'elle soit recréée. La pose de 21 h n'est réévaluée qu'en `onResume`.
- Correctif : mémoriser le jour affiché dans `onCreate`, puis `if (jour != LocalDate.now().toEpochDay()) recreate()` dans `onResume`.

**P14. La chaîne d'alarmes peut se rompre.** PLAUSIBLE.
- `scheduleNext` et `scheduleQuiz` sont appelés après `show` et `quizReminder` (DailyReceiver.kt:20-21). Une exception (par exemple au dessin d'un VectorDrawable de Glose pour la notification) supprime les rappels suivants jusqu'à la prochaine ouverture de l'appli ou au prochain redémarrage.
- Correctif : reprogrammer d'abord, puis afficher.

**P15. Jour de citation seule dans l'appli.** CONFIRMÉ en lecture.
- La citation n'est pas dans « Déjà vus aujourd'hui » : après un mot au hasard, on ne peut plus y revenir.
- La rangée de puces reste vide, et le quiz répond « Aucun mot vu aujourd'hui pour l'instant » alors qu'une citation a été vue, sans suggérer le mot au hasard.
- Lié à P1.

**P16. Le bouton « mot au hasard » agit même quand l'affichage quotidien est désactivé.** CONFIRMÉ en lecture, intention à confirmer.
- Il pousse une notification et le fond d'écran malgré « Afficher un mot chaque jour » décoché (MainActivity.kt:45).
- Avec 3 mots par jour, il retire les 3 notifications du jour (DailyReceiver.kt:101), en mode « notification seule » aussi.

**P17. Heure du quiz avant l'heure du mot : aucun rappel ce jour-là.** CONFIRMÉ en lecture. Exemple : quiz à 7 h, mot à 8 h, appli pas ouverte → `seenToday` est vide à 7 h. Correctif : avertir dans le réglage, ou repousser le rappel après l'heure du mot.

**P18. Champ de données `academie` jamais lu et peu fiable.** CONFIRMÉ.
- Présent dans les 1 343 entrées (`true`/`false`), jamais lu par `Words.parse`.
- 335 mots sont marqués `false`, mais la recherche réelle trouve `aboucher`, `abstème`, `hyménée`, `sérendipité`, … (34 sur 40 de l'échantillon). Le bouton Académie fonctionne grâce au repli. Conséquence : seulement des données mortes.

**P19. Textes de l'interface.** CONFIRMÉ (strings.xml, MainActivity.kt:64-77).
- Espace simple avant « : » dans `time`, `quiz_time`, `palettes`, `polices`, `test_done`, `slow_default`, `wallpaper_hint`, `quiz_notif`, alors que `unlock_new` et `quiz_retry` utilisent l'insécable.
- Espaces simples à l'intérieur de « » dans `glose_bravo`, `glose_encore` et les noms de `Unlocks` (« « mot » »).
- « mot(s) vu(s) » au lieu d'un `plurals`.
- « Quiz du jour » (bouton) contre « Quiz du soir » (titre et réglage).
- Libellés des menus déroulants écrits en dur dans le code.
- `credits` ne cite pas Wikiquote pour les citations.
- README : « dans la notification du quiz : endormi dans sa tasse » sans préciser « une fois débloqué » (DailyReceiver.kt:70 affiche « assis » avant).

## 3. Couverture des tests (constat)
- Bien couverts : délais 9 mois / 4 mois (simulation sur 3 ans), ratio des citations, stabilité du jour, notifications et fond d'écran, redémarrage, table de déblocage, annonce unique, Collection, pose de 21 h, notification du quiz, `Quiz.passed`.
- Non couverts :
  - changement du nombre de mots, du mode ou de la fin pendant un affichage ;
  - jours de citation seule et séries (P1) ;
  - rappel quand le quiz est déjà fait ;
  - avertissement au-delà de 3 ;
  - liens (Académie, CNRTL, Wikiquote) ;
  - rotation du quiz ;
  - changement de jour ;
  - position du texte ;
  - réaction bravo/encore (seulement dans les captures, sans assertion).
- Suite existante non relancée : seuls les 12 tests jetables ont tourné (`--tests AuditTest`, BUILD SUCCESSFUL).

## 4. Points vérifiés sans problème
- Une citation ne tombe jamais au quiz (Words.kt:204-210, test `dailyWordsAreStableAndMarkedSeen`).
- Un mot au hasard compte dans le délai du mot du jour (`History.mark`), et la réserve « mots par jour × 270 » le protège. Pas de violation non plus en passant de 1 à 3 mots par jour après un usage intensif du hasard (test jetable).
- Toutes les cartes d'écran de verrouillage (1 343 mots + 450 citations) tiennent dans les 40 % prévus, dans les deux positions.
- Données des mots : aucune espace parasite, aucune apostrophe droite, ponctuation finale partout, palettes et polices toutes valides, aucun `|` (séparateur de `dailyMots`) dans les mots ni dans les citations.
