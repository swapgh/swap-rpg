package online;

import java.util.List;

public record PlayerProgressSnapshot(
        String characterName,
        String classId,
        int level,
        int hp,
        int maxHp,
        int coins,
        int enemiesKilled,
        List<String> inventory,
        List<String> quests) {
    public String toJson() {
        return """
                {
                  "character": {
                    "name": %s,
                    "class_id": %s,
                    "level": %d,
                    "hp": %d,
                    "max_hp": %d,
                    "coins": %d,
                    "enemies_killed": %d,
                    "inventory": %s,
                    "quests": %s
                  }
                }
                """.formatted(
                quote(characterName),
                quote(classId),
                level,
                hp,
                maxHp,
                coins,
                enemiesKilled,
                stringArray(inventory),
                stringArray(quests));
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
