package data;

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
