# Audit SEC itération 1 (sécurité + CRAP), HEAD c843268

Lecture seule. Worktree `agent-a7baa014512ef62e2`. Références `fichier:ligne` sur c843268.
Les audits du 6 nov. (`docs/audits/securite_crap.md` sur 442fcaf, 0.6.0) sont revérifiés, pas recopiés.

## 0. Contexte vérifié
- Prefs.kt : lectures déjà bornées (`int(k,d,range)`, `runCatching` sur le type) : N4/N5 de l'audit du 6 nov. en grande partie corrigés côté Prefs (à vérifier côté Glose).
- Profile.SETTINGS (36 clés) = exactement les clés de Prefs (vérifié une à une, `wallpaper` compris).
- Manifeste : seule MainActivity est exportée (LAUNCHER) ; DailyReceiver, UpdateReceiver, Widget, UnlockService, ShareProvider ont `exported=false`.

## 1. Sécurité

**Bilan** : 0 critique, 0 haute, 2 moyennes (S1, S2 : CI, zone gelée, rapport seulement), 6 basses (S3-S8), 10 infos (S9, I1-I9).
Aucun `Log.*`, `println` ni `printStackTrace` dans `app/src/main` (grep). Aucun secret dans le code.

### 1.1 Statut des constats de l'audit du 6 nov. (442fcaf)

