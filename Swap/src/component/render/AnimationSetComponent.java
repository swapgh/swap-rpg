package component.render;

public final class AnimationSetComponent {
    public final String idleBase;
    public final String walkBase;
    public final String attackBase;

    public AnimationSetComponent(String idleBase, String walkBase, String attackBase) {
        this.idleBase = idleBase;
        this.walkBase = walkBase;
        this.attackBase = attackBase;
    }
}
