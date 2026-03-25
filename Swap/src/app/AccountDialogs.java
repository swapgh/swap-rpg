package app;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import online.AuthOutcome;
import online.OnlineAccountService;

public final class AccountDialogs {
    private AccountDialogs() {
    }

    public static String showLogin(OnlineAccountService accountService) {
        JTextField urlField = new JTextField(GameConfig.SWAP_WEB_URL);
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPanel panel = formPanel(
                "URL de Swap Web", urlField,
                "Correo", emailField,
                "Contrasena", passwordField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Iniciar sesion en Swap",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return "Login cancelado.";
        }

        AuthOutcome outcome = accountService.login(
                urlField.getText(),
                emailField.getText(),
                new String(passwordField.getPassword()));

        return outcome.ok() ? "Sesion iniciada como " + accountService.displayLabel() + "." : outcome.error();
    }

    public static String showRegister(OnlineAccountService accountService) {
        JTextField urlField = new JTextField(GameConfig.SWAP_WEB_URL);
        JTextField usernameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPanel panel = formPanel(
                "URL de Swap Web", urlField,
                "Usuario", usernameField,
                "Correo", emailField,
                "Contrasena", passwordField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Crear cuenta en Swap",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return "Registro cancelado.";
        }

        AuthOutcome outcome = accountService.register(
                urlField.getText(),
                usernameField.getText(),
                emailField.getText(),
                new String(passwordField.getPassword()));

        return outcome.ok() ? "Cuenta creada para " + accountService.displayLabel() + "." : outcome.error();
    }

    private static JPanel formPanel(Object... parts) {
        JPanel panel = new JPanel(new GridLayout(parts.length / 2, 2, 8, 8));
        for (int i = 0; i < parts.length; i += 2) {
            panel.add(new JLabel(String.valueOf(parts[i])));
            panel.add((java.awt.Component) parts[i + 1]);
        }
        return panel;
    }
}
