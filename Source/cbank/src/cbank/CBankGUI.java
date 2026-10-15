package cbank;

import javax.swing.*;
import java.awt.*;

public class CBankGUI extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final BankSystem bankSystem = BankSystem.getInstance();

    // UI Cards
    private CardLayout cardLayout;
    private JPanel mainContainer;

    // Login Fields
    private JTextField accField;
    private JPasswordField pinField;

    // Dashboard Components
    private JLabel welcomeLabel;
    private JLabel balanceLabel;
    private JLabel vaultLabel;
    private JButton adminLogBtn;
    private JButton adminVaultBtn;

    public CBankGUI() {
        setTitle("CBank Automated Banking Machine - SOEN 6611");
        setSize(550, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        mainContainer.add(buildLoginPanel(), "LOGIN");
        mainContainer.add(buildDashboardPanel(), "DASHBOARD");

        add(mainContainer);
        cardLayout.show(mainContainer, "LOGIN");
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("CBank Terminal Authentication"));

        accField = new JTextField();
        pinField = new JPasswordField();
        JButton loginBtn = new JButton("Login");

        panel.add(new JLabel(" Account Number:"));
        panel.add(accField);
        panel.add(new JLabel(" 4-Digit PIN:"));
        panel.add(pinField);
        panel.add(new JLabel(" (Default: 1001/1234 or Admin: 9999/0000)"));
        panel.add(loginBtn);

        loginBtn.addActionListener(e -> {
            try {
                String accNum = accField.getText().trim();
                String pin = new String(pinField.getPassword());
                bankSystem.login(accNum, pin);
                updateDashboardUI();
                cardLayout.show(mainContainer, "DASHBOARD");
            } catch (CustomExceptions.AuthenticationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Authentication Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(3, 1));
        welcomeLabel = new JLabel("Welcome!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        balanceLabel = new JLabel("Balance: $0.00 CAD", SwingConstants.CENTER);
        vaultLabel = new JLabel("Vault: $0.00 CAD", SwingConstants.CENTER);

        headerPanel.add(welcomeLabel);
        headerPanel.add(balanceLabel);
        headerPanel.add(vaultLabel);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Actions Grid
        JPanel actionsPanel = new JPanel(new GridLayout(4, 2, 8, 8));

        JButton withdrawBtn = new JButton("Withdraw Cash (CAD)");
        JButton depositBtn = new JButton("Deposit Cash (Multi-Currency)");
        JButton changePinBtn = new JButton("Change PIN");
        JButton logoutBtn = new JButton("Logout / End Session");

        adminLogBtn = new JButton("[Admin] View Audit Logs");
        adminVaultBtn = new JButton("[Admin] Replenish Vault");

        actionsPanel.add(withdrawBtn);
        actionsPanel.add(depositBtn);
        actionsPanel.add(changePinBtn);
        actionsPanel.add(logoutBtn);
        actionsPanel.add(adminLogBtn);
        actionsPanel.add(adminVaultBtn);

        panel.add(actionsPanel, BorderLayout.CENTER);

        // Action Handlers
        withdrawBtn.addActionListener(e -> handleWithdrawal());
        depositBtn.addActionListener(e -> handleDeposit());
        changePinBtn.addActionListener(e -> handleChangePin());
        logoutBtn.addActionListener(e -> {
            bankSystem.logout();
            accField.setText("");
            pinField.setText("");
            cardLayout.show(mainContainer, "LOGIN");
        });

        adminLogBtn.addActionListener(e -> {
            String logs = bankSystem.getAuditLogsFormatted();
            JTextArea textArea = new JTextArea(logs, 15, 40);
            textArea.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "System Audit Logs", JOptionPane.INFORMATION_MESSAGE);
        });

        adminVaultBtn.addActionListener(e -> {
            String val = JOptionPane.showInputDialog(this, "Enter CAD amount to add to machine vault:");
            if (val != null) {
                try {
                    double amt = Double.parseDouble(val);
                    bankSystem.adminReplenishVault(amt);
                    updateDashboardUI();
                    JOptionPane.showMessageDialog(this, "Vault replenished successfully.");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid cash amount.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }

    private void updateDashboardUI() {
        Account user = bankSystem.getCurrentSessionUser();
        if (user != null) {
            welcomeLabel.setText("Active User: " + user.getAccountName() + " (" + user.getAccountNumber() + ")");
            balanceLabel.setText(String.format("Account Balance: $%.2f CAD", user.getBalanceCad()));
            vaultLabel.setText(String.format("ABM Physical Cash Vault: $%.2f CAD", bankSystem.getMachineCashVaultCad()));

            adminLogBtn.setEnabled(user.isAdmin());
            adminVaultBtn.setEnabled(user.isAdmin());
        }
    }

    private void handleWithdrawal() {
        String val = JOptionPane.showInputDialog(this, "Enter CAD amount to withdraw:");
        if (val != null) {
            try {
                double amount = Double.parseDouble(val);
                bankSystem.withdrawCash(amount);
                updateDashboardUI();
                JOptionPane.showMessageDialog(this, "Please collect your cash.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (CustomExceptions.InsufficientFundsException | CustomExceptions.VaultDepletedException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Transaction Denied", JOptionPane.WARNING_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number entered.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeposit() {
        String[] currencies = bankSystem.getConverter().getSupportedCurrencies();
        String selectedCurrency = (String) JOptionPane.showInputDialog(this, "Select Deposit Currency:",
                "Deposit", JOptionPane.QUESTION_MESSAGE, null, currencies, currencies[0]);

        if (selectedCurrency != null) {
            String val = JOptionPane.showInputDialog(this, "Enter amount in " + selectedCurrency + ":");
            if (val != null) {
                try {
                    double amount = Double.parseDouble(val);
                    bankSystem.depositCash(amount, selectedCurrency);
                    updateDashboardUI();
                    JOptionPane.showMessageDialog(this, "Deposit processed and converted to CAD.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid number entered.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void handleChangePin() {
        String newPin = JOptionPane.showInputDialog(this, "Enter new 4-Digit PIN:");
        if (newPin != null) {
            try {
                bankSystem.changePin(newPin);
                JOptionPane.showMessageDialog(this, "PIN updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (CustomExceptions.InvalidPinException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CBankGUI().setVisible(true));
    }
}