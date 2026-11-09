# Backlog de nuit

Sources : `audit_sec_1`, `audit_fon_son_1`, `audit_ux_1`, `glose_1` (scratchpad de session, copiés dans le rapport final).

## Itération 1

| Lot | Axe | Contenu | Qui | État |
|---|---|---|---|---|
| sec-0 | sec | Test de profil doré 0.14.0 et couverture des clés de prefs | maître | fait 1ca8efc |
| sec-1 | sec | Export `"wt"` (S3), BOM toléré (S8) | maître | fait c8ab3ed |
| crap-1 | crap | Tests du flux d'installation et de Réglages › Mises à jour (sans toucher au code de prod) | équipe SEC | en cours |
| fon-A | fon/sec | Minuit (N1, N7), bandeau notifications (N3), extra `mot` restreint (S4) | équipe FON | en cours |
| gla-1 | gla | Benchmark et audit de Glose, plan de retouches | équipe GLOSE | en cours |

## Suivants (P1 → P3)

- fon-B : rotation de l'écran de score (N2) et préchauffage des sons du quiz.
- fon-C : renommage de Glose dans Réglages (fichiers réservés attribués), signature `WordCard`, titre de Collection (N5).
- maître : durée hors liste écrasée (N4), import/export hors du thread UI (S7), citations qui commencent par « (P12), crédit Wikiquote (P19).
- son : niveau des sons tampon/ronron au haut-parleur (−16 à −18 dB) : proposition d'écoute, pas de fusion.
- fon : reprogrammer avant d'afficher (N6), UnlockService sans mot (N10), avertissement quiz avant le mot (P17), libellés des menus en ressources (P19), états à la rotation (N11), pause de la vie au repos de Glose.
- crap : découpage de `QuizActivity.onCreate` (CC 23) après tests de caractérisation.
- Propriétaire : secrets de signature dans l'environnement `release` (S1), tags (S2), empreinte du certificat au README (S6), `verification-metadata` (S5).
