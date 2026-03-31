package data.world;

import data.ColliderData;

/**
 * Immutable definition for interactive world objects such as coins, keys, doors and
 * chests.
 */
public record WorldObjectData(
        String id,
        String name,
        String spriteId,
        int layer,
        ColliderData collider,
        boolean solid,
        CollectibleDropData collectible,
        DoorRuleData door,
        ChestRuleData chest,
        InteractionRuleData interaction) {

    public record CollectibleDropData(String itemId, int amount) {
    }

    public record DoorRuleData(boolean locked, String requiredItemId) {
    }

    public record ChestRuleData(String openedSpriteId) {
    }

    public record InteractionRuleData(
            String interactionHint,
            String successToast,
            String failureToast,
            String successAudioId,
            String failureAudioId) {
    }
}
