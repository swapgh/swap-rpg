package system.quest;

import audio.AudioService;
import component.character.PlayerComponent;
import component.progression.ProgressionComponent;
import component.progression.QuestComponent;
import data.DataRegistry;
import ecs.EcsSystem;
import ecs.EcsWorld;
import ui.state.UiState;
import ui.text.ContentText;

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
        ProgressionComponent progression = world.require(player, ProgressionComponent.class);
        completeQuestIfActive(quests, progression.enemiesKilled > 0, data.questCatalog().firstKillQuestId());

        for (String questId : quests.activeQuestIds()) {
            String activeHint = ContentText.text(data.quest(questId).activeHintKey());
            if (activeHint != null && !activeHint.isBlank()) {
                ui.contextHint = activeHint;
                break;
            }
        }
    }

    private void completeQuestIfActive(QuestComponent quests, boolean conditionMet, String questId) {
        if (!conditionMet || !quests.complete(questId)) {
            return;
        }
        audio.playEffect("quest.complete");
        ui.pushToast(ContentText.text(data.quest(questId).completionToastKey()), 120);
    }
}
