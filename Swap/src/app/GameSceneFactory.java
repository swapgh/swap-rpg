package app;

import asset.AssetManager;
import audio.AudioService;
import data.DataRegistry;
import online.OnlineAccountService;
import scene.LoginScene;
import scene.TitleScene;
import scene.WorldScene;
import state.SceneManager;
import ui.HudRenderer;
import ui.UiState;

public final class GameSceneFactory {
    private final SceneManager sceneManager;
    private final KeyboardState keyboard;
    private final AssetManager assets;
    private final AudioService audio;
    private final DataRegistry data;
    private final OnlineAccountService accountService;
    private final HudRenderer hud;
    private final int tileSize;
    private final int screenWidth;
    private final int screenHeight;

    public GameSceneFactory(SceneManager sceneManager, KeyboardState keyboard, AssetManager assets, AudioService audio,
            DataRegistry data, OnlineAccountService accountService, int tileSize, int screenWidth, int screenHeight) {
        this.sceneManager = sceneManager;
        this.keyboard = keyboard;
        this.assets = assets;
        this.audio = audio;
        this.data = data;
        this.accountService = accountService;
        this.tileSize = tileSize;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.hud = new HudRenderer(assets, tileSize);
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
        return new TitleScene(
                keyboard,
                sceneManager,
                this,
                hud,
                new UiState(),
                accountService,
                screenWidth,
                screenHeight);
    }

    public WorldScene createWorldScene() {
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
                GameConfig.SAVE_FILE,
                accountService,
                this);
    }
}
