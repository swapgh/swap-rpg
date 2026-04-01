package app;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

import asset.AssetManager;
import audio.AudioBootstrap;
import audio.AudioService;
import content.bootstrap.AssetBootstrap;
import data.DataRegistry;
import online.OnlineAccountService;
import scene.gameplay.WorldScene;
import state.SceneManager;
import ui.text.UiText;

public final class GamePanel extends JPanel implements Runnable {
    private static final long PERF_REPORT_INTERVAL_NANOS = 1_000_000_000L;
    private static final long SLOW_FRAME_33MS_NANOS = 33_000_000L;
    private static final long SLOW_FRAME_50MS_NANOS = 50_000_000L;

    private final KeyboardState keyboard = new KeyboardState();
    private final SceneManager sceneManager = new SceneManager();
    private final Object perfLock = new Object();
    private final List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    private Thread loopThread;
    private boolean performanceProfilerEnabled;
    private long perfWindowNanos;
    private int updateCount;
    private int renderCount;
    private long updateTotalNanos;
    private long renderTotalNanos;
    private long updateMaxNanos;
    private long renderMaxNanos;
    private volatile List<String> performanceOverlayLines = List.of();
    private long lastGcCount = currentGcCount();
    private long lastGcTimeMillis = currentGcTimeMillis();
    private String slowFrameReason = "";

    public GamePanel() {
        setPreferredSize(new Dimension(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT));
        setDoubleBuffered(true);
        setFocusable(true);
        addKeyListener(keyboard);

        UiPreferencesStore uiPreferences = new UiPreferencesStore();
        UiText.applyLanguage(uiPreferences.loadLanguage(GameConfig.UI_PREFERENCES_FILE));

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
        sceneManager.setScene(accountService.isLoggedIn()
                ? sceneFactory.createTitleScene()
                : sceneFactory.createLoginScene());
    }

    public void start() {
        if (loopThread == null) {
            loopThread = new Thread(this, "swap-rpg-loop");
            loopThread.start();
        }
    }

