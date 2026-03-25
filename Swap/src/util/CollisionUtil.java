package util;

import java.awt.Rectangle;

import component.ColliderComponent;
import component.PositionComponent;

public final class CollisionUtil {
    private CollisionUtil() {
    }

    public static Rectangle rect(PositionComponent pos, ColliderComponent collider) {
        return new Rectangle((int) pos.x + collider.offsetX, (int) pos.y + collider.offsetY, collider.width, collider.height);
    }

    public static Rectangle movedRect(PositionComponent pos, ColliderComponent collider, double dx, double dy) {
        return new Rectangle((int) (pos.x + dx) + collider.offsetX, (int) (pos.y + dy) + collider.offsetY, collider.width,
                collider.height);
    }
}
