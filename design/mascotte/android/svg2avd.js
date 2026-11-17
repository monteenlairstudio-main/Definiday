// Conversion maison SVG animé (sortie de build.js, variante {all:true}) -> VectorDrawable + AnimatedVectorDrawable (framework, sans AndroidX).
// Module utilisé par make_android.js : convert(svgText, nom, dossier_res, { once }) -> { attr: valeur par défaut } des portes.
// Écrit res/drawable/<nom>_vd.xml (image fixe) et res/drawable/<nom>.xml (animated-vector, chaque animateur dans animator/glose_k_*).
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
  '#d9a401': 'gloseSpecs', '#788101': 'gloseStripe', '#f0e801': 'gloseHalo' }; // monture, rayures, liseré de nuit : sentinelles du mode {all:true}
const GATES = [[/^noeud$/, 'Noeud'], [/^echarpe(_dos)?$/, 'Echarpe'], [/bonnet$/, 'Bonnet'], [/^plume$/, 'Plume'], [/^marque_page$/, 'Marque'],
  [/^ruban$/, 'Ruban'], [/^paillettes_/, 'Gilt'], [/^etoiles_/, 'Stars'], [/eclats$/, 'Glint'], [/^grelot(_ruban)?$/, 'Grelot'], [/beret$/, 'Beret'],
  [/couronne$/, 'Couronne'], [/^sapin$/, 'Sapin'], [/papier$/, 'Papier'], [/^lavalliere$/, 'Lavalliere'],
  [/^croissant$/, 'Croissant'], [/^bandeau$/, 'Bandeau']];
// Gates drawn as separate layers (one drawable per piece and pose), not hidden in every body drawable: the accessories,
// and the glints of the rare pelages (drawn last, so they do not cut the body).
const LAYERED = ['Noeud', 'Echarpe', 'Bonnet', 'Plume', 'Marque', 'Grelot', 'Beret', 'Couronne', 'Sapin', 'Papier', 'Lavalliere', 'Croissant', 'Bandeau', 'Glint'];
const WRITTEN = new Map(), ALIASES = {}, LAYERS = {}, PARTS = {}; // content -> file; key -> file of same content; drawable -> [gate, layer]; VD -> layers
const IDLE0 = {}; // drawable -> target -> property -> { start: value on the first frame of its life (reactions end there), still: in the VD, low: min }
// Writes one file per content: a drawable identical to one already written (ecrit words, families) reuses it.
// A key (name used by the app) that would duplicate a file becomes a resource alias instead.
function put(resDir, n, xml, key) {
  const old = WRITTEN.get(xml);
  if (old) { if (key && old !== n) ALIASES[n] = old; return old; }
  fs.writeFileSync(path.join(resDir, 'drawable', `${n}.xml`), xml); WRITTEN.set(xml, n); return n;
}
const avdXml = (vd, targets) => `<?xml version="1.0" encoding="utf-8"?>\n<animated-vector xmlns:android="http://schemas.android.com/apk/res/android"\n    android:drawable="@drawable/${vd}">\n` +
  targets.map(t => `<target android:name="${esc(t.name)}" android:animation="@animator/${t.res}"/>`).join('\n') + '\n</animated-vector>\n';
