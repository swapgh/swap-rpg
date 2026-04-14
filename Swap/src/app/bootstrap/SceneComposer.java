package app.bootstrap;

import asset.AssetManager;
import audio.AudioService;
import data.DataRegistry;
import online.auth.OnlineAccountService;
import save.SaveManager;
import save.SaveReference;
import app.input.KeyboardState;
import scene.gameplay.WorldScene;
import scene.menu.GameOverScene;
import scene.menu.LoginScene;
import scene.menu.TitleScene;
import state.SceneManager;
import ui.hud.HudRenderer;
import ui.state.UiState;

public final class SceneComposer {
    private final SceneManager sceneManager;
    private final KeyboardState keyboard;
    private final AssetManager assets;
    private final AudioService audio;
    private final DataRegistry data;
    private final OnlineAccountService accountService;
    private final SaveManager saveManager;
    private final HudRenderer hud;
    private final int tileSize;
    private final int screenWidth;
    private final int screenHeight;

    public SceneComposer(SceneManager sceneManager, KeyboardState keyboard, AssetManager assets, AudioService audio,
            DataRegistry data, OnlineAccountService accountService, SaveManager saveManager, int tileSize, int screenWidth,
            int screenHeight) {
        this.sceneManager = sceneManager;
        this.keyboard = keyboard;
        this.assets = assets;
        this.audio = audio;
        this.data = data;
        this.accountService = accountService;
        this.saveManager = saveManager;
        this.tileSize = tileSize;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.hud = new HudRenderer(assets, data, tileSize);
    }

    public LoginScene createLoginScene() {
        return new LoginScene(
                keyboard,
                sceneManager,
                this,
                hud,
                new UiState(),
                accountService,
                screenWidth,
                screenHeight);
    }

    public TitleScene createTitleScene() {
        return createTitleScene("");
    }

    public TitleScene createTitleScene(String initialStatusMessage) {
        return new TitleScene(
                keyboard,
                sceneManager,
                this,
                hud,
                new UiState(),
                accountService,
                saveManager,
                data,
                initialStatusMessage,
                screenWidth,
                screenHeight);
    }

    public WorldScene createNewWorldScene() {
        return createNewWorldScene("warrior");
    }

    public WorldScene createNewWorldScene(String classId) {
        return new WorldScene(
                sceneManager,
                keyboard,
                assets,
                audio,
                data,
                new UiState(),
                tileSize,
                screenWidth,
                screenHeight,
                saveManager,
                SaveReference.autosave(),
                false,
                accountService,
                this,
                classId);
    }

    public WorldScene createWorldScene(SaveReference saveReference) {
        return new WorldScene(
                sceneManager,
                keyboard,
                assets,
                audio,
                data,
                new UiState(),
                tileSize,
                screenWidth,
                screenHeight,
                saveManager,
                saveReference,
                true,
                accountService,
                this,
                null);
    }

    public GameOverScene createGameOverScene(WorldScene backgroundScene) {
        return new GameOverScene(
                keyboard,
                sceneManager,
                this,
                hud,
                saveManager,
                backgroundScene,
                screenWidth,
                screenHeight);
    }
}
