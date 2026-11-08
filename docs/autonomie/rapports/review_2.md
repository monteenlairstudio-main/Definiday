# Relecture indépendante 2 : `6a3a199..e4179a2` (app/, tools/, design/mascotte/*.js, design/mascotte/android)

Relecteur frais, lecture seule, sans Gradle. Chaque constat a été vérifié de bout en bout avec git, la lecture des fichiers et des scripts Python ou Node isolés dans `scratchpad/r2/`. Les pistes non confirmées sont écartées et listées en fin de document.

## Bloquant

### B1. `widget.xml` n’est plus du XML valide : la compilation des ressources échoue
- Où : `app/src/main/res/layout/widget.xml:66-67`, commit e4179a2, toujours présent à HEAD 62934f7.
- Le commit retire la ligne `android:textStyle="italic" />` du TextView `widget_nature`. Cette ligne portait aussi la fermeture `/>` de la balise. Le TextView reste donc ouvert, et `</LinearLayout>` se retrouve à l’intérieur.
- Vérification : `xml.dom.minidom` refuse le fichier (« not well-formed (invalid token): line 67, column 12 »). aapt2 le refusera aussi. `assembleDebug`, `assembleRelease` et `testDebugUnitTest` (qui fusionne les ressources) échouent, et la CI de la branche passe au rouge.
- Le second retrait (`widget_auteur`, ligne 98) est correct : `/>` y était sur la ligne suivante.
- Tous les autres XML modifiés dans la plage sont bien formés (vérification automatique).
- Correctif minimal : à la ligne 66, écrire `android:textSize="13sp" />`.

## Important

### I1. La nuit, le liseré de 8 calques d’accessoires est peint sur le corps de Glose (découpe perdue)
- Où : `design/mascotte/glose_lib.js:599-605` (commit 7834355) et `design/mascotte/android/svg2avd.js:134-140`.
- Calques générés concernés : `app/src/main/res/drawable/glose_assis_vd_grelot.xml`, `glose_ecrit_mot_vd_grelot.xml`, `glose_reveil_vd_grelot.xml`, `glose_soir_vd_grelot.xml`, `glose_reveil_vd_grelot_ruban.xml`, `glose_soir_vd_grelot_ruban.xml`, `glose_reveil_vd_noeud.xml`, `glose_soir_vd_noeud.xml`.
- Le mécanisme :
  1. Le liseré d’une pièce est découpé hors de la silhouette du corps par `clipIn(X(notIn(all), box), [s])`.
  2. Quand la pièce est entièrement posée sur le corps, le masque `X(notIn(all), box)` est vide. C’est le cas du grelot sur le poitrail, et du nœud et du grelot à ruban dans les poses couchées.
  3. Paper.js exporte ce masque en `<clipPath><path d=""/></clipPath>` (`exportCompoundPath` omet `d`).
  4. `svg2avd.js:134` : `shapeData` rend `null`, `.filter(Boolean)` retire la découpe, puis la ligne 140 aplatit le groupe devenu sans nom.
  5. Résultat : le chemin `?attr/glose<Porte>_f0e801` (remplissage plus trait de 33 unités) est émis **sans découpe**, en tête du groupe de la pièce.
- Vérification :
  - Aucun `<clip-path>` dans ces 8 fichiers, alors que les 22 autres pièces ont bien le leur.
  - Avec paper.js, j’ai fait l’union des aplats `gloseFur`/`gloseCream` des calques `_1`/`_2` de chaque pose (script `scratchpad/r2/geo.js`). Entre 78 et 100 % de la surface du liseré tombe dans la fourrure.
- Conséquence :
  - Le jour, rien ne se voit (`glose_halo` vaut `#00F0E8D8`).
  - La nuit, une pastille crème `#F0E8D8` déborde d’environ 16 unités autour du grelot, et autour du nœud dans les poses réveil et soir. Elle est peinte sur le pelage, quel qu’il soit.
  - C’est précisément ce que l’itération voulait éviter (glose_3.md : « rien n’est peint sur le chat »). La planche zoom de l’équipe ne montrait ni le grelot ni le nœud.
- Correctif minimal, à la racine, dans le convertisseur partagé : à `svg2avd.js`, quand un groupe a un `clip-path` qui ne donne aucun chemin, ne rien émettre (`if (cp && !clip) return;`). Une découpe vide ne montre rien, comme en SVG. On peut aussi, ou en plus, ajouter `if (masque.isEmpty()) continue;` dans la boucle `PIECE` de `glose_lib.js`. Il faut ensuite régénérer avec `make_android.js`.
- Garde de test à ajouter : tout chemin `_f0e801` d’un calque d’accessoire doit se trouver dans un groupe qui a une `<clip-path>`. GloseTest lit déjà les XML.

## Mineur

