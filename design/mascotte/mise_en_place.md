# Monsieur Glose dans Definiday : proposition de mise en place

Légende : **[vérifié]** = constaté ici (outil exécuté, aapt2, rendu) ou lu dans la documentation officielle
developer.android.com (lien donné) ; **[supposé]** = non vérifié, à confirmer sur appareil.

## 1. Où apparaît Glose

| Endroit | Pose / animation | Taille | Détail |
|---|---|---|---|
| Écran principal (`MainActivity`), en tête au-dessus du mot du jour | la pose **choisie dans la collection** (défaut « assis »), animation en boucle (respiration, clignement, reflet des lunettes) | 160 dp | arrêtée en `onPause`, relancée en `onResume`. Un appui la relance depuis le début (clin d'œil). Après 21 h et si débloquée, la pose du soir remplace la pose choisie. |
| Pendant le quiz (`QuizActivity`) | **icône** (tête seule) en haut, statique | 56 dp | ne distrait pas de la définition. |
| Fin du quiz : score | **« le fil écrit »**, joué une fois (écriture ≈ 3,3 s puis mot tenu, pas de rembobinage) | 200 dp | **parfait** → « bravo » + éclats ; **réussi** → « bravo » ; **raté** → « encore » (jamais moqueur). Sous le dessin : « 2 / 3 ». |
| Après un quiz qui débloque quelque chose | carte « Nouveau : écharpe tricotée », Glose qui la porte | 160 dp | même écran que le score, bouton « Voir la collection ». |
| Notification du quiz du soir (21 h) | **pose du soir** en image fixe (BigPictureStyle) si débloquée, sinon « assis » ; grande icône = tête endormie | bitmap 512 × 256 px | une notification ne peut pas jouer d'AnimatedVectorDrawable : on dessine le VectorDrawable dans un Bitmap. `bigPicture(Bitmap)` existe depuis l'API 16, `setLargeIcon(Icon)` depuis l'API 23 [vérifié, référence `Notification.BigPictureStyle`]. La petite icône (`ic_stat`) reste monochrome. |
| Écran de verrouillage | **non par défaut** | — | c'est l'espace du mot ; ajouter un dessin sur `WordCard` gênerait la lecture et l'horloge. Option possible plus tard : petite tête dans un coin (case « Glose sur l'écran de verrouillage », décochée). |
| Écran « Collection » (nouvelle activité) | grille : poses, mots du fil, pelages, pelotes, accessoires | vignettes 96 dp, aperçu 200 dp | élément verrouillé = silhouette grisée + condition en clair (« encore 3 soirs d'affilée »). Un appui sur un élément débloqué l'essaie dans l'aperçu animé, « Choisir » l'enregistre. |

Accessibilité : `contentDescription` (« Monsieur Glose écrit bravo ») ; si `ValueAnimator.areAnimatorsEnabled()`
renvoie faux (animations coupées ou économiseur de batterie), on affiche l'image fixe [vérifié : API 26, référence
`ValueAnimator`].

## 2. Table de déblocage

Compteurs existants (`Prefs`) : `quizzes` (soirs de quiz, un par jour), `perfectQuizzes`, `correctAnswers`,
`streak` (soirs consécutifs). Un compteur à **ajouter** : `passedQuizzes` (une ligne dans `Quiz.record`,
initialisé à `perfectQuizzes` pour les utilisateurs existants).

**Seuil « quiz réussi », à valider.** Le quiz compte 1 à 3 questions (`wordsPerDay`). Avec 70 %, on exige
ceil(0,7 × n) bonnes réponses : 1/1, 2/2, 3/3. **70 % revient donc à exiger un sans-faute pour toutes les tailles
de quiz actuelles.** Je propose « réussi = au plus une erreur par tranche de 3 questions » (score × 3 ≥ total × 2,
soit 66,7 %) : 1/1, 2/2, 2/3.

Estimation en soirs, avec un quiz par soir et environ 80 % de bonnes réponses :

