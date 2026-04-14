package ui.hud;

import asset.AssetManager;
import component.character.NameComponent;
import component.combat.HealthComponent;
import component.progression.EquipmentComponent;
import component.progression.InventoryComponent;
import component.progression.ProgressionComponent;
import component.progression.QuestComponent;
import component.world.WorldTimeComponent;
import component.world.WorldTierComponent;
import data.DataRegistry;
import java.awt.Graphics2D;
import java.util.List;
import ui.state.UiState;

public final class HudRenderer {
    private final TitleHudRenderer titleRenderer;
    private final WorldHudRenderer worldRenderer;
    private final CharacterHudRenderer characterRenderer;
    private final InventoryHudRenderer inventoryRenderer;
    private final OverlayHudRenderer overlayRenderer;

    public HudRenderer(AssetManager assets, DataRegistry data, int tileSize) {
        HudDrawSupport support = new HudDrawSupport(assets, tileSize);
        this.titleRenderer = new TitleHudRenderer(support);
        this.worldRenderer = new WorldHudRenderer(support, data);
        this.characterRenderer = new CharacterHudRenderer(support, data);
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
            InventoryComponent inventory, ProgressionComponent progression, EquipmentComponent equipment, QuestComponent quests,
            WorldTimeComponent worldTime, WorldTierComponent worldTier, String accountLabel,
            boolean accountLoggedIn) {
        worldRenderer.drawWorldHud(g2, ui, screenWidth, screenHeight, health, inventory, progression, equipment, quests, worldTime, worldTier,
                accountLabel, accountLoggedIn);
    }

    public void drawCharacter(Graphics2D g2, NameComponent name, HealthComponent health, ProgressionComponent progression,
            EquipmentComponent equipment, int screenWidth, int screenHeight, boolean alongsideInventory) {
        characterRenderer.drawCharacter(g2, name, health, progression, equipment, screenWidth, screenHeight,
                alongsideInventory);
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
            int screenHeight, boolean compactRight) {
        inventoryRenderer.drawInventory(g2, ui, inventory, screenWidth, screenHeight, compactRight);
    }

    public void drawShop(Graphics2D g2, UiState ui, InventoryComponent inventory, List<SidePanelEntry> entries, int screenWidth,
            int screenHeight) {
        inventoryRenderer.drawShop(g2, ui, inventory, entries, screenWidth, screenHeight);
    }

    public void drawLoot(Graphics2D g2, UiState ui, InventoryComponent inventory, List<SidePanelEntry> entries, int screenWidth,
            int screenHeight) {
        inventoryRenderer.drawLoot(g2, ui, inventory, entries, screenWidth, screenHeight);
    }
}
