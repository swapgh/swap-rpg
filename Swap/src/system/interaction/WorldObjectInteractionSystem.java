package system.interaction;

import java.awt.Rectangle;

import audio.AudioService;
import component.character.InputComponent;
import component.character.PlayerComponent;
import component.progression.InventoryComponent;
import component.world.ColliderComponent;
import component.world.DoorComponent;
import component.world.PositionComponent;
import component.world.WorldObjectComponent;
import ecs.EcsWorld;
import ui.state.UiState;
import ui.text.ContentText;
import util.CollisionUtil;

final class WorldObjectInteractionSystem {
    private final UiState ui;
    private final AudioService audio;
    private final InteractionSupport support;

    WorldObjectInteractionSystem(UiState ui, AudioService audio, InteractionSupport support) {
        this.ui = ui;
        this.audio = audio;
        this.support = support;
    }

    boolean handle(EcsWorld world) {
        int player = world.entitiesWith(PlayerComponent.class).get(0);
        PositionComponent playerPos = world.require(player, PositionComponent.class);
        ColliderComponent playerCollider = world.require(player, ColliderComponent.class);
        InventoryComponent inventory = world.require(player, InventoryComponent.class);
        InputComponent input = world.require(player, InputComponent.class);
        Rectangle interactRect = support.interactionRect(playerPos, playerCollider, world.require(player, component.character.FacingComponent.class).direction);

        for (int entity : world.entitiesWith(DoorComponent.class, PositionComponent.class, ColliderComponent.class)) {
            Rectangle rect = CollisionUtil.rect(world.require(entity, PositionComponent.class),
                    world.require(entity, ColliderComponent.class));
            if (!interactRect.intersects(rect)) {
                continue;
            }
            DoorComponent door = world.require(entity, DoorComponent.class);
            WorldObjectComponent object = world.require(entity, WorldObjectComponent.class);
            ui.contextHint = ContentText.text(object.interactionHintKey);
            if (!input.interactPressed) {
                return true;
            }
            if (door.locked && inventory.itemIds.contains(door.requiredItemId)) {
                inventory.itemIds.remove(door.requiredItemId);
                world.destroyEntity(entity);
                audio.playEffect(object.successAudioId);
                ui.pushToast(ContentText.text(object.successToastKey), 120);
            } else {
                audio.playEffect(object.failureAudioId);
                ui.pushToast(ContentText.text(object.failureToastKey), 120);
            }
            return true;
        }

        return false;
    }
}
