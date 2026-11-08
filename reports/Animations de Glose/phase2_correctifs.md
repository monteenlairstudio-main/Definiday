# Animations de Monsieur Glose, phase 2 : corrections (bloquants de la revue experte, puis points 4 à 6)

Worktree `agent-a9760a5e470864021`, branche `worktree-agent-a9760a5e470864021`, partie du prototype `cafebba` (base `2f261b6`).
Commits : `53020ef` (API 33), `beee3d0` (cou), `3d52f2e` (`*_fin`), `5a41120` (squash), `12c8cb0` (auto-contrôle, « z »),
`f7fcbd8` (Hermite), `34717c8` (animateurs partagés, poids). Rien poussé (GitHub injoignable). Bilan chiffré : §8.
Écrit au fil de l'eau. Mesures : outils de la phase 1 (`anim/model.py`, `measure.py`, `events.py`, `velo.py`, `halo.py`,
`shortseg.py`), avant = `anim/before_main` (res de `2f261b6`), après = `app/src/main` du worktree. px = ImageView 128 dp à 420 dpi (336 px).

## 1. Bloquant (a) : `getDurationScale()` sur API 26–32 — commit `53020ef`

- `Glose.durationScale(ctx)` : `ValueAnimator.getDurationScale()` si `SDK_INT >= 33`, sinon
  `Settings.Global.getFloat(…, ANIMATOR_DURATION_SCALE, 1f)` (la même valeur, que le système recopie dans `ValueAnimator`).
  `play()` l'utilise pour l'arrêt de la vie au repos.
- Test `GloseTest.idleStopFollowsTheDurationScale` : branche 33+ (échelle forcée à 2 par `setDurationScale`), puis branche < 33 en
  forçant `Build.VERSION.SDK_INT = 28` (Robolectric n'a ici que l'image Android 16 hors ligne) : réglage absent → 1, réglage 0,5 → 0,5.

## 2. Bloquant (b) : accessoires du cou et respiration rétablie — commit `beee3d0`

Mesure (`anim/neck.py`) : déplacement d'un point de l'accessoire par sa chaîne d'animateurs moins son déplacement par la chaîne du corps
qui le porte (corps ; « dormeur » pour nœud et grelot du soir), max sur 13 s de vie au repos, pas 50 ms.

| Pose | Base `2f261b6` (respiration morte) | Prototype `cafebba` | Après |
|---|---|---|---|
| assis | 0 | écharpe **2,85**, dos 2,27, nœud 2,56, grelot 2,14, ruban 2,47 | **0,00** partout |
| livres (pile) | 0 | écharpe 1,83, nœud 1,74, grelot 1,36, ruban 1,73 | **0,00** |
| écrit | 0 | écharpe 2,59, dos 2,07, nœud 2,29, grelot 1,89, ruban 2,20 | **0,00** |
| soir / réveil | 0 | écharpe 1,72 (nœud, grelot : dans « dormeur ») | **0,00** |

La prédiction de l'expert est confirmée (> 1 px partout). Correctif (`glose_lib.js` `neckBreath()`, appelé par `seatedGlose`,
`pose_livres.js`, `pose_soir.js`) : chaque pièce du cou reçoit l'animation de respiration du corps (même pivot, mêmes valeurs, même
période), donc un animateur identique. Les images fixes ne changent pas (groupes d'animation à l'identité au repos).

Reste, hors critère et déjà dans la base : la **tête** bouge par rapport au nœud et au grelot sous le menton (roulis ±2,5°, respiration −3,
bâillement −7) : 1,4 à 2,9 px (base 1,2 à 4,4), écharpe/tête jusqu'à 6,2 px (pile). C'est le cou qui se plie au-dessus du col : à juger
en vidéo, pas un défaut d'alignement.

## 3. Bloquant (c) : `*_fin` (fin de quiz, « écrit ») qui sautait au départ — commit `3d52f2e`

