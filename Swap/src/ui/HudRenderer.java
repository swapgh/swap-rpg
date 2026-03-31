package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import asset.AssetManager;
import component.HealthComponent;
import component.InventoryComponent;
import component.QuestComponent;
import component.WorldTimeComponent;
import content.ItemCatalog.ItemData;
import ui.InventoryViewModel.ItemStackView;

public final class HudRenderer {
    private static final int INVENTORY_CAPACITY = 30;
    private static final int SYSTEM_LOG_VISIBLE_TICKS = 300;

    private final AssetManager assets;
    private final int tileSize;

    public HudRenderer(AssetManager assets, int tileSize) {
        this.assets = assets;
        this.tileSize = tileSize;
    }

    public void drawTitle(Graphics2D g2, UiState ui, int screenWidth, int screenHeight) {
        g2.setColor(new Color(18, 24, 33));
        g2.fillRect(0, 0, screenWidth, screenHeight);
        g2.setColor(new Color(241, 220, 171));
        g2.setFont(assets.font("title"));
        drawCentered(g2, ui.titleMessage, screenWidth, screenHeight / 3);
        g2.setFont(assets.font("body"));
        drawCenteredParagraph(g2, ui.subtitleMessage, screenWidth, screenHeight / 2, screenWidth - 96, 42);
    }

    public void drawTitleMenu(Graphics2D g2, String title, String sectionLabel, String accountLabel, boolean loggedIn,
            List<String> options, int selectedIndex, String footer, int screenWidth, int screenHeight) {
        g2.setColor(new Color(17, 22, 30));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(new Color(26, 35, 49));
        g2.fillRoundRect(52, 42, screenWidth - 104, screenHeight - 84, 34, 34);
        g2.setColor(new Color(78, 96, 133, 140));
        g2.drawRoundRect(52, 42, screenWidth - 104, screenHeight - 84, 34, 34);

        g2.setColor(new Color(241, 220, 171));
        g2.setFont(assets.font("title"));
        g2.drawString(title, 82, 138);

        g2.setFont(assets.font("small"));
        g2.setColor(new Color(255, 247, 219));
        g2.drawString(sectionLabel, 86, 174);

        int accountWidth = Math.max(140, g2.getFontMetrics().stringWidth(accountLabel) + 60);
        int accountX = screenWidth - accountWidth - 74;
        int accountY = 78;
        g2.setColor(new Color(14, 20, 29, 220));
        g2.fillRoundRect(accountX, accountY, accountWidth, 46, 18, 18);
        g2.setColor(loggedIn ? new Color(74, 162, 101, 170) : new Color(120, 128, 141, 170));
        g2.drawRoundRect(accountX, accountY, accountWidth, 46, 18, 18);
        g2.setColor(new Color(255, 247, 219));
        g2.drawString(UiText.accountLabel(loggedIn), accountX + 14, accountY + 14);
        g2.setColor(new Color(241, 220, 171));
        g2.drawString(accountLabel, accountX + 14, accountY + 32);

        int menuX = 86;
        int menuY = 198;
        int menuWidth = screenWidth - 172;
        int itemHeight = 54;
        int gap = 16;
        g2.setFont(assets.font("body"));
        for (int i = 0; i < options.size(); i++) {
            int y = menuY + i * (itemHeight + gap);
            boolean selected = i == selectedIndex;
            g2.setColor(selected ? new Color(66, 82, 114, 220) : new Color(20, 27, 38, 210));
            g2.fillRoundRect(menuX, y, menuWidth, itemHeight, 20, 20);
            g2.setColor(selected ? new Color(241, 220, 171, 220) : new Color(95, 117, 156, 145));
            g2.drawRoundRect(menuX, y, menuWidth, itemHeight, 20, 20);
            g2.setColor(selected ? new Color(255, 247, 219) : new Color(227, 231, 238));
            g2.drawString((selected ? "> " : "") + options.get(i), menuX + 18, y + 35);
        }

        g2.setFont(assets.font("small"));
        g2.setColor(new Color(255, 247, 219));
        g2.drawString(UiText.FOOTER_NAVIGATION, 86, screenHeight - 84);
        if (footer != null && !footer.isBlank()) {
            g2.setColor(new Color(241, 220, 171));
            g2.drawString(footer, 86, screenHeight - 56);
        }
    }

