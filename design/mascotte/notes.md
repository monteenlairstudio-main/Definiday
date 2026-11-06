# Monsieur Glose v3 : journal d'auto-critique

Pipeline : `node build.js <pose> <sortie.svg> '<variante JSON>' <préfixe_png> <t>` ou `node build.js --jobs jobs.json`
(glose_lib.js + pose_<pose>.js exécutés dans Chromium avec Paper.js, export SVG, injection SMIL, rendus 1024 + 128 à t s).
Rendus d'itération dans `iter/`. Chaque critique : 3 défauts relevés, corrigés à l'itération suivante.

## Factorisation (cohérence d'une pose à l'autre)
- `gloseHead(C, o)` : UNE tête pour toutes les poses (mêmes proportions v2 : crâne 176×146, yeux 35×41, lunettes R 56),
  placée par une matrice (position, inclinaison autour du cou, échelle 1 partout). Torsion 3/4 par projection sur une
  sphère (R 200) : chaque point du visage glisse de x' = R·sin(asin(x/R)+turn), poignées horizontales × cos(θ+turn)/cos θ :
  l'œil et le verre lointains s'affinent, le museau se décale, la touffe de joue lointaine rétrécit. Variante `sleep`
  (yeux fermés en « ‿ », lunettes glissées sur le nez).
- `seatedGlose` (assis + écrit), `arm()` (épaule, coude, patte, rayures qui suivent le membre), `yarn`, `strand`
  (fil cerné 18 px à 1024), `book`, accessoires (`bowTie`, `scarf`, `nightcap`, `quill`, `bookmark`), pelages
  (`SKINS`), pelotes (`YARNS`), éclats animés des pelages rares (`rareGlints`).
- Palette commune (BASE) : encre #3B2A20, rose #F0A6A2, nez #E98C8C, bleu nuit #2C3A5E/#1F2944, or #D9A441/#A87A26,
  parchemin #F3E2BE, bordeaux #8E3A44, sépia #9C6B4A, lueur #FFD690 ; + pelage (3 teintes) + pelote (2 teintes).

