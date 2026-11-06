package cbank.account;

import cbank.CustomExceptions;

public class Account {
    private final String accountNumber;
    private final String accountName;
    private String pin;
    private double balanceCad;
    private final AccountType accountType;

    public Account(String accountNumber, String accountName, String pin, double initialBalance, AccountType accountType) {
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.pin = pin;
        this.balanceCad = initialBalance;
        this.accountType = accountType;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getAccountName() { return accountName; }
    public boolean validatePin(String inputPin) { return this.pin.equals(inputPin); }
    public void setPin(String newPin) { this.pin = newPin; }
    public double getBalanceCad() { return balanceCad; }
    public boolean isAdmin() { return accountType instanceof AdminAccount; }

    public void deposit(double amount) throws CustomExceptions.InvalidTransactionException {
        this.balanceCad += accountType.checkDeposit(amount);
    }

    public void withdraw(double amount) throws CustomExceptions.InvalidTransactionException,
            CustomExceptions.InsufficientFundsException {
        if(amount > balanceCad){
            throw new CustomExceptions.InsufficientFundsException("Insufficient funds in account.");
        }
        this.balanceCad -= accountType.checkWithdraw(amount);
    }

    public double getDepositLimit() {
        return accountType.DEPOSIT_LIMIT;
    }

    public double getWithdrawLimit() {
        return accountType.WITHDRAW_LIMIT;
    }
}
