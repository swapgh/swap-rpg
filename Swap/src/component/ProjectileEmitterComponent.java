package component;

public final class ProjectileEmitterComponent {
    public final String projectileSpriteId;
    public final int projectileSpeed;
    public final int projectileDamage;
    public final int projectileLifetimeTicks;
    public final int cooldownTicks;
    public final int projectileSize;
    public final String targetFaction;
    public final boolean playerTriggered;
    public final boolean aimAtPlayer;
    public int cooldownRemaining;

    public ProjectileEmitterComponent(String projectileSpriteId, int projectileSpeed, int projectileDamage,
            int projectileLifetimeTicks, int cooldownTicks, int projectileSize, String targetFaction,
            boolean playerTriggered, boolean aimAtPlayer) {
        this.projectileSpriteId = projectileSpriteId;
        this.projectileSpeed = projectileSpeed;
        this.projectileDamage = projectileDamage;
        this.projectileLifetimeTicks = projectileLifetimeTicks;
        this.cooldownTicks = cooldownTicks;
        this.projectileSize = projectileSize;
        this.targetFaction = targetFaction;
        this.playerTriggered = playerTriggered;
        this.aimAtPlayer = aimAtPlayer;
    }
}
