package component.combat;

public final class AttackComponent {
    public int power;
    public final int range;
    public final int cooldownTicks;
    public int cooldownRemaining;
    public int activeTicks;

    public AttackComponent(int power, int range, int cooldownTicks) {
        this.power = power;
        this.range = range;
        this.cooldownTicks = cooldownTicks;
    }
}
