package content;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ItemCatalog {
    private static final ItemData FALLBACK = new ItemData(
            "unknown",
            "Objeto",
            "object.chest",
            "Objeto sin registrar.",
            false);

    private static final Map<String, ItemData> ITEMS = createItems();

    private ItemCatalog() {
    }

    public static ItemData get(String itemId) {
        return ITEMS.getOrDefault(itemId, fallback(itemId));
    }

    private static Map<String, ItemData> createItems() {
        Map<String, ItemData> items = new LinkedHashMap<>();
        items.put("coin", new ItemData(
                "coin",
                "Moneda",
                "object.coin",
                "Moneda comun para intercambios y compras.",
                true));
        items.put("key", new ItemData(
                "key",
                "Llave",
                "object.key",
                "Abre puertas y accesos bloqueados.",
                false));
        items.put("potion", new ItemData(
                "potion",
                "Pocion Roja",
                "object.potion",
                "Consumible valioso para futuras mejoras.",
                false));
        items.put("potion_red", new ItemData(
                "potion_red",
                "Pocion Roja",
                "object.potion",
                "Consumible valioso para futuras mejoras.",
                false));
        return items;
    }

    private static ItemData fallback(String itemId) {
        String normalized = itemId == null ? "" : itemId.trim();
        if (normalized.isEmpty()) {
            return FALLBACK;
        }
        String[] parts = normalized.replace('-', '_').split("_");
        StringBuilder label = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (label.length() > 0) {
                label.append(' ');
            }
            label.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                label.append(part.substring(1));
            }
        }
        return new ItemData(normalized, label.toString(), FALLBACK.iconId(), FALLBACK.description(), false);
    }

    public record ItemData(String id, String displayName, String iconId, String description, boolean stackInHud) {
    }
}
