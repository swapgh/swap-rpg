package online;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import data.JsonDataLoader;

public final class SwapWebClient {
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public AuthOutcome login(String siteUrl, String email, String password) {
        return authenticate(siteUrl, "/api/auth/login", """
                {
                  "email": %s,
                  "password": %s
                }
                """.formatted(quote(email), quote(password)));
    }

    public AuthOutcome register(String siteUrl, String username, String email, String password) {
        return authenticate(siteUrl, "/api/auth/register", """
                {
                  "username": %s,
                  "email": %s,
                  "password": %s
                }
                """.formatted(quote(username), quote(email), quote(password)));
    }

    public SyncOutcome syncProgress(String siteUrl, String apiToken, PlayerProgressSnapshot snapshot) {
        try {
            HttpRequest request = HttpRequest.newBuilder(apiUri(siteUrl, "/api/account/progression"))
                    .timeout(Duration.ofSeconds(8))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Authorization", "Bearer " + apiToken)
                    .POST(HttpRequest.BodyPublishers.ofString(snapshot.toJson(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return SyncOutcome.failure(errorMessage(response.body(), "No se pudo sincronizar el progreso."));
            }

            return SyncOutcome.success("Progreso sincronizado.");
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return SyncOutcome.failure("No se pudo conectar con Swap Web.");
        }
    }

    private AuthOutcome authenticate(String siteUrl, String path, String body) {
        try {
            HttpRequest request = HttpRequest.newBuilder(apiUri(siteUrl, path))
                    .timeout(Duration.ofSeconds(8))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return AuthOutcome.failure(errorMessage(response.body(), "No se pudo autenticar la cuenta."));
            }

            Map<String, Object> root = JsonDataLoader.parseObjectText(response.body());
            Map<String, Object> user = JsonDataLoader.objectValue(root, "user");
            AccountSession session = new AccountSession(
                    normalizeSiteUrl(siteUrl),
                    string(user, "id"),
                    string(user, "username"),
                    string(user, "name"),
                    string(user, "email"),
                    string(user, "api_token"));

            if (!session.isValid()) {
                return AuthOutcome.failure("Swap Web no devolvio un token valido.");
            }

            return AuthOutcome.success(session);
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return AuthOutcome.failure("No se pudo conectar con Swap Web.");
        } catch (IllegalArgumentException ex) {
            return AuthOutcome.failure(ex.getMessage());
        }
    }

    private static URI apiUri(String siteUrl, String path) {
        return URI.create(normalizeSiteUrl(siteUrl) + path);
    }

    private static String normalizeSiteUrl(String siteUrl) {
        return siteUrl == null ? "" : siteUrl.strip().replaceAll("/+$", "");
    }

    private static String errorMessage(String json, String fallback) {
        try {
            Map<String, Object> root = JsonDataLoader.parseObjectText(json);
            return string(root, "error");
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private static String string(Map<String, Object> value, String key) {
        Object nested = value.get(key);
        if (nested instanceof String text) {
            return text;
        }
        if (nested == null) {
            return "";
        }
        if (nested instanceof Number || nested instanceof Boolean) {
            return String.valueOf(nested);
        }
        throw new IllegalArgumentException("Valor invalido en " + key + ".");
    }

    private static String quote(String value) {
        String text = value == null ? "" : value;
        return "\"" + text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }
}
