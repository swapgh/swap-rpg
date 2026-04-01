package content.world;

import asset.AssetManager;
import asset.MapLoader;
import asset.TileDefinition;
import asset.TileMap;
import component.world.RespawnAreaComponent;
import component.world.RespawnSpawnerComponent;
import component.world.WorldTimeComponent;
import component.world.WorldPlacementComponent;
import content.catalog.TileCatalog;
import content.prefab.PrefabFactory;
import data.DataRegistry;
import data.world.WorldLayoutData;
import ecs.EcsWorld;
import java.time.Instant;
import java.time.LocalTime;

/** Construye el estado inicial del mundo jugable. */
public final class WorldSeeder {
    private WorldSeeder() {
    }

    /** Crea el TileMap runtime a partir del catalogo de tiles y del archivo de mapa. */
    public static TileMap createMap(AssetManager assets, int tileSize, DataRegistry data) {
        TileDefinition[] definitions = TileCatalog.register(assets, tileSize);
        return new MapLoader().load(data.worldLayout().mapResource(), tileSize, definitions);
    }

    /** Coloca el jugador usando la data de spawn definida en contenido externo. */
    public static int seedPlayer(EcsWorld world, int tileSize, DataRegistry data) {
        return PrefabFactory.createPlayer(world, data.player("hero"), data, tileSize);
    }

    public static void seedWorldTime(EcsWorld world) {
        LocalTime now = LocalTime.now();
        long totalSeconds = now.toSecondOfDay();
        int entity = world.createEntity();
        world.add(entity, new WorldTimeComponent(totalSeconds, Instant.now().getEpochSecond()));
    }

    /** Poblacion inicial del mundo. */
    public static void seedWorld(EcsWorld world, int tileSize, DataRegistry data) {
        WorldLayoutData layout = data.worldLayout();

        for (WorldLayoutData.NpcSpawnData npc : layout.npcs()) {
            int entity = PrefabFactory.createNpc(world, data.npc(npc.npcId()), tileSize * npc.tileX(), tileSize * npc.tileY(),
                    tileSize);
            world.add(entity, new WorldPlacementComponent(npc.placementId()));
        }

        for (WorldLayoutData.EnemySpawnerData spawner : layout.enemySpawners()) {
            createEnemySpawner(
                    world,
                    spawner.placementId(),
                    spawner.prefabId(),
                    spawner.minTileX(),
                    spawner.minTileY(),
                    spawner.maxTileX(),
                    spawner.maxTileY(),
                    spawner.maxAlive(),
                    spawner.respawnDelayTicks(),
                    spawner.retryDelayTicks(),
                    spawner.maxSpawnAttempts(),
                    spawner.minPlayerDistanceTiles());
        }

        for (WorldLayoutData.WorldObjectSpawnData object : layout.objects()) {
            createWorldObject(world, data, object, tileSize);
        }
    }

    private static void createWorldObject(EcsWorld world, DataRegistry data, WorldLayoutData.WorldObjectSpawnData object,
            int tileSize) {
        int worldX = tileSize * object.tileX();
        int worldY = tileSize * object.tileY();
        int entity = PrefabFactory.createWorldObject(world, data.worldObject(object.objectId()), worldX, worldY, tileSize);
        world.add(entity, new WorldPlacementComponent(object.placementId()));
    }

    private static void createEnemySpawner(EcsWorld world, String placementId, String prefabId, int minTileX, int minTileY, int maxTileX,
            int maxTileY, int maxAlive, int respawnDelayTicks, int retryDelayTicks, int maxSpawnAttempts,
            int minPlayerDistanceTiles) {
        int entity = world.createEntity();
        world.add(entity, new WorldPlacementComponent(placementId));
        world.add(entity, new RespawnAreaComponent(minTileX, minTileY, maxTileX, maxTileY));
        world.add(entity, new RespawnSpawnerComponent(prefabId, maxAlive, respawnDelayTicks, retryDelayTicks,
                maxSpawnAttempts, minPlayerDistanceTiles));
    }
}
