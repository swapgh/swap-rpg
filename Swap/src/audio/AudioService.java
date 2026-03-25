package audio;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;

public final class AudioService {
    private final Map<String, String> effects = new HashMap<>();
    private boolean enabled = true;

    public void registerEffect(String id, String resourcePath) {
        effects.put(id, resourcePath);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void playEffect(String id) {
        if (!enabled) {
            return;
        }
        String resourcePath = effects.get(id);
        if (resourcePath == null) {
            return;
        }
        try (InputStream raw = AudioService.class.getResourceAsStream(resourcePath)) {
            if (raw == null) {
                return;
            }
            try (BufferedInputStream buffered = new BufferedInputStream(raw);
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(buffered)) {
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
                clip.start();
            }
        } catch (Exception ex) {
            // keep the reboot playable even if audio is not available in the runtime environment
        }
    }
}
