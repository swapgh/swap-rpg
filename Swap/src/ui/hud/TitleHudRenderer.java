package ui.hud;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import ui.runtime.UiState;
import ui.text.UiText;

final class TitleHudRenderer {
    private final HudDrawSupport support;

    TitleHudRenderer(HudDrawSupport support) {
        this.support = support;
    }

    void drawTitle(Graphics2D g2, UiState ui, int screenWidth, int screenHeight) {
        g2.setColor(new Color(18, 24, 33));
        g2.fillRect(0, 0, screenWidth, screenHeight);
        g2.setColor(new Color(241, 220, 171));
        g2.setFont(support.assets().font("title"));
        support.drawCentered(g2, ui.titleMessage, screenWidth, screenHeight / 3);
        g2.setFont(support.assets().font("body"));
        support.drawCenteredParagraph(g2, ui.subtitleMessage, screenWidth, screenHeight / 2, screenWidth - 96, 42);
    }

    void drawTitleMenu(Graphics2D g2, String title, String sectionLabel, String accountLabel, boolean loggedIn,
            List<String> options, int selectedIndex, String footer, int screenWidth, int screenHeight) {
        g2.setColor(new Color(17, 22, 30));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(new Color(26, 35, 49));
        g2.fillRoundRect(52, 42, screenWidth - 104, screenHeight - 84, 34, 34);
        g2.setColor(new Color(78, 96, 133, 140));
        g2.drawRoundRect(52, 42, screenWidth - 104, screenHeight - 84, 34, 34);

        g2.setColor(new Color(241, 220, 171));
        g2.setFont(support.assets().font("title"));
        g2.drawString(title, 82, 138);

        g2.setFont(support.assets().font("small"));
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
        int itemHeight = 48;
        int gap = 12;
        int footerY = screenHeight - 56;
        int availableHeight = footerY - 24 - menuY;
        int totalHeight = options.size() * itemHeight + Math.max(0, options.size() - 1) * gap;
        if (totalHeight > availableHeight && options.size() > 1) {
            gap = 8;
            itemHeight = Math.max(40, (availableHeight - ((options.size() - 1) * gap)) / options.size());
        }
        g2.setFont(support.assets().font("body"));
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

        g2.setFont(support.assets().font("small"));
        if (footer != null && !footer.isBlank()) {
            g2.setColor(new Color(241, 220, 171));
            g2.drawString(footer, 86, footerY);
        }
    }
}
