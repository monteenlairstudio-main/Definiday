# Relecture indépendante 1 : `08a954d..6a3a199` (app/, tools/, design/mascotte/*.js)

Relecteur frais, lecture seule, sans Gradle. Chaque constat a été suivi dans le code de bout en bout ; les pistes non confirmées sont écartées (liste en fin).

## Bloquant

Aucun. Pas de plantage, de perte de profil ni de faille trouvés sur les chemins relus.

## Important

### I1. Deux citations changent de clé : l’historique est perdu, le délai de 270 jours n’est plus tenu
- `app/src/main/assets/citations.json:107` et `:1211` (commit 900742b), avec `Quotes.kt:7` : `key = "q:$texte"`.
- La clé d’une citation, c’est son texte. En retirant les « » extérieurs, on crée deux nouvelles clés. Chez un utilisateur qui avait déjà vu Scarbo (Bertrand) ou Mme de Staël, l’entrée `History` reste sous l’ancienne clé. `Words.nextDaily(quotes, h)` considère donc la citation comme jamais vue : elle peut revenir avant 270 jours, ce qui viole `decision/cooldowns`. De plus, le compteur `k` (`Unlocks.counts`, citations lues) la compte deux fois. Si l’une des deux était la citation du jour à la mise à jour, `Words.today` ne la retrouve plus dans `dailyMots` et tire un autre élément en cours de journée (la carte de verrouillage montre encore l’ancien). Enfin, une notification de la veille portant l’ancienne clé tombe sur le mot du jour (`MainActivity.find`).
- Correctif minimal : garder la clé stable. Par exemple, un champ facultatif `"cle"` (l’ancien texte) sur ces deux entrées et `key = "q:" + (cle ?: texte)`. Autre solution : une migration en une passe des deux anciennes clés dans `History` et `dailyMots`. Un test de données « aucune clé de citation de 0.14.0 ne disparaît » (la fixture dorée peut porter les clés) éviterait de retomber dans le même piège.

## Mineur

### M1. Le test de rotation de l’écran de score ne peut pas voir un son rejoué
- `app/src/test/java/fr/definiday/BugHuntTest.kt:368`.
- Le scénario débloque un objet, donc la fin joue `STAMP`, pas `CHIME`. Depuis `Sounds.hold`, les 4 sons sont préchargés mais aucun chargement n’est notifié au `ShadowSoundPool` : `STAMP` reste dans `waiting`. Le test ne notifie que `CHIME`, qui n’a jamais été demandé (et le commentaire « throws: never loaded » est faux depuis le préchargement). `Sounds.ALL.none { wasResourcePlayed }` est donc vrai même si la rotation rejoue le tampon. La partie texte du test reste valable.
- Correctif : avant `recreate()`, notifier `Sounds.ALL` chargés, puis `clearPlayed()`. Après `recreate()`, vérifier que rien n’est joué. Un `STAMP` rejoué serait alors dans `ready`, donc joué, et le test le verrait.

### M2. Import ou export de profil : le résultat est perdu en silence après une rotation
- `app/src/main/java/fr/definiday/SettingsActivity.kt:165-180`.
- Le fil de travail poste vers l’instance qui l’a lancé. Si l’activité est recréée (rotation, passage jour/nuit) pendant la lecture d’un fichier Drive, `isDestroyed` est vrai et le résultat est jeté : pas de dialogue de confirmation, pas de toast « enregistré » ou « illisible ». La nouvelle instance réactive les boutons. Rien n’est perdu, mais l’utilisateur ne sait pas si l’export a réussi.
- Correctif minimal : déposer le résultat dans un champ du `companion` (profil lu ou id du toast) et le consommer dans `onResume` de l’instance vivante. Le manifeste est gelé, donc pas de `configChanges`.

### M3. « aucun rappel ne viendra » est faux dans un cas courant
- `app/src/main/res/values/strings.xml:47`, avec `SettingsActivity.kt:237`.
- `quizReminder` ne demande qu’un mot vu aujourd’hui. Or ouvrir l’appli avant l’heure du quiz affiche le mot du jour et le marque vu (`MainActivity.onCreate` → `Words.shown`). Un « Au hasard » le fait aussi. Avec le quiz à 8 h et le mot à 9 h, un utilisateur qui ouvre l’appli à 7 h reçoit bien le rappel de 8 h. Le texte promet le contraire.
- Correctif : nuancer la chaîne, par exemple « Avant l’heure du mot du jour, le rappel ne vient que si vous avez déjà ouvert un mot. »

