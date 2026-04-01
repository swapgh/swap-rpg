package ui.hud;

import component.progression.InventoryComponent;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import ui.runtime.UiState;
import ui.text.UiText;
import ui.viewmodel.InventoryViewModel;
import ui.viewmodel.InventoryViewModel.ItemStackView;

final class InventoryHudRenderer {
    private static final int INVENTORY_CAPACITY = 30;
    private static final int INVENTORY_COLUMNS = 5;

    private final HudDrawSupport support;
    private final int tileSize;

    InventoryHudRenderer(HudDrawSupport support, int tileSize) {
        this.support = support;
        this.tileSize = tileSize;
    }

    void drawInventory(Graphics2D g2, UiState ui, InventoryComponent inventory, int screenWidth, int screenHeight,
            boolean compactRight) {
        if (compactRight) {
            InventoryViewModel viewModel = InventoryViewModel.from(inventory);
            List<ItemStackView> items = viewModel.stacks();
            DualWindowLayout layout = DualWindowLayout.create(screenWidth, screenHeight);
            drawBackdrop(g2, screenWidth, screenHeight);
            drawInventoryWindowShell(g2, layout.rightX, layout.windowY, layout.rightWidth, layout.windowHeight);
            drawCompactInventoryWindow(g2, inventory, viewModel, items, layout, ui.inventorySelectedIndex);
            return;
        }

        int panelX = screenWidth / 8;
        int panelY = screenHeight / 10;
        int panelWidth = screenWidth * 3 / 4;
        int panelHeight = screenHeight * 4 / 5;
        int innerX = panelX + 22;
        int innerY = panelY + 20;
        int previewWidth = panelWidth - 44;
        int previewHeight = 120;
        int slotSize = tileSize + 6;
        int slotGap = 14;
        int labelGap = 14;
        int gridStartX = innerX + 8;
        int gridStartY = panelY + previewHeight + 44;

        InventoryViewModel viewModel = InventoryViewModel.from(inventory);
        List<ItemStackView> items = viewModel.stacks();
        int selectedIndex = items.isEmpty() ? -1 : Math.max(0, Math.min(ui.inventorySelectedIndex, items.size() - 1));
        ItemStackView selected = selectedIndex >= 0 ? items.get(selectedIndex) : null;

        drawBackdrop(g2, screenWidth, screenHeight);
        drawInventoryWindowShell(g2, panelX, panelY, panelWidth, panelHeight);

        g2.setColor(new Color(69, 89, 125, 165));
        g2.fillRoundRect(innerX, innerY, previewWidth, previewHeight, 24, 24);

        g2.setColor(new Color(241, 220, 171));
        g2.setFont(support.assets().font("body"));
        g2.drawString(UiText.INVENTORY, innerX + 16, innerY + 28);

        g2.setFont(support.assets().font("small"));
        g2.setColor(Color.WHITE);
        g2.drawString(UiText.inventoryCoins(viewModel.coins()), innerX + 16, innerY + 54);
        g2.drawString(UiText.inventoryOccupied(viewModel.occupiedSlots(), INVENTORY_CAPACITY), innerX + 160, innerY + 54);
        g2.drawString(UiText.INVENTORY_CLOSE, innerX + previewWidth - 92, innerY + 54);

        drawSelectedItemPanel(g2, selected, innerX + 12, innerY + 70, previewWidth - 24, 34);

        if (items.isEmpty()) {
            drawEmptySlots(g2, gridStartX, gridStartY, INVENTORY_COLUMNS, slotSize, slotGap);
            return;
        }

        for (int index = 0; index < items.size(); index++) {
            int col = index % INVENTORY_COLUMNS;
            int row = index / INVENTORY_COLUMNS;
            int slotX = gridStartX + col * (slotSize + slotGap);
            int slotY = gridStartY + row * (slotSize + labelGap + 12);
            drawInventorySlot(g2, slotX, slotY, slotSize, items.get(index), index == selectedIndex);
        }
    }

