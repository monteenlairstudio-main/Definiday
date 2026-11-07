# Ne plus perdre son profil

Le profil, c’est tout ce que Definiday garde de vous : réglages, mots déjà vus, révisions du quiz, favoris, série et collection de Glose (poses, pelages, pelotes, accessoires, nom).

## Pourquoi des profils ont été perdus

Android n’efface les données d’une appli qu’à sa **désinstallation**. Une mise à jour normale garde tout.

Mais Android n’accepte une mise à jour que si elle est signée par **la même clé** que l’appli installée. Sinon il refuse, et la seule issue est de désinstaller, ce qui efface le profil.

C’est ce qui arrivait avec les APK de la CI : le job `test` construit l’APK release **sans les secrets**, donc signé avec la clé de debug de la machine GitHub, une clé jetable, différente à chaque exécution et différente de la clé du propriétaire. Installer un tel APK par-dessus la vraie appli imposait de la désinstaller.

## Les règles

1. **Par-dessus la vraie appli, n’installer que des versions signées release** : l’artefact `definiday-release` (job `release`, sur `main` et les tags) ou un APK construit avec le keystore (voir le README).
2. **Les versions de test s’appellent « Definiday (test) »**, avec une icône rouge cire, et ont l’identifiant `fr.definiday.dev`. Ce sont toutes les versions signées debug : `assembleDebug`, et `assembleRelease` sans keystore (artefact `definiday-test` de la CI). Elles s’installent **à côté** de la vraie appli, avec leur propre profil, et ne peuvent jamais la remplacer. Les deux applis peuvent cohabiter sans gêne (widget, partage, alarmes séparés).
3. **Exporter son profil avant toute opération risquée** : changement de téléphone, de clé de signature, passage d’un APK à la version Play Store, réinitialisation.

La sauvegarde automatique d’Android reste active (compte Google, transfert d’un téléphone à l’autre) : elle couvre tout le profil sauf l’état du fond d’écran (`wallpaper_state`), propre à chaque téléphone.

## Exporter

Réglages › Mon profil › **Exporter mon profil**. Android propose où enregistrer le fichier `definiday-profil-AAAA-MM-JJ.json` (Téléchargements, Drive…). Aucune permission n’est demandée : c’est le sélecteur de fichiers du système.

## Restaurer

Réglages › Mon profil › **Restaurer un profil**, puis choisir le fichier. L’appli le vérifie d’abord : un fichier qui n’est pas un profil Definiday valide est refusé et rien ne change. Sinon elle demande « Remplacer le profil actuel ? » : le profil du fichier remplace alors entièrement celui de l’appli, les rappels (mot du jour, quiz du soir) sont reprogrammés et le widget redessiné.

Un profil exporté depuis « Definiday (test) » se restaure dans la vraie appli, et inversement.

## Format du fichier (pour les développeurs)

```json
{
  "format": "definiday-profil",
  "version": 1,
  "app": "0.12.0",
  "date": "2026-11-07",
  "files": {
    "definiday": { "hour": { "t": "int", "v": 8 }, "unlocked": { "t": "set", "v": ["livres"] } },
    "history":   { "abaque": { "t": "long", "v": 20399 } },
    "memoire":   { "m:abaque": { "t": "string", "v": "20399/2/1" }, "favoris": { "t": "set", "v": ["abaque"] } }
  }
}
```

- Un objet par fichier de préférences du profil (`Profile.FILES`), une entrée par clé avec son type : `string`, `int`, `long`, `boolean`, `float` ou `set` (liste de chaînes).
- À l’import : les trois fichiers sont obligatoires ; les fichiers et clés inconnus sont ignorés ; une clé connue au mauvais type, hors limites ou trop longue fait refuser tout le fichier ; 4 Mio au plus.
- Code : `Profile.export(ctx, uri)` et `Profile.import(ctx, uri)` (sans confirmation, à demander avant), ou `Profile.read` puis `Profile.apply` pour vérifier avant de confirmer, comme les Réglages.
