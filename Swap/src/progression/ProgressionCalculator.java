package progression;

import app.bootstrap.GameConfig;
import component.progression.EquipmentComponent;
import component.progression.ProgressionComponent;
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
        return snapshot(rpgClass, rules, level, null, null);
    }

    public static DerivedStatsSnapshot snapshot(RpgClassData rpgClass, ProgressionRulesData rules, ProgressionComponent progression,
            EquipmentComponent equipment) {
        int level = progression == null ? 1 : progression.level;
        return snapshot(rpgClass, rules, level, progression, equipment);
    }

    public static DerivedStatsSnapshot snapshot(RpgClassData rpgClass, ProgressionRulesData rules, int level,
            ProgressionComponent progression, EquipmentComponent equipment) {
        AttributesData attributes = attributesAt(rpgClass, Math.min(GameConfig.MAX_CHARACTER_LEVEL, level));
        AttributesData progressionBonus = progressionAttributes(progression);
        EquipmentStatProfile equipmentBonus = equipmentProfile(equipment);
        AttributesData finalAttributes = add(add(attributes, progressionBonus), equipmentBonus.attributes());
        double effectiveSta = scaledAttribute(finalAttributes.sta());
        double effectiveStr = scaledAttribute(finalAttributes.str());
        double effectiveInt = scaledAttribute(finalAttributes.intel());
        double effectiveAgi = scaledAttribute(finalAttributes.agi());
        double effectiveSpi = scaledAttribute(finalAttributes.spi());
        int hp = (int) Math.round(rpgClass.baseHp() + 2.0 * effectiveSta + 1.0 * effectiveSpi);
        int mana = (int) Math.round(rpgClass.baseMana() + 3.0 * effectiveInt + 2.0 * effectiveSpi);
        double attackPower = rpgClass.strAttackScale() * effectiveStr
                + rpgClass.agiAttackScale() * effectiveAgi
                + rpgClass.intAttackScale() * effectiveInt
                + rpgClass.spiAttackScale() * effectiveSpi;
        double masteryBasicDamageBonus = masteryBasicDamageBonus(progression);
        double attack = (rpgClass.startingWeaponPower() + attackPower + equipmentBonus.weaponPower()
                + (progression == null ? 0 : progression.bonusWeaponPower)) * (1.0 + masteryBasicDamageBonus);
        double movementSpeed = softCap(rpgClass.baseSpeed() + 0.05 * effectiveAgi + equipmentBonus.movementSpeed(), 1.25, 0.35, 1.45);
        double attackSpeed = softCap(rules.attackSpeedBase() + rules.attackSpeedPerAgi() * effectiveAgi, 1.00, 0.35, 1.20);
        double dps = attack * attackSpeed;
        double abilityPower = rules.abilityPowerPerInt() * finalAttributes.intel() + rules.abilityPowerPerSpi() * finalAttributes.spi();
        double defense = softCap(rpgClass.startingArmor() + 0.3 * effectiveSta
                + 0.15 * effectiveStr + equipmentBonus.armor()
                + (progression == null ? 0 : progression.bonusArmor), 6.0, 0.35, 10.0);
        double healingPower = rules.healingPowerPerSpi() * finalAttributes.spi() + rules.healingPowerPerInt() * finalAttributes.intel();
        return new DerivedStatsSnapshot(
                rpgClass.id(),
                Math.max(1, Math.min(GameConfig.MAX_CHARACTER_LEVEL, level)),
                finalAttributes,
                hp,
                mana,
                attackPower,
                attack,
                movementSpeed,
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

    public static int xpToNextLevel(int level) {
        int appliedLevel = Math.max(1, level);
        return 14 + (appliedLevel - 1) * 8;
    }

    public static int xpRewardForEnemy(int enemyMaxHp) {
        return Math.max(1, enemyMaxHp / 4);
    }

    public static double masteryBasicDamageBonus(ProgressionComponent progression) {
        if (progression == null) {
            return 0.0;
        }
        return Math.min(0.20, progression.masteryOffensePoints * 0.02);
    }

    public static double masterySkillDamageBonus(ProgressionComponent progression) {
        if (progression == null) {
            return 0.0;
        }
        return Math.min(0.24, progression.masterySkillPoints * 0.03);
    }

    public static double masteryDamageReduction(ProgressionComponent progression) {
        if (progression == null) {
            return 0.0;
        }
        return Math.min(0.14, progression.masteryDefensePoints * 0.02);
    }

    private static AttributesData progressionAttributes(ProgressionComponent progression) {
        if (progression == null) {
            return new AttributesData(0, 0, 0, 0, 0);
        }
        return new AttributesData(
                progression.bonusSta,
                progression.bonusStr,
                progression.bonusInt,
                progression.bonusAgi,
                progression.bonusSpi);
    }

    private static AttributesData add(AttributesData left, AttributesData right) {
        return new AttributesData(
                left.sta() + right.sta(),
                left.str() + right.str(),
                left.intel() + right.intel(),
                left.agi() + right.agi(),
                left.spi() + right.spi());
    }

    private static double scaledAttribute(int value) {
        if (value <= 5) {
            return value;
        }
        return 5.0 + (value - 5) * 0.5;
    }

    private static double softCap(double rawValue, double softCap, double overflowFactor, double hardCap) {
        double softened = rawValue <= softCap ? rawValue : softCap + (rawValue - softCap) * overflowFactor;
        return Math.min(hardCap, softened);
    }

    private static EquipmentStatProfile equipmentProfile(EquipmentComponent equipment) {
        if (equipment == null) {
            return EquipmentStatProfile.NONE;
        }
        EquipmentStatProfile weapon = equipmentBonus(equipment.weaponItemId);
        EquipmentStatProfile offhand = equipmentBonus(equipment.offhandItemId);
        EquipmentStatProfile armor = equipmentBonus(equipment.armorItemId);
        EquipmentStatProfile boots = equipmentBonus(equipment.bootsItemId);
        EquipmentStatProfile accessory = equipmentBonus(equipment.accessoryItemId);
        return new EquipmentStatProfile(
                add(add(add(add(weapon.attributes(), offhand.attributes()), armor.attributes()), boots.attributes()),
                        accessory.attributes()),
                weapon.weaponPower() + offhand.weaponPower() + armor.weaponPower() + boots.weaponPower() + accessory.weaponPower(),
                weapon.armor() + offhand.armor() + armor.armor() + boots.armor() + accessory.armor(),
                weapon.movementSpeed() + offhand.movementSpeed() + armor.movementSpeed() + boots.movementSpeed() + accessory.movementSpeed());
    }

    private static EquipmentStatProfile equipmentBonus(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return EquipmentStatProfile.NONE;
        }
        return switch (itemId.trim()) {
        case "starter_sword" -> new EquipmentStatProfile(new AttributesData(0, 0, 0, 0, 0), 1, 0, 0.0);
        case "starter_shield" -> new EquipmentStatProfile(new AttributesData(0, 0, 0, 0, 0), 0, 1, 0.0);
        case "starter_boots" -> new EquipmentStatProfile(new AttributesData(0, 0, 0, 0, 0), 0, 0, 0.25);
        default -> EquipmentStatProfile.NONE;
        };
    }
}
