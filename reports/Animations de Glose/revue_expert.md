# Revue experte : animations de Monsieur Glose (audit + prototype `cafebba`)

Relecteur : ingénieur graphique Android (RenderThread, AVD, VectorDrawable, cadence) et animateur de personnage.
Entrées lues : `best_practices.md`, `audit.md`, `audit/` (planches, `velo_compare.txt`, `events_*.txt`, `image_metrics*.txt`),
copies AOSP `AVD.java`, `Animator.cpp`, `PVAS.cpp`, diff `2f261b6..cafebba` et XML régénérés du worktree. Rien modifié, aucun Gradle.

Marquage : **[F]** fait sourcé (fichier:ligne AOSP ou XML lu), **[D]** déduction ou mesure que j'ai refaite, **[H]** hypothèse à mesurer
sur téléphone. « De mémoire AOSP » = code que je connais mais pas relu ici : à confirmer par la trace.

---

## 1. Constats de l'audit : d'accord / pas d'accord / nuance

### 1.1 Interpolateur implicite AccelerateDecelerate : **d'accord**, priorité de fusion n° 1
- [F] `ValueAnimator.sDefaultInterpolator = AccelerateDecelerateInterpolator` ; `AnimatorInflater` n'appelle `setInterpolator` que si
  `android:interpolator` est présent (doc ValueAnimator + code cités dans le brief). Sur RT, chaque animateur enfant garde **son**
  interpolateur : `createNativeChildAnimator` lit `animator.getInterpolator()` → `NativeInterpolatorFactory` (`AVD.java:1540-1549`).
  L'ensemble natif, lui, est linéaire (`PVAS.cpp:42`) : seul l'interpolateur par animateur compte.
- [D] J'ai refait les calculs : clé 0,1 atteinte à t = 0,205 (cos x = 0,8) → +63 ms sur 600 ms, +189 ms sur 1 800 ms ; clé 0,33 à
  t = 0,390 → +107 ms ; pente max π/2 = 1,57. Chiffres de l'audit exacts.
- Nuance : le brief dit que deux calques de durées différentes « se décalent ». Chez Glose, les parties liées partagent la même durée
  et le même `glose_k_*` : le défaut est un écart au **timing écrit** (anticipation et contacts en retard, milieu de geste trop
  rapide), pas une désynchronisation entre calques. Ça reste le correctif au meilleur rapport effet/risque (3 lignes).

### 1.2 `glose_ease` sur chaque clé : **d'accord sur le diagnostic, le correctif `eases()` est incomplet**
- [D] Diagnostic juste : (0,45 ; 0 ; 0,55 ; 1) = easeInOutQuad, pente nulle aux deux bouts, donc arrêt à chaque clé.
- [D] J'ai contrôlé la continuité de vitesse aux clés de passage dans les XML **régénérés** (critère n° 2 du brief : rapport
  vitesse sortante / entrante dans [0,5 ; 2]). Sur 40 clés de passage, **26 échouent encore** : 6 arrêts nets (accélérations
  explicites `'out'`/`'in'` de `reactions.js` posées sur une clé de passage, ex. `glose_k_8adad7b1` rotation 2,24 → 30 → 72) et
  20 sauts de vitesse jusqu'à ×6,7 (`glose_k_041a9257`, rebonds du squash ; `glose_k_30307cef`, tête du toucher). Cause : un
  `accelerate_quad` finit à 2 × la pente moyenne du segment, un `decelerate_quad` part à 2 × la sienne. La vitesse n'est donc
  continue que si les deux segments voisins ont la même pente Δvaleur/Δtemps, ce qui est rarement le cas dans les réactions.
- [D] La couture de boucle reste un repos **au centre** d'un balancier : sur `vitesses_avant_apres.png` (repos assis), la vitesse
  tombe à 0 aux extrêmes (950, 2 850 ms), ce qui est voulu, mais aussi à 0° toutes les 3,8 s, à la couture. Pour un animateur, un
  balancier qui s'arrête au point bas reste la signature du « stop and go », simplement 3 fois moins souvent.
- [F] `build.js` ignore `calcMode="linear"` (vapeur) : d'accord, corrigé côté Android seulement.

