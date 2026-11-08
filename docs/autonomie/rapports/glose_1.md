# Équipe GLOSE, itération 1 : audit, benchmark, plan

Worktree `agent-a6e0a1c6e8991b760`, base `14bc0a7`. Aucun changement de code d'appli à cette itération.

## 0. Méthode de rendu

- `node make_all.js` (25 s) régénère SVG + `rendus/` ; les SVG versionnés ne diffèrent que par la précision : remis à l'état `14bc0a7`.
- Planches : `night/board.js` (Playwright, tailles en dp = px CSS, facteur 2,625 = 420 dpi, animations SMIL retirées = image de repos),
  `night/mkspecs.js` + `night/boards.sh <dossier_mascotte> <préfixe>`.
- Tailles réelles dans l'appli (relevées dans les layouts/Kotlin) : widget 40 dp, en-tête accueil 128 dp, bienvenue 160 dp,
  icône de tête en bitmap 128 px (détail) et 192 px (grande icône de notification), icône d'appli adaptative (`ic_launcher_fg`, tête à l'échelle 0,15).
- Fonds : papier jour `#F8F3E9`, papier nuit `#141829` (`values-night/colors.xml`).

Planches « avant » (`night/glose_1/`) : `avant_poses_jour.png`, `avant_poses_nuit.png`, `avant_silhouettes.png` (aplat noir),
`avant_valeurs.png` (niveaux de gris), `avant_variantes.png`, `avant_variantes_nuit.png`.

## 1. Constats bruts sur les rendus

- Jour, 96 à 160 dp : dessin propre, trait d'encre régulier, tête 3/4 vivante, lunettes dorées = signature immédiate. Niveau « appli soignée ».
- Silhouette (aplat noir) : excellente pour la tête (oreilles + touffes de joues), lisible pour assis/écrit ; « soir » et « réveil » se lisent
  comme « chat dans une tasse » dès 48 px. Le mot écrit devient une tache sous 48 px (attendu).
- Nuit : **défaut principal**. L'encre `#3B2A20` (contour, moustaches, sourcils, mot écrit) disparaît sur `#141829` ; le livre bleu nuit
  `#2C3A5E` et la tasse bleu nuit fondent dans le fond : le chat flotte, le socle n'a plus de bord, le mot « encore » est presque illisible.
- Valeurs (gris) : la masse la plus sombre est le **livre** (bleu nuit + 6 bandes + filets or + étiquette + coins), pas le visage :
  à 48 dp l'œil va au socle. Le pelage gris moyen, la monture or et la pelote ont presque la même valeur.
