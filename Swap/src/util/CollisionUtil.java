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

    public static int left(PositionComponent pos, ColliderComponent collider) {
        return (int) pos.x + collider.offsetX;
    }

    public static int top(PositionComponent pos, ColliderComponent collider) {
        return (int) pos.y + collider.offsetY;
    }

    public static int movedLeft(PositionComponent pos, ColliderComponent collider, double dx) {
        return (int) (pos.x + dx) + collider.offsetX;
    }

    public static int movedTop(PositionComponent pos, ColliderComponent collider, double dy) {
        return (int) (pos.y + dy) + collider.offsetY;
    }

    public static boolean intersects(int ax, int ay, int aw, int ah, int bx, int by, int bw, int bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }
}