### 1.3 Pré-échantillonnage RT ≤ 300 points : **d'accord, effet faible**
- [F] `getFrameCount` (`AVD.java:1494-1506`) : pas = intervalle de trame **tronqué en ms et lu à la construction** (16, 11 ou 8),
  plafonné à 300 points avec log « Duration for the animation is too long » ; interpolation linéaire entre points.
- Nuance [D] : à 120 Hz (pas de 8 ms), toute boucle de plus de 2,4 s touche le plafond et loggue. C'est sans effet visible pour
  des boucles lentes, mais un clignement ne doit **jamais** entrer dans un animateur long : 29 s / 300 = 97 ms par point, un
  clignement de 150 ms y serait réduit à 2 points.

### 1.4 Respiration morte (arrondi à 0,1) : **d'accord, très bonne trouvaille, mais la corriger ouvre un risque d'alignement**
- [D] Vérifié dans le XML régénéré : `glose_assis.xml` a5 / `glose_assis_1.xml` a6 = scaleX 1 → 1,015 → 1, scaleY 1 → 1,025 → 1,
  pivot (500, 700). Flamme 0,94/1,06 : conforme au dessin.
- Pas d'accord sur « coût RT sans effet » : de mémoire AOSP (`libs/hwui/VectorDrawable.h`, macro de mutation des propriétés), une
  propriété de VD ne marque l'arbre sale que si sa valeur **change**. Un animateur mort coûte donc un `setFraction` par trame,
  pas une re-rastérisation. Le vrai coût est ailleurs (1.9).
