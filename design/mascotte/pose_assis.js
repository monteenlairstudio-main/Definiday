// POSE « ASSIS » (par défaut) : Monsieur Glose assis sur son gros volume, serre sa pelote.
// Une patte la soutient par-dessous, l'autre est posée dessus ; queue enroulée devant les pieds ; tête 3/4 penchée.
function draw(paper) {
  const C = makeC(VARIANT);
  const { L, anims, H } = seatedGlose(C);
  const ballC = [528, 606];
  const ballG = yarn('pelote', ballC, 76, C);
  anims.push({ id: 'pelote', type: 'rotate', pivot: ballC, values: [0, 6, 0, -6, 0], dur: 3.8 });
  // brin libre : sort sous la pelote, file sur le livre et pend sur sa face et passe le bord du bas (le liseré de nuit compris), d’où part le fil de la page (épais : lisible à 128 px)
  const brin = strand([[496, 682], [476, 714], [430, 728], [370, 726], [318, 720], [296, 742], [298, 778], [312, 806], [320, 830], [323, 858]], C, 10, 'brin');
  const lien = tether('brin_lien', [496, 682], C); brin.insertChild(0, lien[0]); brin.addChild(lien[1]); // se déroule quand la pelote roule
  const near = arm(C, [[430, 504], [404, 552], [414, 600], [444, 618]], 64, 54, { c: [456, 626], rx: 40, ry: 30, rot: -18 });
  const far = arm(C, [[606, 500], [636, 536], [636, 566], [612, 576]], 58, 50, { c: [604, 574], rx: 36, ry: 28, rot: -24 });
  const arms = G('pattes', [far.group, near.group]);
  const glint = rareGlints(C, [[300, 300, 16], [760, 470, 12], [690, 210, 10]], 'a_');
  anims.push(...glint.anims);
  paper.project.activeLayer.addChildren([L.shadow, L.book, L.seat, L.ribbon, L.tree, L.scarfBack, L.body, L.feet, L.tailShadow, L.tail, brin, ballG, arms, L.scarf, L.jingleBand, L.head, L.bow, L.jingle, glint.group].filter(Boolean));
  // yarn : bout du brin, juste sous le bord du bas du livre : le fil de la page (Postal.Yarn, style correspondance § 9) part du bout visible
  return { anims, yarn: [323, 858], viewBox: [110, 70, 820, 820], title: 'Monsieur Glose, chat bibliothécaire à lunettes dorées, assis sur un gros livre, serre sa pelote' };
}
