# Revue des textes de l’interface : Definiday (commit 79ba046)

Méthode : `design:ux-copy` (clair, concis, cohérent, utile, humain ; erreurs = ce qui se passe + pourquoi + que faire ; état vide = ce que c’est + pourquoi c’est vide + comment commencer ; boutons = verbe + résultat).
Matériel : `res/values/strings.xml` (77 lignes, toutes les chaînes utilisées), textes en dur des `*.kt`, `assets/citations.json` et `words.json` (affichés tels quels), captures `revue_ui/*.png` (accueil, réglages w360 ×2,0, quiz, Collection, notification du quiz, écran de verrouillage).
Déjà traité dans `docs/audits/interface.md` et corrigé : non repris. Points de cet audit encore ouverts : signalés « (rappel Mx) », sur une ligne.

Convention des propositions : l’espace avant `: ; ! ?` et à l’intérieur de `« »` est l’espace fine insécable U+202F (`\u202F` dans `strings.xml`), comme dans `words.json`. Priorité : 1 = trompe ou bloque l’utilisateur, 2 = gêne la compréhension, 3 = finition.

## Tableau

### Réglages

| Prio | Clé ou fichier:ligne | Texte actuel | Problème | Proposition |
|---|---|---|---|---|
| 1 | `wallpaper_hint` | Le premier mot du jour devient le fond de l’écran de verrouillage. À la fin, l’écran de verrouillage reprend votre fond d’écran d’accueil. | La conséquence importante (le fond de verrouillage d’origine est perdu) est dite de façon voilée ; « reprend » laisse croire à un retour à l’état d’avant. | Le premier mot du jour devient le fond de l’écran de verrouillage. Ensuite, c’est votre fond d’accueil qui s’y affiche : votre ancien fond de verrouillage n’est pas rétabli. |
| 1 | `exact_no` | Alarmes exactes non autorisées : le mot peut arriver jusqu’à une heure plus tard, et la fin au déverrouillage peut ne pas démarrer. | Jargon (« alarmes exactes ») ; le nom réel de la page système est « Alarmes et rappels » ; « peut ne pas démarrer » est flou ; 6 lignes en w360 ×2,0. | Le mot peut arriver jusqu’à 1 h en retard et la fin au déverrouillage peut échouer. Autorisez « Alarmes et rappels » pour l’éviter. |
| 1 | `exact_ask` | Autoriser les alarmes exactes | Ne correspond à aucun libellé que l’utilisateur trouvera sur la page qui s’ouvre. | Autoriser « Alarmes et rappels » |
| 2 | `exact_ok` | Alarmes exactes autorisées : le mot arrive à l’heure choisie. | Affiché aussi sous Android 8 à 11, où cette autorisation n’existe pas ; seule la seconde moitié est utile. | Le mot arrivera à l’heure choisie. |
| 2 | `mode` | Affichage | Trop vague : 6 textes de l’écran utilisent « afficher/affichage ». Le libellé doit dire le choix. | Où afficher le mot |
| 2 | `unlocks` | Fin sur l’écran de verrouillage | Groupe nominal opaque ; on ne comprend qu’en lisant les options. | Retirer le mot de l’écran de verrouillage |
| 2 | `unlocks_hint` | Fin au déverrouillage : une notification discrète de suivi reste affichée jusque-là, et la durée reste un maximum. | Deux idées en une phrase, « la durée reste un maximum » est abstrait. | Pour compter les déverrouillages, une petite notification reste affichée. Le mot part quand même au bout de la durée choisie. |
| 2 | `more_quotes` | Plus de citations (2 sur 5 au lieu de 1 sur 5) | « 2 sur 5 » de quoi ? Libellé d’interrupteur sur 3 lignes en w360 ×2,0 (« (2 / sur 5 au lieu de 1 sur / 5) »). | Libellé : Plus de citations. Aide dessous : Une citation remplace le mot 2 fois sur 5, au lieu d’1 fois sur 5. |
| 2 | `palettes`, `polices` | Palettes utilisées : %1$d sur %2$d | « utilisées » se lit comme « déjà vues » (l’appli a aussi une Collection à compteurs) ; en ×2,0, « 18 / sur 18 » se coupe. | Palettes au choix : %1$d\u00A0sur\u00A0%2$d (idem `polices`, `palettes_title` « Palettes au choix ») |
| 3 | `duration_both`, `duration_notif`, `duration_lock` | … se retirent après cette durée au plus tard. | « se retirer » pour une notification sonne administratif. | … disparaissent au plus tard après cette durée. |
| 3 | `credits` | … Les boutons ouvrent les articles officiels … | Ce sont maintenant des liens soulignés, plus des boutons. | … Les liens ouvrent les articles officiels … |
| 3 | `per_day` | Nombre de mots par jour | Long pour un libellé de menu ; « nombre de » n’apporte rien. | Mots par jour |
| 3 | `test_done_notif` + `SettingsActivity.kt:51` | Mot affiché dans les notifications. | Toast affiché même notifications refusées (rappel B2, vérification du toast non faite). | Si notifications coupées : Notifications désactivées : le mot n’a pas pu s’afficher. |