- **Risque nouveau (non mesuré par l'audit)** [D] : au repos, l'écharpe, le nœud et le grelot sont des **VD statiques**
  (`GloseLayers.kt` : `glose_assis_vd_echarpe`, `_vd_echarpe_dos`, `_vd_noeud`, `_vd_grelot_ruban`). Avec la respiration rétablie,
  le corps grandit de 2,5 % en Y autour de y = 700 et la tête monte de 3 unités, alors que l'accessoire du cou ne bouge pas.
  Estimation au niveau du cou (y ≈ 450–500) : le corps monte de 5 à 6 unités, soit ≈ 2–2,5 px à 128 dp et ≈ 3,5 px à 200 dp, sous une
  écharpe immobile. C'est exactement la classe de défaut « mal aligné » que signale le propriétaire, et le prototype la **crée**
  là où la base (respiration morte) était alignée par accident. À mesurer avant fusion, puis à corriger (voir §2).

### 1.5 `stop()` = `end()` → saut à la clé 1 : **d'accord, confirmé dans le code**
- [F] `AVD.stop()` → `mAnimatorSet.end()` (`AVD.java:945-953`) → RT `nEnd` (`:1702`) → `BaseRenderNodeAnimator::end` → `Request::End` :
  `mPlayTime = mDuration`, puis `animate()` → `updatePlayTime(mDuration)` (`Animator.cpp:143-146, 241-243`) →
  `PropertyValuesAnimatorSet::onPlayTimeChanged` → `PropertyAnimator::setCurrentPlayTime` : `playTime >= mTotalDuration` ⇒ fraction 1
  (`PVAS.cpp`). Pour une boucle infinie, `mTotalDuration = 2³² × durée` ; la durée de l'ensemble est le max de ces totaux, donc
  **chaque** animateur reçoit sa fraction 1. (Détail : avec `repeatMode="reverse"`, l'itération 2³²−1 est impaire et la fraction
  s'inverse : on obtiendrait la clé 0. Glose n'utilise que `restart`.)
- [F] Échelle de durée : `startDelay *= getDurationScale(); duration *= getDurationScale()` (`AVD.java:1546-1547`), à la
  construction. Le minuteur `postDelayed(IDLE_MS)` n'était pas mis à l'échelle : d'accord.
- Nuance : `stop()` sert aussi à la **pause par appui long** (WCAG 2.2.2) et à `onPause`. Avec le prototype, toute pause atterrit
  sur l'image fixe : la pause de l'utilisateur fait donc sauter la pose (même amplitude que le toucher en plein cycle). On ne
  peut pas figer sur place sur RT (cf. 1.6). C'est à accepter ou à traiter comme le toucher.
- Sur le fil UI (`AnimatorSet.end()` qui ne termine pas les enfants infinis) : je n'ai pas relu le code. C'est mesuré sous
  Robolectric, et sans importance ici (Glose n'est jamais en repli logiciel).

### 1.6 Fin de réaction → vie, toucher en plein cycle : **d'accord**
- [F] `VectorDrawableAnimatorRT.pause()/resume()` = `TODO` vides (`AVD.java:1768-1775`). Il n'y a pas non plus de
  `setCurrentPlayTime` public sur un AVD : on ne peut ni figer ni chercher une position. « Repartir de la pose courante » est
  donc impossible sans pré-générer des variantes.
- Nuance d'animateur sur soir/réveil : quand on touche un chat qui dort, voir les « z » **disparaître** d'un coup est un vrai
  temps de jeu (sursaut). Ce qui est faux, c'est l'inverse : des « z » qui **réapparaissent** à pleine opacité au toucher, ou
  qui s'éteignent d'un coup au retour à la vie. Le correctif est dans les clés (réaction qui part avec les « z » à 0 et revient
  sur l'image 0 de la vie, #9), pas dans un fondu.

### 1.7 Liseré qui glisse : **d'accord** [D]
- Le correctif « le liseré refait tous les mouvements » est juste. La forme est discutable (+41 Ko de XML bruts, animateurs en
  ligne dupliqués) : voir §2.

### 1.8 Segments < 33 ms : **d'accord**, correctif trivial à faire tout de suite
- [D] Mon contrôle de vitesse retombe sur le même animateur (`glose_k_30307cef`, clés 0,26/0,28 et 0,38/0,40) : arrêt + sauts
  de vitesse ×2,6 et ×3,7.

### 1.9 Coût de dessin : **d'accord sur le mécanisme, chiffres téléphone à mesurer, et il manque quatre éléments**
- De mémoire AOSP (`libs/hwui/VectorDrawable.cpp`, `Tree::updateBitmapCache`) : quand l'arbre est sale, le cache bitmap est
  effacé (`eraseColor`) puis **tout** l'arbre est redessiné sur le CPU (canvas raster), dans une tranche de trace
  `VectorDrawable repaint <l>x<h>`, puis l'image est re-téléversée en texture. Il n'y a pas de rastérisation partielle.
  [F] La taille du cache suit la taille affichée × l'échelle du canvas, plafonnée à 2 048 (`VectorDrawable.java`, `draw`).
- [F] Un `PropertyValuesAnimatorSet` déclare `dirtyMask() = DISPLAY_LIST` (`PVAS.cpp:124`) : tant qu'un AVD tourne, le RT produit
  une trame à chaque vsync, à 60, 90 ou 120 Hz, pendant 26 s.
- [D] Les 4–9 ms mesurés sous Robolectric (Skia raster sur PC) sont un bon **ordre de grandeur relatif**, puisque la repeinture du
  VD est aussi faite sur le CPU au téléphone. Le facteur ×3 à ×6 du téléphone est [H].
- Manques :
  1. [D] Les vues de **200 dp** (fin de quiz `glose(…, 200)`, collection) font 525 px à 420 dpi, soit 2,4 fois les pixels de 128 dp :
     c'est le pire cas, à mesurer en priorité.
  2. [D] À 120 Hz, le budget tombe à 8,3 ms et le nombre de repeintures par seconde double.
  3. [D] Chaque calque sale est re-téléversé en entier à chaque trame : 336² × 4 = 450 Ko, 525² × 4 = 1,1 Mo par calque et par trame.
  4. [H] **Gonflement sur le fil UI** à chaque toucher et à chaque retour à la vie : `once()` appelle `dress(res)` puis
     `dress(idle)`, ce qui fait une dizaine d'AVD à parser (ou à cloner depuis le cache), le clonage des animateurs et le
     pré-échantillonnage Java (`createFloatDataPoints`, jusqu'à 300 évaluations de keyframes par propriété) au premier dessin.
     Cela peut faire sauter une trame **pile au toucher**, ce qui se lit comme une saccade de départ. À mesurer (§3).
- Piège de protocole dans le brief (§2.5) : pendant la vie au repos, le fil UI ne dessine rien et les trames sont produites par
  les animateurs RT. De mémoire AOSP (`FrameInfo.h`), elles portent `FLAGS = 2` (RTAnimation). Ne garder que `FLAGS = 0`, comme le
  conseille la doc, **jette précisément les trames à mesurer**. Garder les lignes 0 et 2.

### 1.10 Vérifications sans défaut : **d'accord**
- [F] Départ commun des calques : `addPendingAction(START_ANIMATION)` puis `recordLastSeenTarget` au prochain dessin
  (`AVD.java:1560-1628`). Tous les AVD d'un même `ImageView` sont animés par le même `RenderNode`, donc avec le même temps de trame.
- [F] `reset()` → `Request::Reset` → `updatePlayTime(0)` → tous les animateurs à la fraction 0, en ordre inverse (`PVAS.cpp`) :
  le `reset(); start()` de `play()` est correct, même sur un drawable déjà affiché.

### 1.11 Absent de l'audit
- [D] Respiration, hochement de tête (a1) et balancier de la pelote (a3) ont **la même période, 3 800 ms**, et partent ensemble :
  ils tournent en phase, d'où l'aspect mécanique. Il faut des durées premières entre elles (principe de la cigale, brief §1.9).
- [D] `velo_compare.txt` montre des régressions que l'audit ne commente pas : `glose_livres` patte, arrêts **0 → 5** ;
  `glose_soir` bougie **9 → 16** ; tête du spécial assis 1 → 3 ; tête du spécial écrit 0 → 2 ; à-coup de la tête au toucher
  4,5 → 5,8 (assis) et 4,4 → 5,5 (livres). Le dernier vient du `'out'` explicite qui n'est plus adouci par AccelerateDecelerate :
  un départ sec après l'anticipation, défendable en animation (« snap » hors de l'anticipation), mais à valider en vidéo.
  Les arrêts de la patte (livres) doivent être expliqués avant la fusion.

---

## 2. Revue du prototype `2f261b6..cafebba`

| Fichier / changement | Correct ? | Bonne pratique ? | Risques / remarques |
|---|---|---|---|
| `svg2avd.js` `android:interpolator="@android:interpolator/linear"` sur chaque animateur ; idem `reactions.js`, `make_android.js` (hoche/penche) | oui | oui (brief §2.3.1) | aucun ; à fusionner tel quel |
| `toFixed(3)` sur les valeurs de clés | oui (toutes les clés sont `floatType`, vérifié : pas de couleur ni de `pathData`) | oui | la respiration revit : voir l'accessoire du cou (§1.4), **bloquant tant que ce n'est pas mesuré** |
| `eases()` (ease aux repos, quad aux passages, linéaire entre deux passages) + auto-contrôle | oui pour un balancier symétrique | partiel (brief §2.3.2) | vitesse continue seulement si les pentes voisines sont égales : 26/40 clés de passage hors [0,5 ; 2] (§1.2) ; couture = repos au centre du balancier |
| `calcMode="linear"` respecté | oui | oui | aperçu SVG (`build.js`) toujours en ease : l'aperçu ne montre plus ce que joue Android |
| Boucles entières (`repeatCount` fini, `floor((26000 − offset)/durée)`) | oui, idée élégante : `end()` tombe sur la clé 1 = l'image fixe | oui (brief §2.2) | (a) une boucle plus longue que la vie (29 s) joue une fois et c'est le `stop()` à 26 s qui la coupe : sans saut seulement si sa valeur est au repos de 26 à 29 s. C'est vrai aujourd'hui, mais par chance : il faut une **assertion dans le générateur** ; (b) `IDLE_MS` est dupliqué en JS et en Kotlin : l'émettre dans `GloseLayers.kt` ou le vérifier ; (c) les parties se posent l'une après l'autre (clignement à 19,5 s, pelote à 22,8 s) : ce « ralentissement » est acceptable, voire joli |
| `pose_ecrit.js` : boucle qui commence mot écrit | oui : même timing décalé de 0,45 (tenu 2,17 s, rembobinage 0,98, caché 0,56, écriture 3,29) ; sens de rotation de la pelote cohérent (écrire = sens +) | oui | **saut déplacé, pas supprimé, dans `*_fin`** : vérifié dans `glose_ecrit_bravo_fin.xml`, pelote `[-150, -150, 0]` et tête `[-3, -3, 0]` (base : `[0, 0, 150]`). Au premier affichage c'est invisible (la vue apparaît déjà à cette image), mais tout `play()` de reprise (`onResume`) saute de 150° sur la pelote. Correctif : rotation de la pelote en multiples de 360° (une pelote est identique tous les 360°), ou clé de « rembobinage éclair » de 150–200 ms en tête des animations « une fois » |
| `glose_lib.js` `visible = () => true` | oui | oui (le liseré doit suivre sa partie) | +41 Ko bruts. Mieux : dédupliquer **toutes** les animations idle en `@animator/glose_k_*` par hachage, comme les réactions. Le liseré, la partie et le chapeau référencent alors la même ressource (synchronisés par construction) et le XML **maigrit**. `amp` est devenu du code mort |
| `Glose.kt` `IDLE_MS * ValueAnimator.getDurationScale()` | **non sur API 26–32** | intention bonne (brief §2.6) | [F] `getDurationScale()` n'est public qu'à partir de l'**API 33** (doc ValueAnimator), alors que `minSdk = 26`. Lint `NewApi` (non bloquant par défaut, mais vrai). À l'exécution sur 28–32, l'accès dépend des listes d'API cachées [H]. Les tests Robolectric ne le voient pas. Correctif en une ligne : `if (SDK_INT >= 33) ValueAnimator.getDurationScale() else Settings.Global.getFloat(v.context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)`. **Bloquant** |
| `reactions.js` `kfs()` (`'out'`/`'in'` explicites prioritaires sur `eases()`) | oui | partiel | les 6 arrêts nets restants viennent de ces `'out'`/`'in'` posés sur des clés de passage : relecture de données par l'animateur (choc voulu ou non ?) |
| Réactions : contrôle « fin = début » | inchangé | oui | soir/réveil : il faut « fin = image 0 de la vie » (#9), non fait |

Ce qui n'est pas dans le prototype et devrait accompagner la fusion : l'alignement des clés du squash sur celles de la tête
(#11, 3 nombres) et l'assertion « boucle > `IDLE_MS` ⇒ au repos sur [`IDLE_MS`, fin] ».

**Perte de caractère ?** [D] Non, au contraire. Le timing joué redevient celui écrit dans `reactions.js` (anticipation à 60 ms au
lieu de 120 ms, contact de la pelote à l'heure) et les balanciers retrouvent un poids. Deux points de vigilance d'animateur :
le départ de tête plus sec au toucher (à juger en vidéo) et une vie au repos plus ample, qui rend le toucher en plein cycle plus
visible (assis 2,3 → 4,8 px de médiane).

**Taille** [D] : +41 Ko de XML bruts, compilés en binaire dans l'APK (probablement moins). La déduplication proposée ci-dessus
devrait rendre le bilan négatif. Mesure : `apkanalyzer apk compare base.apk proto.apk` (ou `ls -l` des deux APK release).

**Correctif recommandé pour `eases()`** (remplace les trois cas par un seul, ~20 lignes de générateur) : tangentes monotones
(Fritsch–Carlson) à chaque clé, nulles aux extrêmes et aux tenues, puis chaque segment écrit comme Hermite cubique. Un segment
Hermite de pentes normalisées a (départ) et b (arrivée) **est exactement** la Bézier `pathInterpolator controlX1=1/3
controlY1=a/3 controlX2=2/3 controlY2=1−b/3`. En quantifiant a et b au pas de 0,25 sur [0 ; 3], on obtient au plus une quarantaine
de petites ressources `interpolator/glose_h_<a>_<b>.xml`. Résultat : vitesse continue (C1) partout, repos aux extrêmes,
pas de dépassement (monotone, important pour les alpha). Le contrôle « rapport ∈ [0,5 ; 2] » s'ajoute à l'auto-contrôle
`node android/svg2avd.js`.

---

## 3. Plan priorisé

### 3.1 À fusionner maintenant (une fois les 3 bloquants levés)
1. Interpolateur linéaire partout (#2, #10) ; `calcMode` linéaire (#14) ; 3 décimales (#7).
2. Boucles entières + minuteur mis à l'échelle (#4, #12), **avec la garde API 33** et l'assertion « boucle longue au repos après `IDLE_MS` ».
3. Boucle « écrit » qui commence mot écrit (#3), avec le correctif `*_fin` (pelote en multiples de 360° ou rembobinage éclair).
4. Liseré qui suit tout (#8), de préférence via la déduplication `@animator`.
5. Clés du squash alignées sur la tête (#11).
6. `eases()` tel quel en attendant l'Hermite (c'est déjà mieux que la base), à condition d'expliquer les régressions de
   `velo_compare` (patte « livres » 0 → 5 arrêts, bougie 9 → 16).

Bloquants avant fusion : (a) garde API 33 de `getDurationScale` ; (b) mesure de l'écart cou / écharpe, nœud, grelot avec la
respiration rétablie (≤ 1 px, sinon les accessoires du cou reçoivent le même animateur de respiration que le corps, comme les
chapeaux reçoivent celui de la tête) ; (c) régénération + tests verts.

### 3.2 À mesurer sur téléphone d'abord (base `2f261b6` contre `cafebba`)
Build **release non débogable** (ART dégrade le code débogable). Deux appareils : un 60 Hz d'entrée de gamme (Helio G/SD 4xx,
2–3 Go) et un 120 Hz. Scénarios : accueil jour/nuit, × sans accessoire et bonnet + écharpe, 25 s de vie au repos ; puis 10 touchers
+ 1 spécial ; puis fin de quiz (vue de 200 dp).

```sh
# lancement propre, sans connaître le nom de l'activité
adb shell am force-stop fr.definiday
adb shell monkey -p fr.definiday -c android.intent.category.LAUNCHER 1
sleep 2; adb shell dumpsys gfxinfo fr.definiday reset
# framestats ne garde que ~120 trames : relever toutes les 2 s pendant la vie au repos
for i in $(seq 1 12); do adb shell dumpsys gfxinfo fr.definiday framestats >> fs_idle.txt; sleep 2; done
adb shell dumpsys gfxinfo fr.definiday > gfx_idle.txt   # Janky frames %, 50/90/95/99th percentile
adb logcat -d -s AnimatedVectorDrawable:W | grep -c "too long"   # sous-échantillonnage (attendu à 120 Hz, sans gravité)
```
Dépouillement de `framestats` : dédoublonner par `INTENDED_VSYNC`, durée = `FRAME_COMPLETED − INTENDED_VSYNC`, **garder les
lignes FLAGS = 0 et FLAGS = 2** (trames poussées par les animateurs RT), écarter les autres (changement de fenêtre, trame sautée).
Le temps RenderThread seul = `FRAME_COMPLETED − SYNC_START`.

```sh
# trace système de 40 s (catégories hwui = « view »)
adb shell perfetto -o /data/misc/perfetto-traces/glose.pftrace -t 40s -b 64mb sched freq idle gfx view input
adb pull /data/misc/perfetto-traces/glose.pftrace
```
Requête `trace_processor` (ou onglet SQL de ui.perfetto.dev) :
```sql
select s.name, count(*) n, round(avg(s.dur)/1e6,2) avg_ms, round(max(s.dur)/1e6,2) max_ms
from slice s join thread_track tt on s.track_id = tt.id join thread t using(utid) join process p using(upid)
where p.name = 'fr.definiday' and t.name = 'RenderThread'
  and (s.name like 'VectorDrawable%' or s.name like 'Upload%' or s.name in ('DrawFrames','syncFrameState','flush commands'))
group by s.name order by avg_ms desc;
-- trames ratées vues par SurfaceFlinger
select count(*) total, sum(jank_type != 'None') janky from actual_frame_timeline_slice a join process using(upid)
where process.name = 'fr.definiday';
```
Au toucher : sur le fil principal, durée de `Choreographer#doFrame` / `inflate` entre l'événement d'entrée et la première trame
de la réaction (hypothèse 1.9-4). Fréquence d'écran : options développeur « Forcer la fréquence maximale », ou
`adb shell settings put system peak_refresh_rate 60.0` (dépend du fabricant). Vidéo à 240 i/s du téléphone (appareil externe :
`screenrecord` ne montre pas les trames sautées) pour juger les à-coups de courbe, invisibles dans gfxinfo.

### 3.3 À repenser
**a. Découpage des calques (seulement si la trace montre `VectorDrawable repaint` > ~4 ms par trame au p95 sur l'entrée de gamme).**
- Le générateur classe chaque groupe en « immobile » (dans la vie **et** dans les réactions) ou « animé ». Les immobiles contigus
  dans l'ordre de dessin forment des **VD simples** (jamais sales : rastérisés une fois). Les animés forment de petits AVD.
- Le vrai gain est de **recadrer** chaque AVD animé : le cache bitmap a la taille des bornes du calque. Une pelote dans un VD dont
  le viewport ne couvre que son enveloppe de mouvement (sur toute l'animation), placée par fractions de la boîte (un
  `DrawableWrapper` de ~15 lignes qui pose les bornes de l'enfant en fractions ; `setLayerInset` est en px fixes alors que la vue
  varie de 72 à 200 dp), repeint ~100² px au lieu de 336². Sans recadrage, multiplier les calques multiplie les caches pleine
  taille (450 Ko à 1,1 Mo chacun) et les téléversements : viser ≤ 3 calques sales par trame.
- Liserés dans un calque à part, ouvert par la porte de nuit (`GloseLayers.gates`) : le jour, ils ne sont ni dessinés ni animés.
- Contraintes : l'ordre de dessin entrelacé (pelote entre les bras) impose des coupes ; les parties immobiles deviennent
  partageables entre la vie et les réactions (moins d'octets).

**b. Toucher en plein cycle (et pause par appui long).** Dans l'ordre :
1. Corriger les clés soir/réveil (les « z » partent à 0, la réaction finit sur l'image 0 de la vie). Ça supprime les sauts d'alpha,
   les plus visibles.
2. Juger les sauts de position restants (médiane 3–5 px, max ~9 px) en vidéo 240 i/s : ils tombent au moment où la réaction
   démarre et sont en grande partie masqués par elle.
3. S'ils restent visibles : **fondu de 100–120 ms par vue superposée**, c'est-à-dire garder l'ancien `LayerDrawable` dans un
   `ImageView` frère (même `FrameLayout`) et animer son `View.alpha` 1 → 0 (rendu hors écran du groupe, donc un alpha correct),
   puis le retirer. ~20 lignes. Pas de `TransitionDrawable` : il applique l'alpha calque par calque, et le corps se verrait à travers
   l'écharpe à 50 %. Coût : les deux piles repeintes pendant 6–7 trames.
4. Écarté : pré-générer la réaction pour N phases de la vie (taille ×N) ; attendre la prochaine couture (latence > 150 ms).

**c. Variation de la vie au repos, dans les limites du RT.** Kotlin ne peut pas lancer **une partie** d'un AVD. Un AVD séparé ne
peut pas hériter des transformations animées de la tête d'un autre AVD. Donc des clignements tirés au hasard par `postDelayed`
ne sont possibles que pour des éléments dont les parents sont immobiles : reflets, « z », flamme, vapeur, étincelles. Pour les
yeux, les oreilles et la queue, l'irrégularité se **cuit à la génération** :
- plusieurs animateurs **courts** sur la même propriété, chacun avec son `startOffset`. Le RT les gère : avant son délai un
  animateur n'écrit rien, après sa fin il écrit sa clé 1 une seule fois (`PVAS.cpp:146-171`). Clignement de 140–180 ms
  (fermeture 60–70 ms en accélérant, ouverture 90–110 ms en décélérant), intervalles tirés d'un aléa à graine dans [2 ; 10] s,
  parfois un double clignement. Chaque animateur court garde l'échantillonnage à 16 ms ;
- périodes premières entre elles pour les boucles de fond (ex. respiration 3 800, pelote 4 300, roulis de tête 6 700, queue 7 300 ms)
  au lieu de trois boucles calées sur 3 800 ms ;
- balancier : **intro / boucle / sortie** en trois animateurs enchaînés par `startOffset` (0 → 6° en quart de période, boucle
  6 → −6 → 6 en ease-in-out, sortie 6 → 0°). Il ne s'arrête qu'aux extrêmes pendant la vie et part ou se pose au repos aux
  deux seuls bouts. Possible seulement maintenant que les boucles sont finies ;
- en option, 2 ou 3 variantes cuites par pose, tirées par `play()` sans jamais répéter la précédente, si le budget APK le permet.

### 3.4 Critères d'acceptation
| Domaine | Critère | Seuil |
|---|---|---|
| Statique (générateur) | animateurs sans interpolateur | 0 |
| | clés de passage : rapport vitesse sortante / entrante | ∈ [0,5 ; 2] pour 100 % (aujourd'hui 14/40) |
| | segments en mouvement < 33 ms | 0 |
| | arrêt à `IDLE_MS` (et à `IDLE_MS` × 0,5 et × 2), fin de réaction → image 0 de la vie | ≤ 0,5 px, Δalpha ≤ 0,01 |
| | liseré / partie ; accessoire du cou / corps | ≤ 0,5 px ; ≤ 1 px |
| | toucher en plein cycle | médiane ≤ 3 px, max ≤ 6 px, aucun alpha qui apparaît (sinon fondu 3.3b) |
| Téléphone 60 Hz, entrée de gamme | trames ratées (gfxinfo, vie au repos 25 s + 10 touchers) | < 1 % ; p90 ≤ 16,7 ms ; p99 ≤ 25 ms |
| | temps RenderThread par trame (p95) | ≤ 8 ms (la moitié du budget) |
| | `VectorDrawable repaint` cumulé par trame (p95), vue de 200 dp comprise | ≤ 4 ms |
| | fil UI à la trame du toucher (`doFrame`) | ≤ 16 ms ; 1re image de la réaction ≤ 2 trames après l'entrée |
| Téléphone 120 Hz | mêmes critères au budget 8,3 ms | < 1 % ; p99 ≤ 12,5 ms |
| Vidéo 240 i/s | pause visible entre clés, « pop » au changement de drawable | 0 |
| Poids | APK release | ≤ +50 Ko par rapport à la base |

---

## 4. Verdict

**Audit : solide.** Les mécanismes sont justes et vérifiés dans le code AOSP, et les chiffres que j'ai recalculés sont exacts.
Trois corrections : un animateur mort ne coûte pas de repeinture ; le protocole gfxinfo doit garder les trames `FLAGS = 2` ;
il manque le pire cas (200 dp, 120 Hz) et le gonflement au toucher.

**Prototype `cafebba` : prêt à fusionner après trois corrections**, toutes petites :
1. Garde API 33 sur `ValueAnimator.getDurationScale()` (repli `Settings.Global.ANIMATOR_DURATION_SCALE`).
2. Mesure, puis correction si > 1 px, du glissement corps / écharpe, nœud, grelot causé par la respiration rétablie (les
   accessoires du cou reçoivent l'animateur du corps).
3. `*_fin` d'« écrit » : supprimer le saut de 150° de la pelote et de 3° de la tête en tête d'animation (pelote en multiples de
   360°, ou rembobinage éclair), plus l'alignement des clés du squash (#11) et l'assertion « boucle longue au repos après
   `IDLE_MS` ».

Puis, **avant toute refonte des calques**, mesurer base et `cafebba` sur deux téléphones (§3.2). Le « saccadé » a deux causes
indépendantes : les courbes (corrigées en grande partie ici, invisibles dans gfxinfo) et la repeinture CPU des VD (non traitée,
visible seulement dans gfxinfo et Perfetto). Le découpage et le recadrage des calques ne se justifient que si la trace dépasse
les seuils du §3.4. Ensuite viennent l'Hermite C1 pour les clés de passage, les clés soir/réveil (#9) et la vie au repos
« intro / boucle / sortie » à périodes premières.
