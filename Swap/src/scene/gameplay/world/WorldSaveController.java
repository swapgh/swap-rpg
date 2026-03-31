package scene.gameplay.world;

import app.SaveDialogs;
import component.HealthComponent;
import component.NameComponent;
import component.PlayerComponent;
import component.WorldTimeComponent;
import ecs.EcsWorld;
import save.SaveManager;
import save.SaveReference;
import save.SaveSlotMetadata;
import system.persistence.SaveLoadSystem;
import ui.runtime.UiState;
import ui.text.UiText;

public final class WorldSaveController {
    private final SaveManager saveManager;
    private final SaveLoadSystem saveLoadSystem;
    private final UiState ui;

    private SaveReference activeSaveReference = SaveReference.autosave();
    private String activeManualSaveName;
    private boolean exitSceneRequested;

    public WorldSaveController(SaveManager saveManager, SaveLoadSystem saveLoadSystem, UiState ui) {
        this.saveManager = saveManager;
        this.saveLoadSystem = saveLoadSystem;
        this.ui = ui;
    }

    public void initialize(SaveReference initialSaveReference) {
        this.activeSaveReference = initialSaveReference == null ? SaveReference.autosave() : initialSaveReference;
        this.activeManualSaveName = null;
        this.exitSceneRequested = false;
    }

    public void loadIfRequested(EcsWorld world, boolean loadFromSave) {
        if (!loadFromSave || saveManager == null || !saveManager.exists(activeSaveReference)) {
            return;
        }
        saveLoadSystem.load(world, saveManager.savePath(activeSaveReference));
        saveManager.markLastUsed(activeSaveReference);
        activeManualSaveName = saveManager.findMetadata(activeSaveReference)
                .map(SaveSlotMetadata::displayName)
                .orElse(null);
    }

    public SaveReference activeSaveReference() {
        return activeSaveReference;
    }

    public String activeManualSaveName() {
        return activeManualSaveName;
    }

    public void saveProgress(EcsWorld world) {
        if (exitSceneRequested || !canSaveLivingPlayer(world)) {
            return;
        }
        saveAutoProgress(world, false);
    }

    public void closeScene(EcsWorld world) {
        if (exitSceneRequested) {
            return;
        }
        exitSceneRequested = true;
        saveProgress(world);
    }

    public void quickSaveCurrentProgress(EcsWorld world) {
        if (!canSaveLivingPlayer(world) || saveManager == null) {
            return;
        }
        if (activeSaveReference != null && activeSaveReference.isManual()) {
            SaveSlotMetadata metadata = saveManager.saveManual(
                    world,
                    saveLoadSystem,
                    activeSaveReference.slotId(),
                    activeManualSaveName);
            activeSaveReference = metadata.reference();
            activeManualSaveName = metadata.displayName();
            ui.pushToast(UiText.quickSaveCreated(metadata.displayName()), 180);
            return;
        }
        saveManager.saveAutosave(world, saveLoadSystem);
        activeSaveReference = SaveReference.autosave();
        activeManualSaveName = null;
        ui.pushToast(UiText.quickSaveCreated("Autosave"), 180);
    }

    public void saveManualProgress(EcsWorld world) {
        if (!canSaveLivingPlayer(world) || saveManager == null) {
            return;
        }
        String saveName = SaveDialogs.showManualSaveName(defaultManualSaveName(world));
        if (saveName == null) {
            ui.pushToast(UiText.STATUS_MANUAL_SAVE_CANCELLED, 120);
            return;
        }
        SaveSlotMetadata metadata = saveManager.saveManual(world, saveLoadSystem, null, saveName);
        activeSaveReference = metadata.reference();
        activeManualSaveName = metadata.displayName();
        ui.pushToast(UiText.saveCreated(metadata.displayName()), 180);
    }

    public void saveAutoProgress(EcsWorld world, boolean notify) {
        if (!canSaveLivingPlayer(world) || saveManager == null) {
            return;
        }
        saveManager.saveAutosave(world, saveLoadSystem);
        activeSaveReference = SaveReference.autosave();
        activeManualSaveName = null;
        if (notify) {
            ui.pushToast(UiText.STATUS_AUTOSAVED, 120);
        }
    }

    private boolean canSaveLivingPlayer(EcsWorld world) {
        var players = world.entitiesWith(PlayerComponent.class, HealthComponent.class);
        if (players.isEmpty()) {
            return false;
        }
        int player = players.get(0);
        return world.require(player, HealthComponent.class).current > 0;
    }

    private String defaultManualSaveName(EcsWorld world) {
        var players = world.entitiesWith(PlayerComponent.class, NameComponent.class);
        String playerName = players.isEmpty() ? "Partida" : world.require(players.get(0), NameComponent.class).value;
        var timeEntities = world.entitiesWith(WorldTimeComponent.class);
        if (timeEntities.isEmpty()) {
            return playerName;
        }
        WorldTimeComponent time = world.require(timeEntities.get(0), WorldTimeComponent.class);
        return "%s D%d %02d-%02d".formatted(playerName, time.dayNumber(), time.hour(), time.minute());
    }
}
