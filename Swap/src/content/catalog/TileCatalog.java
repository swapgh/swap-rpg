package content.catalog;

import asset.AssetManager;
import asset.TileDefinition;

public final class TileCatalog {
    private TileCatalog() {
    }

    public static TileDefinition[] register(AssetManager assets, int tileSize, String catalogId) {
        String resolvedId = catalogId == null || catalogId.isBlank() ? "legacy" : catalogId.trim().toLowerCase();
        return switch (resolvedId) {
        case "legacy" -> LegacyTileCatalog.register(assets, tileSize);
        case "tiled_woods" -> TiledWoodsTileCatalog.register(assets, tileSize);
        default -> throw new IllegalArgumentException("Unknown tile catalog: " + catalogId);
        };
    }
}
