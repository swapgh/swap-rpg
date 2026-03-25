package scene;

import app.Camera;
import app.GameConfig;
import app.KeyboardState;
import asset.AssetManager;
import asset.TileMap;
import audio.AudioBootstrap;
import audio.AudioService;
import component.HealthComponent;
import component.InputComponent;
import component.InventoryComponent;
import component.PlayerComponent;
import component.ProgressionComponent;
import component.QuestComponent;
import content.AssetBootstrap;
import content.WorldSeeder;
import data.DataRegistry;
import ecs.EcsSystem;
import ecs.EcsWorld;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.List;
import online.OnlineAccountService;
import online.PlayerProgressSnapshot;
import online.PlayerProgressSnapshotFactory;
import online.SyncOutcome;
import state.GameMode;
import state.Scene;
import state.SceneManager;
import system.AnimationSystem;
import system.CameraSystem;
import system.CombatSystem;
import system.HealthSystem;
import system.InputSystem;
import system.InteractionSystem;
import system.InventorySystem;
import system.MovementSystem;
import system.ProjectileSystem;
import system.QuestSystem;
import system.RenderSystem;
import system.SaveLoadSystem;
import system.WanderSystem;
import ui.HudRenderer;
import ui.UiState;

public final class WorldScene implements Scene {
    private static final double AUTO_SYNC_START_DELAY_SECONDS = 3.0;
    private static final double AUTO_SYNC_SUCCESS_COOLDOWN_SECONDS = 10.0;
    private static final double AUTO_SYNC_FAILURE_COOLDOWN_SECONDS = 30.0;

    private final SceneManager sceneManager;
    private final KeyboardState keyboard;
    private final AssetManager assets;
    private final AudioService audio;
    private final DataRegistry data;
    private final UiState ui;
    private final HudRenderer hud;
    private final OnlineAccountService accountService;
    private final Camera camera = new Camera();
    private final TileMap map;
    private final EcsWorld world;

    private final List<EcsSystem> systems;
    private final RenderSystem renderSystem;
    private final SaveLoadSystem saveLoadSystem = new SaveLoadSystem();
    private final Path savePath;
    private final int screenWidth;
    private final int screenHeight;
    private final int tileSize;
    private double autoSyncCooldown;
    private double loginReminderCooldown;

    public WorldScene(SceneManager sceneManager, KeyboardState keyboard, AssetManager assets, AudioService audio,
            DataRegistry data, UiState ui, int tileSize, int screenWidth, int screenHeight, Path savePath,
            OnlineAccountService accountService) {
        this.sceneManager = sceneManager;
        this.keyboard = keyboard;
        this.assets = assets;
        this.audio = audio;
        this.data = data;
        this.ui = ui;
        this.hud = new HudRenderer(assets, tileSize);
        this.accountService = accountService;
        this.map = WorldSeeder.createMap(assets, tileSize);
        this.world = new EcsWorld();
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.tileSize = tileSize;
        this.autoSyncCooldown = AUTO_SYNC_START_DELAY_SECONDS;

        WorldSeeder.seedPlayer(world, tileSize, data);
        WorldSeeder.seedWorld(world, tileSize, data);

        this.renderSystem = new RenderSystem(assets, map, camera, screenWidth, screenHeight);
        this.systems = List.of(
                new InputSystem(keyboard, ui),
                new InventorySystem(ui),
                new WanderSystem(),
                new MovementSystem(map),
                new ProjectileSystem(map, audio, ui),
                new InteractionSystem(ui, audio, tileSize),
                new CombatSystem(ui, audio, tileSize),
                new HealthSystem(ui),
                new QuestSystem(ui, audio),
                new AnimationSystem(assets),
                new CameraSystem(camera, map, screenWidth, screenHeight));
        this.savePath = savePath;
        saveLoadSystem.load(world, savePath);
    }

