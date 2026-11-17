// POSE « SUR LA PILE » (ex-Virgule) : Glose lové en pain sur trois livres anciens, une patte pend et taquine
// la pelote ; queue en virgule dont le bout repasse par-dessus le tronc (croisement propre, sans trait doublé).
function draw(paper) {
  const C = makeC(VARIANT), anims = [];
  const D = [44, -34];
  const b1 = { name: 'livre_bas', x: 322, y: 818, w: 506, h: 96, d: D, cover: C.S ? C.S.low : C.bookCover ? C.night : C.sepia, coverD: C.S ? C.S.lowD : C.bookCover ? C.nightD : C.sepiaD, pages: C.parch, gold: C.gold, label: true, labelC: C.parch }, B1 = book(b1);
  const band = C.has('bandeau') ? prizeBand(B1, b1, 0.2, 0.34) : null;
  const B2 = book({ name: 'livre_milieu', x: 352, y: 724, w: 452, h: 84, d: D, cover: C.bord, coverD: C.bordD, pages: C.parch, gold: C.gold, bands: [0.12, 0.27, 0.73, 0.88] });
  const B3 = book({ name: 'livre_haut', x: 378, y: 636, w: 400, h: 78, d: D, cover: C.S ? C.S.top : C.bookCover ? C.sepia : C.night, coverD: C.S ? C.S.topD : C.bookCover ? C.sepiaD : C.nightD, pages: C.parch, gold: C.gold, label: true, labelC: C.gold, bands: [0.1, 0.24, 0.76, 0.9] });
  const rib = ribbonOrMark(C, [[836, 846], [840, 870], [842, 888]], [[854, 858], [866, 888], [884, 910], [916, 916]], 20);
  const shadowOn = (upper, lower, dx, dy) => fill(X(moved(upper.sil, dx, dy), S(lower.top, upper.sil)), INK, 0.22);
  const bookShadows = G(null, [shadowOn(B2, B1, 10, 12), shadowOn(B3, B2, 10, 12)]);

  // corps en pain
  const body = blob([[452, 650], [440, 566], [492, 484], [612, 450], [738, 460], [810, 516], [830, 596], [812, 650], [640, 660]], 0.5);
  const stripes = [560, 628, 694, 756].map((sx, i) => taper([[sx - 4, 440], [sx + 10, 492], [sx + 4 + i * 4, 540]], 30, 6));
  const bodyG = vol('corps', body, C.fur, [fill(X(U(...stripes), body), C.stripe), shadowOf(body, 30, 34, 0.17), rimOf(body, 12, 14),
    ...skinExtra(C, body, 'body', [[600, 500], [720, 520], [660, 600]]), line(curve([[694, 652], [702, 592], [748, 552], [806, 566]]), IN)]);
  const breath = breaths(41, { period: [3200, 4300] });
  anims.push({ id: 'corps', type: 'scale', pivot: [640, 655], life: along(breath, [1, 1], [1.012, 1.03]) });
  const seat = fill(X(moved(body, 8, 14), S(B3.top, body)), INK, 0.2);

  // queue en virgule
  const T = crossingTail(C, 'queue', [[770, 628], [812, 666], [826, 732], [818, 800], [786, 846], [736, 864], [692, 840], [682, 792], [706, 756], [752, 746], [800, 762], [846, 794]], 6, 50, 22,
    [[0.3, 0.46, 0.62, 0.8], [0.28, 0.55, 0.8]], [818, 774], 56);
  anims.push({ id: 'queue', type: 'rotate', pivot: [770, 636], life: sway(43, { half: [3000, 4400], amp: [2.2, 4.2], at: 1200 }) },
    { id: 'queue', type: 'rotate', pivot: [770, 636], life: gestures(44, { gap: [3600, 8000], at: 4300 }, flick(3.5)) }); // balancement, et le bout qui fouette

  // patte rentrée posée au bord du livre
  const paw2 = ell([588, 652], [44, 27]);
  const paw2G = vol(null, paw2, C.cream, [shadowOf(paw2, 8, 10, 0.18), ...toes([588, 652], 0, 2, 18, 22, paw2)]);

  // patte qui pend et taquine la pelote : brin enroulé au poignet
  const leg = arm(C, [[440, 588], [392, 640], [346, 708], [314, 768]], 66, 54, { c: [304, 786], rx: 38, ry: 31, rot: 14 }, { bands: [0.42, 0.66] });
  const rr = q => xy(P(q).add([-8, 22]).rotate(30, [310, 774]));
  const ringBack = line(arc(rr([278, 752]), rr([318, 737]), rr([358, 752])), 10, C.ballD);
  const ringFront = G(null, [line(arc(rr([278, 752]), rr([318, 768]), rr([358, 752])), 18), line(arc(rr([278, 752]), rr([318, 768]), rr([358, 752])), 10, C.ball)]);
  const thread = strand([[262, 812], [268, 800], rr([284, 766])], C, 10);
  const lien = tether('patte_lien', [262, 812], C); // suit la pelote qui saute
  const legG = G('patte', [ringBack, leg.group, lien[0], thread, lien[1], ringFront]);
  // il tapote la pelote à intervalles irréguliers, parfois deux fois de suite : la patte se lève, frappe, revient ; la pelote part au contact
  const pats = [], pat = gestures(45, { gap: [1300, 3900], at: 900 }, r => { const k = between(r, [0.7, 1]), two = r() < 0.25;
    const one = () => [[between(r, [400, 520]), 4 * k], [between(r, [380, 460]), -2 * k, 'in'], [between(r, [460, 560]), 0]];
    return two ? [...one(), [between(r, [120, 220]), 0], ...one()] : one(); });
  pat.forEach(([t, v], i) => { if (v < 0) pats.push([t, -v / 2]); });
  anims.push({ id: 'patte', type: 'rotate', pivot: [432, 596], life: pat });

  // tête : tournée vers la pelote (3/4 gauche), penchée
  const H = gloseHead(C, { at: [450, 432], tilt: -7, turn: -0.16, mood: 'curieux', quill: 24 }); // plume penchée : sa pointe restait coupée par le haut du cadre
  anims.push(...H.anims);
  neckShadow(bodyG, H.head, body, 10, 14);
  anims.push({ id: 'tete', type: 'translate', life: along(breath, [0, 0], [0, -3], 120) });
  anims.push({ id: 'tete', type: 'rotate', pivot: H.neck, life: headSway() });
  let scarfB = null, scarfF = null, bow = null;
  if (C.has('echarpe')) { const sc = scarf(C, { c: [520, 566], w: 96, h: 38, sag: 20, tail: [[642, 578], [664, 628], [674, 688]], tailFront: true }); scarfF = sc.band; }
  if (C.has('noeud')) bow = bowTie(C, [H.chin[0], H.chin[1] + 2], -10);
  const jg = C.has('grelot') ? jingle(C, [H.chin[0], H.chin[1] - 4], { w: 56 }) : {};
  const lav = C.has('lavalliere') ? lavalliere(C, [H.chin[0], H.chin[1] + 4], -10) : null;
  const objs = besides(C, [852, 908], 1, [892, 904], 1.05);
  neckBreath(anims, anims.find(a => a.id === 'corps'), scarfF, jg.band, bow, jg.bell, lav);

  const ballG0 = yarn('pelote', [240, 864], 64, C, [line(curve([[198, 836], [228, 822], [264, 828]]), 6, C.ballD)]);
  const loose = strand([[194, 906], [164, 920], [136, 910], [118, 920]], C, 8);
  const sol = tether('sol_lien', [194, 906], C, 8);
  const ballG = G('pelote_sol', [sol[0], loose, sol[1], ballG0]);
  const roll = [[0, 0]]; // au contact de la patte : la pelote roule d'un coup (départ vif), revient en se berçant
  pats.forEach(([t, k], i) => { const last = roll[roll.length - 1][0], t0 = Math.max(t, last + 60); if (t0 + 1100 > END) return;
    roll.push([t0, 0], [t0 + 280, -7 * k, 'out'], [t0 + 620, 1.2 * k], [t0 + 1000, 0]); });
  anims.push({ id: 'pelote', type: 'rotate', pivot: [240, 864], life: ms(roll.concat([[LIFE, 0]])) });
  const shadow = G('ombre', [floorShadow([580, 916], [330, 24], 0.32), floorShadow([236, 920], [80, 13], 0.32)]);
  const glint = rareGlints(C, [[250, 400, 16], [880, 470, 12], [620, 300, 10]], 'l_');
  anims.push(...glint.anims);
  paper.project.activeLayer.addChildren([shadow, B1.group, rib, band, ...objs, B2.group, bookShadows, B3.group, seat, T.group, scarfB, bodyG, paw2G, legG, ballG, scarfF, jg.band, H.group, bow, lav, jg.bell, glint.group].filter(Boolean));
  // yarn : bout du brin libre, d'où part le fil de la page (Postal.Yarn) ; cadre ouvert en haut (même bas) : les bonnets restent dedans quand
  // la tête se relève (double prise du toucher, spécial), Glose 4,4 % plus petit
  return { anims, yarn: [118, 920], viewBox: [75, 110, 900, 900], title: 'Monsieur Glose lové sur une pile de livres, la patte qui taquine sa pelote' };
}