// Each distinct animator (idle life and reactions) is written once, res/animator/glose_k_<hash>.xml, and shared by every layer and pose
// that plays it (a part and its night outline, the head copies under each hat, the ecrit words…): inline (aapt:attr), each copy was a
// resource file of its own in the APK (279 for 41 distinct idle animators).
function shared(resDir, xml) {
  const n = 'glose_k_' + require('crypto').createHash('sha1').update(xml).digest('hex').slice(0, 8);
  fs.mkdirSync(path.join(resDir, 'animator'), { recursive: true });
  fs.writeFileSync(path.join(resDir, 'animator', `${n}.xml`), '<?xml version="1.0" encoding="utf-8"?>\n' +
    xml.replace(/^<(\w+)/, '<$1 xmlns:android="http://schemas.android.com/apk/res/android"') + '\n');
  return n;
}
const IDLE_MS = 26000; // Glose.IDLE_MS
// Easing of each keyframe segment (index i = the segment ending at key i): monotone cubic Hermite (Fritsch–Carlson, as PCHIP). The speed
// is continuous through every key the motion passes (equal on both sides), zero where it rests: extremum, hold, first and last key (a
// loop starts from and comes back to its still without a jump in speed). cyclic: the loop is rotated by its phase, its first (= last)
// key is a pass-through when the motion goes through it. A segment of end tangents a, b (in units of its mean slope) is exactly the
// Bézier pathInterpolator (1/3, a/3, 2/3, 1 − b/3): a, b are quantized on Q, interpolator/glose_h_<a>_<b> (make_android.js writes
// HERMITE). Rest to rest keeps glose_ease; (2, 0) and (0, 2) are decelerate_quad and accelerate_quad, (1, 1) linear.
// force[i] ('out' | 'in', reactions): a snap out of the rest that starts segment i (a = 2), or an impact on the rest that ends it (b = 2);
// ignored where the key is a pass-through. (glose_ease on every key stopped the motion at each pass: a swing halted at 0°.)
const Q = [0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 1, 1.25, 1.5, 2, 2.5, 3], HERMITE = new Set();
const EASE = '@interpolator/glose_ease', DECEL = '@android:interpolator/decelerate_quad', ACCEL = '@android:interpolator/accelerate_quad';
function hermite(vals, kts, cyclic = false, force = []) {
  const n = vals.length, h = [], d = [];
  for (let i = 0; i < n - 1; i++) { h.push(kts[i + 1] - kts[i]); d.push((vals[i + 1] - vals[i]) / h[i]); }
  const tan = (d0, d1, h0, h1) => d0 * d1 <= 0 ? 0 : 3 * (h0 + h1) / ((2 * h1 + h0) / d0 + (h1 + 2 * h0) / d1); // 0 at an extremum or hold
  const m = vals.map((_, i) => i > 0 && i < n - 1 ? tan(d[i - 1], d[i], h[i - 1], h[i]) : cyclic ? tan(d[n - 2], d[0], h[n - 2], h[0]) : 0);
  const q = x => Q.reduce((p, c) => Math.abs(c - x) < Math.abs(p - x) ? c : p);
  const ab = vals.map((_, i) => {
    if (!i || !d[i - 1]) return null; // a hold: nothing moves
    let a = m[i - 1] / d[i - 1], b = m[i] / d[i - 1];
    if (force[i] === 'out' && !m[i - 1]) a = 2;
    if (force[i] === 'in' && !m[i]) b = 2;
    return [q(Math.min(3, a)), q(Math.min(3, b))];
  });
  const rest = m.map(x => !x); // keys where the motion rests (speed 0): a life track may be cut there (lifeXml)
  for (let i = 1; i < n - 1; i++) if (m[i] && ab[i] && ab[i + 1]) { // speed in / out at a pass-through key, after quantization
    const r = (ab[i + 1][0] * d[i]) / (ab[i][1] * d[i - 1]);
    if (!(r >= 0.5 && r <= 2)) throw new Error(`hermite: speed ratio ${r} at key ${i} of ${vals}`);
  }
  return Object.defineProperty(ab.map(x => {
    if (!x) return null;
    const [a, b] = x;
    if (!a && !b) return EASE;
    if (a === 2 && !b) return DECEL;
    if (!a && b === 2) return ACCEL;
    if (a === 1 && b === 1) return null;
    const nm = `glose_h_${a * 1000}_${b * 1000}`; HERMITE.add(nm); return `@interpolator/${nm}`;
  }), 'rest', { value: rest });
}
// A life track (vie.js: one property over the whole idle life, from its still back to it) as short animators: cut only where every
// property is on its still value, and left out where it rests there. Each animator starts at its first move (startOffset), lasts at most
// MAXC where a cut is possible (one RenderThread sample per 16 ms frame, 300 at most per animator) and nothing is written in the rests.
// Every animator thus starts and ends on the still: whatever order a runtime applies their start values in (RenderThread at play time 0,
// reverse order of end; UI-thread AnimatorSet at start), it shows the still, and the cut changes nothing in the motion (the eases are the
// whole track's Hermite segments, so the speed is the same on both sides of a cut). keys = [[property, values, fractions of the life]],
// force = accents per key ('out' | 'in').
const MAXC = 4800;
function lifeXml(keys, force, at) {
  keys = keys.filter(([, v]) => v.some(x => x !== v[0])); // a property that does not move is left out (its still value stays)
  if (!keys.length) return null;
  const k = keys[0][2], T = k.map(x => Math.round(x * IDLE_MS)), n = k.length, E = keys.map(([, v]) => hermite(v, k, false, force));
  const still = i => keys.every(([, v]) => v[i] === v[0]), rests = i => still(i) && still(i + 1);
  for (let i = 0; i < n - 1; i++) if (keys.some(([, v]) => v[i] !== v[i + 1]) && T[i + 1] - T[i] < 33) throw new Error(`${at}: life key ${T[i]} -> ${T[i + 1]} ms, under 2 frames`);
  const chunks = []; let i0 = null;
  for (let i = 0; i < n - 1; i++) {
    if (rests(i)) { if (i0 !== null) chunks.push([i0, i]); i0 = null; continue; }
    if (i0 === null) i0 = i;
    if (i + 1 < n - 1 && still(i + 1) && !rests(i + 1)) { // a cut is possible after key i + 1: take it if the next one would be too far
      let j = i + 2; while (j < n - 1 && !still(j)) j++;
      if (T[j] - T[i0] > MAXC) { chunks.push([i0, i + 1]); i0 = null; }
    }
  }
  if (i0 !== null) chunks.push([i0, n - 1]);
  const xml = chunks.map(([a, b]) => { const d = T[b] - T[a];
    return `<objectAnimator android:duration="${d}" android:interpolator="@android:interpolator/linear" android:startOffset="${T[a]}">` +
      keys.map(([p, v], j) => `<propertyValuesHolder android:propertyName="${p}" android:valueType="floatType">` + v.slice(a, b + 1).map((x, i) =>
        `<keyframe android:fraction="${+((T[a + i] - T[a]) / d).toFixed(4)}" android:value="${x}"${i && E[j][a + i] ? ` android:interpolator="${E[j][a + i]}"` : ''}/>`).join('') + '</propertyValuesHolder>').join('') + '</objectAnimator>'; });
  return { xml: xml.length > 1 ? `<set>${xml.join('')}</set>` : xml[0], chunks: chunks.map(([a, b]) => [T[a], T[b]]) };
}
const hermiteXml = nm => { const [a, b] = nm.split('_').slice(2).map(x => x / 1000);
  return `<?xml version="1.0" encoding="utf-8"?>\n<pathInterpolator xmlns:android="http://schemas.android.com/apk/res/android" android:controlX1="0.3333" android:controlY1="${+(a / 3).toFixed(4)}" android:controlX2="0.6667" android:controlY2="${+(1 - b / 3).toFixed(4)}"/>\n`; };
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
// values at 3 decimals: at 1 decimal the breathing (scale 1.015) was written 1 -> 1 and the flame's 0.94 0.9
let pend = []; // keys of the animator being written: [property, values, fractions]
function keyframes(prop, vals, kts, type = 'floatType', linear = false, cyclic = false) {
  const ease = linear ? vals.map(() => null) : hermite(vals, kts, cyclic);
  pend.push([prop, vals.map(v => +(+v).toFixed(3)), kts]);
  return `<propertyValuesHolder android:propertyName="${prop}" android:valueType="${type}">` + vals.map((v, i) =>
    `<keyframe android:fraction="${kts[i]}" android:value="${+(+v).toFixed(3)}"${i && ease[i] ? ` android:interpolator="${ease[i]}"` : ''}/>`).join('') + '</propertyValuesHolder>';
}
// opts.once = [keyTimes, from, ms]: the animators of these keyTimes (the thread writing) play once, from their key « from » (the still:
// word written) to the end (written again), their first segment (the rewind) squeezed to ms. The quiz end then starts on the still frame
// (no jump of the ball, the head or the word when it appears or replays), quickly rewinds and writes the word.
const ONCE = an => opts.once && an.keyTimes === opts.once[0];
function onceTimes(an) { // [kept key indices, their new fractions, duration ms]
  const k = kts(an), full = parseFloat(an.dur) * 1000, [, c0, ms] = opts.once, i0 = k.indexOf(c0), c1 = k[i0 + 1], dur = ms + (1 - c1) * full;
  if (i0 < 0) throw new Error(`${name}: once from ${c0} is not a key`);
  return [k.map((_, i) => i).slice(i0), k.slice(i0).map(t => t === c0 ? 0 : +((ms + (t - c1) * full) / dur).toFixed(4)), Math.round(dur)];
}
function animator(an, holders) {
  const dur = ONCE(an) ? onceTimes(an)[2] : Math.round(parseFloat(an.dur) * 1000), begin = parseFloat(an.begin || '0') * 1000;
  const offset = begin < 0 ? 0 : Math.round(begin); // negative begin: keys rotated by the phase (keyframesOf)
  // Whole loops only, as many as fit in the idle life: each part comes to rest on its own loop end (= its still frame) instead of
  // being cut mid-cycle by Glose.play()'s stop() (end() jumps to the last key). A loop longer than the idle life plays once.
  // linear: without it every objectAnimator gets AccelerateDecelerate over its whole timeline, on top of the keyframe easing.
  const n = ONCE(an) ? 1 : Math.max(1, Math.floor((IDLE_MS - offset) / dur));
  return [`<objectAnimator android:duration="${dur}" android:interpolator="@android:interpolator/linear" android:repeatCount="${n - 1}" android:startOffset="${offset}">${holders}</objectAnimator>`, { dur, offset, n }];
}
function target(nm, an, holders, keys = pend) {
  if (an['data-life'] != null) { const L = lifeXml(keys, an['data-life'].split(';').map(x => x || undefined), `${name} ${nm}`);
    if (L) targets.push({ name: nm, xml: L.xml, keys, life: L.chunks, dur: IDLE_MS, offset: 0, n: 1 }); pend = []; return; }
  const [xml, t] = animator(an, holders); targets.push({ name: nm, xml, keys, ...t }); pend = []; }
