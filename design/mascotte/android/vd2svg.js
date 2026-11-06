// node vd2svg.js vd.xml out.svg [attr=valeur ...] : relit un VectorDrawable produit (contrôle visuel de la conversion,
// thème résolu comme le ferait Resources.Theme). Groupes : translate puis rotate/scale autour du pivot, comme VectorDrawable.
const fs = require('fs'); const [, , inp, out, ...kv] = process.argv;
const theme = { gloseFur: '#A4ACBC', gloseFurD: '#788196', gloseCream: '#F8EEDD', gloseBall: '#9A4250', gloseBallD: '#5A2230', gloseAccNoeud: '0', gloseAccEcharpe: '0', gloseAccBonnet: '0', gloseAccPlume: '0', gloseAccMarque: '0' };
kv.forEach(s => { const [k, v] = s.split('='); theme[k] = v; });
const src = fs.readFileSync(inp, 'utf8'); const res = (v) => v && v.startsWith('?attr/') ? theme[v.slice(6)] : v;
let out_ = '', gid = 0, defs = '';
const re = /<(\/?)([a-zA-Z:-]+)((?:\s+[^\s=]+="[^"]*")*)\s*(\/?)>/g; let m; const stack = [];
const A = s => { const o = {}; s.replace(/([^\s=]+)="([^"]*)"/g, (_, k, v) => { o[k.replace('android:', '')] = v; }); return o; };
let pendingGrad = null, curPath = null;
while ((m = re.exec(src))) {
  const [, close, tag, as, self] = m, a = A(as);
  if (close) { if (tag === 'group') out_ += '</g>'; if (tag === 'path' && curPath) { out_ += curPath(pendingGrad); curPath = null; pendingGrad = null; } continue; }
  if (tag === 'vector') { out_ += `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${a.viewportWidth} ${a.viewportHeight}">`; continue; }
  if (tag === 'group') {
    const tx = +res(a.translateX) || 0, ty = +res(a.translateY) || 0, px = +a.pivotX || 0, py = +a.pivotY || 0;
    const sx = a.scaleX != null ? +res(a.scaleX) : 1, sy = a.scaleY != null ? +res(a.scaleY) : 1, r = +a.rotation || 0;
    out_ += `<g transform="translate(${tx + px} ${ty + py}) rotate(${r}) scale(${sx} ${sy}) translate(${-px} ${-py})">`; if (self) out_ += '</g>'; continue;
  }
  if (tag === 'clip-path') { const id = 'c' + gid++; defs += `<clipPath id="${id}"><path d="${a.pathData}"/></clipPath>`; out_ += `<g clip-path="url(#${id})">`; stack.push(1); continue; }
  if (tag === 'gradient') { const id = 'g' + gid++; pendingGrad = { id, a, stops: [] }; continue; }
  if (tag === 'item' && pendingGrad) { pendingGrad.stops.push(a); continue; }
  if (tag === 'path') {
    const mk = (g) => { let fillv = res(a.fillColor) || 'none';
      if (g) { const ga = g.a, st = g.stops.map(s => `<stop offset="${s.offset}" stop-color="#${s.color.slice(3)}" stop-opacity="${parseInt(s.color.slice(1, 3), 16) / 255}"/>`).join('');
        defs += ga.type === 'radial' ? `<radialGradient id="${g.id}" gradientUnits="userSpaceOnUse" cx="${ga.centerX}" cy="${ga.centerY}" r="${ga.gradientRadius}">${st}</radialGradient>` : `<linearGradient id="${g.id}" gradientUnits="userSpaceOnUse" x1="${ga.startX}" y1="${ga.startY}" x2="${ga.endX}" y2="${ga.endY}">${st}</linearGradient>`; fillv = `url(#${g.id})`; }
      return `<path d="${a.pathData}" fill="${fillv}" fill-opacity="${a.fillAlpha || 1}" stroke="${res(a.strokeColor) || 'none'}" stroke-width="${a.strokeWidth || 0}" stroke-opacity="${a.strokeAlpha || 1}" stroke-linecap="${a.strokeLineCap || 'butt'}" stroke-linejoin="${a.strokeLineJoin || 'miter'}"${a.fillType === 'evenOdd' ? ' fill-rule="evenodd"' : ''}/>`; };
    if (self) out_ += mk(null); else curPath = mk; continue;
  }
}
// les clip-path VectorDrawable s'appliquent jusqu'à la fin du groupe : fermeture approximative en fin de groupe parent
out_ = out_.replace(/<\/svg>$/, '');
let depth = 0; const fixed = out_.replace(/<g clip-path[^>]*>|<g [^>]*>|<\/g>/g, t => t); // (contrôle visuel suffisant)
fs.writeFileSync(out, fixed.replace('<svg', '<svg').replace(/(<svg[^>]*>)/, `$1<defs>${defs}</defs>`) + '</g>'.repeat((fixed.match(/<g clip-path/g) || []).length) + '</svg>');
