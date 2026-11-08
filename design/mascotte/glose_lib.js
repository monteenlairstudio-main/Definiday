// MONSIEUR GLOSE — bibliothèque commune (préfixée à chaque pose par build.js).
// Tout est créé avec insert:false et rangé dans des groupes nommés : ordre des calques = ordre des listes.
// Repère : canevas 1024, lumière en haut à gauche, trait extérieur OUT, intérieur IN, encre INK.
const { Path, Point, Group, Color, Matrix } = paper;
const INK = '#3B2A20', [OUT, IN] = VARIANT.trait || [9, 5]; // variante {trait:[OUT, IN]} : essai d'épaisseur (décision du propriétaire)
let SW = 1; // multiplicateur d'épaisseur (version « icône » : 1.7)
const P = a => (a instanceof Point ? a : new Point(a));
const NI = { insert: false };
const xy = p => [p.x, p.y];

// ---------- primitives ----------
function poly(pts) { return new Path({ segments: pts, closed: true, insert: false }); }
function blob(pts, f = 0.5) { const p = poly(pts); p.smooth({ type: 'catmull-rom', factor: f }); return p; }
function curve(pts, f = 0.5) { const p = new Path({ segments: pts, insert: false }); if (pts.length > 2) p.smooth({ type: 'catmull-rom', factor: f }); return p; }
function ell(c, r) { return new Path.Ellipse({ center: c, radius: r, insert: false }); }
function circ(c, r) { return new Path.Circle({ center: c, radius: r, insert: false }); }
function arc(a, b, c) { return new Path.Arc({ from: a, through: b, to: c, insert: false }); }
function rect(x, y, w, h) { return new Path.Rectangle({ point: [x, y], size: [w, h], insert: false }); }
function U(...ps) { ps = ps.filter(Boolean); let r = ps[0]; for (let i = 1; i < ps.length; i++) r = r.unite(ps[i], NI); return r; }
function S(a, ...bs) { let r = a; for (const b of bs) if (b) r = r.subtract(b, NI); return r; }
function X(a, b) { return a.intersect(b, NI); }
function moved(p, dx, dy) { const c = p.clone(NI); c.translate(new Point(dx, dy)); return c; }
function rpoly(pts, r) {
  const p = new Path(NI), n = pts.length;
  for (let i = 0; i < n; i++) {
    const a = P(pts[(i - 1 + n) % n]), b = P(pts[i]), c = P(pts[(i + 1) % n]);
    const rr = Math.min([].concat(r)[i % [].concat(r).length], a.getDistance(b) / 2, c.getDistance(b) / 2);
    const p1 = b.add(a.subtract(b).normalize(rr)), p2 = b.add(c.subtract(b).normalize(rr));
    if (i === 0) p.moveTo(p1); else p.lineTo(p1);
    p.quadraticCurveTo(b, p2);
  }
  p.closePath(); return p;
}
// membre / queue effilé le long d'une ligne médiane lissée ; prof(t) = largeur
function taper(pts, w0, w1, prof) {
  const c = curve(pts), L = c.length, N = 48, A = [], B = [];
  const W = prof || (t => w0 + (w1 - w0) * t);
  for (let i = 0; i <= N; i++) {
    const s = Math.min(L * i / N, L - 0.01), p = c.getPointAt(s), nrm = c.getNormalAt(s), w = W(i / N) / 2;
    A.push(p.add(nrm.multiply(w))); B.push(p.subtract(nrm.multiply(w)));
  }
  const body = poly(A.concat(B.reverse())); body.smooth({ type: 'catmull-rom', factor: 0.5 });
  return U(body, circ(c.firstSegment.point, W(0) / 2), circ(c.lastSegment.point, W(1) / 2));
}
// ---------- style ----------
function fill(p, c, o) { p.fillColor = c; p.strokeColor = null; if (o != null) p.opacity = o; return p; }
function line(p, w = IN, c = INK, o) { p.fillColor = null; p.strokeColor = c; p.strokeWidth = w * SW; p.strokeCap = 'round'; p.strokeJoin = 'round'; if (o != null) p.opacity = o; return p; }
function outline(p, w = OUT) { return line(p.clone(NI), w); }
function grad(p, stops, o, d, radial) { p.fillColor = { gradient: { stops, radial: !!radial }, origin: o, destination: d }; p.strokeColor = null; return p; }
function shadowOf(shape, dx = 22, dy = 26, o = 0.16, c = INK) { return fill(S(shape, moved(shape, -dx, -dy)), c, o); }
function rimOf(shape, dx = 10, dy = 12, o = 0.26, c = '#FFF6E6') { return fill(S(shape, moved(shape, dx, dy)), c, o); }
function G(name, children) { const g = new Group({ children: children.filter(Boolean), insert: false }); if (name) g.name = name; return g; }
function clipIn(shape, items) { const m = shape.clone(NI); m.clipMask = true; return new Group({ children: [m, ...items.filter(Boolean)], clipped: true, insert: false }); }
function vol(name, shape, color, details = [], w = OUT) { return G(name, [fill(shape.clone(NI), color), ...details, outline(shape, w)]); }
function floorShadow(c, r, o = 0.3) { return G(null, [1, 0.86, 0.72, 0.58].map(k => fill(ell(c, [r[0] * k, r[1] * k]), INK, o / 4))); }
// bandes de pelage le long d'une courbe (anneaux de queue, rayures de membre)
function ringsAlong(pts, fracs, half = 44, thick = 9) {
  const tc = curve(pts), out = [];
  for (const f of fracs) {
    const s = tc.length * f, p = tc.getPointAt(s), n = tc.getNormalAt(s), tg = tc.getTangentAt(s);
    out.push(blob([p.add(n.multiply(half)).add(tg.multiply(-thick)), p.add(tg.multiply(-thick * 0.3)), p.add(n.multiply(-half)).add(tg.multiply(-thick * 0.8)),
      p.add(n.multiply(-half)).add(tg.multiply(thick * 0.8)), p.add(tg.multiply(thick)), p.add(n.multiply(half)).add(tg.multiply(thick))], 0.3));
  }
  return U(...out);
}

// ---------- palettes ----------
const BASE = { rose: '#F0A6A2', noseC: '#E98C8C', night: '#2C3A5E', nightD: '#1F2944', gold: '#D9A441', goldD: '#A87A26', parch: '#F3E2BE',
  bord: '#8E3A44', bordD: '#6E2A33', sepia: '#9C6B4A', sepiaD: '#7C5236', glow: '#FFD690', white: '#FFFAF0' };
const SKINS = {
  tigre:      { label: 'Gris tigré', fur: '#A4ACBC', furD: '#788196', cream: '#F8EEDD' },
  creme:      { label: 'Crème caramel', fur: '#E8D3B4', furD: '#B98E66', cream: '#FCF4E6' },
  enluminure: { label: 'Enluminure', fur: '#E9C477', furD: '#B47A2C', cream: '#FFF5DC', extra: 'gilt', rare: true },
  roux:       { label: "Roux pain d'épices", fur: '#DE9457', furD: '#B4652F', stripe: '#B4652F', cream: '#FBEEDC' },
  smoking:    { label: 'Smoking', fur: '#4A4448', furD: '#3A3438', stripe: '#433D41', cream: '#FFFAF2', bookCover: '#8E3A44', bookCoverD: '#6E2A33', cup: '#8E3A44', cupD: '#6E2A33' },
  nuit:       { label: "Nuit d'encre", fur: '#4A5A86', furD: '#2C3659', cream: '#DCE2F2', extra: 'stars', rare: true, noseC: '#D98A9A', rose: '#E6A0B0',
                bookCover: '#8E3A44', bookCoverD: '#6E2A33', cup: '#8E3A44', cupD: '#6E2A33' }, // socle et tasse bordeaux : le pelage bleu nuit ne s'y fond pas
};
const YARNS = {
  bordeaux: { label: 'Bordeaux', ball: '#9A4250', ballD: '#5A2230' },
  moutarde: { label: 'Moutarde', ball: '#D9A441', ballD: '#8A5F1E' },
  sauge:    { label: 'Sauge', ball: '#8FA585', ballD: '#4F6347' },
  rose:     { label: 'Rose poudré', ball: '#E7A3A0', ballD: '#9C5050' },
  lavande:  { label: 'Lavande', ball: '#A99BC4', ballD: '#65587F' },
  bleu_prusse: { label: 'Bleu de Prusse', ball: '#2F5B8C', ballD: '#173252' },
  vert_eau: { label: "Vert d'eau", ball: '#A9D6C4', ballD: '#5E8E7C' },
};
const SPECS = { ecaille: '#8A5530' }; // monture (lunettes) ; défaut : C.gold
function makeC(v) {
  v = Object.assign({ skin: 'tigre', yarn: 'bordeaux', acc: [] }, v || {});
  const C = Object.assign({}, BASE, SKINS[v.skin] || SKINS.tigre, YARNS[v.yarn] || YARNS.bordeaux);
  C.v = v; C.has = a => v.acc.includes(a);
  C.stripe = C.stripe || C.furD; C.specs = SPECS[v.specs] || C.gold;
  // v.all (export Android) : tous les accessoires et détails rares dessinés, couleurs de socle/tasse en sentinelles ;
  // svg2avd.js les rend visibles ou non par attributs de thème (voir GATES et TOKENS dans android/svg2avd.js)
  if (v.all) { C.has = () => true; C.S = { book: '#2C3A01', bookD: '#1F2901', top: '#2C3A02', topD: '#1F2902', low: '#9C6B03', lowD: '#7C5203', cup: '#2C3A04' }; C.stripe = '#788101'; C.specs = '#D9A401'; }
  return C;
}

