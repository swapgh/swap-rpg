package system.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

import component.HealthComponent;
import component.InventoryComponent;
import component.NameComponent;
import component.PlayerComponent;
import component.PositionComponent;
import component.ProgressionComponent;
import component.QuestComponent;
import component.ShopComponent;
import component.WorldTimeComponent;
import component.WorldPlacementComponent;
import ecs.EcsWorld;

public final class SaveLoadSystem {
    public void save(EcsWorld world, Path path) {
        int player = world.entitiesWith(PlayerComponent.class).get(0);
        PositionComponent pos = world.require(player, PositionComponent.class);
        NameComponent name = world.require(player, NameComponent.class);
        HealthComponent health = world.require(player, HealthComponent.class);
        InventoryComponent inventory = world.require(player, InventoryComponent.class);
        QuestComponent quests = world.require(player, QuestComponent.class);
        ProgressionComponent progression = world.require(player, ProgressionComponent.class);
        Properties properties = new Properties();
        properties.setProperty("player.name", name.value);
        properties.setProperty("player.x", Integer.toString((int) pos.x));
        properties.setProperty("player.y", Integer.toString((int) pos.y));
        properties.setProperty("player.hp", Integer.toString(health.current));
        properties.setProperty("coins", Integer.toString(inventory.coins));
        properties.setProperty("items", String.join(",", inventory.itemIds));
        properties.setProperty("quests.completed", String.join(",", quests.completed));
        properties.setProperty("progress.enemies_killed", Integer.toString(progression.enemiesKilled));
        List<Integer> timeEntities = world.entitiesWith(WorldTimeComponent.class);
        if (!timeEntities.isEmpty()) {
            WorldTimeComponent time = world.require(timeEntities.get(0), WorldTimeComponent.class);
            properties.setProperty("world.total_seconds", Long.toString(time.totalSeconds));
            properties.setProperty("world.last_real_epoch_seconds",
                    Long.toString(Instant.now().getEpochSecond()));
        }
        for (int entity : world.entitiesWith(ShopComponent.class, WorldPlacementComponent.class)) {
            ShopComponent shop = world.require(entity, ShopComponent.class);
            WorldPlacementComponent placement = world.require(entity, WorldPlacementComponent.class);
            for (var entry : shop.remainingStock.entrySet()) {
                properties.setProperty("shop.stock." + placement.placementId + "." + entry.getKey(),
                        Integer.toString(entry.getValue()));
            }
        }
        try (OutputStream out = Files.newOutputStream(path)) {
            properties.store(out, "swap-rpg save");
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo guardar", ex);
        }
    }

    public void load(EcsWorld world, Path path) {
        if (!Files.exists(path)) {
            return;
        }
        int player = world.entitiesWith(PlayerComponent.class).get(0);
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo cargar", ex);
        }
        PositionComponent pos = world.require(player, PositionComponent.class);
        HealthComponent health = world.require(player, HealthComponent.class);
        InventoryComponent inventory = world.require(player, InventoryComponent.class);
        QuestComponent quests = world.require(player, QuestComponent.class);
        ProgressionComponent progression = world.require(player, ProgressionComponent.class);
        int loadedHp = Integer.parseInt(properties.getProperty("player.hp", Integer.toString(health.current)));
        if (loadedHp <= 0) {
            return;
        }
        pos.x = Integer.parseInt(properties.getProperty("player.x", Integer.toString((int) pos.x)));
        pos.y = Integer.parseInt(properties.getProperty("player.y", Integer.toString((int) pos.y)));
        health.current = loadedHp;
        inventory.coins = Integer.parseInt(properties.getProperty("coins", "0"));
        inventory.itemIds.clear();
        String items = properties.getProperty("items", "");
        if (!items.isBlank()) {
            for (String item : items.split(",")) {
                inventory.itemIds.add(item);
            }
        }
        quests.completed.clear();
        String completed = properties.getProperty("quests.completed", "");
        if (!completed.isBlank()) {
            for (String quest : completed.split(",")) {
                if (!quest.isBlank()) {
                    quests.completed.add(quest);
                }
            }
        }
        progression.enemiesKilled = Integer.parseInt(properties.getProperty("progress.enemies_killed", "0"));
        List<Integer> timeEntities = world.entitiesWith(WorldTimeComponent.class);
        if (!timeEntities.isEmpty()) {
            WorldTimeComponent time = world.require(timeEntities.get(0), WorldTimeComponent.class);
            long nowEpochSeconds = Instant.now().getEpochSecond();
            long savedTotalSeconds = Long.parseLong(
                    properties.getProperty("world.total_seconds", Long.toString(time.totalSeconds)));
            long savedEpochSeconds = Long.parseLong(
                    properties.getProperty("world.last_real_epoch_seconds", Long.toString(nowEpochSeconds)));
            long elapsedSeconds = Math.max(0L, nowEpochSeconds - savedEpochSeconds);
            time.totalSeconds = savedTotalSeconds + elapsedSeconds;
            time.lastRealEpochSeconds = nowEpochSeconds;
            time.secondProgress = 0;
        }
        for (int entity : world.entitiesWith(ShopComponent.class, WorldPlacementComponent.class)) {
            ShopComponent shop = world.require(entity, ShopComponent.class);
            WorldPlacementComponent placement = world.require(entity, WorldPlacementComponent.class);
            shop.remainingStock.clear();
            String prefix = "shop.stock." + placement.placementId + ".";
            for (String key : properties.stringPropertyNames()) {
                if (!key.startsWith(prefix)) {
                    continue;
                }
                String stockKey = key.substring(prefix.length());
                shop.remainingStock.put(stockKey, Integer.parseInt(properties.getProperty(key, "0")));
            }
        }
        progression.dirtySync = true;
    }
}
