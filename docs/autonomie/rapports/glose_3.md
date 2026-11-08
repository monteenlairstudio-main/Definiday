# Équipe GLOSE, itération 3 : icône d'appli, livre allégé, liseré des accessoires (commités)

Branche `worktree-agent-a6e0a1c6e8991b760`, base `6a3a199`. `.graphene/` non touché (le hook signale `mascot`/`main-ui` : à l'intégration).

| Commit | Étape du plan | APK release |
|---|---|---|
| `4d545b4` gla: app icon generated from Glose's head, cream outline, inside the mask | 3 | −248 o |
| `74b28c7` gla: lighter book under Glose (fewer gold lines, softer shading, smaller label) | 4 | −512 o |
| `f921be7` gla: night outline on Glose's accessories and bookmark ribbon | 7 | +12 404 o |
| **Total** | | **2 039 519 → 2 051 163 o : +11 644 o (+11,4 Kio)**, budget 40 Kio |

## Étape 3 : icône d'appli
- `ic_launcher_fg.xml` était fait à la main depuis une ancienne tête. `make_android.js` l'écrit maintenant depuis `glose_icone` :
  couleurs par défaut, liseré crème fixe (#F0E8D8, h = 8), moustaches −15 % (`whisk: 0.85`, nouveau paramètre de `gloseHead`, 1 par défaut).
- Échelle 0,15 → 0,13, recentrée. Mesure sur le rendu : la tête occupait 19,5..85,8 × 26..84 sur 108 et la pointe des oreilles sortait
  du cercle sûr (rayon 33 autour de 54) ; maintenant tête, liseré et moustaches tiennent dans le masque rond.
- Couche monochrome : rendu identique au pixel (5 tailles). Tous les `glose_*` : identiques.
- Planche `glose_3/icone_appli_avant_apres.png` : 24/36/48/108 px, masques rond et carré arrondi, lanceur clair (#F2F2F2) et sombre
  (#202124), plus la couche monochrome teintée. Recomposée depuis le rendu Robolectric de `ic_launcher_fg` (432 px, partie visible 72/108,
  fond `icon_bg`), parce que l'icône adaptative telle que Robolectric la dessine a un fond faux (rouge). Le liseré sépare la tête du
  bleu nuit à 24–36 px, surtout sur lanceur sombre.

## Étape 4 : livre allégé
- `book()` : plus de filets d'or aux deux bords (les 4 bandes restent), filets des bandes à 60 %, bandes sans cerne d'encre, étiquette
  0,18 × 0,34 (au lieu de 0,24 × 0,44), ombre basse 0,2 → 0,12, reflet 0,13 → 0,18.
- Vérification des valeurs (`glose_3/valeurs_livre_avant_apres.png`, niveaux de gris 48/96/160 dp) ; contraste RMS (écart type de la
  luminance) mesuré sur le rendu 256 px :

  | Pose | tête | livre avant → après | rapport tête/livre |
  |---|---|---|---|
  | assis | 59,3 | 59,5 → 57,7 | 1,00 → 1,03 |
  | écrit bravo | 59,6 | 58,7 → 57,1 | 1,02 → 1,04 |

  Le livre perd ses détails à haute fréquence (planche `livre_avant_apres_160dp.png` : plus calme) et le visage devient de peu la zone la
  plus contrastée. La masse bleu nuit reste la plus sombre : aller plus loin demande une reliure par défaut plus claire (jeton
  `gloseBook`, couleur d'identité) : à décider par le propriétaire.
- Rendus jour : seules les poses à livre changent (ecrit 248 images, assis/livres/soir/reveil 32 chacune) ; icônes et icône d'appli identiques.
- SVG de conception des poses à livre régénérés (assis et soir étaient en retard sur les sources depuis le lot « vie au repos »).

## Étape 7 : liseré des accessoires
- `glose_lib.js` : chaque pièce (`PIECE` : noeud, echarpe, echarpe_dos, plume, marque_page, ruban, grelot, grelot_ruban, sapin, bonnet,
  beret, couronne) reçoit, en tête de son propre groupe, l'union de ses contours d'encre élargie (même `h`), découpée hors de la silhouette
  du corps et réduite à son cadre. Même porte que la pièce, donc même calque Android et visible seulement portée. Même jeton : la couleur
  `glose<Porte>_f0e801` vaut `?attr/gloseHalo` quand la porte est ouverte.
- Paillettes, étoiles et éclats : sans liseré (clairs, déjà visibles la nuit).
- Rendus jour : 456/456 dessins identiques (la planche composée varie avec l'ordre des rendus, comme constaté à l'itération 2).
  Nuit : 381 rendus changent, ceux qui portent un accessoire ou le ruban.
- Planches : `accessoires_nuit_avant_apres.png` (5 poses × 9 accessoires, avant/après), `accessoires_zoom.png` (bonnet, plume, sapin,
  marque-page, écharpe à 256 px). Les pièces se détachent du papier de nuit et rien n'est peint sur le chat.
- Coût : +12,4 Ko (≈ 0,3 à 0,5 Ko par pièce et par famille de poses ; le bonnet est le plus lourd).

## Vérifications
- Tests ciblés (sous flock) : GloseTest 15, GloseTapsTest 3, DefinidayTest 20, VoixTest 7, tous verts.
- Vidages Robolectric (test temporaire non commité, copie `glose_3/GloseDumpTest.kt.txt`) : `dump_avant`, `dump_etape3`, `dump_etape4`,
  `dump_etape7` (jour et nuit, chaque dessin seul et avec chaque surcouche, poses à 48/96/160 dp, icône d'appli et monochrome à
  24/36/48/108/432 px).
- Planches générales : `avant_apres_jour.png`, `avant_apres_nuit.png` (6a3a199 | après, 48/96/160 dp).

## Restants
- Reliure par défaut plus claire si l'on veut un vrai écart de contraste tête/livre (choix d'identité).
- Le marque-page de cuir et le ruban rose ont maintenant un liseré : à vérifier sur téléphone que le ruban fin ne paraît pas trop « cerné ».
