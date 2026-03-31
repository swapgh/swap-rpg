package system.progression;

import audio.AudioService;
import component.InventoryComponent;
import component.PlayerComponent;
import component.QuestComponent;
import data.DataRegistry;
import ecs.EcsSystem;
import ecs.EcsWorld;
import ui.runtime.UiState;

public final class QuestSystem implements EcsSystem {
    private final UiState ui;
    private final AudioService audio;
    private final DataRegistry data;

    public QuestSystem(UiState ui, AudioService audio, DataRegistry data) {
        this.ui = ui;
        this.audio = audio;
        this.data = data;
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        int player = world.entitiesWith(PlayerComponent.class).get(0);
        QuestComponent quests = world.require(player, QuestComponent.class);
        InventoryComponent inventory = world.require(player, InventoryComponent.class);
        completeQuestIfActive(quests, inventory.coins > 0, data.questCatalog().firstCoinQuestId());
        completeQuestIfActive(quests, true, data.questCatalog().firstKillQuestId());

        for (String questId : quests.active) {
            if (quests.completed.contains(questId)) {
                continue;
            }
            String activeHint = data.quest(questId).activeHint();
            if (activeHint != null && !activeHint.isBlank()) {
                ui.contextHint = activeHint;
                break;
            }
        }
    }

    private void completeQuestIfActive(QuestComponent quests, boolean conditionMet, String questId) {
        if (!conditionMet || !quests.active.remove(questId)) {
            return;
        }
        quests.completed.add(questId);
        audio.playEffect("quest.complete");
        ui.pushToast(data.quest(questId).completionToast(), 120);
    }
}
