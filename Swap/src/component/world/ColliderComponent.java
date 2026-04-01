package component.world;

public final class ColliderComponent {
    public final int offsetX;
    public final int offsetY;
    public final int width;
    public final int height;

    public ColliderComponent(int offsetX, int offsetY, int width, int height) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.width = width;
        this.height = height;
    }
}
