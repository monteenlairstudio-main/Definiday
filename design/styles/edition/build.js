// Direction « Édition » : génère les maquettes HTML (360 × 800) et les rend en PNG (×2), puis la planche.
// node design/styles/edition/build.js
const fs = require('fs'), path = require('path');
const { chromium } = require('playwright');

const D = __dirname, M = path.join(D, '../../mascotte'), F = '../../../app/src/main/res/font/';
const svg = f => fs.readFileSync(path.join(M, f), 'utf8');
const uri = f => 'data:image/svg+xml;base64,' + Buffer.from(svg(f)).toString('base64');
// espace fine insécable (avant ; : ! ? et dans « ») : EB Garamond n’a pas U+202F, on pose U+2009 entre deux U+2060
const t = '\u2060\u2009\u2060';
const DEF = `Tran&shy;chant d’une épée, ou coup por&shy;té avec ce tran&shy;chant${t}; aus&shy;si, lourde épée de guerre.`;

const THEMES = {
  light: { papier: '#F5EFE2', feuille: '#FBF8F1', fond2: '#EDE3CF', encre: '#211A14', encre2: '#5B5144',
           violet: '#5B3B8C', surViolet: '#F5EFE2', violetPale: '#E9E1F1', filet: 'rgba(33,26,20,.18)',
           sauge: '#B9D1AB', terre: '#EBC0AE', surEtat: '#211A14', grain: 'rgba(70,50,25,.022)', halo: '#EDE3CF' },
  dark:  { papier: '#15110D', feuille: '#201A15', fond2: '#2A221B', encre: '#EEE6D6', encre2: '#B6AA96',
           violet: '#B79CE8', surViolet: '#15110D', violetPale: '#2C2438', filet: 'rgba(238,230,214,.16)',
           sauge: '#3E5A3A', terre: '#6E3A2C', surEtat: '#EEE6D6', grain: 'rgba(255,240,210,.014)', halo: 'radial-gradient(circle at 50% 62%,#5A4630,#2A221B 70%)' },
};
// palette « braise » de styles.json (celle d’estramaçon)
const P = { fond_haut: '#3A1A10', fond_bas: '#1A0A06', mot: '#FFB36B', nature: '#E08A5E', texte: '#F2DACB', exemple: '#D9A88F' };

const ICON = {
  pelote: '<circle cx="12" cy="12" r="8"/><path d="M5.5 8.5c4 .5 9 4 10.5 10M8 5c4 2 8 7 8.5 13M4.5 13c3 0 7 2 9 6.5M20 13.5c1.5 2 2.5 4 1.5 6.5"/>',
  livres: '<path d="M4 4h4v16H4zM9.5 6h4v14h-4zM15 6.8l3.6-1.2 3.4 13.6-3.6 1.2z"/><path d="M4 8h4M9.5 10h4"/>',
  reglages: '<path d="M4 7h10M18 7h2M4 17h4M12 17h8"/><circle cx="16" cy="7" r="2"/><circle cx="10" cy="17" r="2"/>',
  academie: '<path d="M3 9 12 4l9 5H3zM5 9v8M9.5 9v8M14.5 9v8M19 9v8M3.5 19.5h17"/>',
  tlfi: '<path d="M12 6.5C9.5 5 6.5 4.5 3.5 5v13c3-.5 6 0 8.5 1.5 2.5-1.5 5.5-2 8.5-1.5V5c-3-.5-6 0-8.5 1.5zM12 6.5v13"/>',
  partager: '<path d="M12 3v12M7.5 7.5 12 3l4.5 4.5M5 12v8h14v-8"/>',
  hasard: '<rect x="4" y="4" width="16" height="16" rx="2.5"/><circle cx="9" cy="9" r=".9" fill="currentColor"/><circle cx="15" cy="15" r=".9" fill="currentColor"/><circle cx="12" cy="12" r=".9" fill="currentColor"/>',
  fermer: '<path d="M6 6l12 12M18 6 6 18"/>',
  retour: '<path d="M19 12H5M11 6l-6 6 6 6"/>',
};
const icon = (n, s = 22) => `<svg class="ic" width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${ICON[n]}</svg>`;
// la pelote du filet : un petit disque d’encre avec deux brins
const ball = (c, s = 9) => `<svg width="${s}" height="${s}" viewBox="0 0 10 10" aria-hidden="true"><circle cx="5" cy="5" r="4.4" fill="${c}"/><path d="M1.6 3.6c2.4.2 5 2.4 5.8 5M3.2 1.4c2.2 1 4.6 4 5 7" stroke="var(--papier)" stroke-width=".7" fill="none" opacity=".75"/></svg>`;
const filet = (c, w = '100%') => `<div class="filet" style="width:${w};color:${c}"><i></i>${ball(c)}<i></i></div>`;

