package progression;

import data.progression.AttributesData;
import data.progression.ProgressionRulesData;
import data.progression.RpgClassData;

public final class ProgressionCalculator {
    private ProgressionCalculator() {
    }

    public static AttributesData attributesAt(RpgClassData rpgClass, int level) {
        int appliedLevel = Math.max(1, level);
        int delta = appliedLevel - 1;
        AttributesData base = rpgClass.baseAttributes();
        AttributesData growth = rpgClass.growthPerLevel();
        return new AttributesData(
                base.sta() + growth.sta() * delta,
                base.str() + growth.str() * delta,
                base.intel() + growth.intel() * delta,
                base.agi() + growth.agi() * delta,
                base.spi() + growth.spi() * delta);
    }

    public static DerivedStatsSnapshot snapshot(RpgClassData rpgClass, ProgressionRulesData rules, int level) {
        AttributesData attributes = attributesAt(rpgClass, level);
        int hp = rpgClass.baseHp() + rules.hpPerSta() * attributes.sta() + rules.hpPerSpi() * attributes.spi();
        int mana = rpgClass.baseMana() + rules.manaPerInt() * attributes.intel() + rules.manaPerSpi() * attributes.spi();
        double attackPower = rules.apPerStr() * attributes.str() + rules.apPerAgi() * attributes.agi();
        double attack = rpgClass.startingWeaponPower() + attackPower;
        double attackSpeed = rules.attackSpeedBase() + rules.attackSpeedPerAgi() * attributes.agi();
        double dps = attack * attackSpeed;
        double abilityPower = rules.abilityPowerPerInt() * attributes.intel() + rules.abilityPowerPerSpi() * attributes.spi();
        double defense = rpgClass.startingArmor() + rules.defensePerSta() * attributes.sta() + rules.defensePerStr() * attributes.str();
        double healingPower = rules.healingPowerPerSpi() * attributes.spi() + rules.healingPowerPerInt() * attributes.intel();
        return new DerivedStatsSnapshot(
                rpgClass.id(),
                Math.max(1, level),
                attributes,
                hp,
                mana,
                attackPower,
                attack,
                attackSpeed,
                dps,
                abilityPower,
                defense,
                healingPower);
    }

    public static int normalEnemyHp(double dps) {
        return (int) Math.round(dps * 4.0);
    }

    public static int eliteEnemyHp(double dps) {
        return (int) Math.round(dps * 8.0);
    }

    public static int bossEnemyHp(double dps) {
        return (int) Math.round(dps * 20.0);
    }
}
