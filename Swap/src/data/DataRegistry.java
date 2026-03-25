package data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * In-memory catalog of external gameplay data.
 *
 * The registry hides file paths from the rest of the game. World seeding and prefab
 * creation only need stable ids such as `hero` or `green_slime`.
 */
public final class DataRegistry {
    private final Map<String, PlayerData> players = new LinkedHashMap<>();
    private final Map<String, EnemyData> enemies = new LinkedHashMap<>();
    private final Map<String, NpcData> npcs = new LinkedHashMap<>();

    /**
     * Loads the built-in content bundle shipped with the project.
     *
     * The list is explicit for now because it keeps startup deterministic while the
     * content pipeline is still small.
     */
    public static DataRegistry loadDefaults() {
        JsonDataLoader loader = new JsonDataLoader();
        DataRegistry registry = new DataRegistry();
        registry.players.put("hero", loader.loadPlayer("hero", "/content/players/hero.json"));
        registry.enemies.put("green_slime", loader.loadEnemy("green_slime", "/content/enemies/green_slime.json"));
        registry.enemies.put("orc_pyromancer", loader.loadEnemy("orc_pyromancer", "/content/enemies/orc_pyromancer.json"));
        registry.npcs.put("old_man", loader.loadNpc("old_man", "/content/npcs/old_man.json"));
        registry.npcs.put("merchant", loader.loadNpc("merchant", "/content/npcs/merchant.json"));
        return registry;
    }

    public PlayerData player(String id) {
        PlayerData data = players.get(id);
        if (data == null) {
            throw new IllegalArgumentException("Missing player data: " + id);
        }
        return data;
    }

    public EnemyData enemy(String id) {
        EnemyData data = enemies.get(id);
        if (data == null) {
            throw new IllegalArgumentException("Missing enemy data: " + id);
        }
        return data;
    }

    public NpcData npc(String id) {
        NpcData data = npcs.get(id);
        if (data == null) {
            throw new IllegalArgumentException("Missing npc data: " + id);
        }
        return data;
    }
}
