package save.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Optional;
import java.util.Properties;
import save.SaveKind;
import save.SaveReference;
import save.metadata.SaveSlotMetadata;

public final class SaveIndexStore {
    private final SaveProfilePaths paths;
    private final int schemaVersion;

    public SaveIndexStore(SaveProfilePaths paths, int schemaVersion) {
        this.paths = paths;
        this.schemaVersion = schemaVersion;
    }

    public void ensureInitialized(String remoteProfileId) {
        Properties index = load();
        boolean changed = false;
        if (!Files.exists(paths.indexPath())) {
            changed = true;
        }
        if (!Integer.toString(schemaVersion).equals(index.getProperty("schema.version"))) {
            index.setProperty("schema.version", Integer.toString(schemaVersion));
            changed = true;
        }
        if (!index.containsKey("sync.remote_profile_id")) {
            index.setProperty("sync.remote_profile_id", remoteProfileId == null ? "" : remoteProfileId);
            changed = true;
        }
        if (!index.containsKey("sync.cursor")) {
            index.setProperty("sync.cursor", "");
            changed = true;
        }
        if (!index.containsKey("sync.last_uploaded_epoch_seconds")) {
            index.setProperty("sync.last_uploaded_epoch_seconds", "0");
            changed = true;
        }
        if (changed) {
            store(index);
        }
    }

    public Optional<SaveReference> loadLastUsedReference() {
        Properties index = load();
        String kindValue = index.getProperty("last_used.kind", "");
        String slotId = index.getProperty("last_used.slot_id", "");
        if (kindValue.isBlank() || slotId.isBlank()) {
            return Optional.empty();
        }
        try {
            SaveKind kind = SaveKind.valueOf(kindValue);
            return Optional.of(new SaveReference(kind, slotId));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public void markLastUsed(SaveReference reference, SaveSlotMetadata metadata) {
        Properties index = load();
        index.setProperty("last_used.kind", reference.kind().name());
        index.setProperty("last_used.slot_id", reference.slotId());
        if (reference.isManual()) {
            index.setProperty("last_manual.slot_id", reference.slotId());
        }
        store(index);
    }

    public void clearIfAutosaveWasLastUsed() {
        Properties index = load();
        if (SaveKind.AUTO.name().equals(index.getProperty("last_used.kind"))) {
            index.remove("last_used.kind");
            index.remove("last_used.slot_id");
            store(index);
        }
    }

    public void clearManual(String slotId) {
        Properties index = load();
        boolean changed = false;
        if (SaveKind.MANUAL.name().equals(index.getProperty("last_used.kind"))
                && slotId.equals(index.getProperty("last_used.slot_id"))) {
            index.remove("last_used.kind");
            index.remove("last_used.slot_id");
            changed = true;
        }
        if (slotId.equals(index.getProperty("last_manual.slot_id"))) {
            index.remove("last_manual.slot_id");
            changed = true;
        }
        if (changed) {
            store(index);
        }
    }

    public SaveSlotMetadata withLastLoadedTimestamp(SaveSlotMetadata metadata) {
        return new SaveSlotMetadata(
                metadata.reference(),
                metadata.displayName(),
                metadata.playerName(),
                metadata.createdAtEpochSeconds(),
                metadata.updatedAtEpochSeconds(),
                Instant.now().getEpochSecond(),
                metadata.worldDay(),
                metadata.worldHour(),
                metadata.worldMinute(),
                metadata.enemiesKilled(),
                metadata.syncState(),
                metadata.remoteId(),
                metadata.lastSyncedAtEpochSeconds());
    }

    private Properties load() {
        if (!Files.exists(paths.indexPath())) {
            return new Properties();
        }
        return readProperties(paths.indexPath());
    }

    private void store(Properties properties) {
        try (OutputStream out = Files.newOutputStream(paths.indexPath())) {
            properties.store(out, "swap-rpg save index");
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo guardar el indice de saves", ex);
        }
    }

    private Properties readProperties(java.nio.file.Path path) {
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo leer " + path, ex);
        }
        return properties;
    }
}