// ---------- tête de Monsieur Glose (repère local v2 : centre crâne 500,352) ----------
// Torsion 3/4 : chaque détail du visage est « projeté » sur une sphère de rayon R tournée de `turn` radians
// (x' = R·sin(asin(x/R) + turn) ; poignées horizontales × cos(θ+turn)/cos θ) : le visage glisse, l'œil lointain s'affine.
function headWarp(turn, R = 200, cx = 500) {
  const cl = s => Math.max(-0.97, Math.min(0.97, s));
  return {
    x: x => cx + R * Math.sin(Math.asin(cl((x - cx) / R)) + turn),
    k: x => { const t = Math.asin(cl((x - cx) / R)); return Math.cos(t + turn) / Math.cos(t); },
  };
}
function warp(item, W) {
  if (item.children) { item.children.forEach(c => warp(c, W)); return item; }
  for (const s of item.segments) {
    const p = s.point, k = W.k(p.x), hi = s.handleIn, ho = s.handleOut;
    s.point = new Point(W.x(p.x), p.y); s.handleIn = new Point(hi.x * k, hi.y); s.handleOut = new Point(ho.x * k, ho.y);
  }
  return item;
}
function cuteEye(c, rx, ry, lidColor, iris = ['#2A1D16', '#3E5486']) {
  c = P(c);
  const base = grad(ell(c, [rx, ry]), iris, c.add([0, -ry * 0.3]), c.add([0, ry]));
  const h1 = fill(circ(c.add([-rx * 0.3, -ry * 0.34]), Math.min(rx, ry) * 0.44), '#FFFFFF');
  const h2 = fill(circ(c.add([rx * 0.34, ry * 0.36]), Math.min(rx, ry) * 0.18), '#FFFFFF', 0.9);
  const eye = G(null, [base, h1, h2]);
  const lid = G(null, [fill(ell(c, [rx + 4, ry + 4]), lidColor),
    line(arc(c.add([-rx - 3, ry * 0.05]), c.add([0, ry * 0.55]), c.add([rx + 3, ry * 0.05])), IN)]);
  return { eye, lid, top: c.add([0, -ry - 4]) };
}
function nose(c, w = 30, h = 20, color = '#E98C8C') {
  c = P(c);
  const n = rpoly([c.add([-w / 2, -h / 2]), c.add([w / 2, -h / 2]), c.add([0, h / 2])], [7, 7, 6]);
  return G(null, [fill(n.clone(NI), color), fill(ell(c.add([-w * 0.14, -h * 0.18]), [w * 0.15, h * 0.12]), '#FFFFFF', 0.6), outline(n, 4)]);
}
function mouthW(c, s = 1) {
  c = P(c);
  return G(null, [line(curve([c, c.add([0, 9 * s])])),
    line(arc(c.add([0, 9 * s]), c.add([-12 * s, 21 * s]), c.add([-25 * s, 9 * s]))),
    line(arc(c.add([0, 9 * s]), c.add([12 * s, 21 * s]), c.add([25 * s, 9 * s])))]);
}
// o : { at:[x,y] centre du crâne dans la scène, tilt (°, autour du cou), turn (rad), scale, sleep, prefix, glare, mood ('fier' | 'curieux'),
//       whisk (longueur relative des moustaches), ear (hauteur relative des oreilles, pointe ramenée vers la base) }
function gloseHead(C, o = {}) {
  const pre = o.prefix || '', turn = o.turn || 0, W = headWarp(turn), We = headWarp(turn * 0.6);
  const wx = x => W.x(x), anims = [];
  const sleep = !!o.sleep;
  // silhouette : crâne + oreilles (projetées plus doucement) + touffes de joues (la lointaine un peu réduite)
  const skull = ell([500, 352], [176, 146]);
  const ek = o.ear || 1, eT = ([x, y]) => x < 500 ? [394 + (x - 394) * ek, 265 + (y - 265) * ek] : [606 + (x - 606) * ek, 265 + (y - 265) * ek]; // pointe -> milieu de la base
  const earL = warp(rpoly([[334, 304], eT([346, 128]), [454, 226]], [40, 32, 40]), We);
  const earR = warp(rpoly([[546, 226], eT([654, 128]), [666, 304]], [40, 32, 40]), We);
  const tL = blob([[346, 366], [304, 402], [330, 414], [312, 446], [356, 458], [370, 410]], 0.5);
  const tR = blob([[654, 366], [696, 402], [670, 414], [688, 446], [644, 458], [630, 410]], 0.5);
  const kT = 1 - Math.max(0, turn) * 0.9, kTl = 1 - Math.max(0, -turn) * 0.9;
  tR.scale(kT, [650, 410]); tL.scale(kTl, [350, 410]);
  const head = U(skull, earL, earR, tL, tR);
  const innerL = warp(rpoly([[360, 278], eT([362, 166]), [434, 236]], [26, 18, 26]), We), innerR = warp(rpoly([[566, 236], eT([638, 166]), [640, 278]], [26, 18, 26]), We);
  const earHair = [[[376, 284], [372, 256], [380, 234]], [[390, 282], [390, 262]], [[624, 284], [628, 256], [620, 234]], [[610, 282], [610, 262]]]
    .map(pts => line(warp(curve(pts.map(eT)), We), 4, C.cream, 0.9));
  const muzzle = X(warp(U(circ([478, 452], 36), circ([522, 452], 36), ell([500, 480], [52, 30])), W), head);
  const mMark = U(warp(U(blob([[486, 210], [514, 210], [506, 272], [494, 272]], 0.4), blob([[442, 222], [464, 214], [468, 262], [460, 264]], 0.4),
    blob([[536, 214], [558, 222], [540, 264], [532, 262]], 0.4)), W), taper([[322, 340], [362, 350]], 18, 6), taper([[678, 340], [638, 350]], 18, 6),
    taper([[326, 372], [356, 376]], 14, 5), taper([[674, 372], [644, 376]], 14, 5));
  const eyeC = [[428, 384], [572, 384]].map(([x, y]) => ({ c: [wx(x), y], k: W.k(x) }));
  const shift = x => wx(x) - x;
  const whisk = [[[386, 442], [330, 430], [282, 434]], [[388, 456], [334, 460], [290, 476]], [[614, 442], [670, 430], [718, 434]], [[612, 456], [666, 460], [710, 476]]]
    .map(w => w.map(([x, y]) => [w[0][0] + (x - w[0][0]) * (o.whisk || 1), w[0][1] + (y - w[0][1]) * (o.whisk || 1)]))
    .map(w => { const d = shift(w[0][0]); return line(curve(w.map(([x, y], i) => [x + d * (1 - i * 0.35), y])), 4.5, INK, 0.85); });
  // yeux
  let eyes = [], lids = null, eyeTops = [];
  if (!sleep) {
    const e = eyeC.map(({ c, k }) => cuteEye(c, 35 * k, 41, C.fur));
    eyes = e.map(q => q.eye); eyeTops = e.map(q => q.top);
    lids = G(pre + 'paupieres', [G(pre + 'paupiere_g', [e[0].lid]), G(pre + 'paupiere_d', [e[1].lid])]);
  } else {
    eyes = eyeC.map(({ c, k }) => {
      const [x, y] = c, r = 31 * k;
      return G(null, [line(curve([[x - r, y - 2], [x - r * 0.45, y + 13], [x + r * 0.45, y + 13], [x + r, y - 2]], 0.5), 7),
        line(curve([[x + (x < wx(500) ? -r : r), y - 2], [x + (x < wx(500) ? -r - 10 : r + 10), y - 9]]), IN)]);
    });
  }
  // lunettes rondes dorées : verres en ellipses projetées, branches vers les tempes, reflet animé
  const drop = o.glassesDrop || 0, LR = 56;
  const gl = [[428, 386 + drop], [572, 386 + drop]];
  const lenses = gl.map(c => warp(circ(c, LR - 6), W));
  const rings = gl.map(c => warp(S(circ(c, LR + 6), circ(c, LR - 6)), W));
  const dbl = (pts, w) => [line(curve(pts), w + 8), line(curve(pts), w, C.specs)];
  const lx = wx(428 - LR), rx = wx(572 + LR), br = [wx(480), wx(500), wx(520)];
  const glare = G(pre + 'reflet', gl.map(([x, y]) => { const c = wx(x); return fill(poly([[c - 40, y + 70], [c - 14, y + 70], [c + 40, y - 70], [c + 14, y - 70]]), '#FFFFFF', 0.5); }));
  const glassItems = [
    ...lenses.map(l => fill(l.clone(NI), '#FFFFFF', 0.12)), o.glare === false ? null : clipIn(U(...lenses), [glare]),
    ...dbl([[332, 364 + drop], [lx + 4, 372 + drop]], 7), ...dbl([[668, 364 + drop], [rx - 4, 372 + drop]], 7),
    ...dbl([[br[0], 380 + drop], [br[1], 368 + drop], [br[2], 380 + drop]], 7),
    ...rings.map(r => G(null, [fill(r.clone(NI), C.specs), fill(S(r, moved(r, -5, -6)), INK, 0.2), line(r, 4)])),
    ...gl.map(([x, y]) => fill(circ([wx(x - 30), y - 34], 5.5), '#FFF6E6', 0.9))];
  const glasses = G(pre + 'lunettes', glassItems);
  if (o.glassesTilt) glasses.rotate(o.glassesTilt, [wx(500), 400]);
  // vie au repos : chaque partie a sa propre période (paupières 9,4 = 2 × 4,7 s, reflet 11 s, bâillement 29 s, queue 7,3 s dans les poses),
  // les cycles ne se recalent qu'après plusieurs minutes : le motif paraît irrégulier
  if (o.glare !== false) anims.push({ id: pre + 'reflet', type: 'translate', values: [[-110, 0], [-110, 0], [110, 0], [110, 0]], keyTimes: [0, 0.8, 0.9, 1], dur: 11, begin: o.glareBegin || 0, base: 'translate(-110 0)' });
  const brows = sleep ? [line(curve([[wx(398), 318], [wx(420), 312], [wx(444), 316]]), 5, INK, 0.7), line(curve([[wx(556), 316], [wx(580), 312], [wx(602), 318]]), 5, INK, 0.7)]
    : o.mood === 'curieux' ? [line(curve([[wx(396), 304], [wx(420), 296], [wx(446), 302]]), 5, INK, 0.8), line(curve([[wx(552), 290], [wx(578), 270], [wx(606), 280]]), 5, INK, 0.8)]
    : o.mood === 'fier' ? [line(curve([[wx(394), 300], [wx(420), 284], [wx(446), 292]]), 5, INK, 0.8), line(curve([[wx(554), 292], [wx(580), 284], [wx(606), 300]]), 5, INK, 0.8)]
    : [line(curve([[wx(396), 304], [wx(420), 294], [wx(446), 300]]), 5, INK, 0.8), line(curve([[wx(554), 300], [wx(580), 294], [wx(604), 304]]), 5, INK, 0.8)];
  // humeur : bouche ouverte en « D » (fier), petit « o » (curieux), sinon le « w » de toujours
  const mx = wx(500), mk = W.k(500);
  const mouth = o.mood === 'fier' ? (() => { const m = blob([[mx - 26 * mk, 450], [mx + 26 * mk, 450], [mx + 18 * mk, 468], [mx, 476], [mx - 18 * mk, 468]], 0.45);
      return vol(null, m, '#7A3A3E', [fill(X(ell([mx + 4, 476], [16 * mk, 10]), m), C.rose)], IN); })()
    : o.mood === 'curieux' ? vol(null, ell([mx + 6, 464], [8 * mk, 10]), '#7A3A3E', [], IN) : mouthW([mx, 448]);
  const extra = skinExtra(C, head, 'head', [[wx(410), 250], [wx(600), 262], [wx(470), 300]]);
  // bâillement (une fois par cycle de 29 s) : bouche ouverte et paupières closes, cachées (échelle 0) le reste du temps
  const yawn = !sleep && o.yawn !== false, YK = [0, 0.5, 0.53, 0.6, 0.63, 1], YV = [[1, 0.001], [1, 0.001], [1, 1], [1, 1], [1, 0.001], [1, 0.001]];
  const mouthO = yawn ? G(pre + 'baille', [vol(null, ell([wx(500), 466], [15 * W.k(500), 19]), '#7A3A3E', [fill(X(ell([wx(500), 482], [11 * W.k(500), 8]), ell([wx(500), 466], [15 * W.k(500), 19])), C.rose)], IN)]) : null;
  const headIn = [fill(X(U(innerL, innerR), head), C.rose), ...earHair,
    fill(X(mMark, head), C.stripe), shadowOf(head, 26, 30, 0.14), rimOf(head, 10, 12), fill(muzzle.clone(NI), C.cream), shadowOf(muzzle, 8, 12, 0.1),
    ...extra,
    fill(ell([wx(386), 452], [28 * W.k(386), 16]), C.rose, sleep ? 0.85 : 0.7), fill(ell([wx(614), 452], [28 * W.k(614), 16]), C.rose, sleep ? 0.85 : 0.7),
    ...eyes, nose([wx(500), 438], 30, 20, C.noseC), mouth, mouthO, lids,
    ...(yawn ? [0, 1].map(i => G(pre + 'baille_' + 'gd'[i], [lids.children[i].children[0].clone(NI)])) : []), glasses, ...brows];
  const parts = [o.behind || null, C.has('plume') ? quill(C, turn) : null, vol(null, head, C.fur, headIn), ...whisk,
    C.has('bonnet') ? nightcap(C, earL, earR, innerL, innerR, turn, pre) : null, C.has('beret') ? beret(C, W, pre) : null, C.has('couronne') ? crown(C, W, pre) : null, ...(o.front || [])];
  const g = G(pre + 'tete', parts);
  // placement dans la scène
  const at = P(o.at || [500, 352]), s = o.scale || 1, neck = P([500, 520]);
  const M = new Matrix().translate(at.subtract([500, 352])).rotate(o.tilt || 0, neck).scale(s, neck);
  g.transform(M);
  const T = q => xy(P(q).transform(M));
  g.data = { pivot: T(neck) }; // pivot des réactions one-shot (svg2avd -> android:pivotX/Y du groupe « tete »)
  if (!sleep) { // un clignement, puis un double clignement
    const blink = { values: [[1, 0.001], [1, 0.001], [1, 1], [1, 0.001], [1, 0.001], [1, 1], [1, 0.001], [1, 1], [1, 0.001], [1, 0.001]], keyTimes: [0, 0.27, 0.3, 0.33, 0.86, 0.885, 0.91, 0.935, 0.96, 1], dur: 9.4, begin: o.blinkBegin || 0.7, base: 'scale(1 0.001)' };
    anims.push(Object.assign({ id: pre + 'paupiere_g', type: 'scale', pivot: T(eyeTops[0]) }, blink), Object.assign({ id: pre + 'paupiere_d', type: 'scale', pivot: T(eyeTops[1]) }, blink));
  }
  if (yawn) {
    anims.push({ id: pre + 'baille', type: 'scale', pivot: T([wx(500), 448]), values: YV.map(([, y]) => [y, y]), keyTimes: YK, dur: 29, base: 'scale(0.001)' }); // caché en point, pas en trait
    eyeTops.forEach((t, i) => anims.push({ id: pre + 'baille_' + 'gd'[i], type: 'scale', pivot: T(t), values: YV, keyTimes: YK, dur: 29, base: 'scale(1 0.001)' }));
    anims.push({ id: pre + 'tete', type: 'translate', values: [[0, 0], [0, 0], [0, -7], [0, -7], [0, 0], [0, 0]], keyTimes: YK, dur: 29 });
  }
  return { group: g, anims, T, M, chin: T([wx(500), 500]), neck: T(neck), crown: T([wx(500), 206]), head };
}
// ---------- variantes de pelage : détails rares ----------
// gilt : paillettes d'or qui scintillent ; stars : petites étoiles pâles sur le pelage « Nuit d'encre »
let SPARK_ID = 0;
function sparkle(c, r, color, o = 1) {
  c = P(c);
  const s = rpoly([c.add([0, -r]), c.add([r * 0.22, -r * 0.22]), c.add([r, 0]), c.add([r * 0.22, r * 0.22]), c.add([0, r]), c.add([-r * 0.22, r * 0.22]), c.add([-r, 0]), c.add([-r * 0.22, -r * 0.22])], r * 0.08);
  return fill(s, color, o);
}
function skinExtra(C, shape, where, pts) {
  if (C.v.all) return [G('paillettes_' + where, skinExtra({ extra: 'gilt', v: {} }, shape, where, pts)), G('etoiles_' + where, skinExtra({ extra: 'stars', v: {} }, shape, where, pts))];
  if (C.extra === 'gilt') return [clipIn(shape, pts.map((p, i) => sparkle(p, i % 2 ? 9 : 13, '#FFF7D8', 0.95)))];
  if (C.extra === 'stars') return [clipIn(shape, pts.map((p, i) => sparkle(p, i % 2 ? 7 : 10, '#F4E7B8', 0.9)))];
  return [];
}
function rareGlints(C, pts, prefix) { // scintillement animé autour du personnage (pelages rares uniquement)
  if (!C.extra && !C.v.all) return { group: null, anims: [] };
  const col = C.extra === 'stars' ? '#F4E7B8' : '#FFE9A8', anims = [], items = [];
  pts.forEach(([x, y, r], i) => {
    const id = `${prefix}eclat${i}`, s = G(id, [sparkle([x, y], r, col, 1), outline(circ([x, y], 0.1), 0.1)]);
    items.push(s);
    anims.push({ id, type: 'scale', pivot: [x, y], values: [[0.001, 0.001], [0.001, 0.001], [1, 1], [0.001, 0.001], [0.001, 0.001]], keyTimes: [0, 0.5 + i * 0.1, 0.62 + i * 0.1, 0.74 + i * 0.1, 1].map(v => Math.min(1, v)), dur: 4, base: 'scale(0.001)' });
  });
  return { group: G(prefix + 'eclats', items), anims };
}

