# Audit interface et navigation : Definiday (commit 442fcaf)

Méthodes appliquées : `design:design-critique` (première impression, utilisabilité, hiérarchie, cohérence) et `design:accessibility-review` (WCAG 2.1 AA, Android : TalkBack, cibles 48 dp, grande police, mode sombre, animations).

Matériel :
- Captures Robolectric (GraphicsMode NATIVE) produites dans un worktree jetable avec un test d'audit (`captures/UiAuditTest.kt`, non versionné) : écran principal clair, sombre (`night`), police ×1,3 et ×2,0, w360dp, citation du jour, mot au hasard, mot le plus long, quiz (question, juste, faux, fin, refait le même jour, vide, sombre, ×2,0), Collection (début, sombre), notifications (RemoteViews gonflées), animations coupées. Relevé des vues (taille en dp, couleur, contentDescription, labelFor) : `captures/dump_vues.txt`.
- Captures existantes `app/build/demo/` relues (app, collection, quiz_bravo/encore, lockscreen_*, notif_quiz).
- Lecture : README, `.graphene/` (décisions propriétaire), `res/layout/activity_main.xml`, `values/`, `values-night/`, les 4 Activities, `DailyReceiver`, `WordCard`, `Styles`, `Unlocks`, `Glose`.

Convention : **[vérifié]** = observé dans une capture ou un test Robolectric ; **[code]** = lu dans le code ; **[déduction]** = conséquence du code et des règles Android, non vérifiée sur appareil.
Contrastes : calcul WCAG (luminance relative), script `contrast.py`.

## Synthèse

| Gravité | Nombre |
|---|---|
| Bloquant | 3 |
| Important | 12 |
| Mineur | 14 |

Première impression (2 s) : l'œil va d'abord à Monsieur Glose puis au mot, c'est juste ; la carte est belle et bien hiérarchisée (label, mot 40 sp, nature, définition, exemple). Dès la carte passée, l'écran devient un formulaire système gris : trois actions principales, puis quatorze réglages et un crédit, sans distinction visuelle entre « faire » et « régler ». Les réglages occupent 55 % de la hauteur de la page (de 795 dp à 1 767 dp sur 1 767 dp).

Ce qui marche bien :
- Carte du mot : hiérarchie typographique nette, contrastes forts sur les 18 palettes pour le mot, la définition et l'exemple (≥ 5,3:1).
- Toutes les cibles tactiles font au moins 48 dp de haut [vérifié, dump] ; puces « déjà vus » 48 dp ; cases de Collection ≥ 96 dp.
- Monsieur Glose : dessin soigné, lisible en clair comme en sombre, contentDescription présente ; fin de quiz bienveillante (« encore », « Demain, on retente ? »).
- Collection : éléments verrouillés avec la condition en texte (« Encore 3 soirs de quiz d'affilée »), pas seulement une silhouette.
- Liens : https seulement, repli CNRTL, message « Aucun navigateur » ; animations arrêtées hors écran et coupées si les animations système sont désactivées.
- Mode sombre de l'appli : fond, libellés dorés (8,2:1) et textes secondaires (9,1:1) bien contrastés.

---

## Bloquant

### B1. Toucher la notification d'un mot au hasard (ou d'hier) ouvre un autre mot [vérifié]
- Où : `MainActivity.kt:41` : `daily.firstOrNull { it.key == intent.getStringExtra(EXTRA_MOT) } ?: daily.first()`. La clé n'est cherchée que parmi les mots du jour.
- Test : mot au hasard « velléitaire » notifié, `MainActivity` lancée avec son EXTRA_MOT : l'écran affiche « euphonie » (mot du jour). Même chose pour une notification de la veille encore affichée après minuit (durée jusqu'à 24 h).
- Impact : l'utilisateur touche le mot qu'il veut lire et en voit un autre ; le mot au hasard est perdu (sa notification se ferme, `setAutoCancel`).
- Correctif minimal :
  ```kotlin
  val key = intent.getStringExtra(EXTRA_MOT)
  show((daily + Words.load(this) + Quotes.load(this)).firstOrNull { it.key == key } ?: daily.first())
  ```

