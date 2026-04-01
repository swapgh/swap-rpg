package system.progression;

import java.awt.event.KeyEvent;

import app.KeyboardState;
import ecs.EcsSystem;
import ecs.EcsWorld;
import state.GameMode;
import ui.runtime.UiState;

public final class CharacterScreenSystem implements EcsSystem {
    private final KeyboardState keyboard;
    private final UiState ui;

    public CharacterScreenSystem(KeyboardState keyboard, UiState ui) {
        this.keyboard = keyboard;
        this.ui = ui;
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        if (ui.mode == GameMode.SHOP || ui.mode == GameMode.LOOT || ui.mode == GameMode.DIALOGUE || ui.mode == GameMode.OPTIONS) {
            return;
        }

        if (ui.characterVisible) {
            if (keyboard.consumePressed(KeyEvent.VK_C) || keyboard.consumePressed(KeyEvent.VK_ESCAPE)
                    || keyboard.consumePressed(KeyEvent.VK_BACK_SPACE)) {
                ui.characterVisible = false;
                ui.mode = ui.inventoryVisible ? GameMode.INVENTORY : GameMode.PLAY;
            }
            return;
        }

        if ((ui.mode == GameMode.PLAY || ui.inventoryVisible) && keyboard.consumePressed(KeyEvent.VK_C)) {
            ui.characterVisible = true;
            if (!ui.inventoryVisible) {
                ui.mode = GameMode.CHARACTER;
            }
        }
    }
}