- Échelle : le cadrage laisse ~12 % de vide autour du sujet ; à 40 dp (widget) la tête mesure ~14 dp, les yeux ~3 dp.
  Le trait OUT=9 sur un viewBox de 820 donne 1,4 dp à 128 dp mais 0,5 dp à 48 dp (il s'efface). Seule l'icône a des traits ×1,7.
- Expression : la même bouche « w » et les mêmes yeux ronds dans toutes les poses éveillées ; seule « dort » change. Aucun regard dirigé
  (les pupilles ne regardent ni la pelote ni le mot).

Icône d'appli (`avant_icone_appli_1x.png`, `_420dpi.png`, masques cercle et squircle, fonds de lanceur clair et sombre) :
tête bien cadrée dans la zone sûre, lunettes lisibles dès 36 px ; mais le fond `icon_bg #1B2440` se confond avec un fond
de lanceur sombre (`#202124`), le contour brun de la tête disparaît sur ce bleu nuit à 24–36 px, et les moustaches sont coupées par le masque.

## 2. Benchmark : ce que font les meilleurs (principes, pas de copie)

| Référence | Principe retenu | Source |
|---|---|---|
| Duolingo, refonte de Duo | La silhouette d'abord : « si la forme générale n'est pas attrayante, les détails ne peuvent pas la compléter ». Angle préféré : léger quart de tour. | https://blog.duolingo.com/reshaping-duo/ |
| Duolingo, guide d'illustration | 3 formes de base (rectangle arrondi, cercle, triangle arrondi), le moins de formes possible (~15 par composition ; 6 trop abstrait, 30 trop chargé) ; tête et corps en 1–2 formes ; la bouche est la seule forme non géométrique et penche d'un côté ; ombre portée en pilule sous les personnages. (Page désormais redirigée vers le blog ; contenu relevé via l'index de recherche.) | https://design.duolingo.com/illustration , https://design.duolingo.com/illustration/characters |
| Duolingo, « Shape language » | Exagérer les détails clés « presque jusqu'à la caricature », rythme = variété de formes et de tailles, espace négatif généreux autour du personnage pour qu'il se lise sur petit écran. | https://blog.duolingo.com/shape-language-duolingos-art-style/ |
| Duolingo, personnages animés | Rive : machine d'états, bouche et yeux en états séparés qui tournent en parallèle du corps (visèmes). | https://blog.duolingo.com/world-character-visemes |
| Duolingo, Vikram | Harmoniser un personnage au langage de forme commun (corps rond → pilule). | https://blog.duolingo.com/vikram-redesign/ |
| Mailchimp, Freddie (2018, Collins) | Pour l'usage petit : silhouette simplifiée monochrome qui garde les 2 marqueurs d'identité (chapeau + clin d'œil), formes et détails retouchés « pour qu'il soit beau à toutes les tailles ». | https://www.creativebloq.com/news/mailchimp-rebrand-does-away-with-script-wordmark , https://www.designweek.co.uk/issues/1-7-october-2018/mailchimp-rebrand-aims-to-unify-bran |
| Headspace | Formes rondes sans angle vif, visages minimaux, un « visage » à 6 expressions réutilisable sur toute forme : l'expression porte l'émotion, pas le décor. | https://advertisingweek.com/aw360/news/unlocking-headspaces-inner-brand-character/19717/ , https://blakecrosley.com/guides/design/headspace |
| Notion (R. Muradov, Buck « Notion Faces ») | Une seule épaisseur de trait par illustration, trait noir sur fond chaud, version inversée pour le mode sombre ; système de visages composables. | https://buck.co/work/notion-faces , https://getillustrations.com/blog/notion-style-illustrations-how-to-use-them-in-templates-websites-and-saas-products/ |
| Finch | Rig 2.5D (tête et corps qui tournent et se déforment) sans changer le dessin ; brief d'animation : squash & stretch, anticipation, mouvement secondaire ; personnalisation par vêtements. | https://contra.com/p/seOAX3PZ-expressive-character-animation-for-finch-app |
| Pokémon Sleep | Le sommeil comme répertoire d'expressions : visages endormis, respiration, bâillement, « pris au dépourvu » ; styles de sommeil nommés par personnage. | https://corporate.pokemon.co.jp/en/topics/detail/110.html |
| Apple HIG, mode sombre | Ajuster les valeurs d'une illustration pour fond sombre afin que les détails restent visibles ; un contour utile sur un fond peut être inutile sur l'autre. | https://developer.apple.com/design/human-interface-guidelines/dark-mode |
| Systèmes Adapt / Anvil | Deux voies : une palette qui marche dans les deux thèmes, ou une version par thème choisie à l'exécution. | https://adapt-design-system.pages.dev/docs/foundation/illustrations , https://anvil.servicetitan.com/docs/kits/illustrations/foundations.md |
| Android, icônes adaptatives | Zone sûre de 66 dp dans 108 ; silhouettes nettes, palette limitée ; couche monochrome pour les icônes thématiques. | https://developer.android.com/develop/ui/views/launch/icon_design_adaptive , https://medium.com/google-design/515af294c783 |
| Mascottes, test de silhouette | Aplat noir : reconnaissable à 32 px ? Prévoir 3 niveaux de détail (illustration complète, buste, icône minimale). | https://madegooddesigns.com/esports-logo-design/ , https://www.recraft.ai/blog/mascot-design |
| « Kindchenschema » (Lorenz), Pusheen | Grosse tête, grands yeux bas et écartés, corps rond ; plus le dessin est simple, plus une petite variation de proportion change la personnalité. | https://design.tutsplus.com/articles/the-elements-of-cute-character-design--vector-3533 , https://www.cartoonbrew.com/promote-article/why-is-it-so-difficult-to-make-cute-characters-81160.html |

Limites : pas de guide officiel trouvé pour Finch, LINE Friends ni les icônes d'Animal Crossing (seulement des packs de fans : écartés).
Les principes « autocollant » (liseré clair autour du personnage sur fond sombre) sont l'usage courant des stickers de messagerie ; pas de source de design citée ici.

## 3. Audit de Glose contre ces principes

Note sur 5 (5 = au niveau des meilleurs), avec le défaut précis et la cause dans le code.

| Critère | Note | Constat | Cause (source) |
|---|---|---|---|
| Silhouette à petite taille | 4 | Tête très reconnaissable (oreilles + touffes de joues + lunettes qui débordent en branches). Assis/écrit/tasse se lisent en aplat à 48 px. Point faible : sous 48 dp le **socle** (livre) pèse autant que le chat. | `book()` dans `glose_lib.js` : livre 470×150 + tranche ; `viewBox` des poses |
| Épaisseur de trait | 3 | Un seul système (OUT 9, IN 5) mais calculé pour 1024 px : 1,4 dp à 128 dp, **0,5 dp à 48 dp** (le contour s'efface, le dessin devient « mou »). L'icône passe à ×1,7, les poses jamais. Le mot écrit a un liseré de 4,5 contre 9 pour le corps. | `OUT`, `IN`, `SW` en tête de `glose_lib.js` ; `pose_icone.js` (`SW = 1.7`) |
| Proportions | 4 | Tête/corps ≈ 1:1 (chibi), yeux grands et bas, écartés : bon « Kindchenschema ». Les lunettes (R 56) recouvrent toute la zone des yeux : c'est la signature, à garder. | `gloseHead` : crâne 176×146, yeux 35×41 |
| Lisibilité de l'expression | 2 | Une seule expression éveillée (yeux pleins, bouche « w », sourcils neutres) dans assis, pile, écrit, réveil. Le regard ne suit pas l'action (il écrit « encore » en regardant le lecteur). Les meilleurs (Headspace : 6 visages ; Duolingo : bouche asymétrique, exagération) font porter l'émotion par le visage. | `gloseHead` : `cuteEye`, `mouthW`, `brows` sans paramètre d'humeur |
| Couleur / palettes | 3 | Palette cohérente et chaude (or, bordeaux, parchemin, bleu nuit). Mais en gris : socle = masse la plus sombre, pelage/monture/pelote = même valeur moyenne. Le gris tigré par défaut `#A4ACBC` est froid à côté du papier `#F8F3E9` et de l'accent plume `#2C4A9A`. | `BASE`, `SKINS.tigre`, `book()` |
| Attrait | 4 | Visage mignon, lunettes à reflet, pattes à coussinets, accessoires soignés. Manque de « jeu » : poses symétriquement posées, peu d'exagération (Duolingo : « presque jusqu'à la caricature »). | poses |
| Cohérence entre poses | 4 | Tête unique (`gloseHead`), palette commune : très bon. Échelle de la tête dans le cadre (largeur 392 sur le viewBox) : 48 % assis, 46 % pile, 44 % écrit, 43 % soir : homogène. | `viewBox` de chaque `pose_*.js` |
| Icône à 48 px | 4 | Tête seule à traits ×1,7 : très lisible. Défauts : fond `#1B2440` sans bord sur lanceur sombre ; contour brun sur bleu nuit peu contrasté ; moustaches coupées par le masque. | `ic_launcher_fg.xml`, `icon_bg` |
| Mode sombre | **1** | Contour, moustaches, mot écrit en encre brune invisibles sur `#141829` ; livre et tasse bleu nuit fondus dans le fond. C'est le défaut le plus visible de l'audit. | aucune adaptation nuit : `INK` codé en dur, socle `night` |
| Potentiel d'animation | 4 | Groupes nommés et pivots propres (tête, queue, pelote, paupières, bâillement, fil en trimPath), réactions au toucher. Manque : anticipation / squash & stretch (le corps ne fait que respirer à 1,5–2,5 %), pas d'états d'humeur. | `seatedGlose` anims, `android/reactions.js` |

Correction du §0 : le widget n'affiche pas Glose (il montre un timbre, `Widget.stamp`) ; la plus petite pose réelle est la vignette de la
collection (~70 dp, `CollectionActivity.stamp`). Les colonnes 40/48 dp des planches restent un test de robustesse.

## 4. Prototypes (worktree `agent-a6e0a1c6e8991b760`, **non commités** ; patch : `night/glose_1/prototypes.patch`, +62 −4 lignes)

### P1. Liseré « autocollant » pour le mode nuit (`glose_lib.js` : `halo()` ; `build.js` : variante `{halo:'#couleur', haloW}`)
- Derrière tout le dessin : union des contours d'encre fermés (épaisseur OUT) élargie de `h` = 12 (≈ 2 dp à 128 dp) + copie élargie des
  traits ouverts qui sortent de la silhouette (moustaches, brins, mot du fil, « z »). Un chemin par groupe animé : `tete_halo`, `queue_halo`,
  `pelote_halo`, `corps_halo`, `dormeur_halo`, `z1_halo`… qui **rejouent les mêmes animations** (rotation, translation, échelle, opacité)
  et portent le même pivot (`data.pivot`, lu par svg2avd) ; le liseré du mot (`fil_a_halo`) rejoue le `stroke-dashoffset`, donc le
  `trimPathEnd` côté Android.
- Pièces à porte exclues (accessoires, ruban, paillettes/étoiles, éclats) : sinon l'export Android `{all:true}` dessinerait le liseré des
  pièces non portées. Marqué `ponytail:` dans le code (liseré par calque d'accessoire si le manque se voit).
- Planches : `proto1_lisere_nuit.png` (avant / crème `#F0E8D8` / bord `#3A4566` / kraft `#6B5A3C`, 40 à 128 dp), `proto1_zoom.png`.
  Le crème rend au chat sa silhouette et au mot sa lisibilité dès 48 dp ; `#3A4566` est plus discret mais laisse moustaches et mot sombres.
  **Recommandation : crème `#F0E8D8`, l'encre du thème nuit** (même couleur que le texte : « correspondance à la bougie »).
- Coût mesuré (svg2avd sur l'export `{all:true}`, dans un dossier temporaire, rien d'écrit dans `app/`) : calque arrière `_vd`
  17,5 → 27,3 Ko (assis), 17,5 → 30,3 Ko (écrit), 16,1 → 23,4 Ko (soir) ; +9 à +15 chemins ; AVD +5 à +7 Ko de cibles ; ≈ +4 % du XML
  d'une pose. Couches et coupes inchangées.
- Mode jour : aucune différence si le jeton vaut `#00000000` ; sans variante `halo`, rendu identique au pixel.

### P2. Humeurs du visage (`gloseHead` : `o.mood` = `fier` | `curieux`, forçable par la variante `{mood}` ; `neutre` = aucune)
- `fier` : bouche ouverte en « D » (gorge `#7A3A3E`, langue `C.rose`), sourcils hauts et arqués → pose « écrit ».
- `curieux` : un sourcil levé, petite bouche « o » décalée → pose « pile » (il guette sa pelote).
- Essai écarté : « yeux qui sourient » (croissant de joue sous l'œil) : à 96–128 dp il coupe l'iris et se lit **triste**. Supprimé.
- Planche `proto2_humeurs.png` : le « D » se lit dès 96 dp, le « o » à partir de 128 dp. Assis, soir, icônes : rendu identique au pixel à
  `14bc0a7` (différence d'images 1024 px = 0) ; seule « pile » change, voulu (« écrit » aussi, non mesuré).
- Coût Android : 2–3 chemins dans le groupe `tete`, aucune cible d'animation, aucun calque, aucun jeton.

### Essai 3 (hors patch) : trait OUT 12 / IN 6 au lieu de 9 / 5
`essai3_trait.png` : plus net à 64–80 dp (vignettes de la collection), un peu lourd à 128 dp. Gain réel mais modeste, et il change tous
les pixels de toutes les poses : à décider après P1/P2.

## 5. Plan ordonné pour les itérations suivantes

Chaque étape : un commit, `node make_all.js` + `node android/make_android.js`, planches avant/après (`night/boards.sh`), build Gradle et
tests Robolectric (rendu jour identique quand c'est promis).

| # | Changement (fichiers) | Gain visuel attendu | Risque pipeline Android | Taille |
|---|---|---|---|---|
| 1 | **Liseré nuit** : P1 + intégration. `make_android.js` : variante `{all:true, halo:'#F0E801'}` (sentinelle) ; `svg2avd.js` : jeton `#f0e801 → gloseHalo` ; style `Glose` : `gloseHalo = @color/glose_halo` ; `values/colors.xml` `#00F0E8D8`, `values-night/colors.xml` `#F0E8D8` ; `reactions.js` / `animated()` : toute cible `X` s'applique aussi à `X_halo` si le groupe existe (sinon le liseré de la tête reste en place pendant un hochement de 9°). | Le plus gros : Glose redevient lisible en mode sombre (accueil, bienvenue, collection, notification). | +4 % de XML ; +1 cible par groupe animé ; Robolectric jour inchangé (transparent) ; accessoires sans liseré la nuit (bonnet, plume restent cernés de brun). | ~80 lignes + XML régénérés |
| 2 | **Humeurs** : P2 tel quel, + humeur par mot pour « écrit » (`bravo` → `fier`, `encore` → `doux` : bouche « w » + sourcils relevés au centre) dans `pose_ecrit.js` / `make_android.js`. | Glose réagit à ce qu'il fait ; écran de fin de quiz plus expressif. | Faible : chemins du groupe `tete` seulement ; les mots partagent aujourd'hui `glose_ecrit_mot_vd*` : une humeur par mot casse ce partage (≈ +60 Ko si chaque mot a sa tête) → limiter à 2 humeurs pour « écrit ». | ~30 lignes |
| 3 | **Icône d'appli** : générer `ic_launcher_fg.xml` depuis `glose_icone` avec liseré crème fixe (étape `make_android.js`, aujourd'hui fait à la main) ; moustaches raccourcies de 15 % pour rester dans le masque. | Tête détachée du bleu nuit à 24–48 px, sur lanceur clair comme sombre. | Nul sur les poses ; fichier isolé ; vérifier la couche monochrome inchangée. | ~40 lignes |
| 4 | **Hiérarchie de valeurs du socle** : `book()` appelé avec 4 bandes au lieu de 6, filets or à 60 % d'opacité, étiquette plus petite (`seatedGlose`, `pose_livres.js`, `pose_soir.js`). | Le visage redevient la zone la plus contrastée à 64–96 dp (Duolingo : « le moins de formes possible »). | Faible : moins de chemins (−10 à −15 par pose) ; jetons `gloseBook*` intacts. | ~15 lignes |
| 5 | **Trait** OUT 9 → 11, IN 5 → 6 (`glose_lib.js`, 1 ligne) si l'essai 3 est validé sur téléphone. | Vignettes de collection plus nettes. | Tous les pixels changent (mettre à jour les références Robolectric) ; épaisseurs du mot inchangées (`fil_a` 20). | 1 ligne + régénération |
| 6 | **Anticipation / squash & stretch dans les réactions** (`android/reactions.js`) : avant le saut ou le hochement, `corps` scaleY 0,96 / scaleX 1,03 sur 120 ms, puis 1,04 / 0,98, retour à 1. | Réactions plus vivantes (principes Finch/Disney). | Le générateur vérifie déjà « fin = début » ; ajouter les cibles `corps_halo` si l'étape 1 est faite. | ~40 lignes |
| 7 | **Liseré des accessoires** : un liseré par calque d'accessoire (dans chaque pièce `LAYERED`), même couleur `gloseHalo`. | Bonnet, plume, couronne, sapin lisibles la nuit. | +1 chemin par pièce et par pose ; pièces de même viewport, aucune coupe nouvelle. | ~30 lignes |
| 8 | (Au propriétaire de décider) **Pelage par défaut un peu plus chaud** : `#A4ACBC` → `#A8A8B4` environ (`SKINS.tigre`, `DEFAULT` de `make_android.js`). | Harmonie avec le papier crème et le kraft. | Nul techniquement ; touche l'identité : à valider. | 2 lignes |

Hors plan (écarté) : refaire la tête ou les proportions (déjà bonnes : Kindchenschema, silhouette 4/5) ; ajouter des expressions animées
façon visèmes (Rive) : sans AndroidX ni Rive, le coût en AVD ne se justifie pas pour une appli sans voix.
