package data;

/**
 * Spawn position expressed in tile coordinates.
 *
 * Storing tile coordinates keeps content files easy to read against the map grid. The
 * runtime converts these values to pixels when building live ECS entities.
 */
public record SpawnData(
        int tileX,
        int tileY) {
}