    public void drawWorldHud(Graphics2D g2, UiState ui, int screenWidth, int screenHeight, HealthComponent health,
            InventoryComponent inventory, QuestComponent quests, WorldTimeComponent worldTime, String accountLabel,
            boolean accountLoggedIn) {
        int heartSize = Math.max(18, tileSize - 18);
        int heartsX = 18;
        int heartsY = 18;
        int heartCount = Math.max(1, health.max / 2);
        int heartsWidth = heartCount * heartSize + 18;
        int heartsHeight = heartSize + 16;

        g2.setColor(new Color(7, 11, 18, 150));
        g2.fillRoundRect(12, 12, heartsWidth, heartsHeight, 18, 18);
        g2.setColor(new Color(95, 117, 156, 120));
        g2.drawRoundRect(12, 12, heartsWidth, heartsHeight, 18, 18);

        int x = heartsX;
        int y = heartsY;
        for (int i = 0; i < health.max / 2; i++) {
            g2.drawImage(assets.image("ui.heartBlank"), x, y, heartSize, heartSize, null);
            x += heartSize;
        }
        x = heartsX;
        int remaining = health.current;
        while (remaining > 0) {
            g2.drawImage(assets.image("ui.heartHalf"), x, y, heartSize, heartSize, null);
            if (remaining > 1) {
                g2.drawImage(assets.image("ui.heartFull"), x, y, heartSize, heartSize, null);
            }
            x += heartSize;
            remaining -= 2;
        }

        g2.setFont(assets.font("small"));
        drawConnectionBadge(g2, screenWidth, accountLabel, accountLoggedIn);
        drawTimeBadge(g2, screenWidth, worldTime);
        drawInventoryBadge(g2, screenWidth, screenHeight, inventory);

        if (ui.contextHint != null && !ui.contextHint.isBlank()) {
            int hintWidth = g2.getFontMetrics().stringWidth(ui.contextHint) + 24;
            int hintX = (screenWidth - hintWidth) / 2;
            int hintY = 18;
            g2.setColor(new Color(7, 11, 18, 210));
            g2.fillRoundRect(hintX, hintY, hintWidth, 26, 14, 14);
            g2.setColor(new Color(201, 165, 92, 180));
            g2.drawRoundRect(hintX, hintY, hintWidth, 26, 14, 14);
            g2.setColor(new Color(255, 247, 219));
            g2.drawString(ui.contextHint, hintX + 12, hintY + 18);
        }

        if (ui.combatToastTicks > 0 && ui.combatToast != null && !ui.combatToast.isBlank()) {
            int toastWidth = Math.min(180, g2.getFontMetrics().stringWidth(ui.combatToast) + 22);
            int toastHeight = 24;
            int toastX = 12;
            int toastY = heartsHeight + 18;
            g2.setColor(new Color(9, 14, 20, 195));
            g2.fillRoundRect(toastX, toastY, toastWidth, toastHeight, 14, 14);
            g2.setColor(new Color(201, 110, 110, 170));
            g2.drawRoundRect(toastX, toastY, toastWidth, toastHeight, 14, 14);
            g2.setColor(Color.WHITE);
            g2.drawString(ui.combatToast, toastX + 11, toastY + 16);
        }

        drawSystemLog(g2, ui, screenHeight);
    }

