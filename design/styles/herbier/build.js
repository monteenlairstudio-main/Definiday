// Herbier des mots: builds the HTML mockups and renders them to PNG (deviceScaleFactor 2).
// node build.js   (Playwright + Chromium must be resolvable; see design/mascotte/README.md)
const fs = require('fs'), path = require('path');
const G = require('./gravure');
const OUT = __dirname, MASC = path.join(__dirname, '../../mascotte'), FONT = '../../../app/src/main/res/font/';
const nb = ' '; // fine space before ; : ? ! and inside guillemets
const q = s => `«${nb}${s}${nb}»`;

// ---------- Glose, inlined from the mascot sources (animations removed, ids prefixed) ----------
function gloseInner(file, pre) {
  let s = fs.readFileSync(path.join(MASC, file), 'utf8');
  s = s.replace(/<animate[^>]*\/>/g, '').replace(/<animate[\s\S]*?<\/animate\w*>/g, '').replace(/<title>[\s\S]*?<\/title>/, '');
  s = s.replace(/id="([^"]+)"/g, `id="${pre}$1"`).replace(/url\(#([^)]+)\)/g, `url(#${pre}$1)`).replace(/href="#([^"]+)"/g, `href="#${pre}$1"`);
  return s.slice(s.indexOf('>', s.indexOf('<svg')) + 1, s.lastIndexOf('</svg>'));
}
let gid = 0;
const glose = (file, vb, cls, label) => `<svg class="${cls}" viewBox="${vb}" role="img" aria-label="${label}">${gloseInner(file, 'g' + (gid++) + '-')}</svg>`;

// a lens that magnifies part of Glose: the drawing is used twice, once scaled inside a circular clip
function lens(pre, cx, cy, r, k) {
  return `<clipPath id="${pre}lc"><circle cx="${cx}" cy="${cy}" r="${r}"/></clipPath>
    <circle cx="${cx}" cy="${cy}" r="${r}" fill="#FBF8F1"/>
    <use href="#${pre}body" clip-path="url(#${pre}lc)" transform="translate(${cx} ${cy}) scale(${k}) translate(${-cx} ${-cy})"/>
    <circle cx="${cx}" cy="${cy}" r="${r}" fill="#CFE3EE" opacity=".16"/>
    <path d="M${cx - r * 0.62},${cy - r * 0.35}a${r * 0.72},${r * 0.72} 0 0 1 ${r * 0.5},${-r * 0.42}" stroke="#fff" stroke-width="${r * 0.09}" stroke-linecap="round" fill="none" opacity=".75"/>
    <circle cx="${cx}" cy="${cy}" r="${r + r * 0.07}" fill="none" stroke="#3B2A1E" stroke-width="${r * 0.2}"/>
    <circle cx="${cx}" cy="${cy}" r="${r + r * 0.07}" fill="none" stroke="#C99A48" stroke-width="${r * 0.11}"/>
    <circle cx="${cx}" cy="${cy}" r="${r + r * 0.03}" fill="none" stroke="#F3D79A" stroke-width="${r * 0.025}" opacity=".9"/>`;
}

// home: Glose on his book peering through a bench magnifier (loupe à pied)
function gloseLoupe(cls) {
  const pre = 'gl' + (gid++) + '-';
  const body = gloseInner('glose_assis.svg', pre);
  return `<svg class="${cls}" viewBox="110 70 860 820" role="img" aria-label="Monsieur Glose regarde le mot du jour à travers une loupe à pied">
  <defs><g id="${pre}body">${body}</g></defs>
  <use href="#${pre}body"/>
  <ellipse cx="800" cy="868" rx="62" ry="12" fill="#000" opacity=".12"/>
  <path d="M742,866c0,-22 116,-22 116,0z" fill="#8A6630" stroke="#3B2A1E" stroke-width="8" stroke-linejoin="round"/>
  <path d="M800,850V352" stroke="#3B2A1E" stroke-width="22" stroke-linecap="round"/>
  <path d="M800,850V352" stroke="#C99A48" stroke-width="10" stroke-linecap="round"/>
  <path d="M800,352L668,364" stroke="#3B2A1E" stroke-width="20" stroke-linecap="round"/>
  <path d="M800,352L668,364" stroke="#C99A48" stroke-width="9" stroke-linecap="round"/>
  <circle cx="800" cy="352" r="17" fill="#C99A48" stroke="#3B2A1E" stroke-width="7"/>
  <circle cx="800" cy="600" r="13" fill="#C99A48" stroke="#3B2A1E" stroke-width="7"/>
  ${lens(pre, 602, 368, 60, 1.4)}
  </svg>`;
}