// SMIL starts a loop of negative begin mid-cycle. On Android it was a delayed start, and a delayed RenderThread animator writes nothing
// before its delay: the still values showed, then jumped (a « z » at full opacity vanished at 1.2 s). The keys are rotated instead, the
// loop starts at that phase p. A value that jumps at the seam (a rising « z ») keeps its jump, 0.001 long, hidden there as in the loop.
function phase(vals, k, p) {
  const at = t => { const i = k.findIndex(x => x >= t); return k[i] === t ? vals[i] : vals[i - 1] + (vals[i] - vals[i - 1]) * (t - k[i - 1]) / (k[i] - k[i - 1]); };
  const V = [at(p)], K = [0], n = vals.length;
  k.forEach((t, i) => { if (t > p) { V.push(vals[i]); K.push(t - p); } });
  if (vals[n - 1] !== vals[0]) { V.push(vals[0]); K.push(1 - p + 0.001); }
  k.forEach((t, i) => { if (t > 0 && t < p) { V.push(vals[i]); K.push(t + 1 - p); } });
  V.push(V[0]); K.push(1);
  return [V, K.map(x => +x.toFixed(4))];
}
function kts(an, n) { return an.keyTimes ? an.keyTimes.split(';').map(Number) : Array.from({ length: n }, (_, i) => i / (n - 1)); }
function keyframesOf(an, prop, vals) { // keyframes, réduits et étirés pour une animation « une fois »
  let k = kts(an, vals.length);
  if (an['data-life'] != null) { pend.push([prop, vals.map(v => +(+v).toFixed(3)), k]); return ''; } // a life track: lifeXml() in target()
  if (ONCE(an)) { const [ix, t] = onceTimes(an); vals = ix.map(i => vals[i]); k = t; }
  const p = parseFloat(an.begin || '0') < 0 ? (-parseFloat(an.begin) / parseFloat(an.dur)) % 1 : 0;
  if (p) [vals, k] = phase(vals, k, p);
  return keyframes(prop, vals, k, 'floatType', an.calcMode === 'linear', p > 0);
}

