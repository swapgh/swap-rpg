package data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads gameplay data records from JSON resources.
 *
 * The project stays on plain Java, so this loader includes a very small JSON parser
 * instead of depending on external libraries. It only supports the JSON features this
 * content pipeline needs: objects, arrays, strings, numbers, booleans and null.
 */
public final class JsonDataLoader {
    public PlayerData loadPlayer(String id, String resourcePath) {
        Map<String, Object> root = object(loadJson(resourcePath));
        return new PlayerData(
                id,
                string(root, "name"),
                string(root, "faction", "player"),
                spawn(object(root, "spawn")),
                visual(object(root, "visual")),
                collider(object(root, "collider")),
                stats(object(root, "stats")),
                attack(object(root, "attack")),
                projectile(object(root, "projectile")),
                flags(object(root, "flags")));
    }

    public EnemyData loadEnemy(String id, String resourcePath) {
        Map<String, Object> root = object(loadJson(resourcePath));
        return new EnemyData(
                id,
                string(root, "name"),
                string(root, "faction", "enemy"),
                visual(object(root, "visual")),
                collider(object(root, "collider")),
                stats(object(root, "stats")),
                projectile(object(root, "projectile")),
                flags(object(root, "flags")),
                bool(root, "wander"));
    }

    public NpcData loadNpc(String id, String resourcePath) {
        Map<String, Object> root = object(loadJson(resourcePath));
        return new NpcData(
                id,
                string(root, "name"),
                string(root, "faction", "npc"),
                visual(object(root, "visual")),
                collider(object(root, "collider")),
                flags(object(root, "flags")),
                stringArray(root, "dialogue"));
    }