    void drawShop(Graphics2D g2, UiState ui, InventoryComponent inventory, List<SidePanelEntry> entries, int screenWidth,
            int screenHeight) {
        InventoryViewModel viewModel = InventoryViewModel.from(inventory);
        List<ItemStackView> items = viewModel.stacks();

        DualWindowLayout layout = DualWindowLayout.create(screenWidth, screenHeight);
        drawBackdrop(g2, screenWidth, screenHeight);
        drawSideWindow(g2, layout.leftX, layout.windowY, layout.leftWidth, layout.windowHeight);
        drawInventoryWindowShell(g2, layout.rightX, layout.windowY, layout.rightWidth, layout.windowHeight);

        drawShopWindow(g2, ui, entries, layout);
        drawCompactInventoryWindow(g2, inventory, viewModel, items, layout, ui.inventorySelectedIndex);
    }

    void drawLoot(Graphics2D g2, UiState ui, InventoryComponent inventory, List<SidePanelEntry> entries, int screenWidth,
            int screenHeight) {
        InventoryViewModel viewModel = InventoryViewModel.from(inventory);
        List<ItemStackView> items = viewModel.stacks();

        DualWindowLayout layout = DualWindowLayout.create(screenWidth, screenHeight);
        drawBackdrop(g2, screenWidth, screenHeight);
        drawSideWindow(g2, layout.leftX, layout.windowY, layout.leftWidth, layout.windowHeight);
        drawInventoryWindowShell(g2, layout.rightX, layout.windowY, layout.rightWidth, layout.windowHeight);

        drawLootWindow(g2, ui, entries, layout);
        drawCompactInventoryWindow(g2, inventory, viewModel, items, layout, ui.inventorySelectedIndex);
    }

    private void drawBackdrop(Graphics2D g2, int screenWidth, int screenHeight) {
        g2.setColor(new Color(5, 8, 14, 185));
        g2.fillRect(0, 0, screenWidth, screenHeight);
    }

    private void drawInventoryWindowShell(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(new Color(7, 11, 18, 240));
        g2.fillRoundRect(x, y, width, height, 32, 32);
        g2.setColor(new Color(82, 104, 145, 130));
        g2.fillRoundRect(x + 10, y + 10, width - 20, height - 20, 28, 28);
        g2.setColor(new Color(13, 19, 29, 245));
        g2.fillRoundRect(x + 16, y + 16, width - 32, height - 32, 26, 26);
    }

    private void drawSideWindow(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(new Color(7, 11, 18, 236));
        g2.fillRoundRect(x, y, width, height, 28, 28);
        g2.setColor(new Color(92, 113, 146, 125));
        g2.drawRoundRect(x, y, width, height, 28, 28);
        g2.setColor(new Color(18, 25, 36, 245));
        g2.fillRoundRect(x + 12, y + 12, width - 24, height - 24, 22, 22);
    }

    private void drawShopWindow(Graphics2D g2, UiState ui, List<SidePanelEntry> entries, DualWindowLayout layout) {
        int innerX = layout.leftX + 24;
        int innerY = layout.windowY + 24;
        int innerWidth = layout.leftWidth - 48;
        int gridY = layout.windowY + 92;

        g2.setColor(new Color(241, 220, 171));
        g2.setFont(support.assets().font("body"));
        g2.drawString(UiText.SHOP, innerX, innerY + 4);

        g2.setFont(support.assets().font("small"));
        g2.setColor(new Color(171, 184, 204));
        g2.drawString(ui.shopSpeaker.isBlank() ? UiText.SHOP_KEEPER : ui.shopSpeaker, innerX, innerY + 28);
        g2.setColor(Color.WHITE);
        g2.drawString(UiText.SHOP_CLOSE, innerX + innerWidth - 86, innerY + 28);
        drawSideEntryGrid(g2, entries, ui.shopSelectedIndex, innerX, gridY, false);
        drawWindowCornerHint(g2, ui.shopStatusMessage == null || ui.shopStatusMessage.isBlank() ? UiText.SHOP_BUY : ui.shopStatusMessage,
                layout.leftX + layout.leftWidth - 18, layout.windowY + layout.windowHeight - 18);
    }