### B2. Notifications refusées : en mode par défaut, l'appli n'affiche plus rien, sans le dire [code]
- Où : `Prefs.kt:26` (mode par défaut = `MODE_NOTIF` pour un nouvel utilisateur, cf. capture `02_main_clair_reglages.png` : « Notification seulement ») ; `MainActivity.kt:37` demande `POST_NOTIFICATIONS` au premier lancement, sans contexte, et à chaque `onCreate` (rotation comprise) ; `DailyReceiver.kt:163` avale la `SecurityException` (`runCatching`).
- Impact : sur Android 13+, refuser la demande (fréquent quand elle arrive avant d'avoir vu l'appli) rend la fonction principale muette ; ni l'écran ni « Afficher maintenant » (qui affiche quand même « Mot affiché : verrouillez l'écran pour le voir ») ne signalent le problème. Le quiz du soir disparaît aussi.
- Correctif minimal (sans AndroidX) : dans `onResume`, si `SDK_INT >= 33 && checkSelfPermission(POST_NOTIFICATIONS) != GRANTED && p.mode != MODE_LOCK`, afficher en tête un `TextView` cliquable « Notifications désactivées : le mot du jour ne peut pas s'afficher. Toucher pour les activer. » qui ouvre `Settings.ACTION_APP_NOTIFICATION_SETTINGS` (extra `EXTRA_APP_PACKAGE`). Ne demander la permission que si `savedInstanceState == null`. Même vérification pour le toast de « Afficher maintenant ».

### B3. Quiz : juste ou faux n'est signalé que par la couleur, et le texte devient illisible [vérifié]
- Où : `QuizActivity.kt:50-53` (`isEnabled = false` puis `setBackgroundColor(GOOD/BAD)`), couleurs `QuizActivity.kt:102-103`.
- Captures : `12_quiz_juste.png`, `13_quiz_faux.png`, `15_quiz_w360_police200.png`.
- Mesures : vert `#7FB77E` contre rouge `#E08A7E` : 1,11:1 (indiscernables en deutéranopie/protanopie, ~8 % des hommes) ; texte désactivé sur vert 1,76:1, sur rouge 1,73:1, sur gris 1,86:1 (exigé 4,5:1). Aucun texte « Juste »/« Faux », rien d'annoncé à TalkBack. WCAG 1.4.1 (niveau A), 1.4.3, 4.1.3.
- Impact : un utilisateur daltonien ou malvoyant ne sait pas quelle réponse était la bonne ; or c'est le moment où l'on apprend. Effet secondaire : `setBackgroundColor` efface la forme du bouton, les deux boutons colorés se touchent bord à bord (capture 13).
- Correctif minimal :
  ```kotlin
  o.isEnabled = false
  if (q.options[j] == q.answer) { o.text = "✓ ${o.text}"; o.background.setTint(GOOD) }
  else if (j == i) { o.text = "✗ ${o.text}"; o.background.setTint(BAD) }
  o.setTextColor(0xDE000000.toInt()) // 7,7:1 sur vert, 7,1:1 sur rouge
  ...
  box.announceForAccessibility(if (right) "Juste" else "Faux, c'était ${q.answer.mot}")
  ```
  (`background.mutate().setTint` garde la forme et la marge du bouton Material.) Donner le focus à « Suivant » (`requestFocus()` après `addView`).

---

## Important

### I1. Rotation, changement de thème ou de police : le quiz repart de zéro [vérifié]
- Où : `QuizActivity.kt:18-20` (`questions by lazy` avec `Random.Default`, `index`, `score` non sauvegardés).
- Test : réponse à la question 1, `recreate()` : l'écran passe de « 2 / 3 » à « 1 / 3 », avec de nouvelles propositions tirées au sort. Arrive aussi au passage clair/sombre automatique du soir (le quiz est à 21 h), au pliage, au multi-fenêtre.
- Impact : progression et score perdus ; on peut aussi « rejouer » une question ratée.
- Correctif minimal : `onSaveInstanceState` avec `index`, `score` et une graine `seed` ; `Quiz.build(..., rnd = Random(seed))` (le paramètre existe déjà). Restaurer dans `onCreate`.

