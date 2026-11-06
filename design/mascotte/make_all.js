// node make_all.js [filtre]  -> régénère tous les livrables (SVG animés + PNG 1024/128) dans un seul Chromium.
const { buildOne } = require('./build.js');
const { chromium } = require(process.env.PLAYWRIGHT || 'playwright'); // PLAYWRIGHT=/opt/node-tools/node_modules/playwright si installé ailleurs
const WORDS = ['mot', 'bravo', 'rare', 'dodo', 'encore'];
const T = { assis: 1, livres: 1, ecrit: 4.5, soir: 1, icone: 1 };
const jobs = [];
const add = (pose, name, variant = {}, dir = '', sizes) => jobs.push({ pose, variant, out: `${dir}${name}.svg`, png: `rendus/${name}`, t: T[pose], sizes });
add('assis', 'glose_assis'); add('livres', 'glose_livres'); add('soir', 'glose_soir');
for (const w of WORDS) add('ecrit', `glose_ecrit_${w}`, { word: w });
add('icone', 'glose_icone', {}, '', [1024, 128, 48]); add('icone', 'glose_icone_dort', { sleep: true }, '', [1024, 128, 48]);
for (const skin of ['creme', 'enluminure', 'nuit']) for (const pose of ['assis', 'livres', 'soir']) add(pose, `glose_${pose}_pelage_${skin}`, { skin }, 'variantes/');
add('ecrit', 'glose_ecrit_bravo_pelage_enluminure', { skin: 'enluminure', word: 'bravo' }, 'variantes/');
for (const yarn of ['moutarde', 'sauge', 'rose']) add('assis', `glose_assis_pelote_${yarn}`, { yarn }, 'variantes/');
for (const a of ['noeud', 'echarpe', 'bonnet', 'plume', 'marque']) add('assis', `glose_assis_acc_${a}`, { acc: [a] }, 'variantes/');
add('soir', 'glose_soir_acc_bonnet', { acc: ['bonnet'] }, 'variantes/');
add('livres', 'glose_livres_acc_echarpe', { acc: ['echarpe'] }, 'variantes/');
add('ecrit', 'glose_ecrit_bravo_acc_noeud', { acc: ['noeud'], word: 'bravo' }, 'variantes/');
add('livres', 'glose_livres_acc_plume', { acc: ['plume'], yarn: 'moutarde' }, 'variantes/');
(async () => {
  const f = process.argv[2], b = await chromium.launch();
  try { for (const j of jobs) if (!f || j.out.includes(f)) await buildOne(b, j); } finally { await b.close(); }
})().catch(e => { console.error(e.message); process.exit(1); });
