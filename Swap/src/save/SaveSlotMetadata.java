package save;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record SaveSlotMetadata(
        SaveReference reference,
        String displayName,
        String playerName,
        long createdAtEpochSeconds,
        long updatedAtEpochSeconds,
        long lastLoadedAtEpochSeconds,
        int worldDay,
        int worldHour,
        int worldMinute,
        int enemiesKilled,
        String syncState,
        String remoteId,
        long lastSyncedAtEpochSeconds) {
    private static final DateTimeFormatter MENU_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    public String menuLabel() {
        String name = displayName == null || displayName.isBlank() ? fallbackName() : displayName;
        String when = updatedAtEpochSeconds > 0 ? MENU_TIME_FORMATTER.format(Instant.ofEpochSecond(updatedAtEpochSeconds)) : "--";
        return "%s  D%d %02d:%02d  %s".formatted(name, Math.max(1, worldDay), worldHour, worldMinute, when);
    }

    public String fallbackName() {
        if (reference.isAutosave()) {
            return "Autosave";
        }
        return playerName == null || playerName.isBlank() ? "Guardado" : playerName;
    }
}
