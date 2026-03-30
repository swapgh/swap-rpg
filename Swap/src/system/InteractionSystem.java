package system;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import audio.AudioService;
import component.CollectibleComponent;
import component.ColliderComponent;
import component.DialogueComponent;
import component.DoorComponent;
import component.FacingComponent;
import component.InputComponent;
import component.InventoryComponent;
import component.NameComponent;
import component.NpcComponent;
import component.PlayerComponent;
import component.PositionComponent;
import component.QuestComponent;
import component.SpriteComponent;
import ecs.EcsSystem;
import ecs.EcsWorld;
import state.GameMode;
import ui.UiState;
import ui.UiText;
import util.CollisionUtil;
import util.Direction;

public final class InteractionSystem implements EcsSystem {
    private final UiState ui;
    private final AudioService audio;
    private final int tileSize;

    public InteractionSystem(UiState ui, AudioService audio, int tileSize) {
        this.ui = ui;
        this.audio = audio;
        this.tileSize = tileSize;
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        ui.contextHint = "";

        int player = world.entitiesWith(PlayerComponent.class).get(0);
        PositionComponent playerPos = world.require(player, PositionComponent.class);
        ColliderComponent playerCollider = world.require(player, ColliderComponent.class);
        InventoryComponent inventory = world.require(player, InventoryComponent.class);
        QuestComponent quests = world.require(player, QuestComponent.class);
        Rectangle playerRect = CollisionUtil.rect(playerPos, playerCollider);

        List<Integer> picked = new ArrayList<>();
        for (int entity : world.entitiesWith(CollectibleComponent.class, PositionComponent.class, ColliderComponent.class)) {
            Rectangle itemRect = CollisionUtil.rect(world.require(entity, PositionComponent.class),
                    world.require(entity, ColliderComponent.class));
            if (!playerRect.intersects(itemRect)) {
                continue;
            }
            CollectibleComponent collectible = world.require(entity, CollectibleComponent.class);
            String itemId = collectible.itemId;
            if (world.has(entity, DoorComponent.class)) {
                continue;
            }
            if (world.has(entity, SpriteComponent.class) && world.has(entity, NameComponent.class)
                    && "Chest".equals(world.require(entity, NameComponent.class).value)) {
                continue;
            }
            applyCollectible(inventory, quests, collectible);
            audio.playEffect("coin".equals(itemId) ? "pickup.coin" : "pickup.key");
            ui.pushToast(UiText.itemPickedUp(itemId), 120);
            picked.add(entity);
        }
        for (int entity : picked) {
            world.destroyEntity(entity);
        }

        if (ui.mode == GameMode.DIALOGUE) {
            ui.contextHint = UiText.WORLD_HINT_CONTINUE;
            InputComponent input = world.require(player, InputComponent.class);
            if (!input.interactPressed) {
                return;
            }
            ui.mode = GameMode.PLAY;
            ui.dialogueSpeaker = "";
            ui.dialogueLines = new String[0];
            return;
        }

        Rectangle interactRect = interactionRect(playerPos, playerCollider, world.require(player, FacingComponent.class).direction);
        InputComponent input = world.require(player, InputComponent.class);

        for (int npc : world.entitiesWith(NpcComponent.class, DialogueComponent.class, PositionComponent.class, ColliderComponent.class)) {
            Rectangle npcRect = CollisionUtil.rect(world.require(npc, PositionComponent.class),
                    world.require(npc, ColliderComponent.class));
            if (interactRect.intersects(npcRect)) {
                ui.contextHint = UiText.WORLD_HINT_TALK;
                if (!input.interactPressed) {
                    return;
                }
                audio.playEffect("dialogue.open");
                ui.mode = GameMode.DIALOGUE;
                ui.dialogueSpeaker = world.require(npc, NameComponent.class).value;
                ui.dialogueLines = world.require(npc, DialogueComponent.class).lines;
                return;
            }
        }

        for (int entity : world.entitiesWith(DoorComponent.class, PositionComponent.class, ColliderComponent.class)) {
            Rectangle rect = CollisionUtil.rect(world.require(entity, PositionComponent.class),
                    world.require(entity, ColliderComponent.class));
            if (!interactRect.intersects(rect)) {
                continue;
            }
            DoorComponent door = world.require(entity, DoorComponent.class);
            ui.contextHint = UiText.WORLD_HINT_OPEN_DOOR;
            if (!input.interactPressed) {
                return;
            }
            if (door.locked && inventory.itemIds.contains(door.requiredItemId)) {
                inventory.itemIds.remove(door.requiredItemId);
                world.destroyEntity(entity);
                audio.playEffect("door.open");
                ui.pushToast(UiText.STATUS_DOOR_OPENED, 120);
            } else {
                audio.playEffect("door.locked");
                ui.pushToast(UiText.STATUS_MISSING_KEY, 120);
            }
            return;
        }

        for (int entity : world.entitiesWith(CollectibleComponent.class, NameComponent.class, PositionComponent.class, ColliderComponent.class)) {
            if (!"Chest".equals(world.require(entity, NameComponent.class).value)) {
                continue;
            }
            Rectangle rect = CollisionUtil.rect(world.require(entity, PositionComponent.class),
                    world.require(entity, ColliderComponent.class));
            if (!interactRect.intersects(rect)) {
                continue;
            }
            ui.contextHint = UiText.WORLD_HINT_OPEN_CHEST;
            if (!input.interactPressed) {
                return;
            }
            applyCollectible(inventory, quests, world.require(entity, CollectibleComponent.class));
            audio.playEffect("pickup.coin");
            ui.pushToast(UiText.STATUS_CHEST_OPENED, 120);
            world.require(entity, SpriteComponent.class).imageId = "object.chestOpen";
            world.remove(entity, CollectibleComponent.class);
            world.remove(entity, component.SolidComponent.class);
            return;
        }
    }

    private Rectangle interactionRect(PositionComponent pos, ColliderComponent collider, Direction direction) {
        int baseX = (int) pos.x + collider.offsetX;
        int baseY = (int) pos.y + collider.offsetY;
        return switch (direction) {
        case UP -> new Rectangle(baseX, baseY - tileSize / 2, collider.width, collider.height + tileSize / 2);
        case DOWN -> new Rectangle(baseX, baseY, collider.width, collider.height + tileSize / 2);
        case LEFT -> new Rectangle(baseX - tileSize / 2, baseY, collider.width + tileSize / 2, collider.height);
        case RIGHT -> new Rectangle(baseX, baseY, collider.width + tileSize / 2, collider.height);
        };
    }

    private void applyCollectible(InventoryComponent inventory, QuestComponent quests, CollectibleComponent collectible) {
        if ("coin".equals(collectible.itemId)) {
            inventory.coins += collectible.amount;
            quests.active.add("first_coin");
        } else {
            for (int i = 0; i < collectible.amount; i++) {
                inventory.itemIds.add(collectible.itemId);
            }
        }
    }
}
