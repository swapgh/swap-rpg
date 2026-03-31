package data.world;

import java.util.Map;

/**
 * Catalog of world object definitions available to the current content bundle.
 */
public record WorldObjectCatalogData(Map<String, WorldObjectData> objects) {
    public WorldObjectData object(String id) {
        WorldObjectData object = objects.get(id);
        if (object == null) {
            throw new IllegalArgumentException("Missing world object data: " + id);
        }
        return object;
    }
}
