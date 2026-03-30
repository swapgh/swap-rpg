package system;

import java.util.ArrayList;
import java.util.List;

import component.EnemyComponent;
import component.HealthComponent;
import component.PlayerComponent;
import component.ProgressionComponent;
import component.QuestComponent;
import ecs.EcsSystem;
import ecs.EcsWorld;
import state.GameMode;
import ui.UiState;
import ui.UiText;

public final class HealthSystem implements EcsSystem {
    private final UiState ui;

    public HealthSystem(UiState ui) {
        this.ui = ui;
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        List<Integer> dead = new ArrayList<>();
        for (int entity : world.entitiesWith(HealthComponent.class)) {
            HealthComponent health = world.require(entity, HealthComponent.class);
            if (health.invulnerabilityTicks > 0) {
                health.invulnerabilityTicks--;
            }
            if (health.current > 0) {
                continue;
            }
            if (world.has(entity, PlayerComponent.class)) {
                ui.mode = GameMode.TITLE;
                ui.subtitleMessage = UiText.STATUS_DIED_SUBTITLE;
            } else if (world.has(entity, EnemyComponent.class)) {
                dead.add(entity);
            }
        }
        for (int entity : dead) {
            world.destroyEntity(entity);
            int player = world.entitiesWith(PlayerComponent.class).get(0);
            QuestComponent quests = world.require(player, QuestComponent.class);
            ProgressionComponent progression = world.require(player, ProgressionComponent.class);
            progression.enemiesKilled++;
            progression.dirtySync = true;
            quests.active.add("first_kill");
        }
    }
}
