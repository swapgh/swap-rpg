package debug;

import audio.AudioService;
import component.actor.FacingComponent;
import component.actor.InputComponent;
import component.progression.InventoryComponent;
import component.render.SpriteComponent;
import component.world.PositionComponent;
import content.prefab.PrefabFactory;
import data.DataRegistry;
import ecs.EcsWorld;
import system.progression.LootSystem;
import state.GameMode;
import ui.runtime.UiState;
import util.Direction;

public final class ChestLootSmokeTest {
    private static final int TILE_SIZE = 48;

    private ChestLootSmokeTest() {
    }

    public static void main(String[] args) {
        DataRegistry data = DataRegistry.loadDefaults();
        EcsWorld world = new EcsWorld();
        UiState ui = new UiState();
        ui.mode = GameMode.PLAY;
        AudioService audio = new AudioService();
        LootSystem lootSystem = new LootSystem(new app.KeyboardState(), ui, audio, TILE_SIZE, data);

        int player = PrefabFactory.createPlayer(world, data.player("hero"), data, TILE_SIZE);
        int chest = PrefabFactory.createWorldObject(world, data.worldObject("chest"), TILE_SIZE * 2, TILE_SIZE * 2, TILE_SIZE);

        PositionComponent playerPosition = world.require(player, PositionComponent.class);
        playerPosition.x = TILE_SIZE * 2;
        playerPosition.y = TILE_SIZE * 2;
        world.require(player, FacingComponent.class).direction = Direction.DOWN;
        world.require(player, InputComponent.class).interactPressed = true;

        InventoryComponent inventory = world.require(player, InventoryComponent.class);
        int coinsBefore = inventory.coins;

        lootSystem.update(world, 1.0 / 60.0);

        if (ui.mode != GameMode.LOOT) {
            throw new IllegalStateException("Chest did not open loot HUD.");
        }
        if (inventory.coins != coinsBefore) {
            throw new IllegalStateException("Chest granted loot before looting window interaction.");
        }
        if (!world.has(chest, component.progression.CollectibleComponent.class)) {
            throw new IllegalStateException("Chest loot vanished before the player looted it.");
        }

        world.require(player, InputComponent.class).interactPressed = false;
        ui.mode = GameMode.LOOT;
        ui.lootEntity = chest;
        ui.lootSourceName = "Chest";
        ui.lootStatusMessage = "";
        audio.setEnabled(false);

        component.progression.CollectibleComponent collectible = world.require(chest, component.progression.CollectibleComponent.class);
        inventory.coins += collectible.amount;
        world.require(player, component.progression.QuestComponent.class).activate(data.questCatalog().firstCoinQuestId());
        world.require(player, component.progression.ProgressionComponent.class).dirtySync = true;
        world.remove(chest, component.progression.CollectibleComponent.class);

        if (inventory.coins != coinsBefore + 5) {
            throw new IllegalStateException("Chest did not grant 5 coins after looting. coins=" + inventory.coins);
        }
        if (world.has(chest, component.progression.CollectibleComponent.class)) {
            throw new IllegalStateException("Chest still has CollectibleComponent after opening.");
        }
        if (world.has(chest, component.world.SolidComponent.class)) {
            throw new IllegalStateException("Chest is still solid after opening.");
        }
        String spriteId = world.require(chest, SpriteComponent.class).imageId;
        if (!"object.chestOpen".equals(spriteId)) {
            throw new IllegalStateException("Chest sprite did not switch to opened state: " + spriteId);
        }

        System.out.println("Chest loot smoke test passed.");
    }
}
