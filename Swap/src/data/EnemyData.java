package data;

public record EnemyData(
        String id,
        String name,
        String faction,
        VisualData visual,
        ColliderData collider,
        StatsData stats,
        ProjectileData projectile,
        LootData loot,
        FlagsData flags,
        boolean wander) {
}