### I2. Rotation de l'écran principal : le mot au hasard (ou la citation ouverte) est remplacé par le mot du jour [vérifié]
- Où : `MainActivity.kt:41` rappelle `show(daily...)` à chaque `onCreate` ; `current` (`MainActivity.kt:131`) n'est pas sauvegardé. Les `TimePickerDialog` et la boîte « Prenez le temps » se ferment aussi.
- Test : 4 tirages au hasard (« casuistique »), `recreate()` : « euphonie ».
- Correctif minimal : `onSaveInstanceState { putString("cur", current?.key) }` et, dans `onCreate`, chercher cette clé comme en B1 (même ligne de recherche, à factoriser en `find(key)`).

### I3. Arrivée depuis la notification d'un mot : une seconde copie de l'écran principal s'empile [déduction]
- Où : `DailyReceiver.kt:148-151` (`PendingIntent` vers `MainActivity` sans drapeaux), `AndroidManifest.xml` (pas de `launchMode`), pas d'`onNewIntent`.
- Raison : l'intention de la notification n'a ni l'action `MAIN` ni la catégorie `LAUNCHER` ; Android la considère différente de l'intention racine de la tâche et lance une nouvelle `MainActivity` au-dessus (règle de réutilisation de tâche d'`ActivityStarter`, AOSP). Non vérifiable en Robolectric.
- Impact : « Retour » ramène à une autre copie de l'écran principal, avec l'ancien mot ; chaque notification touchée ajoute une copie.
- Correctif minimal : `android:launchMode="singleTop"` sur `MainActivity`, intention de notification avec `FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TOP`, et
  ```kotlin
  override fun onNewIntent(i: Intent) { super.onNewIntent(i); setIntent(i); show(find(i.getStringExtra(EXTRA_MOT)) ?: Words.today(this).first()) }
  ```
- Notification du quiz (`DailyReceiver.kt:68`) : correcte appli ouverte ; appli fermée, « Retour » sort de l'appli (pas d'écran principal derrière). Option : `PendingIntent.getActivities(ctx, QUIZ_ID, arrayOf(main, quiz), FLAG_IMMUTABLE)`.

### I4. Les réglages sont dans le même défilement que le contenu [vérifié]
- Où : `activity_main.xml:153-260` ; captures `01_main_clair_ecran1.png`, `02_main_clair_reglages.png`.
- Constat : 14 contrôles (2 interrupteurs, 2 heures, 5 menus dont 2 masqués selon le mode, 1 interrupteur citations, 1 interrupteur quiz, palettes, polices), une aide, un bouton de test et les crédits suivent la carte. Les réglages se font une fois ; le mot, le hasard et le quiz tous les jours. Les mêmes boutons gris servent aux deux usages : « Quiz du jour » et « Heure d'affichage : 08:00 » ont exactement le même poids visuel.
- Impact : page de 1 767 dp (environ 2 écrans de 891 dp, 3 écrans en w360) ; risque de toucher un réglage en faisant défiler ; les actions utiles se perdent visuellement.
- Correctif proposé : écran Réglages séparé (voir Proposition de navigation). Coût : une `SettingsActivity` (Activity simple, comme Quiz et Collection), un `activity_settings.xml` = lignes 153-260 déplacées, et le code `MainActivity.kt:51-102` + `spinner()` + `pool()` déplacés ; `MainActivity.onResume` rappelle `show(current)` pour appliquer une nouvelle palette. Variante plus légère : un bouton « Réglages ▾ » qui bascule la visibilité d'un `LinearLayout` regroupant ces lignes (≈6 lignes), sans nouvel écran.

