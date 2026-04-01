package system.render;

import asset.AssetManager;
import component.render.AnimationComponent;
import component.render.AnimationSetComponent;
import component.combat.AttackComponent;
import component.actor.FacingComponent;
import component.render.SpriteComponent;
import component.world.VelocityComponent;
import ecs.EcsSystem;
import ecs.EcsWorld;
import util.Direction;

public final class AnimationSystem implements EcsSystem {
    private final AssetManager assets;

    public AnimationSystem(AssetManager assets) {
        this.assets = assets;
    }

    @Override
    public void update(EcsWorld world, double dtSeconds) {
        for (int entity : world.entitiesWith(SpriteComponent.class, AnimationComponent.class, AnimationSetComponent.class,
                FacingComponent.class, VelocityComponent.class)) {
            SpriteComponent sprite = world.require(entity, SpriteComponent.class);
            AnimationComponent animation = world.require(entity, AnimationComponent.class);
            String clipId = selectClip(world, entity);
            if (!clipId.equals(animation.clipId)) {
                animation.clipId = clipId;
                animation.frameIndex = 0;
                animation.tick = 0;
            }
            animation.tick++;
            String[] frames = assets.clip(animation.clipId);
            int ticksPerFrame = animation.ticksPerFrame;
            if (world.has(entity, AttackComponent.class)) {
                AttackComponent attack = world.require(entity, AttackComponent.class);
                if (attack.activeTicks > 0) {
                    ticksPerFrame = Math.max(3, animation.ticksPerFrame / 4);
                }
            }
            if (animation.tick >= ticksPerFrame) {
                animation.tick = 0;
                animation.frameIndex = (animation.frameIndex + 1) % frames.length;
            }
            sprite.imageId = frames[animation.frameIndex];
        }
    }

    private String selectClip(EcsWorld world, int entity) {
        AnimationSetComponent set = world.require(entity, AnimationSetComponent.class);
        Direction direction = world.require(entity, FacingComponent.class).direction;
        VelocityComponent velocity = world.require(entity, VelocityComponent.class);
        boolean moving = velocity.dx != 0 || velocity.dy != 0;
        if (world.has(entity, AttackComponent.class)) {
            AttackComponent attack = world.require(entity, AttackComponent.class);
            if (attack.activeTicks > 0 && set.attackBase != null) {
                return set.attackBase + "." + direction.name().toLowerCase();
            }
        }
        if (moving) {
            return set.walkBase + "." + direction.name().toLowerCase();
        }
        return set.idleBase + "." + direction.name().toLowerCase();
    }
}
