package app;

import java.nio.file.Path;

public final class GameConfig {
    public static final double AUTO_SAVE_INTERVAL_SECONDS = 30.0;
    public static final int ORIGINAL_TILE_SIZE = 16;
    public static final int SCALE = 3;
    public static final int TILE_SIZE = ORIGINAL_TILE_SIZE * SCALE;
    public static final int SCREEN_COLS = 16;
    public static final int SCREEN_ROWS = 12;
    public static final int SCREEN_WIDTH = TILE_SIZE * SCREEN_COLS;
    public static final int SCREEN_HEIGHT = TILE_SIZE * SCREEN_ROWS;
    public static final int TARGET_FPS = 60;
    public static final int FOG_VISION_RAY_COUNT = 256;
    public static final double FOG_VISION_STEP_PIXELS = 6.0;
    public static final int FOG_VISION_REFINE_STEPS = 4;
    public static final int FOG_VISION_RADIUS_PIXELS = 165;
    public static final int FOG_VISION_FADE_PIXELS = 78;
    public static final int FOG_DAYLIGHT_CORE_RADIUS = 78;
    public static final int FOG_ALWAYS_VISIBLE_RADIUS = 46;
    public static final String WINDOW_TITLE = "Swap RPG ECS";
    public static final Path SAVE_ROOT_DIR = Path.of("save-data");
    public static final Path ACCOUNT_FILE = Path.of("swap-rpg-account.properties");
    public static final String SWAP_WEB_URL = "https://swap.com.es";

    private GameConfig() {
    }
}
