package ui.hud;

import asset.AssetManager;
import component.HealthComponent;
import component.InventoryComponent;
import component.QuestComponent;
import component.WorldTimeComponent;
import java.awt.Graphics2D;
import java.util.List;
import ui.runtime.UiState;

public final class HudRenderer {
    private final TitleHudRenderer titleRenderer;
    private final WorldHudRenderer worldRenderer;
    private final InventoryHudRenderer inventoryRenderer;
    private final OverlayHudRenderer overlayRenderer;

    public HudRenderer(AssetManager assets, int tileSize) {
        HudDrawSupport support = new HudDrawSupport(assets, tileSize);
        this.titleRenderer = new TitleHudRenderer(support);
        this.worldRenderer = new WorldHudRenderer(support, tileSize);
        this.inventoryRenderer = new InventoryHudRenderer(support, tileSize);
        this.overlayRenderer = new OverlayHudRenderer(support);
    }

    public void drawTitle(Graphics2D g2, UiState ui, int screenWidth, int screenHeight) {
        titleRenderer.drawTitle(g2, ui, screenWidth, screenHeight);
    }

    public void drawTitleMenu(Graphics2D g2, String title, String sectionLabel, String accountLabel, boolean loggedIn,
            List<String> options, int selectedIndex, String footer, int screenWidth, int screenHeight) {
        titleRenderer.drawTitleMenu(g2, title, sectionLabel, accountLabel, loggedIn, options, selectedIndex, footer,
                screenWidth, screenHeight);
    }

    public void drawWorldHud(Graphics2D g2, UiState ui, int screenWidth, int screenHeight, HealthComponent health,
            InventoryComponent inventory, QuestComponent quests, WorldTimeComponent worldTime, String accountLabel,
            boolean accountLoggedIn) {
        worldRenderer.drawWorldHud(g2, ui, screenWidth, screenHeight, health, inventory, quests, worldTime,
                accountLabel, accountLoggedIn);
    }

    public void drawDialogue(Graphics2D g2, UiState ui, int screenWidth, int screenHeight) {
        overlayRenderer.drawDialogue(g2, ui, screenWidth, screenHeight);
    }

    public void drawCompactGameOverOverlay(Graphics2D g2, String title, List<String> options, int selectedIndex,
            String statusMessage, int screenWidth, int screenHeight) {
        overlayRenderer.drawCompactGameOverOverlay(g2, title, options, selectedIndex, statusMessage, screenWidth,
                screenHeight);
    }

    public void drawCompactMenuOverlay(Graphics2D g2, String title, List<String> options, int selectedIndex,
            String statusMessage, int screenWidth, int screenHeight) {
        overlayRenderer.drawCompactMenuOverlay(g2, title, options, selectedIndex, statusMessage, screenWidth,
                screenHeight);
    }

    public void drawInventory(Graphics2D g2, UiState ui, InventoryComponent inventory, int screenWidth,
            int screenHeight) {
        inventoryRenderer.drawInventory(g2, ui, inventory, screenWidth, screenHeight);
    }

    public void drawShop(Graphics2D g2, UiState ui, InventoryComponent inventory, List<String> entries, int screenWidth,
            int screenHeight) {
        inventoryRenderer.drawShop(g2, ui, inventory, entries, screenWidth, screenHeight);
    }
}
