package ui.runtime;

import ui.text.UiText;

import state.GameMode;

public final class UiState {
    private static final int SYSTEM_LOG_SIZE = 10;

    public GameMode mode = GameMode.TITLE;
    public String titleMessage = UiText.GAME_TITLE;
    public String subtitleMessage = UiText.START_PROMPT;
    public String dialogueSpeaker = "";
    public String[] dialogueLines = new String[0];
    public String toast = "";
    public int toastTicks;
    public final String[] systemLog = new String[SYSTEM_LOG_SIZE];
    public final int[] systemLogAges = new int[SYSTEM_LOG_SIZE];
    public boolean systemLogExpanded;
    public String combatToast = "";
    public int combatToastTicks;
    public String contextHint = "";
    public boolean inventoryVisible;
    public int inventorySelectedIndex;
    public int shopNpcEntity = -1;
    public String shopSpeaker = "";
    public String shopStatusMessage = "";
    public int shopSelectedIndex;

    public void pushToast(String message, int ticks) {
        toast = message;
        toastTicks = ticks;
        for (int i = 0; i < SYSTEM_LOG_SIZE - 1; i++) {
            systemLog[i] = systemLog[i + 1];
            systemLogAges[i] = systemLogAges[i + 1];
        }
        systemLog[SYSTEM_LOG_SIZE - 1] = message;
        systemLogAges[SYSTEM_LOG_SIZE - 1] = 0;
    }

    public void tickSystemLog() {
        for (int i = 0; i < SYSTEM_LOG_SIZE; i++) {
            if (systemLog[i] != null && !systemLog[i].isBlank()) {
                systemLogAges[i]++;
            }
        }
    }

    public void clearSystemLog() {
        toast = "";
        toastTicks = 0;
        systemLogExpanded = false;
        for (int i = 0; i < SYSTEM_LOG_SIZE; i++) {
            systemLog[i] = null;
            systemLogAges[i] = 0;
        }
    }
}
