// node crop.js in.svg out.png x,y,w,h [t] -> zoom sur une zone (vérif. des raccords de traits)
const { shot } = require('./build.js'); const { chromium } = require(process.env.PLAYWRIGHT || 'playwright'); const fs = require('fs');
(async () => { const [, , i, o, r, t = '1'] = process.argv; const b = await chromium.launch();
  await shot(b, fs.readFileSync(i, 'utf8').replace(/viewBox="[^"]*"/, `viewBox="${r.split(',').join(' ')}"`), o, 600, +t, '#F6EEDF'); await b.close(); })();