## Cursive d'un seul trait (iter/cursive1..3)
Lettres en points de passage (hauteur d'x 100), concaténées puis lissées en catmull-rom : un seul chemin par mot.
Mots retenus : « mot » (tracé v2 validé, « t » barré par sa boucle), « bravo », « rare », « dodo », « encore ».
« lire » et « rêve » proposés par le propriétaire demandent un point ou un accent, donc un lever de plume : écartés
pour rester d'un seul trait (« dodo » remplace « rêve » pour le soir, « rare » renvoie au mot rare du jour).
### Itération 1
1. Lettres trop serrées pour un fil épais : boucles de « e » et « b » bouchées, « o » illisible.
2. « t » sans barre : « mot » se lit « mol ».
3. Retours de jambage (m, n) en pointes dures.
### Itération 2
Lettres élargies (m 166, a 100…), « o » qui se referme sur sa remontée (leçon v2), « mot » repris du tracé v2.
1. « r » lu comme un « n » (épaule trop ronde) : « rare » → « nare ».
2. Sortie traînante du « o » final (bravo, dodo) : longue queue horizontale inutile.
3. Raccord « o → r » un peu long dans « encore » (assumé : c'est la ligature naturelle).
### Itération 3
« r » à pointe montante (au-dessus de la hauteur d'x) puis épaule plate : « rare » lisible ; sortie finale coupée.

## Pose « assis » (par défaut)
### Itération 1 (iter/assis1)
Tête 3/4 (turn 0,22) et inclinée de −6° : la raideur de face disparaît.
1. Queue énorme qui fait le tour complet du socle : se lit comme une bouée, cache les deux pieds.
2. Bras : la patte proche, très basse, forme un boudin en boucle ; la patte lointaine semble « faire coucou ».
3. Brin libre en crochet de canne au coin du livre.
### Itération 2 (iter/assis2)
Queue amincie (40 → 22) mais encore en anneau ; bras recalés.
1. Queue toujours trop longue : l'enroulement devant les deux pieds reste une bande grise.
2. Bras proche trop long (150 px de l'épaule à la patte) : perd l'aspect « chibi » de la v2.
3. Verre lointain collé au contour du crâne, branche de lunettes invisible à droite.
### Itération 3 (iter/assis3)
Queue raccourcie : elle sort de derrière la hanche droite et s'enroule devant le pied droit, le bout remonte (« 6 »).
Bras proche raccourci. Lisible à 128 px (lunettes, pelote, brin).
1. Torsion 0,22 un peu forte : verre droit tangent au contour.
2. Brin libre qui barre le pied gauche.
3. Brin encore raide (un bâton) une fois tombé du livre.
### Itération 4 (iter/assis4) : base retenue
Torsion 0,17, brin repassé sous les pieds puis tombant du coin avant. Les deux pattes font deux choses différentes
(l'une soutient la pelote par-dessous, l'autre est posée sur son flanc), queue qui enveloppe, tête penchée.
Restant : le brin qui pend reste assez droit (assumé, il pèse) ; plastron peu visible derrière la pelote.

## Pose « le fil écrit » (ex-Maille)
Même socle que « assis » (`seatedGlose`) : une patte serre la pelote, l'autre est levée et guide le fil.
Animation 7 s : caché (0–0,08) → écriture (→ 0,55) → tenu (→ 0,86) → rembobinage (→ 1) ; `stroke-dashoffset` sur les
3 traits du fil (encre 20, âme 11, lumière 3), pelote qui tourne de 150° en se dévidant puis revient, tête qui suit.
Sans SMIL (PNG, VectorDrawable), l'attribut de base laisse le mot entièrement écrit.
### Itération 1 (iter/ecrit1)
1. Le mot percute l'oreille droite (b de « bravo ») et sort du cadre à droite.
2. Raccord patte → mot en boucle : le fil part vers la gauche puis fait demi-tour pour attaquer la lettre.
3. Le fil pelote → patte barre le torse en diagonale (effet bandoulière).
### Itération 2 (iter/ecrit2)
Mot posé juste au-dessus de la patte (le fil monte tout droit et attaque la première lettre), cadrage décalé à droite.
1. « o » final encore rogné par le bord droit.
2. Touffe du coude en pâté informe.
3. Fil bas toujours visible en travers du ventre.
### Itération 3 (iter/ecrit3_mot, iter/ecrit3_bravo)
Mot limité à 276 px, touffe de coude réduite à une petite mèche, fil bas caché derrière le bras.
1. À 128 px le mot reste lisible mais l'âme du fil (10 px) se bouche dans les boucles du « b ».
2. Bras levé : bien construit (épaule, coude, avant-bras, coussinets visibles) mais longe les moustaches droites.
3. Petite boucle triangulaire de la barre du « t » (héritée de la v2, assumée : elle lit « t »).
### Itération 4 (iter/ecrit4_frames.png) : base retenue
Fil du mot épaissi (encre 20 / âme 11). Planche de 12 images : écriture lettre à lettre, tenue, rembobinage vers la
patte, pelote synchronisée, aucun à-coup au rebouclage (valeur finale = valeur initiale).

## Pose « sur la pile » (ex-Virgule)
### Itération 1 (iter/livres1, iter/livres1_crop)
Tête commune (turn −0,16 vers la pelote, inclinée −7°), pile de 3 livres, patte pendante.
1. Bout de queue : raccord visible et minuscule trou d'encre dans la boucle (le défaut « trait doublé » de la v2).
2. Patte pendante à 50 px au-dessus de la pelote : elle ne la « taquine » pas.
3. Anneau de fil du poignet fin (5 px) : invisible à 128 px.
### Itération 2 (iter/livres2_crop)
Boucle agrandie (trou de ~70 px), bout en volume séparé, anneau de fil cerné 18 px.
1. Raccord tronc/bout : marche d'escalier et ergot du capuchon du tronc qui dépasse.
2. Contour du bout supprimé sur un disque trop large : trou dans le trait.
3. (code) profil d'épaisseur du tronc mal écrit.
### Itération 3 (iter/livres3, iter/livres3_crop)
Nouvelle méthode `crossingTail` : tronc ∪ bout en UNE forme (pas de raccord), puis seul le disque de croisement
est redessiné par-dessus (fond, rayures, ombre portée sur le tronc et contour du bout limité au disque).
Croisement net : le bout passe devant le tronc, un seul trait partout.
1. Patte toujours trop haute (pas de contact avec la pelote).
2. Queue un peu massive en bas à droite (assumé : c'est la « virgule »).
3. Brin libre au sol : OK ; ombre de la pelote OK.
### Itération 4 (iter/livres4, iter/livres4_crop) : base retenue
Patte allongée qui appuie sur le haut de la pelote, fil enroulé autour de la patte.
Restant : la boucle de fil couvre une partie des doigts (lecture « patte prise dans le fil », voulue).

## Pose du soir (ex-Tisane)
Tête commune en mode `sleep` (yeux « ‿ », joues plus roses, lunettes glissées de 16 px et inclinées de 5°, reflet coupé).
### Itération 1 (iter/soir1)
1. Intérieur de la tasse invisible : le corps occupe toute l'ouverture.
2. « z » partis hors cadre en haut à gauche (le troisième est coupé), vapeur gris foncé façon fumée.
3. Queue qui pend entre tasse et bougie et s'enroule contre le bougeoir : lecture confuse.
### Itération 2 (iter/soir2)
Corps rétréci : l'ouverture se lit à droite (fond sombre + paroi du fond éclairée en croissant + liseré d'or) ; vapeur
sépia translucide ; « z » qui naissent contre l'oreille gauche ; queue drapée sur le bord avant.
1. Bosse du dos trop basse : on ne voit plus que la tête, le chat semble « posé » sur la tasse.
2. Bout de queue recourbé vers la tasse comme une main.
3. Étoiles et lune : agrandies (r 34 / 44), lisibles à 128 px (vérifié sur iter/soir2_128).
### Itération 3 (iter/soir3, iter/soir3_frames.png)
Dos relevé derrière la tête, bout de queue tourné vers l'extérieur. Planche de 8 images : respiration, « z » en
crescendo, flamme, halo, vapeur, sachet ; aucun à-coup.
1. Les « z » 2 et 3 se chevauchent un instant (t ≈ 3 s) : accepté, c'est un flux.
2. Ficelle du sachet (3,5 px) invisible à 128 px : détail de lecture rapprochée, assumé.
3. Bonnet (accessoire) : bandeau traversé par un trait parasite → corrigé en itération 4.
### Itération 4 (iter/soir4_bonnet) : base retenue
Bonnet : dôme + revers crème + pointe tombante et pompon, pointes d'oreilles qui sortent par des fentes, sans trait
horizontal parasite. Pelage « Nuit d'encre » : la tasse passe en bordeaux (sinon le chat s'y fond).

## Accessoires, pelages, pelotes (iter/acc_planche1, iter/skins_planche1-2, iter/final_check1)
1. Plume d'oie trop longue, coupée par le haut du cadre → raccourcie (rachis 300 → 96).
2. Écharpe de la pose « pile » : boudin rayé posé sur le dos → bande nouée sous le menton + pan qui pend devant
   le livre du haut, à droite de la patte rentrée.
3. « Nuit d'encre » sur le volume bleu nuit : le bas du corps disparaît → socle bordeaux pour ce pelage (et livre
   du haut sépia dans la pose « pile », tasse bordeaux le soir).
Autres : marque-page de cuir à gland d'or raccourci (le gland sortait du cadre) ; « Enluminure » : paillettes d'or
clippées dans la tête et le corps + 3 éclats animés autour (rares uniquement).
Version « icône » : tête seule, traits ×1,7, sans reflet animé ; lisible à 48 et 24 px (iter/skins_planche2).

## Bogue trouvé pendant la conversion Android
Le pivot de rotation de la tête valait `undefined` (xy() appliqué à un tableau) : dans le navigateur l'animation
d'inclinaison de la tête était ignorée sans erreur. Détecté par aapt2 (« NaN incompatible avec pivotX »), corrigé,
tout régénéré.

## Défauts corrigés (demande du propriétaire) / restants
| Défaut v2 | État v3 |
|---|---|
| Glose raide et symétrique de face | corrigé : torsion 3/4 projetée (0,17 à 0,24), tête penchée (−6° à +2°), queue qui enveloppe, pattes asymétriques |
| Fils trop fins à 128 px | corrigé : fil cerné 18–20 px à 1024 (était 10–13) + version « icône » à traits ×1,7 |
| Trait doublé au croisement de la queue | corrigé : `crossingTail` (union + disque de croisement redessiné) |
| Bras peu travaillé (écriture) | corrigé : épaule, coude (mèche), avant-bras à rayures, coussinets roses, doigts qui pincent le fil |
| Intérieur de tasse peu visible | corrigé : fond sombre, paroi du fond éclairée, liseré d'or, corps rétréci |
| « z » trop loin de la tête | corrigé : naissent contre l'oreille gauche |
| Étoiles de tasse illisibles en petit | corrigé : 1 lune + 3 étoiles, 2× plus grandes |
| Même personnage d'une pose à l'autre | `gloseHead` unique (mêmes proportions, couleurs, lunettes), palette commune |
Restants : mot du fil à 128 px lisible mais petit pour « encore » (6 lettres) ; boucle triangulaire du « t » ;
brin qui pend du livre un peu raide ; ficelle du sachet invisible en petit ; les phases négatives SMIL des « z » et de
la vapeur deviennent des départs différés en AnimatedVectorDrawable (premier cycle légèrement vide).

## Première vague de la collection (lot 2) et vie au repos
Vie au repos : périodes propres à chaque partie (paupières 9,4 s = un clignement puis un double, queue 7,3 s, reflet 11 s,
bâillement 29 s : bouche ouverte `baille` + paupières `baille_g/d` + tête qui se soulève, à 14,5 s), respiration 3,8 s inchangée.
L'appli arrête la boucle au bout de 26 s (`Glose.IDLE_MS`), un toucher la relance. Groupe `tete` : pivot au cou (`data` Paper.js
→ `android:pivotX/Y`) pour les réactions one-shot `glose_icone_hoche` / `glose_icone_penche` (animateurs seuls, sur `glose_icone_vd`).
Jetons : `C.specs` (monture, sentinelle #D9A401 → `gloseSpecs`) et `C.stripe` (rayures, #788101 → `gloseStripe`) ; `furD` ne colore plus
que le bout de la queue. Rendu Robolectric avant/après : poses et anciennes surcouches identiques au pixel (icône : 5 px à ≤ 3 %).
Nouveaux : pelages `roux`, `smoking` (socle et tasse bordeaux comme « Nuit d'encre ») ; pelotes lavande, bleu de Prusse, vert d'eau ;
reliures ; monture écaille ; béret, couronne des rois (dans `gloseHead`) ; grelot (ruban sous la tête, grelot devant, 3 familles de
poses) ; petit sapin de papier (sur le livre, au sol à droite de la pile, sous l'anse de la tasse) ; écharpe sucre d'orge et bonnet
des neiges (`swap` de couleurs d'une porte) ; mots « coucou », « ronron », « flocon » (lettres u, l, f ajoutées) ; pose `reveil`
(soir {awake} : yeux ouverts, lunettes en place, bougie éteinte, sans « z »).
### Itération 1 (iter/sheet1)
1. Sapin trop petit sur le livre (illisible à 96 dp) → ×1,25 ; à gauche dans « écrit » (le bras levé occupe la droite).
2. « f » de « flocon » confondu avec « l » → jambage descendant à boucle fermée à droite.
3. Bouche du bâillement cachée en `scale(1 0.001)` : un trait d'un pixel sous le nez → cachée en point (`scale(0.001)`).
Restants : sapin encore petit dans la pose « pile » et sous l'anse ; grelot en partie caché par le menton dans la tasse ;
la monture écaille est un aplat (pas de marbrure) ; poids : +272 Ko sur l'APK release (les accessoires sont dessinés dans
chaque pose : passer aux calques par emplacement avant le 20e).

## Réactions au toucher (`android/reactions.js`, `Glose.tap`)
AVD à animateurs seuls sur les VD existants (aucun nouveau dessin), cibles relues dans le VD (groupe animé sous `paupiere_d`, pivot de
la pelote, traits `fil_a/b/c`) ; chaque valeur finit où elle commence (vérifié par le générateur). Un toucher (600 ms) : clin d'œil et
tête penchée ; endormi : il se blottit, les « z » sautillent. 4 touchers d'affilée (1,8 s) : assis, la pelote lui échappe, roule au bord
du livre et rebondit dans ses pattes ; pile, sa patte tape la pelote qui rebondit deux fois, tête relevée, queue qui frétille ; soir, il
s'étire hors de la tasse et se rendort, un gros « z » s'envole ; réveil, museau dans le thé (yeux clos), « ah », bouffée de vapeur ;
« le fil écrit » (tous les mots), le fil rembobine le mot et le réécrit d'un trait, la pelote tourne avec. Contrôle : `node android/apercu_reactions.js <poses>`.
### Itération 1 (rendus/react_*.png)
1. Assis : pelote lancée vers le haut, cachée derrière le menton et les pattes → elle roule vers la droite, devant le livre.
2. Pile : pelote qui roule à gauche, hors du cadre → rebonds sur place ; rotation finale 200° (saut au retour) → 360°.
3. Réveil : la bouffée de vapeur rétrécissait d'un coup à la fin → elle s'efface en montant (fillAlpha) puis revient ;
   « le fil écrit » : bras levé qui sautillait et décollait la patte du fil → retiré.
Restants : pas de cœur ni de fioriture (il faudrait un nouveau tracé) ; la pelote assise se décolle du brin qui pend pendant qu'elle roule ;
Robolectric ne dessine pas les valeurs animées d'une AVD (aperçu Chromium à la place).
