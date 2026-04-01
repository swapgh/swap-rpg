package system.combat;

import java.util.ArrayList;
import java.util.List;

import asset.TileMap;
import audio.AudioService;
import component.world.ColliderComponent;
import component.combat.FactionComponent;
import component.actor.FacingComponent;
import component.combat.HealthComponent;
import component.actor.InputComponent;
import component.actor.NameComponent;
import component.actor.PlayerComponent;
import component.world.PositionComponent;
import component.combat.ProjectileComponent;
import component.combat.ProjectileEmitterComponent;
import component.render.SpriteComponent;
import component.world.VelocityComponent;
import ecs.EcsSystem;
import ecs.EcsWorld;
import ui.runtime.UiState;
import ui.text.UiText;
import util.CollisionUtil;

/** Genera proyectiles desde emitters y resuelve su simulacion. */
public final class ProjectileSystem implements EcsSystem {
    private final TileMap map;
    private final AudioService audio;
    private final UiState ui;
    private final Object perfLock = new Object();
    private long perfSpawnTotalNanos;
    private long perfSpawnMaxNanos;
    private long perfMoveTotalNanos;
    private long perfMoveMaxNanos;
    private long perfTargetCollisionTotalNanos;
    private long perfTargetCollisionMaxNanos;
    private long perfMapCollisionTotalNanos;
    private long perfMapCollisionMaxNanos;
    private long perfAudioTotalNanos;
    private long perfAudioMaxNanos;
    private long perfDestroyTotalNanos;
    private long perfDestroyMaxNanos;
    private int perfSamples;

    public ProjectileSystem(TileMap map, AudioService audio, UiState ui) {
        this.map = map;
        this.audio = audio;
        this.ui = ui;
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        long spawnStart = System.nanoTime();
        spawnProjectiles(world);
        long spawnNanos = System.nanoTime() - spawnStart;
        long moveStart = System.nanoTime();
        moveProjectiles(world);
        long moveNanos = System.nanoTime() - moveStart;
        synchronized (perfLock) {
            perfSamples++;
            perfSpawnTotalNanos += spawnNanos;
            perfSpawnMaxNanos = Math.max(perfSpawnMaxNanos, spawnNanos);
            perfMoveTotalNanos += moveNanos;
            perfMoveMaxNanos = Math.max(perfMoveMaxNanos, moveNanos);
        }
    }

    private void spawnProjectiles(EcsWorld world) {
        List<Integer> players = world.entitiesWith(PlayerComponent.class);
        int player = players.isEmpty() ? -1 : players.get(0);
        PositionComponent playerPos = player == -1 ? null : world.require(player, PositionComponent.class);

        for (int entity : world.entitiesWith(PositionComponent.class, FacingComponent.class, ProjectileEmitterComponent.class,
                FactionComponent.class)) {
            ProjectileEmitterComponent emitter = world.require(entity, ProjectileEmitterComponent.class);
            if (emitter.cooldownRemaining > 0) {
                emitter.cooldownRemaining--;
            }
            if (emitter.cooldownRemaining > 0) {
                continue;
            }

            boolean shouldFire;
            double dx;
            double dy;
            if (emitter.playerTriggered) {
                if (!world.has(entity, InputComponent.class)) {
                    continue;
                }

                InputComponent input = world.require(entity, InputComponent.class);
                shouldFire = input.projectilePressed;
                FacingComponent facing = world.require(entity, FacingComponent.class);
                dx = facing.direction.dx * emitter.projectileSpeed;
                dy = facing.direction.dy * emitter.projectileSpeed;
            } else {
                if (playerPos == null) {
                    continue;
                }
                PositionComponent source = world.require(entity, PositionComponent.class);
                double deltaX = playerPos.x - source.x;
                double deltaY = playerPos.y - source.y;
                double distanceSquared = deltaX * deltaX + deltaY * deltaY;

                shouldFire = distanceSquared < (map.tileSize() * 8.0) * (map.tileSize() * 8.0);
                if (!shouldFire) {
                    continue;
                }
                double magnitude = Math.max(1.0, Math.hypot(deltaX, deltaY));
                dx = (deltaX / magnitude) * emitter.projectileSpeed;
                dy = (deltaY / magnitude) * emitter.projectileSpeed;
            }

            if (!shouldFire) {
                continue;
            }
            spawnProjectile(world, entity, dx, dy, emitter);
            emitter.cooldownRemaining = emitter.cooldownTicks;
            long audioStart = System.nanoTime();
            audio.playEffect("projectile.cast");
            recordAudioPerf(System.nanoTime() - audioStart);
        }
    }

