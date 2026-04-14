package system.loot;

import audio.AudioService;
import app.input.KeyboardState;
import component.character.NameComponent;
import component.world.ChestComponent;
import component.progression.CollectibleComponent;
import component.world.ColliderComponent;
import component.character.FacingComponent;
import component.character.InputComponent;
import component.progression.InventoryComponent;
import component.character.PlayerComponent;
import component.world.PositionComponent;
import component.progression.ProgressionComponent;
import component.progression.QuestComponent;
import system.inventory.InventoryOps;
import component.render.SpriteComponent;
import component.world.SolidComponent;
import component.world.WorldObjectComponent;
import content.catalog.ItemCatalog;
import ecs.EcsSystem;
import ecs.EcsWorld;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.KeyEvent;
import state.GameMode;
import ui.hud.SidePanelEntry;
import ui.state.UiState;
import ui.text.ContentText;
import ui.text.UiText;
import util.CollisionUtil;
import util.Direction;

public final class LootSystem implements EcsSystem {
    private static final int LOOT_COLUMNS = 3;

    private final KeyboardState keyboard;
    private final UiState ui;
    private final AudioService audio;
    private final int tileSize;
    private final data.DataRegistry data;

    public LootSystem(KeyboardState keyboard, UiState ui, AudioService audio, int tileSize, data.DataRegistry data) {
        this.keyboard = keyboard;
        this.ui = ui;
        this.audio = audio;
        this.tileSize = tileSize;
        this.data = data;
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        int player = world.entitiesWith(PlayerComponent.class).get(0);
        PositionComponent playerPos = world.require(player, PositionComponent.class);
        ColliderComponent playerCollider = world.require(player, ColliderComponent.class);
        InventoryComponent inventory = world.require(player, InventoryComponent.class);
        QuestComponent quests = world.require(player, QuestComponent.class);
        ProgressionComponent progression = world.require(player, ProgressionComponent.class);
        Rectangle playerRect = CollisionUtil.rect(playerPos, playerCollider);

        if (ui.mode == GameMode.PLAY) {
            collectGroundLoot(world, playerRect, inventory, quests, progression);
        }

        if (ui.mode == GameMode.LOOT) {
            updateLootWindow(world, inventory, quests, progression);
            return;
        }

        if (ui.mode != GameMode.PLAY) {
            return;
        }

        InputComponent input = world.require(player, InputComponent.class);
        if (!input.interactPressed) {
            return;
        }

        Rectangle interactRect = interactionRect(playerPos, playerCollider, world.require(player, FacingComponent.class).direction);
        collectChestLoot(world, interactRect, inventory, quests, progression);
    }

    private void collectGroundLoot(EcsWorld world, Rectangle playerRect, InventoryComponent inventory, QuestComponent quests,
            ProgressionComponent progression) {
        List<Integer> picked = new ArrayList<>();
        for (int entity : world.entitiesWith(CollectibleComponent.class, PositionComponent.class, ColliderComponent.class)) {
            if (world.has(entity, ChestComponent.class)) {
                continue;
            }
            Rectangle itemRect = CollisionUtil.rect(world.require(entity, PositionComponent.class),
                    world.require(entity, ColliderComponent.class));
            if (!playerRect.intersects(itemRect)) {
                continue;
            }
            CollectibleComponent collectible = world.require(entity, CollectibleComponent.class);
            InventoryOps.addCollectible(inventory, quests, progression, collectible, data);
            audio.playEffect("coin".equals(collectible.itemId) ? "pickup.coin" : "pickup.key");
            ui.pushToast(UiText.itemPickedUp(collectible.itemId), 120);
            picked.add(entity);
        }
        for (int entity : picked) {
            world.destroyEntity(entity);
        }
    }

    private void collectChestLoot(EcsWorld world, Rectangle interactRect, InventoryComponent inventory, QuestComponent quests,
            ProgressionComponent progression) {
        for (int entity : world.entitiesWith(CollectibleComponent.class, ChestComponent.class, WorldObjectComponent.class,
                PositionComponent.class, ColliderComponent.class)) {
            Rectangle rect = CollisionUtil.rect(world.require(entity, PositionComponent.class),
                    world.require(entity, ColliderComponent.class));
            if (!interactRect.intersects(rect)) {
                continue;
            }
            WorldObjectComponent object = world.require(entity, WorldObjectComponent.class);
            ui.contextHint = ContentText.text(object.interactionHintKey);
            audio.playEffect(object.successAudioId);
            ui.pushToast(ContentText.text(object.successToastKey), 120);
            world.require(entity, SpriteComponent.class).imageId = world.require(entity, ChestComponent.class).openedSpriteId;
            world.remove(entity, SolidComponent.class);
            ui.mode = GameMode.LOOT;
            ui.lootEntity = entity;
            ui.lootSourceName = world.require(entity, NameComponent.class).value;
            ui.lootStatusMessage = "";
            ui.lootSelectedIndex = 0;
            return;
        }
    }

