package online;

public record SyncOutcome(
        boolean ok,
        String message) {
    public static SyncOutcome success(String message) {
        return new SyncOutcome(true, message);
    }

    public static SyncOutcome failure(String message) {
        return new SyncOutcome(false, message);
    }
}