    private void drawLootWindow(Graphics2D g2, UiState ui, List<SidePanelEntry> entries, DualWindowLayout layout) {
        int innerX = layout.leftX + 24;
        int innerY = layout.windowY + 24;
        int innerWidth = layout.leftWidth - 48;
        int gridY = layout.windowY + 92;

        g2.setColor(new Color(241, 220, 171));
        g2.setFont(support.assets().font("body"));
        g2.drawString(UiText.LOOT, innerX, innerY + 4);

        g2.setFont(support.assets().font("small"));
        g2.setColor(new Color(171, 184, 204));
        g2.drawString(ui.lootSourceName.isBlank() ? UiText.LOOT_CONTAINER : ui.lootSourceName, innerX, innerY + 28);
        g2.setColor(Color.WHITE);
        g2.drawString(UiText.LOOT_CLOSE, innerX + innerWidth - 86, innerY + 28);
        drawSideEntryGrid(g2, entries, ui.lootSelectedIndex, innerX, gridY, true);
        drawWindowCornerHint(g2, ui.lootStatusMessage == null || ui.lootStatusMessage.isBlank() ? UiText.LOOT_TAKE : ui.lootStatusMessage,
                layout.leftX + layout.leftWidth - 18, layout.windowY + layout.windowHeight - 18);
    }

    private void drawCompactInventoryWindow(Graphics2D g2, InventoryComponent inventory, InventoryViewModel viewModel,
            List<ItemStackView> items, DualWindowLayout layout, int selectedIndex) {
        int panelX = layout.rightX;
        int panelY = layout.windowY;
        int panelWidth = layout.rightWidth;
        int innerX = panelX + 22;
        int innerY = panelY + 20;
        int previewWidth = panelWidth - 44;
        int previewHeight = 120;
        int slotSize = tileSize + 2;
        int slotGap = 10;
        int labelGap = 14;
        int gridStartX = innerX + 4;
        int gridStartY = panelY + previewHeight + 44;
        int clampedIndex = items.isEmpty() ? -1 : Math.max(0, Math.min(selectedIndex, items.size() - 1));
        ItemStackView selected = clampedIndex >= 0 ? items.get(clampedIndex) : null;

        g2.setColor(new Color(69, 89, 125, 165));
        g2.fillRoundRect(innerX, innerY, previewWidth, previewHeight, 24, 24);

        g2.setColor(new Color(241, 220, 171));
        g2.setFont(support.assets().font("body"));
        g2.drawString(UiText.INVENTORY, innerX + 16, innerY + 28);

        g2.setFont(support.assets().font("small"));
        g2.setColor(Color.WHITE);
        g2.drawString(UiText.inventoryCoins(inventory.coins), innerX + 16, innerY + 54);
        g2.drawString(UiText.inventoryOccupied(viewModel.occupiedSlots(), INVENTORY_CAPACITY), innerX + 124, innerY + 54);
        drawSelectedItemPanel(g2, selected, innerX + 12, innerY + 70, previewWidth - 24, 34);

        int maxVisible = Math.min(items.size(), INVENTORY_COLUMNS * 4);
        for (int index = 0; index < maxVisible; index++) {
            int col = index % INVENTORY_COLUMNS;
            int row = index / INVENTORY_COLUMNS;
            int slotX = gridStartX + col * (slotSize + slotGap);
            int slotY = gridStartY + row * (slotSize + labelGap + 12);
            drawInventorySlot(g2, slotX, slotY, slotSize, items.get(index), index == clampedIndex);
        }
        if (items.isEmpty()) {
            drawEmptySlots(g2, gridStartX, gridStartY, INVENTORY_COLUMNS, slotSize, slotGap);
        }
    }

