# Definiday : audit de sécurité et score CRAP (commit 442fcaf, version 0.6.0)

Lecture seule sur le dépôt : tout a été fait dans le worktree jetable `audits2/wt_sec`. Les seuls ajouts au worktree sont le plugin JaCoCo et un test sonde `AuditProbeTest`, qui ne sont pas commités.
Les références `fichier:ligne` portent sur 442fcaf. Les deux statuts possibles :
- CONFIRMÉ : vérifié dans le code, par une mesure, par un test sonde Robolectric ou dans la documentation officielle ;
- PLAUSIBLE : le scénario repose sur l'hypothèse indiquée.

Doc consultée le 2026-11-06 :
- docs.github.com, *Using secrets in GitHub Actions* : « secrets are not passed to the runner when a workflow is triggered from a forked repository » ; « Secrets are not available to workflows triggered by Dependabot events ».
- docs.github.com, *Security hardening* : « Any user with write access to your repository has read access to all secrets » ; « Pin references to actions to a commit SHA » ; pour une valeur transformée (Base64…), « register the new value as a secret too », car « automatic redaction is not guaranteed » ; « A workflow job cannot access environment secrets until approval is granted by a reviewer ».
- docs.gradle.org, `Test.environment` : « Defaults to the environment of this process ».
- developer.android.com, *Auto Backup* : `fullBackupContent` s'applique jusqu'à Android 11 et `dataExtractionRules` à partir d'Android 12 pour targetSdk ≥ 31. Une section absente (`<device-transfer>`, etc.) signifie « fully enabled ». Avec `allowBackup="false"`, certains constructeurs gardent quand même le transfert D2D.

---

## Partie 1 : sécurité

**Bilan** : 0 critique, 0 haute, 3 moyennes, 9 basses, 8 infos.
- Anciens constats corrigés : #1 et #3 en entier, #2 sur l'essentiel.
- Anciens constats partiellement corrigés : #5, #7, #9 et #10.
- Anciens constats non corrigés : #4 (gravité relevée), #6 et #8 (étendu).

### 1.1 Statut des 17 constats de l'audit 6e93968

| # | Ancienne gravité | Statut à 442fcaf | Preuve |
|---|---|---|---|
| 1 | moyenne | **CORRIGÉ** (CONFIRMÉ) | `wallpaperId`, `wallpaperEndAt` et `unlocksSeen` sont passés dans `wallpaper_state.xml` (Prefs.kt:10, 32-41). Ce fichier est exclu de `<cloud-backup>` et de `<device-transfer>` (data_extraction_rules.xml:4-9), ainsi que de `full-backup-content` (backup_rules.xml:3), le tout déclaré dans AndroidManifest.xml:13-14. C'est conforme à la doc. |
| 2 | moyenne | **CORRIGÉ en grande partie** (CONFIRMÉ) | Keystore externe lu depuis une propriété ou une variable d'environnement (app/build.gradle.kts:7-15, 27-39). Le build échoue si le keystore est présent sans mot de passe. Aucun secret dans l'historique git (recherche de `*.jks`, `*.keystore`, `storePassword`, `BEGIN` sur toutes les branches : rien). Restent les constats N6 et N7 ci-dessous : repli silencieux sur la clé debug, et empreinte du certificat non publiée. |
| 3 | moyenne | **CORRIGÉ** (CONFIRMÉ) | `permissions: contents: read` (build.yml:3-4) et `persist-credentials: false` (build.yml:10). |
| 4 | basse | **NON CORRIGÉ, gravité relevée à moyenne** | Voir N3. |
| 5 | basse | **PARTIEL** | Corrigés : `distributionSha256Sum` correspond bien à la valeur officielle de 8.14.3, et `validateDistributionUrl=true`. Le jar du wrapper reste celui d'origine (SHA-256 `7d3a4ac4…6172`, mesuré, inchangé depuis 4dfff47). Il manque toujours une étape de validation du wrapper en CI. Voir N8. |
| 6 | basse | **NON CORRIGÉ** | Pas de `gradle/verification-metadata.xml`. L'impact augmente, puisque le job Gradle détient maintenant la clé de signature (N1). |
| 7 | basse | **PARTIEL** | Une PR venant d'un fork produit un APK signé avec la clé debug, donc reconnaissable au certificat. En revanche, chaque push sur n'importe quelle branche du dépôt (dont les branches `claude/*` des agents) produit un APK signé avec la clé **release** à partir de code non relu. Voir N2. |
| 8 | basse | **NON CORRIGÉ, étendu** | Aucune valeur restaurée n'est bornée. Le test sonde le CONFIRME : avec `wordsPerDay=0`, `Words.today()` puis `.first()` lève NoSuchElementException. Nouvelles clés restaurables : `quizHour`/`quizMinute` (avec `quizHour=25`, `onReceive(BOOT_COMPLETED)` lève DateTimeException, le receiver de démarrage plante), plus les clés de N4. |
| 9 | basse | **PARTIEL** | `open()` n'accepte plus que `https` et ajoute `CATEGORY_BROWSABLE` (MainActivity.kt:225-233). Il n'y a pas de liste blanche d'hôtes. `merge_words.py` et `merge_quotes.py` ne valident toujours pas `source`, et seuls les tests imposent `https://fr.(wiktionary\|wikipedia).org/` et `https://fr.wikiquote.org/wiki/` (DefinidayTest.kt:32, 67). Mesuré : 801/801 sources de mots et 450/450 sources de citations sont conformes. |
| 10 | basse | **PARTIEL** | Ajouts : garde `!isDestroyed` et délai global de 12 s (MainActivity.kt:248). Toujours présents : `readText()` sans limite (l.244), aucun `disconnect()`, et un tap répété lance un thread de plus à chaque fois. |
| 11 | info | inchangé, rien à corriger (CONFIRMÉ) | Préfixe `https://www.dictionnaire-academie.fr/` avec le `/` final (l.245). |
| 12 | info | inchangé (CONFIRMÉ) | UnlockService n'est pas exporté et `USER_PRESENT` est une diffusion protégée. |
| 13 | info | inchangé, plus le nouveau PendingIntent du quiz (CONFIRMÉ) | Voir I1. |
| 14 | info | inchangé, plus les citations (CONFIRMÉ) | Mesuré : 0 caractère bidi ou de contrôle dans les 1343 mots et les 450 citations. Tout le texte passe par `TextView.text`, sans HTML. |
| 15 | info | inchangé (CONFIRMÉ) | Aucune permission ajoutée. |
| 16 | info | inchangé (CONFIRMÉ) | La notification du quiz n'expose que le nombre de mots du jour. |
| 17 | info | inchangé (CONFIRMÉ) | `pull_request`, et non `pull_request_target` : un fork n'a ni secrets ni jeton en écriture (doc GitHub). Aucun secret dans le dépôt. |

