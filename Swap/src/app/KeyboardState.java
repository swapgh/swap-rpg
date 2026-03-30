package app;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

public final class KeyboardState implements KeyListener {
    private final Set<Integer> down = new HashSet<>();
    private final Set<Integer> pressedThisFrame = new HashSet<>();

    public boolean isDown(int keyCode) {
        return down.contains(keyCode);
    }

    public boolean consumePressed(int keyCode) {
        return pressedThisFrame.remove(keyCode);
    }

    public void endFrame() {
        pressedThisFrame.clear();
    }

    public void reset() {
        down.clear();
        pressedThisFrame.clear();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (down.add(keyCode)) {
            pressedThisFrame.add(keyCode);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        down.remove(e.getKeyCode());
    }
}
