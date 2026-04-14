package scene.gameplay.world;

import app.GameSceneFactory;
import app.KeyboardState;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import save.SaveManager;
import save.SaveSlotMetadata;
import state.GameMode;
import state.SceneManager;
import ui.runtime.UiState;
import ui.text.UiText;

public final class WorldOptionsMenu {
    private final KeyboardState keyboard;
    private final SceneManager sceneManager;
    private final GameSceneFactory sceneFactory;
    private final SaveManager saveManager;

    private int selectedIndex;
    private String statusMessage = "";
    private boolean selectingManualSave;
    private boolean showingKeybinds;

    public WorldOptionsMenu(KeyboardState keyboard, SceneManager sceneManager, GameSceneFactory sceneFactory,
            SaveManager saveManager) {
        this.keyboard = keyboard;
        this.sceneManager = sceneManager;
        this.sceneFactory = sceneFactory;
        this.saveManager = saveManager;
    }

    public void toggle(UiState ui) {
        if (ui.mode == GameMode.OPTIONS) {
            close(ui);
            return;
        }
        if (ui.mode == GameMode.PLAY) {
            ui.mode = GameMode.OPTIONS;
            resetState();
        }
    }

    public void update(UiState ui) {
        List<String> options = entries();
        if (keyboard.consumePressed(KeyEvent.VK_W) || keyboard.consumePressed(KeyEvent.VK_UP)) {
            selectedIndex = (selectedIndex - 1 + options.size()) % options.size();
        } else if (keyboard.consumePressed(KeyEvent.VK_S) || keyboard.consumePressed(KeyEvent.VK_DOWN)) {
            selectedIndex = (selectedIndex + 1) % options.size();
        }

        if (selectingManualSave && (keyboard.consumePressed(KeyEvent.VK_ESCAPE) || keyboard.consumePressed(KeyEvent.VK_BACK_SPACE))) {
            selectingManualSave = false;
            selectedIndex = 0;
            return;
        }
        if (showingKeybinds && (keyboard.consumePressed(KeyEvent.VK_ESCAPE) || keyboard.consumePressed(KeyEvent.VK_BACK_SPACE))) {
            showingKeybinds = false;
            selectedIndex = 0;
            return;
        }

        if (keyboard.consumePressed(KeyEvent.VK_ESCAPE) || keyboard.consumePressed(KeyEvent.VK_BACK_SPACE)) {
            close(ui);
            return;
        }

        if (keyboard.consumePressed(KeyEvent.VK_ENTER) || keyboard.consumePressed(KeyEvent.VK_E)) {
            confirm(ui);
        }
    }

    public String title() {
        if (selectingManualSave) {
            return UiText.SECTION_SAVE_SLOTS;
        }
        if (showingKeybinds) {
            return UiText.SECTION_KEYBINDS;
        }
        return UiText.MENU_OPTIONS;
    }

    public List<String> entries() {
        if (showingKeybinds) {
            return UiText.keybindEntries();
        }
        if (selectingManualSave) {
            List<String> options = new ArrayList<>();
            if (saveManager != null) {
                for (SaveSlotMetadata save : saveManager.listManualSaves()) {
                    options.add(save.menuLabel());
                }
            }
            options.add(UiText.MENU_BACK);
            return options;
        }
        return List.of(
                UiText.MENU_CONTINUE,
                "World Tier: WT" + uiCurrentWorldTier,
                UiText.MENU_LOAD_SAVE,
                UiText.MENU_KEYBINDS,
                UiText.MENU_MAIN_MENU);
    }

    public int selectedIndex() {
        return selectedIndex;
    }

    public String statusMessage() {
        return statusMessage;
    }

    private int uiCurrentWorldTier = 1;

    public void syncWorldTier(int tier) {
        uiCurrentWorldTier = Math.max(1, Math.min(5, tier));
    }

    private void confirm(UiState ui) {
        if (selectingManualSave) {
            confirmManualSaveSelection();
            return;
        }
        if (showingKeybinds) {
            showingKeybinds = false;
            selectedIndex = 0;
            return;
        }

        switch (selectedIndex) {
        case 0 -> close(ui);
        case 1 -> {
            ui.requestedWorldTier = uiCurrentWorldTier >= 5 ? 1 : uiCurrentWorldTier + 1;
            uiCurrentWorldTier = ui.requestedWorldTier;
            statusMessage = "World Tier cambiado a WT" + uiCurrentWorldTier;
        }
        case 2 -> {
            if (saveManager == null || !saveManager.hasManualSaves()) {
                statusMessage = UiText.STATUS_NO_MANUAL_SAVE;
            } else {
                selectingManualSave = true;
                selectedIndex = 0;
            }
        }
        case 3 -> {
            showingKeybinds = true;
            selectedIndex = 0;
        }
        case 4 -> sceneManager.setScene(sceneFactory.createTitleScene());
        default -> {
        }
        }
    }

    private void confirmManualSaveSelection() {
        if (saveManager == null) {
            statusMessage = UiText.STATUS_NO_MANUAL_SAVE;
            return;
        }
        List<SaveSlotMetadata> saves = saveManager.listManualSaves();
        if (selectedIndex >= saves.size()) {
            selectingManualSave = false;
            selectedIndex = 0;
            return;
        }
        SaveSlotMetadata selected = saves.get(selectedIndex);
        saveManager.markLastUsed(selected.reference());
        var worldScene = sceneFactory.createWorldScene(selected.reference());
        worldScene.prepareForPlay();
        sceneManager.setScene(worldScene);
    }

    private void close(UiState ui) {
        ui.mode = GameMode.PLAY;
        resetState();
    }

    private void resetState() {
        selectingManualSave = false;
        showingKeybinds = false;
        selectedIndex = 0;
        statusMessage = "";
    }
}
