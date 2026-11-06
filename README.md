# Definiday

Appli Android : chaque jour, à l’heure choisie, un mot français rare ou savoureux s’affiche sur l’écran de verrouillage pendant une durée réglable.

## Fonctionnement
- **Notification publique** (visible sur l’écran de verrouillage), retirée automatiquement après la durée choisie (`setTimeoutAfter`).
- **Option fond d’écran de verrouillage** : le mot est dessiné en plein écran (`WallpaperManager`, `FLAG_LOCK`), puis retiré à la fin de la durée. L’écran de verrouillage reprend alors le fond d’accueil.
- Réglages : activation, heure, 1 à 3 mots par jour (une notification par mot), durée (15 min à 24 h), mode fond d’écran (premier mot du jour), bouton « Afficher maintenant ».
- Bouton « Nouveau mot au hasard » et liste des mots déjà vus aujourd’hui.
- Historique local : un mot déjà montré (du jour ou au hasard) ne redevient mot du jour qu’après 270 jours (~9 mois), et ne ressort au hasard qu’après 120 jours (~4 mois) ; les mots jamais vus passent en premier. Limite : avec 3 mots par jour, au-delà d’environ 1 tirage au hasard par jour en moyenne, la réserve s’épuise et le mot vu le plus anciennement est repris.
- Anecdote (« Le saviez-vous ? ») avec lien vers sa source Wiktionnaire/Wikipédia quand elle existe.

## Sources
Les définitions et exemples sont **rédigés pour Definiday** et vérifiés avec le [Wiktionnaire](https://fr.wiktionary.org) (CC BY-SA). Ils ne sont pas copiés depuis le Dictionnaire de l’Académie française ni depuis le TLFi : les CGU de dictionnaire-academie.fr interdisent la reproduction et l’extraction automatisée.
Chaque mot propose deux liens :
- **Académie française** : au moment du clic, une recherche sur dictionnaire-academie.fr ouvre l’article (avec repli sur le CNRTL).
- **TLFi (CNRTL)** : `cnrtl.fr/definition/<mot>`.

Les données (≈1 200 mots, plus de 3 ans sans répétition) sont dans `app/src/main/assets/words.json`, générées par `python3 tools/merge_words.py tools/lexique/words_*.json tools/lexique/qa_*.json` à partir des lots et des correctifs de relecture (`qa_*`, dont le tri qui a retiré les mots courants). Le test unitaire exige au moins 3 × 365 mots.

## Build
```
./gradlew :app:testDebugUnitTest :app:assembleRelease
```
- APK : `app/build/outputs/apk/release/app-release.apk` (signé avec la clé debug, MVP).
- Captures de démo générées par les tests (Robolectric) : `app/build/demo/`.
- Le CI GitHub Actions publie l’APK et les captures en artefact.
