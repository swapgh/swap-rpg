package component.combat;

public final class HealthComponent {
    public int current;
    public int max;
    public int invulnerabilityTicks;

    public HealthComponent(int current, int max) {
        this.current = current;
        this.max = max;
    }
}
