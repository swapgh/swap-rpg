package system.combat;

import java.util.List;

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
import ui.runtime.UiState;
import ui.text.UiText;
import util.CollisionUtil;
import util.Direction;

public final class CombatSystem implements EcsSystem {
    private final UiState ui;
    private final AudioService audio;
    private final int tileSize;
    private final Object perfLock = new Object();
    private long perfPlayerContactTotalNanos;
    private long perfPlayerContactMaxNanos;
    private long perfAttackHitTotalNanos;
    private long perfAttackHitMaxNanos;
    private long perfAudioTotalNanos;
    private long perfAudioMaxNanos;
    private int perfSamples;

    public CombatSystem(UiState ui, AudioService audio, int tileSize) {
        this.ui = ui;
        this.audio = audio;
        this.tileSize = tileSize;
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        long playerContactNanos = 0;
        long attackHitNanos = 0;
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
            long audioStart = System.nanoTime();
            audio.playEffect("attack.swing");
            recordAudioPerf(System.nanoTime() - audioStart);
            attack.activeTicks = 10;
            attack.cooldownRemaining = attack.cooldownTicks;
            long attackStart = System.nanoTime();
            hitEnemies(world, player, attack.power);
            attackHitNanos += System.nanoTime() - attackStart;
        }

        PositionComponent playerPos = world.require(player, PositionComponent.class);
        VelocityComponent playerVelocity = world.require(player, VelocityComponent.class);
        ColliderComponent playerCollider = world.require(player, ColliderComponent.class);
        HealthComponent playerHealth = world.require(player, HealthComponent.class);
        StatsComponent playerStats = world.require(player, StatsComponent.class);
        int playerX = CollisionUtil.left(playerPos, playerCollider);
        int playerY = CollisionUtil.top(playerPos, playerCollider);
        int playerTouchX = CollisionUtil.movedLeft(playerPos, playerCollider, playerVelocity.dx);
        int playerTouchY = CollisionUtil.movedTop(playerPos, playerCollider, playerVelocity.dy);
        List<Integer> enemies = world.entitiesWith(EnemyComponent.class, PositionComponent.class, ColliderComponent.class,
                StatsComponent.class, HealthComponent.class);
        long playerContactStart = System.nanoTime();
        for (int enemy : enemies) {
            PositionComponent enemyPos = world.require(enemy, PositionComponent.class);
            ColliderComponent enemyCollider = world.require(enemy, ColliderComponent.class);
            int enemyX = CollisionUtil.left(enemyPos, enemyCollider);
            int enemyY = CollisionUtil.top(enemyPos, enemyCollider);
            boolean touchingPlayer = CollisionUtil.intersects(playerX, playerY, playerCollider.width, playerCollider.height,
                    enemyX, enemyY, enemyCollider.width, enemyCollider.height);
            boolean touchingMovedPlayer = CollisionUtil.intersects(playerTouchX, playerTouchY, playerCollider.width,
                    playerCollider.height, enemyX, enemyY, enemyCollider.width, enemyCollider.height);
            if (!touchingPlayer && !touchingMovedPlayer) {
                continue;
            }
            if (playerHealth.invulnerabilityTicks > 0) {
                continue;
            }
            int damage = Math.max(1, world.require(enemy, StatsComponent.class).attack - playerStats.defense + 1);
            playerHealth.current = Math.max(0, playerHealth.current - damage);
            playerHealth.invulnerabilityTicks = 45;
            long audioStart = System.nanoTime();
            audio.playEffect("player.hurt");
            recordAudioPerf(System.nanoTime() - audioStart);
            ui.combatToast = UiText.playerDamage(damage);
            ui.combatToastTicks = 55;
        }
        playerContactNanos += System.nanoTime() - playerContactStart;
        synchronized (perfLock) {
            perfSamples++;
            perfPlayerContactTotalNanos += playerContactNanos;
            perfPlayerContactMaxNanos = Math.max(perfPlayerContactMaxNanos, playerContactNanos);
            perfAttackHitTotalNanos += attackHitNanos;
            perfAttackHitMaxNanos = Math.max(perfAttackHitMaxNanos, attackHitNanos);
        }
    }

    private void hitEnemies(EcsWorld world, int player, int baseDamage) {
        int[] attackRect = attackRect(world, player);
        List<Integer> enemies = world.entitiesWith(EnemyComponent.class, PositionComponent.class, ColliderComponent.class,
                HealthComponent.class);
        for (int enemy : enemies) {
            PositionComponent enemyPos = world.require(enemy, PositionComponent.class);
            ColliderComponent enemyCollider = world.require(enemy, ColliderComponent.class);
            int enemyX = CollisionUtil.left(enemyPos, enemyCollider);
            int enemyY = CollisionUtil.top(enemyPos, enemyCollider);
            if (!CollisionUtil.intersects(attackRect[0], attackRect[1], attackRect[2], attackRect[3],
                    enemyX, enemyY, enemyCollider.width, enemyCollider.height)) {
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
            long audioStart = System.nanoTime();
            audio.playEffect("attack.hit");
            recordAudioPerf(System.nanoTime() - audioStart);
            ui.combatToast = UiText.enemyDamage(world.require(enemy, NameComponent.class).value, damage);
            ui.combatToastTicks = 55;
        }
    }

    public List<String> snapshotAndResetPerformance() {
        synchronized (perfLock) {
            int samples = Math.max(1, perfSamples);
            List<String> lines = List.of(
                    formatLine("Combat contact", perfPlayerContactTotalNanos, perfPlayerContactMaxNanos, samples),
                    formatLine("Combat melee-hit", perfAttackHitTotalNanos, perfAttackHitMaxNanos, samples),
                    formatLine("Combat audio", perfAudioTotalNanos, perfAudioMaxNanos, samples));
            perfSamples = 0;
            perfPlayerContactTotalNanos = 0;
            perfPlayerContactMaxNanos = 0;
            perfAttackHitTotalNanos = 0;
            perfAttackHitMaxNanos = 0;
            perfAudioTotalNanos = 0;
            perfAudioMaxNanos = 0;
            return lines;
        }
    }

    private int[] attackRect(EcsWorld world, int player) {
        PositionComponent pos = world.require(player, PositionComponent.class);
        ColliderComponent collider = world.require(player, ColliderComponent.class);
        Direction direction = world.require(player, FacingComponent.class).direction;
        int x = (int) pos.x + collider.offsetX;
        int y = (int) pos.y + collider.offsetY;
        return switch (direction) {
        case UP -> new int[] { x, y - tileSize, collider.width, tileSize };
        case DOWN -> new int[] { x, y + collider.height, collider.width, tileSize };
        case LEFT -> new int[] { x - tileSize, y, tileSize, collider.height };
        case RIGHT -> new int[] { x + collider.width, y, tileSize, collider.height };
        };
    }

    private void recordAudioPerf(long nanos) {
        synchronized (perfLock) {
            perfAudioTotalNanos += nanos;
            perfAudioMaxNanos = Math.max(perfAudioMaxNanos, nanos);
        }
    }

    private String formatLine(String label, long totalNanos, long maxNanos, int samples) {
        return String.format("%s  %.2fms avg  %.2fms max",
                label,
                nanosToMillis(totalNanos / (double) samples),
                nanosToMillis(maxNanos));
    }

    private double nanosToMillis(double nanos) {
        return nanos / 1_000_000.0;
    }
}
