// node android/make_android.js  -> régénère les dessins Android de Monsieur Glose dans app/src/main/res (XML versionnés : la CI ne lance pas Node).
// Variante {all:true} de chaque pose (tous les accessoires et détails rares présents, masqués par attributs de thème), puis svg2avd.
// Écrit drawable/glose_*.xml, interpolator/glose_ease.xml, values/glose_attrs.xml, values/glose_styles.xml.
const fs = require('fs'), path = require('path');
const { chromium } = require(process.env.PLAYWRIGHT || 'playwright');
const { buildOne } = require('../build.js');
const { convert, TOKENS } = require('./svg2avd.js');
const RES = path.join(__dirname, '../../../app/src/main/res'), TMP = 'rendus/android';
const WORDS = ['mot', 'bravo', 'encore', 'rare', 'dodo'], ONCE = ['bravo', 'encore']; // réaction de fin de quiz : écrit une fois
const KT_ECRIT = '0;0.08;0.55;0.86;1', WRITTEN = 0.55; // pose_ecrit.js : caché, écriture, tenu, rembobinage

// Couleurs des jetons : défaut (gris tigré, pelote bordeaux) et surcouches. Valeurs = palettes de glose_lib.js.
const DEFAULT = { gloseFur: '#A4ACBC', gloseFurD: '#788196', gloseCream: '#F8EEDD', gloseBall: '#9A4250', gloseBallD: '#5A2230',
  gloseNose: '#E98C8C', gloseRose: '#F0A6A2', gloseCupIn: '#18203A', gloseCupLit: '#4A5E92', gloseCup: '#2C3A5E',
  gloseBook: '#2C3A5E', gloseBookD: '#1F2944', gloseBookTop: '#2C3A5E', gloseBookTopD: '#1F2944', gloseBookLow: '#9C6B4A', gloseBookLowD: '#7C5236' };
const STYLES = {
  'Pelage.Creme': { gloseFur: '#E8D3B4', gloseFurD: '#B98E66', gloseCream: '#FCF4E6' },
  'Pelage.Enluminure': { gloseFur: '#E9C477', gloseFurD: '#B47A2C', gloseCream: '#FFF5DC', gates: ['Gilt', 'Glint'] },
  'Pelage.Nuit': { gloseFur: '#4A5A86', gloseFurD: '#2C3659', gloseCream: '#DCE2F2', gloseNose: '#D98A9A', gloseRose: '#E6A0B0',
    gloseBook: '#8E3A44', gloseBookD: '#6E2A33', gloseCup: '#8E3A44', gloseCupIn: '#4A1E26', gloseCupLit: '#B0606A',
    gloseBookTop: '#9C6B4A', gloseBookTopD: '#7C5236', gloseBookLow: '#2C3A5E', gloseBookLowD: '#1F2944', gates: ['Stars', 'Glint'], glint: '#F4E7B8' },
  'Pelote.Moutarde': { gloseBall: '#D9A441', gloseBallD: '#8A5F1E' },
  'Pelote.Sauge': { gloseBall: '#8FA585', gloseBallD: '#4F6347' },
  'Pelote.Rose': { gloseBall: '#E7A3A0', gloseBallD: '#9C5050' },
  'Acc.Noeud': { gates: ['Noeud'] }, 'Acc.Echarpe': { gates: ['Echarpe'] }, 'Acc.Bonnet': { gates: ['Bonnet'] },
  'Acc.Plume': { gates: ['Plume'] }, 'Acc.Marque': { gates: ['Marque'], off: ['Ruban'] },
  Eclats: { gates: ['Glint'] }, // quiz sans faute : éclats autour de « bravo »
};
const jobs = [['assis', 'glose_assis'], ['livres', 'glose_livres'], ['soir', 'glose_soir'], ...WORDS.map(w => ['ecrit', `glose_ecrit_${w}`, { word: w }]),
  ['icone', 'glose_icone'], ['icone', 'glose_icone_dort', { sleep: true }]];

(async () => {
  const b = await chromium.launch(), gates = {};
  try {
    for (const [pose, name, v] of jobs) {
      const out = `${TMP}/${name}.svg`;
      await buildOne(b, { pose, out, variant: Object.assign({ all: true }, v) });
      const svg = fs.readFileSync(path.join(__dirname, '..', out), 'utf8');
      Object.assign(gates, convert(svg, name, RES));
      if (ONCE.includes(v && v.word)) convert(svg, `${name}_fin`, RES, { once: [KT_ECRIT, WRITTEN], vd: `${name}_vd` });
    }
  } finally { await b.close(); }
  fs.mkdirSync(path.join(RES, 'interpolator'), { recursive: true });
  fs.writeFileSync(path.join(RES, 'interpolator', 'glose_ease.xml'), '<?xml version="1.0" encoding="utf-8"?>\n<pathInterpolator xmlns:android="http://schemas.android.com/apk/res/android" android:controlX1="0.45" android:controlY1="0" android:controlX2="0.55" android:controlY2="1"/>\n');
  const names = Object.keys(gates).sort(), gate = a => /^glose([A-Z][a-z]+)_/.exec(a)[1];
  const item = (k, v) => `<item name="${k}">${v}</item>`;
  const open = (g, swap = {}) => names.filter(a => gate(a) === g).map(a => item(a, swap[gates[a]] || gates[a])).join('');
  const shut = g => names.filter(a => gate(a) === g).map(a => item(a, '#00000000')).join('');
  const attrs = [...new Set(Object.values(TOKENS))].concat(names).map(a => `    <attr name="${a}" format="color"/>`).join('\n');
  fs.writeFileSync(path.join(RES, 'values', 'glose_attrs.xml'), `<?xml version="1.0" encoding="utf-8"?>\n<!-- Généré par design/mascotte/android/make_android.js -->\n<resources>\n${attrs}\n</resources>\n`);
  const tokens = o => Object.entries(o).filter(([k]) => k.startsWith('glose')).map(([k, v]) => item(k, v)).join('');
  const styles = [`    <style name="Glose">${tokens(DEFAULT)}${open('Ruban')}${[...new Set(names.map(gate))].filter(g => g !== 'Ruban').map(shut).join('')}</style>`,
    ...Object.entries(STYLES).map(([n, o]) => `    <style name="Glose.${n}" parent="">${tokens(o)}${(o.gates || []).map(g => open(g, o.glint ? { '#ffe9a8': o.glint } : {})).join('')}${(o.off || []).map(shut).join('')}</style>`)];
  fs.writeFileSync(path.join(RES, 'values', 'glose_styles.xml'), `<?xml version="1.0" encoding="utf-8"?>\n<!-- Généré par design/mascotte/android/make_android.js. Glose = défaut ; les autres se superposent avec Theme.applyStyle(id, true) -->\n<resources>\n${styles.join('\n')}\n</resources>\n`);
})().catch(e => { console.error(e.stack || e.message); process.exit(1); });
