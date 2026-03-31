package data.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class JsonResourceLoader {
    public Object load(String resourcePath) {
        try (InputStream is = JsonResourceLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Missing data resource: " + resourcePath);
            }
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return MiniJsonParser.parse(json);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load data " + resourcePath, ex);
        }
    }
}