### M1. « N mots à revoir » change de sens sans décision du propriétaire consignée
- Où : `MotsActivity.kt:116-127` et `strings.xml` (`stat_fragile` devient `stat_due`), commit 18a6904.
- Avant, la ligne du registre comptait les mots ratés 2 fois ou plus. Elle compte maintenant les mots dus sur l’échelle de révision, sans plafond.
- Or `Quiz.due` met à l’échelle tout mot jamais répondu, dès J+2 après son affichage (« words seen before this version join the ladder too »). Pour un utilisateur ancien, le chiffre vaut donc presque « tous les mots vus » : la capture de l’équipe montrait 7 sur 9, et après un an ce serait plusieurs centaines. Le quiz n’en reprend que 2 par soir (`MAX_REVIEWS`). Le chiffre baisse donc très lentement, ce qui va à l’encontre du ton « Prenez le temps ».
- L’équipe l’a signalé comme « Choix à valider » (team_fon_E.md § 2), mais le journal ne le range pas dans les décisions en attente. Le garde-fou du plan dit pourtant : « Toute question qui relève du propriétaire (…) produit (…) va dans « Décisions en attente » ».
- Correctif : l’inscrire dans les décisions en attente du rapport. Si l’on veut un chiffre sobre en attendant, on peut ne compter que les mots déjà répondus au moins une fois (`entries[mot] != null`), à la fois dans le filtre et dans le registre.

### M2. Après une rotation pendant la lecture d’un profil, les boutons Exporter et Importer sont de nouveau actifs
- Où : `SettingsActivity.kt:162-171`. C’est le reliquat de la correction M2 de la relecture 1.
- La nouvelle instance n’hérite pas de `isEnabled = false` : cet état n’est pas sauvegardé. L’utilisateur peut donc relancer un export ou un import alors que le premier fil tourne encore. Les deux résultats arrivent alors sur l’écran au premier plan : deux toasts, ou deux dialogues « Remplacer » empilés.
- Aucune perte de données, puisque chaque fil lit ou écrit son propre URI.
- Correctif minimal : un compteur `busy` dans le `companion`, incrémenté au départ du fil et décrémenté dans `profileDone`. Dans `onResume`, appeler `profileButtons(busy == 0)`.

### M3. FontsTest compare des noms de fichiers à `Styles.NO_NNBSP`, qui contient des ids
- Où : `FontsTest.kt:57`, avec `Styles.kt:24-28`.
- `Styles.fine` décide selon `st.police?.id`. Le test, lui, décide selon `f.nameWithoutExtension`. Pour la première fois, les deux divergent : l’id `libre_baskerville` est conservé (prefs, words.json), mais le fichier s’appelle `definiday_serif`.
- Aujourd’hui le résultat reste juste, car aucun des deux n’est dans la liste. Mais si quelqu’un remettait `libre_baskerville` dans `NO_NNBSP`, ou ajoutait une face renommée sans U+202F, le test ne verrait pas l’écart entre la police livrée et la règle appliquée au texte.
- Correctif : dans le test, passer de l’id au fichier avec `Styles.load(ctx).polices` (`fichier`), et vérifier que `id in NO_NNBSP` équivaut à « le fichier n’a pas U+202F ».

## Correctifs de la relecture 1 : vérifiés

- **I1 (clés de citations), f3e19b5 : correct et complet.**
  - L’ensemble des clés de 08a954d est identique à celui de HEAD, avec `cle ?: texte`, sur 450 citations : aucune perdue, aucune nouvelle.
  - Les sources `tools/citations/*.json` portent déjà U+202F dans `cle`. `merge_quotes.py` rejoué en mémoire reproduit `citations.json` à l’octet près.
  - Aucun autre code ne reconstruit une clé avec `"q:" + texte` : `Unlocks.counts` compte les préfixes, et `MainActivity.find` passe par `key`.
- **M1 (test de rotation, son), ee51487 : le test peut maintenant échouer.** Les sons demandés avant la rotation sont joués puis effacés. Après la rotation, ils sont tous notifiés comme chargés. Comme `Sounds.play` joue directement un son `ready`, un STAMP rejoué serait vu.
- **M2 (résultat de profil après rotation), ee51487 : corrigé.**
  - `resumed` est posé dans `onResume` et effacé dans `onPause` seulement si c’est bien l’instance courante. Il n’y a donc pas de fuite au-delà du premier plan.
  - La recréation (pause, destroy, create, resume) se fait dans un seul message du looper, donc le `runOnUiThread` ne peut pas s’intercaler entre les deux instances.
  - Le test `profileReadEndingAfterARotationReachesTheNewScreen` échouait avec l’ancien code : le dialogue s’ouvrait sur l’ancienne instance.
  - Reliquat : voir M2 ci-dessus.
- **M3 (`quiz_early`) : texte exact.** Il correspond au comportement de `quizReminder`, qui demande un mot vu ce jour-là.
- **M4 (TalkBack) : corrigé.** `quiz_answer_picked` n’est utilisée que pour `j == picked && !good` (`QuizActivity.kt:175`).
- **M5 (Zilla), d934ab5 : corrigé.** Le fichier est identique, octet pour octet (sha256), à 08a954d, qui est antérieur à 7a45d30. Il ne contient pas U+202F, `zilla_slab` est dans `NO_NNBSP`, et `fonts_nnbsp.py` le laisse de côté (`TRADEMARK`).

