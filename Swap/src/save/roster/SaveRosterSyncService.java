package save.roster;

import component.progression.EquipmentComponent;
import component.progression.ProgressionComponent;
import data.DataRegistry;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import online.auth.OnlineAccountService;
import online.sync.PlayerProgressSnapshot;
import online.sync.SyncOutcome;
import progression.DerivedStatsSnapshot;
import progression.ProgressionCalculator;
import save.SaveManager;
import save.SaveReference;
import save.metadata.SaveSlotMetadata;

public final class SaveRosterSyncService {
    private final OnlineAccountService accountService;
    private final SaveManager saveManager;

    public SaveRosterSyncService(OnlineAccountService accountService, SaveManager saveManager) {
        this.accountService = accountService;
        this.saveManager = saveManager;
    }

    public SaveManager.RosterSyncResult syncManualRoster(DataRegistry data) {
        saveManager.initializeProfile();
        if (!accountService.isLoggedIn()) {
            SaveManager.RosterSyncResult result = new SaveManager.RosterSyncResult(0, 0, 0, List.of("No connected account."));
            writeRosterSyncReport(result);
            return result;
        }

        List<SaveSlotMetadata> saves = saveManager.listManualSaves();
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

        SaveManager.RosterSyncResult result = new SaveManager.RosterSyncResult(saves.size(), syncedCount, failedCount, List.copyOf(failures));
        writeRosterSyncReport(result);
        return result;
    }

    private SyncOutcome syncManualSave(SaveReference reference, SaveSlotMetadata metadata, DataRegistry data, Set<String> remoteIds) {
        Path path = saveManager.savePath(reference);
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

        saveManager.storeMetadata(new SaveSlotMetadata(
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
        saveManager.storeMetadata(new SaveSlotMetadata(
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
        Path path = saveManager.savePath(reference);
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

    private void writeRosterSyncReport(SaveManager.RosterSyncResult result) {
        Path reportPath = saveManager.paths().profileRoot().resolve("last-roster-sync-report.txt");
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
            Files.createDirectories(reportPath.getParent());
            Files.write(reportPath, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo escribir el reporte de sync del roster", ex);
        }
    }
}
