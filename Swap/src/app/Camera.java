package app;

public final class Camera {
    private double x;
    private double y;

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public void centerOn(double worldX, double worldY, int screenWidth, int screenHeight) {
        x = worldX - (screenWidth / 2.0);
        y = worldY - (screenHeight / 2.0);
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
    }
}