// ---------- accessoires ----------
// plume d'oie glissée derrière l'oreille proche (dessinée AVANT la tête : sa base disparaît derrière le crâne)
function quill(C, turn) {
  const side = turn >= 0 ? -1 : 1, bx = 500 + side * 120, rach = [[bx, 300], [bx + side * 44, 216], [bx + side * 100, 146], [bx + side * 168, 96]];
  const vane = taper(rach.slice(1), 50, 8, t => 12 + 40 * Math.sin(Math.PI * Math.min(1, t * 1.15)) * (1 - 0.3 * t));
  const notches = [0.3, 0.52, 0.72].map(f => { const c = curve(rach.slice(1)), p = c.getPointAt(c.length * f), n = c.getNormalAt(c.length * f), tg = c.getTangentAt(c.length * f);
    return poly([p.add(n.multiply(side * 40)), p.add(n.multiply(side * 12)).add(tg.multiply(-6)), p.add(n.multiply(side * 40)).add(tg.multiply(-18))]); });
  const v2 = S(vane, ...notches);
  const barbs = [0.2, 0.36, 0.5, 0.64, 0.78].map(f => { const c = curve(rach.slice(1)), p = c.getPointAt(c.length * f), n = c.getNormalAt(c.length * f), tg = c.getTangentAt(c.length * f);
    return line(curve([p, p.add(n.multiply(-side * 22)).add(tg.multiply(-12))]), 3, '#8C93A6', 0.55); });
  return G('plume', [vol(null, v2, '#FBF6EC', [clipIn(v2, barbs), shadowOf(v2, -8 * side, 10, 0.14)], 7), line(curve(rach), 4.5, C.goldD)]);
}
// bonnet de nuit : dôme qui épouse le crâne, revers crème, pointe tombante + pompon ; les pointes d'oreilles passent par des fentes
function nightcap(C, earL, earR, inL, inR, turn, pre) {
  const red = '#C75B54';
  const W = headWarp(turn);
  const dome = blob([[318, 300], [338, 236], [410, 196], [500, 186], [590, 196], [662, 236], [682, 300], [500, 276]], 0.5);
  const tailC = [[560, 214], [652, 160], [742, 178], [790, 252], [796, 330]];
  const cap = U(dome, taper(tailC, 120, 30));
  const cuff = X(blob([[306, 314], [324, 258], [500, 236], [676, 258], [694, 314], [500, 290]], 0.5), dome);
  const pp = P(tailC[tailC.length - 1]).add([0, 12]), pompon = circ(pp, 28);
  const cut = rect(0, 0, 1024, 228);
  const tips = U(X(earL, cut), X(earR, cut));
  return G(pre + 'bonnet', [
    vol(null, cap, red, [shadowOf(cap, 30, 18, 0.16), rimOf(cap, 10, 10, 0.3), fill(cuff.clone(NI), C.cream), shadowOf(cuff, 14, 10, 0.12),
      line(S(cuff, moved(cuff, 0, 400)), IN), ...[360, 420, 480, 540, 600, 650].map(x => line(curve([[x, 258], [x + 2, 296]]), 3, INK, 0.25))]),
    vol(null, tips, C.fur, [fill(X(U(inL, inR), tips), C.rose)]),
    vol(null, pompon, C.cream, [shadowOf(pompon, 10, 10, 0.15), ...[0, 60, 120, 180, 240, 300].map(a => line(curve([pp, pp.add(new Point(18, 0).rotate(a))]), 3, INK, 0.2))]),
  ]);
}
// béret de peintre penché sur l'oreille lointaine, queue de feutre au sommet (dans la tête : suit ses animations)
function beret(C, W, pre) {
  const col = '#4A5470', colD = '#343C55';
  const cap = warp(blob([[372, 236], [392, 186], [460, 152], [548, 146], [626, 166], [668, 206], [652, 236], [560, 230], [470, 240]], 0.5), W);
  cap.rotate(8, [520, 200]);
  const band = X(warp(blob([[366, 244], [372, 218], [470, 226], [560, 216], [656, 222], [660, 246], [560, 252], [470, 258]], 0.5), W).rotate(8, [520, 200]), cap);
  const stalk = warp(taper([[536, 156], [540, 132], [552, 118]], 13, 8), W).rotate(8, [520, 200]);
  return G(pre + 'beret', [vol(null, stalk, colD, [], 6),
    vol(null, cap, col, [fill(band.clone(NI), colD), shadowOf(cap, 18, 16, 0.18), rimOf(cap, 8, 10, 0.3), fill(ell([462, 180], [44, 12]).rotate(-12), '#FFFFFF', 0.16)])]);
}
// couronne des rois : papier doré de la galette, pointes et gemmes, posée entre les oreilles
function crown(C, W, pre) {
  const gold = '#E9BE55', goldD = '#B98A2A';
  const pts = [[404, 250], [400, 196], [432, 150], [452, 196], [482, 140], [500, 192], [518, 140], [548, 196], [568, 150], [600, 196], [596, 250], [500, 262]];
  const c = warp(rpoly(pts, [14, 4, 6, 4, 6, 4, 6, 4, 6, 4, 14, 30]), W);
  const band = X(warp(blob([[396, 246], [398, 214], [500, 224], [602, 214], [604, 246], [500, 258]], 0.5), W), c);
  const gems = [[[432, 236], '#C75B54'], [[500, 242], '#4A6FA5'], [[568, 236], '#5E9A6E']].map(([p, k]) => vol(null, warp(circ(p, 9), W), k, [fill(circ([p[0] - 3, p[1] - 3], 3), '#FFFFFF', 0.6)], 3));
  return G(pre + 'couronne', [vol(null, c, gold, [fill(band.clone(NI), goldD, 0.55), shadowOf(c, 14, 14, 0.16), rimOf(c, 6, 8, 0.4), ...gems,
    ...[[432, 150], [482, 140], [518, 140], [568, 150]].map(p => fill(warp(circ([p[0], p[1] + 14], 4), W), '#FFF6E6', 0.8))], 6)]);
}
// grelot doré sur un ruban : ruban sous la tête (calque « grelot_ruban »), grelot devant (« grelot »)
function jingle(C, chin, o = {}) {
  const c = P(chin), w = o.w || 64, s = o.s || 1, red = '#C75B54';
  const rib = taper([xy(c.add([-w, -14])), xy(c.add([-w * 0.5, 4])), xy(c.add([0, 10])), xy(c.add([w * 0.5, 4])), xy(c.add([w, -14]))], 16, 16);
  const bc = c.add([0, 10 + 24 * s]), r = 23 * s, ball = circ(bc, r);
  const slit = U(taper([xy(bc.add([-r * 0.55, r * 0.28])), xy(bc.add([r * 0.55, r * 0.28]))], 5 * s, 5 * s), circ(bc.add([0, r * 0.32]), 5 * s));
  return { band: G('grelot_ruban', [vol(null, rib, red, [shadowOf(rib, 4, 6, 0.2), rimOf(rib, 3, 4, 0.3)], IN)]),
    bell: G('grelot', [vol(null, circ(bc.add([0, -r]), 8 * s), C.gold, [], 4),
      vol(null, ball, C.gold, [shadowOf(ball, 8 * s, 10 * s, 0.25), rimOf(ball, 4, 5, 0.4), line(arc(bc.add([-r, -2]), bc.add([0, 6 * s]), bc.add([r, -2])), 3, INK, 0.45),
        fill(slit, INK, 0.8), fill(circ(bc.add([-r * 0.4, -r * 0.42]), 5 * s), '#FFFFFF', 0.7)], IN)]) };
}
// petit sapin de papier plié : trois étages (pli clair / pli ombré), étoile dorée ; base au point `at`
function paperTree(C, at, s = 1) {
  const b = P(at), q = (x, y) => xy(b.add([x * s, y * s]));
  const green = '#5E9A6E', greenD = '#3F7350', items = [];
  [[0, 46, 22], [-30, 38, 18], [-58, 30, 14]].forEach(([y, hw, _], i) => {
    const top = q(0, y - 58 + i * 2), L = q(-hw, y), R = q(hw, y);
    const t = rpoly([top, R, q(0, y + 6), L], [4, 6, 8, 6]);
    items.push(vol(null, t, green, [fill(X(poly([top, q(0, y + 10), q(-hw - 4, y + 4)]), t), '#FFFFFF', 0.18), fill(X(poly([top, q(hw + 4, y + 4), q(0, y + 10)]), t), greenD, 0.55),
      line(curve([top, q(0, y + 5)]), 3, INK, 0.45)], 6));
  });
  const trunk = rpoly([q(-9, 4), q(9, 4), q(9, 22), q(-9, 22)], 3);
  const star = sparkleShape(q(0, -120), 15 * s);
  return G('sapin', [fill(ell(b.add([0, 22 * s]), [44 * s, 8 * s]), INK, 0.2), vol(null, trunk, C.sepia, [], 5), ...items, vol(null, star, C.gold, [], 4)]);
}
function sparkleShape(c, r) { c = P(c); return rpoly([c.add([0, -r]), c.add([r * 0.26, -r * 0.26]), c.add([r, 0]), c.add([r * 0.26, r * 0.26]), c.add([0, r]), c.add([-r * 0.26, r * 0.26]), c.add([-r, 0]), c.add([-r * 0.26, -r * 0.26])], r * 0.1); }
// nœud papillon bleu nuit à pois d'or (posé sous le menton)
function bowTie(C, at, rot = 0, s = 1.15) {
  const wL = rpoly([[494, 500], [436, 470], [430, 532]], [10, 14, 14]), wR = rpoly([[506, 500], [570, 532], [564, 470]], [10, 14, 14]);
  const knot = rpoly([[484, 484], [516, 484], [520, 516], [480, 516]], 8);
  const g = G('noeud', [
    vol(null, wL, C.night, [shadowOf(wL, 8, 10, 0.25), ...[[452, 488], [446, 514], [470, 506]].map(c => fill(circ(c, 4.5), C.gold)), line(curve([[452, 484], [470, 498], [450, 516]]), 3, INK, 0.5)]),
    vol(null, wR, C.night, [shadowOf(wR, 8, 10, 0.25), ...[[548, 488], [554, 514], [530, 506]].map(c => fill(circ(c, 4.5), C.gold)), line(curve([[548, 484], [530, 498], [550, 516]]), 3, INK, 0.5)]),
    vol(null, knot, C.night, [fill(circ([493, 494], 5), '#FFFFFF', 0.4)], 7)]);
  g.scale(s, [500, 500]); g.rotate(rot, [500, 500]); g.translate(P(at).subtract([500, 500]));
  return g;
}
// écharpe tricotée : bande autour du cou (côtes + rayures or) + pan qui pend, frange or
function scarf(C, o) {
  const c = P(o.c), w = o.w || 150, h = o.h || 46, sag = o.sag || 26;
  const band = blob([c.add([-w, -h * 0.35]), c.add([0, sag - h * 0.1]), c.add([w, -h * 0.4]), c.add([w + 8, h * 0.5]), c.add([0, sag + h * 0.9]), c.add([-w - 8, h * 0.55])], 0.5);
  const tailPts = o.tail.map(P), tailS = taper(tailPts.map(xy), 58, 52);
  const ribs = []; for (let x = -w - 10; x <= w + 10; x += 22) ribs.push(line(curve([c.add([x, -h]), c.add([x + 4, sag + h * 1.2])]), 3, '#FFFFFF', 0.22));
  const stripesT = [0.35, 0.62].map(f => { const tc = curve(tailPts.map(xy)), p = tc.getPointAt(tc.length * f), n = tc.getNormalAt(tc.length * f);
    return taper([xy(p.add(n.multiply(40))), xy(p.subtract(n.multiply(40)))], 13, 13); });
  const stripeB = [-0.55, 0.15, 0.75].map(f => taper([xy(c.add([w * f, -h])), xy(c.add([w * f + 4, sag + h * 1.2]))], 16, 16));
  const last = tailPts[tailPts.length - 1], tg = curve(tailPts.map(xy)).getTangentAt(curve(tailPts.map(xy)).length - 1), n = tg.rotate(90);
  const fringe = [-18, -6, 6, 18].map(d => line(curve([xy(last.add(n.multiply(d))), xy(last.add(n.multiply(d)).add(tg.multiply(26)))]), 6, C.gold));
  const tailG = vol(null, tailS, C.night, [fill(X(U(...stripesT), tailS), C.gold), shadowOf(tailS, 10, 10, 0.25), rimOf(tailS, 6, 6, 0.2)]);
  const bandG = vol(null, band, C.night, [clipIn(band, ribs), fill(X(U(...stripeB), band), C.gold, 0.95), shadowOf(band, 10, 14, 0.25), rimOf(band, 6, 8, 0.2)]);
  return { back: G('echarpe_dos', o.tailFront ? [] : [...fringe, tailG]), band: G('echarpe', [bandG, ...(o.tailFront ? [...fringe, tailG] : [])]) };
}
// marque-page de cuir bordeaux à gland d'or, qui sort des pages
function bookmark(C, pts) {
  const strip = ribbonShape(pts, 26);
  const end = P(pts[pts.length - 1]);
  const cord = line(curve([xy(end), xy(end.add([6, 20])), xy(end.add([2, 34]))]), 4, C.goldD);
  const tassel = rpoly([end.add([-10, 34]), end.add([14, 34]), end.add([20, 78]), end.add([-14, 78])].map(xy), [8, 8, 4, 4]);
  return G('marque_page', [vol(null, strip, C.bord, [shadowOf(strip, 6, 0, 0.2), line(S(strip, moved(strip, 0, 0)).clone(NI), 0.1), fill(X(curveBand(pts, 8), strip), C.gold, 0.9)], IN),
    cord, vol(null, tassel, C.gold, [shadowOf(tassel, 6, 8, 0.22), ...[-4, 4, 12].map(d => line(curve([xy(end.add([d, 44])), xy(end.add([d + 1, 74]))]), 2.5, INK, 0.35))], IN),
    vol(null, circ(end.add([2, 34]), 9), C.gold, [], IN)]);
}
function curveBand(pts, w) { return taper(pts.slice(0, -1), w, w, () => w); }
function ribbonShape(pts, w) {
  const c = curve(pts), r = taper(pts, w, w, () => w), end = c.lastSegment.point, tg = c.getTangentAt(c.length - 0.01), nrm = tg.rotate(90);
  const cut = poly([end.subtract(tg.multiply(w * 0.7)), end.add(nrm.multiply(w)).add(tg.multiply(w)), end.subtract(nrm.multiply(w)).add(tg.multiply(w))]);
  const sq = poly([end.add(nrm.multiply(w)), end.add(nrm.multiply(w)).add(tg.multiply(w * 2)), end.subtract(nrm.multiply(w)).add(tg.multiply(w * 2)), end.subtract(nrm.multiply(w))]);
  return S(r, cut, sq);
}
function ribbonOrMark(C, markPts, ribPts, w) { // le marque-page remplace le ruban (les deux en export Android)
  return G(null, [C.v.all || !C.has('marque') ? G('ruban', [ribbon(ribPts, w, C.rose)]) : null, C.has('marque') ? bookmark(C, markPts) : null]);
}
function ribbon(pts, w, color) {
  const sh = ribbonShape(pts, w);
  return G(null, [fill(sh.clone(NI), color), fill(S(sh, moved(sh, -6, 0)), INK, 0.15), outline(sh, IN)]);
}
// ---------- pelote & fil ----------
function yarn(name, c, r, C, extras = []) {
  c = P(c); const ball = circ(c, r), strands = [];
  for (let k = -3; k <= 3; k++) { const a = arc(c.add([-r * 1.3, k * r * 0.26 - r * 0.25]), c.add([0, k * r * 0.26 + r * 0.12]), c.add([r * 1.3, k * r * 0.26 - r * 0.25])); a.rotate(28, c); strands.push(line(a, 6, C.ballD, 0.75)); }
  for (let k = -1; k <= 2; k++) { const a = arc(c.add([k * r * 0.3 - r * 0.2, -r * 1.3]), c.add([k * r * 0.3 + r * 0.25, 0]), c.add([k * r * 0.3 - r * 0.2, r * 1.3])); a.rotate(-12, c); strands.push(line(a.clone(NI), 10, C.ballD, 0.6), line(a, 4, '#FFFFFF', 0.35)); }
  return G(name, [fill(ball.clone(NI), C.ball), clipIn(ball, [...strands, shadowOf(ball, r * 0.35, r * 0.4, 0.22),
    rimOf(ball, r * 0.12, r * 0.14, 0.32), fill(ell(c.add([-r * 0.42, -r * 0.5]), [r * 0.12, r * 0.07]).rotate(-35), '#FFFFFF', 0.55)]), ...extras, outline(ball)]);
}
// brin de laine cerné : âme couleur pelote + liseré d'encre (lisible à 128 px : 18 px d'épaisseur à 1024)
function strand(pts, C, w = 10, name) {
  const c = curve(pts);
  return G(name, [line(c.clone(NI), w + 8), line(c.clone(NI), w, C.ball), line(moved(c, -1.5, -2), w * 0.28, '#FFFFFF', 0.3)]);
}
// ---------- livre oblique (dos, couverture, tranche) ----------
function book(o) {
  const { x, y, w, h } = o, [dx, dy] = o.d, ct = Math.max(9, h * 0.14);
  const hex = [[x, y + h], [x, y], [x + dx, y + dy], [x + w + dx, y + dy], [x + w + dx, y + h + dy], [x + w, y + h]];
  const sil = rpoly(hex, [12, 8, 8, 8, 8, 12]);
  const front = X(poly([[x - 20, y], [x + w, y], [x + w, y + h + 20], [x - 20, y + h + 20]]), sil);
  const top = X(poly([[x - 20, y], [x + dx - 20, y + dy - 20], [x + w + dx, y + dy - 20], [x + w, y]]), sil);
  const side = X(poly([[x + w, y], [x + w + dx + 20, y + dy], [x + w + dx + 20, y + h + dy + 20], [x + w, y + h + 20]]), sil);
  const pages = X(poly([[x + w + 2, y + ct], [x + w + dx - 6, y + dy + ct], [x + w + dx - 6, y + h + dy - ct], [x + w + 2, y + h - ct]]), side);
  const items = [fill(front.clone(NI), o.cover), fill(top.clone(NI), o.cover), fill(top.clone(NI), '#FFFFFF', 0.14),
    fill(side.clone(NI), o.cover), fill(pages.clone(NI), o.pages)];
  for (let yy = y + ct + 7; yy < y + h - ct - 3; yy += 7) items.push(line(curve([[x + w + 3, yy], [x + w + dx - 8, yy + dy]]), 2, INK, 0.22));
  items.push(fill(X(poly([[x + w, y + h - ct - 12], [x + w + dx, y + h + dy - ct - 12], [x + w + dx, y + h + dy], [x + w, y + h]]), pages), INK, 0.1));
  const sp = [fill(X(poly([[x, y + h * 0.1], [x + w, y + h * 0.1], [x + w, y + h * 0.26], [x, y + h * 0.26]]), front), '#FFFFFF', 0.18),
    fill(X(poly([[x, y + h * 0.72], [x + w, y + h * 0.72], [x + w, y + h + 9], [x, y + h + 9]]), front), INK, 0.12)]; // ombre basse et reflet adoucis, bandes sans cerne : livre moins contrasté que le visage
  for (const f of o.bands || [0.14, 0.3, 0.7, 0.86]) {
    const bx = x + w * f, band = X(poly([[bx - 8, y], [bx + 8, y], [bx + 8, y + h], [bx - 8, y + h]]), front);
    sp.push(fill(band.clone(NI), o.coverD), line(curve([[bx - 4, y + 7], [bx - 4, y + h - 7]]), 3, o.gold, 0.6)); // filets d'or à 60 %
  }
  if (o.label) {
    const lx = x + w * 0.5, lb = rpoly([[lx - w * 0.09, y + h * 0.33], [lx + w * 0.09, y + h * 0.33], [lx + w * 0.09, y + h * 0.67], [lx - w * 0.09, y + h * 0.67]], 5);
    sp.push(fill(lb.clone(NI), o.labelC || o.gold), line(curve([[lx - w * 0.052, y + h * 0.46], [lx + w * 0.052, y + h * 0.46]]), 3, INK, 0.7),
      line(curve([[lx - w * 0.034, y + h * 0.56], [lx + w * 0.034, y + h * 0.56]]), 3, INK, 0.5), line(lb, 3));
  }
  const corner = (a, b, c2) => { const t = X(poly([a, b, c2]), sil); return [fill(t.clone(NI), o.gold), line(t, 3)]; };
  const cs = 30, ux = dx / Math.hypot(dx, dy), uy = dy / Math.hypot(dx, dy);
  items.push(...sp, ...corner([x + dx - 4, y + dy - 4], [x + dx + cs, y + dy], [x + dx - ux * cs * 0.8, y + dy - uy * cs * 0.8]),
    ...corner([x + w + dx + 4, y + dy - 4], [x + w + dx - cs, y + dy], [x + w + dx - ux * cs, y + dy - uy * cs]),
    ...corner([x + w + dx + 4, y + h + dy + 4], [x + w + dx + 4, y + h + dy - cs * 0.8], [x + w + dx - ux * cs, y + h + dy - uy * cs]));
  items.push(line(curve([[x + 4, y], [x + w, y], [x + w, y + h - 4]]), IN), line(curve([[x + w, y], [x + w + dx - 3, y + dy + 2]]), IN), line(pages, 3));
  items.push(...(o.extras || []), outline(sil));
  return { group: G(o.name, items), sil, top, front, side, pagesTop: [x + w + 10, y + ct + 4] };
}
// pattes : patte avant ronde avec doigts, coussinets optionnels
function pawShape(c, rx, ry, rot = 0) { const p = ell(c, [rx, ry]); p.rotate(rot, c); return p; }
function toes(c, rot = 0, n = 2, len = 14, gap = 22) {
  c = P(c); const out = [];
  for (let i = 0; i < n; i++) { const o = (i - (n - 1) / 2) * gap; const a = c.add(new Point(o, 4).rotate(rot)), b = c.add(new Point(o, 4 + len).rotate(rot)); out.push(line(curve([xy(a), xy(b)]), 4)); }
  return out;
}

