package online.sync;

import java.util.List;
import java.util.Locale;

public record PlayerProgressSnapshot(
        String characterId,
        String characterName,
        String classId,
        int level,
        int masteryPoints,
        MasterySnapshot mastery,
        int hp,
        int maxHp,
        int coins,
        int enemiesKilled,
        EquipmentSnapshot equipment,
        AttributesSnapshot attributes,
        StatsSnapshot stats,
        List<String> inventory,
        List<String> quests) {
    public String toJson() {
        return """
                {
                  "character": {
                    "character_id": %s,
                    "name": %s,
                    "class_id": %s,
                    "level": %d,
                    "mastery_points": %d,
                    "mastery": %s,
                    "hp": %d,
                    "max_hp": %d,
                    "coins": %d,
                    "enemies_killed": %d,
                    "equipment": %s,
                    "attributes": %s,
                    "stats": %s,
                    "inventory": %s,
                    "quests": %s
                  }
                }
                """.formatted(
                quote(characterId),
                quote(characterName),
                quote(classId),
                level,
                masteryPoints,
                mastery.toJson(),
                hp,
                maxHp,
                coins,
                enemiesKilled,
                equipment.toJson(),
                attributes.toJson(),
                stats.toJson(),
                stringArray(inventory),
                stringArray(quests));
    }

    public record MasterySnapshot(
            int offense,
            int skill,
            int defense) {
        String toJson() {
            return """
                    {
                      "offense": %d,
                      "skill": %d,
                      "defense": %d
                    }
                    """.formatted(offense, skill, defense);
        }
    }

    public record EquipmentSnapshot(
            String weapon,
            String offhand,
            String armor,
            String boots,
            String accessory) {
        String toJson() {
            return """
                    {
                      "weapon": %s,
                      "offhand": %s,
                      "armor": %s,
                      "boots": %s,
                      "accessory": %s
                    }
                    """.formatted(
                    quote(weapon),
                    quote(offhand),
                    quote(armor),
                    quote(boots),
                    quote(accessory));
        }
    }

    public record AttributesSnapshot(
            int sta,
            int str,
            int intel,
            int agi,
            int spi) {
        String toJson() {
            return """
                    {
                      "sta": %d,
                      "str": %d,
                      "int": %d,
                      "agi": %d,
                      "spi": %d
                    }
                    """.formatted(sta, str, intel, agi, spi);
        }
    }

    public record StatsSnapshot(
            int mana,
            double attack,
            double dps,
            double abilityPower,
            double defense,
            double healingPower) {
        String toJson() {
            return String.format(Locale.US, """
                    {
                      "mana": %d,
                      "attack": %.1f,
                      "dps": %.1f,
                      "ability_power": %.1f,
                      "defense": %.1f,
                      "healing_power": %.1f
                    }
                    """, mana, attack, dps, abilityPower, defense, healingPower);
        }
    }

    private static String stringArray(List<String> values) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(quote(values.get(i)));
        }
        builder.append(']');
        return builder.toString();
    }

    private static String quote(String value) {
        String text = value == null ? "" : value;
        return "\"" + text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }
}
