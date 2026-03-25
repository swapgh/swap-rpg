# Swap RPG ECS

Punto de entrada:
- app.Main

Compilacion y ejecucion manual:
- cd swap-rpg
- find Swap/src -name '*.java' | sort > build_sources.txt
- javac -d build_classes @build_sources.txt
- java -cp build_classes:Swap/res app.Main

Arquitectura:
- Entity: un int creado por ecs.EcsWorld
- Components: datos puros en component
- Systems: logica en system
- Assets: asset.AssetManager carga imagenes, fuentes y clips una sola vez
- Content: content registra tiles, sprites, prefabs y mapa
- Scenes: TitleScene y WorldScene
- Modos: TITLE, PLAY, DIALOGUE, INVENTORY

Systems principales:
- InputSystem
- MovementSystem
- InteractionSystem
- CombatSystem
- HealthSystem
- AnimationSystem
- CameraSystem
- RenderSystem
- InventorySystem
- QuestSystem
- SaveLoadSystem

Entidades ejemplo:
- Jugador = Position + Velocity + Sprite + Animation + Collider + Health + Stats + Attack + Input + Inventory + Quest + CameraTarget
- Slime = Position + Velocity + Sprite + Animation + Collider + Health + Stats + Enemy + WanderAi + Solid
- NPC = Position + Velocity + Sprite + Animation + Collider + Dialogue + Npc + Solid
- Llave = Position + Sprite + Collider + Collectible
- Puerta = Position + Sprite + Collider + Door + Solid
- Cofre = Position + Sprite + Collider + Collectible + Solid

Como extender:
1. Añade sprites en content.AssetBootstrap
2. Registra clips si la entidad tiene animacion
3. Crea un prefab nuevo en content.PrefabFactory
4. Combina componentes, no subclases gigantes
5. Si necesita reglas nuevas, crea un system nuevo o amplia uno existente con responsabilidad clara

Ejemplo conceptual:
Una gallina inmortal que tira fuego se modelaria con Position, Velocity, Sprite, Animation, Collider, Enemy, Stats, un AI de rango, ProjectileEmitter e Immortal. Luego AnimationSystem la anima, MovementSystem la mueve, un ProjectileSystem nuevo la hace disparar y HealthSystem ignora su muerte si existe Immortal.

Estado actual del proyecto:
- mapa real cargado desde worldV2.txt
- jugador con movimiento, ataque y HUD
- slime con AI simple
- NPC con dialogo
- llave, puerta, cofre y monedas
- inventario y quests base
- save/load simple

Pendiente para siguientes iteraciones:

- mas escenas enlazadas
- quests complejas
- enemigos y NPCs data-driven
