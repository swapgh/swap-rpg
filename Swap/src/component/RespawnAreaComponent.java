package component;

/**
 * Inclusive tile-space bounds where a spawner may place new entities.
 */
public final class RespawnAreaComponent {
    public final int minTileX;
    public final int minTileY;
    public final int maxTileX;
    public final int maxTileY;

    public RespawnAreaComponent(int minTileX, int minTileY, int maxTileX, int maxTileY) {
        this.minTileX = minTileX;
        this.minTileY = minTileY;
        this.maxTileX = maxTileX;
        this.maxTileY = maxTileY;
    }
}