### 1.2 Constats actuels (nouveaux et résiduels), du plus grave au moins grave

| # | Gravité | Statut | Fichier:ligne | Scénario concret | Correctif minimal |
|---|---|---|---|---|---|
| N1 | **moyenne** | Exposition CONFIRMÉE (code et doc Gradle) ; exploitation PLAUSIBLE (suppose une dépendance de test compromise) | .github/workflows/build.yml:14-24 ; app/build.gradle.kts:47, 54-57 | Une seule commande Gradle lance `:app:testDebugUnitTest` **et** `:app:assembleRelease` avec `DEFINIDAY_KEYSTORE_PASSWORD`, `DEFINIDAY_KEY_PASSWORD` et `DEFINIDAY_KEYSTORE` (le chemin du `.jks` décodé, via `GITHUB_ENV`) dans l'environnement. La JVM de test hérite de cet environnement (doc Gradle). Elle exécute Robolectric, ses dépendances transitives (bouncycastle, guava, asm…) et les jars `android-all` que **Robolectric télécharge à l'exécution** ; or `verification-metadata.xml` ne couvrirait même pas ces jars. Un jar compromis lit le mot de passe et `$DEFINIDAY_KEYSTORE`, puis les exfiltre (les runners ont un accès réseau sortant). L'attaquant peut alors signer une « mise à jour » Definiday qui s'installe par-dessus l'appli, avec ses données et ses droits SET_WALLPAPER et notifications. La clé ne se révoque pas. De plus, le `.jks` reste sur le disque jusqu'à la fin du job, y compris pendant `upload-artifact`. | Séparer les étapes : (1) `./gradlew --no-daemon :app:testDebugUnitTest` **sans** aucun secret ; (2) décodage du keystore, puis `./gradlew --no-daemon :app:assembleRelease` avec les secrets ; (3) `rm -f "$RUNNER_TEMP/definiday.jks"` dans une étape `if: always()` placée juste après. |
| N2 | **moyenne** | CONFIRMÉ (workflow et doc GitHub) | .github/workflows/build.yml:2, 15, 21-24 | Le déclencheur `on: [push, pull_request, …]` n'a ni filtre de branche ni `environment`. Les secrets de signature sont donc servis à **chaque push sur n'importe quelle branche** et à chaque PR interne au dépôt. La doc dit : « Any user with write access … has read access to all secrets ». Des sessions d'agents poussent régulièrement des branches `claude/*` sur ce dépôt. Une session détournée par injection de prompt, ou un collaborateur, modifie `build.yml` dans sa branche (`echo "$KEYSTORE_BASE64" \| base64 -d \| base64 -w0 \| rev`) : une valeur transformée n'est pas masquée (doc). Elle récupère ainsi la clé, sans fusion ni relecture. | Mettre la signature dans un job `environment: release` et déplacer les 4 secrets en *environment secrets*, avec une règle de déploiement limitée à `main` et aux tags `v*` (et un relecteur obligatoire si l'on veut). Les autres branches compilent une version signée avec la clé debug. |
| N3 (ex-#4) | **moyenne** (relevée de basse) | CONFIRMÉ | build.yml:9, 11, 25 | `actions/checkout@v4`, `actions/setup-java@v4` et `actions/upload-artifact@v4` sont référencées par des tags qui peuvent bouger. **setup-java fournit le JDK qui exécute Gradle** dans le même job que les secrets : un tag réécrit (même schéma que tj-actions en mars 2025) installe un `java` piégé ou un script d'init Gradle, puis lit les secrets à l'étape suivante. `upload-artifact` tourne pendant que le `.jks` est encore sur le disque. Le risque est atténué : ces actions appartiennent à GitHub. | Épingler les 3 actions par SHA complet (`@<sha40> # v4.x.y`), puis ajouter `.github/dependabot.yml` (écosystème `github-actions`) pour les mises à jour. |
| N4 | basse | CONFIRMÉ (test sonde Robolectric) | Glose.kt:22-27, 34 ; Prefs.kt:97-108 ; MainActivity.kt:110 ; DailyReceiver.kt:70-74 | `glosePose` est restauré (cloud ou D2D) sans contrôle de type. `Glose.choice` accepte tout id présent dans `unlocked` ou dans les ids de base (`assis`, `tigre`, `bordeaux` sont toujours débloqués). Ensuite `Glose.pose` prend `byId[id]?.res`, qui vaut **0** pour un pelage ou une pelote, et non `null`. Résultat : avec `glosePose="tigre"`, ou `"nuit"` débloqué, `getDrawable(0)` lève Resources.NotFoundException dans `MainActivity.onResume`, donc à chaque lancement, ainsi que dans la notification du quiz. L'interface ne produit jamais ces valeurs. Il faut un fichier de sauvegarde falsifié, un appareil source contrôlé ou le root : même vecteur que #8. Effet : déni de service local jusqu'à l'effacement des données, aucune fuite. | `fun pose(c) = Unlocks.byId[c[POSE]]?.takeIf { it.kind == Unlocks.Kind.POSE }?.res ?: R.drawable.glose_assis`. Dans `choice()`, `pick` doit aussi vérifier `Unlocks.byId[id]?.kind == kind`. |
| N5 (ex-#8) | basse | CONFIRMÉ (test sonde) | Prefs.kt:16, 19, 43, 64, 67, 126 ; Words.kt:60 ; DailyReceiver.kt:22-25, 52, 61 | Les prefs restaurées ne sont pas bornées. `wordsPerDay=0` fait planter MainActivity et SHOW. `wordsPerDay=10⁶` provoque un ANR dans la boucle `while`. `hour=25` ou `quizHour=25` lèvent DateTimeException dans `scheduleNext`/`scheduleQuiz`, appelés depuis `onCreate` **et** depuis `BOOT_COMPLETED` : plus aucune alarme après un redémarrage. Une valeur non Long dans `history.xml` lève ClassCastException. `adb restore` est exclu (targetSdk ≥ 31, appli non débogable). | Borner à la lecture : `getInt("wordsPerDay",1).coerceIn(1,3)`, `hour`/`quizHour` `.coerceIn(0,23)`, `minute`/`quizMinute` `.coerceIn(0,59)`, `mode` dans 0..2 et `unlocks` dans 0..3. Pour l'historique : `sp.all.mapNotNull { (k,v) -> (v as? Long)?.let { k to it } }.toMap()`. |
| N6 | basse | PLAUSIBLE (suppose un secret supprimé, renommé ou expiré) | app/build.gradle.kts:13-15, 39 ; build.yml:17 | Sans `DEFINIDAY_KEYSTORE`, l'APK release est signé avec la clé debug, et seul un `logger.warn` le signale. Si le secret disparaît sur `main` (rotation ratée, renommage), le CI publie sans erreur un artefact « definiday » qui porte un certificat debug aléatoire. Le propriétaire l'installe et doit désinstaller (historique perdu), ou bien un APK tiers devient indiscernable. | Dans l'étape de décodage : `[ "$GITHUB_REF" = refs/heads/main ] && [ -z "$KEYSTORE_BASE64" ] && { echo "::error::keystore secret missing"; exit 1; }`. |
| N7 | basse | CONFIRMÉ | README.md:41-80 ; .graphene/nodes/release-signing.md (empreinte tronquée `7dba7d1c…b7fffd`) | L'empreinte SHA-256 complète du certificat release n'est publiée nulle part. Un utilisateur ou un testeur ne peut donc pas distinguer l'APK officiel d'un APK repackagé ou signé debug, alors que c'était l'objectif du correctif #2. | Ajouter au README la ligne `apksigner verify --print-certs` attendue, avec l'empreinte SHA-256 complète. |
| N8 (ex-#5) | basse | CONFIRMÉ | gradle/wrapper/gradle-wrapper.jar ; build.yml:20 | Une PR qui remplace `gradle-wrapper.jar` (diff binaire illisible) s'exécute en CI. Venant d'une branche du dépôt, elle s'exécute **avec les secrets** (N2) et contourne en plus `distributionSha256Sum`, puisque c'est le jar qui fait cette vérification. | Ajouter l'étape `gradle/actions/wrapper-validation@<sha>` avant tout `./gradlew`. |
| N9 (ex-#6) | basse | CONFIRMÉ | settings.gradle.kts:2, 6-8 | Plugins et dépendances sont résolus sans vérification de somme de contrôle, sur `google()`, le miroir googleapis de Central, `mavenCentral()` et `gradlePluginPortal()`. Un artefact substitué s'exécute dans Gradle, qui détient la clé (N1). | `./gradlew --write-verification-metadata sha256 help :app:assembleRelease :app:testDebugUnitTest`, puis commit de `gradle/verification-metadata.xml`. |
| N10 (ex-#7) | basse | CONFIRMÉ | build.yml:2, 25-30 | Chaque push sur n'importe quelle branche (`claude/*` comprises) publie un APK signé avec la clé **officielle** à partir de code non relu. Quiconque récupère cet artefact obtient un APK qui s'installe comme mise à jour légitime sur le téléphone du propriétaire. | Le correctif de N2 suffit : seules `main` et les tags signent avec la clé release. |
| N11 (ex-#9/#10) | basse | CONFIRMÉ | MainActivity.kt:156, 166, 225-233, 236-249 ; tools/merge_words.py:25 ; tools/merge_quotes.py | Résidu des anciens #9 et #10. Le seul contrôle sur l'hôte des liens est le test unitaire. Un lot `tools/*/*.json` piégé (`https://<site>` vérifié en App Link par une autre appli) ouvrirait cette appli si le test était assoupli. Côté Académie : lecture sans limite de la réponse et un thread par tap. Impact faible : contenu embarqué, action déclenchée par l'utilisateur, `runCatching` en place. | Dans `open()` : `uri.host in setOf("fr.wiktionary.org","fr.wikipedia.org","fr.wikiquote.org","www.cnrtl.fr","www.dictionnaire-academie.fr")`. Faire le même contrôle dans les deux scripts de fusion. Côté Académie, lire au plus 64 Kio et ignorer les taps tant qu'une requête est en cours. |
| N12 | basse | CONFIRMÉ | design/mascotte/package.json:13 ; build.js:7, 10 ; android/make_android.js:5 | Chaîne d'approvisionnement des scripts Node de la mascotte : `paper` en `^0.12.18`, sans `package-lock.json` versionné ; Playwright chargé depuis `$PLAYWRIGHT` ou depuis le `require` global, sans version. Ces scripts tournent uniquement sur le poste du développeur ou de l'agent, pas en CI. Un `paper` 0.12.x compromis exécuterait du code sur ce poste au `npm install` ou au rendu (`new Function` dans Chromium). La sortie, des VectorDrawable XML versionnés d'environ 180 Ko, est **inerte** : pas de code, seulement des chemins et des attributs de thème. Mesuré : aucune URL dans `res/` hors des espaces de noms android et aapt. | Commiter `package-lock.json`, utiliser `npm ci` et épingler `"paper": "0.12.18"`. Préciser dans le README de la mascotte la version de Playwright utilisée. |
| I1 | info | CONFIRMÉ | AndroidManifest.xml:28-29 ; DailyReceiver.kt:68, 148-151 | `QuizActivity` et `CollectionActivity` ont `exported="false"` et ne lisent aucun extra. Le PendingIntent du quiz (requestCode 50) est explicite et `FLAG_IMMUTABLE`, distinct des codes 1 à 3 des mots. Seule MainActivity est exportée ; elle lit seulement `mot`, comparé à la liste du jour. Rien d'injectable. | Rien |
| I2 | info | CONFIRMÉ (lecture et test sonde) | QuizActivity.kt:122-130 ; Unlocks.kt:43-46 ; Prefs.kt:70-95 | **Triche sur les déblocages.** Les compteurs (`quizzes`, `perfectQuizzes`, `passedQuizzes`, `streak`) et `unlocked`/`announced` sont dans les prefs sauvegardées. C'est voulu : la collection suit l'utilisateur sur un nouveau téléphone. Moyens de tricher : (a) avancer l'horloge système d'un jour, puis refaire le quiz : `Quiz.record` compte une fois par `LocalDate.now()`, la série augmente, et 30 changements de date débloquent `nuit` ; (b) le root ou une sauvegarde falsifiée : avec des compteurs à 1000, le test sonde débloque 18/18 items. **Impact purement cosmétique** : aucun serveur, aucun classement, aucun achat, aucun contenu protégé. `Unlocks.update` n'enlève jamais rien et les ids inconnus sont ignorés par `byId`, mais voir N4 pour `glosePose`. | Rien ; une vérification côté serveur serait disproportionnée |
| I3 | info | CONFIRMÉ | build.yml:13-19 | La gestion des secrets est correcte sur ces points : le secret passe par `env:` et n'est pas interpolé dans `run:` (pas d'injection shell) ; `printf '%s'`, aucun écho ; le mot de passe est masqué ; le message du `require` (build.gradle.kts:11) n'affiche que le chemin ; `$RUNNER_TEMP` disparaît avec le runner hébergé ; les forks et Dependabot ne reçoivent aucun secret (doc). | Rien |
| I4 | info | CONFIRMÉ | app/build.gradle.kts:28-39 ; .gitignore | `storePassword` et `keyPassword` sont lus via `providers` et ne sont jamais écrits dans le dépôt. `*.jks`, `*.keystore` et `local.properties` sont ignorés par git. `keyPassword` vaut par défaut `storePassword`, ce qui est cohérent avec PKCS12. `isMinifyEnabled=true`. Release non débogable. | Rien |
| I5 | info | CONFIRMÉ (mesure) | assets/words.json ; citations.json ; styles.json | Parsing des assets : `org.json` sur des fichiers embarqués dans l'APK. Une entrée malformée provoquerait un plantage au chargement, sans RCE ; ce chargement est couvert par les tests. Les polices sont résolues par `getIdentifier(fichier,"font")`, restreint aux ressources de l'APK, et le test l.139 vérifie que chacune existe. `Quotes.load` est protégé par `runCatching`. | Rien |
| I6 | info | PLAUSIBLE | data_extraction_rules.xml | La doc Android 16 QPR2 ajoute `<cross-platform-transfer platform="ios">`, et une section absente vaut « fully enabled ». Un transfert vers iOS exige cependant une appli iOS correspondante (`bundleId`/`teamId`), qui n'existe pas : rien ne sort. | Facultatif : ajouter la même exclusion `wallpaper_state.xml` le jour où une version iOS existera |
| I7 | info | CONFIRMÉ | DailyReceiver.kt:64-81 | `quizReminder` gonfle deux VectorDrawables d'environ 180 Ko et dessine des bitmaps de 512×256 et 192×192 sur le thread principal du receiver. Pas de déni de service exploitable, car l'alarme est interne et le PendingIntent immuable. À surveiller seulement sur les téléphones lents (limite de 10 s d'`onReceive`). | Rien côté sécurité |
| I8 | info | CONFIRMÉ | — | Écartés comme dans l'audit précédent : StrandHogg et tapjacking (aucun écran sensible), Parcelable piégé dans l'Intent de lancement (seule l'instance de l'attaquant plante), confusion de dépendances via `google()`. | — |

**Les 3 plus importants** : N1, N2 et N3. Ils touchent tous les trois la **clé de signature release**, seul actif irréversible du projet : une clé volée permet de pousser des mises à jour malveillantes, et elle ne se révoque pas. Leur correctif commun tient en environ 15 lignes de `build.yml` :
- un job de tests sans secrets ;
- un job de signature dans un `environment: release` limité à `main` et aux tags ;
- les actions épinglées par SHA ;
- la suppression du `.jks` en fin d'étape.

---

## Partie 2 : score CRAP

**CRAP : `:app`, `fr.definiday.*`, commit 442fcaf.**
- Couverture : branche, avec repli sur la ligne pour les méthodes sans branche.
- Source : `audits2/jacoco.xml`, produit par `./gradlew --no-daemon :app:testDebugUnitTest :app:jacocoReport` dans le worktree : 26 tests (DefinidayTest 18 et GloseTest 8), 0 échec.
- Configuration : plugin `jacoco`, `isIncludeNoLocationClasses = true`, `excludes = listOf("jdk.internal.*")`, classes `build/tmp/kotlin-classes/debug` sans `R`.
- Complexité : JaCoCo (bytecode, v(G) = B − D + 1).
- Script : crap.py du skill, identique à celui de l'audit précédent (vérifié avec `cmp`).

**285 méthodes, 0 > 30 (0 %), CRAPload 0. Verdict projet : ok.**

### 2.1 Couverture globale et comparaison

| Indicateur | 6e93968 (audit 1) | 442fcaf | Écart |
|---|---:|---:|---:|
| Lignes | 263/332 = 79,2 % | **596/704 = 84,7 %** | +5,5 pts |
| Branches | 81/147 = 55,1 % | **223/324 = 68,8 %** | +13,7 pts |
| Instructions | 77,9 % | 6445/7626 = 84,5 % | +6,6 pts |
| Méthodes (lambdas comprises) | 137 | 285 | ×2,1 |
| CC max | 9 (`DailyReceiver.onReceive`) | 13 (`Styles.of`) | +4 |
| Méthodes CRAP > 30 | 1 (0,7 %) | **0** | −1 |
| CRAPload | 7 | 0 | −7 |
| Tests | 11 | 26 | +15 |

`DailyReceiver.onReceive` passe de CRAP 50,5 (CC 9, 20 %) à **8,3** (CC 7, 70 %) grâce aux tests de redémarrage ajoutés après l'audit 1. Ces tests suivaient exactement la recommandation de l'audit.

Par fichier, en lignes et en branches :

| Fichier | Lignes | Branches |
|---|---|---|
| QuizActivity | 83/84 | 33/34 |
| Unlocks | 32/32 | 18/18 |
| Words | 38/38 | 28/34 |
| Styles | 25/25 | 28/32 |
| DailyReceiver | 104/105 | 35/48 |
| WordCard | 36/36 | 14/22 |
| CollectionActivity | 64/67 | 15/18 |
| Glose | 29/30 | 19/24 |
| Prefs | 61/71 | 6/8 |
| Quotes | 7/8 | 8/16 |
| **MainActivity** | **109/182** | **16/62** |
| **UnlockService** | **8/26** | **3/8** |

`UnlockService` et son receiver sont les deux seules classes jamais chargées.

### 2.2 Méthodes au-dessus du seuil (CRAP > 30)

Aucune.

**À la frontière** : `MainActivity.warnIfHurried` (MainActivity.kt:120) a un CRAP de **exactement 30,0** (CC 5, couverture 0 %). Le script compte « > 30 », mais certains outils comptent « ≥ 30 » et la classeraient crappy. C'est la seule méthode à cette limite : le bouton « Mot au hasard » (`onCreate$lambda$1`, l.43) n'est jamais cliqué par les tests.

### 2.3 Tableau par CRAP décroissant (69 méthodes de CC ≥ 2 ; les 216 méthodes de CC 1 ont un CRAP ≤ 2, dont 47 non couvertes : setters de Prefs, lambdas de clic, cycle de vie d'UnlockService)

| # | CRAP | CC | Cov | Fonction | Emplacement |
|---:|---:|---:|---:|---|---|
| 1 | 30 | 5 | 0 % | `MainActivity.warnIfHurried` | app/src/main/java/fr/definiday/MainActivity.kt:120 |
| 2 | 20 | 4 | 0 % | `MainActivity.pool$lambda$0$2` | app/src/main/java/fr/definiday/MainActivity.kt:200 |
| 3 | 20 | 4 | 0 % | `MainActivity.openAcademie$lambda$0` | app/src/main/java/fr/definiday/MainActivity.kt:237 |
| 4 | 19.1 | 9 | 50 % | `MainActivity.show` | app/src/main/java/fr/definiday/MainActivity.kt:135 |
| 5 | 13.3 | 13 | 88 % | `Styles.of` | app/src/main/java/fr/definiday/Styles.kt:42 |
| 6 | 12 | 3 | 0 % | `MainActivity.pool$lambda$0` | app/src/main/java/fr/definiday/MainActivity.kt:196 |
| 7 | 12 | 3 | 0 % | `MainActivity.openAcademie$lambda$0$1` | app/src/main/java/fr/definiday/MainActivity.kt:248 |
| 8 | 8.8 | 7 | 67 % | `WordCard.lines` | app/src/main/java/fr/definiday/WordCard.kt:19 |
| 9 | 8.3 | 7 | 70 % | `DailyReceiver.onReceive` | app/src/main/java/fr/definiday/DailyReceiver.kt:19 |
| 10 | 8.1 | 5 | 50 % | `WordCard.render` | app/src/main/java/fr/definiday/WordCard.kt:43 |
| 11 | 8.1 | 5 | 50 % | `Quote.getSignature` | app/src/main/java/fr/definiday/Quotes.kt:9 |
| 12 | 7.2 | 7 | 83 % | `Words.today` | app/src/main/java/fr/definiday/Words.kt:53 |
| 13 | 6.8 | 3 | 25 % | `CollectionActivity.cell$lambda$0$4` | app/src/main/java/fr/definiday/CollectionActivity.kt:82 |
| 14 | 6.3 | 6 | 80 % | `DailyReceiver$Companion.texts` | app/src/main/java/fr/definiday/DailyReceiver.kt:138 |
| 15 | 6 | 2 | 0 % | `Quotes.slow` | app/src/main/java/fr/definiday/Quotes.kt:25 |
| 16 | 6 | 2 | 0 % | `UnlockService$receiver$1.onReceive` | app/src/main/java/fr/definiday/UnlockService.kt:20 |
| 17 | 6 | 6 | 90 % | `QuizActivity.finishQuiz` | app/src/main/java/fr/definiday/QuizActivity.kt:62 |
| 18 | 6 | 4 | 50 % | `Glose.play` | app/src/main/java/fr/definiday/Glose.kt:57 |
| 19 | 6 | 4 | 50 % | `MainActivity.onCreate` | app/src/main/java/fr/definiday/MainActivity.kt:35 |
| 20 | 6 | 2 | 0 % | `MainActivity.open` | app/src/main/java/fr/definiday/MainActivity.kt:226 |
| 21 | 6 | 2 | 0 % | `MainActivity.onCreate$lambda$12` | app/src/main/java/fr/definiday/MainActivity.kt:78 |
| 22 | 6 | 2 | 0 % | `UnlockService.onCreate` | app/src/main/java/fr/definiday/UnlockService.kt:24 |
| 23 | 5.4 | 5 | 75 % | `DailyReceiver$Companion.clearWallpaper` | app/src/main/java/fr/definiday/DailyReceiver.kt:122 |
| 24 | 5 | 5 | 100 % | `CollectionActivity.cell` | app/src/main/java/fr/definiday/CollectionActivity.kt:66 |
| 25 | 5 | 5 | 100 % | `Unlocks.hint` | app/src/main/java/fr/definiday/Unlocks.kt:50 |
| 26 | 5 | 5 | 100 % | `Quiz.record` | app/src/main/java/fr/definiday/QuizActivity.kt:123 |
| 27 | 5 | 5 | 88 % | `Words.nextRandom` | app/src/main/java/fr/definiday/Words.kt:47 |
| 28 | 4.6 | 4 | 67 % | `Words.seenToday` | app/src/main/java/fr/definiday/Words.kt:77 |
| 29 | 4.1 | 3 | 50 % | `Quotes.load` | app/src/main/java/fr/definiday/Quotes.kt:21 |
| 30 | 4.1 | 3 | 50 % | `Glose.stop` | app/src/main/java/fr/definiday/Glose.kt:62 |
| 31 | 4.1 | 3 | 50 % | `DailyReceiver$Companion.quizReminder` | app/src/main/java/fr/definiday/DailyReceiver.kt:65 |
| 32 | 4.1 | 3 | 50 % | `DailyReceiver$Companion.set` | app/src/main/java/fr/definiday/DailyReceiver.kt:89 |
| 33 | 4 | 4 | 100 % | `Unlocks.value` | app/src/main/java/fr/definiday/Unlocks.kt:36 |
| 34 | 4 | 4 | 100 % | `Unlocks.update` | app/src/main/java/fr/definiday/Unlocks.kt:44 |
| 35 | 4 | 4 | 100 % | `QuizActivity.next$lambda$1$0` | app/src/main/java/fr/definiday/QuizActivity.kt:48 |
| 36 | 4 | 4 | 100 % | `DailyReceiver$Companion.show` | app/src/main/java/fr/definiday/DailyReceiver.kt:99 |
| 37 | 3.1 | 3 | 75 % | `UnlockService$Companion.countUnlock` | app/src/main/java/fr/definiday/UnlockService.kt:56 |
| 38 | 3 | 3 | 100 % | `CollectionActivity.refresh` | app/src/main/java/fr/definiday/CollectionActivity.kt:90 |
| 39 | 3 | 3 | 100 % | `Styles.parse` | app/src/main/java/fr/definiday/Styles.kt:24 |
| 40 | 3 | 3 | 100 % | `Glose.mainPose` | app/src/main/java/fr/definiday/Glose.kt:38 |
| 41 | 3 | 3 | 100 % | `Glose.styles` | app/src/main/java/fr/definiday/Glose.kt:41 |
| 42 | 3 | 3 | 100 % | `Words.isQuoteSlot` | app/src/main/java/fr/definiday/Words.kt:43 |
| 43 | 2.5 | 2 | 50 % | `Prefs.getMode` | app/src/main/java/fr/definiday/Prefs.kt:26 |
| 44 | 2.5 | 2 | 50 % | `Prefs.getDailyMots` | app/src/main/java/fr/definiday/Prefs.kt:50 |
| 45 | 2.5 | 2 | 50 % | `Styles.load` | app/src/main/java/fr/definiday/Styles.kt:34 |
| 46 | 2.5 | 2 | 50 % | `MainActivity.onCreate$lambda$7` | app/src/main/java/fr/definiday/MainActivity.kt:65 |
| 47 | 2.5 | 2 | 50 % | `MainActivity.onCreate$showUnlocks` | app/src/main/java/fr/definiday/MainActivity.kt:68 |
| 48 | 2.5 | 2 | 50 % | `MainActivity.pool$refresh` | app/src/main/java/fr/definiday/MainActivity.kt:193 |
| 49 | 2.5 | 2 | 50 % | `DailyReceiver$Companion.scheduleNext` | app/src/main/java/fr/definiday/DailyReceiver.kt:48 |
| 50 | 2.5 | 2 | 50 % | `DailyReceiver$Companion.scheduleQuiz` | app/src/main/java/fr/definiday/DailyReceiver.kt:57 |
| 51 | 2.5 | 2 | 50 % | `Words.load` | app/src/main/java/fr/definiday/Words.kt:85 |
| 52 | 2 | 2 | 100 % | `CollectionActivity.onCreate` | app/src/main/java/fr/definiday/CollectionActivity.kt:27 |
| 53 | 2 | 2 | 100 % | `Unlocks.of` | app/src/main/java/fr/definiday/Unlocks.kt:40 |
| 54 | 2 | 2 | 100 % | `WordCard.render$total` | app/src/main/java/fr/definiday/WordCard.kt:57 |
| 55 | 2 | 2 | 100 % | `Quotes.parse` | app/src/main/java/fr/definiday/Quotes.kt:14 |
| 56 | 2 | 2 | 100 % | `Prefs.getUnlocked` | app/src/main/java/fr/definiday/Prefs.kt:91 |
| 57 | 2 | 2 | 100 % | `Prefs.getAnnounced` | app/src/main/java/fr/definiday/Prefs.kt:94 |
| 58 | 2 | 2 | 100 % | `Styles.pick` | app/src/main/java/fr/definiday/Styles.kt:38 |
| 59 | 2 | 2 | 100 % | `QuizActivity.text` | app/src/main/java/fr/definiday/QuizActivity.kt:30 |
| 60 | 2 | 2 | 100 % | `QuizActivity.next` | app/src/main/java/fr/definiday/QuizActivity.kt:36 |
| 61 | 2 | 2 | 100 % | `QuizActivity.announce` | app/src/main/java/fr/definiday/QuizActivity.kt:83 |
| 62 | 2 | 2 | 100 % | `Glose.pose` | app/src/main/java/fr/definiday/Glose.kt:34 |
| 63 | 2 | 2 | 100 % | `Glose.mine` | app/src/main/java/fr/definiday/Glose.kt:51 |
| 64 | 2 | 2 | 100 % | `Glose.choice$pick` | app/src/main/java/fr/definiday/Glose.kt:24 |
| 65 | 2 | 2 | 100 % | `MainActivity.label` | app/src/main/java/fr/definiday/MainActivity.kt:222 |
| 66 | 2 | 2 | 100 % | `DailyReceiver$Companion.nextTrigger` | app/src/main/java/fr/definiday/DailyReceiver.kt:43 |
| 67 | 2 | 2 | 100 % | `Quiz.build` | app/src/main/java/fr/definiday/QuizActivity.kt:111 |
| 68 | 2 | 2 | 100 % | `Quiz.passed` | app/src/main/java/fr/definiday/QuizActivity.kt:119 |
| 69 | 2 | 2 | 100 % | `Words.parse` | app/src/main/java/fr/definiday/Words.kt:24 |

### 2.4 Recommandations minimales, de la plus rentable à la moins rentable

1. **`MainActivity.warnIfHurried` (CRAP 30,0, CC 5, couverture 0 %) : un test Robolectric, pas de refactor.** Le scénario est le suivant : `Robolectric.buildActivity(MainActivity::class.java).setup()`, puis 4 clics sur `R.id.new_word`, puis `assertNotNull(ShadowAlertDialog.getLatestAlertDialog())`. Ensuite `ShadowAlertDialog.reset()`, un 5ᵉ clic, et `assertNull(...)` pour vérifier qu'il n'y a qu'un avertissement par jour. Ce test couvre aussi `onCreate$lambda$1` (bouton hasard) et le chemin `DailyReceiver.show(ctx, listOf(w))`. CRAP attendu : 5.
2. **Test de non-régression des prefs falsifiées (sécurité N4 et N5)** : ce test transforme le sondage `AuditProbeTest` du worktree en vrai test. Il doit passer `glosePose="tigre"`, `wordsPerDay=0` et `quizHour=25`, puis vérifier qu'il n'y a **aucune exception** dans `Glose.mine(ctx, Glose.mainPose(p))`, `Words.today(ctx)` et `onReceive(BOOT_COMPLETED)`. Il échoue aujourd'hui, ce qui est voulu, et passera une fois les `coerceIn` et le contrôle `kind == POSE` ajoutés. Il fait aussi monter la couverture de `Glose.choice` et `Glose.pose`.
3. **`MainActivity.show` (CRAP 19,1, CC 9, 50 %)** : sous le seuil, mais c'est la méthode la plus lourde de l'écran principal. Un test suffit pour la branche `Quote` : mettre `dailyDay = aujourd'hui` et `dailyMots = listOf(quote.key)`, lancer l'activité, puis vérifier que `R.id.academie` est `GONE` et que `R.id.cnrtl` affiche « Source (Wikiquote) ». Pas de découpage.
4. **`Styles.of` (CC 13, 88 %, CRAP 13,3)** : la couverture est bonne, mais la CC dépasse le budget de 10. Ne la découper que si la fonction doit évoluer : extraire `fun fonts(cat, p, item): List<Police>` (lignes 45-49 : filtre du pool, `ifEmpty`, exclusion des polices décoratives pour les citations). La CC descend alors à environ 7 + 6, chaque morceau testable seul.
5. **Rien d'autre n'est nécessaire.** Les méthodes non testées restantes ont toutes une CC ≤ 4 et un CRAP ≤ 20 :
   - `pool$lambda$0$2` (CRAP 20) ;
   - `openAcademie$lambda$0` (CRAP 20 ; réseau, ne pas la tester en unitaire) ;
   - `UnlockService.onCreate` et son receiver (CRAP 6 chacun). Si l'on veut un filet : `Robolectric.buildService(UnlockService::class.java).create()`, puis `ctx.sendBroadcast(Intent(Intent.ACTION_USER_PRESENT))`, `shadowOf(Looper.getMainLooper()).idle()`, et vérifier `Prefs.unlocksSeen == 1`. C'est le seul moyen de charger cette classe en test.

### 2.5 Limites

- JaCoCo mesure la CC sur le bytecode. Un `when` sur une `String` compile en `hashCode()` + `equals()`, et les lambdas Kotlin comptent comme des méthodes à part, sans rattachement à leur fonction englobante. Ne comparer qu'avec d'autres mesures JaCoCo, ce qui est le cas de l'audit 1.
- La hausse de 137 à 285 méthodes vient surtout des lambdas de l'UI programmatique (QuizActivity, CollectionActivity) et des accesseurs de Prefs. Le pourcentage de méthodes crappy n'est donc pas pondéré par la taille.
- `Comparisons.kt` (code inline de la stdlib) figure dans le rapport, sans méthode associée.
- La couverture est celle de Robolectric (GraphicsMode NATIVE). Elle ne dit rien du comportement réel sur un appareil (fond d'écran, horloge du constructeur).
- CRAP ne mesure ni la qualité des assertions ni la duplication.

Fichiers bruts : `audits2/jacoco.xml`, `audits2/crap.json` (285 méthodes), `audits2/crap_table.md` (sortie complète de crap.py), `audits2/cc2_table.md`.