// quiz: the head, a hand lens in front of one eye
function iconeLoupe(cls) {
  const pre = 'gi' + (gid++) + '-';
  const body = gloseInner('glose_icone.svg', pre);
  return `<svg class="${cls}" viewBox="240 92 540 540" role="img" aria-label="Monsieur Glose examine la question à la loupe">
  <defs><g id="${pre}body">${body}</g></defs>
  <use href="#${pre}body"/>
  <path d="M650,440L765,560" stroke="#3B2A1E" stroke-width="44" stroke-linecap="round"/>
  <path d="M650,440L765,560" stroke="#8A5A33" stroke-width="28" stroke-linecap="round"/>
  <path d="M694,486l16,17M722,515l16,17" stroke="#3B2A1E" stroke-width="6"/>
  ${lens(pre, 584, 374, 84, 1.34)}
  </svg>`;
}

// ---------- shared CSS ----------
const css = `
@font-face{font-family:EBG;src:url(${FONT}eb_garamond.ttf)}
@font-face{font-family:EBG;font-style:italic;src:url(fonts/eb_garamond_italic.woff2) format('woff2');unicode-range:U+0000-00FF,U+0131,U+0152-0153,U+02BB-02BC,U+02C6,U+02DA,U+02DC,U+2000-206F,U+20AC,U+2122,U+2212}
@font-face{font-family:EBG;font-style:italic;src:url(fonts/eb_garamond_italic_ext.woff2) format('woff2');unicode-range:U+0100-024F,U+1E00-1EFF,U+2C60-2C7F,U+A720-A7FF}
@font-face{font-family:Fell;src:url(${FONT}im_fell_english.ttf)}
@font-face{font-family:Courier Prime;src:url(${FONT}courier_prime.ttf)}
@font-face{font-family:Dancing;src:url(${FONT}dancing_script.ttf);font-weight:400 700}
:root{--papier:#F2ECDF;--planche:#FBF8F1;--etiq:#FFFDF7;--encre:#2A2018;--encre2:#5C4C3B;--sepia:#7B5530;--herbier:#3D5A36;--garance:#9A3B2B;--prusse:#2E4A6E;--trait:#B5A58A;--kraft:#D2B88C;--laiton:#A8823F;--juste:#D6E2C8;--faux:#EFD3C9;--bois:#ECE3D1;--ombre:rgba(60,40,20,.16);--grain:.05;--btn:#3D5A36;--btnTxt:#F2ECDF;--btnBord:#3D5A36}
.nuit{--papier:#16120E;--planche:#211B15;--etiq:#2A231B;--encre:#EEE5D3;--encre2:#BCAE97;--sepia:#D4AA6E;--herbier:#A9C79A;--garance:#E8A08C;--prusse:#9DBAE0;--trait:#6A5A45;--kraft:#7A6040;--laiton:#B8924E;--juste:#2F4630;--faux:#5A2E26;--bois:#1E1813;--ombre:rgba(0,0,0,.5);--grain:.07;--btn:#3D5A36;--btnTxt:#EEE5D3;--btnBord:#A9C79A}
*{box-sizing:border-box;margin:0;padding:0}
body{width:360px;height:800px;overflow:hidden;background:var(--papier);color:var(--encre);font-family:EBG,serif;position:relative;-webkit-font-smoothing:antialiased}
body::before{content:"";position:absolute;inset:0;pointer-events:none;opacity:var(--grain);background-image:url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='160' height='160'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='.85' numOctaves='3' stitchTiles='stitch'/%3E%3CfeColorMatrix values='0 0 0 0 .35 0 0 0 0 .25 0 0 0 0 .12 0 0 0 1.4 -.35'/%3E%3C/filter%3E%3Crect width='160' height='160' filter='url(%23n)'/%3E%3C/svg%3E");z-index:50}
.g-ln{fill:none;stroke:var(--sepia);stroke-width:1.1;stroke-linecap:round;stroke-linejoin:round}
.g-fn{fill:none;stroke:var(--sepia);stroke-width:.6;stroke-linecap:round;opacity:.85}
.g-kraft{fill:var(--kraft)}
.g-pin{fill:var(--garance)} .g-acier{stroke:#8C8C8C;stroke-width:1.2} .g-ombre{stroke:rgba(0,0,0,.18);stroke-width:1.6;stroke-linecap:round}
.g-tampon{fill:none;stroke:var(--garance);stroke-width:1;opacity:.72} .g-tampon text{fill:var(--garance);stroke:none;font-family:'Courier Prime'}
.ic{display:block}.i-ln{fill:none;stroke:var(--encre);stroke-width:1.5;stroke-linecap:round;stroke-linejoin:round}
.i-ht{fill:none;stroke:var(--encre);stroke-width:.75;stroke-linecap:round;opacity:.8}
.i-bg{fill:var(--papier);stroke:var(--encre);stroke-width:1.5}.i-pt{fill:var(--encre)}.i-sceau{fill:var(--garance)}
.statut{height:24px;display:flex;justify-content:space-between;align-items:center;padding:0 16px;font:500 12px/1 Roboto,'DejaVu Sans',sans-serif;color:var(--encre2)}
.statut i{display:inline-block;width:14px;height:8px;border:1.2px solid currentColor;border-radius:2px;margin-left:5px}
.geste{position:absolute;bottom:8px;left:50%;width:108px;height:4px;margin-left:-54px;border-radius:2px;background:var(--encre);opacity:.35}
.cote{font-family:'Courier Prime',monospace;font-size:12px;letter-spacing:.12em;text-transform:uppercase;color:var(--sepia)}
.main{font-family:Dancing,cursive;color:var(--prusse);font-weight:500}
.planche{position:absolute;background:var(--planche);box-shadow:0 1px 0 var(--ombre),0 6px 18px -8px var(--ombre);border-radius:2px}
.planche::after{content:"";position:absolute;inset:5px;border:1px solid var(--trait);outline:.5px solid var(--trait);outline-offset:-4px;pointer-events:none;border-radius:1px}
.coin{position:absolute}.coin-tl{top:-5px;left:-5px}.coin-tr{top:-5px;right:-5px}.coin-bl{bottom:-5px;left:-5px}.coin-br{bottom:-5px;right:-5px}
.btn-p{display:flex;align-items:center;justify-content:center;gap:10px;height:52px;background:var(--btn);color:var(--btnTxt);border:1px solid var(--btnBord);font-size:21px;border-radius:4px;position:relative}
.btn-p::after{content:"";position:absolute;inset:3px;border:1px solid var(--btnTxt);opacity:.45;border-radius:2px}
.btn-s{display:flex;align-items:center;justify-content:center;height:48px;border:1.5px solid var(--sepia);color:var(--encre);font-size:19px;border-radius:4px;background:var(--etiq)}
`;

