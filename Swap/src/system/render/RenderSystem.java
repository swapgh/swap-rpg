package system.render;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import app.Camera;
import asset.AssetManager;
import asset.TileMap;
import component.AttackComponent;
import component.FacingComponent;
import component.PositionComponent;
import component.SpriteComponent;
import ecs.EcsWorld;
import util.Direction;

public final class RenderSystem {
    private final AssetManager assets;
    private final TileMap map;
    private final Camera camera;
    private final int screenWidth;
    private final int screenHeight;

    public RenderSystem(AssetManager assets, TileMap map, Camera camera, int screenWidth, int screenHeight) {
        this.assets = assets;
        this.map = map;
        this.camera = camera;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void render(Graphics2D g2, EcsWorld world) {
        map.render(g2, assets, camera.x(), camera.y(), screenWidth, screenHeight);
        List<Integer> entities = new ArrayList<>(world.entitiesWith(PositionComponent.class, SpriteComponent.class));
        entities.sort(Comparator.comparingInt(entity -> (int) world.require(entity, PositionComponent.class).y));
        for (int entity : entities) {
            PositionComponent pos = world.require(entity, PositionComponent.class);
            SpriteComponent sprite = world.require(entity, SpriteComponent.class);
            BufferedImage image = assets.image(sprite.imageId);
            int drawX = (int) (pos.x - camera.x());
            int drawY = (int) (pos.y - camera.y());

            if (world.has(entity, AttackComponent.class) && world.has(entity, FacingComponent.class)) {
                AttackComponent attack = world.require(entity, AttackComponent.class);
                if (attack.activeTicks > 0) {
                    int extraWidth = Math.max(0, image.getWidth() - sprite.width);
                    int extraHeight = Math.max(0, image.getHeight() - sprite.height);
                    Direction facing = world.require(entity, FacingComponent.class).direction;
                    switch (facing) {
                    case UP -> drawY -= extraHeight;
                    case LEFT -> drawX -= extraWidth;
                    case DOWN, RIGHT -> {
                    }
                    }
                }
            }

            g2.drawImage(image, drawX, drawY, null);
        }
    }
}
