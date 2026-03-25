package system;

import component.InputComponent;
import component.PlayerComponent;
import ecs.EcsSystem;
import ecs.EcsWorld;
import state.GameMode;
import ui.UiState;

public final class InventorySystem implements EcsSystem {
    private final UiState ui;

    public InventorySystem(UiState ui) {
        this.ui = ui;
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        for (int entity : world.entitiesWith(PlayerComponent.class, InputComponent.class)) {
            InputComponent input = world.require(entity, InputComponent.class);
            if (!input.inventoryPressed) {
                return;
            }
            if (ui.mode == GameMode.INVENTORY) {
                ui.mode = GameMode.PLAY;
                ui.inventoryVisible = false;
            } else if (ui.mode == GameMode.PLAY) {
                ui.mode = GameMode.INVENTORY;
                ui.inventoryVisible = true;
            }
            return;
        }
    }
}
