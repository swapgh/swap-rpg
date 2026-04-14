package system.interaction;

import java.awt.Rectangle;
import component.world.ColliderComponent;
import component.world.PositionComponent;
import util.Direction;

final class InteractionSupport {
    private final int tileSize;

    InteractionSupport(int tileSize) {
        this.tileSize = tileSize;
    }

    Rectangle interactionRect(PositionComponent pos, ColliderComponent collider, Direction direction) {
        int baseX = (int) pos.x + collider.offsetX;
        int baseY = (int) pos.y + collider.offsetY;
        return switch (direction) {
        case UP -> new Rectangle(baseX, baseY - tileSize / 2, collider.width, collider.height + tileSize / 2);
        case DOWN -> new Rectangle(baseX, baseY, collider.width, collider.height + tileSize / 2);
        case LEFT -> new Rectangle(baseX - tileSize / 2, baseY, collider.width + tileSize / 2, collider.height);
        case RIGHT -> new Rectangle(baseX, baseY, collider.width + tileSize / 2, collider.height);
        };
    }
}