Le prototype faisait commencer l'animation « une fois » à la clé « mot caché » (pelote −150°, tête −3°, mot effacé), alors que le dessin
fixe (affiché avant le départ, après une pause, et que `stop()` retrouve) a pelote 0°, tête 0° et le mot écrit.
Choix : **rembobinage éclair** plutôt que des tours entiers de pelote (un tour de 360° aurait gardé la pelote, mais pas le mot, qui
disparaissait d'un coup). L'animation part de la clé « rembobinage » de la boucle (= le fixe), ce segment est ramené à 200 ms
(`REWIND_MS`, `make_android.js`), puis tenue mot caché 560 ms et écriture 3 290 ms comme avant (total 4 050 ms au lieu de 3 850).
`svg2avd.js` `onceTimes()`.

Mesure (`anim/fin.py`, modèle RT, px à 128 dp) :

| `glose_ecrit_bravo_fin` / `_encore_fin` | Base `2f261b6` | Prototype `cafebba` | Après |
|---|---|---|---|
| image 0 / dessin fixe | 0 (le fixe de la base avait le mot caché) | **81,3 px** (pelote), mot effacé | **0,00 px**, mot écrit (trim 1) |
| dernière image / fixe | 81,3 px | 0 | **0,00 px**, mot écrit |
| `stop()` / fixe | 81,3 px | 0 | **0,00 px** |

Vitesse du rembobinage : pelote 16 px/image au pic à 128 dp (≈ 21°/image), 25 px/image à 200 dp : c'est un « coup sec » voulu
(150° en 200 ms), au-delà du repère de la checklist (12°/image). À juger en vidéo ; `REWIND_MS` est le réglage (300 ms ⇒ ≈ 14°/image).

## 4. Clés du squash alignées sur celles de la tête — commit `5a41120`

`reactions.js` : toucher (3 poses éveillées) squash 0,26 → 0,28, 0,38 → 0,40, 0,9 → 0,85 ; spécial « pile » 0,86 → 0,85.
`animators()` refuse désormais tout segment qui bouge en moins de 33 ms. `shortseg.py` : **10 segments < 33 ms → 0**.

## 5. Auto-contrôle du générateur, « z » et vapeur du soir — commit `12c8cb0`

Constat nouveau en corrigeant le modèle : sur le RenderThread, un animateur **n'écrit rien avant son `startOffset`**
(`PVAS.cpp` `setCurrentPlayTime` : `if (playTime < mStartDelay) return;`). Le modèle de la phase 1 lui donnait sa clé 0 : il ratait donc
un saut réel. Les boucles que le SVG fait partir en milieu de cycle (`begin` négatif : 2e et 3e « z », 2e volute de vapeur) étaient
traduites en départ retardé ; avant leur délai elles montraient la valeur du fixe (alpha 1 pour les « z », 0,3 pour la vapeur, à leur
place du dessin), puis sautaient à leur clé 0 (alpha 0). Modèle corrigé (`model.py` ; l'ancien gardé dans `model_phase1.py`), base
`2f261b6` : **un « z » passe d'alpha 1 à 0 d'un coup à 2,4 s** (soir), la vapeur de 0,3 à 0 à 2,2 s (réveil).

Correctifs :
- `svg2avd.js` `phase()` : une boucle à `begin` négatif a ses clés **tournées** de sa phase (elle part en milieu de cycle, comme dans
  l'aperçu SVG) au lieu d'un départ retardé. Le saut de couture d'une particule (« z » qui revient en bas) reste, long de 0,001 de
  cycle, caché par l'alpha 0 comme dans la boucle. La couture d'une boucle tournée est un passage, pas un repos (`cyclic`).
- `pose_soir.js` : sachet `[−4, 4, −4]` → `[0, 4, 0, −4, 0]`, lueur de bougie `[0,8 ; 1 ; 0,85 ; 0,8]` → `[1 ; 0,85 ; 0,8 ; 1]` :
  mêmes mouvements, qui partent de leur valeur du fixe (le sachet sautait de 3,1 px en fin de réaction).
- `reactions.js` `settle()` : chaque réaction finit sur **l'image 0 de la vie** (et non sur le fixe). Ce que la vie montre et cache
  (« z », vapeur : alpha qui descend à 0) part de 0 au toucher et apparaît en 120 ms (il disparaît au toucher : un sursaut de dormeur,
  jamais un « pop » qui apparaît), puis rejoint ses valeurs de l'image 0 de la vie dans les 150 dernières ms.
- Auto-contrôles de `make_android.js` (le générateur échoue sinon) : (1) une boucle retardée part de sa valeur du fixe ; (2) à `IDLE_MS`
  chaque boucle a joué des cycles entiers, ou (boucle plus longue que la vie : bâillement, oreille, 29 s) ne bouge plus à partir de cet
  instant ; (3) chaque propriété d'une réaction finit sur l'image 0 de la vie (le fixe si la vie ne l'anime pas) ; (4) aucun segment de
  clés de réaction en mouvement de moins de 33 ms (point 4 ; dans la vie, restent 5 coutures de particules de 4 ms, « z » et vapeur à
  alpha 0, voulues). À l'échelle d'animation ×0,5 ou ×2, animateurs et arrêt changent d'échelle ensemble : mêmes fractions, mêmes
  résultats.
- Conséquence voulue : au repos (après 26 s ou une pause), la pose du soir garde la 2e et la 3e « z » et une volute (l'image 0 de la
  vie), là où le fixe (animations coupées) montre les trois « z » et les deux volutes. Le premier affichage ne montre pas le fixe :
  l'AVD démarre au premier dessin (`addPendingAction`).

## 6. Nouveaux arrêts de `velo_compare.txt`, puis Hermite monotone — commit `f7fcbd8`

**Les « arrêts » nouveaux du prototype ne sont pas des régressions.** Repérés dans les courbes (`velo_after.json`) :
- patte de la pose « pile » (0 → 5) : les 5 arrêts tombent tous à 458 ms du cycle de 2 600 ms, c'est-à-dire sur la clé 0,18 (468 ms),
  valeur +4°, **l'extrême** où la patte repart en arrière (0 → 4 → −2 → 0). La vitesse y est nulle par nature. Avant, l'AccelerateDecelerate
  implicite ralentissait tout le début du cycle : le mouvement avant cette clé restait sous le seuil « > 50 % du pic » du compteur.
- bougie (9 → 16) : arrêts à 425 et 858 ms du cycle de 1 700 ms = clés 0,25 et 0,5, les deux **extrêmes** du scintillement
  (0,9/1,1 puis 1,06/0,94, ce dernier enfin écrit au lieu de 1,1/0,9). Même mécanisme : deux inversions par cycle au lieu d'une comptée.
- Même lecture pour les touchers (corps 5 → 7) : le squash est une oscillation amortie, chaque clé est un extrême.

Le compteur `halts` de `velo.py` compte les inversions ; le critère pertinent est **l'arrêt à une clé de passage** (valeur strictement
entre ses voisines), contrôlé à part par `anim/passratio.py` (dérivées des interpolateurs de clé à gauche et à droite de chaque clé de
passage, timeline linéaire) :

| | Base `2f261b6` | Prototype `cafebba` | Après |
|---|---|---|---|
| clés de passage | 39 | 40 | 31 (segments de 12 ms supprimés) |
| rapport vitesse sortante / entrante hors [0,5 ; 2] | 39 (toutes : arrêt) | **26** (dont 6 arrêts nets) | **0** |
| arrêts nets à une clé de passage | 39 | 6 | **0** |

Correctif : `svg2avd.js` `hermite()` remplace `eases()`. Tangente de Fritsch–Carlson (PCHIP) à chaque clé : 0 aux extrêmes, tenues et
bouts ; moyenne harmonique pondérée aux passages ; boucle tournée (point 5) : la couture est un passage. Chaque segment de tangentes a, b
(en unités de sa pente moyenne) **est** la Bézier `pathInterpolator (1/3, a/3, 2/3, 1 − b/3)` ; a et b quantifiés sur
{0 ; 0,125 ; 0,25 ; 0,375 ; 0,5 ; 0,625 ; 0,75 ; 1 ; 1,25 ; 1,5 ; 2 ; 2,5 ; 3} → **19 ressources** `interpolator/glose_h_<a>_<b>.xml`
(≤ 40). Repos → repos garde `glose_ease` ; (2, 0) et (0, 2) sont `decelerate_quad` / `accelerate_quad` ; (1, 1) linéaire.
Les `'out'`/`'in'` de `reactions.js` restent un départ sec d'un repos (a = 2 : le « snap » hors de l'anticipation) ou un choc sur un
repos (b = 2 : pelote qui touche le livre), jamais à une clé de passage. Le générateur refuse une clé de passage dont le rapport, après
quantification, sort de [0,5 ; 2] ; auto-contrôle `node android/svg2avd.js` → `hermite ok`.

Vitesses (modèle RT, `phase2/velo_compare_s6.txt`, base → après) : pelote du spécial « assis » 18,8 → 12,3 px/image, pelote au sol du
spécial « pile » 16,6 → 10,6, dormeur du spécial « réveil » 7,9 → 4,9 (arrêts 7 → 2), corps du spécial « écrit » arrêts 6 → 1.
À-coup de la tête au toucher 4,5 → 5,7 (assis) : départ sec voulu après l'anticipation (`'out'`), plus adouci par AccelerateDecelerate.

## 7. Poids : animateurs de la vie partagés en ressources — commit `34717c8`

Premier APK release mesuré après le point 6 : **2 084 108 o, soit +57,9 Ko** par rapport à la base (2 026 216 o) : au-delà du budget
(+50 Ko). Cause : chaque animateur en ligne (`aapt:attr`) devient un fichier de ressource à part dans l'APK ; il y en avait 279 pour 41
animateurs distincts (une partie, son liseré, ses copies sous chaque chapeau et accessoire), et le point 2 en ajoutait. Correctif
(proposé par l'expert) : `svg2avd.js` écrit chaque animateur de la vie une seule fois, `animator/glose_k_<hachage>`, comme ceux des
réactions, et les AVD les référencent ; le liseré et sa partie jouent la même ressource (synchronisés par construction).
**APK release : 1 925 050 o, soit −101 166 o (−98,8 Ko) par rapport à la base.**

## 8. Mesure finale : base `2f261b6` / prototype `cafebba` / après (HEAD `34717c8`)

Modèle RT corrigé (délai = valeur du fixe) pour les trois colonnes ; `phase2/events2_{before,proto,after}.txt`, `static_all.txt`,
`passratio.txt`, `velo_compare_s6.txt`. « px » = pire partie, ImageView 128 dp à 420 dpi.

| Critère (revue §3.4) | Seuil | Base | Prototype | Après | |
|---|---|---|---|---|---|
| animateurs sans interpolateur (AccelerateDecelerate implicite) | 0 | 297 | 0 | **0** | ok |
| clés de passage : rapport vitesse sortante / entrante hors [0,5 ; 2] | 0 | 39/39 (arrêts) | 26/40 | **0/31** | ok |
| segments de clés en mouvement < 33 ms | 0 | 10 | 10 | **0** dans les réactions ; 5 coutures de particules à alpha 0 | ok |
| arrêt à 26 s (×0,5, ×2 : mêmes fractions) | ≤ 0,5 px, Δalpha ≤ 0,01 | écrit 81,3 ; pile 7,1 ; soir 3,0 + « z » alpha 1 ; réveil vapeur 0,3 | 0 | **0,00 px, 0,000** (5 poses + accessoires) | ok |
| fin de réaction → image 0 de la vie (toucher et spécial) | ≤ 0,5 px, Δalpha ≤ 0,01 | soir/réveil 3,1 px (sachet), « z » alpha 1, vapeur 0,3 ; écrit spécial : mot | soir/réveil 3,1 px + alpha 1 / 0,3 | **0,00 px, 0,000** | ok |
| sauts internes à la vie (départs retardés) | — | « z » alpha 1 → 0 à 2,4 s ; vapeur 0,3 à 2,2 s | idem | **Δalpha max 0,034 / image** (fondu normal) | ok |
| `*_fin` : départ / fin / `stop()` vs fixe | ≤ 0,5 px | 0 / 81,3 / 81,3 | 81,3 / 0 / 0 | **0 / 0 / 0**, mot écrit | ok |
| liseré / partie au repos | ≤ 0,5 px | tête 3,3–3,9 | 0 | **0** (réveil « dormeur » 2,6 px : artefact connu, chaînes de groupes différentes ; tête/liseré 0) | ok |
| accessoire du cou / corps | ≤ 1 px | 0 (respiration morte) | 1,4–2,9 | **0,00** | ok |
| toucher en plein cycle, médiane | ≤ 3 px | assis 2,2 ; pile 1,7 ; soir 7,6 ; réveil 7,1 ; écrit 36,0 | 4,8 ; 3,6 ; 8,0 ; 7,5 ; 4,4 | **4,2 ; 3,3 ; 1,9 ; 1,9 ; 4,2** (assis + acc. 4,6) | **non** (éveillés) |
| toucher en plein cycle, max | ≤ 6 px | 8,7 ; 7,4 ; 17,6 ; 17,6 ; 81 | 8,4 ; 7,4 ; 14,8 ; 14,8 ; 81 | **8,3 ; 7,4 ; 3,3 ; 5,1 ; 81** | **non** (éveillés, écrit) |
| toucher : alpha qui apparaît d'un coup | 0 | soir 1,0 ; réveil 0,3 | idem | **soir 0,2** (lueur de bougie au creux de son scintillement) ; « z », vapeur : 0 | presque |
| APK release vs base | ≤ +50 Ko | 2 026 216 o | — | **1 925 050 o (−98,8 Ko)** | ok |
| images fixes de jour (`renderControlImages` : poses, accessoires, combinaisons) | inchangées | — | — | **0 pixel changé** sur les 3 planches | ok |
| tests GloseTest 20, GloseTapsTest 3, DefinidayTest 20, OnboardingTest 6, UiTest 20 | verts | | | **69/69 verts** | ok |

Toucher en plein cycle : la vie au repos est plus ample (respiration rétablie, tête qui roule, Hermite qui ne s'arrête plus aux
passages), et une réaction ne peut partir que du fixe (pas de `setCurrentPlayTime` ni de pause sur RT). Les seuils ≤ 3 / ≤ 6 px ne
sont atteignables, pour les poses éveillées, qu'avec le fondu de 100–120 ms par vue superposée (revue §3.3b, ~20 lignes Kotlin, non
fait ici). « Écrit » : 81 px quand on touche pendant la phase « mot caché » de la boucle (pelote à −150°) : la réaction repart du mot
écrit ; même remède. Le soir et le réveil passent sous les seuils grâce au point 5.

Images Android réelles (Robolectric, test temporaire `phase2/GloseAnimAuditTest.kt.txt`, non commité ; images dans `phase2/frames`,
`phase2/image_metrics.txt`) : toucher → retour à la vie **0 pixel changé** pour toutes les séquences de toucher (soir : 2 155 px avant) ;
arrêt à 26 s 0 px (jour et nuit, 5 poses) ; fixe → première image de `*_fin` **0 px**. Spéciaux soir / réveil → vie : 758 / 40 px
(jour) : la dernière image échantillonnée tombe 17 ms avant la fin (pas de 16,7 ms), en plein dans les 150 ms de raccord ; le modèle,
lui, donne 0 à la fin exacte. Sur les planches `seq_*`, les teintes rouges ou roses vives sur la vapeur et les « z » en fondu viennent
de `strips.py` (alpha ignoré : couleur pleine d'un pixel presque transparent), pas du rendu.

## 9. Planches (regardées)

- `phase2/vitesses_phase2.png` : base (rouge), prototype (pointillé), après (bleu). Balancier de la pelote « assis » : plus d'arrêt au
  passage par 0° (creux à 0,10 px/image au lieu de 0), arrêt aux extrêmes et à la couture (3,8 s, voulu) ; patte « pile » : arrêt aux
  extrêmes seulement ; fin de quiz : le rembobinage de 200 ms (pic 16 px/image) puis l'écriture.
- `phase2/sauts_phase2.png` : arrêt à 26 s, fin de réaction (px et alpha), toucher en plein cycle (médiane, max), sauts d'alpha
  pendant la vie, par pose.
- `phase2/seq_{jour,nuit}_*.png` : séquences vie → réaction → vie ; `seq_*_soir_tap` : les « z » et la vapeur s'effacent au toucher puis
  reviennent en fondu, retour à la vie sans saut ; `seq_*_ecrit_bravo_fin_t0` : fixe → départ identique, rembobinage, mot réécrit.
- `phase2/stop26_*.png` : dernières images et `stop()`, identiques.

## 10. Reste à mesurer sur téléphone

1. Coût de rendu (audit 1.9, non traité) : `dumpsys gfxinfo … framestats` (lignes FLAGS 0 et 2), Perfetto `VectorDrawable repaint`,
   base / HEAD, 60 Hz entrée de gamme et 120 Hz, vue de 200 dp (fin de quiz) comprise. Les animateurs partagés ne changent pas ce coût
   (mêmes propriétés animées), l'APK et le nombre de ressources à gonfler baissent.
2. Vidéo 240 i/s : départ sec de la tête au toucher (`'out'`), rembobinage éclair de la fin de quiz (≈ 21°/image), sursaut des « z » au
   toucher du dormeur, couture des balanciers (repos à 0° toutes les 3,8 s).
3. Gonflement au toucher (`once()` : 2 `dress()`), première image de la réaction ≤ 2 trames.
4. Échelle d'animation ×0,5 / ×2 sur un appareil API 28–32 (branche `Settings.Global`), puis API 33+.
5. 120 Hz : log « Duration for the animation is too long » attendu pour les boucles > 2,4 s (sans effet visible).
