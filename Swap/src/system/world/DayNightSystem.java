package system.world;

import app.KeyboardState;
import component.AnimationComponent;
import component.AnimationSetComponent;
import component.EnemyComponent;
import component.FacingComponent;
import component.HealthComponent;
import component.NameComponent;
import component.PlayerComponent;
import component.QuestComponent;
import component.StatsComponent;
import component.WorldTimeComponent;
import data.DataRegistry;
import data.EnemyData;
import data.world.WorldPhaseData;
import ecs.EcsSystem;
import ecs.EcsWorld;
import java.awt.event.KeyEvent;
import java.time.Instant;
import ui.runtime.UiState;
import ui.text.UiText;

public final class DayNightSystem implements EcsSystem {
    private static final int DAY_TEST_TIME_SECONDS = 9 * 60 * 60;
    private static final int NIGHT_TEST_TIME_SECONDS = 21 * 60 * 60;

    private final KeyboardState keyboard;
    private final UiState ui;
    private final DataRegistry data;
    private Boolean lastDayPhase;

    public DayNightSystem(KeyboardState keyboard, UiState ui, DataRegistry data) {
        this.keyboard = keyboard;
        this.ui = ui;
        this.data = data;
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        WorldTimeComponent time = world.entitiesWith(WorldTimeComponent.class).isEmpty()
                ? null
                : world.require(world.entitiesWith(WorldTimeComponent.class).get(0), WorldTimeComponent.class);
        if (time == null) {
            return;
        }

        if (keyboard.consumePressed(KeyEvent.VK_F12)) {
            togglePhase(time);
        }

        boolean dayPhase = time.isDay();
        if (lastDayPhase == null || lastDayPhase.booleanValue() != dayPhase) {
            applyWorldPhase(world, dayPhase);
            updateQuestAvailability(world, dayPhase);
            ui.pushToast(dayPhase ? UiText.STATUS_DAY_BREAK : UiText.STATUS_NIGHT_FALL, 120);
            lastDayPhase = dayPhase;
        }
    }

    private void togglePhase(WorldTimeComponent time) {
        int target = time.isDay() ? NIGHT_TEST_TIME_SECONDS : DAY_TEST_TIME_SECONDS;
        if (!time.isDay() && target <= time.secondOfDay()) {
            time.totalSeconds += WorldTimeComponent.SECONDS_PER_DAY;
        }
        time.setSecondOfDay(target);
        time.lastRealEpochSeconds = Instant.now().getEpochSecond();
    }

    private void applyWorldPhase(EcsWorld world, boolean dayPhase) {
        EnemyData slimeData = data.enemy("green_slime");
        WorldPhaseData.SlimePhaseProfile slimeProfile = data.worldPhase().slimeProfile(slimeData, dayPhase);
        for (int entity : world.entitiesWith(EnemyComponent.class, NameComponent.class, StatsComponent.class, HealthComponent.class)) {
            EnemyComponent enemy = world.require(entity, EnemyComponent.class);
            if (!"green_slime".equals(enemy.enemyType)) {
                continue;
            }

            NameComponent name = world.require(entity, NameComponent.class);
            StatsComponent stats = world.require(entity, StatsComponent.class);
            HealthComponent health = world.require(entity, HealthComponent.class);
            int nextMaxHealth = slimeProfile.maxHealth();
            int deltaHealth = nextMaxHealth - health.max;

            name.value = slimeProfile.displayName();
            stats.attack = slimeProfile.attack();
            stats.defense = slimeProfile.defense();
            health.max = nextMaxHealth;
            health.current = Math.max(1, Math.min(health.current + Math.max(0, deltaHealth), health.max));
            replaceSlimeAnimationSet(world, entity, slimeProfile.animationBaseClipId());
        }
    }

    private void updateQuestAvailability(EcsWorld world, boolean dayPhase) {
        if (world.entitiesWith(PlayerComponent.class, QuestComponent.class).isEmpty()) {
            return;
        }
        int player = world.entitiesWith(PlayerComponent.class, QuestComponent.class).get(0);
        QuestComponent quests = world.require(player, QuestComponent.class);
        data.worldPhase().updateVisitQuestAvailability(quests, dayPhase);
    }

    private void replaceSlimeAnimationSet(EcsWorld world, int entity, String baseClipId) {
        world.remove(entity, AnimationSetComponent.class);
        world.add(entity, new AnimationSetComponent(baseClipId + ".idle", baseClipId + ".walk", null));
        if (world.has(entity, AnimationComponent.class) && world.has(entity, FacingComponent.class)) {
            AnimationComponent animation = world.require(entity, AnimationComponent.class);
            String direction = world.require(entity, FacingComponent.class).direction.name().toLowerCase();
            animation.clipId = baseClipId + ".idle." + direction;
            animation.frameIndex = 0;
            animation.tick = 0;
        }
    }
}