### I5. Réglages de l'écran de verrouillage : aide mal placée, libellé trompeur [vérifié]
- Où : `activity_main.xml:233-238` (aide `wallpaper_hint`) placée entre « Heure du quiz » et « Palettes », à 6 contrôles du menu « Affichage » qu'elle explique, et visible même en « Notification seulement » ; `strings.xml:11` « Durée d'affichage sur l'écran de verrouillage » alors que la durée retire aussi la notification (`setTimeoutAfter`, `DailyReceiver.kt:161`).
- Impact : l'utilisateur lit une aide sur un mode qu'il n'a pas choisi, loin du choix concerné ; en mode notification il croit que la durée ne le concerne pas.
- Correctif : déplacer le `TextView` juste sous le menu `mode`, lui donner un id et l'ajouter à la liste `unlocks` de `MainActivity.kt:67` (masqué en mode notification) ; renommer en « Durée d'affichage ». Grouper sous des sous-titres : Mot du jour / Écran de verrouillage / Quiz du soir / Apparence.

### I6. Menus déroulants sans nom pour TalkBack [vérifié, dump]
- Où : `activity_main.xml:167-217` : chaque `Spinner` est précédé d'un `TextView` sans `labelFor` (dump : `labelFor=-1`, spinner `cd=null`).
- Impact : TalkBack annonce « 2 h, menu déroulant » sans dire de quoi ; « Au 1er déverrouillage » sans contexte. WCAG 1.3.1, 4.1.2.
- Correctif : `android:labelFor="@id/per_day"` (etc.) sur les 5 `TextView` de libellé.

### I7. Contrastes insuffisants : libellés dorés, nature sur 6 palettes claires, indices de Collection [calcul + dump]

