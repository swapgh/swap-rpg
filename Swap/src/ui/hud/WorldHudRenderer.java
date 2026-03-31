package ui.hud;

import component.HealthComponent;
import component.InventoryComponent;
import component.QuestComponent;
import component.WorldTimeComponent;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import ui.runtime.UiState;
import ui.text.UiText;
import ui.viewmodel.InventoryViewModel;

final class WorldHudRenderer {
    private static final int INVENTORY_CAPACITY = 30;
    private static final int SYSTEM_LOG_VISIBLE_TICKS = 300;

    private final HudDrawSupport support;
    private final int tileSize;

    WorldHudRenderer(HudDrawSupport support, int tileSize) {
        this.support = support;
        this.tileSize = tileSize;
    }

    void drawWorldHud(Graphics2D g2, UiState ui, int screenWidth, int screenHeight, HealthComponent health,
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
            g2.drawImage(support.assets().image("ui.heartBlank"), x, y, heartSize, heartSize, null);
            x += heartSize;
        }
        x = heartsX;
        int remaining = health.current;
        while (remaining > 0) {
            g2.drawImage(support.assets().image("ui.heartHalf"), x, y, heartSize, heartSize, null);
            if (remaining > 1) {
                g2.drawImage(support.assets().image("ui.heartFull"), x, y, heartSize, heartSize, null);
            }
            x += heartSize;
            remaining -= 2;
        }

        g2.setFont(support.assets().font("small"));
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
        Font logFont = support.assets().font("small").deriveFont(14f);
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
        Color accent = ui.toastTicks > 0 ? support.colorWithAlpha(new Color(201, 165, 92), 190, panelFade)
                : support.colorWithAlpha(new Color(95, 117, 156), 170, panelFade);

        g2.setColor(support.colorWithAlpha(new Color(9, 14, 20), 210, panelFade));
        g2.fillRoundRect(logX, logY, logWidth, logHeight, 18, 18);
        g2.setColor(accent);
        g2.drawRoundRect(logX, logY, logWidth, logHeight, 18, 18);

        g2.setColor(support.colorWithAlpha(new Color(241, 220, 171), 255, panelFade));
        g2.drawString(UiText.LOGIN_LOG, logX + 12, logY + 16);
        g2.setColor(support.colorWithAlpha(new Color(189, 196, 207), 255, panelFade));
        String toggleLabel = ui.systemLogExpanded ? UiText.LOGIN_CLOSE_LOG : UiText.LOGIN_OPEN_LOG;
        g2.drawString(toggleLabel, logX + logWidth - g2.getFontMetrics().stringWidth(toggleLabel) - 12, logY + 16);

        int lineY = logY + headerHeight + 2;
        for (SystemLogEntry line : lines) {
            float lineFade = ui.systemLogExpanded ? 1f : messageAlpha(line.age());
            g2.setColor(support.colorWithAlpha(Color.WHITE, 255, lineFade));
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

    @SuppressWarnings("unused")
    private void drawHudChip(Graphics2D g2, int x, int y, int width, String text, Color accent) {
        g2.setColor(new Color(7, 11, 18, 185));
        g2.fillRoundRect(x, y, width, 24, 14, 14);
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 170));
        g2.drawRoundRect(x, y, width, 24, 14, 14);
        g2.setColor(new Color(255, 247, 219));
        support.drawCenteredInBox(g2, text, x, y + 17, width);
    }

    private record SystemLogEntry(String text, int age) {
    }
}
