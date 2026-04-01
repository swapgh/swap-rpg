package data.progression;

public record ProgressionRulesData(
        int hpPerSta,
        int hpPerSpi,
        int manaPerInt,
        int manaPerSpi,
        double apPerStr,
        double apPerAgi,
        double attackSpeedBase,
        double attackSpeedPerAgi,
        double abilityPowerPerInt,
        double abilityPowerPerSpi,
        double defensePerSta,
        double defensePerStr,
        double healingPowerPerSpi,
        double healingPowerPerInt) {
}
