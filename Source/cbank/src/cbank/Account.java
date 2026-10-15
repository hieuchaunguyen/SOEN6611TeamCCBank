package cbank;

public class Account {
    private final String accountNumber;
    private final String accountName;
    private String pin;
    private double balanceCad;
    private final boolean isAdmin;

    public Account(String accountNumber, String accountName, String pin, double initialBalance, boolean isAdmin) {
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.pin = pin;
        this.balanceCad = initialBalance;
        this.isAdmin = isAdmin;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getAccountName() { return accountName; }
    public boolean validatePin(String inputPin) { return this.pin.equals(inputPin); }
    public void setPin(String newPin) { this.pin = newPin; }
    public double getBalanceCad() { return balanceCad; }
    public boolean isAdmin() { return isAdmin; }

    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit must be positive.");
        this.balanceCad += amount;
    }

    public void withdraw(double amount) throws CustomExceptions.InsufficientFundsException {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal must be positive.");
        if (amount > this.balanceCad) {
            throw new CustomExceptions.InsufficientFundsException("Insufficient funds in account.");
        }
        this.balanceCad -= amount;
    }
}
