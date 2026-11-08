# Rapport de nuit : 8 novembre 2026

Travail autonome de 00h10 à __FIN__ (Paris), sur la branche `claude/android-daily-word-definitions-8ngyty`. Rien n'a été fusionné sur `main`, aucune release, aucune PR, versionCode inchangé (0.14.0).

- **Départ** : `08a954d`. **Arrivée** : `__HEAD__`.
- **Tests** : 164 tests Robolectric, tous verts. `assembleRelease` passe et la CI de la branche est verte.
- **Plan** : `plan.md`, validé par un agent expert avec 11 amendements. Le journal minute par minute est dans `journal.md`.
- **Consommation** : __CONSO__ % de la limite hebdomadaire. Le seuil d'arrêt était fixé à 10 %.

## En 5 minutes : ce qui a changé

| Lot | Axe | Commits | Risque | À tester sur le téléphone |
|---|---|---|---|---|
| Profil blindé | sec | 1ca8efc, c8ab3ed, 112326c, ee51487, a28a2b3 | faible | Réglages › Mon profil : exporter par-dessus un ancien fichier, puis importer (aussi depuis Drive). |
| Mot du jour après minuit, bandeau, extra limité | fon/sec | 45927b7 | moyen | Laisser l'appli ouverte après minuit, puis revenir : le mot du jour est celui du nouveau jour. |
| Durée restaurée gardée, citations sans double « « | fon | 442c8d1, 900742b, f3e19b5 | faible | Réglages › durée. |
| Détail au style de l'accueil, focus de l'étoile, album | ux/gra | 15183f7, 7640dd5, 2e55360 | faible | Mes mots › ouvrir un mot. |
| Score du quiz qui survit à la rotation, sons du quiz préchauffés, chaîne d'alarmes robuste | fon/son | 9e70f64, a523b87, 77f9302 | moyen | Faire le quiz avec sons, tourner le téléphone sur l'écran du score. |
| **Renommer Glose** (Réglages › Apparence) | fon | c1dbecf | moyen | Taper un nom, voir l'accueil, la Collection et la carte partagée. |
| Quiz : ✓/✗ en icônes et TalkBack, carte « Noter » seulement depuis le Play Store, avertissement quiz avant le mot | ux/fon | 6985e65, cc1b11e | faible | Quiz, réponse fausse. |
| **Glose : liseré crème de nuit, humeurs, livre allégé, liseré des accessoires** | gla | c71720e, 9666fcd, d34715b, 7834355, e67dd7b | moyen | Mode sombre : accueil, quiz, Collection avec accessoires. |
| **Glose : réactions vivantes, pause** | gla | 2640407, e968a4a | faible | Toucher Glose, toucher 4 fois, appui long. |
| Espace fine dans les polices, interface en « Definiday Serif » | gra | 7a45d30, 1b81918, aeadfd9, d934ab5 | faible | Lire « ; : ! ? » partout. |
| En-tête compact en grande police | ux | 49fe6be | faible | Police système ×2. |
| Mes mots « À revoir », rotation, Couleurs et polices sans vide, menus en ressources | fon/ux | 18a6904, 8d411a2, 897cb26 | faible | Mes mots › filtres ; Couleurs et polices › décocher jusqu'à la dernière. |
| Divers : ordinal du déverrouillage, widget en romain, aide aérée | ux | d7da9e8, e4179a2, 31a1197 | faible | Widget. |
| Tests de l'updater, découpage de QuizActivity | crap | ca54ffd, 4b25a40, 0048e13 | faible | (aucun : tests et refactor sans changement de comportement). |
| Proposition de niveaux sonores (non appliquée) | son | 8fa55bb | aucun | Écouter `docs/son/proposition_niveaux/`. |

**Installer la version de test** : l'APK est l'artefact `definiday-test` du dernier run CI de la branche. Il s'installe à côté de l'appli, sous le nom `fr.definiday.dev`, sans toucher à ton profil.

**Annuler** :
- un lot : `git revert <commit>` ; chaque lot est un commit séparé.
- toute la nuit : `git reset --hard 08a954d`, puis `git push --force-with-lease`, sur la branche seulement.

## Monsieur Glose (ta demande de 00h30)

1. **Benchmark et audit** : `rapports/glose_1.md`, avec les sources de Duolingo, Mailchimp, Headspace, Notion, Finch, etc.
   - Le plus gros défaut était la nuit : le contour brun disparaissait sur le fond nuit.
   - Autres défauts : la même expression partout, et le livre plus contrasté que le visage.
2. **Corrigé** :
   - liseré « autocollant » crème la nuit, qui suit toutes les animations et les accessoires (+68 Ko) ;
   - humeurs : « fier » en écrivant, « curieux » sur la pile ;
   - livre allégé ;
   - réactions au toucher avec anticipation et squash & stretch (−32 Ko grâce aux animateurs partagés) ;
   - pause de la vie au repos par appui long, aussi avec TalkBack.
   - Les rendus de jour sont identiques au pixel, sauf là où le changement est voulu.
