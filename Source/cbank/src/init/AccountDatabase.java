package init;

import cbank.account.Account;
import cbank.account.AdminAccount;
import cbank.account.CommercialAccount;
import cbank.account.IndividualAccount;

import java.util.HashMap;
import java.util.Map;

public class AccountDatabase {

    public static final Map<String, Account> initialDatabase(){
        Map<String, Account> database = new HashMap<>();
        database.put("1001", new Account("1001", "Alice Smith", "1234", 1500.00, new CommercialAccount()));
        database.put("1002", new Account("1002", "Bob Jones", "5678", 500.00, new IndividualAccount()));
        // Admin account
        database.put("9999", new Account("9999", "System Admin", "0000", 0.0, new AdminAccount()));

        return database;
    }
}
