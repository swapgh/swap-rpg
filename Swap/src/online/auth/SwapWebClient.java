package online.auth;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.JsonDataLoader;
import online.session.AccountSession;
import online.sync.PlayerProgressSnapshot;
import online.sync.SyncOutcome;

public final class SwapWebClient {
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public AuthOutcome login(String siteUrl, String identifier, String password) {
        return authenticate(siteUrl, "/api/auth/login", """
                {
                  "identifier": %s,
                  "password": %s
                }
                """.formatted(quote(identifier), quote(password)));
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
            if (response.statusCode() == 401) {
                return SyncOutcome.authFailure("La sesion online ha caducado o ya no es valida.");
            }
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

    public Set<String> fetchRemoteCharacterIds(String siteUrl, String apiToken) {
        try {
            HttpRequest request = HttpRequest.newBuilder(apiUri(siteUrl, "/api/account/characters"))
                    .timeout(Duration.ofSeconds(8))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + apiToken)
                    .GET()
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 401) {
                return null;
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return Set.of();
            }

            Map<String, Object> root = JsonDataLoader.parseObjectText(response.body());
            Object rawCharacters = root.get("characters");
            if (!(rawCharacters instanceof List<?> list)) {
                return Set.of();
            }

            Set<String> ids = new LinkedHashSet<>();
            for (Object value : list) {
                if (!(value instanceof Map<?, ?> item)) {
                    continue;
                }
                Object rawId = item.get("character_id");
                if (rawId instanceof String id && !id.isBlank()) {
                    ids.add(id);
                }
            }
            return ids;
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return Set.of();
        } catch (IllegalArgumentException ex) {
            return Set.of();
        }
    }

    public SyncOutcome reconcileRoster(String siteUrl, String apiToken, Set<String> characterIds) {
        StringBuilder ids = new StringBuilder("[");
        int index = 0;
        for (String characterId : characterIds) {
            if (index++ > 0) {
                ids.append(", ");
            }
            ids.append(quote(characterId));
        }
        ids.append(']');

        String body = """
                {
                  "character_ids": %s
                }
                """.formatted(ids);

        try {
            HttpRequest request = HttpRequest.newBuilder(apiUri(siteUrl, "/api/account/roster/reconcile"))
                    .timeout(Duration.ofSeconds(8))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Authorization", "Bearer " + apiToken)
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 401) {
                return SyncOutcome.authFailure("La sesion online ha caducado o ya no es valida.");
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return SyncOutcome.failure(errorMessage(response.body(), "Could not reconcile roster."));
            }

            return SyncOutcome.success("Roster reconciled.");
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return SyncOutcome.failure("Could not connect to Swap Web.");
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
                    string(root, "api_token"),
                    string(root, "api_token_expires_at"));

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

    public void logout(String siteUrl, String apiToken) {
        try {
            HttpRequest request = HttpRequest.newBuilder(apiUri(siteUrl, "/api/auth/logout"))
                    .timeout(Duration.ofSeconds(8))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + apiToken)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            http.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static URI apiUri(String siteUrl, String path) {
        return URI.create(normalizeSiteUrl(siteUrl) + path);
    }

    private static String normalizeSiteUrl(String siteUrl) {
        String normalized = siteUrl == null ? "" : siteUrl.strip();
        if (!normalized.contains("://") && !normalized.isBlank()) {
            String lower = normalized.toLowerCase();
            if (lower.equals("localhost") || lower.startsWith("localhost:")) {
                normalized = "http://" + normalized;
            } else if (lower.equals("127.0.0.1")
                    || lower.startsWith("127.0.0.1:")
                    || lower.equals("[::1]")
                    || lower.startsWith("[::1]:")) {
                normalized = "http://" + normalized;
            } else {
                normalized = "https://" + normalized;
            }
        }
        normalized = normalized.replaceAll("/+$", "");
        return normalized;
    }

    private static String errorMessage(String json, String fallback) {
        try {
            Map<String, Object> root = JsonDataLoader.parseObjectText(json);
            String message = string(root, "error");
            return message == null || message.isBlank() ? fallback : message;
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
