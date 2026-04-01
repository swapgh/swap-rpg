package progression;

import data.progression.AttributesData;

public record DerivedStatsSnapshot(
        String classId,
        int level,
        AttributesData attributes,
        int hp,
        int mana,
        double attackPower,
        double attack,
        double movementSpeed,
        double attackSpeed,
        double dps,
        double abilityPower,
        double defense,
        double healingPower) {
}
