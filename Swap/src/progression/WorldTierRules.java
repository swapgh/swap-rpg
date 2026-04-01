package progression;

public final class WorldTierRules {
    private WorldTierRules() {
    }

    public static int clampTier(int tier) {
        return Math.max(1, Math.min(5, tier));
    }

    public static double enemyHpMultiplier(int tier) {
        return switch (clampTier(tier)) {
        case 2 -> 1.20;
        case 3 -> 1.40;
        case 4 -> 1.65;
        case 5 -> 2.00;
        default -> 1.00;
        };
    }

    public static double enemyDamageMultiplier(int tier) {
        return switch (clampTier(tier)) {
        case 2 -> 1.10;
        case 3 -> 1.20;
        case 4 -> 1.30;
        case 5 -> 1.45;
        default -> 1.00;
        };
    }
}
