package scene.gameplay;

import app.camera.Camera;
import app.bootstrap.GameConfig;
import app.input.KeyboardState;
import app.bootstrap.SceneComposer;
import asset.AssetManager;
import asset.TileMap;
import audio.AudioBootstrap;
import audio.AudioService;
import component.character.NameComponent;
import component.character.EnemyComponent;
import component.combat.HealthComponent;
import component.character.InputComponent;
import component.combat.ProjectileEmitterComponent;
import component.combat.StatsComponent;
import component.progression.EquipmentComponent;
import component.progression.InventoryComponent;
import component.character.PlayerComponent;
import component.progression.ProgressionComponent;
import component.progression.QuestComponent;
import component.world.PositionComponent;
import component.world.WorldTimeComponent;
import component.world.WorldTierComponent;
import content.world.WorldSeeder;
import data.DataRegistry;
import ecs.EcsSystem;
import ecs.EcsWorld;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import online.auth.OnlineAccountService;
import save.SaveManager;
import save.SaveReference;
import scene.gameplay.control.WorldOptionsMenu;
import scene.gameplay.world.WorldProgressSyncController;
import scene.gameplay.runtime.WorldPerformanceTracker;
import scene.gameplay.control.WorldSaveController;
import scene.gameplay.world.WorldStartLayout;
import state.GameMode;
import state.Scene;
import state.SceneManager;
import system.combat.CombatSystem;
import system.combat.DropSystem;
import system.combat.HealthSystem;
import system.combat.ProjectileSystem;
import system.input.InputSystem;
import system.persistence.SaveLoadSystem;
import system.inventory.CharacterScreenSystem;
import system.interaction.InteractionSystem;
import system.inventory.InventorySystem;
import system.loot.LootSystem;
import system.progression.ProgressionSystem;
import system.quest.QuestSystem;
import system.interaction.ShopSystem;
import system.interaction.TradeSystem;
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
import ui.state.UiState;
import progression.WorldTierRules;

public final class WorldScene implements Scene {
    private final SceneManager sceneManager;
    private final KeyboardState keyboard;
    private final DataRegistry data;
    private final UiState ui;
    private final HudRenderer hud;
    private final FogOfWarRenderer fogOfWar;
    private final OnlineAccountService accountService;
    private final SceneComposer sceneFactory;
    private final Camera camera = new Camera();
    private final TileMap map;
    private final EcsWorld world;

    private final List<EcsSystem> systems;
    private final ProjectileSystem projectileSystem;
    private final CombatSystem combatSystem;
    private final RenderSystem renderSystem;
    private final SaveLoadSystem saveLoadSystem = new SaveLoadSystem();
    private final ShopSystem shopSystem;
    private final LootSystem lootSystem;
    private final WorldOptionsMenu optionsMenu;
    private final WorldSaveController saveController;
    private final WorldProgressSyncController progressSyncController;
    private final WorldPerformanceTracker performanceTracker;
    private final int screenWidth;
    private final int screenHeight;
    private final int tileSize;
    private final String[] systemPerfNames = {
            "UiOverlay",
            "Input",
            "DayNight",
            "Inventory",
            "Character",
            "Wander",
            "Movement",
            "Projectile",
            "Loot",
            "Interaction",
            "Combat",
            "Drop",
            "Health",
            "Progression",
            "Respawn",
            "Trade",
            "Quest",
            "Animation",
            "Camera"
    };
    private double autoSaveCooldown = GameConfig.AUTO_SAVE_INTERVAL_SECONDS;