### M4. TalkBack ne dit pas qu’une réponse choisie est fausse
- `QuizActivity.kt:164`, `strings.xml:134`.
- Avant, le texte « ✗ mot » portait l’information. Maintenant, la réponse choisie et fausse se lit « mot, votre réponse ». Seule la croix, visuelle, dit « faux ». La région active « Pas tout à fait… » compense en partie, mais si on revient sur l’option, rien ne dit qu’elle est fausse.
- Correctif : une chaîne `quiz_answer_wrong` (« %1$s, votre réponse, inexacte ») pour `j == picked && !good`.

### M5. Zilla Slab modifiée garde un nom déposé (à valider par le propriétaire)
- `app/src/main/res/font/zilla_slab.ttf` (`tools/fonts_nnbsp.py`).
- Côté OFL, c’est conforme : pas de Reserved Font Name dans la notice ni dans les noms 0/13 (vérifié avec fontTools). Contours, hinting, GSUB et table name sont identiques à l’original pour les 4 fichiers. En revanche, le nom 7 dit « Zilla is a trademark of The Mozilla Corporation », et la marque relève du droit des marques, pas de l’OFL. Une version modifiée distribuée sous le nom « Zilla Slab » est un risque mince mais réel.
- Correctif minimal, si le propriétaire préfère ne pas trancher : ajouter `zilla_slab` à `Styles.NO_NNBSP` et restaurer le fichier d’origine (`git show 08a954d:app/src/main/res/font/zilla_slab.ttf`). FontsTest suit, puisque la liste pilote l’assertion.

## Remarques (pas des défauts de ce diff)

- Le message de 1b81918 affirme « Every bundled face now has U+202F or goes through Styles.fine ». C’est inexact. Libre Baskerville, la police de toute l’interface, a un RFN et n’a pas U+202F. Les « ; : ! ? » des chaînes et des données (dont les nouvelles `quiz_early` et `name_reset`) tombent donc toujours dans le serif système. C’est documenté comme décision en attente (team_ux_B.md § 50) : il faut que ce point figure bien dans le rapport du matin.
- `exemple_note` passe en U+00A0 avant « ! », une dérogation à la règle U+202F du plan. Elle est justifiée par la licence de Dancing Script et documentée dans strings.xml : c’est au propriétaire de la valider.

## Vérifié sans constat

- Zones gelées : `Update.kt`, la signature et le job release ne sont pas touchés. Dans le manifeste, seul le libellé de CollectionActivity change ; permissions et composants exportés sont intacts.
- Prefs et profil : aucune nouvelle clé (`gloseName` existait déjà, dans `SETTINGS`). `everyPrefsKeyIsInTheProfile` couvre les écritures `sp.`. La fixture dorée 0.14.0 et le BOM sont gérés. Le nom est revalidé à la lecture (`nameError`), donc un profil hostile retombe sur le défaut.
- Validation du nom : la limite compte les points de code. Sont refusés CONTROL, FORMAT (U+202E, U+200B, U+200D), U+2028/2029 et les substituts isolés. Le nom ne sert jamais de chaîne de format, et il ne passe pas par `Html.fromHtml`.
- MainActivity exportée : `find()` n’ouvre qu’un élément du jour ou déjà vu (S4). La rotation, la mort du processus et le passage de minuit (`dailyOn`) suivent le flux sans double marquage.
- Quiz : l’enregistrement, l’annonce et le son ne se jouent qu’une fois (`won`/`rated` dans le Bundle). La carte de notation n’apparaît que pour une installation Play, et `rateAsked` n’est pas consommé ailleurs (court-circuit `fromPlay &&`).
- Alarmes : on reprogramme avant d’afficher, et `UnlockService` s’arrête s’il est relancé sans mot. Sons : désactivés par défaut, préchargement et `hold`/`letGo` appariés à onResume/onPause, rappel d’un pool libéré ignoré.
- Pas de Glose sur la carte de verrouillage (`render` passe `vignette = null`). La signature en texte avec le nom choisi y figure, ce qui est déjà acté dans le nœud lockscreen-display.
- Polices Cinzel, Courier Prime, EB Garamond et Zilla : mêmes tables, mêmes contours et instructions, mêmes noms. Les écarts de taille viennent de la recompilation par fontTools. Aucune ne porte de RFN.

## Pistes écartées après vérification

- Test `exportOverALongerFile` jugé incapable d’échouer avec `"w"` : non retenu. Sur Android 10+, `"w"` ne tronque pas (O_TRUNC seulement avec `t`). Je n’ai pas pu confirmer le chemin Robolectric sans Gradle.
- `MotsActivity.itemsCanFocus` qui rendrait la ligne inaccessible au clavier : non retenu. La ligne, cliquable, est focusable en mode auto (API 26+).
- Popup d’erreur du champ nom qui fuirait au `onPause` : non retenu. `Editor` masque l’erreur au détachement.
