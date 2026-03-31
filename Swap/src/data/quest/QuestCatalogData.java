package data.quest;

import java.util.Map;

/**
 * Catalog of built-in quest hooks and the ids used by runtime events.
 */
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
