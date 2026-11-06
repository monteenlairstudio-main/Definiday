// Conversion maison SVG animé (sortie de build.js, variante {all:true}) -> VectorDrawable + AnimatedVectorDrawable (framework, sans AndroidX).
// Module utilisé par make_android.js : convert(svgText, nom, dossier_res, { once }) -> { attr: valeur par défaut } des portes.
// Écrit res/drawable/<nom>_vd.xml (image fixe) et res/drawable/<nom>.xml (animated-vector, cibles inline aapt:attr).
// Pourquoi maison : Svg2Vector (sdk-common, testé) aplatit les groupes, perd les noms et ignore SMIL.
// Couleurs : TOKENS -> ?attr/glose* (pelage, pelote, socle...). Groupes « portes » (accessoires, détails des pelages rares) :
// chaque couleur -> ?attr/glose<Porte>_<hex>, transparente par défaut, posée par le style Glose.Acc.* / Glose.Pelage.*.
// Seul mécanisme utilisé : couleur de chemin en attribut de thème (documenté) ; pas de ?attr sur scaleX de groupe.
// Layers: each accessory piece (LAYERED) is written as its own drawable (<nom>_<piece>, same viewport, its animated ancestors
// replicated with the same names and animators), and the body is cut where a piece sits between two body parts
// (<nom>_vd, <nom>_vd_1...). LAYERS lists them back to front for GloseLayers.kt; identical files are written once.
const fs = require('fs'), path = require('path');
const TOKENS = { '#a4acbc': 'gloseFur', '#788196': 'gloseFurD', '#f8eedd': 'gloseCream', '#9a4250': 'gloseBall', '#5a2230': 'gloseBallD',
  '#e98c8c': 'gloseNose', '#f0a6a2': 'gloseRose', '#18203a': 'gloseCupIn', '#4a5e92': 'gloseCupLit', '#2c3a04': 'gloseCup',
  '#2c3a01': 'gloseBook', '#1f2901': 'gloseBookD', '#2c3a02': 'gloseBookTop', '#1f2902': 'gloseBookTopD', '#9c6b03': 'gloseBookLow', '#7c5203': 'gloseBookLowD',
  '#d9a401': 'gloseSpecs', '#788101': 'gloseStripe' }; // monture des lunettes et rayures : sentinelles du mode {all:true}
const GATES = [[/^noeud$/, 'Noeud'], [/^echarpe(_dos)?$/, 'Echarpe'], [/bonnet$/, 'Bonnet'], [/^plume$/, 'Plume'], [/^marque_page$/, 'Marque'],
  [/^ruban$/, 'Ruban'], [/^paillettes_/, 'Gilt'], [/^etoiles_/, 'Stars'], [/eclats$/, 'Glint'], [/^grelot(_ruban)?$/, 'Grelot'], [/beret$/, 'Beret'],
  [/couronne$/, 'Couronne'], [/^sapin$/, 'Sapin']];
// Gates drawn as separate layers (one drawable per piece and pose), not hidden in every body drawable: the accessories,
// and the glints of the rare pelages (drawn last, so they do not cut the body).
const LAYERED = ['Noeud', 'Echarpe', 'Bonnet', 'Plume', 'Marque', 'Grelot', 'Beret', 'Couronne', 'Sapin', 'Glint'];
const WRITTEN = new Map(), ALIASES = {}, LAYERS = {}, PARTS = {}; // content -> file; key -> file of same content; drawable -> [gate, layer]; VD -> layers
// Writes one file per content: a drawable identical to one already written (ecrit words, families) reuses it.
// A key (name used by the app) that would duplicate a file becomes a resource alias instead.
function put(resDir, n, xml, key) {
  const old = WRITTEN.get(xml);
  if (old) { if (key && old !== n) ALIASES[n] = old; return old; }
  fs.writeFileSync(path.join(resDir, 'drawable', `${n}.xml`), xml); WRITTEN.set(xml, n); return n;
}
const avdXml = (vd, targets) => `<?xml version="1.0" encoding="utf-8"?>\n<animated-vector xmlns:android="http://schemas.android.com/apk/res/android" xmlns:aapt="http://schemas.android.com/aapt"\n    android:drawable="@drawable/${vd}">\n` +
  targets.map(t => `<target android:name="${esc(t.name)}"><aapt:attr name="android:animation">${t.xml}</aapt:attr></target>`).join('\n') + '\n</animated-vector>\n';
