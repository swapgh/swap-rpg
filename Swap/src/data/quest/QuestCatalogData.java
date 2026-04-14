package data.quest;

import java.util.Map;

public record QuestCatalogData(
        Map<String, QuestData> quests,
        String firstCoinQuestId,
        String firstKillQuestId) {

    public QuestData quest(String id) {
        QuestData quest = quests.get(id);
        if (quest == null) {
            throw new IllegalArgumentException("Missing quest data: " + id);
        }
        return quest;
    }
}
