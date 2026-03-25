package online;

public record AuthOutcome(
        boolean ok,
        AccountSession session,
        String error) {
    public static AuthOutcome success(AccountSession session) {
        return new AuthOutcome(true, session, "");
    }

    public static AuthOutcome failure(String error) {
        return new AuthOutcome(false, null, error);
    }
}