const css = th => `
@font-face{font-family:Garamond;src:url('${F}eb_garamond.ttf')}
@font-face{font-family:Garamond;font-style:italic;src:url('fonts/eb_garamond_italic.woff2')}
@font-face{font-family:Didone;src:url('${F}playfair_display.ttf');font-weight:400 900}
@font-face{font-family:Tenor;src:url('${F}tenor_sans.ttf')}
:root{${Object.entries(th).map(([k, v]) => `--${k}:${v}`).join(';')}}
*{box-sizing:border-box;margin:0;padding:0}
html,body{width:360px;height:800px;overflow:hidden}
body{background:var(--papier);color:var(--encre);font:17px/1.4 Garamond,serif;font-feature-settings:"onum","liga","kern";
 position:relative;-webkit-font-smoothing:antialiased}
.verge{position:absolute;inset:0;pointer-events:none;background:
 repeating-linear-gradient(0deg,var(--grain) 0 1px,transparent 1px 3px),
 repeating-linear-gradient(90deg,var(--grain) 0 1px,transparent 1px 27px)}
.sc{font-variant:small-caps;font-feature-settings:"smcp","c2sc","lnum";letter-spacing:.09em}
.lsc{font-variant:small-caps;font-feature-settings:"smcp","lnum";letter-spacing:.06em}
.it{font-style:italic}
.filet{display:flex;align-items:center;gap:6px;margin:0 auto}
.filet i{flex:1;height:4px;border-top:1px solid currentColor;border-bottom:1px solid currentColor}
.ic{display:block}
.btn{width:44px;height:44px;display:grid;place-items:center;color:var(--encre)}
.primary{display:flex;align-items:center;justify-content:center;gap:10px;height:52px;border-radius:4px;background:var(--violet);
 color:var(--surViolet);font-size:19px}
.ghost{display:flex;align-items:center;justify-content:center;height:52px;border-radius:4px;border:1px solid var(--violet);
 color:var(--violet);font-size:18px;outline:1px solid var(--violet);outline-offset:-5px}
`;
const page = (th, title, body) => `<!doctype html><html lang="fr"><head><meta charset="utf-8"><title>${title}</title>
<style>${css(THEMES[th])}</style></head><body>${body}</body></html>`;

// ───── couverture du mot (carte) : palette du mot + cadre et numéro de la collection
const cover = () => `
<div style="position:relative;margin:0 16px;border-radius:4px;padding:30px 24px 22px;
 background:linear-gradient(${P.fond_haut},${P.fond_bas});box-shadow:0 1px 0 rgba(0,0,0,.06),0 10px 24px -14px rgba(40,20,10,.55);
 outline:1px solid ${P.nature}55;outline-offset:-1px">
 <div style="position:absolute;inset:8px;border:1px solid ${P.nature}88;border-radius:2px"></div>
 <div class="sc" style="position:absolute;top:0;left:50%;transform:translateX(-50%);background:${P.fond_haut};padding:0 10px;line-height:17px;font-size:13px;color:${P.nature}">N° 0412</div>
 <svg width="22" height="38" viewBox="0 0 22 38" style="position:absolute;top:-1px;right:22px" aria-label="Ajouter un signet">
  <path d="M1 0h20v36l-10-7-10 7z" fill="${P.nature}"/><path d="M5 0v28" stroke="${P.fond_bas}" stroke-opacity=".25"/></svg>
 <div class="sc" style="text-align:center;font-size:13px;color:${P.nature};margin-top:2px">Mot du jour</div>
 <div style="font-family:Didone;font-weight:500;font-size:42px;line-height:1.08;text-align:center;color:${P.mot};margin-top:2px;letter-spacing:.005em">estramaçon</div>
 <div class="it" style="text-align:center;font-size:17px;color:${P.nature};margin-top:2px">n.${t}m.</div>
 <div style="width:96px;margin:10px auto 12px">${filet(P.nature)}</div>
 <p style="text-align:center;font-size:17.5px;line-height:1.38;color:${P.texte}">
  <span class="lsc" style="color:${P.nature}">Vieilli.</span> ${DEF}</p>
</div>`;

