package app;

import java.nio.file.Path;

public final class GameConfig {
    public static final int ORIGINAL_TILE_SIZE = 16;
    public static final int SCALE = 3;
    public static final int TILE_SIZE = ORIGINAL_TILE_SIZE * SCALE;
    public static final int SCREEN_COLS = 16;
    public static final int SCREEN_ROWS = 12;
    public static final int SCREEN_WIDTH = TILE_SIZE * SCREEN_COLS;
    public static final int SCREEN_HEIGHT = TILE_SIZE * SCREEN_ROWS;
    public static final int TARGET_FPS = 60;
    public static final String WINDOW_TITLE = "Swap RPG ECS";
    public static final Path SAVE_FILE = Path.of("swap-rpg-save");

    private GameConfig() {
    }
}
