// Planche d'essai de la cursive (outil d'itération, pas un livrable).
function draw(paper) {
  const C = makeC(VARIANT), words = (C.v.words || 'mot,bravo,rare,dodo,encore').split(',');
  const items = [fill(rect(0, 0, 1024, 1024), '#F6EEDF')];
  words.forEach((w, i) => {
    const ox = 50 + (i % 2) * 500, oy = 210 + Math.floor(i / 2) * 300;
    const { path } = placeWord(w, [ox, oy], 1.0);
    if (path.bounds.width > 440) path.scale(440 / path.bounds.width, [ox, oy]);
    items.push(line(path.clone(NI), 18), line(path.clone(NI), 10, C.ball), line(moved(path, -1.5, -2), 2.8, '#FFFFFF', 0.3));
    if (C.v.dots) path.segments.forEach(s => items.push(fill(circ(s.point, 3), '#0060FF', 0.6)));
  });
  paper.project.activeLayer.addChildren(items);
  return { viewBox: [0, 0, 1024, 1024] };
}