const statut = (t = '08:12') => `<div class="statut"><span>${t}</span><span>●●● <i></i></span></div>`;
const page = (title, body, cls = '', extra = '') => `<!doctype html><html lang="fr"><head><meta charset="utf-8"><title>${title}</title><style>${css}${extra}</style></head><body class="${cls}">${body}<div class="geste"></div></body></html>`;

const W = { mot: 'estramaçon', nature: 'n.&nbsp;m.', def: `Vieilli. Tranchant d’une épée, ou coup porté avec ce tranchant${nb}; aussi, lourde épée de guerre.`, ex: `D’un estramaçon, le reître fendit le chapeau de son adversaire.` };
// palette braise (styles.json), the word's own colours
const BR = { haut: '#3A1A10', bas: '#1A0A06', mot: '#FFB36B', nature: '#E08A5E', texte: '#F2DACB', exemple: '#D9A88F' };

// ---------- 1. Home ----------
const homeCss = `
.barre{position:absolute;top:28px;left:8px;right:8px;display:flex;justify-content:space-between;align-items:flex-start}
.ib{width:48px;height:48px;display:flex;align-items:center;justify-content:center;position:relative}
.barre .cpt{display:flex;align-items:center;gap:0}.barre .cpt span{font-family:'Courier Prime';font-size:13px;color:var(--sepia);letter-spacing:.06em}
.glose{position:absolute;top:30px;left:50%;width:162px;height:154px;margin-left:-74px;z-index:3}
.halo{position:absolute;top:30px;left:50%;width:260px;height:200px;margin-left:-130px;background:radial-gradient(closest-side,rgba(232,180,100,.22),transparent);z-index:0}
.p-home{left:12px;right:12px;top:176px;height:466px}
.p-tete{position:absolute;left:20px;right:14px;top:16px;display:flex;justify-content:space-between;align-items:center}
.monture{position:absolute;left:24px;right:24px;top:52px;height:204px;background:linear-gradient(${BR.haut},${BR.bas});padding:20px 20px 0;box-shadow:0 2px 0 rgba(0,0,0,.08),0 0 0 1px rgba(224,138,94,.35)}
.monture .mot{font-size:46px;line-height:1;color:${BR.mot};letter-spacing:.005em}
.monture .nat{font-style:italic;font-size:19px;color:${BR.nature};margin-top:4px}
.monture .def{font-size:18.5px;line-height:1.3;color:${BR.texte};margin-top:10px;hyphens:auto}
.etiq{position:absolute;left:112px;right:20px;top:284px;height:160px;background:var(--etiq);border:1px solid var(--trait);padding:9px 12px 0 14px;box-shadow:0 3px 8px -5px var(--ombre)}
.etiq::before{content:"";position:absolute;left:0;right:0;top:28px;border-top:.75px solid var(--trait)}
.etiq .ex{font-style:italic;font-size:16.5px;line-height:1.28;margin-top:12px;color:var(--encre)}
.etiq .pied{position:absolute;left:14px;bottom:8px;font-size:19px}
.etiq .epingle{position:absolute;top:-8px;left:50%}
.etiq .tampon{position:absolute;right:-6px;bottom:-12px;transform:rotate(-14deg)}
.brin{position:absolute;left:16px;top:274px;width:110px;height:184px}
.actions{position:absolute;top:654px;left:8px;right:8px;display:grid;grid-template-columns:repeat(4,1fr)}
.act{display:flex;flex-direction:column;align-items:center;gap:3px;height:56px;justify-content:center;font-size:15px;color:var(--encre)}
.cta{position:absolute;left:16px;right:16px;top:722px}
`;
function home(dark) {
  const body = `${statut()}
  ${dark ? '<div class="halo"></div>' : ''}
  <div class="barre"><div class="cpt"><div class="ib">${G.icon('cabinet')}</div><span>18/39</span></div><div class="ib">${G.icon('reglages')}</div></div>
  ${gloseLoupe('glose')}
  <div class="planche p-home">
    <div class="p-tete"><span class="cote">Planche n° 412 · mot du jour</span><div class="ib" style="width:40px;height:40px;margin:-8px -6px -8px 0">${G.icon('epingle', 22)}</div></div>
    <div class="monture">${G.corner('tl')}${G.corner('tr')}${G.corner('bl')}${G.corner('br')}
      <div class="mot">${W.mot}</div><div class="nat">${W.nature}</div><div class="def">${W.def}</div></div>
    <svg class="brin" viewBox="-60 -160 120 200" aria-hidden="true">${G.sprig({ L: 140, rot: -12, x: 2, y: 4 })}</svg>
    <div class="etiq">${G.pin()}
      <div class="cote" style="font-size:12px">Herbier de M.${nb}Glose</div>
      <div class="ex">${q(W.ex)}</div>
      <div class="pied main">cueilli le 6 novembre</div>${G.stamp(20)}</div>
  </div>
  <div class="actions">${[['academie', 'Académie'], ['livre', 'TLFi'], ['lettre', 'Envoyer'], ['hasard', 'Au hasard']].map(([i, l]) => `<div class="act">${G.icon(i)}<span>${l}</span></div>`).join('')}</div>
  <div class="cta"><div class="btn-p">Quiz du soir</div></div>`;
  return page('Herbier, accueil', body, dark ? 'nuit' : '', homeCss);
}

