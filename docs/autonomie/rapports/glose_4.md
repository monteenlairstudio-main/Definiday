# Équipe GLOSE, itération 4 : anticipation / squash & stretch, pause de la vie au repos (commités)

Branche `worktree-agent-a32a9883082163fed`, base `d934ab5`. `.graphene/` non touché (consigne) : le hook signale `mascot`, `main-ui`,
`onboarding`, `build-ci` : à mettre à jour à l'intégration.

| Commit | Contenu | APK release |
|---|---|---|
| `4daf429` gla: anticipation, squash and stretch in Glose's tap reactions | étape 6 du plan | 2 055 261 → 2 022 160 o (−33 101) |
| `65bfd75` gla: long press pauses or resumes Glose's idle life (TalkBack action) | décision du propriétaire (WCAG 2.2.2) | +652 o |
| **Total** | | **2 055 261 → 2 022 812 o : −32 449 o (−31,7 Kio)**, budget +15 Kio |

## 1. Anticipation, squash & stretch (`design/mascotte/android/reactions.js`)
- Nouveaux outils dans `reactions.js` : `squash(famille, sy)` écrase/étire le corps autour de sa base (scaleY = sy, scaleX = 1 + 0,6·(1 − sy),
  volume à peu près gardé) et fait suivre les parties posées dessus (translateY = (sy − 1)·k, k = hauteur au-dessus de la base) ;
  table `BODY` par famille : assis/écrit (`corps` base [500, 700] ; tête, pattes, pelote, brin, bras levé, fil bas, accessoires du cou),
  pile (`corps` [640, 655] ; tête, patte, cou), soir/réveil (`dormeur` [490, 620] : tête et pattes s'écrasent sur le bord de la tasse).
  `merge()` additionne les courbes de même propriété (somme pour translations/rotations, produit pour les échelles, pivot du premier).
- Toucher éveillé : il se tasse (60 ms), se redresse étiré, la tête va au-delà (7°) puis se pose (5°), clin d'œil, retour avec un petit
  écrasement à l'arrivée. Toucher endormi : léger soulèvement, puis blotti (écrasé), les « z » se tassent avant de sauter.
- Spéciaux : assis (sursaut tassé/étiré, la pelote s'écrase à chaque contact, il la rattrape en se tassant) ; pile (la patte recule avant
  la frappe, la pelote s'étire en montant et s'écrase en retombant, la tête dépasse puis se pose) ; soir (tassé au fond, étiré au-delà,
  retombe écrasé, gros « z » qui se tasse puis gonfle) ; réveil (se soulève, plonge écrasé, ressort étiré) ; écrit (la pelote tourne
  d'abord dans l'autre sens et dépasse au retour, corps tassé au rembobinage, étiré au premier trait).
- Durées inchangées (600 / 1 800 ms) : `Glose.Taps` (700 ms / 3 s d'immunité) intact. Le contrôle « fin = début » du générateur passe ;
  vérifié en plus sur les images Android : la dernière image de chaque réaction est identique au pixel à la première (jour et nuit,
  avec et sans accessoire).
- Liseré de nuit : `corps_halo`, `tete_halo`, `pelote_halo`, `dormeur_halo`, `zzz_halo` suivent (via `animated()`). Pattes, bras, brin
  n'ont pas de groupe de liseré : leur déplacement reste ≤ 8,4 (k choisis pour), sous les 12 du liseré fixe ; vérifié sur les zooms.
  Les accessoires du cou (écharpe, nœud, grelot) suivent la tête ; ceux de la tête étaient déjà dans « tete ».
- **Poids** : premier essai à **+80,9 Ko** (nouveaux calques animés × copies inline des animateurs). Correctif dans `svg2avd.js` :
  les animateurs des réactions sont écrits une seule fois, `res/animator/glose_k_<sha1 8>.xml`, et référencés
  (`android:animation="@animator/…"`) par tous les calques, poses et mots (avant : un `aapt:attr` inline = une ressource par copie ;
  la tête et son liseré sous 5 chapeaux = 7 copies). Résultat : −32 Ko au lieu de +81. `make_android.js` vide aussi `animator/glose_*`,
  `apercu_reactions.js` relit les animateurs partagés. Les AVD de la vie au repos (convert) restent inline : **aucun dessin au repos
  ne change** (git : seuls `*_tap*`, `*_special*`, `glose_icone_hoche/penche` et `GloseLayers.kt` diffèrent), donc rendus jour/nuit
  au repos identiques par construction.

## 2. Pause de la vie au repos (WCAG 2.2.2)
- Déjà en place : la vie au repos s'arrête seule après 26 s (`Glose.IDLE_MS`, image fixe) ; animations désactivées (échelle 0) :
  `play()` ne démarre rien et ne fait pas `reset()` (le mot reste écrit). Vérifié dans le code et par `tapsMakeGloseReact`.
- Ajout : `Glose.paused` + `Glose.pausable(v)`. Appui long sur Glose (accueil, bienvenue, collection : une ligne chacun) = pause /
  reprise ; pour TalkBack, l'action d'appui long est renommée « Mettre en pause » / « Reprendre » (aucun bouton visible ajouté).
  En pause, `play()` laisse l'image fixe partout jusqu'à la reprise ou au redémarrage de l'appli (pas de Prefs : fichier hors périmètre) ;
  les touchers font encore réagir Glose (mouvement demandé), puis il se repose. Animations désactivées : pas d'action, appui long ignoré.
  Les réactions ponctuelles du quiz (`once`) ne sont pas concernées.
- Chaînes (`values/strings.xml`) : `glose_pause` « Mettre en pause », `glose_resume` « Reprendre ».
- `MainActivity` : la ligne du clic devient `apply { setOnClickListener {…}; Glose.pausable(this) }`.
- Test `GloseTest.longPressPausesTheIdleLife` : libellé, arrêt, pause conservée après pause/reprise de l'activité, reprise, aucune action
  quand l'échelle d'animation vaut 0.
- Limite : TalkBack n'annonce pas le changement d'état (le libellé de l'action change) ; un `stateDescription` (API 30+) serait l'étape
  suivante si besoin.

## Vérifications
- Images Android réelles des réactions : test Robolectric temporaire non commité (`glose_4/GloseFramesTest.kt.txt`) qui force les
  animateurs de l'AVD sur le fil UI (`forceAnimationOnUI`, caché) puis avance l'`AnimatorSet` (`setCurrentPlayTime`) avant chaque dessin
  logiciel : contrairement à ce que notait `apercu_reactions.js`, Robolectric dessine bien une AVD en cours par ce chemin. Toucher toutes
  les 50 ms, spécial toutes les 100 ms, 288 px, jour et nuit, sans accessoire / bonnet / écharpe : `glose_4/frames_avant`, `frames_apres`.
  `glose_4/top.py` mesure le haut du dessin par image (ex. pile spéciale : 18→9 px au lieu de 18→12, avec la descente d'anticipation à 22).
- Tests ciblés sous flock : GloseTest 17, GloseTapsTest 3, DefinidayTest 20, UiTest 20, OnboardingTest 6 : tous verts.

## Planches (`night/glose_4/`)
Chaque planche : 4 lignes (avant jour, après jour, avant nuit, après nuit), une image par pas de temps.
- `reaction_assis_tap.png`, `reaction_soir_tap.png` (toucher, 50 ms)
- `reaction_livres_special.png`, `reaction_reveil_special.png`, `reaction_assis_special__echarpe.png` (spéciaux, 100 ms ; l'écharpe et
  son liseré suivent le cou)
- Outils : `strips.py <sortie> <réaction> frames_avant frames_apres`, `top.py <réaction>` ; APK : `avant.apk`, `apres.apk`.

## Restants
- Le squash est volontairement modéré (5 à 7 %, ≈ 1,5 à 2 dp à 128 dp) : à juger sur téléphone ; les amplitudes sont dans `reactions.js`.
- Graphe graphene (`mascot`, `main-ui`, `onboarding`) à mettre à jour à l'intégration : animateurs partagés `res/animator/glose_k_*`,
  `Glose.pausable` / `Glose.paused`.
