package component.progression;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class QuestComponent {
    private final Map<String, QuestStatus> statuses = new LinkedHashMap<>();

    public QuestStatus statusOf(String questId) {
        return statuses.getOrDefault(questId, QuestStatus.AVAILABLE);
    }

    public void setAvailable(String questId) {
        if (statusOf(questId) != QuestStatus.COMPLETED) {
            statuses.put(questId, QuestStatus.AVAILABLE);
        }
    }

    public void activate(String questId) {
        if (statusOf(questId) != QuestStatus.COMPLETED) {
            statuses.put(questId, QuestStatus.ACTIVE);
        }
    }

    public boolean isActive(String questId) {
        return statusOf(questId) == QuestStatus.ACTIVE;
    }

    public boolean isCompleted(String questId) {
        return statusOf(questId) == QuestStatus.COMPLETED;
    }

    public boolean complete(String questId) {
        if (!isActive(questId)) {
            return false;
        }
        statuses.put(questId, QuestStatus.COMPLETED);
        return true;
    }

    public Set<String> activeQuestIds() {
        Set<String> result = new LinkedHashSet<>();
        for (Map.Entry<String, QuestStatus> entry : statuses.entrySet()) {
            if (entry.getValue() == QuestStatus.ACTIVE) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public Set<String> completedQuestIds() {
        Set<String> result = new LinkedHashSet<>();
        for (Map.Entry<String, QuestStatus> entry : statuses.entrySet()) {
            if (entry.getValue() == QuestStatus.COMPLETED) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public void loadCompleted(Iterable<String> questIds) {
        statuses.clear();
        for (String questId : questIds) {
            if (questId != null && !questId.isBlank()) {
                statuses.put(questId, QuestStatus.COMPLETED);
            }
        }
    }
}
