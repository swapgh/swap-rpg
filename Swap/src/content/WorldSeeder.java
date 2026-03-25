package content;

import asset.AssetManager;
import asset.MapLoader;
import asset.TileDefinition;
import asset.TileMap;
import data.DataRegistry;
import ecs.EcsWorld;

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

    /** Poblacion inicial del mundo. */
    public static void seedWorld(EcsWorld world, int tileSize, DataRegistry data) {
        PrefabFactory.createNpc(world, data.npc("old_man"), tileSize * 21, tileSize * 22, tileSize);
        PrefabFactory.createNpc(world, data.npc("merchant"), tileSize * 24, tileSize * 20, tileSize);

        PrefabFactory.createEnemy(world, data.enemy("green_slime"), tileSize * 23, tileSize * 35, tileSize);
        PrefabFactory.createEnemy(world, data.enemy("green_slime"), tileSize * 26, tileSize * 12, tileSize);

        PrefabFactory.createEnemy(world, data.enemy("orc_pyromancer"), tileSize * 28, tileSize * 18, tileSize);

        PrefabFactory.createCoin(world, tileSize * 25, tileSize * 21, tileSize);
        PrefabFactory.createKey(world, tileSize * 18, tileSize * 18, tileSize);
        PrefabFactory.createDoor(world, tileSize * 10, tileSize * 12, tileSize);
        PrefabFactory.createChest(world, tileSize * 10, tileSize * 9, tileSize);
    }
}
