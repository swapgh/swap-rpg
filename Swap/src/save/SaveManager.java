package save;

import ecs.EcsWorld;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import data.DataRegistry;
import online.auth.OnlineAccountService;
import save.metadata.SaveMetadataFactory;
import save.metadata.SaveSlotMetadata;
import save.roster.SaveRosterSyncService;
import save.store.SaveIndexStore;
import save.store.SaveMetadataStore;
import save.store.SaveProfilePaths;
import system.persistence.SaveLoadSystem;
import ui.text.UiText;

public final class SaveManager {
    private static final int SCHEMA_VERSION = 2;

    private final OnlineAccountService accountService;
    private final SaveMetadataFactory metadataFactory = new SaveMetadataFactory();
    private final SaveRosterSyncService rosterSyncService;
    private final SaveProfilePaths paths;
    private final SaveIndexStore indexStore;
    private final SaveMetadataStore metadataStore;

    public SaveManager(OnlineAccountService accountService) {
        this.accountService = accountService;
        this.paths = new SaveProfilePaths(accountService);
        this.indexStore = new SaveIndexStore(paths, SCHEMA_VERSION);
        this.metadataStore = new SaveMetadataStore(paths);
        this.rosterSyncService = new SaveRosterSyncService(accountService, this);
    }

    public Optional<SaveReference> selectContinueReference() {
        initializeProfile();
        SaveReference lastUsed = loadLastUsedReference().orElse(null);
        if (lastUsed != null && exists(lastUsed)) {
            return Optional.of(lastUsed);
        }

        List<SaveSlotMetadata> candidates = new ArrayList<>();
        latestAutosave().ifPresent(candidates::add);
        candidates.addAll(listManualSaves());
        candidates.sort(Comparator.comparingLong(SaveSlotMetadata::updatedAtEpochSeconds).reversed());
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(candidates.get(0).reference());
    }

    public Optional<SaveReference> loadLastUsedReference() {
        initializeProfile();
        return indexStore.loadLastUsedReference();
    }

    public List<SaveSlotMetadata> listManualSaves() {
        initializeProfile();
        return metadataStore.listManualSaves();
    }

    public Optional<SaveSlotMetadata> latestAutosave() {
        initializeProfile();
        return metadataStore.latestAutosave();
    }

    public Optional<SaveSlotMetadata> findMetadata(SaveReference reference) {
        initializeProfile();
        return metadataStore.find(reference);
    }

    public boolean hasAnySave() {
        return latestAutosave().isPresent() || !listManualSaves().isEmpty();
    }

    public boolean hasManualSaves() {
        return !listManualSaves().isEmpty();
    }

    public boolean exists(SaveReference reference) {
        initializeProfile();
        return Files.exists(savePath(reference));
    }

    public Path savePath(SaveReference reference) {
        return paths.savePath(reference);
    }

    public SaveProfilePaths paths() {
        return paths;
    }

    public void storeMetadata(SaveSlotMetadata metadata) {
        metadataStore.store(metadata);
    }

    public SaveSlotMetadata saveAutosave(EcsWorld world, SaveLoadSystem serializer) {
        initializeProfile();
        SaveReference reference = SaveReference.autosave();
        saveWorld(serializer, world, reference);
        SaveSlotMetadata metadata = metadataFactory.fromWorld(
                world,
                reference,
                UiText.LABEL_AUTOSAVE,
                0,
                0,
                currentSyncState(),
                currentSyncState(),
                "",
                0);
        metadataStore.store(metadata);
        return metadata;
    }

    public SaveSlotMetadata saveManual(EcsWorld world, SaveLoadSystem serializer, String existingSlotId, String displayName) {
        initializeProfile();
        SaveReference reference = existingSlotId == null || existingSlotId.isBlank()
                ? SaveReference.manual(newManualSlotId())
                : SaveReference.manual(existingSlotId);
        SaveSlotMetadata previous = findMetadata(reference).orElse(null);
        saveWorld(serializer, world, reference);
        SaveSlotMetadata metadata = metadataFactory.fromWorld(
                world,
                reference,
                displayName,
                previous == null ? 0 : previous.createdAtEpochSeconds(),
                previous == null ? 0 : previous.lastLoadedAtEpochSeconds(),
                previous == null ? currentSyncState() : previous.syncState(),
                currentSyncState(),
                previous == null ? "" : previous.remoteId(),
                previous == null ? 0 : previous.lastSyncedAtEpochSeconds());
        metadataStore.store(metadata);
        markLastUsed(reference);
        return metadata;
    }

    public void markLastUsed(SaveReference reference) {
        initializeProfile();
        findMetadata(reference).ifPresent(metadata -> {
            indexStore.markLastUsed(reference, metadata);
            metadataStore.store(indexStore.withLastLoadedTimestamp(metadata));
        });
    }

    public void deleteAutosaves() {
        initializeProfile();
        try {
            Files.deleteIfExists(savePath(SaveReference.autosave()));
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo borrar el autosave", ex);
        }
        metadataStore.delete(SaveReference.autosave());
        indexStore.clearIfAutosaveWasLastUsed();
    }

    public SaveSlotMetadata renameManualSave(String slotId, String newDisplayName) {
        initializeProfile();
        SaveReference reference = SaveReference.manual(slotId);
        SaveSlotMetadata metadata = findMetadata(reference)
                .orElseThrow(() -> new IllegalStateException("No existe ese save manual"));
        SaveSlotMetadata renamed = new SaveSlotMetadata(
                metadata.reference(),
                metadataFactory.normalizeName(newDisplayName, reference),
                metadata.playerName(),
                metadata.createdAtEpochSeconds(),
                metadata.updatedAtEpochSeconds(),
                metadata.lastLoadedAtEpochSeconds(),
                metadata.worldDay(),
                metadata.worldHour(),
                metadata.worldMinute(),
                metadata.enemiesKilled(),
                metadata.syncState(),
                metadata.remoteId(),
                metadata.lastSyncedAtEpochSeconds());
        metadataStore.store(renamed);
        return renamed;
    }

    public void deleteManualSave(String slotId) {
        initializeProfile();
        SaveReference reference = SaveReference.manual(slotId);
        try {
            Files.deleteIfExists(savePath(reference));
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo borrar el guardado manual", ex);
        }
        metadataStore.delete(reference);
        indexStore.clearManual(slotId);
    }

    public RosterSyncResult syncManualRoster(DataRegistry data) {
        return rosterSyncService.syncManualRoster(data);
    }

    public void initializeProfile() {
        paths.ensureDirectories();
        indexStore.ensureInitialized(accountService.saveProfileKey());
    }

    private void saveWorld(SaveLoadSystem serializer, EcsWorld world, SaveReference reference) {
        Path path = savePath(reference);
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo preparar el guardado", ex);
        }
        serializer.save(world, path);
    }

    private String currentSyncState() {
        return accountService.isLoggedIn() ? "pending-web-sync" : "local-only";
    }

    private String newManualSlotId() {
        return "manual-" + Instant.now().getEpochSecond();
    }

    public record RosterSyncResult(
            int found,
            int synced,
            int failed,
            List<String> failures) {
        public boolean anyProcessed() {
            return found > 0;
        }

        public String firstFailure() {
            return failures.isEmpty() ? "" : failures.get(0);
        }
    }
}