const esc = s => s.replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;');

function convert(src, name, resDir, opts = {}) {
const gates = {}; // attr -> valeur (hex ou ?attr/token) quand la porte est ouverte
// ---------- mini-parseur XML (l'entrée est produite par XMLSerializer : bien formée) ----------
function parse(src) {
  const root = { tag: '#root', attrs: {}, kids: [] }, st = [root];
  const re = /<(\/?)([a-zA-Z:]+)((?:\s+[^\s=]+="[^"]*")*)\s*(\/?)>|<!--[\s\S]*?-->|([^<]+)/g; let m;
  while ((m = re.exec(src))) {
    if (m[5] !== undefined || !m[2]) continue;
    if (m[1]) { st.pop(); continue; }
    const attrs = {}; m[3].replace(/([^\s=]+)="([^"]*)"/g, (_, k, v) => { attrs[k] = v.replace(/&quot;/g, '"').replace(/&amp;/g, '&'); });
    const el = { tag: m[2], attrs, kids: [] }; st[st.length - 1].kids.push(el); if (!m[4]) st.push(el);
  }
  return root.kids[0];
}
const svg = parse(src);
const [vx, vy, vw, vh] = svg.attrs.viewBox.split(/[\s,]+/).map(Number);
const defs = {}; (function walk(e) { if (e.attrs.id) defs[e.attrs.id] = e; e.kids.forEach(walk); })(svg);

// ---------- formes -> pathData ----------
const f = n => +(+n).toFixed(1);
function shapeData(e) {
  const a = e.attrs;
  if (e.tag === 'path') return a.d;
  if (e.tag === 'circle' || e.tag === 'ellipse') {
    const cx = +a.cx || 0, cy = +a.cy || 0, rx = +(a.rx || a.r), ry = +(a.ry || a.r);
    return `M${f(cx - rx)},${f(cy)}a${f(rx)},${f(ry)} 0 1,0 ${f(2 * rx)},0a${f(rx)},${f(ry)} 0 1,0 ${f(-2 * rx)},0z`;
  }
  if (e.tag === 'rect') { const x = +a.x || 0, y = +a.y || 0, w = +a.width, h = +a.height; return `M${x},${y}h${w}v${h}h${-w}z`; }
  return null;
}
function color(c, gate) { // -> valeur d'attribut | null
  if (!c || c === 'none') return null;
  const hex = c.toLowerCase(), v = TOKENS[hex] ? `?attr/${TOKENS[hex]}` : hex;
  if (!gate) return v;
  const attr = `glose${gate}_${hex.slice(1)}`; gates[attr] = v; return `?attr/${attr}`;
}
function gradientXml(id, attr) {
  const g = defs[id]; if (!g) return '';
  const stops = g.kids.filter(k => k.tag === 'stop').map(s => {
    const st = s.attrs['stop-color'] || '#000', op = s.attrs['stop-opacity'] != null ? +s.attrs['stop-opacity'] : 1;
    let hex = st.replace('#', ''), o2 = op;
    const rg = /rgba?\(([^)]+)\)/.exec(st);
    if (rg) { const v = rg[1].split(',').map(Number); hex = v.slice(0, 3).map(x => Math.round(x).toString(16).padStart(2, '0')).join(''); if (v.length > 3) o2 *= v[3]; }
    const aa = Math.round(o2 * 255).toString(16).padStart(2, '0');
    return `<item android:offset="${f(parseFloat(s.attrs.offset) / (String(s.attrs.offset).includes('%') ? 100 : 1))}" android:color="#${aa}${hex}"/>`;
  }).join('');
  const a = g.attrs, lin = g.tag === 'linearGradient';
  const geo = lin ? `android:type="linear" android:startX="${a.x1}" android:startY="${a.y1}" android:endX="${a.x2}" android:endY="${a.y2}"`
    : `android:type="radial" android:centerX="${a.cx}" android:centerY="${a.cy}" android:gradientRadius="${a.r}"`;
  return `<aapt:attr name="${attr}"><gradient ${geo}>${stops}</gradient></aapt:attr>`;
}

