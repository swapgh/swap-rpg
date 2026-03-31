package content.prefab;

import component.ColliderComponent;
import component.EnemyComponent;
import component.FactionComponent;
import component.HealthComponent;
import component.NameComponent;
import component.PositionComponent;
import component.ProjectileEmitterComponent;
import component.SolidComponent;
import component.StatsComponent;
import component.VelocityComponent;
import component.WanderAiComponent;
import data.EnemyData;
import ecs.EcsWorld;

final class EnemyPrefabBuilder {
    private EnemyPrefabBuilder() {
    }

    static int create(EcsWorld world, EnemyData data, int x, int y, int tileSize) {
        int entity = world.createEntity();
        world.add(entity, new EnemyComponent(data.id()));
        world.add(entity, new FactionComponent(data.faction()));
        world.add(entity, new NameComponent(data.name()));
        world.add(entity, new PositionComponent(x, y));
        world.add(entity, new VelocityComponent());
        PrefabVisualSupport.addAnimatedSprite(world, entity, data.visual(), tileSize);
        world.add(entity, new ColliderComponent(
                data.collider().offsetX(),
                data.collider().offsetY(),
                data.collider().width(),
                data.collider().height()));
        world.add(entity, new StatsComponent(data.stats().speed(), data.stats().attack(), data.stats().defense()));
        world.add(entity, new HealthComponent(data.stats().health(), data.stats().health()));
        world.add(entity, new SolidComponent(data.flags().solid()));

        if (data.wander()) {
            world.add(entity, new WanderAiComponent(60, entity * 31));
        }
        if (data.projectile().enabled()) {
            world.add(entity, new ProjectileEmitterComponent(
                    data.projectile().spriteId(),
                    data.projectile().speed(),
                    data.projectile().damage(),
                    data.projectile().lifetimeTicks(),
                    data.projectile().cooldownTicks(),
                    PrefabVisualSupport.scaledSize(tileSize, data.projectile().sizeScale()),
                    data.projectile().targetFaction(),
                    data.projectile().playerTriggered(),
                    data.projectile().aimAtPlayer()));
        }
        return entity;
    }
}
