package audio;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import util.ResourceStreams;

public final class AudioService {
    private static final int DEFAULT_CLIP_POOL_SIZE = 4;

    private final Map<String, LoadedEffect> effects = new HashMap<>();
    private boolean enabled = true;

    public void registerEffect(String id, String resourcePath) {
        LoadedEffect effect = loadEffect(resourcePath);
        if (effect != null) {
            effects.put(id, effect);
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void playEffect(String id) {
        if (!enabled) {
            return;
        }
        LoadedEffect effect = effects.get(id);
        if (effect == null) {
            return;
        }
        try {
            Clip clip = effect.borrowClip();
            if (clip == null) {
                return;
            }
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.start();
        } catch (Exception ex) {
            // keep the reboot playable even if audio is not available in the runtime environment
        }
    }

    public void prewarmEffect(String id) {
        LoadedEffect effect = effects.get(id);
        if (effect == null) {
            return;
        }
        try {
            effect.prewarm();
        } catch (Exception ex) {
            // keep the game playable even if audio warmup is not available
        }
    }

    public void prewarmEffects(String... ids) {
        if (ids == null) {
            return;
        }
        for (String id : ids) {
            prewarmEffect(id);
        }
    }

    private LoadedEffect loadEffect(String resourcePath) {
        try (InputStream raw = ResourceStreams.open(AudioService.class, resourcePath)) {
            if (raw == null) {
                return null;
            }
            try (BufferedInputStream buffered = new BufferedInputStream(raw);
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(buffered)) {
                AudioFormat format = audioStream.getFormat();
                byte[] audioData = readAllBytes(audioStream);
                int frameSize = Math.max(1, format.getFrameSize());
                long frameLength = audioData.length / frameSize;
                return new LoadedEffect(format, audioData, frameLength);
            }
        } catch (Exception ex) {
            return null;
        }
    }

    private static byte[] readAllBytes(AudioInputStream audioStream) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = audioStream.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private static final class LoadedEffect {
        private final AudioFormat format;
        private final byte[] audioData;
        private final long frameLength;
        private final List<Clip> clips = new ArrayList<>();
        private int roundRobinIndex;

        private LoadedEffect(AudioFormat format, byte[] audioData, long frameLength) {
            this.format = format;
            this.audioData = audioData;
            this.frameLength = frameLength;
        }

        private synchronized Clip borrowClip() throws Exception {
            for (Clip clip : clips) {
                if (!clip.isRunning()) {
                    return clip;
                }
            }
            if (clips.size() < DEFAULT_CLIP_POOL_SIZE) {
                Clip clip = createClip();
                clips.add(clip);
                return clip;
            }
            Clip clip = clips.get(roundRobinIndex);
            roundRobinIndex = (roundRobinIndex + 1) % clips.size();
            return clip;
        }

        private synchronized void prewarm() throws Exception {
            if (!clips.isEmpty()) {
                return;
            }
            clips.add(createClip());
        }

        private Clip createClip() throws Exception {
            Clip clip = AudioSystem.getClip();
            ByteArrayInputStream input = new ByteArrayInputStream(audioData);
            AudioInputStream stream = new AudioInputStream(input, format, frameLength);
            clip.open(stream);
            stream.close();
            return clip;
        }
    }
}
