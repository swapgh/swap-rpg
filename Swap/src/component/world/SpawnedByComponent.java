package component.world;

/**
 * Links a spawned runtime entity back to the spawner entity that owns it.
 */
public final class SpawnedByComponent {
    public final int spawnerEntity;

    public SpawnedByComponent(int spawnerEntity) {
        this.spawnerEntity = spawnerEntity;
    }
}
