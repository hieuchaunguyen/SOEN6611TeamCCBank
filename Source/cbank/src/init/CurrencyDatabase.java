package init;

import java.util.HashMap;
import java.util.Map;

public class CurrencyDatabase {

    //Exchange Rate as in 2nd November 2026. Source: Bank of Canada
    public static final Map<String, Double> initializeDatabase(){
        Map<String,Double> database = new HashMap<>();
        database.put("CAD", 1.00);
        database.put("USD", 1.40);
        database.put("EUR", 1.57);
        database.put("GBP", 1.88);
        database.put("VND", 0.0000543);
        database.put("INR", 0.0147);
        return database;
    }
}