| Élément | Type | Condition | 1 mot/j | 3 mots/j |
|---|---|---|---|---|
| Lunettes dorées | toujours | — | — | — |
| Assis sur son volume | pose | dès l'installation | 0 | 0 |
| Gris tigré | pelage | défaut | 0 | 0 |
| Pelote bordeaux | pelote | défaut | 0 | 0 |
| Le fil écrit « mot » | pose + mot | `quizzes ≥ 1` | 1 | 1 |
| Mot « encore » | mot | `quizzes ≥ 1` (sert de réaction au quiz raté) | 1 | 1 |
| Mot « bravo » | mot | `passedQuizzes ≥ 1` | ~1 | ~1 |
| Nœud papillon | accessoire | `perfectQuizzes ≥ 1` | ~1 | ~2 |
| Pose du soir (tasse) | pose | `streak ≥ 3` | 3 | 3 |
| Mot « dodo » | mot | avec la pose du soir | 3 | 3 |
| Pelote moutarde | pelote | `quizzes ≥ 5` | 5 | 5 |
| Écharpe tricotée | accessoire | `streak ≥ 7` | 7 | 7 |
| Sur la pile de livres | pose | `quizzes ≥ 10` | 10 | 10 |
| Crème caramel | pelage | `perfectQuizzes ≥ 5` | ~6 | ~10 |
| Bonnet de nuit | accessoire | `streak ≥ 14` | 14 | 14 |
| Pelote sauge | pelote | `quizzes ≥ 20` | 20 | 20 |
| Mot « rare » | mot | `correctAnswers ≥ 30` | ~38 | ~13 |
| Plume d'oie | accessoire | `correctAnswers ≥ 60` | ~75 | ~25 |
| Pelote rose poudré | pelote | `perfectQuizzes ≥ 15` | ~19 | ~30 |
| Marque-page à gland d'or | accessoire | `quizzes ≥ 45` | 45 | 45 |
| **Enluminure** (rare, doré + éclats) | pelage | `correctAnswers ≥ 150` | ~190 | ~63 |
| **Nuit d'encre** (rare, bleu nuit étoilé) | pelage | `streak ≥ 30` | 30 | 30 |

Remarques : les seuils sur `correctAnswers` avantagent les réglages à 3 mots ; c'est voulu (plus d'effort), mais on
peut les remplacer par `quizzes` si le propriétaire préfère l'équité. Rien ne se perd : une série cassée ne retire
aucun déblocage (la condition est vraie « au moins une fois », on mémorise l'ensemble débloqué dans `Prefs`).
Code : une fonction pure `Unlocks.of(quizzes, perfect, passed, correct, streak): Set<String>` + un test JUnit
(table ci-dessus en données), et un `Set<String>` « déjà vus » pour annoncer les nouveautés une seule fois.

## 3. Conversion technique (sans AndroidX, minSdk 26)

### 3.1 Outil de conversion : ce qui a été essayé
- **Svg2Vector d'Android Studio** (`com.android.ide.common.vectordrawable.Svg2Vector`, dans `sdk-common-31.13.2.jar`,
  déjà présent dans le cache Gradle de la machine) : **installé et exécuté ici** [vérifié] (`android/Conv.java`).
  Résultat sur `glose_assis.svg` : image fixe correcte, mais `<animateTransform> is not supported`, tous les groupes
  aplatis (2 groupes restants sur 54) et noms perdus : inutilisable pour l'animation. La documentation le confirme :
  pas de tirets, pas d'opacité de groupe, un seul masque par groupe [vérifié, « Vector Asset Studio »].
