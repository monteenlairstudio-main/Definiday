# Équipe GLOSE, itération 2 : liseré de nuit + humeurs (commités)

Branche `worktree-agent-a6e0a1c6e8991b760`, base `df2c794`.

| Commit | Contenu |
|---|---|
| `e2a68ec` gla: night outline behind Monsieur Glose (gloseHalo) | étape 1 du plan |
| `c5d3da4` gla: moods on Monsieur Glose's face (proud while writing, curious on the pile) | étape 2 du plan |

`.graphene/` non touché (consigne) : le hook demande de mettre à jour les nœuds `mascot` et `main-ui`, c'est à faire à l'intégration.

## Étape 1 : liseré de nuit
- `glose_lib.js` : `halo(root, anims, color, h)`. Derrière le dessin, l'union des contours d'encre (épaisseur OUT) élargie de 12
  (≈ 2 dp à 128 dp), un chemin par groupe animé ou touché par une réaction (`REACTED` : tete, dormeur, pelote, patte, queue, zzz) dans
  un groupe `<id>_halo` qui garde le même pivot (`data.pivot`). Il ne recopie que les animateurs qui bougent plus que le liseré
  (rotation ≥ 2°, translation ≥ 8, échelle ≥ 5 %, opacité toujours) : la respiration et les petits roulis restent couverts.
  Les traits ouverts qui sortent de la silhouette (moustaches, brins, « z ») sont élargis. Le fil qui écrit reçoit son liseré juste derrière
  lui, découpé hors de la silhouette (masque réduit au cadre du fil). Il reste ainsi dans le calque Android du mot, et
  `glose_ecrit_mot_vd*` reste commun à tous les mots.
- `build.js` : variante `{halo, haloW}` ; `android/make_android.js` : export `{all:true, halo:'#F0E801'}` (sentinelle ; icônes h = 8) ;
  `android/svg2avd.js` : jeton `#f0e801 → ?attr/gloseHalo` ; `animated()` anime aussi `<cible>_halo` quand il existe. Les réactions au
  toucher et le hochement ou la tête penchée du quiz font donc bouger le liseré avec la tête, la pelote, la patte, le dormeur et les « z ».
  `android/reactions.js` : la queue de la pile (cible anonyme `aN`) a aussi son double `queue_halo`.
- Style `Glose` : `gloseHalo = @color/glose_halo` ; `values/colors.xml` : `#00F0E8D8` (transparent) ; `values-night/colors.xml` : `#F0E8D8`
  (encre du thème nuit). Aucun Kotlin modifié en dehors de `GloseLayers.kt` (régénéré).
- `android/apercu_reactions.js` : ids de découpe/dégradé uniques dans la page (les calques empilés partageaient `c0` : le liseré du mot était
  coupé dans l'aperçu, pas dans l'appli).
- Pas de liseré sur les accessoires, le ruban ni les détails rares (pièces à porte) : étape 7 du plan.

## Étape 2 : humeurs
- `gloseHead(C, {mood})` : `fier` = bouche ouverte en « D » (gorge #7A3A3E, langue `C.rose`) + sourcils arqués ; `curieux` = sourcil
  lointain levé + petite bouche « o ».
- « le fil écrit », **tous les mots** en `fier` : la tête vit dans `glose_ecrit_mot_vd_2`, commun à tous les mots. Seuls 2 dessins changent
  (`glose_ecrit_mot_vd_2`, `glose_livres_vd_2`). APK +16 octets par rapport à l'étape 1.
- « doux » pour `encore` : **écarté**. Une tête propre à un mot sort ce mot de la partie commune : il faudrait un `_vd_2` à lui, ses AVD
  idle/tap/special/fin, ≈ +10 à 12 Ko par mot, et le budget restant était de 3,6 Ko. Le « D » encourageant se lit bien aussi pour « encore ».
- La pile passe en `curieux`. Assis, soir, réveil, icônes : inchangés.
- SVG de conception régénérés pour les poses touchées (`glose_ecrit_*.svg`, `glose_livres.svg`, variantes `ecrit`/`livres`).

## Mesures
- **APK release** : 1 975 606 → 2 033 374 octets, **+57 768 o (+56,4 Kio)**, sous le budget de 60 Ko. Premier essai à +79 Ko, ramené par
  le masque réduit au cadre du fil (au lieu de toute la silhouette) et par la suppression des animateurs en double sous l'amplitude du
  liseré. Le reste du coût : chemins du liseré dans les calques arrière (+2,4 à +3,5 Ko par pose), animateurs des réactions doublés
  (+0,4 à +1,9 Ko), `resources.arsc` +4,7 Ko (10 AVD de réaction qui étaient des alias deviennent des fichiers : leur calque arrière porte
  maintenant des cibles). Outils : `night/glose_2/apknames.py avant.apk etape2.apk`.
- **Rendu jour identique au pixel** (Robolectric, vidage temporaire non commité `GloseDumpTest`) : 441 images (chaque dessin glose_*
  seul et avec chaque surcouche à 256 px, plus chaque pose seule à 48/96/160 dp en xxhdpi). Étape 1 contre base : 441/441 identiques.
  Seule la planche composée diffère, et elle diffère aussi entre deux vidages de la **même** base (cache du VectorDrawable qui dépend de
  l'ordre des rendus) : ce n'est pas un changement de dessin.
  Étape 2 contre étape 1 : seuls `ecrit` (248) et `livres` (32) changent, comme voulu. `assis`, `soir`, `reveil` et `icone` sont identiques.
- **Tests** (ciblés, sous flock) : GloseTest 15, GloseTapsTest 3, DefinidayTest 19, BugHuntTest 24, ProfileTest 9, VoixTest 3 : tous verts.

## Planches (`night/glose_2/`)
- `avant_apres_nuit.png`, `avant_apres_jour.png` : rendus Robolectric de l'appli, 48/96/160 dp, avant (df2c794) | après.
  La nuit, le chat et son socle se détachent du papier dès 48 dp et le mot écrit redevient lisible. Le jour, seuls les visages de « écrit »
  et de la pile changent.
- `react_nuit_{assis,livres,soir,reveil,ecrit_mot}.png` : aperçu Chromium des réactions au toucher, de nuit. Le liseré suit la tête,
  la pelote qui roule, la patte, le dormeur, les « z » et le mot qui se réécrit.
- `zoom_etape1.png` : écrit « bravo » et soir avec bonnet, de nuit (le bonnet n'a pas encore de liseré).
- Vidages bruts : `dump_avant2/`, `dump_etape1b/`, `dump_etape2/` (jour, nuit) ; comparaison : `cmp.py`.

## Suite
Étape 3 du plan (icône d'appli générée avec liseré), puis 7 (liseré des accessoires, en surveillant le poids) et 4 (socle allégé).
