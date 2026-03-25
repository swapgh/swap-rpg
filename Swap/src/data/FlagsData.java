package data;

/**
 * Small boolean switches grouped together so data files can express optional behaviour
 * without creating many unrelated top-level fields.
 */
public record FlagsData(
        boolean solid,
        boolean cameraTarget) {
}
