package state;

import java.awt.Graphics2D;
import java.util.List;

public interface Scene {
    void update(double dtSeconds);
    void render(Graphics2D g2);

    default List<String> performanceLines() {
        return List.of();
    }

    default List<String> performanceOverlayLines() {
        return performanceLines();
    }
}
