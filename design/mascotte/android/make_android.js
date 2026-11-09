// node android/make_android.js  -> régénère les dessins Android de Monsieur Glose dans app/src/main/res (XML versionnés : la CI ne lance pas Node).
// Variante {all:true} de chaque pose (tous les accessoires et détails rares présents, masqués par attributs de thème), puis svg2avd.
// Écrit drawable/glose_*.xml, interpolator/glose_ease.xml, values/glose_attrs.xml, values/glose_styles.xml.
const fs = require('fs'), path = require('path');
const { chromium } = require(process.env.PLAYWRIGHT || 'playwright');
const { buildOne } = require('../build.js');
const { convert, animated, hermiteXml, HERMITE, TOKENS, LAYERED, LAYERS, ALIASES } = require('./svg2avd.js');
// Essais pour les décisions du propriétaire (docs/autonomie/glose_decisions.md), sans effet quand ils sont absents :
//   GLOSE_RES=<dossier>      écrit ailleurs que dans app/src/main/res (le Kotlin dans <dossier>/../java)
//   GLOSE_VARIANT='{"trait":[11,6]}'   variante ajoutée à chaque pose (épaisseur du trait)
//   GLOSE_TOKENS='{"gloseFur":"#A8A8B4"}'   couleurs par défaut remplacées (pelage, reliure...)
//   GLOSE_ICON=a2|a3         écrit aussi drawable/ic_launcher_fg.xml depuis glose_icone (voir ICONS)
const RES = process.env.GLOSE_RES ? path.resolve(process.env.GLOSE_RES) : path.join(__dirname, '../../../app/src/main/res'), TMP = 'rendus/android', KT = path.join(RES, '../java/fr/definiday/GloseLayers.kt');
const OPT = JSON.parse(process.env.GLOSE_VARIANT || '{}');
const WORDS = ['mot', 'bravo', 'encore', 'rare', 'dodo', 'coucou', 'ronron', 'flocon'], ONCE = ['bravo', 'encore']; // réaction de fin de quiz : écrit une fois
const KT_ECRIT = '0;0.31;0.45;0.53;1', ONCE_FROM = 0.31, REWIND_MS = 200; // pose_ecrit.js : écrit (tenu), rembobinage, caché, écriture ; « une fois » = rembobinage éclair puis écriture

// Couleurs des jetons : défaut (gris tigré, pelote bordeaux) et surcouches. Valeurs = palettes de glose_lib.js.
const DEFAULT = { gloseFur: '#AAA6AC', gloseFurD: '#7E7980', gloseCream: '#F8EEDD', gloseBall: '#9A4250', gloseBallD: '#5A2230',
  gloseNose: '#E98C8C', gloseRose: '#F0A6A2', gloseCupIn: '#18203A', gloseCupLit: '#4A5E92', gloseCup: '#2C3A5E',
  gloseBook: '#6A7EA6', gloseBookD: '#50638A', gloseBookTop: '#6A7EA6', gloseBookTopD: '#50638A', gloseBookLow: '#9C6B4A', gloseBookLowD: '#7C5236',
  gloseSpecs: '#D9A441', gloseStripe: '#7E7980', gloseHalo: '@color/glose_halo' }; // liseré : transparent le jour, crème la nuit (values-night)
Object.assign(DEFAULT, JSON.parse(process.env.GLOSE_TOKENS || '{}'));
// socle et tasse bordeaux des pelages sombres (le chat ne se fond pas dans le bleu nuit)
const DARK_BASE = { gloseBook: '#8E3A44', gloseBookD: '#6E2A33', gloseCup: '#8E3A44', gloseCupIn: '#4A1E26', gloseCupLit: '#B0606A',
  gloseBookTop: '#9C6B4A', gloseBookTopD: '#7C5236', gloseBookLow: '#2C3A5E', gloseBookLowD: '#1F2944' };
