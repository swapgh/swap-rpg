package component;

public final class ProjectileComponent {
    public final int ownerEntity;
    public final String sourceFaction;
    public final String targetFaction;
    public final int damage;
    public int remainingTicks;

    public ProjectileComponent(int ownerEntity, String sourceFaction, String targetFaction, int damage, int remainingTicks) {
        this.ownerEntity = ownerEntity;
        this.sourceFaction = sourceFaction;
        this.targetFaction = targetFaction;
        this.damage = damage;
        this.remainingTicks = remainingTicks;
    }
}
