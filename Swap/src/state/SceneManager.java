package state;

import java.awt.Graphics2D;

public final class SceneManager {
    private Scene current;

    public void setScene(Scene scene) {
        this.current = scene;
    }

    public Scene current() {
        return current;
    }

    public void update(double dtSeconds) {
        if (current != null) {
            current.update(dtSeconds);
        }
    }

    public void render(Graphics2D g2) {
        if (current != null) {
            current.render(g2);
        }
    }
}