// ---------- cursive d'un seul trait ----------
// Lettres en unités « hauteur d'x = 100 », ligne de base y = 0, entrée en bas à gauche (0,0), sortie à droite (w, y).
// Le mot = concaténation des points puis lissage catmull-rom : UN seul chemin, sans lever la plume (pas de points sur i,
// pas d'accents ; le « t » se barre d'une boucle). Après une lettre à sortie haute (o, v, b), la suivante saute sa montée.
const SCRIPT = {
  m: [[0, 0], [12, -60], [22, -94], [36, -100], [48, -86], [50, -48], [50, 0], [52, -60], [64, -96], [80, -100], [92, -86], [96, -48], [96, 0], [98, -60], [110, -96], [126, -100], [138, -86], [140, -48], [142, -16], [152, -2], [166, -8]],
  n: [[0, 0], [12, -60], [22, -94], [36, -100], [48, -86], [50, -48], [50, 0], [52, -60], [64, -96], [80, -100], [92, -86], [96, -48], [98, -16], [108, -2], [122, -8]],
  o: [[0, 0], [20, -10], [32, -50], [42, -84], [56, -98], [36, -84], [28, -50], [34, -12], [54, -2], [74, -18], [80, -52], [72, -86], [56, -98], [50, -86], [60, -82], [80, -86], [98, -88]],
  r: [[0, 0], [16, -60], [26, -100], [30, -112], [34, -98], [42, -92], [56, -96], [64, -94], [62, -60], [61, -18], [66, -3], [82, -8]],
  a: [[0, 0], [26, -40], [56, -84], [72, -96], [52, -100], [28, -88], [12, -50], [16, -12], [32, -2], [52, -18], [66, -58], [72, -96], [70, -50], [72, -10], [82, -2], [100, -10]],
  v: [[0, 0], [10, -60], [16, -96], [24, -60], [34, -14], [44, -2], [56, -24], [66, -70], [72, -98], [66, -86], [76, -82], [96, -86]],
  b: [[0, 0], [24, -80], [44, -160], [52, -196], [40, -202], [30, -176], [22, -110], [16, -40], [18, -6], [36, -2], [58, -24], [64, -62], [56, -90], [46, -86], [52, -78], [66, -84], [86, -88]],
  d: [[0, 0], [26, -40], [56, -84], [72, -96], [52, -100], [28, -88], [12, -50], [16, -12], [32, -2], [52, -18], [68, -70], [78, -140], [84, -196], [80, -150], [74, -80], [72, -24], [82, -2], [102, -10]],
  e: [[0, 0], [24, -20], [50, -46], [58, -76], [48, -94], [32, -88], [18, -64], [14, -30], [26, -6], [48, -2], [74, -16]],
  c: [[0, 0], [24, -52], [50, -90], [66, -98], [70, -88], [62, -84], [42, -94], [22, -70], [14, -34], [28, -6], [50, -2], [76, -16]],
  u: [[0, 0], [10, -56], [18, -96], [16, -52], [18, -14], [32, -2], [50, -14], [60, -52], [66, -96], [64, -50], [66, -12], [76, -2], [94, -8]],
  l: [[0, 0], [24, -80], [44, -160], [52, -196], [40, -202], [30, -176], [22, -110], [16, -40], [18, -6], [36, -2], [58, -10]],
  f: [[0, 0], [24, -80], [44, -160], [52, -196], [40, -202], [30, -176], [22, -110], [16, -30], [12, 40], [12, 92], [20, 104], [32, 90], [32, 50], [24, 10], [34, -4], [52, -8], [66, -14]],
};
const HIGH = { o: 1, v: 1, b: 1 };
// « mot » reprend tel quel le tracé validé en v2 (bézier), le « t » barré par sa boucle.
const SCRIPT_D = { mot: 'M0,0 C6,-50 18,-98 36,-98 C50,-98 48,-60 46,0 C46,-50 54,-98 74,-98 C94,-98 96,-60 94,0 C94,-50 102,-98 122,-98 C142,-98 144,-60 142,-22 '
  + 'C141,-2 156,4 166,-14 C174,-34 178,-82 198,-96 C184,-100 170,-80 170,-50 C170,-18 180,0 196,0 C212,0 222,-24 222,-52 C222,-80 210,-98 198,-96 '
  + 'C192,-95 194,-84 204,-84 C222,-84 240,-104 252,-120 C276,-121 300,-123 322,-122 C346,-122 334,-146 292,-172 C288,-120 282,-50 284,-16 C286,2 306,6 324,-8' };
