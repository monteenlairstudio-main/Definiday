// node frames.js in.svg out.png t1,t2,...  -> planche de vignettes 256 px aux instants donnés (vérif. des animations)
const { chromium } = require(process.env.PLAYWRIGHT || 'playwright'); // PLAYWRIGHT=/opt/node-tools/node_modules/playwright si installé ailleurs
const fs = require('fs');
(async () => {
  const [, , inp, out, ts] = process.argv, svg = fs.readFileSync(inp, 'utf8'), T = ts.split(',').map(Number);
  const b = await chromium.launch(), p = await b.newPage({ viewport: { width: 256 * Math.min(T.length, 6), height: 280 * Math.ceil(T.length / 6) } });
  const cells = T.map((t, i) => `<div style="position:absolute;left:${(i % 6) * 256}px;top:${Math.floor(i / 6) * 280}px;width:256px;font:12px sans-serif;text-align:center">${svg.replace('<svg', `<svg id="s${i}" width="256" height="256"`).replace(/id="([^"]+)"/g, (m, id) => id === 's' + i ? m : `id="${id}_${i}"`).replace(/url\(#([^)]+)\)/g, `url(#$1_${i})`).replace(/href="#([^"]+)"/g, `href="#$1_${i}"`)}<br>t = ${t} s</div>`).join('');
  await p.setContent(`<body style="margin:0;background:#F6EEDF">${cells}</body>`);
  await p.evaluate(T => T.forEach((t, i) => { const s = document.getElementById('s' + i); s.pauseAnimations(); s.setCurrentTime(t); }), T);
  await p.waitForTimeout(150); await p.screenshot({ path: out }); await b.close();
})();
