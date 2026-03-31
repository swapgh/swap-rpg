package scene.menu;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import app.GameSceneFactory;
import app.KeyboardState;
import app.SaveDialogs;
import online.OnlineAccountService;
import save.SaveManager;
import save.SaveReference;
import save.SaveSlotMetadata;
import scene.gameplay.WorldScene;
import state.Scene;
import state.SceneManager;
import ui.hud.HudRenderer;
import ui.runtime.UiState;
import ui.text.UiText;

public final class TitleScene implements Scene {
    private static final int STATUS_TICKS = 300;

    private final KeyboardState keyboard;
    private final SceneManager sceneManager;
    private final GameSceneFactory sceneFactory;
    private final HudRenderer hud;
    private final UiState ui;
    private final OnlineAccountService accountService;
    private final SaveManager saveManager;
    private final int screenWidth;
    private final int screenHeight;
    private int selectedIndex;
    private String statusMessage = "";
    private int statusTicks;
    private boolean selectingManualSave;
    private SaveSlotMetadata selectedManualSave;
    private int saveActionIndex;

    public TitleScene(KeyboardState keyboard, SceneManager sceneManager, GameSceneFactory sceneFactory, HudRenderer hud, UiState ui,
            OnlineAccountService accountService, SaveManager saveManager, int screenWidth, int screenHeight) {
        this.keyboard = keyboard;
        this.sceneManager = sceneManager;
        this.sceneFactory = sceneFactory;
        this.hud = hud;
        this.ui = ui;
        this.accountService = accountService;
        this.saveManager = saveManager;
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
            if (selectedManualSave != null) {
                saveActionIndex = (saveActionIndex - 1 + currentOptions().size()) % currentOptions().size();
            } else {
                selectedIndex = (selectedIndex - 1 + currentOptions().size()) % currentOptions().size();
            }
        } else if (consumeMenuDown()) {
            if (selectedManualSave != null) {
                saveActionIndex = (saveActionIndex + 1) % currentOptions().size();
            } else {
                selectedIndex = (selectedIndex + 1) % currentOptions().size();
            }
        }

