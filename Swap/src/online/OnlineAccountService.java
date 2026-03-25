package online;

import java.nio.file.Path;

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

    public AuthOutcome login(String siteUrl, String email, String password) {
        AuthOutcome outcome = client.login(siteUrl, email, password);
        if (outcome.ok() && outcome.session() != null) {
            session = outcome.session();
            store.save(sessionPath, session);
        }
        return outcome;
    }

    public AuthOutcome register(String siteUrl, String username, String email, String password) {
        AuthOutcome outcome = client.register(siteUrl, username, email, password);
        if (outcome.ok() && outcome.session() != null) {
            session = outcome.session();
            store.save(sessionPath, session);
        }
        return outcome;
    }

    public void logout() {
        session = null;
        store.clear(sessionPath);
    }

    public SyncOutcome sync(PlayerProgressSnapshot snapshot) {
        if (!isLoggedIn()) {
            return SyncOutcome.failure("No hay cuenta conectada.");
        }
        return client.syncProgress(session.siteUrl(), session.apiToken(), snapshot);
    }
}
