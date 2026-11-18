// Réactions au toucher de Monsieur Glose : require('./reactions.js')(RES), à la fin de make_android.js (les calques sont en mémoire).
// AVD à animateurs seuls sur les VD déjà générés : drawable/glose_<pose>_tap.xml (un toucher) et glose_<pose>_special.xml
// (4 touchers d'affilée, Glose.Taps). Les cibles sont relues dans le VD (groupe animé dans paupiere_d, pivot de la pelote,
// traits du fil) : la numérotation a0, a1… du convertisseur peut changer sans casser ces réactions.
// Written through svg2avd.animated(): one AVD per layer holding a target, so worn accessories (copies of tete / dormeur) follow.
// Clé d'image : [fraction, valeur, accélération] ; 'out' : part vite d'un repos (lancer), 'in' : arrive vite sur un repos (chute, choc) ; sinon Hermite (svg2avd).
// Chaque geste a son anticipation (petit mouvement contraire avant), son squash & stretch (corps tassé au départ et à l'arrivée, étiré
// dans l'élan ; pelote écrasée à chaque contact) et son dépassement (il va un peu au-delà, puis se pose).
const { animated, hermite, PARTS, IDLE0 } = require('./svg2avd.js');
const WORDS = ['mot', 'bravo', 'encore', 'rare', 'dodo', 'coucou', 'ronron', 'flocon']; // mêmes mots que make_android.js
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
// Les accessoires du cou suivent la tête (neck()) ; ceux de la tête sont déjà dans « tete ».
const BODY = {
  assis: { corps: [500, 700], tete: -182, pattes: -120, pelote: -94, brin: -20 },
  ecrit: { corps: [500, 700], tete: -180, pattes: -96, pelote: -84, bras_leve: -110, fil_bas: -100, pince: -110, fil: -110 }, // the pinching fingers and the thread they hold go with the raised paw
  livres: { corps: [640, 655], tete: -160, patte: -60 },
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

// the part's inner animated group (its idle life) and its night outline's: reactions key them alike
const both = (vd, g, keys) => Object.fromEntries([g, g + '_halo'].filter(n => vd.includes(`android:name="${n}"`)).map(n => [inner(vd, n), keys]));
const mirror = k => k.map(([t, v, f]) => [t, -v, f]);
const whiskers = (vd, k) => ({ ...both(vd, 'moustaches_g', { rotation: k }), ...both(vd, 'moustaches_d', { rotation: mirror(k) }) });
const eyes = (vd, a, b) => ({ [inner(vd, 'paupiere_g')]: lid(a, b), [inner(vd, 'paupiere_d')]: lid(a, b) });
// A tap, awake: anticipation (he sinks, the head dips), action (stretch, the head rises then tilts: an arc, the tilt peaking ~50 ms after
// the rise), follow-through (tail and whiskers start after the body and settle after it), landing (a small squash, the head overshoots
// and settles). Keys on one grid (0.08 apart at least: merge() never makes a segment under 33 ms). One personality per pose:
// assis content (wink), livres curious (double take, surprised blink, the paw twitches), écrit proud (chest out, chin up, smug slow blink),
// réveil sleepy (slow blink). Body squash leads, head lags it by 1–2 frames, tail and whiskers by 4–8. The head holds ~120 ms at the
// top of the gesture (an accent; the eyes and the tail keep moving): its layers (and the worn pieces) are not redrawn meanwhile; all is
// settled ~50 ms before the end. RenderThread cost per frame then stays at or under the base's (glose_qa animation.md).
const PERSONA = {
  assis: { sy: [[0, 1], [0.08, 0.94], [0.2, 1.045, 'out'], [0.36, 0.99], [0.46, 1], [0.66, 1], [0.74, 0.97], [0.84, 1.01], [0.92, 1], [1, 1]],
    rot: [[0, 0], [0.08, -2], [0.36, 7, 'out'], [0.46, 5], [0.66, 5], [0.8, -1.5], [0.92, 0], [1, 0]],
    ty: [[0, 0], [0.08, 1.5], [0.28, -7, 'out'], [0.46, -5], [0.66, -5], [0.84, 1.2], [0.92, 0], [1, 0]],
    tail: [[0, 0], [0.2, 0], [0.4, 4.5], [0.56, -2], [0.7, 0.6], [0.8, 0], [1, 0]],
    whisk: [[0, 0], [0.08, 0], [0.2, -4, 'out'], [0.36, 1.5], [0.46, 0]], eyes: vd => wink(vd, 0.2, 0.46) },
  livres: { sy: [[0, 1], [0.08, 0.97], [0.2, 1.03, 'out'], [0.36, 0.995], [0.46, 1], [1, 1]],
    rot: [[0, 0], [0.08, 2], [0.28, -9, 'out'], [0.46, -7], [0.62, -7], [0.73, 3.5], [0.82, -0.8], [0.9, 0], [1, 0]],
    ty: [[0, 0], [0.08, 1], [0.2, -5, 'out'], [0.36, -4], [0.62, -4], [0.76, 0.8], [0.86, 0], [1, 0]],
    tail: [[0, 0], [0.14, 0], [0.28, -3], [0.42, 6, 'out'], [0.56, -2.5], [0.66, 0.8], [0.76, 0], [1, 0]],
    whisk: [[0, 0], [0.08, 0], [0.2, -5, 'out'], [0.36, -4], [0.46, 0]], eyes: vd => eyes(vd, 0.36, 0.44),
    extra: vd => ({ [inner(vd, 'patte')]: { rotation: [[0, 0], [0.2, 0], [0.36, -2.5], [0.46, 0.8], [0.56, 0], [1, 0]] } }) },
  ecrit: { sy: [[0, 1], [0.08, 0.96], [0.28, 1.04, 'out'], [0.36, 1.03], [0.56, 1.03], [0.76, 0.985], [0.86, 1], [1, 1]],
    rot: [[0, 0], [0.08, 1], [0.28, -5, 'out'], [0.36, -4.5], [0.56, -4.5], [0.7, 3], [0.8, -0.8], [0.9, 0], [1, 0]],
    ty: [[0, 0], [0.08, 1], [0.28, -8, 'out'], [0.36, -7], [0.56, -7], [0.76, 1], [0.86, 0], [1, 0]],
    tail: [[0, 0], [0.2, 0], [0.38, 3], [0.54, -1.5], [0.7, 0], [1, 0]],
    whisk: [[0, 0], [0.2, 0], [0.3, -3], [0.4, 0]], eyes: vd => eyes(vd, 0.56, 0.7) },
};
PERSONA.reveil = Object.assign({}, PERSONA.assis, { tail: null, eyes: vd => eyes(vd, 0.36, 0.6) });
const awake = (vd, family) => { const m = PERSONA[family];
  return merge(m.eyes(vd), { tete: { rotation: m.rot, translateY: m.ty } }, m.tail ? both(vd, 'queue', { rotation: m.tail }) : {},
    whiskers(vd, m.whisk), m.extra ? m.extra(vd) : {}, squash(family, m.sy)); };
// un toucher éveillé : awake() ; endormi : il se soulève à peine puis se blottit, écrasé sur le bord de la tasse ; les « z » s'effacent avec
// l'ancienne image (fondu du toucher) et ne reviennent qu'à la fin, en montant à leur place (settle, Z_BACK)
const TAP = {
  awake,
  soir: (vd, family) => merge(
    { tete: { rotation: [[0, 0], [0.12, 1.5], [0.35, -5], [0.65, 3], [0.85, -1], [1, 0]] },
      zzz: { ...at([255, 305]), translateY: [[0, 0], [0.66, 14], [1, 0]], scaleY: [[0, 1], [0.66, 0.85], [1, 1]], scaleX: [[0, 1], [0.66, 0.85], [1, 1]] } },
    whiskers(vd, [[0, 0], [0.2, 0], [0.4, 3], [0.6, -1], [0.8, 0]]), // a dreamer's twitch
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
  whiskers(vd, [[0, 0], [0.04, 0], [0.1, -6, 'out'], [0.2, 2], [0.3, 0], [0.74, 0], [0.8, -3], [0.86, 0]]), // startled, then pleased
  both(vd, 'queue', { rotation: [[0, 0], [0.06, 0], [0.14, 7, 'out'], [0.26, -3], [0.38, 1.5], [0.5, 0], [0.74, 0], [0.8, 3], [0.88, 0], [1, 0]] }),
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
  }, wink(vd, 0.84, 0.92), whiskers(vd, [[0, 0], [0.12, 0], [0.2, -4, 'out'], [0.3, 1], [0.4, 0], [0.62, 0], [0.7, -3], [0.8, 0]]), squash('livres', [[0, 1], [0.08, 0.96], [0.24, 1.03, 'out'], [0.36, 1], [0.8, 1], [0.85, 0.98], [0.94, 1], [1, 1]])),
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
  reveil: vd => ({ ...whiskers(vd, [[0, 0], [0.5, 0], [0.6, -4, 'out'], [0.7, 1.5], [0.8, 0]]), // « ah »
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
      return [n, { fillAlpha: [[0, 0], [0.07, a], [0.5, a], [0.6, 2 * a], [0.88, 0], [0.9, 0], [1, a]] }]; })),
  }),
  // la pelote tourne d'abord dans l'autre sens, le fil rembobine le mot et le réécrit d'un trait, la pelote va un peu au-delà puis revient ;
  // il se tasse au rembobinage, s'étire au premier trait ; clin d'œil (même réaction pour tous les mots)
  ecrit: vd => merge({
    ...Object.fromEntries(['fil_a', 'fil_b', 'fil_c'].map(n => [n, { trimPathEnd: [[0, 1], [0.2, 0, 'in'], [0.75, 1, 'out'], [1, 1]] }])),
    pelote: { ...at(pivot(vd, 'pelote')), rotation: [[0, 0], [0.06, 12], [0.2, -150, 'in'], [0.75, 10, 'out'], [0.85, -3], [0.93, 0]] },
    tete: { rotation: [[0, 0], [0.06, 1.5], [0.2, -3], [0.75, 5, 'out'], [0.86, -1], [1, 0]] },
  }, wink(vd, 0.8, 0.9), whiskers(vd, [[0, 0], [0.7, 0], [0.78, -3.5, 'out'], [0.86, 1], [0.92, 0]]),
  both(vd, 'queue', { rotation: [[0, 0], [0.06, 0], [0.18, -2.5], [0.4, 1], [0.7, 0], [0.78, 4], [0.86, -1], [0.92, 0], [1, 0]] }), squash('ecrit', [[0, 1], [0.2, 0.96], [0.32, 1.03, 'out'], [0.44, 1], [0.76, 1], [0.82, 0.98], [0.9, 1], [1, 1]])),
};

