package system.interaction;

import app.input.KeyboardState;
import audio.AudioService;
import data.DataRegistry;
import ecs.EcsSystem;
import ecs.EcsWorld;
import ui.state.UiState;
import state.GameMode;

public final class InteractionSystem implements EcsSystem {
    private final UiState ui;
    private final NpcInteractionSystem npcInteractions;
    private final WorldObjectInteractionSystem worldObjectInteractions;

    public InteractionSystem(UiState ui, AudioService audio, KeyboardState keyboard, int tileSize, DataRegistry data) {
        this.ui = ui;
        InteractionSupport support = new InteractionSupport(tileSize);
        this.npcInteractions = new NpcInteractionSystem(ui, audio, keyboard, data, support);
        this.worldObjectInteractions = new WorldObjectInteractionSystem(ui, audio, support);
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        if (ui.mode == GameMode.LOOT) {
            return;
        }
        ui.contextHint = "";
        if (npcInteractions.handle(world)) {
            return;
        }
        worldObjectInteractions.handle(world);
    }
}
