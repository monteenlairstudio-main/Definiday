# Animations de Monsieur Glose : audit, bonnes pratiques et plan

8 novembre 2026. Trois équipes ont travaillé sur ce rapport :
- une équipe de recherche, sur les bonnes pratiques et les modèles ;
- une équipe d'audit, qui a mesuré les animations image par image ;
- un expert (ingénieur graphique Android et animateur), qui a fait la revue.

Notation : **[F]** fait sourcé, **[D]** déduction, **[H]** hypothèse.

Les détails sont dans `Animations de Glose/` :
- `bonnes_pratiques.md` : sources et checklist de 28 points ;
- `audit_mesure.md` ;
- `revue_expert.md` : commandes de mesure sur téléphone et critères d'acceptation.

## En bref

La sensation de saccade a surtout des causes **dans nos fichiers d'animation**, que le générateur peut corriger :
- la puissance du téléphone n'en est pas la cause principale ;
- le coût de dessin reste toutefois à mesurer sur un vrai téléphone.

L'expert valide l'audit, avec tous les chiffres recalculés. Le prototype de correctif est **fusionnable après 3 corrections**, qui sont en cours.

## Ce qui ne va pas (mesuré)

| # | Symptôme | Cause | Preuve |
|---|---|---|---|
| 1 | Le timing prévu est faussé (jusqu'à ±189 ms dans les spéciales) et les calques de durées différentes se décalent entre eux | Aucune animation ne précise sa courbe : Android applique par défaut un ralenti au début et à la fin de toute l'animation. **[F]** code AOSP, lu sur les 297 animateurs gonflés | +63 ms sur l'anticipation d'un toucher |
| 2 | Arrêt à chaque étape d'un balancement (« stop-and-go ») | La courbe « ease » est posée sur chaque clé, même au milieu d'un mouvement | pelote de la pile : 5 arrêts en 13 s |
| 3 | « Le fil écrit » : le mot disparaît à l'arrêt et après chaque toucher, la pelote saute de 150° | La boucle commence mot caché | saut de 81 px |
| 4 | Saut quand la vie au repos s'arrête à 26 s | `stop()` envoie chaque partie à sa dernière clé, en plein cycle **[F]** code AOSP | livres 7 px, écrit 81 px |
| 5 | Toucher en plein mouvement : la pose saute | La réaction repart toujours du dessin immobile | médiane 2 à 8 px |
| 6 | Respiration morte | Valeurs arrondies à 1 décimale : 1,015 devient 1 | 127 mouvements inertes |
| 7 | Liseré de nuit qui glisse sous la tête | Il ne suit ni la respiration ni le bâillement | 3 à 4 px pour un liseré de 4,5 px |
| 8 | Les « z » et la vapeur s'éteignent d'un coup en fin de réaction (soir, réveil) | La réaction ne finit pas sur la première image du repos | — |
| 9 | Coût de dessin, cause possible de trames perdues **[H]** | Chaque calque animé est redessiné en entier sur le processeur, à chaque image | 4 à 9 ms par image sur un processeur de bureau, à mesurer sur téléphone |

Planches (dessins de Glose uniquement) :
- `Animations de Glose/sauts_avant_apres.jpg` : sauts à l'arrêt et au toucher, avant et après le prototype ;
- `vitesses_avant_apres.jpg` : courbes de vitesse ;
- `ecrit_tap_avant.jpg` et `ecrit_tap_apres.jpg` : le mot écrit pendant un toucher.

## Bonnes pratiques retenues (extrait, sources dans `bonnes_pratiques.md`)

- **Courbes**
  - Courbe linéaire **au niveau de l'animateur**, courbes de vitesse **par segment**.
  - Ralenti seulement aux vrais repos, vitesse continue aux points de passage. Pour cela, des segments de Hermite monotones écrits en `pathInterpolator`.
- **Durées**
  - Clignement : 100 à 150 ms. Micro-réaction : 400 à 700 ms.
  - Pas de segment de moins de 2 images (33 ms à 60 Hz).
- **Vie au repos**
  - Elle doit varier : périodes de boucle sans facteur commun, gestes courts à intervalles irréguliers.
  - Aujourd'hui, respiration, tête et pelote durent toutes 3 800 ms.
- **Arrêt et accessibilité**
  - Arrêt propre en fin de cycle, durée calée sur le réglage « vitesse des animations ».
  - Respect de « supprimer les animations » (WCAG 2.2.2, 2.3.3).
- **Performance AnimatedVectorDrawable**
  - Calques petits (le cache a la taille du calque), au plus 3 calques à redessiner par image.
  - Les liserés de nuit dans un calque à part, dessiné seulement la nuit.

## Modèles (principes seulement, jamais leurs dessins)

| # | Modèle | Ce qu'on en retient | Source |
|---|---|---|---|
| 1 | Duolingo, personnages du World | expressions lisibles, bouches par son | https://blog.duolingo.com/world-character-visemes/ |
| 2 | Duolingo, Lily (Rive) | repos qui varie, transitions entre états | https://rive.app/blog/duolingo-s-ai-powered-video-call-brings-lily-to-life |
| 3 | Notion AI (Buck) | visage minimal, micro-mouvements | https://buck.co/work/notion-ai |
| 4 | Headspace (Nexus Studios) | douceur, dessins tenus 2 images (5,24 s à 25 i/s) | https://nexusstudios.com/work/headspace/ |
| 5 | Headspace Ebb | compagnon calme, clignement rare | https://www.figma.com/blog/headspace-ebb-ai-companion/ |
| 6 | Google, points de l'Assistant | courbes soignées, éléments décalés | https://design.google/library/evolving-google-identity |
| 7 | Mailchimp, high five de Freddie | anticipation, dépassement, retour au repos | https://mailchimp.com/resources/designing-the-freddie-high-five-animation-for-facebook-ads/ |
| 8 | Finch (essai d'animation) | réactions au toucher, squash & stretch | https://contra.com/p/seOAX3PZ-expressive-character-animation-for-finch-app |
| 9 | Pokémon Sleep | **[H]**, la page ne détaille pas l'animation | https://www.pokemonsleep.net/en/ |
| 10 | Apple HIG Motion | principes généraux | https://developer.apple.com/design/human-interface-guidelines/motion |

La planche d'aperçus de ces 10 modèles t'a été envoyée dans la conversation. Elle reste hors du dépôt, car ce sont des dessins d'autres studios.

## Revue de l'expert

**Verdict** : le prototype est fusionnable après 3 corrections bloquantes.
1. `ValueAnimator.getDurationScale()` n'est public qu'à partir de l'API 33. Il faut un repli pour les API 26 à 32.
2. La respiration revenue fait glisser le corps de 2 à 3,5 px sous l'écharpe, le nœud et le grelot (estimation). Il faut leur donner la même respiration.
3. La fin de quiz « écrit » commence maintenant avec la pelote à −150° : le saut est passé de la fin au début. Il faut des tours complets.

**Il ajoute** :
- le correctif des arrêts (`eases()`) est partiel : 26 clés de passage sur 40 gardent une cassure de vitesse ;
- le protocole de mesure sur téléphone doit garder les trames FLAGS = 2, celles que poussent les animations ;
- reconstruire le dessin à chaque toucher peut faire perdre une image ;
- découper les calques fixes et mobiles, seulement si la trace du téléphone dépasse environ 4 ms de dessin vectoriel par image ;
- fondu de 100 à 120 ms au toucher en plein mouvement, seulement s'il reste plus de 3 à 5 px de saut après les autres correctifs.

**Critères d'acceptation** :
- moins de 1 % d'images en retard ;
- 99e centile ≤ 25 ms à 60 Hz et ≤ 12,5 ms à 120 Hz ;
- pas de saut de plus de 0,5 px à l'arrêt ni en fin de réaction ;
- toucher en plein mouvement : médiane ≤ 3 px, maximum ≤ 6 px ;
- APK au plus +50 Ko.

## Où on en est

1. **Phase 2 faite** (`phase2_correctifs.md`, planche `Animations de Glose/sauts_phase2.jpg`). Les 3 corrections bloquantes et le reste du plan de l'expert sont appliqués et intégrés à la branche (en local, pas poussés).

   | Contrôle | Base | Après |
   |---|---|---|
   | Arrêt à 26 s (aussi à ×0,5 et ×2) | jusqu'à 81 px, « z » qui disparaissent | **0 px** |
   | Fin de réaction → repos | 3,1 px, « z » et vapeur qui s'éteignent | **0 px** |
   | Liseré de nuit / sa partie | 3,3 à 3,9 px | **0** |
   | Accessoires du cou / corps | — | **0** |
   | Cassures de vitesse aux points de passage | 39 sur 39 | **0 sur 31** |
   | APK release | 2 026 216 o | **1 925 050 o (−98,8 Ko)** |

   - Défaut découvert en route : sur le RenderThread, une animation retardée n'écrit rien avant son départ. Les 2e et 3e « z » s'affichaient en plein puis s'éteignaient. C'est corrigé, et le générateur le refuse désormais.
   - **Seul critère non tenu** : toucher Glose en plein mouvement. Les poses éveillées sautent encore de 3 à 8 px, et « écrit » de 81 px quand on touche pendant que le mot est caché. Il faut le fondu de 100 à 120 ms proposé par l'expert.
   - Tests : 167 verts, rendus fixes de jour inchangés.
2. **À mesurer sur ton téléphone** : coût de dessin (`dumpsys gfxinfo framestats`, Perfetto, commandes dans `revue_expert.md` §3.2), vidéo à 240 i/s, vitesse des animations ×0,5 et ×2 sur Android 8 à 12 et 13+.
3. **Ensuite** :
   - le fondu au toucher ;
   - une vie au repos qui varie : périodes premières entre elles, clignements et gestes à intervalles irréguliers ;
   - le découpage des calques fixes et mobiles, si la mesure sur téléphone le justifie.

Rien de tout cela n'est poussé : le compte GitHub est suspendu. Le travail est dans la copie de secours `definiday.bundle`.
