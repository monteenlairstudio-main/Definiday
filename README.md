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

Les données (≈1 340 mots, dont ≈800 avec une anecdote sourcée) sont dans `app/src/main/assets/words.json`, générées par `python3 tools/merge_words.py tools/lexique/words_*.json tools/lexique/qa_*.json tools/lexique/enrich_*.json tools/lexique/style_*.json` à partir des lots et des correctifs de relecture (`qa_*`, dont le tri qui a retiré les mots courants). Le test unitaire exige au moins 3 × 365 mots.
`tools/check_copies.py` compare chaque définition à celles du Wiktionnaire (index local du dump `frwiktionary-…-pages-articles.xml.bz2`) et signale les formulations trop proches : la dernière passe n’en signale aucune.

## Build
```
./gradlew :app:testDebugUnitTest :app:assembleRelease
```
- APK : `app/build/outputs/apk/release/app-release.apk`, signé avec la clé release si elle est configurée (voir ci-dessous), sinon avec la clé debug.
- Captures de démo générées par les tests (Robolectric) : `app/build/demo/`.
- Le CI GitHub Actions publie l’APK et les captures en artefact.

## Signature release
La clé et ses mots de passe ne sont **jamais** dans le dépôt. Gradle les lit dans `~/.gradle/gradle.properties` ou dans des variables d’environnement de même nom :

| Nom | Requis | Rôle |
|---|---|---|
| `DEFINIDAY_KEYSTORE` | oui | chemin **absolu** du `.jks` (`~` n’est pas développé) |
| `DEFINIDAY_KEYSTORE_PASSWORD` | oui | mot de passe du keystore |
| `DEFINIDAY_KEY_ALIAS` | non | alias, `definiday` par défaut |
| `DEFINIDAY_KEY_PASSWORD` | non | mot de passe de la clé, celui du keystore par défaut (format PKCS12) |

Sans `DEFINIDAY_KEYSTORE`, l’APK release est signé avec la clé debug. Keystore indiqué mais mot de passe absent : la build échoue (pas de repli silencieux).

### Générer la clé (une seule fois)
```
keytool -genkeypair -v -keystore definiday.jks -alias definiday -keyalg RSA -keysize 4096 -validity 10000
```
Hors du dépôt (par ex. `~/keys/definiday.jks`). Puis dans `~/.gradle/gradle.properties` :
```
DEFINIDAY_KEYSTORE=/home/moi/keys/definiday.jks
DEFINIDAY_KEYSTORE_PASSWORD=...
```
Vérifier : `apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk` ne doit plus afficher `CN=Android Debug`.

### Sauvegarder la clé
**Perdre le `.jks` ou son mot de passe = plus aucune mise à jour possible** : Android refuse d’installer une mise à jour signée par une autre clé, il faut désinstaller (données perdues) et republier sous un autre identifiant.
- Mot de passe dans un gestionnaire de mots de passe.
- Au moins deux copies du `.jks` hors de la machine, dont une hors ligne (clé USB chiffrée, coffre de gestionnaire de mots de passe, stockage cloud chiffré).
- Le secret GitHub n’est **pas** une sauvegarde : un secret ne se relit pas.

Changement de clé : l’APK actuel, signé debug, ne se met pas à jour vers l’APK signé release. Désinstaller une fois (l’historique des mots vus est perdu), puis installer le nouvel APK.

### CI GitHub Actions
Dans *Settings → Secrets and variables → Actions* du dépôt :
- `DEFINIDAY_KEYSTORE_BASE64` : le `.jks` en base64, sur une ligne. Linux : `base64 -w0 definiday.jks` ; macOS : `base64 -i definiday.jks` ; PowerShell : `[Convert]::ToBase64String([IO.File]::ReadAllBytes((Resolve-Path definiday.jks).Path)) | Set-Clipboard` (chemin résolu : .NET ne suit pas le dossier courant de PowerShell).
- `DEFINIDAY_KEYSTORE_PASSWORD`.
- Optionnels : `DEFINIDAY_KEY_ALIAS`, `DEFINIDAY_KEY_PASSWORD`.

Le workflow décode le keystore dans `$RUNNER_TEMP` sans rien afficher (GitHub masque aussi les secrets dans les logs). Sans secret (forks, PR externes), l’APK reste signé debug. Pour d’autres noms de secrets, ne modifier que les `secrets.*` de `.github/workflows/build.yml`.

### Play Console, piste de test interne (optionnel)
Voie la plus fiable contre les avertissements de Play Protect : l’appli est installée par le Play Store.
1. Compte développeur Google Play (frais uniques, 25 USD à ce jour) et vérification d’identité.
2. Créer l’appli `fr.definiday`. Play exige un **AAB** : `./gradlew :app:bundleRelease` → `app/build/outputs/bundle/release/app-release.aab`, signé avec la clé ci-dessus.
3. *Play App Signing* (obligatoire pour les nouvelles applis) : Google signe l’appli distribuée avec sa propre clé, `definiday.jks` devient la *clé d’importation*. Conséquence : un APK installé à la main et la version Play n’ont pas la même signature, passer de l’un à l’autre impose de désinstaller. La Play Console propose aussi d’importer sa propre clé comme clé de signature d’appli : à décider avant la première publication.
4. *Tests → Test interne* : importer l’AAB, ajouter les testeurs (jusqu’à 100 adresses Google), partager le lien d’inscription. Pas de délai d’examen long pour cette piste, contrairement à la production.

Contexte : Google étend la vérification des développeurs aux applis installées hors Play Store (premiers pays fin septembre 2026, monde entier annoncé pour 2027).
