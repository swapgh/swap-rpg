package system.progression;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import audio.AudioService;
import component.ChestComponent;
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
import component.ShopComponent;
import component.SpriteComponent;
import component.WorldObjectComponent;
import component.WorldTimeComponent;
import data.DataRegistry;
import ecs.EcsSystem;
import ecs.EcsWorld;
import state.GameMode;
import ui.runtime.UiState;
import ui.text.UiText;
import util.CollisionUtil;
import util.Direction;

public final class InteractionSystem implements EcsSystem {
    private final UiState ui;
    private final AudioService audio;
    private final int tileSize;
    private final DataRegistry data;

    public InteractionSystem(UiState ui, AudioService audio, int tileSize, DataRegistry data) {
        this.ui = ui;
        this.audio = audio;
        this.tileSize = tileSize;
        this.data = data;
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
            if (world.has(entity, ChestComponent.class)) {
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
        boolean dayPhase = isDay(world);

        for (int npc : world.entitiesWith(NpcComponent.class, DialogueComponent.class, PositionComponent.class, ColliderComponent.class)) {
            Rectangle npcRect = CollisionUtil.rect(world.require(npc, PositionComponent.class),
                    world.require(npc, ColliderComponent.class));
            if (interactRect.intersects(npcRect)) {
                ui.contextHint = world.has(npc, ShopComponent.class) ? UiText.WORLD_HINT_OPEN_SHOP : UiText.WORLD_HINT_TALK;
                if (!input.interactPressed) {
                    return;
                }
                if (world.has(npc, ShopComponent.class)) {
                    ui.mode = GameMode.SHOP;
                    ui.shopNpcEntity = npc;
                    ui.shopSpeaker = world.require(npc, NameComponent.class).value;
                    ui.shopStatusMessage = "";
                    ui.shopSelectedIndex = 0;
                    return;
                }
                audio.playEffect("dialogue.open");
                NpcComponent npcComponent = world.require(npc, NpcComponent.class);
                ui.mode = GameMode.DIALOGUE;
                ui.dialogueSpeaker = world.require(npc, NameComponent.class).value;
                ui.dialogueLines = data.npc(npcComponent.npcType).dialogueForPhase(dayPhase);
                completeNpcTimeQuest(quests, npcComponent.npcType, dayPhase);
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
            WorldObjectComponent object = world.require(entity, WorldObjectComponent.class);
            ui.contextHint = object.interactionHint;
            if (!input.interactPressed) {
                return;
            }
            if (door.locked && inventory.itemIds.contains(door.requiredItemId)) {
                inventory.itemIds.remove(door.requiredItemId);
                world.destroyEntity(entity);
                audio.playEffect(object.successAudioId);
                ui.pushToast(object.successToast, 120);
            } else {
                audio.playEffect(object.failureAudioId);
                ui.pushToast(object.failureToast, 120);
            }
            return;
        }

        for (int entity : world.entitiesWith(CollectibleComponent.class, ChestComponent.class, WorldObjectComponent.class,
                PositionComponent.class, ColliderComponent.class)) {
            Rectangle rect = CollisionUtil.rect(world.require(entity, PositionComponent.class),
                    world.require(entity, ColliderComponent.class));
            if (!interactRect.intersects(rect)) {
                continue;
            }
            WorldObjectComponent object = world.require(entity, WorldObjectComponent.class);
            ui.contextHint = object.interactionHint;
            if (!input.interactPressed) {
                return;
            }
            applyCollectible(inventory, quests, world.require(entity, CollectibleComponent.class));
            audio.playEffect(object.successAudioId);
            ui.pushToast(object.successToast, 120);
            world.require(entity, SpriteComponent.class).imageId = world.require(entity, ChestComponent.class).openedSpriteId;
            world.remove(entity, CollectibleComponent.class);
            world.remove(entity, component.SolidComponent.class);
            return;
        }
    }

    private boolean isDay(EcsWorld world) {
        List<Integer> times = world.entitiesWith(WorldTimeComponent.class);
        return times.isEmpty() || world.require(times.get(0), WorldTimeComponent.class).isDay();
    }

    private void completeNpcTimeQuest(QuestComponent quests, String npcType, boolean dayPhase) {
        String questId = data.worldPhase().visitQuestForNpc(npcType, dayPhase);
        if (questId == null || !quests.active.remove(questId)) {
            return;
        }
        quests.completed.add(questId);
        audio.playEffect("quest.complete");
        ui.pushToast(data.quest(questId).completionToast(), 120);
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
            quests.active.add(data.questCatalog().firstCoinQuestId());
        } else {
            for (int i = 0; i < collectible.amount; i++) {
                inventory.itemIds.add(collectible.itemId);
            }
        }
    }
}