// swap : couleurs d'une porte remplacées (variante de couleur d'un accessoire existant)
const STYLES = {
  'Pelage.Creme': { gloseFur: '#E8D3B4', gloseFurD: '#B98E66', gloseStripe: '#B98E66', gloseCream: '#FCF4E6' },
  'Pelage.Enluminure': { gloseFur: '#E9C477', gloseFurD: '#B47A2C', gloseStripe: '#B47A2C', gloseCream: '#FFF5DC', gates: ['Gilt', 'Glint'] },
  'Pelage.Nuit': { gloseFur: '#4A5A86', gloseFurD: '#2C3659', gloseStripe: '#2C3659', gloseCream: '#DCE2F2', gloseNose: '#D98A9A', gloseRose: '#E6A0B0',
    ...DARK_BASE, gates: ['Stars', 'Glint'], swap: { '#ffe9a8': '#F4E7B8' } },
  'Pelage.Roux': { gloseFur: '#DE9457', gloseFurD: '#B4652F', gloseStripe: '#B4652F', gloseCream: '#FBEEDC' },
  'Pelage.Smoking': { gloseFur: '#4A4448', gloseFurD: '#3A3438', gloseStripe: '#433D41', gloseCream: '#FFFAF2', ...DARK_BASE },
  'Pelote.Moutarde': { gloseBall: '#D9A441', gloseBallD: '#8A5F1E' },
  'Pelote.Sauge': { gloseBall: '#8FA585', gloseBallD: '#4F6347' },
  'Pelote.Rose': { gloseBall: '#E7A3A0', gloseBallD: '#9C5050' },
  'Pelote.Lavande': { gloseBall: '#A99BC4', gloseBallD: '#65587F' },
  'Pelote.BleuPrusse': { gloseBall: '#2F5B8C', gloseBallD: '#173252' },
  'Pelote.VertEau': { gloseBall: '#A9D6C4', gloseBallD: '#5E8E7C' },
  'Livre.Bordeaux': { gloseBook: '#8E3A44', gloseBookD: '#6E2A33', gloseBookTop: '#8E3A44', gloseBookTopD: '#6E2A33' },
  'Livre.Verte': { gloseBook: '#2F5A46', gloseBookD: '#1F3F31', gloseBookTop: '#2F5A46', gloseBookTopD: '#1F3F31' },
  'Yeux.Ecaille': { gloseSpecs: '#8A5530' },
  'Acc.Noeud': { gates: ['Noeud'] }, 'Acc.Echarpe': { gates: ['Echarpe'] }, 'Acc.Bonnet': { gates: ['Bonnet'] },
  'Acc.Plume': { gates: ['Plume'] }, 'Acc.Marque': { gates: ['Marque'], off: ['Ruban'] },
  'Acc.Grelot': { gates: ['Grelot'] }, 'Acc.Beret': { gates: ['Beret'] }, 'Acc.Couronne': { gates: ['Couronne'] }, 'Acc.Sapin': { gates: ['Sapin'] },
  'Acc.EcharpeSucreOrge': { gates: ['Echarpe'], swap: { '#2c3a5e': '#C8453E', '#d9a441': '#FFF8EE' } },
  'Acc.BonnetNeige': { gates: ['Bonnet'], swap: { '#c75b54': '#9CC4DD', '?attr/gloseCream': '#FFFFFF' } },
  Eclats: { gates: ['Glint'] }, // quiz sans faute : éclats autour de « bravo »
};
const jobs = [['assis', 'glose_assis'], ['livres', 'glose_livres'], ['soir', 'glose_soir'], ...WORDS.map(w => ['ecrit', `glose_ecrit_${w}`, { word: w }]),
  ['soir', 'glose_reveil', { awake: true }], ['icone', 'glose_icone'], ['icone', 'glose_icone_dort', { sleep: true }]];
// réactions one-shot du quiz (animateurs seuls sur le groupe « tete » de l'icône, pivot au cou) : [fraction, valeur]
// icône d'appli générée (non branchée : ic_launcher_fg.xml reste fait à la main) : tête aux couleurs par défaut, liseré crème fixe
// (elle se détache du fond bleu nuit) ; [variante, échelle, translateX, translateY] dans le repère 108 dp.
// a2 = fec50e1 (annulé : visage plus petit) ; a3 = le visage actuel (échelle 0,15, tête penchée) gardé, oreilles −20 % et moustaches −30 % pour tenir dans le cercle (rayon max 35,7 sur 36).
const ICONS = {
  a2: [{ whisk: 0.85 }, 0.13, 20.89, 25.18],
  a3: [{ whisk: 0.7, ear: 0.8 }, 0.15, 16.8, 17.4],
};
const REACTIONS = {
  glose_icone_hoche: [900, { translateY: [[0, 0], [0.25, 10], [0.5, 0], [0.75, 8], [1, 0]], scaleY: [[0, 1], [0.25, 0.96], [0.5, 1], [0.75, 0.97], [1, 1]] }], // hoche la tête : oui
  glose_icone_penche: [1500, { rotation: [[0, 0], [0.22, -11], [0.72, -11], [1, 0]], translateX: [[0, 0], [0.22, -6], [0.72, -6], [1, 0]] }], // penche la tête, curieux
};

