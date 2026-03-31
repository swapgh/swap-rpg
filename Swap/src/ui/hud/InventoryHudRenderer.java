package ui.hud;

import component.InventoryComponent;
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

    private final HudDrawSupport support;
    private final int tileSize;

    InventoryHudRenderer(HudDrawSupport support, int tileSize) {
        this.support = support;
        this.tileSize = tileSize;
    }

    void drawInventory(Graphics2D g2, UiState ui, InventoryComponent inventory, int screenWidth, int screenHeight) {
        int panelX = screenWidth / 8;
        int panelY = screenHeight / 10;
        int panelWidth = screenWidth * 3 / 4;
        int panelHeight = screenHeight * 4 / 5;
        int innerX = panelX + 22;
        int innerY = panelY + 20;
        int previewWidth = panelWidth - 44;
        int previewHeight = 120;
        int columns = 5;
        int slotSize = tileSize + 6;
        int slotGap = 14;
        int labelGap = 14;
        int gridStartX = innerX + 8;
        int gridStartY = panelY + previewHeight + 44;

        InventoryViewModel viewModel = InventoryViewModel.from(inventory);
        List<ItemStackView> items = viewModel.stacks();
        int selectedIndex = items.isEmpty() ? -1 : Math.max(0, Math.min(ui.inventorySelectedIndex, items.size() - 1));

        g2.setColor(new Color(5, 8, 14, 185));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(new Color(7, 11, 18, 240));
        g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 32, 32);
        g2.setColor(new Color(82, 104, 145, 130));
        g2.fillRoundRect(panelX + 10, panelY + 10, panelWidth - 20, panelHeight - 20, 28, 28);
        g2.setColor(new Color(13, 19, 29, 245));
        g2.fillRoundRect(panelX + 16, panelY + 16, panelWidth - 32, panelHeight - 32, 26, 26);

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

        ItemStackView selected = selectedIndex >= 0 ? items.get(selectedIndex) : null;
        drawSelectedItemPanel(g2, selected, innerX + 12, innerY + 70, previewWidth - 24, 34);

        if (items.isEmpty()) {
            drawEmptySlots(g2, gridStartX, gridStartY, columns, slotSize, slotGap);
            return;
        }

        for (int index = 0; index < items.size(); index++) {
            int col = index % columns;
            int row = index / columns;
            int slotX = gridStartX + col * (slotSize + slotGap);
            int slotY = gridStartY + row * (slotSize + labelGap + 12);
            drawInventorySlot(g2, slotX, slotY, slotSize, items.get(index), index == selectedIndex);
        }
    }

    void drawShop(Graphics2D g2, UiState ui, InventoryComponent inventory, List<String> entries, int screenWidth,
            int screenHeight) {
        int panelX = screenWidth / 7;
        int panelY = screenHeight / 8;
        int panelWidth = screenWidth * 5 / 7;
        int panelHeight = screenHeight * 3 / 4;
        int innerX = panelX + 22;
        int innerY = panelY + 20;

        g2.setColor(new Color(5, 8, 14, 185));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(new Color(7, 11, 18, 240));
        g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 32, 32);
        g2.setColor(new Color(82, 104, 145, 130));
        g2.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 32, 32);

        g2.setColor(new Color(241, 220, 171));
        g2.setFont(support.assets().font("body"));
        g2.drawString(UiText.SHOP + " - " + ui.shopSpeaker, innerX, innerY + 8);

        g2.setFont(support.assets().font("small"));
        g2.setColor(Color.WHITE);
        g2.drawString(UiText.shopCoins(inventory.coins), innerX, innerY + 34);
        g2.drawString(UiText.SHOP_BUY, innerX + 170, innerY + 34);
        g2.drawString(UiText.SHOP_CLOSE, panelX + panelWidth - 100, innerY + 34);

        int listX = innerX;
        int listY = innerY + 58;
        int itemHeight = 30;
        int gap = 10;
        int width = panelWidth - 44;
        for (int i = 0; i < entries.size(); i++) {
            int y = listY + i * (itemHeight + gap);
            boolean selected = i == ui.shopSelectedIndex;
            g2.setColor(selected ? new Color(66, 82, 114, 220) : new Color(20, 27, 38, 210));
            g2.fillRoundRect(listX, y, width, itemHeight, 16, 16);
            g2.setColor(selected ? new Color(241, 220, 171, 220) : new Color(95, 117, 156, 145));
            g2.drawRoundRect(listX, y, width, itemHeight, 16, 16);
            g2.setColor(selected ? new Color(255, 247, 219) : new Color(227, 231, 238));
            int baseline = y + ((itemHeight - g2.getFontMetrics().getHeight()) / 2) + g2.getFontMetrics().getAscent();
            g2.drawString((selected ? "> " : "") + entries.get(i), listX + 12, baseline);
        }

        if (ui.shopStatusMessage != null && !ui.shopStatusMessage.isBlank()) {
            g2.setColor(new Color(255, 247, 219));
            g2.drawString(ui.shopStatusMessage, innerX, panelY + panelHeight - 18);
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

        g2.setFont(support.assets().font("small"));
        g2.setColor(Color.WHITE);
        support.drawCenteredInBox(g2, item.item().displayName(), x - 12, y + size + 16, size + 24);

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

        g2.setColor(Color.WHITE);
        g2.setFont(support.assets().font("small"));
        g2.drawString(item.item().description(), textX, y + 32);
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
}
