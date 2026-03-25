package scene;

import java.awt.Color;
import java.awt.Graphics2D;
import java.nio.file.Path;
import java.util.List;

import app.Camera;
import app.KeyboardState;
import asset.AssetManager;
import asset.TileMap;
import audio.AudioBootstrap;
import audio.AudioService;
import component.HealthComponent;
import component.InputComponent;
import component.InventoryComponent;
import component.PlayerComponent;
import component.QuestComponent;
import content.AssetBootstrap;
import content.WorldSeeder;
import data.DataRegistry;
import ecs.EcsSystem;
import ecs.EcsWorld;
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

/** Escena principal de juego. Orquesta mundo, systems, UI y render. */
public final class WorldScene implements Scene {
    private final SceneManager sceneManager;
    private final KeyboardState keyboard;
    private final AssetManager assets;
    private final AudioService audio;
    private final DataRegistry data;
    private final UiState ui;
    private final HudRenderer hud;
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

    public WorldScene(SceneManager sceneManager, KeyboardState keyboard, AssetManager assets, AudioService audio,
            DataRegistry data, UiState ui, int tileSize, int screenWidth, int screenHeight, Path savePath) {
        this.sceneManager = sceneManager;
        this.keyboard = keyboard;
        this.assets = assets;
        this.audio = audio;
        this.data = data;
        this.ui = ui;
        this.hud = new HudRenderer(assets, tileSize);
        this.map = WorldSeeder.createMap(assets, tileSize);
        this.world = new EcsWorld();
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.tileSize = tileSize;

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

    /** Helper para crear la escena con assets, audio y data ya cargados. */
    public static WorldScene create(SceneManager sceneManager, KeyboardState keyboard, int tileSize, int screenWidth,
            int screenHeight, Path savePath) {
        AssetManager assets = new AssetManager();
        AssetBootstrap.loadAll(assets, tileSize);
        AudioService audio = AudioBootstrap.createDefault();
        DataRegistry data = DataRegistry.loadDefaults();
        return new WorldScene(sceneManager, keyboard, assets, audio, data, new UiState(), tileSize, screenWidth,
                screenHeight, savePath);
    }

    @Override
    public void update(double dtSeconds) {
        if (ui.toastTicks > 0) {
            ui.toastTicks--;
        }

        for (EcsSystem system : systems) {
            system.update(world, dtSeconds);
        }

        int player = world.entitiesWith(PlayerComponent.class).get(0);
        InputComponent input = world.require(player, InputComponent.class);

        if (ui.mode == GameMode.TITLE) {
            saveLoadSystem.save(world, savePath);
            UiState titleUi = new UiState();
            titleUi.subtitleMessage = "Has caido. Pulsa ENTER para empezar otra vez";
            sceneManager.setScene(new TitleScene(keyboard, sceneManager,
                    new WorldScene(sceneManager, keyboard, assets, audio, data, new UiState(), tileSize, screenWidth,
                            screenHeight, savePath),
                    new HudRenderer(assets, tileSize), titleUi, screenWidth, screenHeight));
            return;
        }

        if (input.interactPressed && ui.mode == GameMode.PLAY) {
            ui.dialogueSpeaker = "";
        }
    }

    @Override
    public void render(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, screenWidth, screenHeight);
        renderSystem.render(g2, world);

        int player = world.entitiesWith(PlayerComponent.class).get(0);
        hud.drawWorldHud(g2, ui, screenWidth, world.require(player, HealthComponent.class),
                world.require(player, InventoryComponent.class), world.require(player, QuestComponent.class));

        if (ui.mode == GameMode.DIALOGUE) {
            hud.drawDialogue(g2, ui, screenWidth, screenHeight);
        }
        if (ui.mode == GameMode.INVENTORY) {
            hud.drawInventory(g2, world.require(player, InventoryComponent.class), screenWidth, screenHeight);
        }
    }
}
