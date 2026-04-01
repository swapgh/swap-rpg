package system.combat;

import java.util.ArrayList;
import java.util.List;

import component.actor.EnemyComponent;
import component.combat.HealthComponent;
import component.actor.PlayerComponent;
import component.progression.ProgressionComponent;
import ecs.EcsSystem;
import ecs.EcsWorld;
import state.GameMode;
import ui.runtime.UiState;

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
                ui.mode = GameMode.GAME_OVER;
            } else if (world.has(entity, EnemyComponent.class)) {
                dead.add(entity);
            }
        }
        for (int entity : dead) {
            world.destroyEntity(entity);
            int player = world.entitiesWith(PlayerComponent.class).get(0);
            ProgressionComponent progression = world.require(player, ProgressionComponent.class);
            progression.enemiesKilled++;
            progression.dirtySync = true;
        }
    }
}