    private Object loadJson(String resourcePath) {
        try (InputStream is = JsonDataLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Missing data resource: " + resourcePath);
            }
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return new Parser(json).parseValue();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load data " + resourcePath, ex);
        }
    }

    private static SpawnData spawn(Map<String, Object> value) {
        return new SpawnData(integer(value, "tileX"), integer(value, "tileY"));
    }

    private static VisualData visual(Map<String, Object> value) {
        return new VisualData(
                string(value, "idleBase"),
                string(value, "walkBase"),
                string(value, "attackBase", ""),
                string(value, "initialFacing", "down"),
                integer(value, "initialFrame", 1),
                integer(value, "layer"),
                integer(value, "animationFrameTicks"));
    }

    private static ColliderData collider(Map<String, Object> value) {
        return new ColliderData(
                integer(value, "offsetX"),
                integer(value, "offsetY"),
                integer(value, "width"),
                integer(value, "height"));
    }

    private static StatsData stats(Map<String, Object> value) {
        return new StatsData(
                integer(value, "health"),
                integer(value, "speed"),
                integer(value, "attack"),
                integer(value, "defense"));
    }

    private static AttackData attack(Map<String, Object> value) {
        return new AttackData(
                integer(value, "damage"),
                number(value, "rangeScale"),
                integer(value, "cooldownTicks"));
    }

    private static ProjectileData projectile(Map<String, Object> value) {
        return new ProjectileData(
                bool(value, "enabled"),
                string(value, "spriteId", ""),
                integer(value, "speed"),
                integer(value, "damage"),
                integer(value, "lifetimeTicks"),
                integer(value, "cooldownTicks"),
                number(value, "sizeScale", 0.5),
                string(value, "targetFaction", ""),
                bool(value, "playerTriggered"),
                bool(value, "aimAtPlayer"));
    }

    private static FlagsData flags(Map<String, Object> value) {
        return new FlagsData(bool(value, "solid"), bool(value, "cameraTarget"));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new IllegalArgumentException("Expected JSON object");
    }

    private static Map<String, Object> object(Map<String, Object> value, String key) {
        Object nested = value.get(key);
        if (nested == null) {
            throw new IllegalArgumentException("Missing object key: " + key);
        }
        return object(nested);
    }

    private static String string(Map<String, Object> value, String key) {
        return string(value, key, null);
    }

    private static String string(Map<String, Object> value, String key, String defaultValue) {
        Object nested = value.get(key);
        if (nested == null) {
            if (defaultValue != null) {
                return defaultValue;
            }
            throw new IllegalArgumentException("Missing string key: " + key);
        }
        if (nested instanceof String text) {
            return text;
        }
        throw new IllegalArgumentException("Expected string for key: " + key);
    }

    private static int integer(Map<String, Object> value, String key) {
        return integer(value, key, 0);
    }

    private static int integer(Map<String, Object> value, String key, int defaultValue) {
        Object nested = value.get(key);
        if (nested == null) {
            return defaultValue;
        }
        if (nested instanceof Number number) {
            return number.intValue();
        }
        throw new IllegalArgumentException("Expected number for key: " + key);
    }

    private static double number(Map<String, Object> value, String key) {
        return number(value, key, 0.0);
    }

    private static double number(Map<String, Object> value, String key, double defaultValue) {
        Object nested = value.get(key);
        if (nested == null) {
            return defaultValue;
        }
        if (nested instanceof Number number) {
            return number.doubleValue();
        }
        throw new IllegalArgumentException("Expected number for key: " + key);
    }

    private static boolean bool(Map<String, Object> value, String key) {
        Object nested = value.get(key);
        if (nested == null) {
            return false;
        }
        if (nested instanceof Boolean flag) {
            return flag;
        }
        throw new IllegalArgumentException("Expected boolean for key: " + key);
    }

    private static String[] stringArray(Map<String, Object> value, String key) {
        Object nested = value.get(key);
        if (!(nested instanceof List<?> list)) {
            throw new IllegalArgumentException("Expected array for key: " + key);
        }
        String[] result = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            if (!(item instanceof String text)) {
                throw new IllegalArgumentException("Expected string in array: " + key);
            }
            result[i] = text;
        }
        return result;
    }

    /**
     * Minimal recursive-descent parser for the project's JSON content files.
     *
     * Keeping this parser private to JsonDataLoader makes it clear that the rest of the
     * codebase should deal with typed records, not generic JSON trees.
     */
    private static final class Parser {
        private final String text;
        private int index;

        private Parser(String text) {
            this.text = text;
        }

        private Object parseValue() {
            skipWhitespace();
            if (index >= text.length()) {
                throw new IllegalArgumentException("Unexpected end of JSON");
            }
            char current = text.charAt(index);
            return switch (current) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't' -> parseLiteral("true", Boolean.TRUE);
                case 'f' -> parseLiteral("false", Boolean.FALSE);
                case 'n' -> parseLiteral("null", null);
                default -> parseNumber();
            };
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> result = new LinkedHashMap<>();
            index++;
            skipWhitespace();
            if (peek('}')) {
                index++;
                return result;
            }
            while (true) {
                String key = parseString();
                skipWhitespace();
                expect(':');
                Object value = parseValue();
                result.put(key, value);
                skipWhitespace();
                if (peek('}')) {
                    index++;
                    return result;
                }
                expect(',');
            }
        }

        private List<Object> parseArray() {
            List<Object> result = new ArrayList<>();
            index++;
            skipWhitespace();
            if (peek(']')) {
                index++;
                return result;
            }
            while (true) {
                result.add(parseValue());
                skipWhitespace();
                if (peek(']')) {
                    index++;
                    return result;
                }
                expect(',');
            }
        }

        private String parseString() {
            expect('"');
            StringBuilder builder = new StringBuilder();
            while (index < text.length()) {
                char current = text.charAt(index++);
                if (current == '"') {
                    return builder.toString();
                }
                if (current == '\\') {
                    if (index >= text.length()) {
                        throw new IllegalArgumentException("Unterminated escape sequence");
                    }
                    char escaped = text.charAt(index++);
                    builder.append(switch (escaped) {
                        case '"', '\\', '/' -> escaped;
                        case 'b' -> '\b';
                        case 'f' -> '\f';
                        case 'n' -> '\n';
                        case 'r' -> '\r';
                        case 't' -> '\t';
                        case 'u' -> parseUnicode();
                        default -> throw new IllegalArgumentException("Unsupported escape: \\" + escaped);
                    });
                    continue;
                }
                builder.append(current);
            }
            throw new IllegalArgumentException("Unterminated string");
        }

        private char parseUnicode() {
            if (index + 4 > text.length()) {
                throw new IllegalArgumentException("Invalid unicode escape");
            }
            String hex = text.substring(index, index + 4);
            index += 4;
            return (char) Integer.parseInt(hex, 16);
        }

        private Object parseNumber() {
            int start = index;
            if (index < text.length() && text.charAt(index) == '-') {
                index++;
            }
            while (index < text.length() && Character.isDigit(text.charAt(index))) {
                index++;
            }
            boolean decimal = false;
            if (index < text.length() && text.charAt(index) == '.') {
                decimal = true;
                index++;
                while (index < text.length() && Character.isDigit(text.charAt(index))) {
                    index++;
                }
            }
            String slice = text.substring(start, index).trim();
            return decimal ? Double.parseDouble(slice) : Long.parseLong(slice);
        }

        private Object parseLiteral(String expected, Object value) {
            if (!text.startsWith(expected, index)) {
                throw new IllegalArgumentException("Expected " + expected);
            }
            index += expected.length();
            return value;
        }

        private void skipWhitespace() {
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
        }

        private void expect(char expected) {
            skipWhitespace();
            if (index >= text.length() || text.charAt(index) != expected) {
                throw new IllegalArgumentException("Expected '" + expected + "'");
            }
            index++;
            skipWhitespace();
        }

        private boolean peek(char expected) {
            skipWhitespace();
            return index < text.length() && text.charAt(index) == expected;
        }
    }
}
