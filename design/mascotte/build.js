// Chaîne de production Monsieur Glose.
//   node build.js <pose> <out.svg> [variante JSON] [prefixe_png] [t]      (un rendu)
//   node build.js --jobs jobs.json                                         (lot : [{pose,out,variant,png,t,bg}])
// glose_lib.js + pose_<pose>.js s'exécutent dans Chromium (Paper.js), export SVG, injection SMIL
// (animateTransform translate/rotate/scale autour d'un pivot, animate d'attribut : opacity, stroke-dashoffset),
// puis rendus PNG 1024 et 128 de l'image figée à t secondes.
const { chromium } = require('/opt/node-tools/node_modules/playwright');
const fs = require('fs'), path = require('path');
const D = __dirname;
const PAPER = path.join(D, 'kit/node_modules/paper/dist/paper-full.min.js');

async function buildOne(b, job) {
  const lib = fs.readFileSync(path.join(D, 'glose_lib.js'), 'utf8');
  const pose = fs.readFileSync(path.join(D, `pose_${job.pose}.js`), 'utf8');
  const code = `var VARIANT=${JSON.stringify(job.variant || {})};\n${lib}\n${pose}`;
  const p = await b.newPage();
  let err = null; p.on('pageerror', e => { err = e; });
  await p.setContent('<canvas id="c" width="1024" height="1024"></canvas>');
  await p.addScriptTag({ path: PAPER });
  const svg = await p.evaluate((code) => {
    paper.setup(document.getElementById('c'));
    const res = new Function('paper', code + '\n;return draw(paper);')(paper) || {};
    const root = paper.project.exportSVG({ precision: 1 });
    const NS = 'http://www.w3.org/2000/svg', mk = t => document.createElementNS(NS, t);
    const ease = '0.45 0 0.55 1';
    for (const a of res.anims || []) {
      const el = root.querySelector('#' + a.id);
      if (!el) throw new Error('groupe absent : ' + a.id);
      const n = a.values.length;
      const kt = a.keyTimes || a.values.map((_, i) => +(i / (n - 1)).toFixed(4));
      const timing = { dur: a.dur + 's', begin: (a.begin || 0) + 's', repeatCount: 'indefinite', keyTimes: kt.join(';') };
      if (a.linear) timing.calcMode = 'linear'; else Object.assign(timing, { calcMode: 'spline', keySplines: Array(n - 1).fill(a.spline || ease).join(';') });
      if (a.attr) { // animation d'attribut (opacité, tracé du fil)
        const an = mk('animate');
        Object.entries(Object.assign({ attributeName: a.attr, values: a.values.join(';') }, timing)).forEach(([k, v]) => an.setAttribute(k, v));
        if (a.attr === 'stroke-dashoffset' && a.initial != null) el.setAttribute('stroke-dashoffset', a.initial);
        el.appendChild(an); continue;
      }
      const inner = mk('g'); while (el.firstChild) inner.appendChild(el.firstChild);
      const ag = mk('g'); const [px, py] = a.pivot || [0, 0];
      if (a.type === 'scale') {
        const o = mk('g'); o.setAttribute('transform', `translate(${px} ${py})`);
        const i2 = mk('g'); i2.setAttribute('transform', `translate(${-px} ${-py})`);
        i2.appendChild(inner); ag.appendChild(i2); o.appendChild(ag); el.appendChild(o);
      } else { ag.appendChild(inner); el.appendChild(ag); }
      const vals = a.values.map(v => a.type === 'rotate' ? `${v} ${px} ${py}` : [].concat(v).join(' '));
      if (a.base) ag.setAttribute('transform', a.base);
      const at = mk('animateTransform');
      Object.entries(Object.assign({ attributeName: 'transform', type: a.type, values: vals.join(';') }, timing)).forEach(([k, v]) => at.setAttribute(k, v));
      ag.appendChild(at);
    }
    root.removeAttribute('width'); root.removeAttribute('height');
    root.setAttribute('viewBox', (res.viewBox || [0, 0, 1024, 1024]).join(' '));
    root.setAttribute('role', 'img');
    if (res.title) { const t = mk('title'); t.textContent = res.title; root.insertBefore(t, root.firstChild); root.setAttribute('aria-label', res.title); }
    return new XMLSerializer().serializeToString(root);
  }, code).catch(e => { throw new Error(`${job.out}: ${e.message}`); });
  await p.close();
  if (err) throw new Error(`${job.out}: ${err}`);
  fs.mkdirSync(path.dirname(path.join(D, job.out)), { recursive: true });
  fs.writeFileSync(path.join(D, job.out), svg);
  if (job.png) for (const w of job.sizes || [1024, 128]) await shot(b, svg, path.join(D, `${job.png}_${w}.png`), w, job.t || 0, job.bg || '#F6EEDF');
  console.log('ok', job.out, (svg.length / 1024).toFixed(0) + ' Ko');
  return svg;
}
async function shot(b, svg, file, w, t, bg) {
  const q = await b.newPage({ viewport: { width: w, height: w } });
  await q.setContent(`<body style="margin:0;background:${bg}">${svg.replace('<svg', `<svg width="${w}" height="${w}"`)}</body>`);
  await q.evaluate(t => { const s = document.querySelector('svg'); s.pauseAnimations(); s.setCurrentTime(+t); }, t);
  await q.waitForTimeout(80);
  fs.mkdirSync(path.dirname(file), { recursive: true });
  await q.screenshot({ path: file, omitBackground: bg === 'transparent' }); await q.close();
}
module.exports = { buildOne, shot };
if (require.main === module) (async () => {
  const a = process.argv.slice(2);
  const jobs = a[0] === '--jobs' ? JSON.parse(fs.readFileSync(a[1], 'utf8'))
    : [{ pose: a[0], out: a[1], variant: a[2] ? JSON.parse(a[2]) : {}, png: a[3], t: +(a[4] || 0) }];
  const b = await chromium.launch();
  try { for (const j of jobs) await buildOne(b, j); } finally { await b.close(); }
})().catch(e => { console.error(e.message); process.exit(1); });
