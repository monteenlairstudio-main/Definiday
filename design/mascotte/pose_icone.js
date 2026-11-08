// VERSION « ICÔNE » : la tête seule, traits épaissis ×1,7 (lisible de 24 à 128 px) : icône de notification,
// raccourci, pastille de l'écran « collection ». Mêmes lunettes, même tête que toutes les poses.
function draw(paper) {
  SW = 1.7;
  const C = makeC(VARIANT);
  const v = C.v, tilt = v.tilt == null ? -5 : v.tilt; // variantes de l'icône d'appli (make_android.js GLOSE_ICON) : whisk, ear, tilt
  const H = gloseHead(C, { at: [500, 352], tilt, turn: 0.12, glare: false, sleep: !!v.sleep, whisk: v.whisk, ear: v.ear });
  paper.project.activeLayer.addChildren([H.group]);
  return { anims: H.anims, viewBox: [240, 92, 520, 520], title: 'Monsieur Glose (icône)' };
}
