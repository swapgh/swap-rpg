package ui.hud;

import component.actor.NameComponent;
import component.combat.HealthComponent;
import component.progression.EquipmentComponent;
import component.progression.ProgressionComponent;
import content.catalog.ItemCatalog;
import data.DataRegistry;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import progression.DerivedStatsSnapshot;
import progression.ProgressionCalculator;
import ui.text.UiText;

final class CharacterHudRenderer {
    private final HudDrawSupport support;
    private final DataRegistry data;
    private final int tileSize;

    CharacterHudRenderer(HudDrawSupport support, DataRegistry data, int tileSize) {
        this.support = support;
        this.data = data;
        this.tileSize = tileSize;
    }

    void drawCharacter(Graphics2D g2, NameComponent name, HealthComponent health, ProgressionComponent progression,
            EquipmentComponent equipment, int screenWidth, int screenHeight, boolean alongsideInventory) {
        DerivedStatsSnapshot snapshot = ProgressionCalculator.snapshot(
                data.rpgClass(progression.classId),
                data.progressionRules(),
                progression.level);

        CharacterLayout layout = CharacterLayout.create(screenWidth, screenHeight, alongsideInventory);
        int panelX = layout.panelX();
        int panelY = layout.panelY();
        int panelWidth = layout.panelWidth();
        int panelHeight = layout.panelHeight();
        int leftX = panelX + 24;
        int topY = panelY + 22;
        int portraitWidth = layout.portraitWidth();
        int portraitHeight = layout.portraitHeight();
        int portraitX = panelX + (panelWidth - portraitWidth) / 2;
        int portraitY = panelY + 82;
        int portraitCenterX = portraitX + portraitWidth / 2;
        int slotSize = layout.slotSize();
        int slotOffset = layout.slotOffset();

        if (!alongsideInventory) {
            g2.setColor(new Color(5, 8, 14, 185));
            g2.fillRect(0, 0, screenWidth, screenHeight);
        }

        g2.setColor(new Color(7, 11, 18, 242));
        g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 34, 34);
        g2.setColor(new Color(78, 99, 138, 110));
        g2.fillRoundRect(panelX + 10, panelY + 10, panelWidth - 20, panelHeight - 20, 28, 28);
        g2.setColor(new Color(14, 20, 31, 244));
        g2.fillRoundRect(panelX + 18, panelY + 18, panelWidth - 36, panelHeight - 36, 26, 26);

        g2.setColor(new Color(241, 220, 171));
        g2.setFont(support.assets().font("body"));
        g2.drawString(name.value, leftX, topY + 8);
        g2.setFont(support.assets().font("small"));
        g2.setColor(Color.WHITE);
        g2.drawString(UiText.CHARACTER_CLOSE, panelX + panelWidth - 100, topY + 8);
        g2.drawString(UiText.LABEL_LEVEL + " " + progression.level, leftX, topY + 34);
        g2.drawString(UiText.LABEL_CLASS + " " + progression.classId.toUpperCase(), leftX + 120, topY + 34);