### Accueil et notifications

| Prio | Clé ou fichier:ligne | Texte actuel | Problème | Proposition |
|---|---|---|---|---|
| 1 | `notif_off` | Notifications désactivées : le mot du jour ne peut pas s’afficher. Toucher pour les activer. | Faux en mode « Notification et écran de verrouillage » (le mot reste sur l’écran de verrouillage) ; ne dit pas que le rappel du quiz est perdu aussi ; infinitif « Toucher » alors que l’appli vouvoie. | Notifications désactivées : ni le mot ni le rappel du quiz ne vous parviennent. Touchez ici pour les réactiver. |
| 1 | `channel_name` | Mot du jour | Le rappel du quiz passe par le même canal (`DailyReceiver.kt:81`) : couper « Mot du jour » dans les réglages Android coupe aussi le quiz, sans que le nom le dise. | Mot du jour et quiz du soir (mieux : un canal « Quiz du soir » à part, petit changement de code) |
| 2 | `new_word_hint` | Il remplace aussi le mot affiché sur l’écran de verrouillage. | Affiché en mode « Notification seulement », où il n’y a pas de mot sur l’écran de verrouillage ; ne dit pas qu’il remplace aussi les notifications du jour (`DailyReceiver.kt:111`). | Selon le mode : Il remplace aussi le mot du jour sur l’écran de verrouillage et dans les notifications. / … dans les notifications. / … sur l’écran de verrouillage. |
| 2 | `watch_text` | Le mot quittera l’écran de verrouillage au déverrouillage n° %1$d. | « n° 1 » fait formulaire. | `string-array` : Le mot quittera l’écran de verrouillage au 1er déverrouillage. / au 2e … / au 3e … |
| 2 | `seen_today` | Mots d’aujourd’hui | La rangée contient aussi des puces « Citation ». | Aujourd’hui |
| 3 | `quote_chip` | Citation | Avec 3 mots par jour et « Plus de citations », deux puces « Citation » identiques. | Nom de l’auteur : « Hugo », ou Citation de %1$s |
| 3 | `MainActivity.kt:103` (bouton du dialogue « Prenez le temps ») | OK (système) | Seul « OK » sec dans une boîte au ton littéraire. | D’accord |
| 3 | `no_browser` | Aucun navigateur pour ouvrir le lien. | Dit la cause, pas l’effet. | Impossible d’ouvrir le lien : aucun navigateur installé. |

### Quiz

