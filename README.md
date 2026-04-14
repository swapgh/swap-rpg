# Swap RPG

Swap RPG es un juego 2D en Java con una base híbrida: ECS para la lógica viva, contenido data-driven para datos editables y escenas para la navegación general.

## Mapa rápido

`Main -> GamePanel -> SceneComposer -> Scene -> Systems -> EcsWorld`

Si quieres leer el proyecto con calma, empieza por ese flujo.

## Estructura principal

- `Swap/src/app/bootstrap`: arranque, ventana, composición y config.
- `Swap/src/app/input`: teclado.
- `Swap/src/app/camera`: cámara 2D.
- `Swap/src/app/dialog`: diálogos de login y guardado.
- `Swap/src/app/prefs`: preferencias locales de UI.
- `Swap/src/scene`: escenas de menú y gameplay.
- `Swap/src/scene/gameplay/control`: controladores de mundo.
- `Swap/src/scene/gameplay/runtime`: helpers de runtime del mundo.
- `Swap/src/scene/gameplay/world`: layout y sync del mundo.
- `Swap/src/ecs`: núcleo ECS.
- `Swap/src/component`: componentes puros.
- `Swap/src/system`: reglas del juego.
- `Swap/src/data`: modelos y loaders de JSON.
- `Swap/src/content`: prefabs, catálogos y bootstrap de contenido.
- `Swap/src/asset`: mapas, tiles y recursos.
- `Swap/src/save`: guardado local y roster sync.
- `Swap/src/online`: autenticación, sesión y sincronización con la web.
- `Swap/src/ui`: HUD, estado visual y textos.
- `Swap/res`: mapas, sprites, audio y JSON.

## Ejecutar

Necesitas Java 17 o superior.

Desde terminal:

```bash
find Swap/src -name '*.java' | sort > /tmp/swap-rpg-sources.txt
javac -d /tmp/swap-rpg-build_classes @/tmp/swap-rpg-sources.txt
java -cp /tmp/swap-rpg-build_classes:Swap/res app.bootstrap.Main
```

Desde VS Code:

- abre la carpeta `swap-rpg`
- usa `Run and Debug`
- ejecuta `Run Swap RPG`
 - la compilación se hace en `/tmp/swap-rpg-build_classes`, así no se ensucia el explorador del proyecto

## Qué tocar para cambiar cosas

- Login y cuenta: `Swap/src/app/dialog/AccountDialogs.java`, `Swap/src/scene/menu/LoginScene.java`
- Menú principal: `Swap/src/scene/menu/TitleScene.java`
- Mundo principal: `Swap/src/scene/gameplay/WorldScene.java`
- Spawn, mapa inicial y layout: `Swap/src/scene/gameplay/world/WorldStartLayout.java`, `Swap/src/app/bootstrap/GameConfig.java`
- Sync online del progreso: `Swap/src/scene/gameplay/world/WorldProgressSyncController.java`
- Guardado manual/autosave: `Swap/src/save/SaveManager.java`, `Swap/src/save/metadata/SaveMetadataFactory.java`, `Swap/src/save/roster/SaveRosterSyncService.java`
- Interacciones: `Swap/src/system/interaction/*`
- Inventario: `Swap/src/system/inventory/*`
- Combate: `Swap/src/system/combat/*`
- HUD y overlays: `Swap/src/ui/hud/*`
- Progresión de clases: `Swap/res/content/progression/classes/*.json`, `Swap/src/progression/ProgressionCalculator.java`
- Mapas: `Swap/res/maps/world/worldV2.txt`, `Swap/res/maps/world/tiled/map.tmx`, `Swap/src/asset/MapLoader.java`, `Swap/src/asset/TmxMapLoader.java`

## Documentación útil

- Arquitectura ECS y flujo general: [Swap/ECS_PROJECT.md](Swap/ECS_PROJECT.md)
- Progresión y balance de clases: [Swap/RPG_PROGRESSION.md](Swap/RPG_PROGRESSION.md)
- Guía rápida de cambios: [CHANGE_GUIDE.md](CHANGE_GUIDE.md)

## Estado actual

- jugador con movimiento, combate, inventario y HUD
- slimes y enemigo a distancia
- NPCs con diálogo
- llaves, puertas, cofres y monedas
- guardado local con autosave y manual saves
- sincronización con cuenta web para progreso y roster

## Convenciones

- `component` guarda datos puros.
- `system` guarda reglas y comportamiento.
- `content` convierte data en entidades.
- `scene` coordina pantallas.
- `asset` maneja mapas y recursos.
- `save` persiste estado.
- `online` habla con la web.
- `ui` dibuja y organiza overlays visuales.

Si no sabes por dónde empezar, abre `CHANGE_GUIDE.md`.
