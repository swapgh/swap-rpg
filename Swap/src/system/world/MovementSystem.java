package system.world;

import java.awt.Rectangle;

import asset.TileMap;
import component.ColliderComponent;
import component.PositionComponent;
import component.SolidComponent;
import component.StatsComponent;
import component.VelocityComponent;
import ecs.EcsSystem;
import ecs.EcsWorld;
import util.CollisionUtil;

public final class MovementSystem implements EcsSystem {
    private final TileMap map;

    public MovementSystem(TileMap map) {
        this.map = map;
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        for (int entity : world.entitiesWith(PositionComponent.class, VelocityComponent.class, ColliderComponent.class,
                StatsComponent.class)) {
            PositionComponent pos = world.require(entity, PositionComponent.class);
            VelocityComponent velocity = world.require(entity, VelocityComponent.class);
            StatsComponent stats = world.require(entity, StatsComponent.class);
            ColliderComponent collider = world.require(entity, ColliderComponent.class);

            double moveX = Math.signum(velocity.dx) * Math.min(Math.abs(velocity.dx), stats.speed);
            double moveY = Math.signum(velocity.dy) * Math.min(Math.abs(velocity.dy), stats.speed);

            if (!blocked(world, entity, pos, collider, moveX, 0)) {
                pos.x += moveX;
            }
            if (!blocked(world, entity, pos, collider, 0, moveY)) {
                pos.y += moveY;
            }
        }
    }

    private boolean blocked(EcsWorld world, int movingEntity, PositionComponent pos, ColliderComponent collider, double dx,
            double dy) {
        Rectangle target = CollisionUtil.movedRect(pos, collider, dx, dy);
        if (map.isBlockedPixel(target.x, target.y)
                || map.isBlockedPixel(target.x + target.width - 1, target.y)
                || map.isBlockedPixel(target.x, target.y + target.height - 1)
                || map.isBlockedPixel(target.x + target.width - 1, target.y + target.height - 1)) {
            return true;
        }

        for (int entity : world.entitiesWith(PositionComponent.class, ColliderComponent.class, SolidComponent.class)) {
            if (entity == movingEntity) {
                continue;
            }
            SolidComponent solid = world.require(entity, SolidComponent.class);
            if (!solid.solid) {
                continue;
            }
            Rectangle other = CollisionUtil.rect(world.require(entity, PositionComponent.class),
                    world.require(entity, ColliderComponent.class));
            if (target.intersects(other)) {
                return true;
            }
        }
        return false;
    }
}
