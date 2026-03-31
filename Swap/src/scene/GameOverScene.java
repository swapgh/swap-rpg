package scene;

import app.GameConfig;
import app.GameSceneFactory;
import app.KeyboardState;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import state.Scene;
import state.SceneManager;
import ui.HudRenderer;
import ui.UiText;

public final class GameOverScene implements Scene {
    private static final List<String> OPTIONS = List.of(
            UiText.MENU_CONTINUE,
            UiText.MENU_LOAD_SAVE,
            UiText.MENU_NEW_GAME,
            UiText.MENU_MAIN_MENU,
            UiText.MENU_CLOSE_APP);

    private final KeyboardState keyboard;
    private final SceneManager sceneManager;
    private final GameSceneFactory sceneFactory;
    private final HudRenderer hud;
    private final WorldScene backgroundScene;
    private final int screenWidth;
    private final int screenHeight;
    private int selectedIndex;
    private String statusMessage = "";

    public GameOverScene(KeyboardState keyboard, SceneManager sceneManager, GameSceneFactory sceneFactory, HudRenderer hud,
            WorldScene backgroundScene, int screenWidth, int screenHeight) {
        this.keyboard = keyboard;
        this.sceneManager = sceneManager;
        this.sceneFactory = sceneFactory;
        this.hud = hud;
        this.backgroundScene = backgroundScene;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    @Override
    public void update(double dtSeconds) {
        if (keyboard.consumePressed(KeyEvent.VK_W) || keyboard.consumePressed(KeyEvent.VK_UP)) {
            selectedIndex = (selectedIndex - 1 + OPTIONS.size()) % OPTIONS.size();
        } else if (keyboard.consumePressed(KeyEvent.VK_S) || keyboard.consumePressed(KeyEvent.VK_DOWN)) {
            selectedIndex = (selectedIndex + 1) % OPTIONS.size();
        }

        if (keyboard.consumePressed(KeyEvent.VK_ENTER) || keyboard.consumePressed(KeyEvent.VK_E)) {
            activateSelection();
        }
    }

    @Override
    public void render(Graphics2D g2) {
        backgroundScene.render(g2);
        drawBackdrop(g2);
        hud.drawCompactGameOverOverlay(g2, OPTIONS, selectedIndex, screenWidth, screenHeight);
    }

    private void drawBackdrop(Graphics2D g2) {
        g2.setColor(new Color(6, 8, 12, 55));
        g2.fillRect(0, 0, screenWidth, screenHeight);
    }

    private void activateSelection() {
        switch (selectedIndex) {
        case 0 -> continueFromAutoSave();
        case 1 -> loadManualSave();
        case 2 -> startNewGame();
        case 3 -> sceneManager.setScene(sceneFactory.createTitleScene());
        case 4 -> System.exit(0);
        default -> {
        }
        }
    }

    private void continueFromAutoSave() {
        if (!Files.exists(GameConfig.AUTO_SAVE_FILE)) {
            statusMessage = UiText.STATUS_NO_SAVE;
            return;
        }
        WorldScene worldScene = sceneFactory.createWorldScene(GameConfig.AUTO_SAVE_FILE);
        worldScene.prepareForPlay();
        sceneManager.setScene(worldScene);
    }

    private void loadManualSave() {
        if (!Files.exists(GameConfig.MANUAL_SAVE_FILE)) {
            statusMessage = UiText.STATUS_NO_MANUAL_SAVE;
            return;
        }
        WorldScene worldScene = sceneFactory.createWorldScene(GameConfig.MANUAL_SAVE_FILE);
        worldScene.prepareForPlay();
        sceneManager.setScene(worldScene);
    }

    private void startNewGame() {
        try {
            Files.deleteIfExists(GameConfig.AUTO_SAVE_FILE);
            Files.deleteIfExists(GameConfig.MANUAL_SAVE_FILE);
        } catch (IOException ex) {
            statusMessage = UiText.STATUS_RESET_FAILED;
            return;
        }
        WorldScene worldScene = sceneFactory.createWorldScene(GameConfig.AUTO_SAVE_FILE);
        worldScene.prepareForPlay();
        sceneManager.setScene(worldScene);
    }
}
