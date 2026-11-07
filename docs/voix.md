# Voix de Glose

Règle unique, appliquée dans `values/strings.xml` (revue UI 0.11, `docs/audits/revue_ui_011.md`, n° 17).

## Qui parle

| Cas | Voix | Exemples |
|---|---|---|
| Glose s’adresse à vous : onboarding, états vides, retours du quiz, carte de notation, bulles et notes à venir | **1re personne**, vouvoiement ; « on » complice permis | « Je suis %1$s, chat bibliothécaire. », « À quelle heure passerai-je ? », « Lisez le mot du jour : je vous attends ce soir. », « Demain, on retente ? », « Nos mots vous plaisent ? Un mot sur le Play Store m’aiderait beaucoup. » |
| L’interface parle de lui : titres, libellés, TalkBack, Collection, annonces de déblocage | **3e personne**, par son nom | « %1$s, chat bibliothécaire », « %1$s a gagné : », « Collection %1$s », « %1$s écrit « bravo » avec le fil de sa pelote » |
| Réglages, aides, notifications utilitaires | **neutre** (ni « je » ni Glose) | « Le mot quitte l’écran de verrouillage après cette durée au plus tard. », « le ronron du chat » |

L’appli ne parle jamais d’elle-même à la place de Glose (« Definiday vous plaît ? » est devenu une réplique de Glose). « Il » seul est réservé au mot ; pour lever l’ambiguïté, on écrit « Ce mot porte… ».

## Le nom

- Une seule chaîne l’écrit : `glose_name` (« Monsieur Glose »), défaut de `Prefs.gloseName`. Toutes les autres le reçoivent en `%1$s` (ou `%3$s`). `VoixTest` vérifie qu’aucune autre ressource ne contient « Glose ».
- **Élision** : écrire sans « de %1$s » (sujet + verbe, « %1$s a gagné »). Sinon, passer `de(nom)` (`Prefs.kt`) : « d’Anatole », « d’Émile », mais « de Monsieur Glose », « de Hugo » (pas d’élision devant h, Y ou une initiale : « de » y est toujours admis).
- **Genre** : aucun adjectif ni participe accordé à Glose dans ses répliques (« J’ai sommeil », pas « Je suis fatigué »). Exception assumée : « Enchanté. » en tête d’onboarding, affiché avant tout renommage possible.
- Pas encore d’écran pour le renommer (longueur 20 ou 24 caractères à trancher) ; le getter rejette déjà le vide, plus de 24 caractères et les caractères de contrôle.

## Typographie

Apostrophe ’, guillemets « » avec espaces fines, U+202F avant ; : ! ? et dans « 2 / 4 », U+00A0 dans « 2 h ».