// ---------- 2. Quiz (wrong answer state) ----------
const quizCss = `
.q-tete{position:absolute;top:40px;left:20px;right:20px}
.prog{display:flex;gap:10px;align-items:center;margin-top:10px}
.prog .p{width:14px;height:14px;border-radius:50%;border:1.5px solid var(--sepia)}
.prog .p.cour{border:none;background:radial-gradient(circle at 35% 35%,#fff8 0 18%,transparent 20%),var(--garance);box-shadow:0 0 0 3px var(--papier),0 0 0 4.5px var(--garance)}
.prog span{font-size:16px;color:var(--encre2);margin-left:4px}
.gl-q{position:absolute;top:30px;right:14px;width:108px;height:108px}
.q-titre{position:absolute;top:118px;left:20px;right:20px;font-family:Fell;font-size:23px;line-height:1.15}
.p-quiz{left:16px;right:16px;top:186px;padding:22px 22px 20px}
.p-quiz .cote{font-size:12px}
.p-quiz .def{font-size:20px;line-height:1.32;margin-top:8px}
.tags{position:absolute;top:350px;left:16px;right:16px;display:flex;flex-direction:column;gap:10px}
.tag{height:52px;position:relative;display:flex;align-items:center;padding-left:52px;font-size:20.5px}
.tag svg.f{position:absolute;inset:0;width:100%;height:100%;overflow:visible}
.tag .t{position:relative;display:flex;align-items:center;gap:10px}
.tag .m{font-family:'Courier Prime';font-size:12px;letter-spacing:.1em;position:absolute;right:16px;top:19px}
.note{position:absolute;top:600px;left:16px;right:16px;display:flex;gap:10px;align-items:flex-start}
.note .main{font-size:24px;line-height:1}
.note p{font-size:17.5px;line-height:1.3;color:var(--encre)}
.q-cta{position:absolute;left:16px;right:16px;top:718px}
`;
function tag(word, state) {
  const fill = { juste: 'var(--juste)', faux: 'var(--faux)', '': 'var(--etiq)' }[state];
  const sw = state ? 'var(--encre)' : 'var(--sepia)';
  const mark = state === 'juste' ? '<b style="font-size:22px">✓</b>' : state === 'faux' ? '<b style="font-size:20px">✗</b>' : '';
  const side = state === 'juste' ? '<span class="m">la bonne</span>' : state === 'faux' ? '<span class="m">votre choix</span>' : '';
  return `<div class="tag" style="${state ? '' : 'color:var(--encre)'}"><svg class="f" viewBox="0 0 328 52" preserveAspectRatio="none" aria-hidden="true">
    <path d="M22,1H325a2,2 0 0 1 2,2V49a2,2 0 0 1 -2,2H22L1,34V18Z" fill="${fill}" stroke="${sw}" stroke-width="1.4" vector-effect="non-scaling-stroke"/>
    <circle cx="22" cy="26" r="5.5" fill="var(--papier)" stroke="${sw}" stroke-width="1.2"/><circle cx="22" cy="26" r="8.5" fill="none" stroke="var(--laiton)" stroke-width="1.6"/>
    <path d="M16.5,26c-8,-2 -12,4 -16,2" fill="none" stroke="var(--kraft)" stroke-width="1.6"/></svg>
    <div class="t">${mark}<span>${word}</span></div>${side}</div>`;
}
function quiz() {
  const body = `${statut('21:04')}
  <div class="q-tete"><div class="cote">Quiz du soir</div><div class="prog"><i class="p cour"></i><i class="p"></i><i class="p"></i><i class="p"></i><span>spécimen 1 sur 4</span></div></div>
  ${iconeLoupe('gl-q')}
  <div class="q-titre">À quel mot appartient<br>cette description${nb}?</div>
  <div class="planche p-quiz"><div class="cote">Description</div><div class="def">${W.def}</div></div>
  <div class="tags">${tag(W.mot, 'juste')}${tag('tintouin', 'faux')}${tag('bisaïeul', '')}${tag('regrattier', '')}</div>
  <div class="note"><div><div class="main" style="color:var(--herbier)">Presque${nb}!</div><p style="margin-top:6px">C’était ${q('estramaçon')}, la lourde épée. Il revient dans votre herbier demain soir.</p></div></div>
  <div class="q-cta"><div class="btn-p">Spécimen suivant</div></div>`;
  return page('Herbier, quiz', body, '', quizCss);
}