function cursiveWord(word) {
  if (SCRIPT_D[word]) { const p = new Path({ pathData: SCRIPT_D[word], insert: false }); return { path: p, width: p.bounds.right }; }
  let x = 0, pts = [], prevHigh = false;
  for (const ch of word) {
    const L = SCRIPT[ch]; if (!L) throw new Error('lettre absente : ' + ch);
    let lp = L.map(([a, b]) => [a + x, b]);
    if (prevHigh) { let i = 0; while (i < lp.length - 1 && lp[i][1] > -70) i++; lp = lp.slice(i); }
    else if (pts.length) lp = lp.slice(1);
    pts = pts.concat(lp); x += L[L.length - 1][0]; prevHigh = !!HIGH[ch];
  }
  if (HIGH[word[word.length - 1]]) pts.splice(pts.length - 2, 2, [pts[pts.length - 3][0] + 12, pts[pts.length - 3][1] - 4]); // pas de sortie traînante en fin de mot
  return { path: curve(pts, 0.5), width: x, pts };
}
// place le mot : origine (ligne de base, début), échelle, pente d'italique
function placeWord(word, o, k, slant = -0.2) {
  const w = cursiveWord(word);
  w.path.transform(new Matrix(k, 0, slant * k, k, o[0], o[1]));
  return w;
}