    private void drawSystemLog(Graphics2D g2, UiState ui, int screenHeight) {
        List<SystemLogEntry> history = systemLogHistory(ui);
        if (history.isEmpty()) {
            return;
        }
        if (!ui.systemLogExpanded && !hasRecentSystemLog(history)) {
            return;
        }

        Font previousFont = g2.getFont();
        Font logFont = assets.font("small").deriveFont(14f);
        g2.setFont(logFont);

        List<SystemLogEntry> lines = ui.systemLogExpanded ? tail(history, 8) : tailRecent(history, 2);
        if (lines.isEmpty()) {
            g2.setFont(previousFont);
            return;
        }

        int logWidth = ui.systemLogExpanded ? 250 : 190;
        for (SystemLogEntry line : lines) {
            logWidth = Math.max(logWidth, g2.getFontMetrics().stringWidth(line.text()) + 24);
        }

        int headerHeight = 30;
        int bodyLineHeight = 15;
        int bottomPadding = 10;
        int logHeight = headerHeight + lines.size() * bodyLineHeight + bottomPadding;
        int logX = 14;
        int logY = Math.max(12, screenHeight - logHeight - 14);
        float panelFade = ui.systemLogExpanded ? 1f : panelAlpha(lines);
        Color accent = ui.toastTicks > 0 ? colorWithAlpha(new Color(201, 165, 92), 190, panelFade)
                : colorWithAlpha(new Color(95, 117, 156), 170, panelFade);

        g2.setColor(colorWithAlpha(new Color(9, 14, 20), 210, panelFade));
        g2.fillRoundRect(logX, logY, logWidth, logHeight, 18, 18);
        g2.setColor(accent);
        g2.drawRoundRect(logX, logY, logWidth, logHeight, 18, 18);

        g2.setColor(colorWithAlpha(new Color(241, 220, 171), 255, panelFade));
        g2.drawString(UiText.LOGIN_LOG, logX + 12, logY + 16);
        g2.setColor(colorWithAlpha(new Color(189, 196, 207), 255, panelFade));
        String toggleLabel = ui.systemLogExpanded ? UiText.LOGIN_CLOSE_LOG : UiText.LOGIN_OPEN_LOG;
        g2.drawString(toggleLabel, logX + logWidth - g2.getFontMetrics().stringWidth(toggleLabel) - 12, logY + 16);

        int lineY = logY + headerHeight + 2;
        for (SystemLogEntry line : lines) {
            float lineFade = ui.systemLogExpanded ? 1f : messageAlpha(line.age());
            g2.setColor(colorWithAlpha(Color.WHITE, 255, lineFade));
            g2.drawString(line.text(), logX + 12, lineY);
            lineY += bodyLineHeight;
        }
        g2.setFont(previousFont);
    }

    private List<SystemLogEntry> systemLogHistory(UiState ui) {
        List<SystemLogEntry> lines = new ArrayList<>();
        for (int i = 0; i < ui.systemLog.length; i++) {
            String line = ui.systemLog[i];
            if (line != null && !line.isBlank()) {
                lines.add(new SystemLogEntry(line, ui.systemLogAges[i]));
            }
        }
        return lines;
    }

    private List<SystemLogEntry> tail(List<SystemLogEntry> lines, int maxSize) {
        int fromIndex = Math.max(0, lines.size() - maxSize);
        return new ArrayList<>(lines.subList(fromIndex, lines.size()));
    }

    private List<SystemLogEntry> tailRecent(List<SystemLogEntry> lines, int maxSize) {
        List<SystemLogEntry> recent = new ArrayList<>();
        for (SystemLogEntry line : lines) {
            if (line.age() < SYSTEM_LOG_VISIBLE_TICKS) {
                recent.add(line);
            }
        }
        return tail(recent, maxSize);
    }

    private boolean hasRecentSystemLog(List<SystemLogEntry> lines) {
        for (SystemLogEntry line : lines) {
            if (line.age() < SYSTEM_LOG_VISIBLE_TICKS) {
                return true;
            }
        }
        return false;
    }