// ---------- 3. Collection: the cabinet of drawers ----------
const colCss = `
.c-titre{position:absolute;top:36px;left:20px;font-family:Fell;font-size:25px;line-height:1}
.c-sous{position:absolute;top:70px;left:20px}
.gl-c{position:absolute;top:30px;right:22px;width:86px;height:86px;z-index:0}
.patte{position:absolute;top:88px;width:20px;height:12px;border-radius:10px 10px 6px 6px;background:#F6EBDD;border:2.5px solid #3B2A1E;z-index:2}
.patte::after{content:"";position:absolute;left:5px;top:3px;width:1.5px;height:4px;background:#3B2A1E;box-shadow:4px 0 #3B2A1E}
.meuble{z-index:1}
.meuble{position:absolute;left:10px;right:10px;top:104px;bottom:28px;background:var(--bois);border:1.5px solid var(--sepia);border-radius:3px 3px 2px 2px;padding:8px}
.meuble::before{content:"";position:absolute;left:-6px;right:-6px;top:-9px;height:9px;border:1.5px solid var(--sepia);border-bottom:none;background:var(--bois);border-radius:2px 2px 0 0}
.grain{position:absolute;inset:0;width:100%;height:100%;opacity:.5}
.tiroir{position:relative;height:56px;border:1px solid var(--sepia);background:var(--planche);border-radius:2px;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:5px}
.tiroir::before{content:"";position:absolute;inset:3px;border:.6px solid var(--trait)}
.porte{position:relative;min-width:104px;height:24px;padding:0 9px;border:1.3px solid var(--laiton);background:var(--etiq);display:flex;align-items:center;justify-content:center;font-family:'Courier Prime';font-size:12px;letter-spacing:.08em;color:var(--encre);text-transform:uppercase;border-radius:2px}
.porte::before,.porte::after{content:"";position:absolute;top:9px;width:4px;height:4px;border-radius:50%;background:var(--laiton)}.porte::before{left:-8px}.porte::after{right:-8px}
.nb{font-size:15px;color:var(--encre2);line-height:1}
.ouvert{border-color:var(--sepia);background:var(--etiq)}
.plateau{position:relative;margin:0 6px;border:1px solid var(--sepia);border-top:none;background:var(--etiq);padding:8px;display:grid;grid-template-columns:1fr 1fr;gap:8px;box-shadow:inset 0 8px 10px -8px var(--ombre)}
.case{position:relative;height:150px;border:1px solid var(--trait);background:var(--planche);display:flex;flex-direction:column;align-items:center;padding-top:6px}
.case svg.gl{width:96px;height:96px}
.case .lb{position:relative;margin-top:2px;padding:2px 8px;border:.8px solid var(--trait);background:var(--etiq);font-size:15px;line-height:1.15;text-align:center;max-width:140px}
.case.porte-le{border:1.6px solid var(--herbier)}
.case .porte-tag{position:absolute;top:6px;left:6px;font-family:'Courier Prime';font-size:12px;letter-spacing:.08em;color:var(--herbier);text-transform:uppercase}
.case.vide svg.gl{filter:grayscale(1) sepia(.6) contrast(.6) brightness(1.1);opacity:.28}
.case.vide{border-style:dashed;border-color:var(--sepia)}
.case.vide .lb{border-style:dashed}
.case .cond{font-size:14px;color:var(--encre2);margin-top:3px}
.case .epingle{position:absolute;top:-6px;left:50%;margin-left:-7px}
.grille{display:grid;grid-template-columns:1fr 1fr;gap:7px;margin-top:8px}
.pied-m{position:absolute;bottom:-16px;left:10px;right:10px;height:12px;border:1.5px solid var(--sepia);border-top:none;background:var(--bois)}
`;
function collection() {
  const cases = [
    ['glose_assis.svg', '110 70 820 820', 'Sur son volume', 'porte-le'],
    ['glose_soir.svg', null, 'Sieste dans la tasse', ''],
    ['glose_livres.svg', null, 'Sur la pile de livres', ''],
    ['glose_soir.svg', null, 'Réveil dans la tasse', 'vide'],
  ].map(([f, vb, l, c]) => {
    const v = vb || fs.readFileSync(path.join(MASC, f), 'utf8').match(/viewBox="([^"]+)"/)[1];
    return `<div class="case ${c}">${c === 'vide' ? '' : G.pin()}${c === 'porte-le' ? '<span class="porte-tag">porté</span>' : ''}
      ${glose(f, v, 'gl', c === 'vide' ? 'Pose à trouver' : 'Monsieur Glose, pose ' + l)}<div class="lb">${l}</div>${c === 'vide' ? '<div class="cond">à cueillir · encore 9 quiz</div>' : ''}</div>`;
  }).join('');
  const tiroirs = [['Mots du fil', '5 / 8'], ['Pelages', '3 / 6'], ['Pelotes', '3 / 7'], ['Sur la tête', '0 / 5'], ['Au cou', '3 / 4'], ['Montures', '0 / 1'], ['Objets', '0 / 2'], ['Reliures', '1 / 2']]
    .map(([n, c]) => `<div class="tiroir"><div class="porte">${n}</div><div class="nb">${c}</div></div>`).join('');
  let grain = '';
  for (let i = 0; i < 26; i++) { const y = 8 + i * 27 + (i % 3) * 3; grain += `<path class="g-fn" d="M0,${y}C80,${y - 4} 160,${y + 5} 340,${y - 2}"/>`; }
  const body = `${statut()}
  <div class="c-titre">Le cabinet de M.${nb}Glose</div><div class="cote c-sous">18 spécimens sur 39</div>
  ${glose('glose_icone.svg', '240 92 520 520', 'gl-c', 'Monsieur Glose')}<i class="patte" style="left:262px"></i><i class="patte" style="left:306px"></i>
  <div class="meuble"><svg class="grain" preserveAspectRatio="none" viewBox="0 0 340 670" aria-hidden="true">${grain}</svg>
    <div class="tiroir ouvert" style="flex-direction:row;gap:12px"><div class="porte">Poses</div><div class="nb">3 / 4</div></div>
    <div class="plateau">${cases}</div>
    <div class="grille">${tiroirs}</div>
    <div class="pied-m"></div></div>`;
  return page('Herbier, cabinet', body, '', colCss);
}

