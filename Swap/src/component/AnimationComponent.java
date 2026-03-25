package component;

public final class AnimationComponent {
    public String clipId;
    public int frameIndex;
    public int tick;
    public int ticksPerFrame;

    public AnimationComponent(String clipId, int ticksPerFrame) {
        this.clipId = clipId;
        this.ticksPerFrame = ticksPerFrame;
    }
}
