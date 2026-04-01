package data.loader;

import static data.loader.JsonValueReader.array;
import static data.loader.JsonValueReader.bool;
import static data.loader.JsonValueReader.integer;
import static data.loader.JsonValueReader.number;
import static data.loader.JsonValueReader.object;
import static data.loader.JsonValueReader.optionalArray;
import static data.loader.JsonValueReader.optionalObject;
import static data.loader.JsonValueReader.optionalStringArray;
import static data.loader.JsonValueReader.string;
import static data.loader.JsonValueReader.stringArray;

import data.AttackData;
import data.ColliderData;
import data.EnemyData;
import data.FlagsData;
import data.LootData;
import data.NpcData;
import data.PlayerData;
import data.ProjectileData;
import data.SpawnData;
import data.StatsData;
import data.VisualData;
import data.quest.QuestCatalogData;
import data.quest.QuestData;
import data.progression.AttributesData;
import data.progression.ProgressionRulesData;
import data.progression.RpgClassData;
import data.shop.ShopData;
import data.world.EconomyData;
import data.world.WorldLayoutData;
import data.world.WorldObjectCatalogData;
import data.world.WorldObjectData;
import data.world.WorldPhaseData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonContentMapper {
    public PlayerData toPlayer(String id, Object json) {
        Map<String, Object> root = object(json);
        return new PlayerData(
                id,
                string(root, "name"),
                string(root, "classId", "warrior"),
                string(root, "faction", "player"),
                integer(root, "startingLevel", 1),
                spawn(object(root, "spawn")),
                visual(object(root, "visual")),
                collider(object(root, "collider")),
                stats(object(root, "stats")),
                attack(object(root, "attack")),
                projectile(object(root, "projectile")),
                flags(object(root, "flags")));
    }

    public EnemyData toEnemy(String id, Object json) {
        Map<String, Object> root = object(json);
        return new EnemyData(
                id,
                string(root, "name"),
                string(root, "faction", "enemy"),
                visual(object(root, "visual")),
                collider(object(root, "collider")),
                stats(object(root, "stats")),
                projectile(object(root, "projectile")),
                loot(optionalObject(root, "loot")),
                flags(object(root, "flags")),
                bool(root, "wander"));
    }

    public NpcData toNpc(String id, Object json) {
        Map<String, Object> root = object(json);
        Map<String, Object> scheduledDialogue = optionalObject(root, "scheduledDialogue");
        Map<String, Object> shop = optionalObject(root, "shop");
        return new NpcData(
                id,
                string(root, "nameKey"),
                string(root, "faction", "npc"),
                visual(object(root, "visual")),
                collider(object(root, "collider")),
                flags(object(root, "flags")),
                stringArray(root, "dialogueKeys"),
                optionalStringArray(scheduledDialogue, "dayKeys"),
                optionalStringArray(scheduledDialogue, "nightKeys"),
                shopData(shop));
    }

    public WorldLayoutData toWorldLayout(Object json) {
        Map<String, Object> root = object(json);
        return new WorldLayoutData(
                string(root, "mapResource"),
                npcSpawns(array(root, "npcs")),
                enemySpawners(array(root, "enemySpawners")),
                worldObjects(array(root, "objects")));
    }

    public String toWorldMapResource(Object json) {
        return string(object(json), "mapResource");
    }

    public List<WorldLayoutData.NpcSpawnData> toNpcSpawns(Object json) {
        return npcSpawns(array(object(json), "npcs"));
    }

    public List<WorldLayoutData.EnemySpawnerData> toEnemySpawners(Object json) {
        return enemySpawners(array(object(json), "enemySpawners"));
    }

    public List<WorldLayoutData.WorldObjectSpawnData> toWorldObjectSpawns(Object json) {
        return worldObjects(array(object(json), "objects"));
    }

    public WorldPhaseData toWorldPhase(Object json) {
        Map<String, Object> root = object(json);
        Map<String, Object> visitQuests = object(root, "visitQuests");
        Map<String, Object> slimePhase = object(root, "slimePhase");
        return new WorldPhaseData(
                string(visitQuests, "day"),
                string(visitQuests, "night"),
                slimePhaseRule(object(slimePhase, "day")),
                slimePhaseRule(object(slimePhase, "night")));
    }

    public QuestCatalogData toQuestCatalog(Object json) {
        Map<String, Object> root = object(json);
        Map<String, Object> hooks = object(root, "hooks");
        Map<String, QuestData> quests = new LinkedHashMap<>();
        for (Object value : array(root, "quests")) {
            Map<String, Object> entry = object(value);
            QuestData quest = new QuestData(
                    string(entry, "id"),
                    string(entry, "completionToastKey"),
                    string(entry, "activeHintKey", ""));
            quests.put(quest.id(), quest);
        }
        return new QuestCatalogData(
                Map.copyOf(quests),
                string(hooks, "firstCoin"),
                string(hooks, "firstKill"));
    }

    public WorldObjectCatalogData toWorldObjectCatalog(Object json) {
        Map<String, Object> root = object(json);
        Map<String, WorldObjectData> objects = new LinkedHashMap<>();
        for (Object value : array(root, "objects")) {
            Map<String, Object> entry = object(value);
            WorldObjectData data = new WorldObjectData(
                    string(entry, "id"),
                    string(entry, "nameKey"),
                    string(entry, "spriteId"),
                    integer(entry, "layer"),
                    collider(object(entry, "collider")),
                    bool(entry, "solid"),
                    collectibleDrop(entry.get("collectible")),
                    doorRule(entry.get("door")),
                    chestRule(entry.get("chest")),
                    interactionRule(entry.get("interaction")));
            objects.put(data.id(), data);
        }
        return new WorldObjectCatalogData(Map.copyOf(objects));
    }

    public EconomyData toEconomy(Object json) {
        Map<String, Object> root = object(json);
        return new EconomyData(
                economyPhase(object(root, "day")),
                economyPhase(object(root, "night")));
    }

    public RpgClassData toRpgClass(String id, Object json) {
        Map<String, Object> root = object(json);
        return new RpgClassData(
                id,
                string(root, "role"),
                attributes(object(root, "baseAttributes")),
                attributes(object(root, "growthPerLevel")),
                integer(root, "baseHp"),
                integer(root, "baseMana"),
                number(root, "baseSpeed"),
                integer(root, "startingWeaponPower"),
                integer(root, "startingArmor"),
                number(root, "strAttackScale", 0.0),
                number(root, "agiAttackScale", 0.0),
                number(root, "intAttackScale", 0.0),
                number(root, "spiAttackScale", 0.0));
    }

    public ProgressionRulesData toProgressionRules(Object json) {
        Map<String, Object> root = object(json);
        return new ProgressionRulesData(
                integer(root, "hpPerSta"),
                integer(root, "hpPerSpi"),
                integer(root, "manaPerInt"),
                integer(root, "manaPerSpi"),
                number(root, "apPerStr"),
                number(root, "apPerAgi"),
                number(root, "attackSpeedBase"),
                number(root, "attackSpeedPerAgi"),
                number(root, "abilityPowerPerInt"),
                number(root, "abilityPowerPerSpi"),
                number(root, "defensePerSta"),
                number(root, "defensePerStr"),
                number(root, "healingPowerPerSpi"),
                number(root, "healingPowerPerInt"));
    }

    private SpawnData spawn(Map<String, Object> value) {
        return new SpawnData(integer(value, "tileX"), integer(value, "tileY"));
    }

    private VisualData visual(Map<String, Object> value) {
        return new VisualData(
                string(value, "idleBase"),
                string(value, "walkBase"),
                string(value, "attackBase", ""),
                string(value, "initialFacing", "down"),
                integer(value, "initialFrame", 1),
                integer(value, "layer"),
                integer(value, "animationFrameTicks"));
    }

    private ColliderData collider(Map<String, Object> value) {
        return new ColliderData(
                integer(value, "offsetX"),
                integer(value, "offsetY"),
                integer(value, "width"),
                integer(value, "height"));
    }

    private StatsData stats(Map<String, Object> value) {
        return new StatsData(
                integer(value, "health"),
                integer(value, "speed"),
                integer(value, "attack"),
                integer(value, "defense"));
    }

    private AttackData attack(Map<String, Object> value) {
        return new AttackData(
                integer(value, "damage"),
                number(value, "rangeScale"),
                integer(value, "cooldownTicks"));
    }

    private ProjectileData projectile(Map<String, Object> value) {
        return new ProjectileData(
                bool(value, "enabled"),
                string(value, "spriteId", ""),
                integer(value, "speed"),
                integer(value, "damage"),
                integer(value, "lifetimeTicks"),
                integer(value, "cooldownTicks"),
                number(value, "sizeScale", 0.5),
                string(value, "targetFaction", ""),
                bool(value, "playerTriggered"),
                bool(value, "aimAtPlayer"));
    }

    private FlagsData flags(Map<String, Object> value) {
        return new FlagsData(bool(value, "solid"), bool(value, "cameraTarget"));
    }

    private LootData loot(Map<String, Object> value) {
        if (value == null) {
            return null;
        }
        return new LootData(
                string(value, "itemId"),
                integer(value, "amount"),
                number(value, "dropChance", 1.0));
    }

    private AttributesData attributes(Map<String, Object> value) {
        return new AttributesData(
                integer(value, "sta"),
                integer(value, "str"),
                integer(value, "int"),
                integer(value, "agi"),
                integer(value, "spi"));
    }

    private List<WorldLayoutData.NpcSpawnData> npcSpawns(List<Object> values) {
        List<WorldLayoutData.NpcSpawnData> result = new ArrayList<>();
        for (Object value : values) {
            Map<String, Object> entry = object(value);
            result.add(new WorldLayoutData.NpcSpawnData(
                    string(entry, "placementId"),
                    string(entry, "npcId"),
                    integer(entry, "tileX"),
                    integer(entry, "tileY")));
        }
        return List.copyOf(result);
    }

    private List<WorldLayoutData.EnemySpawnerData> enemySpawners(List<Object> values) {
        List<WorldLayoutData.EnemySpawnerData> result = new ArrayList<>();
        for (Object value : values) {
            Map<String, Object> entry = object(value);
            result.add(new WorldLayoutData.EnemySpawnerData(
                    string(entry, "placementId"),
                    string(entry, "prefabId"),
                    integer(entry, "minTileX"),
                    integer(entry, "minTileY"),
                    integer(entry, "maxTileX"),
                    integer(entry, "maxTileY"),
                    integer(entry, "maxAlive"),
                    integer(entry, "respawnDelayTicks"),
                    integer(entry, "retryDelayTicks"),
                    integer(entry, "maxSpawnAttempts"),
                    integer(entry, "minPlayerDistanceTiles")));
        }
        return List.copyOf(result);
    }

    private List<WorldLayoutData.WorldObjectSpawnData> worldObjects(List<Object> values) {
        List<WorldLayoutData.WorldObjectSpawnData> result = new ArrayList<>();
        for (Object value : values) {
            Map<String, Object> entry = object(value);
            result.add(new WorldLayoutData.WorldObjectSpawnData(
                    string(entry, "placementId"),
                    string(entry, "objectId"),
                    integer(entry, "tileX"),
                    integer(entry, "tileY")));
        }
        return List.copyOf(result);
    }

    private WorldObjectData.CollectibleDropData collectibleDrop(Object value) {
        if (value == null) {
            return null;
        }
        Map<String, Object> entry = object(value);
        return new WorldObjectData.CollectibleDropData(
                string(entry, "itemId"),
                integer(entry, "amount"));
    }

    private WorldObjectData.DoorRuleData doorRule(Object value) {
        if (value == null) {
            return null;
        }
        Map<String, Object> entry = object(value);
        return new WorldObjectData.DoorRuleData(
                bool(entry, "locked"),
                string(entry, "requiredItemId", ""));
    }

    private WorldObjectData.ChestRuleData chestRule(Object value) {
        if (value == null) {
            return null;
        }
        Map<String, Object> entry = object(value);
        return new WorldObjectData.ChestRuleData(string(entry, "openedSpriteId"));
    }

    private WorldObjectData.InteractionRuleData interactionRule(Object value) {
        if (value == null) {
            return null;
        }
        Map<String, Object> entry = object(value);
        return new WorldObjectData.InteractionRuleData(
                string(entry, "interactionHintKey", ""),
                string(entry, "successToastKey", ""),
                string(entry, "failureToastKey", ""),
                string(entry, "successAudioId", ""),
                string(entry, "failureAudioId", ""));
    }

    private EconomyData.EconomyPhaseData economyPhase(Map<String, Object> value) {
        Map<String, EconomyData.ItemEconomyData> items = new LinkedHashMap<>();
        Map<String, Object> itemsObject = optionalObject(value, "items");
        if (itemsObject != null) {
            for (Map.Entry<String, Object> entry : itemsObject.entrySet()) {
                Map<String, Object> itemValue = object(entry.getValue());
                items.put(entry.getKey(), new EconomyData.ItemEconomyData(
                        (float) number(itemValue, "priceMultiplier", 1.0),
                        integer(itemValue, "stockDelta", 0)));
            }
        }
        return new EconomyData.EconomyPhaseData(
                (float) number(value, "priceMultiplier", 1.0),
                integer(value, "stockDelta", 0),
                Map.copyOf(items));
    }

    private WorldPhaseData.SlimePhaseRuleData slimePhaseRule(Map<String, Object> value) {
        return new WorldPhaseData.SlimePhaseRuleData(
                string(value, "displayName", ""),
                integer(value, "healthDelta"),
                integer(value, "attackDelta"),
                integer(value, "defenseDelta"),
                string(value, "animationBaseClipId"));
    }

    private ShopData shopData(Map<String, Object> value) {
        if (value == null) {
            return null;
        }
        return new ShopData(
                shopListings(optionalArray(value, "day")),
                shopListings(optionalArray(value, "night")));
    }

    private List<ShopData.ShopListingData> shopListings(List<Object> values) {
        if (values == null) {
            return List.of();
        }
        List<ShopData.ShopListingData> result = new ArrayList<>();
        for (Object value : values) {
            Map<String, Object> entry = object(value);
            result.add(new ShopData.ShopListingData(
                    string(entry, "itemId"),
                    integer(entry, "price"),
                    integer(entry, "stock", -1)));
        }
        return List.copyOf(result);
    }
}
