package cbank;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private final String timestamp;
    private final String accountNumber;
    private final String type;
    private final double amountCad;
    private final String status;

    public Transaction(String accountNumber, String type, double amountCad, String status) {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.accountNumber = accountNumber;
        this.type = type;
        this.amountCad = amountCad;
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("[%s] Account: %s | Type: %s | Amount: $%.2f CAD | Status: %s",
                timestamp, accountNumber, type, amountCad, status);
    }
}