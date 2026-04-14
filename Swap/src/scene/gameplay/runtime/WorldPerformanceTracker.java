package scene.gameplay.runtime;

import java.util.ArrayList;
import java.util.List;

public final class WorldPerformanceTracker {
    private final Object lock = new Object();
    private final String[] systemNames;
    private final long[] systemTotals;
    private final long[] systemMax;

    private long sceneUpdateTotalNanos;
    private long sceneUpdateMaxNanos;
    private long renderWorldTotalNanos;
    private long renderWorldMaxNanos;
    private long renderFogTotalNanos;
    private long renderFogMaxNanos;
    private long renderUiTotalNanos;
    private long renderUiMaxNanos;
    private int samples;
    private double sampleSeconds;
    private volatile List<String> performanceLines = List.of();

    public WorldPerformanceTracker(String[] systemNames) {
        this.systemNames = systemNames.clone();
        this.systemTotals = new long[systemNames.length];
        this.systemMax = new long[systemNames.length];
    }

    public void recordSystem(int systemIndex, long nanos) {
        synchronized (lock) {
            if (systemIndex < 0 || systemIndex >= systemTotals.length) {
                return;
            }
            systemTotals[systemIndex] += nanos;
            systemMax[systemIndex] = Math.max(systemMax[systemIndex], nanos);
        }
    }

    public void recordSceneUpdate(long nanos, double dtSeconds, String sceneName, int entityCount, List<String> extraLines) {
        synchronized (lock) {
            samples++;
            sampleSeconds += dtSeconds;
            sceneUpdateTotalNanos += nanos;
            sceneUpdateMaxNanos = Math.max(sceneUpdateMaxNanos, nanos);
            if (sampleSeconds >= 1.0) {
                refreshSnapshot(sceneName, entityCount, extraLines);
            }
        }
    }

    public void recordWorldRender(long nanos) {
        synchronized (lock) {
            renderWorldTotalNanos += nanos;
            renderWorldMaxNanos = Math.max(renderWorldMaxNanos, nanos);
        }
    }

    public void recordFogRender(long nanos) {
        synchronized (lock) {
            renderFogTotalNanos += nanos;
            renderFogMaxNanos = Math.max(renderFogMaxNanos, nanos);
        }
    }

    public void recordUiRender(long nanos) {
        synchronized (lock) {
            renderUiTotalNanos += nanos;
            renderUiMaxNanos = Math.max(renderUiMaxNanos, nanos);
        }
    }

    public List<String> performanceLines() {
        return performanceLines;
    }

    public List<String> performanceOverlayLines(int entityCount) {
        List<String> lines = new ArrayList<>();
        lines.add(String.format("Scene: world  Entities: %d", entityCount));
        lines.add(String.format("Render split  fog %.2fms  world %.2fms  ui %.2fms",
                averageMillis(renderFogTotalNanos, samples),
                averageMillis(renderWorldTotalNanos, samples),
                averageMillis(renderUiTotalNanos, samples)));
        int[] hottest = hottestSystems(2);
        if (hottest[0] >= 0) {
            lines.add(String.format("Hot system: %s %.2fms avg",
                    systemNames[hottest[0]],
                    averageMillis(systemTotals[hottest[0]], samples)));
        }
        if (hottest.length > 1 && hottest[1] >= 0) {
            lines.add(String.format("Next: %s %.2fms avg",
                    systemNames[hottest[1]],
                    averageMillis(systemTotals[hottest[1]], samples)));
        }
        lines.add("Use console for detailed profiler lines");
        return lines;
    }

    private void refreshSnapshot(String sceneName, int entityCount, List<String> extraLines) {
        List<String> lines = new ArrayList<>();
        int sampleCount = Math.max(1, samples);
        lines.add(String.format("Scene %s  ent %d  upd %.2fms avg %.2fms max",
                sceneName,
                entityCount,
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
                    systemNames[index],
                    nanosToMillis(systemTotals[index] / (double) sampleCount),
                    nanosToMillis(systemMax[index])));
        }
        lines.addAll(extraLines);
        performanceLines = List.copyOf(lines);
        resetSamples();
    }

    private void resetSamples() {
        samples = 0;
        sampleSeconds = 0;
        sceneUpdateTotalNanos = 0;
        sceneUpdateMaxNanos = 0;
        renderWorldTotalNanos = 0;
        renderWorldMaxNanos = 0;
        renderFogTotalNanos = 0;
        renderFogMaxNanos = 0;
        renderUiTotalNanos = 0;
        renderUiMaxNanos = 0;
        for (int i = 0; i < systemTotals.length; i++) {
            systemTotals[i] = 0;
            systemMax[i] = 0;
        }
    }

    private int[] hottestSystems(int limit) {
        int[] result = new int[limit];
        for (int i = 0; i < limit; i++) {
            result[i] = -1;
        }
        for (int i = 0; i < systemTotals.length; i++) {
            for (int slot = 0; slot < limit; slot++) {
                if (result[slot] == -1 || systemTotals[i] > systemTotals[result[slot]]) {
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

    private double averageMillis(long totalNanos, int sampleCount) {
        return sampleCount <= 0 ? 0.0 : nanosToMillis(totalNanos / (double) sampleCount);
    }
}
