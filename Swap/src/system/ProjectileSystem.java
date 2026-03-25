package system;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import asset.TileMap;
import audio.AudioService;
import component.ColliderComponent;
import component.FactionComponent;
import component.FacingComponent;
import component.HealthComponent;
import component.InputComponent;
import component.NameComponent;
import component.PlayerComponent;
import component.PositionComponent;
import component.ProjectileComponent;
import component.ProjectileEmitterComponent;
import component.SpriteComponent;
import component.VelocityComponent;
import ecs.EcsSystem;
import ecs.EcsWorld;
import ui.UiState;
import util.CollisionUtil;

/** Genera proyectiles desde emitters y resuelve su simulacion. */
public final class ProjectileSystem implements EcsSystem {
    private final TileMap map;
    private final AudioService audio;
    private final UiState ui;

    public ProjectileSystem(TileMap map, AudioService audio, UiState ui) {
        this.map = map;
        this.audio = audio;
        this.ui = ui;
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        spawnProjectiles(world);
        moveProjectiles(world);
    }

    private void spawnProjectiles(EcsWorld world) {
        int player = world.entitiesWith(PlayerComponent.class).isEmpty() ? -1 : world.entitiesWith(PlayerComponent.class).get(0);
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
            audio.playEffect("projectile.cast");
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
            Rectangle rect = CollisionUtil.movedRect(pos, col, vel.dx, vel.dy);

            if (map.isBlockedPixel(rect.x, rect.y)
                    || map.isBlockedPixel(rect.x + rect.width - 1, rect.y)
                    || map.isBlockedPixel(rect.x, rect.y + rect.height - 1)
                    || map.isBlockedPixel(rect.x + rect.width - 1, rect.y + rect.height - 1)) {
                destroy.add(projectile);
                continue;
            }

            boolean hit = false;
            for (int target : world.entitiesWith(PositionComponent.class, ColliderComponent.class, HealthComponent.class, FactionComponent.class)) {
                if (target == projectile || target == projectileComponent.ownerEntity) {
                    continue;
                }
                FactionComponent faction = world.require(target, FactionComponent.class);
                if (!projectileComponent.targetFaction.equals(faction.id)) {
                    continue;
                }
                Rectangle targetRect = CollisionUtil.rect(world.require(target, PositionComponent.class),
                        world.require(target, ColliderComponent.class));
                if (!rect.intersects(targetRect)) {
                    continue;
                }
                HealthComponent health = world.require(target, HealthComponent.class);
                if (health.invulnerabilityTicks > 0) {
                    hit = true;
                    break;
                }
                health.current = Math.max(0, health.current - projectileComponent.damage);
                health.invulnerabilityTicks = 20;
                if (world.has(target, PlayerComponent.class)) {
                    audio.playEffect("player.hurt");
                    ui.toast = "Un proyectil te golpea por " + projectileComponent.damage;
                } else {
                    audio.playEffect("attack.hit");
                    ui.toast = world.has(target, NameComponent.class)
                            ? world.require(target, NameComponent.class).value + " recibe " + projectileComponent.damage
                            : "Impacto";
                }
                ui.toastTicks = 90;
                hit = true;
                break;
            }
            if (hit) {
                destroy.add(projectile);
                continue;
            }
            pos.x = nextX;
            pos.y = nextY;
        }

        for (int projectile : destroy) {
            world.destroyEntity(projectile);
        }
    }
}
