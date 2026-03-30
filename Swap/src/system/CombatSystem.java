package system;

import java.awt.Rectangle;

import audio.AudioService;
import component.AttackComponent;
import component.ColliderComponent;
import component.EnemyComponent;
import component.FacingComponent;
import component.HealthComponent;
import component.InputComponent;
import component.NameComponent;
import component.PlayerComponent;
import component.PositionComponent;
import component.StatsComponent;
import component.VelocityComponent;
import ecs.EcsSystem;
import ecs.EcsWorld;
import ui.UiState;
import ui.UiText;
import util.CollisionUtil;
import util.Direction;

public final class CombatSystem implements EcsSystem {
    private final UiState ui;
    private final AudioService audio;
    private final int tileSize;

    public CombatSystem(UiState ui, AudioService audio, int tileSize) {
        this.ui = ui;
        this.audio = audio;
        this.tileSize = tileSize;
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        int player = world.entitiesWith(PlayerComponent.class).get(0);
        AttackComponent attack = world.require(player, AttackComponent.class);
        InputComponent input = world.require(player, InputComponent.class);
        if (attack.cooldownRemaining > 0) {
            attack.cooldownRemaining--;
        }
        if (attack.activeTicks > 0) {
            attack.activeTicks--;
        }
        if (input.attackPressed && attack.cooldownRemaining == 0) {
            audio.playEffect("attack.swing");
            attack.activeTicks = 10;
            attack.cooldownRemaining = attack.cooldownTicks;
            hitEnemies(world, player, attack.power);
        }

        PositionComponent playerPos = world.require(player, PositionComponent.class);
        VelocityComponent playerVelocity = world.require(player, VelocityComponent.class);
        ColliderComponent playerCollider = world.require(player, ColliderComponent.class);
        HealthComponent playerHealth = world.require(player, HealthComponent.class);
        Rectangle playerRect = CollisionUtil.rect(playerPos, playerCollider);
        Rectangle playerTouchRect = CollisionUtil.movedRect(playerPos, playerCollider, playerVelocity.dx, playerVelocity.dy);
        for (int enemy : world.entitiesWith(EnemyComponent.class, PositionComponent.class, ColliderComponent.class,
                StatsComponent.class, HealthComponent.class)) {
            Rectangle enemyRect = CollisionUtil.rect(world.require(enemy, PositionComponent.class),
                    world.require(enemy, ColliderComponent.class));
            if (!playerRect.intersects(enemyRect) && !playerTouchRect.intersects(enemyRect)) {
                continue;
            }
            if (playerHealth.invulnerabilityTicks > 0) {
                continue;
            }
            int damage = Math.max(1, world.require(enemy, StatsComponent.class).attack
                    - world.require(player, StatsComponent.class).defense + 1);
            playerHealth.current = Math.max(0, playerHealth.current - damage);
            playerHealth.invulnerabilityTicks = 45;
            audio.playEffect("player.hurt");
            ui.combatToast = UiText.playerDamage(damage);
            ui.combatToastTicks = 55;
        }
    }

    private void hitEnemies(EcsWorld world, int player, int baseDamage) {
        Rectangle attackRect = attackRect(world, player);
        for (int enemy : world.entitiesWith(EnemyComponent.class, PositionComponent.class, ColliderComponent.class,
                HealthComponent.class)) {
            Rectangle enemyRect = CollisionUtil.rect(world.require(enemy, PositionComponent.class),
                    world.require(enemy, ColliderComponent.class));
            if (!attackRect.intersects(enemyRect)) {
                continue;
            }
            HealthComponent health = world.require(enemy, HealthComponent.class);
            if (health.invulnerabilityTicks > 0) {
                continue;
            }
            int defense = world.has(enemy, StatsComponent.class) ? world.require(enemy, StatsComponent.class).defense : 0;
            int damage = Math.max(1, baseDamage - defense);
            health.current -= damage;
            health.invulnerabilityTicks = 20;
            audio.playEffect("attack.hit");
            ui.combatToast = UiText.enemyDamage(world.require(enemy, NameComponent.class).value, damage);
            ui.combatToastTicks = 55;
        }
    }

    private Rectangle attackRect(EcsWorld world, int player) {
        PositionComponent pos = world.require(player, PositionComponent.class);
        ColliderComponent collider = world.require(player, ColliderComponent.class);
        Direction direction = world.require(player, FacingComponent.class).direction;
        int x = (int) pos.x + collider.offsetX;
        int y = (int) pos.y + collider.offsetY;
        return switch (direction) {
        case UP -> new Rectangle(x, y - tileSize, collider.width, tileSize);
        case DOWN -> new Rectangle(x, y + collider.height, collider.width, tileSize);
        case LEFT -> new Rectangle(x - tileSize, y, tileSize, collider.height);
        case RIGHT -> new Rectangle(x + collider.width, y, tileSize, collider.height);
        };
    }
}
