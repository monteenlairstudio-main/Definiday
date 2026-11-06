# Chasse aux bugs — Definiday @ 442fcaf

Méthode : lecture de tout `app/src/main` (12 fichiers Kotlin, manifeste, res/xml, assets), des tests et de `.graphene/`. Chaque bug est prouvé par un test Robolectric qui **échoue** sur 442fcaf, dans `app/src/test/java/fr/definiday/BugHuntTest.kt` du worktree (copie : `audits2/tests_bugs/BugHuntTest.kt`). Les pistes sans bug sont vérifiées par `PistesTest.kt` (5 tests qui passent) ou par trace.

Exécution : `./gradlew --no-daemon -q :app:testDebugUnitTest` → DefinidayTest 18/18 et GloseTest 8/8 passent, PistesTest 5/5 passent, **BugHuntTest 10/10 échouent** (bug 6 a deux tests : quiz et écran principal). Seul le SDK Robolectric 36 est en cache (un `@Config(sdk = [34])` échoue au téléchargement).

Astuce de reproduction « minuit / fuseau » : `LocalDate.now()` lit `TimeZone.getDefault()` à chaque appel ; passer de `Etc/GMT+12` (UTC−12) à `Etc/GMT-12` (UTC+12) donne le même instant avec une date +1, sans horloge injectable.

## Résumé

| # | Gravité | Bug |
|---|---|---|
| 1 | Haute | La série (streak) plafonne à 4 avec les réglages par défaut : les jours « citation » n'ont pas de quiz |
| 2 | Moyenne | Quiz commencé avant minuit et fini après : compté pour le lendemain (série cassée, quiz du lendemain ignoré) |
| 3 | Moyenne | Passer « Fin » à « Après la durée choisie » pendant l'affichage retire le mot au déverrouillage suivant |
| 4 | Moyenne | « Retrait au Nᵉ déverrouillage » inopérant pour le mot du matin sur Android 12+ |
| 5 | Basse | Toucher la notification d'un mot « au hasard » ouvre l'appli sur le mot du jour |
| 6 | Basse | Rotation : le quiz repart de la question 1 ; la carte de l'écran principal revient au mot du jour |
| 7 | Basse | Deux mots partagent la même définition : le quiz peut proposer les deux, l'un est compté faux |
| 8 | Basse | Rappel du quiz envoyé même quand le quiz du jour est déjà fait |
| 9 | Basse (cas limite) | Nuit du passage à l'heure d'hiver : alarme de 2 h xx livrée en retard → mot ré-affiché 20 min plus tard |

Aides communes aux tests (dans BugHuntTest) :
```kotlin
    private fun views(a: android.app.Activity) = generateSequence(listOf<View>(a.window.decorView)) { l ->
        l.flatMap { v -> (v as? android.view.ViewGroup)?.let { g -> (0 until g.childCount).map(g::getChildAt) } ?: emptyList() }.ifEmpty { null }
    }.flatten()
    private fun texts(a: android.app.Activity) = views(a).filterIsInstance<TextView>().map { it.text.toString() }.toList()
    /** Answers the current question (first option) and goes to the next one. */
    private fun answerOne(a: android.app.Activity) {
        val defs = Words.load(ctx).map { it.definition }.toSet()
        val q = texts(a).first { it in defs }
        views(a).filterIsInstance<Button>().first { b -> Words.load(ctx).any { it.mot == b.text.toString() && it.definition == q } }.performClick()
        views(a).filterIsInstance<Button>().first { it.text == ctx.getString(R.string.quiz_next) }.performClick()
    }
```

## Bug 1 — La série (streak) plafonne à 4 avec les réglages par défaut : les jours « citation » n'ont pas de quiz

