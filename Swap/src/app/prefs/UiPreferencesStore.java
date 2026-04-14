package app.prefs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import ui.text.UiLanguage;

public final class UiPreferencesStore {
    public UiLanguage loadLanguage(Path path) {
        if (!Files.exists(path)) {
            return UiLanguage.EN;
        }

        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not load UI preferences", ex);
        }

        String code = properties.getProperty("language", UiLanguage.EN.code());
        for (UiLanguage language : UiLanguage.values()) {
            if (language.code().equalsIgnoreCase(code)) {
                return language;
            }
        }
        return UiLanguage.EN;
    }

    public void saveLanguage(Path path, UiLanguage language) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Could not prepare UI preferences", ex);
        }

        Properties properties = new Properties();
        properties.setProperty("language", language.code());
        try (OutputStream out = Files.newOutputStream(path)) {
            properties.store(out, "swap-rpg ui preferences");
        } catch (IOException ex) {
            throw new IllegalStateException("Could not save UI preferences", ex);
        }
    }
}
