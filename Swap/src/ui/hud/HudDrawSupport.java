package ui.hud;

import asset.AssetManager;
import content.catalog.ItemCatalog.ItemData;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

final class HudDrawSupport {
    private final AssetManager assets;
    private final int tileSize;

    HudDrawSupport(AssetManager assets, int tileSize) {
        this.assets = assets;
        this.tileSize = tileSize;
    }

    AssetManager assets() {
        return assets;
    }

    int tileSize() {
        return tileSize;
    }

    BufferedImage itemIcon(ItemData item) {
        try {
            return assets.image(item.iconId());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    String itemFallbackLabel(ItemData item) {
        String label = item.displayName();
        return label.isBlank() ? "?" : label.substring(0, 1).toUpperCase();
    }

    void drawCenteredInBox(Graphics2D g2, String text, int x, int baselineY, int width) {
        int textWidth = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, x + Math.max(0, (width - textWidth) / 2), baselineY);
    }

    void drawCentered(Graphics2D g2, String text, int screenWidth, int y) {
        Font font = g2.getFont();
        int x = (screenWidth - g2.getFontMetrics(font).stringWidth(text)) / 2;
        g2.drawString(text, x, y);
    }

    void drawCenteredParagraph(Graphics2D g2, String text, int screenWidth, int startY, int maxWidth, int lineHeight) {
        List<String> lines = wrapText(g2, text, maxWidth);
        int y = startY;
        for (String line : lines) {
            drawCentered(g2, line, screenWidth, y);
            y += lineHeight;
        }
    }

    List<String> wrapText(Graphics2D g2, String text, int maxWidth) {
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

    Color colorWithAlpha(Color base, int alpha, float multiplier) {
        int finalAlpha = Math.max(0, Math.min(255, Math.round(alpha * multiplier)));
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), finalAlpha);
    }
}
