// Réactions au toucher de Monsieur Glose : require('./reactions.js')(RES), à la fin de make_android.js (les calques sont en mémoire).
// AVD à animateurs seuls sur les VD déjà générés : drawable/glose_<pose>_tap.xml (un toucher) et glose_<pose>_special.xml
// (4 touchers d'affilée, Glose.Taps). Les cibles sont relues dans le VD (groupe animé dans paupiere_d, pivot de la pelote,
// traits du fil) : la numérotation a0, a1… du convertisseur peut changer sans casser ces réactions.
// Written through svg2avd.animated(): one AVD per layer holding a target, so worn accessories (copies of tete / dormeur) follow.
// Clé d'image : [fraction, valeur, accélération] ; accélération 'out' (décélère : lancer), 'in' (accélère : chute), sinon glose_ease.
// Chaque geste a son anticipation (petit mouvement contraire avant), son squash & stretch (corps tassé au départ et à l'arrivée, étiré
// dans l'élan ; pelote écrasée à chaque contact) et son dépassement (il va un peu au-delà, puis se pose).
const { animated, eases, PARTS } = require('./svg2avd.js');
const WORDS = ['mot', 'bravo', 'encore', 'rare', 'dodo', 'coucou', 'ronron', 'flocon']; // mêmes mots que make_android.js
const EASE = { out: '@android:interpolator/decelerate_quad', in: '@android:interpolator/accelerate_quad' };
const TAP_MS = 600, SPECIAL_MS = 1800; // Glose.Taps : silence de 700 ms après un toucher, 3 s après le spécial

// groupe animé (aN) juste sous le groupe nommé g, et son pivot
const inner = (vd, g) => new RegExp(`android:name="${g}"[\\s\\S]*?android:name="(a\\d+)"`).exec(vd)[1];
const pivot = (vd, g) => new RegExp(`android:name="${g}"[\\s\\S]*?android:pivotX="([-\\d.]+)" android:pivotY="([-\\d.]+)"`).exec(vd).slice(1).map(Number);
const at = ([x, y]) => ({ pivotX: [[0, x], [1, x]], pivotY: [[0, y], [1, y]] }); // pivot posé sur un groupe nommé qui n'en a pas
// paupière fermée de a à b (fractions), sinon ouverte (scaleY 0.001 dans le VD)
const lid = (a, b) => ({ scaleY: [[0, 0.001], [a, 0.001], [a + 0.06, 1], [b, 1], [b + 0.08, 0.001], [1, 0.001]] });
const wink = (vd, a, b) => ({ [inner(vd, 'paupiere_d')]: lid(a, b) });

// Squash & stretch du corps : l'échelle verticale sy (clés d'image) l'écrase ou l'étire autour de sa base, la largeur compense
// (volume à peu près gardé) et chaque partie posée dessus suit : translateY = (sy - 1)·k, k = sa hauteur au-dessus de la base (< 0).
// Les parties sans liseré à elles (pattes, bras, brin) bougent de moins de 8 : le liseré fixe derrière elles (12) les couvre encore.
// Les accessoires du cou (écharpe, nœud, grelot) suivent la tête ; ceux de la tête sont déjà dans « tete ».
const NECK = k => Object.fromEntries(['echarpe', 'echarpe_dos', 'noeud', 'grelot', 'grelot_ruban'].map(g => [g, k]));
const BODY = {
  assis: { corps: [500, 700], tete: -182, ...NECK(-190), pattes: -120, pelote: -94, brin: -20 },
  ecrit: { corps: [500, 700], tete: -180, ...NECK(-190), pattes: -96, pelote: -84, bras_leve: -110, fil_bas: -100 },
  livres: { corps: [640, 655], tete: -160, ...NECK(-160), patte: -60 },
  soir: { dormeur: [490, 620] }, reveil: { dormeur: [490, 620] }, // tête et pattes sont dans « dormeur » : il s'écrase sur le bord de la tasse
};
const r3 = v => Math.round(v * 1000) / 1000;
function squash(family, sy) {
  const [[body, xy], ...parts] = Object.entries(BODY[family]);
  return { [body]: { ...at(xy), scaleY: sy, scaleX: sy.map(([t, v, e]) => [t, r3(1 + (1 - v) * 0.6), e]) },
    ...Object.fromEntries(parts.map(([g, k]) => [g, { translateY: sy.map(([t, v, e]) => [t, r3((v - 1) * k), e]) }])) };
}
// deux courbes de clés sur la même propriété : somme (translations, rotations) ou produit (échelles), aux fractions de l'une et de l'autre
const val = (k, t) => { const i = k.findIndex(q => q[0] >= t); if (i < 0) return k[k.length - 1][1]; if (i === 0 || k[i][0] === t) return k[i][1];
  const [t0, v0] = k[i - 1], [t1, v1] = k[i]; return v0 + (v1 - v0) * (t - t0) / (t1 - t0); };
