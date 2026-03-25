package data;

/**
 * Immutable definition for one enemy archetype loaded from external content.
 *
 * This is not a live ECS entity. It is the source data PrefabFactory reads to assemble
 * components when an enemy is spawned into the world.
 */
public record EnemyData(
        String id,
        String name,
        String faction,
        VisualData visual,
        ColliderData collider,
        StatsData stats,
        ProjectileData projectile,
        FlagsData flags,
        boolean wander) {
}