    public WorldScene(SceneManager sceneManager, KeyboardState keyboard, AssetManager assets, AudioService audio,
            DataRegistry data, UiState ui, int tileSize, int screenWidth, int screenHeight, SaveManager saveManager,
            SaveReference initialSaveReference, boolean loadFromSave, OnlineAccountService accountService,
            SceneComposer sceneFactory, String initialPlayerClassId) {
        this.sceneManager = sceneManager;
        this.keyboard = keyboard;
        this.data = data;
        this.ui = ui;
        this.hud = new HudRenderer(assets, data, tileSize);
        this.accountService = accountService;
        this.sceneFactory = sceneFactory;
        String initialMapResource = loadFromSave ? null : WorldStartLayout.mapResourceFor(initialPlayerClassId);
        this.map = initialMapResource == null
                ? WorldSeeder.createMap(assets, tileSize, data)
                : WorldSeeder.createMap(assets, tileSize, data, initialMapResource);
        this.fogOfWar = new FogOfWarRenderer(map, camera, screenWidth, screenHeight, tileSize);
        this.world = new EcsWorld();
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.tileSize = tileSize;
        this.optionsMenu = new WorldOptionsMenu(keyboard, sceneManager, sceneFactory, saveManager);
        this.saveController = new WorldSaveController(saveManager, saveLoadSystem, ui);
        this.progressSyncController = new WorldProgressSyncController(accountService, data, ui);
        this.saveController.initialize(initialSaveReference);
        this.performanceTracker = new WorldPerformanceTracker(systemPerfNames);

        int[] spawn = WorldStartLayout.spawnFor(initialPlayerClassId);
        WorldSeeder.seedPlayer(world, tileSize, data, initialPlayerClassId, spawn[0], spawn[1]);
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
                new CharacterScreenSystem(keyboard, ui),
                shopSystem,
                new WanderSystem(),
                new MovementSystem(map),
                projectileSystem,
                lootSystem = new LootSystem(keyboard, ui, audio, tileSize, data),
                new InteractionSystem(ui, audio, keyboard, tileSize, data),
                combatSystem,
                new DropSystem(tileSize),
                new HealthSystem(ui),
                new ProgressionSystem(data, ui),
                new RespawnSystem(map, data, tileSize),
                new TradeSystem(keyboard, ui, data),
                new QuestSystem(ui, audio, data),
                new AnimationSystem(assets),
                new CameraSystem(camera, map, screenWidth, screenHeight));
        AudioBootstrap.prewarmWorldEffects(audio);
        saveController.loadIfRequested(world, loadFromSave);
        centerCameraOnPlayer();
    }

    @Override
    public void update(double dtSeconds) {
        if (keyboard.consumePressed(KeyEvent.VK_F10) && (ui.mode == GameMode.PLAY || ui.mode == GameMode.OPTIONS)) {
            optionsMenu.toggle(ui);
            return;
        }

        if (ui.mode == GameMode.OPTIONS) {
            syncWorldTierUi();
            optionsMenu.update(ui);
            applyRequestedWorldTierIfNeeded();
            return;
        }

        long updateStart = System.nanoTime();
        if (ui.mode == GameMode.PLAY) {
            autoSaveCooldown = Math.max(0, autoSaveCooldown - dtSeconds);
        }

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
        syncWorldTierUi();
        applyRequestedWorldTierIfNeeded();

        if (ui.mode == GameMode.TITLE) {
            progressSyncController.syncInitialAccountState(world);
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

        progressSyncController.update(world, dtSeconds, keyboard.consumePressed(KeyEvent.VK_Y));
    }

    private void syncWorldTierUi() {
        List<Integer> timeEntities = world.entitiesWith(WorldTimeComponent.class, WorldTierComponent.class);
        if (timeEntities.isEmpty()) {
            return;
        }
        int tier = world.require(timeEntities.get(0), WorldTierComponent.class).tier;
        ui.currentWorldTier = tier;
        optionsMenu.syncWorldTier(tier);
    }

    private void applyRequestedWorldTierIfNeeded() {
        if (ui.requestedWorldTier < 1) {
            return;
        }
        List<Integer> timeEntities = world.entitiesWith(WorldTimeComponent.class, WorldTierComponent.class);
        if (timeEntities.isEmpty()) {
            ui.requestedWorldTier = -1;
            return;
        }
        WorldTierComponent worldTier = world.require(timeEntities.get(0), WorldTierComponent.class);
        int nextTier = WorldTierRules.clampTier(ui.requestedWorldTier);
        ui.requestedWorldTier = -1;
        if (worldTier.tier == nextTier) {
            return;
        }
        worldTier.tier = nextTier;
        rescaleLivingEnemies(nextTier);
        ui.pushToast("World Tier WT" + nextTier, 180);
    }

    private void rescaleLivingEnemies(int tier) {
        double hpMultiplier = WorldTierRules.enemyHpMultiplier(tier);
        double damageMultiplier = WorldTierRules.enemyDamageMultiplier(tier);
        for (int enemy : world.entitiesWith(EnemyComponent.class, StatsComponent.class, HealthComponent.class)) {
            EnemyComponent enemyComponent = world.require(enemy, EnemyComponent.class);
            var dataEnemy = data.enemy(enemyComponent.enemyType);
            HealthComponent health = world.require(enemy, HealthComponent.class);
            StatsComponent stats = world.require(enemy, StatsComponent.class);
            int previousMax = Math.max(1, health.max);
            int scaledMax = Math.max(1, (int) Math.round(dataEnemy.stats().health() * hpMultiplier));
            health.max = scaledMax;
            health.current = Math.max(1, Math.min(scaledMax, (int) Math.round(health.current * (scaledMax / (double) previousMax))));
            stats.attack = Math.max(1, (int) Math.round(dataEnemy.stats().attack() * damageMultiplier));
            if (world.has(enemy, ProjectileEmitterComponent.class)) {
                world.require(enemy, ProjectileEmitterComponent.class).projectileDamage =
                        Math.max(1, (int) Math.round(dataEnemy.projectile().damage() * damageMultiplier));
            }
        }
    }

    public void prepareForPlay() {
        ui.mode = GameMode.PLAY;
        ui.inventoryVisible = false;
        ui.characterVisible = false;
        ui.contextHint = "";
        ui.clearSystemLog();
        ui.combatToast = "";
        ui.combatToastTicks = 0;
        centerCameraOnPlayer();
    }

    public void syncInitialAccountState() {
        if (!accountService.isLoggedIn()) {
            return;
        }
        progressSyncController.syncInitialAccountState(world);
    }

    public void createCharacterSlot(String displayName) {
        if (displayName != null && !displayName.isBlank()) {
            List<Integer> players = world.entitiesWith(PlayerComponent.class, NameComponent.class);
            if (!players.isEmpty()) {
                world.require(players.get(0), NameComponent.class).value = displayName.trim();
            }
        }
        saveController.createCharacterSlot(world, displayName);
    }

    private void centerCameraOnPlayer() {
        List<Integer> players = world.entitiesWith(PlayerComponent.class, PositionComponent.class);
        if (players.isEmpty()) {
            return;
        }
        int player = players.get(0);
        PositionComponent pos = world.require(player, PositionComponent.class);
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
        WorldTierComponent worldTier = timeEntities.isEmpty() || !world.has(timeEntities.get(0), WorldTierComponent.class)
                ? null
                : world.require(timeEntities.get(0), WorldTierComponent.class);
        long fogRenderStart = System.nanoTime();
        fogOfWar.render(g2, world, player, worldTime == null || worldTime.isDay());
        performanceTracker.recordFogRender(System.nanoTime() - fogRenderStart);
        long uiRenderStart = System.nanoTime();
        hud.drawWorldHud(g2, ui, screenWidth, screenHeight, world.require(player, HealthComponent.class),
                world.require(player, InventoryComponent.class), world.require(player, ProgressionComponent.class),
                world.require(player, EquipmentComponent.class),
                world.require(player, QuestComponent.class), worldTime, worldTier,
                accountService.displayLabel(), accountService.isLoggedIn());

        if (ui.mode == GameMode.DIALOGUE) {
            hud.drawDialogue(g2, ui, screenWidth, screenHeight);
        }
        if (ui.inventoryVisible) {
            hud.drawInventory(g2, ui, world.require(player, InventoryComponent.class), screenWidth, screenHeight,
                    ui.characterVisible);
        }
        if (ui.characterVisible) {
            hud.drawCharacter(g2,
                    world.require(player, NameComponent.class),
                    world.require(player, HealthComponent.class),
                    world.require(player, ProgressionComponent.class),
                    world.require(player, EquipmentComponent.class),
                    screenWidth,
                    screenHeight,
                    ui.inventoryVisible);
        }
        if (ui.mode == GameMode.SHOP) {
            hud.drawShop(g2, ui, world.require(player, InventoryComponent.class), shopSystem.currentShopEntries(world),
                    screenWidth, screenHeight);
        }
        if (ui.mode == GameMode.LOOT) {
            hud.drawLoot(g2, ui, world.require(player, InventoryComponent.class), lootSystem.currentLootEntries(world),
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