| # | Statut sur c843268 | Preuve |
|---|---|---|
| N1 (secrets dans la JVM de test) | **CORRIGÉ** | build.yml:7-21 : job `test` sans aucun secret ; job `release` séparé, `assembleRelease` seul (l.43). |
| N2 (secrets servis à toute branche) | **PARTIEL, reste ouvert** | `environment: release` (l.28) et filtre main/tags (l.26), MAIS le commentaire l.23 dit que les secrets restent des *repository secrets* (choix du propriétaire) : voir S1. |
| N3 (actions non épinglées) | **CORRIGÉ** | Actions épinglées par SHA (l.10, 12, 13, 16, 33, 35, 52) + `.github/dependabot.yml` (github-actions + gradle). |
| N4 (`glosePose` = pelage → `getDrawable(0)`) | **CORRIGÉ** | Glose.kt:39-40 (`fits` vérifie le slot), Glose.kt:51 (`takeIf { kind.slot == POSE }`, repli `glose_assis`). |
| N5 (prefs restaurées non bornées) | **CORRIGÉ** | Prefs.kt:14-18 (`int(k,d,range)` + `runCatching` sur le type), bornes hour/minute/quizHour/quizMinute/duration/mode/unlocks/wordsPerDay ; History.all (Prefs.kt:177) et Memory.entries (Prefs.kt:193-197) filtrent le type. |
| N6 (repli silencieux sur la clé debug) | **CORRIGÉ** | build.yml:40 échoue si le secret manque. |
| N7 (empreinte du certificat non publiée) | **NON CORRIGÉ** | README.md:65 et 91 disent seulement « pas `CN=Android Debug` ». Voir S6. |
| N8 (validation du wrapper) | **CORRIGÉ** | build.yml:12 `gradle/actions/wrapper-validation` (job `test`, dont dépend `release`). |
| N9 (pas de `verification-metadata.xml`) | **NON CORRIGÉ** | `gradle/` ne contient que `wrapper`. Voir S5. |
| N10 (APK signé release depuis toute branche) | **CORRIGÉ pour les branches, résiduel pour les tags** | Voir S2. |
| N11 (liste blanche d'hôtes des liens) | **PARTIEL, inchangé** | DetailActivity.kt:86-94 `open()` : https + BROWSABLE, pas d'hôte. Les tests (DefinidayTest.kt:33, 68) restent le seul garde-fou. Académie : 64 Kio max (l.107-108), un appel à la fois (`isEnabled=false`, l.98). |
| N12 (npm mascotte) | non revérifié (poste local, hors appli) | — |
| I1 (composants exportés) | **ÉVOLUÉ** | Voir S4 : MainActivity accepte maintenant toute clé du catalogue (correctif du bug 5). |

### 1.2 Constats actuels

| # | Gravité | Statut | Fichier:ligne | Scénario | Correctif minimal |
|---|---|---|---|---|---|
| S1 | **moyenne** (zone gelée : rapport) | CONFIRMÉ (lecture) ; exploitation PLAUSIBLE (suppose un jeton de push capable de modifier `.github/workflows/`) | .github/workflows/build.yml:23, 38, 46-48 | Les 4 secrets de signature sont des *repository secrets* : lisibles par **tout workflow de toute branche** du dépôt, quel que soit `environment:`. Une branche `claude/*` (session d'agent détournée par injection de prompt) ou un collaborateur pousse un `build.yml` modifié qui imprime `secrets.DEFINIDAY_KEYSTORE_BASE64` transformé (`| rev`, non masqué). La clé release ne se révoque pas : l'attaquant publie ensuite une « mise à jour » acceptée par Android **et** par l'updater (Update.kt vérifie hôte + SHA-256 ; la vraie garantie est la signature). | Action propriétaire, sans code : déplacer les 4 secrets dans l'environnement `release` (Settings › Environments › release), règle de déploiement « main + tags `v*` », puis **supprimer** les repository secrets. build.yml référence déjà `environment: release`. |
| S2 | **moyenne/basse** (zone gelée : rapport) | CONFIRMÉ (lecture) | build.yml:26 | `startsWith(github.ref, 'refs/tags/')` : tout tag poussé, sur n'importe quel commit non relu, produit l'artefact `definiday-release` signé avec la clé officielle (sans release publiée, l.59). Cet APK s'installe par-dessus l'appli du propriétaire. | Ruleset GitHub qui réserve la création de tags au propriétaire ; ou (branche à part) restreindre à `refs/tags/v*` + vérifier `git merge-base --is-ancestor $GITHUB_SHA origin/main`. |
| S3 | basse | CONFIRMÉ (comportement documenté d'Android, issue tracker 180526528) ; impact PLAUSIBLE selon le fournisseur | Profile.kt:69 | `openOutputStream(uri, "w")` : sur plusieurs fournisseurs SAF (ExternalStorageProvider en Android 10+, certains fournisseurs cloud), `"w"` **ne tronque pas**. Si l'utilisateur écrase un ancien export plus long, le fichier garde la fin de l'ancien JSON → toast « enregistré » mais fichier illisible à l'import. Contraire au garde-fou « Profil jamais perdu ». | `"wt"` au lieu de `"w"` ; en option relire le fichier écrit avec `Profile.read` avant de dire « enregistré ». |
| S4 | basse | CONFIRMÉ (lecture) | MainActivity.kt:48, 83, 94, 149 ; AndroidManifest.xml:30-33 | MainActivity est exportée (LAUNCHER) et lit l'extra `mot`. Depuis le correctif du bug 5, `find()` accepte **toute clé du catalogue**, puis `show()` appelle `Words.shown()` → l'historique la marque « vue aujourd'hui ». N'importe quelle appli, sans permission, peut injecter des mots dans l'historique (cooldowns 270/120 j faussés, quiz du soir rempli de mots jamais lus, « Mes mots » pollué). Pas de fuite ; intégrité, impact faible. | N'accepter l'extra que pour une clé déjà connue : today ∪ `History.all().keys` (une notification ou le widget ne portent que des items du jour ou déjà marqués vus). Test Robolectric : mot jamais vu en extra → carte = mot du jour, historique inchangé. |
| S5 | basse | CONFIRMÉ | settings.gradle.kts ; gradle/ | Ex-N9 : pas de `gradle/verification-metadata.xml` ; un artefact substitué (miroir googleapis, Central, plugin portal) s'exécute dans le job `release` qui détient la clé. Dependabot propose des versions, il ne vérifie pas les sommes. | `./gradlew --write-verification-metadata sha256 help :app:assembleRelease :app:testDebugUnitTest` (en ligne, branche à part) puis commit. Fichier généré de milliers de lignes, à régénérer à chaque PR Dependabot : décision propriétaire. |
| S6 | basse | CONFIRMÉ | README.md:65, 91 ; .graphene/nodes/release-signing.md (empreinte tronquée `7dba7d1c…b7fffd`) | Ex-N7 : empreinte SHA-256 complète du certificat release publiée nulle part ; impossible de distinguer un APK officiel d'un APK repackagé. L'updater n'en dépend pas. | Une ligne README avec l'empreinte complète (valeur à fournir par le propriétaire). |
| S7 | basse | CONFIRMÉ (lecture) | SettingsActivity.kt:125-127 ; Profile.kt:68-78 | `Profile.export` et `Profile.read` tournent sur le thread UI (`onActivityResult`). Un document Drive/OneDrive (distant, jusqu'à 4 Mio) bloque le thread principal le temps du réseau → ANR possible ; l'utilisateur tue l'appli en plein export. Robustesse plus que sécurité. | Thread + `runOnUiThread` (même motif que `updates()`), boutons désactivés pendant l'opération (~15 lignes). |
| S8 | basse | CONFIRMÉ (lecture) | Profile.kt:77 | Un export retouché sous Windows (« UTF-8 avec BOM ») commence par `﻿` : `JSONObject` le rejette → « profil illisible » alors que le contenu est bon. | `parse(String(bytes).removePrefix("﻿"))` + 1 cas de test. |
| S9 | info (zone gelée) | CONFIRMÉ | build.yml:28-31, 43, 58-83 | Le job `release` a `contents: write` pendant le build Gradle. `persist-credentials: false` et `GH_TOKEN` exposé seulement à l'étape de publication : un plugin Gradle piégé n'a pas le jeton en variable d'environnement. Défense en profondeur : publier dans un 3ᵉ job (`needs: release`, `contents: write`) qui télécharge l'artefact sans lancer Gradle. | Facultatif. |
| I1 | info | CONFIRMÉ | Update.kt (gelé) | Updater revu, rien à corriger : hôte exact `github.com` + chemin du dépôt, sans `..`, userinfo ni port (l.39-42) ; HTTPS seul, redirection https→http refusée et revérifiée (l.63) ; 64 Kio pour le JSON et 64 Mio pour l'APK, vérifiés en flux (l.65, 71) ; SHA-256 sur 64 hex, fichier effacé s'il diffère (l.48, 89) ; paquet de l'APK = paquet courant (l.130) ; `newer` refuse un versionCode égal ou inférieur (l.50) ; PendingIntent **mutable** (exigé par PackageInstaller) mais explicite vers un receiver non exporté (l.138, manifeste l.68) ; notes en texte brut. Remarques mineures : pas de contrôle que le versionCode de l'APK = `update.json` (Android refuse un downgrade de toute façon) ; `autoCheck` retient l'Activity jusqu'à 30 s dans un thread (fuite bornée). | Rien |
| I2 | info | CONFIRMÉ | Profile.kt:28-124 | Import hostile : format + version vérifiés ; chaque fichier attendu présent ; clés de `definiday` limitées à `SETTINGS` (36 clés = celles de Prefs) : **aucune injection de clé** dans les réglages ; `history` : toute clé en `long`, dans son propre fichier ; `memoire` : `m:*`, `favoris`, `reviews*` ; type exact sinon rejet total ; Int hors bornes, Double, texte > 10 000, ensemble > 100 000, fichier > 4 Mio rejetés ; lecture bornée. Valeurs du bon type mais hors plage (hour = 99, glosePose = "x") acceptées puis neutralisées par Prefs/Glose. JSON très imbriqué → StackOverflowError attrapée par `runCatching` (Throwable). Import seulement via ACTION_OPEN_DOCUMENT + confirmation (SettingsActivity non exportée). Effets résiduels d'un profil hostile : collection débloquée (triche cosmétique acceptée le 6 nov.) et jusqu'à 100 000 entrées d'historique (`History.all()` copié à chaque `Words.today` : lenteur choisie par l'utilisateur). Branche `"float"` jamais utilisée (aucune clé float) : code mort, voir § 3. | Rien (robustesse : S3, S7, S8) |
| I3 | info | CONFIRMÉ | AndroidManifest.xml | Seule MainActivity est exportée. DailyReceiver (actions système protégées), UpdateReceiver, Widget (AppWidgetService passe en uid système), UnlockService, ShareProvider : non exportés. `registerReceiver(USER_PRESENT)` sans drapeau (UnlockService.kt:27) toléré en targetSdk 34+ (diffusion système protégée). Pas de deep link ni de filtre VIEW/SEND. | Rien |
| I4 | info | CONFIRMÉ | DailyReceiver.kt:79-80, 104-107, 168-171 ; Widget.kt:96-98 | PendingIntent `FLAG_IMMUTABLE` et explicites, codes distincts (1-3 mots, 50 quiz, 100 widget, `action.hashCode()` alarmes), sauf PackageInstaller (I1). QuizActivity borne son extra jour à `today-1..today` (QuizActivity.kt:45). | Rien |
| I5 | info | CONFIRMÉ | Share.kt:17-50 ; manifeste l.50-54 | Partage : provider maison non exporté, `grantUriPermissions`, lecture seule quel que soit `mode`, un seul fichier servi quel que soit le chemin (pas de traversée), insert/update/delete inopérants. `cache/definiday.png` réécrit à chaque partage (une appli qui garde l'autorisation verrait la carte suivante, publique). | Rien |
| I6 | info | CONFIRMÉ | DetailActivity.kt:86-115 | Liens https + BROWSABLE ; Académie bornée (64 Kio, préfixe d'URL avec slash final, 12 s, un appel à la fois). Aucune WebView. | Rien (N11 : liste blanche facultative) |
| I7 | info | CONFIRMÉ | res/xml/backup_rules.xml, data_extraction_rules.xml | `wallpaper_state.xml` exclu du cloud, du D2D et de full-backup ; le reste est sauvegardé exprès (profil). | Rien |
| I8 | info | CONFIRMÉ | AndroidManifest.xml:4-13 | Permissions toutes justifiées (REQUEST_INSTALL_PACKAGES pour l'updater, SCHEDULE_EXACT_ALARM pour le bug 4, FGS special use avec sous-type). | Rien |
| I9 | info | CONFIRMÉ | build.yml:63-83 | Publication : `versionName` filtré `[0-9A-Za-z.-]` avant de servir de tag ; `notes` passées par `jq --arg` et `--notes "$notes"` : pas d'injection shell depuis le message de commit. | Rien |

**À retenir (sécurité)** : le seul vrai risque restant est S1 (clé release lisible depuis toute branche tant que les secrets restent au niveau du dépôt) : 5 minutes du propriétaire dans les réglages GitHub, sans code. Côté appli, les correctifs utiles sont petits : S3 (`"wt"`), S4 (extra de MainActivity), S7-S8 (robustesse import/export).

## 2. CRAP

**CRAP : `:app`, `fr.definiday.*`, c843268** (couverture : branche, repli ligne pour les méthodes sans branche ; source : JaCoCo 0.8.13 ajouté **temporairement** au worktree, `:app:jacocoAudit` = `testDebugUnitTest` + rapport XML ; modification annulée par `git checkout`, rien de commité ; worktree propre).
Tests : **120, 0 échec, 0 ignoré**. Complexité : JaCoCo (bytecode). Script : `crap.py` du skill (`scratchpad/night/crap.py`), données brutes `scratchpad/night/sec_jacoco.xml` et `sec_crap.json`.

**692 méthodes (lambdas comprises), 3 > 30 (0,4 %), CRAPload 19. Verdict projet : ok (< 5 %).**

### 2.1 Couverture globale (comparée au 6 nov., 442fcaf)

| Indicateur | 442fcaf | c843268 |
|---|---:|---:|
| Lignes | 84,7 % | **95,0 %** (2090/2201) |
| Branches | 68,8 % | **81,4 %** (1265/1555) |
| Instructions | 84,5 % | 93,3 % |
| Méthodes | 285 | 692 (couvertes 600) |
| CRAP > 30 | 0 | **3** (toutes dans le flux de mise à jour) |
| Tests | 26 | 120 |

Branches par fichier, du moins couvert au plus couvert : DetailActivity 14/38 (37 %), **SettingsActivity 30/72 (42 %)**, Quotes 9/16, Sounds 9/16, Share 8/14 (56-57 %), **Update 65/106 (61 %)**, StylesActivity 77 %, WordCard 80 %, Words 81 %, Profile 82 %, Postal 83 %, MainActivity 84 %, Styles 84 %, Widget 85 %, DailyReceiver 86 %, UnlockService 86 %, QuizActivity 89 %, Onboarding 91 %, Collection 92 %, Mots 93 %, Prefs 93 %, Glose 94 %, Unlocks 100 %.

### 2.2 Méthodes CRAP > 20

| # | Fonction | CC | Cov (branche) | CRAP | Emplacement | Action |
|---:|---|---:|---:|---:|---|---|
| 1 | `Update.install$lambda$1` (thread : téléchargement, contrôle du paquet, session PackageInstaller) | 7 | 0 % | **56** | Update.kt:126 | tests (≥ 23 %) ; Update.kt gelé : **tests seulement** |
| 2 | `UpdateReceiver.onReceive` | 7 | 0 % | **56** | Update.kt:151 | tests (≥ 23 %) ; gelé : tests seulement |
| 3 | `SettingsActivity.updates` | 6 | 10 % | **32,2** | SettingsActivity.kt:155 | tests (≥ 13 %) |
| 4 | `QuizActivity.onCreate` | 23 | 82 % | 26,2 | QuizActivity.kt:42 | sous 30 ; CC > 15 : découpage conseillé |
| 5 | `Update.get` | 14 | 62 % | 25,2 | Update.kt:60 | sous 30 ; gelé |
| 6 | `Profile.value` | 20 | 76 % | 25,2 | Profile.kt:98 | sous 30 ; CC gonflée par `when` sur String (hashCode+equals en bytecode) |
| 7 | `QuizActivity.finishQuiz` | 20 | 92 % | 20,2 | QuizActivity.kt:218 | sous 30 ; CC > 15 |

À la limite (CRAP 20, CC 4, 0 %) : `openAcademie$lambda$0` (DetailActivity.kt:100, réseau, ne pas tester en unitaire). Autres CC > 15 bien couvertes : `OnboardingActivity.go` (CC 19, 94 %), `MainActivity.showSeen` (15, 75 %), `Widget.views` (15, 96 %).

Les 3 méthodes crappy forment **un seul chemin non testé : le bouton « Installer » de Réglages › Mises à jour et le retour de PackageInstaller**. C'est le chemin qui a été validé à la main sur le téléphone (0.13 → 0.14) mais qu'aucun test ne protège : une régression ne se verrait qu'à la prochaine release.

### 2.3 Propositions, de la plus rentable à la moins rentable

1. **Tests du flux d'installation (sans toucher Update.kt, gelé)** : `UpdateInstallTest` Robolectric.
   - `UpdateReceiver.onReceive` : 5 intents (`STATUS_PENDING_USER_ACTION` avec `EXTRA_INTENT` → `shadowOf(app).nextStartedActivity` porte `FLAG_ACTIVITY_NEW_TASK` ; `STATUS_SUCCESS` → aucun toast ; `FAILURE_ABORTED`, `FAILURE_CONFLICT`, `FAILURE_STORAGE`, autre → `ShadowToast.getTextOfLatestToast()` = la bonne chaîne). CC 7 → 100 %, CRAP 56 → 7.
   - `Update.install` : `shadowOf(packageManager).setCanRequestPackageInstalls(true)` (à vérifier dans Robolectric 4.17, sinon garder le cas « refus » déjà testé), `Update.open` remplacé par une fausse connexion qui sert des octets connus, puis attente du `status(_, true)` (boucle `shadowOf(mainLooper).idle()` bornée). Cas : hash correct → `update_installing` et une session créée (`ShadowPackageInstaller.allSessions`) ; hash faux → `update_bad_hash`, fichier effacé ; exception réseau → `update_failed`. CRAP 56 → ≤ 10.
   - Taille : ~90 lignes de test, 0 ligne de prod.
2. **`SettingsActivity.updates`** : 3 tests via la même couture `Update.open` : « Rechercher » → `update_none` / offre (`update_install` visible) / `update_unreachable` ; + l'offre reçue par `Update.EXTRA`. CRAP 32 → ~6. ~50 lignes de test. Couvre aussi `updates$offer` et ses lambdas (CRAP 12 chacune).
3. **`QuizActivity.onCreate` (CC 23)** : refactor sans changement de comportement, **après** tests de caractérisation (les tests de rotation existent déjà : `BugHuntTest.quizSurvivesRotation` ; ajouter un cas « extra jour hors bornes → aujourd'hui » et « jour déjà compté → done »). Découpage : `restore(s: Bundle)` / `fresh()` au lieu de 10 `s?.getX() ?: …` (chaque `?.`/`?:` compte 2 branches en bytecode). CC attendue ~6 + 4 + 8. ~40 lignes changées. Priorité basse : CRAP 26 < 30.
4. **`QuizActivity.finishQuiz` (CC 20, 92 %)** : extraire `endingMessages(passed, fresh, done)` (les 4 `if` de textes finaux) et le choix de l'image de Glose (`endingGlose`). Seulement si le lot FON « texte de fin de quiz » touche cette fonction : le faire dans le même lot, tests d'abord. Sinon ne rien faire.
5. Rien d'autre n'est utile : `Profile.value` et `Update.get` sont sous 30, bien couverts sur les cas d'erreur, et leur CC vient du bytecode (`when` sur String, `use {}` et `check`). Update.kt est gelé.

### 2.4 Limites
- CC JaCoCo sur bytecode : `?.`, `?:`, `use {}`, `when` sur String gonflent la CC ; les lambdas comptent comme méthodes à part. Comparer seulement à d'autres mesures JaCoCo.
- Couverture Robolectric (pas d'appareil) : le flux réel PackageInstaller, le fond d'écran et les alarmes exactes restent à vérifier sur le téléphone.
- CRAP ne mesure ni la qualité des assertions ni la duplication.

## 3. Sur-ingénierie et code mort (scan rapide, esprit ponytail)

Le code est déjà sobre (pas d'AndroidX, pas d'interface à une implémentation, coutures de test réduites à des `var` de fonction : `Update.open`, `Wall.lockId/lockLive`). Constats :

| # | Où | Quoi | Proposition |
|---|---|---|---|
| P1 | strings.xml:91 `tile_sample`, strings.xml:137 `settings_link` | Chaînes jamais référencées (grep code + res + manifeste). | Supprimer (strings.xml réservé au maître : à appliquer à l'intégration). 2 lignes. |
| P2 | WordCard.kt:88 et :127 | Dette `ponytail:` **échue** : « fixed name; read it from Prefs once Glose's name can be customized ». `Prefs.gloseName` existe déjà (Prefs.kt:150) et le renommage est le lot FON prioritaire : la carte du fond d'écran et la carte partagée signeront toujours « Glose ». | À faire dans le lot FON « renommage » : `Prefs(ctx).gloseName` au lieu de `"Glose"` (1 ligne + 1 assertion). |
| P3 | Profile.kt:55-56, 103, 116 | Type `"float"` géré à l'export, à la validation et à l'écriture, alors qu'aucune clé n'est float (`type()` ne renvoie jamais `"float"`). | Laisser : l'export générique reste tolérant si une clé float apparaît un jour ; le coût est 3 jetons. Pas d'action. |
| P4 | Words.kt:96-97, Styles.kt:45, Quotes.kt:21 | Assets re-parsés à chaque appel (`MainActivity.onResume` → `show` → `Styles.of` + `initialFace` + `showSeen` → `today` + `seenToday` : ~5 parsings par affichage). Dette déjà marquée `ponytail:` avec son seuil. | Rien tant que ça ne se voit pas ; sinon `by lazy` sur l'application. |
| P5 | drawable `glose_icone_dort` | Seule la variante `_vd` est utilisée ; le fichier est produit par `make_android.js`. | Rien côté appli (sortie générée) ; à la rigueur l'ôter du script. |
| P6 | QuizActivity.kt:265-266 | La carte « Noter » ouvre `market://details?id=fr.definiday` alors que l'appli est distribuée par GitHub (et en `fr.definiday.dev` pour les tests) : page Play introuvable. | Question produit pour FON / propriétaire (« Décisions en attente »), pas de sécurité. |

Pas de dépendance inutile (junit + robolectric seulement), pas de code mort de fonction (toutes les fonctions à 1 occurrence sont des `override`).

## 4. Propositions pour le maître (fichiers réservés)
- strings.xml : supprimer `tile_sample` et `settings_link` (P1).
- Profile.kt (S3, S8) et SettingsActivity.kt (S7) sont réservés au maître : le lot SEC proposé ci-dessous les demande explicitement pour ce tour.
- Aucune nouvelle clé de prefs proposée.

## Backlog proposé

- [P1] axe(sec) Secrets de signature au niveau de l'environnement `release` (action propriétaire, pas de code) — réglages GitHub ; build.yml inchangé — les 4 secrets `DEFINIDAY_*` n'existent plus qu'en *environment secrets* de `release` (règle main + tags `v*`) ; le dernier run `main` signe toujours avec le certificat 7dba7d1c… ; à mettre dans « Décisions en attente » — 0 ligne
- [P1] axe(crap) Tests du flux d'installation de mise à jour (Update.kt gelé, tests seulement) — app/src/test/java/fr/definiday/UpdateInstallTest.kt (nouveau) — `UpdateReceiver.onReceive` et `Update.install$lambda$1` passent de CRAP 56 à ≤ 10 (JaCoCo) ; 5 statuts PackageInstaller + 3 issues d'installation (installing / bad_hash / failed) assertés ; aucune ligne de Update.kt modifiée — ~90 lignes
- [P2] axe(sec) Export de profil qui tronque, import qui tolère le BOM — Profile.kt, ProfileTest.kt — `openOutputStream(uri, "wt")` ; un export écrit par-dessus un fichier plus long se relit à l'identique (test : fichier pré-rempli de 10 Kio puis export puis `Profile.read` non nul) ; un export préfixé de `﻿` s'importe ; tests existants verts — ~20 lignes
- [P2] axe(sec) MainActivity n'accepte l'extra `mot` que pour un item du jour ou déjà vu — MainActivity.kt, BugHuntTest.kt ou UiTest.kt — intent externe avec un mot jamais vu → carte = mot du jour et `History` inchangé ; notification d'un mot au hasard (bug 5) et widget toujours ouverts sur leur mot (tests existants verts) — ~25 lignes
- [P2] axe(crap) Tests de Réglages › Mises à jour — SettingsActivity (lecture seule), app/src/test/java/fr/definiday/UpdateTest.kt — `SettingsActivity.updates` CRAP 32 → < 10 ; cas « aucune », « nouvelle version » (bouton Installer visible), « injoignable », offre par `Update.EXTRA` ; via la couture `Update.open` — ~50 lignes
- [P2] axe(sec) Import/export de profil hors du thread UI — SettingsActivity.kt, ProfileTest.kt — `Profile.read`/`export` dans un thread, boutons désactivés pendant l'opération, toast et dialogue de confirmation sur le thread UI ; test `settingsExportThenRestoreAfterConfirmation` adapté (attente bornée), aucun comportement visible changé — ~30 lignes
- [P3] axe(sec) Tags : seule une version relue peut être signée (zone gelée : branche à part ou ruleset) — .github/workflows/build.yml (job release, gelé) ou ruleset GitHub — un tag poussé sur un commit absent de `main` ne produit pas d'artefact signé release (`git merge-base --is-ancestor`), ou création de tags réservée au propriétaire ; « Décisions en attente » — ~6 lignes
- [P3] axe(sec) Empreinte du certificat release dans le README — README.md — la sortie attendue de `apksigner verify --print-certs` (SHA-256 complet, fourni par le propriétaire) figure dans la section « Signature release » — ~3 lignes
- [P3] axe(sec) `gradle/verification-metadata.xml` (décision propriétaire : maintenance à chaque PR Dependabot) — gradle/verification-metadata.xml (généré), README — `./gradlew --offline :app:testDebugUnitTest :app:assembleRelease` passe avec la vérification ; CI verte — généré (milliers de lignes, hors budget « lignes écrites à la main ») ; à faire en ligne, pas en sandbox hors ligne
- [P3] axe(crap) Découper `QuizActivity.onCreate` (CC 23) sans changer le comportement — QuizActivity.kt, BugHuntTest.kt — tests de caractérisation commités avant (extra jour hors bornes, jour déjà compté, rotation) ; après refactor aucune méthode de QuizActivity n'a CC > 15 dans JaCoCo ; tous les tests verts — ~60 lignes
- [P3] axe(crap) Nettoyage : chaînes mortes + signature de la carte avec le nom de Glose — strings.xml (maître), WordCard.kt — `tile_sample` et `settings_link` supprimées ; la carte du fond d'écran et du partage signe `Prefs.gloseName` (à joindre au lot FON « renommage de Glose ») — ~6 lignes
- [P3] axe(sec) Publication de la release dans un job sans Gradle (zone gelée : branche à part) — .github/workflows/build.yml — le job qui a `contents: write` ne lance pas Gradle (télécharge l'artefact `definiday-release`) ; actionlint passe — ~20 lignes