// ---------- animations ----------
let targets = [], cur = ''; // { name, anims: [objectAnimator xml] } ; layer being emitted
let uid = 0;
const splineInterp = '@interpolator/glose_ease';
function keyframes(prop, vals, kts, type = 'floatType') {
  return `<propertyValuesHolder android:propertyName="${prop}" android:valueType="${type}">` + vals.map((v, i) =>
    `<keyframe android:fraction="${kts[i]}" android:value="${f(v)}"${i ? ` android:interpolator="${splineInterp}"` : ''}/>`).join('') + '</propertyValuesHolder>';
}
// opts.once = [keyTimes, coupure] : les animateurs de ces keyTimes (le fil qui écrit) jouent une fois jusqu'à la coupure (mot écrit, sans rembobinage)
const ONCE = an => opts.once && an.keyTimes === opts.once[0];
function animator(an, holders) {
  const dur = Math.round(parseFloat(an.dur) * 1000 * (ONCE(an) ? opts.once[1] : 1)), begin = parseFloat(an.begin || '0') * 1000;
  const offset = begin < 0 ? Math.round(dur + begin) % dur : Math.round(begin); // phase négative ≈ départ retardé (approximation)
  return `<objectAnimator android:duration="${dur}" android:repeatCount="${ONCE(an) ? 0 : 'infinite'}" android:startOffset="${offset}">${holders}</objectAnimator>`;
}
function kts(an, n) { return an.keyTimes ? an.keyTimes.split(';').map(Number) : Array.from({ length: n }, (_, i) => i / (n - 1)); }
function keyframesOf(an, prop, vals) { // keyframes, tronqués et étirés pour une animation « une fois »
  let k = kts(an, vals.length);
  if (ONCE(an)) { const c = opts.once[1]; vals = vals.filter((_, i) => k[i] <= c); k = k.filter(t => t <= c).map(t => +(t / c).toFixed(4)); }
  return keyframes(prop, vals, k);
}

