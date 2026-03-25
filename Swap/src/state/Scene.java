package state;

import java.awt.Graphics2D;

public interface Scene {
    void update(double dtSeconds);
    void render(Graphics2D g2);
}