function combine(a, b, p) {
  if (/^pivot/.test(p)) return a;
  const ts = [...new Set([...a, ...b].map(q => q[0]))].sort((x, y) => x - y);
  return ts.map(t => [t, r3(/^scale/.test(p) ? val(a, t) * val(b, t) : val(a, t) + val(b, t)), (a.find(q => q[0] === t) || b.find(q => q[0] === t))[2]]);
}
function merge(...all) {
  const out = {};
  for (const o of all) for (const [g, props] of Object.entries(o)) {
    out[g] = out[g] || {};
    for (const [p, k] of Object.entries(props)) out[g][p] = out[g][p] ? combine(out[g][p], k, p) : k;
  }
  return out;
}

// un toucher : il se tasse, se redresse en s'étirant, penche la tête un peu au-delà puis s'y pose, clin d'œil, et revient en rebondissant ;
// endormi : il se soulève à peine puis se blottit, écrasé sur le bord de la tasse ; les « z » se tassent puis sautent
const TAP = {
  awake: (vd, family) => merge(wink(vd, 0.1, 0.55),
    { tete: { rotation: [[0, 0], [0.1, -2], [0.28, 7, 'out'], [0.4, 5], [0.6, 5], [0.82, -1.5], [1, 0]],
      translateY: [[0, 0], [0.1, 0], [0.28, -7, 'out'], [0.4, -5], [0.6, -5], [0.85, 0]] } },
    squash(family, [[0, 1], [0.1, 0.93], [0.26, 1.05, 'out'], [0.38, 0.985], [0.48, 1], [0.66, 1], [0.78, 0.965], [0.9, 1.012], [1, 1]])),
  soir: (vd, family) => merge(
    { tete: { rotation: [[0, 0], [0.12, 1.5], [0.35, -5], [0.65, 3], [0.85, -1], [1, 0]] },
      zzz: { ...at([255, 305]), translateY: [[0, 0], [0.14, 4], [0.42, -16, 'out'], [0.72, 2], [0.86, -1], [1, 0]],
        scaleY: [[0, 1], [0.14, 0.86], [0.3, 1.12], [0.48, 1], [1, 1]], scaleX: [[0, 1], [0.14, 1.1], [0.3, 0.92], [0.48, 1], [1, 1]] } },
    squash(family, [[0, 1], [0.12, 1.025], [0.38, 0.95], [0.58, 1.015], [0.76, 1], [1, 1]])),
};
// 4 touchers : une réaction propre à chaque pose
const SPECIAL = {
  // il sursaute (tassé puis étiré) et la pelote lui échappe, roule au bord du livre en s'écrasant à chaque contact ; il la suit des yeux,
  // elle rebondit dans ses pattes, il l'attrape en se tassant, clin d'œil
  assis: vd => merge({
    pelote: { ...at(pivot(vd, 'pelote')),
      translateX: [[0, 0], [0.08, 0], [0.32, 120, 'in'], [0.4, 135], [0.5, 135], [0.72, 0]],
      translateY: [[0, 0], [0.08, 0], [0.32, 72, 'in'], [0.37, 60, 'out'], [0.42, 72, 'in'], [0.5, 72], [0.61, -30, 'out'], [0.72, 0, 'in'], [0.76, 6], [0.82, 0]],
      rotation: [[0, 0], [0.08, 0], [0.4, 120], [0.5, 120], [0.72, 0], [1, 0]],
      scaleY: [[0, 1], [0.3, 1.06], [0.33, 0.84], [0.36, 1.05], [0.41, 1], [0.43, 0.9], [0.47, 1], [0.6, 1.07], [0.71, 1], [0.74, 0.9], [0.8, 1]],
      scaleX: [[0, 1], [0.3, 0.96], [0.33, 1.1], [0.36, 0.97], [0.41, 1], [0.43, 1.06], [0.47, 1], [0.6, 0.95], [0.71, 1], [0.74, 1.06], [0.8, 1]] },
    tete: { rotation: [[0, 0], [0.1, 0], [0.32, 11, 'out'], [0.4, 9], [0.5, 9], [0.7, -3], [0.82, 1], [0.9, 0]],
      translateY: [[0, 0], [0.32, 6], [0.5, 6], [0.62, -6], [0.8, 0]] },
  }, wink(vd, 0.82, 0.9),
  squash('assis', [[0, 1], [0.04, 0.95], [0.1, 1.04, 'out'], [0.18, 1], [0.68, 1], [0.74, 0.95], [0.82, 1.02], [0.9, 1], [1, 1]])),
  // il recule la patte puis tape la pelote, qui s'étire en montant et s'écrase en retombant, deux fois ;
  // il relève la tête d'un coup (au-delà, puis s'y pose), la queue frétille
  livres: vd => merge({
    patte: { ...at(pivot(vd, 'patte')), rotation: [[0, 0], [0.07, -5], [0.14, 11, 'out'], [0.22, -3], [0.3, 0], [0.6, 0], [0.64, -2], [0.7, 6], [0.8, 0]] },
    pelote: { ...at(pivot(vd, 'pelote')),
      translateY: [[0, 0], [0.22, 0], [0.4, -70, 'out'], [0.55, 0, 'in'], [0.65, -22, 'out'], [0.74, 0, 'in']],
      scaleY: [[0, 1], [0.2, 1], [0.24, 0.86], [0.3, 1.08], [0.38, 1], [0.52, 1.06], [0.56, 0.88], [0.62, 1.04], [0.72, 1], [0.75, 0.93], [0.8, 1]],
      scaleX: [[0, 1], [0.2, 1], [0.24, 1.08], [0.3, 0.95], [0.38, 1], [0.52, 0.96], [0.56, 1.07], [0.62, 0.98], [0.72, 1], [0.75, 1.04], [0.8, 1]],
      rotation: [[0, 0], [0.24, 0], [0.74, 360], [1, 360]] },
    tete: { rotation: [[0, 0], [0.08, 2], [0.24, -10, 'out'], [0.45, -8], [0.7, 1], [0.8, 0], [1, 0]],
      translateY: [[0, 0], [0.08, 4], [0.24, -13, 'out'], [0.34, -10], [0.6, -10], [0.85, 1], [0.92, 0]] },
    ...Object.fromEntries(['queue', 'queue_halo'].map(q => [inner(vd, q), { rotation: [[0, 0], [0.5, 0], [0.6, 8], [0.7, -6], [0.8, 4], [0.9, 0]] }])), // et son liseré de nuit
  }, wink(vd, 0.84, 0.92), squash('livres', [[0, 1], [0.08, 0.96], [0.24, 1.03, 'out'], [0.36, 1], [0.8, 1], [0.86, 0.98], [0.94, 1], [1, 1]])),
  // il se tasse au fond de la tasse, s'étire au-dehors (au-delà, puis tenu), soupire, retombe en s'écrasant et se rendort ;
  // un gros « z » se tasse, se gonfle et s'envole
  soir: () => ({
    dormeur: { ...at([490, 700]), translateY: [[0, 0], [0.1, 4], [0.25, -22, 'out'], [0.32, -18], [0.5, -18], [0.68, 2], [0.76, -1], [0.84, 0]],
      scaleY: [[0, 1], [0.1, 0.96], [0.25, 1.07, 'out'], [0.32, 1.05], [0.5, 1.05], [0.68, 0.95], [0.76, 1.01], [0.84, 1]],
      scaleX: [[0, 1], [0.1, 1.025], [0.25, 0.96, 'out'], [0.32, 0.97], [0.5, 0.97], [0.68, 1.03], [0.76, 1], [0.84, 1]] },
    tete: { rotation: [[0, 0], [0.1, 1], [0.25, -9, 'out'], [0.32, -8], [0.5, -8], [0.68, 2], [0.8, 0]] },
    zzz: { ...at([255, 305]), scaleX: [[0, 1], [0.6, 1], [0.66, 0.9], [0.8, 1.7, 'out'], [0.86, 1.6], [0.92, 1.6], [1, 1]],
      scaleY: [[0, 1], [0.6, 1], [0.66, 0.9], [0.8, 1.7, 'out'], [0.86, 1.6], [0.92, 1.6], [1, 1]],
      translateY: [[0, 0], [0.6, 0], [0.66, 4], [0.92, -20], [1, 0]] },
  }),
  // il se soulève puis plonge le museau dans son thé en s'écrasant, ressort étiré et ravi (« ah »), la vapeur fait pouf
  reveil: vd => ({
    dormeur: { ...at([490, 620]), translateY: [[0, 0], [0.08, -6], [0.2, 16, 'in'], [0.45, 16], [0.58, -8, 'out'], [0.66, 1], [0.72, 0]],
      scaleY: [[0, 1], [0.08, 1.03], [0.2, 0.94], [0.26, 0.97], [0.45, 0.97], [0.58, 1.05, 'out'], [0.66, 0.99], [0.72, 1]],
      scaleX: [[0, 1], [0.08, 0.98], [0.2, 1.035], [0.26, 1.02], [0.45, 1.02], [0.58, 0.97, 'out'], [0.66, 1], [0.72, 1]] },
    tete: { rotation: [[0, 0], [0.08, -2], [0.2, 5], [0.45, 5], [0.58, -5, 'out'], [0.7, 1], [0.8, 0]] },
    [inner(vd, 'paupiere_g')]: lid(0.14, 0.42), [inner(vd, 'paupiere_d')]: lid(0.14, 0.42),
    [inner(vd, 'baille')]: { scaleX: [[0, 0.001], [0.5, 0.001], [0.58, 0.6], [0.75, 0.6], [0.82, 0.001], [1, 0.001]],
      scaleY: [[0, 0.001], [0.5, 0.001], [0.58, 0.6], [0.75, 0.6], [0.82, 0.001], [1, 0.001]] },
    vapeur: { ...at([700, 520]), translateY: [[0, 0], [0.5, 0], [0.88, -40, 'out'], [0.9, 0]],
      scaleX: [[0, 1], [0.5, 1], [0.88, 1.5, 'out'], [0.9, 1]], scaleY: [[0, 1], [0.5, 1], [0.88, 1.5, 'out'], [0.9, 1]] },
    // la bouffée s'efface en montant, puis la vapeur ordinaire revient
    ...Object.fromEntries(['vap1_o', 'vap2_o'].map(n => { const a = +new RegExp(`name="${n}"[^>]*?fillAlpha="([\\d.]+)"`).exec(vd)[1];
      return [n, { fillAlpha: [[0, a], [0.5, a], [0.6, 2 * a], [0.88, 0], [0.9, 0], [1, a]] }]; })),
  }),
  // la pelote tourne d'abord dans l'autre sens, le fil rembobine le mot et le réécrit d'un trait, la pelote va un peu au-delà puis revient ;
  // il se tasse au rembobinage, s'étire au premier trait ; clin d'œil (même réaction pour tous les mots)
  ecrit: vd => merge({
    ...Object.fromEntries(['fil_a', 'fil_b', 'fil_c'].map(n => [n, { trimPathEnd: [[0, 1], [0.2, 0, 'in'], [0.75, 1, 'out'], [1, 1]] }])),
    pelote: { ...at(pivot(vd, 'pelote')), rotation: [[0, 0], [0.06, 12], [0.2, -150, 'in'], [0.75, 10, 'out'], [0.85, -3], [0.93, 0]] },
    tete: { rotation: [[0, 0], [0.06, 1.5], [0.2, -3], [0.75, 5, 'out'], [0.86, -1], [1, 0]] },
  }, wink(vd, 0.8, 0.9), squash('ecrit', [[0, 1], [0.2, 0.96], [0.32, 1.03, 'out'], [0.44, 1], [0.76, 1], [0.82, 0.98], [0.9, 1], [1, 1]])),
};

