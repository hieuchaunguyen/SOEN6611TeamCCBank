package cbank.account;

import cbank.CustomExceptions;

public abstract class AccountType {

    final double DEPOSIT_LIMIT;
    final double WITHDRAW_LIMIT;
    final String TYPE_NAME;

    double currentDepositLimit;
    double currentWithdrawLimit;

    AccountType(double dLimit, double wLimit, String name){
        DEPOSIT_LIMIT = dLimit;
        WITHDRAW_LIMIT = wLimit;
        TYPE_NAME = name;
        resetLimit();
    }

    public double checkDeposit(double amount) throws CustomExceptions.InvalidTransactionException{
        if(amount <= 0){
            throw new CustomExceptions.InvalidTransactionException("Deposit must be positive.");
        }
        else if(DEPOSIT_LIMIT >= 0.0 && amount > currentDepositLimit){
            throw new CustomExceptions.InvalidTransactionException("Amount exceeded your monthly deposit limit " + amount + " CAD.");
        }
        if(currentDepositLimit >= 0){
            currentDepositLimit -= amount;
        }
        return amount;
    }

    public double checkWithdraw(double amount)
            throws CustomExceptions.InvalidTransactionException{
        if(amount <= 0){
            throw new CustomExceptions.InvalidTransactionException("Withdrawal must be positive.");
        }
        if(WITHDRAW_LIMIT >= 0.0 && amount > currentWithdrawLimit){
            throw new CustomExceptions.InvalidTransactionException("Amount exceeded your monthly deposit limit " + amount + " CAD.");
        }
        else if(currentWithdrawLimit >= 0){
            currentWithdrawLimit -= amount;
        }
        return amount;
    }
    public void resetLimit(){
        currentDepositLimit = DEPOSIT_LIMIT;
        currentWithdrawLimit = WITHDRAW_LIMIT;
    }

    public String getAccountType(){
        return TYPE_NAME;
    }
}
