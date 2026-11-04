package cbank;

import cbank.account.Account;
import init.AccountDatabase;

import java.util.HashMap;
import java.util.Map;

public class BankSystem {
    private static BankSystem instance;

    private final Map<String, Account> accounts;
    private final AuditLogger logger = new AuditLogger();
    private final CurrencyConverter converter = new CurrencyConverter();

    private double machineCashVaultCad = 5000.00; // Physical cash inside ABM
    private Account currentSessionUser = null;

    private BankSystem() {
        accounts = AccountDatabase.initialDatabase();
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


    public void withdrawCash(double amountCad)
            throws CustomExceptions.InsufficientFundsException,
            CustomExceptions.VaultDepletedException,
            CustomExceptions.InvalidTransactionException {

        if (amountCad > machineCashVaultCad) {
            logger.log(currentSessionUser.getAccountNumber(), "WITHDRAWAL", amountCad, "FAILED_VAULT_LIMIT");
            throw new CustomExceptions.VaultDepletedException("ABM has insufficient physical cash reserves.");
        }
        currentSessionUser.withdraw(amountCad);
        machineCashVaultCad -= amountCad;
        logger.log(currentSessionUser.getAccountNumber(), "WITHDRAWAL", amountCad, "SUCCESS");
    }

    public void depositCash(double amount, String currency) throws CustomExceptions.InvalidTransactionException {
        double convertedCad = converter.convertToCad(amount, currency);
        currentSessionUser.deposit(convertedCad);

        //Only CAD Cash are considered added to the ABM vault
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