package data;

import data.quest.QuestCatalogData;
import data.quest.QuestData;
import data.progression.ProgressionRulesData;
import data.progression.RpgClassData;
import data.world.EconomyData;
import data.world.WorldLayoutData;
import data.world.WorldObjectCatalogData;
import data.world.WorldObjectData;
import data.world.WorldPhaseData;
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
    private final Map<String, RpgClassData> rpgClasses = new LinkedHashMap<>();
    private final Map<String, EnemyData> enemies = new LinkedHashMap<>();
    private final Map<String, NpcData> npcs = new LinkedHashMap<>();
    private EconomyData economy;
    private ProgressionRulesData progressionRules;
    private QuestCatalogData questCatalog;
    private WorldObjectCatalogData worldObjectCatalog;
    private WorldLayoutData worldLayout;
    private WorldPhaseData worldPhase;

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
        registry.rpgClasses.put("warrior", loader.loadRpgClass("warrior", "/content/progression/classes/warrior.json"));
        registry.rpgClasses.put("mage", loader.loadRpgClass("mage", "/content/progression/classes/mage.json"));
        registry.rpgClasses.put("druid", loader.loadRpgClass("druid", "/content/progression/classes/druid.json"));
        registry.enemies.put("green_slime", loader.loadEnemy("green_slime", "/content/enemies/slime/green.json"));
        registry.enemies.put("orc_pyromancer", loader.loadEnemy("orc_pyromancer", "/content/enemies/orc/pyromancer.json"));
        registry.npcs.put("old_man", loader.loadNpc("old_man", "/content/npcs/old_man.json"));
        registry.npcs.put("merchant", loader.loadNpc("merchant", "/content/npcs/merchant.json"));
        registry.economy = loader.loadEconomy("/content/world/rules/economy.json");
        registry.progressionRules = loader.loadProgressionRules("/content/progression/rules/core.json");
        registry.questCatalog = loader.loadQuestCatalog("/content/quests/catalog.json");
        registry.worldObjectCatalog = loader.loadWorldObjectCatalog("/content/world/catalogs/objects.json");
        registry.worldLayout = new WorldLayoutData(
                loader.loadWorldMapResource("/content/world/map/map.json"),
                loader.loadNpcSpawns("/content/world/placements/npcs.json"),
                loader.loadEnemySpawners("/content/world/placements/enemy_spawners.json"),
                loader.loadWorldObjectSpawns("/content/world/placements/objects.json"));
        registry.worldPhase = loader.loadWorldPhase("/content/world/rules/day_night.json");
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

    public RpgClassData rpgClass(String id) {
        RpgClassData data = rpgClasses.get(id);
        if (data == null) {
            throw new IllegalArgumentException("Missing RPG class data: " + id);
        }
        return data;
    }

    public ProgressionRulesData progressionRules() {
        if (progressionRules == null) {
            throw new IllegalStateException("Missing progression rules data");
        }
        return progressionRules;
    }

    public NpcData npc(String id) {
        NpcData data = npcs.get(id);
        if (data == null) {
            throw new IllegalArgumentException("Missing npc data: " + id);
        }
        return data;
    }

    public WorldLayoutData worldLayout() {
        if (worldLayout == null) {
            throw new IllegalStateException("Missing world layout data");
        }
        return worldLayout;
    }

    public WorldPhaseData worldPhase() {
        if (worldPhase == null) {
            throw new IllegalStateException("Missing world phase data");
        }
        return worldPhase;
    }

    public QuestCatalogData questCatalog() {
        if (questCatalog == null) {
            throw new IllegalStateException("Missing quest catalog data");
        }
        return questCatalog;
    }

    public QuestData quest(String id) {
        return questCatalog().quest(id);
    }

    public WorldObjectCatalogData worldObjectCatalog() {
        if (worldObjectCatalog == null) {
            throw new IllegalStateException("Missing world object catalog data");
        }
        return worldObjectCatalog;
    }

    public WorldObjectData worldObject(String id) {
        return worldObjectCatalog().object(id);
    }

    public EconomyData economy() {
        if (economy == null) {
            throw new IllegalStateException("Missing economy data");
        }
        return economy;
    }
}