// ---------- Glose assis sur son gros volume (poses « assis » et « écrit ») ----------
// o.arms : 'hug' (serre la pelote, une patte dessous, l'autre posée dessus) | 'write' (bras lointain levé tient le fil)
function seatedGlose(C, o = {}) {
  const anims = [], L = {};
  const B = book({ name: 'livre', x: 250, y: 700, w: 470, h: 150, d: [60, -46], cover: C.S ? C.S.book : C.bookCover || C.night, coverD: C.S ? C.S.bookD : C.bookCoverD || C.nightD, pages: C.parch, gold: C.gold, label: true, labelC: C.gold, bands: [0.1, 0.24, 0.76, 0.9] });
  L.book = B.group;
  L.ribbon = ribbonOrMark(C, [[754, 722], [758, 760], [760, 790]], [[752, 724], [762, 780], [770, 830], [796, 852], [836, 856]], 22);
  L.shadow = G('ombre', [floorShadow([520, 852], [340, 24], 0.32)]);
  L.seat = fill(X(ell([510, 696], [200, 22]), B.top), INK, 0.25);
  // corps en poire, légèrement tourné vers la droite (plastron décalé, flanc gauche plus visible)
  const body = blob([[340, 690], [330, 604], [366, 520], [430, 466], [508, 450], [584, 466], [636, 522], [664, 606], [652, 690], [500, 706]], 0.5);
  const bib = X(U(circ([522, 502], 70), circ([476, 548], 40), circ([566, 548], 37), circ([524, 594], 38), circ([498, 580], 30), circ([548, 580], 30)), body);
  const flank = U(taper([[330, 588], [374, 600]], 24, 6), taper([[334, 628], [382, 638]], 24, 6), taper([[346, 664], [386, 668]], 20, 6),
    taper([[664, 600], [630, 610]], 20, 6), taper([[660, 640], [626, 646]], 18, 6));
  L.body = vol('corps', body, C.fur, [fill(X(flank, body), C.stripe), shadowOf(body, 30, 30, 0.16), rimOf(body, 12, 12), ...skinExtra(C, body, 'body', [[404, 520], [612, 560], [380, 640]]),
    fill(bib.clone(NI), C.cream), shadowOf(bib, 10, 12, 0.1),
    line(curve([[370, 690], [382, 638], [420, 614]]), IN), line(curve([[632, 690], [622, 646], [594, 626]]), IN)]);
  anims.push({ id: 'corps', type: 'scale', pivot: [500, 700], values: [[1, 1], [1.015, 1.025], [1, 1]], dur: 3.8 });
  const footN = ell([424, 694], [52, 27]), footF = ell([592, 691], [44, 24]);
  L.feet = G(null, [footF, footN].map(f => { const c = f.position; return vol(null, f, C.cream, [shadowOf(f, 8, 10, 0.18), ...toes([c.x, c.y + 2], 0, 2, 13, 24)]); }));
  // queue qui enveloppe les pieds : part de derrière la hanche droite, passe devant, le bout remonte contre le pied gauche
  const tailPts = [[614, 674], [670, 690], [688, 712], [664, 730], [604, 736], [546, 728], [520, 710], [526, 690], [544, 686]];
  const tailFull = taper(tailPts, 40, 22, t => 40 - 18 * Math.pow(t, 1.3) + 3 * Math.sin(Math.PI * t));
  const tail = S(tailFull, body);
  L.tail = vol('queue', tail, C.fur, [fill(X(ringsAlong(tailPts, [0.3, 0.46, 0.62], 34, 8), tail), C.stripe), fill(X(circ([540, 688], 20), tail), C.furD),
    shadowOf(tail, 10, 12, 0.18), rimOf(tail, 6, 6)]);
  L.tailShadow = fill(X(S(moved(tailFull, 6, 10), tailFull), B.top), INK, 0.18);
  anims.push({ id: 'queue', type: 'rotate', pivot: [640, 680], values: [0, 1.6, 0, -1.2, 0], dur: 7.3 });
  // tête : 3/4 vers la droite, penchée
  const H = gloseHead(C, Object.assign({ at: [514, 350], tilt: -6, turn: 0.17 }, o.head || {}));
  anims.push(...H.anims);
  anims.push({ id: 'tete', type: 'translate', values: [[0, 0], [0, -3], [0, 0]], dur: 3.8 });
  anims.push({ id: 'tete', type: 'rotate', pivot: H.neck, values: [0, -2.5, 0, 2, 0], dur: 6.4 });
  L.head = H.group;
  // accessoires du cou
  if (C.has('echarpe')) { const sc = scarf(C, { c: [512, 478], w: 132, h: 44, sag: 24, tail: [[430, 500], [418, 560], [424, 610]] }); L.scarfBack = sc.back; L.scarf = sc.band; }
  if (C.has('noeud')) L.bow = bowTie(C, [H.chin[0] + 4, H.chin[1] + 4], -6);
  if (C.has('grelot')) { const j = jingle(C, [H.chin[0] + 4, H.chin[1] - 4]); L.jingleBand = j.band; L.jingle = j.bell; }
  if (C.has('sapin')) L.tree = paperTree(C, o.tree || [744, 676], 1.25);
  return { B, L, anims, H, body };
}
// bras : épaule -> coude -> poignet, patte ronde au bout, rayures qui suivent le membre, ombres et liseré
function arm(C, pts, w0, w1, paw, o = {}) {
  const pawS = pawShape(paw.c, paw.rx, paw.ry, paw.rot || 0);
  const a = U(taper(pts, w0, w1), pawS);
  const bands = ringsAlong(pts, o.bands || [0.38, 0.62], w0 * 0.75, 8);
  const tip = X(pawShape(paw.c, paw.rx + 6, paw.ry + 4, paw.rot || 0), a);
  const det = [fill(X(bands, S(a, pawS)), C.stripe), fill(tip.clone(NI), C.cream), shadowOf(a, 12, 12, 0.18), rimOf(a, 6, 7)];
  if (o.pads) det.push(fill(U(ell(P(paw.c).add([2, 8]), [15, 11]), circ(P(paw.c).add([-17, -10]), 7), circ(P(paw.c).add([1, -18]), 7), circ(P(paw.c).add([19, -10]), 7)), C.rose));
  else det.push(...toes(paw.c, paw.rot || 0, 3, 12, 15));
  if (o.elbow) det.push(line(curve(o.elbow), IN, INK, 0.8));
  return { group: vol(o.name || null, a, C.fur, det), shape: a };
}

