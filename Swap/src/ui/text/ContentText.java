package ui.text;

import data.JsonDataLoader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ContentText {
    private static final Map<String, String> entries = new LinkedHashMap<>();

    static {
        reload(UiText.language());
    }

    private ContentText() {
    }

    public static void reload(UiLanguage language) {
        entries.clear();
        entries.putAll(loadBundle("/content/text/" + language.code() + "/gameplay.json"));
    }

    public static String text(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        return entries.getOrDefault(key, key);
    }

    public static String[] lines(String[] keys) {
        if (keys == null || keys.length == 0) {
            return new String[0];
        }
        String[] resolved = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            resolved[i] = text(keys[i]);
        }
        return resolved;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> loadBundle(String resourcePath) {
        try (InputStream input = ContentText.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IllegalStateException("Missing content text resource: " + resourcePath);
            }
            String json = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> raw = JsonDataLoader.parseObjectText(json);
            Map<String, String> result = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : raw.entrySet()) {
                if (entry.getValue() instanceof String text) {
                    result.put(entry.getKey(), text);
                    continue;
                }
                if (entry.getValue() instanceof List<?> list) {
                    for (int i = 0; i < list.size(); i++) {
                        Object value = list.get(i);
                        if (!(value instanceof String line)) {
                            throw new IllegalStateException("Expected string line for key: " + entry.getKey());
                        }
                        result.put(entry.getKey() + "." + i, line);
                    }
                    continue;
                }
                throw new IllegalStateException("Unsupported content text value for key: " + entry.getKey());
            }
            return result;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load content text resource: " + resourcePath, ex);
        }
    }
}
