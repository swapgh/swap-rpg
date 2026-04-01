package component.world;

public final class WorldTierComponent {
    public int tier;

    public WorldTierComponent(int tier) {
        this.tier = Math.max(1, Math.min(5, tier));
    }
}
