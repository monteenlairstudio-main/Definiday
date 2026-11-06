// Builds screens.html (Glose SVGs inlined as <symbol>s) from screens.src.html, then renders the PNGs.
// Run: node design/styles/correspondance/render.js
const fs = require('fs'), path = require('path');
const { chromium } = require('playwright');
const here = __dirname, masc = path.join(here, '../../mascotte');

const glose = {
  assis: 'glose_assis.svg', icone: 'glose_icone.svg', livres: 'glose_livres.svg', soir: 'glose_soir.svg',
  creme: 'variantes/glose_assis_pelage_creme.svg', nuit: 'variantes/glose_assis_pelage_nuit.svg',
  moutarde: 'variantes/glose_assis_pelote_moutarde.svg',
};
// each file reuses ids like color-1 / clip-1: prefix them per symbol
const symbols = Object.entries(glose).map(([k, f]) => {
  const src = fs.readFileSync(path.join(masc, f), 'utf8');
  const vb = src.match(/viewBox="([^"]+)"/)[1];
  const inner = src.slice(src.indexOf('>', src.indexOf('<svg')) + 1, src.lastIndexOf('</svg>'))
    .replace(/<title>.*?<\/title>/s, '')
    .replace(/id="([^"]+)"/g, `id="${k}-$1"`).replace(/url\(#([^)]+)\)/g, `url(#${k}-$1)`).replace(/href="#([^"]+)"/g, `href="#${k}-$1"`);
  return `<symbol id="g-${k}" viewBox="${vb}">${inner}</symbol>`;
}).join('\n');

const html = fs.readFileSync(path.join(here, 'screens.src.html'), 'utf8')
  .replace('<!--GLOSE_SYMBOLS-->', `<svg width="0" height="0" style="position:absolute" aria-hidden="true">${symbols}</svg>`);
const out = path.join(here, 'screens.html');
fs.writeFileSync(out, html);

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 2400, height: 1200 }, deviceScaleFactor: 2 });
  await page.goto('file://' + out);
  await page.waitForSelector('body[data-ready]');
  for (const [id, name] of [['home_light', 'home_light'], ['home_dark', 'home_dark'], ['quiz', 'quiz'], ['collection', 'collection'], ['lockscreen', 'lockscreen']]) {
    const el = page.locator('#' + id);
    await el.evaluate(e => e.classList.add('flat'));
    await el.screenshot({ path: path.join(here, name + '.png') });
    await el.evaluate(e => e.classList.remove('flat'));
  }
  await page.locator('.board').screenshot({ path: path.join(here, 'board.png') });
  await browser.close();
})();
