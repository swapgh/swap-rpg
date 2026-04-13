package online;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AccountSessionStore {
    public AccountSession load(Path path) {
        Path preferred = normalizePath(path);
        Path source = resolveLoadPath(path);
        if (source == null || !Files.exists(source)) {
            return null;
        }

        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(source)) {
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
                properties.getProperty("api_token", ""),
                properties.getProperty("api_token_expires_at", ""));

        if (!session.isValid()) {
            return null;
        }

        if (!preferred.equals(source)) {
            save(path, session);
        }

        return session;
    }

    public void save(Path path, AccountSession session) {
        Path target = normalizePath(path);
        try {
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
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
        properties.setProperty("api_token_expires_at", session.apiTokenExpiresAt());

        try (OutputStream out = Files.newOutputStream(target)) {
            properties.store(out, "swap-rpg online session");
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo guardar la sesion online", ex);
        }
    }

    public void clear(Path path) {
        Path target = normalizePath(path);
        try {
            Files.deleteIfExists(target);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo limpiar la sesion online", ex);
        }
    }

    private Path resolveLoadPath(Path preferredPath) {
        Path normalizedPreferred = normalizePath(preferredPath);
        if (Files.exists(normalizedPreferred)) {
            return normalizedPreferred;
        }

        String fileName = normalizedPreferred.getFileName().toString();
        Path current = Path.of("").toAbsolutePath().normalize();
        for (int i = 0; i < 4 && current != null; i++) {
            Path candidate = current.resolve(fileName);
            if (Files.exists(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        return normalizedPreferred;
    }

    private Path normalizePath(Path path) {
        return path.toAbsolutePath().normalize();
    }
}
