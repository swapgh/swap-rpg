package ui.viewmodel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import component.progression.InventoryComponent;
import content.catalog.ItemCatalog;
import content.catalog.ItemCatalog.ItemData;

public final class InventoryViewModel {
    private final int coins;
    private final List<ItemStackView> stacks;

    private InventoryViewModel(int coins, List<ItemStackView> stacks) {
        this.coins = coins;
        this.stacks = List.copyOf(stacks);
    }

    public static InventoryViewModel from(InventoryComponent inventory) {
        Map<String, Integer> grouped = new LinkedHashMap<>();
        for (String itemId : inventory.itemIds) {
            grouped.merge(itemId, 1, Integer::sum);
        }

        List<ItemStackView> stacks = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : grouped.entrySet()) {
            stacks.add(new ItemStackView(ItemCatalog.get(entry.getKey()), entry.getValue()));
        }
        return new InventoryViewModel(inventory.coins, stacks);
    }

    public int coins() {
        return coins;
    }

    public int occupiedSlots() {
        return stacks.size();
    }

    public List<ItemStackView> stacks() {
        return stacks;
    }

    public record ItemStackView(ItemData item, int count) {
    }
}
