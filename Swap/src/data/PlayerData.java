package data;

public record PlayerData(
        String id,
        String name,
        String classId,
        String faction,
        int startingLevel,
        SpawnData spawn,
        VisualData visual,
        ColliderData collider,
        StatsData stats,
        AttackData attack,
        ProjectileData projectile,
        FlagsData flags) {
}