// a key without its own acceleration eases only where the motion rests (svg2avd.eases): no stop where it passes through
const kfs = k => { const ease = eases(k.map(q => q[1]));
  return k.map(([t, v, e], i) => `<keyframe android:fraction="${t}" android:value="${v}"${i && (EASE[e] || ease[i]) ? ` android:interpolator="${EASE[e] || ease[i]}"` : ''}/>`).join(''); };
// { target: objectAnimator xml } for svg2avd.animated()
function animators(vd, ms, targets) {
  for (const [t, props] of Object.entries(targets)) for (const [p, k] of Object.entries(props)) // fin = début : pas de saut au retour à la vie au repos
    if ((k[k.length - 1][1] - k[0][1]) % (p === 'rotation' ? 360 : Infinity)) throw new Error(`${vd} ${t}.${p} ne revient pas à sa valeur de départ`);
  // linear timeline: the fractions above are times (without it, AccelerateDecelerate moved the 0.1 key to 0.2)
  return Object.fromEntries(Object.entries(targets).map(([t, props]) => [t, `<objectAnimator android:duration="${ms}" android:interpolator="@android:interpolator/linear">` +
    Object.entries(props).map(([p, k]) => `<propertyValuesHolder android:propertyName="${p}" android:valueType="floatType">${kfs(k)}</propertyValuesHolder>`).join('') + '</objectAnimator>']));
}
const poses = [['assis', 'assis'], ['livres', 'livres'], ['soir', 'soir'], ['reveil', 'reveil'], ...WORDS.map(w => [`ecrit_${w}`, 'ecrit'])];
module.exports = RES => {
  for (const [pose, family] of poses) {
    const name = `glose_${pose}_vd`, vd = PARTS[name].map(l => l.xml).join('\n'); // every layer of the pose, in drawing order
    for (const [kind, ms, make] of [['tap', TAP_MS, TAP[family] || TAP.awake], ['special', SPECIAL_MS, SPECIAL[family]]])
      animated(`glose_${pose}_${kind}`, name, animators(name, ms, make(vd, family)), RES);
  }
};