    private void spawnProjectile(EcsWorld world, int owner, double dx, double dy, ProjectileEmitterComponent emitter) {
        PositionComponent origin = world.require(owner, PositionComponent.class);
        int projectile = world.createEntity();

        world.add(projectile, new PositionComponent(origin.x + map.tileSize() / 4.0, origin.y + map.tileSize() / 4.0));
        VelocityComponent velocity = new VelocityComponent();
        velocity.dx = dx;
        velocity.dy = dy;
        world.add(projectile, velocity);
        world.add(projectile, new SpriteComponent(emitter.projectileSpriteId, emitter.projectileSize, emitter.projectileSize, 14));
        world.add(projectile, new ColliderComponent(0, 0, emitter.projectileSize, emitter.projectileSize));
        String sourceFaction = world.require(owner, FactionComponent.class).id;
        world.add(projectile, new ProjectileComponent(owner, sourceFaction, emitter.targetFaction, emitter.projectileDamage,
                emitter.projectileLifetimeTicks));
    }

    private void moveProjectiles(EcsWorld world) {
        List<Integer> destroy = new ArrayList<>();
        List<Integer> targets = world.entitiesWith(PositionComponent.class, ColliderComponent.class, HealthComponent.class,
                FactionComponent.class);
        for (int projectile : world.entitiesWith(ProjectileComponent.class, PositionComponent.class, VelocityComponent.class,
                ColliderComponent.class)) {
            ProjectileComponent projectileComponent = world.require(projectile, ProjectileComponent.class);
            projectileComponent.remainingTicks--;
            if (projectileComponent.remainingTicks <= 0) {
                destroy.add(projectile);
                continue;
            }
            PositionComponent pos = world.require(projectile, PositionComponent.class);
            VelocityComponent vel = world.require(projectile, VelocityComponent.class);
            ColliderComponent col = world.require(projectile, ColliderComponent.class);

            double nextX = pos.x + vel.dx;
            double nextY = pos.y + vel.dy;
            int rectX = CollisionUtil.movedLeft(pos, col, vel.dx);
            int rectY = CollisionUtil.movedTop(pos, col, vel.dy);
            int rectWidth = col.width;
            int rectHeight = col.height;

            long mapCollisionStart = System.nanoTime();
            if (map.isBlockedPixel(rectX, rectY)
                    || map.isBlockedPixel(rectX + rectWidth - 1, rectY)
                    || map.isBlockedPixel(rectX, rectY + rectHeight - 1)
                    || map.isBlockedPixel(rectX + rectWidth - 1, rectY + rectHeight - 1)) {
                recordMapCollisionPerf(System.nanoTime() - mapCollisionStart);
                destroy.add(projectile);
                continue;
            }
            recordMapCollisionPerf(System.nanoTime() - mapCollisionStart);

            boolean hit = false;
            long targetCollisionStart = System.nanoTime();
            for (int target : targets) {
                if (target == projectile || target == projectileComponent.ownerEntity) {
                    continue;
                }
                FactionComponent faction = world.require(target, FactionComponent.class);
                if (!projectileComponent.targetFaction.equals(faction.id)) {
                    continue;
                }
                PositionComponent targetPos = world.require(target, PositionComponent.class);
                ColliderComponent targetCollider = world.require(target, ColliderComponent.class);
                int targetX = CollisionUtil.left(targetPos, targetCollider);
                int targetY = CollisionUtil.top(targetPos, targetCollider);
                if (!CollisionUtil.intersects(rectX, rectY, rectWidth, rectHeight,
                        targetX, targetY, targetCollider.width, targetCollider.height)) {
                    continue;
                }
                HealthComponent health = world.require(target, HealthComponent.class);
                if (health.invulnerabilityTicks > 0) {
                    hit = true;
                    recordTargetCollisionPerf(System.nanoTime() - targetCollisionStart);
                    break;
                }
                health.current = Math.max(0, health.current - projectileComponent.damage);
                health.invulnerabilityTicks = 20;
                long audioStart = System.nanoTime();
                if (world.has(target, PlayerComponent.class)) {
                    audio.playEffect("player.hurt");
                    ui.combatToast = UiText.projectileDamage(projectileComponent.damage);
                } else {
                    audio.playEffect("attack.hit");
                    ui.combatToast = world.has(target, NameComponent.class)
                            ? UiText.enemyDamage(world.require(target, NameComponent.class).value, projectileComponent.damage)
                            : UiText.STATUS_HIT;
                }
                recordAudioPerf(System.nanoTime() - audioStart);
                ui.combatToastTicks = 55;
                hit = true;
                recordTargetCollisionPerf(System.nanoTime() - targetCollisionStart);
                break;
            }
            if (!hit) {
                recordTargetCollisionPerf(System.nanoTime() - targetCollisionStart);
            }
            if (hit) {
                destroy.add(projectile);
                continue;
            }
            pos.x = nextX;
            pos.y = nextY;
        }

        long destroyStart = System.nanoTime();
        for (int projectile : destroy) {
            world.destroyEntity(projectile);
        }
        recordDestroyPerf(System.nanoTime() - destroyStart);
    }

