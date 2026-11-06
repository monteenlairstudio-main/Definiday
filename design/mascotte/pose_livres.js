// POSE « SUR LA PILE » (ex-Virgule) : Glose lové en pain sur trois livres anciens, une patte pend et taquine
// la pelote ; queue en virgule dont le bout repasse par-dessus le tronc (croisement propre, sans trait doublé).
function draw(paper) {
  const C = makeC(VARIANT), anims = [];
  const D = [44, -34];
  const B1 = book({ name: 'livre_bas', x: 322, y: 818, w: 506, h: 96, d: D, cover: C.S ? C.S.low : C.bookCover ? C.night : C.sepia, coverD: C.S ? C.S.lowD : C.bookCover ? C.nightD : C.sepiaD, pages: C.parch, gold: C.gold, label: true, labelC: C.parch });
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
  anims.push({ id: 'corps', type: 'scale', pivot: [640, 655], values: [[1, 1], [1.012, 1.03], [1, 1]], dur: 3.6 });
  const seat = fill(X(moved(body, 8, 14), S(B3.top, body)), INK, 0.2);

  // queue en virgule
  const T = crossingTail(C, 'queue', [[770, 628], [812, 666], [826, 732], [818, 800], [786, 846], [736, 864], [692, 840], [682, 792], [706, 756], [752, 746], [800, 762], [846, 794]], 6, 50, 22,
    [[0.3, 0.46, 0.62, 0.8], [0.28, 0.55, 0.8]], [818, 774], 56);
  anims.push({ id: 'queue', type: 'rotate', pivot: [770, 636], values: [0, 4, 0, -3, 0], dur: 7.3 });

  // patte rentrée posée au bord du livre
  const paw2 = ell([588, 652], [44, 27]);
  const paw2G = vol(null, paw2, C.cream, [shadowOf(paw2, 8, 10, 0.18), ...toes([588, 652], 0, 2, 13, 22)]);

  // patte qui pend et taquine la pelote : brin enroulé au poignet
  const leg = arm(C, [[440, 588], [392, 640], [346, 708], [314, 768]], 66, 54, { c: [304, 786], rx: 38, ry: 31, rot: 14 }, { bands: [0.42, 0.66] });
  const rr = q => xy(P(q).add([-8, 22]).rotate(30, [310, 774]));
  const ringBack = line(arc(rr([278, 752]), rr([318, 737]), rr([358, 752])), 10, C.ballD);
  const ringFront = G(null, [line(arc(rr([278, 752]), rr([318, 768]), rr([358, 752])), 18), line(arc(rr([278, 752]), rr([318, 768]), rr([358, 752])), 10, C.ball)]);
  const thread = strand([[262, 812], [268, 800], rr([284, 766])], C, 10);
  const legG = G('patte', [ringBack, leg.group, thread, ringFront]);
  anims.push({ id: 'patte', type: 'rotate', pivot: [432, 596], values: [0, 4, -2, 0, 0], keyTimes: [0, 0.18, 0.36, 0.55, 1], dur: 2.6 });

  // tête : tournée vers la pelote (3/4 gauche), penchée
  const H = gloseHead(C, { at: [450, 432], tilt: -7, turn: -0.16 });
  anims.push(...H.anims);
  anims.push({ id: 'tete', type: 'translate', values: [[0, 0], [0, -3], [0, 0]], dur: 3.6 });
  anims.push({ id: 'tete', type: 'rotate', pivot: H.neck, values: [0, -2.5, 0, 1.5, 0], dur: 6 });
  let scarfB = null, scarfF = null, bow = null;
  if (C.has('echarpe')) { const sc = scarf(C, { c: [520, 566], w: 96, h: 38, sag: 20, tail: [[642, 578], [664, 628], [674, 688]], tailFront: true }); scarfF = sc.band; }
  if (C.has('noeud')) bow = bowTie(C, [H.chin[0], H.chin[1] + 2], -10);
  const jg = C.has('grelot') ? jingle(C, [H.chin[0], H.chin[1] - 4], { w: 56 }) : {};
  const tree = C.has('sapin') ? paperTree(C, [892, 904], 1.05) : null;

  const ballG0 = yarn('pelote', [240, 864], 64, C, [line(curve([[198, 836], [228, 822], [264, 828]]), 6, C.ballD)]);
  const loose = strand([[194, 906], [164, 920], [136, 910], [118, 920]], C, 8);
  const ballG = G('pelote_sol', [loose, ballG0]);
  anims.push({ id: 'pelote', type: 'rotate', pivot: [240, 864], values: [0, -7, 0, 0], keyTimes: [0, 0.3, 0.6, 1], dur: 2.6 });
  const shadow = G('ombre', [floorShadow([580, 916], [330, 24], 0.32), floorShadow([236, 920], [80, 13], 0.32)]);
  const glint = rareGlints(C, [[250, 400, 16], [880, 470, 12], [620, 300, 10]], 'l_');
  anims.push(...glint.anims);
  paper.project.activeLayer.addChildren([shadow, B1.group, rib, tree, B2.group, bookShadows, B3.group, seat, T.group, scarfB, bodyG, paw2G, legG, ballG, scarfF, jg.band, H.group, bow, jg.bell, glint.group].filter(Boolean));
  return { anims, viewBox: [90, 150, 860, 860], title: 'Monsieur Glose lové sur une pile de livres, la patte qui taquine sa pelote' };
}
