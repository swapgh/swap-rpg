package system.inventory;

import component.progression.CollectibleComponent;
import component.progression.InventoryComponent;
import component.progression.ProgressionComponent;
import component.progression.QuestComponent;
import data.DataRegistry;

public final class InventoryOps {
    private InventoryOps() {
    }

    public static void addCollectible(InventoryComponent inventory, QuestComponent quests, ProgressionComponent progression,
            CollectibleComponent collectible, DataRegistry data) {
        if ("coin".equals(collectible.itemId)) {
            inventory.coins += collectible.amount;
            quests.activate(data.questCatalog().firstCoinQuestId());
        } else {
            addItem(inventory, collectible.itemId, collectible.amount);
        }
        progression.dirtySync = true;
    }

    public static void addItem(InventoryComponent inventory, String itemId, int amount) {
        for (int i = 0; i < amount; i++) {
            inventory.itemIds.add(itemId);
        }
    }
}
