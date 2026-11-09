# Audit des animations de Monsieur Glose (phase 1 : mesures, causes, correctifs proposés)

Base `2f261b6`, worktree `agent-afd73c3143ecd822c`. Rien de fusionné. Document écrit au fil de l'eau.

## 0. Méthode

Deux instruments, parce que Robolectric joue les AVD sur le fil UI alors que le téléphone les joue sur le RenderThread (RT) :

1. **Modèle RT en Python** (`anim/model.py`, `measure.py`) : relit les XML générés (`res/drawable/glose_*`, `res/animator/glose_k_*`,
   `GloseLayers.kt`), reproduit la sémantique AOSP lue dans le code (`AnimatedVectorDrawable.java` android14, `libhwui/PropertyValuesAnimatorSet.cpp`,
   copies dans `anim/AVD.java`, `anim/PVAS.cpp`) :
   interpolateur de l'`objectAnimator` (par défaut AccelerateDecelerate), interpolateur de chaque clé, pré-échantillonnage RT
   (`getFrameCount` : 1 point par 16 ms, **300 points max**, linéaire entre points), `startOffset`, `repeatCount`, `stop()` = `end()` =
   chaque animateur à sa fraction 1, matrices de groupe `VGroup` (pivot, scale, rotate, translate). Il calcule la position de chaque
   point de chemin par image à 60 i/s et donne des **déplacements en px sur un ImageView de 128 dp à 420 dpi (336 px)**.
   Paupières et bâillement (échelle → 0) sont mesurés à part (« apparitions »), le reflet des lunettes (masqué aux deux bouts) est exclu.
2. **Images Android réelles** (test Robolectric temporaire `GloseAnimAuditTest.kt`, non commité, copie dans `anim/audit/`) : technique de
   l'itération 4 (`forceAnimationOnUI` + `AnimatorSet.setCurrentPlayTime`), une image toutes les 16,7 ms, jour et nuit, séquences
   « vie au repos → toucher en plein cycle → retour à la vie », arrêt à 26 s, bâillement. Il lit aussi l'interpolateur réel des animateurs gonflés.

Scripts : `events.py` (sauts aux événements), `velo.py` (vitesses, arrêts en plein mouvement, à-coups ; 3 variantes A/B/C),
`halo.py` (liseré vs partie, animateurs morts, sous-échantillonnage), `rtsample.py` (erreur RT, décalage des clés).

## 1. Constats mesurés (au fil de l'eau)

### 1.1 Interpolateur implicite AccelerateDecelerate sur chaque animateur (hypothèse 1 : CONFIRMÉE)
- Aucun `objectAnimator` (idle inline, `animator/glose_k_*`, hoche/penche) n'a `android:interpolator`. Lu sur les animateurs gonflés par
  Android (Robolectric, `frames/jour/interpolators.txt`) : `interp=AccelerateDecelerateInterpolator` pour tous.
