# Catalogue de skins et d’accessoires de Monsieur Glose : structure de collection et proposition de catalogue

Légende. **[fait]** : sourcé (lien) ou constaté dans le dépôt (chemin donné). **[proposition]** : proposition de design, à valider. Tout le catalogue (section 3) est une **proposition de design**, et non un état de l’existant.

Rien n’a été modifié dans le code. Lu : `app/src/main/java/fr/definiday/Unlocks.kt`, `Glose.kt`, `Prefs.kt`, `design/mascotte/README.md`, `mise_en_place.md`, `notes.md`, `glose_lib.js`, `app/src/main/res/values/glose_attrs.xml` et `glose_styles.xml`, `design/mascotte/apercu.png`, la capture `revue_ui/collection_light.png` et `reports/Benchmark applis mot du jour.md`.

---

## 1. Comment les meilleurs systèmes de collection sont structurés (raretés, séries, saisons et FOMO, doublons, aperçus, rythme, gagné ou acheté)

### Takeaway
Les jeux « cosy » de référence associent un grand catalogue fini, tout entier visible dès le départ, à des conditions d’obtention qui découlent du comportement voulu (les objets posés attirent les chats rares, les dons complètent les paquets, le sommeil enregistre des styles). La rareté y est un palier lisible, souvent en étoiles. Les séries récompensent leur complétion. Le levier le plus critiqué est la rareté artificielle limitée dans le temps (FOMO). Pour Definiday, la piste la plus sûre est un système **déterministe** : sans tirage, donc sans doublon, avec des fenêtres saisonnières qui **reviennent chaque année**, des séries thématiques qui rapportent un cadre ou un décor une fois complètes, et des déblocages fondés sur la mémorisation différée.

