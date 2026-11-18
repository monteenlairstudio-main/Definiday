// MONSIEUR GLOSE — vie au repos (lue par build.js après glose_lib.js, avant la pose).
// Une « piste » anime une partie sur toute la vie au repos (LIFE ms = Glose.IDLE_MS) : { id, type, pivot, life: [[ms, valeur, accent], …] }.
// Elle part du dessin fixe et y revient avant LIFE (l'arrêt tombe sur un repos), à des temps irréguliers tirés d'un aléa à graine
// (le même dessin à chaque génération). svg2avd la coupe à ses repos en animateurs courts (startOffset) : chacun garde un
// échantillon par image sur le RenderThread, et rien n'est écrit entre deux gestes. Les coupes tombent sur la valeur du fixe : les
// fonctions ci-dessous y passent régulièrement (clé de passage) quand le geste est long (balancement, vapeur, flamme). Accent : 'out' départ vif d'un repos,
// 'in' arrivée vive sur un repos (svg2avd.hermite) ; sinon vitesse nulle aux extrêmes, continue aux passages.
// Les périodes de fond n'ont pas de facteur commun et varient d'un cycle à l'autre : rien ne se répète pendant la vie.
const LIFE = 26000, YAWN = 14200; // bâillement de gloseHead (les clignements l'évitent)
// sortie : tout est revenu au repos à END ; les 2 dernières secondes, il se pose (l'arrêt à 26 s ne coupe rien, les calques ne se redessinent plus)
const END = LIFE - 2000;
function seeded(s) { return () => { s = s + 0x6D2B79F5 | 0; let t = Math.imul(s ^ s >>> 15, 1 | s); t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t; return ((t ^ t >>> 14) >>> 0) / 4294967296; }; }
const between = (r, [a, b]) => a + (b - a) * r();
const ms = k => k.map(([t, ...v]) => [Math.round(t), ...v]);
// respiration : k = 0 au repos, ~1 au plus haut de l'inspiration (plus courte que l'expiration), petite pause après certaines ;
// un soupir de temps en temps (plus ample, plus long). o.at : premier souffle ; o.end : dernier repos.
function breaths(seed, o) {
  const r = seeded(seed), out = [[0, 0]], end = o.end || END; let t = o.at || 0;
  if (t) out.push([t, 0]);
  for (;;) {
    const sigh = r() < (o.sigh || 0.15), P = between(r, o.period) * (sigh ? 1.3 : 1), k = sigh ? 1.3 : between(r, [0.65, 1.2]);
    if (t + P > end) break;
    out.push([t + P * (o.inhale || 0.42), k], [t + P, 0]); t += P;
    const rest = between(r, o.pause || [100, 700]); if (rest > 60) { t += rest; out.push([t, 0]); }
  }
  return ms(out.concat([[LIFE, 0]]));
}
// k (0..1) -> valeur : [a, b] interpolé (échelles, translations), pour suivre le même souffle avec un léger retard (lag ms)
const along = (keys, a, b, lag = 0) => keys.map(([t, k, f], i) => [i && i < keys.length - 1 ? t + lag : t, Array.isArray(a) ? a.map((x, j) => +(x + (b[j] - x) * k).toFixed(4)) : +(a + (b - a) * k).toFixed(4), f]);
// balancement : extrêmes alternés d'amplitude et de demi-période irrégulières, parfois tenus ; part du repos (0) et y revient,
// et passe par 0 (clé de passage, vitesse continue) entre deux extrêmes
function sway(seed, o) {
  const r = seeded(seed), end = o.end || END; let t = o.at || 0, s = o.sign || (r() < 0.5 ? 1 : -1);
  const out = [[0, 0], [t, 0]];
  for (;;) {
    const h = between(r, o.half);
    if (t + h + o.half[0] > end) break;
    const a = s * between(r, o.amp), [t0, a0] = out[out.length - 1];
    if (a0) out.push([t0 + h * Math.abs(a0) / (Math.abs(a0) + Math.abs(a)), 0]); // crosses 0 where a straight swing would
    t += h; out.push([t, a]); s = -s;
    if (r() < (o.hold || 0)) { t += between(r, [300, 900]); out.push([t, out[out.length - 1][1]]); }
  }
  out.push([Math.min(end, t + o.half[0]), 0], [LIFE, 0]);
  return ms(out);
}
// geste bref à intervalles irréguliers : shape(r) = [[dt, valeur, accent], …] depuis le repos (0) et retour à 0 ; pas pendant avoid
function gestures(seed, o, shape) {
  const r = seeded(seed), out = [[0, 0]], end = o.end || END - 500; let t = o.at || between(r, o.gap);
  while (t < end) {
    if (!(o.avoid || []).some(([a, b]) => t > a - 500 && t < b + 300)) {
      const g = shape(r); out.push([t, 0]); let u = t;
      for (const [dt, v, f] of g) { u += dt; out.push([u, v, f]); }
      t = u;
    }
    t += between(r, o.gap);
  }
  return ms(out.concat([[LIFE, 0]]));
}
// clignement (0 ouvert, 1 fermé) : fermeture 60–72 ms en accélérant, ouverture 90–110 ms en décélérant (150–180 ms), parfois double
const blinks = (seed, o = {}) => gestures(seed, Object.assign({ gap: [2200, 6800] }, o), r => {
  const one = () => [[between(r, [60, 72]), 1, 'in'], [between(r, [90, 110]), 0, 'out']];
  return r() < (o.double ?? 0.3) ? [...one(), [between(r, [110, 150]), 0], ...one()] : one();
});
// petit coup (queue, moustaches) : léger contre-temps, départ vif, dépassement amorti, repos
const flick = (amp, speed = 1) => r => { const a = amp * between(r, [0.7, 1.1]) * (r() < 0.5 ? 1 : -1);
  return [[90 * speed, -0.25 * a], [110 * speed, a, 'out'], [170 * speed, -0.35 * a], [200 * speed, 0.1 * a], [220 * speed, 0]]; };
