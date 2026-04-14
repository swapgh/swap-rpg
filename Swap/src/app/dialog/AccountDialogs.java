package app.dialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import app.bootstrap.GameConfig;
import online.auth.AuthOutcome;
import online.auth.OnlineAccountService;
import ui.text.UiText;

public final class AccountDialogs {
    private AccountDialogs() {
    }

    public static String showLogin(OnlineAccountService accountService) {
        JComboBox<String> urlField = serverSelector(defaultUrl(accountService));
        JTextField identifierField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JCheckBox rememberSession = new JCheckBox(UiText.DIALOG_REMEMBER_SESSION, false);
        JPanel panel = formPanel(
                UiText.DIALOG_SWAP_WEB_URL, urlField,
                UiText.DIALOG_USERNAME_OR_EMAIL, identifierField,
                UiText.DIALOG_PASSWORD, passwordField,
                rememberSession);

        int result = JOptionPane.showConfirmDialog(null, panel, UiText.DIALOG_LOGIN_TITLE,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return UiText.LOGIN_CANCELLED;
        }

        AuthOutcome outcome = accountService.login(
                textValue(urlField),
                identifierField.getText(),
                new String(passwordField.getPassword()),
                rememberSession.isSelected());

        return outcome.ok() ? UiText.loginSuccess(accountService.displayLabel()) : outcome.error();
    }

    public static String showRegister(OnlineAccountService accountService) {
        JComboBox<String> urlField = serverSelector(defaultUrl(accountService));
        JTextField usernameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JCheckBox rememberSession = new JCheckBox(UiText.DIALOG_REMEMBER_SESSION, false);
        JPanel panel = formPanel(
                UiText.DIALOG_SWAP_WEB_URL, urlField,
                UiText.DIALOG_USERNAME, usernameField,
                UiText.DIALOG_EMAIL, emailField,
                UiText.DIALOG_PASSWORD, passwordField,
                rememberSession);

        int result = JOptionPane.showConfirmDialog(null, panel, UiText.DIALOG_REGISTER_TITLE,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return UiText.REGISTER_CANCELLED;
        }

        AuthOutcome outcome = accountService.register(
                textValue(urlField),
                usernameField.getText(),
                emailField.getText(),
                new String(passwordField.getPassword()),
                rememberSession.isSelected());

        return outcome.ok() ? UiText.registerSuccess(accountService.displayLabel()) : outcome.error();
    }

    private static JPanel formPanel(Object... parts) {
        int fieldPairs = parts.length;
        boolean hasCheckbox = fieldPairs > 0 && parts[fieldPairs - 1] instanceof JCheckBox;
        int rows = hasCheckbox ? (fieldPairs - 1) / 2 + 1 : fieldPairs / 2;
        JPanel panel = new JPanel(new GridLayout(rows, 2, 8, 8));
        int limit = hasCheckbox ? fieldPairs - 1 : fieldPairs;
        for (int i = 0; i < limit; i += 2) {
            panel.add(new JLabel(String.valueOf(parts[i])));
            panel.add((Component) parts[i + 1]);
        }
        if (hasCheckbox) {
            panel.add(new JLabel(""));
            panel.add((Component) parts[fieldPairs - 1]);
        }
        return panel;
    }

    private static JComboBox<String> serverSelector(String currentUrl) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement(GameConfig.SWAP_WEB_URL);
        model.addElement("localhost:8000");
        model.addElement("127.0.0.1:8000");

        JComboBox<String> combo = new JComboBox<>(model);
        combo.setEditable(true);
        combo.setSelectedItem(currentUrl);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, combo.getPreferredSize().height));
        combo.setToolTipText("Choose the server or type your own URL.");
        return combo;
    }

    private static String textValue(JComboBox<String> comboBox) {
        Object value = comboBox.isEditable() ? comboBox.getEditor().getItem() : comboBox.getSelectedItem();
        return value == null ? "" : value.toString();
    }

    private static String defaultUrl(OnlineAccountService accountService) {
        String active = accountService.siteUrl();
        return active == null || active.isBlank() ? GameConfig.SWAP_WEB_URL : active;
    }
}
