package cbank;

import java.util.HashMap;
import java.util.Map;

public class BankSystem {
    private static BankSystem instance;

    private final Map<String, Account> accounts = new HashMap<>();
    private final AuditLogger logger = new AuditLogger();
    private final CurrencyConverter converter = new CurrencyConverter();

    private double machineCashVaultCad = 5000.00; // Physical cash inside ABM
    private Account currentSessionUser = null;

    private BankSystem() {
        // Sample accounts
        accounts.put("1001", new Account("1001", "Alice Smith", "1234", 1500.00, false));
        accounts.put("1002", new Account("1002", "Bob Jones", "5678", 500.00, false));
        // Admin account
        accounts.put("9999", new Account("9999", "System Admin", "0000", 0.00, true));
    }

    public static synchronized BankSystem getInstance() {
        if (instance == null) instance = new BankSystem();
        return instance;
    }

    public Account login(String accNum, String pin) throws CustomExceptions.AuthenticationException {
        Account acc = accounts.get(accNum);
        if (acc != null && acc.validatePin(pin)) {
            currentSessionUser = acc;
            logger.log(accNum, "LOGIN", 0.0, "SUCCESS");
            return acc;
        }
        logger.log(accNum != null ? accNum : "UNKNOWN", "LOGIN", 0.0, "FAILED");
        throw new CustomExceptions.AuthenticationException("Invalid Account Number or PIN.");
    }

    public void logout() {
        if (currentSessionUser != null) {
            logger.log(currentSessionUser.getAccountNumber(), "LOGOUT", 0.0, "SUCCESS");
            currentSessionUser = null;
        }
    }

    public Account getCurrentSessionUser() { return currentSessionUser; }
    public double getMachineCashVaultCad() { return machineCashVaultCad; }
    public CurrencyConverter getConverter() { return converter; }

    public void withdrawCash(double amountCad) throws CustomExceptions.InsufficientFundsException, CustomExceptions.VaultDepletedException {
        if (amountCad > machineCashVaultCad) {
            logger.log(currentSessionUser.getAccountNumber(), "WITHDRAWAL", amountCad, "FAILED_VAULT_LIMIT");
            throw new CustomExceptions.VaultDepletedException("ABM has insufficient physical cash reserves.");
        }
        currentSessionUser.withdraw(amountCad);
        machineCashVaultCad -= amountCad;
        logger.log(currentSessionUser.getAccountNumber(), "WITHDRAWAL", amountCad, "SUCCESS");
    }

    public void depositCash(double amount, String currency) {
        double convertedCad = converter.convertToCad(amount, currency);
        currentSessionUser.deposit(convertedCad);
        if ("CAD".equalsIgnoreCase(currency)) {
            machineCashVaultCad += amount;
        }
        logger.log(currentSessionUser.getAccountNumber(), "DEPOSIT (" + currency + ")", convertedCad, "SUCCESS");
    }

    public void changePin(String newPin) throws CustomExceptions.InvalidPinException {
        if (newPin == null || !newPin.matches("\\d{4}")) {
            throw new CustomExceptions.InvalidPinException("PIN must be exactly 4 digits.");
        }
        currentSessionUser.setPin(newPin);
        logger.log(currentSessionUser.getAccountNumber(), "PIN_CHANGE", 0.0, "SUCCESS");
    }

    // Admin Operations
    public void adminReplenishVault(double amountCad) {
        if (!currentSessionUser.isAdmin()) throw new SecurityException("Admin access required.");
        machineCashVaultCad += amountCad;
        logger.log(currentSessionUser.getAccountNumber(), "ADMIN_VAULT_REPLENISH", amountCad, "SUCCESS");
    }

    public String getAuditLogsFormatted() {
        if (!currentSessionUser.isAdmin()) throw new SecurityException("Admin access required.");
        StringBuilder sb = new StringBuilder();
        for (Transaction t : logger.getAllLogs()) {
            sb.append(t.toString()).append("\n");
        }
        return sb.toString();
    }
}