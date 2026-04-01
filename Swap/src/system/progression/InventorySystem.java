package system.progression;

import java.awt.event.KeyEvent;

import app.KeyboardState;
import component.actor.InputComponent;
import component.progression.InventoryComponent;
import component.actor.PlayerComponent;
import ecs.EcsSystem;
import ecs.EcsWorld;
import state.GameMode;
import ui.runtime.UiState;
import ui.viewmodel.InventoryViewModel;

public final class InventorySystem implements EcsSystem {
    private static final int INVENTORY_COLUMNS = 5;

    private final KeyboardState keyboard;
    private final UiState ui;

    public InventorySystem(KeyboardState keyboard, UiState ui) {
        this.keyboard = keyboard;
        this.ui = ui;
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        if (ui.mode == GameMode.SHOP || ui.mode == GameMode.LOOT || ui.mode == GameMode.DIALOGUE || ui.mode == GameMode.OPTIONS) {
            return;
        }

        for (int entity : world.entitiesWith(PlayerComponent.class, InputComponent.class, InventoryComponent.class)) {
            InputComponent input = world.require(entity, InputComponent.class);
            InventoryComponent inventory = world.require(entity, InventoryComponent.class);
            if (!input.inventoryPressed) {
                if (ui.inventoryVisible) {
                    updateSelection(InventoryViewModel.from(inventory).occupiedSlots());
                }
                return;
            }
            if (ui.inventoryVisible) {
                ui.inventoryVisible = false;
                ui.mode = ui.characterVisible ? GameMode.CHARACTER : GameMode.PLAY;
            } else if (ui.mode == GameMode.PLAY || ui.characterVisible) {
                ui.inventoryVisible = true;
                ui.mode = GameMode.INVENTORY;
                ui.inventorySelectedIndex = 0;
                updateSelection(InventoryViewModel.from(inventory).occupiedSlots());
            } else if (ui.mode == GameMode.INVENTORY) {
                ui.inventoryVisible = false;
            }
            return;
        }
    }

    private void updateSelection(int itemCount) {
        if (itemCount <= 0) {
            ui.inventorySelectedIndex = 0;
            return;
        }

        int selected = Math.max(0, Math.min(ui.inventorySelectedIndex, itemCount - 1));
        if (keyboard.consumePressed(KeyEvent.VK_A) || keyboard.consumePressed(KeyEvent.VK_LEFT)) {
            selected = Math.max(0, selected - 1);
        }
        if (keyboard.consumePressed(KeyEvent.VK_D) || keyboard.consumePressed(KeyEvent.VK_RIGHT)) {
            selected = Math.min(itemCount - 1, selected + 1);
        }
        if (keyboard.consumePressed(KeyEvent.VK_W) || keyboard.consumePressed(KeyEvent.VK_UP)) {
            selected = Math.max(0, selected - INVENTORY_COLUMNS);
        }
        if (keyboard.consumePressed(KeyEvent.VK_S) || keyboard.consumePressed(KeyEvent.VK_DOWN)) {
            selected = Math.min(itemCount - 1, selected + INVENTORY_COLUMNS);
        }
        ui.inventorySelectedIndex = selected;
    }
}
