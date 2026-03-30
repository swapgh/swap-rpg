package content;

import component.AnimationComponent;
import component.AnimationSetComponent;
import component.AttackComponent;
import component.CameraTargetComponent;
import component.ColliderComponent;
import component.CollectibleComponent;
import component.DialogueComponent;
import component.DoorComponent;
import component.EnemyComponent;
import component.FactionComponent;
import component.FacingComponent;
import component.HealthComponent;
import component.InputComponent;
import component.InventoryComponent;
import component.NameComponent;
import component.NpcComponent;
import component.PlayerComponent;
import component.PositionComponent;
import component.ProgressionComponent;
import component.ProjectileEmitterComponent;
import component.QuestComponent;
import component.SolidComponent;
import component.SpriteComponent;
import component.StatsComponent;
import component.VelocityComponent;
import component.WanderAiComponent;
import data.EnemyData;
import data.NpcData;
import data.PlayerData;
import ecs.EcsWorld;
import util.Direction;

/** Traduce data externa a entidades ECS listas para usarse. */
public final class PrefabFactory {
    private PrefabFactory() {
    }

    /** Crea el jugador a partir de PlayerData. */
    public static int createPlayer(EcsWorld world, PlayerData data, int tileSize) {
        int entity = world.createEntity();
        world.add(entity, new PlayerComponent(data.id()));
        world.add(entity, new FactionComponent(data.faction()));
        world.add(entity, new NameComponent(data.name()));
        world.add(entity, new PositionComponent(data.spawn().tileX() * tileSize, data.spawn().tileY() * tileSize));
        world.add(entity, new VelocityComponent());
        Direction initialFacing = direction(data.visual().initialFacing());
        world.add(entity, new FacingComponent(initialFacing));

        world.add(entity, new SpriteComponent(initialSpriteId(data.visual()), tileSize, tileSize, data.visual().layer()));
        world.add(entity, new AnimationComponent(data.visual().idleBase() + "." + directionSuffix(initialFacing),
                data.visual().animationFrameTicks()));
        world.add(entity, new AnimationSetComponent(data.visual().idleBase(), data.visual().walkBase(),
                blankToNull(data.visual().attackBase())));

        world.add(entity, new ColliderComponent(data.collider().offsetX(), data.collider().offsetY(),
                data.collider().width(), data.collider().height()));
        world.add(entity, new StatsComponent(data.stats().speed(), data.stats().attack(), data.stats().defense()));
        world.add(entity, new HealthComponent(data.stats().health(), data.stats().health()));

        world.add(entity, new AttackComponent(data.attack().damage(),
                scaledSize(tileSize, data.attack().rangeScale()), data.attack().cooldownTicks()));
        if (data.projectile().enabled()) {
            world.add(entity, new ProjectileEmitterComponent(data.projectile().spriteId(), data.projectile().speed(),
                    data.projectile().damage(), data.projectile().lifetimeTicks(), data.projectile().cooldownTicks(),
                    scaledSize(tileSize, data.projectile().sizeScale()), data.projectile().targetFaction(),
                    data.projectile().playerTriggered(), data.projectile().aimAtPlayer()));
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

    /** Crea un enemigo a partir de EnemyData. */
    public static int createEnemy(EcsWorld world, EnemyData data, int x, int y, int tileSize) {
        int entity = world.createEntity();
        world.add(entity, new EnemyComponent(data.id()));
        world.add(entity, new FactionComponent(data.faction()));
        world.add(entity, new NameComponent(data.name()));
        world.add(entity, new PositionComponent(x, y));
        world.add(entity, new VelocityComponent());
        Direction initialFacing = direction(data.visual().initialFacing());
        world.add(entity, new FacingComponent(initialFacing));
        world.add(entity, new SpriteComponent(initialSpriteId(data.visual()), tileSize, tileSize, data.visual().layer()));
        world.add(entity, new AnimationComponent(data.visual().idleBase() + "." + directionSuffix(initialFacing),
                data.visual().animationFrameTicks()));
        world.add(entity, new AnimationSetComponent(data.visual().idleBase(), data.visual().walkBase(),
                blankToNull(data.visual().attackBase())));
        world.add(entity,
                new ColliderComponent(data.collider().offsetX(), data.collider().offsetY(), data.collider().width(),
                        data.collider().height()));
        world.add(entity, new StatsComponent(data.stats().speed(), data.stats().attack(), data.stats().defense()));
        world.add(entity, new HealthComponent(data.stats().health(), data.stats().health()));
        world.add(entity, new SolidComponent(data.flags().solid()));

        if (data.wander()) {
            world.add(entity, new WanderAiComponent(60, entity * 31));
        }

        if (data.projectile().enabled()) {
            world.add(entity,
                    new ProjectileEmitterComponent(data.projectile().spriteId(), data.projectile().speed(),
                            data.projectile().damage(), data.projectile().lifetimeTicks(),
                            data.projectile().cooldownTicks(), scaledSize(tileSize, data.projectile().sizeScale()),
                            data.projectile().targetFaction(), data.projectile().playerTriggered(),
                            data.projectile().aimAtPlayer()));
        }
        return entity;
    }

    /** Crea un NPC a partir de NpcData. */
    public static int createNpc(EcsWorld world, NpcData data, int x, int y, int tileSize) {
        int entity = world.createEntity();
        world.add(entity, new NpcComponent(data.id()));
        world.add(entity, new FactionComponent(data.faction()));
        world.add(entity, new NameComponent(data.name()));
        world.add(entity, new PositionComponent(x, y));
        world.add(entity, new VelocityComponent());
        Direction initialFacing = direction(data.visual().initialFacing());
        world.add(entity, new FacingComponent(initialFacing));
        world.add(entity, new SpriteComponent(initialSpriteId(data.visual()), tileSize, tileSize, data.visual().layer()));
        world.add(entity, new AnimationComponent(data.visual().idleBase() + "." + directionSuffix(initialFacing),
                data.visual().animationFrameTicks()));
        world.add(entity, new AnimationSetComponent(data.visual().idleBase(), data.visual().walkBase(),
                blankToNull(data.visual().attackBase())));
        world.add(entity,
                new ColliderComponent(data.collider().offsetX(), data.collider().offsetY(), data.collider().width(),
                        data.collider().height()));
        world.add(entity, new SolidComponent(data.flags().solid()));
        world.add(entity, new DialogueComponent(data.dialogueLines()));
        return entity;
    }

    public static int createCoin(EcsWorld world, int x, int y, int tileSize) {
        int entity = world.createEntity();
        world.add(entity, new NameComponent("Bronze Coin"));
        world.add(entity, new PositionComponent(x, y));
        world.add(entity, new SpriteComponent("object.coin", tileSize, tileSize, 10));
        world.add(entity, new ColliderComponent(10, 10, tileSize - 20, tileSize - 20));
        world.add(entity, new CollectibleComponent("coin", 1));
        return entity;
    }

    public static int createKey(EcsWorld world, int x, int y, int tileSize) {
        int entity = world.createEntity();
        world.add(entity, new NameComponent("Dungeon Key"));
        world.add(entity, new PositionComponent(x, y));
        world.add(entity, new SpriteComponent("object.key", tileSize, tileSize, 10));
        world.add(entity, new ColliderComponent(10, 10, tileSize - 20, tileSize - 20));
        world.add(entity, new CollectibleComponent("key", 1));
        return entity;
    }

    public static int createDoor(EcsWorld world, int x, int y, int tileSize) {
        int entity = world.createEntity();
        world.add(entity, new NameComponent("Wooden Door"));
        world.add(entity, new PositionComponent(x, y));
        world.add(entity, new SpriteComponent("object.door", tileSize, tileSize, 12));
        world.add(entity, new ColliderComponent(6, 6, tileSize - 12, tileSize - 6));
        world.add(entity, new SolidComponent(true));
        world.add(entity, new DoorComponent(true, "key"));
        return entity;
    }

    public static int createChest(EcsWorld world, int x, int y, int tileSize) {
        int entity = world.createEntity();
        world.add(entity, new NameComponent("Chest"));
        world.add(entity, new PositionComponent(x, y));
        world.add(entity, new SpriteComponent("object.chest", tileSize, tileSize, 12));
        world.add(entity, new ColliderComponent(6, 10, tileSize - 12, tileSize - 10));
        world.add(entity, new SolidComponent(true));
        world.add(entity, new CollectibleComponent("coin", 5));
        return entity;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static Direction direction(String value) {
        return Direction.valueOf(value.toUpperCase());
    }

    private static String directionSuffix(Direction direction) {
        return direction.name().toLowerCase();
    }

    /** Convierte el clip idle base en el id del primer frame registrado en assets. */
    private static String initialSpriteId(data.VisualData visual) {
        String[] parts = visual.idleBase().split("\\.");
        if (parts.length >= 2) {
            StringBuilder base = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                if (i > 0) {
                    base.append('.');
                }
                base.append(parts[i]);
            }
            return base + "." + directionSuffix(direction(visual.initialFacing())) + "." + visual.initialFrame();
        }
        return visual.idleBase() + "." + directionSuffix(direction(visual.initialFacing())) + "." + visual.initialFrame();
    }

    private static int scaledSize(int tileSize, double scale) {
        return Math.max(1, (int) Math.round(tileSize * scale));
    }
}
