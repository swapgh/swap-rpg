package component.world;

public final class DoorComponent {
    public boolean locked;
    public final String requiredItemId;

    public DoorComponent(boolean locked, String requiredItemId) {
        this.locked = locked;
        this.requiredItemId = requiredItemId;
    }
}
