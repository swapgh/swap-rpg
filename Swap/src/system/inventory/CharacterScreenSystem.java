package system.inventory;

import java.awt.event.KeyEvent;

import app.bootstrap.GameConfig;
import app.input.KeyboardState;
import component.character.PlayerComponent;
import component.progression.ProgressionComponent;
import ecs.EcsSystem;
import ecs.EcsWorld;
import state.GameMode;
import ui.state.UiState;

public final class CharacterScreenSystem implements EcsSystem {
    private final KeyboardState keyboard;
    private final UiState ui;

    public CharacterScreenSystem(KeyboardState keyboard, UiState ui) {
        this.keyboard = keyboard;
        this.ui = ui;
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        if (ui.mode == GameMode.SHOP || ui.mode == GameMode.LOOT || ui.mode == GameMode.DIALOGUE || ui.mode == GameMode.OPTIONS) {
            return;
        }

        if (ui.characterVisible) {
            handleMasteryAllocation(world);
            if (keyboard.consumePressed(KeyEvent.VK_C) || keyboard.consumePressed(KeyEvent.VK_ESCAPE)
                    || keyboard.consumePressed(KeyEvent.VK_BACK_SPACE)) {
                ui.characterVisible = false;
                ui.mode = ui.inventoryVisible ? GameMode.INVENTORY : GameMode.PLAY;
            }
            return;
        }

        if ((ui.mode == GameMode.PLAY || ui.inventoryVisible) && keyboard.consumePressed(KeyEvent.VK_C)) {
            ui.characterVisible = true;
            if (!ui.inventoryVisible) {
                ui.mode = GameMode.CHARACTER;
            }
        }
    }

    private void handleMasteryAllocation(EcsWorld world) {
        var players = world.entitiesWith(PlayerComponent.class, ProgressionComponent.class);
        if (players.isEmpty()) {
            return;
        }
        ProgressionComponent progression = world.require(players.get(0), ProgressionComponent.class);
        if (progression.level < GameConfig.MAX_CHARACTER_LEVEL) {
            return;
        }

        if (keyboard.consumePressed(KeyEvent.VK_R)) {
            int refunded = progression.masteryOffensePoints + progression.masterySkillPoints + progression.masteryDefensePoints;
            if (refunded > 0) {
                progression.masteryPoints += refunded;
                progression.masteryOffensePoints = 0;
                progression.masterySkillPoints = 0;
                progression.masteryDefensePoints = 0;
                progression.dirtySync = true;
                ui.pushToast("Mastery reiniciada", 150);
            }
            return;
        }

        if (progression.masteryPoints <= 0) {
            return;
        }
        if (keyboard.consumePressed(KeyEvent.VK_1) && progression.masteryOffensePoints < 10) {
            progression.masteryPoints--;
            progression.masteryOffensePoints++;
            progression.dirtySync = true;
            ui.pushToast("Mastery Offense +" + progression.masteryOffensePoints, 150);
        } else if (keyboard.consumePressed(KeyEvent.VK_2) && progression.masterySkillPoints < 8) {
            progression.masteryPoints--;
            progression.masterySkillPoints++;
            progression.dirtySync = true;
            ui.pushToast("Mastery Skill +" + progression.masterySkillPoints, 150);
        } else if (keyboard.consumePressed(KeyEvent.VK_3) && progression.masteryDefensePoints < 7) {
            progression.masteryPoints--;
            progression.masteryDefensePoints++;
            progression.dirtySync = true;
            ui.pushToast("Mastery Defense +" + progression.masteryDefensePoints, 150);
        }
    }
}
