# Guia rapida: que archivo tocar

Este archivo sirve como mapa de navegación. Si quieres cambiar algo y no sabes dónde mirar, empieza aquí.

## Regla general

- Si cambias comportamiento, casi siempre vas a `Swap/src`.
- Si cambias datos editables, ve a `Swap/res`.
- Si cambias navegación o pantallas, mira `Swap/src/scene`.
- Si cambias guardado, mira `Swap/src/save`.
- Si cambias integración con la web, mira `Swap/src/online`.

## Arranque y composición

### Quiero cambiar cómo arranca el juego

Archivos:

- `Swap/src/app/bootstrap/Main.java`
- `Swap/src/app/bootstrap/GamePanel.java`
- `Swap/src/app/bootstrap/SceneComposer.java`
- `Swap/src/app/bootstrap/GameConfig.java`

Usalos para:

- ventana inicial
- bucle del juego
- dependencias compartidas
- parámetros globales como tamaño de pantalla o mapa base

### Quiero cambiar teclas, cámara o utilidades de entrada

Archivos:

- `Swap/src/app/input/KeyboardState.java`
- `Swap/src/app/camera/Camera.java`

## Escenas y flujo

### Quiero cambiar el menú principal

Archivos:

- `Swap/src/scene/menu/TitleScene.java`
- `Swap/src/scene/menu/TitleRosterSyncController.java`

### Quiero cambiar el login

Archivos:

- `Swap/src/scene/menu/LoginScene.java`
- `Swap/src/app/dialog/AccountDialogs.java`
- `Swap/src/online/auth/OnlineAccountService.java`
- `Swap/src/online/auth/SwapWebClient.java`

### Quiero cambiar el game over

Archivo:

- `Swap/src/scene/menu/GameOverScene.java`

### Quiero cambiar el mundo principal

Archivos:

- `Swap/src/scene/gameplay/WorldScene.java`
- `Swap/src/scene/gameplay/world/WorldStartLayout.java`
- `Swap/src/scene/gameplay/world/WorldProgressSyncController.java`

Usalos para:

- spawn inicial
- mapa inicial
- sync de progreso con la cuenta
- coordinación general del mundo

### Quiero cambiar opciones dentro del mundo

Archivos:

- `Swap/src/scene/gameplay/control/WorldOptionsMenu.java`
- `Swap/src/scene/gameplay/control/WorldSaveController.java`
- `Swap/src/scene/gameplay/runtime/WorldPerformanceTracker.java`

## Guardado

### Quiero cambiar autosave, manual save o selección de guardados

Archivos:

- `Swap/src/save/SaveManager.java`
- `Swap/src/save/SaveReference.java`
- `Swap/src/save/SaveKind.java`
- `Swap/src/save/metadata/SaveMetadataFactory.java`
- `Swap/src/save/metadata/SaveSlotMetadata.java`
- `Swap/src/save/roster/SaveRosterSyncService.java`
- `Swap/src/save/store/SaveIndexStore.java`
- `Swap/src/save/store/SaveMetadataStore.java`
- `Swap/src/save/store/SaveProfilePaths.java`

Usalos para:

- elegir cuál save cargar
- crear metadata
- borrar o renombrar saves
- sincronizar roster manual con la cuenta web

## ECS y gameplay

### Quiero cambiar el movimiento o la entrada del jugador

Archivos:

- `Swap/src/system/input/InputSystem.java`
- `Swap/src/system/world/MovementSystem.java`

### Quiero cambiar combate

Archivos:

- `Swap/src/system/combat/CombatSystem.java`
- `Swap/src/system/combat/DropSystem.java`
- `Swap/src/system/combat/HealthSystem.java`
- `Swap/src/system/combat/ProjectileSystem.java`

### Quiero cambiar inventario, interacción o quests

Archivos:

- `Swap/src/system/inventory/InventorySystem.java`
- `Swap/src/system/inventory/InventoryOps.java`
- `Swap/src/system/inventory/CharacterScreenSystem.java`
- `Swap/src/system/interaction/InteractionSystem.java`
- `Swap/src/system/interaction/NpcInteractionSystem.java`
- `Swap/src/system/interaction/WorldObjectInteractionSystem.java`
- `Swap/src/system/interaction/ShopSystem.java`
- `Swap/src/system/interaction/TradeSystem.java`
- `Swap/src/system/interaction/InteractionSupport.java`
- `Swap/src/system/quest/QuestSystem.java`
- `Swap/src/system/loot/LootSystem.java`

