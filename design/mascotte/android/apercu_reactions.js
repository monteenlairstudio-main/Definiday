// node android/apercu_reactions.js assis livres soir reveil ecrit_mot  -> rendus/react_<pose>.png : images de glose_<pose>_tap (ligne 1,
// toutes les 100 ms) et _special (ligne 2, toutes les 200 ms). Robolectric ne sait pas dessiner une AVD en cours (les valeurs animées
// n'atteignent pas le rendu) : ici, les clés d'image sont appliquées au VD, converti en SVG (comme vd2svg.js) et rendu dans Chromium.
const fs = require('fs'), path = require('path');
const { chromium } = require(process.env.PLAYWRIGHT || 'playwright');
const RES = path.join(__dirname, '../../../app/src/main/res'), OUT = path.join(__dirname, '../rendus'), poses = process.argv.slice(2);
fs.mkdirSync(OUT, { recursive: true });
const DRAW = path.join(RES, 'drawable');
let GID = 0; // ids de clip/dégradé uniques dans la page (les calques d'une image y sont des <svg> empilés)
const theme = {}; /<style name="Glose">(.*?)<\/style>/.exec(fs.readFileSync(path.join(RES, 'values/glose_styles.xml'), 'utf8'))[1]
  .replace(/<item name="([^"]+)">([^<]+)<\/item>/g, (_, k, v) => { theme[k] = v; });
