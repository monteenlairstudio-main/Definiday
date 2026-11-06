// Engraved (taille-douce) motifs as SVG strings. Colours come from CSS classes (.g-*), so light and dark share the drawing.
const f = n => +n.toFixed(1);

// Oak leaf, base at (0,0), tip at (0,-L). Returns outline, midrib, veins and edge hatching.
function oakLeaf(L) {
  const W = 0.30 * L, N = 120, side = (ph, dir) => {
    const pts = [];
    for (let i = 0; i <= N; i++) {
      const t = i / N, env = W * Math.pow(Math.sin(Math.PI * Math.pow(t, 0.8)), 0.85);
      const lobe = 0.5 + 0.5 * Math.pow(Math.abs(Math.sin(Math.PI * (4.5 * t + ph))), 0.55);
      pts.push([dir * env * lobe, -t * L]);
    }
    return pts;
  };
  const R = side(0, 1), Lf = side(0.18, -1);
  const outline = 'M' + R.map(p => f(p[0]) + ',' + f(p[1])).join('L') + 'L' + Lf.reverse().map(p => f(p[0]) + ',' + f(p[1])).join('L') + 'Z';
  Lf.reverse();
  let veins = '';
  for (let k = 0; k < 4; k++) {
    for (const [pts, ph] of [[R, 0], [Lf, 0.18]]) {
      const t = (k + 0.5 - ph) / 4.5; if (t <= 0.05 || t >= 0.95) continue;
      const p = pts[Math.round(t * N)];
      veins += `M0,${f(-(t - 0.07) * L)}Q${f(p[0] * 0.45)},${f(-(t - 0.03) * L)} ${f(p[0] * 0.86)},${f(p[1])}`;
    }
  }
  let edge = '';
  for (let i = 4; i < N - 3; i += 2) { const p = R[i]; edge += `M${f(p[0])},${f(p[1])}L${f(p[0] * 0.8)},${f(p[1] + 2)}`; }
  for (let i = 4; i < N - 3; i += 3) { const p = Lf[i]; edge += `M${f(p[0])},${f(p[1])}L${f(p[0] * 0.88)},${f(p[1] + 1.5)}`; }
  return { outline, midrib: `M0,${f(0.16 * L)}Q1.5,${f(-0.3 * L)} 0,${f(-0.97 * L)}`, veins, edge };
}

// parallel hatch lines clipped by a clipPath id (hatching = the engraved shade)
function hatch(clip, x0, y0, w, h, gap, ang, cls) {
  let d = '';
  const c = Math.cos(ang), s = Math.sin(ang), D = Math.hypot(w, h);
  for (let k = -D; k < D; k += gap) {
    const cx = x0 + w / 2 + k * -s, cy = y0 + h / 2 + k * c;
    d += `M${f(cx - c * D)},${f(cy - s * D)}L${f(cx + c * D)},${f(cy + s * D)}`;
  }
  return `<path class="${cls}" clip-path="url(#${clip})" d="${d}"/>`;
}

let uid = 0;
// Sprig: oak leaf + acorn, engraved. size = leaf length in user units.
function sprig({ L = 120, rot = -18, x = 0, y = 0, cls = '' } = {}) {
  const id = 'lf' + (uid++), o = oakLeaf(L);
  const acorn = (ax, ay, r) => {
    const cid = id + 'c' + ax;
    return `<g transform="translate(${ax},${ay}) rotate(24)">
      <path class="g-ln" d="M0,${-r * 0.2}c${r * 0.9},0 ${r * 0.95},${r * 1.1} 0,${r * 1.75}c${-r * 0.95},${-r * 0.65} ${-r * 0.9},${-r * 1.75} 0,${-r * 1.75}z"/>
      <path class="g-fn" d="M${-r * 0.35},${r * 0.2}q${r * 0.1},${r * 0.7} ${r * 0.35},${r * 1.2}M${-r * 0.55},${r * 0.1}q${r * 0.05},${r * 0.5} ${r * 0.25},${r * 1}"/>
      <clipPath id="${cid}"><path d="M${-r * 0.8},0a${r * 0.8},${r * 0.62} 0 0 1 ${r * 1.6},0z"/></clipPath>
      <path class="g-ln" d="M${-r * 0.8},0a${r * 0.8},${r * 0.62} 0 0 1 ${r * 1.6},0z"/>
      ${hatch(cid, -r, -r, 2 * r, r, 2.1, Math.PI / 4, 'g-fn')}${hatch(cid, -r, -r, 2 * r, r, 2.1, -Math.PI / 4, 'g-fn')}
      <path class="g-ln" d="M0,${-r * 0.6}q1,-4 3,-6"/></g>`;
  };
  return `<g class="${cls}" transform="translate(${x},${y}) rotate(${rot})">
    <defs><clipPath id="${id}x"><path d="${o.outline}"/></clipPath><clipPath id="${id}s"><path d="M-200,-400H0V200H-200Z"/></clipPath></defs>
    <g clip-path="url(#${id}s)">${hatch(id + 'x', -L, -L, L * 2, L * 1.2, 2.4, Math.PI * 0.2, 'g-fn')}</g>
    ${hatch(id + 'x', -L, -L, L * 2, L * 1.2, 7, Math.PI * 0.2, 'g-fn')}
    <path class="g-ln" d="${o.outline}"/>
    <path class="g-ln" d="${o.midrib}"/><path class="g-fn" d="${o.veins}"/><path class="g-fn" d="${o.edge}"/>
    ${acorn(16, -6, 9)}
  </g>`;
}

