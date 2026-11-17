// POSE DU SOIR (ex-Tisane) : Monsieur Glose endormi dans sa tasse bleu nuit, menton sur le bord, lunettes glissées
// sur le nez ; soucoupe sur deux livres, bougie à gauche, « z » qui naissent contre sa tête, vapeur et sachet à droite.
function draw(paper) {
  const C = makeC(VARIANT), anims = [];
  const cupC = C.S ? C.S.cup : C.cup || C.night, cupIn = C.cup ? '#4A1E26' : '#18203A', cupLit = C.cup ? '#B0606A' : '#4A5E92';

  // ---------- livres et soucoupe ----------
  const b1 = { name: 'livre_bas', x: 120, y: 884, w: 790, h: 78, d: [40, -30], cover: C.sepia, coverD: C.sepiaD, pages: C.parch, gold: C.gold, label: true, labelC: C.parch, bands: [0.06, 0.16, 0.84, 0.94] }, B1 = book(b1);
  const band = C.has('bandeau') ? prizeBand(B1, b1, 0.64, 0.76) : null;
  const B2 = book({ name: 'livre_haut', x: 150, y: 800, w: 730, h: 76, d: [40, -30], cover: C.bord, coverD: C.bordD, pages: C.parch, gold: C.gold, bands: [0.06, 0.16, 0.84, 0.94] });
  const rib = ribbonOrMark(C, [[888, 836], [894, 872], [898, 900]], [[892, 838], [902, 880], [916, 920], [946, 940]], 18);
  const bookSh = fill(X(moved(B2.sil, 10, 12), S(B1.top, B2.sil)), INK, 0.22);
  const SC = P([540, 784]);
  const saucer = U(ell(SC, [272, 28]), ell(SC.add([0, 9]), [258, 24]));
  const gSaucer = G('soucoupe', [fill(saucer.clone(NI), cupC), fill(ell(SC.add([0, -3]), [214, 15]), '#FFFFFF', 0.12), shadowOf(saucer, 50, 0, 0.25),
    edgeLine(ell(SC.add([0, 2]), [258, 22]), rect(0, SC.y + 4, 1100, 60), 5, C.gold), outline(saucer)]);

  // ---------- tasse ----------
  const RC = P([540, 540]), RX = 262, RY = 52;
  const rimEll = ell(RC, [RX, RY]), interior = ell(RC.add([0, -2]), [RX - 20, RY - 12]);
  const bp = [[-1, 0], [-0.97, 0.36], [-0.83, 0.7], [-0.5, 0.93], [0, 1]];
  const bowl = U(blob(bp.concat(bp.slice(0, 4).reverse().map(([x, y]) => [-x, y])).map(([x, y]) => [RC.x + x * RX, RC.y + y * 236]).concat([[RC.x, RC.y + 20]]), 0.5), rimEll.clone(NI));
  const foot = ell(RC.add([0, 228]), [132, 16]);
  const HC = [RC.x + RX + 12, 640];
  const handle = S(S(ell(HC, [88, 78]), ell(HC, [52, 44])), bowl);
  const cupSil = U(bowl, foot, handle);
  const upperRim = X(rimEll, rect(0, 0, 1100, RC.y));
  const cupFront = S(S(cupSil, interior), upperRim);
  // intérieur lisible : fond sombre + paroi du fond éclairée (croissant) + liseré d'or tout autour de l'ouverture
  const backWall = X(interior, S(interior, moved(interior, 0, 26)));
  const gCupBack = G('tasse_fond', [fill(cupSil.clone(NI), cupC), fill(upperRim.clone(NI), '#FFFFFF', 0.14),
    fill(interior.clone(NI), cupIn), fill(backWall, cupLit, 0.9), fill(X(interior, moved(ell(RC.add([-150, -10]), [90, 30]), 0, 0)), C.glow, 0.18),
    line(interior.clone(NI), IN), outline(cupSil)]);
  // décor : lune et 3 étoiles, grandes (lisibles à 128 px)
  const deco = X(S(bowl, ell(RC, [RX + 4, RY + 18])), U(S(circ([RC.x + 118, 690], 44), circ([RC.x + 136, 678], 38)),
    sparkleShape([RC.x - 40, 690], 34), sparkleShape([RC.x + 20, 640], 18), sparkleShape([RC.x - 150, 650], 20)));
  const enamel = blob([[RC.x - 236, 612], [RC.x - 222, 606], [RC.x - 192, 690], [RC.x - 150, 746], [RC.x - 158, 750], [RC.x - 210, 698]], 0.5);
  const glowL = X(S(cupSil, moved(cupSil, 18, 0)), cupFront);
  const gCupFront = G('tasse', [fill(cupFront.clone(NI), cupC), fill(X(rimEll, cupFront), '#FFFFFF', 0.14), fill(deco.clone(NI), C.gold), line(deco, 3, INK, 0.5),
    fill(X(shadowOf(cupSil, 64, 0, 1), cupFront), INK, 0.2), fill(glowL, C.glow, 0.35),
    fill(X(enamel, cupFront), '#FFFFFF', 0.45), fill(circ([RC.x - 206, 632], 6), '#FFFFFF', 0.7), fill(ell([RC.x + RX + 24, 594], [16, 7]).rotate(30), '#FFFFFF', 0.35),
    edgeLine(rimEll, rect(0, RC.y, 1100, 200), 7, C.gold), edgeLine(interior, rect(0, RC.y, 1100, 200), IN),
    edgeLine(cupSil, rect(0, RC.y + 1, 1100, 400), OUT)]);

  // ---------- Glose dans la tasse ----------
  const body = blob([[330, 600], [346, 520], [440, 466], [580, 440], [660, 462], [698, 530], [692, 600], [340, 610]], 0.5);
  const bodyStripes = X(body, U(...[[590, 458], [632, 474], [672, 506]].map(([x, y], i) => taper([[x, y - 8], [x - 10 - i * 4, y + 66]], 30 - i * 3, 6))));
  const gBody = G('corps', [vol(null, body, C.fur, [fill(bodyStripes, C.stripe), shadowOf(body, 40, 14, 0.16), rimOf(body, 12, 10, 0.3, C.glow),
    ...skinExtra(C, body, 'body', [[620, 500], [700, 540]])])]);
  // endormi : souffle lent (4,8 à 5,8 s, longue expiration, pause au creux) ; réveillé : souffle ordinaire. « dormeur » et l'écharpe suivent.
  const breath = C.v.awake ? breaths(51, { period: [3600, 4400] }) : breaths(52, { period: [4800, 5800], inhale: 0.38, pause: [200, 900], sigh: 0.15 });
  anims.push({ id: 'corps', type: 'scale', pivot: [540, 600], life: along(breath, [1, 1], [1.012, 1.035]) });
  // queue drapée par-dessus le bord, à gauche, bout relevé
  const tailPts = [[352, 584], [314, 600], [296, 642], [300, 694], [306, 732], [286, 760], [260, 752], [258, 730]];
  const tail = taper(tailPts, 46, 26);
  const gTail = vol('queue', tail, C.fur, [fill(X(ringsAlong(tailPts, [0.3, 0.5, 0.7], 40, 8), tail), C.stripe), fill(X(circ([258, 730], 22), tail), C.furD),
    shadowOf(tail, 12, 10, 0.16), rimOf(tail, 8, 6, 0.35, C.glow)]);
  anims.push({ id: 'queue', type: 'rotate', pivot: [350, 586], life: sway(53, { half: [3400, 5200], amp: [1, 2.6], at: 1800 }).map(([t, v]) => [t, v > 0 ? v : +(-0.2 * v).toFixed(3)]) }); // posée sur le bord : d'un seul côté
  // tête endormie posée sur le bord, pattes croisées dessous
  const awake = !!C.v.awake; // « Réveil dans la tasse » : yeux ouverts, lunettes en place, bougie éteinte, sans « z »
  const H = awake ? gloseHead(C, { at: [486, 432], tilt: -6, turn: -0.08 }) : gloseHead(C, { at: [486, 440], tilt: -9, turn: -0.08, sleep: true, glassesDrop: 16, glassesTilt: 5, glare: false, prefix: '' });
  anims.push(...H.anims);
  const pawL = pawShape([430, 610], 48, 28, -8), pawR = pawShape([548, 616], 46, 27, 6);
  const paws = G('pattes', [pawR, pawL].map(p => vol(null, p, C.cream, [shadowOf(p, 12, 10, 0.12), ...toes(p.position, 0, 2, 18, 24, p)])));
  let scarfG = null, bow = null;
  if (C.has('echarpe')) { const sc = scarf(C, { c: [620, 600], w: 70, h: 34, sag: 10, tail: [[640, 600], [652, 650], [650, 700]], tailFront: true }); scarfG = sc.band; }
  if (C.has('noeud')) bow = bowTie(C, [H.chin[0] + 20, H.chin[1] + 4], -9, 1);
  const jg = C.has('grelot') ? jingle(C, [H.chin[0] + 14, H.chin[1] - 6], { w: 50, s: 0.85 }) : {};
  const lav = C.has('lavalliere') ? lavalliere(C, [H.chin[0] + 20, H.chin[1] + 4], -9, 0.95, 0.55) : null;
  const objs = besides(C, [868, 788], 0.8);
  const gHead = G('dormeur', [paws, jg.band, H.group, bow, lav, jg.bell]); // les pièces du cou dans « dormeur » : elles le suivent
  gCupFront.insertChild(gCupFront.children.length - 3, fill(X(moved(U(H.head, pawL, pawR), 8, 14), cupFront), INK, 0.22)); // ombre portée de la tête et des pattes sur la tasse
  neckBreath(anims, anims.find(a => a.id === 'corps'), scarfG); // nœud et grelot sont dans « dormeur »
  anims.push({ id: 'dormeur', type: 'scale', pivot: [490, 620], life: along(breath, [1, 1], [1.008, 1.02], 150) });

  // ---------- bougie ----------
  const CX = 178, CT = 640, base = 790;
  const holder = U(ell([CX, base + 6], [64, 12]), ell([CX, base], [44, 10]));
  const wax = U(rpoly([[CX - 32, CT], [CX + 32, CT], [CX + 32, base + 2], [CX - 32, base + 2]], [6, 6, 2, 2]), ell([CX, CT], [32, 9]),
    blob([[CX - 32, CT], [CX - 16, CT + 2], [CX - 14, CT + 42], [CX - 23, CT + 56], [CX - 32, CT + 42]], 0.5), blob([[CX + 6, CT + 6], [CX + 20, CT + 4], [CX + 18, CT + 26], [CX + 12, CT + 34], [CX + 6, CT + 24]], 0.5));
  const FY = CT - 30;
  const flameO = blob([[CX + 2, FY - 76], [CX + 14, FY - 44], [CX + 18, FY - 20], [CX + 2, FY], [CX - 14, FY - 20], [CX - 10, FY - 44]], 0.5);
  const flameI = blob([[CX + 2, FY - 44], [CX + 8, FY - 25], [CX + 2, FY - 6], [CX - 4, FY - 25]], 0.5);
  const halo = circ([CX, FY - 30], 190);
  halo.fillColor = { gradient: { stops: [[C.glow, 0], [new Color(1, 0.84, 0.56, 0.35), 0.45], [new Color(1, 0.84, 0.56, 0), 1]], radial: true }, origin: [CX, FY - 30], destination: [CX + 190, FY - 30] };
  const gCandle = G('bougie', [vol(null, holder, C.gold, [shadowOf(holder, 30, 0, 0.2)]), vol(null, wax, C.white, [shadowOf(wax, 22, 0, 0.12), fill(ell([CX, CT], [24, 5]), INK, 0.12)]),
    line(curve([[CX, CT - 6], [CX + 2, FY - 4]]), 5),
    awake ? null : G('flamme', [fill(flameO.clone(NI), C.gold), fill(flameI, C.glow), outline(flameO, IN)])]);
  const gHalo = awake ? null : G('halo', [halo]);
  // flamme qui vacille sans motif (extrêmes tous les 0,16 à 0,38 s), lueur qui respire plus lentement ; départ et fin sur le dessin fixe
  if (!awake) anims.push({ id: 'flamme', type: 'scale', pivot: [CX + 2, FY], life: scaleOf(flicker(54, { step: [300, 650], amp: [0.04, 0.1] }), -1, 1) },
    { id: 'halo', attr: 'opacity', life: flicker(55, { step: [900, 1600], amp: [0.04, 0.12], hold: 0.85 }).map(([t, v]) => [t, +(1 - Math.abs(v - 1) * (v < 1 ? 1.6 : 0.5)).toFixed(3)]) });

  // ---------- vapeur, « z », sachet ----------
  // vapeur : deux volutes qui montent tour à tour, montées de 3,9 à 5 s, parfois un temps sans vapeur (vie.js particles)
  const vap = particles(56, { from: [0, 20], to: [0, -50], rise: [3900, 5000], gap: [0, 900], peak: 0.5, still: 0.3, lanes: 2 });
  const steam = (id, x, y, d) => {
    const v = vap[id === 'vap1' ? 0 : 1];
    anims.push({ id, type: 'translate', life: v.pos }, { id: id + '_o', attr: 'opacity', life: v.alpha }); // peak 0.5 while it rises (svg2avd: replaces the path's own 0.3, the still)
    const s = fill(taper([[x, y], [x - 18, y - 40], [x + 12, y - 80], [x - 8, y - 120], [x + 6, y - 160]], 16, 3), C.sepia, 0.32); s.name = id + '_o';
    return G(id, [s]);
  };
  const steamG = G('vapeur', [steam('vap1', 720, 500, 0), steam('vap2', 758, 516, -2.2)]);
  // « z » : naissent contre l'oreille gauche et montent en s'éloignant, en crescendo
  // « z » : à chaque expiration, les trois montent l'un après l'autre (0,9 s d'écart), petit, moyen, grand
  const exhale = breath.filter(([, k]) => k > 0).map(([t]) => t);
  const zs = [0, 1, 2].map(i => particles(57 + i, { from: [10, 16], to: [-14, -22], rise: [3400, 3800], peak: 1, still: 1, fade: [0.25, 0.7], at: exhale.map(t => t + 300 + 900 * i) })[0]);
  const zz = (id, x, y, s, d) => {
    const v = zs[+id[1] - 1];
    anims.push({ id, type: 'translate', life: v.pos }, { id: id + '_o', attr: 'opacity', life: v.alpha });
    const z = G(id + '_o', [line(curve([[x - s, y - s], [x + s, y - s], [x - s, y + s], [x + s, y + s]].map(xy0 => xy0), 0), Math.max(7, s * 0.4) + 6), line(new Path({ segments: [[x - s, y - s], [x + s, y - s], [x - s, y + s], [x + s, y + s]], insert: false }), Math.max(7, s * 0.4) - 1, C.white)]);
    z.children[0].segments.forEach(sg => { sg.handleIn = null; sg.handleOut = null; });
    return G(id, [z]);
  };
  const ear = H.T([346, 140]);
  const zG = awake ? null : G('zzz', [zz('z1', ear[0] - 34, ear[1] + 34, 13, 0), zz('z2', ear[0] - 62, ear[1] - 12, 18, -1.2), zz('z3', ear[0] - 84, ear[1] - 70, 24, -2.4)]);
  const tag = rpoly([[732, 622], [776, 622], [776, 674], [732, 674]], 6), leaf = blob([[754, 634], [766, 646], [754, 666], [742, 646]], 0.5);
  const gTea = G('sachet', [line(curve([[742, 554], [750, 572], [754, 594], [754, 622]]), 3.5), vol(null, tag, C.white, [shadowOf(tag, 8, 0, 0.12), fill(leaf, C.gold), line(curve([[754, 640], [754, 662]]), 2.5)], IN)]);
  anims.push({ id: 'sachet', type: 'rotate', pivot: [744, 556], life: sway(58, { half: [1900, 2700], amp: [2.4, 4.5], at: 600 }) }); // starts on the still (0°)
  const ground = fill(ell([540, 964], [440, 22]), INK, 0.14);
  const glint = rareGlints(C, [[880, 360, 14], [640, 300, 10], [940, 540, 10]], 's_');
  anims.push(...glint.anims);
  paper.project.activeLayer.addChildren([ground, gHalo, B1.group, rib, band, B2.group, bookSh, ...objs, gSaucer, gCupBack, steamG, gBody, gCupFront, gTea, gTail, gCandle, scarfG, gHead, zG, glint.group].filter(Boolean));
  // yarn : pas de pelote : le fil de la page sort de derrière le bas du livre du dessous
  return { anims, yarn: [312, 958], viewBox: [60, 110, 920, 920], title: awake ? 'Monsieur Glose réveillé dans sa tasse, le thé du matin' : 'Monsieur Glose endormi dans sa tasse bleu nuit, à la lueur d’une bougie' };
}

