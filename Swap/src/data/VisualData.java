package data;

/**
 * Groups sprite and animation identifiers that describe how an entity should look.
 *
 * `initialFacing` is kept as data so future character variants can start facing a
 * different direction without touching code.
 */
public record VisualData(
        String idleBase,
        String walkBase,
        String attackBase,
        String initialFacing,
        int initialFrame,
        int layer,
        int animationFrameTicks) {
}