// ---------- 4. Lock screen card (rendered by WordCard on the word's palette) ----------
const lockCss = `
body{background:linear-gradient(${BR.haut},${BR.bas})}
body::before{opacity:.06}
.heure{position:absolute;top:96px;width:100%;text-align:center;font:200 84px/1 Roboto,'DejaVu Sans',sans-serif;color:#F7EDE4;letter-spacing:-.02em}
.date{position:absolute;top:190px;width:100%;text-align:center;font:400 16px Roboto,'DejaVu Sans',sans-serif;color:#F7EDE4;opacity:.9}
.brin-l{position:absolute;right:6px;top:284px;width:150px;height:170px;opacity:.42}
.brin-l .g-ln,.brin-l .g-fn{stroke:${BR.nature}}
.lab{position:absolute;left:22px;right:22px;top:446px;height:262px;border:1px solid rgba(224,138,94,.55);padding:18px 18px 0}
.lab::before{content:"";position:absolute;inset:3px;border:.5px solid rgba(224,138,94,.4)}
.lab .coin .g-kraft{fill:${BR.nature};opacity:.85}.lab .coin .g-fn{stroke:${BR.haut}}
.lab .ent{display:flex;justify-content:space-between;font-family:'Courier Prime';font-size:12px;letter-spacing:.12em;color:${BR.nature};text-transform:uppercase}
.lab .mot{font-size:48px;line-height:1;color:${BR.mot};margin-top:14px}
.lab .nat{font-style:italic;font-size:19px;color:${BR.nature};margin-top:4px}
.lab .def{font-size:18px;line-height:1.3;color:${BR.texte};margin-top:10px}
.lab .ex{font-style:italic;font-size:16.5px;line-height:1.28;color:${BR.exemple};margin-top:8px}
.raccourcis{position:absolute;bottom:34px;left:30px;right:30px;display:flex;justify-content:space-between}
.raccourcis i{width:44px;height:44px;border-radius:50%;background:rgba(255,255,255,.12)}
.statut{color:#F7EDE4}.geste{background:#F7EDE4}
`;
function lock() {
  const body = `${statut()}<div class="heure">08:12</div><div class="date">vendredi 6 novembre</div>
  <svg class="brin-l" viewBox="-80 -170 160 180" aria-hidden="true">${G.sprig({ L: 150, rot: 38, x: -30, y: 0 })}</svg>
  <div class="lab">${G.corner('tl', 12)}${G.corner('tr', 12)}${G.corner('bl', 12)}${G.corner('br', 12)}
    <div class="ent"><span>n° 412</span><span>cueilli le 6 nov.</span></div>
    <div class="mot">${W.mot}</div><div class="nat">${W.nature}</div><div class="def">${W.def}</div><div class="ex">${q(W.ex)}</div></div>
  <div class="raccourcis"><i></i><i></i></div>`;
  return page('Herbier, verrouillage', body, 'nuit', lockCss);
}

