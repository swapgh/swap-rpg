package content.prefab;

import component.combat.AttackComponent;
import component.render.CameraTargetComponent;
import component.world.ColliderComponent;
import component.combat.FactionComponent;
import component.combat.HealthComponent;
import component.actor.InputComponent;
import component.progression.EquipmentComponent;
import component.progression.InventoryComponent;
import component.actor.NameComponent;
import component.actor.PlayerComponent;
import component.world.PositionComponent;
import component.progression.ProgressionComponent;
import component.combat.ProjectileEmitterComponent;
import component.progression.QuestComponent;
import component.world.SolidComponent;
import component.combat.StatsComponent;
import component.world.VelocityComponent;
import data.DataRegistry;
import data.PlayerData;
import progression.DerivedStatsSnapshot;
import progression.ProgressionCalculator;
import ecs.EcsWorld;
import java.util.UUID;

final class PlayerPrefabBuilder {
    private PlayerPrefabBuilder() {
    }

    static int create(EcsWorld world, PlayerData data, DataRegistry registry, int tileSize) {
        int entity = world.createEntity();
        ProgressionComponent progression = new ProgressionComponent();
        progression.characterId = "character-" + UUID.randomUUID().toString();
        progression.classId = data.classId();
        progression.level = data.startingLevel();
        EquipmentComponent equipment = starterEquipment(data.classId());
        DerivedStatsSnapshot snapshot = ProgressionCalculator.snapshot(
                registry.rpgClass(data.classId()),
                registry.progressionRules(),
                progression,
                equipment);
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
        world.add(entity, new StatsComponent(
                Math.max(1, (int) Math.round(snapshot.movementSpeed() * 2.0)),
                Math.max(1, (int) Math.round(snapshot.attack())),
                Math.max(0, (int) Math.round(snapshot.defense()))));
        world.add(entity, new HealthComponent(snapshot.hp(), snapshot.hp()));
        world.add(entity, new AttackComponent(
                Math.max(1, (int) Math.round(snapshot.attack())),
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
        world.add(entity, equipment);
        world.add(entity, progression);
        world.add(entity, new SolidComponent(data.flags().solid()));

        if (data.flags().cameraTarget()) {
            world.add(entity, new CameraTargetComponent());
        }
        return entity;
    }

    private static EquipmentComponent starterEquipment(String classId) {
        EquipmentComponent equipment = new EquipmentComponent();
        if ("warrior".equals(classId)) {
            equipment.weaponItemId = "starter_sword";
            equipment.offhandItemId = "starter_shield";
            equipment.bootsItemId = "starter_boots";
        }
        return equipment;
    }
}
