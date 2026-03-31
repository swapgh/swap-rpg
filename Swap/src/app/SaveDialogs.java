package app;

import javax.swing.JOptionPane;

public final class SaveDialogs {
    private SaveDialogs() {
    }

    public static String showManualSaveName(String initialValue) {
        Object input = JOptionPane.showInputDialog(
                null,
                "Nombre del guardado",
                "Guardar partida",
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
                "Borrar \"" + saveName + "\"?",
                "Borrar guardado",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
}