    private void updateLootWindow(EcsWorld world, InventoryComponent inventory, QuestComponent quests,
            ProgressionComponent progression) {
        if (ui.lootEntity < 0 || !world.isAlive(ui.lootEntity) || !world.has(ui.lootEntity, ChestComponent.class)
                || !world.has(ui.lootEntity, WorldObjectComponent.class)) {
            closeLoot();
            return;
        }
        if (!playerCanReachLoot(world)) {
            closeLoot();
            return;
        }

        boolean hasLoot = world.has(ui.lootEntity, CollectibleComponent.class);
        if (!hasLoot) {
            ui.lootStatusMessage = "";
        }

        if (keyboard.consumePressed(KeyEvent.VK_ESCAPE) || keyboard.consumePressed(KeyEvent.VK_BACK_SPACE)
                || keyboard.consumePressed(KeyEvent.VK_I)) {
            closeLoot();
            return;
        }

        if (!hasLoot) {
            return;
        }

        int maxIndex = currentLootEntries(world).size() - 1;
        int selected = Math.max(0, Math.min(ui.lootSelectedIndex, Math.max(0, maxIndex)));
        if (keyboard.consumePressed(KeyEvent.VK_A) || keyboard.consumePressed(KeyEvent.VK_LEFT)) {
            selected = Math.max(0, selected - 1);
        }
        if (keyboard.consumePressed(KeyEvent.VK_D) || keyboard.consumePressed(KeyEvent.VK_RIGHT)) {
            selected = Math.min(maxIndex, selected + 1);
        }
        if (keyboard.consumePressed(KeyEvent.VK_W) || keyboard.consumePressed(KeyEvent.VK_UP)) {
            selected = Math.max(0, selected - LOOT_COLUMNS);
        }
        if (keyboard.consumePressed(KeyEvent.VK_S) || keyboard.consumePressed(KeyEvent.VK_DOWN)) {
            selected = Math.min(maxIndex, selected + LOOT_COLUMNS);
        }
        ui.lootSelectedIndex = selected;

        if (keyboard.consumePressed(KeyEvent.VK_ENTER) || keyboard.consumePressed(KeyEvent.VK_E)) {
            CollectibleComponent collectible = world.require(ui.lootEntity, CollectibleComponent.class);
            InventoryOps.addCollectible(inventory, quests, progression, collectible, data);
            ui.lootStatusMessage = "";
            audio.playEffect("coin".equals(collectible.itemId) ? "pickup.coin" : "pickup.key");
            ui.pushToast(UiText.itemPickedUp(collectible.itemId), 120);
            world.remove(ui.lootEntity, CollectibleComponent.class);
        }
    }

    public List<SidePanelEntry> currentLootEntries(EcsWorld world) {
        if (ui.lootEntity < 0 || !world.isAlive(ui.lootEntity) || !world.has(ui.lootEntity, CollectibleComponent.class)) {
            return List.of();
        }
        CollectibleComponent collectible = world.require(ui.lootEntity, CollectibleComponent.class);
        return List.of(new SidePanelEntry(
                collectible.itemId,
                ItemCatalog.get(collectible.itemId).displayName(),
                UiText.itemCount(collectible.amount)));
    }

    private void closeLoot() {
        ui.mode = GameMode.PLAY;
        ui.lootEntity = -1;
        ui.lootSourceName = "";
        ui.lootStatusMessage = "";
        ui.lootSelectedIndex = 0;
    }

    private boolean playerCanReachLoot(EcsWorld world) {
        List<Integer> players = world.entitiesWith(PlayerComponent.class, PositionComponent.class, ColliderComponent.class,
                FacingComponent.class);
        if (players.isEmpty()) {
            return false;
        }
        int player = players.get(0);
        Rectangle interactRect = interactionRect(
                world.require(player, PositionComponent.class),
                world.require(player, ColliderComponent.class),
                world.require(player, FacingComponent.class).direction);
        Rectangle chestRect = CollisionUtil.rect(
                world.require(ui.lootEntity, PositionComponent.class),
                world.require(ui.lootEntity, ColliderComponent.class));
        return interactRect.intersects(chestRect);
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
}
