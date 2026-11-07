# Bug Samsung : « le fond disparaît » (fond d'écran de verrouillage de l'utilisateur perdu)

Date : 7 novembre 2026. Version étudiée : 0.11.0 (minSdk 26, targetSdk 36).
Symptôme confirmé par le propriétaire : sur un Samsung Galaxy, après que Definiday a posé puis retiré sa carte, le fond d'écran de verrouillage choisi par l'utilisateur a disparu et ne revient pas. Modèle et version de One UI inconnus.

Convention : **[fait]** = lu dans le code AOSP, la doc officielle ou une source citée ; **[inférence]** = déduction non vérifiée sur appareil ; **[à vérifier]** = à confirmer sur le téléphone du propriétaire (section 3).

## 1. Ce que fait le code aujourd'hui

- `DailyReceiver.show()` (DailyReceiver.kt) : si `mode != MODE_NOTIF`, dessine la carte (`WordCard.render`, fond dégradé plein écran) et appelle `WallpaperManager.setBitmap(carte, null, true, FLAG_LOCK)`. L'identifiant renvoyé va dans `Prefs.wallpaperId`, la fin dans `wallpaperEndAt` (fichier local `wallpaper_state`, exclu de la sauvegarde).
- Aucun relevé de l'état précédent : on ne regarde pas si l'utilisateur avait un fond de verrouillage à lui avant d'écrire par dessus.
- `clearWallpaper()` (alarme END ou Nᵉ déverrouillage via `UnlockService`) : si `getWallpaperId(FLAG_LOCK) == wallpaperId`, appelle `clear(FLAG_LOCK)`. Rien d'autre n'est restauré.
- `FLAG_SYSTEM` n'est jamais passé : l'écran d'accueil n'est jamais écrit par l'appli.
- Le texte `wallpaper_hint` (strings.xml) le dit déjà : « votre ancien fond de verrouillage n'est pas rétabli ». Il n'apparaît que dans les Réglages, en petit, une fois le mode choisi.
- Le mode par défaut est « notification seule » (`Prefs.mode` vaut `MODE_NOTIF` sauf ancien réglage « wallpaper »). Le bug ne touche donc que les utilisateurs qui ont choisi « Écran de verrouillage » ou « Les deux ».

## 2. Ce qui se passe, très probablement

### 2.1 Mécanique AOSP (vaut pour tous les Android 7 à 16)