### Cited Findings
**Neko Atsume**
- 86 chats à collectionner en version 1.15.0, dont 26 « rares » qui n’apparaissent que si certains objets et certaines nourritures sont posés dans le jardin — [Wikipedia](https://en.wikipedia.org/wiki/Neko_Atsume)
- Les chats laissent en partant des poissons d’argent ou d’or (la monnaie) et parfois des « souvenirs » (mementos) à collectionner. Un mot de passe quotidien donne des poissons, et des poissons s’achètent aussi en argent réel. Le jeu est gratuit et n’a « pas de fin » tant qu’on pose de la nourriture — [Wikipedia](https://en.wikipedia.org/wiki/Neko_Atsume)
- 10 millions de téléchargements au 4 décembre 2015, prix CEDEC du meilleur game design (août 2015) — [Wikipedia](https://en.wikipedia.org/wiki/Neko_Atsume)
- Les chats rares ont chacun leur liste d’objets préférés, et certains ne laissent leur souvenir qu’après plusieurs visites — résumé de recherche fondé sur le wiki Fandom ([Rare Cats](https://nekoatsume.fandom.com/wiki/Rare_Cats?oldid=8730)) et des guides ([iMore](https://www.imore.com/neko-atsume-game-guide-how-collect-all-cats/4)). Le wiki Fandom a répondu HTTP 402 en lecture directe : non vérifié ligne à ligne.

**Stardew Valley (séries à compléter)**
- Le Centre communautaire compte 6 salles et 30 paquets (bundles). Chaque paquet complété donne une récompense immédiate. Une salle complète déclenche une récompense plus grande à la fin de la journée, avec une cinématique (par exemple la serre restaurée ou le pont vers la carrière). Tout compléter restaure le bâtiment entier. Certains paquets acceptent une qualité supérieure à celle demandée — [Stardew Valley Wiki, Bundles](https://stardewvalleywiki.com/Bundles)

**Pokémon Sleep (collection fondée sur un comportement réel)**
- Chaque Pokémon a plusieurs « styles de sommeil », recensés dans un Sleep Style Dex. La rareté est notée en étoiles : de 1 à 4 étoiles selon la page récente, de 1 à 5 selon une version plus ancienne. C’est une contradiction entre versions de la même source — [Bulbapedia, Sleep style](https://bulbapedia.bulbagarden.net/wiki/Sleep_style)
- La première île (Greengrass Isle) compte 52 Pokémon et 157 styles. Des objectifs de Dex (« Dex goals ») récompensent l’enregistrement de nouveaux styles. Des « semaines de découverte » facilitent les styles manquants — [Bulbapedia, Greengrass Isle](https://m.bulbapedia.bulbagarden.net/wiki/Greengrass_Isle), [Serebii, Dex goals](https://serebii.net/pokemonsleep/dexgoals.shtml)

**Finch**
- Les tâches de soin de soi rapportent des « rainbow stones », qui s’échangent contre des vêtements et des meubles pour l’oiseau — [Bustle](https://www.bustle.com/wellness/finch-app-review-features-price) (d’après le résumé de recherche, page non lue en entier)
- Des événements saisonniers (par exemple « Woodland Whimsy » en mars) prennent fin et sortent de la boutique. Aucune source fiable ne dit s’ils reviennent — [Lemon8, publication d’utilisateur, source faible](https://www.lemon8-app.com/@rosar2015/7437952244085654072?region=us). Le centre d’aide de Finch a répondu HTTP 403.

**Animal Crossing: New Horizons**
- Les objets saisonniers sont rangés dans un onglet à part des « Special Goods » de Nook Shopping et paraissent à l’approche d’une fête — [Twinfinite](https://twinfinite.net/guides/animal-crossing-new-horizons-buy-seasonal-items/)
- Les objets du solstice d’été étaient proposés du 7 juin au 6 juillet. Un article ultérieur donne la même fenêtre, ce qui indique une fenêtre récurrente. D’autres objets sont des exceptions ponctuelles (le mobilier Mario d’un anniversaire) — [Animal Crossing World](https://animalcrossingworld.com/2020/06/make-sure-to-get-your-summer-solstice-crown-winter-solstice-sweater-in-animal-crossing-new-horizons), [GamesRadar](https://www.gamesradar.com/animal-crossing-new-horizons-adds-winter-and-summer-solstice-items-to-the-nook-shop)

**Duolingo**
- L’éditeur d’avatar officiel propose coiffures, carrures, teintes de peau et accessoires, dont des lunettes, depuis la page de profil. Il est distinct de la boutique de gemmes — [Blog Duolingo](https://blog.duolingo.com/avatar-creator/)
- Les tenues de Duo achetables en gemmes : leur disponibilité actuelle est contradictoire. Un utilisateur qui revenait n’en trouvait plus, une réponse dit que les gemmes ne servent plus qu’aux bonus et aux cœurs — [Lemmy](https://lemmy.dbzer0.com/post/6240265). Non confirmé par une source officielle.

**FOMO et rareté artificielle**
- « The entire point of FOMO is to drive up the value of content with that fear » ; « cosmetic-only monetization is still abusive even if there is no gameplay attached to it » ; « Keeping content held hostage is not how you can build a long-term sustainable game » — [Game Developer](https://www.gamedeveloper.com/design/how-video-games-abuse-the-fear-of-missing-out)
- Alternative citée : une archive permanente, où les anciens objets restent accessibles, supprime la rareté artificielle — [Wayline](https://www.wayline.io/blog/artificial-scarcity-manipulating-games) (d’après le résumé de recherche, non lu en entier)

**Definiday aujourd’hui [fait, dépôt]**
- 21 éléments en 4 types (8 poses, 4 pelages, 4 pelotes, 5 accessoires). Chaque élément est débloqué par un seul compteur (`q` quiz, `p` sans-faute, `r` réussis, `s` série) et un seuil. Un déblocage n’est jamais retiré, et une série cassée ne reprend rien. Un élément verrouillé apparaît en silhouette grise avec ce qui manque en clair (« Encore 3 soirs de quiz d’affilée ») — `Unlocks.kt`, `CollectionActivity.kt`, capture `collection_light.png`
- Un seul accessoire peut être porté à la fois (`Kind.ACC`, une valeur dans `Glose.choice`) — `Glose.kt`

### Inferences
- **Rareté** [proposition] : 4 paliers nommés comme des éditions, avec des étoiles pour la lisibilité, sur le modèle des étoiles de Pokémon Sleep. ★ **Poche** (courant), ★★ **Broché**, ★★★ **Relié** (rare : scintillement possible, comme les pelages rares actuels), ★★★★ **Édition originale** (légendaire : éclats animés et silhouette en « ? » avec un indice poétique).
- **Séries** [proposition] : environ 15 collections thématiques de 6 à 17 éléments. Compléter une collection débloque un **cadre** ou un **décor** (une condition `e:`), comme le Centre communautaire de Stardew. Aucune collection ne demande un élément saisonnier pour être complétée la première année : la complétion compte les éléments non saisonniers, et les saisonniers sont un bonus.
- **Doublons** : aucun, faute de tirage. Neko Atsume tient son charme de l’aléa des visites, mais pour Definiday un tirage brouillerait le lien « j’ai appris, donc j’obtiens ». Il ouvrirait aussi la porte à une mécanique de type boîte à butin.
- **Saisons sans FOMO** : chaque fenêtre revient chaque année, dure au moins une semaine et ne demande qu’un quiz (au plus 3) dans la fenêtre. Hors fenêtre, l’élément s’affiche « Revient en mars », jamais « Expiré ». C’est la récurrence observée chez Animal Crossing, sans la date unique.
- **Visibilité** : garder la silhouette et l’indice. Ajouter une ligne « Prochain : Monture écaille (encore 4 bonnes réponses) » en fin de quiz, et ne jamais montrer plus de 3 prochains objectifs : au-delà, cela devient une liste de corvées.
- **Rythme** (cible) : mois 1, environ 15 déblocages ; mois 2, environ 10 ; mois 3 à 6, environ 4 par mois ; ensuite 2 à 3 par mois et les fêtes. Avec environ 150 éléments futurs, la complétion prend 2 à 3 ans à 1 mot par jour. C’est l’ordre de grandeur de Neko Atsume (86 chats) ou de la première île de Pokémon Sleep (157 styles).
- **Gagné ou acheté** : tout se gagne, sans monnaie. Une monnaie créerait une boucle d’économie, des prix et un équilibrage à maintenir, ce qu’on peut ignorer tant qu’il n’y a pas de boutique. Pour un **pack de soutien** facultatif plus tard : il ne contiendrait que des remerciements cosmétiques hors des collections (aucun ne compte pour une complétion), jamais d’élément saisonnier ni d’accélération des déblocages. On ne vend pas un raccourci à l’apprentissage. Exemples en section 3.
- **Nom personnalisable** : aucun nom d’élément ne contient « Glose » (« Sa madeleine » plutôt que « La madeleine de Glose »).

### Gaps
- Le wiki Fandom de Neko Atsume (HTTP 402) et le centre d’aide de Finch (HTTP 403) n’ont pas pu être lus. Le nombre de souvenirs, les prix et le retour des saisons de Finch restent non confirmés.
- Aucune conférence de design (GDC ou autre) sur les systèmes de collection n’a été lue dans le budget de recherche.
- Les sources ne disent pas explicitement si les objets saisonniers d’Animal Crossing reviennent chaque année. La récurrence est déduite de deux articles qui donnent la même fenêtre.

---

## 2. Conditions de déblocage qui récompensent l’apprentissage et calendrier culturel français

### Takeaway
Le benchmark recommande de récompenser le **rappel différé** plutôt que le sans-faute du soir même. Il faut pour cela la révision espacée à paliers fixes (J+2, J+7, J+30, J+90), qui n’existe pas encore. Le catalogue garde donc deux étages. La **base** n’utilise que des compteurs existants ou déductibles de l’historique. Le **futur** s’appuie sur des compteurs de mots retenus, une série tolérante et des fenêtres de dates du calendrier du livre et de la langue.

### Cited Findings
- Benchmark interne : une exposition unique suivie d’un QCM le soir même donne un bon score du jour et un faible souvenir un mois plus tard. Il recommande 1 à 2 questions de révision par soir à J+2, J+7, J+30 et J+90, en s’appuyant sur Cepeda et al. 2008 et Rowland 2014 — `reports/Benchmark applis mot du jour.md` ([PubMed Cepeda](https://pubmed.ncbi.nlm.nih.gov/19076480/), [PubMed Rowland](https://pubmed.ncbi.nlm.nih.gov/25150680/))
- Le dépôt n’a aucune révision espacée aujourd’hui. Les compteurs disponibles sont `quizzes`, `perfectQuizzes`, `passedQuizzes`, `correctAnswers` (présent dans `Prefs` mais inutilisé par `Unlocks`) et `streak`. L’historique (clé mot ou « q: » + citation, vers le jour affiché) permet de compter les mots vus et les citations lues — `Prefs.kt`, `MainActivity.kt`, `Words.kt`
- **Semaine de la langue française et de la Francophonie** : du 13 au 21 mars 2027, organisée chaque année autour du 20 mars (Journée internationale de la Francophonie) — [education.gouv.fr](https://www.education.gouv.fr/semaine-de-la-langue-francaise-et-de-la-francophonie-7643)
- **Dis-moi dix mots 2026-2027** : thème « d’un monde à savourer ». Les dix mots sont boucané, caramélisé, concocter, épluchette, ferment, mandoline, millésime, papilles, pimenter et zeste — [Eduscol](https://eduscol.education.gouv.fr/1912/dis-moi-dix-mots-concours-des-dix-mots)
- **Printemps des Poètes 2027** : du 8 au 31 mars, thème « La poésie : zones d’action » — [Agenda culturel](https://75.agendaculturel.fr/festival/printemps-des-poetes-paris.html), [Livres Hebdo](https://www.livreshebdo.fr/article/le-printemps-des-poetes-devoile-le-theme-de-son-edition-2027), [ActuaLitté](https://actualitte.com/article/132087/salons-festivals/apres-la-liberte-le-printemps-des-poetes-2027-choisit-l-engagement)
- **Nuits de la lecture 2027** : du 20 au 24 janvier, thème « L’enfance ». Créées en 2017 par le ministère de la Culture, pilotées par le Centre national du livre — [Sortir à Paris](https://www.sortiraparis.com/en/what-to-visit-in-paris/walks/guides/267162-nuits-de-la-lecture-2027-in-paris-and-ile-de-france-dates-and-theme). Page officielle du ministère non trouvée.
- **Journée mondiale du livre et du droit d’auteur** : le 23 avril, créée par l’UNESCO en 1995 — [education.gouv.fr](https://www.education.gouv.fr/journee-mondiale-du-livre-et-du-droit-d-auteur-467526)
- **Partir en Livre** (CNL, livre jeunesse, gratuit) : du 17 juin au 19 juillet 2026, thème « Nos petits et grands héros ». En 2025 : du 18 juin au 20 juillet. Les dates 2027 ne sont pas publiées — [CNL, communiqué](https://centrenationaldulivre.fr/sites/default/files/2026-03/CP_Partir%20en%20Livre%2026_Lancement_VF.pdf), [Sortir à Paris](https://sortiraparis.com/en/what-to-do-in-paris/shows-and-fairs/articles/120254-partir-en-livre-2025-date-and-theme-of-this-major-children-s-book-festival)

### Inferences
**Compteurs proposés.** Une lettre par compteur, comme `Item.on` aujourd’hui. On garde un seul compteur par élément : une condition composée n’apporterait que du code.

| Code | Compteur | Disponible | Ce qu’il récompense |
|---|---|---|---|
| `q` `p` `r` `s` | quiz, sans-faute, réussis, série stricte | **oui** (existants) | régularité ; `p` à éviter désormais (sans-faute du soir même) |
| `c` | bonnes réponses | **oui** (`correctAnswers`, une ligne dans `Unlocks.value`) | effort (avantage les réglages à 3 mots) |
| `m` | mots vus (historique, hors « q: ») | **oui**, déductible de l’historique | lecture quotidienne |
| `k` | citations lues (clés « q: ») | **oui**, déductible de l’historique | goût des citations |
| `l` | mots dont on a ouvert un lien Académie ou TLFi, **distincts** | non : un `Set` dans `Prefs` | curiosité ; « distincts » empêche de tapoter en boucle |
| `v` | mots retenus : bonne réponse à une révision ≥ J+7 | non : dépend de la révision espacée | **rappel différé** |
| `V` | mots ancrés : bonne réponse à J+30 | non : idem | mémoire à un mois |
| `g` | mots gravés : bonne réponse à J+90 | non : idem | mémoire durable (légendaires) |
| `h` | semaines actives : au moins 4 soirs de quiz sur 7, consécutives ou non | non : une ligne dans `Quiz.record` | **série tolérante**, qu’une soirée manquée ne casse pas |
| `d:MM-JJ..MM-JJ` | au moins un quiz dans la fenêtre, n’importe quelle année | non : un champ `window` | calendrier culturel, sans FOMO |
| `e:<série>` | tous les éléments non saisonniers de la série | non : un champ `set` | complétion |

Estimation [proposition, à mesurer] : à 1 mot par jour et environ 80 % de réussite, `v` progresse d’environ 0,8 par jour après la première semaine, `V` d’autant après un mois, `g` après trois mois. Avec 3 mots par jour, ces rythmes triplent. Pour ne pas trop avantager ces réglages, les seuils des légendaires restent assez hauts pour se gagner en plus d’un an à 1 mot par jour.

**Fenêtres saisonnières** [proposition]. Les dates sont élargies pour ne pas dépendre de l’édition de l’année. Chaque fenêtre revient chaque année. La première année, une fenêtre manquée laisse l’élément visible avec « Revient en … ».

| Fenêtre | Événement | Source des dates |
|---|---|---|
| `01-02..01-20` | Épiphanie, galette | usage (non sourcé ici) |
| `01-15..01-31` | Nuits de la lecture (20 au 24 janvier en 2027) | Sortir à Paris, ci-dessus |
| `01-28..02-08` | Chandeleur (2 février) | usage (non sourcé ici) |
| `02-10..02-16` | Saint-Valentin, « billet doux » | usage |
| `03-08..03-31` | Printemps des Poètes | Agenda culturel, Livres Hebdo |
| `03-13..03-23` | Semaine de la langue française et de la Francophonie (20 mars) | education.gouv.fr |
| `03-29..04-05` | 1er avril, poisson en papier | usage ; fenêtre élargie volontairement (un seul jour, ce serait du FOMO) |
| `04-16..04-30` | Journée mondiale du livre (23 avril) | education.gouv.fr |
| `04-25..05-08` | 1er mai, muguet | usage |
| `06-15..08-31` | Été, Partir en Livre (mi-juin à mi-juillet) | CNL |
| `08-20..10-15` | Rentrée littéraire | **non sourcé** (voir Gaps) |
| `10-24..11-02` | Halloween | usage |
| `11-01..11-20` | Saison des prix littéraires (bandeau rouge) | **non sourcé** |
| `12-01..12-31` | Avent et Noël | usage |

À éviter : les récompenses de connexion quotidienne (du type mot de passe du jour de Neko Atsume), qui récompensent l’ouverture de l’appli et non l’apprentissage. À éviter aussi : les fenêtres d’un jour et les compteurs qui baissent.

### Gaps
- Dates de la Rentrée littéraire et de la saison des prix : aucune source officielle cherchée (budget). La fenêtre proposée est une convention de design.
- Fêtes fixes (Épiphanie, Chandeleur, 1er mai, Halloween, Noël) : dates usuelles, non sourcées ici.
- Le rythme réel de `v`, `V` et `g` dépend de la conception de la révision (1 ou 2 questions par soir, gestion des erreurs). À mesurer une fois la fonction écrite.
- Partir en Livre 2027 : dates non publiées.

---

## 3. Catalogue (proposition de design) : base élargie et catalogue futur

### Takeaway
On garde les 21 éléments existants et on ajoute **14 éléments de base**, tous faisables avec le pipeline actuel (surcouches de thème, une forme simple dans la tête) et atteignables en 60 soirs : environ 30 éléments obtenus au bout de 2 mois. S’y ajoute un **catalogue futur de 170 éléments** répartis en 18 tableaux thématiques (cadres et décors compris). Les déblocages futurs s’appuient surtout sur les mots retenus (`v`, `V`, `g`), la curiosité (`l`), les semaines actives (`h`) et le calendrier (`d:`). Deux contraintes du pipeline dictent la faisabilité. D’abord, une teinte correspond à un rôle : séparer quelques jetons (`gloseStripe`, `gloseSpecs`) ouvre de nombreuses variantes « simples ». Ensuite, tous les accessoires sont aujourd’hui dessinés, cachés, dans chaque dessin : au-delà d’une vingtaine, il faudra des calques par emplacement.

### Cited Findings
**Faits du pipeline, qui fondent la colonne « faisabilité »** [fait, dépôt]
- Pelage = 3 jetons de thème (`gloseFur`, `gloseFurD`, `gloseCream`) plus, pour les pelages rares, des détails dessinés partout et cachés par défaut (`paillettes_*`, `etoiles_*`, éclats animés `gloseGlint_*`) — `glose_lib.js` (SKINS, skinExtra, rareGlints), `glose_styles.xml`
- Pelote = 2 jetons (`gloseBall`, `gloseBallD`), qui colorent aussi le fil qui écrit les mots — `glose_lib.js` (YARNS, strand)
- Livre(s) sous le chat : jetons déjà séparés (`gloseBook`, `gloseBookD`, `gloseBookTop`, `gloseBookLow`…), et « Nuit d’encre » les remplace déjà par du bordeaux pour que le chat ne se fonde pas dans le livre — `glose_attrs.xml`, `glose_styles.xml`
- Les accessoires sont des groupes nommés dessinés dans chaque pose. Leurs couleurs sont des attributs transparents par défaut, posés par `Glose.Acc.*`. Le bonnet et la plume sont dans le groupe `tete`, qu’on n’écrit qu’une fois (`gloseHead`, commune à toutes les poses) et qui suit ses animations — `glose_lib.js`, `mise_en_place.md` §3.3
- Les couleurs d’un accessoire existant sont déjà des attributs (`gloseBonnet_c75b54`, `gloseNoeud_2c3a5e`…). Une **variante de couleur** d’un accessoire existant ne coûte donc qu’une surcouche de style — `glose_styles.xml`
- Les lunettes utilisent `C.gold`, la même teinte que les pois du nœud, les rayures de l’écharpe et le gland du marque-page. Il faut un jeton `gloseSpecs` séparé pour changer de monture — `glose_lib.js` (gloseHead, l. 175 à 182)
- Les rayures du tigré sont des bandes de la couleur du pelage. Pour un pelage uni (chartreux, noir, blanc), elles doivent recevoir leur propre jeton (`gloseStripe`) — `glose_lib.js` (ringsAlong, arm)
- Environ 180 chemins par pose. La documentation Android conseille de limiter les vecteurs et prévient du coût du premier chargement. La solution de repli prévue superpose deux `ImageView` (le corps et un AVD « accessoire » de même viewport et de mêmes durées) — `mise_en_place.md` §3.3 et §3.5
- Les mots du fil doivent s’écrire d’un seul trait : pas de « i », de « j », d’accent ni de cédille. Au-delà de 6 lettres, le mot devient petit à 128 px — `notes.md` (Cursive), `mise_en_place.md` §5
- La notification « tête seule » et les icônes coupent chapeaux et plumes (`accessory = false`) — `Glose.kt` (mine)

### Inferences

#### 3.0 Conventions
- **Emplacements** [proposition] : on scinde `Kind.ACC` en `TETE`, `COU`, `YEUX` (monture), `OBJET` (posé près de Glose ou sur le livre) et `LIVRE`, et on ajoute `MOT` (le fil écrit…), `DECOR` (fond derrière Glose) et `CADRE` (bordure de l’aperçu). `Glose.choice` gère déjà une valeur par type : ajouter des types revient à ajouter des entrées à l’enum, et on peut porter un accessoire par emplacement.
- **Faisabilité** :
  - **S** (simple) : une surcouche de style ou une forme d’interface (cadre). Environ 15 minutes.
  - **S\*** : simple, après une séparation de jeton faite une fois pour toutes (`gloseStripe`, `gloseSpecs`). Environ 1 heure en tout.
  - **M** (moyen) : un nouveau groupe de formes simples (cercles, `rpoly`, `blob`). Dans la tête, on le dessine une fois et il vaut pour toutes les poses ; au cou, sur le livre ou posé à côté, il faut le placer dans 3 familles de poses (assis et écrit, pile, tasse). Une à trois heures.
  - **C** (complexe) : nouvelle pose, silhouette modifiée, motif à nombreux chemins ou transparence. Une demi-journée ou plus, avec itérations d’autocritique.
- **Rareté** : P = Poche ★, B = Broché ★★, R = Relié ★★★, O = Édition originale ★★★★.
- **Ids** : snake_case ASCII. Ceux qui sont proposés ne collisionnent avec aucun id existant.

#### 3.1 Existant (ne pas dupliquer) — `Unlocks.kt`
| id | Nom | Type | Condition | Soir estimé (1 / 3 mots par jour, d’après mise_en_place.md) |
|---|---|---|---|---|
| `assis` | Assis sur son volume | pose | défaut | 0 |
| `ecrit_mot` | Le fil écrit « mot » | pose | q≥1 | 1 |
| `ecrit_encore` | Le fil écrit « encore » | pose | q≥1 | 1 |
| `ecrit_bravo` | Le fil écrit « bravo » | pose | r≥1 | ~1 |
| `soir` | Endormi dans sa tasse | pose | s≥3 | 3 |
| `ecrit_dodo` | Le fil écrit « dodo » | pose | s≥3 | 3 |
| `livres` | Sur la pile de livres | pose | q≥10 | 10 |
| `ecrit_rare` | Le fil écrit « rare » | pose | r≥30 | ~35 |
| `tigre` | Gris tigré | pelage | défaut | 0 |
| `creme` | Crème caramel | pelage | p≥5 | ~6 / ~10 |
| `nuit` | Nuit d’encre (rare) | pelage | s≥30 | 30 |
| `enluminure` | Enluminure (rare) | pelage | r≥150 | ~175 |
| `bordeaux` | Pelote bordeaux | pelote | défaut | 0 |
| `moutarde` | Pelote moutarde | pelote | q≥5 | 5 |
| `sauge` | Pelote sauge | pelote | q≥20 | 20 |
| `rose` | Pelote rose poudré | pelote | p≥15 | ~19 / ~30 |
| `noeud` | Nœud papillon | acc. cou | p≥1 | ~1 / ~2 |
| `echarpe` | Écharpe tricotée | acc. cou | s≥7 | 7 |
| `bonnet` | Bonnet de nuit | acc. tête | s≥14 | 14 |
| `plume` | Plume d’oie | acc. tête | r≥60 | ~70 |
| `marque` | Marque-page à gland d’or | acc. livre | q≥45 | 45 |

Les lunettes dorées sont toujours portées. Elles font partie de l’identité et ne sont pas un élément de collection. Au bout de 60 soirs, environ 17 des 21 éléments sont obtenus. Deux remarques, sans rien modifier ici : `creme`, `rose` et `noeud` reposent sur `p` (le sans-faute du soir même, que le benchmark déconseille), et `nuit` demande 30 soirs **stricts** d’affilée. Une fois `v` et `h` en place, on pourrait leur donner une condition de rechange (« ou »), mais cela suppose des conditions composées : à décider plus tard.

#### 3.2 Base élargie : 14 éléments, compteurs existants, avant 60 soirs
| id | Nom | Description | Emplacement | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|---|
| `roux` | Roux pain d’épices | Pelage orangé chaud, rayures cannelle, plastron crème. | pelage | P | q≥3 | S |
| `smoking` | Smoking | Chat noir et blanc : pelage anthracite (#4A4448, pas noir pur, pour garder le trait d’encre lisible), plastron et museau blancs. | pelage | B | q≥30 | S\* (rayures quasi invisibles) |
| `lavande` | Pelote lavande | Mauve gris, fil doux. | pelote | P | q≥8 | S |
| `bleu_prusse` | Pelote bleu de Prusse | Bleu profond des encres anciennes. | pelote | P | c≥25 | S |
| `vert_eau` | Pelote vert d’eau | Vert pâle et frais (le fil reste lisible grâce à son liseré d’encre). | pelote | B | r≥12 | S |
| `reliure_bordeaux` | Reliure bordeaux | Le volume sous Glose habillé de bordeaux, plaque d’or. | livre | P | q≥2 | S |
| `reliure_verte` | Reliure vert bouteille | Toile verte, nerfs dorés. | livre | B | m≥40 | S |
| `monture_ecaille` | Monture écaille | Lunettes rondes couleur écaille de tortue. | yeux | B | c≥20 | S\* (`gloseSpecs`) |
| `ecrit_coucou` | Le fil écrit « coucou » | Un bonjour en laine. | mot | P | q≥3 | M |
| `ecrit_ronron` | Le fil écrit « ronron » | Le mot du contentement. | mot | B | s≥10 | M |
| `grelot` | Grelot doré | Petit grelot au cou, sur un ruban. | cou | P | q≥12 | M (3 placements, 2 formes) |
| `beret` | Béret de peintre | Béret penché, queue de feutre au sommet. | tête | B | k≥15 | M (une fois dans `gloseHead`) |
| `reveil_tasse` | Réveillé dans sa tasse | La pose du soir, yeux ouverts : le thé du matin. | pose | B | q≥21 | M (pose `soir` et tête éveillée) |
| `cadre_dore` | Cadre doré | Fine moulure dorée autour de l’aperçu de la Collection. | cadre | P | q≥7 | S (interface) |

Bilan : 35 éléments dans la base, dont environ 30 au bout de 60 soirs. On prépare au passage les jetons `gloseSpecs` et `gloseStripe`, qui servent ensuite à une vingtaine d’éléments futurs. Règle de contraste à coder : un livre ou une monture choisis s’appliquent **avant** le pelage, pour que « Nuit d’encre » garde son socle bordeaux.

#### 3.3 Catalogue futur, par collection

Récompense de complétion de chaque série entre crochets.

**A. Robes de chat** (pelages) [cadre « Médaillon de race »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `chartreux` | Chartreux bleu ardoise | Gris bleuté uni, yeux restant ceux de Glose. | B | h≥8 | S\* |
| `velin` | Vélin | Blanc crème uni, comme le papier fin ; nez et oreilles roses. | B | m≥90 | S\* |
| `cacao` | Cacao | Brun chocolat, rayures à peine plus sombres. | P | c≥120 | S |
| `ambre` | Ambre | Blond doré, plus clair que l’Enluminure, sans paillettes. | P | v≥20 | S |
| `siamois` | Siamois sépia | Corps crème, masque, oreilles, pattes et queue sépia. | R | V≥40 | M (groupe « points » clippé, caché par défaut) |
| `courtepointe` | Tricolore courtepointe | Calico : taches blanches, rousses et noires comme un patchwork. | R | v≥80 | M |
| `ecaille` | Écaille de tortue | Marbrures noires et rousses mêlées. | R | l≥60 | M |
| `mouchete` | Moucheté | Pelage sable à petites taches rondes. | B | h≥20 | M |
| `lavis` | Lavis d’encre | Dégradé d’encre du gris au blanc, comme un lavis (dégradés VD autorisés, API 24 et plus). | R | k≥120 | M |
| `persan_nuage` | Persan nuage | Poil long et gonflé : silhouette plus ronde. | R | V≥120 | C (silhouette) |
| `palimpseste` | Palimpseste | Pelage parchemin où transparaissent des lettres effacées. | O | g≥150 | C (chemins de texte clippés) |
| `papier_marbre` | Papier marbré | Robe aux volutes de papier dominoté, comme une garde de reliure. | O | e:robes | C |

**B. Nuancier** (pelotes) [cadre « Nuancier »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `prune` | Pelote prune | Violet sombre. | P | q≥40 | S |
| `corail` | Pelote corail | Orange rosé. | P | m≥60 | S |
| `mimosa` | Pelote mimosa | Jaune vif, celle de février. | P | d:01-28..03-10 | S |
| `coquelicot` | Pelote coquelicot | Rouge franc. | P | v≥10 | S |
| `pervenche` | Pelote pervenche | Bleu lilas. | P | k≥40 | S |
| `pistache` | Pelote pistache | Vert tendre. | P | h≥6 | S |
| `chataigne` | Pelote châtaigne | Brun d’automne. | P | d:09-22..11-15 | S |
| `ardoise` | Pelote ardoise | Gris bleu des toits. | P | c≥200 | S |
| `ivoire` | Pelote ivoire | Blanc cassé. | B | v≥40 | S |
| `encre_chine` | Pelote encre de Chine | Noir profond, reflet bleu. | B | l≥30 | S |
| `chinee` | Pelote chinée | Deux brins mêlés (bordeaux et crème). | B | V≥30 | M (brins en 2e couleur) |
| `arlequin` | Pelote d’Arlequin | Losanges de couleur, clin d’œil à la commedia. | R | e:nuancier | M |
| `veilleuse` | Pelote veilleuse | Pelote qui luit doucement la nuit (halo en dégradé). | R | h≥30 | M |
| `fil_or` | Fil d’or | Pelote dorée qui scintille ; le fil écrit en or. | O | g≥200 | M (réutilise les éclats) |

**C. Mots du fil** (poses « le fil écrit… », d’un seul trait, 6 lettres au plus, ni i, ni j, ni accent) [décor « Abécédaire »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `ecrit_chut` | Le fil écrit « chut » | Pour les soirs calmes. | P | s≥5 | M |
| `ecrit_zut` | Le fil écrit « zut » | Après une erreur, sans drame. | P | q≥15 | M |
| `ecrit_lu` | Le fil écrit « lu » | Le plus court des mots du fil. | P | m≥30 | M |
| `ecrit_ode` | Le fil écrit « ode » | Pour les amateurs de poésie. | B | d:03-08..03-31 | M |
| `ecrit_conte` | Le fil écrit « conte » | Il était une fois… | B | k≥30 | M |
| `ecrit_fable` | Le fil écrit « fable » | Avec sa morale. | B | v≥30 | M |
| `ecrit_prose` | Le fil écrit « prose » | Pour monsieur Jourdain. | B | c≥150 | M |
| `ecrit_verbe` | Le fil écrit « verbe » | Au commencement. | B | l≥20 | M |
| `ecrit_plume` | Le fil écrit « plume » | Le mot de l’écrivain. | B | h≥12 | M |
| `ecrit_encre` | Le fil écrit « encre » | Le mot de la page. | B | V≥20 | M |
| `ecrit_lettre` | Le fil écrit « lettre » | Un mot qui en contient une autre. | B | d:02-10..02-16 | M |
| `ecrit_recto` | Le fil écrit « recto » | Le devant de la page… | P | m≥120 | M |
| `ecrit_verso` | Le fil écrit « verso » | …et le dos. | B | v≥60 | M |
| `ecrit_flocon` | Le fil écrit « flocon » | Le mot de l’hiver. | B | d:12-01..12-31 | M |
| `ecrit_brume` | Le fil écrit « brume » | Le mot de l’automne. | B | d:10-01..11-15 | M |
| `ecrit_zeste` | Le fil écrit « zeste » | Un des « dix mots » 2026-2027. | B | d:03-13..03-23 | M |
| `ecrit_hourra` | Le fil écrit « hourra » | Pour les grandes occasions. | R | V≥100 | M |

**D. Cabinet de lecture** (livres sous Glose) [décor « Bibliothèque »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `cuir_fauve` | Cuir fauve | Reliure cuir clair, coins usés. | P | h≥4 | S |
| `livre_poche` | Livre de poche | Couverture blanche à bandeau de couleur. | P | m≥80 | M |
| `toile_jouy` | Toile de Jouy | Couverture à scènes champêtres monochromes. | B | k≥60 | M (motif clippé) |
| `reliure_marbree` | Reliure à papier marbré | Plats marbrés, dos de cuir. | B | v≥50 | M |
| `dictionnaire` | Dictionnaire à onglets | Gros volume à encoches alphabétiques sur la tranche. | R | l≥100 | M |
| `grimoire` | Grimoire à fermoir | Cuir sombre, fermoir de laiton. | B | d:10-24..11-02 | M |
| `carnet` | Carnet à élastique | Carnet de notes noir, élastique et ruban. | P | c≥80 | M |
| `herbier` | Herbier | Volume d’où dépasse une fleur séchée. | B | d:04-25..05-08 | M |
| `atlas` | Atlas | Grand format bleu, rose des vents dorée. | B | h≥16 | M |
| `bandeau_prix` | Bandeau de prix | Le bandeau rouge des livres primés, posé sur le volume. | B | d:11-01..11-20 | M |
| `edition_originale` | Édition originale | Tranche dorée, coiffe usée, éclats d’or. | O | e:cabinet | M (réutilise les éclats) |

**E. Lunettes et lorgnons** (yeux ; les lunettes restent, seule la monture change) [cadre « Étui à lunettes »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `monture_argent` | Monture argent | Fil d’argent fin. | P | q≥60 | S\* |
| `monture_nuit` | Monture bleu nuit | Acétate bleu nuit. | P | v≥15 | S\* |
| `monture_rose` | Monture rose poudré | Assortie à la pelote rose. | P | k≥25 | S\* |
| `monture_verte` | Monture vert d’Immortel | Vert sombre brodé d’or. | B | l≥80 | S\* |
| `verres_ambres` | Verres ambrés | Verres légèrement teintés pour l’été (les yeux restent visibles). | B | d:06-15..08-31 | S\* (`fillAlpha`) |
| `lorgnon` | Pince-nez | Deux verres sans branches, pincés sur le nez. | R | V≥80 | C (forme des lunettes dans `gloseHead`) |

**F. Grands auteurs** (des évocations d’œuvres et d’objets, jamais un visage ni une ressemblance) [décor « Cabinet de l’écrivain »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `panache` | Chapeau à panache | Feutre à grande plume blanche : un panache de cape et d’épée (Cyrano, personnage). | R | V≥60 | M |
| `madeleine` | Sa madeleine | Une madeleine sur une soucoupe : la mémoire qui revient d’un coup. Récompense de la mémoire durable. | O | g≥100 | M (objet) |
| `canne_flaneur` | Canne de flâneur | Canne à pommeau, pour arpenter les passages couverts. | B | m≥200 | M |
| `cafetiere_nuit` | Cafetière des nuits blanches | Petite cafetière d’écrivain qui veille (évocation des romanciers noctambules). | B | h≥24 | M |
| `rose_matin` | Rose du matin | Rose à peine éclose (« Mignonne, allons voir… »). | B | d:04-16..04-30 | M |
| `fromage_corbeau` | Fromage du corbeau | Un fromage tenu… et bien gardé (La Fontaine). | B | v≥45 | M |
| `col_claudine` | Col Claudine | Petit col rond blanc d’écolière (le personnage de Colette). | P | k≥50 | M |
| `gilet_hernani` | Gilet rouge d’Hernani | Gilet écarlate des soirs de bataille romantique. | R | e:auteurs | C (vêtement de torse, 3 poses) |
| `ballon_verne` | Cinq semaines en ballon | Petite montgolfière qui flotte au-dessus de Glose. | R | m≥365 | M (objet en hauteur) |
| `scaphandre` | Casque de scaphandrier | Casque de cuivre à hublot : vingt mille lieues de mots. | O | c≥2000 | C (transparence du hublot) |

**G. Genres littéraires** [cadre « Rayon de librairie »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `feutre_detective` | Feutre de détective | Chapeau mou de roman policier. | B | v≥25 | M |
| `loupe` | Loupe de détective | Grosse loupe posée contre le livre. | P | l≥15 | M |
| `couronne_conte` | Couronne de conte | Petite couronne d’or à trois pointes. | P | k≥20 | M |
| `chapeau_enchanteur` | Chapeau d’enchanteur | Chapeau pointu bleu nuit semé d’étoiles. | B | h≥10 | M |
| `bulle_cosmonaute` | Bulle de cosmonaute | Casque de verre de science-fiction rétro. | R | V≥150 | C (transparence) |
| `col_gothique` | Col de cape gothique | Haut col noir doublé de rouge. | B | d:10-24..11-02 | M |
| `bandeau_pirate` | Bandeau de corsaire | Bandeau de tissu sur le front, nœud sur le côté (sans cacher un œil). | B | c≥300 | M |
| `boussole` | Boussole d’aventurier | Boussole de laiton ouverte. | P | m≥150 | M |
| `masques_theatre` | Masques de théâtre | Comédie et tragédie sur un ruban. | B | k≥80 | M |
| `parchemin` | Parchemin de fable | Rouleau scellé d’une morale. | P | v≥35 | M |

**H. Métiers du livre** [décor « Atelier d’imprimerie »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `bonnet_imprimeur` | Bonnet de papier d’imprimeur | Chapeau plié dans une feuille imprimée. | B | c≥250 | M |
| `caracteres_plomb` | Caractères de plomb | Quelques lettres de plomb dans un composteur. | B | v≥70 | M |
| `coquille` | La coquille | Un coquillage posé sur le livre (en typographie, une coquille est une faute). | B | l≥40 | M |
| `crayon_correcteur` | Crayon rouge du correcteur | Crayon bicolore rouge et bleu. | P | c≥100 | M |
| `tampon_bibliotheque` | Tampon de bibliothécaire | Tampon dateur et sa fiche de prêt. | P | h≥8 | M |
| `plioir` | Plioir du relieur | Plioir d’os pour marquer les plis. | B | V≥50 | M |
| `boite_bouquiniste` | Boîte de bouquiniste | Décor : boîte verte ouverte sur les quais, gravures et poches. | R | m≥300 | M (décor) |
| `sacoche_facteur` | Sacoche de facteur | Besace de cuir pleine de lettres. | B | k≥100 | M |
| `tablier_libraire` | Tablier de libraire | Tablier de toile à poche à crayons. | R | e:metiers | C (vêtement) |

**I. Calendrier des lettres** (temps forts du livre et de la langue, chaque année)
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `lampe_chevet` | Lampe de chevet | Lumière douce pour lire tard (Nuits de la lecture). | B | d:01-15..01-31 | M |
| `sous_couette` | Lecture sous la couette | Décor : couverture tirée, lampe de poche, livre d’enfance (thème 2027 : l’enfance). | R | d:01-15..01-31 + q≥3 dans la fenêtre | M (décor) |
| `laurier` | Couronne de laurier | Couronne de poète (Printemps des Poètes). | B | d:03-08..03-31 | M |
| `ruban_dix_mots` | Ruban aux dix mots | Ruban au cou brodé de petits mots (Semaine de la langue française). | B | d:03-13..03-23 | M |
| `rose_livre` | Une rose et un livre | Rose posée sur un livre ficelé (Journée mondiale du livre). | B | d:04-16..04-30 | M |
| `chapeau_soleil` | Chapeau de lecture estivale | Chapeau de paille à ruban (été, Partir en Livre). | B | d:06-15..08-31 | M |
| `cartable_cuir` | Cartable de cuir | Cartable à rabat, la rentrée littéraire. | B | d:08-20..10-15 | M |
| `feuille_automne` | Feuille d’automne | Une feuille rousse tombée entre les oreilles. | P | d:09-22..11-15 | M |

**J. Saisons et fêtes** (chaque année)
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `couronne_rois` | Couronne des rois | Couronne de papier doré de la galette. | P | d:01-02..01-20 | M |
| `crepe` | Crêpe dorée | Une crêpe dans une petite poêle. | P | d:01-28..02-08 | M |
| `billet_doux` | Billet doux | Enveloppe scellée d’un cœur. | P | d:02-10..02-16 | M |
| `poisson_avril` | Poisson d’avril | Poisson de papier collé dans le dos de Glose. | B | d:03-29..04-05 | M (3 placements) |
| `muguet` | Brin de muguet | Brin glissé derrière l’oreille, comme la plume. | P | d:04-25..05-08 | M (ancre de la plume) |
| `couronne_fleurs` | Couronne des champs | Pâquerettes et boutons d’or. | B | d:04-01..06-20 | M |
| `chapeau_sorciere` | Chapeau de sorcière | Pointu, bord large, boucle dorée. | B | d:10-24..11-02 | M |
| `citrouille` | Lanterne-citrouille | Petite citrouille sculptée, bougie dedans. | P | d:10-24..11-02 | M |
| `echarpe_sucre_orge` | Écharpe sucre d’orge | L’écharpe tricotée en rouge et blanc. | P | d:12-01..12-31 | S (variante de couleur) |
| `bonnet_neige` | Bonnet de nuit des neiges | Le bonnet de nuit en bleu glacier, pompon blanc. | P | d:12-01..02-28 | S (variante de couleur) |
| `sapin_papier` | Petit sapin de papier | Sapin plié posé sur la pile. | B | d:12-01..12-31 | M |
| `bougie_an` | Bougie d’un an | Gâteau à une bougie : une année ensemble (personnel, pas une date du calendrier). | R | h≥40 (≈ un an tolérant) | M |

**K. Pâtisseries françaises** (lien avec « Dis-moi dix mots d’un monde à savourer », 2026-2027) [décor « Salon de thé »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `croissant` | Croissant du matin | Croissant doré sur une soucoupe. | P | q≥30 | M |
| `macaron` | Macaron framboise | Un macaron rose et sa collerette. | P | v≥12 | M |
| `eclair` | Éclair au café | Glaçage brun, une rayure blanche. | P | c≥60 | M |
| `chou_creme` | Chou à la crème | Chou poudré de sucre glace. | P | m≥50 | M |
| `religieuse` | Religieuse | Deux choux superposés, collerette de crème. | B | V≥25 | M |
| `tarte_tatin` | Tarte Tatin | Pommes caramélisées, « caramélisé » étant un des dix mots. | B | d:03-13..03-23 | M |
| `toque` | Toque de pâtissier | Haute toque plissée. | B | h≥14 | M |
| `praline` | Praliné | Pelage noisette caramel. | B | c≥400 | S |
| `mille_feuille` | Mille-feuille | Mille feuilles, comme mille pages : récompense de mille bonnes réponses. | O | c≥1000 | M (et éclats) |

**L. Régions de France et francophonie** (traits respectueux ; coiffes traditionnelles stylisées, pas de caricature) [décor « Carte de France »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `coiffe_bigoudene` | Coiffe bigoudène | Haute coiffe de dentelle blanche (Bretagne). | R | V≥70 | C (haute, dentelle) |
| `col_marin` | Col marin | Col rayé bleu et blanc (Bretagne). | P | h≥5 | M |
| `kouign_amann` | Kouign-amann | Gâteau feuilleté au beurre. | B | m≥100 | M |
| `noeud_alsacien` | Grand nœud alsacien | Large nœud noir de coiffe. | B | v≥55 | M |
| `brin_lavande` | Brin de lavande | Glissé derrière l’oreille (Provence). | P | d:06-15..08-31 | M (ancre de la plume) |
| `cigale` | Cigale de faïence | Petite cigale de Provence posée sur le livre. | B | k≥70 | M |
| `canele` | Canelé | Petit gâteau cannelé de Bordeaux. | P | c≥180 | M |
| `chapeau_gardian` | Chapeau de gardian | Feutre noir à large bord (Camargue). | B | l≥50 | M |
| `edelweiss` | Edelweiss | Fleur des Alpes à la boutonnière. | B | h≥18 | M |
| `tuque` | Tuque | Bonnet à pompon québécois (francophonie). | B | d:03-13..03-23 | M |
| `phare` | Phare breton | Décor : phare rayé et embruns. | R | e:regions | M (décor) |

**M. Calligraphie et encres** [cadre « Lettrine »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `encrier` | Encrier de porcelaine | Encrier blanc à filet bleu. | P | v≥8 | M |
| `buvard` | Buvard rose | Buvard taché d’encre. | P | q≥50 | S (rectangle) |
| `plume_ecolier` | Plume d’écolier | Porte-plume de bois et plume métallique. | P | m≥70 | M |
| `pinceau_calligraphe` | Pinceau de calligraphe | Pinceau et goutte d’encre. | B | k≥90 | M |
| `sceau_cire` | Sceau de cire | Médaillon de cire rouge au cou. | B | V≥35 | M |
| `ecritoire` | Écritoire | Décor : pupitre incliné, feuilles et chandelle. | R | e:calligraphie | M (décor) |
| `lettrine` | Lettrine enluminée | Cadre : bordure à lettrine fleurie, assortie au pelage Enluminure. | R | v≥150 | S (interface) |

**N. Belle Époque** [décor « Affiche de cabaret »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `canotier` | Canotier | Chapeau de paille plat à ruban. | B | h≥22 | M |
| `haut_de_forme` | Haut-de-forme | Tube de soie noire. | B | V≥45 | M |
| `lavalliere` | Lavallière | Grand nœud souple de poète. | B | c≥160 | M |
| `ombrelle` | Ombrelle de dentelle | Ombrelle posée contre le livre. | B | k≥110 | M |
| `chat_cabaret` | Chat noir de cabaret | Pelage noir de jais aux yeux d’or (évocation des affiches de Montmartre). | R | V≥90 | S\* (trait d’encre éclairci, à tester) |
| `boa` | Boa de plumes | Boa duveteux autour du cou. | B | d:12-28..01-05 | M |
| `bouquet_violettes` | Bouquet de violettes | Petit bouquet à la boutonnière. | P | v≥18 | M |
| `velocipede` | Vélocipède | Décor : grand-bi posé contre un réverbère. | R | e:belle_epoque | M (décor) |

**O. Nuit et étoiles** (prolonge « Nuit d’encre » et la pose du soir) [cadre « Constellation »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `bonnet_etoile` | Bonnet de nuit bleu nuit | Le bonnet de nuit en bleu nuit, pompon or. | P | s≥21 | S (variante de couleur) |
| `croissant_lune` | Barrette croissant de lune | Petite lune d’or entre les oreilles. | B | h≥26 | M |
| `lanterne` | Lanterne de papier | Lanterne ronde qui luit (halo comme la bougie). | B | k≥130 | M |
| `telescope` | Lunette d’astronome | Une lunette sur trépied (un jeu de mots pour un chat à lunettes). | R | V≥110 | M |
| `ciel_ete` | Ciel d’été | Décor : nuit bleue, étoiles, croissant. | R | h≥34 | M (décor) |
| `pelote_etoiles` | Pelote d’étoiles | Pelote bleu nuit semée d’étoiles pâles. | O | g≥250 | M (réutilise les étoiles) |
| `sur_la_lune` | Assis sur la lune | Pose : Glose sur un croissant de lune, le fil pend vers la Terre. | O | h≥104 (≈ 2 ans tolérants) | C (nouvelle pose) |

**P. Poses** (hors mots du fil) [cadre « Album »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `lecture` | Plongé dans sa lecture | Assis, un livre ouvert entre les pattes, lunettes sur le nez. La pose « cœur » de l’appli : à faire en premier. | B | v≥10 | C |
| `sieste_pile` | Sieste sur la pile | La pose « pile » avec la tête endormie. | P | s≥12 | M |
| `entre_livres` | Caché entre deux livres | Tête et pattes qui dépassent d’un rayon. | B | l≥25 | M |
| `carton_livres` | Dans un carton de livres | Assis dans un carton de déménagement plein de livres. | B | m≥250 | C |
| `poursuite_fil` | À la poursuite du fil | Bond après la pelote qui roule. | R | V≥130 | C |
| `etirement` | Grand étirement | Le dos rond du réveil. | B | h≥28 | C |
| `fenetre_pluie` | À la fenêtre un jour de pluie | Pose avec son décor : gouttes, vitre, coussin. | R | k≥150 | C |

**Q. Cadres et décors** (hors récompenses de série) [aucune récompense de complétion]
| id | Nom | Description | Emplacement | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|---|
| `timbre_poste` | Timbre-poste | Bordure dentelée. | cadre | P | k≥10 | S |
| `carte_postale` | Carte postale | Coin d’oblitération et filet. | cadre | P | m≥45 | S |
| `photo_instantanee` | Photo instantanée | Bordure blanche épaisse en bas. | cadre | P | h≥3 | S |
| `frise_laurier` | Frise de laurier | Rameaux d’olivier et de laurier. | cadre | B | v≥100 | S |
| `ex_libris` | Ex-libris | Cadre gravé « ex-libris » portant le nom choisi pour la mascotte. | cadre | R | V≥200 | M (texte et nom personnalisable) |
| `vitrail` | Vitrail | Cadre de plombs et verres colorés. | cadre | R | e:auteurs+genres | M |
| `coin_feu` | Coin du feu | Fauteuil, cheminée et plaid. | décor | B | d:11-15..02-28 | M |
| `quais_seine` | Quais au crépuscule | Parapet, réverbère, péniche. | décor | R | m≥400 | M |
| `scriptorium` | Scriptorium | Arcades, pupitres et manuscrits : le décor de l’Enluminure. | décor | O | g≥300 | M |

**R. Les Immortels** (légendaire, sur le thème du dictionnaire ; une évocation de l’habit d’académicien, sans logo ni nom d’institution) [titre « Immortel » + cadre « Habit vert »]
| id | Nom | Description | Rareté | Condition | Faisab. |
|---|---|---|---|---|---|
| `bicorne` | Bicorne d’Immortel | Bicorne noir à plumes. | O | V≥250 | M |
| `col_rameaux` | Col brodé de rameaux | Col vert brodé de feuillages d’or. | R | l≥150 | M |
| `epee_immortel` | Épée d’Immortel | Épée de cérémonie posée contre le livre. | O | g≥365 | M |

**S. Pack de soutien facultatif** (si un jour il y a des dons ; hors collections, hors rareté, aucun ne compte pour une complétion)
| id | Nom | Description | Emplacement | Faisab. |
|---|---|---|---|---|
| `merci_medaille` | Médaille de mécène | Petite médaille « Merci » au ruban bordeaux. | cou | M |
| `merci_cadre` | Cadre des amis des livres | Bordure dorée avec une branche. | cadre | S |
| `merci_salle` | Salle de lecture | Décor : longues tables, lampes vertes. | décor | M |

#### 3.4 Volumes
- **Base** : 21 existants et 14 nouveaux, soit 35 éléments, environ 30 en 60 soirs.
- **Futur** : 170 éléments en 18 tableaux (A à R, avec les cadres et les décors), plus 3 éléments de soutien hors collection, tous avec des ids uniques qui ne collisionnent pas avec l’existant (vérifié par script). Répartition approximative : pelages 14, pelotes 14, mots du fil 17, poses 8, livres 11, montures 6, tête environ 30, cou environ 12, objets environ 40, décors environ 12, cadres environ 10.
- **Faisabilité** : 23 S, 8 S\*, 124 M, 15 C. Toutes les poses, toutes les silhouettes modifiées et les vêtements de torse sont en C.

#### 3.5 Limites et ordre de réalisation [proposition]
1. **D’abord**, une heure de séparation de jetons (`gloseSpecs`, `gloseStripe`) : elle débloque environ 20 éléments S\*.
2. **Avant le 20e accessoire**, passer aux calques par emplacement (la solution de repli de `mise_en_place.md` §3.3). Tous les accessoires dessinés, cachés, dans chaque pose feraient grossir chaque dessin bien au-delà des environ 180 chemins mesurés, et le coût de chargement d’un VD croît avec ses chemins, même transparents. Les accessoires de tête restent les moins chers : on ne les dessine qu’une fois dans `gloseHead`, mais leur calque doit rejouer les animations de la tête (mêmes durées, démarrées dans la même image).
3. **Décors** : un dessin statique derrière Glose, en aplats peu contrastés, décliné en thème clair et sombre (capture `collection_dark.png`). Ils ne touchent pas au pipeline du personnage.
4. **Cadres** : purement interface (forme XML ou vecteur autour de l’aperçu).
5. **`lecture`** est la pose la plus utile du catalogue : à prévoir dès que la révision espacée existe (`v≥10`).
6. Le code reste petit : `Item` gagne `window` et `set` (nullables), `value()` gagne `c`, `m`, `k`, `l`, `v`, `V`, `g` et `h`. Un test JUnit vérifie la table, comme pour l’existant.

#### 3.6 Revue des noms (kitsch, clarté, marques)
- **Écartés pour cause de marque ou de droits** :
  - Petit Prince : œuvre et marque gardées par la succession ; rien de ce qui l’évoque n’est retenu.
  - « Moleskine » → « Carnet à élastique ».
  - « Sergent-Major » (marque de plumes) → « Plume d’écolier ».
  - « Polaroid » → « Photo instantanée ».
  - « Bleu Klein / IKB » → « Bleu de Prusse ».
  - « Pierre de Ronsard » (nom de variété de rose commercialisée) → « Rose du matin ».
  - Prix Goncourt, Académie française (nom et emblème) → « Bandeau de prix », « Immortel » ; aucun logo.
  - Le Chat Noir : cabaret historique, cité seulement comme évocation (« Chat noir de cabaret »), sans reprendre l’affiche.
- **Personnes réelles** : aucun visage, aucune moustache ni aucun portrait. On évoque par un objet (la madeleine, le gilet d’Hernani, la cafetière, le panache) ou par un personnage du domaine public (Cyrano, Claudine, Gavroche).
- **Écartés pour kitsch** : « arc-en-ciel », « licorne », « paillettes » en dehors des légendaires. Les jeux de mots restent rares et littéraires (coquille, mille-feuille, lunette d’astronome).
- **Clarté** : chaque nom dit ce qu’on voit (« Pelote vert d’eau », pas « Pelote Rêverie »). Les noms poétiques sont réservés aux légendaires (Palimpseste, Scriptorium), avec une description claire.
- **Typographie** : apostrophe typographique ’ et guillemets « » avec espaces fines dans tous les noms ; ids en ASCII sans œ (`noeud_alsacien`).
- **Sensibilité culturelle** : les coiffes régionales et le « madras » antillais (écarté pour l’instant) demandent un dessin respectueux et non caricatural. À relire avec une personne concernée avant publication.

### Gaps
- Aucun test sur appareil du coût de chargement avec de nombreux groupes cachés : le seuil de « 20 accessoires » est une estimation, non une mesure.
- Le contraste d’un pelage noir (`chat_cabaret`) avec le trait d’encre #3B2A20 n’a pas été rendu. Il faudra sans doute éclaircir le contour pour ce pelage seulement, ce qui demande un jeton de contour.
- Le statut juridique exact des noms cités (variétés de roses, cabaret, coiffes) n’a pas été vérifié auprès de l’INPI : des choix de prudence, pas un avis juridique.
- Les estimations de rythme de `v`, `V`, `g` et `h` sont théoriques tant que la révision espacée n’existe pas.
