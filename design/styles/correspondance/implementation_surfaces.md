# Correspondance : surfaces dessinées (écran verrouillé, partage, widget)

Surfaces peintes par le code, indépendantes du thème de l’appli. Captures : `./gradlew --offline :app:testDebugUnitTest --tests 'fr.definiday.DefinidayTest.renderDemoImages' --tests 'fr.definiday.WidgetTest.renderWidgets'`, puis `app/build/demo/` (`lockscreen_*`, `quote_*`, `style_*`, `share_*`) et `app/build/demo/widget/`.

## Aides `Postal` (`app/src/main/java/fr/definiday/Postal.kt`)

Fonctions `Canvas` réutilisables par l’appli (carte de l’accueil, album, quiz). Tailles en px ; `dp` = px d’un dp à l’échelle du dessin. Tout est décoratif (`contentDescription` nul) : la date et le mot sont toujours écrits en clair ailleurs.

| Fonction | Dessine | Notes |
|---|---|---|
| `drawStamp(c, rect, paper, ink, initial?, face?, vignette?)` | timbre perforé : feuille `paper` moins des trous ronds (`Path.op(DIFFERENCE)`, rayon = largeur / 22, pas ≈ 3,2 rayons), filet intérieur `ink`, puis l’initiale centrée sur son encre (`getTextBounds`) ou une vignette bitmap sur fond `ink` | carte : `paper` = `mot`, `ink` = `fond_haut` |
| `drawPostmark(c, ctx, cx, cy, r, color, date, wavesTo, arcs = true)` | cachet : double anneau, « DEFINIDAY » sur l’arc haut, le jour de la semaine sur l’arc bas (`drawTextOnPath`), « 6 NOV » et l’année au centre, trois ondes d’oblitération jusqu’à `wavesTo` ; couleur à 75 % | `arcs = false` sous ~16 dp de rayon (widget) : la date seule, plus grande |
| `drawThread(c, x0, x1, y, dp, palette)` | fil de laine ondulé terminé par une boucle : brin plein 2,6 dp + brin torsadé 1,1 dp en tirets 1,6 / 2,8 dp | `strands(palette)` : `fil_clair` plein sur les palettes sombres, `fil` sur les claires |
| `mention(ctx, size, color)` | `TextPaint` Courier Prime capitales, interlettrage 0,13 em (gras synthétique : pas de coupe grasse) | « LETTRE DU 6 NOVEMBRE », « VIA DEFINIDAY » |
| `day(date)`, `initial(ctx, item, style)` | « 6 novembre » / « 1er mai » ; initiale du timbre dans la police **suggérée** du mot (même décorative), celle de l’auteur pour une citation | |

## Écran verrouillé (`WordCard.render`)

De haut en bas, dans le bloc ancré au-dessus des raccourcis (84 % de la hauteur, 40 % au plus, réduction du texte jusqu’à 0,6) : « LETTRE DU 6 NOVEMBRE » (`card_letter`, ou « CITATION DU… » `card_quote`) + cachet + timbre 0,12 × 0,14 de la largeur ; le mot dans sa police et sa couleur ; le fil, entré par le bord gauche, juste sous les jambages de la dernière ligne, boucle après le mot ; nature, définition, exemple en Libre Baskerville (une seule serif de texte, pas d’italique : la couleur `exemple` signale la citation) ; signature « Glose » en Dancing Script, couleur `nature`, mots seulement (redondante, jamais seule porteuse d’information ; nom fixe en attendant un nom personnalisable). Pas de dessin de Glose.

Contrastes (WCAG, contre les deux extrémités du dégradé, 18 palettes ; test `DefinidayTest.lockCardKeepsContrastOnEveryPalette`) :

| Élément | Exigence | Minimum |
|---|---|---|
| Mention, nature, signature (`nature`) | 4,5:1 | 4,61:1 (sanguine, lavande, craie) |
| Feuille du timbre (`mot`) | 3:1 | 6,15:1 (aube) |
| Initiale (`fond_haut` sur `mot`) | 4,5:1 | 7,55:1 (aube) |
| Fil, brin plein | 3:1 | 3,61:1 (sépia, `fil`) ; 3,67:1 (vert-de-gris, `fil_clair`) |
| Cachet (`nature` à 75 %) | décor | 2,9:1 (craie) à 4,6:1 |

## Partage (`WordCard.share`, 1080 × 1350)

Carte postale sur kraft (`#EBDDC2`) : liseré `feuillet` (`#FFFCF5`) à ombre douce, face au dégradé de la palette, même bloc que l’écran verrouillé centré, la tête de Glose en vignette du timbre (permis ici), « VIA DEFINIDAY » (`share_via`) en mention postale au pied.

## Widget (`Widget.kt`, `res/layout/widget.xml`)

Liseré `feuillet` de 4 dp (`widget_frame`), dégradé de la palette dedans (`widget_bg` + `widget_fade`, rayon `widget_radius_inner`, `system_app_widget_inner_radius` en 12+), marges intérieures 12 dp (16 dp au total, comme avant : le calcul de taille du mot reste juste). Le coin de 40 dp est un bitmap (`Widget.stamp`, ~120 px) : timbre à l’initiale dans la police suggérée et cachet daté sans arcs. RemoteViews ne peut ni tracer ni changer de police : le texte reste en `serif` système. L’aperçu du sélecteur (`previewLayout`) montre `drawable-nodpi/widget_stamp_preview.png`, produit par `WidgetTest.renderWidgets` (`stamp_preview.png`).
