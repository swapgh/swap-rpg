package online;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AccountSessionStore {
    public AccountSession load(Path path) {
        if (!Files.exists(path)) {
            return null;
        }

        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo cargar la sesion online", ex);
        }

        AccountSession session = new AccountSession(
                properties.getProperty("site_url", ""),
                properties.getProperty("user_id", ""),
                properties.getProperty("username", ""),
                properties.getProperty("display_name", ""),
                properties.getProperty("email", ""),
                properties.getProperty("api_token", ""));

        return session.isValid() ? session : null;
    }

    public void save(Path path, AccountSession session) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo preparar la sesion online", ex);
        }

        Properties properties = new Properties();
        properties.setProperty("site_url", session.siteUrl());
        properties.setProperty("user_id", session.userId());
        properties.setProperty("username", session.username());
        properties.setProperty("display_name", session.displayName());
        properties.setProperty("email", session.email());
        properties.setProperty("api_token", session.apiToken());

        try (OutputStream out = Files.newOutputStream(path)) {
            properties.store(out, "swap-rpg online session");
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo guardar la sesion online", ex);
        }
    }

    public void clear(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo limpiar la sesion online", ex);
        }
    }
}
