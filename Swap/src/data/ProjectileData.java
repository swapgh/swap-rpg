package data;

/**
 * Describes optional ranged attack behaviour for an entity type.
 *
 * `enabled` lets one data shape serve both ranged and non-ranged entities without
 * requiring separate loader code paths.
 */
public record ProjectileData(
        boolean enabled,
        String spriteId,
        int speed,
        int damage,
        int lifetimeTicks,
        int cooldownTicks,
        double sizeScale,
        String targetFaction,
        boolean playerTriggered,
        boolean aimAtPlayer) {
}
