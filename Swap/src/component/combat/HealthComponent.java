package component.combat;

public final class HealthComponent {
    public static final int ENEMY_BAR_VISIBLE_TICKS = 120;

    public int current;
    public int max;
    public int invulnerabilityTicks;
    public int enemyBarVisibleTicks;

    public HealthComponent(int current, int max) {
        this.current = current;
        this.max = max;
    }
}
