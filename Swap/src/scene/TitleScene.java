package scene;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import app.KeyboardState;
import state.Scene;
import state.SceneManager;
import ui.HudRenderer;
import ui.UiState;

public final class TitleScene implements Scene {
    private final KeyboardState keyboard;
    private final SceneManager sceneManager;
    private final Scene nextScene;
    private final HudRenderer hud;
    private final UiState ui;
    private final int screenWidth;
    private final int screenHeight;

    public TitleScene(KeyboardState keyboard, SceneManager sceneManager, Scene nextScene, HudRenderer hud, UiState ui,
            int screenWidth, int screenHeight) {
        this.keyboard = keyboard;
        this.sceneManager = sceneManager;
        this.nextScene = nextScene;
        this.hud = hud;
        this.ui = ui;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    @Override
    public void update(double dtSeconds) {
        if (keyboard.consumePressed(KeyEvent.VK_ENTER)) {
            ui.mode = state.GameMode.PLAY;
            ui.subtitleMessage = "WASD mueve, E interactua, ESPACIO melee, F proyectil, I inventario.";
            sceneManager.setScene(nextScene);
        }
    }

    @Override
    public void render(Graphics2D g2) {
        hud.drawTitle(g2, ui, screenWidth, screenHeight);
    }
}
