# Thunder Beacon (Fabric, Minecraft 1.21.1)

Remplace le rendu du rayon de balise par :
- une colonne ronde (24 faces) en 4 couches, bleu tonnerre, qui vibre et scintille ;
- un noyau sphérique lumineux au-dessus de la balise ;
- des orbes d'énergie qui montent le long du rayon ;
- une hélice de petites orbes autour du rayon.

## Compiler
1. Java 21 + Gradle 8.8+ installés.
2. Dans ce dossier : `gradle wrapper --gradle-version 8.10` puis `./gradlew build`
3. Le .jar se trouve dans `build/libs/thunderbeacon-1.0.0.jar` (utiliser celui sans `-sources`).
4. Le copier dans `.minecraft/mods/` avec Fabric Loader 0.16+ pour 1.21.1. Mod client uniquement.

Dev : `./gradlew runClient`
