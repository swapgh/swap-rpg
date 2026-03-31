package save;

public record SaveReference(SaveKind kind, String slotId) {
    public static final String AUTO_SLOT_ID = "latest";

    public SaveReference {
        if (kind == null) {
            throw new IllegalArgumentException("kind");
        }
        if (slotId == null || slotId.isBlank()) {
            throw new IllegalArgumentException("slotId");
        }
    }

    public static SaveReference autosave() {
        return new SaveReference(SaveKind.AUTO, AUTO_SLOT_ID);
    }

    public static SaveReference manual(String slotId) {
        return new SaveReference(SaveKind.MANUAL, slotId);
    }

    public boolean isAutosave() {
        return kind == SaveKind.AUTO;
    }

    public boolean isManual() {
        return kind == SaveKind.MANUAL;
    }

    public String stableId() {
        return kind.name() + ":" + slotId;
    }
}
