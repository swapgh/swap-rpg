package scene.menu;

import scene.gameplay.WorldScene;

import app.GameSceneFactory;
import app.KeyboardState;
import app.SaveDialogs;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import save.SaveManager;
import save.SaveSlotMetadata;
import state.Scene;
import state.SceneManager;
import ui.hud.HudRenderer;
import ui.text.UiText;

public final class GameOverScene implements Scene {
    private static final List<String> OPTIONS = List.of(
            UiText.MENU_CONTINUE,
            UiText.MENU_LOAD_SAVE,
            UiText.MENU_NEW_GAME,
            UiText.MENU_MAIN_MENU);

    private final KeyboardState keyboard;
    private final SceneManager sceneManager;
    private final GameSceneFactory sceneFactory;
    private final HudRenderer hud;
    private final SaveManager saveManager;
    private final WorldScene backgroundScene;
    private final int screenWidth;
    private final int screenHeight;
    private int selectedIndex;
    private String statusMessage = "";
    private boolean selectingManualSave;
    private SaveSlotMetadata selectedManualSave;
    private int saveActionIndex;

    public GameOverScene(KeyboardState keyboard, SceneManager sceneManager, GameSceneFactory sceneFactory, HudRenderer hud,
            SaveManager saveManager, WorldScene backgroundScene, int screenWidth, int screenHeight) {
        this.keyboard = keyboard;
        this.sceneManager = sceneManager;
        this.sceneFactory = sceneFactory;
        this.hud = hud;
        this.saveManager = saveManager;
        this.backgroundScene = backgroundScene;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    @Override
    public void update(double dtSeconds) {
        if (keyboard.consumePressed(KeyEvent.VK_W) || keyboard.consumePressed(KeyEvent.VK_UP)) {
            if (selectedManualSave != null) {
                saveActionIndex = (saveActionIndex - 1 + currentOptions().size()) % currentOptions().size();
            } else {
                selectedIndex = (selectedIndex - 1 + currentOptions().size()) % currentOptions().size();
            }
        } else if (keyboard.consumePressed(KeyEvent.VK_S) || keyboard.consumePressed(KeyEvent.VK_DOWN)) {
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
        }
    }

    @Override
    public void render(Graphics2D g2) {
        backgroundScene.render(g2);
        drawBackdrop(g2);
        hud.drawCompactGameOverOverlay(
                g2,
                currentSectionTitle(),
                currentOptions(),
                currentSelectedIndex(),
                statusMessage,
                screenWidth,
                screenHeight);
    }

    private void drawBackdrop(Graphics2D g2) {
        g2.setColor(new Color(6, 8, 12, 55));
        g2.fillRect(0, 0, screenWidth, screenHeight);
    }

    private void activateSelection() {
        if (selectingManualSave) {
            if (selectedManualSave != null) {
                activateManualSaveAction();
            } else {
                activateManualSaveSelection();
            }
            return;
        }
        switch (selectedIndex) {
        case 0 -> continueFromAutoSave();
        case 1 -> openManualLoadMenu();
        case 2 -> startNewGame();
        case 3 -> sceneManager.setScene(sceneFactory.createTitleScene());
        default -> {
        }
        }
    }

    private void continueFromAutoSave() {
        var autosave = saveManager.latestAutosave();
        if (autosave.isEmpty()) {
            statusMessage = UiText.STATUS_AUTOSAVE_MISSING;
            return;
        }
        saveManager.markLastUsed(autosave.get().reference());
        WorldScene worldScene = sceneFactory.createWorldScene(autosave.get().reference());
        worldScene.prepareForPlay();
        sceneManager.setScene(worldScene);
    }

    private void openManualLoadMenu() {
        if (!saveManager.hasManualSaves()) {
            statusMessage = UiText.STATUS_NO_MANUAL_SAVE;
            return;
        }
        selectingManualSave = true;
        selectedManualSave = null;
        selectedIndex = 0;
    }

    private void startNewGame() {
        saveManager.deleteAutosaves();
        WorldScene worldScene = sceneFactory.createNewWorldScene();
        worldScene.prepareForPlay();
        sceneManager.setScene(worldScene);
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
            WorldScene worldScene = sceneFactory.createWorldScene(selectedManualSave.reference());
            worldScene.prepareForPlay();
            sceneManager.setScene(worldScene);
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
        return OPTIONS;
    }

    private int currentSelectedIndex() {
        return selectedManualSave != null ? saveActionIndex : selectedIndex;
    }

    private String currentSectionTitle() {
        if (!selectingManualSave) {
            return UiText.GAME_OVER_TITLE;
        }
        return selectedManualSave != null ? UiText.SECTION_SAVE_ACTIONS : UiText.SECTION_SAVE_SLOTS;
    }
}
