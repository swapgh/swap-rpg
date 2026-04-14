package scene.menu;

import java.util.Optional;

import data.DataRegistry;
import online.auth.OnlineAccountService;
import save.SaveManager;
import ui.text.UiText;

final class TitleRosterSyncController {
    private boolean attempted;

    Optional<String> syncIfNeeded(OnlineAccountService accountService, SaveManager saveManager, DataRegistry data) {
        if (attempted || !accountService.isLoggedIn()) {
            return Optional.empty();
        }
        attempted = true;
        SaveManager.RosterSyncResult result = saveManager.syncManualRoster(data);
        if (!result.anyProcessed()) {
            return Optional.empty();
        }
        return Optional.of(result.failed() > 0 && !result.firstFailure().isBlank()
                ? UiText.rosterSyncSummary(result.found(), result.synced(), result.failed()) + " | " + result.firstFailure()
                : UiText.rosterSyncSummary(result.found(), result.synced(), result.failed()));
    }
}
