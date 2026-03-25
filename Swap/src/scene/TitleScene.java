package scene;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import app.AccountDialogs;
import app.KeyboardState;
import online.OnlineAccountService;
import state.Scene;
import state.SceneManager;
import ui.HudRenderer;
import ui.UiState;

public final class TitleScene implements Scene {
    private static final int STATUS_TICKS = 300;

    private final KeyboardState keyboard;
    private final SceneManager sceneManager;
    private final Scene nextScene;
    private final HudRenderer hud;
    private final UiState ui;
    private final OnlineAccountService accountService;
    private final int screenWidth;
    private final int screenHeight;
    private String statusMessage = "";
    private int statusTicks;

    public TitleScene(KeyboardState keyboard, SceneManager sceneManager, Scene nextScene, HudRenderer hud, UiState ui,
            OnlineAccountService accountService, int screenWidth, int screenHeight) {
        this.keyboard = keyboard;
        this.sceneManager = sceneManager;
        this.nextScene = nextScene;
        this.hud = hud;
        this.ui = ui;
        this.accountService = accountService;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    @Override
    public void update(double dtSeconds) {
        if (statusTicks > 0) {
            statusTicks--;
        }

        if (keyboard.consumePressed(KeyEvent.VK_L)) {
            statusMessage = AccountDialogs.showLogin(accountService);
            statusTicks = STATUS_TICKS;
            ui.subtitleMessage = buildSubtitle();
            return;
        }

        if (keyboard.consumePressed(KeyEvent.VK_R)) {
            statusMessage = AccountDialogs.showRegister(accountService);
            statusTicks = STATUS_TICKS;
            ui.subtitleMessage = buildSubtitle();
            return;
        }

        if (keyboard.consumePressed(KeyEvent.VK_U)) {
            accountService.logout();
            statusMessage = "Sesion cerrada. Ahora juegas como invitado.";
            statusTicks = STATUS_TICKS;
            ui.subtitleMessage = buildSubtitle();
            return;
        }

        if (keyboard.consumePressed(KeyEvent.VK_ENTER)) {
            ui.mode = state.GameMode.PLAY;
            ui.subtitleMessage = "WASD mueve, E interactua, ESPACIO melee, F proyectil, I inventario, Y sync.";
            sceneManager.setScene(nextScene);
            return;
        }

        ui.subtitleMessage = buildSubtitle();
    }

    @Override
    public void render(Graphics2D g2) {
        hud.drawTitle(g2, ui, screenWidth, screenHeight);
    }

    private String buildSubtitle() {
        String accountLine = accountService.isLoggedIn()
                ? "Cuenta: conectada como " + accountService.displayLabel()
                : "Cuenta: Invitado";

        if (statusTicks > 0 && statusMessage != null && !statusMessage.isBlank()) {
            return String.join("\n",
                    "ENTER  juega",
                    "L      login",
                    "R      registro",
                    "U      logout",
                    accountLine,
                    statusMessage);
        }

        return String.join("\n",
                "ENTER  juega",
                "L      login",
                "R      registro",
                "U      logout",
                accountLine);
    }
}
