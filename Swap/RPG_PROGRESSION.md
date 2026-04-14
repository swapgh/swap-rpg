# RPG Progression

Este documento explica cómo se balancean las clases del juego y dónde tocar los números sin perderte.

## Dónde vive la progresión

- `Swap/res/content/players/hero.json`
- `Swap/res/content/progression/classes/*.json`
- `Swap/res/content/progression/rules/core.json`
- `Swap/src/progression/ProgressionCalculator.java`

`hero.json` define el personaje base.
Los archivos de clase definen el perfil real de `warrior`, `mage` y `druid`.
`ProgressionCalculator` convierte esos datos en stats runtime.

## Qué define cada archivo

### `hero.json`

Define la base del jugador:

- nombre
- clase inicial
- facción
- spawn base
- collider
- visual base
- stats mínimos
- ataque y proyectil base
- flags de mundo

No debería usarse como balance fino de clase. Ese trabajo pertenece a los archivos de clase.

### `classes/*.json`

Cada clase define:

- atributos base
- crecimiento por nivel
- hp base
- mana base
- velocidad base
- escalado de ataque
- arma o armadura inicial si aplica

## Clases actuales

- `warrior`: tanque y melee sostenido.
- `mage`: burst caster / glass cannon.
- `druid`: híbrido con sustain.

## Fórmula base

La progresión de atributos sigue esta idea:

`attribute(level) = base + growth * (level - 1)`

Luego `ProgressionCalculator` deriva el resto de stats:

- HP
- Mana
- Attack
- Attack Speed
- Ability Power
- Defense
- Healing Power

## Qué tocar para balancear

### Si quieres que el warrior aguante más

Modifica:

- `Swap/res/content/progression/classes/warrior.json`
- `Swap/src/progression/ProgressionCalculator.java` solo si cambias la fórmula global

### Si quieres que el mage pegue más fuerte

Modifica:

- `Swap/res/content/progression/classes/mage.json`
- `Swap/src/progression/ProgressionCalculator.java` solo si ajustas la relación entre atributos y daño

### Si quieres que el druid cure más o escale mejor

Modifica:

- `Swap/res/content/progression/classes/druid.json`
- `Swap/src/progression/ProgressionCalculator.java` solo si cambias la fórmula de curación o AP

## Qué no tocar si solo quieres balance

- `Swap/src/content/prefab/PlayerPrefabBuilder.java` si no vas a cambiar el arranque del jugador.
- `Swap/src/content/world/WorldSeeder.java` si no vas a cambiar el mapa o el spawn.

Esos archivos están más cerca de la creación del personaje que del balance puro.

## Sobre `hero.json`

Ahora mismo el héroe base arranca con:

- `classId: warrior`
- `spawn` definido en el archivo
- stats mínimos muy bajos

Eso está bien porque `hero.json` sirve como plantilla de arranque.
Las diferencias reales entre clases viven en los archivos de clase.

## Guía mental rápida

- Cambiar números de clase: `classes/*.json`
- Cambiar fórmula global: `ProgressionCalculator.java`
- Cambiar cómo nace el jugador: `hero.json` y `PlayerPrefabBuilder.java`
- Cambiar mapa o spawn inicial: `WorldStartLayout.java` y `GameConfig.java`

## Regla práctica

Si el cambio afecta a una sola clase, casi siempre basta con tocar su JSON.
Si el cambio afecta a todas las clases, seguramente va en `ProgressionCalculator`.
Si el cambio afecta al arranque del jugador, ya no es balance: es construcción de personaje.
