package online.sync;

import java.util.ArrayList;

import component.combat.HealthComponent;
import component.progression.InventoryComponent;
import component.character.NameComponent;
import component.character.PlayerComponent;
import component.progression.EquipmentComponent;
import component.progression.ProgressionComponent;
import component.progression.QuestComponent;
import data.DataRegistry;
import ecs.EcsWorld;
import progression.DerivedStatsSnapshot;
import progression.ProgressionCalculator;

public final class PlayerProgressSnapshotFactory {
    private PlayerProgressSnapshotFactory() {
    }

    public static PlayerProgressSnapshot fromWorld(EcsWorld world) {
        return fromWorld(world, DataRegistry.loadDefaults());
    }

    public static PlayerProgressSnapshot fromWorld(EcsWorld world, DataRegistry data) {
        int player = world.entitiesWith(PlayerComponent.class).get(0);
        NameComponent name = world.require(player, NameComponent.class);
        HealthComponent health = world.require(player, HealthComponent.class);
        InventoryComponent inventory = world.require(player, InventoryComponent.class);
        QuestComponent quests = world.require(player, QuestComponent.class);
        ProgressionComponent progression = world.require(player, ProgressionComponent.class);
        EquipmentComponent equipment = world.require(player, EquipmentComponent.class);
        DerivedStatsSnapshot snapshot = ProgressionCalculator.snapshot(
                data.rpgClass(progression.classId),
                data.progressionRules(),
                progression,
                equipment);

        return new PlayerProgressSnapshot(
                progression.characterId,
                name.value,
                progression.classId,
                progression.level,
                progression.masteryPoints,
                new PlayerProgressSnapshot.MasterySnapshot(
                        progression.masteryOffensePoints,
                        progression.masterySkillPoints,
                        progression.masteryDefensePoints),
                health.current,
                snapshot.hp(),
                inventory.coins,
                progression.enemiesKilled,
                new PlayerProgressSnapshot.EquipmentSnapshot(
                        equipment.weaponItemId,
                        equipment.offhandItemId,
                        equipment.armorItemId,
                        equipment.bootsItemId,
                        equipment.accessoryItemId),
                new PlayerProgressSnapshot.AttributesSnapshot(
                        snapshot.attributes().sta(),
                        snapshot.attributes().str(),
                        snapshot.attributes().intel(),
                        snapshot.attributes().agi(),
                        snapshot.attributes().spi()),
                new PlayerProgressSnapshot.StatsSnapshot(
                        snapshot.mana(),
                        snapshot.attack(),
                        snapshot.dps(),
                        snapshot.abilityPower(),
                        snapshot.defense(),
                        snapshot.healingPower()),
                new ArrayList<>(inventory.itemIds),
                new ArrayList<>(quests.completedQuestIds()));
    }
}
