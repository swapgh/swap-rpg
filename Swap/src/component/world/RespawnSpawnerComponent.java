package component.world;

import java.util.ArrayList;
import java.util.List;

/**
 * Runtime state for an ECS-controlled respawn point.
 *
 * The current implementation respawns enemy prefabs by id, but the shape is
 * generic enough to evolve later if other prefab families get factory support.
 */
public final class RespawnSpawnerComponent {
    public final String prefabId;
    public final int maxAlive;
    public final int respawnDelayTicks;
    public final int retryDelayTicks;
    public final int maxSpawnAttempts;
    public final int minPlayerDistanceTiles;
    public final List<Integer> activeSpawned = new ArrayList<>();
    public int cooldownTicks;
    public int lastAliveCount;
    public boolean initialized;

    public RespawnSpawnerComponent(String prefabId, int maxAlive, int respawnDelayTicks, int retryDelayTicks,
            int maxSpawnAttempts, int minPlayerDistanceTiles) {
        this.prefabId = prefabId;
        this.maxAlive = maxAlive;
        this.respawnDelayTicks = respawnDelayTicks;
        this.retryDelayTicks = retryDelayTicks;
        this.maxSpawnAttempts = maxSpawnAttempts;
        this.minPlayerDistanceTiles = minPlayerDistanceTiles;
    }
}