    private void drawInventorySlot(Graphics2D g2, int x, int y, int size, ItemStackView item, boolean selected) {
        g2.setColor(selected ? new Color(182, 144, 70, 235) : new Color(31, 40, 56, 220));
        g2.fillRoundRect(x, y, size, size, 16, 16);
        g2.setColor(selected ? new Color(255, 236, 186, 250) : new Color(108, 135, 175, 200));
        g2.drawRoundRect(x, y, size, size, 16, 16);
        g2.setColor(new Color(14, 21, 31, 190));
        g2.fillRoundRect(x + 4, y + 4, size - 8, size - 8, 12, 12);

        int iconSize = Math.max(16, tileSize - 10);
        int iconX = x + (size - iconSize) / 2;
        int iconY = y + 6;
        BufferedImage icon = support.itemIcon(item.item());
        if (icon != null) {
            g2.drawImage(icon, iconX, iconY, iconSize, iconSize, null);
        } else {
            g2.setColor(new Color(241, 220, 171));
            g2.setFont(support.assets().font("body"));
            support.drawCenteredInBox(g2, support.itemFallbackLabel(item.item()), x, y + tileSize - 6, size);
        }

        g2.setColor(new Color(12, 16, 22, 225));
        g2.fillRoundRect(x + size - 32, y + size - 24, 28, 18, 10, 10);
        g2.setColor(selected ? new Color(255, 238, 190) : new Color(241, 220, 171));
        g2.drawString(UiText.itemCount(item.count()), x + size - 29, y + size - 10);
    }

    private void drawSelectedItemPanel(Graphics2D g2, ItemStackView item, int x, int y, int width, int height) {
        g2.setColor(new Color(14, 20, 31, 228));
        g2.fillRoundRect(x, y, width, height, 20, 20);
        g2.setColor(new Color(255, 233, 174, 180));
        g2.drawRoundRect(x, y, width, height, 20, 20);

        int iconFrameSize = height - 10;
        int iconFrameX = x + 8;
        int iconFrameY = y + 5;
        g2.setColor(new Color(38, 48, 66, 220));
        g2.fillRoundRect(iconFrameX, iconFrameY, iconFrameSize, iconFrameSize, 18, 18);
        g2.setColor(new Color(201, 165, 92, 190));
        g2.drawRoundRect(iconFrameX, iconFrameY, iconFrameSize, iconFrameSize, 18, 18);

        if (item == null) {
            g2.setColor(new Color(255, 255, 255, 190));
            g2.setFont(support.assets().font("small"));
            g2.drawString(UiText.INVENTORY_EMPTY_SELECTION, iconFrameX + iconFrameSize + 14, y + 21);
            return;
        }

        BufferedImage icon = support.itemIcon(item.item());
        if (icon != null) {
            int iconSize = Math.max(16, tileSize - 8);
            int iconX = iconFrameX + (iconFrameSize - iconSize) / 2;
            int iconY = iconFrameY + (iconFrameSize - iconSize) / 2;
            g2.drawImage(icon, iconX, iconY, iconSize, iconSize, null);
        } else {
            g2.setColor(new Color(241, 220, 171));
            g2.setFont(support.assets().font("body"));
            support.drawCenteredInBox(g2, support.itemFallbackLabel(item.item()), iconFrameX,
                    iconFrameY + iconFrameSize / 2 + 8, iconFrameSize);
        }

        int textX = iconFrameX + iconFrameSize + 14;
        g2.setColor(new Color(241, 220, 171));
        g2.setFont(support.assets().font("small"));
        g2.drawString(UiText.selectedItemLabel(item.item().displayName(), item.count()), textX, y + 18);
    }

    private void drawSideEntryGrid(Graphics2D g2, List<SidePanelEntry> entries, int selectedIndex, int startX, int startY,
            boolean lootPalette) {
        int columns = 3;
        int slotSize = tileSize + 10;
        int gap = 14;
        int rowGap = 30;
        int maxVisible = Math.min(entries.size(), columns * 4);

        if (entries.isEmpty()) {
            drawEmptySlots(g2, startX, startY, columns, slotSize, gap);
            return;
        }

        for (int i = 0; i < maxVisible; i++) {
            int col = i % columns;
            int row = i / columns;
            int slotX = startX + col * (slotSize + gap);
            int slotY = startY + row * (slotSize + rowGap);
            drawSideEntrySlot(g2, entries.get(i), slotX, slotY, slotSize, i == selectedIndex, lootPalette);
        }
    }

