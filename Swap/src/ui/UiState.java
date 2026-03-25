package ui;

import state.GameMode;

public final class UiState {
    public GameMode mode = GameMode.TITLE;
    public String titleMessage = "Swap RPG";
    public String subtitleMessage = "Pulsa ENTER para empezar";
    public String dialogueSpeaker = "";
    public String[] dialogueLines = new String[0];
    public String toast = "";
    public int toastTicks;
    public boolean inventoryVisible;
}
