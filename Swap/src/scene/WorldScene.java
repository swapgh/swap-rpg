package scene;

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
import content.AssetBootstrap;
import content.WorldSeeder;
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
import state.GameMode;
import state.Scene;
import state.SceneManager;
import system.AnimationSystem;
import system.CameraSystem;
import system.CombatSystem;
import system.DayNightSystem;
import system.HealthSystem;
import system.InputSystem;
import system.InteractionSystem;
import system.InventorySystem;
import system.MovementSystem;
import system.ProjectileSystem;
import system.QuestSystem;
import system.RenderSystem;
import system.RespawnSystem;
import system.SaveLoadSystem;
import system.TimeSystem;
import system.UiOverlaySystem;
import system.WanderSystem;
import ui.FogOfWarRenderer;
import ui.HudRenderer;
import ui.UiState;
import ui.UiText;

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
    private final Path loadPath;
    private final Path manualSavePath;
    private final Path autoSavePath;
    private final int screenWidth;
    private final int screenHeight;
    private final int tileSize;
    private final Object perfLock = new Object();
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
    private final long[] systemPerfTotals = new long[systemPerfNames.length];
    private final long[] systemPerfMax = new long[systemPerfNames.length];
    private long sceneUpdateTotalNanos;
    private long sceneUpdateMaxNanos;
    private long renderWorldTotalNanos;
    private long renderWorldMaxNanos;
    private long renderFogTotalNanos;
    private long renderFogMaxNanos;
    private long renderUiTotalNanos;
    private long renderUiMaxNanos;
    private int perfSamples;
    private double perfSampleSeconds;
    private volatile List<String> performanceLines = List.of();
    private double autoSyncCooldown;
    private double loginReminderCooldown;
    private double autoSaveCooldown = GameConfig.AUTO_SAVE_INTERVAL_SECONDS;
    private boolean exitSceneRequested;

    public WorldScene(SceneManager sceneManager, KeyboardState keyboard, AssetManager assets, AudioService audio,
            DataRegistry data, UiState ui, int tileSize, int screenWidth, int screenHeight, Path loadPath,
            Path manualSavePath, Path autoSavePath,
            OnlineAccountService accountService, GameSceneFactory sceneFactory) {
        this.sceneManager = sceneManager;
        this.keyboard = keyboard;
        this.assets = assets;
        this.audio = audio;
        this.data = data;
        this.ui = ui;
        this.hud = new HudRenderer(assets, tileSize);
        this.accountService = accountService;
        this.sceneFactory = sceneFactory;
        this.map = WorldSeeder.createMap(assets, tileSize);
        this.fogOfWar = new FogOfWarRenderer(map, camera, screenWidth, screenHeight, tileSize);
        this.world = new EcsWorld();
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.tileSize = tileSize;
        this.autoSyncCooldown = AUTO_SYNC_START_DELAY_SECONDS;
        this.loadPath = loadPath;
        this.manualSavePath = manualSavePath;
        this.autoSavePath = autoSavePath;

        WorldSeeder.seedPlayer(world, tileSize, data);
        WorldSeeder.seedWorldTime(world);
        WorldSeeder.seedWorld(world, tileSize, data);

        this.projectileSystem = new ProjectileSystem(map, audio, ui);
        this.combatSystem = new CombatSystem(ui, audio, tileSize);
        this.renderSystem = new RenderSystem(assets, map, camera, screenWidth, screenHeight);
        this.systems = List.of(
                new UiOverlaySystem(keyboard, ui),
                new TimeSystem(),
                new DayNightSystem(keyboard, ui, data),
                new InputSystem(keyboard, ui),
                new InventorySystem(keyboard, ui),
                new WanderSystem(),
                new MovementSystem(map),
                projectileSystem,
                new InteractionSystem(ui, audio, tileSize),
                combatSystem,
                new HealthSystem(ui),
                new RespawnSystem(map, data, tileSize),
                new QuestSystem(ui, audio),
                new AnimationSystem(assets),
                new CameraSystem(camera, map, screenWidth, screenHeight));
        AudioBootstrap.prewarmWorldEffects(audio);
        saveLoadSystem.load(world, loadPath);
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
        return new WorldScene(sceneManager, keyboard, assets, audio, data, new UiState(), tileSize, screenWidth,
                screenHeight, savePath, GameConfig.MANUAL_SAVE_FILE, GameConfig.AUTO_SAVE_FILE, accountService, sceneFactory);
    }

    @Override
    public void update(double dtSeconds) {
        long updateStart = System.nanoTime();
        if (ui.mode == GameMode.PLAY) {
            autoSaveCooldown = Math.max(0, autoSaveCooldown - dtSeconds);
        }
        autoSyncCooldown = Math.max(0, autoSyncCooldown - dtSeconds);
        loginReminderCooldown = Math.max(0, loginReminderCooldown - dtSeconds);

        for (int i = 0; i < systems.size(); i++) {
            long systemStart = System.nanoTime();
            systems.get(i).update(world, dtSeconds);
            recordSystemPerf(i, System.nanoTime() - systemStart);
        }
        recordSceneUpdate(System.nanoTime() - updateStart, dtSeconds);

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
            saveManualProgress();
        }

        if (ui.mode == GameMode.PLAY && autoSaveCooldown <= 0) {
            saveAutoProgress(false);
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
        recordWorldRender(System.nanoTime() - worldRenderStart);

        int player = world.entitiesWith(PlayerComponent.class).get(0);
        List<Integer> timeEntities = world.entitiesWith(WorldTimeComponent.class);
        WorldTimeComponent worldTime = timeEntities.isEmpty() ? null : world.require(timeEntities.get(0), WorldTimeComponent.class);
        long fogRenderStart = System.nanoTime();
        fogOfWar.render(g2, world, player, worldTime == null || worldTime.isDay());
        recordFogRender(System.nanoTime() - fogRenderStart);
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
        recordUiRender(System.nanoTime() - uiRenderStart);
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
        if (exitSceneRequested) {
            return;
        }
        List<Integer> players = world.entitiesWith(PlayerComponent.class, HealthComponent.class);
        if (players.isEmpty()) {
            return;
        }
        int player = players.get(0);
        HealthComponent health = world.require(player, HealthComponent.class);
        if (health.current <= 0) {
            return;
        }
        saveLoadSystem.save(world, autoSavePath);
    }

    public void closeScene() {
        if (exitSceneRequested) {
            return;
        }
        exitSceneRequested = true;
        saveProgress();
    }

    private void saveManualProgress() {
        if (!canSaveLivingPlayer()) {
            return;
        }
        saveLoadSystem.save(world, manualSavePath);
        ui.pushToast(UiText.STATUS_MANUAL_SAVED, 180);
    }

    private void saveAutoProgress(boolean notify) {
        if (!canSaveLivingPlayer()) {
            return;
        }
        saveLoadSystem.save(world, autoSavePath);
        if (notify) {
            ui.pushToast(UiText.STATUS_AUTOSAVED, 120);
        }
    }

    private boolean canSaveLivingPlayer() {
        List<Integer> players = world.entitiesWith(PlayerComponent.class, HealthComponent.class);
        if (players.isEmpty()) {
            return false;
        }
        int player = players.get(0);
        return world.require(player, HealthComponent.class).current > 0;
    }

    @Override
    public List<String> performanceLines() {
        return performanceLines;
    }

    @Override
    public List<String> performanceOverlayLines() {
        List<String> lines = new ArrayList<>();
        lines.add(String.format("Scene: world  Entities: %d", world.entities().size()));
        lines.add(String.format("Render split  fog %.2fms  world %.2fms  ui %.2fms",
                averageMillis(renderFogTotalNanos, perfSamples),
                averageMillis(renderWorldTotalNanos, perfSamples),
                averageMillis(renderUiTotalNanos, perfSamples)));
        int[] hottest = hottestSystems(2);
        if (hottest[0] >= 0) {
            lines.add(String.format("Hot system: %s %.2fms avg",
                    systemPerfNames[hottest[0]],
                    averageMillis(systemPerfTotals[hottest[0]], perfSamples)));
        }
        if (hottest.length > 1 && hottest[1] >= 0) {
            lines.add(String.format("Next: %s %.2fms avg",
                    systemPerfNames[hottest[1]],
                    averageMillis(systemPerfTotals[hottest[1]], perfSamples)));
        }
        lines.add("Use console for detailed profiler lines");
        return lines;
    }

    private void recordSystemPerf(int systemIndex, long nanos) {
        synchronized (perfLock) {
            if (systemIndex >= 0 && systemIndex < systemPerfTotals.length) {
                systemPerfTotals[systemIndex] += nanos;
                systemPerfMax[systemIndex] = Math.max(systemPerfMax[systemIndex], nanos);
            }
        }
    }

    private void recordSceneUpdate(long nanos, double dtSeconds) {
        synchronized (perfLock) {
            perfSamples++;
            perfSampleSeconds += dtSeconds;
            sceneUpdateTotalNanos += nanos;
            sceneUpdateMaxNanos = Math.max(sceneUpdateMaxNanos, nanos);
            if (perfSampleSeconds >= 1.0) {
                refreshPerformanceSnapshot();
            }
        }
    }

    private void recordWorldRender(long nanos) {
        synchronized (perfLock) {
            renderWorldTotalNanos += nanos;
            renderWorldMaxNanos = Math.max(renderWorldMaxNanos, nanos);
        }
    }

    private void recordFogRender(long nanos) {
        synchronized (perfLock) {
            renderFogTotalNanos += nanos;
            renderFogMaxNanos = Math.max(renderFogMaxNanos, nanos);
        }
    }

    private void recordUiRender(long nanos) {
        synchronized (perfLock) {
            renderUiTotalNanos += nanos;
            renderUiMaxNanos = Math.max(renderUiMaxNanos, nanos);
        }
    }

    private void refreshPerformanceSnapshot() {
        List<String> lines = new ArrayList<>();
        int sampleCount = Math.max(1, perfSamples);
        lines.add(String.format("Scene %s  ent %d  upd %.2fms avg %.2fms max",
                getClass().getSimpleName(),
                world.entities().size(),
                nanosToMillis(sceneUpdateTotalNanos / (double) sampleCount),
                nanosToMillis(sceneUpdateMaxNanos)));
        lines.add(String.format("Render  world %.2fms  fog %.2fms  ui %.2fms",
                nanosToMillis(renderWorldTotalNanos / (double) sampleCount),
                nanosToMillis(renderFogTotalNanos / (double) sampleCount),
                nanosToMillis(renderUiTotalNanos / (double) sampleCount)));

        int[] hottest = hottestSystems(3);
        for (int index : hottest) {
            if (index < 0) {
                continue;
            }
            lines.add(String.format("%s  %.2fms avg  %.2fms max",
                    systemPerfNames[index],
                    nanosToMillis(systemPerfTotals[index] / (double) sampleCount),
                    nanosToMillis(systemPerfMax[index])));
        }
        lines.addAll(projectileSystem.snapshotAndResetPerformance());
        lines.addAll(combatSystem.snapshotAndResetPerformance());
        performanceLines = List.copyOf(lines);

        perfSamples = 0;
        perfSampleSeconds = 0;
        sceneUpdateTotalNanos = 0;
        sceneUpdateMaxNanos = 0;
        renderWorldTotalNanos = 0;
        renderWorldMaxNanos = 0;
        renderFogTotalNanos = 0;
        renderFogMaxNanos = 0;
        renderUiTotalNanos = 0;
        renderUiMaxNanos = 0;
        for (int i = 0; i < systemPerfTotals.length; i++) {
            systemPerfTotals[i] = 0;
            systemPerfMax[i] = 0;
        }
    }

    private int[] hottestSystems(int limit) {
        int[] result = new int[limit];
        for (int i = 0; i < limit; i++) {
            result[i] = -1;
        }
        for (int i = 0; i < systemPerfTotals.length; i++) {
            for (int slot = 0; slot < limit; slot++) {
                if (result[slot] == -1 || systemPerfTotals[i] > systemPerfTotals[result[slot]]) {
                    for (int shift = limit - 1; shift > slot; shift--) {
                        result[shift] = result[shift - 1];
                    }
                    result[slot] = i;
                    break;
                }
            }
        }
        return result;
    }

    private double nanosToMillis(double nanos) {
        return nanos / 1_000_000.0;
    }

    private double averageMillis(long totalNanos, int samples) {
        return samples <= 0 ? 0.0 : nanosToMillis(totalNanos / (double) samples);
    }

}
