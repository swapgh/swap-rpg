package app.dialog;

import javax.swing.JOptionPane;

import ui.text.UiText;

public final class SaveDialogs {
    private SaveDialogs() {
    }

    public static String showManualSaveName(String initialValue) {
        Object input = JOptionPane.showInputDialog(
                null,
                UiText.DIALOG_SAVE_NAME,
                UiText.DIALOG_SAVE_GAME,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                initialValue);
        if (input == null) {
            return null;
        }
        String value = String.valueOf(input).trim();
        return value.isBlank() ? null : value;
    }

    public static boolean confirmDeleteSave(String saveName) {
        int result = JOptionPane.showConfirmDialog(
                null,
                UiText.confirmDeleteSave(saveName),
                UiText.DIALOG_DELETE_SAVE,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
}
