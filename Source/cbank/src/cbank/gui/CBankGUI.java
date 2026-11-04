package cbank.gui;

import cbank.BankSystem;
import cbank.CustomExceptions;
import cbank.account.Account;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CBankGUI extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final BankSystem bankSystem = BankSystem.getInstance();

    // UI Cards
    private final CardLayout cardLayout;
    private final JPanel mainContainer;

    // UI Panels
    private final LoginWindow loginWindow;
    private final DashBoardWindow dashBoardWindow;


    public CBankGUI() {
        setTitle("CBank Automated Banking Machine - SOEN 6611");
        setSize(550, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        loginWindow = new LoginWindow(this);
        dashBoardWindow = new DashBoardWindow(this);

        mainContainer.add(loginWindow.buildLoginPanel(), "LOGIN");
        mainContainer.add(dashBoardWindow.buildDashboardPanel(), "DASHBOARD");

        add(mainContainer);
        cardLayout.show(mainContainer, "LOGIN");
    }

    protected void attemptLogin(String accNum, String pin) throws CustomExceptions.AuthenticationException {
        bankSystem.login(accNum, pin);
        updateDashboardUI();
        cardLayout.show(mainContainer, "DASHBOARD");
    }

    protected void failedLogin(CustomExceptions.AuthenticationException ex){
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Authentication Error", JOptionPane.ERROR_MESSAGE);
    }

    protected void logout(){
        bankSystem.logout();
        loginWindow.resetWindow();
        cardLayout.show(mainContainer, "LOGIN");
    }

    private void updateDashboardUI() {
        Account user = bankSystem.getCurrentSessionUser();
        if (user != null) {
            String line1 = "Active User: " + user.getAccountName() + " (" + user.getAccountNumber() + ")";
            String line2, line3;
            if(user.isAdmin()) {
                line2 = String.format("ABM Physical Cash Vault: $%.2f CAD", bankSystem.getMachineCashVaultCad());
                line3 = "";
            }
            else{
                line2 = String.format("Account Balance: $%.2f CAD", user.getBalanceCad());
                line3 = String.format("Deposit Limit: $%.2f CAD| Withdraw Limit: $%.2f CAD", user.getDepositLimit(), user.getWithdrawLimit());
            }
            dashBoardWindow.setWindow(line1, line2, line3, user.isAdmin());
        }
    }

    protected void handleWithdrawal() {
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
                JOptionPane.showMessageDialog(this, "Invalid input entered: \"" + val + "\"" , "Error", JOptionPane.ERROR_MESSAGE);
            } catch (CustomExceptions.InvalidTransactionException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    protected void handleDeposit(){
        String val = JOptionPane.showInputDialog(this, "Enter CAD amount to deposit:");
        if (val != null) {
            try {
                double amount = Double.parseDouble(val);
                bankSystem.depositCash(amount, "CAD");
                updateDashboardUI();
                JOptionPane.showMessageDialog(this, "Deposit Successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input entered: \"" + val + "\"" , "Error", JOptionPane.ERROR_MESSAGE);
            } catch (CustomExceptions.InvalidTransactionException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    protected void handleForeignDeposit() {
        List<String> currencies = bankSystem.getConverter().getSupportedCurrenciesInfo();
        String selectedCurrency = (String) JOptionPane.showInputDialog(this, "Select Deposit Currency:",
                "Deposit", JOptionPane.QUESTION_MESSAGE, null, currencies.toArray(), currencies.get(0));

        if (selectedCurrency != null) {
            String getCurrency = bankSystem.getConverter().infoToCurrencies(selectedCurrency);
            String val = JOptionPane.showInputDialog(this, "Enter amount in " + getCurrency + ":");
            if (val != null) {
                try {
                    double amount = Double.parseDouble(val);
                    bankSystem.depositCash(amount, getCurrency);
                    updateDashboardUI();
                    JOptionPane.showMessageDialog(this, "Deposit processed and converted to CAD.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input entered: \"" + val + "\"" , "Error", JOptionPane.ERROR_MESSAGE);
                } catch (CustomExceptions.InvalidTransactionException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    protected void handleChangePin() {
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

    protected void handleAuditLog() {
        String logs = bankSystem.getAuditLogsFormatted();
        JTextArea textArea = new JTextArea(logs, 15, 40);
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "System Audit Logs", JOptionPane.INFORMATION_MESSAGE);
    }

    public void handleVaultAccess() {
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
    }
}