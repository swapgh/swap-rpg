package scene.gameplay;

import app.Camera;
import app.GameConfig;
import app.GameSceneFactory;
import app.KeyboardState;
import asset.AssetManager;
import asset.TileMap;
import audio.AudioBootstrap;
import audio.AudioService;
import component.HealthComponent;
import component.InputComponent;
import component.InventoryComponent;
import component.PlayerComponent;
import component.PositionComponent;
import component.ProgressionComponent;
import component.QuestComponent;
import component.WorldTimeComponent;
import content.bootstrap.AssetBootstrap;
import content.world.WorldSeeder;
import data.DataRegistry;
import ecs.EcsSystem;
import ecs.EcsWorld;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import online.OnlineAccountService;
import online.PlayerProgressSnapshot;
import online.PlayerProgressSnapshotFactory;
import online.SyncOutcome;
import save.SaveManager;
import save.SaveReference;
import scene.gameplay.world.WorldOptionsMenu;
import scene.gameplay.world.WorldPerformanceTracker;
import scene.gameplay.world.WorldSaveController;
import state.GameMode;
import state.Scene;
import state.SceneManager;
import system.combat.CombatSystem;
import system.combat.HealthSystem;
import system.combat.ProjectileSystem;
import system.input.InputSystem;
import system.persistence.SaveLoadSystem;
import system.progression.InteractionSystem;
import system.progression.InventorySystem;
import system.progression.QuestSystem;
import system.progression.ShopSystem;
import system.render.AnimationSystem;
import system.render.RenderSystem;
import system.render.UiOverlaySystem;
import system.world.CameraSystem;
import system.world.DayNightSystem;
import system.world.MovementSystem;
import system.world.RespawnSystem;
import system.world.TimeSystem;
import system.world.WanderSystem;
import ui.hud.HudRenderer;
import ui.render.FogOfWarRenderer;
import ui.runtime.UiState;

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
    private final FogOfWarRenderer fogOfWar;
    private final OnlineAccountService accountService;
    private final GameSceneFactory sceneFactory;
    private final Camera camera = new Camera();
    private final TileMap map;
    private final EcsWorld world;

    private final List<EcsSystem> systems;
    private final ProjectileSystem projectileSystem;
    private final CombatSystem combatSystem;
    private final RenderSystem renderSystem;
    private final SaveLoadSystem saveLoadSystem = new SaveLoadSystem();
    private final ShopSystem shopSystem;
    private final WorldOptionsMenu optionsMenu;
    private final WorldSaveController saveController;
    private final WorldPerformanceTracker performanceTracker;
    private final int screenWidth;
    private final int screenHeight;
    private final int tileSize;
    private final String[] systemPerfNames = {
            "UiOverlay",
            "Input",
            "DayNight",
            "Inventory",
            "Wander",
            "Movement",
            "Projectile",
            "Interaction",
            "Combat",
            "Health",
            "Respawn",
            "Quest",
            "Animation",
            "Camera"
    };
    private double autoSyncCooldown;
    private double loginReminderCooldown;
    private double autoSaveCooldown = GameConfig.AUTO_SAVE_INTERVAL_SECONDS;

    public WorldScene(SceneManager sceneManager, KeyboardState keyboard, AssetManager assets, AudioService audio,
            DataRegistry data, UiState ui, int tileSize, int screenWidth, int screenHeight, Path loadPath,
            Path manualSavePath, Path autoSavePath,
            OnlineAccountService accountService, GameSceneFactory sceneFactory) {
        this(sceneManager, keyboard, assets, audio, data, ui, tileSize, screenWidth, screenHeight, null, null, false,
                accountService, sceneFactory);
    }

    public WorldScene(SceneManager sceneManager, KeyboardState keyboard, AssetManager assets, AudioService audio,
            DataRegistry data, UiState ui, int tileSize, int screenWidth, int screenHeight, SaveManager saveManager,
            SaveReference initialSaveReference, boolean loadFromSave, OnlineAccountService accountService,
            GameSceneFactory sceneFactory) {
        this.sceneManager = sceneManager;
        this.keyboard = keyboard;
        this.assets = assets;
        this.audio = audio;
        this.data = data;
        this.ui = ui;
        this.hud = new HudRenderer(assets, tileSize);
        this.accountService = accountService;
        this.sceneFactory = sceneFactory;
        this.map = WorldSeeder.createMap(assets, tileSize, data);
        this.fogOfWar = new FogOfWarRenderer(map, camera, screenWidth, screenHeight, tileSize);
        this.world = new EcsWorld();
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.tileSize = tileSize;
        this.autoSyncCooldown = AUTO_SYNC_START_DELAY_SECONDS;
        this.optionsMenu = new WorldOptionsMenu(keyboard, sceneManager, sceneFactory, saveManager);
        this.saveController = new WorldSaveController(saveManager, saveLoadSystem, ui);
        this.saveController.initialize(initialSaveReference);
        this.performanceTracker = new WorldPerformanceTracker(systemPerfNames);

        WorldSeeder.seedPlayer(world, tileSize, data);
        WorldSeeder.seedWorldTime(world);
        WorldSeeder.seedWorld(world, tileSize, data);

        this.projectileSystem = new ProjectileSystem(map, audio, ui);
        this.combatSystem = new CombatSystem(ui, audio, tileSize);
        this.renderSystem = new RenderSystem(assets, map, camera, screenWidth, screenHeight);
        this.shopSystem = new ShopSystem(keyboard, ui, data);
        this.systems = List.of(
                new UiOverlaySystem(keyboard, ui),
                new TimeSystem(),
                new DayNightSystem(keyboard, ui, data),
                new InputSystem(keyboard, ui),
                new InventorySystem(keyboard, ui),
                shopSystem,
                new WanderSystem(),
                new MovementSystem(map),
                projectileSystem,
                new InteractionSystem(ui, audio, tileSize, data),
                combatSystem,
                new HealthSystem(ui, data),
                new RespawnSystem(map, data, tileSize),
                new QuestSystem(ui, audio, data),
                new AnimationSystem(assets),
                new CameraSystem(camera, map, screenWidth, screenHeight));
        AudioBootstrap.prewarmWorldEffects(audio);
        saveController.loadIfRequested(world, loadFromSave);
        centerCameraOnPlayer();
    }

    public static WorldScene create(SceneManager sceneManager, KeyboardState keyboard, int tileSize, int screenWidth,
            int screenHeight, Path savePath) {
        AssetManager assets = new AssetManager();
        AssetBootstrap.loadAll(assets, tileSize);
        AudioService audio = AudioBootstrap.createDefault();
        DataRegistry data = DataRegistry.loadDefaults();
        OnlineAccountService accountService = new OnlineAccountService(GameConfig.ACCOUNT_FILE);
        GameSceneFactory sceneFactory = new GameSceneFactory(
                sceneManager,
                keyboard,
                assets,
                audio,
                data,
                accountService,
                tileSize,
                screenWidth,
                screenHeight);
        return sceneFactory.createNewWorldScene();
    }

    @Override
    public void update(double dtSeconds) {
        if (keyboard.consumePressed(KeyEvent.VK_F10) && (ui.mode == GameMode.PLAY || ui.mode == GameMode.OPTIONS)) {
            optionsMenu.toggle(ui);
            return;
        }

        if (ui.mode == GameMode.OPTIONS) {
            optionsMenu.update(ui);
            return;
        }

        long updateStart = System.nanoTime();
        if (ui.mode == GameMode.PLAY) {
            autoSaveCooldown = Math.max(0, autoSaveCooldown - dtSeconds);
        }
        autoSyncCooldown = Math.max(0, autoSyncCooldown - dtSeconds);
        loginReminderCooldown = Math.max(0, loginReminderCooldown - dtSeconds);

        for (int i = 0; i < systems.size(); i++) {
            long systemStart = System.nanoTime();
            systems.get(i).update(world, dtSeconds);
            performanceTracker.recordSystem(i, System.nanoTime() - systemStart);
        }
        performanceTracker.recordSceneUpdate(
                System.nanoTime() - updateStart,
                dtSeconds,
                getClass().getSimpleName(),
                world.entities().size(),
                collectPerformanceExtras());

        if (ui.mode == GameMode.GAME_OVER) {
            keyboard.reset();
            sceneManager.setScene(sceneFactory.createGameOverScene(this));
            return;
        }

        int player = world.entitiesWith(PlayerComponent.class).get(0);
        InputComponent input = world.require(player, InputComponent.class);
        ProgressionComponent progression = world.require(player, ProgressionComponent.class);

        if (ui.mode == GameMode.TITLE) {
            syncProgress(true, false);
            if (accountService.isLoggedIn()) {
                sceneManager.setScene(sceneFactory.createTitleScene());
            } else {
                sceneManager.setScene(sceneFactory.createLoginScene());
            }
            return;
        }

        if (input.interactPressed && ui.mode == GameMode.PLAY) {
            ui.dialogueSpeaker = "";
        }

        if (ui.mode == GameMode.PLAY && keyboard.consumePressed(KeyEvent.VK_F5)) {
            saveController.quickSaveCurrentProgress(world);
        }
        if (ui.mode == GameMode.PLAY && keyboard.consumePressed(KeyEvent.VK_F6)) {
            saveController.saveManualProgress(world);
        }

        if (ui.mode == GameMode.PLAY && autoSaveCooldown <= 0) {
            saveController.saveAutoProgress(world, false);
            autoSaveCooldown = GameConfig.AUTO_SAVE_INTERVAL_SECONDS;
        }

        if (keyboard.consumePressed(KeyEvent.VK_Y)) {
            syncProgress(false, true);
        } else if (progression.dirtySync && accountService.isLoggedIn() && autoSyncCooldown <= 0) {
            syncProgress(false, false);
        } else if (progression.dirtySync && !accountService.isLoggedIn() && loginReminderCooldown <= 0) {
            loginReminderCooldown = 10.0;
        }
    }

    public void prepareForPlay() {
        ui.mode = GameMode.PLAY;
        ui.inventoryVisible = false;
        ui.contextHint = "";
        ui.clearSystemLog();
        ui.combatToast = "";
        ui.combatToastTicks = 0;
        centerCameraOnPlayer();
    }

    private void centerCameraOnPlayer() {
        List<Integer> players = world.entitiesWith(PlayerComponent.class, component.PositionComponent.class);
        if (players.isEmpty()) {
            return;
        }
        int player = players.get(0);
        component.PositionComponent pos = world.require(player, component.PositionComponent.class);
        camera.centerOn(pos.x + tileSize / 2.0, pos.y + tileSize / 2.0, screenWidth, screenHeight);
    }

    @Override
    public void render(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, screenWidth, screenHeight);
        long worldRenderStart = System.nanoTime();
        renderSystem.render(g2, world);
        performanceTracker.recordWorldRender(System.nanoTime() - worldRenderStart);

        int player = world.entitiesWith(PlayerComponent.class).get(0);
        List<Integer> timeEntities = world.entitiesWith(WorldTimeComponent.class);
        WorldTimeComponent worldTime = timeEntities.isEmpty() ? null : world.require(timeEntities.get(0), WorldTimeComponent.class);
        long fogRenderStart = System.nanoTime();
        fogOfWar.render(g2, world, player, worldTime == null || worldTime.isDay());
        performanceTracker.recordFogRender(System.nanoTime() - fogRenderStart);
        long uiRenderStart = System.nanoTime();
        hud.drawWorldHud(g2, ui, screenWidth, screenHeight, world.require(player, HealthComponent.class),
                world.require(player, InventoryComponent.class), world.require(player, QuestComponent.class), worldTime,
                accountService.displayLabel(), accountService.isLoggedIn());

        if (ui.mode == GameMode.DIALOGUE) {
            hud.drawDialogue(g2, ui, screenWidth, screenHeight);
        }
        if (ui.mode == GameMode.INVENTORY) {
            hud.drawInventory(g2, ui, world.require(player, InventoryComponent.class), screenWidth, screenHeight);
        }
        if (ui.mode == GameMode.SHOP) {
            hud.drawShop(g2, ui, world.require(player, InventoryComponent.class), shopSystem.currentShopEntries(world),
                    screenWidth, screenHeight);
        }
        if (ui.mode == GameMode.OPTIONS) {
            hud.drawCompactMenuOverlay(
                    g2,
                    optionsMenu.title(),
                    optionsMenu.entries(),
                    optionsMenu.selectedIndex(),
                    optionsMenu.statusMessage(),
                    screenWidth,
                    screenHeight);
        }
        performanceTracker.recordUiRender(System.nanoTime() - uiRenderStart);
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
            ui.pushToast(outcome.message(), 180);
        }
    }

    public void saveProgress() {
        saveController.saveProgress(world);
    }

    public void closeScene() {
        saveController.closeScene(world);
    }

    @Override
    public List<String> performanceLines() {
        return performanceTracker.performanceLines();
    }

    @Override
    public List<String> performanceOverlayLines() {
        return performanceTracker.performanceOverlayLines(world.entities().size());
    }

    private List<String> collectPerformanceExtras() {
        List<String> lines = new ArrayList<>();
        lines.addAll(projectileSystem.snapshotAndResetPerformance());
        lines.addAll(combatSystem.snapshotAndResetPerformance());
        return lines;
    }
}
