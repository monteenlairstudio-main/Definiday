# Definiday

Appli Android : chaque jour, à l’heure choisie, un mot français rare ou savoureux s’affiche sur l’écran de verrouillage pendant une durée réglable.

## Fonctionnement
- **Notification publique** (visible sur l’écran de verrouillage), retirée automatiquement après la durée choisie (`setTimeoutAfter`).
- **Option fond d’écran de verrouillage** : le mot est dessiné en plein écran (`WallpaperManager`, `FLAG_LOCK`), puis retiré à la fin de la durée. L’écran de verrouillage reprend alors le fond d’accueil.
- Réglages : activation, heure, 1 à 3 mots par jour, durée (15 min à 24 h), bouton « Afficher maintenant ».
- Affichage : notification et écran de verrouillage, notification seule, ou écran de verrouillage seul (premier mot du jour en fond d’écran de verrouillage).
- Fin sur l’écran de verrouillage : après la durée, ou au 1er/2e/3e déverrouillage (service de premier plan discret, car Android ne signale plus les déverrouillages aux applis en arrière-plan ; la durée reste un maximum).
- Bouton « Nouveau mot au hasard » et liste des mots déjà vus aujourd’hui.
- Historique local : un mot déjà montré (du jour ou au hasard) ne redevient mot du jour qu’après 270 jours (~9 mois), et ne ressort au hasard qu’après 120 jours (~4 mois) ; les mots jamais vus passent en premier. Les prochains mots du jour (mots par jour × 270) sont exclus du hasard, ce qui garantit les 9 mois quel que soit l’usage du bouton. Les 4 mois du hasard sont garantis avec 1 mot par jour ; avec 3 mots par jour la réserve hors mots du jour est trop petite, le hasard reprend alors le mot vu le plus anciennement.
- Styles : 18 palettes et 14 polices libres (SIL OFL, licences dans `assets/licences/`) au catalogue `assets/styles.json` ; chaque mot a sa palette et sa police assorties, utilisées si elles font partie de la sélection de l’utilisateur (boutons « Palettes » et « Polices »), sinon un choix stable dans la sélection.
- Anecdote (« Le saviez-vous ? ») avec lien vers sa source Wiktionnaire/Wikipédia quand elle existe.

## Sources
Les définitions et exemples sont **rédigés pour Definiday** et vérifiés avec le [Wiktionnaire](https://fr.wiktionary.org) (CC BY-SA). Ils ne sont pas copiés depuis le Dictionnaire de l’Académie française ni depuis le TLFi : les CGU de dictionnaire-academie.fr interdisent la reproduction et l’extraction automatisée.
Chaque mot propose deux liens :
- **Académie française** : au moment du clic, une recherche sur dictionnaire-academie.fr ouvre l’article (avec repli sur le CNRTL).
- **TLFi (CNRTL)** : `cnrtl.fr/definition/<mot>`.

Les données (≈1 340 mots, dont ≈800 avec une anecdote sourcée) sont dans `app/src/main/assets/words.json`, générées par `python3 tools/merge_words.py tools/lexique/words_*.json tools/lexique/qa_*.json tools/lexique/enrich_*.json` à partir des lots et des correctifs de relecture (`qa_*`, dont le tri qui a retiré les mots courants). Le test unitaire exige au moins 3 × 365 mots.
`tools/check_copies.py` compare chaque définition à celles du Wiktionnaire (index local du dump `frwiktionary-…-pages-articles.xml.bz2`) et signale les formulations trop proches : la dernière passe n’en signale aucune.

## Build
```
./gradlew :app:testDebugUnitTest :app:assembleRelease
```
- APK : `app/build/outputs/apk/release/app-release.apk` (signé avec la clé debug, MVP).
- Captures de démo générées par les tests (Robolectric) : `app/build/demo/`.
- Le CI GitHub Actions publie l’APK et les captures en artefact.
