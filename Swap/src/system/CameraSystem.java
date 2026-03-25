package system;

import app.Camera;
import asset.TileMap;
import component.CameraTargetComponent;
import component.PositionComponent;
import ecs.EcsSystem;
import ecs.EcsWorld;

public final class CameraSystem implements EcsSystem {
    private final Camera camera;
    private final TileMap map;
    private final int screenWidth;
    private final int screenHeight;

    public CameraSystem(Camera camera, TileMap map, int screenWidth, int screenHeight) {
        this.camera = camera;
        this.map = map;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        for (int entity : world.entitiesWith(CameraTargetComponent.class, PositionComponent.class)) {
            PositionComponent pos = world.require(entity, PositionComponent.class);
            camera.centerOn(pos.x + map.tileSize() / 2.0, pos.y + map.tileSize() / 2.0, screenWidth, screenHeight);
            return;
        }
    }
}