    private void drawSideEntrySlot(Graphics2D g2, SidePanelEntry entry, int x, int y, int size, boolean selected,
            boolean lootPalette) {
        Color fill = selected
                ? (lootPalette ? new Color(86, 138, 93, 220) : new Color(184, 142, 73, 220))
                : new Color(28, 36, 50, 220);
        Color stroke = selected
                ? (lootPalette ? new Color(188, 235, 179, 220) : new Color(241, 220, 171, 220))
                : new Color(95, 117, 156, 145);
        g2.setColor(fill);
        g2.fillRoundRect(x, y, size, size, 18, 18);
        g2.setColor(stroke);
        g2.drawRoundRect(x, y, size, size, 18, 18);
        g2.setColor(new Color(14, 21, 31, 190));
        g2.fillRoundRect(x + 5, y + 5, size - 10, size - 10, 14, 14);

        BufferedImage icon = support.itemIcon(content.catalog.ItemCatalog.get(entry.itemId()));
        int iconSize = Math.max(18, tileSize - 6);
        int iconX = x + (size - iconSize) / 2;
        int iconY = y + 10;
        if (icon != null) {
            g2.drawImage(icon, iconX, iconY, iconSize, iconSize, null);
        } else {
            g2.setColor(new Color(241, 220, 171));
            g2.setFont(support.assets().font("body"));
            support.drawCenteredInBox(g2, entry.primaryText().substring(0, 1), x, y + size / 2 + 6, size);
        }

        g2.setFont(support.assets().font("small"));
        g2.setColor(Color.WHITE);
        support.drawCenteredInBox(g2, entry.primaryText(), x - 8, y + size + 16, size + 16);
        g2.setColor(selected ? new Color(241, 255, 237) : new Color(193, 204, 222));
        support.drawCenteredInBox(g2, entry.secondaryText(), x - 8, y + size + 30, size + 16);
    }

    private void drawWindowCornerHint(Graphics2D g2, String text, int rightX, int bottomY) {
        if (text == null || text.isBlank()) {
            return;
        }
        g2.setFont(support.assets().font("small"));
        int width = g2.getFontMetrics().stringWidth(text) + 18;
        int x = rightX - width;
        int y = bottomY - 24;
        g2.setColor(new Color(15, 21, 31, 210));
        g2.fillRoundRect(x, y, width, 18, 10, 10);
        g2.setColor(new Color(241, 220, 171));
        g2.drawString(text, x + 9, y + 13);
    }

    private void drawEmptySlots(Graphics2D g2, int startX, int startY, int columns, int slotSize, int slotGap) {
        for (int index = 0; index < columns; index++) {
            int x = startX + index * (slotSize + slotGap);
            g2.setColor(new Color(30, 39, 52, 170));
            g2.fillRoundRect(x, startY, slotSize, slotSize, 16, 16);
            g2.setColor(new Color(92, 108, 132, 180));
            g2.drawRoundRect(x, startY, slotSize, slotSize, 16, 16);
        }
    }

    private record DualWindowLayout(
            int leftX,
            int rightX,
            int windowY,
            int leftWidth,
            int rightWidth,
            int windowHeight) {

        static DualWindowLayout create(int screenWidth, int screenHeight) {
            int windowY = screenHeight / 10;
            int windowHeight = screenHeight * 4 / 5;
            int gap = 18;
            int leftWidth = screenWidth * 5 / 14;
            int rightWidth = screenWidth * 5 / 12;
            int totalWidth = leftWidth + gap + rightWidth;
            int leftX = (screenWidth - totalWidth) / 2;
            int rightX = leftX + leftWidth + gap;
            return new DualWindowLayout(leftX, rightX, windowY, leftWidth, rightWidth, windowHeight);
        }
    }
}