    public List<String> snapshotAndResetPerformance() {
        synchronized (perfLock) {
            int samples = Math.max(1, perfSamples);
            List<String> lines = List.of(
                    formatLine("Projectile spawn", perfSpawnTotalNanos, perfSpawnMaxNanos, samples),
                    formatLine("Projectile move", perfMoveTotalNanos, perfMoveMaxNanos, samples),
                    formatLine("Projectile hit-check", perfTargetCollisionTotalNanos, perfTargetCollisionMaxNanos, samples),
                    formatLine("Projectile map-check", perfMapCollisionTotalNanos, perfMapCollisionMaxNanos, samples),
                    formatLine("Projectile audio", perfAudioTotalNanos, perfAudioMaxNanos, samples),
                    formatLine("Projectile destroy", perfDestroyTotalNanos, perfDestroyMaxNanos, samples));
            perfSamples = 0;
            perfSpawnTotalNanos = 0;
            perfSpawnMaxNanos = 0;
            perfMoveTotalNanos = 0;
            perfMoveMaxNanos = 0;
            perfTargetCollisionTotalNanos = 0;
            perfTargetCollisionMaxNanos = 0;
            perfMapCollisionTotalNanos = 0;
            perfMapCollisionMaxNanos = 0;
            perfAudioTotalNanos = 0;
            perfAudioMaxNanos = 0;
            perfDestroyTotalNanos = 0;
            perfDestroyMaxNanos = 0;
            return lines;
        }
    }

    private void recordTargetCollisionPerf(long nanos) {
        synchronized (perfLock) {
            perfTargetCollisionTotalNanos += nanos;
            perfTargetCollisionMaxNanos = Math.max(perfTargetCollisionMaxNanos, nanos);
        }
    }

    private void recordMapCollisionPerf(long nanos) {
        synchronized (perfLock) {
            perfMapCollisionTotalNanos += nanos;
            perfMapCollisionMaxNanos = Math.max(perfMapCollisionMaxNanos, nanos);
        }
    }

    private void recordAudioPerf(long nanos) {
        synchronized (perfLock) {
            perfAudioTotalNanos += nanos;
            perfAudioMaxNanos = Math.max(perfAudioMaxNanos, nanos);
        }
    }

    private void recordDestroyPerf(long nanos) {
        synchronized (perfLock) {
            perfDestroyTotalNanos += nanos;
            perfDestroyMaxNanos = Math.max(perfDestroyMaxNanos, nanos);
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