    private float newestAlpha(List<SystemLogEntry> lines) {
        float alpha = 0f;
        for (SystemLogEntry line : lines) {
            alpha = Math.max(alpha, messageAlpha(line.age()));
        }
        return alpha;
    }

    private float panelAlpha(List<SystemLogEntry> lines) {
        float alpha = newestAlpha(lines);
        return Math.max(0.35f, alpha);
    }

    private float messageAlpha(int age) {
        if (age <= 180) {
            return 1f;
        }
        if (age >= SYSTEM_LOG_VISIBLE_TICKS) {
            return 0f;
        }
        return (SYSTEM_LOG_VISIBLE_TICKS - age) / 120f;
    }

    private Color colorWithAlpha(Color base, int alpha, float multiplier) {
        int finalAlpha = Math.max(0, Math.min(255, Math.round(alpha * multiplier)));
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), finalAlpha);
    }

    private record SystemLogEntry(String text, int age) {
    }

    private void drawConnectionBadge(Graphics2D g2, int screenWidth, String accountLabel, boolean accountLoggedIn) {
        String label = accountLoggedIn ? accountLabel : UiText.GUEST;
        int badgeWidth = Math.max(104, g2.getFontMetrics().stringWidth(label) + 34);
        int badgeHeight = 22;
        int badgeX = screenWidth - badgeWidth - 14;
        int badgeY = 14;
        Color accent = accountLoggedIn ? new Color(63, 146, 92) : new Color(189, 76, 76);

        g2.setColor(new Color(7, 11, 18, 185));
        g2.fillRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 14, 14);
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 170));
        g2.drawRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 14, 14);
        g2.setColor(accent);
        g2.fillOval(badgeX + 8, badgeY + 7, 8, 8);
        g2.setColor(new Color(255, 247, 219));
        g2.drawString(label, badgeX + 22, badgeY + 16);
    }

    private void drawInventoryBadge(Graphics2D g2, int screenWidth, int screenHeight, InventoryComponent inventory) {
        InventoryViewModel viewModel = InventoryViewModel.from(inventory);
        String statsText = UiText.hudInventoryStats(viewModel.coins(), viewModel.occupiedSlots(), INVENTORY_CAPACITY);
        int badgeWidth = Math.max(170, g2.getFontMetrics().stringWidth(statsText) + 24);
        int badgeHeight = 24;
        int badgeX = screenWidth - badgeWidth - 14;
        int badgeY = screenHeight - badgeHeight - 14;

        g2.setColor(new Color(7, 11, 18, 185));
        g2.fillRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 14, 14);
        g2.setColor(new Color(95, 117, 156, 170));
        g2.drawRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 14, 14);
        g2.setColor(new Color(255, 247, 219));
        g2.drawString(statsText, badgeX + 12, badgeY + 17);
    }

    private void drawTimeBadge(Graphics2D g2, int screenWidth, WorldTimeComponent worldTime) {
        if (worldTime == null) {
            return;
        }
        String label = UiText.worldTimeLabel(
                worldTime.dayNumber(),
                worldTime.hour(),
                worldTime.minute(),
                worldTime.second(),
                worldTime.isDay());
        int badgeWidth = Math.max(130, g2.getFontMetrics().stringWidth(label) + 24);
        int badgeHeight = 24;
        int badgeX = screenWidth - badgeWidth - 14;
        int badgeY = 42;
        Color accent = worldTime.isDay() ? new Color(201, 165, 92) : new Color(110, 150, 201);

        g2.setColor(new Color(7, 11, 18, 185));
        g2.fillRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 14, 14);
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 170));
        g2.drawRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 14, 14);
        g2.setColor(new Color(255, 247, 219));
        g2.drawString(label, badgeX + 12, badgeY + 17);
    }

    private void drawHudChip(Graphics2D g2, int x, int y, int width, String text, Color accent) {
        g2.setColor(new Color(7, 11, 18, 185));
        g2.fillRoundRect(x, y, width, 24, 14, 14);
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 170));
        g2.drawRoundRect(x, y, width, 24, 14, 14);
        g2.setColor(new Color(255, 247, 219));
        drawCenteredInBox(g2, text, x, y + 17, width);
    }

    public void drawDialogue(Graphics2D g2, UiState ui, int screenWidth, int screenHeight) {
        g2.setColor(new Color(0, 0, 0, 190));
        g2.fillRoundRect(32, screenHeight - 180, screenWidth - 64, 140, 24, 24);
        g2.setColor(Color.WHITE);
        g2.setFont(assets.font("body"));
        g2.drawString(ui.dialogueSpeaker, 56, screenHeight - 132);
        g2.setFont(assets.font("small"));
        int y = screenHeight - 100;
        for (String line : ui.dialogueLines) {
            g2.drawString(line, 56, y);
            y += 26;
        }
    }

    public void drawCompactGameOverOverlay(Graphics2D g2, List<String> options, int selectedIndex, int screenWidth,
            int screenHeight) {
        int itemHeight = 26;
        int gap = 10;
        int totalHeight = options.size() * itemHeight + Math.max(0, options.size() - 1) * gap;
        g2.setFont(assets.font("title").deriveFont(74f));
        String title = UiText.GAME_OVER_TITLE;
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        int titlePaddingX = 14;
        int titleBoxWidth = titleWidth + titlePaddingX * 2;
        int titleBoxHeight = 46;
        int menuWidth = 0;
        g2.setFont(assets.font("small"));
        for (int i = 0; i < options.size(); i++) {
            String optionText = (i == selectedIndex ? "> " : "") + options.get(i);
            menuWidth = Math.max(menuWidth, g2.getFontMetrics().stringWidth(optionText) + 26);
        }
        menuWidth = Math.min(menuWidth, screenWidth - 260);
        int menuX = (screenWidth - menuWidth) / 2;
        int menuY = (screenHeight - totalHeight) / 2 + 28;
        int titleBoxX = (screenWidth - titleBoxWidth) / 2;
        int titleBoxY = menuY - 68;
        g2.setFont(assets.font("title").deriveFont(74f));
        g2.setColor(new Color(44, 10, 10, 145));
        g2.fillRoundRect(titleBoxX, titleBoxY, titleBoxWidth, titleBoxHeight, 16, 16);
        g2.setColor(new Color(210, 78, 78, 165));
        g2.drawRoundRect(titleBoxX, titleBoxY, titleBoxWidth, titleBoxHeight, 16, 16);
        g2.setColor(new Color(255, 120, 120));
        int titleBaseline = titleBoxY + ((titleBoxHeight - g2.getFontMetrics().getHeight()) / 2) + g2.getFontMetrics().getAscent();
        g2.drawString(title, titleBoxX + titlePaddingX, titleBaseline);

        g2.setFont(assets.font("small"));
        for (int i = 0; i < options.size(); i++) {
            int y = menuY + i * (itemHeight + gap);
            boolean selected = i == selectedIndex;
            g2.setColor(selected ? new Color(66, 82, 114, 165) : new Color(20, 27, 38, 115));
            g2.fillRoundRect(menuX, y, menuWidth, itemHeight, 14, 14);
            g2.setColor(selected ? new Color(241, 220, 171, 200) : new Color(95, 117, 156, 95));
            g2.drawRoundRect(menuX, y, menuWidth, itemHeight, 14, 14);
            g2.setColor(selected ? new Color(255, 247, 219) : new Color(227, 231, 238));
            int baseline = y + ((itemHeight - g2.getFontMetrics().getHeight()) / 2) + g2.getFontMetrics().getAscent();
            g2.drawString((selected ? "> " : "") + options.get(i), menuX + 12, baseline);
        }
    }

    public void drawInventory(Graphics2D g2, UiState ui, InventoryComponent inventory, int screenWidth, int screenHeight) {
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
        g2.setFont(assets.font("body"));
        g2.drawString(UiText.INVENTORY, innerX + 16, innerY + 28);

        g2.setFont(assets.font("small"));
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
        BufferedImage icon = itemIcon(item.item());
        if (icon != null) {
            g2.drawImage(icon, iconX, iconY, iconSize, iconSize, null);
        } else {
            g2.setColor(new Color(241, 220, 171));
            g2.setFont(assets.font("body"));
            drawCenteredInBox(g2, itemFallbackLabel(item.item()), x, y + tileSize - 6, size);
        }

        g2.setFont(assets.font("small"));
        g2.setColor(Color.WHITE);
        drawCenteredInBox(g2, item.item().displayName(), x - 12, y + size + 16, size + 24);

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
            g2.setFont(assets.font("small"));
            g2.drawString(UiText.INVENTORY_EMPTY_SELECTION, iconFrameX + iconFrameSize + 14, y + 21);
            return;
        }

        BufferedImage icon = itemIcon(item.item());
        if (icon != null) {
            int iconSize = Math.max(16, tileSize - 8);
            int iconX = iconFrameX + (iconFrameSize - iconSize) / 2;
            int iconY = iconFrameY + (iconFrameSize - iconSize) / 2;
            g2.drawImage(icon, iconX, iconY, iconSize, iconSize, null);
        } else {
            g2.setColor(new Color(241, 220, 171));
            g2.setFont(assets.font("body"));
            drawCenteredInBox(g2, itemFallbackLabel(item.item()), iconFrameX, iconFrameY + iconFrameSize / 2 + 8, iconFrameSize);
        }

        int textX = iconFrameX + iconFrameSize + 14;
        g2.setColor(new Color(241, 220, 171));
        g2.setFont(assets.font("small"));
        g2.drawString(UiText.selectedItemLabel(item.item().displayName(), item.count()), textX, y + 18);

        g2.setColor(Color.WHITE);
        g2.setFont(assets.font("small"));
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

    private BufferedImage itemIcon(ItemData item) {
        try {
            return assets.image(item.iconId());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String itemFallbackLabel(ItemData item) {
        String label = item.displayName();
        return label.isBlank() ? "?" : label.substring(0, 1).toUpperCase();
    }

    private void drawCenteredInBox(Graphics2D g2, String text, int x, int baselineY, int width) {
        int textWidth = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, x + Math.max(0, (width - textWidth) / 2), baselineY);
    }

    private void drawCentered(Graphics2D g2, String text, int screenWidth, int y) {
        Font font = g2.getFont();
        int x = (screenWidth - g2.getFontMetrics(font).stringWidth(text)) / 2;
        g2.drawString(text, x, y);
    }

    private void drawCenteredParagraph(Graphics2D g2, String text, int screenWidth, int startY, int maxWidth,
            int lineHeight) {
        List<String> lines = wrapText(g2, text, maxWidth);
        int y = startY;
        for (String line : lines) {
            drawCentered(g2, line, screenWidth, y);
            y += lineHeight;
        }
    }

    private List<String> wrapText(Graphics2D g2, String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        for (String rawLine : text.split("\\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                lines.add("");
                continue;
            }

            String[] words = line.split("\\s+");
            StringBuilder current = new StringBuilder();
            for (String word : words) {
                String candidate = current.length() == 0 ? word : current + " " + word;
                if (g2.getFontMetrics().stringWidth(candidate) <= maxWidth) {
                    current.setLength(0);
                    current.append(candidate);
                } else {
                    if (current.length() > 0) {
                        lines.add(current.toString());
                    }
                    current.setLength(0);
                    current.append(word);
                }
            }

            if (current.length() > 0) {
                lines.add(current.toString());
            }
        }
        return lines;
    }
}
