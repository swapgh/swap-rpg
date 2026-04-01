package data;

import data.shop.ShopData;

/**
 * Immutable definition for one NPC type.
 *
 * NPCs currently focus on visuals, collision and dialogue, but keeping them as data
 * records leaves room to add shops, schedules or quest hooks later without changing
 * the overall creation pipeline.
 */
public record NpcData(
        String id,
        String nameKey,
        String faction,
        VisualData visual,
        ColliderData collider,
        FlagsData flags,
        String[] dialogueLineKeys,
        String[] dayDialogueLineKeys,
        String[] nightDialogueLineKeys,
        ShopData shop) {

    public String[] dialogueKeysForPhase(boolean dayPhase) {
        if (dayPhase && dayDialogueLineKeys != null && dayDialogueLineKeys.length > 0) {
            return dayDialogueLineKeys;
        }
        if (!dayPhase && nightDialogueLineKeys != null && nightDialogueLineKeys.length > 0) {
            return nightDialogueLineKeys;
        }
        return dialogueLineKeys;
    }
}
