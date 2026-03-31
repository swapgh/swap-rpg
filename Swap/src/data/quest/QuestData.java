package data.quest;

/**
 * Immutable definition for one quest hook used by the runtime.
 */
public record QuestData(
        String id,
        String completionToast,
        String activeHint) {
}
