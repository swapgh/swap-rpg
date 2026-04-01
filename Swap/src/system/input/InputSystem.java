package system.input;

import java.awt.event.KeyEvent;

import app.KeyboardState;
import component.actor.FacingComponent;
import component.actor.InputComponent;
import component.actor.PlayerComponent;
import component.combat.StatsComponent;
import component.world.VelocityComponent;
import ecs.EcsSystem;
import ecs.EcsWorld;
import state.GameMode;
import ui.runtime.UiState;
import util.Direction;

public final class InputSystem implements EcsSystem {
    private final KeyboardState keyboard;
    private final UiState ui;

    public InputSystem(KeyboardState keyboard, UiState ui) {
        this.keyboard = keyboard;
        this.ui = ui;
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        for (int entity : world.entitiesWith(PlayerComponent.class, InputComponent.class, VelocityComponent.class,
                FacingComponent.class, StatsComponent.class)) {
            InputComponent input = world.require(entity, InputComponent.class);
            VelocityComponent velocity = world.require(entity, VelocityComponent.class);
            FacingComponent facing = world.require(entity, FacingComponent.class);
            StatsComponent stats = world.require(entity, StatsComponent.class);

            input.up = false;
            input.down = false;
            input.left = false;
            input.right = false;
            input.attackPressed = false;
            input.interactPressed = false;
            input.inventoryPressed = false;
            input.projectilePressed = false;
            velocity.dx = 0;
            velocity.dy = 0;

            if (ui.mode == GameMode.TITLE || ui.mode == GameMode.GAME_OVER) {
                continue;
            }

            if (ui.inventoryVisible) {
                if (keyboard.consumePressed(KeyEvent.VK_I)) {
                    input.inventoryPressed = true;
                }
                continue;
            }

            if (ui.characterVisible || ui.mode == GameMode.CHARACTER || ui.mode == GameMode.SHOP || ui.mode == GameMode.LOOT) {
                if (keyboard.consumePressed(KeyEvent.VK_I)) {
                    input.inventoryPressed = true;
                }
                continue;
            }

            if (ui.mode == GameMode.DIALOGUE) {
                if (keyboard.consumePressed(KeyEvent.VK_E) || keyboard.consumePressed(KeyEvent.VK_ENTER)) {
                    input.interactPressed = true;
                }
                continue;
            }

            if (keyboard.consumePressed(KeyEvent.VK_I)) {
                input.inventoryPressed = true;
            }
            if (keyboard.consumePressed(KeyEvent.VK_E) || keyboard.consumePressed(KeyEvent.VK_ENTER)) {
                input.interactPressed = true;
            }
            if (keyboard.consumePressed(KeyEvent.VK_SPACE)) {
                input.attackPressed = true;
            }
            if (keyboard.consumePressed(KeyEvent.VK_F)) {
                input.projectilePressed = true;
            }

            input.up = keyboard.isDown(KeyEvent.VK_W) || keyboard.isDown(KeyEvent.VK_UP);
            input.down = keyboard.isDown(KeyEvent.VK_S) || keyboard.isDown(KeyEvent.VK_DOWN);
            input.left = keyboard.isDown(KeyEvent.VK_A) || keyboard.isDown(KeyEvent.VK_LEFT);
            input.right = keyboard.isDown(KeyEvent.VK_D) || keyboard.isDown(KeyEvent.VK_RIGHT);

            double axisX = 0;
            double axisY = 0;
            if (input.up) {
                facing.direction = Direction.UP;
                axisY = -1;
            } else if (input.down) {
                facing.direction = Direction.DOWN;
                axisY = 1;
            }
            if (input.left) {
                facing.direction = Direction.LEFT;
                axisX = -1;
            } else if (input.right) {
                facing.direction = Direction.RIGHT;
                axisX = 1;
            }

            if (axisX != 0 || axisY != 0) {
                double magnitude = Math.hypot(axisX, axisY);
                velocity.dx = (axisX / magnitude) * stats.speed;
                velocity.dy = (axisY / magnitude) * stats.speed;
            }
        }
    }
}
