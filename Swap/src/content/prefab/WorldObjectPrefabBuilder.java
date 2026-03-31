package content.prefab;

import component.ChestComponent;
import component.ColliderComponent;
import component.CollectibleComponent;
import component.DoorComponent;
import component.NameComponent;
import component.PositionComponent;
import component.SolidComponent;
import component.SpriteComponent;
import component.WorldObjectComponent;
import data.world.WorldObjectData;
import ecs.EcsWorld;

final class WorldObjectPrefabBuilder {
    private WorldObjectPrefabBuilder() {
    }

    static int create(EcsWorld world, WorldObjectData data, int x, int y, int tileSize) {
        int entity = world.createEntity();
        world.add(entity, new NameComponent(data.name()));
        world.add(entity, new PositionComponent(x, y));
        world.add(entity, new SpriteComponent(data.spriteId(), tileSize, tileSize, data.layer()));
        world.add(entity, new ColliderComponent(
                data.collider().offsetX(),
                data.collider().offsetY(),
                data.collider().width(),
                data.collider().height()));
        if (data.solid()) {
            world.add(entity, new SolidComponent(true));
        }
        if (data.collectible() != null) {
            world.add(entity, new CollectibleComponent(data.collectible().itemId(), data.collectible().amount()));
        }
        if (data.door() != null) {
            world.add(entity, new DoorComponent(data.door().locked(), data.door().requiredItemId()));
        }
        if (data.chest() != null) {
            world.add(entity, new ChestComponent(data.chest().openedSpriteId()));
        }
        if (data.interaction() != null) {
            world.add(entity, new WorldObjectComponent(
                    data.id(),
                    data.interaction().interactionHint(),
                    data.interaction().successToast(),
                    data.interaction().failureToast(),
                    data.interaction().successAudioId(),
                    data.interaction().failureAudioId()));
        }
        return entity;
    }
}
