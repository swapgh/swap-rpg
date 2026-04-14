package scene.gameplay.world;

import component.character.PlayerComponent;
import component.progression.ProgressionComponent;
import data.DataRegistry;
import ecs.EcsWorld;
import online.auth.OnlineAccountService;
import online.sync.PlayerProgressSnapshot;
import online.sync.PlayerProgressSnapshotFactory;
import online.sync.SyncOutcome;
import ui.state.UiState;

public final class WorldProgressSyncController {
    private static final double AUTO_SYNC_START_DELAY_SECONDS = 0.0;
    private static final double AUTO_SYNC_SUCCESS_COOLDOWN_SECONDS = 10.0;
    private static final double AUTO_SYNC_FAILURE_COOLDOWN_SECONDS = 30.0;
    private static final double LOGIN_REMINDER_COOLDOWN_SECONDS = 10.0;

    private final OnlineAccountService accountService;
    private final DataRegistry data;
    private final UiState ui;

    private double autoSyncCooldown = AUTO_SYNC_START_DELAY_SECONDS;
    private double loginReminderCooldown;

    public WorldProgressSyncController(OnlineAccountService accountService, DataRegistry data, UiState ui) {
        this.accountService = accountService;
        this.data = data;
        this.ui = ui;
    }

    public void reset() {
        autoSyncCooldown = AUTO_SYNC_START_DELAY_SECONDS;
        loginReminderCooldown = 0.0;
    }

    public void syncInitialAccountState(EcsWorld world) {
        if (!accountService.isLoggedIn()) {
            return;
        }
        syncProgress(world, true, true);
    }

    public void update(EcsWorld world, double dtSeconds, boolean manualSyncRequested) {
        autoSyncCooldown = Math.max(0, autoSyncCooldown - dtSeconds);
        loginReminderCooldown = Math.max(0, loginReminderCooldown - dtSeconds);

        if (manualSyncRequested) {
            syncProgress(world, false, true);
            return;
        }

        if (!hasProgressionDirtySync(world)) {
            return;
        }

        if (accountService.isLoggedIn() && autoSyncCooldown <= 0) {
            syncProgress(world, false, false);
        } else if (!accountService.isLoggedIn() && loginReminderCooldown <= 0) {
            loginReminderCooldown = LOGIN_REMINDER_COOLDOWN_SECONDS;
        }
    }

    private void syncProgress(EcsWorld world, boolean silent, boolean manual) {
        PlayerProgressSnapshot snapshot = PlayerProgressSnapshotFactory.fromWorld(world, data);
        SyncOutcome outcome = accountService.sync(snapshot);
        if (outcome.ok()) {
            int player = world.entitiesWith(PlayerComponent.class).get(0);
            world.require(player, ProgressionComponent.class).dirtySync = false;
            autoSyncCooldown = AUTO_SYNC_SUCCESS_COOLDOWN_SECONDS;
        } else if (!manual) {
            autoSyncCooldown = AUTO_SYNC_FAILURE_COOLDOWN_SECONDS;
        }
        if (!silent || !outcome.ok()) {
            ui.pushToast(outcome.message(), 180);
        }
    }

    private boolean hasProgressionDirtySync(EcsWorld world) {
        var players = world.entitiesWith(PlayerComponent.class, ProgressionComponent.class);
        if (players.isEmpty()) {
            return false;
        }
        return world.require(players.get(0), ProgressionComponent.class).dirtySync;
    }
}