// ---------- émission ----------
const INH = ['fill', 'stroke', 'stroke-width', 'stroke-linecap', 'stroke-linejoin', 'fill-rule', 'stroke-dasharray', 'stroke-dashoffset'];
function emit(e, alpha, gate, out, inh = {}) {
  const a = Object.assign({}, inh, e.attrs); delete a.opacity; if (e.attrs.opacity != null) a.opacity = e.attrs.opacity;
  const inhKids = {}; INH.forEach(k => { if (a[k] != null) inhKids[k] = a[k]; });
  if (e.tag === 'g' || e.tag === 'svg') {
    const op = a.opacity != null ? +a.opacity : 1;
    const anim = e.kids.find(k => k.tag === 'animateTransform');
    const nm = a.id || e.nm || null;
    let attrs = nm ? ` android:name="${esc(nm)}"` : '';
    let t = a.transform || '';
    const tr = /translate\(([-\d.e]+)[ ,]*([-\d.e]*)\)/.exec(t), sc = /scale\(([-\d.e]+)[ ,]*([-\d.e]*)\)/.exec(t);
    if (tr) attrs += ` android:translateX="${f(tr[1])}" android:translateY="${f(tr[2] || 0)}"`;
    if (sc) attrs += ` android:scaleX="${sc[1]}" android:scaleY="${sc[2] || sc[1]}"`;
    const pv = a['data-paper-data'] && JSON.parse(a['data-paper-data']).pivot; // pivot des réactions one-shot (groupe « tete »)
    if (pv) attrs += ` android:pivotX="${f(pv[0])}" android:pivotY="${f(pv[1])}"`;
    const g = a.id && GATES.find(([re]) => re.test(a.id));
    const holders = [];
    if (anim) {
      const t2 = anim.attrs.type, vals = anim.attrs.values.split(';').map(v => v.trim().split(/[\s,]+/).map(Number)), kf = (p, v) => keyframesOf(anim.attrs, p, v);
      if (t2 === 'rotate') { attrs += ` android:pivotX="${vals[0][1]}" android:pivotY="${vals[0][2]}"`; holders.push(kf('rotation', vals.map(v => v[0]))); }
      if (t2 === 'translate') { holders.push(kf('translateX', vals.map(v => v[0])), kf('translateY', vals.map(v => v[1] || 0))); }
      if (t2 === 'scale') { holders.push(kf('scaleX', vals.map(v => v[0])), kf('scaleY', vals.map(v => v[1] != null ? v[1] : v[0]))); }
    }
    let clip = '';
    const cp = /url\(#([^)]+)\)/.exec(a['clip-path'] || '');
    if (cp && defs[cp[1]]) clip = defs[cp[1]].kids.map(shapeData).filter(Boolean).map(d => `<clip-path android:pathData="${d}"/>`).join('');
    const kids = [];
    for (const k of e.kids) if (k.tag !== 'animateTransform' && k.tag !== 'animate' && k.tag !== 'title' && k.tag !== 'defs' && k.tag !== 'clipPath') emit(k, alpha * op, g ? g[1] : gate, kids, inhKids);
    if (!kids.length) return; // nothing of this layer inside
    if (anim) targets.push({ name: nm, xml: animator(anim.attrs, holders.join('')) });
    // un groupe sans nom, sans transformation ni découpe est inutile en VectorDrawable : on l'aplatit
    if (!nm && !tr && !sc && !clip) { out.push(...kids); return; }
    out.push(`<group${attrs}>${clip}${kids.join('')}</group>`);
    return;
  }
  const d = shapeData(e); if (!d || e.layer !== cur) return;
  const op = (a.opacity != null ? +a.opacity : 1) * alpha;
  const nm = a.id || e.nm || null;
  let s = `<path${nm ? ` android:name="${esc(nm)}"` : ''} android:pathData="${d}"`, inner = '';
  const fillV = a.fill === undefined ? '#000000' : a.fill;
  const gm = /url\(#([^)]+)\)/.exec(fillV || '');
  if (gm && gate) throw new Error(`${name}: dégradé dans la porte ${gate}`);
  if (gm) inner += gradientXml(gm[1], 'android:fillColor');
  else { const c = color(fillV, gate); if (c) s += ` android:fillColor="${c}"`; }
  const fo = (a['fill-opacity'] != null ? +a['fill-opacity'] : 1) * op;
  if ((gm || fillV !== 'none') && fo < 1) s += ` android:fillAlpha="${f(fo * 100) / 100}"`;
  if (a.stroke && a.stroke !== 'none') {
    s += ` android:strokeColor="${color(a.stroke, gate)}" android:strokeWidth="${a['stroke-width'] || 1}"`;
    if (a['stroke-linecap']) s += ` android:strokeLineCap="${a['stroke-linecap']}"`;
    if (a['stroke-linejoin']) s += ` android:strokeLineJoin="${a['stroke-linejoin']}"`;
    const so = (a['stroke-opacity'] != null ? +a['stroke-opacity'] : 1) * op;
    if (so < 1) s += ` android:strokeAlpha="${f(so * 100) / 100}"`;
  }
  if (a['fill-rule'] === 'evenodd') s += ' android:fillType="evenOdd"';
  const anims = e.kids.filter(k => k.tag === 'animate');
  for (const an of anims) {
    const vals = an.attrs.values.split(';').map(Number);
    const tname = nm;
    if (an.attrs.attributeName === 'stroke-dashoffset') { // fil qui s'écrit : dashoffset L..0 -> trimPathEnd 0..1
      const L = parseFloat((a['stroke-dasharray'] || '1').split(/[ ,]+/)[0]);
      targets.push({ name: tname, xml: animator(an.attrs, keyframesOf(an.attrs, 'trimPathEnd', vals.map(v => 1 - v / L))) });
    } else if (an.attrs.attributeName === 'opacity') {
      const prop = a.fill && a.fill !== 'none' ? 'fillAlpha' : 'strokeAlpha';
      targets.push({ name: tname, xml: animator(an.attrs, keyframesOf(an.attrs, prop, vals.map(v => v * op))) });
    }
  }
  out.push(inner ? `${s}>${inner}</path>` : `${s}/>`);
}
// les <animate> d'opacité posés sur un groupe (z, vapeur, halo) sont redescendus sur ses chemins
(function push(e) { for (const k of e.kids) push(k);
  if (e.tag === 'g') { const an = e.kids.filter(k => k.tag === 'animate'); if (an.length) { e.kids = e.kids.filter(k => k.tag !== 'animate'); (function down(x) { for (const k of x.kids) { if (k.tag === 'g') down(k); else if (shapeData(k)) k.kids.push(...an); } })(e); } } })(svg);

// Layers, in drawing order: each accessory piece (LAYERED gate group) is its own layer, and the body is cut wherever a piece
// is drawn between two body parts. opts.noSlots (head icons, which never wear them): pieces dropped.
// Anonymous animated nodes are named here, so that a group shared by several layers (the head) has the same name in each.
const KID = k => !['animateTransform', 'animate', 'title', 'defs', 'clipPath'].includes(k.tag);
const order = []; let segs = 0, open = true;
(function mark(e, piece) {
  if (e.tag === 'g' || e.tag === 'svg') {
    if (!e.attrs.id && e.kids.some(k => k.tag === 'animateTransform')) e.nm = `a${uid++}`;
    const g = !piece && e.attrs.id && GATES.find(([re]) => re.test(e.attrs.id));
    const p = piece || (g && LAYERED.includes(g[1]) ? e.attrs.id : null);
    if (p && !piece && !opts.noSlots) { order.push([g[1], `_${p}`]); open = true; }
    e.kids.filter(KID).forEach(k => mark(k, p)); return;
  }
  if (!shapeData(e)) return;
  if (!e.attrs.id && e.kids.some(k => k.tag === 'animate')) e.nm = `p${uid++}`;
  if (piece) { e.layer = opts.noSlots ? null : `_${piece}`; return; }
  if (open) { order.push(['', segs ? `_${segs}` : '']); segs++; open = false; }
  e.layer = segs > 1 ? `_${segs - 1}` : '';
})(svg);
const W = 256, base = opts.vd || `${name}_vd`, layers = [];
fs.mkdirSync(path.join(resDir, 'drawable'), { recursive: true });
let size = 0;
for (const [gate, key] of order) {
  targets = []; cur = key;
  const body = []; emit(svg, 1, null, body);
  const vd = `<?xml version="1.0" encoding="utf-8"?>\n<vector xmlns:android="http://schemas.android.com/apk/res/android" xmlns:aapt="http://schemas.android.com/aapt"\n    android:width="${W}dp" android:height="${W}dp" android:viewportWidth="${vw}" android:viewportHeight="${vh}">\n<group android:name="cadre" android:translateX="${-vx}" android:translateY="${-vy}">${body.join('\n')}</group>\n</vector>\n`;
  const vdName = put(resDir, base + key, vd, !key), avd = targets.length || !key ? put(resDir, name + key, avdXml(vdName, targets), !key) : null;
  layers.push({ gate, key, vd: vdName, xml: vd, res: targets.length ? avd : vdName, names: new Set([...vd.matchAll(/android:name="([^"]+)"/g)].map(m => m[1])) });
  size += vd.length;
}
if (layers.length > 1) { LAYERS[name] = layers.map(l => [l.gate, l.res]); if (!opts.vd) LAYERS[base] = layers.map(l => [l.gate, l.vd]); }
if (!opts.vd) PARTS[base] = layers;
console.log(name, `${layers.length} calques`, opts.vd ? '' : `VD ${(size / 1024).toFixed(0)} Ko`);
return gates;
}
// Animators-only AVD [name] on the already converted drawable [vdName] (reactions): targets = { node name: objectAnimator xml }.
// One layer per layer of the drawable, animated where its targets are, so that worn accessories follow the head.
function animated(name, vdName, targets, resDir) {
  const layers = PARTS[vdName].map(l => {
    const t = Object.keys(targets).filter(n => l.names.has(n)).map(n => ({ name: n, xml: targets[n] }));
    const avd = t.length || !l.key ? put(resDir, name + l.key, avdXml(l.vd, t), !l.key) : null; // the key is always written
    return [l.gate, t.length ? avd : l.vd];
  });
  if (layers.length > 1) LAYERS[name] = layers;
}
module.exports = { convert, animated, TOKENS, LAYERED, LAYERS, ALIASES, PARTS };
