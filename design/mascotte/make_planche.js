// node make_planche.js -> planche.html (autonome : SVG animés en data-URI, aucune ressource externe) + planche.png
const fs = require('fs'), path = require('path');
const { chromium } = require('/opt/node-tools/node_modules/playwright');
const D = __dirname;
const img = (f, alt, cls = '') => `<img class="${cls}" alt="${alt}" src="data:image/svg+xml;base64,${fs.readFileSync(path.join(D, f)).toString('base64')}">`;
const card = (f, title, sub, txt, opt = {}) => `<figure class="card${opt.small ? ' small' : ''}">${img(f, title)}<figcaption><b>${title}</b>${opt.rare ? ' <span class="rare">rare</span>' : ''}<i>${sub}</i>${txt ? `<span>${txt}</span>` : ''}</figcaption></figure>`;
const V = n => `variantes/${n}.svg`;

const html = `<!doctype html><html lang="fr"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Monsieur Glose</title>
<style>
:root{--bg:#F6EEDF;--card:#FBF6EC;--ink:#3B2A20;--muted:#7C5236;--gold:#A87A26;--line:#E6D7BC;--night:#2C3A5E;--chip:#EFE3CC}
@media (prefers-color-scheme:dark){:root:not([data-theme="light"]){--bg:#1C2236;--card:#262E47;--ink:#F3E8D4;--muted:#C9B79A;--gold:#E2B65A;--line:#39436A;--night:#F3E2BE;--chip:#323B5C}}
:root[data-theme="dark"]{--bg:#1C2236;--card:#262E47;--ink:#F3E8D4;--muted:#C9B79A;--gold:#E2B65A;--line:#39436A;--night:#F3E2BE;--chip:#323B5C}
*{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--ink);font:16px/1.5 Georgia,"Iowan Old Style","Times New Roman",serif}
main{max-width:1100px;margin:0 auto;padding:20px 16px 40px}
h1{font-size:30px;margin:4px 0 0;letter-spacing:.01em}h2{font-size:21px;margin:34px 0 4px;border-bottom:1px solid var(--line);padding-bottom:6px}
.lead{color:var(--muted);font-style:italic;margin:2px 0 12px}p{margin:6px 0}
.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(240px,1fr));gap:12px;margin-top:12px}
.grid.s{grid-template-columns:repeat(auto-fill,minmax(150px,1fr))}
.card{margin:0;background:var(--card);border:1px solid var(--line);border-radius:14px;padding:10px;display:flex;flex-direction:column}
.card img{width:100%;aspect-ratio:1;display:block;background:#F6EEDF;border-radius:10px}
figcaption{padding:8px 2px 2px;display:flex;flex-direction:column;gap:2px;font-size:14px}figcaption b{font-size:16px}
figcaption i{color:var(--gold);font-size:13px}figcaption span{color:var(--muted)}
.rare{display:inline-block;font:600 11px/1.6 system-ui,sans-serif;text-transform:uppercase;letter-spacing:.08em;background:var(--gold);color:#fff;border-radius:6px;padding:0 6px;width:max-content}
.hero{display:grid;grid-template-columns:1fr;gap:12px;align-items:center}
.hero img{width:100%;max-width:360px;justify-self:center;background:#F6EEDF;border-radius:16px}
@media (min-width:700px){.hero{grid-template-columns:320px 1fr}}
table{width:100%;border-collapse:collapse;font-size:14px;margin-top:8px}th,td{text-align:left;padding:6px 8px;border-bottom:1px solid var(--line);vertical-align:top}
th{font:600 12px system-ui,sans-serif;text-transform:uppercase;letter-spacing:.06em;color:var(--muted)}
.tw{overflow-x:auto;background:var(--card);border:1px solid var(--line);border-radius:14px;padding:4px 8px}
code{font:13px ui-monospace,Menlo,monospace;background:var(--chip);border-radius:4px;padding:0 4px;overflow-wrap:anywhere}
ol li,ul li{margin:4px 0}.ok{color:#5E8A4E;font-weight:bold}
.chips{display:flex;flex-wrap:wrap;gap:6px;margin-top:8px}.chips span{background:var(--chip);border-radius:999px;padding:2px 10px;font-size:13px}
</style></head><body><main>
<header class="hero">${img('glose_assis.svg', 'Monsieur Glose assis sur son gros volume')}
<div><h1>Monsieur Glose</h1><p class="lead">Le chat bibliothécaire de Definiday : lunettes rondes dorées, pelote de laine, et un mot rare par jour.</p>
<p>Une seule tête dessinée une fois pour toutes (mêmes proportions, mêmes lunettes, même palette) et posée dans quatre scènes. On débloque les poses, les mots du fil, les pelages, les pelotes et les accessoires avec les quiz du soir.</p>
<div class="chips"><span>4 poses animées</span><span>5 mots en cursive d'un trait</span><span>4 pelages dont 2 rares</span><span>4 pelotes</span><span>5 accessoires</span><span>≈ 160 Ko dans l'APK</span></div></div></header>

<h2>Les poses</h2><p class="lead">Animations SMIL en boucle sans à-coup. Le même dessin devient un AnimatedVectorDrawable Android.</p>
<div class="grid">
${card('glose_assis.svg', 'Assis sur son volume', 'pose par défaut', 'Écran principal. Tête de trois quarts, penchée ; une patte sous la pelote, l\'autre posée sur son flanc ; la queue enveloppe les pieds.')}
${card('glose_livres.svg', 'Sur la pile de livres', 'après 10 quiz', 'La patte qui pend taquine la pelote ; queue en virgule qui repasse devant elle-même, sans trait doublé.')}
${card('glose_ecrit_mot.svg', 'Le fil écrit', 'dès le 1er quiz', 'La patte levée guide le fil, qui écrit le mot d\'un seul trait puis se rembobine ; la pelote tourne avec.')}
${card('glose_soir.svg', 'Le soir, dans sa tasse', '3 soirs d\'affilée', 'Endormi, lunettes glissées sur le nez. Bougie, vapeur, « z » qui naissent contre son oreille. Image de la notification du quiz.')}
</div>

<h2>Les mots du fil</h2><p class="lead">Cursive d'un seul trait, sans point ni accent (aucun lever de plume).</p>
<div class="grid s">
${card('glose_ecrit_mot.svg', '« mot »', 'défaut', 'le mot du jour')}
${card('glose_ecrit_bravo.svg', '« bravo »', 'quiz réussi', 'réaction de fin de quiz')}
${card('glose_ecrit_encore.svg', '« encore »', 'quiz raté', 'jamais moqueur')}
${card('glose_ecrit_rare.svg', '« rare »', '30 bonnes réponses', 'clin d\'œil au mot rare')}
${card('glose_ecrit_dodo.svg', '« dodo »', 'avec la pose du soir', 'remplace « rêve »')}
</div>

<h2>Pelages</h2>
<div class="grid s">
${card('glose_assis.svg', 'Gris tigré', 'défaut', '')}
${card(V('glose_assis_pelage_creme'), 'Crème caramel', '5 quiz parfaits', '')}
${card(V('glose_assis_pelage_enluminure'), 'Enluminure', '150 bonnes réponses', 'paillettes d\'or, éclats animés', { rare: true })}
${card(V('glose_assis_pelage_nuit'), 'Nuit d\'encre', '30 soirs d\'affilée', 'pelage étoilé, socle bordeaux', { rare: true })}
${card(V('glose_soir_pelage_nuit'), 'Nuit d\'encre, le soir', 'tasse bordeaux', '', { rare: true })}
${card(V('glose_livres_pelage_enluminure'), 'Enluminure sur la pile', '', '', { rare: true })}
</div>

<h2>Pelotes</h2>
<div class="grid s">
${card('glose_assis.svg', 'Bordeaux', 'défaut', '')}
${card(V('glose_assis_pelote_moutarde'), 'Moutarde', '5 quiz', '')}
${card(V('glose_assis_pelote_sauge'), 'Sauge', '20 quiz', '')}
${card(V('glose_assis_pelote_rose'), 'Rose poudré', '15 quiz parfaits', '')}
</div>

<h2>Accessoires</h2><p class="lead">Les lunettes sont toujours là. Le reste se porte au choix, dans toutes les poses.</p>
<div class="grid s">
${card(V('glose_assis_acc_noeud'), 'Nœud papillon', '1er quiz parfait', '')}
${card(V('glose_assis_acc_echarpe'), 'Écharpe tricotée', '7 soirs d\'affilée', '')}
${card(V('glose_soir_acc_bonnet'), 'Bonnet de nuit', '14 soirs d\'affilée', '')}
${card(V('glose_assis_acc_plume'), 'Plume d\'oie', '60 bonnes réponses', '')}
${card(V('glose_assis_acc_marque'), 'Marque-page', '45 quiz', 'cuir et gland d\'or')}
${card('glose_icone.svg', 'Version icône', 'notification, collection', 'traits ×1,7, lisible en 24 px')}
</div>

<h2>Mise en place proposée</h2>
<div class="tw"><table><tr><th>Où</th><th>Quoi</th></tr>
<tr><td>Écran principal</td><td>pose choisie, animée, 160 dp ; pose du soir après 21 h (si débloquée)</td></tr>
<tr><td>Quiz</td><td>tête (icône) 56 dp pendant les questions ; à la fin : « bravo » (réussi ou parfait) ou « encore » (raté), joué une fois</td></tr>
<tr><td>Notification du soir</td><td>pose du soir en grande image fixe, tête endormie en grande icône</td></tr>
<tr><td>Écran de verrouillage</td><td>non (c'est la place du mot)</td></tr>
<tr><td>Collection</td><td>nouvel écran : grille des déblocages, silhouettes et conditions en clair, aperçu animé</td></tr></table></div>
<h3>Technique (Android sans AndroidX, minSdk 26)</h3>
<ol>
<li><b>Conversion maison</b> SVG → VectorDrawable + AnimatedVectorDrawable (<code>svg2avd.js</code>). Svg2Vector d'Android Studio a été essayé ici : il aplatit les groupes et ignore les animations.</li>
<li><b>Validé par aapt2</b> (compile + link, android-36, minSdk 26) sur 5 dessins, et relu visuellement. Reste à tester sur émulateur.</li>
<li><b>Pelages et pelotes par attributs de thème</b> (<code>?attr/gloseFur</code>…), superposés avec <code>Theme.applyStyle</code>. Pas de tint, qui recolorerait tout le dessin d'une seule teinte.</li>
<li><b>Accessoires</b> : groupes nommés dans le même dessin, affichés via <code>scaleX="?attr/gloseAccX"</code> (0 ou 1). C'est le point à vérifier en premier. En repli : un drawable par combinaison.</li>
<li><b>Mot écrit</b> : <code>stroke-dashoffset</code> devient <code>trimPathEnd</code> 0 → 1. Coût : ≈ 17 à 20 Ko compressés par pose, ≈ 160 Ko au total (+13 % de l'APK).</li></ol>
<h3>À décider</h3>
<ul><li>Seuil « réussi » : <b>70 % revient à exiger un sans-faute</b> avec des quiz de 1 à 3 questions ; je propose 2/3.</li>
<li>Les 5 mots, ou bien « lire » et « rêve » avec un petit nœud de laine en guise de point ou d'accent.</li>
<li>Seuils de déblocage, Glose sur l'écran de verrouillage ou non, pose du soir automatique après 21 h.</li></ul>

<h2>Défauts de la v2 corrigés</h2>
<ul>
<li><span class="ok">✓</span> Raideur de face : torsion de trois quarts projetée sur une sphère, tête penchée, queue qui enveloppe, pattes asymétriques.</li>
<li><span class="ok">✓</span> Fils illisibles en 128 px : fil cerné de 18 à 20 px (au lieu de 10 à 13), et une version icône.</li>
<li><span class="ok">✓</span> Trait doublé au croisement de la queue : une seule forme, le croisement redessiné par-dessus.</li>
<li><span class="ok">✓</span> Bras d'écriture : épaule, coude, avant-bras rayé, coussinets, doigts qui pincent le fil.</li>
<li><span class="ok">✓</span> Intérieur de tasse visible, « z » contre la tête, lune et étoiles deux fois plus grandes.</li>
<li><span class="ok">✓</span> Même personnage partout : une seule fonction <code>gloseHead</code> et une palette commune.</li></ul>
</main></body></html>`;
fs.writeFileSync(path.join(D, 'planche.html'), html);
console.log('planche.html', (html.length / 1048576).toFixed(2) + ' Mo');
(async () => {
  const b = await chromium.launch(), p = await b.newPage({ viewport: { width: 360, height: 800 }, deviceScaleFactor: 2 });
  await p.goto('file://' + path.join(D, 'planche.html')); await p.waitForTimeout(2500);
  await p.screenshot({ path: path.join(D, 'planche.png'), fullPage: true });
  const w = await p.evaluate(() => document.documentElement.scrollWidth); console.log('largeur de page', w);
  await b.close();
})();