// speed continuous through the keys the motion passes, zero where it rests ('out' / 'in': snap out of a rest, impact on one; svg2avd.hermite)
const kfs = k => { const ease = hermite(k.map(q => q[1]), k.map(q => q[0]), false, k.map(q => q[2]));
  return k.map(([t, v], i) => `<keyframe android:fraction="${t}" android:value="${v}"${i && ease[i] ? ` android:interpolator="${ease[i]}"` : ''}/>`).join(''); };
// A reaction ends on the first frame of the idle life (once() then plays it afresh), not on the still: for the « z » and the steam of the
// evening poses, which the idle starts mid-cycle; they reach it in the reaction's last 150 ms. What the idle shows and hides (« z »,
// steam: alpha down to 0) may be hidden at the tap: the reaction fades it in from 0 (it vanishes then, a sleeper's start, never pops in).
// The « z » (bold ink: their fade-in right after the tap's crossfade read as a flicker) come back at Z_BACK instead: 'end', over the
// reaction's last 200 ms (as they rise into place, TAP.soir); a fraction, faded in from there (the special's big « z »).
// A reaction that animates such a property itself keeps its keys but these ends.
const Z_BACK = { tap: 'end', special: 0.6 };
function settle(targets, idle0, ms, isZ = () => false, back = 0) {
  const fade = +(120 / ms).toFixed(3), fin = +(1 - 150 / ms).toFixed(3), late = +(1 - 200 / ms).toFixed(3);
  for (const [t, props] of Object.entries(idle0)) for (const [p, { start, still, low }] of Object.entries(props)) {
    const hides = /Alpha$/.test(p) && low === 0, b = isZ(t) ? back : 0;
    if (start === still && !hides) continue;
    const k = (targets[t] = targets[t] || {})[p];
    if (k) { k[k.length - 1][1] = start; if (hides) k[0][1] = 0; }
    else targets[t][p] = !hides ? [[0, still], [fin, still], [1, start]] : b === 'end' ? [[0, 0], [late, 0], [1, start]]
      : [[0, 0], ...(b ? [[b, 0]] : []), [+(b + fade).toFixed(3), still], [fin, still], [1, start]];
  }
  return targets;
}
// is the target named t (a group or a path) inside a group named g, in some layer of the VD
function under(vd, t, g) {
  const st = [], re = /<(\/?)(group|path|clip-path)\b([^>]*?)(\/?)>/g; let m;
  while ((m = re.exec(vd))) {
    if (m[1]) { if (m[2] === 'group') st.pop(); continue; }
    const name = (/android:name="([^"]*)"/.exec(m[3]) || [])[1];
    if (name === t && st.some(n => n === g || n === g + '_halo')) return true; // and its night outline's
    if (m[2] === 'group' && !m[4]) st.push(name);
  }
  return false;
}
// { target: objectAnimator xml } for svg2avd.animated()
function animators(vd, ms, targets, idle0) {
  for (const [t, props] of Object.entries(targets)) for (const [p, k] of Object.entries(props)) { // fin = image 0 de la vie : pas de saut au retour
    const end = idle0[t] && idle0[t][p] ? idle0[t][p].start : k[0][1]; // a property the idle does not animate: back to where it started (the still)
    if (Math.abs((k[k.length - 1][1] - end) % (p === 'rotation' ? 360 : Infinity)) > 1e-6) throw new Error(`${vd} ${t}.${p} ne finit pas sur l'image 0 de la vie (${end})`);
    // a key segment shorter than 2 frames at 60 Hz is a jump, not a motion (merge() crossing nearby keys of two curves made 12 ms ones)
    k.slice(1).forEach(([t1, v1], i) => { if ((t1 - k[i][0]) * ms < 33 && v1 !== k[i][1]) throw new Error(`${vd} ${t}.${p}: ${k[i][0]} -> ${t1} lasts ${((t1 - k[i][0]) * ms).toFixed(0)} ms`); });
  }
  // linear timeline: the fractions above are times (without it, AccelerateDecelerate moved the 0.1 key to 0.2)
  return Object.fromEntries(Object.entries(targets).map(([t, props]) => [t, `<objectAnimator android:duration="${ms}" android:interpolator="@android:interpolator/linear">` +
    Object.entries(props).map(([p, k]) => `<propertyValuesHolder android:propertyName="${p}" android:valueType="floatType">${kfs(k)}</propertyValuesHolder>`).join('') + '</objectAnimator>']));
}
// ---------- unwinding thread (glose_lib.js tether()) ----------
// When the ball goes away from where its loose thread leaves it, a straight run of yarn joins the thread to the ball's outline and
// lengthens by the way it went (the ball unwinds; coming back, it rewinds). Tether group (anchor, ball radius): its keys are
// computed from the reaction's own keys (same easing as Android), so its end stays on the (squashed) ball at every frame.
const TETHERS = { assis: [['brin_lien', 76]], livres: [['sol_lien', 64], ['patte_lien', 64]] }; // radii: pose_assis.js, pose_livres.js
const ALPHA = { _a: 1, _b: 1, _c: 0.3, _h: 1 }; // its strokes once shown (ink, yarn, sheen, night outline): tether() draws them at 0
const TETHER_LEN = 200; // glose_lib.js: the run is drawn this long, trimmed to its length
const CAP = 9; // the fixed thread's round cap (ink 18 / 2): a run shorter than this is hidden under it, no tether animator then
const bez = (x1, y1, x2, y2) => t => { let lo = 0, hi = 1, u; for (let i = 0; i < 40; i++) { u = (lo + hi) / 2;
  if (3 * (1 - u) ** 2 * u * x1 + 3 * (1 - u) * u * u * x2 + u ** 3 < t) lo = u; else hi = u; }
  return 3 * (1 - u) ** 2 * u * y1 + 3 * (1 - u) * u * u * y2 + u ** 3; };