- **Conversion maison** `android/svg2avd.js` (Node, sans dépendance, environ 200 lignes) écrite et testée ici : elle
  lit le SVG produit par `build.js` et écrit `res/drawable/glose_<pose>_vd.xml` (VectorDrawable) +
  `res/drawable/glose_<pose>.xml` (animated-vector, animateurs inline `aapt:attr`) + `res/interpolator/glose_ease.xml`
  (`pathInterpolator` 0,45 0 0,55 1 = les keySplines SMIL).
  - groupes nommés conservés, pivots de rotation, enveloppes `translate/scale` des pivots d'échelle ;
  - opacité de groupe multipliée dans `fillAlpha`/`strokeAlpha` des chemins (les groupes VD n'ont pas d'alpha) ;
  - styles hérités des `<g>` redescendus ; cercles et ellipses en `pathData` ; dégradés linéaires et radiaux en
    `<gradient>` inline (API 24+, donc OK en minSdk 26 [vérifié, référence `VectorDrawable`]) ;
  - `<clipPath>` → `<clip-path>` ;
  - **mot écrit** : `stroke-dashoffset` L → 0 converti en `trimPathEnd` 0 → 1 sur les 3 traits du fil
    (`trimPathEnd` est animable [vérifié, tableau des attributs animables d'`AnimatedVectorDrawable`]) ;
  - `animate opacity` (z, vapeur, halo) → `fillAlpha`/`strokeAlpha` (animables [vérifié]) ;
  - images clés SMIL → `propertyValuesHolder` + `keyframe` (fraction, valeur, interpolateur), `repeatCount="infinite"`.
- **Validation** : `aapt2 compile` + `aapt2 link` (build-tools 36.0.0, `android-36/android.jar`, minSdk 26) sur
  5 dessins (assis + accessoires, écrit « bravo », soir, pile, icône) : **aucune erreur** [vérifié]
  (`android/glose_test.apk`). Contrôle visuel : `android/vd2svg.js` relit le VectorDrawable produit en appliquant
  les règles du VD (thème résolu) → `android/check_vd.png` : identique au SVG, pelage recoloré et bonnet affiché par
  thème. Ce n'est pas un rendu Android réel : **à confirmer sur émulateur** [supposé].

### 3.2 Recoloration par pelage et pelote : attributs de thème
- Le convertisseur (`--theme`) remplace les 5 teintes de pelage/pelote par `?attr/gloseFur`, `gloseFurD`,
  `gloseCream`, `gloseBall`, `gloseBallD` ; `res/values/glose_attrs.xml` les déclare, `glose_styles.xml` définit
  `Glose` (défaut) et les surcouches `Glose.Pelage.*`, `Glose.Pelote.*`, `Glose.Acc.*`.
- Une couleur VD peut être un attribut de thème : l'exemple officiel utilise `android:strokeColor="?android:colorControlNormal"`
  [vérifié, guide « Vector drawables overview »].
- Composition au moment d'afficher :
  ```kotlin
  fun gloseDrawable(ctx: Context, res: Int, styles: List<Int>): Drawable {
      val themed = ContextThemeWrapper(ctx, R.style.Glose)
      styles.forEach { themed.theme.applyStyle(it, true) } // pelage, pelote, accessoires portés
      return themed.getDrawable(res)!!                      // AnimatedVectorDrawable : start()/stop()
  }
  ```
  `Theme.applyStyle(resId, force=true)` écrase les valeurs déjà présentes [vérifié, API 1, `Resources.Theme`].
- Pas de `setTint` : il teinterait tout le dessin d'une seule couleur.
- Limite : une teinte = un rôle. Le bleu nuit sert à la fois au livre, à la tasse et au nœud ; pour que « Nuit d'encre »
  change le socle en bordeaux, il faut des jetons distincts (`gloseBook`, `gloseCup`) avec des hex distincts dans le
  dessin. À faire au passage en production (environ 1 h).

### 3.3 Accessoires : groupes dans le même dessin
> **Remplacé (0.9.x) par des calques par emplacement** : voir `README.md`. Chaque pièce d'accessoire est un dessin à part
> (même viewport, groupes parents animés recopiés avec les mêmes animateurs), le corps est coupé là où une pièce
> s'intercale (3 à 6 parties selon la pose), et `Glose.drawable` compose un `LayerDrawable`. Rendu Robolectric avant/après
> (16 poses × 19 combinaisons, fixe et à 6 instants de l'animation) : écart maximal 64/255 sur un pixel, au plus 3 pixels
> au-delà de 8/255 par image (anticrénelage des bords). APK release : 1 909 454 → 1 700 637 octets. À vérifier sur appareil :
> synchronisme des calques sur le RenderThread, mémoire (un cache bitmap par calque affiché).

Historique :
- Chaque accessoire est un groupe nommé (`noeud`, `echarpe`, `bonnet`, `plume`, `marque_page`) déjà placé dans la
  bonne couche : bonnet et plume DANS le groupe `tete` (ils suivent ses animations), nœud et écharpe au cou, marque-page
  sur le livre. Le convertisseur leur met `android:scaleX/scaleY="?attr/gloseAccX"` (0 = caché, 1 = porté).
- aapt2 accepte `?attr` sur `scaleX` d'un `<group>` [vérifié]. Que le framework résolve un attribut de thème sur un
  attribut de **groupe** (et pas seulement de chemin) n'est pas écrit dans la documentation [supposé, d'après le code
  AOSP qui garde les `themeAttrs` des groupes : **à tester en premier sur émulateur**].
- Solution de repli si ça échoue : générer un drawable par combinaison utile (pose × accessoire porté, environ
  20 Ko compressés chacun), ou superposer deux `ImageView` (le corps et un AVD « accessoire » de même viewport et de
  mêmes durées, démarrés dans la même frame).

### 3.4 Animation du mot
- Un fichier par mot (`glose_ecrit_mot|bravo|rare|dodo|encore`) : même corps, seul le chemin du fil change.
  `trimPathEnd` 0 → 1 en 3,3 s, tenu, puis 1 → 0 (rembobinage) ; pelote `rotation` 0 → 150° synchronisée.
- Pour la réaction de fin de quiz : jouer une seule fois. `AnimatedVectorDrawable.registerAnimationCallback`
  (`Animatable2`) permet d'enchaîner [vérifié, référence AVD]. Pour s'arrêter sur le mot écrit, le convertisseur
  produira une variante « une fois » (`repeatCount=0`, sans la phase de rembobinage).
- Depuis l'API 25, les AVD tournent sur le RenderThread : fluides même si le fil UI est chargé [vérifié, référence AVD].
- Écart connu : les phases négatives SMIL (« z », vapeur) deviennent des `startOffset` ; le premier cycle commence
  donc vide pendant 1 à 2 s.

### 3.5 Poids et performance
- Mesuré dans `glose_test.apk` (XML binaire compressé) : assis ≈ 20 Ko, pile ≈ 20 Ko, soir ≈ 17 Ko, écrit ≈ 19 Ko
  par mot, icône ≈ 6 Ko, enveloppes AVD ≈ 0,5 Ko. **Total estimé : 4 poses + 5 mots + icône ≈ 160 Ko**, soit
  **+13 % de l'APK release actuel (1,24 Mo)**. Les variantes (pelages, pelotes, accessoires) ne coûtent rien de plus
  (thèmes + groupes).
- Complexité : environ 180 chemins par pose. La documentation recommande de limiter un vecteur à 200 × 200 dp et
  prévient que le premier chargement coûte plus de CPU qu'un bitmap [vérifié, « Vector Asset Studio »]. Chaque
  VectorDrawable garde UN cache bitmap à sa taille [vérifié, référence `VectorDrawable`]. Donc : 160–200 dp maximum,
  une instance par taille, mesurer le temps de premier affichage (`Debug.startMethodTracing` ou Perfetto). Si c'est
  trop lent sur un appareil d'entrée de gamme : précision réduite à des coordonnées entières, fusion des ombres.

## 4. Étapes livrables

| # | Livrable | Effort | Risque principal |
|---|---|---|---|
| 0 | `tools/glose/` dans le dépôt : sources Paper.js, `build.js`, `svg2avd.js`, script `make` ; XML générés versionnés dans `res/` (la CI ne lance pas Node) | 0,5 j | aucun |
| 1 | Test sur émulateur : `?attr` de couleur + `?attr` sur `scaleX` de groupe + `trimPathEnd` + premier affichage (temps) | 0,5 j | **décide de la solution d'accessoires (3.3)** |
| 2 | Glose sur l'écran principal (pose « assis », animée, arrêt en `onPause`, repli fixe si animations coupées) | 0,5 j | coût de chargement |
| 3 | Réaction de fin de quiz (« bravo » / « encore », une fois) + compteur `passedQuizzes` + test | 1 j | ton du message raté |
| 4 | `Unlocks` (fonction pure + test de la table) + annonce « Nouveau : … » | 1 j | réglage des seuils |
| 5 | Écran Collection (grille, verrouillés en silhouette, aperçu animé, choix enregistré) | 1,5 j | lisibilité des silhouettes |
| 6 | Pelages, pelotes, accessoires par thèmes ; jetons `gloseBook`/`gloseCup` | 1 j | voir étape 1 |
| 7 | Notification du soir en BigPicture (VD → Bitmap 512 × 256) + tête endormie en grande icône | 0,5 j | rendu sur les thèmes sombres de notification |
| 8 | Pose du soir sur l'écran principal après 21 h | 0,25 j | aucun |

Total : environ 6,5 à 7 jours. Ordre conseillé : 0 → 1 → 2 → 3 (premier livrable visible), puis 4 → 5 → 6, puis 7 → 8.

## 5. À décider par le propriétaire
1. **Seuil « réussi »** : 70 % (soit un sans-faute avec 1 à 3 questions) ou 2/3 (proposé) ?
2. **Mots** : « mot », « bravo », « rare », « dodo », « encore » conviennent-ils ? « lire » et « rêve » demandent un
   point ou un accent, donc un deuxième trait : faut-il les accepter avec un petit nœud de laine en guise de point
   ou d'accent ?
3. **Seuils de la table** (rythme : premier déblocage dès le premier soir, rares à 30 soirs d'affilée et ~150 bonnes
   réponses), et faut-il baser `correctAnswers` sur `quizzes` pour ne pas avantager les réglages à 3 mots ?
4. **Écran de verrouillage** : rester sans Glose (proposé) ou option discrète ?
5. **Pose du soir automatique après 21 h** sur l'écran principal : oui / non ?
6. **Un message sous la réaction « encore »** (« Demain, on retente ? ») ou le dessin seul ?
7. **Couleur du bonnet** (rouge brique actuel) et socle bordeaux pour « Nuit d'encre » : validés ?
