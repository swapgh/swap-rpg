package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import asset.AssetManager;
import component.HealthComponent;
import component.InventoryComponent;
import component.QuestComponent;

public final class HudRenderer {
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

    public void drawWorldHud(Graphics2D g2, UiState ui, int screenWidth, HealthComponent health,
            InventoryComponent inventory, QuestComponent quests, String accountLabel, boolean accountLoggedIn) {
        int x = tileSize / 2;
        int y = tileSize / 2;
        for (int i = 0; i < health.max / 2; i++) {
            g2.drawImage(assets.image("ui.heartBlank"), x, y, null);
            x += tileSize;
        }
        x = tileSize / 2;
        int remaining = health.current;
        while (remaining > 0) {
            g2.drawImage(assets.image("ui.heartHalf"), x, y, null);
            if (remaining > 1) {
                g2.drawImage(assets.image("ui.heartFull"), x, y, null);
            }
            x += tileSize;
            remaining -= 2;
        }

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(screenWidth - 260, 16, 240, 112, 18, 18);
        g2.setColor(Color.WHITE);
        g2.setFont(assets.font("small"));
        g2.drawString("Monedas: " + inventory.coins, screenWidth - 240, 42);
        g2.drawString("Items: " + inventory.itemIds, screenWidth - 240, 62);
        g2.drawString("Quests: " + quests.completed.size() + "/" + (quests.active.size() + quests.completed.size()),
                screenWidth - 240, 82);
        g2.drawString("Cuenta: " + (accountLoggedIn ? accountLabel : "Invitado"), screenWidth - 240, 102);

        if (ui.toastTicks > 0 && ui.toast != null && !ui.toast.isBlank()) {
            g2.setColor(new Color(0, 0, 0, 170));
            g2.fillRoundRect(20, screenWidth > 700 ? 100 : 80, 320, 46, 18, 18);
            g2.setColor(Color.WHITE);
            g2.drawString(ui.toast, 36, screenWidth > 700 ? 130 : 110);
        }
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

    public void drawInventory(Graphics2D g2, InventoryComponent inventory, int screenWidth, int screenHeight) {
        g2.setColor(new Color(12, 16, 22, 220));
        g2.fillRoundRect(screenWidth / 4, screenHeight / 5, screenWidth / 2, screenHeight / 2, 24, 24);
        g2.setColor(Color.WHITE);
        g2.setFont(assets.font("body"));
        g2.drawString("Inventario", screenWidth / 4 + 24, screenHeight / 5 + 40);
        g2.setFont(assets.font("small"));
        int y = screenHeight / 5 + 76;
        g2.drawString("Monedas: " + inventory.coins, screenWidth / 4 + 24, y);
        y += 28;
        for (String item : inventory.itemIds) {
            g2.drawString("- " + item, screenWidth / 4 + 24, y);
            y += 24;
        }
        if (inventory.itemIds.isEmpty()) {
            g2.drawString("- vacio -", screenWidth / 4 + 24, y);
        }
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
