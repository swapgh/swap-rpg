package online;

import java.util.ArrayList;

import component.combat.HealthComponent;
import component.progression.InventoryComponent;
import component.actor.NameComponent;
import component.actor.PlayerComponent;
import component.progression.ProgressionComponent;
import component.progression.QuestComponent;
import ecs.EcsWorld;

public final class PlayerProgressSnapshotFactory {
    private PlayerProgressSnapshotFactory() {
    }

    public static PlayerProgressSnapshot fromWorld(EcsWorld world) {
        int player = world.entitiesWith(PlayerComponent.class).get(0);
        PlayerComponent playerData = world.require(player, PlayerComponent.class);
        NameComponent name = world.require(player, NameComponent.class);
        HealthComponent health = world.require(player, HealthComponent.class);
        InventoryComponent inventory = world.require(player, InventoryComponent.class);
        QuestComponent quests = world.require(player, QuestComponent.class);
        ProgressionComponent progression = world.require(player, ProgressionComponent.class);

        return new PlayerProgressSnapshot(
                name.value,
                playerData.archetypeId,
                1,
                health.current,
                health.max,
                inventory.coins,
                progression.enemiesKilled,
                new ArrayList<>(inventory.itemIds),
                new ArrayList<>(quests.completedQuestIds()));
    }
}
