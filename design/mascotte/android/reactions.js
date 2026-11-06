// Réactions au toucher de Monsieur Glose : require('./reactions.js')(RES), à la fin de make_android.js (les calques sont en mémoire).
// AVD à animateurs seuls sur les VD déjà générés : drawable/glose_<pose>_tap.xml (un toucher) et glose_<pose>_special.xml
// (4 touchers d'affilée, Glose.Taps). Les cibles sont relues dans le VD (groupe animé dans paupiere_d, pivot de la pelote,
// traits du fil) : la numérotation a0, a1… du convertisseur peut changer sans casser ces réactions.
// Written through svg2avd.animated(): one AVD per layer holding a target, so worn accessories (copies of tete / dormeur) follow.
// Clé d'image : [fraction, valeur, accélération] ; accélération 'out' (décélère : lancer), 'in' (accélère : chute), sinon glose_ease.
const { animated, PARTS } = require('./svg2avd.js');
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

// un toucher : clin d'œil et tête qui se penche (yeux ouverts) ; endormi : il se blottit, les « z » sautillent
const TAP = {
  awake: vd => ({ ...wink(vd, 0.1, 0.55), tete: { rotation: [[0, 0], [0.25, 5], [0.6, 5], [1, 0]], translateY: [[0, 0], [0.25, -5], [0.6, -5], [1, 0]] } }),
  soir: () => ({ tete: { rotation: [[0, 0], [0.3, -4], [0.65, 3], [1, 0]] }, zzz: { translateY: [[0, 0], [0.4, -14], [1, 0]] } }),
};
// 4 touchers : une réaction propre à chaque pose
const SPECIAL = {
  // la pelote lui échappe, roule au bord du livre ; il la suit des yeux, elle rebondit dans ses pattes, clin d'œil
  assis: vd => ({
    pelote: { ...at(pivot(vd, 'pelote')),
      translateX: [[0, 0], [0.08, 0], [0.32, 120, 'in'], [0.4, 135], [0.5, 135], [0.72, 0]],
      translateY: [[0, 0], [0.08, 0], [0.32, 72, 'in'], [0.37, 60, 'out'], [0.42, 72, 'in'], [0.5, 72], [0.61, -30, 'out'], [0.72, 0, 'in'], [0.76, 6], [0.82, 0]],
      rotation: [[0, 0], [0.08, 0], [0.4, 120], [0.5, 120], [0.72, 0], [1, 0]] },
    tete: { rotation: [[0, 0], [0.1, 0], [0.32, 9], [0.5, 9], [0.7, -2], [0.82, 0]], translateY: [[0, 0], [0.32, 6], [0.5, 6], [0.62, -6], [0.8, 0]] },
    ...wink(vd, 0.82, 0.9),
  }),
  // sa patte tape la pelote, qui rebondit deux fois ; il relève la tête, la queue frétille
  livres: vd => ({
    patte: { ...at(pivot(vd, 'patte')), rotation: [[0, 0], [0.12, 10], [0.22, -3], [0.3, 0], [0.62, 0], [0.7, 6], [0.8, 0]] },
    pelote: { ...at(pivot(vd, 'pelote')),
      translateY: [[0, 0], [0.22, 0], [0.4, -70, 'out'], [0.55, 0, 'in'], [0.65, -22, 'out'], [0.74, 0, 'in']],
      scaleY: [[0, 1], [0.2, 1], [0.24, 0.88], [0.3, 1], [0.54, 1], [0.57, 0.92], [0.62, 1]],
      rotation: [[0, 0], [0.24, 0], [0.74, 360], [1, 360]] },
    tete: { rotation: [[0, 0], [0.2, -4], [0.45, -8], [0.7, 0], [1, 0]], translateY: [[0, 0], [0.2, -10], [0.6, -10], [0.85, 0]] },
    [inner(vd, 'queue')]: { rotation: [[0, 0], [0.5, 0], [0.6, 8], [0.7, -6], [0.8, 4], [0.9, 0]] },
    ...wink(vd, 0.84, 0.92),
  }),
  // il s'étire hors de la tasse, soupire, se rendort ; un gros « z » s'envole
  soir: () => ({
    dormeur: { ...at([490, 700]), translateY: [[0, 0], [0.25, -18], [0.5, -18], [0.68, 2], [0.76, 0]], scaleY: [[0, 1], [0.25, 1.05], [0.5, 1.05], [0.68, 1]] },
    tete: { rotation: [[0, 0], [0.25, -8], [0.5, -8], [0.68, 2], [0.8, 0]] },
    zzz: { ...at([255, 305]), scaleX: [[0, 1], [0.6, 1], [0.8, 1.6], [0.92, 1.6], [1, 1]], scaleY: [[0, 1], [0.6, 1], [0.8, 1.6], [0.92, 1.6], [1, 1]],
      translateY: [[0, 0], [0.6, 0], [0.92, -20], [1, 0]] },
  }),
  // il plonge le museau dans son thé, ressort ravi (« ah »), la vapeur fait pouf
  reveil: vd => ({
    dormeur: { translateY: [[0, 0], [0.2, 16], [0.45, 16], [0.58, -6], [0.7, 0]] },
    tete: { rotation: [[0, 0], [0.2, 5], [0.45, 5], [0.58, -4], [0.8, 0]] },
    [inner(vd, 'paupiere_g')]: lid(0.14, 0.42), [inner(vd, 'paupiere_d')]: lid(0.14, 0.42),
    [inner(vd, 'baille')]: { scaleX: [[0, 0.001], [0.5, 0.001], [0.58, 0.6], [0.75, 0.6], [0.82, 0.001], [1, 0.001]],
      scaleY: [[0, 0.001], [0.5, 0.001], [0.58, 0.6], [0.75, 0.6], [0.82, 0.001], [1, 0.001]] },
    vapeur: { ...at([700, 520]), translateY: [[0, 0], [0.5, 0], [0.88, -40, 'out'], [0.9, 0]],
      scaleX: [[0, 1], [0.5, 1], [0.88, 1.5, 'out'], [0.9, 1]], scaleY: [[0, 1], [0.5, 1], [0.88, 1.5, 'out'], [0.9, 1]] },
    // la bouffée s'efface en montant, puis la vapeur ordinaire revient
    ...Object.fromEntries(['vap1_o', 'vap2_o'].map(n => { const a = +new RegExp(`name="${n}"[^>]*?fillAlpha="([\\d.]+)"`).exec(vd)[1];
      return [n, { fillAlpha: [[0, a], [0.5, a], [0.6, 2 * a], [0.88, 0], [0.9, 0], [1, a]] }]; })),
  }),
  // le fil rembobine le mot et le réécrit d'un trait, la pelote tourne avec, clin d'œil (même réaction pour tous les mots)
  ecrit: vd => ({
    ...Object.fromEntries(['fil_a', 'fil_b', 'fil_c'].map(n => [n, { trimPathEnd: [[0, 1], [0.2, 0, 'in'], [0.75, 1, 'out'], [1, 1]] }])),
    pelote: { ...at(pivot(vd, 'pelote')), rotation: [[0, 0], [0.2, -150, 'in'], [0.75, 0, 'out'], [1, 0]] },
    tete: { rotation: [[0, 0], [0.2, -3], [0.75, 4], [1, 0]] },
    ...wink(vd, 0.8, 0.9),
  }),
};

