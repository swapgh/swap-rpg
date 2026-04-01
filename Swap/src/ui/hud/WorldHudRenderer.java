package ui.hud;

import component.combat.HealthComponent;
import component.progression.EquipmentComponent;
import component.progression.InventoryComponent;
import component.progression.ProgressionComponent;
import component.progression.QuestComponent;
import component.world.WorldTimeComponent;
import component.world.WorldTierComponent;
import app.GameConfig;
import data.DataRegistry;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import progression.DerivedStatsSnapshot;
import progression.ProgressionCalculator;
import ui.runtime.UiState;
import ui.text.UiText;
import ui.viewmodel.InventoryViewModel;

final class WorldHudRenderer {
    private static final int INVENTORY_CAPACITY = 30;
    private static final int SYSTEM_LOG_VISIBLE_TICKS = 300;

    private final HudDrawSupport support;
    private final DataRegistry data;
    private final int tileSize;

    WorldHudRenderer(HudDrawSupport support, DataRegistry data, int tileSize) {
        this.support = support;
        this.data = data;
        this.tileSize = tileSize;
    }

    void drawWorldHud(Graphics2D g2, UiState ui, int screenWidth, int screenHeight, HealthComponent health,
            InventoryComponent inventory, ProgressionComponent progression, EquipmentComponent equipment, QuestComponent quests,
            WorldTimeComponent worldTime, WorldTierComponent worldTier, String accountLabel,
            boolean accountLoggedIn) {
        drawVitalsCard(g2, health, progression, equipment);

        g2.setFont(support.assets().font("small"));
        drawConnectionBadge(g2, screenWidth, accountLabel, accountLoggedIn);
        drawTimeBadge(g2, screenWidth, worldTime);
        drawWorldTierBadge(g2, screenWidth, worldTier);
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
            int toastY = 84;
            g2.setColor(new Color(9, 14, 20, 195));
            g2.fillRoundRect(toastX, toastY, toastWidth, toastHeight, 14, 14);
            g2.setColor(new Color(201, 110, 110, 170));
            g2.drawRoundRect(toastX, toastY, toastWidth, toastHeight, 14, 14);
            g2.setColor(Color.WHITE);
            g2.drawString(ui.combatToast, toastX + 11, toastY + 16);
        }

        drawSystemLog(g2, ui, screenHeight);
    }

    private void drawVitalsCard(Graphics2D g2, HealthComponent health, ProgressionComponent progression, EquipmentComponent equipment) {
        DerivedStatsSnapshot snapshot = ProgressionCalculator.snapshot(
                data.rpgClass(progression.classId),
                data.progressionRules(),
                progression,
                equipment);
        int cardX = 12;
        int cardY = 12;
        int cardWidth = 220;
        int cardHeight = 86;

        g2.setColor(new Color(7, 11, 18, 185));
        g2.fillRoundRect(cardX, cardY, cardWidth, cardHeight, 18, 18);
        g2.setColor(new Color(95, 117, 156, 120));
        g2.drawRoundRect(cardX, cardY, cardWidth, cardHeight, 18, 18);

        boolean atCap = progression.level >= GameConfig.MAX_CHARACTER_LEVEL;
        g2.setColor(new Color(241, 220, 171));
        g2.drawString(UiText.LABEL_LEVEL + " " + progression.level + "  " + progression.classId.toUpperCase(), cardX + 12, cardY + 16);
        drawBar(g2, cardX + 12, cardY + 24, cardWidth - 24, 14, new Color(144, 54, 54), health.current, snapshot.hp(),
                UiText.LABEL_HP + " " + health.current + "/" + snapshot.hp());
        drawBar(g2, cardX + 12, cardY + 44, cardWidth - 24, 14, new Color(57, 91, 160), snapshot.mana(), snapshot.mana(),
                UiText.LABEL_MANA + " " + snapshot.mana());
        drawBar(g2, cardX + 12, cardY + 64, cardWidth - 24, 14, atCap ? new Color(116, 91, 154) : new Color(104, 126, 54),
                atCap ? progression.masteryExperience : progression.experience,
                atCap ? GameConfig.MASTERY_XP_PER_POINT : ProgressionCalculator.xpToNextLevel(progression.level),
                atCap ? "MSTR " + progression.masteryPoints + "  XP " + progression.masteryExperience + "/" + GameConfig.MASTERY_XP_PER_POINT
                        : "XP " + progression.experience + "/" + ProgressionCalculator.xpToNextLevel(progression.level));
    }

    private void drawBar(Graphics2D g2, int x, int y, int width, int height, Color fill, int current, int max, String label) {
        int safeMax = Math.max(1, max);
        int fillWidth = (int) Math.round(width * (Math.max(0, current) / (double) safeMax));
        g2.setColor(new Color(17, 24, 35, 220));
        g2.fillRoundRect(x, y, width, height, 10, 10);
        g2.setColor(fill);
        g2.fillRoundRect(x, y, fillWidth, height, 10, 10);
        g2.setColor(new Color(210, 220, 236, 110));
        g2.drawRoundRect(x, y, width, height, 10, 10);
        g2.setColor(Color.WHITE);
        support.drawCenteredInBox(g2, label, x, y + 11, width);
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

    private void drawWorldTierBadge(Graphics2D g2, int screenWidth, WorldTierComponent worldTier) {
        if (worldTier == null) {
            return;
        }
        String label = "WT" + worldTier.tier;
        int badgeWidth = Math.max(66, g2.getFontMetrics().stringWidth(label) + 24);
        int badgeHeight = 24;
        int badgeX = screenWidth - badgeWidth - 14;
        int badgeY = 70;

        g2.setColor(new Color(7, 11, 18, 185));
        g2.fillRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 14, 14);
        g2.setColor(new Color(173, 104, 76, 170));
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
