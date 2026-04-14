package component.character;

public final class WanderAiComponent {
    public int ticksUntilTurn;
    public int seed;

    public WanderAiComponent(int ticksUntilTurn, int seed) {
        this.ticksUntilTurn = ticksUntilTurn;
        this.seed = seed;
    }
}
