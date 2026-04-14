package save.metadata;

import component.character.NameComponent;
import component.character.PlayerComponent;
import component.progression.ProgressionComponent;
import component.world.WorldTimeComponent;
import ecs.EcsWorld;
import java.time.Instant;
import java.util.List;
import save.SaveReference;
import ui.text.UiText;

public final class SaveMetadataFactory {
    public SaveSlotMetadata fromWorld(EcsWorld world, SaveReference reference, String displayName,
            long existingCreatedAt, long existingLastLoadedAt, String syncState, String fallbackSyncState,
            String remoteId, long lastSyncedAt) {
        long now = Instant.now().getEpochSecond();
        int player = world.entitiesWith(PlayerComponent.class).get(0);
        String playerName = world.require(player, NameComponent.class).value;
        int worldDay = 1;
        int worldHour = 0;
        int worldMinute = 0;
        List<Integer> timeEntities = world.entitiesWith(WorldTimeComponent.class);
        if (!timeEntities.isEmpty()) {
            WorldTimeComponent time = world.require(timeEntities.get(0), WorldTimeComponent.class);
            worldDay = time.dayNumber();
            worldHour = time.hour();
            worldMinute = time.minute();
        }
        int enemiesKilled = world.require(player, ProgressionComponent.class).enemiesKilled;
        return new SaveSlotMetadata(
                reference,
                normalizeName(displayName, reference),
                playerName,
                existingCreatedAt > 0 ? existingCreatedAt : now,
                now,
                existingLastLoadedAt,
                worldDay,
                worldHour,
                worldMinute,
                enemiesKilled,
                syncState == null || syncState.isBlank() ? fallbackSyncState : syncState,
                remoteId == null ? "" : remoteId,
                lastSyncedAt);
    }

    public String defaultSyncState(boolean loggedIn) {
        return loggedIn ? "pending-web-sync" : "local-only";
    }

    public String normalizeName(String displayName, SaveReference reference) {
        if (displayName == null || displayName.isBlank()) {
            return reference.isAutosave() ? UiText.LABEL_AUTOSAVE : UiText.LABEL_SAVE_FALLBACK;
        }
        return displayName.trim();
    }

}
