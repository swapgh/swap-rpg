package online.sync;

public record SyncOutcome(
        boolean ok,
        String message,
        boolean authInvalid) {
    public static SyncOutcome success(String message) {
        return new SyncOutcome(true, message, false);
    }

    public static SyncOutcome failure(String message) {
        return new SyncOutcome(false, message, false);
    }

    public static SyncOutcome authFailure(String message) {
        return new SyncOutcome(false, message, true);
    }
}