const EASES = { '@interpolator/glose_ease': bez(0.45, 0, 0.55, 1), '@android:interpolator/decelerate_quad': t => 1 - (1 - t) ** 2,
  '@android:interpolator/accelerate_quad': t => t * t };
const easeOf = i => !i ? (t => t) : EASES[i] || (([a, b]) => bez(1 / 3, a / 3000, 2 / 3, 1 - b / 3000))(i.split('_').slice(-2).map(Number));
function valueAt(k, f) { // keys [fraction, value, force] eased as kfs() writes them
  const e = hermite(k.map(q => q[1]), k.map(q => q[0]), false, k.map(q => q[2]));
  if (f <= k[0][0]) return k[0][1];
  for (let i = 1; i < k.length; i++) if (f <= k[i][0]) return k[i - 1][1] + (k[i][1] - k[i - 1][1]) * easeOf(e[i])((f - k[i - 1][0]) / (k[i][0] - k[i - 1][0]));
  return k[k.length - 1][1];
}
// groups enclosing the group named nm in the VD (outermost first, that group last), as attribute maps
function chainOf(vd, nm) {
  const st = [], re = /<(\/?)(group|path|clip-path)\b([^>]*?)(\/?)>/g; let m;
  while ((m = re.exec(vd))) {
    if (m[1]) { if (m[2] === 'group') st.pop(); continue; }
    const attrs = Object.fromEntries([...m[3].matchAll(/android:(\w+)="([^"]*)"/g)].map(x => [x[1], x[2]]));
    if (m[2] === 'group') { st.push(attrs); if (attrs.name === nm) return st.slice(); if (m[4]) st.pop(); }
  }
  throw new Error(`tether: no group ${nm}`);
}
const mul = (A, B) => [A[0] * B[0] + A[1] * B[2], A[0] * B[1] + A[1] * B[3], A[2] * B[0] + A[3] * B[2], A[2] * B[1] + A[3] * B[3],
  A[0] * B[4] + A[1] * B[5] + A[4], A[2] * B[4] + A[3] * B[5] + A[5]];
const ap = (M, [x, y]) => [M[0] * x + M[1] * y + M[4], M[2] * x + M[3] * y + M[5]];
const inv = ([a, b, c, d, e, f]) => { const k = a * d - b * c; return [d / k, -b / k, -c / k, a / k, (b * f - d * e) / k, (c * e - a * f) / k]; };
function world(chain, targets, f) { // VGroup matrix: translate + pivot, rotate, scale, minus pivot; reaction keys over the VD's values
  return chain.reduce((W, g) => {
    const p = q => targets[g.name] && targets[g.name][q] ? valueAt(targets[g.name][q], f) : +(g[q] ?? (/^scale/.test(q) ? 1 : 0));
    const r = p('rotation') * Math.PI / 180, c = Math.cos(r), s = Math.sin(r), sx = p('scaleX'), sy = p('scaleY'), px = p('pivotX'), py = p('pivotY');
    const L = [sx * c, -sy * s, sx * s, sy * c];
    return mul(W, [...L, p('translateX') + px - L[0] * px - L[1] * py, p('translateY') + py - L[2] * px - L[3] * py]);
  }, [1, 0, 0, 1, 0, 0]);
}
// { tether target: objectAnimator xml } for one reaction of [ms] whose keys are [targets]; none for a run that stays under the cap
function tethers(vd, family, ms, targets) {
  const out = {}, n = Math.floor(ms / 17); // keys every frame (≥ 17 ms), linear in between as the RenderThread samples them
  for (const [nm, r] of TETHERS[family] || []) {
    const tc = chainOf(vd, nm), parent = tc.slice(0, -1), at = [+tc[tc.length - 1].translateX, +tc[tc.length - 1].translateY];
    const inner = /android:name="pelote"><group android:name="(a\d+)" android:pivotX="([-\d.]+)" android:pivotY="([-\d.]+)"/.exec(vd);
    const ball = chainOf(vd, 'pelote').concat([{ name: inner[1], pivotX: inner[2], pivotY: inner[3] }]), C = [+inner[2], +inner[3]];
    const keys = [];
    for (let i = 0; i <= n; i++) {
      const f = i / n, P = world(parent, targets, f), B = world(ball, targets, f), A = ap(P, at), Al = ap(inv(B), A);
      const d = Math.hypot(Al[0] - C[0], Al[1] - C[1]);
      const T = d > r ? ap(B, [C[0] + r * (Al[0] - C[0]) / d, C[1] + r * (Al[1] - C[1]) / d]) : A; // the outline, toward the anchor
      const v = ap(inv(P), T), len = Math.hypot(v[0] - at[0], v[1] - at[1]);
      keys.push([+f.toFixed(4), Math.atan2(v[1] - at[1], v[0] - at[0]) * 180 / Math.PI, len]);
    }
    if (Math.max(...keys.map(k => k[2])) <= CAP) continue;
    for (let i = 1; i < keys.length; i++) { // continuous angle, held where the run is too short to have one
      if (keys[i][2] < 0.5) keys[i][1] = keys[i - 1][1];
      while (keys[i][1] - keys[i - 1][1] > 180) keys[i][1] -= 360;
      while (keys[i][1] - keys[i - 1][1] < -180) keys[i][1] += 360;
    }
    // every other key only where the line between its neighbours puts the end within 0.4 of it (0.2 px at 160 dp): fewer keys
    const end = k => [k[2] * Math.cos(k[1] * Math.PI / 180), k[2] * Math.sin(k[1] * Math.PI / 180)];
    for (let i = keys.length - 2; i > 0; i -= 2) {
      const [a, b, c] = [keys[i - 1], keys[i], keys[i + 1]], mid = end([0, (a[1] + c[1]) / 2, (a[2] + c[2]) / 2]), e = end(b);
      if (Math.hypot(mid[0] - e[0], mid[1] - e[1]) < 0.4 && i > 1 && i < keys.length - 2) keys.splice(i, 1);
    }
    const kf = (p, ks) => `<propertyValuesHolder android:propertyName="${p}" android:valueType="floatType">` +
      ks.map(([f, v]) => `<keyframe android:fraction="${f}" android:value="${+v.toFixed(4)}"/>`).join('') + '</propertyValuesHolder>';
    const anim = h => `<objectAnimator android:duration="${ms}" android:interpolator="@android:interpolator/linear">${h}</objectAnimator>`;
    // the group turns; each stroke is trimmed to the length (a scaled group would stretch the round caps: the VD strokes in its frame)
    // and shown from the 2nd key to the one before last
    if (Math.max(...keys.map(k => k[2])) > TETHER_LEN) throw new Error(`${nm}: run longer than ${TETHER_LEN}`);
    out[nm] = out[nm + '_f'] = anim(kf('rotation', keys.map(k => [k[0], k[1]]))); // ink and core groups (glose_lib.js tether())
    const trim = kf('trimPathEnd', keys.map(k => [k[0], k[2] / TETHER_LEN])), shown = a => [[0, 0], [keys[1][0], a], [keys[keys.length - 2][0], a], [1, 0]];
    for (const [s, a] of Object.entries(ALPHA)) out[nm + s] = anim(trim + kf('strokeAlpha', shown(a)));
  }
  return out;
}
// Neck accessories ride with what carries the head in each reaction: « tete » awake (its keys and pivot: they turn and lift with it, squash
// included), « dormeur » in the cup (the scarf; the bow and the bell are inside it). Their idle breathing is on an inner group, at rest here.
const NECK = ['echarpe', 'echarpe_dos', 'noeud', 'grelot', 'grelot_ruban', 'lavalliere'];
function neck(vd, family, targets) {
  const by = family === 'soir' || family === 'reveil' ? 'dormeur' : 'tete', k = targets[by];
  if (!k) return targets;
  for (const g of NECK) if (vd.includes(`android:name="${g}"`) && !chainOf(vd, g).some(a => a.name === by)) targets[g] = { ...(k.pivotX ? {} : at(pivot(vd, by))), ...k };
  return targets;
}
// Bonnet de nuit en tissu (glose_lib.js softCap, essais 'long' / 'rond') : sa pointe traîne derrière la tête. Rotation propre = K × (rotation de
// la tête LAG ms plus tôt − la sienne) : elle part en retard, rattrape, se pose avec elle. Clés aux seuls extremums (repos entre deux :
// ni arrêt en route ni saut), les plus proches que 40 ms fondues ; quelques degrés au plus (elle reste sur la tête, test everyItemShowsInEveryPose).
const LAG = 120, K = 0.6;
function sway(vd, ms, targets) {
  const r = targets.tete && targets.tete.rotation;
  if (!r || !vd.includes('android:name="bonnet_pointe"')) return targets;
  const n = Math.round(ms / 10), s = [];
  for (let i = 0; i <= n; i++) { const f = i / n; s.push([f, K * (valueAt(r, Math.max(0, f - LAG / ms)) - valueAt(r, f))]); }
  let keys = s.filter(([, v], i) => i > 0 && i < n && (v - s[i - 1][1]) * (s[i + 1][1] - v) < 0 && Math.abs(v) > 0.3).map(([f, v]) => [+f.toFixed(3), +v.toFixed(2)]);
  keys = [[0, 0], ...keys, [1, 0]];
  for (let i = 1; i < keys.length; i++) if ((keys[i][0] - keys[i - 1][0]) * ms < 40) { // too close: keep the larger swing (the ends stay)
    const drop = i === keys.length - 1 || (i > 1 && Math.abs(keys[i - 1][1]) < Math.abs(keys[i][1])) ? i - 1 : i;
    keys.splice(drop, 1); i = 0;
  }
  if (keys.length > 2) targets.bonnet_pointe = { rotation: keys };
  return targets;
}
const poses = [['assis', 'assis'], ['livres', 'livres'], ['soir', 'soir'], ['reveil', 'reveil'], ...WORDS.map(w => [`ecrit_${w}`, 'ecrit'])];
module.exports = RES => {
  for (const [pose, family] of poses) {
    const name = `glose_${pose}_vd`, vd = PARTS[name].map(l => l.xml).join('\n'); // every layer of the pose, in drawing order
    const idle0 = IDLE0[`glose_${pose}`];
    for (const [kind, ms, make] of [['tap', TAP_MS, TAP[family] || TAP.awake], ['special', SPECIAL_MS, SPECIAL[family]]]) {
      const t = settle(sway(vd, ms, neck(vd, family, make(vd, family))), idle0, ms, n => under(vd, n, 'zzz'), Z_BACK[kind]);
      animated(`glose_${pose}_${kind}`, name, Object.assign(animators(name, ms, t, idle0), tethers(vd, family, ms, t)), RES);
    }
  }
};
