package app;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

import asset.AssetManager;
import audio.AudioBootstrap;
import audio.AudioService;
import content.AssetBootstrap;
import data.DataRegistry;
import online.OnlineAccountService;
import scene.LoginScene;
import state.SceneManager;

public final class GamePanel extends JPanel implements Runnable {
    private final KeyboardState keyboard = new KeyboardState();
    private final SceneManager sceneManager = new SceneManager();
    private Thread loopThread;

    public GamePanel() {
        setPreferredSize(new Dimension(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT));
        setDoubleBuffered(true);
        setFocusable(true);
        addKeyListener(keyboard);

        AssetManager assets = new AssetManager();
        AssetBootstrap.loadAll(assets, GameConfig.TILE_SIZE);
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
                GameConfig.TILE_SIZE,
                GameConfig.SCREEN_WIDTH,
                GameConfig.SCREEN_HEIGHT);
        sceneManager.setScene(sceneFactory.createLoginScene());
    }

    public void start() {
        if (loopThread == null) {
            loopThread = new Thread(this, "swap-rpg-loop");
            loopThread.start();
        }
    }

    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / GameConfig.TARGET_FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        while (Thread.currentThread() == loopThread) {
            long now = System.nanoTime();
            delta += (now - lastTime) / drawInterval;
            lastTime = now;
            while (delta >= 1) {
                sceneManager.update(1.0 / GameConfig.TARGET_FPS);
                keyboard.endFrame();
                repaint();
                delta--;
            }
            try {
                Thread.sleep(2);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        sceneManager.render((Graphics2D) g);
    }
}
