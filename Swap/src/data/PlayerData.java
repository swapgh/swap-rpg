package data;

/**
 * Immutable definition for the controllable player archetype.
 *
 * Keeping the player in the same data pipeline as enemies and NPCs makes it easier to
 * add future classes such as mage, druid or warrior without rewriting PrefabFactory.
 */
public record PlayerData(
        String id,
        String name,
        String faction,
        SpawnData spawn,
        VisualData visual,
        ColliderData collider,
        StatsData stats,
        AttackData attack,
        ProjectileData projectile,
        FlagsData flags) {
}
