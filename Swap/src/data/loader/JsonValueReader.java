package data.loader;

import java.util.List;
import java.util.Map;

public final class JsonValueReader {
    private JsonValueReader() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> object(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new IllegalArgumentException("Expected JSON object");
    }

    @SuppressWarnings("unchecked")
    public static List<Object> array(Map<String, Object> value, String key) {
        Object nested = value.get(key);
        if (nested instanceof List<?> list) {
            return (List<Object>) list;
        }
        throw new IllegalArgumentException("Expected array for key: " + key);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> optionalArray(Map<String, Object> value, String key) {
        if (value == null) {
            return null;
        }
        Object nested = value.get(key);
        if (nested == null) {
            return null;
        }
        if (nested instanceof List<?> list) {
            return (List<Object>) list;
        }
        throw new IllegalArgumentException("Expected array for key: " + key);
    }

    public static Map<String, Object> object(Map<String, Object> value, String key) {
        Object nested = value.get(key);
        if (nested == null) {
            throw new IllegalArgumentException("Missing object key: " + key);
        }
        return object(nested);
    }

    public static Map<String, Object> optionalObject(Map<String, Object> value, String key) {
        if (value == null) {
            return null;
        }
        Object nested = value.get(key);
        if (nested == null) {
            return null;
        }
        return object(nested);
    }

    public static String string(Map<String, Object> value, String key) {
        return string(value, key, null);
    }

    public static String string(Map<String, Object> value, String key, String defaultValue) {
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

    public static int integer(Map<String, Object> value, String key) {
        return integer(value, key, 0);
    }

    public static int integer(Map<String, Object> value, String key, int defaultValue) {
        Object nested = value.get(key);
        if (nested == null) {
            return defaultValue;
        }
        if (nested instanceof Number number) {
            return number.intValue();
        }
        throw new IllegalArgumentException("Expected number for key: " + key);
    }

    public static double number(Map<String, Object> value, String key) {
        return number(value, key, 0.0);
    }

    public static double number(Map<String, Object> value, String key, double defaultValue) {
        Object nested = value.get(key);
        if (nested == null) {
            return defaultValue;
        }
        if (nested instanceof Number number) {
            return number.doubleValue();
        }
        throw new IllegalArgumentException("Expected number for key: " + key);
    }

    public static boolean bool(Map<String, Object> value, String key) {
        Object nested = value.get(key);
        if (nested == null) {
            return false;
        }
        if (nested instanceof Boolean flag) {
            return flag;
        }
        throw new IllegalArgumentException("Expected boolean for key: " + key);
    }

    public static String[] stringArray(Map<String, Object> value, String key) {
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

    public static String[] optionalStringArray(Map<String, Object> value, String key) {
        if (value == null || !value.containsKey(key)) {
            return new String[0];
        }
        return stringArray(value, key);
    }
}
