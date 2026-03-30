package scene;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import app.AccountDialogs;
import app.GameConfig;
import app.GameSceneFactory;
import app.KeyboardState;
import online.OnlineAccountService;
import state.Scene;
import state.SceneManager;
import ui.HudRenderer;
import ui.UiState;
import ui.UiText;

public final class TitleScene implements Scene {
    private static final int STATUS_TICKS = 300;

    private final KeyboardState keyboard;
    private final SceneManager sceneManager;
    private final GameSceneFactory sceneFactory;
    private final HudRenderer hud;
    private final UiState ui;
    private final OnlineAccountService accountService;
    private final int screenWidth;
    private final int screenHeight;
    private int selectedIndex;
    private String statusMessage = "";
    private int statusTicks;

    public TitleScene(KeyboardState keyboard, SceneManager sceneManager, GameSceneFactory sceneFactory, HudRenderer hud, UiState ui,
            OnlineAccountService accountService, int screenWidth, int screenHeight) {
        this.keyboard = keyboard;
        this.sceneManager = sceneManager;
        this.sceneFactory = sceneFactory;
        this.hud = hud;
        this.ui = ui;
        this.accountService = accountService;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.selectedIndex = 0;
        ui.titleMessage = UiText.GAME_TITLE;
    }

    @Override
    public void update(double dtSeconds) {
        if (statusTicks > 0) {
            statusTicks--;
        }

        if (consumeMenuUp()) {
            selectedIndex = (selectedIndex - 1 + currentOptions().size()) % currentOptions().size();
        } else if (consumeMenuDown()) {
            selectedIndex = (selectedIndex + 1) % currentOptions().size();
        }

        if (keyboard.consumePressed(KeyEvent.VK_ENTER) || keyboard.consumePressed(KeyEvent.VK_E)) {
            activateSelection();
            return;
        }
    }

    @Override
    public void render(Graphics2D g2) {
        hud.drawTitleMenu(
                g2,
                ui.titleMessage,
                UiText.MENU_MAIN,
                accountService.displayLabel(),
                accountService.isLoggedIn(),
                currentOptions(),
                selectedIndex,
                currentFooter(),
                screenWidth,
                screenHeight);
    }

    private void activateSelection() {
        activateMainMenuSelection();
    }

    private void activateMainMenuSelection() {
        switch (selectedIndex) {
        case 0 -> continueGame();
        case 1 -> startNewGame();
        case 2 -> handleAccountOption();
        case 3 -> System.exit(0);
        default -> {
        }
        }
    }

    private void continueGame() {
        if (!hasSaveFile()) {
            statusMessage = UiText.STATUS_NO_SAVE;
            statusTicks = STATUS_TICKS;
            return;
        }
        launchWorld();
    }

    private void startNewGame() {
        try {
            Files.deleteIfExists(GameConfig.SAVE_FILE);
        } catch (IOException ex) {
            statusMessage = UiText.STATUS_RESET_FAILED;
            statusTicks = STATUS_TICKS;
            return;
        }
        launchWorld();
    }

    private void launchWorld() {
        WorldScene worldScene = sceneFactory.createWorldScene();
        worldScene.prepareForPlay();
        sceneManager.setScene(worldScene);
    }

    private boolean hasSaveFile() {
        return Files.exists(GameConfig.SAVE_FILE);
    }

    private List<String> currentOptions() {
        return List.of(
                UiText.MENU_CONTINUE,
                UiText.MENU_NEW_GAME,
                UiText.menuAccountOption(accountService.isLoggedIn()),
                UiText.MENU_EXIT);
    }

    private String currentFooter() {
        String base = UiText.footerForSave(hasSaveFile());
        if (statusTicks > 0 && statusMessage != null && !statusMessage.isBlank()) {
            return statusMessage;
        }
        return base;
    }

    private void handleAccountOption() {
        if (accountService.isLoggedIn()) {
            accountService.logout();
            keyboard.reset();
            sceneManager.setScene(sceneFactory.createLoginScene());
            return;
        }

        statusMessage = AccountDialogs.showLogin(accountService);
        statusTicks = STATUS_TICKS;
        keyboard.reset();
    }

    private boolean consumeMenuUp() {
        return keyboard.consumePressed(KeyEvent.VK_W) || keyboard.consumePressed(KeyEvent.VK_UP);
    }

    private boolean consumeMenuDown() {
        return keyboard.consumePressed(KeyEvent.VK_S) || keyboard.consumePressed(KeyEvent.VK_DOWN);
    }
}