const exlibris = (th, gl) => `
<div style="position:relative;width:112px;height:120px;margin:0 auto">
 <div style="position:absolute;left:0;right:0;top:12px;bottom:0;border-radius:56px 56px 3px 3px;border:1px solid var(--violet);padding:3px">
  <div style="width:100%;height:100%;border-radius:52px 52px 1px 1px;border:1px solid var(--violet);background:var(--halo)"></div></div>
 <div style="position:absolute;left:-6px;right:-6px;top:-6px;height:124px">${gl}</div>
</div>`;

function home(th) {
  const gl = svg('glose_assis.svg').replace('<svg ', '<svg width="124" height="124" style="display:block" ');
  return page(th, 'Definiday · accueil', `<div class="verge"></div>
<div style="position:relative;height:800px">
 <div style="display:flex;justify-content:space-between;align-items:center;padding:10px 10px 0 12px">
  <div style="display:flex;align-items:center;gap:4px;height:44px;color:var(--encre)">${icon('pelote')}<span class="sc" style="font-size:15px;color:var(--violet)">8/39</span></div>
  <div style="display:flex">
   <div class="btn" aria-label="Collection">${icon('livres')}</div><div class="btn" aria-label="Réglages">${icon('reglages')}</div></div>
 </div>
 <div style="margin-top:-38px">${exlibris(th, gl)}</div>
 <div style="text-align:center;margin:6px 0 14px;font-size:15px;color:var(--encre2)"><span class="it">vendredi 6 novembre</span></div>
 ${cover()}
 <div style="display:grid;grid-template-columns:repeat(4,1fr);margin:12px 16px 0">
  ${[['academie', 'Académie'], ['tlfi', 'TLFi'], ['partager', 'Partager'], ['hasard', 'Au hasard']].map(([i, l], k) => `
  <div style="height:62px;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:4px;color:var(--encre);
   ${k ? 'border-left:1px solid var(--filet)' : ''}">${icon(i)}<span class="sc" style="font-size:13px;letter-spacing:.05em;color:var(--encre2)">${l}</span></div>`).join('')}
 </div>
 <div style="margin:14px 28px 0 76px;text-align:right">
  <div class="sc" style="font-size:13px;color:var(--violet)">Exemple</div>
  <p class="it" style="font-size:17px;line-height:1.35;color:var(--encre)">«${t}D’un estramaçon, le reître fendit le chapeau de son adversaire.${t}»</p>
 </div>
 <div style="margin:16px 20px 14px">${filet('var(--violet)')}</div>
 <div style="margin:0 20px"><div class="primary"><span class="lsc">Quiz du soir</span><span class="it" style="font-size:15px;opacity:.85">quatre épreuves</span></div></div>
 <div style="margin:16px 20px 0">
  <div class="sc" style="font-size:13px;color:var(--encre2)">Au sommaire du jour</div>
  <div style="display:flex;gap:22px;margin-top:4px;font-size:17px;white-space:nowrap">
  ${[['i', 'estramaçon', 1], ['ii', 'adret'], ['iii', 'souquenille']].map(([n, w, on]) => `
  <span style="height:40px;display:flex;align-items:center;gap:5px;${on ? 'box-shadow:inset 0 -2px var(--violet)' : ''}"><span class="it" style="color:var(--violet);font-size:15px">${n}.</span>${w}</span>`).join('')}
  </div>
 </div>
</div>`);
}

function quiz() {
  const pel = [1, 1, 0, 0];
  return page('light', 'Definiday · quiz', `<div class="verge"></div>
<div style="position:relative;height:800px">
 <div style="display:flex;align-items:center;justify-content:space-between;padding:10px 10px 0">
  <div class="btn" aria-label="Fermer">${icon('fermer')}</div>
  <div class="sc" style="font-size:15px;color:var(--violet)">Épreuves du soir</div>
  <div class="sc" style="width:44px;text-align:center;font-size:15px;color:var(--encre2)">2/4</div>
 </div>
 <div style="position:relative;margin:6px 60px 0;height:20px" aria-label="Question 2 sur 4">
  <div style="position:absolute;left:0;right:0;top:9px;border-top:1px solid var(--violet);opacity:.55"></div>
  <div style="position:relative;display:flex;justify-content:space-between">${pel.map((d, i) => `<span style="display:grid;place-items:center;width:20px;height:20px;background:var(--papier)">${d ? ball('var(--violet)', 16) : i === 2 ? `<svg width="16" height="16"><circle cx="8" cy="8" r="6.5" fill="none" stroke="var(--violet)" stroke-width="1.5"/></svg>` : `<svg width="16" height="16"><circle cx="8" cy="8" r="6.5" fill="none" stroke="var(--encre2)" stroke-width="1"/></svg>`}</span>`).join('')}</div>
 </div>
 <div style="margin:26px 24px 0">
  <div class="it" style="font-size:16px;color:var(--encre2)">Quel mot répond à cette définition${t}?</div>
  <p style="margin-top:10px;font-size:20px;line-height:1.36;text-align:justify;hyphens:manual">
   <span style="float:left;font-family:Didone;font-size:62px;line-height:50px;margin:5px 6px 0 -2px;color:var(--violet)">V</span><span class="lsc" style="color:var(--violet)">ieilli.</span> ${DEF}</p>
 </div>
 <div style="margin:22px 16px 0;border-top:1px solid var(--filet)">
  ${[['a', 'capitan', 'faux'], ['b', 'gueux'], ['c', 'estramaçon', 'juste'], ['d', 'atticisme']].map(([l, w, s]) => `
  <div style="display:flex;align-items:center;gap:14px;height:56px;padding:0 14px;border-bottom:1px solid var(--filet);
   ${s === 'faux' ? 'background:var(--terre);' : s === 'juste' ? 'background:var(--sauge);' : ''}${s ? 'color:var(--surEtat)' : 'color:var(--encre2)'}">
   <span class="it" style="width:12px;font-size:17px;${s ? '' : 'color:var(--violet)'}">${l}</span>
   <span style="flex:1;font-size:20px">${w}${s === 'faux' ? '<sup style="font-size:12px;margin-left:1px;font-variant-numeric:lining-nums">1</sup>' : ''}</span>
   ${s === 'faux' ? '<span class="sc" style="font-size:13px">votre choix&nbsp;&nbsp;✗</span>' : s === 'juste' ? '<span class="sc" style="font-size:13px">bonne réponse&nbsp;&nbsp;✓</span>' : ''}
  </div>`).join('')}
 </div>
 <div style="position:absolute;left:24px;right:24px;bottom:96px">
  <div style="width:84px;border-top:1px solid var(--encre2);margin-bottom:10px"></div>
  <div style="display:flex;gap:12px;align-items:flex-start">
   <img src="${uri('glose_icone.svg')}" width="40" height="40" alt="" style="flex:none;margin-top:2px">
   <p style="font-size:15.5px;line-height:1.38;text-align:justify;hyphens:auto"><sup style="font-size:10px;font-variant-numeric:lining-nums">1</sup>&nbsp;<span class="it">Presque${t}! Le capitan est un fanfaron de comédie. L’estramaçon, lui, tranche.</span> <span class="sc" style="font-size:12px;color:var(--violet)">N.${t}d.${t}G.</span></p>
  </div>
 </div>
 <div style="position:absolute;left:20px;right:20px;bottom:24px"><div class="ghost"><span class="lsc">Épreuve suivante</span></div></div>
</div>`);
}

function collection() {
  const plate = (f, no, nom, st, kind) => `
 <div style="text-align:center">
  <div style="position:relative;height:96px;border:1px solid ${kind === 'on' ? 'var(--violet)' : 'var(--filet)'};${kind === 'on' ? 'outline:1px solid var(--violet);outline-offset:2px;' : ''}
   ${kind === 'off' ? 'background:repeating-linear-gradient(135deg,transparent 0 6px,var(--filet) 6px 7px);' : 'background:var(--feuille);'}display:grid;place-items:center">
   <img src="${uri(f)}" width="84" height="84" alt="" style="${kind === 'off' ? 'opacity:.35;filter:saturate(.6)' : ''}">
  </div>
  <div class="sc" style="font-size:12px;color:var(--violet);margin-top:7px">Pl.${t}${no}</div>
  <div style="font-size:15px;line-height:1.15">${nom}</div>
  <div class="${kind === 'on' ? 'sc' : 'it'}" style="font-size:${kind === 'on' ? 12 : 13.5}px;line-height:1.2;color:${kind === 'on' ? 'var(--violet)' : 'var(--encre2)'};margin-top:2px">${st}</div>
 </div>`;
  const sect = (n, titre, count) => `<div style="display:flex;align-items:baseline;gap:10px;margin:0 20px 12px;padding-bottom:6px;border-bottom:1px solid var(--filet)">
  <span class="it" style="color:var(--violet);font-size:17px">${n}.</span><span class="sc" style="font-size:15px;flex:1">${titre}</span><span class="it" style="font-size:14px;color:var(--encre2)">${count}</span></div>`;
  return page('light', 'Definiday · collection', `<div class="verge"></div>
<div style="position:relative">
 <div style="display:flex;align-items:center;padding:10px 10px 0"><div class="btn" aria-label="Retour">${icon('retour')}</div></div>
 <div style="text-align:center;margin-top:-6px">
  <div class="sc" style="font-size:13px;color:var(--violet)">Collection de Monsieur Glose</div>
  <div style="font-family:Didone;font-size:34px;line-height:1.15;margin-top:2px">Catalogue</div>
  <div class="it" style="font-size:16px;color:var(--encre2)">trente-neuf planches, dont huit parues</div>
 </div>
 <div style="margin:14px 20px 20px">${filet('var(--violet)')}</div>
 ${sect('I', 'Poses', '4 planches')}
 <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px 14px;margin:0 20px 22px">
  ${plate('glose_assis.svg', 1, 'Sur son volume', 'porté', 'on')}
  ${plate('glose_soir.svg', 2, 'Sieste dans la tasse', 'parue')}
  ${plate('glose_livres.svg', 3, 'Sur la pile de livres', 'parue')}
  ${plate('glose_icone_dort.svg', 4, 'Réveil dans la tasse', '<span class="sc" style="font-size:12px">à paraître</span><br>encore 9 quiz', 'off')}
 </div>
 ${sect('II', 'Mots du fil', '7 planches')}
 <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px 14px;margin:0 20px">
  ${plate('glose_ecrit_mot.svg', 5, '«' + t + 'mot' + t + '»', 'parue')}
  ${plate('glose_ecrit_encore.svg', 6, '«' + t + 'encore' + t + '»', 'parue')}
  ${plate('glose_ecrit_rare.svg', 7, '«' + t + 'rare' + t + '»', '<span class="sc" style="font-size:12px">à paraître</span><br>encore 21 quiz', 'off')}
 </div>
</div>`);
}

function lock() {
  return page('light', 'Definiday · écran verrouillé', `
<div style="position:absolute;inset:0;background:linear-gradient(${P.fond_haut},${P.fond_bas})"></div>
<div class="verge" style="--grain:rgba(255,220,190,.018)"></div>
<div style="position:relative;height:800px;color:${P.texte}">
 <div style="display:flex;justify-content:space-between;padding:8px 16px 0;font:13px Tenor,sans-serif;opacity:.85"><span>10:24</span><span>▾ ▴ 82 %</span></div>
 <div style="text-align:center;margin-top:62px;font:300 84px/1 Tenor,sans-serif;letter-spacing:-.02em;opacity:.95">10:24</div>
 <div style="text-align:center;margin-top:10px;font:15px Tenor,sans-serif;opacity:.85">ven. 6 novembre</div>
 <div style="position:absolute;left:32px;right:32px;top:404px">
  <div class="sc" style="text-align:center;font-size:13px;color:${P.nature}">Definiday · N° 0412</div>
  <div style="margin:8px auto 0;width:150px">${filet(P.nature)}</div>
  <div style="font-family:Didone;font-weight:500;font-size:46px;line-height:1.1;text-align:center;color:${P.mot};margin-top:12px">estramaçon</div>
  <div class="it" style="text-align:center;font-size:18px;color:${P.nature}">n.${t}m.</div>
  <p style="margin-top:12px;text-align:center;font-size:18.5px;line-height:1.36">
   <span class="lsc" style="color:${P.nature}">Vieilli.</span> ${DEF}</p>
  <p class="it" style="margin-top:10px;text-align:center;font-size:16px;line-height:1.35;color:${P.exemple}">«${t}D’un estramaçon, le reître fendit le chapeau de son adversaire.${t}»</p>
  <div class="sc" style="text-align:center;margin-top:16px;font-size:11.5px;color:${P.exemple};letter-spacing:.12em">achevé d’imprimer le 6 novembre</div>
 </div>
 <div style="position:absolute;bottom:26px;left:28px;right:28px;display:flex;justify-content:space-between;opacity:.5">
  <span style="width:44px;height:44px;border-radius:50%;background:rgba(255,255,255,.12)"></span><span style="width:44px;height:44px;border-radius:50%;background:rgba(255,255,255,.12)"></span></div>
</div>`);
}

function board(shots) {
  const T = THEMES;
  const sw = (n, hex, fg, ratio) => `<div style="display:flex;align-items:center;gap:10px"><span style="width:36px;height:36px;border-radius:3px;background:${hex};border:1px solid rgba(0,0,0,.12);display:grid;place-items:center;color:${fg};font:16px Garamond">Aa</span>
   <span style="font-size:15px;line-height:1.15"><span class="sc" style="font-size:13px">${n}</span><br><span style="font-feature-settings:'lnum'">${hex}</span> <span class="it" style="color:var(--encre2)">${ratio}</span></span></div>`;
  return `<!doctype html><html lang="fr"><head><meta charset="utf-8"><title>Édition · planche</title><style>${css(T.light)}
html,body{width:2010px;height:1200px}</style></head><body><div class="verge"></div>
<div style="position:relative;padding:44px 48px 0">
 <div style="display:flex;align-items:flex-end;gap:48px">
  <div style="flex:none">
   <div class="sc" style="font-size:15px;color:var(--violet)">Definiday · direction de style</div>
   <div style="font-family:Didone;font-size:64px;line-height:1">Édition</div>
  </div>
  <p class="it" style="flex:1;font-size:19px;line-height:1.35;color:var(--encre2);max-width:760px">Chaque mot est un volume de la collection de Monsieur Glose${t}: papier vergé, encre violette d’écolier, filet double coupé d’une pelote, numéro de catalogue, petites capitales, texte justifié à la française. Le quiz devient des épreuves, la collection un catalogue, Glose signe l’ex-libris et les notes.</p>
  <div style="display:grid;grid-template-columns:repeat(4,auto);gap:10px 26px">
   ${sw('Papier', T.light.papier, T.light.encre, 'fond')}${sw('Encre', T.light.encre, T.light.papier, '15,0:1')}${sw('Encre 2', T.light.encre2, T.light.papier, '6,8:1')}${sw('Violet', T.light.violet, T.light.papier, '7,5:1')}
   ${sw('Nuit', T.dark.papier, T.dark.encre, 'fond')}${sw('Craie', T.dark.encre, T.dark.papier, '15,1:1')}${sw('Craie 2', T.dark.encre2, T.dark.papier, '8,2:1')}${sw('Violet nuit', T.dark.violet, T.dark.papier, '8,0:1')}
  </div>
 </div>
 <div style="margin:22px 0 26px">${filet('var(--violet)')}</div>
 <div style="display:flex;gap:28px">
  ${shots.map(([f, cap]) => `<figure style="width:360px"><img src="${f}" width="360" height="800" style="display:block;border-radius:22px;box-shadow:0 0 0 1px rgba(33,26,20,.14),0 18px 36px -18px rgba(40,25,10,.45)">
   <figcaption class="sc" style="text-align:center;margin-top:12px;font-size:15px;color:var(--encre2)">${cap}</figcaption></figure>`).join('')}
 </div>
 <div style="margin:34px 0 18px">${filet('var(--violet)')}</div>
 <div style="display:flex;align-items:center;gap:56px">
  <div><div class="sc" style="font-size:13px;color:var(--violet)">Titres · Playfair Display</div><div style="font-family:Didone;font-size:40px;line-height:1.1">estramaçon</div></div>
  <div><div class="sc" style="font-size:13px;color:var(--violet)">Texte · EB Garamond</div><div style="font-size:24px">Tranchant d’une épée <span class="sc" style="font-size:20px">Vieilli · N° 0412</span></div></div>
  <div><div class="sc" style="font-size:13px;color:var(--violet)">Exemples · EB Garamond Italique</div><div class="it" style="font-size:24px">«${t}le reître fendit le chapeau${t}»</div></div>
  <div style="margin-left:auto;display:flex;align-items:center;gap:14px">
   <div style="text-align:right"><div class="sc" style="font-size:13px;color:var(--encre2)">Colophon</div><div class="it" style="font-size:18px;color:var(--encre2)">achevé d’imprimer par Monsieur Glose</div></div>
   <div style="width:64px;height:64px;border-radius:50%;border:1px solid var(--violet);outline:1px solid var(--violet);outline-offset:3px;display:grid;place-items:center;background:var(--fond2)"><img src="${uri('glose_icone.svg')}" width="50" height="50" alt=""></div>
  </div>
 </div>
</div></body></html>`;
}

(async () => {
  const out = { home_light: home('light'), home_dark: home('dark'), quiz: quiz(), collection: collection(), lockscreen: lock() };
  const b = await chromium.launch(), p = await b.newPage({ viewport: { width: 360, height: 800 }, deviceScaleFactor: 2 });
  for (const [n, html] of Object.entries(out)) {
    fs.writeFileSync(path.join(D, n + '.html'), html);
    await p.goto('file://' + path.join(D, n + '.html')); await p.evaluate(() => document.fonts.ready);
    await p.waitForTimeout(4500); // laisse le fil écrire son mot (SMIL des poses « écrit »)
    await p.screenshot({ path: path.join(D, n + '.png') });
  }
  fs.writeFileSync(path.join(D, 'board.html'), board([['home_light.png', 'Accueil, jour'], ['home_dark.png', 'Accueil, nuit'], ['quiz.png', 'Épreuves du soir'], ['collection.png', 'Catalogue'], ['lockscreen.png', 'Écran verrouillé']]));
  await p.setViewportSize({ width: 2010, height: 1200 });
  await p.goto('file://' + path.join(D, 'board.html')); await p.evaluate(() => document.fonts.ready);
  await p.screenshot({ path: path.join(D, 'board.png') });
  await b.close();
})();
