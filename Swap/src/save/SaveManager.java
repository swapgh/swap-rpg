package save;

import component.actor.NameComponent;
import component.actor.PlayerComponent;
import component.progression.EquipmentComponent;
import component.progression.ProgressionComponent;
import component.world.WorldTimeComponent;
import ecs.EcsWorld;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import data.DataRegistry;
import online.OnlineAccountService;
import online.PlayerProgressSnapshot;
import online.SyncOutcome;
import progression.DerivedStatsSnapshot;
import progression.ProgressionCalculator;
import save.store.SaveIndexStore;
import save.store.SaveMetadataStore;
import save.store.SaveProfilePaths;
import system.persistence.SaveLoadSystem;
import ui.text.UiText;

public final class SaveManager {
    private static final int SCHEMA_VERSION = 2;

    private final OnlineAccountService accountService;
    private final SaveProfilePaths paths;
    private final SaveIndexStore indexStore;
    private final SaveMetadataStore metadataStore;

    public SaveManager(OnlineAccountService accountService) {
        this.accountService = accountService;
        this.paths = new SaveProfilePaths(accountService);
        this.indexStore = new SaveIndexStore(paths, SCHEMA_VERSION);
        this.metadataStore = new SaveMetadataStore(paths);
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

    public SaveSlotMetadata saveAutosave(EcsWorld world, SaveLoadSystem serializer) {
        initializeProfile();
        SaveReference reference = SaveReference.autosave();
        saveWorld(serializer, world, reference);
        SaveSlotMetadata metadata = metadataFromWorld(world, reference, UiText.LABEL_AUTOSAVE, 0, 0, currentSyncState(), "", 0);
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
        SaveSlotMetadata metadata = metadataFromWorld(
                world,
                reference,
                displayName,
                previous == null ? 0 : previous.createdAtEpochSeconds(),
                previous == null ? 0 : previous.lastLoadedAtEpochSeconds(),
                previous == null ? currentSyncState() : previous.syncState(),
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
                normalizeName(newDisplayName, reference),
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
        initializeProfile();
        if (!accountService.isLoggedIn()) {
            RosterSyncResult result = new RosterSyncResult(0, 0, 0, List.of("No connected account."));
            writeRosterSyncReport(result);
            return result;
        }

        List<SaveSlotMetadata> saves = listManualSaves();
        int syncedCount = 0;
        int failedCount = 0;
        List<String> failures = new ArrayList<>();
        Set<String> remoteIds = new LinkedHashSet<>(accountService.remoteCharacterIds());
        Set<String> localCharacterIds = new LinkedHashSet<>();

        for (SaveSlotMetadata metadata : saves) {
            String localCharacterId = characterIdFor(metadata.reference());
            if (!localCharacterId.isBlank()) {
                localCharacterIds.add(localCharacterId);
            }
            SyncOutcome outcome = syncManualSave(metadata.reference(), metadata, data, remoteIds);
            if (outcome.ok()) {
                syncedCount++;
            } else {
                failedCount++;
                failures.add(metadata.displayName() + ": " + outcome.message());
            }
        }

        if (failedCount == 0) {
            SyncOutcome reconcileOutcome = accountService.reconcileRoster(localCharacterIds);
            if (!reconcileOutcome.ok()) {
                failedCount++;
                failures.add("Roster reconcile: " + reconcileOutcome.message());
            }
        }

        RosterSyncResult result = new RosterSyncResult(saves.size(), syncedCount, failedCount, List.copyOf(failures));
        writeRosterSyncReport(result);
        return result;
    }

    private void initializeProfile() {
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

    private SaveSlotMetadata metadataFromWorld(EcsWorld world, SaveReference reference, String displayName,
            long existingCreatedAt, long existingLastLoadedAt, String syncState, String remoteId, long lastSyncedAt) {
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
                syncState == null || syncState.isBlank() ? currentSyncState() : syncState,
                remoteId == null ? "" : remoteId,
                lastSyncedAt);
    }

    private String normalizeName(String displayName, SaveReference reference) {
        if (displayName == null || displayName.isBlank()) {
            return reference.isAutosave() ? UiText.LABEL_AUTOSAVE : UiText.LABEL_SAVE_FALLBACK;
        }
        return displayName.trim();
    }

    private String currentSyncState() {
        return accountService.isLoggedIn() ? "pending-web-sync" : "local-only";
    }

    private String newManualSlotId() {
        return "manual-" + Instant.now().getEpochSecond();
    }

    private SyncOutcome syncManualSave(SaveReference reference, SaveSlotMetadata metadata, DataRegistry data, Set<String> remoteIds) {
        Path path = savePath(reference);
        if (!Files.exists(path)) {
            storeSyncFailure(metadata, "missing-save-file");
            return SyncOutcome.failure("Missing save file.");
        }

        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
        } catch (IOException ex) {
            storeSyncFailure(metadata, "read-failed");
            return SyncOutcome.failure("Could not read save file.");
        }

        String characterId = properties.getProperty("progress.character_id", "").trim();
        if (characterId.isBlank()) {
            characterId = "legacy-" + reference.slotId();
        }
        String classId = properties.getProperty("progress.class_id", "warrior").trim();
        if (classId.isBlank()) {
            classId = "warrior";
        }
        int level = parseInt(properties.getProperty("progress.level"), 1);
        ProgressionComponent progression = new ProgressionComponent();
        progression.classId = classId;
        progression.level = level;
        progression.experience = parseInt(properties.getProperty("progress.experience"), 0);
        progression.attributePoints = parseInt(properties.getProperty("progress.attribute_points"), 0);
        progression.skillPoints = parseInt(properties.getProperty("progress.skill_points"), 0);
        progression.bonusSta = parseInt(properties.getProperty("progress.bonus_sta"), 0);
        progression.bonusStr = parseInt(properties.getProperty("progress.bonus_str"), 0);
        progression.bonusInt = parseInt(properties.getProperty("progress.bonus_int"), 0);
        progression.bonusAgi = parseInt(properties.getProperty("progress.bonus_agi"), 0);
        progression.bonusSpi = parseInt(properties.getProperty("progress.bonus_spi"), 0);
        progression.bonusWeaponPower = parseInt(properties.getProperty("progress.bonus_weapon_power"), 0);
        progression.bonusArmor = parseInt(properties.getProperty("progress.bonus_armor"), 0);
        EquipmentComponent equipment = new EquipmentComponent();
        equipment.weaponItemId = properties.getProperty("equipment.weapon", "");
        equipment.offhandItemId = properties.getProperty("equipment.offhand", "");
        equipment.armorItemId = properties.getProperty("equipment.armor", "");
        equipment.bootsItemId = properties.getProperty("equipment.boots", "");
        equipment.accessoryItemId = properties.getProperty("equipment.accessory", "");
        DerivedStatsSnapshot stats = ProgressionCalculator.snapshot(
                data.rpgClass(classId),
                data.progressionRules(),
                progression,
                equipment);

        PlayerProgressSnapshot snapshot = new PlayerProgressSnapshot(
                characterId,
                properties.getProperty("player.name", metadata.playerName()),
                classId,
                level,
                progression.masteryPoints,
                new PlayerProgressSnapshot.MasterySnapshot(
                        progression.masteryOffensePoints,
                        progression.masterySkillPoints,
                        progression.masteryDefensePoints),
                parseInt(properties.getProperty("player.hp"), stats.hp()),
                stats.hp(),
                parseInt(properties.getProperty("coins"), 0),
                parseInt(properties.getProperty("progress.enemies_killed"), 0),
                new PlayerProgressSnapshot.EquipmentSnapshot(
                        equipment.weaponItemId,
                        equipment.offhandItemId,
                        equipment.armorItemId,
                        equipment.bootsItemId,
                        equipment.accessoryItemId),
                new PlayerProgressSnapshot.AttributesSnapshot(
                        stats.attributes().sta(),
                        stats.attributes().str(),
                        stats.attributes().intel(),
                        stats.attributes().agi(),
                        stats.attributes().spi()),
                new PlayerProgressSnapshot.StatsSnapshot(
                        stats.mana(),
                        stats.attack(),
                        stats.dps(),
                        stats.abilityPower(),
                        stats.defense(),
                        stats.healingPower()),
                splitCsv(properties.getProperty("items", "")),
                splitCsv(properties.getProperty("quests.completed", "")));

        SyncOutcome outcome = accountService.sync(snapshot);
        if (!outcome.ok()) {
            storeSyncFailure(metadata, "sync-failed");
            return outcome;
        }

        remoteIds.addAll(accountService.remoteCharacterIds());
        if (!remoteIds.contains(characterId)) {
            storeSyncFailure(metadata, "remote-missing");
            return SyncOutcome.failure("Character was not confirmed in remote roster.");
        }

        metadataStore.store(new SaveSlotMetadata(
                metadata.reference(),
                metadata.displayName(),
                metadata.playerName(),
                metadata.createdAtEpochSeconds(),
                metadata.updatedAtEpochSeconds(),
                metadata.lastLoadedAtEpochSeconds(),
                metadata.worldDay(),
                metadata.worldHour(),
                metadata.worldMinute(),
                metadata.enemiesKilled(),
                "synced",
                characterId,
                Instant.now().getEpochSecond()));
        return outcome;
    }

    private void storeSyncFailure(SaveSlotMetadata metadata, String syncState) {
        metadataStore.store(new SaveSlotMetadata(
                metadata.reference(),
                metadata.displayName(),
                metadata.playerName(),
                metadata.createdAtEpochSeconds(),
                metadata.updatedAtEpochSeconds(),
                metadata.lastLoadedAtEpochSeconds(),
                metadata.worldDay(),
                metadata.worldHour(),
                metadata.worldMinute(),
                metadata.enemiesKilled(),
                syncState,
                metadata.remoteId(),
                metadata.lastSyncedAtEpochSeconds()));
    }

    private int parseInt(String value, int fallback) {
        try {
            return value == null || value.isBlank() ? fallback : Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String characterIdFor(SaveReference reference) {
        Path path = savePath(reference);
        if (!Files.exists(path)) {
            return "";
        }
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
        } catch (IOException ex) {
            return "";
        }
        String characterId = properties.getProperty("progress.character_id", "").trim();
        if (!characterId.isBlank()) {
            return characterId;
        }
        return "legacy-" + reference.slotId();
    }

    private List<String> splitCsv(String csv) {
        List<String> values = new ArrayList<>();
        if (csv == null || csv.isBlank()) {
            return values;
        }
        for (String raw : csv.split(",")) {
            String value = raw == null ? "" : raw.trim();
            if (!value.isBlank()) {
                values.add(value);
            }
        }
        return values;
    }

    private void writeRosterSyncReport(RosterSyncResult result) {
        Path reportPath = paths.profileRoot().resolve("last-roster-sync-report.txt");
        List<String> lines = new ArrayList<>();
        lines.add("swap-rpg roster sync report");
        lines.add("generated_at=" + Instant.now());
        lines.add("site_url=" + (accountService.siteUrl().isBlank() ? "not-logged-in" : accountService.siteUrl()));
        lines.add("profile_key=" + accountService.saveProfileKey());
        lines.add("found=" + result.found());
        lines.add("synced=" + result.synced());
        lines.add("failed=" + result.failed());
        if (result.failures().isEmpty()) {
            lines.add("failures=none");
        } else {
            for (int i = 0; i < result.failures().size(); i++) {
                lines.add("failure." + (i + 1) + "=" + result.failures().get(i));
            }
        }

        try {
            Files.write(reportPath, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo escribir el reporte de sync del roster", ex);
        }
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
