package data;

import data.shop.ShopData;

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