## Vérifié sans constat

- **Découpage de QuizActivity (0048e13), comparé ligne à ligne.** Le comportement n’a pas changé :
  - `start()` et `restore()` reprennent exactement les branches `s == null` et `s != null` de l’ancien `onCreate`. Avec un Bundle, `getInt("picked")` valait déjà 0 et non -1, comme avant.
  - `"mots"` est toujours écrit par `onSaveInstanceState`, donc `orEmpty()` ne change rien.
  - `scoreScreen`, `record` et `signature` gardent l’ordre des vues, l’enregistrement unique (`won ?:`), `rated` posé avant `rate()`, et le son.
  - Les tests de caractérisation (4b25a40) ont été commités avant le découpage, et peuvent échouer.
- **Mes mots.**
  - Le filtre « À revoir » donne l’intersection avec les favoris. Les clés de citations de `History` passent dans `Quiz.due` mais sont filtrées par `list()`, qui ne garde que les mots.
  - La restauration se fait avant la pose des écouteurs des interrupteurs. `search.setText` rafraîchit une fois, sans danger : toutes les vues sont prêtes.
- **Collection, essai restauré après rotation.** Chaque id restauré est revérifié comme dans `Glose.choice` : débloqué et du bon emplacement, ou `""` pour un accessoire seulement. Modifier la valeur d’une clé existante pendant le parcours de `trial.keys` ne lève pas de ConcurrentModificationException.
- **Couleurs et polices.**
  - La dernière tuile est désactivée, avec `stateDescription` « gardée, la dernière ». La ligne d’aide est en zone live polie.
  - `toggle` remet l’état si un `performClick` passe malgré tout.
  - « Celle du mot » garde la palette ou la police du mot, ou la première du catalogue.
- **Libellés des menus en `string-array`.** Ils sont identiques à l’ancien code Kotlin, caractère pour caractère, y compris « l’horloge ». Les durées `duration_m/h/hm` donnent les mêmes textes (« 1 h 30 »).
- **Ordinal du déverrouillage.** `UnlockService` et `showUnlocks` lisent `unlocks_ordinals` : « 1er », « 2e », « 3e », avec `coerceIn` et `getOrNull`. L’aide est masquée en mode Notification et quand `unlocks = 0`.
- **Definiday Serif.**
  - Table name (fontTools) : les ids 1, 3, 4, 6, 25 et les noms d’instances 256 à 266 sont renommés. Les ids 0, 7, 8, 9, 13 et 14 sont gardés. Le nom 10 dit « Modified version of Libre Baskerville ». Aucun enregistrement Mac.
  - Contours, hmtx, GSUB, GPOS, GDEF, OS/2, fvar, STAT et avar sont identiques à l’original. Seuls le glyphe `uni202F` et ses entrées gvar, HVAR, hhea et post sont ajoutés.
  - Notice OFL complète, avec le paragraphe « Modified Version ». L’id de style `libre_baskerville` est conservé, ce qui garde les prefs, les profils et words.json. Les crédits mentionnent le renommage.
- **AVD.** Les 152 animated-vector ont toutes leurs cibles présentes. Chaque cible `X` dont la VD contient `X_halo` anime aussi `X_halo`.
  - Chaque `?attr/glose…` utilisé dans les drawables est déclaré et a une valeur par défaut dans le style `Glose`.
  - Chaque style `Glose.Acc.*` ouvre aussi la porte `_f0e801`, et `Glose.Acc.Marque` ferme celle du ruban.
  - `ic_launcher_fg.xml` est identique à 6a3a199, puisque l’icône générée a été annulée.
- **`make_sounds.py`.** Sans `--phone-level`, le chemin est équivalent à l’ancien : mêmes tirages du générateur, même ordre niveau, crête puis fondus. Aucun `res/raw` n’est modifié.
- **Garde-fous.** Aucune nouvelle clé de prefs, aucune dépendance. Manifeste, `Update.kt` et signature ne sont pas touchés.

## Pistes écartées après vérification

- Liseré d’une pièce animée (plume dans `tete`) qui déborderait sur le corps pendant le mouvement : non retenu. La découpe est dans le groupe animé et ne bouge qu’avec la tête. Le débord éventuel est de quelques unités, la nuit seulement, et je ne l’ai pas mesuré.
- `resumed` statique qui garderait une activité d’un test Robolectric au suivant : chaque test qui importe reprend la main dans `onResume` avant le résultat. Ce n’est pas un défaut de production.
- `restore()` avec `index > questions.size` après une mise à jour de words.json et une mort du processus : le comportement est identique à l’ancien code, ce n’est donc pas une dérive du découpage.
- Nom 7 de Definiday Serif (« Libre Baskerville is a trademark of Pablo Impallari ») : c’est une mention de marque, pas un usage du nom réservé. Conforme.
