package online;

import java.time.Instant;

public record AccountSession(
        String siteUrl,
        String userId,
        String username,
        String displayName,
        String email,
        String apiToken,
        String apiTokenExpiresAt) {
    public boolean isValid() {
        return siteUrl != null && !siteUrl.isBlank()
                && apiToken != null && !apiToken.isBlank()
                && userId != null && !userId.isBlank()
                && !isExpired();
    }

    public boolean isExpired() {
        if (apiTokenExpiresAt == null || apiTokenExpiresAt.isBlank()) {
            return true;
        }

        try {
            return Instant.parse(apiTokenExpiresAt).isBefore(Instant.now());
        } catch (RuntimeException ex) {
            return true;
        }
    }
}
