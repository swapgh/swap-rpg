# Guia Rapida: Que Archivo Tocar

Esta guia es un mapa rapido del proyecto para saber donde cambiar cada cosa sin tener que buscar todo otra vez.

## Regla general

- `Swap/src`: logica del juego
- `Swap/res`: contenido externo como mapas, sprites, audio y JSON

Si queres cambiar:
- comportamiento: casi siempre `Swap/src`
- stats, dialogos base o prefabs: muchas veces `Swap/res/content`
- arte, audio o mapas: `Swap/res`

## Gameplay y mundo

### Respawn de enemigos

Archivo principal:
- `Swap/src/content/WorldSeeder.java`

Que cambias ahi:
- que enemigo spawnea
- area de spawn
- cantidad maxima viva
- tiempo de respawn
- tiempo de reintento
- distancia minima al jugador

Ejemplo actual:
- slimes y orcos se crean con `createEnemySpawner(...)`

Logica runtime:
- `Swap/src/system/RespawnSystem.java`
- `Swap/src/component/RespawnSpawnerComponent.java`
- `Swap/src/component/RespawnAreaComponent.java`

Si queres:
- cambiar solo numeros: toca `WorldSeeder.java`
- cambiar reglas del sistema de spawn: toca `RespawnSystem.java`

### Hora del mundo, dia/noche y cambios por horario

Archivos principales:
- `Swap/src/system/DayNightSystem.java`
- `Swap/src/component/WorldTimeComponent.java`
- `Swap/src/system/TimeSystem.java`

Que cambias ahi:
- cuando se considera dia o noche
- que cosas cambian segun horario
- tecla de prueba para forzar cambio horario

Persistencia de hora:
- `Swap/src/system/SaveLoadSystem.java`

### Player, combate y movimiento

Archivos principales:
- `Swap/src/system/InputSystem.java`
- `Swap/src/system/MovementSystem.java`
- `Swap/src/system/CombatSystem.java`
- `Swap/src/system/ProjectileSystem.java`

Usalos para:
- cambiar controles base
- cambiar hitboxes o chequeos de combate
- ajustar proyectiles o melee

## Enemigos, NPCs y prefabs

### Stats base de enemigos

Archivos:
- `Swap/res/content/enemies/green_slime.json`
- `Swap/res/content/enemies/orc_pyromancer.json`
- `Swap/src/data/DataRegistry.java`

Que cambias ahi:
- vida
- daño
- defensa
- velocidad
- collider
- visual base referenciada

`DataRegistry.java` registra los ids de contenido que el juego conoce.

### Player

Archivos:
- `Swap/res/content/players/hero.json`
- `Swap/src/content/PrefabFactory.java`

Usalos para:
- spawn del player
- collider
- visuales base y animaciones

### NPCs

Archivos:
- `Swap/res/content/npcs/...`
- `Swap/src/content/PrefabFactory.java`
- `Swap/src/system/InteractionSystem.java`

Usalos para:
- dialogos
- comportamiento de interaccion
- quests o respuestas por horario

## Mapas y tiles

### Mapa actual

Archivo:
- `Swap/res/map/worldV2.txt`

Carga:
- `Swap/src/asset/MapLoader.java`
- `Swap/src/content/WorldSeeder.java`

Si queres:
- cambiar el layout del mundo: edita `worldV2.txt`
- cambiar como se interpreta el archivo: edita `MapLoader.java`

### Tiles del mundo

Archivos:
- `Swap/src/content/TileCatalog.java`
- `Swap/res/tiles/...`

Usalos para:
- cambiar que imagen usa cada tile
- marcar si un tile bloquea o no

## Arte y animaciones

### Registro de sprites y clips

Archivo:
- `Swap/src/content/AssetBootstrap.java`

Usalo para:
- registrar frames
- cambiar clips de animacion
- apuntar a nuevos PNG

Recursos reales:
- `Swap/res/player`
- `Swap/res/enemy`
- `Swap/res/tiles`

## UI, HUD y escenas

### HUD y overlays

Archivo principal:
- `Swap/src/ui/HudRenderer.java`

Usalo para:
- barras, textos, overlays
- menu de game over
- menu de opciones
- mostrar hora, monedas, inventario, ayudas visuales

Textos:
- `Swap/src/ui/UiText.java`

### Menu principal

Archivo:
- `Swap/src/scene/TitleScene.java`

Usalo para:
- opciones del menu
- cargar guardados
- submenu de renombrar/borrar saves

### Juego principal

Archivo:
- `Swap/src/scene/WorldScene.java`

Usalo para:
- flujo dentro del mundo
- pausa y menu `F10`
- quick save y guardado manual
- apertura de inventario
- cambio entre modos del juego

### Game Over

Archivo:
- `Swap/src/scene/GameOverScene.java`

Usalo para:
- opciones al morir
- cargar autosave
- cargar manual save
- volver al menu principal

## Guardado

### Sistema nuevo de saves

Archivos:
- `Swap/src/save/SaveManager.java`
- `Swap/src/save/SaveReference.java`
- `Swap/src/save/SaveSlotMetadata.java`
- `Swap/src/save/SaveKind.java`
- `Swap/src/app/SaveDialogs.java`

Usalos para:
- autosave vs manual save
- multiples manual saves
- renombrar y borrar saves
- guardar cual fue el ultimo save usado

Configuracion:
- `Swap/src/app/GameConfig.java`

Datos guardados:
- `Swap/src/system/SaveLoadSystem.java`

## Audio

Archivos:
- `Swap/src/audio/AudioService.java`
- `Swap/src/audio/AudioBootstrap.java`
- `Swap/res/audio/...`

Usalos para:
- registrar sonidos
- cambiar efectos
- ajustar precarga y reproduccion

## Si queres cambiar algo puntual

### "Quiero que el slime tarde mas en reaparecer"

- `Swap/src/content/WorldSeeder.java`

### "Quiero que el orco aparezca en otra zona"

- `Swap/src/content/WorldSeeder.java`
- `Swap/res/map/worldV2.txt`

### "Quiero cambiar stats del slime"

- `Swap/res/content/enemies/green_slime.json`

### "Quiero cambiar el sprite del orco"

- `Swap/res/enemy/...`
- `Swap/src/content/AssetBootstrap.java`

### "Quiero cambiar la iluminacion o la logica dia/noche"

- `Swap/src/system/DayNightSystem.java`
- `Swap/src/system/TimeSystem.java`

### "Quiero cambiar las opciones del menu principal"

- `Swap/src/scene/TitleScene.java`

### "Quiero cambiar el menu F10"

- `Swap/src/scene/WorldScene.java`
- `Swap/src/ui/HudRenderer.java`
- `Swap/src/ui/UiText.java`

### "Quiero cambiar que guarda una partida"

- `Swap/src/system/SaveLoadSystem.java`

## Que tan dificil es cambiar cosas

- Facil: cambiar numeros, textos, tiempos, areas de spawn, opciones de menu.
- Medio: cambiar reglas de systems concretos como respawn, combate o saves.
- Mas dificil: pasar algo hardcodeado a totalmente editable por archivo o por mapa.

Hoy el proyecto ya esta bastante mejor organizado para cambiar cosas sin romper todo, pero todavia hay partes de mundo y gameplay que siguen centralizadas en Java.
