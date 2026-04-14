# Swap RPG ECS

Este documento explica la arquitectura real del proyecto para que alguien nuevo pueda ubicar rápido cada pieza.

## Flujo de arranque

`Main -> GamePanel -> SceneComposer -> SceneManager -> Scene -> Systems -> EcsWorld`

La idea es simple:

- `Main` crea la ventana.
- `GamePanel` arranca servicios base y el bucle de juego.
- `SceneComposer` compone escenas con dependencias compartidas.
- `SceneManager` cambia entre escenas.
- Cada escena usa systems sobre un `EcsWorld`.

## Qué es ECS aquí

El núcleo ECS está bien separado:

- `ecs.EcsWorld` guarda entidades y componentes.
- `component.*` contiene datos puros.
- `system.*` contiene la lógica que lee esos datos y los transforma.

Eso significa que el comportamiento no vive dentro de los componentes.

## Qué es data-driven aquí

La parte data-driven vive en:

- `Swap/res/content/...`
- `Swap/res/maps/...`
- `Swap/src/data/...`
- `Swap/src/content/...`

El contenido externo define:

- clases
- NPCs
- enemigos
- economía
- quests
- mapas
- assets registrables

La lógica Java lee ese contenido y lo convierte en entidades o configuraciones runtime.

## Capas importantes

### App

`Swap/src/app/bootstrap`, `Swap/src/app/input`, `Swap/src/app/camera`, `Swap/src/app/dialog` y `Swap/src/app/prefs` contienen el arranque y utilidades de pegamento:

- `Main`
- `GamePanel`
- `SceneComposer`
- `GameConfig`
- `KeyboardState`
- `Camera`
- diálogos de cuenta y guardado
- preferencias locales

### Scene

`Swap/src/scene` organiza pantallas y flujo:

- `scene.menu` para título, login y game over
- `scene.gameplay` para el mundo
- `scene.gameplay.control` para controladores de sesión y opciones
- `scene.gameplay.runtime` para utilidades runtime del mundo
- `scene.gameplay.world` para layout y sync del mundo

### Save

`Swap/src/save` maneja persistencia:

- guardado local
- metadata de slots
- roster sync
- selección de último save

### Online

`Swap/src/online` maneja:

- login con la web
- sincronización de progreso
- perfiles remotos
- tokens y estado de sesión

### Asset, content y data

`Swap/src/asset`, `Swap/src/content` y `Swap/src/data` se ocupan de:

- cargar imágenes y clips
- crear prefabs
- cargar mapas
- registrar recursos base
- cargar JSON y convertirlo a modelos runtime

## Clases que merecen atención especial

- `WorldScene`: coordina mapa, systems, UI y persistencia.
- `SaveManager`: es la fachada de guardado local.
- `InteractionSystem`: sigue siendo la puerta de entrada a interacciones de juego, aunque ahora delega mejor.
- `SceneComposer`: compone escenas y dependencias compartidas.
- `WorldStartLayout`: decide mapa y spawn inicial según clase.
- `WorldProgressSyncController`: coordina sync del progreso con la cuenta online.

## Qué respeta bien la arquitectura

- El núcleo ECS está separado.
- Los datos viven fuera de la lógica.
- Los systems no deberían depender de sprites o archivos directamente.
- Los loaders son responsables de leer formatos, no de jugar.

## Qué no es ECS puro

No es un ECS puro al 100%.

La orquestación todavía vive en varias capas:

- escenas
- save manager
- login y sync online
- controladores auxiliares

Eso no es malo. Solo significa que el proyecto es un híbrido práctico, no una demostración académica de ECS.

## Cómo extender el proyecto

Si añades una entidad nueva:

1. Define sus datos en `component`.
2. Registra o carga su contenido en `content` o `data`.
3. Crea un prefab en `content`.
4. Añade o amplía un `system` si necesita reglas nuevas.
5. Evita meter lógica en la entidad o en el componente.

Si cambias una regla global:

1. Busca si es mundo, save, UI o online.
2. Cambia primero el controlador de la capa adecuada.
3. No metas esa regla directamente en `Main` o `GamePanel` salvo que sea bootstrap puro.

## Resumen corto

El proyecto ya tiene una base ECS y data-driven buena.
La mayor complejidad no está en los componentes ni en los systems, sino en la orquestación de escenas, save y online.

Si quieres entender un cambio concreto, mira `CHANGE_GUIDE.md`.
