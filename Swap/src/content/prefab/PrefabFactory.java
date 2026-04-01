package content.prefab;

import data.DataRegistry;
import data.EnemyData;
import data.NpcData;
import data.PlayerData;
import data.world.WorldObjectData;
import ecs.EcsWorld;

/** Traduce data externa a entidades ECS listas para usarse. */
public final class PrefabFactory {
    private PrefabFactory() {
    }

    /** Crea el jugador a partir de PlayerData. */
    public static int createPlayer(EcsWorld world, PlayerData data, DataRegistry registry, int tileSize) {
        return PlayerPrefabBuilder.create(world, data, registry, tileSize);
    }

    /** Crea un enemigo a partir de EnemyData. */
    public static int createEnemy(EcsWorld world, EnemyData data, int x, int y, int tileSize) {
        return EnemyPrefabBuilder.create(world, data, x, y, tileSize);
    }

    /** Crea un NPC a partir de NpcData. */
    public static int createNpc(EcsWorld world, NpcData data, int x, int y, int tileSize) {
        return NpcPrefabBuilder.create(world, data, x, y, tileSize);
    }

    public static int createWorldObject(EcsWorld world, WorldObjectData data, int x, int y, int tileSize) {
        return WorldObjectPrefabBuilder.create(world, data, x, y, tileSize);
    }
}