const res = v => { while (v && v.startsWith('?attr/')) v = theme[v.slice(6)]; return v; };
const bez = (x1, y1, x2, y2) => x => { let lo = 0, hi = 1; for (let i = 0; i < 40; i++) { const t = (lo + hi) / 2, bx = 3 * (1 - t) ** 2 * t * x1 + 3 * (1 - t) * t * t * x2 + t ** 3; if (bx < x) lo = t; else hi = t; } const t = (lo + hi) / 2; return 3 * (1 - t) ** 2 * t * y1 + 3 * (1 - t) * t * t * y2 + t ** 3; };
const INTERP = { '@interpolator/glose_ease': bez(0.45, 0, 0.55, 1), '@android:interpolator/decelerate_quad': x => 1 - (1 - x) ** 2, '@android:interpolator/accelerate_quad': x => x * x };
function targets(avd) {
  const out = []; const re = /<target android:name="([^"]+)">.*?android:duration="(\d+)"[^>]*>(.*?)<\/objectAnimator>/g; let m;
  while ((m = re.exec(avd))) {
    const holders = []; m[3].replace(/propertyName="(\w+)"[^>]*>(.*?)<\/propertyValuesHolder>/g, (_, p, ks) => {
      const k = []; ks.replace(/fraction="([\d.]+)" android:value="([-\d.]+)"(?: android:interpolator="([^"]+)")?/g, (_, f, v, i) => k.push([+f, +v, i])); holders.push([p, k]); });
    out.push({ name: m[1], dur: +m[2], holders });
  }
  return out;
}
function value(k, f) {
  if (f <= k[0][0]) return k[0][1];
  for (let i = 1; i < k.length; i++) if (f <= k[i][0]) { const [f0, v0] = k[i - 1], [f1, v1, ip] = k[i]; const x = (f - f0) / (f1 - f0 || 1); return v0 + (v1 - v0) * (INTERP[ip] || (x => x))(x); }
  return k[k.length - 1][1];
}
function frame(vd, ts, ms) {
  for (const t of ts) {
    const f = Math.min(1, ms / t.dur);
    vd = vd.replace(new RegExp(`<(group|path) android:name="${t.name}"([^>]*?)(/?)>`), (all, tag, attrs, self) => {
      for (const [p, k] of t.holders) { const v = value(k, f).toFixed(3); attrs = attrs.replace(new RegExp(` android:${p}="[^"]*"`), '') + ` android:${p}="${v}"`; }
      return `<${tag} android:name="${t.name}"${attrs}${self}>`;
    });
  }
  return vd2svg(vd);
}
function vd2svg(src) { // from design/mascotte/android/vd2svg.js, + rotation, trimPathEnd, theme from glose_styles.xml
  let o = '', clips = [0], gid = (GID += 1000), defs = '', pendingGrad = null, curPath = null;
  const re = /<(\/?)([a-zA-Z:-]+)((?:\s+[^\s=]+="[^"]*")*)\s*(\/?)>/g; let m;
  const A = s => { const o = {}; s.replace(/([^\s=]+)="([^"]*)"/g, (_, k, v) => { o[k.replace('android:', '')] = v; }); return o; };
  const css = c => c && c.length === 9 ? '#' + c.slice(3) + c.slice(1, 3) : c;
  while ((m = re.exec(src))) {
    const [, close, tag, as, self] = m, a = A(as);
    if (close) { if (tag === 'group') o += '</g>'.repeat(clips.pop() + 1); if (tag === 'path' && curPath) { o += curPath(pendingGrad); curPath = null; pendingGrad = null; } continue; }
    if (tag === 'vector') { o += `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${a.viewportWidth} ${a.viewportHeight}" width="256" height="256">`; continue; }
    if (tag === 'group') {
      const tx = +a.translateX || 0, ty = +a.translateY || 0, px = +a.pivotX || 0, py = +a.pivotY || 0;
      const sx = a.scaleX != null ? +a.scaleX : 1, sy = a.scaleY != null ? +a.scaleY : 1, r = +a.rotation || 0;
      clips.push(0); o += `<g transform="translate(${tx + px} ${ty + py}) rotate(${r}) scale(${sx} ${sy}) translate(${-px} ${-py})">`; if (self) o += '</g>'.repeat(clips.pop() + 1); continue;
    }
    if (tag === 'clip-path') { const id = 'c' + gid++; defs += `<clipPath id="${id}"><path d="${a.pathData}"/></clipPath>`; o += `<g clip-path="url(#${id})">`; clips[clips.length - 1]++; continue; }
    if (tag === 'gradient') { pendingGrad = { id: 'g' + gid++, a, stops: [] }; continue; }
    if (tag === 'item' && pendingGrad) { pendingGrad.stops.push(a); continue; }
    if (tag === 'path') {
      const mk = g => { let fill = css(res(a.fillColor)) || 'none';
        if (g) { const ga = g.a, st = g.stops.map(s => `<stop offset="${s.offset}" stop-color="#${s.color.slice(3)}" stop-opacity="${parseInt(s.color.slice(1, 3), 16) / 255}"/>`).join('');
          defs += ga.type === 'radial' ? `<radialGradient id="${g.id}" gradientUnits="userSpaceOnUse" cx="${ga.centerX}" cy="${ga.centerY}" r="${ga.gradientRadius}">${st}</radialGradient>` : `<linearGradient id="${g.id}" gradientUnits="userSpaceOnUse" x1="${ga.startX}" y1="${ga.startY}" x2="${ga.endX}" y2="${ga.endY}">${st}</linearGradient>`; fill = `url(#${g.id})`; }
        return `<path d="${a.pathData}" fill="${fill}" fill-opacity="${a.fillAlpha || 1}" stroke="${css(res(a.strokeColor)) || 'none'}" stroke-width="${a.strokeWidth || 0}" stroke-opacity="${a.strokeAlpha || 1}" stroke-linecap="${a.strokeLineCap || 'butt'}" stroke-linejoin="${a.strokeLineJoin || 'miter'}"${a.fillType === 'evenOdd' ? ' fill-rule="evenodd"' : ''}${a.trimPathEnd != null ? ` data-trim="${a.trimPathEnd}"` : ''}/>`; };
      if (self) o += mk(null); else curPath = mk; continue;
    }
  }
  return o.replace(/(<svg[^>]*>)/, `$1<defs>${defs}</defs>`) + '</g>'.repeat(clips.pop()) + '</svg>';
}
(async () => {
  const b = await chromium.launch(), page = await b.newPage({ viewport: { width: 10 * 256, height: 512 } });
  for (const pose of poses) {
    const rows = [['tap', 6, 100], ['special', 10, 200]].map(([k, n, step]) => {
      // body layers of the reaction (GloseLayers.kt), each an AVD (its VD + targets) or a still VD, stacked in a 256 px cell
      const kt = fs.readFileSync(path.join(RES, '../java/fr/definiday/GloseLayers.kt'), 'utf8');
      const list = new RegExp(`R\\.drawable\\.glose_${pose}_${k} to listOf\\((.*?)\\),\\n`).exec(kt)[1];
      const layers = [...list.matchAll(/"" to R\.drawable\.(\w+)/g)].map(([, n]) => {
        const x = fs.readFileSync(path.join(DRAW, `${n}.xml`), 'utf8'), a = /android:drawable="@drawable\/(\w+)"/.exec(x);
        return a ? { vd: fs.readFileSync(path.join(DRAW, `${a[1]}.xml`), 'utf8'), ts: targets(x) } : { vd: x, ts: [] };
      });
      return Array.from({ length: n }, (_, i) => `<span style="display:inline-block;position:relative;width:256px;height:256px">${
        layers.map(l => frame(l.vd, l.ts, i * step).replace('<svg ', '<svg style="position:absolute;left:0;top:0" ')).join('')}</span>`).join('');
    });
    await page.setContent(`<body style="margin:0;background:#F6EEDF;line-height:0">${rows.map(r => `<div>${r}</div>`).join('')}</body>`);
    await page.evaluate(() => document.querySelectorAll('path[data-trim]').forEach(p => { const L = p.getTotalLength(); p.style.strokeDasharray = `${L} ${L}`; p.style.strokeDashoffset = L * (1 - +p.dataset.trim); }));
    await page.screenshot({ path: path.join(OUT, `react_${pose}.png`) });
  }
  await b.close();
})();
