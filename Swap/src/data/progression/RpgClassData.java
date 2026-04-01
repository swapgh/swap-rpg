package data.progression;

public record RpgClassData(
        String id,
        String role,
        AttributesData baseAttributes,
        AttributesData growthPerLevel,
        int baseHp,
        int baseMana,
        double baseSpeed,
        int startingWeaponPower,
        int startingArmor,
        double strAttackScale,
        double agiAttackScale,
        double intAttackScale,
        double spiAttackScale) {
}