const kf = ([t, v, e], i) => `<keyframe android:fraction="${t}" android:value="${v}"${i ? ` android:interpolator="${EASE[e] || '@interpolator/glose_ease'}"` : ''}/>`;
// { target: objectAnimator xml } for svg2avd.animated()
function animators(vd, ms, targets) {
  for (const [t, props] of Object.entries(targets)) for (const [p, k] of Object.entries(props)) // fin = début : pas de saut au retour à la vie au repos
    if ((k[k.length - 1][1] - k[0][1]) % (p === 'rotation' ? 360 : Infinity)) throw new Error(`${vd} ${t}.${p} ne revient pas à sa valeur de départ`);
  return Object.fromEntries(Object.entries(targets).map(([t, props]) => [t, `<objectAnimator android:duration="${ms}">` +
    Object.entries(props).map(([p, k]) => `<propertyValuesHolder android:propertyName="${p}" android:valueType="floatType">${k.map(kf).join('')}</propertyValuesHolder>`).join('') + '</objectAnimator>']));
}
const poses = [['assis', 'assis'], ['livres', 'livres'], ['soir', 'soir'], ['reveil', 'reveil'], ...WORDS.map(w => [`ecrit_${w}`, 'ecrit'])];
module.exports = RES => {
  for (const [pose, family] of poses) {
    const name = `glose_${pose}_vd`, vd = PARTS[name].map(l => l.xml).join('\n'); // every layer of the pose, in drawing order
    for (const [kind, ms, make] of [['tap', TAP_MS, TAP[family] || TAP.awake], ['special', SPECIAL_MS, SPECIAL[family]]])
      animated(`glose_${pose}_${kind}`, name, animators(name, ms, make(vd)), RES);
  }
};
