package data;

/**
 * Raw collider rectangle offsets and size in pixels.
 *
 * These values stay in pixels instead of tile fractions because collision usually needs
 * exact hand-tuned control around the visible art.
 */
public record ColliderData(
        int offsetX,
        int offsetY,
        int width,
        int height) {
}
