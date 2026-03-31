package system.render;

import java.awt.event.KeyEvent;

import app.KeyboardState;
import ecs.EcsSystem;
import ecs.EcsWorld;
import state.GameMode;
import ui.runtime.UiState;

public final class UiOverlaySystem implements EcsSystem {
    private final KeyboardState keyboard;
    private final UiState ui;

    public UiOverlaySystem(KeyboardState keyboard, UiState ui) {
        this.keyboard = keyboard;
        this.ui = ui;
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        if (ui.toastTicks > 0) {
            ui.toastTicks--;
        }
        if (ui.combatToastTicks > 0) {
            ui.combatToastTicks--;
        }
        ui.tickSystemLog();

        if (ui.mode != GameMode.TITLE && keyboard.consumePressed(KeyEvent.VK_L)) {
            ui.systemLogExpanded = !ui.systemLogExpanded;
        }
    }
}
