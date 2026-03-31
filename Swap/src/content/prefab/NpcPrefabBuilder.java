package content.prefab;

import component.ColliderComponent;
import component.DialogueComponent;
import component.FactionComponent;
import component.NameComponent;
import component.NpcComponent;
import component.PositionComponent;
import component.ShopComponent;
import component.SolidComponent;
import component.VelocityComponent;
import data.NpcData;
import ecs.EcsWorld;

final class NpcPrefabBuilder {
    private NpcPrefabBuilder() {
    }

    static int create(EcsWorld world, NpcData data, int x, int y, int tileSize) {
        int entity = world.createEntity();
        world.add(entity, new NpcComponent(data.id()));
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
        world.add(entity, new SolidComponent(data.flags().solid()));
        world.add(entity, new DialogueComponent(data.dialogueLines()));
        if (data.shop() != null) {
            world.add(entity, new ShopComponent());
        }
        return entity;
    }
}