    public void shutdown() {
        if (sceneManager.current() instanceof WorldScene worldScene) {
            worldScene.closeScene();
        }
        Thread thread = loopThread;
        loopThread = null;
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / GameConfig.TARGET_FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        while (Thread.currentThread() == loopThread) {
            long now = System.nanoTime();
            long elapsed = now - lastTime;
            delta += elapsed / drawInterval;
            lastTime = now;
            accumulatePerfWindow(elapsed);
            while (delta >= 1) {
                long updateStart = System.nanoTime();
                sceneManager.update(1.0 / GameConfig.TARGET_FPS);
                long updateNanos = System.nanoTime() - updateStart;
                recordUpdate(updateNanos);
                reportSlowFrameIfNeeded("update", updateNanos);
                if (keyboard.consumePressed(KeyEvent.VK_F3)) {
                    togglePerformanceProfiler();
                }
                keyboard.endFrame();
                repaint();
                delta--;
            }
            flushPerfReportIfNeeded();
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
        long renderStart = System.nanoTime();
        Graphics2D g2 = (Graphics2D) g;
        sceneManager.render(g2);
        long renderNanos = System.nanoTime() - renderStart;
        recordRender(renderNanos);
        reportSlowFrameIfNeeded("render", renderNanos);
        if (performanceProfilerEnabled) {
            drawPerformanceOverlay(g2);
        }
    }

    private void recordUpdate(long nanos) {
        synchronized (perfLock) {
            updateCount++;
            updateTotalNanos += nanos;
            updateMaxNanos = Math.max(updateMaxNanos, nanos);
        }
    }

    private void recordRender(long nanos) {
        synchronized (perfLock) {
            renderCount++;
            renderTotalNanos += nanos;
            renderMaxNanos = Math.max(renderMaxNanos, nanos);
        }
    }

    private void accumulatePerfWindow(long elapsedNanos) {
        synchronized (perfLock) {
            perfWindowNanos += elapsedNanos;
        }
    }

    private void flushPerfReportIfNeeded() {
        if (!performanceProfilerEnabled) {
            synchronized (perfLock) {
                if (perfWindowNanos >= PERF_REPORT_INTERVAL_NANOS) {
                    perfWindowNanos = 0;
                    updateCount = 0;
                    renderCount = 0;
                    updateTotalNanos = 0;
                    renderTotalNanos = 0;
                    updateMaxNanos = 0;
                    renderMaxNanos = 0;
                }
            }
            performanceOverlayLines = List.of();
            return;
        }
        List<String> sceneLines = sceneManager.current() == null ? List.of() : sceneManager.current().performanceLines();
        List<String> overlayLines = sceneManager.current() == null ? List.of() : sceneManager.current().performanceOverlayLines();
        long window;
        int updates;
        int renders;
        long updateTotal;
        long renderTotal;
        long updateMax;
        long renderMax;
        synchronized (perfLock) {
            if (perfWindowNanos < PERF_REPORT_INTERVAL_NANOS) {
                return;
            }
            window = perfWindowNanos;
            updates = updateCount;
            renders = renderCount;
            updateTotal = updateTotalNanos;
            renderTotal = renderTotalNanos;
            updateMax = updateMaxNanos;
            renderMax = renderMaxNanos;
            perfWindowNanos = 0;
            updateCount = 0;
            renderCount = 0;
            updateTotalNanos = 0;
            renderTotalNanos = 0;
            updateMaxNanos = 0;
            renderMaxNanos = 0;
        }

        double seconds = window / 1_000_000_000.0;
        double updatesPerSecond = updates / Math.max(0.001, seconds);
        double rendersPerSecond = renders / Math.max(0.001, seconds);
        double updateAvgMs = updates == 0 ? 0.0 : nanosToMillis(updateTotal / (double) updates);
        double renderAvgMs = renders == 0 ? 0.0 : nanosToMillis(renderTotal / (double) renders);

        List<String> lines = new ArrayList<>();
        lines.add(String.format("Perf  upd %.1f/s  rnd %.1f/s  upd %.2fms avg %.2fms max  rnd %.2fms avg %.2fms max",
                updatesPerSecond,
                rendersPerSecond,
                updateAvgMs,
                nanosToMillis(updateMax),
                renderAvgMs,
                nanosToMillis(renderMax)));
        lines.addAll(sceneLines);
        List<String> overlay = new ArrayList<>();
        overlay.add(String.format("F3 profiler  FPS %.0f  Render %.2fms  Update %.2fms",
                rendersPerSecond,
                renderAvgMs,
                updateAvgMs));
        overlay.add(String.format("Worst frame  render %.2fms  update %.2fms",
                nanosToMillis(renderMax),
                nanosToMillis(updateMax)));
        overlay.addAll(overlayLines);
        if (slowFrameReason != null && !slowFrameReason.isBlank()) {
            overlay.add(slowFrameReason);
        }
        performanceOverlayLines = List.copyOf(overlay);

        if (!lines.isEmpty()) {
            System.out.println("[perf] " + lines.get(0));
            for (int i = 1; i < lines.size(); i++) {
                System.out.println("[perf]   " + lines.get(i));
            }
        }
    }

    private void drawPerformanceOverlay(Graphics2D g2) {
        List<String> lines = performanceOverlayLines;
        if (lines.isEmpty()) {
            return;
        }
        int width = 0;
        for (String line : lines) {
            width = Math.max(width, g2.getFontMetrics().stringWidth(line));
        }
        int lineHeight = 16;
        int boxX = 12;
        int boxY = 12;
        int boxWidth = width + 20;
        int boxHeight = lines.size() * lineHeight + 16;
        g2.setColor(new Color(8, 12, 18, 185));
        g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 14, 14);
        g2.setColor(new Color(95, 117, 156, 160));
        g2.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 14, 14);
        g2.setColor(new Color(255, 247, 219));
        int y = boxY + 18;
        for (String line : lines) {
            g2.drawString(line, boxX + 10, y);
            y += lineHeight;
        }
    }

    private double nanosToMillis(double nanos) {
        return nanos / 1_000_000.0;
    }

    private void reportSlowFrameIfNeeded(String phase, long nanos) {
        if (!performanceProfilerEnabled) {
            return;
        }
        if (nanos < SLOW_FRAME_33MS_NANOS) {
            return;
        }
        long gcCount = currentGcCount();
        long gcTimeMillis = currentGcTimeMillis();
        long gcCountDelta = gcCount - lastGcCount;
        long gcTimeDelta = gcTimeMillis - lastGcTimeMillis;
        lastGcCount = gcCount;
        lastGcTimeMillis = gcTimeMillis;

        String severity = nanos >= SLOW_FRAME_50MS_NANOS ? "very-slow" : "slow";
        String gcSuffix = gcCountDelta > 0 || gcTimeDelta > 0
                ? String.format("  gc +%d collections, +%dms", gcCountDelta, gcTimeDelta)
                : "";
        slowFrameReason = String.format("Last spike: %s %s frame %.2fms%s",
                severity,
                phase,
                nanosToMillis(nanos),
                gcSuffix);
        System.out.println("[perf] slow-frame " + slowFrameReason);
    }

    private void togglePerformanceProfiler() {
        performanceProfilerEnabled = !performanceProfilerEnabled;
        if (!performanceProfilerEnabled) {
            performanceOverlayLines = List.of();
            slowFrameReason = "";
            synchronized (perfLock) {
                perfWindowNanos = 0;
                updateCount = 0;
                renderCount = 0;
                updateTotalNanos = 0;
                renderTotalNanos = 0;
                updateMaxNanos = 0;
                renderMaxNanos = 0;
            }
            lastGcCount = currentGcCount();
            lastGcTimeMillis = currentGcTimeMillis();
        }
    }

    private long currentGcCount() {
        long total = 0;
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            long count = gcBean.getCollectionCount();
            if (count >= 0) {
                total += count;
            }
        }
        return total;
    }

    private long currentGcTimeMillis() {
        long total = 0;
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            long time = gcBean.getCollectionTime();
            if (time >= 0) {
                total += time;
            }
        }
        return total;
    }
}