- Effet : la timeline de chaque animateur est déformée **en plus** des interpolateurs de clé : le temps passe ×1,57 plus vite au milieu,
  ×0 aux bords. Les clés des réactions tombent à côté de l'instant écrit dans `reactions.js` :
  toucher 600 ms : clé 0,10 (tassement d'anticipation) jouée à 0,20 (**+63 ms**), clé 0,90 jouée à 0,80 (−63 ms) ;
  spécial 1 800 ms : **+189 ms / −189 ms**. Le contact de la pelote (0,33) arrive 107 ms en retard.
- Mesure (modèle RT, A = aujourd'hui, B = interpolateur global linéaire) : pic de vitesse de la pelote du spécial « assis »
  **18,8 px/image au lieu de 12,3** (+53 %), à-coup 10,6 au lieu de 6,7 px/image² ; dormeur du toucher « soir » 8,2 au lieu de 5,3 ;
  queue du spécial « pile » 5,9 au lieu de 3,9 ; arrêts en plein mouvement du dormeur (spécial réveil) **7 au lieu de 2**, du corps
  (spécial écrit) **6 au lieu de 1**.
- Cause : `design/mascotte/android/svg2avd.js:109` (`animator()`), `android/reactions.js:137` (`animators()`), `android/make_android.js:240`.

### 1.2 `glose_ease` sur chaque clé (hypothèse 2 : CONFIRMÉE, mais conçue ainsi dans l'aperçu SVG)
- `build.js:32` met `keySplines 0.45 0 0.55 1` sur chaque segment, `svg2avd.js:102` met `@interpolator/glose_ease` sur chaque clé : la
  vitesse retombe à zéro à chaque clé, y compris aux passages (0° d'un balancier 0 → −2,5 → 0 → 2 → 0).
  À l'œil c'est surtout sensible sur les gestes amples (pelote du repos « pile » : 5 arrêts en 13 s avec A ; reste de l'idle : vitesses
  sous 0,3 px/image, l'arrêt se lit comme une « respiration » saccadée de la tête et de la pelote).
- `build.js` ignore déjà son option `a.linear` côté Android : la vapeur montante (`pose_soir.js:89`, `linear: true`) est jouée en ease-in-out ×2.

### 1.3 Pré-échantillonnage RT ≤ 300 points (hypothèse 3 : vraie, effet MESURÉ FAIBLE)
- Durées concernées et pas : 6 000 (20 ms), 6 400 (21), 7 000 (23), 7 300 (24), 9 400 (31), 11 000 (37), **29 000 ms (97 ms)**.
- Erreur RT max / amplitude (linéaire entre points vs courbe exacte) : boucles de 29 s **0,7 %** (bâillement translateY 0,05 unité),
  9 400 ms 1 %, 11 000 ms 0,1 %. Le bâillement dure 870 ms → 9 points : pas de saut visible. L'erreur la plus forte est sur les
  réactions courtes à segments de 12–36 ms (8 à 11 % d'amplitude, cf. hypothèse 5) : ce sont ces segments qu'il faut allonger,
  pas les boucles longues.

### 1.4 Respiration morte : valeurs arrondies à 0,1 (NOUVEAU)
- `svg2avd.js:102` écrit `f(v)` = `toFixed(1)`. Respiration du corps `[1, 1] → [1.015, 1.025]` (assis), `[1.012, 1.03]` (pile),
  `[1.012, 1.035]` (soir), dormeur `[1.008, 1.02]` → toutes écrites **1 → 1 → 1**. **127 propriétés animées ont toutes leurs clés égales**
  (animateurs morts : respiration de chaque pose et ses copies dans les calques d'accessoires, parties des 29 s), coût RT sans effet.
- Inverse pour la flamme (`pose_soir.js:84`) : 0,94/1,06 écrits 0,9/1,1 → **flamme 67 % plus ample** que dessinée.

### 1.5 Arrêt de la vie au repos à 26 s : `stop()` = `end()` (hypothèse 4 : CONFIRMÉE par le code, Robolectric ne peut pas la montrer)
- `Glose.kt:85` poste `stop()` après `IDLE_MS`. `AnimatedVectorDrawable.stop()` → `mAnimatorSet.end()` → RT `nEnd` →
  `Animator.cpp` `Request::End` : `updatePlayTime(mDuration)` → **chaque animateur saute à sa clé de fraction 1** (vu dans
  `anim/Animator.cpp:143` et `:243`). Sur le fil UI (Robolectric, ou repli logiciel) `AnimatorSet.end()` ne termine pas les enfants
  infinis : l'image se fige sans saut. D'où 0 px dans les images Robolectric (`audit/image_metrics.txt`) : seul le modèle RT vaut ici.
- Modèle RT, saut au moment de l'arrêt (px, pire partie) : **écrit : pelote 81 px (rotation 150° → 0), tête 8 px, et le mot écrit
  disparaît d'un coup (trimPathEnd 1 → 0) : la pose « le fil écrit » se repose ensuite sans son mot** (la fraction 1 de la boucle
  = mot caché). Pile : tête 7 px. Soir/réveil : queue 3 px, les « z » et la vapeur s'effacent d'un coup (alpha 1 → 0 sur un « z »).
  Assis : < 1 px (26 000 tombe par chance près de 4 × 6 400).
- Les « 12 à 16 dp » de la pose soir (hypothèse) sont des « z » et de la vapeur dont l'alpha de fin vaut 0 : on voit un effacement
  brusque, pas un déplacement.
- Hypothèse 6 (CONFIRMÉE par le code) : les durées RT sont multipliées par `ValueAnimator.getDurationScale()`
  (`AnimatedVectorDrawable.java` `createNativeChildAnimator`), le minuteur de 26 s ne l'est pas : à l'échelle ×2 l'arrêt tombe à
  mi-boucle, à ×0,5 les boucles tournent 52 s d'animation.

### 1.6 Fin de réaction → retour à la vie, toucher en plein cycle (NOUVEAU)
- Une réaction démarre du dessin fixe (ses clés 0 = valeurs du VD) et finit dessus ; la vie au repos redémarre à sa propre image 0
  (`once()` : nouveau drawable + `play()` → `reset()`). Écart image 0 de la vie / dessin fixe (= fin de réaction) :
  assis, pile : 0. **Écrit : le mot disparaît d'un coup après chaque réaction** (fixe = mot écrit, image 0 de la vie = mot caché ;
  Robolectric : 3 161 px changés en une image jour, 5 249 la nuit, `seq_jour_ecrit_bravo_tap_t2500.png`).
  Soir/réveil : les trois « z » (alpha 1 dans le VD) et la vapeur (0,3) s'éteignent d'un coup (2 155 px jour, 2 928 nuit).
- Toucher en plein cycle : la réaction repart du dessin fixe, donc saut = état courant de la vie − fixe. Modèle RT, pire partie,
  max / médiane sur tout le cycle : assis tête 8,7 / 2,3 px, pile patte 7,4 / tête 1,8, soir vapeur 17,6 / « z » 7,6 (apparition
  des « z » à pleine opacité), **écrit pelote 81 / 39 px et le mot qui réapparaît en entier**. Robolectric : 7 000 à 13 000 px changés
  sur l'image du toucher (assis en plein bâillement : 9 454 px jour, la bouche ouverte disparaît et la tête retombe de 7).
- Pas de pause réelle sur RT : `VectorDrawableAnimatorRT.pause()/resume()` sont des `TODO` vides (`AVD.java:1768`) : on ne peut pas
  « reprendre où on en était ».

### 1.7 Liseré de nuit qui glisse sous la tête (NOUVEAU, « mal aligné »)
- `glose_lib.js:571` (`visible()`) ne recopie sur `X_halo` que les mouvements ≥ 8 unités / 2° / 5 %. La tête a trois animateurs au
  repos (roulis ±2,5°, respiration −3, bâillement −7 sur 29 s) ; `tete_halo` n'a que le roulis. Décalage tête / liseré mesuré :
  **assis 3,3 px, pile 3,9 px, écrit 3,1 px, réveil 2,6 px** (au bâillement, 16,2–16,4 s) pour un liseré de 4,5 px : il s'amincit à
  ~1 px au sommet des oreilles et s'épaissit sous le menton. Queue : 1,6 px.
- Réactions : liseré et partie partagent le même `glose_k_*` : 0 px (vérifié aussi sur les images de nuit du spécial soir ; les 24 px
  que donne le script pour `dormeur` viennent de chaînes de groupes différentes, artefact de mesure, pas de défaut visible).

### 1.8 Segments de clés trop courts (hypothèse 5 : CONFIRMÉE, effet faible)
- 10 segments < 33 ms qui bougent, tous nés de `merge()` (`reactions.js:47`) qui croise les clés de la tête (0,28 ; 0,4 ; 0,82/0,9) et
  celles du squash (0,26 ; 0,38 ; 0,85) : ex. toucher, `tete.translateY` 0,26 → 0,28 = **12 ms**, −15,3 → −14,1. Amplitude ≤ 1,3 px :
  invisible seul, mais c'est là que l'erreur d'échantillonnage RT est la plus forte (8 à 11 % d'amplitude).
- Correctif : aligner les clés du squash sur celles de la tête dans `TAP.awake` (0,26 → 0,28 ; 0,38 → 0,40 ; 0,85 → 0,82).

### 1.9 Coût de dessin : chaque calque animé est re-rastérisé en entier à chaque image (NOUVEAU, cause probable du « saccadé » sur téléphone)
- Sur RT, un VectorDrawable dont une propriété change est redessiné **en entier** dans son cache bitmap (CPU) à chaque image. Chez
  Glose tous les calques du corps sont des AVD (la tête, la pelote, la queue, le liseré y sont mêlés au livre, au corps, à l'ombre).
- Mesure (Robolectric, Skia natif, CPU x86 de bureau, 336 px, médiane de 15 dessins à des instants différents, `frames/*/cost.tsv`) :
  **jour 4,0 à 6,4 ms par image, nuit 5,2 à 9,3 ms** (assis + bonnet + écharpe nuit : 9,2 ms dont 3,1 ms pour le calque 0 qui ne bouge
  que pour le liseré de la tête et de la pelote ; pile jour : calque du corps 4,0 ms pour une queue de ±4°). Un cœur de téléphone
  milieu de gamme est 3 à 6 fois plus lent qu'un cœur de bureau : 15 à 50 ms par image > 16,7 ms → images sautées.
  **À confirmer sur téléphone** (`adb shell dumpsys gfxinfo fr.definiday framestats`, Perfetto : tranche RenderThread `VectorDrawable`).
- Le jour, le liseré est transparent mais son calque (livre, ombre, ruban) est quand même redessiné à chaque image parce que
  `tete_halo`/`pelote_halo` y sont animés.

### 1.10 Vérifications sans défaut
- Démarrage des calques : `play()` et `once()` font `reset()`/`start()` de tous les calques dans le même message ; sur RT, un AVD pas
  encore dessiné met l'action en attente jusqu'au prochain dessin (`addPendingAction`) : tous partent à la même image. Mêmes durées
  et mêmes `glose_k_*` pour la tête et ses copies sous les chapeaux (vérifié dans les XML) : pas de dérive après répétition.
- Raccords de boucle : toutes les boucles idle finissent sur leur valeur de départ ; les sauts de position (« z », vapeur 20 → −50,
  reflet −110 → 110) sont cachés (alpha 0 ou hors masque) à la couture.
- Pas de `setLayerType` (l'AVD resterait sur RT), pas d'échelle de vue animée. Tailles des vues : 72, 112, 128, 160, 200 dp : le VD est
  rastérisé à la taille affichée, pas d'étirement de bitmap (pas de scintillement d'échelle).
- Animations désactivées : `play()`/`once()` ne démarrent rien (`areAnimatorsEnabled`).
- Réactions : la dernière image = la première (contrôle « fin = début » de `reactions.js`) ; réactions et liseré : 0 px d'écart.

## 2. Prototypes (worktree, branche `worktree-agent-afd73c3143ecd822c`, rien de fusionné)

Fichiers : `svg2avd.js` (interpolateur linéaire, `eases()`, 3 décimales, boucles entières, « une fois » = suffixe), `reactions.js`
(linéaire + `eases()`), `make_android.js` (hoche/penche linéaires, clés de « écrit »), `pose_ecrit.js` (boucle qui commence mot
écrit), `glose_lib.js` (liseré : tous les mouvements), `Glose.kt` (minuteur × échelle de durée). XML régénérés par
`node android/make_android.js`. Auto-contrôle : `node android/svg2avd.js` → `eases ok`.

Mesures avant / après (modèle RT ; `audit/velo_compare.txt`, `events_*.txt`, `halo_*.txt`, planches `vitesses_avant_apres.png`,
`sauts_avant_apres.png`) :

| Mesure | Avant | Après |
|---|---|---|
| Interpolateur réel des objectAnimator | AccelerateDecelerate (tous) | linéaire (tous) |
| Spécial assis, pelote : pic / à-coup (px/image, px/image²) | 18,8 / 10,6 | 12,3 / 6,4 |
| Spécial pile, pelote au sol : pic | 16,6 | 10,6 |
| Spécial réveil, dormeur : arrêts en plein mouvement | 7 | 2 |
| Spécial écrit, corps : arrêts | 6 | 1 |
| Toucher soir, dormeur : pic / à-coup | 8,2 / 3,4 | 5,3 / 1,6 |
| Repos pile, pelote : arrêts en plein mouvement (13 s) | 5 | 0 |
| Repos assis, pelote (balancier ±6°) | arrêt au passage par 0° | vitesse max au passage, arrêt aux seuls extrêmes et à la couture |
| Arrêt à 26 s, pire saut (écrit / pile / soir / assis) | 81 / 7 / 3 px + mot effacé | 0 / 0 / 0 / 0 |
| Fin de réaction → vie, « écrit » | mot effacé d'un coup | 0 (mot écrit des deux côtés) |
| Toucher en plein cycle, « écrit », médiane | pelote 39 px | 2,9 px |
| Liseré / tête au repos (bâillement) | 3,3 à 3,9 px | 0 |
| Animateurs morts (clés toutes égales) | 127 | 89 (restent les `translateX` constants des `translate` verticaux : sans effet) |
| Respiration (corps, dormeur) | morte | 1,5 à 3,5 % comme dessiné |
| XML `drawable/glose_*` + `animator/glose_*` | 1 752 592 o | 1 793 906 o (+41 Ko bruts, surtout les animateurs du liseré en ligne ; APK non mesuré) |

Ce qui empire, à juger : le toucher en plein cycle saute un peu plus fort sur « assis » (médiane 2,3 → 4,8 px) parce que la vie au
repos est plus ample (respiration rétablie, plus de « temps mort » près de 0). Les boucles s'arrêtent maintenant chacune à la fin de
sa dernière boucle entière (pelote assis à 22,8 s, clignement à 19,5 s) au lieu de toutes à 26 s.

Non prototypé : le coût de dessin (1.9), les « z »/vapeur de soir au retour de réaction (1.6), le saut du toucher en plein cycle (1.6),
les segments de 12 ms (1.8).

Commit du prototype : **`cafebba`** sur `worktree-agent-afd73c3143ecd822c` (base `2f261b6`). Tests sous flock après régénération :
GloseTest 19, GloseTapsTest 3, DefinidayTest 20, OnboardingTest 6 : verts. Images Android après (`audit/frames_after`,
`image_metrics_apres.txt`) : interpolateur gonflé = `LinearInterpolator` ; « écrit », fin de toucher → vie : **0 px** changé
(avant 3 161 jour / 5 249 nuit) ; soir inchangé (2 155 px : « z »/vapeur, non traité).
Le test d'images n'est pas commité (copie `audit/GloseAnimAuditTest.kt.txt`).

## 3. Classement

| # | Animation | Symptôme | Mesure | Cause (fichier:ligne, base 2f261b6) | Correctif proposé | Risque | Taille | Proto |
|---|---|---|---|---|---|---|---|---|
| 1 | Toutes (idle 60 i/s) | saccades sur téléphone (images sautées) | 4–6,4 ms (jour) / 5–9,3 ms (nuit) de rastérisation CPU par image sur PC de bureau, tous calques du corps re-rastérisés en entier | `svg2avd.js:192-221` (calques coupés aux seuls accessoires), RT redessine tout VD dont une propriété bouge | découper les calques aussi entre groupes animés et statiques (livre/ombre/corps figés en VD statiques, tête/pelote/queue/liseré en petits AVD) ; ou ne pas animer le liseré le jour | moyen (ordre de dessin, `GloseLayers`) | ~60 lignes générateur | non, **à mesurer d'abord sur téléphone** (gfxinfo) |
| 2 | Toutes | timing faux, gestes trop rapides au milieu, lents aux bords | interpolateur gonflé = AccelerateDecelerate ×297 ; clé 0,1 jouée à 0,2 (+63 ms toucher, +189 ms spécial) ; pic pelote spécial 18,8 → 12,3 px/image | `svg2avd.js:109`, `reactions.js:137`, `make_android.js:240` | `android:interpolator="@android:interpolator/linear"` | faible (le rendu suit enfin `reactions.js`/SVG) | 3 lignes | oui |
| 3 | Pose « écrit » (choisie à l'accueil) | le mot disparaît à 26 s, après chaque toucher ; pelote qui saute de 150° | trim 1 → 0 en 1 image ; pelote 81 px ; 3 161 px changés | `pose_ecrit.js:6,11,35,40` (boucle qui commence mot caché) + `Glose.kt:85` | boucle qui commence/finit mot écrit (écarts relatifs), « une fois » = suffixe | faible | 10 lignes | oui |
| 4 | Toutes, à 26 s | saut final de la vie au repos | pile tête 7 px, écrit 81 px, soir « z »/vapeur effacés | `Glose.kt:85` `stop()` = `end()` RT (`Animator.cpp:143`) | boucles entières `repeatCount` fini dans `IDLE_MS` ; minuteur × échelle de durée | faible ; boucles qui s'arrêtent à des instants différents (19,5 à 29 s) | 3 lignes + 1 Kotlin | oui |
| 5 | Toucher en plein cycle (toutes) | la pose saute au début de la réaction | médiane 2–8 px, max 8,7 (assis) / 17,6 (soir vapeur) / 81 px (écrit, avant proto) ; 7 000–13 000 px changés | `Glose.kt:120-139` `once()` remplace le drawable ; réactions qui partent du dessin fixe | court fondu enchaîné (2 ImageView ou `TransitionDrawable` 120 ms), ou démarrer la réaction à la prochaine couture de la vie (≤ 1 boucle d'attente : non) | moyen | ~20 lignes Kotlin | non |
| 6 | Idle : balanciers (pelote, queue, tête) | arrêt au milieu du balancement (« stop and go ») | pelote pile : 5 arrêts / 13 s → 0 | `svg2avd.js:102` (`glose_ease` partout), `build.js:32` | ease aux seuls repos, accélère/décélère aux passages (`eases()`) | faible ; la couture reste un repos (choix) | 15 lignes | oui (Android seulement, pas l'aperçu SVG) |
| 7 | Respiration (corps, dormeur) ; flamme | respiration morte ; flamme trop ample | 127 propriétés mortes ; flamme 0,9/1,1 au lieu de 0,94/1,06 | `svg2avd.js:102` `f(v)` = 1 décimale | 3 décimales | faible ; la respiration réapparaît (accessoires du cou fixes, comme dans l'aperçu) | 1 ligne | oui |
| 8 | Nuit, repos | liseré qui glisse sous la tête (bâillement, respiration) | 3,1 à 3,9 px sur un liseré de 4,5 px | `glose_lib.js:571` seuil `visible()` | le liseré refait tous les mouvements | faible ; +41 Ko de XML bruts (à mesurer en APK) | 1 ligne | oui |
| 9 | Soir / réveil : fin de réaction | les 3 « z » et la vapeur s'éteignent d'un coup | 2 155 px jour, 2 928 nuit | `reactions.js:63-68,99-107` finit sur le fixe (« z » alpha 1) ; idle image 0 = « z » alpha 0 | réactions soir/réveil qui finissent sur l'image 0 de la vie (alpha 0, vapeur basse) ; contrôle « fin = image 0 de la vie » au lieu de « fin = début » | faible | ~10 lignes | non |
| 10 | Spécial assis (+ accessoires) / toucher | contacts de la pelote et anticipation décalés | clé 0,33 jouée +107 ms | idem #2 | idem #2 | — | — | oui |
| 11 | Toucher (3 poses éveillées) | micro-segments de 12 ms | 10 segments < 33 ms, ≤ 1,3 px | `reactions.js:47` `merge()` croise 0,26/0,28, 0,38/0,40, 0,85/0,82 | aligner les clés du squash sur celles de la tête | nul | 3 nombres | non |
| 12 | Durée d'animation ≠ 1 (option développeur, accessibilité) | arrêt à mi-boucle | minuteur non mis à l'échelle | `Glose.kt:85` | `IDLE_MS * ValueAnimator.getDurationScale()` | nul | 1 ligne | oui |
| 13 | Boucles de 29 s (bâillement, oreille) | marches de 97 ms | erreur 0,7 % d'amplitude : **non visible** | RT `MAX_SAMPLE_POINTS = 300` | rien (ou sortir le bâillement en animateur court si on veut des clignements rapides dedans) | — | — | non |
| 14 | Vapeur montante (soir/réveil) | monte en ease-in-out au lieu d'une montée régulière | `linear: true` ignoré | `svg2avd.js` ignorait `calcMode` | lire `calcMode="linear"` | nul | 1 ligne | oui |

## 4. À décider (expert / propriétaire)
1. **Valider sur un téléphone réel** avant tout : `adb shell dumpsys gfxinfo fr.definiday framestats` pendant 26 s d'idle à l'accueil, jour
   et nuit, avec et sans accessoires, sur base et sur `cafebba`. Si les images sautent (#1), le découpage des calques est le vrai chantier.
2. Accepter le changement de « sensation » de #2/#6 (gestes au tempo écrit dans `reactions.js`, balanciers continus) : à voir sur appareil.
3. La couture des boucles reste un repos (départ et arrêt sans saut de vitesse) : alternative = balancier sinusoïdal continu
   (`eases(vals, cyclic)`), qui démarre alors en mouvement depuis l'image fixe.
4. #5 (toucher en plein cycle) : fondu court ou accepter ; c'est le seul saut restant visible à chaque toucher.
5. Poids : +41 Ko de XML bruts (liseré qui suit tout + attributs) : mesurer l'APK release, sinon garder le seuil du liseré à 2 unités.
6. Aperçu SVG (`build.js`) : lui appliquer aussi `eases()` pour que l'aperçu navigateur montre ce que joue Android.
