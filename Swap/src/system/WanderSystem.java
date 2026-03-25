package system;

import component.EnemyComponent;
import component.FacingComponent;
import component.StatsComponent;
import component.VelocityComponent;
import component.WanderAiComponent;
import ecs.EcsSystem;
import ecs.EcsWorld;
import util.Direction;

public final class WanderSystem implements EcsSystem {
    @Override
    public void update(EcsWorld world, double dtSeconds) {
        for (int entity : world.entitiesWith(EnemyComponent.class, WanderAiComponent.class, VelocityComponent.class,
                FacingComponent.class, StatsComponent.class)) {
            WanderAiComponent ai = world.require(entity, WanderAiComponent.class);
            VelocityComponent velocity = world.require(entity, VelocityComponent.class);
            FacingComponent facing = world.require(entity, FacingComponent.class);
            StatsComponent stats = world.require(entity, StatsComponent.class);

            ai.ticksUntilTurn--;
            if (ai.ticksUntilTurn <= 0) {
                ai.seed = (ai.seed * 1103515245 + 12345) & Integer.MAX_VALUE;
                ai.ticksUntilTurn = 45 + (ai.seed % 120);
                int dir = ai.seed % 4;
                facing.direction = switch (dir) {
                case 0 -> Direction.UP;
                case 1 -> Direction.DOWN;
                case 2 -> Direction.LEFT;
                default -> Direction.RIGHT;
                };
            }
            velocity.dx = facing.direction.dx * stats.speed;
            velocity.dy = facing.direction.dy * stats.speed;
        }
    }
}
