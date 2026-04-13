package data.world;

import java.util.List;

/**
 * World population and map bootstrap data loaded from external content.
 */
public record WorldLayoutData(
        String tileCatalogId,
        List<String> mapResources,
        List<NpcSpawnData> npcs,
        List<EnemySpawnerData> enemySpawners,
        List<WorldObjectSpawnData> objects) {

    public record NpcSpawnData(String placementId, String npcId, int tileX, int tileY) {
    }

    public record EnemySpawnerData(
            String placementId,
            String prefabId,
            int minTileX,
            int minTileY,
            int maxTileX,
            int maxTileY,
            int maxAlive,
            int respawnDelayTicks,
            int retryDelayTicks,
            int maxSpawnAttempts,
            int minPlayerDistanceTiles) {
    }

    public record WorldObjectSpawnData(String placementId, String objectId, int tileX, int tileY) {
    }
}
