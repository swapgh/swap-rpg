package content.catalog;

import asset.AssetManager;
import asset.TileDefinition;

final class TiledWoodsTileCatalog {
    private static final int TILE_SOURCE_SIZE = 16;
    private static final int PLAINS_COUNT = 72;
    private static final int GRASS_INDEX = 72;
    private static final int WOODS_START = 73;
    private static final int WOODS_COUNT = 264;
    private static final String PLAINS_PATH = "/tilesets/mystic_woods/plains.png";
    private static final String GRASS_PATH = "/tilesets/mystic_woods/grass.png";
    private static final String WOODS_PATH = "/tilesets/pixel_16_woods/free_pixel_16_woods.png";

    private TiledWoodsTileCatalog() {
    }

    static TileDefinition[] register(AssetManager assets, int tileSize) {
        TileDefinition[] definitions = new TileDefinition[WOODS_START + WOODS_COUNT];
        registerSheet(assets, definitions, 0, PLAINS_COUNT, 6, PLAINS_PATH, "tile.tiledwoods.plains", false, tileSize);
        assets.loadSpriteTile("tile.tiledwoods.grass", GRASS_PATH, 0, 0, TILE_SOURCE_SIZE, TILE_SOURCE_SIZE, tileSize, tileSize);
        definitions[GRASS_INDEX] = new TileDefinition(GRASS_INDEX, "tile.tiledwoods.grass", false);

        registerSheet(assets, definitions, WOODS_START, WOODS_COUNT, 22, WOODS_PATH, "tile.tiledwoods.wood", true, tileSize);
        return definitions;
    }

    private static void registerSheet(AssetManager assets, TileDefinition[] definitions, int startId, int count, int columns,
            String resourcePath, String imageIdPrefix, boolean blocked, int tileSize) {
        for (int i = 0; i < count; i++) {
            int tileId = startId + i;
            int sourceX = (i % columns) * TILE_SOURCE_SIZE;
            int sourceY = (i / columns) * TILE_SOURCE_SIZE;
            String imageId = imageIdPrefix + "." + tileId;
            assets.loadSpriteTile(imageId, resourcePath, sourceX, sourceY, TILE_SOURCE_SIZE, TILE_SOURCE_SIZE, tileSize, tileSize);
            definitions[tileId] = new TileDefinition(tileId, imageId, blocked);
        }
    }
}
