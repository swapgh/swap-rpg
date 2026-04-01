package content.prefab;

import component.world.ChestComponent;
import component.world.ColliderComponent;
import component.progression.CollectibleComponent;
import component.world.DoorComponent;
import component.actor.NameComponent;
import component.world.PositionComponent;
import component.world.SolidComponent;
import component.render.SpriteComponent;
import component.world.WorldObjectComponent;
import data.world.WorldObjectData;
import ecs.EcsWorld;
import ui.text.ContentText;

final class WorldObjectPrefabBuilder {
    private WorldObjectPrefabBuilder() {
    }

        static int create(EcsWorld world, WorldObjectData data, int x, int y, int tileSize) {
        int entity = world.createEntity();
        world.add(entity, new NameComponent(ContentText.text(data.nameKey())));
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
                    data.interaction().interactionHintKey(),
                    data.interaction().successToastKey(),
                    data.interaction().failureToastKey(),
                    data.interaction().successAudioId(),
                    data.interaction().failureAudioId()));
        }
        return entity;
    }
}
