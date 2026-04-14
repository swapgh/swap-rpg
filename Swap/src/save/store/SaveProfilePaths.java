package save.store;

import app.bootstrap.GameConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import online.auth.OnlineAccountService;
import save.SaveReference;

public final class SaveProfilePaths {
    private final OnlineAccountService accountService;

    public SaveProfilePaths(OnlineAccountService accountService) {
        this.accountService = accountService;
    }

    public void ensureDirectories() {
        try {
            Files.createDirectories(profileRoot());
            Files.createDirectories(manualDirectory());
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo preparar el directorio de saves", ex);
        }
    }

    public Path savePath(SaveReference reference) {
        return switch (reference.kind()) {
        case AUTO -> profileRoot().resolve("autosave.properties");
        case MANUAL -> manualDirectory().resolve(reference.slotId() + ".properties");
        };
    }

    public Path metadataPath(SaveReference reference) {
        return switch (reference.kind()) {
        case AUTO -> autosaveMetadataPath();
        case MANUAL -> manualDirectory().resolve(reference.slotId() + ".meta.properties");
        };
    }

    public Path autosaveMetadataPath() {
        return profileRoot().resolve("autosave.meta.properties");
    }

    public Path manualDirectory() {
        return profileRoot().resolve("manual");
    }

    public Path indexPath() {
        return profileRoot().resolve("index.properties");
    }

    public Path profileRoot() {
        return GameConfig.SAVE_ROOT_DIR.resolve(accountService.saveProfileKey());
    }
}
