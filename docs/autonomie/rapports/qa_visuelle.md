# QA visuelle de fin de nuit (a548b1a, comparé à 08a954d)

Relecteur visuel, lecture seule sur le code de l'appli. Captures rendues à a548b1a (`--offline … renderDemoImages, WidgetTest, GloseTest`, BUILD SUCCESSFUL), copiées dans `scratchpad/night/qa/after/` ; avant : `scratchpad/night/qa/before/app/build/demo/`. Extraits cités : `scratchpad/night/qa/`.

Statut : terminé (vers 02 h 30, Paris). Aucun bloquant, 1 important, 8 mineurs (dont 3 déjà là avant la nuit). Aucune modification du code de l'appli ; deux tests jetables (liseré, dernière tuile, nom changé) ont été supprimés aussitôt après leur exécution.

## Constats

### F1 (important, presque bloquant pour le lot Glose) — Le liseré crème de nuit dépend du cache de drawables : absent la nuit après un passage de jour, présent le jour après un passage de nuit

- **Captures** : `ui/home_dark.png`, `ui/onboarding_*_dark.png` (pas de liseré : `qa/zoom_home_dark_glose_after.png`, identique à `zoom_home_dark_glose_before.png`) ; `ui/collection_dark.png` (liseré présent) ; `ui/collection_light.png` (liseré crème **de jour** autour du chat, du livre et du ruban, absent de `collection.png` rendu à froid : `qa/F1_halo_jour_collection_avant_apres_nuit.png`).
- **Preuve** : test jetable (supprimé) dans le même processus Robolectric, code du framework réel (`ResourcesImpl`, `VectorDrawable`) : accueil rendu directement de nuit → liseré (11 645 px crème) ; accueil rendu de jour puis passage `+night` → pas de liseré (4 898 px, = jour), même pour un `Glose.mine()` neuf, non animé. `qa/F1_halo_nuit_froid_vs_apres_jour.png` (gauche à droite : nuit à froid animé, nuit à froid fixe, jour, nuit après jour animé, nuit après jour fixe).
- **Cause** : `Glose.drawable` (`Glose.kt:27`) thème `ContextThemeWrapper(ctx, R.style.Glose)` + overlays : la clé de thème est la même de jour et de nuit, et `gloseHalo` passe par `@color/glose_halo` (`values/colors.xml:40`, `values-night/colors.xml:33`). L'état constant mis en cache (`ThemedResourceCache`, clé = thème) n'est pas purgé au changement d'`uiMode` : la couleur résolue la première fois est réutilisée. Sur téléphone, le cas est courant : thème sombre automatique au coucher du soleil, appli restée en mémoire. **À confirmer sur l'appareil** : ouvrir l'appli de jour, passer en sombre depuis les réglages rapides, revenir.
- Les captures de la suite donnent donc une image fausse : l'accueil de nuit n'a jamais montré le liseré, et la planche `glose_nuit_avant_apres.jpg` a dû être rendue à froid.
- **Correctif minimal** : rendre la clé de thème différente la nuit. Dans `Glose.drawable` : `if (ctx.resources.configuration.isNightModeActive) t.theme.applyStyle(R.style.Glose_Nuit, true)` avec un style `Glose.Nuit` qui met `gloseHalo` à `#F0E8D8`, et `gloseHalo` du style `Glose` en littéral `#00F0E8D8` (plus de `@color/glose_halo`). À faire dans le générateur (`design/mascotte/android/make_android.js`), qui écrit `glose_styles.xml`. Ajouter à `renderInterface` une capture de nuit **après** les captures de jour (c'est déjà le cas) et un test qui compte les pixels crème de `home_dark`.
- Même cause dans l'album de nuit : le timbre « Sur son volume » (tigre, réglages par défaut, déjà rendu de jour) n'a pas de liseré, ses voisins « Sieste dans la tasse » et « Sur la pile » en ont un (`qa/F1_timbre_assis_sans_liseré_nuit.png`, `ui/collection_dark.png` ; idem « Reliure bordeaux »).

### F2 (mineur) — Détail : le lien « Source de l'anecdote » est centré, il est aligné à gauche sur l'accueil

- **Captures** : `ui/mots_detail.png`, `ui/mots_detail_dark.png`, `ui/mots_detail_w360_font200.png` (`qa/F2_detail_lien_source_centre.png` contre `qa/F2_accueil_lien_source_gauche.png`).
- **Cause** : `DetailActivity.kt:97` ajoute le `Button` de style `Link` sans paramètres : largeur `match_parent` dans la feuille verticale, donc texte centré. L'accueil le pose en `wrap_content` avec `marginStart -8dp` (`activity_main.xml:331-337`). Le restyle du Détail visait justement « au style de l'accueil ».
- **Correctif** : `sheet.addView(Button(...), LinearLayout.LayoutParams(-2, -2).apply { marginStart = px(-8) })`.

### F3 (mineur) — Réglages : « Rétablir « Monsieur Glose » » est proposé alors que le nom est déjà celui par défaut

- **Captures** : `ui/settings_light.png`, `ui/settings_dark.png`, `ui/settings_w360_font200.png` (`qa/F3_retablir_nom_par_defaut.png`). L'action est sans effet, et à ×2 le lien centré coupe « Monsieur / Glose ».
- **Cause** : `SettingsActivity.kt:117-120` le montre toujours.
- **Correctif** : `visibility = if (p.gloseName == getString(R.string.glose_name)) View.GONE else View.VISIBLE`, recalculé dans `saveName()` et dans le clic.

### F4 (mineur) — Widget citation : la signature, passée en romain, paraît plus grande que la citation

- **Captures** : `widget/4x2_quote.png` (`qa/F4_widget_signature_romain.png` ; avant : `qa/F4_widget_signature_avant.png`).
- L'italique distinguait l'attribution. En romain, elle a la même police et la même couleur que le libellé « Citation du jour », à 13 sp fixes (`widget.xml:100`), alors que le texte est en taille automatique de 12 à 16 sp (`widget.xml:84-86`) et tombe vers 12 sp sur une longue citation. L'attribution se lit comme un paragraphe de plus, plus gros que la citation.
- **Correctif minimal** : un tiret d'attribution, `"— ${item.signature}"` (`Widget.kt:77`), et 12 sp pour `widget_auteur`.

### F5 (mineur) — Quiz : la croix ✗ est optiquement plus petite et plus légère que la coche ✓

- **Captures** : `ui/quiz_faux.png`, `ui/quiz_dark_faux.png`, `ui/quiz_trous_faux.png`, `ui/quiz_w360_font200_faux.png` (`qa/F5_quiz_croix_petite.png`).
- **Cause** : `ic_cross.xml:5` trace de 7 à 17 sur 24 (10 unités, environ 7,5 dp dans la case de 18 dp), alors que `ic_check.xml` va de 5 à 19 (14 unités). Le commentaire dit pourtant « ic_check's twin ».
- **Correctif** : `M6,6L18,18M18,6L6,18`.

### F6 (mineur, déjà là avant) — En-tête compact : la date se coupe entre « 8 » et « NOVEMBRE »

- **Captures** : `ui/home_font200.png`, `ui/home_w360_font200_long.png` (`qa/F6_date_coupee_entete_compact.png`). La coupure existait avant la nuit, mais l'en-tête compact la met plus en vue.
- **Correctif** : `MainActivity.kt:127`, motif `"EEEE d MMMM"` (Courier Prime a U+00A0).

### F7 (mineur, données, déjà là avant) — Citations : ponctuation haute collée, visible sur l'accueil du jour

- **Captures** : `ui/home_*.png`, `app.png`, `lockscreen_0.png`, `share_0.png` : « Notes sur Paris: vie et opinions… » (`qa/F7_citation_deux_points_colles.png`).
- Le scan de `citations.json` (`qa/tools/typo.py`) trouve 22 champs avec « ; : ! ? » collés ou précédés d'une espace ordinaire, par exemple `oeuvre` « Qu'est-ce qu'une nation? conférence… » et `texte` « c'est l'impossible; trop bas ». Certains textes anciens gardent leur graphie (« connoître; ») ; les titres d'œuvre (`oeuvre`) et les textes modernes sont à corriger.
- **Correctif** : corriger les champs `oeuvre` dans `citations.json` (U+202F avant « ; : ! ? »), ou normaliser `Quote.signature` (`Quotes.kt:10`) avec la règle du reste de l'interface.

### F8 (mineur, déjà là avant) — Le rabat des enveloppes dépasse du coin arrondi

- **Captures** : `ui/home_light.png`, `ui/home_dark.png` (`qa/F8_rabat_enveloppe_deborde.png`, agrandi 4 fois) : les branches du V sortent de 1 à 2 px du coin arrondi.
- **Cause** : `btn_envelope.xml:11,17,26`, le rabat est en retrait de 1 à 3 dp sur un coin de rayon plus grand.
- **Correctif** : un retrait gauche et droit égal au rayon de l'enveloppe. Rien d'urgent.

### F9 (mineur) — Couverture des captures

- `ui/home_deviner.png` montre une citation aujourd'hui, car le premier créneau du 8 novembre est une citation : le voile « Touchez pour révéler » n'est pas capturé. Correctif : ouvrir un mot, `main(<clé d'un mot>)`.
- La suite ne vérifie aucune capture de nuit **après** un rendu de jour (voir F1). Elle ne capture ni le dernier choix de Couleurs et polices, ni un nom de Glose changé. Je les ai rendus à part, avec un test jetable supprimé ensuite : `qa/extra/styles_last_light.png`, `styles_last_dark.png`, `home_renamed*.png`, `settings_renamed.png`, `collection_renamed.png`. **Rien à signaler** : la dernière tuile garde son contour et sa coche, la ligne « Il en faut au moins une : Bleu nuit reste. » s'affiche, et le nom long tient sur 3 lignes à l'accueil (5 à ×2) sans toucher le fil.

## Vérifié sans constat

- **Détail** (clair, sombre, ×2) : carte postale de rayon 16 dp avec liseré, timbre et cachet ; mot à la taille de l'accueil (`fitWord`) ; nature sans italique ; actions en rangée d'icônes (2 colonnes à ×2, libellés entiers) ; carnet Seyès pour l'exemple ; feuille pour l'anecdote. Un seul élément fort (timbre et cachet).
- **Anneau de focus de l'étoile** : entier, non coupé, dans la teinte du mot, sur l'accueil et le Détail (`ui/home_focus_star.png`, `ui/mots_detail_focus_star.png`).
- **Bandes de l'album** : au quart bas du timbre, hors du visage de Glose, absentes des cases verrouillées. La nuit, voile blanc à 10 %, lisible.
- **Definiday Serif et U+202F** : « ; : ! ? », « % » et « » » sans repli visible dans l'interface. Les polices de mot sans U+202F passent par `Styles.fine` (espace insécable ordinaire avant « ; : ! ? »). Seule exception, voulue : la note en Dancing Script (« à glisser au dîner ! »).
- **En-tête compact** (×2, à 360 et 411 dp) : Glose à 96 dp, titre pleine largeur, fil à environ 8 dp du titre, sans chevauchement.
- **Quiz** : icônes ✓/✗ à la place de la case, options désactivées lisibles de jour comme de nuit, carte « Noter » cohérente (`quiz_fin_noter.png`).
- **Glose de jour** (diff au pixel avec 08a954d) : seuls changent le livre allégé (accueil) et le liseré de F1 (`collection_light`).
- **Réglages** : champ du nom au style des autres champs, aide à 1,15 d'interligne, libellés des menus identiques.
- **Mes mots** : filtre « À revoir seulement », compteur « 60 % » avec espace fine.
- **Widget** : texte en romain, guillemets avec U+202F en police système (qui l'a).
