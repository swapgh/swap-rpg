package content.prefab;

import component.render.AnimationComponent;
import component.render.AnimationSetComponent;
import component.character.FacingComponent;
import component.render.SpriteComponent;
import data.VisualData;
import ecs.EcsWorld;
import util.Direction;

final class PrefabVisualSupport {
    private PrefabVisualSupport() {
    }

    static Direction direction(String value) {
        return Direction.valueOf(value.toUpperCase());
    }

    static String directionSuffix(Direction direction) {
        return direction.name().toLowerCase();
    }

    static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    static int scaledSize(int tileSize, double scale) {
        return Math.max(1, (int) Math.round(tileSize * scale));
    }

    static void addAnimatedSprite(EcsWorld world, int entity, VisualData visual, int tileSize) {
        Direction initialFacing = direction(visual.initialFacing());
        world.add(entity, new FacingComponent(initialFacing));
        world.add(entity, new SpriteComponent(initialSpriteId(visual), tileSize, tileSize, visual.layer()));
        world.add(entity, new AnimationComponent(
                visual.idleBase() + "." + directionSuffix(initialFacing),
                visual.animationFrameTicks()));
        world.add(entity, new AnimationSetComponent(
                visual.idleBase(),
                visual.walkBase(),
                blankToNull(visual.attackBase())));
    }

    private static String initialSpriteId(VisualData visual) {
        String[] parts = visual.idleBase().split("\\.");
        if (parts.length >= 2) {
            StringBuilder base = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                if (i > 0) {
                    base.append('.');
                }
                base.append(parts[i]);
            }
            return base + "." + directionSuffix(direction(visual.initialFacing())) + "." + visual.initialFrame();
        }
        return visual.idleBase() + "." + directionSuffix(direction(visual.initialFacing())) + "."
                + visual.initialFrame();
    }
}