// spans [début, fin] où une piste de gestes (gestures, blinks) quitte son repos 0
function spans(keys) {
  const out = [];
  keys.forEach(([t, v], i) => { const n = keys[i + 1];
    if (v === 0 && n && n[1] !== 0) out.push([t, t]); else if (out.length && v === 0 && out[out.length - 1][1] === out[out.length - 1][0]) out[out.length - 1][1] = t; });
  return out;
}
// reflet des lunettes : passe d'un bord à l'autre (caché hors des verres aux deux bouts), puis repasse dans l'autre sens 1,2 à 3,2 s
// plus tard (il finit où il commence). o.avoid : spans où il ne passe pas (clignements, bâillement : l'œil n'a qu'un événement à la
// fois), 150 ms de marge ; un passage qui en croiserait un attend sa fin.
function glints(seed, o) {
  const r = seeded(seed), out = [[0, o.from]], M = 150; let t = o.at || between(r, o.gap);
  const free = (a, d) => { for (const [x, y] of o.avoid || []) if (a < y + M && a + d > x - M) return free(y + M, d); return a; };
  while (t + 4500 < END) {
    const d1 = between(r, [480, 640]), t1 = free(t, d1), back = between(r, [1200, 3200]), d2 = between(r, [480, 640]), t2 = free(t1 + d1 + back, d2);
    if (t2 + d2 > END - 500) break; // both passes or none
    out.push([t1, o.from], [t1 + d1, -o.from], [t2, -o.from], [t2 + d2, o.from]);
    t = t2 + d2 + between(r, o.gap);
  }
  return ms(out.concat([[LIFE, o.from]]));
}
// petites valeurs au hasard autour de 1 (flamme, lueur) : extrêmes alternés tous les step ms, parfois tenus (o.hold), départ et retour à 1
function flicker(seed, o) {
  const r = seeded(seed), out = [[0, 1]]; let t = 0, s = 1, cut = 0;
  while (t < END - 2 * o.step[1]) { // back through 1 about every 3,5 s (a cut for svg2avd)
    const dt = between(r, o.step);
    if (t - cut > 3500) { out.push([t + dt / 2, 1]); cut = t; }
    t += dt; out.push([t, 1 + s * between(r, o.amp)]); s = -s;
    if (r() < (o.hold || 0)) { t += between(r, [400, 1200]); out.push([t, out[out.length - 1][1]]); } // steady a moment (nothing redrawn)
  }
  return ms(out.concat([[END, 1], [LIFE, 1]]));
}
const scaleOf = (keys, ax, ay) => keys.map(([t, v, f]) => [t, [+(1 + (v - 1) * ax).toFixed(4), +(1 + (v - 1) * ay).toFixed(4)], f]);
// roulis de la tête des poses éveillées : demi-périodes de 2,6 à 3,8 s, ±1,4 à 2,6°, parfois un temps d'arrêt (il écoute)
const headSway = () => sway(17, { half: [2600, 3800], amp: [1.4, 2.6], hold: 0.35, at: 700 });
// particules (vapeur, « z ») : chaque voie part de sa place du dessin fixe (translation 0, opacité du fixe) et finit sa montée, puis
// repart d'en bas (saut caché : opacité 0, 40 ms) à chaque émission : o.at (temps donnés) ou après un intervalle o.gap ; la dernière
// émission s'arrête sur la place du fixe et y reste. Montée from -> to (translations), opacité 0 -> peak -> 0 (o.fade : fractions).
function particles(seed, o) {
  const r = seeded(seed), [fi, fo] = o.fade || [0.3, 0.6], [fx, fy] = o.from, [tx, ty] = o.to;
  const p0 = -(fx * (tx - fx) + fy * (ty - fy)) / ((tx - fx) ** 2 + (ty - fy) ** 2); // where the still lies on the way up
  return Array.from({ length: o.lanes || 1 }, (_, lane) => {
    const pos = [[0, [0, 0]]], alpha = [[0, o.still]], R0 = between(r, o.rise), up = R0 * (1 - p0);
    if (o.still < o.peak) alpha.push([Math.max(300, (fi - p0) * R0), o.peak]);
    pos.push([up, o.to]); alpha.push([Math.max(alpha[alpha.length - 1][0] + 200, (fo - p0) * R0), o.peak], [up, 0]);
    let t = up + between(r, o.gap || [0, 0]) + lane * between(r, [1500, 2600]);
    const times = o.at ? o.at.slice() : null, last = R => END - p0 * R;
    for (;;) {
      if (times) { while (times.length && times[0] < t) times.shift(); if (!times.length) break; t = times.shift(); }
      const R = between(r, o.rise);
      if (t + R + 40 + p0 * o.rise[1] > END) break; // room left for the last rise
      pos.push([t - 40, o.to], [t, o.from], [t + p0 * R, [0, 0]], [t + R, o.to]); // through the still's place (a cut for svg2avd)
      alpha.push([t, 0], ...(o.still < o.peak ? [[t + fi * R * o.still / o.peak, o.still]] : []), [t + fi * R, o.peak], [t + fo * R, o.peak], [t + R, 0]);
      t += R + 40 + (times ? 0 : between(r, o.gap));
    }
    const R = between(r, o.rise); t = Math.min(Math.max(t, pos[pos.length - 1][0] + 40), last(R)); // last rise: up to the still, held
    pos.push([t - 40, o.to], [t, o.from], [t + p0 * R, [0, 0]], [LIFE, [0, 0]]);
    alpha.push([t, 0], [t + Math.min(fi * R, p0 * R), o.still], [LIFE, o.still]);
    return { pos: ms(pos), alpha: ms(alpha) };
  });
}
