package component.render;

public final class SpriteComponent {
    public String imageId;
    public final int width;
    public final int height;
    public final int renderLayer;

    public SpriteComponent(String imageId, int width, int height, int renderLayer) {
        this.imageId = imageId;
        this.width = width;
        this.height = height;
        this.renderLayer = renderLayer;
    }
}