    public static WorldScene create(SceneManager sceneManager, KeyboardState keyboard, int tileSize, int screenWidth,
            int screenHeight, Path savePath) {
        AssetManager assets = new AssetManager();
        AssetBootstrap.loadAll(assets, tileSize);
        AudioService audio = AudioBootstrap.createDefault();
        DataRegistry data = DataRegistry.loadDefaults();
        OnlineAccountService accountService = new OnlineAccountService(GameConfig.ACCOUNT_FILE);
        return new WorldScene(sceneManager, keyboard, assets, audio, data, new UiState(), tileSize, screenWidth,
                screenHeight, savePath, accountService);
    }

    @Override
    public void update(double dtSeconds) {
        autoSyncCooldown = Math.max(0, autoSyncCooldown - dtSeconds);
        loginReminderCooldown = Math.max(0, loginReminderCooldown - dtSeconds);

        if (ui.toastTicks > 0) {
            ui.toastTicks--;
        }

        for (EcsSystem system : systems) {
            system.update(world, dtSeconds);
        }

        int player = world.entitiesWith(PlayerComponent.class).get(0);
        InputComponent input = world.require(player, InputComponent.class);
        ProgressionComponent progression = world.require(player, ProgressionComponent.class);

        if (ui.mode == GameMode.TITLE) {
            saveLoadSystem.save(world, savePath);
            syncProgress(true, false);
            UiState titleUi = new UiState();
            titleUi.subtitleMessage = "Has caido. Pulsa ENTER para empezar otra vez";
            sceneManager.setScene(new TitleScene(
                    keyboard,
                    sceneManager,
                    new WorldScene(sceneManager, keyboard, assets, audio, data, new UiState(), tileSize, screenWidth,
                            screenHeight, savePath, accountService),
                    new HudRenderer(assets, tileSize),
                    titleUi,
                    accountService,
                    screenWidth,
                    screenHeight));
            return;
        }

        if (input.interactPressed && ui.mode == GameMode.PLAY) {
            ui.dialogueSpeaker = "";
        }

        if (keyboard.consumePressed(KeyEvent.VK_Y)) {
            syncProgress(false, true);
        } else if (progression.dirtySync && accountService.isLoggedIn() && autoSyncCooldown <= 0) {
            syncProgress(false, false);
        } else if (progression.dirtySync && !accountService.isLoggedIn() && loginReminderCooldown <= 0) {
            ui.toast = "Hay progreso sin subir. Pulsa L en el menu para iniciar sesion en Swap Web.";
            ui.toastTicks = 180;
            loginReminderCooldown = 10.0;
        }
    }

    @Override
    public void render(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, screenWidth, screenHeight);
        renderSystem.render(g2, world);

        int player = world.entitiesWith(PlayerComponent.class).get(0);
        hud.drawWorldHud(g2, ui, screenWidth, world.require(player, HealthComponent.class),
                world.require(player, InventoryComponent.class), world.require(player, QuestComponent.class),
                accountService.displayLabel(), accountService.isLoggedIn());

        if (ui.mode == GameMode.DIALOGUE) {
            hud.drawDialogue(g2, ui, screenWidth, screenHeight);
        }
        if (ui.mode == GameMode.INVENTORY) {
            hud.drawInventory(g2, world.require(player, InventoryComponent.class), screenWidth, screenHeight);
        }
    }

    private void syncProgress(boolean silent, boolean manual) {
        PlayerProgressSnapshot snapshot = PlayerProgressSnapshotFactory.fromWorld(world);
        SyncOutcome outcome = accountService.sync(snapshot);
        if (outcome.ok()) {
            int player = world.entitiesWith(PlayerComponent.class).get(0);
            world.require(player, ProgressionComponent.class).dirtySync = false;
            autoSyncCooldown = AUTO_SYNC_SUCCESS_COOLDOWN_SECONDS;
        } else if (!manual) {
            autoSyncCooldown = AUTO_SYNC_FAILURE_COOLDOWN_SECONDS;
        }
        if (!silent || !outcome.ok()) {
            ui.toast = outcome.message();
            ui.toastTicks = 180;
        }
    }
}
