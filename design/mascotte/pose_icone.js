// VERSION « ICÔNE » : la tête seule, traits épaissis ×1,7 (lisible de 24 à 128 px) : icône de notification,
// raccourci, pastille de l'écran « collection ». Mêmes lunettes, même tête que toutes les poses.
function draw(paper) {
  SW = 1.7;
  const C = makeC(VARIANT);
  const H = gloseHead(C, { at: [500, 352], tilt: -5, turn: 0.12, glare: false, sleep: !!C.v.sleep, whisk: C.v.whisk }); // whisk 0.85 : icône d'appli (masque adaptatif)
  paper.project.activeLayer.addChildren([H.group]);
  return { anims: H.anims, viewBox: [240, 92, 520, 520], title: 'Monsieur Glose (icône)' };
}
