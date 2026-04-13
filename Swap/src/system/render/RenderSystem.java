package system.render;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import app.Camera;
import asset.AssetManager;
import asset.TileMap;
import component.actor.EnemyComponent;
import component.combat.AttackComponent;
import component.combat.HealthComponent;
import component.actor.FacingComponent;
import component.world.PositionComponent;
import component.render.SpriteComponent;
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
        map.renderBackground(g2, assets, camera.x(), camera.y(), screenWidth, screenHeight);
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
            drawEnemyHealthBar(g2, world, entity, drawX, drawY, sprite);
        }
        map.renderForeground(g2, camera.x(), camera.y(), screenWidth, screenHeight);
    }

    private void drawEnemyHealthBar(Graphics2D g2, EcsWorld world, int entity, int drawX, int drawY, SpriteComponent sprite) {
        if (!world.has(entity, EnemyComponent.class) || !world.has(entity, HealthComponent.class)) {
            return;
        }
        HealthComponent health = world.require(entity, HealthComponent.class);
        if (health.current <= 0 || health.enemyBarVisibleTicks <= 0) {
            return;
        }

        int barWidth = Math.max(18, sprite.width);
        int barHeight = 5;
        int barX = drawX + (sprite.width - barWidth) / 2;
        int barY = drawY - 8;
        int fillWidth = (int) Math.round(barWidth * (Math.max(0, health.current) / (double) Math.max(1, health.max)));

        g2.setColor(new java.awt.Color(10, 12, 16, 210));
        g2.fillRoundRect(barX - 1, barY - 1, barWidth + 2, barHeight + 2, 6, 6);
        g2.setColor(new java.awt.Color(58, 18, 18, 230));
        g2.fillRoundRect(barX, barY, barWidth, barHeight, 5, 5);
        g2.setColor(new java.awt.Color(180, 52, 52, 235));
        g2.fillRoundRect(barX, barY, fillWidth, barHeight, 5, 5);
        g2.setColor(new java.awt.Color(230, 230, 230, 180));
        g2.drawRoundRect(barX, barY, barWidth, barHeight, 5, 5);
    }
}
