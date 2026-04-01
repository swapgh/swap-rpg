package system.combat;

import component.world.ColliderComponent;
import component.progression.CollectibleComponent;
import component.combat.HealthComponent;
import component.combat.LootComponent;
import component.world.PositionComponent;
import component.render.SpriteComponent;
import content.catalog.ItemCatalog;
import ecs.EcsSystem;
import ecs.EcsWorld;
import java.util.HashSet;
import java.util.Set;

public final class DropSystem implements EcsSystem {
    private final Set<Integer> processed = new HashSet<>();
    private final int tileSize;

    public DropSystem(int tileSize) {
        this.tileSize = tileSize;
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        for (int entity : world.entitiesWith(HealthComponent.class, LootComponent.class, PositionComponent.class, ColliderComponent.class)) {
            HealthComponent health = world.require(entity, HealthComponent.class);
            if (health.current > 0 || !processed.add(entity)) {
                continue;
            }
            LootComponent loot = world.require(entity, LootComponent.class);
            if (Math.random() > loot.dropChance) {
                continue;
            }

            PositionComponent position = world.require(entity, PositionComponent.class);
            ColliderComponent collider = world.require(entity, ColliderComponent.class);
            int drop = world.createEntity();
            world.add(drop, new PositionComponent(position.x, position.y));
            world.add(drop, new ColliderComponent(collider.offsetX, collider.offsetY, collider.width, collider.height));
            world.add(drop, new CollectibleComponent(loot.itemId, loot.amount));
            world.add(drop, new SpriteComponent(ItemCatalog.get(loot.itemId).iconId(), tileSize, tileSize, 11));
        }
    }
}
