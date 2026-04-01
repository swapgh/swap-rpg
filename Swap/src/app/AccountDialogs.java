package app;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import online.AuthOutcome;
import online.OnlineAccountService;
import ui.text.UiText;

public final class AccountDialogs {
    private AccountDialogs() {
    }

    public static String showLogin(OnlineAccountService accountService) {
        JTextField urlField = new JTextField(defaultUrl(accountService));
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPanel panel = formPanel(
                UiText.DIALOG_SWAP_WEB_URL, urlField,
                UiText.DIALOG_EMAIL, emailField,
                UiText.DIALOG_PASSWORD, passwordField);

        int result = JOptionPane.showConfirmDialog(null, panel, UiText.DIALOG_LOGIN_TITLE,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return UiText.LOGIN_CANCELLED;
        }

        AuthOutcome outcome = accountService.login(
                urlField.getText(),
                emailField.getText(),
                new String(passwordField.getPassword()));

        return outcome.ok() ? UiText.loginSuccess(accountService.displayLabel()) : outcome.error();
    }

    public static String showRegister(OnlineAccountService accountService) {
        JTextField urlField = new JTextField(defaultUrl(accountService));
        JTextField usernameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPanel panel = formPanel(
                UiText.DIALOG_SWAP_WEB_URL, urlField,
                UiText.DIALOG_USERNAME, usernameField,
                UiText.DIALOG_EMAIL, emailField,
                UiText.DIALOG_PASSWORD, passwordField);

        int result = JOptionPane.showConfirmDialog(null, panel, UiText.DIALOG_REGISTER_TITLE,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return UiText.REGISTER_CANCELLED;
        }

        AuthOutcome outcome = accountService.register(
                urlField.getText(),
                usernameField.getText(),
                emailField.getText(),
                new String(passwordField.getPassword()));

        return outcome.ok() ? UiText.registerSuccess(accountService.displayLabel()) : outcome.error();
    }

    private static JPanel formPanel(Object... parts) {
        JPanel panel = new JPanel(new GridLayout(parts.length / 2, 2, 8, 8));
        for (int i = 0; i < parts.length; i += 2) {
            panel.add(new JLabel(String.valueOf(parts[i])));
            panel.add((java.awt.Component) parts[i + 1]);
        }
        return panel;
    }

    private static String defaultUrl(OnlineAccountService accountService) {
        String active = accountService.siteUrl();
        return active == null || active.isBlank() ? GameConfig.SWAP_WEB_URL : active;
    }
}
