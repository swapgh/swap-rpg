package system.interaction;

import app.input.KeyboardState;
import component.character.FacingComponent;
import component.character.NameComponent;
import component.character.NpcComponent;
import component.character.PlayerComponent;
import component.progression.ShopComponent;
import component.world.ColliderComponent;
import component.world.PositionComponent;
import component.world.WorldTimeComponent;
import content.catalog.ItemCatalog;
import data.DataRegistry;
import data.NpcData;
import data.shop.ShopData;
import data.world.EconomyData;
import ecs.EcsSystem;
import ecs.EcsWorld;
import java.awt.event.KeyEvent;
import java.awt.Rectangle;
import java.util.List;
import state.GameMode;
import ui.hud.SidePanelEntry;
import ui.state.UiState;
import ui.text.UiText;
import util.CollisionUtil;
import util.Direction;

public final class ShopSystem implements EcsSystem {
    private static final int SHOP_COLUMNS = 3;

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
        if (!playerCanReachShop(world)) {
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
        if (keyboard.consumePressed(KeyEvent.VK_A) || keyboard.consumePressed(KeyEvent.VK_LEFT)) {
            selected = Math.max(0, selected - 1);
        }
        if (keyboard.consumePressed(KeyEvent.VK_D) || keyboard.consumePressed(KeyEvent.VK_RIGHT)) {
            selected = Math.min(listings.size() - 1, selected + 1);
        }
        if (keyboard.consumePressed(KeyEvent.VK_W) || keyboard.consumePressed(KeyEvent.VK_UP)) {
            selected = Math.max(0, selected - SHOP_COLUMNS);
        }
        if (keyboard.consumePressed(KeyEvent.VK_S) || keyboard.consumePressed(KeyEvent.VK_DOWN)) {
            selected = Math.min(listings.size() - 1, selected + SHOP_COLUMNS);
        }
        ui.shopSelectedIndex = selected;

    }

    public List<SidePanelEntry> currentShopEntries(EcsWorld world) {
        if (ui.shopNpcEntity < 0 || !world.isAlive(ui.shopNpcEntity) || !world.has(ui.shopNpcEntity, ShopComponent.class)
                || !world.has(ui.shopNpcEntity, NpcComponent.class)) {
            return List.of();
        }
        NpcComponent npc = world.require(ui.shopNpcEntity, NpcComponent.class);
        NpcData npcData = data.npc(npc.npcType);
        if (npcData.shop() == null) {
            return List.of();
        }
        boolean dayPhase = isDay(world);
        List<EconomyData.EffectiveShopListing> listings = effectiveListings(npcData.shop(), dayPhase);
        ShopComponent shopComponent = world.require(ui.shopNpcEntity, ShopComponent.class);
        return listings.stream()
                .map(listing -> new SidePanelEntry(
                        listing.itemId(),
                        ItemCatalog.get(listing.itemId()).displayName(),
                        "$" + listing.price() + "  "
                                + (remainingStock(shopComponent, dayPhase, listings.indexOf(listing), listing.stock()) < 0
                                        ? "inf"
                                        : remainingStock(shopComponent, dayPhase, listings.indexOf(listing), listing.stock()))))
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

    private boolean playerCanReachShop(EcsWorld world) {
        List<Integer> players = world.entitiesWith(PlayerComponent.class, PositionComponent.class, ColliderComponent.class,
                FacingComponent.class);
        if (players.isEmpty()) {
            return false;
        }
        int player = players.get(0);
        Rectangle interactRect = interactionRect(
                world.require(player, PositionComponent.class),
                world.require(player, ColliderComponent.class),
                world.require(player, FacingComponent.class).direction);
        Rectangle npcRect = CollisionUtil.rect(
                world.require(ui.shopNpcEntity, PositionComponent.class),
                world.require(ui.shopNpcEntity, ColliderComponent.class));
        return interactRect.intersects(npcRect);
    }

    private Rectangle interactionRect(PositionComponent pos, ColliderComponent collider, Direction direction) {
        int baseX = (int) pos.x + collider.offsetX;
        int baseY = (int) pos.y + collider.offsetY;
        return switch (direction) {
        case UP -> new Rectangle(baseX, baseY - 24, collider.width, collider.height + 24);
        case DOWN -> new Rectangle(baseX, baseY, collider.width, collider.height + 24);
        case LEFT -> new Rectangle(baseX - 24, baseY, collider.width + 24, collider.height);
        case RIGHT -> new Rectangle(baseX, baseY, collider.width + 24, collider.height);
        };
    }

    private void closeShop() {
        ui.mode = GameMode.PLAY;
        ui.shopNpcEntity = -1;
        ui.shopSpeaker = "";
        ui.shopStatusMessage = "";
        ui.shopSelectedIndex = 0;
    }
}