// palette -> engraved specimen (also in style.md)
const PALS = JSON.parse(fs.readFileSync(path.join(__dirname, '../../../app/src/main/assets/styles.json'), 'utf8')).palettes;
const MOTIF = { encre_de_chine: 'noix de galle', bleu_nuit: 'pavot', ardoise: 'lichen', vert_de_gris: 'ammonite', lie_de_vin: 'vrille de vigne', braise: 'chêne roussi', sous_bois: 'fougère', violine: 'aconit', abysses: 'corail', parchemin: 'épi de blé', sepia: 'os de seiche', sanguine: 'églantine', safran: 'crocus', lavande: 'lavande', givre: 'rameau givré', aube: 'coquelicot', pistache: 'pois de senteur', craie: 'nautile' };

// ---------- render ----------
const screens = { home_light: home(false), home_dark: home(true), quiz: quiz(), collection: collection(), lockscreen: lock() };
for (const [n, h] of Object.entries(screens)) fs.writeFileSync(path.join(OUT, n + '.html'), h);

const board = `<!doctype html><html lang="fr"><head><meta charset="utf-8"><title>Herbier des mots</title><style>${css}
body{width:2080px;height:1440px;padding:48px 56px;background:#E9E1D0}
h1{font-family:Fell;font-size:56px;font-weight:400;color:#2A2018}
.sous{font-size:24px;color:#5C4C3B;margin-top:6px;font-style:italic}
.rang{display:flex;gap:40px;margin-top:34px;align-items:flex-start}
.sc{display:flex;flex-direction:column;gap:12px}.sc img{width:360px;height:800px;border-radius:18px;box-shadow:0 12px 30px -12px rgba(60,40,20,.45);border:6px solid #2A2018}
.sc span{font-family:'Courier Prime';font-size:15px;letter-spacing:.12em;text-transform:uppercase;color:#7B5530;text-align:center}
.jet{display:flex;gap:56px;margin-top:30px;align-items:flex-start}
.sw{display:flex;gap:10px}.sw div{width:86px}.sw b{display:block;height:52px;border:1px solid rgba(0,0,0,.18);border-radius:2px}
.sw small{display:block;font-family:'Courier Prime';font-size:12px;color:#2A2018;margin-top:4px;line-height:1.3}
.ty div{color:#2A2018;line-height:1.2}
.pal{display:grid;grid-template-columns:repeat(9,1fr);gap:10px;margin-top:12px}.pal div{height:74px;padding:10px 12px;border-radius:2px;box-shadow:0 0 0 1px rgba(0,0,0,.12)}.pal b{display:block;font-weight:400;font-size:21px;line-height:1.1}.pal small{display:block;font-style:italic;font-size:15.5px;margin-top:4px}
</style></head><body>
<h1>Herbier des mots</h1><div class="sous">Chaque mot est un spécimen cueilli, monté sur sa planche, étiqueté par Monsieur Glose, puis rangé dans le cabinet.</div>
<div class="rang">${[['home_light', 'Accueil · jour'], ['home_dark', 'Accueil · nuit'], ['quiz', 'Quiz · réponse fausse'], ['collection', 'Cabinet'], ['lockscreen', 'Écran verrouillé']].map(([f, l]) => `<div class="sc"><img src="${f}.png"><span>${l}</span></div>`).join('')}</div>
<div class="jet"><div class="sw">${[['#F2ECDF', 'papier'], ['#FBF8F1', 'planche'], ['#2A2018', 'encre'], ['#7B5530', 'sépia'], ['#3D5A36', 'herbier'], ['#9A3B2B', 'garance'], ['#2E4A6E', 'prusse'], ['#D2B88C', 'kraft'], ['#16120E', 'papier nuit'], ['#D4AA6E', 'sépia nuit'], ['#A9C79A', 'herbier nuit']].map(([c, n]) => `<div><b style="background:${c}"></b><small>${n}<br>${c}</small></div>`).join('')}</div>
<div class="ty"><div style="font-size:40px">estramaçon <i style="font-size:24px">n.&nbsp;m.</i></div><div style="font-family:Fell;font-size:26px">Le cabinet de M.${nb}Glose</div><div class="cote" style="font-size:15px">Planche n° 412</div><div class="main" style="font-size:26px">cueilli le 6 novembre</div></div></div>
<div class="cote" style="margin-top:30px;font-size:15px">18 palettes, 18 gravures : la palette du mot teint la monture et choisit le spécimen gravé</div>
<div class="pal">${PALS.map(p => `<div style="background:linear-gradient(${p.fond_haut},${p.fond_bas})"><b style="color:${p.mot}">${p.nom}</b><small style="color:${p.nature}">${MOTIF[p.id]}</small></div>`).join('')}</div>
</body></html>`;
fs.writeFileSync(path.join(OUT, 'board.html'), board);

(async () => {
  const { chromium } = require(process.env.PLAYWRIGHT || 'playwright');
  const b = await chromium.launch();
  for (const n of [...Object.keys(screens), 'board']) {
    const pg = await b.newPage({ viewport: n === 'board' ? { width: 2080, height: 1440 } : { width: 360, height: 800 }, deviceScaleFactor: 2 });
    await pg.goto('file://' + path.join(OUT, n + '.html'));
    await pg.evaluate(() => document.fonts.ready); await pg.waitForTimeout(250);
    await pg.screenshot({ path: path.join(OUT, n + '.png') });
    await pg.close();
  }
  await b.close();
})();
