# Mettre à jour Definiday sans perdre son profil

Android garde **toutes** les données d’une appli (mots vus, quiz, collection, réglages) quand on installe par-dessus une version plus récente **du même paquet (`fr.definiday`) signée avec la même clé**. Il refuse une version signée avec une autre clé : il faudrait alors désinstaller, ce qui efface tout. La règle est donc simple : n’installer que des APK signés avec la clé de publication du propriétaire, et toujours avec un `versionCode` plus grand.

Deux façons, qui reviennent au même :

1. **Installer le fichier** : télécharger `definiday-x.y.z.apk` sur la [page des versions](https://github.com/monteenlairstudio-main/Definiday/releases) et l’ouvrir sur le téléphone.
2. **Depuis l’appli** : Réglages › Mises à jour › « Vérifier les mises à jour » › « Installer ».

## Publication (CI, `.github/workflows/build.yml`, job `release`)

Sur `main` seulement, après le build signé avec la clé de publication :

- lit `versionName`, `versionCode` et `minSdk` dans `app/build.gradle.kts` ;
- si la version `v<versionName>` existe déjà : même `versionCode` → rien à faire ; autre `versionCode` → échec (« bump versionName ») ; une version publiée n’est **jamais** remplacée ;
- sinon crée la GitHub Release `v<versionName>` (tag posé sur le commit, marquée « latest ») avec deux fichiers : `definiday-<versionName>.apk` et `update.json`.

Notes de version : le message du commit de tête (squash ou merge), sans les lignes d’attribution, 12 lignes au plus. Pour publier : augmenter `versionCode` **et** `versionName`, fusionner sur `main`.

Droits : `contents: write` au niveau du job `release` seulement (le reste du workflow reste en lecture), via `gh` et `GITHUB_TOKEN`, sans action supplémentaire. Un tag créé par `GITHUB_TOKEN` ne relance pas le workflow. `concurrency: release` évite que deux pushs se disputent le même tag. Les tags `v*` poussés à la main construisent l’APK (artefact) mais ne publient rien.

`update.json` :

```json
{"versionCode": 13, "versionName": "0.13.0",
 "apk": "https://github.com/monteenlairstudio-main/Definiday/releases/download/v0.13.0/definiday-0.13.0.apk",
 "sha256": "<64 hexadécimaux>", "minSdk": 26, "notes": "…"}
```

## Dans l’appli (`Update.kt`)

- **Aucune requête** sans le geste de l’utilisateur, sauf s’il active « Vérifier chaque semaine » (**désactivé par défaut**, l’appli reste hors ligne d’abord) : alors au plus une requête par semaine, à l’ouverture de l’accueil, qui propose la nouvelle version dans une fenêtre (« Voir » ouvre Réglages).
- Vérification : `GET https://github.com/monteenlairstudio-main/Definiday/releases/latest/download/update.json` (redirection vers le stockage de GitHub), `HttpURLConnection` sur un fil à part, délais de 10 s / 20 s, 64 Kio au plus.
- Plus récent si `versionCode` > celui installé et `minSdk` ≤ la version d’Android.
- Installation : téléchargement dans le cache (progression en %, 64 Mio au plus), **SHA-256 comparé** à `update.json` (fichier effacé s’il diffère), puis session `PackageInstaller` (`MODE_FULL_INSTALL`, `setAppPackageName`) ; l’état revient à `UpdateReceiver` (PendingIntent explicite) : confirmation Android, annulation, place, ou clé différente (« cette version n’est pas signée avec la même clé »).
- Première fois : Android demande d’autoriser Definiday à installer des applis (« Autoriser cette source ») ; l’appli l’explique et ouvre ce réglage.

### Contrôles de sécurité

- URL de l’APK : `https` seulement, hôte `github.com`, chemin sous `/monteenlairstudio-main/Definiday/releases/` (pas de port, d’identifiants ni de `..`) ; `HttpURLConnection` ne suit jamais une redirection de https vers http, et l’URL finale est revérifiée en https.
- `update.json` et l’APK sont lus avec une taille plafonnée ; `sha256` doit faire 64 hexadécimaux.
- La vraie garantie reste Android : il refuse toute mise à jour qui n’est pas signée avec la clé de l’appli installée. Le SHA-256 protège en plus d’un fichier tronqué ou remplacé.
- Une version d’essai (autre `applicationId`) ne se met pas à jour d’elle-même : elle renvoie à la page des versions.

## Ce que le propriétaire fait une fois

- Si l’appli du téléphone a été installée depuis l’artefact du job `test` (APK signé avec la clé de debug), la première version publiée sera refusée (autre clé) : exporter le profil, désinstaller, installer `definiday-x.y.z.apk` depuis la page des versions, importer le profil. Ensuite, plus jamais.
- Garder le dépôt **public** (le téléchargement se fait sans compte) et ne pas le renommer : l’appli n’accepte que `monteenlairstudio-main/Definiday`.
- Facultatif : activer les « releases immuables » dans les réglages du dépôt, pour qu’une version publiée ne puisse plus être modifiée.

## Google Play

Google Play interdit qu’une appli se mette à jour elle-même. Une future version Play doit :

- passer `buildConfigField("boolean", "SELF_UPDATE", "false")` (le groupe « Mises à jour » disparaît, aucune vérification automatique) ;
- retirer la permission, par exemple avec une saveur `play` et un `src/play/AndroidManifest.xml` contenant `<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" tools:node="remove" />`.
