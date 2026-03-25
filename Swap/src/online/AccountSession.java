package online;

public record AccountSession(
        String siteUrl,
        String userId,
        String username,
        String displayName,
        String email,
        String apiToken) {
    public boolean isValid() {
        return siteUrl != null && !siteUrl.isBlank()
                && apiToken != null && !apiToken.isBlank()
                && userId != null && !userId.isBlank();
    }
}
