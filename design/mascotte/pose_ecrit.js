// POSE « LE FIL ÉCRIT » : assis sur son volume, Glose serre sa pelote d'une patte et, de l'autre levée,
// guide le fil qui écrit un mot en cursive d'un seul trait puis se rembobine (stroke-dashoffset ⇄ trimPathEnd).
function draw(paper) {
  const C = makeC(VARIANT), word = C.v.word || 'mot';
  const { L, anims, H } = seatedGlose(C, { head: { turn: 0.24, tilt: 2, at: [508, 352], mood: 'fier' }, tree: [292, 686] });
  // la boucle commence et finit mot écrit (= le dessin fixe) : tenu → rembobinage → caché → écriture. Les réactions, le retour à la vie et
  // l'arrêt de la vie au repos tombent ainsi sur le mot écrit, sans saut. Pelote et tête : écart à la pose « mot écrit ».
  const DUR = 7, KT = [0, 0.31, 0.45, 0.53, 1];

  // pelote serrée contre lui (patte proche dessous), tourne quand le fil se dévide / se rembobine
  const ballC = [500, 616];
  const ballG = yarn('pelote', ballC, 68, C);
  anims.push({ id: 'pelote', type: 'rotate', pivot: ballC, values: [0, 0, -150, -150, 0], keyTimes: KT, dur: DUR });
  const near = arm(C, [[430, 504], [404, 552], [414, 604], [442, 624]], 64, 54, { c: [452, 632], rx: 40, ry: 30, rot: -18 });

  // bras lointain levé, coude marqué (touffe), patte ouverte (coussinets) qui pince le fil
  const PAW = P([770, 414]);
  const raised = arm(C, [[606, 512], [680, 524], [738, 486], [766, 440]], 60, 50, { c: xy(PAW), rx: 38, ry: 34, rot: 12 }, { pads: true, bands: [0.3, 0.52, 0.74] });
  const elbowTuft = blob([[688, 548], [700, 558], [706, 544], [716, 540], [702, 528]], 0.45);
  const raisedG = G('bras_leve', [fill(X(moved(raised.shape, 10, 16), L.body.children[0]), INK, 0.14), vol(null, U(raised.shape, elbowTuft), C.fur, raised.group.children.slice(1, -1))]);

  // fil : de la pelote à la patte (derrière le bras), puis de la patte au mot (devant)
  const low = strand([[548, 576], [592, 548], [644, 522], [704, 488], [760, 444]], C, 10, 'fil_bas');
  const K = 0.82, slant = -0.2;
  let w = placeWord(word, [0, 0], 1, slant);
  const k = Math.min(K, 276 / w.path.bounds.width);
  w = placeWord(word, [0, 0], k, slant);
  const O = P([PAW.x - 36, 262]);
  w.path.translate(O);
  const lead = curve([xy(PAW.add([-4, -26])), xy(PAW.add([-8, -70])), xy(PAW.add([-26, -112])), xy(O)]);
  lead.lastSegment.handleIn = w.path.firstSegment.handleOut.normalize(-30);
  lead.join(w.path);
  const LEN = Math.ceil(lead.length) + 10;
  const mk = (name, wdt, col, op) => { const t = line(lead.clone(NI), wdt, col, op); t.name = name; t.dashArray = [LEN, LEN + 40]; t.dashOffset = 0; return t; };
  const strokes = [mk('fil_a', 20, INK), mk('fil_b', 11, C.ball), mk('fil_c', 3, '#FFFFFF', 0.3)];
  strokes[2].translate([-1.5, -2]);
  for (const s of strokes) anims.push({ id: s.name, attr: 'stroke-dashoffset', values: [0, 0, LEN, LEN, 0], keyTimes: KT, dur: DUR });
  const fil = G('fil', strokes);
  // la patte se pose par-dessus le départ du fil : deux doigts le pincent
  const pinch = G(null, [fill(X(circ(PAW.add([-4, -24]), 16), raised.shape), C.cream), line(arc(PAW.add([-20, -22]), PAW.add([-4, -12]), PAW.add([12, -24])), IN)]);
  // la tête suit l'écriture (léger hochement vers le mot)
  anims.push({ id: 'tete', type: 'rotate', pivot: H.neck, values: [0, 0, -3, -3, 0], keyTimes: KT, dur: DUR });

  const glint = rareGlints(C, [[290, 300, 16], [930, 420, 12], [640, 120, 10]], 'e_');
  anims.push(...glint.anims);
  paper.project.activeLayer.addChildren([L.shadow, L.book, L.seat, L.ribbon, L.tree, L.scarfBack, L.body, L.feet, L.tailShadow, L.tail, low, raisedG, ballG,
    G('pattes', [near.group]), L.scarf, L.jingleBand, L.head, L.bow, L.jingle, fil, pinch, glint.group].filter(Boolean));
  // yarn : pas de bout libre (le fil écrit) : le fil de la page sort de derrière le bas du livre
  return { anims, yarn: [312, 846], viewBox: [170, 50, 900, 900], title: `Monsieur Glose écrit « ${word} » avec le fil de sa pelote` };
}
