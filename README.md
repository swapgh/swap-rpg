# Swap RPG

Java 2D RPG construido sobre una base ECS sencilla y data-driven.

## Estructura

- `Swap/src`: codigo fuente
- `Swap/res`: sprites, audio, mapas y archivos JSON de contenido
- `.vscode`: configuracion para correr el proyecto desde VS Code

## Ejecutar

Desde VS Code:
- abre esta carpeta `swap-rpg`
- usa `Run and Debug`
- ejecuta `Run Swap RPG`

Desde terminal:

```bash
find Swap/src -name '*.java' | sort > build_sources.txt
javac -d build_classes @build_sources.txt
java -cp build_classes:Swap/res app.Main
```

## Arquitectura

- `ecs`: mundo ECS y contrato de systems
- `component`: datos puros
- `system`: logica de juego
- `asset`: carga de recursos y mapas
- `content`: registro de sprites, tiles y prefabs
- `data`: carga de contenido externo desde JSON
- `scene`: escenas principales del juego

## Contenido data-driven

El juego carga definiciones desde:

- `Swap/res/content/players`
- `Swap/res/content/enemies`
- `Swap/res/content/npcs`

Eso permite cambiar stats, visuales y comportamiento base sin reescribir la logica central.

## Estado actual

- jugador con movimiento, combate cuerpo a cuerpo y proyectil
- slimes y enemigo a distancia
- NPCs con dialogo
- inventario, llaves, puertas, cofres y monedas
- save/load simple

## Nota

La documentacion tecnica corta del proyecto esta en `Swap/ECS_PROJECT.md`.
