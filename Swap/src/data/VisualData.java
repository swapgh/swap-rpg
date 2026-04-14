package data;

public record VisualData(
        String idleBase,
        String walkBase,
        String attackBase,
        String initialFacing,
        int initialFrame,
        int layer,
        int animationFrameTicks) {
}
