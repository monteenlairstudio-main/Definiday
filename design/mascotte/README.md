# Mascotte : Monsieur Glose

Sources vectorielles (Paper.js) de la mascotte choisie, ses poses animées (SVG + SMIL), 22 variantes et le convertisseur vers VectorDrawable / AnimatedVectorDrawable (`android/svg2avd.js`).
Proposition d'intégration : [mise_en_place.md](mise_en_place.md). Journal des itérations : [notes.md](notes.md). Aperçu : `apercu.png`.

Régénérer (Node 18+, Chromium via Playwright) :
```
npm install paper@0.12.18            # dans ce dossier (node_modules/ non versionné)
export PLAYWRIGHT=/opt/node-tools/node_modules/playwright   # si Playwright n'est pas installé ici
node make_all.js                     # SVG animés, variantes et rendus PNG (rendus/ et iter/ non versionnés)
node android/make_android.js         # dessins Android -> app/src/main/res (XML versionnés : la CI ne lance pas Node)
node android/apercu_reactions.js assis   # images des réactions au toucher (android/reactions.js) -> rendus/react_assis.png
```
`make_android.js` exporte chaque pose en variante `{all:true}` (tous les accessoires et détails des pelages rares
dessinés à leur place), puis `android/svg2avd.js` remplace leurs couleurs par des attributs de thème : transparents dans
le style `Glose`, posés par `Glose.Acc.*` / `Glose.Pelage.*` (`values/glose_styles.xml`, généré). Pelages et pelotes :
jetons `?attr/gloseFur`… Aucun `?attr` sur un attribut de groupe (non documenté).
Calques par emplacement : chaque pièce d'accessoire (et les éclats des pelages rares) est un dessin à part, de même viewport,
qui recopie ses groupes parents animés (la tête) avec les mêmes animateurs ; le corps est coupé là où une pièce s'intercale.
`GloseLayers.kt` (généré) donne les calques de chaque dessin, de l'arrière vers l'avant ; `Glose.drawable` en fait un
`LayerDrawable` avec les seules pièces portées. Les fichiers identiques (mots du fil, familles de poses) ne sont écrits qu'une
fois (`values/glose_layers.xml` : alias des noms partagés). Une réaction « animateurs seuls » sur une pose passe par
`animated(nom, dessin_vd, { groupe: objectAnimator })` de `svg2avd.js` : les accessoires suivent alors la tête.
