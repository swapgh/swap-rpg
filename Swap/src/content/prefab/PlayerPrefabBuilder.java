package content.prefab;

import component.AttackComponent;
import component.CameraTargetComponent;
import component.ColliderComponent;
import component.FactionComponent;
import component.HealthComponent;
import component.InputComponent;
import component.InventoryComponent;
import component.NameComponent;
import component.PlayerComponent;
import component.PositionComponent;
import component.ProgressionComponent;
import component.ProjectileEmitterComponent;
import component.QuestComponent;
import component.SolidComponent;
import component.StatsComponent;
import component.VelocityComponent;
import data.PlayerData;
import ecs.EcsWorld;

final class PlayerPrefabBuilder {
    private PlayerPrefabBuilder() {
    }

    static int create(EcsWorld world, PlayerData data, int tileSize) {
        int entity = world.createEntity();
        world.add(entity, new PlayerComponent(data.id()));
        world.add(entity, new FactionComponent(data.faction()));
        world.add(entity, new NameComponent(data.name()));
        world.add(entity, new PositionComponent(data.spawn().tileX() * tileSize, data.spawn().tileY() * tileSize));
        world.add(entity, new VelocityComponent());
        PrefabVisualSupport.addAnimatedSprite(world, entity, data.visual(), tileSize);

        world.add(entity, new ColliderComponent(
                data.collider().offsetX(),
                data.collider().offsetY(),
                data.collider().width(),
                data.collider().height()));
        world.add(entity, new StatsComponent(data.stats().speed(), data.stats().attack(), data.stats().defense()));
        world.add(entity, new HealthComponent(data.stats().health(), data.stats().health()));
        world.add(entity, new AttackComponent(
                data.attack().damage(),
                PrefabVisualSupport.scaledSize(tileSize, data.attack().rangeScale()),
                data.attack().cooldownTicks()));

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

        world.add(entity, new InputComponent());
        world.add(entity, new InventoryComponent());
        world.add(entity, new QuestComponent());
        world.add(entity, new ProgressionComponent());
        world.add(entity, new SolidComponent(data.flags().solid()));

        if (data.flags().cameraTarget()) {
            world.add(entity, new CameraTargetComponent());
        }
        return entity;
    }
}
