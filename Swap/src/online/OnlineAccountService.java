package online;

import java.nio.file.Path;
import java.util.Set;

public final class OnlineAccountService {
    private final Path sessionPath;
    private final AccountSessionStore store = new AccountSessionStore();
    private final SwapWebClient client = new SwapWebClient();
    private AccountSession session;

    public OnlineAccountService(Path sessionPath) {
        this.sessionPath = sessionPath;
        this.session = store.load(sessionPath);
    }

    public boolean isLoggedIn() {
        return session != null && session.isValid();
    }

    public String displayLabel() {
        if (!isLoggedIn()) {
            return "Invitado";
        }
        return session.displayName() == null || session.displayName().isBlank()
                ? session.email()
                : session.displayName();
    }

    public String siteUrl() {
        if (!isLoggedIn()) {
            return "";
        }
        return session.siteUrl();
    }

    public String saveProfileKey() {
        if (!isLoggedIn()) {
            return "guest";
        }
        return "account-" + sanitize(session.userId());
    }

    public AuthOutcome login(String siteUrl, String email, String password) {
        clearSessionIfSwitchingSite(siteUrl);
        AuthOutcome outcome = client.login(siteUrl, email, password);
        if (outcome.ok() && outcome.session() != null) {
            session = outcome.session();
            store.save(sessionPath, session);
        }
        return outcome;
    }

    public AuthOutcome register(String siteUrl, String username, String email, String password) {
        clearSessionIfSwitchingSite(siteUrl);
        AuthOutcome outcome = client.register(siteUrl, username, email, password);
        if (outcome.ok() && outcome.session() != null) {
            session = outcome.session();
            store.save(sessionPath, session);
        }
        return outcome;
    }

    public void logout() {
        if (session != null && session.apiToken() != null && !session.apiToken().isBlank()) {
            client.logout(session.siteUrl(), session.apiToken());
        }
        clearSession();
    }

    public SyncOutcome sync(PlayerProgressSnapshot snapshot) {
        if (!isLoggedIn()) {
            return SyncOutcome.failure("No hay cuenta conectada.");
        }
        SyncOutcome outcome = client.syncProgress(session.siteUrl(), session.apiToken(), snapshot);
        return handleProtectedOutcome(outcome);
    }

    public Set<String> remoteCharacterIds() {
        if (!isLoggedIn()) {
            return Set.of();
        }
        if (session != null && session.isExpired()) {
            clearSession();
            return Set.of();
        }
        Set<String> remoteIds = client.fetchRemoteCharacterIds(session.siteUrl(), session.apiToken());
        if (remoteIds == null) {
            clearSession();
            return Set.of();
        }
        return remoteIds;
    }

    public SyncOutcome reconcileRoster(Set<String> characterIds) {
        if (!isLoggedIn()) {
            return SyncOutcome.failure("No connected account.");
        }
        SyncOutcome outcome = client.reconcileRoster(session.siteUrl(), session.apiToken(), characterIds);
        return handleProtectedOutcome(outcome);
    }

    private String sanitize(String value) {
        return value == null ? "guest" : value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private SyncOutcome handleProtectedOutcome(SyncOutcome outcome) {
        if (outcome.ok()) {
            return outcome;
        }

        if (session != null && (outcome.authInvalid() || session.isExpired())) {
            clearSession();
            return SyncOutcome.failure("La sesion online ya no es valida. Inicia sesion otra vez.");
        }

        return outcome;
    }

    private void clearSession() {
        session = null;
        store.clear(sessionPath);
    }

    private void clearSessionIfSwitchingSite(String nextSiteUrl) {
        if (session == null) {
            return;
        }

        String current = canonicalSiteUrl(session.siteUrl());
        String next = canonicalSiteUrl(nextSiteUrl);
        if (!current.isBlank() && !next.isBlank() && !current.equals(next)) {
            clearSession();
        }
    }

    private String canonicalSiteUrl(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase();
        normalized = normalized.replaceFirst("^https?://", "");
        normalized = normalized.replaceAll("/+$", "");
        if (normalized.startsWith("localhost:")) {
            return "loopback:" + normalized.substring("localhost:".length());
        }
        if (normalized.equals("localhost")) {
            return "loopback";
        }
        if (normalized.startsWith("127.0.0.1:")) {
            return "loopback:" + normalized.substring("127.0.0.1:".length());
        }
        if (normalized.equals("127.0.0.1")) {
            return "loopback";
        }
        if (normalized.startsWith("[::1]:")) {
            return "loopback:" + normalized.substring("[::1]:".length());
        }
        if (normalized.equals("[::1]") || normalized.equals("::1")) {
            return "loopback";
        }
        return normalized;
    }
}
