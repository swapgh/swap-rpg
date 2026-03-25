package asset;

import java.awt.Graphics2D;

public final class TileMap {
    private final int tileSize;
    private final int cols;
    private final int rows;
    private final int[][] tileIds;
    private final TileDefinition[] definitions;

    public TileMap(int tileSize, int cols, int rows, int[][] tileIds, TileDefinition[] definitions) {
        this.tileSize = tileSize;
        this.cols = cols;
        this.rows = rows;
        this.tileIds = tileIds;
        this.definitions = definitions;
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
        int tileId = tileIds[col][row];
        TileDefinition definition = definitions[tileId];
        return definition.blocked();
    }

    public void render(Graphics2D g2, AssetManager assets, double cameraX, double cameraY, int screenWidth, int screenHeight) {
        int startCol = Math.max(0, (int) (cameraX / tileSize) - 1);
        int endCol = Math.min(cols, startCol + (screenWidth / tileSize) + 3);
        int startRow = Math.max(0, (int) (cameraY / tileSize) - 1);
        int endRow = Math.min(rows, startRow + (screenHeight / tileSize) + 3);

        for (int col = startCol; col < endCol; col++) {
            for (int row = startRow; row < endRow; row++) {
                int x = (int) (col * tileSize - cameraX);
                int y = (int) (row * tileSize - cameraY);
                g2.drawImage(assets.image(definitions[tileIds[col][row]].imageId()), x, y, null);
            }
        }
    }
}
