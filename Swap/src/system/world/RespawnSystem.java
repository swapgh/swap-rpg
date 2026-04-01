package system.world;

import asset.TileMap;
import component.world.ColliderComponent;
import component.actor.PlayerComponent;
import component.world.PositionComponent;
import component.world.RespawnAreaComponent;
import component.world.RespawnSpawnerComponent;
import component.world.SpawnedByComponent;
import component.world.WorldTimeComponent;
import content.prefab.PrefabFactory;
import data.DataRegistry;
import data.EnemyData;
import ecs.EcsSystem;
import ecs.EcsWorld;
import java.awt.Rectangle;
import java.util.List;
import java.util.Random;
import util.CollisionUtil;

/**
 * Keeps area-based spawners populated without exceeding their configured caps.
 */
public final class RespawnSystem implements EcsSystem {
    private final TileMap map;
    private final DataRegistry data;
    private final int tileSize;
    private final Random random = new Random(0x5EEDC0DEL);

    public RespawnSystem(TileMap map, DataRegistry data, int tileSize) {
        this.map = map;
        this.data = data;
        this.tileSize = tileSize;
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        for (int spawnerEntity : world.entitiesWith(RespawnSpawnerComponent.class, RespawnAreaComponent.class)) {
            RespawnSpawnerComponent spawner = world.require(spawnerEntity, RespawnSpawnerComponent.class);
            RespawnAreaComponent area = world.require(spawnerEntity, RespawnAreaComponent.class);

            cleanupActiveList(world, spawnerEntity, spawner);
            int activeCount = spawner.activeSpawned.size();

            if (!spawner.initialized) {
                fillInitialPopulation(world, spawnerEntity, spawner, area);
                spawner.initialized = true;
                spawner.lastAliveCount = spawner.activeSpawned.size();
                continue;
            }

            if (activeCount < spawner.lastAliveCount) {
                spawner.cooldownTicks = Math.max(spawner.cooldownTicks, spawner.respawnDelayTicks);
            }

            if (activeCount >= spawner.maxAlive) {
                spawner.lastAliveCount = activeCount;
                continue;
            }

            if (spawner.cooldownTicks > 0) {
                spawner.cooldownTicks--;
                spawner.lastAliveCount = activeCount;
                continue;
            }

            if (spawnOne(world, spawnerEntity, spawner, area)) {
                spawner.cooldownTicks = spawner.respawnDelayTicks;
            } else {
                spawner.cooldownTicks = spawner.retryDelayTicks;
            }
            spawner.lastAliveCount = spawner.activeSpawned.size();
        }
    }

    private void fillInitialPopulation(EcsWorld world, int spawnerEntity, RespawnSpawnerComponent spawner,
            RespawnAreaComponent area) {
        while (spawner.activeSpawned.size() < spawner.maxAlive) {
            if (!spawnOne(world, spawnerEntity, spawner, area)) {
                break;
            }
        }
    }

    private boolean spawnOne(EcsWorld world, int spawnerEntity, RespawnSpawnerComponent spawner, RespawnAreaComponent area) {
        EnemyData enemy = data.enemy(resolvePrefabId(world, spawner.prefabId));
        int attempts = Math.max(1, spawner.maxSpawnAttempts);
        for (int attempt = 0; attempt < attempts; attempt++) {
            int tileX = randomTile(area.minTileX, area.maxTileX);
            int tileY = randomTile(area.minTileY, area.maxTileY);
            int worldX = tileX * tileSize;
            int worldY = tileY * tileSize;
            if (!canSpawnAt(world, enemy, worldX, worldY, spawner.minPlayerDistanceTiles)) {
                continue;
            }
            int entity = PrefabFactory.createEnemy(world, enemy, worldX, worldY, tileSize);
            world.add(entity, new SpawnedByComponent(spawnerEntity));
            spawner.activeSpawned.add(entity);
            return true;
        }
        return false;
    }

    private String resolvePrefabId(EcsWorld world, String prefabId) {
        if (!"green_slime".equals(prefabId)) {
            return prefabId;
        }
        List<Integer> timeEntities = world.entitiesWith(WorldTimeComponent.class);
        if (timeEntities.isEmpty()) {
            return prefabId;
        }
        return world.require(timeEntities.get(0), WorldTimeComponent.class).isDay() ? prefabId : "green_slime";
    }

    private int randomTile(int minInclusive, int maxInclusive) {
        if (maxInclusive < minInclusive) {
            return minInclusive;
        }
        return minInclusive + random.nextInt(maxInclusive - minInclusive + 1);
    }

    private boolean canSpawnAt(EcsWorld world, EnemyData enemy, int worldX, int worldY, int minPlayerDistanceTiles) {
        Rectangle candidate = new Rectangle(
                worldX + enemy.collider().offsetX(),
                worldY + enemy.collider().offsetY(),
                enemy.collider().width(),
                enemy.collider().height());

        if (blockedByMap(candidate)) {
            return false;
        }
        if (occupiedByEntity(world, candidate)) {
            return false;
        }
        return farEnoughFromPlayer(world, candidate, minPlayerDistanceTiles);
    }

    private boolean blockedByMap(Rectangle candidate) {
        return map.isBlockedPixel(candidate.x, candidate.y)
                || map.isBlockedPixel(candidate.x + candidate.width - 1, candidate.y)
                || map.isBlockedPixel(candidate.x, candidate.y + candidate.height - 1)
                || map.isBlockedPixel(candidate.x + candidate.width - 1, candidate.y + candidate.height - 1);
    }

    private boolean occupiedByEntity(EcsWorld world, Rectangle candidate) {
        for (int entity : world.entitiesWith(PositionComponent.class, ColliderComponent.class)) {
            Rectangle other = CollisionUtil.rect(
                    world.require(entity, PositionComponent.class),
                    world.require(entity, ColliderComponent.class));
            if (candidate.intersects(other)) {
                return true;
            }
        }
        return false;
    }

    private boolean farEnoughFromPlayer(EcsWorld world, Rectangle candidate, int minPlayerDistanceTiles) {
        double minDistancePixels = Math.max(0, minPlayerDistanceTiles) * tileSize;
        double minDistanceSquared = minDistancePixels * minDistancePixels;
        double candidateCenterX = candidate.getCenterX();
        double candidateCenterY = candidate.getCenterY();
        List<Integer> players = world.entitiesWith(PlayerComponent.class, PositionComponent.class);
        for (int player : players) {
            PositionComponent position = world.require(player, PositionComponent.class);
            double playerCenterX = position.x + tileSize / 2.0;
            double playerCenterY = position.y + tileSize / 2.0;
            double dx = candidateCenterX - playerCenterX;
            double dy = candidateCenterY - playerCenterY;
            if ((dx * dx) + (dy * dy) < minDistanceSquared) {
                return false;
            }
        }
        return true;
    }

    private void cleanupActiveList(EcsWorld world, int spawnerEntity, RespawnSpawnerComponent spawner) {
        spawner.activeSpawned.removeIf(entity -> !world.isAlive(entity)
                || !world.has(entity, SpawnedByComponent.class)
                || world.require(entity, SpawnedByComponent.class).spawnerEntity != spawnerEntity);
    }
}