| Prio | Clé ou fichier:ligne | Texte actuel | Problème | Proposition |
|---|---|---|---|---|
| 1 | `QuizActivity.kt:79` (pas de chaîne) | (définition seule, sans consigne) | Rien ne dit ce qu’on attend : 4 mots sous une définition, aucune question. | Nouvelle chaîne `quiz_prompt` au-dessus de la définition : Quel mot se cache derrière cette définition ? |
| 2 | `quiz_wrong` (annonce TalkBack) | Faux, c’était %1$s | « Faux » est sec pour Monsieur Glose, qui ne se moque jamais. | Pas tout à fait : c’était « %1$s ». |
| 2 | `quiz_right` (annonce TalkBack) | Juste | Correct mais froid. | Bien vu ! |
| 2 | `quiz_notif` | %1$d mot(s) vu(s) aujourd’hui : les reconnaîtrez-vous ? | (rappel M6) `<plurals>` toujours absent ; au singulier, « les » doit devenir « le ». | one : Un mot vu aujourd’hui : le reconnaîtrez-vous ? / other : %1$d mots vus aujourd’hui : les reconnaîtrez-vous ? |
| 2 | `quiz_next` à la dernière question | Suivant | Mène à l’écran du score, pas à une question. | Dernière question : Voir le résultat |
| 2 | `quiz_score` + `QuizActivity.kt:78` | %1$d / %2$d ; « 1 / 4 » en dur | TalkBack lit « 3 barre oblique 4 » ; le compteur de question est en dur. | Visuel inchangé ; contentDescription : %1$d bonnes réponses sur %2$d. Compteur en chaîne `quiz_progress` : Question %1$d sur %2$d |
| 3 | `quiz_empty` | Aucun mot vu aujourd’hui pour l’instant. | (rappel M10) aucune piste pour commencer. | Aucun mot vu aujourd’hui pour l’instant. Lisez le mot du jour, Monsieur Glose vous attend ce soir. |
| 3 | `quiz_eve` | Les mots d’hier | Clair, mais n’explique pas pourquoi (quiz ouvert après minuit). | Le quiz d’hier soir |

### Collection et déblocages

| Prio | Clé ou fichier:ligne | Texte actuel | Problème | Proposition |
|---|---|---|---|---|
| 2 | `unlock_new` | Nouveau : %1$s | Accord faux avec les noms féminins ou plusieurs éléments (« Nouveau : Écharpe tricotée, Plume d’oie ») ; liste jointe par « , » (`QuizActivity.kt:131`). | Nouveau dans la collection : %1$s, dernier élément joint par « et » |
| 2 | `Unlocks.kt:53` (else) | Encore 8 quiz | Se confond avec « Encore 21 quiz réussis » : on ne sait pas que tout quiz compte. | Encore 8 quiz à faire |
| 3 | `Unlocks.kt:53` (n = 1) | Encore 1 quiz réussi | Chiffre seul un peu comptable. | Encore un quiz réussi (un/une selon le cas : « Encore un soir de quiz ») |
| 3 | `collection_choose` | Choisir | (rappel M6) ne dit pas ce qui est choisi ; grisé sans explication quand rien n’a changé. | Garder cette tenue |

### Typographie