        if (selectingManualSave && (keyboard.consumePressed(KeyEvent.VK_ESCAPE) || keyboard.consumePressed(KeyEvent.VK_BACK_SPACE))) {
            if (selectedManualSave != null) {
                selectedManualSave = null;
                saveActionIndex = 0;
            } else {
                selectingManualSave = false;
                selectedIndex = 0;
            }
            return;
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
                currentSectionTitle(),
                accountService.displayLabel(),
                accountService.isLoggedIn(),
                currentOptions(),
                currentSelectedIndex(),
                currentFooter(),
                screenWidth,
                screenHeight);
    }

    private void activateSelection() {
        if (selectingManualSave) {
            if (selectedManualSave != null) {
                activateManualSaveAction();
            } else {
                activateManualSaveSelection();
            }
        } else {
            activateMainMenuSelection();
        }
    }

    private void activateMainMenuSelection() {
        switch (selectedIndex) {
        case 0 -> continueGame();
        case 1 -> openManualLoadMenu();
        case 2 -> startNewGame();
        case 3 -> returnToAccess();
        case 4 -> System.exit(0);
        default -> {
        }
        }
    }

    private void continueGame() {
        SaveReference reference = saveManager.selectContinueReference().orElse(null);
        if (reference == null) {
            statusMessage = UiText.STATUS_NO_SAVE;
            statusTicks = STATUS_TICKS;
            return;
        }
        launchWorld(reference);
    }

    private void startNewGame() {
        saveManager.deleteAutosaves();
        WorldScene worldScene = sceneFactory.createNewWorldScene();
        worldScene.prepareForPlay();
        sceneManager.setScene(worldScene);
    }

    private void launchWorld(SaveReference reference) {
        WorldScene worldScene = sceneFactory.createWorldScene(reference);
        worldScene.prepareForPlay();
        sceneManager.setScene(worldScene);
    }

    private void openManualLoadMenu() {
        if (!saveManager.hasManualSaves()) {
            statusMessage = UiText.STATUS_NO_MANUAL_SAVE;
            statusTicks = STATUS_TICKS;
            return;
        }
        selectingManualSave = true;
        selectedManualSave = null;
        selectedIndex = 0;
    }

    private void activateManualSaveSelection() {
        List<SaveSlotMetadata> saves = saveManager.listManualSaves();
        if (selectedIndex >= saves.size()) {
            selectingManualSave = false;
            selectedIndex = 0;
            return;
        }
        selectedManualSave = saves.get(selectedIndex);
        saveActionIndex = 0;
    }

    private void activateManualSaveAction() {
        switch (saveActionIndex) {
        case 0 -> {
            saveManager.markLastUsed(selectedManualSave.reference());
            launchWorld(selectedManualSave.reference());
        }
        case 1 -> renameSelectedManualSave();
        case 2 -> deleteSelectedManualSave();
        case 3 -> {
            selectedManualSave = null;
            saveActionIndex = 0;
        }
        default -> {
        }
        }
    }

    private void renameSelectedManualSave() {
        String renamed = SaveDialogs.showManualSaveName(selectedManualSave.displayName());
        if (renamed == null) {
            return;
        }
        selectedManualSave = saveManager.renameManualSave(selectedManualSave.reference().slotId(), renamed);
        statusMessage = UiText.STATUS_SAVE_RENAMED;
        statusTicks = STATUS_TICKS;
    }

    private void deleteSelectedManualSave() {
        if (!SaveDialogs.confirmDeleteSave(selectedManualSave.displayName())) {
            return;
        }
        saveManager.deleteManualSave(selectedManualSave.reference().slotId());
        selectedManualSave = null;
        saveActionIndex = 0;
        selectedIndex = 0;
        statusMessage = UiText.STATUS_SAVE_DELETED;
        statusTicks = STATUS_TICKS;
        if (!saveManager.hasManualSaves()) {
            selectingManualSave = false;
        }
    }

    private List<String> currentOptions() {
        if (selectingManualSave) {
            if (selectedManualSave != null) {
                return List.of(
                        UiText.MENU_LOAD_SAVE,
                        UiText.MENU_RENAME_SAVE,
                        UiText.MENU_DELETE_SAVE,
                        UiText.MENU_BACK);
            }
            List<String> options = new ArrayList<>();
            for (SaveSlotMetadata save : saveManager.listManualSaves()) {
                options.add(save.menuLabel());
            }
            options.add(UiText.MENU_BACK);
            return options;
        }
        return List.of(
                UiText.MENU_CONTINUE,
                UiText.MENU_LOAD_SAVE,
                UiText.MENU_NEW_GAME,
                UiText.menuAccountOption(accountService.isLoggedIn()),
                UiText.MENU_CLOSE_APP);
    }

    private String currentFooter() {
        String base = selectingManualSave
                ? selectedManualSave != null ? "ENTER elegir  ESC volver" : "ENTER abrir  ESC volver"
                : UiText.footerForSave(saveManager.hasAnySave());
        if (statusTicks > 0 && statusMessage != null && !statusMessage.isBlank()) {
            return statusMessage;
        }
        return base;
    }

    private void returnToAccess() {
        if (accountService.isLoggedIn()) {
            accountService.logout();
        }
        keyboard.reset();
        sceneManager.setScene(sceneFactory.createLoginScene());
    }

    private boolean consumeMenuUp() {
        return keyboard.consumePressed(KeyEvent.VK_W) || keyboard.consumePressed(KeyEvent.VK_UP);
    }

    private boolean consumeMenuDown() {
        return keyboard.consumePressed(KeyEvent.VK_S) || keyboard.consumePressed(KeyEvent.VK_DOWN);
    }

    private int currentSelectedIndex() {
        return selectedManualSave != null ? saveActionIndex : selectedIndex;
    }

    private String currentSectionTitle() {
        if (!selectingManualSave) {
            return UiText.MENU_MAIN;
        }
        return selectedManualSave != null ? UiText.SECTION_SAVE_ACTIONS : UiText.SECTION_SAVE_SLOTS;
    }
}
