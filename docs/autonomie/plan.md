# Plan de nuit autonome (8 nov. 2026, 00h10 → 09h00 Paris)

Point de départ : `08a954d`. Mandat du propriétaire : amélioration continue sur 6 axes (graphisme, fonctions, UI/UX, son, sécurité, code CRAP), en autonomie, plan validé par un agent expert, pas par lui.

## Organisation

- **Agent maître** (session principale) : tient le backlog, choisit les lots, lance les équipes, fusionne, teste, relit, pousse, journalise. Seul à toucher `.graphene/`, `docs/autonomie/`, `versionCode`.
- **Équipes** (sous-agents en worktree isolé, en arrière-plan, 3 à 4 en parallèle maximum) :
  - **SEC** sécurité + qualité (skills `security-review`, `crap-metric`, `ponytail-audit`) ;
  - **UX** UI/UX + graphisme (skills `design:design-critique`, `design:accessibility-review`, `design:ux-copy`, captures Robolectric `renderDemoImages`) ;
  - **FON** fonctions (backlog produit, bugs ouverts des audits) ;
  - **SON** son (cohérence des 7 sons, déclencheurs, accessibilité, taille).
- **Relecteur indépendant** : un agent `engineering:code-review` (frais, sans mes conclusions) relit le diff de chaque itération avant push.

## Boucle d'itération (environ 2 h, 4 itérations prévues)

