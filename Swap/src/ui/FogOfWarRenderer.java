package ui;

import app.Camera;
import app.GameConfig;
import asset.TileMap;
import component.PositionComponent;
import ecs.EcsWorld;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public final class FogOfWarRenderer {
    private static final int OUTER_DARK_ALPHA = 220;
    private static final int NEAR_BLOCKER_REVEAL_RADIUS = 40;

    private final TileMap map;
    private final Camera camera;
    private final int screenWidth;
    private final int screenHeight;
    private final int tileSize;

    public FogOfWarRenderer(TileMap map, Camera camera, int screenWidth, int screenHeight, int tileSize) {
        this.map = map;
        this.camera = camera;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.tileSize = tileSize;
    }

    public void render(Graphics2D g2, EcsWorld world, int player) {
        Object previousAntialias = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        PositionComponent pos = world.require(player, PositionComponent.class);
        double screenCenterX = pos.x - camera.x() + tileSize / 2.0;
        double screenCenterY = pos.y - camera.y() + tileSize / 2.0;
        double worldCenterX = pos.x + tileSize / 2.0;
        double worldCenterY = pos.y + tileSize / 2.0;

        Ellipse2D.Double clearCircle = new Ellipse2D.Double(
                screenCenterX - GameConfig.FOG_ALWAYS_VISIBLE_RADIUS,
                screenCenterY - GameConfig.FOG_ALWAYS_VISIBLE_RADIUS,
                GameConfig.FOG_ALWAYS_VISIBLE_RADIUS * 2.0,
                GameConfig.FOG_ALWAYS_VISIBLE_RADIUS * 2.0);
        Path2D.Double visiblePolygon = buildVisionPolygon(worldCenterX, worldCenterY, GameConfig.FOG_VISION_RADIUS_PIXELS);

        drawOuterDarkness(g2, visiblePolygon, clearCircle);
        drawVisibilityGradient(g2, visiblePolygon, screenCenterX, screenCenterY);
        drawCenterLight(g2, clearCircle, screenCenterX, screenCenterY);
        drawNearObstacleReveal(g2, clearCircle, worldCenterX, worldCenterY);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, previousAntialias);
    }

    private void drawOuterDarkness(Graphics2D g2, Shape visiblePolygon, Shape clearCircle) {
        Area darkness = new Area(new Rectangle2D.Double(0, 0, screenWidth, screenHeight));
        Area visibleArea = new Area(visiblePolygon);
        visibleArea.add(new Area(clearCircle));
        darkness.subtract(visibleArea);
        g2.setColor(new Color(4, 7, 12, OUTER_DARK_ALPHA));
        g2.fill(darkness);
    }

    private void drawVisibilityGradient(Graphics2D g2, Shape visiblePolygon, double centerX, double centerY) {
        Shape previousClip = g2.getClip();
        Paint previousPaint = g2.getPaint();

        float clearFraction = GameConfig.FOG_ALWAYS_VISIBLE_RADIUS / (float) GameConfig.FOG_VISION_RADIUS_PIXELS;
        g2.setClip(visiblePolygon);
        RadialGradientPaint gradientPaint = new RadialGradientPaint(
                new Point2D.Double(centerX, centerY),
                GameConfig.FOG_VISION_RADIUS_PIXELS,
                new float[] {
                        0f,
                        Math.max(0.01f, clearFraction * 0.7f),
                        clearFraction,
                        Math.min(0.92f, clearFraction + 0.24f),
                        1f
                },
                new Color[] {
                        new Color(4, 7, 12, 0),
                        new Color(4, 7, 12, 0),
                        new Color(4, 7, 12, 18),
                        new Color(4, 7, 12, 108),
                        new Color(4, 7, 12, 184)
                });
        g2.setPaint(gradientPaint);
        g2.fill(new Rectangle2D.Double(
                centerX - GameConfig.FOG_VISION_RADIUS_PIXELS,
                centerY - GameConfig.FOG_VISION_RADIUS_PIXELS,
                GameConfig.FOG_VISION_RADIUS_PIXELS * 2.0,
                GameConfig.FOG_VISION_RADIUS_PIXELS * 2.0));

        g2.setPaint(previousPaint);
        g2.setClip(previousClip);
    }

    private void drawCenterLight(Graphics2D g2, Shape clearCircle, double centerX, double centerY) {
        Shape previousClip = g2.getClip();
        Paint previousPaint = g2.getPaint();

        g2.setClip(clearCircle);
        RadialGradientPaint dayPaint = new RadialGradientPaint(
                new Point2D.Double(centerX, centerY),
                GameConfig.FOG_DAYLIGHT_CORE_RADIUS,
                new float[] { 0f, 0.55f, 1f },
                new Color[] {
                        new Color(255, 249, 229, 56),
                        new Color(255, 243, 204, 26),
                        new Color(255, 243, 204, 0)
                });
        g2.setPaint(dayPaint);
        g2.fill(new Rectangle2D.Double(
                centerX - GameConfig.FOG_DAYLIGHT_CORE_RADIUS,
                centerY - GameConfig.FOG_DAYLIGHT_CORE_RADIUS,
                GameConfig.FOG_DAYLIGHT_CORE_RADIUS * 2.0,
                GameConfig.FOG_DAYLIGHT_CORE_RADIUS * 2.0));

        g2.setPaint(previousPaint);
        g2.setClip(previousClip);
    }

    private void drawNearObstacleReveal(Graphics2D g2, Shape clearCircle, double worldCenterX, double worldCenterY) {
        Shape previousClip = g2.getClip();
        g2.setClip(clearCircle);

        int minCol = Math.max(0, (int) ((worldCenterX - NEAR_BLOCKER_REVEAL_RADIUS) / tileSize));
        int maxCol = Math.min((map.widthPixels() / tileSize) - 1, (int) ((worldCenterX + NEAR_BLOCKER_REVEAL_RADIUS) / tileSize));
        int minRow = Math.max(0, (int) ((worldCenterY - NEAR_BLOCKER_REVEAL_RADIUS) / tileSize));
        int maxRow = Math.min((map.heightPixels() / tileSize) - 1, (int) ((worldCenterY + NEAR_BLOCKER_REVEAL_RADIUS) / tileSize));

        for (int col = minCol; col <= maxCol; col++) {
            for (int row = minRow; row <= maxRow; row++) {
                double tileCenterX = col * tileSize + tileSize / 2.0;
                double tileCenterY = row * tileSize + tileSize / 2.0;
                if (!map.isBlockedPixel(tileCenterX, tileCenterY)) {
                    continue;
                }

                double distance = Point2D.distance(worldCenterX, worldCenterY, tileCenterX, tileCenterY);
                if (distance > NEAR_BLOCKER_REVEAL_RADIUS) {
                    continue;
                }

                float reveal = (float) (1.0 - (distance / NEAR_BLOCKER_REVEAL_RADIUS));
                double screenX = col * tileSize - camera.x();
                double screenY = row * tileSize - camera.y();
                Rectangle2D.Double tileRect = new Rectangle2D.Double(screenX, screenY, tileSize, tileSize);

                g2.setColor(new Color(184, 214, 168, Math.round(24 + 40 * reveal)));
                g2.fill(tileRect);
            }
        }

        g2.setClip(previousClip);
    }

    private Path2D.Double buildVisionPolygon(double worldCenterX, double worldCenterY, double maxRadius) {
        Path2D.Double polygon = new Path2D.Double();
        for (int ray = 0; ray <= GameConfig.FOG_VISION_RAY_COUNT; ray++) {
            double angle = (Math.PI * 2.0 * ray) / GameConfig.FOG_VISION_RAY_COUNT;
            RayHit hit = castVisionRay(worldCenterX, worldCenterY, angle, maxRadius);
            double screenX = hit.worldX() - camera.x();
            double screenY = hit.worldY() - camera.y();
            if (ray == 0) {
                polygon.moveTo(screenX, screenY);
            } else {
                polygon.lineTo(screenX, screenY);
            }
        }
        polygon.closePath();
        return polygon;
    }

    private RayHit castVisionRay(double originX, double originY, double angle, double maxRadius) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double lastVisibleX = originX;
        double lastVisibleY = originY;

        for (double distance = GameConfig.FOG_VISION_STEP_PIXELS; distance <= maxRadius;
                distance += GameConfig.FOG_VISION_STEP_PIXELS) {
            double sampleX = originX + cos * distance;
            double sampleY = originY + sin * distance;
            if (map.isBlockedPixel(sampleX, sampleY)) {
                return refineBlockedRay(lastVisibleX, lastVisibleY, sampleX, sampleY);
            }
            lastVisibleX = sampleX;
            lastVisibleY = sampleY;
        }

        return new RayHit(originX + cos * maxRadius, originY + sin * maxRadius);
    }

    private RayHit refineBlockedRay(double clearX, double clearY, double blockedX, double blockedY) {
        double lowX = clearX;
        double lowY = clearY;
        double highX = blockedX;
        double highY = blockedY;
        for (int i = 0; i < GameConfig.FOG_VISION_REFINE_STEPS; i++) {
            double midX = (lowX + highX) * 0.5;
            double midY = (lowY + highY) * 0.5;
            if (map.isBlockedPixel(midX, midY)) {
                highX = midX;
                highY = midY;
            } else {
                lowX = midX;
                lowY = midY;
            }
        }
        return new RayHit(lowX, lowY);
    }

    private record RayHit(double worldX, double worldY) {
    }
}
