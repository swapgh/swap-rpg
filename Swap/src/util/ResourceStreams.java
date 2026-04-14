package util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ResourceStreams {
    private ResourceStreams() {
    }

    public static InputStream open(Class<?> anchor, String resourcePath) throws IOException {
        InputStream classpathStream = anchor.getResourceAsStream(resourcePath);
        if (classpathStream != null) {
            return classpathStream;
        }

        Path file = resolveFile(resourcePath);
        if (file != null) {
            return Files.newInputStream(file);
        }
        return null;
    }

    private static Path resolveFile(String resourcePath) {
        String relativePath = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        Path cwd = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        for (Path dir = cwd; dir != null; dir = dir.getParent()) {
            Path[] candidates = new Path[] {
                    dir.resolve(relativePath),
                    dir.resolve("res").resolve(relativePath),
                    dir.resolve("Swap").resolve("res").resolve(relativePath),
                    dir.resolve("swap-rpg").resolve("Swap").resolve("res").resolve(relativePath)
            };
            for (Path candidate : candidates) {
                if (Files.isRegularFile(candidate)) {
                    return candidate;
                }
            }
        }
        return null;
    }
}