| Élément | Avant-plan | Fond | Ratio | Exigé | Correctif |
|---|---|---|---|---|---|
| Libellés `Label` 12 sp gras (« DÉJÀ VUS AUJOURD'HUI », « RÉGLAGES », « LE SAVIEZ-VOUS ? », titres Collection), clair | `#9C7A3C` | `#FAF7F0` | 3,73 | 4,5 | `colors.xml` accent clair `#886A34` (4,6:1) |
| Même chose en sombre | `#C9A96E` | `#0F1424` | 8,19 | 4,5 | ok |
| Nature + label de carte (12-16 sp), palette aube | `#A04A5B` | `#F3CFC4` | 4,01 | 4,5 | `#904352` |
| pistache | `#55702D` | `#D7E3B5` | 4,15 | 4,5 | `#4E6729` |
| givre | `#4A6F8C` | `#D9E7EF` | 4,22 | 4,5 | `#466884` |
| lavande (mot du jour de la démo) | `#6A548F` | `#D9CDEB` | 4,24 | 4,5 | `#644F87` |
| sépia | `#6E4A2F` | `#D6BF9D` | 4,40 | 4,5 | `#69462D` |
| sanguine | `#9A3B22` | `#E7C9B2` | 4,43 | 4,5 | `#963921` |
| Indice Collection 11 sp, alpha 0,7 (`CollectionActivity.kt:78`), clair | ≈`#9C9A95` | `#FAF7F0` | 2,63 | 4,5 | retirer `alpha = 0.7f`, garder la couleur secondaire (4,55:1) |
| Texte secondaire système (aide, crédits) | `#99000000` | `#FAF7F0` | 5,62 | 4,5 | ok |

Ratios « nature » = pire cas entre `fond_haut` et `fond_bas` (sur l'écran de verrouillage la nature est près du bas, `WordCard.kt:28`). Correctifs = même teinte, assombrie juste assez (`assets/styles.json`, champ `nature`). Le mot, la définition et l'exemple passent partout.

### I8. Grande police et petit écran : lien TLFi écrasé puis hors écran, mots longs coupés au milieu [vérifié]
- Où : `activity_main.xml:103-121` (deux boutons `wrap_content` côte à côte, sans poids) ; `MainActivity.kt:140` (mot toujours en 40 sp).
- Captures : `06_main_w360_police130_tlfi.png` (« TLFI / (CNRT / L) » sur 3 lignes), `08_main_w360_police200_bande.png` (×2,0 : le bouton TLFi sort de l'écran, grand blanc sous « Académie française »), `07_mot_long_coupe_w360_police130.png` (« suivez-moi-jeune-h / omme » ; 57 mots ont plus de 12 lettres).
- Impact : lien TLFi illisible ou inaccessible pour les utilisateurs en grande police (WCAG 1.4.4, 1.4.10) ; mot coupé au milieu d'une syllabe, précisément le mot à apprendre.
- Correctif : `android:layout_width="0dp" android:layout_weight="1"` sur les deux boutons (ou `orientation="vertical"` sous 400 dp) ; réutiliser la règle de `WordCard.kt:27` : `textSize = if (item is Quote) 24f else if (item.mot.length > 12) 30f else 40f`. Menus : `layout_height="48dp"` fixe (`activity_main.xml:175` etc.) tronque « Notification seule.. » à ×2,0 : `minHeight="48dp"` + `wrap_content`.

### I9. Styles incohérents : carte soignée, boutons gris système, liens qui n'ont pas l'air de liens [vérifié]
- Où : `themes.xml:16-22` (`Row` = bouton Material gris par défaut), `activity_main.xml:95-121` (liens `borderlessButtonStyle` noirs en capitales), `QuizActivity.kt:30-44`, `CollectionActivity.kt:41-44`.
- Constat : capitales imposées sur tous les boutons (« TLFi » devient « TLFI », « Collection de Monsieur Glose » tient sur 2 lignes en w360 ×1,3) ; « Académie française », « TLFi », « Source » ressemblent à du texte noir ; fond des boutons gris `#D6D7D7` à 1,35:1 sur le fond crème (WCAG 1.4.11 limite) ; le quiz utilise du sans-serif gris (`#8A000000`) quand la carte est en serif ; trois styles de titre : `Label` doré (principal, Collection), gris gras 13 sp (quiz), capitales écrites en dur dans les chaînes (`strings.xml:5,8,21,25,29,41,51`).
- Impact : pas de hiérarchie entre action principale, lien externe et réglage ; l'appli paraît inachevée hors de la carte.
- Correctif minimal, sans AndroidX : dans `AppTheme`, `<item name="android:buttonStyle">@style/Btn</item>` avec `Btn` parent `android:Widget.Material.Button.Borderless.Colored`, `android:textAllCaps=false`, texte accent (assombri, I7) ; pour les 3 actions principales, un fond `GradientDrawable` arrondi en contour accent (déjà utilisé pour la carte et la Collection). Liens : préfixer « ↗ » ou suffixer « (site) ». Quiz : réutiliser `@style/Label` pour le titre et `serif` + `textColorPrimary` pour la définition. Capitales : écrire les chaînes en casse normale et mettre `android:textAllCaps="true"` dans `Label`.

### I10. Quiz refait le même jour : rien n'indique que le score ne compte pas [vérifié]
- Où : `QuizActivity.kt:66` (`Quiz.record` ignore un deuxième quiz du jour, `QuizActivity.kt:123`) ; capture `17_quiz_refait_meme_jour.png`.
- Impact : l'utilisateur refait le quiz pour obtenir un sans-faute et un déblocage ; rien ne se passe, sans explication. Le bouton s'appelle toujours « Quiz du jour ».
- Correctif : lire `p.lastQuizDay == today` avant `record` ; si vrai, ajouter sous le score « Déjà fait aujourd'hui : ce score ne compte pas pour la collection. » Sur l'écran principal, libellé « Quiz du soir (fait ✓) » quand `lastQuizDay == today`.

### I11. « Nouveau mot au hasard » : effets cachés, et la citation du jour devient introuvable [vérifié + code]
- Où : `MainActivity.kt:42-47` ; `DailyReceiver.kt:101` annule les notifications 1 à 3 puis poste le mot au hasard ; `MainActivity.kt:178-188` : les puces « déjà vus » ne listent que des mots (`Words.seenToday`), jamais la citation du jour.
- Constat : un toucher remplace le mot de l'écran de verrouillage et retire les 3 notifications du jour (avec 3 mots par jour), puis poste une notification alors qu'on est dans l'appli. Un jour de citation, après un tirage, la citation n'est plus accessible nulle part (sa notification a été retirée). Les jours de citation seule, « DÉJÀ VUS AUJOURD'HUI » s'affiche au-dessus d'une ligne vide (capture `26_jour_citation_deja_vus_vide.png`).
- Décision du propriétaire (graphene 58c1) : le hasard remplace le mot de l'écran de verrouillage. Le constat porte sur l'absence d'information et la perte d'accès, pas sur la règle.
- Correctif : puces = `(Words.today(this) + Words.seenToday(this)).distinctBy { it.key }`, libellé « Citation » pour une citation ; masquer le libellé si la liste est vide ; texte d'aide sous le bouton ou toast une fois : « Il remplace le mot de l'écran de verrouillage. » À trancher : ne remplacer que le fond d'écran et garder les notifications du jour.

### I12. Animations désactivées : les poses « le fil écrit … » perdent leur mot sur l'écran principal [vérifié, correctif vérifié]
- Où : `Glose.kt:58-59` : `d.reset()` est appelé même quand les animations sont coupées ; `reset()` ramène le fil à sa première image, non écrite.
- Test : `ValueAnimator.setDurationScale(0)`, pose `ecrit_mot` : Glose lève la patte sans fil ni mot (`23_animations_off_mot_absent.png`). Avec le correctif ci-dessous, le mot s'affiche (`24_animations_off_apres_correctif.png`). Fin de quiz (`glose_ecrit_*_fin`) : « bravo » s'affiche dans les deux cas.
- Impact : les utilisateurs sensibles au mouvement (réglage « Supprimer les animations », économiseur de batterie) ne voient pas l'élément qu'ils ont débloqué et choisi.
- Correctif (2 lignes, testé dans le worktree puis annulé) :
  ```kotlin
  if (!ValueAnimator.areAnimatorsEnabled()) return // reset() would rewind the thread to its unwritten first frame
  d.reset(); d.start()
  ```

---

## Mineur

### M1. Carte « citation du jour » : grand vide en bas, signature collée [vérifié]
`MainActivity.kt:151-152` met `definition` et `exemple` à "" sans les masquer : deux `TextView` vides gardent leur hauteur de ligne et leurs marges (capture `09_citation_du_jour.png`). Correctif : dans `text()`, `visibility = if (s.isEmpty()) View.GONE else View.VISIBLE` ; `layout_marginTop="8dp"` sur `nature` pour la signature.

### M2. Ordre et retour des liens [vérifié + code]
L'anecdote s'intercale entre la carte et les liens Académie/TLFi qui concernent le mot (`activity_main.xml:73-121`, capture `10_mot_au_hasard.png`) ; « Source » seul ne dit pas de quoi (TalkBack : « Source, bouton »). Le lien Académie lance une requête réseau (`MainActivity.kt:236-249`) sans aucun retour visuel pendant jusqu'à 10 s ; deux touchers ouvrent deux onglets. Correctif : mettre le bloc des liens juste sous la carte ; « Source de l'anecdote » ; `academie.isEnabled = false` pendant la recherche (réactivé dans `runOnUiThread`).

### M3. Écarts excessifs libellé / menu [vérifié]
Les libellés utilisent `style="@style/Row"` (`minHeight 48dp`, `themes.xml:21`) : 48 dp de libellé + 48 dp de menu, le libellé flotte loin de sa valeur (capture `02`). Correctif : style `FieldLabel` sans `minHeight`, 14 sp, `marginTop 16dp`.

### M4. Réglages dépendants non grisés [code]
« Heure d'affichage » reste actif quand « Afficher un mot chaque jour » est coupé, « Heure du quiz » quand « Quiz du soir » est coupé (`MainActivity.kt:51-95`). Correctif : `time.isEnabled = on` dans l'écouteur et à l'initialisation (idem `quizTime`).

### M5. « Afficher maintenant » : place et message [code]
Bouton de test en bas des réglages (`activity_main.xml:248`), toast « verrouillez l'écran pour le voir » (`strings.xml:18`) même en mode notification seule. Correctif : « Afficher le mot maintenant » dans le groupe Mot du jour ; toast selon le mode (« Mot affiché dans les notifications. »).

### M6. Textes : noms du quiz, pluriels, capitales [code]
« Quiz du jour » (bouton, `strings.xml:35`) / « QUIZ DU SOIR » (titre, notification) / « Quiz du soir » (interrupteur) : un seul nom, « Quiz du soir ». « %1$d mot(s) vu(s) » (`strings.xml:30`) : `<plurals>` (« 1 mot vu aujourd'hui », « 3 mots vus »). Titre de notification en capitales (`22_notif_quiz.png`) : « Quiz du soir ». « Choisir » (Collection) : « Adopter » ou « Garder cette tenue ». Ton général juste et chaleureux ; le texte d'aide `wallpaper_hint` (4 phrases) gagnerait à être coupé en deux lignes courtes.

### M7. TalkBack : titres, états, décor [vérifié, dump]
- Libellés de section non marqués comme titres : `android:accessibilityHeading="true"` dans `Label` (API 28+, ignoré en 26-27 sans erreur).
- Collection : la case portée n'est signalée que par un contour (`CollectionActivity.kt:89-96`) : `v.isSelected = id in trial.values` (TalkBack dit « sélectionné ») ; l'aperçu (`CollectionActivity.kt:36`) dit toujours « Monsieur Glose, chat bibliothécaire » : y ajouter les noms des éléments essayés.
- Tête de Glose du quiz (56 dp) annoncée à chaque question : `importantForAccessibility = NO`. Glose de l'accueil cliquable : TalkBack dit « appuyer deux fois pour activer » sans dire quoi ; acceptable, ou retirer le clic de l'arbre d'accessibilité.

### M8. Mode sombre : carte claire éblouissante sur fond nuit [vérifié]
La palette suit le mot, pas le thème : la carte lavande claire s'affiche sur `#0F1424` (`03_main_sombre_ecran1.png`), au moment même du quiz du soir. Le champ `sombre` de `styles.json` n'est pas lu (`Styles.kt:23`). À arbitrer (choix « une palette par mot » du propriétaire) : en mode nuit, si la palette du mot est claire, prendre une palette sombre stable du pool pour la carte de l'appli seulement (l'écran de verrouillage garde la sienne).

### M9. Quiz : typographie et apprentissage [vérifié]
Définition-question en gris secondaire sans-serif (4,55:1, `QuizActivity.kt:42`) alors que c'est le contenu principal ; la fin ne rappelle pas les mots ratés. Correctif : `textColorPrimary` + serif ; à la fin, liste « À revoir : mot : définition » des réponses fausses (garder une liste `missed`).

### M10. États vides [vérifié]
Quiz vide (`18_quiz_vide.png`) : « Aucun mot vu aujourd'hui pour l'instant. » sans action ; le bouton Quiz reste actif. Correctif : bouton « Voir un mot au hasard » qui ferme et déclenche `new_word`, ou griser le bouton Quiz quand `Words.seenToday` est vide (texte « Quiz du soir : aucun mot vu »).

### M11. Sortie du quiz et de la Collection [code]
Pas de barre de titre (`NoActionBar`) ni de bouton de sortie en cours de quiz ; Collection sans « Fermer ». Le geste Retour suffit sur Android, mais rien ne rappelle qu'on peut abandonner le quiz sans perte. Correctif léger : titre cliquable « ‹ Quiz du soir » qui appelle `finish()`, ou rien (acceptable).

### M12. Animation en boucle infinie [code]
Les AVD d'attente bouclent (`repeatCount="infinite"`, ex. `glose_ecrit_bravo_fin.xml`), sans moyen de les arrêter dans l'appli hors réglage système (WCAG 2.2.2 : mouvement de plus de 5 s à côté du contenu). Le mouvement est discret et coupé hors écran. Correctif : un toucher sur Glose alterne lecture/pause (`if (d.isRunning) d.stop() else play(v)`) au lieu de rejouer.

### M13. Palettes et Polices : boîtes sans titre, compteur peu clair, polices décoratives [vérifié]
`MainActivity.kt:197` : `AlertDialog` sans `setTitle` (TalkBack ne sait pas de quelle liste il s'agit) ; « Polices : 14 / 14 » ne dit pas que c'est un choix. Polices manuscrites et gothique (Parisienne, Dancing Script, UnifrakturMaguntia) sur le mot : beau, mais difficile pour les lecteurs dyslexiques ou malvoyants ; les citations les excluent déjà (`Styles.kt:19`). Correctif : `setTitle("Polices utilisées")` ; libellé « Polices utilisées : 14 sur 14 » ; option à envisager « Polices très décoratives » (on/off) qui retire `DISPLAY_ONLY` du pool des mots.

### M14. Contrastes non textuels faibles [calcul]
Silhouettes verrouillées `#B8AFA0` sur `#FAF7F0` : 2,03:1 (l'information passe aussi par le texte, donc impact faible) ; bords des boutons gris 1,35:1 (traité par I9).

---

## Navigation actuelle (constat)

```
Lanceur ──► Accueil (1 seul ScrollView : Glose, carte, liens, 3 actions, déjà vus, 14 réglages, test, crédits)
              ├─[Quiz du jour]──► Quiz ──► Fin ──[Voir la collection]──► Collection
              └─[Collection de Monsieur Glose]──► Collection
Notif mot ──► nouvelle copie de l'Accueil (I3), mot du jour à la place du mot touché si hasard/veille (B1)
Notif quiz ──► Quiz (appli fermée : Retour = sortie)
Rotation : Quiz recommence (I1), Accueil revient au mot du jour (I2)
```

## Proposition de navigation

```
Lanceur ─► ACCUEIL (MainActivity, singleTop)
           │  [bandeau si notifications refusées → réglages système]        (B2)
           │  Glose (décor ; toucher = pause/lecture)
           │  Carte : mot ou citation (taille adaptée aux mots longs)
           │  Liens : Académie française ↗ · TLFi ↗   (juste sous la carte)
           │  Le saviez-vous ? … Source de l'anecdote ↗
           │  ┌ Actions ───────────────────────────────┐
           │  │ [Nouveau mot au hasard]                 │  (aide : remplace l'écran de verrouillage)
           │  │ [Quiz du soir]  (« fait ✓ » si fait)    │
           │  │ [Collection de Monsieur Glose]          │
           │  └─────────────────────────────────────────┘
           │  Vus aujourd'hui : puces (mots + citation du jour)
           │  [Réglages]  (bouton texte en bas, ou icône en haut à droite)
           │
           ├─► QUIZ ─► fin (score, mots à revoir, « ne compte pas » si refait)
           │            └─[Voir la collection]─► COLLECTION ─(Retour)─► fin du quiz
           ├─► COLLECTION ─(Retour)─► Accueil
           └─► RÉGLAGES (SettingsActivity) ─(Retour)─► Accueil (carte rafraîchie)
                 Mot du jour : activer · heure · mots par jour · durée · [Afficher le mot maintenant]
                 Écran de verrouillage : affichage · fin · position du texte · aide (si mode ≠ notification)
                 Quiz du soir : activer · heure
                 Apparence : palettes · polices · plus de citations
                 À propos : sources et crédits

Notif mot  ─► Accueil existant (onNewIntent) sur le mot touché, quel qu'il soit      (B1, I3)
Notif quiz ─► Accueil + Quiz (getActivities) : Retour mène à l'Accueil
Rotation / thème / police : état conservé (onSaveInstanceState : mot affiché ; quiz index, score, graine)
```

Ordre de mise en œuvre conseillé (du moins cher au plus utile) : B1, I12, I6, I7, B3, B2, I1/I2, I3, I8, puis I4 (écran Réglages) qui absorbe I5, M3, M4, M5.

## Limites
- Robolectric : pas de vrai TalkBack, pas de pile de tâches réelle (I3 est une déduction), notifications gonflées hors du volet système (rendu système réel non vérifié), pas de test sur appareil ni sur thème constructeur.
- Contrastes de la carte mesurés sur les couleurs de `styles.json` (pire extrémité du dégradé), pas sur les pixels.
- Aucune modification du dépôt : test d'audit et correctif I12 essayés dans le worktree jetable, puis retirés.
