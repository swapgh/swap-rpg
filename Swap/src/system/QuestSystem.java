package system;

import audio.AudioService;
import component.InventoryComponent;
import component.PlayerComponent;
import component.QuestComponent;
import ecs.EcsSystem;
import ecs.EcsWorld;
import ui.UiState;

public final class QuestSystem implements EcsSystem {
    private final UiState ui;
    private final AudioService audio;

    public QuestSystem(UiState ui, AudioService audio) {
        this.ui = ui;
        this.audio = audio;
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        int player = world.entitiesWith(PlayerComponent.class).get(0);
        QuestComponent quests = world.require(player, QuestComponent.class);
        InventoryComponent inventory = world.require(player, InventoryComponent.class);
        if (inventory.coins > 0 && quests.active.remove("first_coin")) {
            quests.completed.add("first_coin");
            audio.playEffect("quest.complete");
            ui.toast = "Quest completada: primera moneda";
            ui.toastTicks = 120;
        }
        if (quests.active.remove("first_kill")) {
            quests.completed.add("first_kill");
            audio.playEffect("quest.complete");
            ui.toast = "Quest completada: primera victoria";
            ui.toastTicks = 120;
        }
    }
}
