package content;

import asset.AssetManager;
import asset.MapLoader;
import asset.TileDefinition;
import asset.TileMap;
import component.RespawnAreaComponent;
import component.RespawnSpawnerComponent;
import component.WorldTimeComponent;
import data.DataRegistry;
import ecs.EcsWorld;
import java.time.Instant;
import java.time.LocalTime;

/** Construye el estado inicial del mundo jugable. */
public final class WorldSeeder {
    private WorldSeeder() {
    }

    /** Crea el TileMap runtime a partir del catalogo de tiles y del archivo de mapa. */
    public static TileMap createMap(AssetManager assets, int tileSize) {
        TileDefinition[] definitions = TileCatalog.register(assets, tileSize);
        return new MapLoader().load("/map/worldV2.txt", tileSize, definitions);
    }

    /** Coloca el jugador usando la data de spawn definida en contenido externo. */
    public static int seedPlayer(EcsWorld world, int tileSize, DataRegistry data) {
        return PrefabFactory.createPlayer(world, data.player("hero"), tileSize);
    }

    public static void seedWorldTime(EcsWorld world) {
        LocalTime now = LocalTime.now();
        long totalSeconds = now.toSecondOfDay();
        int entity = world.createEntity();
        world.add(entity, new WorldTimeComponent(totalSeconds, Instant.now().getEpochSecond()));
    }

    /** Poblacion inicial del mundo. */
    public static void seedWorld(EcsWorld world, int tileSize, DataRegistry data) {
        PrefabFactory.createNpc(world, data.npc("old_man"), tileSize * 21, tileSize * 22, tileSize);
        PrefabFactory.createNpc(world, data.npc("merchant"), tileSize * 24, tileSize * 20, tileSize);

        createEnemySpawner(world, "green_slime", 21, 33, 25, 37, 1, 180, 20, 12, 6);
        createEnemySpawner(world, "green_slime", 24, 10, 28, 14, 1, 180, 20, 12, 6);
        createEnemySpawner(world, "orc_pyromancer", 26, 18, 30, 22, 1, 300, 30, 16, 8);

        PrefabFactory.createCoin(world, tileSize * 25, tileSize * 21, tileSize);
        PrefabFactory.createKey(world, tileSize * 18, tileSize * 20, tileSize);
        PrefabFactory.createDoor(world, tileSize * 10, tileSize * 12, tileSize);
        PrefabFactory.createChest(world, tileSize * 10, tileSize * 9, tileSize);
    }

    private static void createEnemySpawner(EcsWorld world, String prefabId, int minTileX, int minTileY, int maxTileX,
            int maxTileY, int maxAlive, int respawnDelayTicks, int retryDelayTicks, int maxSpawnAttempts,
            int minPlayerDistanceTiles) {
        int entity = world.createEntity();
        world.add(entity, new RespawnAreaComponent(minTileX, minTileY, maxTileX, maxTileY));
        world.add(entity, new RespawnSpawnerComponent(prefabId, maxAlive, respawnDelayTicks, retryDelayTicks,
                maxSpawnAttempts, minPlayerDistanceTiles));
    }
}