3. **Planches** : `planches/glose_nuit_avant_apres.jpg`, `planches/glose_accessoires_nuit.jpg`, `planches/glose_reaction_tap.jpg`.
4. **À choisir par toi** : `glose_decisions.md` (icône, épaisseur du trait, couleur du pelage, couleur de la reliure). __GLOSE5__

## Décisions en attente (par ordre d'importance)

1. **Sécurité, action GitHub (5 min, sans code)**. Les 4 secrets de signature sont au niveau du dépôt : n'importe quel workflow de n'importe quelle branche peut lire la clé release.
   - Déplace-les dans l'environnement `release` (règle : `main` + tags `v*`), puis supprime ceux du dépôt.
   - Même endroit : restreindre la création de tags (S2).
   - Détail dans `rapports/audit_sec_1.md`, S1 et S2.
2. **Nom de Glose** : je l'ai fait avec les hypothèses suivantes, à confirmer :
   - 24 caractères au maximum ;
   - émojis permis, sauf ceux composés avec U+200D ;
   - la signature des cartes porte le nom complet ;
   - une carte déjà affichée garde l'ancien nom jusqu'au mot suivant.
3. **Carte « Noter »** : elle n'apparaît plus que pour une installation depuis le Play Store, puisque l'appli n'y est pas : la carte menait à une page inexistante. À garder ?
4. **Mes mots** : la ligne « N mots à revoir » compte maintenant les mots dus à la révision (comme le nouveau filtre), et non plus les mots ratés 2 fois ou plus. Pour un utilisateur ancien, ce chiffre peut devenir grand. À garder ?
5. **Sons** : écoute la proposition `docs/son/proposition_niveaux/` (haut-parleur du téléphone, puis casque).
   - Une commande l'applique.
   - Le compromis : au casque, les sons graves deviennent plus forts.
6. **Goût, sans code fusionné** :
   - grain de papier sur le fond de l'appli (`planches/proposition_grain_fond.jpg`) ;
   - icône (`planches/proposition_icone_audit_ux.jpg` et `glose_decisions.md`) ;
   - retirer Uncial du choix de polices (« lendit » se lit « lexdic ») ;
   - en-tête de la Collection composé comme l'accueil ;
   - cachets sur les timbres gagnés ;
   - signature des citations sans italique ;
   - widget en enveloppe kraft.
   - Détail dans `rapports/audit_ux_1.md`, section « Propositions de goût ».
7. **Questions produit** (`rapports/audit_fon_son_1.md`, fin) :
   - le « mot au hasard » doit-il notifier quand le mot du jour est coupé ?
   - jours de citation seule : pas de quiz, ou un mot ajouté ?
   - « cadre doré » de la Collection ;
   - suivre le réglage Android « sons des touches » ?
8. **Exception typographique** : la note manuscrite de Glose (Dancing Script) garde une espace insécable ordinaire avant « ! ». Sa licence interdit de la modifier sans la renommer. À accepter, ou on renomme aussi cette police.
9. **Petites options** :
   - l'identifiant interne `libre_baskerville` est gardé dans `styles.json`, pour ne pas casser les sélections ;
   - amplitude du squash de Glose (5 à 7 %) à juger sur le téléphone.
10. **Hors sandbox** (S5, S6) : `gradle/verification-metadata.xml` et l'empreinte complète du certificat dans le README.

## Ce qui n'a pas été fait

- Pas de nouvelle fonction produit : je ne voulais pas décider à ta place.
- Questions de données (`academie` jamais lu, quiz des jours de citation) : laissées pour décision.
- Une limite connue reste : si un mot est vu pendant que le quiz est ouvert, une rotation peut changer les distracteurs de la question en cours.

## Incidents de la nuit

- **CI rouge sur 3 pushs** (e4179a2 → 31a1197), réparée en 10 min. Une retouche du widget avait cassé `widget.xml`, et mon décompte relisait d'anciens résultats de tests. Le script de vérification efface maintenant les résultats et contrôle la compilation.
- **Deux relectures indépendantes** (`rapports/review_1.md`, `review_2.md`) : 1 bloquant (celui ci-dessus), 2 importants et 8 mineurs. Tous sont corrigés, sauf ceux renvoyés en décision (M1 de la relecture 2, M5 de la relecture 1 sur la marque Zilla, réglé en rendant la police d'origine).

## Ménage

- 36 worktrees d'agents sous `.claude/worktrees/` (ignorés par git), dont 20 d'avant cette nuit. Je n'ai rien supprimé ; leur contenu utile est fusionné.
- Un `git stash` (`fonB-n2-src`) laissé par une équipe : son contenu est déjà dans 9e70f64, tu peux le supprimer.
- Rapports d'équipes et audits : `rapports/` ; planches : `planches/`.
