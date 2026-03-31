package data;

import data.loader.JsonContentMapper;
import data.loader.JsonResourceLoader;
import data.loader.JsonValueReader;
import data.loader.MiniJsonParser;
import data.quest.QuestCatalogData;
import data.world.EconomyData;
import data.world.WorldLayoutData;
import data.world.WorldObjectCatalogData;
import data.world.WorldPhaseData;
import java.util.List;
import java.util.Map;

/**
 * Stable facade for JSON-backed gameplay data loading.
 *
 * This class stays intentionally small: resource loading, tree parsing and content
 * mapping now live in dedicated helpers under `data.loader`.
 */
public final class JsonDataLoader {
    private final JsonResourceLoader resourceLoader = new JsonResourceLoader();
    private final JsonContentMapper mapper = new JsonContentMapper();

    public static Map<String, Object> parseObjectText(String json) {
        return JsonValueReader.object(MiniJsonParser.parse(json));
    }

    public static Map<String, Object> objectValue(Map<String, Object> value, String key) {
        return JsonValueReader.object(JsonValueReader.object(value, key));
    }

    public PlayerData loadPlayer(String id, String resourcePath) {
        return mapper.toPlayer(id, resourceLoader.load(resourcePath));
    }

    public EnemyData loadEnemy(String id, String resourcePath) {
        return mapper.toEnemy(id, resourceLoader.load(resourcePath));
    }

    public NpcData loadNpc(String id, String resourcePath) {
        return mapper.toNpc(id, resourceLoader.load(resourcePath));
    }

    public WorldLayoutData loadWorldLayout(String resourcePath) {
        return mapper.toWorldLayout(resourceLoader.load(resourcePath));
    }

    public String loadWorldMapResource(String resourcePath) {
        return mapper.toWorldMapResource(resourceLoader.load(resourcePath));
    }

    public List<WorldLayoutData.NpcSpawnData> loadNpcSpawns(String resourcePath) {
        return mapper.toNpcSpawns(resourceLoader.load(resourcePath));
    }

    public List<WorldLayoutData.EnemySpawnerData> loadEnemySpawners(String resourcePath) {
        return mapper.toEnemySpawners(resourceLoader.load(resourcePath));
    }

    public List<WorldLayoutData.WorldObjectSpawnData> loadWorldObjectSpawns(String resourcePath) {
        return mapper.toWorldObjectSpawns(resourceLoader.load(resourcePath));
    }

    public WorldPhaseData loadWorldPhase(String resourcePath) {
        return mapper.toWorldPhase(resourceLoader.load(resourcePath));
    }

    public QuestCatalogData loadQuestCatalog(String resourcePath) {
        return mapper.toQuestCatalog(resourceLoader.load(resourcePath));
    }

    public WorldObjectCatalogData loadWorldObjectCatalog(String resourcePath) {
        return mapper.toWorldObjectCatalog(resourceLoader.load(resourcePath));
    }

    public EconomyData loadEconomy(String resourcePath) {
        return mapper.toEconomy(resourceLoader.load(resourcePath));
    }
}