| Prio | Clé ou fichier:ligne | Texte actuel | Problème | Proposition |
|---|---|---|---|---|
| 1 | `assets/citations.json` (450 citations) | C'est une tâche… ; J’aime l'aube… qu’un… ; « tranchant;» | 245 citations avec apostrophe droite ', parfois mêlée à ’ dans la même citation ; 169 espaces ordinaires avant `: ; ! ?`, 10 sans espace. Visible en grand corps sur la carte, l’écran de verrouillage et la notification. `words.json` est, lui, impeccable (U+202F partout). | Normaliser dans `tools/citations` : ' → ’, espace avant `; : ! ?` → U+202F, puis régénérer `citations.json`. |
| 2 | `strings.xml:10,18,30,38,41,62,67,68,70,76` | espace ordinaire U+0020 avant `:` et `?` | Pas de rejet en début de ligne (la coupure UAX#14 l’interdit), mais espace trop large et incohérente avec les lignes 21, 27, 28, 48, 49 (U+00A0) et avec les données (U+202F). | `\u202F` partout avant `: ; ! ?` |
| 3 | `glose_bravo`, `glose_encore` | « bravo » avec espaces ordinaires | Lu par TalkBack seulement, sans effet visuel ; à aligner pour la cohérence. | «\u202Fbravo\u202F» |
| 3 | `time`, `quiz_time` | Heure d’affichage : 08:00 | Convention française « 8 h 00 » ; Android FR accepte 08:00. | Facultatif : Heure d’affichage : %1$d\u00A0h\u00A0%2$02d |

### Textes en dur hors `strings.xml`

Application monolingue, donc pas de bug ; mais ces textes échappent à toute relecture de `strings.xml` (aucun ne figurait dans l’audit précédent).

| Prio | Fichier:ligne | Texte actuel | Proposition |
|---|---|---|---|
| 3 | `SettingsActivity.kt:41` | 1 mot, 2 mots, 3 mots | `string-array per_day_items` |
| 3 | `SettingsActivity.kt:63` | Notification et écran de verrouillage, Notification seulement, Écran de verrouillage seulement | `string-array mode_items` ; en ×2,0 la 1re option tient sur 2 lignes, acceptable. |
| 3 | `SettingsActivity.kt:68` | Après la durée choisie, Au 1er/2e/3e déverrouillage | `string-array unlocks_items` |
| 3 | `SettingsActivity.kt:71` | En bas (laisse la place à l’horloge), Au milieu | `string-array text_pos_items` |
| 3 | `SettingsActivity.kt:129` | « %d min », « %d h » | `<string name="minutes">%1$d\u00A0min</string>`, idem `h` |
| 3 | `Unlocks.kt:5,11-31,53` | Poses, Pelages… ; 21 noms d’éléments ; phrases « Encore … » | `strings.xml` (noms) et `<plurals>` (indices) ; les noms sont bien écrits, rien à changer sur le fond. |
| 3 | `QuizActivity.kt:78,86` | « 1 / 4 », « ✓ », « ✗ » | `quiz_progress` (voir plus haut) ; garder ✓/✗. |
| 3 | `MainActivity.kt:124,138`, `DailyReceiver.kt:153,155`, `WordCard.kt:22,30`, `Quotes.kt:25` | «\u00A0%s\u00A0» répété 6 fois | Une chaîne `guillemets` («\u202F%1$s\u202F») ou une fonction partagée. |

## Notes générales sur le ton

- Vouvoiement tenu partout (« votre fond », « verrouillez », « reconnaîtrez-vous », « Prenez le temps ») : le garder. Seule entorse : l’infinitif « Toucher pour les activer » (`notif_off`). « Demain, on retente ? » : le « on » complice est voulu (choix du propriétaire), il fonctionne.
- Deux registres cohabitent sans heurt : réglages neutres et précis, quiz et Collection chaleureux. La voix de Monsieur Glose n’apparaît presque qu’en image ; deux annonces (juste, faux) et l’état vide sont les endroits naturels pour qu’il parle un peu (« Bien vu ! », « Pas tout à fait »), sans en faire plus.
- Bienveillance à l’échec respectée (« encore », « Demain, on retente ? », pas de score rouge) ; « Faux » dans l’annonce TalkBack est le seul mot sec.
- Noms de la Collection : réussis, littéraires et concrets (« Assis sur son volume », « Nuit d’encre », « Marque-page à gland d’or »). Ne pas y toucher.
- Termes stables : « écran de verrouillage », « Quiz du soir », « Collection de Monsieur Glose » sont désormais uniformes. Le mot « afficher » est en revanche surutilisé dans les réglages (6 fois) : il rend « Affichage » et « Fin sur l’écran de verrouillage » flous ; préférer le verbe de l’effet (« où afficher », « retirer »).
- Les aides des réglages sont justes sur le fond mais écrites en phrases d’ingénieur (deux idées par phrase, « au plus tard », « reste un maximum »). Règle simple : une idée par phrase, la conséquence pour l’utilisateur d’abord.
- Capitales : la casse des chaînes est normale et `Label` met en capitales (accents conservés : « À PROPOS », « ÉCRAN DE VERROUILLAGE ») ; rien à corriger.
- Petits écrans (w360, police ×2,0) : rien n’est tronqué ; seuls `more_quotes`, `palettes`/`polices` et `exact_no` se coupent mal ou s’allongent, traités ci-dessus.
