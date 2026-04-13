package asset;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public final class TileMap {
    private final int tileSize;
    private final int cols;
    private final int rows;
    private final int[][][] layerTileIds;
    private final TileDefinition[] definitions;
    private final boolean[][] blockedCells;
    private final BufferedImage[][] blockedMasks;
    private final BufferedImage[][] foregroundMasks;

    public TileMap(int tileSize, int cols, int rows, int[][][] layerTileIds, TileDefinition[] definitions) {
        this(tileSize, cols, rows, layerTileIds, definitions, null, null, null);
    }

    public TileMap(int tileSize, int cols, int rows, int[][][] layerTileIds, TileDefinition[] definitions, boolean[][] blockedCells) {
        this(tileSize, cols, rows, layerTileIds, definitions, blockedCells, null, null);
    }

    public TileMap(int tileSize, int cols, int rows, int[][][] layerTileIds, TileDefinition[] definitions, boolean[][] blockedCells,
            BufferedImage[][] blockedMasks) {
        this(tileSize, cols, rows, layerTileIds, definitions, blockedCells, blockedMasks, null);
    }

    public TileMap(int tileSize, int cols, int rows, int[][][] layerTileIds, TileDefinition[] definitions, boolean[][] blockedCells,
            BufferedImage[][] blockedMasks, BufferedImage[][] foregroundMasks) {
        this.tileSize = tileSize;
        this.cols = cols;
        this.rows = rows;
        this.layerTileIds = layerTileIds;
        this.definitions = definitions;
        this.blockedCells = blockedCells;
        this.blockedMasks = blockedMasks;
        this.foregroundMasks = foregroundMasks;
    }

    public int tileSize() {
        return tileSize;
    }

    public int widthPixels() {
        return cols * tileSize;
    }

    public int heightPixels() {
        return rows * tileSize;
    }

    public boolean isBlockedPixel(double worldX, double worldY) {
        if (worldX < 0 || worldY < 0 || worldX >= widthPixels() || worldY >= heightPixels()) {
            return true;
        }
        int col = (int) worldX / tileSize;
        int row = (int) worldY / tileSize;
        if (blockedMasks != null && blockedMasks[col][row] != null) {
            BufferedImage mask = blockedMasks[col][row];
            int localX = ((int) worldX) - (col * tileSize);
            int localY = ((int) worldY) - (row * tileSize);
            int alpha = (mask.getRGB(localX, localY) >>> 24) & 0xff;
            return alpha > 0;
        }
        if (blockedCells != null) {
            return blockedCells[col][row];
        }
        for (int layer = 0; layer < layerTileIds.length; layer++) {
            int tileId = layerTileIds[layer][col][row];
            if (tileId < 0) {
                continue;
            }
            TileDefinition definition = definitions[tileId];
            if (definition.blocked()) {
                return true;
            }
        }
        return false;
    }

    public void renderBackground(Graphics2D g2, AssetManager assets, double cameraX, double cameraY, int screenWidth, int screenHeight) {
        int startCol = Math.max(0, (int) (cameraX / tileSize) - 1);
        int endCol = Math.min(cols, startCol + (screenWidth / tileSize) + 3);
        int startRow = Math.max(0, (int) (cameraY / tileSize) - 1);
        int endRow = Math.min(rows, startRow + (screenHeight / tileSize) + 3);

        for (int layer = 0; layer < layerTileIds.length; layer++) {
            for (int col = startCol; col < endCol; col++) {
                for (int row = startRow; row < endRow; row++) {
                    int tileId = layerTileIds[layer][col][row];
                    if (tileId < 0) {
                        continue;
                    }
                    int x = (int) (col * tileSize - cameraX);
                    int y = (int) (row * tileSize - cameraY);
                    g2.drawImage(assets.image(definitions[tileId].imageId()), x, y, null);
                }
            }
        }
    }

    public void renderForeground(Graphics2D g2, double cameraX, double cameraY, int screenWidth, int screenHeight) {
        if (foregroundMasks == null) {
            return;
        }
        int startCol = Math.max(0, (int) (cameraX / tileSize) - 1);
        int endCol = Math.min(cols, startCol + (screenWidth / tileSize) + 3);
        int startRow = Math.max(0, (int) (cameraY / tileSize) - 1);
        int endRow = Math.min(rows, startRow + (screenHeight / tileSize) + 3);

        for (int col = startCol; col < endCol; col++) {
            for (int row = startRow; row < endRow; row++) {
                BufferedImage mask = foregroundMasks[col][row];
                if (mask == null) {
                    continue;
                }
                int x = (int) (col * tileSize - cameraX);
                int y = (int) (row * tileSize - cameraY);
                g2.drawImage(mask, x, y, null);
            }
        }
    }
}