// ---------- liseré « autocollant » (mode sombre) ----------
// Derrière tout le dessin, un liseré de couleur `color` qui déborde de `h` autour de chaque contour d'encre : l'union des contours
// fermés (un seul chemin par groupe animé) + une copie élargie de chaque trait ouvert qui sort de la silhouette (moustaches, brins, mot).
// Les parties animées (tête, queue, fil qui s'écrit…) ont leur liseré dans un groupe `<id>_halo` qui rejoue les mêmes animations.
// Les pièces à porte (accessoires, ruban, détails rares) restent hors du liseré du corps : il dessinerait aussi les pièces non portées
// (export Android {all:true}). Chaque accessoire et le ruban ont le leur, dans leur propre groupe (même porte, même calque Android),
// découpé hors de la silhouette du corps ; les paillettes, étoiles et éclats n'en ont pas (clairs, ils se voient déjà la nuit).
const PIECE = /^(noeud|echarpe(_dos)?|plume|marque_page|ruban|grelot(_ruban)?|sapin)$|(bonnet|beret|couronne)$/;
const GATED = /^(noeud|echarpe(_dos)?|plume|marque_page|ruban|grelot(_ruban)?|sapin|paillettes_.*|etoiles_.*)$|(bonnet|beret|couronne|eclats)$/;
// groupes que les réactions au toucher animent (android/reactions.js, REACTIONS de make_android.js) sans animation au repos :
// leur liseré doit être un groupe à part pour les suivre (svg2avd.animated() anime aussi « <nom>_halo »)
const REACTED = ['tete', 'dormeur', 'pelote', 'patte', 'queue', 'zzz'];
function halo(root, anims, color, h = 12) {
  const moving = new Set(anims.filter(a => a.attr !== 'stroke-dashoffset').map(a => a.id).concat(REACTED)), dash = new Set(anims.filter(a => a.attr === 'stroke-dashoffset').map(a => a.id));
  const ink = new Color(INK), buckets = new Map();
  const walk = (it, chain) => {
    if (!it.visible || it.clipMask || it.opacity < 0.5 || GATED.test(it.name || '')) return;
    if (it.name && moving.has(it.name)) chain = chain.concat(it.name);
    if (it.children && !(it instanceof paper.CompoundPath)) { if (!it.clipped) it.children.forEach(c => walk(c, chain)); return; }
    if (!it.strokeColor || !it.strokeColor.equals(ink) || it.strokeWidth < 4) return;
    const k = chain.join('/'); if (!buckets.has(k)) buckets.set(k, { chain, shut: [], open: [] });
    const shut = it instanceof paper.CompoundPath || it.closed;
    if (shut && it.strokeWidth < OUT * SW) return; // détail intérieur (nez, verres, filets du livre)
    buckets.get(k)[shut ? 'shut' : 'open'].push(it);
  };
  walk(root, []);
  const all = U(...[...buckets.values()].flatMap(b => b.shut.map(p => { const c = p.clone(NI); c.fillColor = 'black'; return c; })));
  const groups = new Map(), extra = [];
  // un mouvement plus petit que le liseré (respiration, petits roulis) ne le découvre pas : pas d'animateur en double (poids des AVD)
  const amp = a => Math.max(...a.values.map(v => Math.max(...[].concat(v).map(x => Math.abs(a.type === 'scale' ? x - 1 : x)))));
  const visible = a => a.attr === 'opacity' || (a.type === 'scale' ? amp(a) >= 0.05 : a.type === 'rotate' ? amp(a) >= 2 : amp(a) >= 8);
  const groupOf = chain => { // groupes « <id>_halo » emboîtés comme les groupes animés d'origine, même pivot (data : réactions Android)
    let parent = null, key = '';
    for (const id of chain) {
      key += '/' + id;
      if (!groups.has(key)) { const g = G(id + '_halo', []); g.data = Object.assign({}, (root.getItem({ name: id }) || {}).data); groups.set(key, g); (parent || root).insertChild(parent ? parent.children.length : 0, g); extra.push(...anims.filter(a => a.id === id && a.attr !== 'stroke-dashoffset' && visible(a)).map(a => Object.assign({}, a, { id: id + '_halo' }))); }
      parent = groups.get(key);
    }
    return parent;
  };
  const back = G('lisere', []); root.insertChild(0, back);
  for (const b of buckets.values()) {
    const out = [];
    if (b.shut.length) { const s = U(...b.shut.map(p => p.clone(NI))); s.fillColor = color; s.strokeColor = color; s.strokeWidth = OUT * SW + 2 * h; s.strokeJoin = 'round'; s.opacity = 1; out.push(s); }
    for (const p of b.open) {
      const n = 12, pts = [...Array(n + 1).keys()].map(i => p.getPointAt(p.length * i / n));
      if (all && pts.filter(q => !all.contains(q)).length < 2) continue; // trait intérieur : liseré caché, inutile
      const c = p.clone(NI); c.strokeColor = color; c.strokeWidth = p.strokeWidth + 2 * h; c.opacity = 1;
      // le fil qui écrit : liseré juste derrière lui, découpé hors de la silhouette ; il reste ainsi dans le calque Android du mot
      // et la partie commune à tous les mots (glose_ecrit_mot_vd) ne change pas d'un mot à l'autre
      if (p.name && dash.has(p.name)) {
        c.name = p.name + '_halo'; extra.push(...anims.filter(a => a.id === p.name).map(a => Object.assign({}, a, { id: c.name })));
        const e = c.strokeWidth / 2 + 2, box = rect(p.bounds.x - e, p.bounds.y - e, p.bounds.width + 2 * e, p.bounds.height + 2 * e);
        clipIn(X(notIn(all), box), [c]).insertBelow(p); continue; // découpe réduite au cadre du fil : quelques segments seulement
      }
      out.push(c);
    }
    if (!out.length) continue;
    const g = b.chain.length ? groupOf(b.chain) : back;
    g.addChildren(out);
  }
  for (const g of root.getItems({ match: it => it.name && PIECE.test(it.name) && it.children })) {
    const shut = g.getItems({ match: it => !(it.parent && it.parent.clipped) && it.strokeColor && it.strokeColor.equals(ink) && it.strokeWidth >= 4 && (it instanceof paper.CompoundPath || it.closed) });
    if (!shut.length) continue;
    const s = U(...shut.map(p => p.clone(NI))); s.fillColor = color; s.strokeColor = color; s.strokeWidth = OUT * SW + 2 * h; s.strokeJoin = 'round';
    const e = OUT * SW / 2 + h + 2, box = rect(s.bounds.x - e, s.bounds.y - e, s.bounds.width + 2 * e, s.bounds.height + 2 * e);
    const area = X(notIn(all), box);
    if (area.isEmpty()) continue; // piece lying entirely on the body: no outline (unclipped, it would paint cream over the fur)
    g.insertChild(0, clipIn(area, [s]));
  }
  anims.push(...extra);
  return root;
}
// ---------- contours partiels ----------
// edgeLine : ne trace que la partie du contour de `shape` située dans `area` (chemin ouvert + intersect sans trace)
function openOf(p) { return (p.children ? p.children : [p]).map(c => { const o = new Path({ segments: c.segments.map(s => s.clone()), closed: false, insert: false }); o.add(c.firstSegment.clone()); return o; }); }
function edgeLine(shape, area, w = OUT, c = INK) { return G(null, openOf(shape).map(o => line(o.intersect(area, { trace: false, insert: false }), w, c))); }
function notIn(area) { return S(rect(-200, -200, 1500, 1500), area); }
// queue qui se recroise : tronc ∪ bout en UNE forme (aucun raccord), puis seule la zone de croisement est redessinée
// par-dessus (bout + ses rayures + son contour limité au disque) → le bout passe nettement DEVANT le tronc,
// sans trait doublé ni « trou bouché ».
function crossingTail(C, name, pts, split, w0, w1, rings, cross, cr = 60) {
  const wm = (w0 + w1) / 2;
  const main = taper(pts.slice(0, split + 1), w0, wm, t => w0 - (w0 - wm) * Math.pow(t, 1.3) + 5 * Math.sin(Math.PI * t));
  const tipPts = pts.slice(split);
  const tip = taper(tipPts, wm, w1);
  const full = U(main, tip);
  const ringsM = X(ringsAlong(pts.slice(0, split + 1), rings[0], 42, 9), main), ringsT = X(ringsAlong(tipPts, rings[1], 34, 8), tip);
  const tipEnd = P(tipPts[tipPts.length - 1]), tipDark = X(circ(tipEnd, 18), tip);
  const disc = circ(cross, cr), patch = X(tip, disc);
  return { shape: full, group: G(name, [
    vol(null, full, C.fur, [fill(ringsM.clone(NI), C.stripe), fill(ringsT.clone(NI), C.stripe), fill(tipDark.clone(NI), C.furD), shadowOf(full, 14, 12, 0.18), rimOf(full, 7, 5)]),
    clipIn(patch, [fill(tip.clone(NI), C.fur), fill(ringsT, C.stripe), fill(tipDark, C.furD), shadowOf(tip, 10, 10, 0.18), rimOf(tip, 5, 5),
      fill(X(moved(tip, 6, 8), S(main, tip)), INK, 0.16)]),
    edgeLine(tip, disc)]) };
}
