package content.prefab;

import component.world.ColliderComponent;
import component.actor.EnemyComponent;
import component.combat.FactionComponent;
import component.combat.HealthComponent;
import component.combat.LootComponent;
import component.actor.NameComponent;
import component.world.PositionComponent;
import component.combat.ProjectileEmitterComponent;
import component.world.SolidComponent;
import component.combat.StatsComponent;
import component.world.VelocityComponent;
import component.actor.WanderAiComponent;
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
        if (data.loot() != null) {
            world.add(entity, new LootComponent(data.loot().itemId(), data.loot().amount(), data.loot().dropChance()));
        }

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