// ---------- émission ----------
const INH = ['fill', 'stroke', 'stroke-width', 'stroke-linecap', 'stroke-linejoin', 'fill-rule', 'stroke-dasharray', 'stroke-dashoffset'];
function emit(e, alpha, gate, out, inh = {}) {
  const a = Object.assign({}, inh, e.attrs); delete a.opacity; if (e.attrs.opacity != null) a.opacity = e.attrs.opacity;
  const inhKids = {}; INH.forEach(k => { if (a[k] != null) inhKids[k] = a[k]; });
  if (e.tag === 'g' || e.tag === 'svg') {
    const op = a.opacity != null ? +a.opacity : 1;
    const anim0 = e.kids.find(k => k.tag === 'animateTransform');
    const anim = anim0 && anim0.attrs['data-fin'] && !opts.once ? null : anim0; // played by the quiz end only (*_fin): same drawing, no animator
    const nm = a.id || e.nm || null;
    let attrs = nm ? ` android:name="${esc(nm)}"` : '';
    let t = a.transform || '';
    const tr = /translate\(([-\d.e]+)[ ,]*([-\d.e]*)\)/.exec(t), sc = /scale\(([-\d.e]+)[ ,]*([-\d.e]*)\)/.exec(t);
    if (tr) attrs += ` android:translateX="${f(tr[1])}" android:translateY="${f(tr[2] || 0)}"`;
    if (sc) attrs += ` android:scaleX="${sc[1]}" android:scaleY="${sc[2] || sc[1]}"`;
    if (anim0 && !anim && anim0.attrs.type === 'rotate') { const v = anim0.attrs.values.split(';')[0].trim().split(/[\s,]+/); attrs += ` android:pivotX="${v[1]}" android:pivotY="${v[2]}"`; }
    const pv = a['data-paper-data'] && JSON.parse(a['data-paper-data']).pivot; // pivot des réactions one-shot (groupe « tete »)
    if (pv) attrs += ` android:pivotX="${f(pv[0])}" android:pivotY="${f(pv[1])}"`;
    const g = a.id && GATES.find(([re]) => re.test(a.id));
    const holders = [];
    let keys = []; // this group's keys (its kids, emitted next, write their own)
    if (anim) { pend = [];
      const t2 = anim.attrs.type, vals = anim.attrs.values.split(';').map(v => v.trim().split(/[\s,]+/).map(Number)), kf = (p, v) => keyframesOf(anim.attrs, p, v);
      if (t2 === 'rotate') { attrs += ` android:pivotX="${vals[0][1]}" android:pivotY="${vals[0][2]}"`; holders.push(kf('rotation', vals.map(v => v[0]))); }
      if (t2 === 'translate') { holders.push(kf('translateX', vals.map(v => v[0])), kf('translateY', vals.map(v => v[1] || 0))); }
      if (t2 === 'scale') { holders.push(kf('scaleX', vals.map(v => v[0])), kf('scaleY', vals.map(v => v[1] != null ? v[1] : v[0]))); }
      keys = pend; pend = [];
    }
    let clip = '';
    const cp = /url\(#([^)]+)\)/.exec(a['clip-path'] || '');
    if (cp && defs[cp[1]]) clip = defs[cp[1]].kids.map(shapeData).filter(Boolean).map(d => `<clip-path android:pathData="${d}"/>`).join('');
    if (cp && !clip) return; // empty clip: nothing of this group may show (dropping the clip would show all of it)
    const kids = [];
    for (const k of e.kids) if (k.tag !== 'animateTransform' && k.tag !== 'animate' && k.tag !== 'title' && k.tag !== 'defs' && k.tag !== 'clipPath') emit(k, alpha * op, g ? g[1] : gate, kids, inhKids);
    if (!kids.length) return; // nothing of this layer inside
    if (anim) target(nm, anim.attrs, holders.join(''), keys);
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
  for (const an of anims) { pend = [];
    const vals = an.attrs.values.split(';').map(Number);
    const tname = nm;
    if (an.attrs.attributeName === 'stroke-dashoffset') { // fil qui s'écrit : dashoffset L..0 -> trimPathEnd 0..1
      const L = parseFloat((a['stroke-dasharray'] || '1').split(/[ ,]+/)[0]);
      target(tname, an.attrs, keyframesOf(an.attrs, 'trimPathEnd', vals.map(v => 1 - v / L)));
    } else if (an.attrs.attributeName === 'opacity') {
      // SMIL: an opacity animation on the path itself replaces its opacity (the steam: 0.3 still, 0.5 while it rises), the groups' still
      // multiplies; one moved down from a group (« z », halo) replaces the group's, the path's own still multiplies
      const prop = a.fill && a.fill !== 'none' ? 'fillAlpha' : 'strokeAlpha';
      target(tname, an.attrs, keyframesOf(an.attrs, prop, vals.map(v => v * (an.down ? op : alpha))));
    }
  }
  out.push(inner ? `${s}>${inner}</path>` : `${s}/>`);
}
// les <animate> d'opacité posés sur un groupe (z, vapeur, halo) sont redescendus sur ses chemins
(function push(e) { for (const k of e.kids) push(k);
  if (e.tag === 'g') { const an = e.kids.filter(k => k.tag === 'animate'); if (an.length) { e.kids = e.kids.filter(k => k.tag !== 'animate'); (function down(x) { for (const k of x.kids) { if (k.tag === 'g') down(k); else if (shapeData(k)) k.kids.push(...an.map(m => ({ ...m, down: true }))); } })(e); } } })(svg);

// Layers, in drawing order: each accessory piece (LAYERED gate group) is its own layer, and the body is cut wherever a piece
// is drawn between two body parts. opts.noSlots (head icons, which never wear them): pieces dropped.
// Anonymous animated nodes are named here, so that a group shared by several layers (the head) has the same name in each.
const KID = k => !['animateTransform', 'animate', 'title', 'defs', 'clipPath'].includes(k.tag);
// The night outline (glose_lib.js halo(): « lisere » and the « <part>_halo » groups, all at the back) is one layer of its own, drawn only at
// night (gate Halo, Glose.drawable): by day the back layer (books, shadow) no longer redraws each frame because invisible outlines move.
const order = []; let segs = 0, open = true;
(function mark(e, piece) {
  if (e.tag === 'g' || e.tag === 'svg') {
    if (!e.attrs.id && e.kids.some(k => k.tag === 'animateTransform')) e.nm = `a${uid++}`;
    const halo = !piece && !opts.noSlots && e.tag === 'g' && /^(lisere|\w+_halo)$/.test(e.attrs.id || '');
    const g = !piece && (halo ? [, 'Halo'] : e.attrs.id && GATES.find(([re]) => re.test(e.attrs.id)));
    const p = piece || (halo ? 'lisere' : g && LAYERED.includes(g[1]) ? e.attrs.id : null);
    if (p && !piece && !opts.noSlots && !(halo && order.length && order[order.length - 1][1] === '_lisere')) { order.push([g[1], `_${p}`]); open = true; }
    if (halo && order.filter(o => o[1] === '_lisere').length > 1) throw new Error(`${name}: night outline groups not contiguous (${e.attrs.id})`);
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
  const vdName = put(resDir, base + key, vd, !key), avd = targets.length || !key ? put(resDir, name + key, avdXml(vdName, targets.map(t => ({ name: t.name, res: shared(resDir, t.xml) }))), !key) : null;
  layers.push({ gate, key, vd: vdName, xml: vd, res: targets.length ? avd : vdName, names: new Set([...vd.matchAll(/android:name="([^"]+)"/g)].map(m => m[1])) });
  for (const t of targets) for (const [prop, vals, k] of t.keys) { // self-check of the idle life, and its first frame for the reactions
    const still = stillValue(vd, t.name, prop), at = `${name}${key} ${t.name}.${prop}`, last = vals[vals.length - 1];
    // a delayed animator writes nothing before its delay: its first key must be the still value, or the part jumps when it starts
    if (t.offset && vals[0] !== still) throw new Error(`${at}: starts at ${vals[0]} after ${t.offset} ms, still value ${still}`);
    // a life track leaves its still and comes back to it within the life: frame 0 and the rest after IDLE_MS are the still drawing
    if (t.life && (vals[0] !== still || last !== still)) throw new Error(`${at}: life track from ${vals[0]} to ${last}, still value ${still}`);
    // stop() (= end() on RenderThread) at IDLE_MS puts every animator on its last key: a loop must have played whole cycles by then,
    // or (a loop longer than the idle life) rest on its last value from that moment
    const cut = (IDLE_MS - t.offset) / t.dur - (t.n - 1);
    if (cut < 1 && vals.some((v, i) => (k[i] >= cut || k[i + 1] > cut) && v !== last)) throw new Error(`${at}: still moving when the idle life stops (${IDLE_MS} ms, fraction ${cut.toFixed(3)}, ${t.n} x ${t.dur} ms from ${t.offset}): ${vals} at ${k}`);
    ((IDLE0[name] = IDLE0[name] || {})[t.name] = IDLE0[name][t.name] || {})[prop] = { start: vals[0], still, low: Math.min(...vals) };
  }
  size += vd.length;
}
if (layers.length > 1) { LAYERS[name] = layers.map(l => [l.gate, l.res]); if (!opts.vd) LAYERS[base] = layers.map(l => [l.gate, l.vd]); }
if (!opts.vd) PARTS[base] = layers;
console.log(name, `${layers.length} calques`, opts.vd ? '' : `VD ${(size / 1024).toFixed(0)} Ko`);
return gates;
}
// value of property [prop] of the node named [nm] in VD xml (attribute, or the VectorDrawable default)
function stillValue(vd, nm, prop) {
  const m = new RegExp(`<(?:group|path) android:name="${nm}"([^>]*)>`).exec(vd), v = m && new RegExp(`android:${prop}="([-\\d.e]+)"`).exec(m[1]);
  return v ? +v[1] : /^(scale|fillAlpha|strokeAlpha|trimPathEnd)/.test(prop) ? 1 : 0;
}
// Animators-only AVD [name] on the already converted drawable [vdName] (reactions): targets = { node name: objectAnimator xml }.
// One layer per layer of the drawable, animated where its targets are, so that worn accessories follow the head.
function animated(name, vdName, targets, resDir) {
  const layers = PARTS[vdName].map(l => {
    const t = Object.keys(targets).flatMap(n => [n, n + '_halo']).filter(n => l.names.has(n)) // the night outline of a group follows it
      .map(n => ({ name: n, res: shared(resDir, targets[n.replace(/_halo$/, '')] || targets[n]) }));
    const avd = t.length || !l.key ? put(resDir, name + l.key, avdXml(l.vd, t), !l.key) : null; // the key is always written
    return [l.gate, t.length ? avd : l.vd];
  });
  if (layers.length > 1) LAYERS[name] = layers;
}
if (require.main === module) { // node android/svg2avd.js : self-check of hermite() (a swing passes 0° at full speed, rests at its ends)
  const eq = require('assert').deepStrictEqual, H = (a, b) => `@interpolator/glose_h_${a * 1000}_${b * 1000}`;
  eq(hermite([0, 6, 0, -6, 0], [0, 0.25, 0.5, 0.75, 1]), [null, EASE, H(0, 1), H(1, 0), EASE]);
  eq(hermite([0, 2, 4, 6], [0, 1 / 3, 2 / 3, 1]), [null, H(0, 1), null, H(1, 0)]);
  eq(hermite([0, 0, 150, 150, 0], [0, 0.2, 0.5, 0.7, 1]), [null, null, EASE, null, EASE]);
  eq(hermite([0, -2, 7, 5], [0, 0.1, 0.28, 0.4], false, [, , 'out']), [null, EASE, DECEL, EASE]); // snap out of the anticipation
  eq(hermite([2, -14, 10, 2], [0, 0.6667, 0.6677, 1], true)[1], H(1, 0)); // a « z » rotated by its phase passes its first key
  console.log('hermite ok');
  // lifeXml: a breath, a hold, a blink: cut at the hold (nothing written during it), the blink accelerates shut and decelerates open
  const k = [0, 1000, 2600, 4000, 4055, 4150, 26000].map(t => t / IDLE_MS), L = lifeXml([['scaleY', [1, 1.02, 1, 1, 0.001, 1, 1], k]], [, , , , 'in', 'out'], 'test');
  eq(L.chunks, [[0, 2600], [4000, 4150]]);
  if (!/startOffset="4000"><propertyValuesHolder[^>]*><keyframe android:fraction="0" android:value="1"\/><keyframe android:fraction="0.3667" android:value="0.001" android:interpolator="@android:interpolator\/accelerate_quad"\/><keyframe android:fraction="1" android:value="1" android:interpolator="@android:interpolator\/decelerate_quad"\/>/.test(L.xml)) throw new Error(L.xml);
  console.log('life ok');
}
module.exports = { convert, animated, hermite, hermiteXml, HERMITE, TOKENS, LAYERED, LAYERS, ALIASES, PARTS, IDLE0 };