### Quiero cambiar animaciones, cámara o render

Archivos:

- `Swap/src/system/render/RenderSystem.java`
- `Swap/src/system/render/AnimationSystem.java`
- `Swap/src/system/render/CameraSystem.java`

## Contenido y data-driven

### Quiero cambiar al jugador base

Archivos:

- `Swap/res/content/players/hero.json`
- `Swap/src/content/prefab/PlayerPrefabBuilder.java`
- `Swap/src/content/prefab/PrefabFactory.java`

### Quiero cambiar clases

Archivos:

- `Swap/res/content/progression/classes/*.json`
- `Swap/src/progression/ProgressionCalculator.java`

### Quiero cambiar enemigos

Archivos:

- `Swap/res/content/enemies/*.json`
- `Swap/src/content/prefab/PrefabFactory.java`

### Quiero cambiar NPCs

Archivos:

- `Swap/res/content/npcs/*.json`
- `Swap/src/content/prefab/PrefabFactory.java`

### Quiero cambiar economía o quests base

Archivos:

- `Swap/res/content/world/rules/*.json`
- `Swap/res/content/world/placements/*.json`
- `Swap/src/data/DataRegistry.java`

## Mapas y tiles

### Quiero cambiar el mapa principal

Archivos:

- `Swap/res/maps/world/worldV2.txt`
- `Swap/res/maps/world/tiled/map.tmx`
- `Swap/src/content/world/WorldSeeder.java`
- `Swap/src/asset/MapLoader.java`
- `Swap/src/asset/TmxMapLoader.java`

### Quiero cambiar tiles, colisión o foreground

Archivos:

- `Swap/src/asset/TileMap.java`
- `Swap/src/content/catalog/TileCatalog.java`
- `Swap/res/tiles/*`

## Assets y audio

### Quiero cambiar sprites o animaciones registradas

Archivos:

- `Swap/src/content/bootstrap/AssetBootstrap.java`
- `Swap/src/asset/AssetManager.java`

### Quiero cambiar audio

Archivos:

- `Swap/src/audio/AudioBootstrap.java`
- `Swap/src/audio/AudioService.java`

## UI

### Quiero cambiar HUD, overlays o texto visible

Archivos:

- `Swap/src/ui/hud/HudRenderer.java`
- `Swap/src/ui/hud/WorldHudRenderer.java`
- `Swap/src/ui/hud/InventoryHudRenderer.java`
- `Swap/src/ui/state/UiState.java`
- `Swap/src/ui/text/UiText.java`

### Quiero cambiar textos o idioma

Archivos:

- `Swap/src/ui/text/ContentText.java`
- `Swap/src/ui/text/UiLanguage.java`
- `Swap/src/ui/text/UiText.java`

## Online y sincronización

### Quiero cambiar la cuenta online o la sincronización con la web

Archivos:

- `Swap/src/online/auth/OnlineAccountService.java`
- `Swap/src/online/auth/SwapWebClient.java`
- `Swap/src/online/sync/PlayerProgressSnapshotFactory.java`
- `Swap/src/online/sync/PlayerProgressSnapshot.java`
- `Swap/src/scene/gameplay/world/WorldProgressSyncController.java`

## Atajos mentales

- Si ves `scene`, piensa en flujo de pantalla.
- Si ves `system`, piensa en reglas del juego.
- Si ves `component`, piensa en datos de entidades.
- Si ves `content`, piensa en creación de entidades y mundo.
- Si ves `save`, piensa en persistencia.
- Si ves `online`, piensa en web y cuenta.
- Si ves `ui`, piensa en lo que se dibuja encima del juego.

## Qué buscar si no sabes por dónde empezar

- Problema de arranque o ventana: `app/bootstrap`
- Problema de mapa o spawn: `scene/gameplay/world` y `content/world`
- Problema de combate: `system/combat`
- Problema de inventario o diálogo: `system/inventory` y `system/interaction`
- Problema de guardado: `save`
- Problema de login o token: `online/auth`

Si una duda no entra en estas categorías, casi siempre empieza por `WorldScene` o `SceneComposer`, porque suelen estar en la frontera entre sistemas.