        drawPortraitPanel(g2, portraitX, portraitY, portraitWidth, portraitHeight);
        drawEquipmentSlots(g2, equipment, portraitCenterX, portraitY, slotSize, slotOffset);
        int bottomPanelY = portraitY + portraitHeight + 18;
        int leftPanelX = panelX + 16;
        int leftPanelWidth = (panelWidth - 48) / 2;
        int rightPanelX = leftPanelX + leftPanelWidth + 16;
        int rightPanelWidth = panelWidth - 16 - rightPanelX - panelX;
        int statsHeight = panelHeight - (bottomPanelY - panelY) - 16;
        drawBaseStatsPanel(g2, snapshot, leftPanelX, bottomPanelY, leftPanelWidth, statsHeight);
        drawDerivedStatsPanel(g2, snapshot, health, rightPanelX, bottomPanelY, rightPanelWidth, statsHeight);
    }

    private void drawPortraitPanel(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(new Color(18, 25, 36, 236));
        g2.fillRoundRect(x, y, width, height, 24, 24);
        g2.setColor(new Color(122, 145, 183, 120));
        g2.drawRoundRect(x, y, width, height, 24, 24);
        g2.setColor(new Color(20, 30, 44, 180));
        g2.fillOval(x + 28, y + 20, width - 56, height - 40);

        BufferedImage player = support.assets().image("player.down.1");
        int drawW = width - 56;
        int drawH = height - 28;
        g2.drawImage(player, x + (width - drawW) / 2, y + 10, drawW, drawH, null);
    }

    private void drawEquipmentSlots(Graphics2D g2, EquipmentComponent equipment, int centerX, int baseY, int slotSize,
            int slotOffset) {
        drawEquipmentSlot(g2, equipment.weaponItemId, centerX - slotOffset - slotSize, baseY + 18, UiText.CHARACTER_SLOT_WEAPON, slotSize);
        drawEquipmentSlot(g2, equipment.offhandItemId, centerX + slotOffset, baseY + 18, UiText.CHARACTER_SLOT_OFFHAND, slotSize);
        drawEquipmentSlot(g2, equipment.armorItemId, centerX - slotOffset - slotSize, baseY + 18 + slotSize + 20, UiText.CHARACTER_SLOT_ARMOR,
                slotSize);
        drawEquipmentSlot(g2, equipment.bootsItemId, centerX + slotOffset, baseY + 18 + slotSize + 20, UiText.CHARACTER_SLOT_BOOTS, slotSize);
    }

    private void drawEquipmentSlot(Graphics2D g2, String itemId, int x, int y, String fallback, int size) {
        g2.setColor(new Color(24, 33, 48, 228));
        g2.fillRoundRect(x, y, size, size, 16, 16);
        g2.setColor(new Color(95, 117, 156, 160));
        g2.drawRoundRect(x, y, size, size, 16, 16);
        BufferedImage icon = null;
        if (itemId != null && !itemId.isBlank()) {
            icon = support.itemIcon(ItemCatalog.get(itemId));
        }
        if (icon != null) {
            g2.drawImage(icon, x + 7, y + 7, size - 14, size - 14, null);
        } else {
            g2.setColor(new Color(201, 165, 92));
            g2.setFont(support.assets().font("small"));
            support.drawCenteredInBox(g2, fallback, x, y + 32, size);
        }
    }

    private void drawBaseStatsPanel(Graphics2D g2, DerivedStatsSnapshot snapshot, int x, int y, int width, int height) {
        g2.setColor(new Color(18, 25, 36, 236));
        g2.fillRoundRect(x, y, width, height, 24, 24);
        g2.setColor(new Color(122, 145, 183, 120));
        g2.drawRoundRect(x, y, width, height, 24, 24);

        g2.setColor(new Color(241, 220, 171));
        g2.setFont(support.assets().font("small"));
        g2.drawString(UiText.CHARACTER_ATTRIBUTES, x + 14, y + 20);
        int lineY = y + 42;
        int valueX = x + Math.max(60, width - 48);
        drawStatLine(g2, "STA", Integer.toString(snapshot.attributes().sta()), x + 16, valueX, lineY);
        lineY += 16;
        drawStatLine(g2, "STR", Integer.toString(snapshot.attributes().str()), x + 16, valueX, lineY);
        lineY += 16;
        drawStatLine(g2, "INT", Integer.toString(snapshot.attributes().intel()), x + 16, valueX, lineY);
        lineY += 16;
        drawStatLine(g2, "AGI", Integer.toString(snapshot.attributes().agi()), x + 16, valueX, lineY);
        lineY += 16;
        drawStatLine(g2, "SPI", Integer.toString(snapshot.attributes().spi()), x + 16, valueX, lineY);
    }

    private void drawDerivedStatsPanel(Graphics2D g2, DerivedStatsSnapshot snapshot, HealthComponent health, int x, int y,
            int width, int height) {
        g2.setColor(new Color(18, 25, 36, 236));
        g2.fillRoundRect(x, y, width, height, 24, 24);
        g2.setColor(new Color(122, 145, 183, 120));
        g2.drawRoundRect(x, y, width, height, 24, 24);

        g2.setColor(new Color(241, 220, 171));
        g2.setFont(support.assets().font("small"));
        g2.drawString(UiText.CHARACTER_STATS, x + 14, y + 20);
        int lineY = y + 42;
        int valueX = x + Math.max(54, width - 60);
        drawStatLine(g2, UiText.LABEL_HP, health.current + "/" + snapshot.hp(), x + 16, valueX, lineY);
        lineY += 16;
        drawStatLine(g2, UiText.LABEL_MANA, Integer.toString(snapshot.mana()), x + 16, valueX, lineY);
        lineY += 16;
        drawStatLine(g2, "ATK", format(snapshot.attack()), x + 16, valueX, lineY);
        lineY += 16;
        drawStatLine(g2, "DPS", format(snapshot.dps()), x + 16, valueX, lineY);
        lineY += 16;
        drawStatLine(g2, "AbP", format(snapshot.abilityPower()), x + 16, valueX, lineY);
        lineY += 16;
        drawStatLine(g2, "DEF", format(snapshot.defense()), x + 16, valueX, lineY);
        lineY += 16;
        drawStatLine(g2, "HEAL", format(snapshot.healingPower()), x + 16, valueX, lineY);
    }

    private void drawStatLine(Graphics2D g2, String label, String value, int x, int valueX, int baselineY) {
        g2.setColor(new Color(201, 165, 92));
        g2.drawString(label, x, baselineY);
        g2.setColor(Color.WHITE);
        g2.drawString(value, valueX, baselineY);
    }

    private String format(double value) {
        return String.format("%.1f", value);
    }

    private record CharacterLayout(
            int panelX,
            int panelY,
            int panelWidth,
            int panelHeight,
            int portraitWidth,
            int portraitHeight,
            int slotSize,
            int slotOffset) {

        static CharacterLayout create(int screenWidth, int screenHeight, boolean alongsideInventory) {
            if (alongsideInventory) {
                int panelY = screenHeight / 10;
                int panelHeight = screenHeight * 4 / 5;
                int gap = 18;
                int leftWidth = screenWidth * 5 / 14;
                int rightWidth = screenWidth * 5 / 12;
                int totalWidth = leftWidth + gap + rightWidth;
                int panelX = (screenWidth - totalWidth) / 2;
                return new CharacterLayout(panelX, panelY, leftWidth, panelHeight, 116, 146, 42, 30);
            }

            int panelWidth = Math.min(430, screenWidth - 120);
            int panelHeight = Math.min(430, screenHeight - 90);
            int panelX = (screenWidth - panelWidth) / 2;
            int panelY = (screenHeight - panelHeight) / 2;
            return new CharacterLayout(panelX, panelY, panelWidth, panelHeight, 132, 168, 46, 34);
        }
    }
}
