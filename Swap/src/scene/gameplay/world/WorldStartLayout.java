package scene.gameplay.world;

import app.bootstrap.GameConfig;

public final class WorldStartLayout {
    private WorldStartLayout() {
    }

    public static String mapResourceFor(String classId) {
        String normalized = normalizeClassId(classId);
        if ("druid".equals(normalized)) {
            return GameConfig.WORLD_CURRENT_MAP;
        }
        return GameConfig.WORLD_OLD_MAP;
    }

    public static int[] spawnFor(String classId) {
        String normalized = normalizeClassId(classId);
        if ("druid".equals(normalized)) {
            return new int[] { GameConfig.WORLD_CURRENT_SPAWN_TILE_X, GameConfig.WORLD_CURRENT_SPAWN_TILE_Y };
        }
        return new int[] { GameConfig.WORLD_OLD_SPAWN_TILE_X, GameConfig.WORLD_OLD_SPAWN_TILE_Y };
    }

    public static String normalizeClassId(String classId) {
        String normalized = classId == null ? "" : classId.trim().toLowerCase();
        return normalized.isBlank() ? "warrior" : normalized;
    }
}
