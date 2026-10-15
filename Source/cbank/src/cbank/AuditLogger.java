package cbank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AuditLogger {
    private final List<Transaction> logs = new ArrayList<>();

    public synchronized void log(String accountNumber, String type, double amountCad, String status) {
        logs.add(new Transaction(accountNumber, type, amountCad, status));
    }

    public synchronized List<Transaction> getAllLogs() {
        return Collections.unmodifiableList(new ArrayList<>(logs));
    }
}