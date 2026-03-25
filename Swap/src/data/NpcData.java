package data;

/**
 * Immutable definition for one NPC type.
 *
 * NPCs currently focus on visuals, collision and dialogue, but keeping them as data
 * records leaves room to add shops, schedules or quest hooks later without changing
 * the overall creation pipeline.
 */
public record NpcData(
        String id,
        String name,
        String faction,
        VisualData visual,
        ColliderData collider,
        FlagsData flags,
        String[] dialogueLines) {
}
