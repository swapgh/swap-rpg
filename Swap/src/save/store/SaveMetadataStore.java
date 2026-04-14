package save.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import save.SaveKind;
import save.SaveReference;
import save.metadata.SaveSlotMetadata;

public final class SaveMetadataStore {
    private final SaveProfilePaths paths;

    public SaveMetadataStore(SaveProfilePaths paths) {
        this.paths = paths;
    }

    public List<SaveSlotMetadata> listManualSaves() {
        List<SaveSlotMetadata> saves = new ArrayList<>();
        try {
            if (!Files.isDirectory(paths.manualDirectory())) {
                return List.of();
            }
            try (var stream = Files.list(paths.manualDirectory())) {
                stream.filter(path -> path.getFileName().toString().endsWith(".meta.properties"))
                        .forEach(path -> load(path).ifPresent(saves::add));
            }
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudieron listar guardados manuales", ex);
        }
        saves.sort(Comparator.comparingLong(SaveSlotMetadata::updatedAtEpochSeconds).reversed());
        return saves;
    }

    public Optional<SaveSlotMetadata> latestAutosave() {
        return load(paths.autosaveMetadataPath());
    }

    public Optional<SaveSlotMetadata> find(SaveReference reference) {
        return load(paths.metadataPath(reference));
    }

    public void store(SaveSlotMetadata metadata) {
        Properties properties = new Properties();
        properties.setProperty("kind", metadata.reference().kind().name());
        properties.setProperty("slot_id", metadata.reference().slotId());
        properties.setProperty("display_name", metadata.displayName());
        properties.setProperty("player_name", metadata.playerName());
        properties.setProperty("created_at_epoch_seconds", Long.toString(metadata.createdAtEpochSeconds()));
        properties.setProperty("updated_at_epoch_seconds", Long.toString(metadata.updatedAtEpochSeconds()));
        properties.setProperty("last_loaded_at_epoch_seconds", Long.toString(metadata.lastLoadedAtEpochSeconds()));
        properties.setProperty("world.day", Integer.toString(metadata.worldDay()));
        properties.setProperty("world.hour", Integer.toString(metadata.worldHour()));
        properties.setProperty("world.minute", Integer.toString(metadata.worldMinute()));
        properties.setProperty("progress.enemies_killed", Integer.toString(metadata.enemiesKilled()));
        properties.setProperty("sync.state", metadata.syncState());
        properties.setProperty("sync.remote_id", metadata.remoteId());
        properties.setProperty("sync.last_synced_at_epoch_seconds", Long.toString(metadata.lastSyncedAtEpochSeconds()));
        try (OutputStream out = Files.newOutputStream(paths.metadataPath(metadata.reference()))) {
            properties.store(out, "swap-rpg save metadata");
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo guardar metadata del save", ex);
        }
    }

    public void delete(SaveReference reference) {
        try {
            Files.deleteIfExists(paths.metadataPath(reference));
        } catch (IOException ex) {
            String label = reference.kind() == SaveKind.AUTO ? "autosave" : "guardado manual";
            throw new IllegalStateException("No se pudo borrar metadata de " + label, ex);
        }
    }

    private Optional<SaveSlotMetadata> load(java.nio.file.Path metaPath) {
        if (!Files.exists(metaPath)) {
            return Optional.empty();
        }
        Properties properties = readProperties(metaPath);
        try {
            SaveReference reference = new SaveReference(
                    SaveKind.valueOf(properties.getProperty("kind", SaveKind.MANUAL.name())),
                    properties.getProperty("slot_id", ""));
            return Optional.of(new SaveSlotMetadata(
                    reference,
                    properties.getProperty("display_name", ""),
                    properties.getProperty("player_name", ""),
                    Long.parseLong(properties.getProperty("created_at_epoch_seconds", "0")),
                    Long.parseLong(properties.getProperty("updated_at_epoch_seconds", "0")),
                    Long.parseLong(properties.getProperty("last_loaded_at_epoch_seconds", "0")),
                    Integer.parseInt(properties.getProperty("world.day", "1")),
                    Integer.parseInt(properties.getProperty("world.hour", "0")),
                    Integer.parseInt(properties.getProperty("world.minute", "0")),
                    Integer.parseInt(properties.getProperty("progress.enemies_killed", "0")),
                    properties.getProperty("sync.state", "local-only"),
                    properties.getProperty("sync.remote_id", ""),
                    Long.parseLong(properties.getProperty("sync.last_synced_at_epoch_seconds", "0"))));
        } catch (RuntimeException ex) {
            return Optional.empty();
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
