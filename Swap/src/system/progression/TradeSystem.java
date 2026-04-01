package system.progression;

import app.KeyboardState;
import component.progression.InventoryComponent;
import component.actor.NameComponent;
import component.actor.NpcComponent;
import component.actor.PlayerComponent;
import component.progression.ProgressionComponent;
import component.progression.ShopComponent;
import component.world.WorldTimeComponent;
import content.catalog.ItemCatalog;
import data.DataRegistry;
import data.NpcData;
import data.shop.ShopData;
import data.world.EconomyData;
import ecs.EcsSystem;
import ecs.EcsWorld;
import java.awt.event.KeyEvent;
import java.util.List;
import state.GameMode;
import ui.runtime.UiState;
import ui.text.UiText;

public final class TradeSystem implements EcsSystem {
    private final KeyboardState keyboard;
    private final UiState ui;
    private final DataRegistry data;

    public TradeSystem(KeyboardState keyboard, UiState ui, DataRegistry data) {
        this.keyboard = keyboard;
        this.ui = ui;
        this.data = data;
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        if (ui.mode != GameMode.SHOP) {
            return;
        }
        if (ui.shopNpcEntity < 0 || !world.isAlive(ui.shopNpcEntity) || !world.has(ui.shopNpcEntity, ShopComponent.class)
                || !world.has(ui.shopNpcEntity, NpcComponent.class)) {
            return;
        }
        if (!keyboard.consumePressed(KeyEvent.VK_ENTER) && !keyboard.consumePressed(KeyEvent.VK_E)) {
            return;
        }

        NpcComponent npc = world.require(ui.shopNpcEntity, NpcComponent.class);
        NpcData npcData = data.npc(npc.npcType);
        ShopData shop = npcData.shop();
        if (shop == null) {
            return;
        }

        boolean dayPhase = isDay(world);
        List<EconomyData.EffectiveShopListing> listings = shop.listingsForPhase(dayPhase).stream()
                .map(listing -> data.economy().apply(listing, dayPhase))
                .toList();
        if (listings.isEmpty()) {
            ui.shopSpeaker = world.require(ui.shopNpcEntity, NameComponent.class).value;
            ui.shopStatusMessage = UiText.STATUS_SHOP_NO_STOCK;
            ui.shopSelectedIndex = 0;
            return;
        }

        buySelected(world, listings, Math.max(0, Math.min(ui.shopSelectedIndex, listings.size() - 1)), dayPhase);
    }

    private void buySelected(EcsWorld world, List<EconomyData.EffectiveShopListing> listings, int selected, boolean dayPhase) {
        int player = world.entitiesWith(PlayerComponent.class, InventoryComponent.class, ProgressionComponent.class).get(0);
        InventoryComponent inventory = world.require(player, InventoryComponent.class);
        ProgressionComponent progression = world.require(player, ProgressionComponent.class);
        ShopComponent shopComponent = world.require(ui.shopNpcEntity, ShopComponent.class);
        EconomyData.EffectiveShopListing listing = listings.get(selected);
        int remaining = remainingStock(shopComponent, dayPhase, selected, listing.stock());
        if (remaining == 0) {
            ui.shopStatusMessage = UiText.STATUS_SHOP_NO_STOCK;
            return;
        }
        if (inventory.coins < listing.price()) {
            ui.shopStatusMessage = UiText.STATUS_SHOP_NOT_ENOUGH_COINS;
            return;
        }
        inventory.coins -= listing.price();
        InventoryOps.addItem(inventory, listing.itemId(), 1);
        progression.dirtySync = true;
        if (listing.stock() >= 0) {
            shopComponent.remainingStock.put(stockKey(dayPhase, selected), remaining - 1);
        }
        ui.shopStatusMessage = UiText.shopPurchased(ItemCatalog.get(listing.itemId()).displayName());
    }

    private int remainingStock(ShopComponent shopComponent, boolean dayPhase, int index, int configuredStock) {
        if (configuredStock < 0) {
            return -1;
        }
        return shopComponent.remainingStock.getOrDefault(stockKey(dayPhase, index), configuredStock);
    }

    private String stockKey(boolean dayPhase, int index) {
        return (dayPhase ? "day:" : "night:") + index;
    }

    private boolean isDay(EcsWorld world) {
        List<Integer> times = world.entitiesWith(WorldTimeComponent.class);
        return times.isEmpty() || world.require(times.get(0), WorldTimeComponent.class).isDay();
    }
}
