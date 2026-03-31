package ui.hud;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import ui.runtime.UiState;

final class OverlayHudRenderer {
    private final HudDrawSupport support;

    OverlayHudRenderer(HudDrawSupport support) {
        this.support = support;
    }

    void drawDialogue(Graphics2D g2, UiState ui, int screenWidth, int screenHeight) {
        g2.setColor(new Color(0, 0, 0, 190));
        g2.fillRoundRect(32, screenHeight - 180, screenWidth - 64, 140, 24, 24);
        g2.setColor(Color.WHITE);
        g2.setFont(support.assets().font("body"));
        g2.drawString(ui.dialogueSpeaker, 56, screenHeight - 132);
        g2.setFont(support.assets().font("small"));
        int y = screenHeight - 100;
        for (String line : ui.dialogueLines) {
            g2.drawString(line, 56, y);
            y += 26;
        }
    }

    void drawCompactGameOverOverlay(Graphics2D g2, String title, List<String> options, int selectedIndex,
            String statusMessage, int screenWidth, int screenHeight) {
        int itemHeight = 26;
        int gap = 10;
        int totalHeight = options.size() * itemHeight + Math.max(0, options.size() - 1) * gap;
        g2.setFont(support.assets().font("title").deriveFont(74f));
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        int titlePaddingX = 14;
        int titleBoxWidth = titleWidth + titlePaddingX * 2;
        int titleBoxHeight = 46;
        int menuWidth = 0;
        g2.setFont(support.assets().font("small"));
        for (int i = 0; i < options.size(); i++) {
            String optionText = (i == selectedIndex ? "> " : "") + options.get(i);
            menuWidth = Math.max(menuWidth, g2.getFontMetrics().stringWidth(optionText) + 26);
        }
        menuWidth = Math.min(menuWidth, screenWidth - 260);
        int menuX = (screenWidth - menuWidth) / 2;
        int menuY = (screenHeight - totalHeight) / 2 + 28;
        int titleBoxX = (screenWidth - titleBoxWidth) / 2;
        int titleBoxY = menuY - 68;
        g2.setFont(support.assets().font("title").deriveFont(74f));
        g2.setColor(new Color(44, 10, 10, 145));
        g2.fillRoundRect(titleBoxX, titleBoxY, titleBoxWidth, titleBoxHeight, 16, 16);
        g2.setColor(new Color(210, 78, 78, 165));
        g2.drawRoundRect(titleBoxX, titleBoxY, titleBoxWidth, titleBoxHeight, 16, 16);
        g2.setColor(new Color(255, 120, 120));
        int titleBaseline = titleBoxY + ((titleBoxHeight - g2.getFontMetrics().getHeight()) / 2)
                + g2.getFontMetrics().getAscent();
        g2.drawString(title, titleBoxX + titlePaddingX, titleBaseline);

        g2.setFont(support.assets().font("small"));
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

        if (statusMessage != null && !statusMessage.isBlank()) {
            g2.setColor(new Color(255, 247, 219));
            support.drawCentered(g2, statusMessage, screenWidth, menuY + totalHeight + 28);
        }
    }

    void drawCompactMenuOverlay(Graphics2D g2, String title, List<String> options, int selectedIndex,
            String statusMessage, int screenWidth, int screenHeight) {
        drawCompactGameOverOverlay(g2, title, options, selectedIndex, statusMessage, screenWidth, screenHeight);
    }
}
