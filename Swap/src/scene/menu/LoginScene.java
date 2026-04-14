package scene.menu;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.List;
import app.dialog.AccountDialogs;
import app.input.KeyboardState;
import app.bootstrap.GameConfig;
import app.bootstrap.SceneComposer;
import app.prefs.UiPreferencesStore;
import online.auth.OnlineAccountService;
import state.Scene;
import state.SceneManager;
import ui.hud.HudRenderer;
import ui.state.UiState;
import ui.text.UiText;

public final class LoginScene implements Scene {
    private static final int STATUS_TICKS = 300;

    private final KeyboardState keyboard;
    private final SceneManager sceneManager;
    private final SceneComposer sceneFactory;
    private final HudRenderer hud;
    private final UiState ui;
    private final OnlineAccountService accountService;
    private final UiPreferencesStore uiPreferences = new UiPreferencesStore();
    private final int screenWidth;
    private final int screenHeight;
    private int selectedIndex;
    private String statusMessage = "";
    private int statusTicks;

    public LoginScene(KeyboardState keyboard, SceneManager sceneManager, SceneComposer sceneFactory, HudRenderer hud,
            UiState ui, OnlineAccountService accountService, int screenWidth, int screenHeight) {
        this.keyboard = keyboard;
        this.sceneManager = sceneManager;
        this.sceneFactory = sceneFactory;
        this.hud = hud;
        this.ui = ui;
        this.accountService = accountService;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.ui.titleMessage = UiText.GAME_TITLE;
    }

    @Override
    public void update(double dtSeconds) {
        if (statusTicks > 0) {
            statusTicks--;
        }

        if (keyboard.consumePressed(KeyEvent.VK_W) || keyboard.consumePressed(KeyEvent.VK_UP)) {
            selectedIndex = (selectedIndex - 1 + options().size()) % options().size();
        } else if (keyboard.consumePressed(KeyEvent.VK_S) || keyboard.consumePressed(KeyEvent.VK_DOWN)) {
            selectedIndex = (selectedIndex + 1) % options().size();
        }

        if (keyboard.consumePressed(KeyEvent.VK_ENTER) || keyboard.consumePressed(KeyEvent.VK_E)) {
            activateSelection();
        }
    }

    @Override
    public void render(Graphics2D g2) {
        hud.drawTitleMenu(
                g2,
                ui.titleMessage,
                UiText.MENU_ACCESS,
                accountService.displayLabel(),
                accountService.isLoggedIn(),
                options(),
                selectedIndex,
                statusTicks > 0 && statusMessage != null && !statusMessage.isBlank() ? statusMessage : UiText.FOOTER_SELECT,
                screenWidth,
                screenHeight);
    }

    private void activateSelection() {
        switch (selectedIndex) {
        case 0 -> {
            statusMessage = AccountDialogs.showLogin(accountService);
            statusTicks = STATUS_TICKS;
            keyboard.reset();
            if (!UiText.LOGIN_CANCELLED.equals(statusMessage) && accountService.isLoggedIn()) {
                sceneManager.setScene(sceneFactory.createTitleScene(statusMessage));
            }
        }
        case 1 -> {
            statusMessage = AccountDialogs.showRegister(accountService);
            statusTicks = STATUS_TICKS;
            keyboard.reset();
            if (!UiText.REGISTER_CANCELLED.equals(statusMessage) && accountService.isLoggedIn()) {
                sceneManager.setScene(sceneFactory.createTitleScene(statusMessage));
            }
        }
        case 2 -> {
            if (accountService.isLoggedIn()) {
                accountService.logout();
            }
            statusMessage = UiText.LOGIN_AS_GUEST;
            statusTicks = STATUS_TICKS;
            keyboard.reset();
            sceneManager.setScene(sceneFactory.createTitleScene());
        }
        case 3 -> {
            UiText.toggleLanguage();
            uiPreferences.saveLanguage(GameConfig.UI_PREFERENCES_FILE, UiText.language());
            ui.titleMessage = UiText.GAME_TITLE;
            statusMessage = UiText.languageOption();
            statusTicks = STATUS_TICKS;
        }
        default -> {
        }
        }
    }

    private List<String> options() {
        return List.of(
                UiText.MENU_LOGIN,
                UiText.MENU_REGISTER,
                UiText.MENU_GUEST,
                UiText.languageOption());
    }
}