// photo corner (coin de montage), placed at a corner: 'tl','tr','bl','br'
function corner(pos, s = 14) {
  const r = { tl: 0, tr: 90, br: 180, bl: 270 }[pos];
  return `<svg class="coin coin-${pos}" width="${s}" height="${s}" viewBox="0 0 16 16" aria-hidden="true"><g transform="rotate(${r} 8 8)">
    <path class="g-kraft" d="M0,0H16L0,16Z"/><path class="g-fn" d="M3,10.5L10.5,3M0,0H16L0,16Z"/></g></svg>`;
}

// pin head (épingle) on a label
const pin = (c = 'g-pin') => `<svg class="epingle" width="14" height="18" viewBox="0 0 14 18" aria-hidden="true">
  <path class="g-ombre" d="M8,9L12,17" /><path class="g-acier" d="M7,7L9.5,15"/>
  <circle class="${c}" cx="7" cy="6" r="4.6"/><circle fill="#fff" opacity=".55" cx="5.6" cy="4.6" r="1.3"/></svg>`;

// herbarium stamp (tampon) with a line-drawn cat head in round glasses
function stamp(r = 22) {
  const id = 'st' + (uid++);
  return `<svg class="tampon" width="${2 * r + 4}" height="${2 * r + 4}" viewBox="${-r - 2} ${-r - 2} ${2 * r + 4} ${2 * r + 4}" aria-hidden="true">
    <defs><path id="${id}" d="M${-(r - 6)},0a${r - 6},${r - 6} 0 1 1 ${2 * (r - 6)},0a${r - 6},${r - 6} 0 1 1 ${-2 * (r - 6)},0"/></defs>
    <g class="g-tampon"><circle r="${r}"/><circle r="${r - 2.2}" stroke-width=".5"/><circle r="${r - 10}" stroke-width=".5"/>
    <text font-size="5.3" letter-spacing=".6"><textPath href="#${id}" startOffset="2%">HERBIER DE M. GLOSE · </textPath></text>
    <path d="M-7,3c0,-5 2,-7 7,-7s7,2 7,7c0,3 -3,4.5 -7,4.5s-7,-1.5 -7,-4.5zM-6.5,-1.5l-.5,-5 4,2.6M6.5,-1.5l.5,-5 -4,2.6" stroke-width=".9"/>
    <circle cx="-2.8" cy="1.6" r="2" stroke-width=".7"/><circle cx="2.8" cy="1.6" r="2" stroke-width=".7"/><path d="M-.8,1.6h1.6" stroke-width=".6"/></g></svg>`;
}

// 24dp engraved line icons: outline 1.5 + 2-4 hatch strokes (0.75) on the shadow side
const ICONS = {
  cabinet: '<path class="i-ln" d="M4.5,3.5h15v17h-15zM4.5,9.2h15M4.5,14.8h15"/><path class="i-ln" d="M10.5,6.3h3M10.5,12h3M10.5,17.6h3"/><path class="i-ht" d="M17,4.5v4M18.3,4.5v4M17,10.2v4M18.3,10.2v4"/>',
  reglages: '<path class="i-ln" d="M4,7h16M4,12h16M4,17h16"/><circle class="i-bg" cx="9" cy="7" r="2.2"/><circle class="i-bg" cx="15" cy="12" r="2.2"/><circle class="i-bg" cx="8" cy="17" r="2.2"/><path class="i-ht" d="M9.6,6.2l-1.2,1.6M15.6,11.2l-1.2,1.6M8.6,16.2l-1.2,1.6"/>',
  academie: '<path class="i-ln" d="M3.5,8.5L12,3.5L20.5,8.5ZM5,20.5h14M4,18h16M6.5,10.5v7M10.2,10.5v7M13.8,10.5v7M17.5,10.5v7"/><path class="i-ht" d="M14,6.3l2,1.4M12,6.6l1.6,1.2"/>',
  livre: '<path class="i-ln" d="M12,6.5c-2.5,-2 -5.5,-2.3 -8.5,-1.5v13.5c3,-.8 6,-.5 8.5,1.5c2.5,-2 5.5,-2.3 8.5,-1.5v-13.5c-3,-.8 -6,-.5 -8.5,1.5zM12,6.5v13.5"/><path class="i-ht" d="M14.5,9l4,-1M14.5,11.5l4,-1M14.5,14l4,-1"/>',
  lettre: '<path class="i-ln" d="M3.5,6.5h17v11h-17zM3.5,6.5l8.5,6.5 8.5,-6.5"/><circle class="i-sceau" cx="12" cy="14.2" r="2.6"/><path class="i-ht" d="M15,15.5l4,0M14.5,16.6h5"/>',
  hasard: '<path class="i-ln" d="M5,5h14v14h-14z"/><circle class="i-pt" cx="8.6" cy="8.6" r="1.25"/><circle class="i-pt" cx="12" cy="12" r="1.25"/><circle class="i-pt" cx="15.4" cy="15.4" r="1.25"/><path class="i-ht" d="M19,7.5l1.5,-1.5v14h-14l1.5,-1.5"/>',
  epingle: '<path class="i-ln" d="M12,13.5v7"/><circle class="i-sceau" cx="12" cy="8.5" r="5"/><circle fill="#fff" opacity=".6" cx="10.3" cy="6.8" r="1.4"/>',
  loupe: '<circle class="i-ln" cx="10" cy="10" r="6"/><path class="i-ln" d="M14.5,14.5l5.5,5.5" stroke-width="2.6"/><path class="i-ht" d="M7,8.5a3.5,3.5 0 0 1 3,-2.5"/>',
};
const icon = (n, s = 24) => `<svg class="ic" width="${s}" height="${s}" viewBox="0 0 24 24" aria-hidden="true">${ICONS[n]}</svg>`;

module.exports = { sprig, corner, pin, stamp, icon, hatch, oakLeaf };
