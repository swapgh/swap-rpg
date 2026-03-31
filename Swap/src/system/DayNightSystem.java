package system;

import app.KeyboardState;
import component.AnimationComponent;
import component.AnimationSetComponent;
import component.EnemyComponent;
import component.FacingComponent;
import component.HealthComponent;
import component.NameComponent;
import component.NpcComponent;
import component.PlayerComponent;
import component.QuestComponent;
import component.StatsComponent;
import component.WorldTimeComponent;
import data.DataRegistry;
import data.EnemyData;
import ecs.EcsSystem;
import ecs.EcsWorld;
import java.awt.event.KeyEvent;
import java.time.Instant;
import ui.UiState;
import ui.UiText;

public final class DayNightSystem implements EcsSystem {
    private static final int DAY_TEST_TIME_SECONDS = 9 * 60 * 60;
    private static final int NIGHT_TEST_TIME_SECONDS = 21 * 60 * 60;
    private static final String DAY_VISIT_QUEST = "visit_merchant_day";
    private static final String NIGHT_VISIT_QUEST = "visit_old_man_night";

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

        if (keyboard.consumePressed(KeyEvent.VK_F6)) {
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
        for (int entity : world.entitiesWith(EnemyComponent.class, NameComponent.class, StatsComponent.class, HealthComponent.class)) {
            EnemyComponent enemy = world.require(entity, EnemyComponent.class);
            if (!"green_slime".equals(enemy.enemyType)) {
                continue;
            }

            NameComponent name = world.require(entity, NameComponent.class);
            StatsComponent stats = world.require(entity, StatsComponent.class);
            HealthComponent health = world.require(entity, HealthComponent.class);
            int baseHealth = slimeData.stats().health();
            int baseAttack = slimeData.stats().attack();
            int baseDefense = slimeData.stats().defense();
            int nextMaxHealth = dayPhase ? baseHealth : baseHealth + 2;
            int deltaHealth = nextMaxHealth - health.max;

            name.value = dayPhase ? slimeData.name() : "Red Slime";
            stats.attack = dayPhase ? baseAttack : baseAttack + 1;
            stats.defense = dayPhase ? baseDefense : baseDefense + 1;
            health.max = nextMaxHealth;
            health.current = Math.max(1, Math.min(health.current + Math.max(0, deltaHealth), health.max));
            replaceSlimeAnimationSet(world, entity, dayPhase ? "enemy.slime" : "enemy.redslime");
        }
    }

    private void updateQuestAvailability(EcsWorld world, boolean dayPhase) {
        if (world.entitiesWith(PlayerComponent.class, QuestComponent.class).isEmpty()) {
            return;
        }
        int player = world.entitiesWith(PlayerComponent.class, QuestComponent.class).get(0);
        QuestComponent quests = world.require(player, QuestComponent.class);
        if (dayPhase) {
            quests.active.remove(NIGHT_VISIT_QUEST);
            if (!quests.completed.contains(DAY_VISIT_QUEST)) {
                quests.active.add(DAY_VISIT_QUEST);
            }
        } else {
            quests.active.remove(DAY_VISIT_QUEST);
            if (!quests.completed.contains(NIGHT_VISIT_QUEST)) {
                quests.active.add(NIGHT_VISIT_QUEST);
            }
        }
    }

    public static String dayVisitQuestId() {
        return DAY_VISIT_QUEST;
    }

    public static String nightVisitQuestId() {
        return NIGHT_VISIT_QUEST;
    }

    public static String[] dialogueFor(String npcType, String[] defaultLines, boolean dayPhase) {
        if ("merchant".equals(npcType)) {
            return dayPhase
                    ? new String[] {
                            "Buenos dias. La tienda esta abierta.",
                            "Si buscas provisiones, hoy es el mejor momento."
                    }
                    : new String[] {
                            "La noche trae clientes extraños.",
                            "Ahora venderia mejor antorchas y pociones."
                    };
        }
        if ("old_man".equals(npcType)) {
            return dayPhase
                    ? new String[] {
                            "De dia el camino al pueblo es mas seguro.",
                            "Pasa a ver al mercader antes del anochecer."
                    }
                    : new String[] {
                            "Cae la noche. Los slimes cambian.",
                            "Si sobrevives, vuelve a hablar conmigo al amanecer."
                    };
        }
        return defaultLines;
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
