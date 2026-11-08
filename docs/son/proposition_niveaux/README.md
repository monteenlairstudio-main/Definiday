# Proposition : sons nivelés pour un haut-parleur de téléphone

**Statut : appliqué le 8 novembre 2026** (choix du propriétaire après écoute). `app/src/main/res/raw` contient maintenant ces fichiers.

```
python3 tools/sounds/make_sounds.py --phone-level --out docs/son/proposition_niveaux
```

## Le problème

Les sons livrés sont égalisés sur la bande pleine (tranche de 50 ms la plus forte à −20 dBFS). Un haut-parleur de téléphone ne restitue presque rien sous 300 Hz environ. Or le tampon (coup sourd 140 → 55 Hz) et le ronron (bruit grave et note à 95 Hz) vivent surtout là : au téléphone, le tampon est ≈ 18 dB sous le tic, le ronron ≈ 16 dB, le hmm et le mrr ≈ 5-7 dB. Le tampon, son de récompense, se perd juste après un tic bien présent.

## Ce que fait `--phone-level`

1. Le niveau est mesuré **après un passe-haut 300 Hz** (deux Butterworth du 2ᵉ ordre, comme le mesureur ffmpeg de l’audit), puis chaque son est porté à −20 dBFS (tranche de 50 ms la plus forte), crête ≤ −3 dBFS, fondus de 5 ms. La mesure porte sur le son déjà fondu (l’attaque du tampon dure 1 ms).
2. Gain seul pour le tic, la page, le carillon, le hmm et le mrr : les timbres ne changent pas.
3. Le tampon et le ronron sont **un peu retouchés**, sinon leur crête dépasserait −3 dBFS bien avant d’atteindre le niveau :
   - tampon : la moitié du coup sourd passe dans ses harmoniques (2 à 6 : on entend la même note grave, même sans la fondamentale) et la « claque » (bruit 300-2 500 Hz) est triplée ;
   - ronron : harmoniques 3 à 8 de la note à 95 Hz et une respiration élargie (bruit 300-900 Hz), même pulsation à 25 Hz.
4. Le script reste déterministe (graine fixe) ; sans le drapeau, il produit octet pour octet les fichiers actuels.

## Mesures avant / après

Tranche de 50 ms la plus forte, en dBFS, mesurée sur les OGG décodés (`ffmpeg`, passe-haut `highpass=f=300:poles=2` deux fois). Crête sur le décodé.

| Son | Avant : plein | Avant : > 300 Hz | Après : plein | **Après : > 300 Hz** | Crête avant → après | Taille avant → après |
|---|---|---|---|---|---|---|
| son_tic | −20,9 | −21,1 | −19,8 | **−20,0** | −12,4 → −11,3 | 4 388 → 4 374 o |
| son_page | −20,1 | −20,2 | −20,1 | **−20,2** | −9,8 → −9,9 | 7 199 → 7 208 o |
| son_carillon | −20,3 | −20,4 | −20,0 | **−20,2** | −12,8 → −12,5 | 7 407 → 7 357 o |
| son_mrr | −19,8 | −25,1 | −14,3 | **−19,6** | −11,6 → −6,2 | 5 876 → 5 902 o |
| son_hmm | −20,0 | −26,9 | −13,2 | **−20,0** | −15,3 → −8,3 | 4 747 → 4 567 o |
| son_ronron | −20,1 | −36,1 | −14,6 | **−20,8** | −12,2 → −3,2 | 15 639 → 19 214 o |
| son_tampon | −20,9 | −39,0 | −16,2 | **−20,2** | −11,5 → −3,8 | 4 965 → 5 013 o |
| **écart max** | 1,1 dB | **18,8 dB** | 6,9 dB | **1,2 dB** | toutes ≤ −3 | 49,0 → 52,4 Ko |

Critères tenus : > 300 Hz à −20 ± 1,5 dB pour les 7 (−20,8 à −19,6), crête ≤ −3 dBFS, chaque fichier < 20 Ko (le plus gros : ronron, 19,2 Ko), aucun écrêtage.

**Le prix** : au casque (bande pleine), le hmm, le mrr, le ronron et le tampon deviennent 4 à 7 dB plus forts que le tic, la page et le carillon. On ne peut pas égaliser les deux à la fois sans retirer les graves de ces sons.

## Guide d’écoute

Le haut-parleur du téléphone est la référence : c’est ainsi que l’appli s’entend le plus souvent. Écouter ensuite au casque, pour vérifier que rien n’y devient gênant.

**Comment** : copier les deux dossiers sur le téléphone (par exemple `adb push app/src/main/res/raw /sdcard/Music/sons_avant` et `adb push docs/son/proposition_niveaux /sdcard/Music/sons_apres`), volume média vers la moitié, et les lire dans un lecteur de fichiers. Un haut-parleur d’ordinateur portable coupe aussi les graves et donne une idée proche ; pas d’écouteurs pour la première passe.

**Quoi comparer** (avant puis après, chaque fois) :

1. **tic puis tampon** : la fin d’un quiz avec nouveauté. Avant, le tampon s’efface derrière les tics ; après, il doit s’entendre autant, et rester un « tchak » de tampon, pas un clic ni un claquement de porte.
2. **carillon puis tampon** : les deux fins de quiz. Même présence, deux caractères.
3. **mrr puis ronron** : toucher Glose, puis le toucher spécial. Le ronron doit rester un ronron (doux, pulsé), pas un bourdonnement ni un moteur.
4. **tic puis hmm** : bonne et mauvaise réponse. Le hmm, plus fort, doit rester doux et jamais faire buzzer.
5. **Au casque**, les 7 à la suite : le hmm, le mrr, le ronron et le tampon sont-ils trop forts face au tic ?

**Décisions possibles** : tout appliquer ; n’appliquer que le tampon et le ronron (les deux vrais trous) ; ou demander un compromis (par exemple viser le milieu entre les deux mesures), à régénérer.

## Appliquer

```
python3 tools/sounds/make_sounds.py --phone-level
```

Cette commande réécrit `app/src/main/res/raw/son_*.ogg` avec exactement les fichiers de ce dossier. Mettre ensuite à jour la section « Volume et durée » de `docs/son.md` (et ce statut). `SoundsTest` (fichiers entre 1 000 et 20 000 octets) reste vert. Pour revenir en arrière : `python3 tools/sounds/make_sounds.py`.

Pour n’appliquer que le tampon et le ronron : `cp docs/son/proposition_niveaux/son_{tampon,ronron}.ogg app/src/main/res/raw/` (le script ne sait pas reproduire ce mélange : à lui ajouter si c’est le choix retenu).
