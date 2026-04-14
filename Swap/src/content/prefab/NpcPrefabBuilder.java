package content.prefab;

import component.world.ColliderComponent;
import component.character.DialogueComponent;
import component.combat.FactionComponent;
import component.character.NameComponent;
import component.character.NpcComponent;
import component.world.PositionComponent;
import component.progression.ShopComponent;
import component.world.SolidComponent;
import component.world.VelocityComponent;
import data.NpcData;
import ecs.EcsWorld;
import ui.text.ContentText;

final class NpcPrefabBuilder {
    private NpcPrefabBuilder() {
    }

    static int create(EcsWorld world, NpcData data, int x, int y, int tileSize) {
        int entity = world.createEntity();
        world.add(entity, new NpcComponent(data.id()));
        world.add(entity, new FactionComponent(data.faction()));
        world.add(entity, new NameComponent(ContentText.text(data.nameKey())));
        world.add(entity, new PositionComponent(x, y));
        world.add(entity, new VelocityComponent());
        PrefabVisualSupport.addAnimatedSprite(world, entity, data.visual(), tileSize);
        world.add(entity, new ColliderComponent(
                data.collider().offsetX(),
                data.collider().offsetY(),
                data.collider().width(),
                data.collider().height()));
        world.add(entity, new SolidComponent(data.flags().solid()));
        world.add(entity, new DialogueComponent(ContentText.lines(data.dialogueKeysForPhase(true))));
        if (data.shop() != null) {
            world.add(entity, new ShopComponent());
        }
        return entity;
    }
}
