# Mascotte : Monsieur Glose

Sources vectorielles (Paper.js) de la mascotte choisie, ses poses animées (SVG + SMIL), 22 variantes et le convertisseur vers VectorDrawable / AnimatedVectorDrawable (`android/svg2avd.js`).
Proposition d'intégration : [mise_en_place.md](mise_en_place.md). Journal des itérations : [notes.md](notes.md). Aperçu : `apercu.png`.

Régénérer (Node + Chromium/Playwright) :
```
npm install paper@0.12.18   # dans ce dossier
node make_all.js            # SVG animés, variantes et rendus PNG (rendus/ et iter/ non versionnés)
```
Les scripts attendaient le kit dans `kit/` (paper + render.js) : adapter le chemin vers `node_modules/paper/dist/paper-full.min.js` si besoin.