(async () => {
  const b = await chromium.launch(), gates = {};
  for (const d of ['drawable', 'animator', 'values']) fs.mkdirSync(path.join(RES, d), { recursive: true }); fs.mkdirSync(path.dirname(KT), { recursive: true });
  for (const d of ['drawable', 'animator', 'interpolator']) if (fs.existsSync(path.join(RES, d))) for (const f of fs.readdirSync(path.join(RES, d))) if (/^glose_.*\.xml$/.test(f)) fs.unlinkSync(path.join(RES, d, f)); // layers come and go
  try {
    for (const [pose, name, v] of jobs) {
      const out = `${TMP}/${name}.svg`;
      await buildOne(b, { pose, out, variant: Object.assign({ all: true, halo: '#F0E801', haloW: pose === 'icone' ? 8 : 12 }, OPT, v) }); // liseré de nuit (jeton gloseHalo)
      const svg = fs.readFileSync(path.join(__dirname, '..', out), 'utf8');
      Object.assign(gates, convert(svg, name, RES, { noSlots: pose === 'icone' })); // head icons never wear accessories (Glose.WORN)
      if (ONCE.includes(v && v.word)) convert(svg, `${name}_fin`, RES, { once: [KT_ECRIT, ONCE_FROM, REWIND_MS], vd: `${name}_vd` });
    }
    const icon = ICONS[process.env.GLOSE_ICON || 'a3']; // owner choice 2026-11-08: A3
    if (icon) {
      const [v, k, tx, ty] = icon, svg = await buildOne(b, { pose: 'icone', out: `${TMP}/ic_launcher.svg`, variant: Object.assign({ halo: '#F0E8D8', haloW: 8 }, v) });
      const tmp = path.join(__dirname, '..', TMP, 'res'); convert(svg, 'ic_launcher_src', tmp, { noSlots: true });
      const vd = fs.readFileSync(path.join(tmp, 'drawable', 'ic_launcher_src_vd.xml'), 'utf8').replace(/\?attr\/(\w+)/g, (_, a) => DEFAULT[a]);
      const body = /<group android:name="cadre"[\s\S]*<\/group>/.exec(vd)[0];
      fs.writeFileSync(path.join(RES, 'drawable', 'ic_launcher_fg.xml'), `<?xml version="1.0" encoding="utf-8"?>\n<!-- Generated by design/mascotte/android/make_android.js (GLOSE_ICON=${process.env.GLOSE_ICON}): Monsieur Glose's head, default colours, cream outline, inside the round mask. -->\n` +
        `<vector xmlns:android="http://schemas.android.com/apk/res/android" xmlns:aapt="http://schemas.android.com/aapt"\n    android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">\n` +
        `<group android:scaleX="${k}" android:scaleY="${k}" android:translateX="${tx}" android:translateY="${ty}">${body}</group>\n</vector>\n`);
    }
  } finally { await b.close(); }
  require('./reactions.js')(RES); // réactions au toucher (glose_<pose>_tap / _special), un AVD par calque animé
  for (const [name, [dur, props]] of Object.entries(REACTIONS)) {
    const h = Object.entries(props).map(([p, kf]) => `<propertyValuesHolder android:propertyName="${p}" android:valueType="floatType">` +
      kf.map(([t, v], i) => `<keyframe android:fraction="${t}" android:value="${v}"${i ? ' android:interpolator="@interpolator/glose_ease"' : ''}/>`).join('') + '</propertyValuesHolder>').join('');
    animated(name, 'glose_icone_vd', { tete: `<objectAnimator android:duration="${dur}" android:interpolator="@android:interpolator/linear">${h}</objectAnimator>` }, RES);
  }
  fs.mkdirSync(path.join(RES, 'interpolator'), { recursive: true });
  for (const n of HERMITE) fs.writeFileSync(path.join(RES, 'interpolator', `${n}.xml`), hermiteXml(n)); // key easings (svg2avd.hermite)
  console.log(`${HERMITE.size} interpolateurs de clé glose_h_*`);
  fs.writeFileSync(path.join(RES, 'interpolator', 'glose_ease.xml'), '<?xml version="1.0" encoding="utf-8"?>\n<pathInterpolator xmlns:android="http://schemas.android.com/apk/res/android" android:controlX1="0.45" android:controlY1="0" android:controlX2="0.55" android:controlY2="1"/>\n');
  const names = Object.keys(gates).sort(), gate = a => /^glose([A-Z][a-z]+)_/.exec(a)[1];
  const item = (k, v) => `<item name="${k}">${v}</item>`;
  const open = (g, swap = {}) => names.filter(a => gate(a) === g).map(a => item(a, swap[gates[a]] || gates[a])).join('');
  const shut = g => names.filter(a => gate(a) === g).map(a => item(a, '#00000000')).join('');
  const attrs = [...new Set(Object.values(TOKENS))].concat(names).map(a => `    <attr name="${a}" format="color"/>`).join('\n');
  fs.writeFileSync(path.join(RES, 'values', 'glose_attrs.xml'), `<?xml version="1.0" encoding="utf-8"?>\n<!-- Généré par design/mascotte/android/make_android.js -->\n<resources>\n${attrs}\n</resources>\n`);
  const tokens = o => Object.entries(o).filter(([k]) => k.startsWith('glose')).map(([k, v]) => item(k, v)).join('');
  const styles = [`    <style name="Glose">${tokens(DEFAULT)}${open('Ruban')}${[...new Set(names.map(gate))].filter(g => g !== 'Ruban').map(shut).join('')}</style>`,
    ...Object.entries(STYLES).map(([n, o]) => `    <style name="Glose.${n}" parent="">${tokens(o)}${(o.gates || []).map(g => open(g, o.swap)).join('')}${(o.off || []).map(shut).join('')}</style>`)];
  // layers of each layered drawable (Glose.drawable composes them) and the accessory gate each overlay opens
  const R = n => `R.drawable.${n}`, gateOf = Object.entries(STYLES).flatMap(([n, o]) => (o.gates || []).filter(g => LAYERED.includes(g)).map(g => `R.style.Glose_${n.replace('.', '_')} to "${g}"`));
  fs.writeFileSync(KT, `// Generated by design/mascotte/android/make_android.js: do not edit.\npackage fr.definiday\n\n` +
    `/** Glose drawn in layers, back to front: body parts (gate "") and accessory pieces, drawn only when an overlay opens their gate. */\n` +
    `internal object GloseLayers {\n    val layers: Map<Int, List<Pair<String, Int>>> = mapOf(\n` +
    Object.entries(LAYERS).sort().map(([k, ls]) => `        ${R(k)} to listOf(${ls.map(([g, n]) => `"${g}" to ${R(n)}`).join(', ')}),\n`).join('') +
    `    )\n    /** Theme overlay -> gate it opens. */\n    val gates: List<Pair<Int, String>> = listOf(${gateOf.join(', ')})\n}\n`);
  fs.writeFileSync(path.join(RES, 'values', 'glose_layers.xml'), `<?xml version="1.0" encoding="utf-8"?>\n<!-- Generated by design/mascotte/android/make_android.js: names used by the app whose drawing is shared -->\n<resources>\n` +
    Object.entries(ALIASES).sort().map(([a, n]) => `    <item name="${a}" type="drawable">@drawable/${n}</item>\n`).join('') + '</resources>\n');
  fs.writeFileSync(path.join(RES, 'values', 'glose_styles.xml'), `<?xml version="1.0" encoding="utf-8"?>\n<!-- Généré par design/mascotte/android/make_android.js. Glose = défaut ; les autres se superposent avec Theme.applyStyle(id, true) -->\n<resources>\n${styles.join('\n')}\n</resources>\n`);
})().catch(e => { console.error(e.stack || e.message); process.exit(1); });