- **Gravité** : Haute
- **Où** : Words.kt:61 (isQuoteSlot, Words.kt:43) + QuizActivity.kt:63 (quiz vide, rien n'est enregistré) + QuizActivity.kt:125 (Quiz.record remet la série à 1) + DailyReceiver.kt:65 (pas de rappel)
- **Scénario** : Réglages par défaut (1 élément/jour, « Plus de citations » désactivé). Un jour sur 5, l'unique créneau du jour est une citation (`isQuoteSlot(day, 0, false)` vrai quand `3·day mod 5 = 0`). Ce jour-là `Words.seenToday()` est vide (les citations ne sont pas matière à quiz) : pas de notification de quiz, et `QuizActivity` affiche « Aucun mot vu aujourd'hui » sans appeler `Quiz.record`. Le lendemain `lastQuizDay == day - 2` → `streak = 1`. Un utilisateur assidu qui fait le quiz chaque soir où c'est possible ne dépasse jamais 4 soirs d'affilée.
- **Attendu** : Les poses/accessoires à série (écharpe 7, bonnet 14, pelage nuit 30) atteignables en jouant chaque soir.
- **Obtenu** : Série max = 4 sur un an → écharpe, bonnet, nuit inatteignables sauf à tirer un mot au hasard les jours citation. Avec « Plus de citations » (2 sur 5 : jours ≡ 0 et 2 mod 5) la série max tombe à **2** : même « Endormi dans sa tasse » et « dodo » (série 3) deviennent inatteignables. Avec 2 mots/jour + plus de citations : 1 jour sur 5 sans mot, max 4.
- **Test** (`BugHuntTest.quoteDaysDoNotCapTheStreak`) :

```kotlin
    /** BUG 1: with the default settings (1 item a day, 1 quote in 5), a quote day has no quiz and resets the streak: max streak 4. */
    @Test fun quoteDaysDoNotCapTheStreak() {
        // Robolectric part: a day whose only daily slot is a quote (what Words.today() picks when isQuoteSlot(day, 0, false))
        val p = Prefs(ctx)
        val today = LocalDate.now().toEpochDay()
        p.dailyDay = today; p.dailyMots = listOf(Quotes.load(ctx).first().key)
        assertTrue(Words.seenToday(ctx).isEmpty())
        val a = Robolectric.buildActivity(QuizActivity::class.java).setup().get()
        assertTrue(ctx.getString(R.string.quiz_empty) in texts(a)) // nothing to answer, nothing recorded
        assertEquals(Long.MIN_VALUE, p.lastQuizDay)
        // A full year of perfect evenings, quizzing every day the app gives quiz material.
        var best = 0
        for (d in 20_000L until 20_365L) if (!Words.isQuoteSlot(d, 0, false)) { Quiz.record(p, 1, 1, d); best = maxOf(best, p.streak) }
        assertTrue("best streak in a year of daily quizzes = $best: écharpe (7), bonnet (14), nuit (30) are unreachable", best >= 30)
    }
```

- **Correctif minimal** : Ne pas casser la série sur un jour sans matière : dans `Quiz.record`, considérer la série continue si tous les jours entre `lastQuizDay` et `day` étaient sans mot, p. ex.
```kotlin
fun noMaterial(d: Long, p: Prefs) = (0 until p.wordsPerDay).all { Words.isQuoteSlot(d, it, p.moreQuotes) }
p.streak = if ((p.lastQuizDay + 1 until day).all { noMaterial(it, p) }) p.streak + 1 else 1
```
(garder `lastQuizDay == Long.MIN_VALUE` → 1 ; borne la boucle). Alternative : garantir au moins un mot par jour dans `Words.today` (mais change le ratio 1/5 avec 1 élément/jour).

## Bug 2 — Quiz commencé avant minuit et fini après : compté pour le lendemain (série cassée, quiz du lendemain ignoré)

- **Gravité** : Moyenne
- **Où** : QuizActivity.kt:66 (`LocalDate.now()` lu à la fin) vs QuizActivity.kt:18 (questions construites avec le jour du début)
- **Scénario** : Quiz du jour D commencé à 23 h 58 (questions = mots de D), dernière réponse à 0 h 01. `Quiz.record(..., LocalDate.now())` enregistre D+1. Conséquences : (a) le jour D est un trou → série remise à 1 ; (b) le soir de D+1, `lastQuizDay == D+1` → le vrai quiz de D+1 n'est pas compté (ni quizzes, ni passed, ni série). Variante : la notification de 21 h touchée après minuit ouvre un quiz vide (`seenToday` du nouveau jour), la notification n'expirant pas. Reproduit en déplaçant le fuseau par défaut de UTC−12 à UTC+12 (même instant, date +1), ce qui couvre aussi le changement de fuseau en voyage.
- **Attendu** : Le quiz compte pour D (lastQuizDay = D, série 6 → 7).
- **Obtenu** : lastQuizDay = D+1, série = 1 (message : `expected:<20763> but was:<20764>`).
- **Test** (`BugHuntTest.quizFinishedAfterMidnightCountsForTheDayItWasStarted`) :

```kotlin
    /** BUG 2: a quiz started before midnight and finished after counts for the next day: streak broken, next evening's quiz ignored. */
    @Test fun quizFinishedAfterMidnightCountsForTheDayItWasStarted() {
        val tz = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Etc/GMT+12")) // UTC-12: evening of day D
            val d = LocalDate.now().toEpochDay()
            val p = Prefs(ctx).apply { lastQuizDay = d - 1; streak = 6 }
            while (Words.seenToday(ctx).isEmpty()) Words.random(ctx)
            val a = Robolectric.buildActivity(QuizActivity::class.java).setup().get()
            TimeZone.setDefault(TimeZone.getTimeZone("Etc/GMT-12")) // same instant seen 24 h later: past midnight, day D+1
            assertEquals(d + 1, LocalDate.now().toEpochDay())
            repeat(Words.run { TimeZone.setDefault(TimeZone.getTimeZone("Etc/GMT+12")); seenToday(ctx).size.also { TimeZone.setDefault(TimeZone.getTimeZone("Etc/GMT-12")) } }) { answerOne(a) }
            assertEquals("quiz of day D recorded as", d, p.lastQuizDay)
            assertEquals(7, p.streak)
        } finally { TimeZone.setDefault(tz) }
    }
```

- **Correctif minimal** : Figer le jour quand les questions sont construites : `private val day = LocalDate.now().toEpochDay()` dans QuizActivity (initialisé avec `questions`) et `Quiz.record(..., day)`. Pour la variante notification : `setTimeoutAfter` jusqu'à minuit sur la notification du quiz.

## Bug 3 — Passer « Fin » à « Après la durée choisie » pendant l'affichage retire le mot au déverrouillage suivant

- **Gravité** : Moyenne
- **Où** : UnlockService.kt:57-59 (`countUnlock`) ; MainActivity.kt:74-75 (le spinner change seulement la pref)
- **Scénario** : Mode verrouillage, « Au 2e déverrouillage », mot affiché → UnlockService tourne. L'utilisateur ouvre l'appli et choisit « Après la durée choisie » (`unlocks = 0`). Le service n'est ni arrêté ni prévenu ; au déverrouillage suivant `unlocksSeen (1) >= unlocks (0)` → `clearWallpaper(force = true)`. Inverse : passer de 0 à 1-3 pendant l'affichage n'a aucun effet (service non démarré) jusqu'au lendemain.
- **Attendu** : Avec 0, le mot reste pour la durée choisie.
- **Obtenu** : Fond d'écran retiré au 1er déverrouillage (wallpaperEndAt = 0).
- **Test** (`BugHuntTest.unlocksSetToZeroKeepsTheWallpaperForTheDuration`) :

```kotlin
    /** BUG 3: switching "Fin sur l'écran de verrouillage" to "Après la durée choisie" while the watch runs removes the word at the next unlock. */
    @Test fun unlocksSetToZeroKeepsTheWallpaperForTheDuration() {
        val p = Prefs(ctx).apply { mode = Prefs.MODE_LOCK; unlocks = 2; durationMinutes = 60 }
        DailyReceiver.show(ctx) // UnlockService started
        p.unlocks = 0 // MainActivity spinner: only the pref changes, the service keeps counting
        UnlockService.countUnlock(ctx) // next unlock
        assertTrue("wallpaper removed at the 1st unlock although the user chose the duration", p.wallpaperEndAt != 0L)
    }
```

- **Correctif minimal** : Dans `countUnlock` : `if (p.wallpaperEndAt == 0L || p.unlocks == 0) return true` (le service s'arrête, le fond reste jusqu'à END). Pour l'inverse, le spinner peut appeler `UnlockService.start(this)` si `unlocks > 0 && wallpaperEndAt != 0L` (premier plan : autorisé).

## Bug 4 — « Retrait au Nᵉ déverrouillage » inopérant pour le mot du matin sur Android 12+

- **Gravité** : Moyenne
- **Où** : DailyReceiver.kt:89 (`canScheduleExactAlarms()`), DailyReceiver.kt:115 (`UnlockService.start` depuis le receiver), AndroidManifest.xml (aucune permission d'alarme exacte)
- **Scénario** : Le manifeste ne déclare ni `SCHEDULE_EXACT_ALARM` ni `USE_EXACT_ALARM`, donc `canScheduleExactAlarms()` est toujours faux sur API 31+ : l'alarme SHOW est toujours inexacte (`setAndAllowWhileIdle`). Une alarme inexacte n'exempte pas des restrictions de démarrage de service de premier plan depuis l'arrière-plan (API 31+) : `startForegroundService` lève `ForegroundServiceStartNotAllowedException`, avalée par `runCatching` dans `UnlockService.start`. Le commentaire du code prévoit ce refus « parfois », mais faute de permission il est **systématique** pour l'affichage planifié : seuls « Afficher maintenant », « Nouveau mot » (premier plan) et le chemin BOOT/TIME_SET (exemptés) démarrent le suivi. L'utilisateur ne voit rien : le mot reste toute la durée. Preuve : test sur les permissions déclarées + trace (Robolectric n'applique pas la restriction FGS).
- **Attendu** : Le mot du matin disparaît au Nᵉ déverrouillage.
- **Obtenu** : Sur Android 12+ il reste jusqu'à la fin de la durée.
- **Test** (`BugHuntTest.dailyAlarmMayStartTheUnlockWatch`) :

```kotlin
    /** BUG 4: without an exact-alarm permission the daily SHOW alarm is inexact on Android 12+, so the background start of UnlockService is refused. */
    @Test fun dailyAlarmMayStartTheUnlockWatch() {
        val perms = ctx.packageManager.getPackageInfo(ctx.packageName, PackageManager.GET_PERMISSIONS).requestedPermissions.orEmpty()
        assertTrue("canScheduleExactAlarms() is always false: ${perms.toList()}",
            perms.any { it == android.Manifest.permission.SCHEDULE_EXACT_ALARM || it == android.Manifest.permission.USE_EXACT_ALARM })
    }
```

- **Correctif minimal** : Déclarer `<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />` (accordée d'office en API 31-32 ; en 33+ il faut l'accord via `Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM`, à demander quand l'utilisateur choisit 1-3 déverrouillages), et à défaut afficher dans les réglages que le retrait au déverrouillage ne s'appliquera qu'à la durée.

## Bug 5 — Toucher la notification d'un mot « au hasard » ouvre l'appli sur le mot du jour

- **Gravité** : Basse
- **Où** : MainActivity.kt:41
- **Scénario** : « Nouveau mot au hasard » → `DailyReceiver.show(listOf(w))` poste la notification de w avec `EXTRA_MOT = w.mot`. Plus tard, toucher cette notification : `onCreate` ne cherche l'extra que dans `Words.today()` → repli sur `daily.first()`. Idem pour une notification d'hier touchée aujourd'hui.
- **Attendu** : La carte affiche le mot touché.
- **Obtenu** : Elle affiche le mot du jour (`expected:<[quinola]> but was:<[euphonie]>`).
- **Test** (`BugHuntTest.randomPickNotificationOpensThatWord`) :

```kotlin
    /** BUG 5: tapping the notification of a random pick opens the app on the daily word, not on the word tapped. */
    @Test fun randomPickNotificationOpensThatWord() {
        Words.today(ctx)
        val r = Words.random(ctx)
        DailyReceiver.show(ctx, listOf(r))
        val n = ctx.getSystemService(NotificationManager::class.java).activeNotifications.single().notification
        val tap = shadowOf(n.contentIntent).savedIntent
        val a = Robolectric.buildActivity(MainActivity::class.java, tap).setup().get()
        assertEquals(r.mot, a.findViewById<TextView>(R.id.mot).text.toString())
    }
```

- **Correctif minimal** : Chercher aussi dans tout le catalogue : `val key = intent.getStringExtra(EXTRA_MOT); show(daily.firstOrNull { it.key == key } ?: (Words.load(this) + Quotes.load(this)).firstOrNull { it.key == key } ?: daily.first())`.

## Bug 6 — Rotation : le quiz repart de la question 1 ; la carte de l'écran principal revient au mot du jour

- **Gravité** : Basse
- **Où** : QuizActivity.kt:18-20 (`questions`, `index`, `score` non sauvegardés) ; MainActivity.kt:131 (`current`) ; manifeste sans `configChanges`
- **Scénario** : Quiz à 2 mots : répondre à la 1re, « Suivant » → « 2 / 2 » ; tourner le téléphone → activité recréée, « 1 / 2 », score à 0, options retirées au sort. Pas de double enregistrement (garde `lastQuizDay`). Écran principal : « Nouveau mot » puis rotation → la carte réaffiche le mot du jour (le mot au hasard reste sur l'écran de verrouillage).
- **Attendu** : L'état est conservé.
- **Obtenu** : Quiz : `after rotation: [1 / 2]` ; carte : `expected:<[fondrièr]e> but was:<[euphoni]e>`.
- **Test** (`BugHuntTest.quizSurvivesRotation`, `mainCardSurvivesRotation`) :

```kotlin
    /** BUG 6: rotating the phone mid-quiz restarts it from question 1 (score lost, options reshuffled). */
    @Test fun quizSurvivesRotation() {
        while (Words.seenToday(ctx).size < 2) Words.random(ctx)
        val c = Robolectric.buildActivity(QuizActivity::class.java).setup()
        answerOne(c.get())
        val total = Words.seenToday(ctx).size
        assertTrue("2 / $total" in texts(c.get()))
        c.recreate()
        assertTrue("after rotation: ${texts(c.get()).filter { "/" in it }}", "2 / $total" in texts(c.get()))
    }

    /** BUG 6b: the random word shown in the card is lost on rotation (daily word comes back). */
    @Test fun mainCardSurvivesRotation() {
        val c = Robolectric.buildActivity(MainActivity::class.java).setup()
        c.get().findViewById<Button>(R.id.new_word).performClick()
        val shown = c.get().findViewById<TextView>(R.id.mot).text.toString()
        c.recreate()
        assertEquals(shown, c.get().findViewById<TextView>(R.id.mot).text.toString())
    }
```

- **Correctif minimal** : Le plus court : `android:configChanges="orientation|screenSize|screenLayout|smallestScreenSize"` sur QuizActivity et MainActivity (mises en page programmatique / simple ScrollView). Sinon `onSaveInstanceState` avec index, score, clés des options et `current.key`.

## Bug 7 — Deux mots partagent la même définition : le quiz peut proposer les deux, l'un est compté faux

- **Gravité** : Basse
- **Où** : app/src/main/assets/words.json (aruspice/haruspice, mercuriale/semonce, même nature) ; QuizActivity.kt:112-114 (`Quiz.build`)
- **Scénario** : Le quiz affiche la définition de « haruspice » ; les distracteurs sont pris d'abord dans la même nature (n. m.), donc « aruspice » (même définition mot pour mot) peut sortir. Les deux boutons sont des réponses justes, un seul est accepté. Sur 2000 tirages : 10 fois.
- **Attendu** : Jamais deux options portant la définition affichée.
- **Obtenu** : 10 / 2000 tirages proposent aruspice ET haruspice.
- **Test** (`BugHuntTest.quizNeverOffersTwoWordsWithTheAnswersDefinition`) :

```kotlin
    /** BUG 7: two words share a definition (aruspice/haruspice, mercuriale/semonce): the quiz can show both, one is marked wrong. */
    @Test fun quizNeverOffersTwoWordsWithTheAnswersDefinition() {
        val all = Words.load(ctx)
        val w = all.first { it.mot == "haruspice" }
        val bad = (0 until 2000).count { s -> Quiz.build(listOf(w), all, kotlin.random.Random(s)).single().options.count { it.definition == w.definition } > 1 }
        assertEquals("seeds offering aruspice AND haruspice for the same definition", 0, bad)
    }
```

- **Correctif minimal** : Dans `Quiz.build` : `val others = all.filter { it.mot != w.mot && it.definition != w.definition }` (et/ou différencier les deux définitions dans words.json : « Variante de haruspice »…).

## Bug 8 — Rappel du quiz envoyé même quand le quiz du jour est déjà fait

- **Gravité** : Basse
- **Où** : DailyReceiver.kt:65 (`quizReminder`)
- **Scénario** : L'utilisateur fait le quiz à 20 h depuis l'appli ; à 21 h l'alarme QUIZ poste quand même « N mot(s) vu(s) aujourd'hui : les reconnaîtrez-vous ? ».
- **Attendu** : Pas de rappel si `lastQuizDay == aujourd'hui`.
- **Obtenu** : Notification id 50 postée.
- **Test** (`BugHuntTest.noQuizReminderOnceTodaysQuizIsDone`) :

```kotlin
    /** BUG 8: the evening reminder is posted even when today's quiz is already done. */
    @Test fun noQuizReminderOnceTodaysQuizIsDone() {
        while (Words.seenToday(ctx).isEmpty()) Words.random(ctx)
        Quiz.record(Prefs(ctx), 1, 1, LocalDate.now().toEpochDay())
        DailyReceiver().onReceive(ctx, Intent(ctx, DailyReceiver::class.java).setAction(DailyReceiver.ACTION_QUIZ))
        assertTrue(ctx.getSystemService(NotificationManager::class.java).activeNotifications.none { it.id == 50 })
    }
```

- **Correctif minimal** : `if (Words.seenToday(ctx).isEmpty() || Prefs(ctx).lastQuizDay == LocalDate.now().toEpochDay()) return`.

## Bug 9 — Nuit du passage à l'heure d'hiver : alarme de 2 h xx livrée en retard → mot ré-affiché 20 min plus tard

- **Gravité** : Basse (cas limite)
- **Où** : DailyReceiver.kt:42-44 (`nextTrigger`)
- **Scénario** : Heure d'affichage 02:30, nuit du 25/10/2026 (Europe/Paris). L'alarme 02:30+02:00 est inexacte (cf. bug 4) et livrée 40 min plus tard, à 02:10+01:00 (2e passage). `now.withHour(2).withMinute(30)` garde l'offset +01:00 → 02:30+01:00, qui est après `now` → reprogrammée le même jour, 20 min plus tard. Second SHOW : mêmes mots, mais notifications re-postées, fond ré-appliqué, durée et compteur de déverrouillages remis à zéro. Ne concerne que les heures 2 h–2 h 59 ce jour-là.
- **Attendu** : Prochain affichage le 26/10.
- **Obtenu** : Le 25/10 à 02:30+01:00.
- **Test** (`BugHuntTest.fallBackLateDeliveryDoesNotFireTwice`) :

```kotlin
    /** BUG 9 (edge): fall-back night, daily time 02:30, inexact alarm delivered 40 min late (02:10 second pass) -> rescheduled 20 min later the same night. */
    @Test fun fallBackLateDeliveryDoesNotFireTwice() {
        val late = ZonedDateTime.parse("2026-10-25T02:10:00+01:00[Europe/Paris]") // the 02:30+02:00 alarm, 40 min late
        val next = DailyReceiver.nextTrigger(late, 2, 30)
        assertEquals("next show", LocalDate.of(2026, 10, 26), next.toLocalDate())
    }
```

- **Correctif minimal** : Ancrer sur la date : `val t = now.toLocalDate().atTime(hour, minute).atZone(now.zone)` (résout l'ambiguïté vers l'offset le plus tôt, donc déjà passé) — ou mémoriser le jour du dernier SHOW et ne pas réafficher le même jour.

## Pistes vérifiées sans bug

Tests qui passent : `PistesTest.kt` (copie dans `audits2/tests_bugs/`).

- **Mot disparu de words.json mais présent dans dailyMots/historique** : `today()` le remplace une fois, renvoie toujours `wordsPerDay` éléments, reste stable ensuite ; `seenToday` l'ignore (`removedWordInDailyMotsIsReplacedOnce`). Les clés orphelines de `history` ne servent qu'à des recherches : sans effet.
- **wordsPerDay 1→3→1→3 dans la journée** : mêmes éléments à chaque retour, 3 distincts (`perDay1to3to1IsStable`). Après 3→1, `dailyMots` garde 3 clés, donc le quiz porte sur les 3 : ils ont bien été montrés ce jour-là.
- **Clés et « | »** : clés de citation préfixées `q:`, aucune collision avec un mot ; aucun mot ni texte de citation ne contient « | » ; pas de mot ni de citation en double, aucun champ obligatoire vide. Risque latent seulement : une future citation contenant « | » casserait `dailyMots` (split) et ferait re-tirer à chaque appel ; un contrôle dans `datasetIsClean` suffirait.
- **words.json mal formé** : `Words.load` lèverait (MainActivity et receiver ; la chaîne d'alarmes s'arrêterait car `scheduleNext` suit `show`). Asset livré et vérifié par `datasetIsClean` : pas un cas d'exécution. `Quotes.load` est protégé.
- **Réserve de tirage / file vide** : `ifEmpty { words }` et `randomOrNull ?: pool.first()` couvrent les cas limites ; cooldowns déjà simulés sur 3 ans par les tests existants.
- **Styles** : identifiants inconnus dans le pool ou suggérés par le mot → repli sur tout le catalogue (`unknownStyleIdsFallBack`) ; pool réduit à des polices d'affichage pour une citation → SERIF (test existant) ; `getIdentifier` = 0 → SERIF.
- **Heure d'été (printemps)** : 02:30 inexistant → 03:30, pas de double déclenchement (`springForwardGapIsSafe`). Le cas « exactement maintenant » donne demain (test existant). TIME_SET/TIMEZONE_CHANGED reprogramment. Un receiver `exported="false"` reçoit quand même les diffusions système.
- **END/SHOW concurrents** : un seul PendingIntent par action (UPDATE_CURRENT) ; un END périmé est ignoré grâce à `wallpaperEndAt` ; END après un retrait forcé → sans effet.
- **Reboot pendant l'affichage** : purge si expiré, sinon END reprogrammé et suivi relancé (BOOT exempté des restrictions FGS) : tests existants.
- **Ids de notification** : 1..3 (mots), 50 (quiz), 100 (service) ; codes de requête des PendingIntent 1..3 (MainActivity) et 50 (QuizActivity) : pas de chevauchement ; `show()` n'annule que 1..3.
- **UnlockService** : compteur en prefs (survit au redémarrage START_STICKY) ; `wallpaperEndAt == 0` → arrêt ; `USER_PRESENT` est une diffusion système protégée, donc pas besoin de `RECEIVER_EXPORTED` en API 34+.
- **Quiz** : moins de 4 mots au catalogue → options distinctes, réponse présente (`quizWithAFewWordsInTheCatalog`) ; double enregistrement bloqué par `lastQuizDay` (y compris après rotation de l'écran de score) ; `passedQuizzes` lit sa valeur par défaut avant que `perfectQuizzes` bouge (test existant) ; un second essai le même jour n'est pas compté (choix documenté).
- **Déblocages / Collection** : les cases verrouillées n'ont pas d'écouteur ; `Glose.choice` revient aux valeurs par défaut pour un id non débloqué (prefs restaurées incohérentes) ; l'annonce n'a lieu qu'une fois (`announced`).
- **Pose du soir** : correcte de 21 h à minuit une fois débloquée ; seulement recalculée dans `onResume` (appli laissée ouverte à 21 h : pose de jour jusqu'au prochain retour, cosmétique) ; après minuit retour à la pose choisie (c'est ce que dit « à partir de 21 h »).
- **Spinners** : le `onItemSelected` de l'initialisation est sans effet (per_day protégé, mode/unlocks/text_pos réécrivent la même valeur). Seule la durée écraserait une valeur absente de la liste (`indexOf` = −1 → 15 min), mais aucune version n'a stocké d'autre valeur (historique git vérifié).
- **Liens** : https seulement ; le thread Académie vérifie `isDestroyed` et le délai de 12 s, valide le préfixe de l'URL, et se rabat sur le CNRTL.
- **Minuit avec MainActivity ouverte** : étiquette « Mot du jour » et puces « déjà vus » restent celles de la veille jusqu'à la prochaine action (cosmétique, pas d'état incohérent stocké).
