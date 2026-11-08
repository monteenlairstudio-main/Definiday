# Le son de Definiday

Definiday peut faire de tout petits bruits doux, comme un livre qu’on feuillette près d’un chat. C’est une option, **désactivée par défaut** : l’appli sert souvent en public (transport, bureau), et le silence reste la règle.

## Où se trouve le réglage

Réglages › Apparence › interrupteur **« Sons »** (`Prefs.sounds`, faux par défaut). L’activer joue la page qui tourne, pour entendre ce qu’on choisit.

## Les moments sonores

| Moment | Fichier (`res/raw`) | Son | Durée | Visuel associé |
|---|---|---|---|---|
| Bonne réponse au quiz | `son_tic` | « tic » de bois, sec comme du papier | 0,16 s | ✓, bouton qui gonfle, Glose hoche la tête |
| Mauvaise réponse | `son_hmm` | « hmm » grave et doux, qui descend un peu ; jamais un buzzer | 0,38 s | ✗, bouton qui tremble, Glose penche la tête |
| Quiz réussi, sans nouveauté | `son_carillon` | petit carillon de deux lames, à la quinte | 0,95 s | Glose écrit « Bravo » |
| Quiz raté | (aucun) | silence, pour ne jamais se moquer ; sauf s’il débloque une nouveauté : tampon | | Glose écrit « Encore » |
| Nouveautés débloquées (quiz réussi ou raté) | `son_tampon` | « tchak » de tampon encreur | 0,20 s | tampons qui arrivent en cascade |
| Nouveau mot au hasard | `son_page` | page qui tourne | 0,45 s | la carte change |
| Toucher Monsieur Glose | `son_mrr` | petit « mrr » qui monte | 0,32 s | réaction de Glose |
| Toucher spécial (4 touchers) | `son_ronron` | ronron, deux respirations | 1,70 s | réaction spéciale |

Déclencheurs : `QuizActivity` (tic / hmm à chaque réponse ; en fin de quiz, tampon s’il y a une nouveauté, même sur un quiz raté, sinon carillon si réussi, sinon rien), `MainActivity` (page, « Au hasard »), `SettingsActivity` (page, en échantillon à l’activation), `Glose.kt` (mrr au toucher, ronron au toucher spécial, sur l’accueil et l’aperçu de la Collection, avec les limites de `Taps`). `Sounds` vérifie seul le réglage et le mode sonnerie.

## Ce qui reste muet

- **L’ouverture de l’appli** : jamais de son, ni à l’accueil ni à l’écran de verrouillage.
- **Les notifications** : elles gardent le son du canal système (« Mot du jour et quiz »), que chacun règle dans Android. Definiday n’y ajoute rien.
- **L’échec** : un quiz raté n’a pas de son.
- **La navigation** (boutons, réglages, collection) : les clics système suffisent.

## Respect du téléphone

- Les sons passent par `SoundPool` avec `AudioAttributes.USAGE_ASSISTANCE_SONIFICATION` : Android les range avec les sons du système (volume « Sons système » ou sonnerie selon l’appareil), pas avec la musique.
- **Aucun son si le téléphone n’est pas en mode sonnerie normal** (`AudioManager.ringerMode` différent de `RINGER_MODE_NORMAL`, donc ni en silencieux ni en vibreur).
- `SoundPool` n’est créé qu’au premier son et libéré après 30 s sans son : rien ne reste chargé pour rien. Exception : le quiz (sons activés) charge le tic, le hmm, le carillon et le tampon dès son ouverture et les garde tant qu’il est ouvert, pour que le premier tic ne soit pas en retard. Un chargement raté est oublié (retenté au son suivant).

## Volume et durée

- Tous les sons font moins de 2 s, la plupart moins d’une demi-seconde.
- Niveau égalisé : la tranche de 50 ms la plus forte de chaque son vise −20 dBFS (écart mesuré ≤ 1 dB), crête plafonnée à −3 dBFS (crêtes réelles entre −10 et −15 dBFS), aucun écrêtage.
- Fondus de 5 ms au début et à la fin (pas de clic).
- Timbres chauds : centre spectral entre 400 Hz et 3 kHz. Le « hmm » porte des harmoniques, car les haut-parleurs de téléphone coupent sous 300 Hz environ.
- Fichiers OGG Vorbis mono 44,1 kHz, chacun sous 20 Ko, 49 Ko en tout.
- **Limite connue** : ce niveau est mesuré sur la bande pleine. À travers un haut-parleur de téléphone (rien sous ≈ 300 Hz), le tampon est ≈ 18 dB sous le tic, le ronron ≈ 16 dB, le hmm et le mrr ≈ 5-7 dB. Une **proposition à écouter** (sons nivelés après passe-haut 300 Hz, mesures avant/après et guide d’écoute) est dans [`son/proposition_niveaux/`](son/proposition_niveaux/README.md). Pour l’appliquer, une commande : `python3 tools/sounds/make_sounds.py --phone-level`.

## Accessibilité

Aucune information n’est donnée par le son seul : chaque son double un signe visible (✓ / ✗, texte, animation) et, au quiz, le texte lu par TalkBack. Couper le son ne fait rien perdre.

## Fabrication et licence

Les sons sont **entièrement synthétisés** par `tools/sounds/make_sounds.py` (numpy, puis ffmpeg pour l’encodage) : bruit filtré pour la page et le tampon, lames amorties façon marimba pour le « tic » et le carillon, bruit grave pulsé à 25 Hz sur une note basse pour le ronron. Aucun échantillon extérieur : ils appartiennent au projet, sous la même licence que le code. Le script est déterministe (graine fixe) et affiche durée, crête, niveau, taille et centre spectral de chaque son :

```
python3 tools/sounds/make_sounds.py                 # les sons livrés
python3 tools/sounds/make_sounds.py --phone-level   # nivelés pour haut-parleur de téléphone (proposition)
```