1. **Tri** : mettre à jour `docs/autonomie/backlog.md` (items priorisés P1/P2/P3 par axe, fichiers touchés, critère d'acceptation). Itération 1 : audits frais en lecture seule sur HEAD par axe (les audits du 6 nov. sont en partie corrigés, on revérifie au lieu de recopier).
2. **Choix** : 1 lot par équipe, ensembles de fichiers disjoints autant que possible (sinon séquencer). Taille cible : livrable en moins de 90 min d'agent.
3. **Exécution** : chaque équipe en worktree (`git reset --hard <HEAD branche>`, `local.properties` avec `sdk.dir=/opt/android-sdk`), implémente, ajoute ou ajuste les tests Robolectric, lance `./gradlew --offline :app:testDebugUnitTest`, commit dans sa branche worktree, écrit son compte rendu incrémental dans le scratchpad (`team_<axe>_<iter>.md`).
4. **Intégration** (maître) : merge des branches worktree, résolution de conflits, suite complète de tests + `assembleRelease`, relecture indépendante, corrections, commit, Graphene à jour, push sur `claude/android-daily-word-definitions-8ngyty` (le job CI `test` doit être vert).
5. **Journal** : `docs/autonomie/journal.md` (fait, testé, reporté, pourquoi).

Battement de cœur : tâche planifiée horaire dans cette session (reprend le fil si rien ne m'a réveillé), tâche unique à 09h00 pour clore. Les fins d'agents réveillent le maître en temps normal.

## Garde-fous (non négociables)

- Pas de merge sur `main`, pas de release, pas de tag, pas de PR, pas de force push. Tout reste sur la branche de travail ; le propriétaire décide au réveil.
- Ne rien casser des décisions prises (Graphene `decision/*`) : style Correspondance + rigueur Édition, pas de Glose sur l'écran de verrouillage, sons désactivés par défaut, vérif hebdo de mise à jour désactivée par défaut, garde-fou Samsung, cooldowns 270/120 j, politique des sources, noms d'objets sans accord, onboarding notif seule, etc.
- Profil jamais perdu : toute nouvelle clé de prefs ajoutée à `Profile.SETTINGS` (ou justifiée hors liste), import tolérant aux valeurs inconnues. Pas de changement d'`applicationId`, de clé, de schéma de prefs incompatible.
- Pas de nouvelle dépendance, pas d'AndroidX, minSdk 26.
- Sécurité : pas de secret dans le code ou les logs, l'updater garde hôte de confiance + SHA-256 + même signature ; pas d'élargissement de permissions sans nécessité.
- Données : aucun nouveau contenu lexical ou citation sans source conforme à `decision/sources-policy`.
- Toute question qui relève du propriétaire (goût, produit, coût) va dans « Décisions en attente » du rapport, pas dans le code. Une variante peut être préparée derrière un réglage désactivé seulement si elle est petite.
- Tests : jamais désactiver ni affaiblir un test pour passer au vert. Typo française (U+202F avant ;:!?, apostrophe typographique) dans les nouvelles chaînes.
- Coût : 3 à 4 agents en parallèle maximum, pas plus d'une vingtaine d'agents sur la nuit.

## Priorités de départ (à confirmer par les audits de l'itération 1)

- FON : interface de renommage de Glose (décision prise, non implémentée), texte de fin de quiz, items ouverts des audits du 6 nov. encore valides.
- UX : écarts restants au style Correspondance, accessibilité (TalkBack quiz, contrastes, états focus), cohérence des écrans récents (Mes mots, Détail, Mises à jour).
- SEC : revue de l'updater et du profil (import JSON hostile), receivers exportés, PendingIntent, CI ; fonctions CRAP > 30 et couverture des branches.
- SON : revue de l'ensemble sonore, volume/durée, respect du mode silencieux, déclencheurs manquants ou en trop.
- GRA : icône, fonds de l'appli (grain, teinte de palette), finitions Postal.

## Fin (09h00)

`docs/autonomie/rapport_nuit.md` : ce qui a été livré par axe, preuves (tests, CI), ce qui est reporté, décisions en attente, comment tester sur le téléphone (APK artefact `definiday-test` du dernier run CI de la branche, installé à côté en `fr.definiday.dev`). Tâches planifiées supprimées.

## Amendements de l'agent expert (validé « avec changements », tous retenus)

1. **Fichiers partagés réservés au maître** : `strings.xml`, `colors.xml`, `themes.xml`, `Prefs.kt`, `Profile.kt`, `SettingsActivity.kt`, `AndroidManifest.xml`. Les équipes proposent leurs ajouts (chaînes, clés de prefs) dans leur compte rendu ; le maître les applique à l'intégration. Exception : une équipe à qui le lot attribue explicitement l'un de ces fichiers, seule ce tour-là.
2. **Machine (4 CPU, 15 Go, Gradle -Xmx3g)** : un seul Gradle à la fois via `flock <scratchpad>/gradle.lock`. Les équipes ne lancent que des tests ciblés (`--tests`) ; le maître lance la suite complète.
3. **Budget agents** : 22 au maximum sur la nuit. Itération 1 = 3 audits + 3 équipes ; ensuite 3 équipes maximum par itération, plus 1 relecteur.
4. **Intégration lot par lot** : chaque lot est rebasé sur le HEAD intégré, avec la suite complète après chaque fusion. Un commit par lot, préfixé par l'axe (`sec:`, `ux:`, `gra:`, `fon:`, `son:`, `crap:`), réversible seul.
5. **Test de profil doré** : une fixture d'export v0.14.0 doit s'importer à l'identique sur HEAD, clés inconnues comprises. Le test passe à chaque intégration. Aucun changement de `Profile.VERSION` sans test de relecture rétrocompatible.
6. **Zones gelées** : `Update.kt`, la signature, les permissions et composants exportés du manifeste, le job `release` de `build.yml`. Une vraie faille trouvée là va dans le rapport, avec un correctif sur une branche à part.
7. **Goût = proposition** : icône, fonds et sons nouveaux ou ré-encodés sont livrés en captures avant/après ou en fichiers audio, dans « Décisions en attente », sans être fusionnés. Seuls les correctifs (contraste, accessibilité, volume, bug de déclencheur) sont fusionnés.
8. **Taille** : environ 300 lignes modifiées par lot. Pas de nouvel écran ni d'Activity, sauf le renommage de Glose (décidé).
9. **Heure limite** : aucun nouveau lot après 07h15, intégration close à 08h15. Si la CI est rouge après 08h00 : revert du dernier lot, pas de correction en avant.
10. **Retour arrière** : SHA de départ `08a954d` et la commande de reset dans le rapport. Pas de changement de `versionCode` ni de `versionName`.
11. **Worktrees anciens** : les 20 dossiers sous `.claude/worktrees/` ne sont pas supprimés ; ils sont listés dans le rapport.

Suggestions retenues :
- le rapport s'ouvre sur un tableau (lot, axe, commit, risque, test sur téléphone, commande de revert) ;
- le relecteur reçoit les critères d'acceptation et les décisions Graphene, pas seulement le diff ;
- GRA est confié à l'équipe UX ;
- un correctif CRAP est un refactor sans changement de comportement, avec ses tests commités avant.
