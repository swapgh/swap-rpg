package system.progression;

import app.KeyboardState;
import component.InventoryComponent;
import component.NameComponent;
import component.NpcComponent;
import component.PlayerComponent;
import component.ShopComponent;
import component.WorldTimeComponent;
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

public final class ShopSystem implements EcsSystem {
    private final KeyboardState keyboard;
    private final UiState ui;
    private final DataRegistry data;

    public ShopSystem(KeyboardState keyboard, UiState ui, DataRegistry data) {
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
            closeShop();
            return;
        }

        if (keyboard.consumePressed(KeyEvent.VK_ESCAPE) || keyboard.consumePressed(KeyEvent.VK_BACK_SPACE)
                || keyboard.consumePressed(KeyEvent.VK_I)) {
            closeShop();
            return;
        }

        NpcComponent npc = world.require(ui.shopNpcEntity, NpcComponent.class);
        NpcData npcData = data.npc(npc.npcType);
        ShopData shop = npcData.shop();
        if (shop == null) {
            closeShop();
            return;
        }

        boolean dayPhase = isDay(world);
        List<EconomyData.EffectiveShopListing> listings = effectiveListings(shop, dayPhase);
        ui.shopSpeaker = world.require(ui.shopNpcEntity, NameComponent.class).value;
        if (listings.isEmpty()) {
            ui.shopStatusMessage = UiText.STATUS_SHOP_NO_STOCK;
            ui.shopSelectedIndex = 0;
            return;
        }

        int selected = Math.max(0, Math.min(ui.shopSelectedIndex, listings.size() - 1));
        if (keyboard.consumePressed(KeyEvent.VK_W) || keyboard.consumePressed(KeyEvent.VK_UP)) {
            selected = Math.max(0, selected - 1);
        }
        if (keyboard.consumePressed(KeyEvent.VK_S) || keyboard.consumePressed(KeyEvent.VK_DOWN)) {
            selected = Math.min(listings.size() - 1, selected + 1);
        }
        ui.shopSelectedIndex = selected;

        if (keyboard.consumePressed(KeyEvent.VK_ENTER) || keyboard.consumePressed(KeyEvent.VK_E)) {
            buySelected(world, listings, selected, dayPhase);
        }
    }

    private void buySelected(EcsWorld world, List<EconomyData.EffectiveShopListing> listings, int selected, boolean dayPhase) {
        int player = world.entitiesWith(PlayerComponent.class, InventoryComponent.class).get(0);
        InventoryComponent inventory = world.require(player, InventoryComponent.class);
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
        inventory.itemIds.add(listing.itemId());
        if (listing.stock() >= 0) {
            shopComponent.remainingStock.put(stockKey(dayPhase, selected), remaining - 1);
        }
        ui.shopStatusMessage = UiText.STATUS_SHOP_PURCHASED + " " + ItemCatalog.get(listing.itemId()).displayName();
    }

    public List<String> currentShopEntries(EcsWorld world) {
        if (ui.shopNpcEntity < 0 || !world.isAlive(ui.shopNpcEntity) || !world.has(ui.shopNpcEntity, ShopComponent.class)
                || !world.has(ui.shopNpcEntity, NpcComponent.class)) {
            return List.of(UiText.STATUS_SHOP_NO_STOCK);
        }
        NpcComponent npc = world.require(ui.shopNpcEntity, NpcComponent.class);
        NpcData npcData = data.npc(npc.npcType);
        if (npcData.shop() == null) {
            return List.of(UiText.STATUS_SHOP_NO_STOCK);
        }
        boolean dayPhase = isDay(world);
        List<EconomyData.EffectiveShopListing> listings = effectiveListings(npcData.shop(), dayPhase);
        ShopComponent shopComponent = world.require(ui.shopNpcEntity, ShopComponent.class);
        return listings.stream()
                .map(listing -> UiText.shopEntry(
                        ItemCatalog.get(listing.itemId()).displayName(),
                        listing.price(),
                        remainingStock(shopComponent, dayPhase, listings.indexOf(listing), listing.stock())))
                .toList();
    }

    private List<EconomyData.EffectiveShopListing> effectiveListings(ShopData shop, boolean dayPhase) {
        return shop.listingsForPhase(dayPhase).stream()
                .map(listing -> data.economy().apply(listing, dayPhase))
                .toList();
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

    private void closeShop() {
        ui.mode = GameMode.PLAY;
        ui.shopNpcEntity = -1;
        ui.shopSpeaker = "";
        ui.shopStatusMessage = "";
        ui.shopSelectedIndex = 0;
    }
}