- **[fait]** `setWallpaper(..., which = FLAG_LOCK)` crée ou remplace l'entrée « lock » du service ; le fichier de l'ancien fond de verrouillage est écrasé par notre bitmap. Si le fond était partagé (pas d'entrée lock), l'accueil n'est pas touché : la branche « system+lock to lock » de `WallpaperDestinationChangeHandler.complete()` se contente de marquer le fond système comme `FLAG_SYSTEM` seul. La migration de l'image vers l'entrée lock n'existe que dans l'autre sens (`which == FLAG_SYSTEM`, `migrateStaticSystemToLockWallpaperLocked`). Source : [WallpaperManagerService.java (AOSP main)](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/services/core/java/com/android/server/wallpaper/WallpaperManagerService.java).
- **[fait]** `clear(FLAG_LOCK)` efface les bitmaps de l'entrée lock puis réapplique la description du fond d'accueil aux deux écrans (`finalWhich = FLAG_LOCK | FLAG_SYSTEM`, sans rebind forcé). Doc : « clear the lockscreen wallpaper. The home screen wallpaper will become visible on the lock screen. » Sources : même fichier, `clearWallpaperLocked` ; [WallpaperManager.java](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/core/java/android/app/WallpaperManager.java) ; [référence WallpaperManager](https://developer.android.com/reference/android/app/WallpaperManager).
- **[fait]** `getWallpaperId(FLAG_LOCK)` renvoie l'id de l'entrée lock, ou **-1 s'il n'y en a pas** (fond partagé avec l'accueil). C'est le seul moyen public de savoir si l'utilisateur avait un fond de verrouillage distinct. Source : `getWallpaperIdForUser` dans WallpaperManagerService.java.
- **[fait]** Relire ou sauvegarder l'image actuelle est impossible sans `MANAGE_EXTERNAL_STORAGE` (ou la permission système `READ_WALLPAPER_INTERNAL`) : `getDrawable`, `peekDrawable`, `getFastDrawable`, `getWallpaperFile` renvoient l'image par défaut sur Android 13 et lèvent `SecurityException` à partir d'Android 14. `MANAGE_EXTERNAL_STORAGE` est une permission « Accès à tous les fichiers » restreinte sur Google Play : exclue. Sources : référence WallpaperManager ; notes `research_notes/Definiday plus fun et mignonne/fonds_ecran.md` et `research_notes/Benchmark applis mot du jour/surfaces_presentation.md`.
- **[fait]** `getWallpaperInfo(FLAG_LOCK)` (API 34) dit si l'écran de verrouillage est un fond animé (`WallpaperService`) ; il renvoie `null` pour une image fixe, ou si l'appli ne « voit » pas le paquet (il faut un `<queries>` sur l'action `android.service.wallpaper.WallpaperService`). `isLockscreenLiveWallpaperEnabled()` est `@hide @TestApi` et renvoie toujours `true` : inutilisable.

Conséquence directe de notre code, sur n'importe quel téléphone :

| Avant Definiday | Pendant | Après `clear(FLAG_LOCK)` | Perte ? |
|---|---|---|---|
| Fond partagé accueil + verrouillage (`getWallpaperId(FLAG_LOCK) < 0`) | carte du mot | fond d'accueil, donc l'état d'origine | non |
| Fond de verrouillage distinct (id ≥ 0) | carte du mot (l'image de l'utilisateur est écrasée) | fond d'accueil | **oui, définitive** |

C'est exactement le symptôme rapporté : « le fond disparaît et ne revient pas ». Ce n'est pas un bug Samsung, c'est la sémantique documentée de `setBitmap(FLAG_LOCK)` + `clear(FLAG_LOCK)`, combinée au fait qu'on ne peut pas relire l'image.

### 2.2 Pourquoi on le voit surtout sur Samsung

- **[fait]** Depuis One UI 5 (Android 13), Samsung pousse l'édition séparée de l'écran de verrouillage (appui long sur l'écran verrouillé, catégories Galerie, Graphique, Couleurs, « Services de fond d'écran »). Source : [SamMobile, One UI 5.0](https://www.sammobile.com/opinion/one-ui-5-0-wallpaper-selection-on-samsung-galaxy-phones/). Les fonds animés distincts pour le verrouillage arrivent avec One UI 6 : [SammyFans](https://www.sammyfans.com/2023/03/24/samsung-one-ui-6-0-to-bring-separate-lock-screen-live-wallpaper-feature/).
- **[fait]** Le verrouillage Samsung peut aussi être piloté par un service : « Écran de verrouillage dynamique » (images renouvelées automatiquement), réglé dans Fond d'écran > Services de fond d'écran. Sources : [Samsung, désactiver l'écran de verrouillage dynamique](https://www.samsung.com/ae/support/mobile-devices/how-to-disable-the-dynamic-lock-screen-on-your-galaxy-phone/) ; [Samsung US, ANS10001593](https://www.samsung.com/us/support/answer/ANS10001593/). Les Modes et routines peuvent porter leur propre fond de verrouillage : [Samsung Community, Galaxy S22](https://us.community.samsung.com/t5/Galaxy-S22/Wallpapers-automatically-change/td-p/2867778).
- **[fait, anecdotique]** Des utilisateurs Samsung rapportent des conflits entre ces services et des applis tierces de fond d'écran (Zedge) : [Samsung Community](https://eu.community.samsung.com/t5/galaxy-s22-series/wallpaper-keeps-changing-randomly/td-p/6505030). Samsung a dû corriger en 2025 un bug One UI 8 où des fonds dynamiques étaient remplacés par le fond par défaut : [SamMobile](https://www.sammobile.com/news/samsung-fixes-one-ui-8-dynamic-wallpaper-issue/).
- **[fait, un seul témoignage]** Sur Galaxy S24 Ultra (octobre 2026), un développeur indique que `setBitmap(bmp, null, true, FLAG_LOCK)` « only touches the lock screen » : l'accueil et ses couleurs restent intacts. Source : [omalauncher, issue #9](https://github.com/SimonSchubert/omalauncher/issues/9). Cela va contre la variante « l'accueil change aussi ».
- **[inférence]** Les utilisateurs Galaxy ont donc bien plus souvent un verrouillage distinct de l'accueil (photo à eux, diaporama, vidéo, écran dynamique, fond lié à un mode). Pour eux, la ligne 2 du tableau s'applique : la carte du mot écrase leur image, puis `clear(FLAG_LOCK)` affiche le fond d'accueil (ou, si l'accueil est un fond Samsung par défaut, le fond Samsung par défaut). Les réglages « vidéo », « plusieurs images », « écran dynamique » ne reviennent pas non plus : notre écriture remplace le type de fond par une image fixe.
- **[à vérifier]** Deux variantes propres à One UI ne sont pas exclues : (1) One UI garde toujours une entrée lock, même quand l'utilisateur a mis la même image partout (alors `getWallpaperId(FLAG_LOCK)` ne vaudrait jamais -1) ; (2) le `clear(FLAG_LOCK)` de Samsung remet le fond Samsung par défaut au lieu du fond d'accueil. Les deux se tranchent avec `dumpsys wallpaper` (section 3).

### 2.3 Autres causes possibles, moins probables

- **[inférence] Le mot ne part jamais.** Si l'id renvoyé par `setBitmap` ne correspond pas à `getWallpaperId(FLAG_LOCK)` sur One UI (renumérotation par la couche Samsung), `clearWallpaper()` ne fait rien et la carte reste jusqu'au prochain jour. L'utilisateur voit aussi « mon fond a disparu ». Même effet si l'alarme END est inexacte ou que `UnlockService` est refusé (cf. `docs/audits/bugs.md`, bug 4). Testable avec `run-as` (section 3.4).
- **[inférence] L'accueil qui change.** Aucun chemin AOSP ne l'explique ; un vieux témoignage Android 12 sur StackOverflow parle d'un `setBitmap` qui aurait touché les deux écrans ([kiwix StackOverflow 74736875](https://kiwix.lema.org/stackoverflow.com_en_all_2023-11/questions/74736875/wallpapermanager-does-not-work-correctly-on-android-12), non lu : page en 401). À ne pas retenir sans preuve.

## 3. Diagnostic sur le téléphone du propriétaire

Prérequis : Options de développement > Débogage USB, `adb devices` voit le téléphone. Pas besoin de root.

### 3.1 Identifier l'appareil

```sh
adb shell getprop ro.product.model              # ex. SM-S928B
adb shell getprop ro.build.version.release      # Android 13 à 16
adb shell getprop ro.build.version.oneui        # propriété Samsung, ex. 80000 = One UI 8.0 [à vérifier]
adb shell pm list packages | grep -i -E "wallpaper|dressroom|dynamiclock|lockstar|goodlock"
adb shell dumpsys package com.samsung.android.app.dressroom | grep versionName   # appli « Fond d'écran et style » [à vérifier]
```

### 3.2 Photographier l'état du service de fond d'écran

```sh
adb shell dumpsys wallpaper > avant.txt
grep -n -E "System wallpaper state|Lock wallpaper state|Fallback|id=|mWhich|mWallpaperComponent|mInfo.component" avant.txt
```

Lecture **[fait, format AOSP]** : sous `Lock wallpaper state:`, **aucune ligne `User 0: id=...`** veut dire fond partagé ; une ligne `id=N: mWhich=2` veut dire fond de verrouillage distinct (`mWhich` : 1 = accueil, 2 = verrouillage, 3 = les deux). `mWallpaperComponent` indique `ImageWallpaper` (image fixe) ou un service (fond animé, vidéo, service Samsung). One UI peut ajouter ses propres lignes : les garder dans le fichier.

`adb shell cmd wallpaper help` : sur AOSP il ne propose que des commandes de luminosité (`set-dim-amount`, `get-dim-amount`) ; utile seulement pour voir si Samsung y ajoute quelque chose.

### 3.3 Réglages Samsung (clés non documentées, à comparer avant / après)

```sh
for ns in system secure global; do adb shell settings list $ns | grep -i -E "wallpaper|lock_?screen|dynamic" > $ns-avant.txt; done
```

Une clé `lockscreen_wallpaper` circule dans des listes non officielles ([Scribd](https://www.scribd.com/document/808780721/new)) ; son espace de noms et son sens sont **[à vérifier]**. On cherche ce qui change quand Definiday écrit, pas une clé précise.

### 3.4 Scénario de reproduction (matrice)

Pour chaque état de départ : (A) même image partout ; (B) photo distincte sur le verrouillage ; (C) verrouillage en « plusieurs images » ou vidéo ; (D) Écran de verrouillage dynamique actif ; (E) fond de verrouillage lié à un mode.

1. Mettre l'état de départ, puis `dumpsys wallpaper > avant.txt` et les `settings list`.
2. Dans Definiday : mode « Écran de verrouillage », durée la plus courte (15 min), « Afficher maintenant ». Verrouiller, constater la carte.
3. `adb shell dumpsys wallpaper > pendant.txt` ; noter l'id de l'entrée lock.
4. Sur une version de débogage seulement : `adb shell run-as fr.definiday cat shared_prefs/wallpaper_state.xml` et comparer `wallpaperId` à l'id lu en 3. S'ils diffèrent, c'est la cause 2.3 (le mot ne part jamais).
5. Attendre la fin (ou régler « au 1er déverrouillage » et déverrouiller), puis `dumpsys wallpaper > apres.txt` et `settings list` à nouveau.
6. Pendant tout le test : `adb logcat -v time | grep -i -E "wallpaper|keyguard|lockscreen" > log.txt`.
7. Noter ce qui s'affiche sur le verrouillage : fond d'accueil, fond Samsung par défaut, ou image d'origine.

Résultat attendu si l'analyse est juste : A revient intact ; B, C, D, E perdent leur fond, et l'entrée lock disparaît de `apres.txt`. Si A perd aussi quelque chose, ou si `avant.txt` montre une entrée lock même en A, c'est une variante One UI (2.2, dernier point) et la détection par `getWallpaperId` ne suffira pas.

## 4. Solutions, classées

Effort pour notre pile (Kotlin, API plateforme, pas d'AndroidX), en jours de dev + tests Robolectric existants.

### Option 1. Garde-fou : ne jamais écraser un fond de verrouillage qui n'est pas à nous (S, 0,5 j). **À faire en premier.**

- Avant `setBitmap`, lire `before = wm.getWallpaperId(FLAG_LOCK)`.
- Autorisé seulement si `before < 0` (fond partagé : `clear(FLAG_LOCK)` rend exactement l'état d'origine) ou `before == p.wallpaperId` (c'est déjà notre carte, cas du mot au hasard).
- Sinon : pas de fond, la notification prend le relais (même en mode « verrouillage seul »), et les Réglages affichent « Vous avez un fond de verrouillage à vous : Definiday n'y touche pas. Choisissez votre photo pour y ajouter le mot. » (lien vers l'option 2).
- Sur API 34+, refuser aussi si `getWallpaperInfo(FLAG_LOCK) != null` (fond animé ou vidéo sur le verrouillage), avec le `<queries>` adéquat.
- Vérifier `isWallpaperSupported()` et `isSetWallpaperAllowed()` (appareils gérés).
- Ajouter un test : `setBitmap` n'est jamais appelé quand l'id lock est ≥ 0 et différent du nôtre, et jamais avec `FLAG_SYSTEM`.
- Limite **[à vérifier]** : si One UI garde toujours une entrée lock (2.2), ce garde-fou bloque le mode verrouillage pour tous les Galaxy. C'est acceptable : c'est sans impact, et l'option 2 rouvre le mode.

### Option 2. Le plan « sans impact » : l'utilisateur choisit SA photo, on la rend à la fin (M, 1,5 à 2 j)

Principe : on ne peut pas relire son fond, donc on lui demande de nous le donner une fois, et on ne fait plus jamais `clear(FLAG_LOCK)` sur un fond distinct.

1. **Choix de la photo, sans permission.** `MediaStore.ACTION_PICK_IMAGES` si `SDK_INT >= 33` ou (`SDK_INT >= 30` et `SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2`), sinon `ACTION_OPEN_DOCUMENT` avec `image/*`. Aucune permission de stockage. Source : [guide du sélecteur de photos](https://developer.android.com/training/data-storage/shared/photopicker).
2. **Copie privée immédiate** dans `filesDir/verrou.jpg`, réduite à la taille de l'écran (`ImageDecoder` sur 28+, `BitmapFactory` + `android.media.ExifInterface` pour l'orientation sur 26-27). Pas besoin de `takePersistableUriPermission`. Exclure ce fichier de la sauvegarde comme `wallpaper_state`.
3. **Mémoriser l'état** dans `wallpaper_state` : `lockIdAtPick = getWallpaperId(FLAG_LOCK)` au moment du choix, `lockWasShared` au moment de poser la carte, `restoredId` = id renvoyé par la restauration.
4. **Poser la carte** : `WordCard` dessine le mot par dessus la photo (voile dégradé en bas pour la lisibilité, déjà prévu dans les notes `fonds_ecran.md`), toujours avec `FLAG_LOCK` seul. Autorisé si `before < 0`, ou `before` vaut `wallpaperId`, `restoredId` ou `lockIdAtPick`. Toute autre valeur veut dire que l'utilisateur a changé son verrouillage depuis : on s'abstient, on notifie, et on propose de rechoisir la photo.
5. **Retirer la carte** (si l'id lock est toujours le nôtre) : `lockWasShared` vrai, alors `clear(FLAG_LOCK)` (retour exact) ; sinon `setBitmap(copie, null, true, FLAG_LOCK)` et on garde l'id dans `restoredId`. L'utilisateur retrouve sa photo.
6. **Bouton « Rétablir mon écran de verrouillage »** dans les Réglages : fait l'étape 5 tout de suite, et supprime la copie si l'utilisateur quitte le mode.

Limites :
- On rend une image fixe : un verrouillage Samsung en vidéo, « plusieurs images », écran dynamique ou lié à un mode ne revient pas à l'identique. Le dire avant activation (option 3).
- Recadrage : One UI peut recadrer différemment de l'original ; passer `visibleCropHint = null` et une image au format de l'écran.
- C'est le modèle de [Bible Lock Screen (F-Droid)](https://f-droid.org/en/packages/com.daklok.biblelockscreen/), qui pose le verset sur « votre photo préférée » choisie par l'utilisateur ; sa stratégie de restauration n'est pas documentée.

### Option 3. Avertissement propre aux Galaxy (S, 0,25 j), en complément de 1 et 2

Si `Build.MANUFACTURER` vaut `samsung` et que l'utilisateur choisit un mode avec verrouillage : afficher dans l'onboarding et les Réglages, avant activation, « Sur Galaxy, Definiday remplace votre fond de verrouillage par une image fixe. Les fonds vidéo, diaporama, l'écran de verrouillage dynamique et les fonds liés aux modes sont désactivés et ne reviennent pas seuls. » Remonter `wallpaper_hint` au moment du choix, pas seulement après.

### Option 4. Retirer le mode verrouillage sur Samsung (XS, 0,1 j)

Masquer « Écran de verrouillage » et « Les deux » quand `MANUFACTURER == samsung`, notification seule. Zéro risque, mais on perd la seule surface toujours visible sur le verrouillage. À garder en repli si le diagnostic montre que One UI casse aussi le cas A (fond partagé).

### Option 5. Surfaces sans fond d'écran (pour mémoire)

- **Notification sur l'écran de verrouillage** (déjà le défaut, `VISIBILITY_PUBLIC`) : aucun impact sur le fond. Sur Galaxy, l'affichage dépend du réglage Notifications > Notifications sur l'écran de verrouillage (icônes seules ou contenu masqué) **[à vérifier]** ; un lien vers ce réglage dans l'appli peut aider. 0,25 j.
- **Live Updates / notification promue (Android 16)** : exclues. La doc interdit l'information « ambient » et les « alerts », exige une activité en cours lancée par l'utilisateur. Source : [Live Updates](https://developer.android.com/develop/ui/views/notifications/live-update). La Now Bar de One UI 8 s'appuie sur ce mécanisme et sur des partenariats : [9to5Google](https://9to5google.com/2025/07/09/samsung-one-ui-8-now-bar-third-party-apps/), [SamMobile](https://www.sammobile.com/news/15-more-apps-will-support-now-bar-on-one-ui-8/).
- **Widgets d'écran de verrouillage** : Android 16 QPR2 les réserve au mode « hub » (en charge ou sur socle) : [9to5Google](https://9to5google.com/2025/08/20/android-qpr2-lock-screen-widgets/). Pour One UI 8, l'ouverture aux tiers n'était qu'une spéculation : [SamMobile](https://www.sammobile.com/news/one-ui-8-bring-third-party-widgets-lock-screen/). Notre widget d'accueil y viendra peut-être gratuitement ; pas un remplaçant.
- **Intent plein écran / activité `showWhenLocked`** : `USE_FULL_SCREEN_INTENT` n'est accordé d'office qu'aux applis d'appel et de réveil (Android 14+, depuis le 22 janvier 2025) : [Play Console](https://support.google.com/googleplay/android-developer/answer/13392821). Hors sujet pour un mot du jour.
- **Fond animé Definiday (WallpaperService)** : l'utilisateur doit le choisir lui-même, et One UI n'applique les fonds animés tiers qu'à l'accueil selon [omalauncher #9](https://github.com/SimonSchubert/omalauncher/issues/9). Non retenu.
- **Écran toujours actif (AOD)** : pas d'API tierce publique (Samsung : appli maison et Good Lock). Non retenu.

## 5. Plan recommandé

1. **Tout de suite (0,5 j)** : option 1 (garde-fou `getWallpaperId(FLAG_LOCK)`), plus option 3 (avertissement Galaxy remonté au moment du choix). Le mode par défaut reste « notification seule ». Plus aucun utilisateur ne perd un fond distinct.
2. **Ensuite (1,5 à 2 j)** : option 2 (photo choisie par l'utilisateur, copie privée, carte composée dessus, restauration de la même photo au lieu de `clear(FLAG_LOCK)`), avec le bouton « Rétablir ».
3. **En parallèle** : le propriétaire lance le diagnostic de la section 3 (cas A et B au minimum). Si le cas A perd aussi le fond, ou si One UI montre toujours une entrée lock, passer à l'option 4 pour les Galaxy sans photo choisie.
4. Pour l'utilisateur déjà touché : son ancienne image ne se récupère pas par l'appli ; il doit la remettre depuis Galerie > Définir comme fond d'écran > Écran de verrouillage. Le dire dans la note de version.

## 6. Questions ouvertes pour le propriétaire

- Modèle exact et version de One UI (Paramètres > À propos du téléphone > Informations sur le logiciel).
- Après le retrait du mot, que montre le verrouillage : le fond d'accueil, le fond Samsung par défaut, ou la carte du mot qui reste ?
- Le verrouillage d'origine était-il une photo distincte de l'accueil, un diaporama, une vidéo, l'écran de verrouillage dynamique, ou un fond lié à un mode ?
- L'écran d'accueil a-t-il changé lui aussi (couleurs du thème comprises) ?
- Accepte-t-on de demander une photo à l'utilisateur pour le mode verrouillage, ou préfère-t-on la notification seule sur Galaxy ?

## 7. Correctif appliqué (option 1, « Garde-fou seulement »)

Décision du propriétaire : garde-fou seul, pas de sélecteur de photo (option 2 non retenue pour l’instant).

- `Wall.lockFree` (DailyReceiver.kt) : la carte n’est posée que si `getWallpaperId(FLAG_LOCK) < 0` (verrouillage partagé avec l’accueil, donc `clear(FLAG_LOCK)` rend exactement l’état d’origine) ou s’il vaut `Prefs.wallpaperId` (notre carte, cas du mot au hasard). Sur API 34+, refus aussi si `getWallpaperInfo(FLAG_LOCK) != null` (fond animé ; `<queries>` sur `WallpaperService` ajouté au manifeste). Une lecture qui lève une exception vaut refus.
- Tous les chemins passent par `DailyReceiver.show()` (mot du jour, « Afficher le mot maintenant », mot au hasard, mots ajoutés en cours de journée). Refus : on ne touche pas au fond, le mot part en notification même en mode « Écran de verrouillage seulement », et `Prefs.lockBlocked` (fichier local `wallpaper_state`) passe à vrai ; le toast de test et l’aide du mot au hasard en tiennent compte.
- `FLAG_SYSTEM` n’est jamais passé ; `clearWallpaper()` garde la vérification d’id (lecture désormais dans `runCatching`).
- Réglages et étape 2 de l’onboarding : si un mode avec verrouillage est choisi et que le garde-fou refuserait, une note de Glose (`NoteBox`, papier à note) remplace `wallpaper_hint` et explique que le mot arrivera par notification ; une ligne de plus sur Samsung (écran de verrouillage dynamique, Modes et routines). `wallpaper_hint` dit maintenant que le fond habituel revient.
- Tests : `WallTest` (verrouillage partagé, distinct, notre carte, fond animé ou illisible, mot au hasard, notes Réglages et onboarding). Robolectric ne garde qu’un id de verrouillage : `Wall.lockId` et `Wall.lockLive` sont des variables, remplacées dans `TestApp`.
- Limite : si One UI garde toujours une entrée lock (2.2, variante 1), le mode verrouillage ne pose plus jamais la carte sur ces Galaxy ; le mot arrive par notification et la note l’explique. À trancher avec `dumpsys wallpaper` (section 3).
