package system.progression;

import app.bootstrap.GameConfig;
import component.character.PlayerComponent;
import component.combat.AttackComponent;
import component.combat.HealthComponent;
import component.combat.ProjectileEmitterComponent;
import component.combat.StatsComponent;
import component.progression.EquipmentComponent;
import component.progression.ProgressionComponent;
import data.DataRegistry;
import data.progression.RpgClassData;
import ecs.EcsSystem;
import ecs.EcsWorld;
import progression.DerivedStatsSnapshot;
import progression.ProgressionCalculator;
import ui.state.UiState;

public final class ProgressionSystem implements EcsSystem {
    private final DataRegistry data;
    private final UiState ui;

    public ProgressionSystem(DataRegistry data, UiState ui) {
        this.data = data;
        this.ui = ui;
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        for (int entity : world.entitiesWith(PlayerComponent.class, ProgressionComponent.class, StatsComponent.class, HealthComponent.class,
                AttackComponent.class, EquipmentComponent.class)) {
            ProgressionComponent progression = world.require(entity, ProgressionComponent.class);
            levelUpIfNeeded(progression);
            applyDerivedStats(world, entity, progression);
        }
    }

    private void levelUpIfNeeded(ProgressionComponent progression) {
        boolean leveled = false;
        while (progression.level < GameConfig.MAX_CHARACTER_LEVEL
                && progression.experience >= ProgressionCalculator.xpToNextLevel(progression.level)) {
            progression.experience -= ProgressionCalculator.xpToNextLevel(progression.level);
            progression.level++;
            progression.attributePoints += 3;
            progression.skillPoints++;
            progression.dirtySync = true;
            leveled = true;
        }
        if (progression.level >= GameConfig.MAX_CHARACTER_LEVEL && progression.experience > 0) {
            progression.masteryExperience += progression.experience;
            progression.experience = 0;
            while (progression.masteryExperience >= GameConfig.MASTERY_XP_PER_POINT) {
                progression.masteryExperience -= GameConfig.MASTERY_XP_PER_POINT;
                progression.masteryPoints++;
                progression.dirtySync = true;
                ui.pushToast("Mastery +" + progression.masteryPoints, 160);
            }
        }
        if (leveled) {
            ui.pushToast("Nivel " + progression.level + " alcanzado", 180);
        }
    }

    private void applyDerivedStats(EcsWorld world, int entity, ProgressionComponent progression) {
        RpgClassData rpgClass = data.rpgClass(progression.classId);
        EquipmentComponent equipment = world.require(entity, EquipmentComponent.class);
        DerivedStatsSnapshot snapshot = ProgressionCalculator.snapshot(rpgClass, data.progressionRules(), progression, equipment);
        StatsComponent stats = world.require(entity, StatsComponent.class);
        HealthComponent health = world.require(entity, HealthComponent.class);
        AttackComponent attack = world.require(entity, AttackComponent.class);

        int previousMaxHealth = Math.max(1, health.max);
        int targetMaxHealth = snapshot.hp();
        stats.speed = Math.max(1, (int) Math.round(snapshot.movementSpeed() * 2.0));
        stats.attack = Math.max(1, (int) Math.round(snapshot.attack()));
        stats.defense = Math.max(0, (int) Math.round(snapshot.defense()));
        attack.power = stats.attack;
        health.max = targetMaxHealth;
        health.current = Math.max(1, Math.min(targetMaxHealth,
                (int) Math.round(health.current * (targetMaxHealth / (double) previousMaxHealth))));

        if (world.has(entity, ProjectileEmitterComponent.class)) {
            ProjectileEmitterComponent projectile = world.require(entity, ProjectileEmitterComponent.class);
            projectile.projectileDamage = Math.max(1,
                    (int) Math.round(snapshot.attack() * 0.75 * (1.0 + ProgressionCalculator.masterySkillDamageBonus(progression))));
        }
    }
}
