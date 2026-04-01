package component.combat;

public final class LootComponent {
    public final String itemId;
    public final int amount;
    public final double dropChance;

    public LootComponent(String itemId, int amount, double dropChance) {
        this.itemId = itemId;
        this.amount = amount;
        this.dropChance = dropChance;
    }
}
