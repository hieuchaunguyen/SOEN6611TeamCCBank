package cbank.gui;

import cbank.BankSystem;
import cbank.CustomExceptions;

import javax.swing.*;
import java.awt.*;

public class LoginWindow extends AbstractCBankWindow {

    // Login Fields
    private JTextField accField;
    private JPasswordField pinField;

    public LoginWindow(CBankGUI mainWindow) {
        super(mainWindow);
    }

    public JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridLayout(10, 1, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("CBank Terminal Authentication"));

        accField = new JTextField();
        pinField = new JPasswordField();
        JButton loginBtn = new JButton("Login");

        panel.add(new JLabel(" Account Number:"));
        panel.add(accField);
        panel.add(new JLabel(""));
        panel.add(new JLabel(" 4-Digit PIN:"));
        panel.add(pinField);
        panel.add(loginBtn);
        panel.add(new JLabel(" (Default: 1001/1234 or Admin: 9999/0000)"));

        loginBtn.addActionListener(e -> {
            try {
                String accNum = accField.getText().trim();
                String pin = new String(pinField.getPassword());
                MAIN_WINDOW.attemptLogin(accNum, pin);
            } catch (CustomExceptions.AuthenticationException ex) {
                MAIN_WINDOW.failedLogin(ex);
            }
        });

        return panel;
    }

    public void resetWindow() {
        accField.setText("");
        pinField.setText("");
    }
}
