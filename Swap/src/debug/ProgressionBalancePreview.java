package debug;

import data.DataRegistry;
import data.progression.RpgClassData;
import progression.DerivedStatsSnapshot;
import progression.ProgressionCalculator;

public final class ProgressionBalancePreview {
    private ProgressionBalancePreview() {
    }

    public static void main(String[] args) {
        DataRegistry data = DataRegistry.loadDefaults();
        previewClass(data.rpgClass("warrior"), data);
        previewClass(data.rpgClass("mage"), data);
        previewClass(data.rpgClass("druid"), data);
    }

    private static void previewClass(RpgClassData rpgClass, DataRegistry data) {
        System.out.println("== " + rpgClass.id().toUpperCase() + " ==");
        printSnapshot(ProgressionCalculator.snapshot(rpgClass, data.progressionRules(), 1));
        printSnapshot(ProgressionCalculator.snapshot(rpgClass, data.progressionRules(), 10));
        printSnapshot(ProgressionCalculator.snapshot(rpgClass, data.progressionRules(), 20));
        System.out.println();
    }

    private static void printSnapshot(DerivedStatsSnapshot snapshot) {
        System.out.printf(
                "L%d  HP %d  Mana %d  Atk %.1f  APS %.2f  DPS %.1f  AbP %.1f  DEF %.1f  Heal %.1f  EnemyHP N/E/B %d/%d/%d%n",
                snapshot.level(),
                snapshot.hp(),
                snapshot.mana(),
                snapshot.attack(),
                snapshot.attackSpeed(),
                snapshot.dps(),
                snapshot.abilityPower(),
                snapshot.defense(),
                snapshot.healingPower(),
                ProgressionCalculator.normalEnemyHp(snapshot.dps()),
                ProgressionCalculator.eliteEnemyHp(snapshot.dps()),
                ProgressionCalculator.bossEnemyHp(snapshot.dps()));
    }
}
